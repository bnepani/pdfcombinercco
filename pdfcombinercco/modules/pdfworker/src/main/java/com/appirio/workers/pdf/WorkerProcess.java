package com.appirio.workers.pdf;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

import org.apache.commons.lang3.SerializationUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.codehaus.jackson.map.ObjectMapper;

import com.appirio.PDFCombiner;
import com.appirio.PDFCombinerArguments;
import com.appirio.PDFCombinerFile;
import com.appirio.report.Reporter;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.QueueingConsumer;

/**
 * @author jesus
 *
 * This class is calls the pdf combiner and reporter and can be used as a Worker process
 * that listens for messages coming from CLOUDAMQP.
 */
public class WorkerProcess {
	private final static String QUEUE_NAME = "pdfcombiner";

	public static void main(String[] argv) throws Exception {
	    String uri = System.getenv("CLOUDAMQP_URL");
	    if (uri == null) uri = "amqp://guest:guest@localhost";

	    ConnectionFactory factory = new ConnectionFactory();
	    factory.setUri(uri);
	    Connection connection = factory.newConnection();
	    Channel channel = connection.createChannel();

	    channel.queueDeclare(QUEUE_NAME, false, false, false, null);
	    System.out.println(" [*] Waiting for messages");
	    //Logger.debug(" [*] Waiting for messages");

	    QueueingConsumer consumer = new QueueingConsumer(channel);
	    channel.basicConsume(QUEUE_NAME, true, consumer);
	    // main loop waits for queue messages
	    while (true) {
		    PDFCombinerArguments pdfCombinerArguments = null;
	    	try {
	    		// get a unique id
	    		String uniqueId = String.valueOf(Calendar.getInstance().getTimeInMillis());

		    	QueueingConsumer.Delivery delivery = consumer.nextDelivery();
		    	byte[] data = delivery.getBody();
		    	pdfCombinerArguments = (PDFCombinerArguments) SerializationUtils.deserialize(data);
	
		    	System.out.println(" [x] Message received");

		    	// download disclaimer
		    	String disclaimerXmlFilename = "/tmp/disclaimers.xml";
		    	downloadFile(pdfCombinerArguments.getDisclaimerUrl(), pdfCombinerArguments.getSessionId(), disclaimerXmlFilename);

		    	// download shipping instructions
		    	String shippingInstructionsXmlFilename = null;
		    	if(pdfCombinerArguments.isExportToPdf()) {
		    		shippingInstructionsXmlFilename = "/tmp/shippingInstructions" + uniqueId + ".xml";
		    		downloadFile(pdfCombinerArguments.getShippingInstructionsUrl(), pdfCombinerArguments.getSessionId(), shippingInstructionsXmlFilename);
		    	}
		    	
		    	//download package summary file
		    	String packageSummaryXmlFilename = "/tmp/packageSummary" + uniqueId + ".xml";
		    	downloadFile(pdfCombinerArguments.getPackageSummaryUrl(), pdfCombinerArguments.getSessionId(), packageSummaryXmlFilename);

		    	// download map panel order preferences
		    	String mapPanelOrderPrefXmlFilename = "/tmp/mapPanelOrderPref" + uniqueId + ".xml";
		    	downloadFile(pdfCombinerArguments.getMapPanelOrderPrefUrl(), pdfCombinerArguments.getSessionId(), mapPanelOrderPrefXmlFilename);

		    	boolean hasLocationMap = hasLocationMapAttachments(pdfCombinerArguments);
		    	// if has contents...
		    	if(pdfCombinerArguments.getContents() != null) {

		    		// iterate over contents
		    		Iterator<PDFCombinerFile> it = pdfCombinerArguments.getContents().iterator();

		    		// while iterator has elements
		    		while (it.hasNext()) {

		    			// get next file
		    			PDFCombinerFile file = it.next();

		    			// format file name
		    			String fileName = "/tmp/" + file.getPathOnClient();

		    			downloadFile(file.getSalesforceUrl(), pdfCombinerArguments.getSessionId(), fileName);
				    	if(fileName.endsWith(".xml")) {
				    		Reporter reporter = new Reporter(fileName, disclaimerXmlFilename, mapPanelOrderPrefXmlFilename, shippingInstructionsXmlFilename, packageSummaryXmlFilename, pdfCombinerArguments.isShowTotalProgramSummary(), pdfCombinerArguments.isShowIndividualMarketSummary(), pdfCombinerArguments.isShowIndividualFlightSummary(), file, pdfCombinerArguments.isExportToPdf(), pdfCombinerArguments.isExportToXls(),pdfCombinerArguments.getAppendixes());

				    		if(pdfCombinerArguments.isExportToXls()) {
				    	    	// upload file to s3
				    	    	// TODO: remove when is enabled
				    	    	//String downloadXlsFileUrl = uploadFileToAmazonS3(reporter.getGeneratedReport("xls"), "xls", pdfCombinerArguments);

				    			// upload to salesforce
						    	String result = uploadFileToSalesforce(reporter.getGeneratedReport("xls"), "xls", pdfCombinerArguments);

						    	// get the id
						    	ObjectMapper mapper = new ObjectMapper(); // can reuse, share globally
						    	Map<String, String> resultJson = mapper.readValue(result, Map.class);
						    	String xlsDocumentId = resultJson.get("id");
								System.out.println("   contentId: " + xlsDocumentId);

						    	// open callback url
						    	//openCallbackUrl(pdfCombinerArguments, xlsDocumentId, null);
								openCallbackUrl(pdfCombinerArguments, null, xlsDocumentId , "Completed" , "" );

				    	    	// open callback url
				    	    	//openCallbackUrl(pdfCombinerArguments, "Ok", "%urlXls%", downloadXlsFileUrl);

				    	    	// as this xls file is not to be combined using pdf combiner,
				    	    	// remove from the collection
								it.remove();
				    		} else {
					    		file.setFileName(reporter.getGeneratedReport("pdf"));

					    		// set custom toc frm report to combiner arguments
					    		pdfCombinerArguments.setPdfCombinerContentEntryList(reporter.getPDFCombinerContentEntryList());
				    		}
				    	} else {
				    		file.setFileName(fileName);
				    	}
			    	}
		    	}
	
		    	// if it has appendixes
		    	if(pdfCombinerArguments.getAppendixes() != null) {
		    		List<PDFCombinerFile> appendixes = new ArrayList<PDFCombinerFile>();
			    	for(PDFCombinerFile file : pdfCombinerArguments.getAppendixes()) {
				    	String fileName = "/tmp/" + file.getPathOnClient();
				    	//downloadFile(file.getSalesforceUrl(), pdfCombinerArguments.getSessionId(), fileName);
				    	if(downloadFile(file.getSalesforceUrl(), pdfCombinerArguments.getSessionId(), fileName)){
				    		appendixes.add(file);
				    	}
				    	file.setFileName(fileName);
			    	}
			    	System.out.println("   appendix list size" + pdfCombinerArguments.getAppendixes().size());
			    	System.out.println("   resultant appendix list size" + appendixes.size());
			    	pdfCombinerArguments.setAppendixes(appendixes);			    	
		    	}
	
		    	// TODO: it will be necessary to export pdf and xls at the same time
		    	if(pdfCombinerArguments.isExportToPdf()) {
			    	// instantiate pdfcombiner and get pdf filename (ephimeral filesystem path)
			    	System.out.println("   combining content");
			    	PDFCombiner combiner = new PDFCombiner();
			    	String pdfFile = combiner.combine(pdfCombinerArguments);
		
			    	// upload file to s3
			    	// TODO: remove when is enabled
			    	//String downloadFileUrl = uploadFileToAmazonS3(pdfFile, "pdf", pdfCombinerArguments);
		
			    	// upload pdf to storage service (to avoid ephimeral file being removed)
			    	// TODO: disabled because it is giving error number 500
			    	String result = uploadFileToSalesforce(pdfFile, "pdf", pdfCombinerArguments);

			    	// get the id
			    	ObjectMapper mapper = new ObjectMapper(); // can reuse, share globally
			    	Map readValue = mapper.readValue(result, Map.class);
					Map<String, String> resultJson = readValue;
			    	String pdfDocumentId = resultJson.get("id");
					System.out.println("   contentId: " + pdfDocumentId);

			    	// open callback url
			    	openCallbackUrl(pdfCombinerArguments, pdfDocumentId, null, "Completed" , "");
		    	}
	
		    	// send file link by email
		    	//Logger.debug("   sending email message");
		    	//sendMail(pdfFile, "Test message", "apps2biz@gmail.com", pdfCombinerArguments.getRequesterUserEmailAddress());
	
		    	// debug info
		    	//Logger.debug("Finishing");
		    	//System.out.println("     pdfFile '" + pdfFile + "'");
	    	} catch (Exception ex) {
	    		System.out.println("Exception in WorkerProcess");
	    		ex.printStackTrace();
				openCallbackUrl(pdfCombinerArguments, null, null, "Failed", ex.getMessage());
	    	}
	    }
	  }


	private static boolean hasLocationMapAttachments(PDFCombinerArguments pdfCombinerArguments) {
		boolean locationMapExists = false;
		// if it has appendixes
    	if(pdfCombinerArguments.getAppendixes() != null) {
    		
	    	for(PDFCombinerFile file : pdfCombinerArguments.getAppendixes()) {
				System.out.println("   appendix attachment url " + file.getSalesforceUrl() );
				if(file.getSalesforceUrl() != null) {
					if(file.getSalesforceUrl().toLowerCase().contains("sobjects/attachment")) {
						locationMapExists = true;
						break;
					}
				}
	    	}
    	}
    	return locationMapExists;
		
	}
	
	private static void openCallbackUrl(PDFCombinerArguments pdfCombinerArguments, String pdfOutputUrl, String excelOutputUrl,
	String status, String message) throws ClientProtocolException, IOException {
		String url = pdfCombinerArguments.getPdfCombinerCallback().getCallbackUrl();

    	System.out.println("openCallbackUrl(): begin");

		HttpClient httpclient = new DefaultHttpClient();
		HttpPost httpreq = new HttpPost(url);
		httpreq.setHeader("Authorization", "Bearer " + pdfCombinerArguments.getSessionId());
		httpreq.setHeader("Content-Type", "application/json");

		String jsonSend = pdfCombinerArguments.getPdfCombinerCallback().getCallbackContents();
		if(StringUtils.isNotEmpty(pdfOutputUrl)) {
			jsonSend = jsonSend.replace("%PDFOutput%", pdfOutputUrl);
		}
		if(StringUtils.isNotEmpty(excelOutputUrl)) {
			jsonSend = jsonSend.replace("%ExcelOutput%", excelOutputUrl);
		}
		jsonSend = jsonSend.replace("%Status%", status);
		jsonSend = jsonSend.replace("%Message%", message);

		StringEntity myEntity = new StringEntity(jsonSend); 

		httpreq.setEntity(myEntity);
		HttpResponse response = httpclient.execute(httpreq);
		// consume response
		//HttpEntity resEntity = response.getEntity();
		if(response.getStatusLine().getStatusCode() > 204) {
			// could not notify callback url
			// TODO: Send email message using an alternate system to salesforce?
		}

    	System.out.println("openCallbackUrl(): end");
	}

	public static String uploadFileToSalesforce(String sourceFileName,
			String fileExtension, PDFCombinerArguments pdfCombinerArguments)
			throws MalformedURLException, IOException {

		System.out.println("uploadFileToSalesforce(): begin");

		FileUploader fileUpload = new FileUploader();
		File file = new File(sourceFileName);

    	String resultingFileName = pdfCombinerArguments.getOutputFileName() + "." + fileExtension;

		HashMap<String, String> headerHashMap = new HashMap<String, String>();
		headerHashMap.put("Authorization", "Bearer " + pdfCombinerArguments.getSessionId());
		headerHashMap.put("Accept", "application/json");

		//creeate jsonContent
		String jsonContent = 
				"{" + 
				"	\"Title\" : \"" + pdfCombinerArguments.getTitle() + "\"," +
				"	\"Description\" : \"" + pdfCombinerArguments.getSubTitle() + "\"," +
				"	\"FirstPublishLocationId\" : \"" + pdfCombinerArguments.getContentDocumentId() + "\"," +
				"	\"ReasonForChange\" : \"" + pdfCombinerArguments.getTitle() + "\"," +
				"	\"PathOnClient\" : \"" + resultingFileName + "\"" +
				"}";

		String response = fileUpload.executeMultiPartRequest(
				pdfCombinerArguments.getInsertContentVersionUrl(),
				file,
				jsonContent,
				headerHashMap);

		System.out.println("   response: " + response);

		return response;
	}

	private static boolean downloadFile(String url, String sessionId, String outputFileName) {
		System.out.println("downloadFile(): begin");
		System.out.println("   url           : " + url);
		System.out.println("   sessionId     : " + sessionId);
		System.out.println("   outputFileName: " + outputFileName);
		System.out.println("   curl command  : curl -H \"Authorization: Bearer " + sessionId + "\" -H \"Accept: application/xml\"" + url);
		boolean isSuccess = false;
		int size=1024;
		InputStream is = null;
		OutputStream outStream = null;
		URLConnection  uCon = null;
		try {
			URL Url;
			byte[] buf;
			int ByteRead,ByteWritten=0;
			Url= new URL(url);
			outStream = new BufferedOutputStream(new FileOutputStream(outputFileName));

			uCon = Url.openConnection();
			uCon.setRequestProperty("Authorization", "Bearer " + sessionId);
			uCon.setRequestProperty("Accept", "application/xml, application/pdf");
			is = uCon.getInputStream();
			buf = new byte[size];
			while ((ByteRead = is.read(buf)) != -1) {
				outStream.write(buf, 0, ByteRead);
				ByteWritten += ByteRead;
			}
			System.out.println("   Downloaded Successfully.");
			System.out.println("   File name:\""+outputFileName+ "\"\nNo ofbytes :" + ByteWritten);
			isSuccess = true;
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				is.close();
				outStream.close();
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
		return isSuccess;
	}

	
}
