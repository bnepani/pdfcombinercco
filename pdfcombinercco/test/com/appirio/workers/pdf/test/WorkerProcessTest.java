package com.appirio.workers.pdf.test;

import static org.junit.Assert.*;

import java.io.IOException;
import java.net.MalformedURLException;

import org.codehaus.jackson.JsonNode;
import org.junit.Before;
import org.junit.Test;

import play.libs.Json;

import com.appirio.PDFCombinerArguments;
import com.appirio.workers.pdf.WorkerProcess;

public class WorkerProcessTest {

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void test() throws MalformedURLException, IOException {

		String url = "https://cs13.salesforce.com/services/data/v27.0/sobjects/ContentVersion";
		String sessionId = "00DW0000000In2Q!ARoAQBmR_M2A6ZfCb3zWisyAUQQnlCQua0gle0HnM7fFBBaY4YgUcDrIpHLrC3Us0OCQKjiNv.kfEAPnyd0gabzj4SwOpm7o";
		String contentDocumentId = "069W00000000IC3IAM";
		String sourceFileName = "D:/tmp/generatedpdf/1365783621636/image1.png";

		String jsonString = "{\"title\":\"aaab\",\"subTitle\":\"444\",\"showTimeAndDateStamp\":false,\"showTableOfContents\":false,\"showPageNumbering\":false,\"sessionId\":\"" + sessionId + "\",\"pdfCombinerCallback\":{\"callbackUrl\":\"https://na14.salesforce.com/services/data/v27.0/sobjects/Account/001d000000HiIUz\",\"callbackContents\":\"{\\\"Site\\\" : \\\"%result%\\\"}\"},\"outputFileName\":\"image1.png\",\"marketName\":null,\"marketContactInformation\":null,\"insertContentVersionUrl\":\"" + url + "\",\"includeServiceGuaranteeDoc\":false,\"includeResearchToolsDoc\":false,\"includeProductionSpecificationDoc\":false,\"includeOutdoorVocabularyTermsDoc\":true,\"email\":null,\"contents\":[],\"contentDocumentId\":\"" + contentDocumentId + "\",\"clientContactInformation\":null,\"clientCompanyName\":null,\"attachmentsUrl\":\"https://na14.salesforce.com/services/data/v27.0/sobjects/Attachment/\",\"appendixes\":[{\"title\":\"PDFCombiner sample document\",\"salesforceUrl\":\"https://c.na14.visual.force.com/services/data/v27.0/sobjects/ContentVersion/068d0000000ibMjAAI/VersionData\",\"pathOnClient\":\"PDFCombiner sample document.pdf\",\"description\":\"This is for simulating an Outdoor Vocabulary Terms Doc\"}],\"agencyName\":null,\"agencyContactInformation\":null}";

		JsonNode jsonNode = Json.parse(jsonString);

		PDFCombinerArguments pdfCombinerArguments = Json.fromJson(jsonNode, PDFCombinerArguments.class);

		//WorkerProcess.uploadFileToSalesforce(sourceFileName, jsonString, pdfCombinerArguments);

	}

}
