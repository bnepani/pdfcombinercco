package controllers;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.codec.binary.Base64InputStream;
import org.apache.commons.lang3.SerializationUtils;
import org.codehaus.jackson.JsonNode;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import play.Logger;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Http.MultipartFormData;
import play.mvc.Http.MultipartFormData.FilePart;
import play.mvc.Result;

import com.appirio.PDFCombinerArguments;
import com.appirio.PDFCombinerFile;
import com.appirio.report.Reporter;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

public class Application extends Controller {

	private static final String CLOUDAMQP_URL = System.getenv("CLOUDAMQP_URL");

	private static final String GENERATED_PDFS_DIR = System.getenv("GENERATED_PDFS_DIR");

	
	public static Result index() {
		return ok(views.html.index.render());
	}

	/**
	 * Stream PDF File
	 * @throws Exception
	 */
	public static Result getPdf(String pdfFile) throws Exception {
		// stream generated PDF content
		pdfFile = System.getenv("GENERATED_PDFS_DIR") + File.separator + pdfFile;

		return ok(new File(pdfFile));
	}

	private final static String QUEUE_NAME = "pdfcombiner";

	/**
	 * Handles uploading of files. Launch PDF combiner.
	 * @return Url to the generated PDF file
	 * @throws Exception
	 */
	public static Result upload() throws Exception {

		// create connection
	    ConnectionFactory factory = new ConnectionFactory();
	    factory.setUri(CLOUDAMQP_URL);
	    Connection connection = factory.newConnection();
	    Channel channel = connection.createChannel();

	    // get json and fill pdfCombinerArguments object
		JsonNode json = request().body().asJson();
		PDFCombinerArguments pdfCombinerArguments = Json.fromJson(json, PDFCombinerArguments.class);

		// broadcast message containing pdfCombinerArguments
	    byte[] data = SerializationUtils.serialize(pdfCombinerArguments);
	    channel.queueDeclare(QUEUE_NAME, false, false, false, null);
	    channel.basicPublish("", QUEUE_NAME, null, data);

	    // debug info
	    System.out.println(" title: " + pdfCombinerArguments.getTitle());
	    System.out.println(" subTitle: " + pdfCombinerArguments.getSubTitle());
	    System.out.println(" email: " + pdfCombinerArguments.getEmail());
	    System.out.println(" [x] Message sent to queue");

	    channel.close();
	    connection.close();

		return redirect(routes.Application.index());
	}

	/**
	 * Handles the report preview generation. Stream the generated file.
	 * @return stream of generated file
	 * @throws Exception
	 */
	public static Result outputPreview() throws Exception {

        System.out.println("outputPreview(): begin");

	    // get uploaded json and fill pdfCombinerArguments object
		JsonNode json = request().body().asJson();
		PDFCombinerArguments pdfCombinerArguments = Json.fromJson(json, PDFCombinerArguments.class);

		// get first contents as pdf combiner file
		PDFCombinerFile file = pdfCombinerArguments.getContents().get(0);

		// set filename for this datasource
    	String fileName = "/tmp/" + file.getPathOnClient();

    	// download datasource
    	generateEmptyXml(file, fileName);

    	// download disclaimer  
    	String disclaimerXmlFilename = "/tmp/disclaimers.xml";
    	downloadFile(pdfCombinerArguments.getDisclaimerUrl(), pdfCombinerArguments.getSessionId(), disclaimerXmlFilename);

    	// download map panel order preferences
    	String mapPanelOrderPrefXmlFilename = "/tmp/mapPanelOrderPref" + pdfCombinerArguments.getSessionId() + ".xml";
    	downloadFile(pdfCombinerArguments.getMapPanelOrderPrefUrl(), pdfCombinerArguments.getSessionId(), mapPanelOrderPrefXmlFilename);
    	
    	//download package summary file
    	String packageSummaryXmlFilename = "/tmp/packageSummary" + pdfCombinerArguments.getSessionId() + ".xml";
    	downloadFile(pdfCombinerArguments.getPackageSummaryUrl(), pdfCombinerArguments.getSessionId(), packageSummaryXmlFilename);
 
    	// process report
    	Reporter reporter = new Reporter(fileName, disclaimerXmlFilename, mapPanelOrderPrefXmlFilename, null, packageSummaryXmlFilename, pdfCombinerArguments.isShowTotalProgramSummary(), pdfCombinerArguments.isShowIndividualMarketSummary(), pdfCombinerArguments.isShowIndividualFlightSummary(), file, pdfCombinerArguments.isExportToPdf(), pdfCombinerArguments.isExportToXls(),
    			null);

		// prepare response
    	String fileExtension = pdfCombinerArguments.isExportToXls() ? "xls" : "pdf";

    	// get generated report file
    	String fileDownloadParameter = reporter.getUniqueId() + File.separator + reporter.getUniqueId() + "." + fileExtension;
    	System.out.println("   fileDownloadParameter: " + fileDownloadParameter);

		// return url to pdf
		String baseUrl = routes.Application.index().absoluteURL(request());
		if(baseUrl.endsWith("/")) {
			baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
		}

		// download url
		String downloadUrl = baseUrl + controllers.routes.Application.getPdf(fileDownloadParameter).url();
		System.out.println("   downloadUrl: " + downloadUrl);
        System.out.println("outputPreview(): end");

        return ok(downloadUrl);
	}

	public static Result getReportFile(String fileName) throws Exception {
		// stream generated PDF content
		fileName = System.getenv("GENERATED_PDFS_DIR") + File.separator + fileName + File.separator + fileName + ".pdf";

		return ok(new File(fileName));
	}

	public static Result showSystemStatus() {
		return ok("System Status...");
	}

	/**
	 * Decodes a base64 encoded file to binary.
	 * @param base64FileName
	 * @param newFileName
	 * @throws Exception
	 */
	public static void decodeBase64File(String base64FileName, String newFileName) throws Exception {
		int BUFFER_SIZE = 4096;
		byte[] buffer = new byte[BUFFER_SIZE];
		Logger.info("   open input stream");
		InputStream input = new Base64InputStream(new FileInputStream(base64FileName));
		Logger.info("   open output stream");
		OutputStream output = new FileOutputStream(newFileName);
		int n = input.read(buffer, 0, BUFFER_SIZE);
		while (n >= 0) {
			Logger.info("   reading");
			output.write(buffer, 0, n);
			n = input.read(buffer, 0, BUFFER_SIZE);
		}
		Logger.info("   closing streams");
		input.close();
		output.close();
	}

	private static void downloadFile(String url, String sessionId, String outputFileName) {
		System.out.println("downloadFile(): begin");
		System.out.println("   url           : " + url);
		System.out.println("   sessionId     : " + sessionId);
		System.out.println("   outputFileName: " + outputFileName);
		System.out.println("   curl command  : curl -H \"Authorization: Bearer " + sessionId + "\" -H \"Accept: application/xml\"" + url);

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
	        uCon.setRequestProperty("Accept", "application/xml");
	        is = uCon.getInputStream();
	        buf = new byte[size];
	        while ((ByteRead = is.read(buf)) != -1) {
	            outStream.write(buf, 0, ByteRead);
	            ByteWritten += ByteRead;
	        }
	        System.out.println("   Downloaded Successfully.");
	        System.out.println("   File name:\""+outputFileName+ "\"\nNo ofbytes :" + ByteWritten);
	    }catch (Exception e) {
	        e.printStackTrace();
	        }
	    finally {
	            try {
	            is.close();
	            outStream.close();
	            }
	            catch (IOException e) {
	        e.printStackTrace();
	            }
	        }
	}

	/**
	 * Generate an empty xml that will serve as dataset for report preview
	 */
	public static void generateEmptyXml(PDFCombinerFile pdfCombinerFile, String outputFileName) {
		try {

			DocumentBuilderFactory docFactory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

			// root elements
			Document doc = docBuilder.newDocument();
			Element rootElement = doc.createElement("QueryResult");
			doc.appendChild(rootElement);

			// staff elements
			Element staff = doc.createElement("records");
			rootElement.appendChild(staff);

			// split fields
			String[] fieldNamesArray = pdfCombinerFile.getFieldNamesPipeDelimited().get(0).split("\\|");

			// for each field name
			for(Integer fieldIndex = 0; fieldIndex < fieldNamesArray.length; fieldIndex++) {
				// add element
				Element recordElement = doc.createElement(fieldNamesArray[fieldIndex]);
				staff.appendChild(recordElement);
			}

            /*
                <Package_Flight__r>
                    <Id>1</Id>
                    <Type__c>Audience</Type__c>
                    <Campaign_Start_Date__c>2013-01-01</Campaign_Start_Date__c>
                    <Campaign_End_Date__c>2013-01-01</Campaign_End_Date__c>
                </Package_Flight__r>
            */
            // add empty flight record
            Element flightElement = doc.createElement("Package_Flight__r");
			staff.appendChild(flightElement);
            
			Element idElement = doc.createElement("Id");
            idElement.setTextContent("1");
			flightElement.appendChild(idElement);
            
			Element typeElement = doc.createElement("Type__c");
            typeElement.setTextContent("Audience");
			flightElement.appendChild(typeElement);

			Element campaignStartDateElement = doc.createElement("Campaign_Start_Date__c");
			campaignStartDateElement.setTextContent("2013-01-01");
			flightElement.appendChild(campaignStartDateElement);

			Element campaignEndDateElement = doc.createElement("Campaign_End_Date__c");
			campaignEndDateElement.setTextContent("2013-12-31");
			flightElement.appendChild(campaignEndDateElement);

			// write the content into xml file
			TransformerFactory transformerFactory = TransformerFactory
					.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			DOMSource source = new DOMSource(doc);
			StreamResult result = new StreamResult(new File(outputFileName));

			// Output to console for testing
			//StreamResult result = new StreamResult(System.out);

			transformer.transform(source, result);

			System.out.println("File saved!");

		} catch (ParserConfigurationException pce) {
			pce.printStackTrace();
		} catch (TransformerException tfe) {
			tfe.printStackTrace();
		}
	}
}