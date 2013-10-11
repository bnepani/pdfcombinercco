package com.appirio.report.test;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

import com.appirio.PDFCombinerContentEntry;
import com.appirio.PDFCombinerFile;
import com.appirio.report.Reporter;

public class ReporterTest {

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void getPageNumberFromLineTest() {
		Assert.assertEquals(1, Reporter.getPageNumberFromLine(" Package Albaquerque DMA,,,,,,..............................................................................................................................................,,,,,,1"));
		Assert.assertEquals(258, Reporter.getPageNumberFromLine(" Package Albaquerque DMA,,,,,,..............................................................................................................................................,,,,,,258"));
	}

	@Test
	public void allLevelsFilledTest() throws Exception {
		PDFCombinerFile pdfCombinerFile = new PDFCombinerFile();

		List<String> fieldNamesPipeDelimited = new ArrayList<String>();
		fieldNamesPipeDelimited.add("Duration__c|Reach_1d__c|Frequency_1d__c");
		fieldNamesPipeDelimited.add("Package_Flight__r/Id");
		fieldNamesPipeDelimited.add("Package_Flight__r/Package_Market__r/Market_Name__c|Package_Flight__r/Package_Market__r/Market_Area__c|Package_Flight__r/Package_Market__r/Market_State__c");
		fieldNamesPipeDelimited.add("Package_Flight__r/Package_Market__r/Package__r/Package_Name__c|Package_Flight__r/Package_Market__r/Package__r/Market_Type__c");
		pdfCombinerFile.setFieldNamesPipeDelimited(fieldNamesPipeDelimited);

		List<String> fieldLabelsPipeDelimited = new ArrayList<String>();
		fieldLabelsPipeDelimited.add("Duration|Reach|Frequency");
		fieldLabelsPipeDelimited.add("Id");
		fieldLabelsPipeDelimited.add("Market Name|State|Area");
		fieldLabelsPipeDelimited.add("Package Name|Market Type");
		pdfCombinerFile.setFieldLabelsPipeDelimited(fieldLabelsPipeDelimited);

		List<String> fieldTypesPipeDelimited = new ArrayList<String>();
		fieldTypesPipeDelimited.add("Double|Double|Double");
		fieldTypesPipeDelimited.add("String");
		fieldTypesPipeDelimited.add("String|String|String");
		fieldTypesPipeDelimited.add("String|String");
		pdfCombinerFile.setFieldTypesPipeDelimited(fieldTypesPipeDelimited);

		Reporter reporter = new Reporter("Data//CPQ_sample1.xml", "Data//CPQ_Proposal_Disclaimer__c.xml", "Data//CPQ_Map_Panel_Order_Pref__c.xml", null, false, false, false, false, pdfCombinerFile, true, true, null);
		showReporterInfo(reporter);

		List<PDFCombinerContentEntry> pdfCombinerContentEntryList = reporter.getPDFCombinerContentEntryList();

		// verify
		Assert.assertNotNull(pdfCombinerContentEntryList);
		Assert.assertEquals("Package Albaquerque DMA", pdfCombinerContentEntryList.get(0).getTitle());
		Assert.assertEquals(1, pdfCombinerContentEntryList.get(0).getPageNumber());
	}

	@Test
	public void networkFlavourReportTest() throws Exception {
		PDFCombinerFile pdfCombinerFile = new PDFCombinerFile();

		List<String> fieldNamesPipeDelimited = new ArrayList<String>();
		fieldNamesPipeDelimited.add("Target_Population__c|Target__c|Campaign_Weeks__c|Campaign_End_Date__c|Campaign_Start_Date__c|Name|Map_Location_Number|Division__c|MarketName__c|MarketTypeName__c|Package_Name|Weekly_Total_18_Imps__c|Target_In_Market_Imps__c|Total_Imps__c|Weekly_TRP_1d__c|PlanTRP_1d__c|Plan_Imps_Reach_Perc__c|Frequency__c|Planning_Rate_4_Wk_Rate_per_Panel__c|X4_Wk_Proposed_Price__c|Total_Price_0d__c|TotalInMarketCPM__c|CPP_0d__c|UnitSize__c|Illuminated__c|Comments__c");
		fieldNamesPipeDelimited.add("Package_Flight__r/Id");
		pdfCombinerFile.setFieldNamesPipeDelimited(fieldNamesPipeDelimited);

		List<String> fieldLabelsPipeDelimited = new ArrayList<String>();
		fieldLabelsPipeDelimited.add("Target_Population__c|Target__c|Campaign_Weeks__c|Campaign_End_Date__c|Campaign_Start_Date__c|Name|Map Location Number|Division|Market Name|Market Type|Package Name|Weekly Total 18+ Impressions|Target In-Market Impressions|Target Total Impressions|Weekly TRP|Plan TRP|Reach %|Frequency|Planning Rate: 4-Week Rate per Panel|4-Week Proposed Price|Total Price|CPM|CPP|Unit Size* (hxw)|Illuminated (Y/N)|Line Item Comments");
		fieldLabelsPipeDelimited.add("Id");
		pdfCombinerFile.setFieldLabelsPipeDelimited(fieldLabelsPipeDelimited);

		List<String> fieldTypesPipeDelimited = new ArrayList<String>();
		fieldTypesPipeDelimited.add("Double|String|Double|String|String|String|String|String|String|String|String|Double|Double|Double|Double|Double|Double|Double|Double|Double|Double|Double|Double|String|String|String|String|String|String");
		fieldTypesPipeDelimited.add("String");
		pdfCombinerFile.setFieldTypesPipeDelimited(fieldTypesPipeDelimited);

		Reporter reporter = new Reporter("Data//CPQ_sample1.xml", "Data//CPQ_Proposal_Disclaimer__c.xml", "Data//CPQ_Map_Panel_Order_Pref__c.xml", null, false, false, false, false, pdfCombinerFile, true, true,null);
		showReporterInfo(reporter);
	}

	@Test
	public void locationFlavourReportTest() throws Exception {
		PDFCombinerFile pdfCombinerFile = new PDFCombinerFile();

		List<String> fieldNamesPipeDelimited = new ArrayList<String>();
		fieldNamesPipeDelimited.add("Map_Location_Number|Division__c|MarketName__c|MarketTypeName__c|Package_Name|Weekly_Total_18_Imps__c|Target_In_Market_Imps__c|Total_Imps__c|Weekly_TRP_1d__c|PlanTRP_1d__c|Plan_Imps_Reach_Perc__c|Frequency__c|Planning_Rate_4_Wk_Rate_per_Panel__c|X4_Wk_Proposed_Price__c|Total_Price_0d__c|TotalInMarketCPM__c|CPP_0d__c|UnitSize__c|Illuminated__c|Comments__c");
		pdfCombinerFile.setFieldNamesPipeDelimited(fieldNamesPipeDelimited);

		List<String> fieldLabelsPipeDelimited = new ArrayList<String>();
		fieldLabelsPipeDelimited.add("Map Location Number|Division|Market Name|Market Type|Package Name|Weekly Total 18+ Impressions|Target In-Market Impressions|Target Total Impressions|Weekly TRP|Plan TRP|Reach %|Frequency|Planning Rate: 4-Week Rate per Panel|4-Week Proposed Price|Total Price|CPM|CPP|Unit Size* (hxw)|Illuminated (Y/N)|Line Item Comments");
		pdfCombinerFile.setFieldLabelsPipeDelimited(fieldLabelsPipeDelimited);

		List<String> fieldTypesPipeDelimited = new ArrayList<String>();
		fieldTypesPipeDelimited.add("String|String|String|String|String|Double|Double|Double|Double|Double|Double|Double|Double|Double|Double|Double|Double|String|String|String|String|String|String");
		pdfCombinerFile.setFieldTypesPipeDelimited(fieldTypesPipeDelimited);

		Reporter reporter = new Reporter("Data//CPQ_sample1.xml", "Data//CPQ_Proposal_Disclaimer__c.xml", "Data//CPQ_Map_Panel_Order_Pref__c.xml", null, false, true, true, false, pdfCombinerFile, true, true,null);
		showReporterInfo(reporter);
	}

	@Test
	public void locationFlavourReportWithMapLocationNumberTest() throws Exception {
		PDFCombinerFile pdfCombinerFile = new PDFCombinerFile();

		List<String> fieldNamesPipeDelimited = new ArrayList<String>();
		fieldNamesPipeDelimited.add("MapLocation_Number__c|Division__c|MarketName__c|MarketTypeName__c|Package_Name|Weekly_Total_18_Imps__c|Target_In_Market_Imps__c|Total_Imps__c|Weekly_TRP_1d__c|PlanTRP_1d__c|Plan_Imps_Reach_Perc__c|Frequency__c|Planning_Rate_4_Wk_Rate_per_Panel__c|X4_Wk_Proposed_Price__c|Total_Price_0d__c|TotalInMarketCPM__c|CPP_0d__c|UnitSize__c|Illuminated__c|Comments__c");
		pdfCombinerFile.setFieldNamesPipeDelimited(fieldNamesPipeDelimited);

		List<String> fieldLabelsPipeDelimited = new ArrayList<String>();
		fieldLabelsPipeDelimited.add("Map Location Number|Division|Market Name|Market Type|Package Name|Weekly Total 18+ Impressions|Target In-Market Impressions|Target Total Impressions|Weekly TRP|Plan TRP|Reach %|Frequency|Planning Rate: 4-Week Rate per Panel|4-Week Proposed Price|Total Price|CPM|CPP|Unit Size* (hxw)|Illuminated (Y/N)|Line Item Comments");
		pdfCombinerFile.setFieldLabelsPipeDelimited(fieldLabelsPipeDelimited);

		List<String> fieldTypesPipeDelimited = new ArrayList<String>();
		fieldTypesPipeDelimited.add("String|String|String|String|String|Double|Double|Double|Double|Double|Double|Double|Double|Double|Double|Double|Double|String|String|String|String|String|String");
		pdfCombinerFile.setFieldTypesPipeDelimited(fieldTypesPipeDelimited);

		Reporter reporter = new Reporter("Data//CPQ_sample1.xml", "Data//CPQ_Proposal_Disclaimer__c.xml", "Data//CPQ_Map_Panel_Order_Pref__c_with_data.xml", null, false, true, true, false, pdfCombinerFile, true, true,null);
		showReporterInfo(reporter);
	}

	@Test
	public void locationFlavourReportAllSummariesTest() throws Exception {
		PDFCombinerFile pdfCombinerFile = new PDFCombinerFile();

		List<String> fieldNamesPipeDelimited = new ArrayList<String>();
		fieldNamesPipeDelimited.add("Map_Location_Number|Division__c|MarketName__c|MarketTypeName__c|Package_Name|Flight_Name__c|Weekly_Total_18_Imps__c|Target_In_Market_Imps__c|Total_Imps__c|Weekly_TRP_1d__c|PlanTRP_1d__c|Plan_Imps_Reach_Perc__c|Frequency__c|Planning_Rate_4_Wk_Rate_per_Panel__c|X4_Wk_Proposed_Price__c|Total_Price_0d__c|TotalInMarketCPM__c|CPP_0d__c|UnitSize__c|Illuminated__c|Comments__c");
		pdfCombinerFile.setFieldNamesPipeDelimited(fieldNamesPipeDelimited);

		List<String> fieldLabelsPipeDelimited = new ArrayList<String>();
		fieldLabelsPipeDelimited.add("Map Location Number|Division|Market Name|Market Type|Package Name|Flight Name|Weekly Total 18+ Impressions|Target In-Market Impressions|Target Total Impressions|Weekly TRP|Plan TRP|Reach %|Frequency|Planning Rate: 4-Week Rate per Panel|4-Week Proposed Price|Total Price|CPM|CPP|Unit Size* (hxw)|Illuminated (Y/N)|Line Item Comments");
		pdfCombinerFile.setFieldLabelsPipeDelimited(fieldLabelsPipeDelimited);

		List<String> fieldTypesPipeDelimited = new ArrayList<String>();
		fieldTypesPipeDelimited.add("String|String|String|String|String|String|Double|Double|Double|Double|Double|Double|Double|Double|Double|Double|Double|Double|String|String|String|String|String|String");
		pdfCombinerFile.setFieldTypesPipeDelimited(fieldTypesPipeDelimited);

		Reporter reporter = new Reporter("Data//CPQ_FourLevelsSample1.xml", "Data//CPQ_Proposal_Disclaimer__c.xml", "Data//CPQ_Map_Panel_Order_Pref__c.xml", null, false, true, true, true, pdfCombinerFile, true, true,null);
		showReporterInfo(reporter);
	}

	@Test
	public void BUG_2013_05_04_01Test() throws Exception {
		PDFCombinerFile pdfCombinerFile = new PDFCombinerFile();

		List<String> fieldNamesPipeDelimited = new ArrayList<String>();
		fieldNamesPipeDelimited.add("Division__c|MarketName__c|MarketTypeName__c|Package_Name__c|Name|Flight_Start_Date__c|Flight_End_Date__c|Target__c|Target_Population__c|MediaTypeName__c|Face_Direction__c|Weekly_Total_18_Imps__c|Target_In_Market_Imps__c|Total_Imps__c|Weekly_TRP_1d__c|Plan_TRP_1d__c|Plan_Imps_Reach_Perc__c|Frequency__c|Total_Price_0d__c|TotalInMarketCPM__c|CPP_0d__c, Package_Flight__r/Name|Package_Flight__r/Market_key_Name__c");
		pdfCombinerFile.setFieldNamesPipeDelimited(fieldNamesPipeDelimited);

		List<String> fieldLabelsPipeDelimited = new ArrayList<String>();
		fieldLabelsPipeDelimited.add("Division|MarketName|MarketTypeName|Package Name|Package Flight Line Name|Flight Start Date|Flight End Date|Target|Target Population|MediaTypeName|Face Direction|Weekly Total 18 Imps|Target In-Market Imps|Total Imps|Weekly TRP|Plan TRP|Plan Imps Reach Perc|Frequency|Total Price|TotalInMarketCPM|CPP, Flight Name|Market key Name");
		pdfCombinerFile.setFieldLabelsPipeDelimited(fieldLabelsPipeDelimited);

		List<String> fieldTypesPipeDelimited = new ArrayList<String>();
		fieldTypesPipeDelimited.add("String|String|String|String|String|String|String|String|Double|String|String|Double|Double|Double|Double|Double|Double|Double|Currency|Double|Currency, String|String");
		pdfCombinerFile.setFieldTypesPipeDelimited(fieldTypesPipeDelimited);

		Reporter reporter = new Reporter("Data//CPQ_sample1.xml", "Data//CPQ_Proposal_Disclaimer__c.xml", "Data//CPQ_Map_Panel_Order_Pref__c.xml", null, false, true, false, false, pdfCombinerFile, true, true,null);
		showReporterInfo(reporter);
	}

	@Test
	public void oneLevelFilledTest() throws Exception {
		PDFCombinerFile pdfCombinerFile = new PDFCombinerFile();

		List<String> fieldNamesPipeDelimited = new ArrayList<String>();
		fieldNamesPipeDelimited.add("Duration__c|Reach_1d__c|Frequency_1d__c");
		pdfCombinerFile.setFieldNamesPipeDelimited(fieldNamesPipeDelimited);

		List<String> fieldLabelsPipeDelimited = new ArrayList<String>();
		fieldLabelsPipeDelimited.add("Duration|Reach|Frequency");
		pdfCombinerFile.setFieldLabelsPipeDelimited(fieldLabelsPipeDelimited);

		List<String> fieldTypesPipeDelimited = new ArrayList<String>();
		fieldTypesPipeDelimited.add("Double|Double|Double");
		pdfCombinerFile.setFieldTypesPipeDelimited(fieldTypesPipeDelimited);

		Reporter reporter = new Reporter("Data//CPQ_sample1.xml", "Data//CPQ_Proposal_Disclaimer__c.xml", "Data//CPQ_Map_Panel_Order_Pref__c.xml", null, false, false, false, false, pdfCombinerFile, true, true,null);
		showReporterInfo(reporter);
	}

	@Test
	public void oneLevelFilledWithShippingInstructionsTest() throws Exception {
		PDFCombinerFile pdfCombinerFile = new PDFCombinerFile();

		List<String> fieldNamesPipeDelimited = new ArrayList<String>();
		fieldNamesPipeDelimited.add("Duration__c|Reach_1d__c|Frequency_1d__c");
		pdfCombinerFile.setFieldNamesPipeDelimited(fieldNamesPipeDelimited);

		List<String> fieldLabelsPipeDelimited = new ArrayList<String>();
		fieldLabelsPipeDelimited.add("Duration|Reach|Frequency");
		pdfCombinerFile.setFieldLabelsPipeDelimited(fieldLabelsPipeDelimited);

		List<String> fieldTypesPipeDelimited = new ArrayList<String>();
		fieldTypesPipeDelimited.add("Double|Double|Double");
		pdfCombinerFile.setFieldTypesPipeDelimited(fieldTypesPipeDelimited);

		Reporter reporter = new Reporter("Data//CPQ_sample1.xml", "Data//CPQ_Proposal_Disclaimer__c.xml", "Data//CPQ_Map_Panel_Order_Pref__c.xml", "Data//CPQ_ShippingInstructions_With_Records.xml", false, false, false, false, pdfCombinerFile, true, true,null);
		showReporterInfo(reporter);
	}

	@Test
	public void twoLevelsFilledTest() throws Exception {
		PDFCombinerFile pdfCombinerFile = new PDFCombinerFile();

		List<String> fieldNamesPipeDelimited = new ArrayList<String>();
		fieldNamesPipeDelimited.add("Duration__c|Reach_1d__c|Frequency_1d__c");
		fieldNamesPipeDelimited.add("Package_Flight__r/Id");
		pdfCombinerFile.setFieldNamesPipeDelimited(fieldNamesPipeDelimited);

		List<String> fieldLabelsPipeDelimited = new ArrayList<String>();
		fieldLabelsPipeDelimited.add("Duration|Reach|Frequency");
		fieldLabelsPipeDelimited.add("Id");
		pdfCombinerFile.setFieldLabelsPipeDelimited(fieldLabelsPipeDelimited);

		List<String> fieldTypesPipeDelimited = new ArrayList<String>();
		fieldTypesPipeDelimited.add("Double|Double|Double");
		fieldTypesPipeDelimited.add("String");
		pdfCombinerFile.setFieldTypesPipeDelimited(fieldTypesPipeDelimited);

		Reporter reporter = new Reporter("Data//CPQ_sample1.xml", "Data//CPQ_Proposal_Disclaimer__c.xml", "Data//CPQ_Map_Panel_Order_Pref__c.xml", null, false, false, false, false, pdfCombinerFile, true, true,null);
		showReporterInfo(reporter);
	}

	/*
	 * Verify generated pdfs/excels for the following:
	 * 		a) If PackageMarketFlight.Buy type is ""Location"", all Disclaimers with this value should be rendered
	 * 		right after the Flight Details
	 * 		b) Print "Rail Station Display Summary" Disclaimers after the Flight Details (i.e. Package Flight Lines, where PackageMarketFlight.Buy Type is ""Network/Custom"" and PackageMarketFlight.MediaCategory is ""Commuter Rail""
	 *      c) Print "Rail Transit Poster Summary" Disclaimers after the Flight Details (i.e. Package Flight Lines, where PackageMarketFlight.Buy Type is ""Network/Custom"" and PackageMarketFlight.MediaCategory is ""Bus/Transit""
	 *      d) Print "Taxi Display Summary" Disclaimers after the Flight Details (i.e. Package Flight Lines, where PackageMarketFlight.Buy Type is ""Network/Custom"" and PackageMarketFlight.MediaCategory is ""Mobile Billboard""
	 */
	@Test
	public void renderAfterFlightSummaryDisclaimerTest() throws Exception {
		PDFCombinerFile pdfCombinerFile = new PDFCombinerFile();

		List<String> fieldNamesPipeDelimited = new ArrayList<String>();
		fieldNamesPipeDelimited.add("Duration__c|Reach_1d__c|Frequency_1d__c");
		fieldNamesPipeDelimited.add("Package_Flight__r/Id|Package_Flight__r/Type__c|Package_Flight__r/Media_Category__c");
		pdfCombinerFile.setFieldNamesPipeDelimited(fieldNamesPipeDelimited);

		List<String> fieldLabelsPipeDelimited = new ArrayList<String>();
		fieldLabelsPipeDelimited.add("Duration|Reach|Frequency");
		fieldLabelsPipeDelimited.add("Id|Buy Type|Media Category");
		pdfCombinerFile.setFieldLabelsPipeDelimited(fieldLabelsPipeDelimited);

		List<String> fieldTypesPipeDelimited = new ArrayList<String>();
		fieldTypesPipeDelimited.add("Double|Double|Double");
		fieldTypesPipeDelimited.add("String|String|String");
		pdfCombinerFile.setFieldTypesPipeDelimited(fieldTypesPipeDelimited);

		Reporter reporter = new Reporter("Data//CPQ_sample1.xml", "Data//CPQ_Proposal_Disclaimer__c.xml", "Data//CPQ_Map_Panel_Order_Pref__c.xml", null, false, false, false, false, pdfCombinerFile, true, true,null);
		showReporterInfo(reporter);
	}

	@Test
	public void isMarketLevelTest() {
		PDFCombinerFile pdfCombinerFile = new PDFCombinerFile();

		List<String> fieldNamesPipeDelimited = new ArrayList<String>();
		fieldNamesPipeDelimited.add("Duration__c|Reach_1d__c|Frequency_1d__c");
		fieldNamesPipeDelimited.add("Package_Flight__r/Id");
		fieldNamesPipeDelimited.add("Package_Flight__r/Package_Market__r/Market_Name__c");
		fieldNamesPipeDelimited.add("Package_Flight__r/Package_Market__r/Package__r/Package_Name__c|Package_Flight__r/Package_Market__r/Package__r/Market_Type__c");

		pdfCombinerFile.setFieldNamesPipeDelimited(fieldNamesPipeDelimited);

		Assert.assertFalse(Reporter.isMarketLevel(pdfCombinerFile, 0));
		Assert.assertFalse(Reporter.isMarketLevel(pdfCombinerFile, 1));
		Assert.assertTrue(Reporter.isMarketLevel(pdfCombinerFile, 2));
		Assert.assertFalse(Reporter.isMarketLevel(pdfCombinerFile, 3));
	}

	/*
	 * All Disclaimers with ""Program Summary (Including individual Market)"" should be rendered at the
	 * end of every Market Summary Section and also at the end of the Plan.
	 */
	@Test
	public void renderAllDisclaimersWithProgramSummaryIncludingIndividualMarketTest() throws Exception {
		PDFCombinerFile pdfCombinerFile = new PDFCombinerFile();

		List<String> fieldNamesPipeDelimited = new ArrayList<String>();
		fieldNamesPipeDelimited.add("Duration__c|Reach_1d__c|Frequency_1d__c");
		fieldNamesPipeDelimited.add("Package_Flight__r/Id");
		fieldNamesPipeDelimited.add("Package_Flight__r/Package_Market__r/Market_Name__c");
		fieldNamesPipeDelimited.add("Package_Flight__r/Package_Market__r/Package__r/Package_Name__c|Package_Flight__r/Package_Market__r/Package__r/Market_Type__c");
		pdfCombinerFile.setFieldNamesPipeDelimited(fieldNamesPipeDelimited);

		List<String> fieldLabelsPipeDelimited = new ArrayList<String>();
		fieldLabelsPipeDelimited.add("Duration|Reach|Frequency");
		fieldLabelsPipeDelimited.add("Id");
		fieldLabelsPipeDelimited.add("Market Name");
		fieldLabelsPipeDelimited.add("Package Name|Market Type");
		pdfCombinerFile.setFieldLabelsPipeDelimited(fieldLabelsPipeDelimited);

		List<String> fieldTypesPipeDelimited = new ArrayList<String>();
		fieldTypesPipeDelimited.add("Double|Double|Double");
		fieldTypesPipeDelimited.add("String");
		fieldTypesPipeDelimited.add("String");
		fieldTypesPipeDelimited.add("String|String");
		pdfCombinerFile.setFieldTypesPipeDelimited(fieldTypesPipeDelimited);

		Reporter reporter = new Reporter("Data//CPQ_sample1.xml", "Data//CPQ_Proposal_Disclaimer__c.xml", "Data//CPQ_Map_Panel_Order_Pref__c.xml", null, false, false, false, false, pdfCombinerFile, true, false,null);
		showReporterInfo(reporter);
	}

	@Test
	public void getPDFCombinerContentEntryListTest() throws Exception {
		// initialize
		String[] lines = new String[]{ "Table of contents", " a,...,1", " a,...,2" , " a,...,3", " a 2,...,5", " a 2,...,6", " a 3,...,7", "Proposal" };

		// act
		List<PDFCombinerContentEntry> pdfCombinerContentEntryList = Reporter.getPDFCombinerContentEntryList(lines);

		// verify
		Assert.assertEquals(3, pdfCombinerContentEntryList.size());

		Assert.assertEquals("a", pdfCombinerContentEntryList.get(0).getTitle());
		Assert.assertEquals(1, pdfCombinerContentEntryList.get(0).getPageNumber());

		Assert.assertEquals("a 2", pdfCombinerContentEntryList.get(1).getTitle());
		Assert.assertEquals(5, pdfCombinerContentEntryList.get(1).getPageNumber());

		Assert.assertEquals("a 3", pdfCombinerContentEntryList.get(2).getTitle());
		Assert.assertEquals(7, pdfCombinerContentEntryList.get(2).getPageNumber());
	}

	private void showReporterInfo(Reporter reporter) throws Exception {
		System.out.println("   resulting files ");
		System.out.println(reporter.getGeneratedReport("pdf"));
		System.out.println(reporter.getGeneratedReport("xls"));
	}
}