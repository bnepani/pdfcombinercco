package com.appirio.test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.appirio.PDFCombiner;
import com.appirio.PDFCombinerArguments;
import com.appirio.PDFCombinerContentEntry;
import com.appirio.PDFCombinerFile;
import com.itextpdf.text.DocumentException;

/**
 * @author jesus
 *
 * Tests of pdf generation process.
 */
public class PDFCombinerTest {

	PDFCombiner pdfCombiner;
	public PDFCombiner getPdfCombiner() {
		return pdfCombiner;
	}

	public void setPdfCombiner(PDFCombiner pdfCombiner) {
		this.pdfCombiner = pdfCombiner;
	}

	@Before
	public void setUp() throws Exception {
		PDFCombiner pdfCombiner = new PDFCombiner();
		setPdfCombiner(pdfCombiner);
	}

	@After
	public void tearDown() throws Exception {
		// remove generated dir
		//getPdfCombiner().deleteGeneratedFiles();
	}

	@Test
	public void getGeneratedPDFDirTest() throws Exception {
		Assert.assertTrue(getPdfCombiner().getGeneratedPDFDir().startsWith(System.getenv("GENERATED_PDFS_DIR")));
	}

//	@Test
//	public void downloadTest() {
//		String url = "https://c.na14.visual.force.com/services/data/v27.0/sobjects/Attachment/00Pd0000003XL1mEAG/Body";
//		String outputFileName = "d:/tmp/kk.ppt";
//		String sessionId = "00Dd0000000fPFX!AQYAQCqqBctHuauW268OAUofr0J9LOhYN9I5j7sIgUsW66kUoGiZmXbwpEWbo2UJM4Veho6bvianPTh0.Ne0pXMmeBFa2dHy";
//
//	    int size=1024;
//	    InputStream is = null;
//	    OutputStream outStream = null;
//	    URLConnection  uCon = null;
//	    try {
//	        URL Url;
//	        byte[] buf;
//	        int ByteRead,ByteWritten=0;
//	        Url= new URL(url);
//	        outStream = new BufferedOutputStream(new FileOutputStream(outputFileName));
//
//	        uCon = Url.openConnection();
//	        uCon.setRequestProperty("Authorization", "Bearer " + sessionId);
//	        is = uCon.getInputStream();
//	        buf = new byte[size];
//	        while ((ByteRead = is.read(buf)) != -1) {
//	            outStream.write(buf, 0, ByteRead);
//	            ByteWritten += ByteRead;
//	        }
//	        System.out.println("   Downloaded Successfully.");
//	        System.out.println("   File name:\""+outputFileName+ "\"\nNo ofbytes :" + ByteWritten);
//	    }catch (Exception e) {
//	        e.printStackTrace();
//	        }
//	    finally {
//	            try {
//	            is.close();
//	            outStream.close();
//	            }
//	            catch (IOException e) {
//	        e.printStackTrace();
//	            }
//	        }
//
//	}
//	
//	@Test
//	public void downloadFileTest() throws IOException {
//		String attachmentsUrl = "https://c.na14.visual.force.com/services/data/v27.0/sobjects/Attachment/";
//		String sessionId = "00Dd0000000fPFX!AQYAQBM92Js1i7UYzg_41Wvzcnc0ZL08eu7qb4i628mTLD_lPy11pWOJsYqVeWofBlfjGjlvo3A1paXnsyAPZq2NO3CkJQLz";
//		String id = "00Pd0000003XL1mEAG";
//
//		String attachmentUrl = attachmentsUrl + id + "/Body";
//
//        URLConnection connection = new URL(attachmentUrl).openConnection();
//        connection.setDoOutput(true); // Triggers POST
//        connection.setRequestProperty("Authorization", "Bearer " + sessionId);
//        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
//        String inputLine;
//        while ((inputLine = in.readLine()) != null) 
//            System.out.println(inputLine);
//        in.close();
//	}

//	@Test(expected = FileNotFoundException.class)
//	public void testCombineFileNotFoundException() throws Exception, DocumentException, IOException {
//		PDFCombinerArguments pdfCombinerArguments = new PDFCombinerArguments();
//		pdfCombinerArguments.setTitle("Proposal test");
//		pdfCombinerArguments.setSubTitle("Subtitle 12344....");
//		pdfCombinerArguments.getContentFileNamesList().add("Data\\logonotfound.png");
//		pdfCombinerArguments.getContentTitlesList().add("Surf");
//		pdfCombinerArguments.getContentDescriptionsList().add("Surf is a water sport");
//		pdfCombinerArguments.getAppendixTitlesList().add("Appendix 1 and only");
//		pdfCombinerArguments.setClientCompanyName("Test name\nTest other data");
//		pdfCombinerArguments.setClientContactInformation("Another information from a client contact");
//		pdfCombinerArguments.setAgencyName("agencyName");
//		pdfCombinerArguments.setAgencyContactInformation("agencyContactInformation");
//		pdfCombinerArguments.setMarketName("marketName");
//		pdfCombinerArguments.setMarketContactInformation("marketContactInformation");
//		getPdfCombiner().combine(pdfCombinerArguments);
//	}
//

	@Test
	public void testCombineZeroContentsZeroAppendixes() throws Exception, DocumentException, IOException {
		PDFCombinerArguments pdfCombinerArguments = new PDFCombinerArguments();
		pdfCombinerArguments.setTitle("Proposal test");
		pdfCombinerArguments.setContents(null);
		pdfCombinerArguments.setAppendixes(null);
		Assert.assertNull(getPdfCombiner().combine(pdfCombinerArguments));
	}

	@Test
	public void testCombineOneContentsZeroAppendixes() throws Exception, DocumentException, IOException {
		PDFCombinerArguments pdfCombinerArguments = new PDFCombinerArguments();
		pdfCombinerArguments.setTitle("Proposal test");
		pdfCombinerArguments.setSubTitle("Subtitle 12344....");
		pdfCombinerArguments.setContents(new ArrayList<PDFCombinerFile>());
		pdfCombinerArguments.getContents().add(new PDFCombinerFile("Data\\Sample Powerpoint 3 Slides.pdf", null, "Ppt to pdf", "Powerpoint converted to PDF", true));
		pdfCombinerArguments.setClientCompanyName("Test name\nTest other data");
		pdfCombinerArguments.setClientContactInformation("Another information from a client contact");
		pdfCombinerArguments.setAgencyName("agencyName");
		pdfCombinerArguments.setAgencyContactInformation("agencyContactInformation");
		pdfCombinerArguments.setMarketName("marketName");
		pdfCombinerArguments.setMarketContactInformation("marketContactInformation");
		pdfCombinerArguments.setAppendixes(null);
		pdfCombinerArguments.setShowTableOfContents(true);

		// set custom TOC
		List<PDFCombinerContentEntry> pdfCombinerContentEntryList = new ArrayList<PDFCombinerContentEntry>();
		PDFCombinerContentEntry pdfCombinerContentEntry;

		pdfCombinerContentEntry = new PDFCombinerContentEntry();
		pdfCombinerContentEntry.setTitle("Package name A");
		pdfCombinerContentEntry.setDescription("Package name A Location List");
		pdfCombinerContentEntry.setPageNumber(5);
		pdfCombinerContentEntryList.add(pdfCombinerContentEntry);

		pdfCombinerContentEntry = new PDFCombinerContentEntry();
		pdfCombinerContentEntry.setTitle("Package name B");
		pdfCombinerContentEntry.setDescription("Package name B Location List");
		pdfCombinerContentEntry.setPageNumber(18);
		pdfCombinerContentEntryList.add(pdfCombinerContentEntry);

		pdfCombinerContentEntry = new PDFCombinerContentEntry();
		pdfCombinerContentEntry.setTitle("Package name C");
		pdfCombinerContentEntry.setDescription("Package name C Location List");
		pdfCombinerContentEntry.setPageNumber(33);
		pdfCombinerContentEntryList.add(pdfCombinerContentEntry);

		// set toc
		pdfCombinerArguments.setPdfCombinerContentEntryList(pdfCombinerContentEntryList);

		// combine
		String generatedPDF = getPdfCombiner().combine(pdfCombinerArguments);
		Assert.assertTrue(new File(generatedPDF).exists());
	}

	@Test
	public void testLargePageNumbering() throws Exception, DocumentException, IOException {

		PDFCombinerArguments pdfCombinerArguments = new PDFCombinerArguments();
		pdfCombinerArguments.setTitle("Proposal test");
		pdfCombinerArguments.setSubTitle("Subtitle 12344....");
		pdfCombinerArguments.setContents(new ArrayList<PDFCombinerFile>());
		pdfCombinerArguments.setShowPageNumbering(true);
		pdfCombinerArguments.setShowTableOfContents(true);

		PDFCombinerFile pdfCombinerFile = new PDFCombinerFile("Data\\reporter output 1369382044397.pdf", null, "TODO: title", "TODO: subtitle", false);
		//pdfCombinerFile.setTableOfContentsRawText(tableOfContentsRawText);
		pdfCombinerArguments.getContents().add(pdfCombinerFile);

		pdfCombinerArguments.setAppendixes(new ArrayList<PDFCombinerFile>());

		// ================
		// appendixes
		// ================
		PDFCombinerFile visibleAppendixPDFCombinerFile1 = new PDFCombinerFile("Data\\logo.png", null, "Logo 1 A (visible)", "Logo 1", false);
		visibleAppendixPDFCombinerFile1.setShowAppendixEntry(true);
		pdfCombinerArguments.getAppendixes().add(visibleAppendixPDFCombinerFile1);

		// hidden appendix
		PDFCombinerFile hiddenAppendixPDFCombinerFile = new PDFCombinerFile("Data\\logo.png", null, "Logo 1 (hidden)", "Logo 1 (hidden)", false);
		hiddenAppendixPDFCombinerFile.setShowAppendixEntry(false);
		pdfCombinerArguments.getAppendixes().add(hiddenAppendixPDFCombinerFile);

		// visible appendix
		PDFCombinerFile visibleAppendixPDFCombinerFile2 = new PDFCombinerFile("Data\\logo.png", null, "Logo 1 B (visible)", "Logo 1 B", false);
		visibleAppendixPDFCombinerFile2.setShowAppendixEntry(true);
		pdfCombinerArguments.getAppendixes().add(visibleAppendixPDFCombinerFile2);

		// data
		pdfCombinerArguments.setClientCompanyName("Test name\nTest other data");
		pdfCombinerArguments.setClientContactInformation("Another information from a client contact");
		pdfCombinerArguments.setAgencyName("agencyName");
		pdfCombinerArguments.setAgencyContactInformation("agencyContactInformation");
		pdfCombinerArguments.setMarketName("marketName");
		pdfCombinerArguments.setMarketContactInformation("marketContactInformation");
		
		// set custom TOC
		List<PDFCombinerContentEntry> pdfCombinerContentEntryList = new ArrayList<PDFCombinerContentEntry>();
		PDFCombinerContentEntry pdfCombinerContentEntry;

		pdfCombinerContentEntry = new PDFCombinerContentEntry();
		pdfCombinerContentEntry.setTitle("Package name A");
		pdfCombinerContentEntry.setDescription("Package name A Location List");
		pdfCombinerContentEntry.setPageNumber(5);
		pdfCombinerContentEntryList.add(pdfCombinerContentEntry);

		pdfCombinerContentEntry = new PDFCombinerContentEntry();
		pdfCombinerContentEntry.setTitle("Package name B");
		pdfCombinerContentEntry.setDescription("Package name B Location List");
		pdfCombinerContentEntry.setPageNumber(18);
		pdfCombinerContentEntryList.add(pdfCombinerContentEntry);

		pdfCombinerContentEntry = new PDFCombinerContentEntry();
		pdfCombinerContentEntry.setTitle("Package name C");
		pdfCombinerContentEntry.setDescription("Package name C Location List");
		pdfCombinerContentEntry.setPageNumber(33);
		pdfCombinerContentEntryList.add(pdfCombinerContentEntry);

		// set toc
		pdfCombinerArguments.setPdfCombinerContentEntryList(pdfCombinerContentEntryList);

		// combine
		String generatedPDF = getPdfCombiner().combine(pdfCombinerArguments);
		Assert.assertTrue(new File(generatedPDF).exists());
	}

	@Test
	public void testCombineAppendixes() throws Exception, DocumentException, IOException {

		PDFCombinerArguments pdfCombinerArguments = new PDFCombinerArguments();
		pdfCombinerArguments.setTitle("Proposal test");
		pdfCombinerArguments.setSubTitle("Subtitle 12344....");
		pdfCombinerArguments.setContents(new ArrayList<PDFCombinerFile>());

		PDFCombinerFile pdfCombinerFile = new PDFCombinerFile("Data\\Sample Powerpoint 3 Slides.pdf", null, "Ppt to pdf", "Powerpoint converted to PDF", false);
		pdfCombinerArguments.getContents().add(pdfCombinerFile);

		pdfCombinerArguments.setAppendixes(new ArrayList<PDFCombinerFile>());
		pdfCombinerArguments.getAppendixes().add(new PDFCombinerFile("Data\\PDFCombiner sample document.pdf", null, "pdf title", "pdf file description", false));
		pdfCombinerArguments.getAppendixes().add(new PDFCombinerFile("Data\\logo.png", null, "Logo 1", "Logo 1", false));
		pdfCombinerArguments.setShowTableOfContents(true);
		pdfCombinerArguments.setClientCompanyName("Test name\nTest other data");
		pdfCombinerArguments.setClientContactInformation("Another information from a client contact");
		pdfCombinerArguments.setAgencyName("agencyName");
		pdfCombinerArguments.setAgencyContactInformation("agencyContactInformation");
		pdfCombinerArguments.setMarketName("marketName");
		pdfCombinerArguments.setMarketContactInformation("marketContactInformation");
		String generatedPDF = getPdfCombiner().combine(pdfCombinerArguments);
		Assert.assertTrue(new File(generatedPDF).exists());
	}

	@Test
	public void testCombineImageDontShowDocumentProperties() throws Exception, DocumentException, IOException {
		PDFCombinerArguments pdfCombinerArguments = new PDFCombinerArguments();
		pdfCombinerArguments.setShowPageNumbering(false);
		pdfCombinerArguments.setShowTableOfContents(false);
		pdfCombinerArguments.setShowTimeAndDateStamp(false);
		pdfCombinerArguments.setTitle("Proposal test");
		pdfCombinerArguments.setSubTitle("Subtitle 12344....");
		pdfCombinerArguments.setContents(new ArrayList<PDFCombinerFile>());
		pdfCombinerArguments.getContents().add(new PDFCombinerFile("Data\\surf.jpg", null, "Surf", "Surf is a water sport", false));
		pdfCombinerArguments.getContents().add(new PDFCombinerFile("Data\\logo.png", null, "Logo 1", "Logo 1", false));
		pdfCombinerArguments.setClientCompanyName("Test name\nTest other data");
		pdfCombinerArguments.setClientContactInformation("Another information from a client contact");
		pdfCombinerArguments.setAgencyName("agencyName");
		pdfCombinerArguments.setAgencyContactInformation("agencyContactInformation");
		pdfCombinerArguments.setMarketName("marketName");
		pdfCombinerArguments.setMarketContactInformation("marketContactInformation");
		String generatedPDF = getPdfCombiner().combine(pdfCombinerArguments);
		Assert.assertTrue(new File(generatedPDF).exists());
	}

	@Test
	public void testCombineImages() throws Exception, DocumentException, IOException {

		PDFCombinerArguments pdfCombinerArguments = new PDFCombinerArguments();
		pdfCombinerArguments.setTitle("Proposal test");
		pdfCombinerArguments.setSubTitle("Subtitle 12344....");
		pdfCombinerArguments.setContents(new ArrayList<PDFCombinerFile>());
		pdfCombinerArguments.getContents().add(new PDFCombinerFile("Data\\surf.jpg", null, "Surf", "Surf is a water sport", false));
		pdfCombinerArguments.getContents().add(new PDFCombinerFile("Data\\logo.png", null, "Logo 1", "Logo 1", false));
		pdfCombinerArguments.getContents().add(new PDFCombinerFile("Data\\logo.png", null, "Logo 1 b", "Logo 1 B description", false));
		pdfCombinerArguments.setClientCompanyName("Test name\nTest other data");
		pdfCombinerArguments.setClientContactInformation("Another information from a client contact");
		pdfCombinerArguments.setAgencyName("agencyName");
		pdfCombinerArguments.setAgencyContactInformation("agencyContactInformation");
		pdfCombinerArguments.setMarketName("marketName");
		pdfCombinerArguments.setMarketContactInformation("marketContactInformation");
		String generatedPDF = getPdfCombiner().combine(pdfCombinerArguments);
		Assert.assertTrue(new File(generatedPDF).exists());
	}

	@Test
	public void testCombineImagesAndPdf() throws Exception, DocumentException, IOException {
		PDFCombinerArguments pdfCombinerArguments = new PDFCombinerArguments();
		pdfCombinerArguments.setTitle("Proposal test");
		pdfCombinerArguments.setSubTitle("Subtitle 12344....");
		pdfCombinerArguments.setContents(new ArrayList<PDFCombinerFile>());
		pdfCombinerArguments.getContents().add(new PDFCombinerFile("Data\\surf.jpg", null, "Surf", "Surf is a water sport", false));
		pdfCombinerArguments.getContents().add(new PDFCombinerFile("Data\\logo.png", null, "Logo 1", "Logo 1", false));
		pdfCombinerArguments.getContents().add(new PDFCombinerFile("Data\\PDFCombiner sample document.pdf", null, "pdf title", "pdf file description", false));
		pdfCombinerArguments.setClientCompanyName("Test name\nTest other data");
		pdfCombinerArguments.setClientContactInformation("Another information from a client contact");
		pdfCombinerArguments.setAgencyName("agencyName");
		pdfCombinerArguments.setAgencyContactInformation("agencyContactInformation");
		pdfCombinerArguments.setMarketName("marketName");
		pdfCombinerArguments.setMarketContactInformation("marketContactInformation");

		String generatedPDF = getPdfCombiner().combine(pdfCombinerArguments);
		Assert.assertTrue(new File(generatedPDF).exists());
	}

	@Test
	public void testCombineImagesAndDoc() throws Exception, DocumentException, IOException {
		PDFCombinerArguments pdfCombinerArguments = new PDFCombinerArguments();
		pdfCombinerArguments.setTitle("Proposal test");
		pdfCombinerArguments.setSubTitle("Subtitle 12344....");
		pdfCombinerArguments.setContents(new ArrayList<PDFCombinerFile>());
		pdfCombinerArguments.getContents().add(new PDFCombinerFile("Data\\logo.png", null, "Logo 1", "Description line 1", false));
		pdfCombinerArguments.getContents().add(new PDFCombinerFile("Data\\PDFCombiner.doc", null, "Microsoft Word Document", "Description line 2", false));
		pdfCombinerArguments.getContents().add(new PDFCombinerFile("Data\\type.png", null, "Another image. This is an image in PNG format.", "Description line 3", false));
		pdfCombinerArguments.setClientCompanyName("Test name\nTest other data");
		pdfCombinerArguments.setClientContactInformation("Another information from a client contact");
		pdfCombinerArguments.setAgencyName("agencyName");
		pdfCombinerArguments.setAgencyContactInformation("agencyContactInformation");
		pdfCombinerArguments.setMarketName("marketName");
		pdfCombinerArguments.setMarketContactInformation("marketContactInformation");

		String generatedPDF = getPdfCombiner().combine(pdfCombinerArguments);
		Assert.assertTrue(new File(generatedPDF).exists());
	}

	@Test
	public void testCombineDocx() throws Exception, DocumentException, IOException {
		PDFCombinerArguments pdfCombinerArguments = new PDFCombinerArguments();
		pdfCombinerArguments.setTitle("Proposal test");
		pdfCombinerArguments.setSubTitle("Subtitle 12344....");
		pdfCombinerArguments.setContents(new ArrayList<PDFCombinerFile>());
		pdfCombinerArguments.getContents().add(new PDFCombinerFile("Data\\PDFCombiner.docx", null, "Microsoft Word Document", "Description line 1", false));
		pdfCombinerArguments.setClientCompanyName("Test name\nTest other data");
		pdfCombinerArguments.setClientContactInformation("Another information from a client contact");
		pdfCombinerArguments.setAgencyName("agencyName");
		pdfCombinerArguments.setAgencyContactInformation("agencyContactInformation");
		pdfCombinerArguments.setMarketName("marketName");
		pdfCombinerArguments.setMarketContactInformation("marketContactInformation");
		String generatedPDF = getPdfCombiner().combine(pdfCombinerArguments);
		Assert.assertTrue(new File(generatedPDF).exists());
	}

	@Test
	public void testImagePptDoc() throws Exception, DocumentException, IOException {
		PDFCombinerArguments pdfCombinerArguments = new PDFCombinerArguments();
		pdfCombinerArguments.setTitle("Proposal test");
		pdfCombinerArguments.setSubTitle("Subtitle 12344....");
		pdfCombinerArguments.setContents(new ArrayList<PDFCombinerFile>());
		pdfCombinerArguments.getContents().add(new PDFCombinerFile("Data\\logo.png", null, "Logo", "1 page long.", false));
		pdfCombinerArguments.getContents().add(new PDFCombinerFile("Data\\Sample Powerpoint 3 Slides.ppt", null, "Powerpoint file", "3 pages long", false));
		pdfCombinerArguments.getContents().add(new PDFCombinerFile("Data\\Sample Powerpoint 3 Slides.pdf", null, "Pdf document", "3 pages long", false));
		pdfCombinerArguments.getContents().add(new PDFCombinerFile("Data\\type.png", null, "Image in PNG format.", "1 page long", false));
		pdfCombinerArguments.setClientCompanyName("Test name\nTest other data");
		pdfCombinerArguments.setClientContactInformation("Another information from a client contact");
		pdfCombinerArguments.setAgencyName("agencyName");
		pdfCombinerArguments.setAgencyContactInformation("agencyContactInformation");
		pdfCombinerArguments.setMarketName("marketName");
		pdfCombinerArguments.setMarketContactInformation("marketContactInformation");

		String generatedPDF = getPdfCombiner().combine(pdfCombinerArguments);
		Assert.assertTrue(new File(generatedPDF).exists());
	}

	@Test
	public void testPageNumber() {
		PDFCombinerArguments pdfCombinerArguments = new PDFCombinerArguments();
		pdfCombinerArguments.setTitle("Proposal test");
		pdfCombinerArguments.setSubTitle("Subtitle 12344....");
		pdfCombinerArguments.setAppendixes(new ArrayList<PDFCombinerFile>());
		pdfCombinerArguments.getAppendixes().add(new PDFCombinerFile("Data\\logo.png", null, "Logo", "1 page long.", false));
		pdfCombinerArguments.getAppendixes().add(new PDFCombinerFile("Data\\Sample Powerpoint 3 Slides.ppt", null, "Powerpoint file", "3 pages long", false));
		pdfCombinerArguments.getAppendixes().add(new PDFCombinerFile("Data\\Sample Powerpoint 3 Slides.pdf", null, "Pdf document", "3 pages long", false));
		pdfCombinerArguments.getAppendixes().add(new PDFCombinerFile("Data\\type.png", null, "Image in PNG format.", "1 page long", false));
		pdfCombinerArguments.getAppendixes().get(0).setNumberOfPages(1);
		pdfCombinerArguments.getAppendixes().get(1).setNumberOfPages(3);
		pdfCombinerArguments.getAppendixes().get(2).setNumberOfPages(3);
		pdfCombinerArguments.getAppendixes().get(3).setNumberOfPages(1);

		// act
		pdfCombinerArguments.refreshPageNumbering(1);

		// verify
		Assert.assertEquals(1, pdfCombinerArguments.getAppendixes().get(0).getStartPageNumber());
		Assert.assertEquals(2, pdfCombinerArguments.getAppendixes().get(1).getStartPageNumber());
		Assert.assertEquals(5, pdfCombinerArguments.getAppendixes().get(2).getStartPageNumber());
		Assert.assertEquals(8, pdfCombinerArguments.getAppendixes().get(3).getStartPageNumber());
	}

//	@Test
//	public void testCombineImagesAndDocWithEmbeddedImage() throws Exception, DocumentException, IOException {
//		PDFCombinerArguments pdfCombinerArguments = new PDFCombinerArguments();
//		pdfCombinerArguments.setTitle("Proposal test");
//		pdfCombinerArguments.setSubTitle("Subtitle 12344....");
//		pdfCombinerArguments.getContentFileNamesList().add("Data\\PDFCombiner With Embedded Image.doc");
//		pdfCombinerArguments.getContentFileNamesList().add("Data\\type.png");
//		pdfCombinerArguments.getContentTitlesList().add("Microsoft Word Document");
//		pdfCombinerArguments.getContentTitlesList().add("Image");
//		pdfCombinerArguments.getContentDescriptionsList().add("Description line 1");
//		pdfCombinerArguments.getContentDescriptionsList().add("Description line 2");
//		pdfCombinerArguments.getAppendixTitlesList().add("Appendix 1 and only");
//		pdfCombinerArguments.setClientCompanyName("Test name\nTest other data");
//		pdfCombinerArguments.setClientContactInformation("Another information from a client contact");
//		pdfCombinerArguments.setAgencyName("agencyName");
//		pdfCombinerArguments.setAgencyContactInformation("agencyContactInformation");
//		pdfCombinerArguments.setMarketName("marketName");
//		pdfCombinerArguments.setMarketContactInformation("marketContactInformation");
//
//		String generatedPDF = getPdfCombiner().combine(pdfCombinerArguments);
//		Assert.assertTrue(new File(generatedPDF).exists());
//	}
//
//	@Test
//	public void testCombine13PagesDoc() throws Exception, DocumentException, IOException {
//		PDFCombinerArguments pdfCombinerArguments = new PDFCombinerArguments();
//		pdfCombinerArguments.setTitle("Proposal test");
//		pdfCombinerArguments.setSubTitle("Subtitle 12344....");
//		pdfCombinerArguments.getContentFileNamesList().add("Data\\Sample doc 13 pages long.doc");
//		pdfCombinerArguments.getContentTitlesList().add("Microsoft Word Document");
//		pdfCombinerArguments.getContentDescriptionsList().add("Description line 2");
//		pdfCombinerArguments.getAppendixTitlesList().add("Appendix 1 and only");
//		pdfCombinerArguments.setClientCompanyName("Test name\nTest other data");
//		pdfCombinerArguments.setClientContactInformation("Another information from a client contact");
//		pdfCombinerArguments.setAgencyName("agencyName");
//		pdfCombinerArguments.setAgencyContactInformation("agencyContactInformation");
//		pdfCombinerArguments.setMarketName("marketName");
//		pdfCombinerArguments.setMarketContactInformation("marketContactInformation");
//
//		String generatedPDF = getPdfCombiner().combine(pdfCombinerArguments);
//		Assert.assertTrue(new File(generatedPDF).exists());
//	}
//	
//	@Test
//	public void testJDocToPdfTestingDoc() throws Exception, DocumentException, IOException {
//		PDFCombinerArguments pdfCombinerArguments = new PDFCombinerArguments();
//		pdfCombinerArguments.setTitle("Proposal test");
//		pdfCombinerArguments.setSubTitle("Subtitle 12344....");
//		pdfCombinerArguments.getContentFileNamesList().add("Data\\JDocToPdf testing document.doc");
//		pdfCombinerArguments.getContentTitlesList().add("Microsoft Word Document");
//		pdfCombinerArguments.getContentDescriptionsList().add("Description line 2");
//		pdfCombinerArguments.getAppendixTitlesList().add("Appendix 1 and only");
//		pdfCombinerArguments.setClientCompanyName("Test name\nTest other data");
//		pdfCombinerArguments.setClientContactInformation("Another information from a client contact");
//		pdfCombinerArguments.setAgencyName("agencyName");
//		pdfCombinerArguments.setAgencyContactInformation("agencyContactInformation");
//		pdfCombinerArguments.setMarketName("marketName");
//		pdfCombinerArguments.setMarketContactInformation("marketContactInformation");
//
//		String generatedPDF = getPdfCombiner().combine(pdfCombinerArguments);
//		Assert.assertTrue(new File(generatedPDF).exists());
//	}
//
//	@Test
//	public void testCombine2x4CellsSheet() throws Exception, DocumentException, IOException {
//		PDFCombinerArguments pdfCombinerArguments = new PDFCombinerArguments();
//		pdfCombinerArguments.setTitle("Proposal test");
//		pdfCombinerArguments.setSubTitle("Subtitle 12344....");
//		pdfCombinerArguments.getContentFileNamesList().add("Data\\Sample excel 2x4 cells.xls");
//		pdfCombinerArguments.getContentTitlesList().add("Microsoft Excel Document");
//		pdfCombinerArguments.getContentDescriptionsList().add("Description line 2");
//		pdfCombinerArguments.getAppendixTitlesList().add("Appendix 1 and only");
//		pdfCombinerArguments.setClientCompanyName("Test name\nTest other data");
//		pdfCombinerArguments.setClientContactInformation("Another information from a client contact");
//		pdfCombinerArguments.setAgencyName("agencyName");
//		pdfCombinerArguments.setAgencyContactInformation("agencyContactInformation");
//		pdfCombinerArguments.setMarketName("marketName");
//		pdfCombinerArguments.setMarketContactInformation("marketContactInformation");
//
//		String generatedPDF = getPdfCombiner().combine(pdfCombinerArguments);
//		Assert.assertTrue(new File(generatedPDF).exists());
//	}
//
//	@Test
//	public void testCombine2x4CellsXLSXSheet() throws Exception, DocumentException, IOException {
//		PDFCombinerArguments pdfCombinerArguments = new PDFCombinerArguments();
//		pdfCombinerArguments.setTitle("Proposal test");
//		pdfCombinerArguments.setSubTitle("Subtitle 12344....");
//		pdfCombinerArguments.getContentFileNamesList().add("Data\\Sample excel 2x4 cells.xlsx");
//		pdfCombinerArguments.getContentTitlesList().add("Microsoft Excel Document");
//		pdfCombinerArguments.getContentDescriptionsList().add("Description line 2");
//		pdfCombinerArguments.getAppendixTitlesList().add("Appendix 1 and only");
//		pdfCombinerArguments.setClientCompanyName("Test name\nTest other data");
//		pdfCombinerArguments.setClientContactInformation("Another information from a client contact");
//		pdfCombinerArguments.setAgencyName("agencyName");
//		pdfCombinerArguments.setAgencyContactInformation("agencyContactInformation");
//		pdfCombinerArguments.setMarketName("marketName");
//		pdfCombinerArguments.setMarketContactInformation("marketContactInformation");
//
//		String generatedPDF = getPdfCombiner().combine(pdfCombinerArguments);
//		Assert.assertTrue(new File(generatedPDF).exists());
//	}
//
//	@Test
//	public void testCombine2x4CellsSheetWithImages() throws Exception, DocumentException, IOException {
//		PDFCombinerArguments pdfCombinerArguments = new PDFCombinerArguments();
//		pdfCombinerArguments.setTitle("Proposal test");
//		pdfCombinerArguments.setSubTitle("Subtitle 12344....");
//		pdfCombinerArguments.getContentFileNamesList().add("Data\\Sample excel 2x4 cells With Embedded Images.xls");
//		pdfCombinerArguments.getContentTitlesList().add("Microsoft Excel Document");
//		pdfCombinerArguments.getContentDescriptionsList().add("Description line 2");
//		pdfCombinerArguments.getAppendixTitlesList().add("Appendix 1 and only");
//		pdfCombinerArguments.setClientCompanyName("Test name\nTest other data");
//		pdfCombinerArguments.setClientContactInformation("Another information from a client contact");
//		pdfCombinerArguments.setAgencyName("agencyName");
//		pdfCombinerArguments.setAgencyContactInformation("agencyContactInformation");
//		pdfCombinerArguments.setMarketName("marketName");
//		pdfCombinerArguments.setMarketContactInformation("marketContactInformation");
//
//		String generatedPDF = getPdfCombiner().combine(pdfCombinerArguments);
//		Assert.assertTrue(new File(generatedPDF).exists());
//	}
//
//	@Test
//	public void testCombine3SlidesPowerpoint() throws Exception, DocumentException, IOException {
//		PDFCombinerArguments pdfCombinerArguments = new PDFCombinerArguments();
//		pdfCombinerArguments.setTitle("Proposal test");
//		pdfCombinerArguments.setSubTitle("Subtitle 12344....");
//		pdfCombinerArguments.getContentFileNamesList().add("Data\\Sample Powerpoint 3 Slides.ppt");
//		pdfCombinerArguments.getContentTitlesList().add("Microsoft Powerpoint Document");
//		pdfCombinerArguments.getContentDescriptionsList().add("Description line 2");
//		pdfCombinerArguments.getAppendixTitlesList().add("Appendix 1 and only");
//		pdfCombinerArguments.setClientCompanyName("Test name\nTest other data");
//		pdfCombinerArguments.setClientContactInformation("Another information from a client contact");
//		pdfCombinerArguments.setAgencyName("agencyName");
//		pdfCombinerArguments.setAgencyContactInformation("agencyContactInformation");
//		pdfCombinerArguments.setMarketName("marketName");
//		pdfCombinerArguments.setMarketContactInformation("marketContactInformation");
//
//		String generatedPDF = getPdfCombiner().combine(pdfCombinerArguments);
//		Assert.assertTrue(new File(generatedPDF).exists());
//	}
//
//	@Test
//	public void testCombine3SlidesPowerpointWithEmbeddedImages() throws Exception, DocumentException, IOException {
//		PDFCombinerArguments pdfCombinerArguments = new PDFCombinerArguments();
//		pdfCombinerArguments.setTitle("Proposal test");
//		pdfCombinerArguments.setSubTitle("Subtitle 12344....");
//		pdfCombinerArguments.getContentFileNamesList().add("Data\\Sample Powerpoint 3 Slides With Embedded Images.ppt");
//		pdfCombinerArguments.getContentTitlesList().add("Microsoft Powerpoint Document");
//		pdfCombinerArguments.getContentDescriptionsList().add("Description line 2");
//		pdfCombinerArguments.getAppendixTitlesList().add("Appendix 1 and only");
//		pdfCombinerArguments.setClientCompanyName("Test name\nTest other data");
//		pdfCombinerArguments.setClientContactInformation("Another information from a client contact");
//		pdfCombinerArguments.setAgencyName("agencyName");
//		pdfCombinerArguments.setAgencyContactInformation("agencyContactInformation");
//		pdfCombinerArguments.setMarketName("marketName");
//		pdfCombinerArguments.setMarketContactInformation("marketContactInformation");
//
//		String generatedPDF = getPdfCombiner().combine(pdfCombinerArguments);
//		Assert.assertTrue(new File(generatedPDF).exists());
//	}
}
