package com.appirio.report.test;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;

import net.sf.jxls.exception.ParsePropertyException;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.junit.Assert;
import org.junit.Test;
import org.xml.sax.SAXException;

import com.appirio.PDFCombinerFile;
import com.appirio.report.ExcelReporter;
import com.appirio.report.PdfReporter;

public class ExcelReporterTest {
	ExcelReporter excelReporter;


	@Test
	public void disclaimersTest() throws ParseException, ParserConfigurationException, SAXException, IOException {

		String disclaimerDataSourceFileName = "Data/real-test-set-2013-10-23-001-disclaimers.xml";

		// init
		PdfReporter reporter = new PdfReporter();
		reporter.new DisclaimersDataExpression(disclaimerDataSourceFileName);

		// simulate getting disclaimer for flight

		String flightName1 = "Chicago - Flight 1";	// Package_Flight__r/Name
		String division1 = "Chicago";				// Package_Flight__r/Division__c
		String mediaCategory1 = "Bulletin";			// Package_Flight__r/Media_Category__c
		runSampleDisclaimer(reporter, flightName1, division1, mediaCategory1);

		String flightName2 = "Chicago - Flight 1";	// Package_Flight__r/Name
		String division2 = "Chicago";				// Package_Flight__r/Division__c
		String mediaCategory2 = "Digital";			// Package_Flight__r/Media_Category__c
		runSampleDisclaimer(reporter, flightName2, division2, mediaCategory2);
	}

	private void runSampleDisclaimer(PdfReporter reporter, String flightName, String division, String mediaCategory) {

		Set<String> disclaimerSet = reporter.getDisclaimerStore().getValidDisclaimers(flightName, division, mediaCategory);

		System.out.println("====================================================================================");
		System.out.println("Disclaimers for");
		System.out.println("   flightName:    " + flightName);
		System.out.println("   division:      " + division);
		System.out.println("   mediaCategory: " + mediaCategory);
		System.out.println("====================================================================================");
		for(String disclaimer : disclaimerSet) {
			System.out.println(disclaimer);
		}
		System.out.println("====================================================================================");
	}

	@Test
	public void dateFormatTest() throws ParseException {

		String dateString = "2013-09-23";

		SimpleDateFormat inputDateFormat = new SimpleDateFormat("yyyy-MM-dd");
		Date dateDate = inputDateFormat.parse(dateString);

		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM/dd/yyyy");

		System.out.println("   formatted date: " + simpleDateFormat.format(dateDate));
	}

	@Test
	public void columnTest() {

		// Buy type column map
		// key: salesforce column name
		// value: reporter column name
		Map<String, String> audienceBuyTypeColumnMap = new LinkedHashMap<String, String>();
		audienceBuyTypeColumnMap.put("Additional_Cost__c", "additionalCost");
		audienceBuyTypeColumnMap.put("Average_Daily_Spots__c", "averageDailySpots");
		audienceBuyTypeColumnMap.put("City__c", "city");
		audienceBuyTypeColumnMap.put("Comments__c", "comments");
		audienceBuyTypeColumnMap.put("County__c", "county");
		audienceBuyTypeColumnMap.put("CPP_0d__c", "cPP0d");
		audienceBuyTypeColumnMap.put("Current_Copy__c", "currentCopy");

		// User columns
		Map<String, String> userColumnMap = new LinkedHashMap<String, String>();
		userColumnMap.put("Average_Daily_Spots__c", "The daily spots used");
		userColumnMap.put("Comments__c", "Your comments here");
		userColumnMap.put("County__c", "County");

		// Result: Report columns (User columns AND Buy type column map)
		// averageDailySpots, The daily spots used
		Map<String, String> reportColumnMap = new LinkedHashMap<String, String>();
		for(String userColumn : userColumnMap.keySet()) {
			String buyTypeColumn = audienceBuyTypeColumnMap.get(userColumn);
			String value = userColumnMap.get(userColumn); 
			reportColumnMap.put(buyTypeColumn, value);
			System.out.println("   key, value: " + buyTypeColumn + " " + value);
		}
	}

	@Test
	public void test() throws ParsePropertyException, InvalidFormatException, IOException, SAXException {

		// ======================= initialize
		String dataSourceFileName = "Data/real-test-set-2013-10-23-001-package.xml";
		//String dataSourceFileName = "Data/data-2013-10-04-reduced.xml";
		//String dataSourceFileName = "Data/real-all-buy-type-data-2013-10-02.xml";
		//String dataSourceFileName = "Data/real-data-2013-10-10.xml";
		String disclaimerDataSourceFileName = "Data/real-test-set-2013-10-23-001-disclaimers.xml";
		String mapPanelOrderPrefDataSourceFileName = "Data/CPQ_Map_Panel_Order_Pref__c_with_data.xml";
		Boolean excludeNetworkDetails = false;
		Boolean showTotalProgramSummary = true;
		Boolean showIndividualMarketSummary = true;
		Boolean showIndividualFlightSummary = true;
		PDFCombinerFile pdfCombinerFile = getTestCombinerFile();

		excelReporter = new ExcelReporter(
				dataSourceFileName,
				disclaimerDataSourceFileName,
				mapPanelOrderPrefDataSourceFileName,
				excludeNetworkDetails,
				showTotalProgramSummary,
				showIndividualMarketSummary,
				showIndividualFlightSummary,
				pdfCombinerFile);

		// ======================= verify audience report columns
		List<String> audienceReportColumns = excelReporter.getAudienceReportColumnNames();
		Assert.assertTrue(audienceReportColumns.isEmpty());

		// generate report
		excelReporter.generate(excelReporter.getGeneratedReport("xlsx"));
	}
	
	private PDFCombinerFile getTestCombinerFile() {
		PDFCombinerFile pdfCombinerFile = new PDFCombinerFile();

		List<String> fieldNamesPipeDelimited = new ArrayList<String>();
		fieldNamesPipeDelimited.add(
				"Media_Type__c" +
				"|Package_Flight__r/Market_Name__c" +
				"|Package_Flight__r/Market_Type__c" +
				"|Package_Flight__r/Division__c" +
				"|Package_Flight__r/Package_Name__c" +
				"|Package_Flight__r/Name" +
				"|Package_Flight__r/Campaign_Start_Date__c" +
				"|Package_Flight__r/Campaign_End_Date__c" +
				"|Package_Flight__r/Flight_Duration__c" +
				"|Package_Flight__r/Target__c" +
				"|Package_Flight__r/Target_Population__c" +
				"|Package_Flight__r/Package_Market__r/Weekly_Total_18_Imps__c" +
				"|Package_Flight__r/Package_Market__r/In_Mkt_Imps__c" +
				"|Package_Flight__r/Package_Market__r/Target_Total_Imps__c" +
				"|Package_Flight__r/Package_Market__r/In_Mkt_TRP__c" +
				"|Package_Flight__r/Package_Market__r/Plan_TRP__c" +
				"|Package_Flight__r/Package_Market__r/Reach__c" +
				"|Package_Flight__r/Package_Market__r/Frequency__c" +
				"|Package_Flight__r/Package_Market__r/Total_Price__c" +
				"|Package_Flight__r/Package_Market__r/CPM__c" +
				"|Package_Flight__r/Package_Market__r/CPP__c" +				
				"|Ride_Order__c" +
				"|Media_Category__c" +
				"|Unit_Size__c" +
				"|Illumination_yn__c" +
				"|Panel_Id_Label__c" +
				"|TAB_Id__c" +
				"|Location_Description__c" +
				"|Face_Direction__c" +
				"|MapLocation_Number__c" +
				"|Number_of_Panels__c" +
				"|Weekly_Total_18_Imps__c" +
				"|In_Mkt_Imps__c" +
				"|Total_Imps__c" +
				"|In_Mkt_TRP__c" +
				"|PlanTRP__c" +
				"|Plan_Imps_Reach_Perc__c" +
				"|Plan_Imps_Avg_Frequency__c" +
				"|Planning_Rate_4_Wk_Rate_per_Panel__c" +
				"|X4_Wk_Proposed_Price__c" +
				"|Net_Amount_Value__c" +
				"|Total_Price_0d__c" +
				"|TotalInMarketCPM_0d__c" +
				"|CPP_0d__c" +
				"|Comments__c" + 
				"|MarketName__c" +
				"|Quattro_Media_Product__c" +
				"|Network_Name__c" +
				""
				);
		fieldNamesPipeDelimited.add("Package_Flight__r/Flight_Comments__c");
		fieldNamesPipeDelimited.add("");
		fieldNamesPipeDelimited.add("");
		pdfCombinerFile.setFieldNamesPipeDelimited(fieldNamesPipeDelimited);

		List<String> fieldLabelsPipeDelimited = new ArrayList<String>();
		fieldLabelsPipeDelimited.add(
				"Media Type" +
				"|Market Name" +
				"|Market Type" +
				"|Division" +
				"|Package Name" +
				"|Flight Name" +
				"|Start Date" +
				"|End Date" +
				"|Weeks" +
				"|Target" +
				"|Target Population" +
				"|Weekly Total 18 Imps" +
				"|In Mkt Imps" +
				"|Target Total Imps" +
				"|In Mkt TRP" +
				"|Plan TRP" +
				"|Reach" +
				"|Frequency" +
				"|Total Price" +
				"|CPM" +
				"|CPP" +
				"|Ride Order" +
				"|Media category" +
				"|Unit Size" +
				"|Illumination(Y/N)" +
				"|Panel Id" +
				"|TAB Id" +
				"|Location Description" +
				"|Face Direction" +
				"|Map Loc. Nr." +
				"|Nr. of Panels" +
				"|Weekly Total 18+ Imps" +
				"|Target In-Market Imps" +
				"|Target Total Imps" +
				"|Weekly TRP" +
				"|Plan TRP" +
				"|Reach %" +
				"|Freq" +
				"|Planning Rate: 4-Wk Rate per Panel" +
				"|4-Wk Proposed Price" +
				"|SubTotal" +
				"|Total Price" +
				"|CPM" +
				"|CPP" +
				"|Line Item Comments" +
				"|Market Name" +
				"|Media Product" +
				"|Network Name" +
				""
				);
		fieldLabelsPipeDelimited.add("Flight Comments");
		fieldLabelsPipeDelimited.add("");
		fieldLabelsPipeDelimited.add("");
		pdfCombinerFile.setFieldLabelsPipeDelimited(fieldLabelsPipeDelimited);

		return pdfCombinerFile;
	}
}