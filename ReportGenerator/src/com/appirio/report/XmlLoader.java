package com.appirio.report;

import java.io.File;
import java.io.IOException;

import org.apache.commons.digester.Digester;
import org.xml.sax.SAXException;

public class XmlLoader {

	/**
	 * Parse xml proposal data using apache digister library.
	 * @param dataSourceFileName Path to the xml file.
	 * @return Packages object
	 */
	public static Packages parseXmlPackages(String dataSourceFileName) throws IOException, SAXException {

		// initialize digister
		Digester digester = new Digester();
		digester.setValidating(false);

		// map paths to objects and properties
		digester.addObjectCreate("QueryResult", Packages.class);

		digester.addObjectCreate("QueryResult/records", com.appirio.report.FlightLine.class);
		digester.addBeanPropertySetter("QueryResult/records/Additional_Cost__c", "additionalCost");
		digester.addBeanPropertySetter("QueryResult/records/Average_Daily_Spots__c", "averageDailySpots");
		digester.addBeanPropertySetter("QueryResult/records/City__c", "city");
		digester.addBeanPropertySetter("QueryResult/records/Comments__c", "comments");
		digester.addBeanPropertySetter("QueryResult/records/County__c", "county");
		digester.addBeanPropertySetter("QueryResult/records/CPP_0d__c", "cPP0d");
		digester.addBeanPropertySetter("QueryResult/records/Current_Copy__c", "currentCopy");
		digester.addBeanPropertySetter("QueryResult/records/Discount__c", "discount");
		digester.addBeanPropertySetter("QueryResult/records/Embellishments__c", "embellishments");
		digester.addBeanPropertySetter("QueryResult/records/Face_Direction__c", "faceDirection");
		digester.addBeanPropertySetter("QueryResult/records/Facing__c", "facing");
		digester.addBeanPropertySetter("QueryResult/records/Id", "id");
		digester.addBeanPropertySetter("QueryResult/records/Illumination_yn__c", "illuminationyn");
		digester.addBeanPropertySetter("QueryResult/records/Illumination__c", "illumination");
		digester.addBeanPropertySetter("QueryResult/records/In_Mkt_Imps__c", "inMktImps");
		digester.addBeanPropertySetter("QueryResult/records/In_Mkt_Perc_Comp__c", "inMktPercComp");
		digester.addBeanPropertySetter("QueryResult/records/In_Mkt_TRP__c", "inMktTRP");
		digester.addBeanPropertySetter("QueryResult/records/Location_Description__c", "locationDescription");
		digester.addBeanPropertySetter("QueryResult/records/Location__Latitude__s", "locationLatitudes");
		digester.addBeanPropertySetter("QueryResult/records/Location__Longitude__s", "locationLongitudes");
		digester.addBeanPropertySetter("QueryResult/records/MapLocation_Number__c", "mapLocationNumber");
		digester.addBeanPropertySetter("QueryResult/records/Media_Product__c", "mediaProduct");
		digester.addBeanPropertySetter("QueryResult/records/Network_Description__c", "networkDescription");
		digester.addBeanPropertySetter("QueryResult/records/Network_Name__c", "networkName");
		digester.addBeanPropertySetter("QueryResult/records/Network_Notes__c", "networkNotes");
		digester.addBeanPropertySetter("QueryResult/records/Net_Amount_Value__c", "netAmountValue");
		digester.addBeanPropertySetter("QueryResult/records/Number_of_Panels__c", "numberofPanels");
		digester.addBeanPropertySetter("QueryResult/records/Panel_Id_Label__c", "panelIdLabel");
		digester.addBeanPropertySetter("QueryResult/records/PlanTRP__c", "planTRP");
		digester.addBeanPropertySetter("QueryResult/records/Plan_Imps_Avg_Frequency__c", "planImpsAvgFrequency");
		digester.addBeanPropertySetter("QueryResult/records/Plan_Imps_Reach_Perc__c", "planImpsReachPerc");
		digester.addBeanPropertySetter("QueryResult/records/Production__c", "production");
		digester.addBeanPropertySetter("QueryResult/records/RecordTypeId", "recordTypeId");
		digester.addBeanPropertySetter("QueryResult/records/Ride_Order__c", "rideOrder");
		digester.addBeanPropertySetter("QueryResult/records/State__c", "state");
		digester.addBeanPropertySetter("QueryResult/records/TAB_Id__c", "tABId");
		digester.addBeanPropertySetter("QueryResult/records/Target_In_Market_Imps_000__c", "targetInMarketImps000");
		digester.addBeanPropertySetter("QueryResult/records/Target_Total_Imps_000__c", "targetTotalImps000");
		digester.addBeanPropertySetter("QueryResult/records/Tax_Amt__c", "taxAmt");
		digester.addBeanPropertySetter("QueryResult/records/Timing__c", "timing");
		digester.addBeanPropertySetter("QueryResult/records/TotalInMarketCPM_0d__c", "totalInMarketCPM0d");
		digester.addBeanPropertySetter("QueryResult/records/Total_Imps__c", "totalImps");
		digester.addBeanPropertySetter("QueryResult/records/Total_Price_0d__c", "totalPrice0d");
		digester.addBeanPropertySetter("QueryResult/records/Unit_Size__c", "unitSize");
		digester.addBeanPropertySetter("QueryResult/records/WeeklyInMarketImps__c", "weeklyInMarketImps");
		digester.addBeanPropertySetter("QueryResult/records/WeeklyMarketImps__c", "weeklyMarketImps");
		digester.addBeanPropertySetter("QueryResult/records/Weekly_In_Market_Target_Imps_000__c", "weeklyInMarketTargetImps000");
		digester.addBeanPropertySetter("QueryResult/records/Weekly_Total_18_Imps_000__c", "weeklyTotal18Imps000");
		digester.addBeanPropertySetter("QueryResult/records/Weekly_Total_18_Imps__c", "weeklyTotal18Imps");
		digester.addBeanPropertySetter("QueryResult/records/Weekly_Total_Target_Imps_000__c", "weeklyTotalTargetImps000");
		digester.addBeanPropertySetter("QueryResult/records/X4_Wk_Avg_Rate_per_Panel__c", "x4WkAvgRateperPanel");
		digester.addBeanPropertySetter("QueryResult/records/X4_Wk_Base_Rate__c", "x4WkBaseRate");
		digester.addBeanPropertySetter("QueryResult/records/X4_Wk_Floor__c", "x4WkFloor");
		digester.addBeanPropertySetter("QueryResult/records/X4_Wk_Proposed_Price__c", "x4WkProposedPrice");
		digester.addBeanPropertySetter("QueryResult/records/Zip__c", "zip");

		digester.addObjectCreate("QueryResult/records/Child_Flight_Lines__r/records", com.appirio.report.FlightLine.class);
		digester.addBeanPropertySetter("QueryResult/records/Child_Flight_Lines__r/records/Parent_Flight_Line__c", "parentFlightLine");
		digester.addBeanPropertySetter("QueryResult/records/Child_Flight_Lines__r/records/MapLocation_Number__c", "mapLocationNumber");
		digester.addBeanPropertySetter("QueryResult/records/Child_Flight_Lines__r/records/Location_Description__c", "locationDescription");
		digester.addBeanPropertySetter("QueryResult/records/Child_Flight_Lines__r/records/Panel_Id_Label__c", "panelIdLabel");
		digester.addBeanPropertySetter("QueryResult/records/Child_Flight_Lines__r/records/TAB_Id__c", "tABId");
		digester.addBeanPropertySetter("QueryResult/records/Child_Flight_Lines__r/records/Face_Direction__c", "faceDirection");
		digester.addBeanPropertySetter("QueryResult/records/Child_Flight_Lines__r/records/Weekly_Total_18_Imps__c", "weeklyTotal18Imps");
		digester.addBeanPropertySetter("QueryResult/records/Child_Flight_Lines__r/records/In_Mkt_Imps__c", "inMktImps");
		digester.addBeanPropertySetter("QueryResult/records/Child_Flight_Lines__r/records/Total_Imps__c", "totalImps");
		digester.addBeanPropertySetter("QueryResult/records/Child_Flight_Lines__r/records/In_Mkt_TRP__c", "inMktTRP");
		digester.addBeanPropertySetter("QueryResult/records/Child_Flight_Lines__r/records/PlanTRP__c", "planTRP");
		digester.addBeanPropertySetter("QueryResult/records/Child_Flight_Lines__r/records/Plan_Imps_Reach_Perc__c", "planImpsReachPerc");
		digester.addBeanPropertySetter("QueryResult/records/Child_Flight_Lines__r/records/Plan_Imps_Avg_Frequency__c", "planImpsAvgFrequency");

		digester.addObjectCreate("QueryResult/records/Package_Flight__r", com.appirio.report.Flight.class);
		digester.addBeanPropertySetter("QueryResult/records/Package_Flight__r/Id", "id");
		digester.addBeanPropertySetter("QueryResult/records/Package_Flight__r/Campaign_End_Date__c", "campaignEndDate");
		digester.addBeanPropertySetter("QueryResult/records/Package_Flight__r/Campaign_Start_Date__c", "campaignStartDate");
		digester.addBeanPropertySetter("QueryResult/records/Package_Flight__r/Division__c", "division");
		digester.addBeanPropertySetter("QueryResult/records/Package_Flight__r/Duration_And_Type__c", "durationAndType");
		digester.addBeanPropertySetter("QueryResult/records/Package_Flight__r/Flight_Comments__c", "flightComments");
		digester.addBeanPropertySetter("QueryResult/records/Package_Flight__r/Market_Name__c", "marketName");
		digester.addBeanPropertySetter("QueryResult/records/Package_Flight__r/Market_Type__c", "marketType");
		digester.addBeanPropertySetter("QueryResult/records/Package_Flight__r/Media_Category__c", "mediaCategory");
		digester.addBeanPropertySetter("QueryResult/records/Package_Flight__r/Name", "name");
		digester.addBeanPropertySetter("QueryResult/records/Package_Flight__r/Package_Comments__c", "packageComments");
		digester.addBeanPropertySetter("QueryResult/records/Package_Flight__r/Package_Name__c", "packageName");
		digester.addBeanPropertySetter("QueryResult/records/Package_Flight__r/Type__c", "type");
		digester.addBeanPropertySetter("QueryResult/records/Package_Flight__r/Target__c", "target");
		digester.addBeanPropertySetter("QueryResult/records/Package_Flight__r/Target_Population__c", "targetPopulation");

		digester.addObjectCreate("QueryResult/records/Package_Flight__r/Package_Market__r", com.appirio.report.Market.class);
		digester.addBeanPropertySetter("QueryResult/records/Package_Flight__r/Package_Market__r/Id", "id");
		digester.addBeanPropertySetter("QueryResult/records/Package_Flight__r/Package_Market__r/MarketName__c", "marketName");
		digester.addBeanPropertySetter("QueryResult/records/Package_Flight__r/Package_Market__r/Weekly_Total_18_Imps__c", "weeklyTotal18Imps");
		digester.addBeanPropertySetter("QueryResult/records/Package_Flight__r/Package_Market__r/In_Mkt_Imps__c", "inMktImps");
		digester.addBeanPropertySetter("QueryResult/records/Package_Flight__r/Package_Market__r/Target_Total_Imps__c", "targetTotalImps");
		digester.addBeanPropertySetter("QueryResult/records/Package_Flight__r/Package_Market__r/In_Mkt_TRP__c", "inMktTRP");
		digester.addBeanPropertySetter("QueryResult/records/Package_Flight__r/Package_Market__r/Plan_TRP__c", "planTRP");
		digester.addBeanPropertySetter("QueryResult/records/Package_Flight__r/Package_Market__r/Total_Price__c", "totalPrice");
		digester.addBeanPropertySetter("QueryResult/records/Package_Flight__r/Package_Market__r/CPM__c", "cPM");
		digester.addBeanPropertySetter("QueryResult/records/Package_Flight__r/Package_Market__r/CPP__c", "cPP");

		digester.addObjectCreate("QueryResult/records/Package_Flight__r/Package_Market__r/Package__r", com.appirio.report.Package.class);
		digester.addBeanPropertySetter("QueryResult/records/Package_Flight__r/Package_Market__r/Package__r/Id", "id");
		digester.addBeanPropertySetter("QueryResult/records/Package_Flight__r/Package_Market__r/Package__r/Package_Name__c", "packageName");
		digester.addBeanPropertySetter("QueryResult/records/Package_Flight__r/Package_Market__r/Package__r/Weekly_Total_18_Imps__c", "weeklyTotal18Imps");
		digester.addBeanPropertySetter("QueryResult/records/Package_Flight__r/Package_Market__r/Package__r/In_Mkt_Imps__c", "inMktImps");
		digester.addBeanPropertySetter("QueryResult/records/Package_Flight__r/Package_Market__r/Package__r/Target_Total_Imps__c", "targetTotalImps");
		digester.addBeanPropertySetter("QueryResult/records/Package_Flight__r/Package_Market__r/Package__r/In_Mkt_TRP__c", "inMktTRP");
		digester.addBeanPropertySetter("QueryResult/records/Package_Flight__r/Package_Market__r/Package__r/Plan_TRP__c", "planTRP");
		digester.addBeanPropertySetter("QueryResult/records/Package_Flight__r/Package_Market__r/Package__r/Total_Price__c", "totalPrice");
		digester.addBeanPropertySetter("QueryResult/records/Package_Flight__r/Package_Market__r/Package__r/CPM__c", "cPM");
		digester.addBeanPropertySetter("QueryResult/records/Package_Flight__r/Package_Market__r/Package__r/CPP__c", "cPP");

		digester.addSetNext("QueryResult/records", "processFlightLine");
		digester.addSetNext("QueryResult/records/Child_Flight_Lines__r/records", "addChildFlightLine");
		digester.addSetNext("QueryResult/records/Package_Flight__r", "setFlight");
		digester.addSetNext("QueryResult/records/Package_Flight__r/Package_Market__r", "setMarket");
		digester.addSetNext("QueryResult/records/Package_Flight__r/Package_Market__r/Package__r", "setPackage");

		// parse
		File inputFile = new File(dataSourceFileName);
		Packages packages = (Packages) digester.parse(inputFile);

		return packages;
	}

	/**
	 * Parse xml map panel order preferences.
	 * @param dataSourceFileName Path to the xml file.
	 * @return MapPanelOrderPreferences object.
	 * @throws IOException
	 * @throws SAXException
	 */
	public static MapPanelOrderPreferences parseXmlMapPanelOrderPreferences(String dataSourceFileName) throws IOException, SAXException {

		// initialize digister
		Digester digester = new Digester();
		digester.setValidating(false);

		// map paths to objects and properties
		digester.addObjectCreate("QueryResult", MapPanelOrderPreferences.class);

		digester.addObjectCreate("QueryResult/records", MapPanelOrderPreference.class);
		digester.addBeanPropertySetter("QueryResult/records/Sort_Sequence__c", "sortSequence");
		digester.addBeanPropertySetter("QueryResult/records/Flight__c", "flight");
		digester.addBeanPropertySetter("QueryResult/records/Panel__c", "panel");
		digester.addSetNext("QueryResult/records", "addMapPanelOrderPreference");

		// parse
		File inputFile = new File(dataSourceFileName);
		MapPanelOrderPreferences mapPanelOrderPreferences = (MapPanelOrderPreferences) digester.parse(inputFile);

		return mapPanelOrderPreferences;
	}
}
