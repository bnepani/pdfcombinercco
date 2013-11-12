package com.appirio.report;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;

import net.sf.jxls.exception.ParsePropertyException;
import net.sf.jxls.transformer.XLSTransformer;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.xml.sax.SAXException;

import com.appirio.PDFCombinerFile;

/**
 * @author jesus
 *
 */
/**
 * @author jesus
 *
 */
public class ExcelReporter extends Reporter {

	/**
	 * Set path to template. Create these environment variables:
	 * 		XLS_TEMPLATE_DIR: directory to templates
	 * 		XLS_TEMPLATE_FILE: template filename
	 */
	private static final String XlsTemplateFileName = System.getenv("XLS_TEMPLATE_DIR") + File.separator + System.getenv("XLS_TEMPLATE_FILE");
	
	/**
	 * XML proposal data source
	 */
	private String dataSourceFileName;

	/**
	 * XML disclaimers data source
	 */
	private String disclaimerDataSourceFileName;

	/**
	 * XML map panel order pref file name
	 */
	private String mapPanelOrderPrefDataSourceFileName;
	
	/**
	 * Flag to show/hide network details (true = show, false = hide)
	 */
	private boolean excludeNetworkDetails;

	/**
	 * Flag to show/hide summary (true = show, false = hide)
	 */
	private boolean showTotalProgramSummary;

	/**
	 * Flag to show/hide summary (true = show, false = hide)
	 */
	private boolean showIndividualMarketSummary;

	/**
	 * Flag to show/hide summary (true = show, false = hide)
	 */
	private boolean showIndividualFlightSummary;

	/**
	 * Parameters to specify field names and labels
	 */
	private PDFCombinerFile pdfCombinerFile;

	private HashMap<String, String> flightLineColumnLabelHashMap = null;

	/**
	 * Allowed fields per report type
	 */
	private Map<String, String> audienceReportColumnMap;

	/**
	 * Allowed fields per report type
	 */
	private Map<String, String> locationReportColumnMap;

	/**
	 * Allowed fields per report type
	 */
	private Map<String, String> rotaryReportColumnMap;

	/**
	 * Allowed fields per report type
	 */
	private Map<String, String> networkReportColumnMap;

	/**
	 * Allowed fields per report type
	 */
	private Map<String, String> networkDetailReportColumnMap;
	
	/**
	 * Allowed fields per report type
	 */
	private Map<String, String> flightSummaryReportColumnMap;

	/**
	 * Allowed fields per report type
	 */
	private Map<String, String> marketSummaryReportColumnMap;

	/**
	 * Allowed fields per report type
	 */
	private Map<String, String> packageSummaryReportColumnMap;

	/**
	 * Calculated report column names and labels (from the allowed fields, this list indicates which fields/labels users should see)
	 */
	private List<String> audienceReportColumnNames;

	/**
	 * Calculated report column names and labels (from the allowed fields, this list indicates which fields/labels users should see)
	 */
	private List<String> audienceReportColumnLabels;

	/**
	 * Calculated report column names and labels (from the allowed fields, this list indicates which fields/labels users should see)
	 */
	private List<String> locationReportColumnNames;

	/**
	 * Calculated report column names and labels (from the allowed fields, this list indicates which fields/labels users should see)
	 */
	private List<String> locationReportColumnLabels;

	/**
	 * Calculated report column names and labels (from the allowed fields, this list indicates which fields/labels users should see)
	 */
	private List<String> rotaryReportColumnNames;

	/**
	 * Calculated report column names and labels (from the allowed fields, this list indicates which fields/labels users should see)
	 */
	private List<String> rotaryReportColumnLabels;

	/**
	 * Calculated report column names and labels (from the allowed fields, this list indicates which fields/labels users should see)
	 */
	private List<String> networkReportColumnNames;

	/**
	 * Calculated report column names and labels (from the allowed fields, this list indicates which fields/labels users should see)
	 */
	private List<String> networkReportColumnLabels;

	/**
	 * Calculated report column names and labels (from the allowed fields, this list indicates which fields/labels users should see)
	 */
	private List<String> networkDetailReportColumnNames;

	/**
	 * Calculated report column names and labels (from the allowed fields, this list indicates which fields/labels users should see)
	 */
	private List<String> networkDetailReportColumnLabels;

	/**
	 * Calculated report column names and labels (from the allowed fields, this list indicates which fields/labels users should see)
	 */
	private List<String> flightSummaryReportColumnNames;

	/**
	 * Calculated report column names and labels (from the allowed fields, this list indicates which fields/labels users should see)
	 */
	private List<String> flightSummaryColumnLabels;

	/**
	 * Calculated report column names and labels (from the allowed fields, this list indicates which fields/labels users should see)
	 */
	private List<String> marketSummaryReportColumnNames;

	/**
	 * Calculated report column names and labels (from the allowed fields, this list indicates which fields/labels users should see)
	 */
	private List<String> marketSummaryColumnLabels;

	/**
	 * Calculated report column names and labels (from the allowed fields, this list indicates which fields/labels users should see)
	 */
	private List<String> packageSummaryReportColumnNames;

	/**
	 * Calculated report column names and labels (from the allowed fields, this list indicates which fields/labels users should see)
	 */
	private List<String> packageSummaryColumnLabels;

	/**
	 * Specify which list to return
	 */
	private enum ReportColumn {
		Names, Labels
	}

	private PdfReporter reporter;

	/**
	 * Generate output
	 */
	public void generate(String proposalOutputFileName) throws ParsePropertyException, InvalidFormatException, IOException, SAXException {

		// load
		Packages packages = XmlLoader.parseXmlPackages(getDataSourceFileName());
		MapPanelOrderPreferences mapPanelOrderPreferences = XmlLoader.parseXmlMapPanelOrderPreferences(getMapPanelOrderPrefDataSourceFileName());

		// update flight line map panel locations
		setMapPanelSortSequence(packages, mapPanelOrderPreferences);

		// sort flight lines by map location (sort position)
		packages.sortFlightLines();

		// debug info
		//dumpMapPanelOrderPreferences(mapPanelOrderPreferences);
		//dumpPackages(packages);

		// TODO load disclaimers
		//XmlLoader.parseXmlDisclaimers(disclaimerDataSourceFileName, packages, TestMode);

		// render
		render(XlsTemplateFileName, packages, proposalOutputFileName);
	}

	/**
	 * Build proposal report in excel file format
	 * @param dataSourceFileName						XML proposal data source
	 * @param disclaimerDataSourceFileName
	 * @param mapPanelOrderPrefDataSourceFileName
	 * @param excludeNetworkDetails
	 * @param showTotalProgramSummary					Flag to show/hide summary (true = show, false = hide)
	 * @param showIndividualMarketSummary				Flag to show/hide summary (true = show, false = hide)
	 * @param showIndividualFlightSummary				Flag to show/hide summary (true = show, false = hide)
	 * @param pdfCombinerFile							Parameters to specify field names and labels
	 * @throws SAXException 
	 * @throws IOException 
	 * @throws InvalidFormatException 
	 * @throws ParsePropertyException 
	 */
	public ExcelReporter(String dataSourceFileName,
			String disclaimerDataSourceFileName,
			String mapPanelOrderPrefDataSourceFileName,
			Boolean excludeNetworkDetails,
			Boolean showTotalProgramSummary,
			Boolean showIndividualMarketSummary,
			Boolean showIndividualFlightSummary,
			PDFCombinerFile pdfCombinerFile) throws ParsePropertyException, InvalidFormatException, IOException, SAXException {

		setDataSourceFileName(dataSourceFileName);
		setDisclaimerDataSourceFileName(disclaimerDataSourceFileName);
		setMapPanelOrderPrefDataSourceFileName(mapPanelOrderPrefDataSourceFileName);
		setExcludeNetworkDetails(excludeNetworkDetails);
		setShowTotalProgramSummary(showTotalProgramSummary);
		setShowIndividualMarketSummary(showIndividualMarketSummary);
		setShowIndividualFlightSummary(showIndividualFlightSummary);
		setPdfCombinerFile(pdfCombinerFile);

		// set flight line column - label
		setFlightLineColumnLabelHashMap(getKeyValueHashMap(
				getPdfCombinerFile().getFieldNamesPipeDelimited().get(0),
				getPdfCombinerFile().getFieldLabelsPipeDelimited().get(0)));
		
		generate(getGeneratedReport("xlsx"));
	}

	/**
	 * @return Flag to show/hide summary (true = show, false = hide)
	 */
	public boolean getShowTotalProgramSummary() {
		return showTotalProgramSummary;
	}

	/**
	 * Set flag to show/hide summary (true = show, false = hide)
	 */
	public void setShowTotalProgramSummary(boolean showTotalProgramSummary) {
		this.showTotalProgramSummary = showTotalProgramSummary;
	}

	/**
	 * @return Flag to show/hide summary (true = show, false = hide)
	 */
	public boolean getShowIndividualMarketSummary() {
		return showIndividualMarketSummary;
	}

	/**
	 * Set flag to show/hide summary (true = show, false = hide)
	 */
	public void setShowIndividualMarketSummary(
			boolean showIndividualMarketSummary) {
		this.showIndividualMarketSummary = showIndividualMarketSummary;
	}

	/**
	 * @return Flag to show/hide summary (true = show, false = hide)
	 */
	public boolean getShowIndividualFlightSummary() {
		return showIndividualFlightSummary;
	}

	/**
	 * Set flag to show/hide summary (true = show, false = hide)
	 */
	public void setShowIndividualFlightSummary(
			boolean showIndividualFlightSummary) {
		this.showIndividualFlightSummary = showIndividualFlightSummary;
	}

	/**
	 * @return Calculated report column labels (from the allowed fields, this list indicates which fields/labels users should see)
	 */
	public List<String> getFlightSummaryColumnLabels() {

		if(flightSummaryColumnLabels == null) {
			Map<String, String> reportColumnMap = getFlightSummaryReportColumnMap();

			Map<String, String> userColumnMap = getFlightLineColumnLabelHashMap();

			flightSummaryColumnLabels = getReportColumnItems(ReportColumn.Labels, reportColumnMap, userColumnMap, true);
		}

		return flightSummaryColumnLabels;
	}

	/**
	 * @return Calculated report column names (from the allowed fields, this list indicates which fields/labels users should see)
	 */
	public List<String> getFlightSummaryReportColumnNames() {

		if(flightSummaryReportColumnNames == null) {
			Map<String, String> reportColumnMap = getFlightSummaryReportColumnMap();
	
			Map<String, String> userColumnMap = getFlightLineColumnLabelHashMap();

			flightSummaryReportColumnNames = getReportColumnItems(ReportColumn.Names, reportColumnMap, userColumnMap, true);
		}

		return flightSummaryReportColumnNames;
	}

	/**
	 * @return Calculated report column labels (from the allowed fields, this list indicates which fields/labels users should see)
	 */
	public List<String> getMarketSummaryColumnLabels() {

		if(marketSummaryColumnLabels == null) {
			Map<String, String> reportColumnMap = getMarketSummaryReportColumnMap();

			Map<String, String> userColumnMap = getFlightLineColumnLabelHashMap();

			marketSummaryColumnLabels = getReportColumnItems(ReportColumn.Labels, reportColumnMap, userColumnMap, true);
		}

		return marketSummaryColumnLabels;
	}

	/**
	 * @return Calculated report column names (from the allowed fields, this list indicates which fields/labels users should see)
	 */
	public List<String> getMarketSummaryReportColumnNames() {

		if(marketSummaryReportColumnNames == null) {
			Map<String, String> reportColumnMap = getMarketSummaryReportColumnMap();
	
			Map<String, String> userColumnMap = getFlightLineColumnLabelHashMap();

			marketSummaryReportColumnNames = getReportColumnItems(ReportColumn.Names, reportColumnMap, userColumnMap, true);
		}

		return marketSummaryReportColumnNames;
	}

	/**
	 * @return Calculated report column labels (from the allowed fields, this list indicates which fields/labels users should see)
	 */
	public List<String> getPackageSummaryColumnLabels() {

		if(packageSummaryColumnLabels == null) {
			Map<String, String> reportColumnMap = getPackageSummaryReportColumnMap();

			Map<String, String> userColumnMap = getFlightLineColumnLabelHashMap();

			packageSummaryColumnLabels = getReportColumnItems(ReportColumn.Labels, reportColumnMap, userColumnMap, true);
		}

		return packageSummaryColumnLabels;
	}

	/**
	 * @return Calculated report column names (from the allowed fields, this list indicates which fields/labels users should see)
	 */
	public List<String> getPackageSummaryReportColumnNames() {

		if(packageSummaryReportColumnNames == null) {
			Map<String, String> reportColumnMap = getPackageSummaryReportColumnMap();
	
			Map<String, String> userColumnMap = getFlightLineColumnLabelHashMap();

			packageSummaryReportColumnNames = getReportColumnItems(ReportColumn.Names, reportColumnMap, userColumnMap, true);
		}

		return packageSummaryReportColumnNames;
	}

	/**
	 * @return Calculated report column names (from the allowed fields, this list indicates which fields/labels users should see)
	 */
	public List<String> getAudienceReportColumnNames() {

		if(audienceReportColumnNames == null) {
			Map<String, String> reportColumnMap = getAudienceReportColumnMap();
	
			Map<String, String> userColumnMap = getFlightLineColumnLabelHashMap();
			
			audienceReportColumnNames = getReportColumnItems(ReportColumn.Names, reportColumnMap, userColumnMap, false);
		}

		return audienceReportColumnNames;
	}

	/**
	 * @return Calculated report column labels (from the allowed fields, this list indicates which fields/labels users should see)
	 */
	public List<String> getAudienceReportColumnLabels() {

		if(audienceReportColumnLabels == null) {
			Map<String, String> reportColumnMap = getAudienceReportColumnMap();
	
			Map<String, String> userColumnMap = getFlightLineColumnLabelHashMap();

			audienceReportColumnLabels = getReportColumnItems(ReportColumn.Labels, reportColumnMap, userColumnMap, false);
		}

		return audienceReportColumnLabels;
	}

	/**
	 * @return Calculated report column names (from the allowed fields, this list indicates which fields/labels users should see)
	 */
	public List<String> getLocationReportColumnNames() {

		if(locationReportColumnNames == null) {
			Map<String, String> reportColumnMap = getLocationReportColumnMap();
	
			Map<String, String> userColumnMap = getFlightLineColumnLabelHashMap();

			locationReportColumnNames = getReportColumnItems(ReportColumn.Names, reportColumnMap, userColumnMap, false);
		}

		return locationReportColumnNames;
	}

	/**
	 * @return Calculated report column labels (from the allowed fields, this list indicates which fields/labels users should see)
	 */
	public List<String> getLocationReportColumnLabels() {

		if(locationReportColumnLabels == null) {
			Map<String, String> reportColumnMap = getLocationReportColumnMap();
	
			Map<String, String> userColumnMap = getFlightLineColumnLabelHashMap();

			locationReportColumnLabels = getReportColumnItems(ReportColumn.Labels, reportColumnMap, userColumnMap, false);
		}

		return locationReportColumnLabels;
	}

	/**
	 * @return Calculated report column names (from the allowed fields, this list indicates which fields/labels users should see)
	 */
	public List<String> getRotaryReportColumnNames() {

		if(rotaryReportColumnNames == null) {
			Map<String, String> reportColumnMap = getRotaryReportColumnMap();

			Map<String, String> userColumnMap = getFlightLineColumnLabelHashMap();

			rotaryReportColumnNames = getReportColumnItems(ReportColumn.Names, reportColumnMap, userColumnMap, false);
		}

		return rotaryReportColumnNames;
	}

	/**
	 * @return Calculated report column labels (from the allowed fields, this list indicates which fields/labels users should see)
	 */
	public List<String> getRotaryReportColumnLabels() {

		if(rotaryReportColumnLabels == null) {
			Map<String, String> reportColumnMap = getRotaryReportColumnMap();
	
			Map<String, String> userColumnMap = getFlightLineColumnLabelHashMap();

			rotaryReportColumnLabels = getReportColumnItems(ReportColumn.Labels, reportColumnMap, userColumnMap, false);
		}

		return rotaryReportColumnLabels;
	}

	/**
	 * @return Calculated report column names (from the allowed fields, this list indicates which fields/labels users should see)
	 */
	public List<String> getNetworkReportColumnNames() {

		if(networkReportColumnNames == null) {
			Map<String, String> reportColumnMap = getNetworkReportColumnMap();

			Map<String, String> userColumnMap = getFlightLineColumnLabelHashMap();

			networkReportColumnNames = getReportColumnItems(ReportColumn.Names, reportColumnMap, userColumnMap, false);
		}

		return networkReportColumnNames;
	}

	/**
	 * @return Calculated report column labels (from the allowed fields, this list indicates which fields/labels users should see)
	 */
	public List<String> getNetworkReportColumnLabels() {

		if(networkReportColumnLabels == null) {
			Map<String, String> reportColumnMap = getNetworkReportColumnMap();
	
			Map<String, String> userColumnMap = getFlightLineColumnLabelHashMap();

			networkReportColumnLabels = getReportColumnItems(ReportColumn.Labels, reportColumnMap, userColumnMap, false);
		}

		return networkReportColumnLabels;
	}

	/**
	 * @return Calculated report column names (from the allowed fields, this list indicates which fields/labels users should see)
	 */
	public List<String> getNetworkDetailReportColumnNames() {

		if(networkDetailReportColumnNames == null) {
			Map<String, String> reportColumnMap = getNetworkDetailReportColumnMap();

			Map<String, String> userColumnMap = getFlightLineColumnLabelHashMap();

			networkDetailReportColumnNames = getReportColumnItems(ReportColumn.Names, reportColumnMap, userColumnMap, true);
		}

		return networkDetailReportColumnNames;
	}

	/**
	 * @return Calculated report column labels (from the allowed fields, this list indicates which fields/labels users should see)
	 */
	public List<String> getNetworkDetailReportColumnLabels() {

		if(networkDetailReportColumnLabels == null) {
			Map<String, String> reportColumnMap = getNetworkDetailReportColumnMap();
	
			Map<String, String> userColumnMap = getFlightLineColumnLabelHashMap();

			networkDetailReportColumnLabels = getReportColumnItems(ReportColumn.Labels, reportColumnMap, userColumnMap, true);
		}

		return networkDetailReportColumnLabels;
	}

	/**
	 * @return Calculate which report column names or labels (from the allowed fields, this list indicates which fields/labels users should see)
	 */
	public List<String> getReportColumnItems(ReportColumn reportColumn, Map<String, String> reportColumnMap, Map<String, String> userColumnMap, boolean isSummary) {

		// object that will be returned
		Map<String, String> reportColumnItemMap = new LinkedHashMap<String, String>();

		// for each user column...
		for(String userColumn : userColumnMap.keySet()) {
			// if its selected in report columns...
			if(!userColumn.equals("X4_Wk_Base_Rate__c") && !userColumn.equals("X4_Wk_Floor__c") && reportColumnMap.containsKey(userColumn)) {
				// set local field name and label set by the user
				String buyTypeColumn = reportColumnMap.get(userColumn);
				String value = userColumnMap.get(userColumn); 
				reportColumnItemMap.put(buyTypeColumn, value);
			}
		}
		// add internal use field in last is selected
		if(!isSummary) {
			if(userColumnMap.containsKey("X4_Wk_Base_Rate__c")) {
				String buyTypeColumn = reportColumnMap.get("X4_Wk_Base_Rate__c");
				String value = userColumnMap.get("X4_Wk_Base_Rate__c"); 
				reportColumnItemMap.put(buyTypeColumn, value);
			}
			if(userColumnMap.containsKey("X4_Wk_Floor__c")) {
				String buyTypeColumn = reportColumnMap.get("X4_Wk_Floor__c");
				String value = userColumnMap.get("X4_Wk_Floor__c"); 
				reportColumnItemMap.put(buyTypeColumn, value);
			}
		}
		// if names should be returned
		if(reportColumn == ReportColumn.Names) {
			// return keys
			return new ArrayList<String>(reportColumnItemMap.keySet());
		} else {
			// return values (labels)
			return new ArrayList<String>(reportColumnItemMap.values());
		}
	}

	public List<String> retrieveDisclaimers() {
		//System.out.println("getDisclaimers(String param1)");

		List<String>disclaimers = new ArrayList<String>();

		disclaimers.add("Hello World");

		return disclaimers;
	}

	/**
	 * Get disclaimers for a flight, division and media category
	 * @param flightName
	 * @param division
	 * @param mediaCategory
	 * @return Set of discaimers
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 * @throws ParseException
	 */
	public Set<String> getDisclaimers(String flightName, String division, String mediaCategory) throws ParserConfigurationException, SAXException, IOException, ParseException {

		return getReporter().getDisclaimerStore().getValidDisclaimers(flightName, division, mediaCategory);
	}

	/**
	 * Build Reporter object and runs its associated DisclaimersDataExpression build passing the disclaimers datasource.
	 */
	private PdfReporter getReporter() throws ParserConfigurationException, SAXException, IOException, ParseException {

		if(reporter == null) {
			reporter = new PdfReporter();
			reporter.new DisclaimersDataExpression(this.getDisclaimerDataSourceFileName());
		}

		return reporter;
	}

	/**
	 * Update flight line map panel locations (sort field value that comes from map panel order pref and it is accessed by map location number field key)
	 * @param packages
	 * @param mapPanelOrderPreferences
	 */
	private void setMapPanelSortSequence(Packages packages, MapPanelOrderPreferences mapPanelOrderPreferences) {

		//System.out.println("setMapPanelSortSequence(): begin");
		//System.out.println("MapPanelOrderPreferences: size" + mapPanelOrderPreferences.getMapPanelOrderPreferences().size());

		// for each flight line...
		for(FlightLine flightLine : packages.getFlightLines()) {

			// find object by key
			MapPanelOrderPreference mapPanelOrderPreference = mapPanelOrderPreferences.getMapPanelOrderPreferences().get(flightLine.getMapLocationNumber());

			boolean mapLocationExists = mapPanelOrderPreference != null;

			//System.out.println("   mapLocationExists: " + mapLocationExists);

			// if it exists...
			if(mapLocationExists) {

				// get map location in integer type and set value in flight line
				Integer mapLocationNumberSort = mapPanelOrderPreference.getSortSequence() == null ? 0 : Integer.parseInt(mapPanelOrderPreference.getSortSequence());

				//System.out.println("   mapLocationNumberSort: " + mapLocationNumberSort);

				flightLine.setMapLocationNumberSort(mapLocationNumberSort);
			}
		}

		//System.out.println("setMapPanelSortSequence(): end");
	}

	/**
	 * Used for debugging map panel order pref values
	 * @param mapPanelOrderPreferences
	 */
	/*private void dumpMapPanelOrderPreferences(MapPanelOrderPreferences mapPanelOrderPreferences) {
		System.out.println("***************************");
		System.out.println("   dumpMapPanelOrderPreferences()");
		System.out.println("   size: " + mapPanelOrderPreferences.getMapPanelOrderPreferences().size());

		for(MapPanelOrderPreference mapPanelOrderPreference : mapPanelOrderPreferences.getMapPanelOrderPreferences().values()) {
			System.out.println("      flight: " + mapPanelOrderPreference.getFlight());
			System.out.println("      panel: " + mapPanelOrderPreference.getPanel());
		}
	}*/

	/**
	 * Used for debugging packages values
	 * @param mapPanelOrderPreferences
	 */
	/*private void dumpPackages(Packages packages) {
		System.out.println("***************************");
		System.out.println("   dumpPackages()");
		System.out.println("   size: " + packages.getFlightLines().size());

		for(FlightLine flightLine : packages.getFlightLines()) {
			com.appirio.report.Package thePackage = flightLine.getFlight().getMarket().getPackage();
			System.out.println("   package: " + thePackage.getPackageName());
			System.out.println("   getSortKey: " + flightLine.getSortKey());
		}
	}*/

	/**
	 * Render output
	 * @param proposalTemplateFileName XLS template
	 * @param packages	Packages collection object
	 * @param proposalOutputFileName Output XLS file name
	 */
	private void render(String proposalTemplateFileName, Packages packages, String proposalOutputFileName) throws ParsePropertyException, InvalidFormatException, IOException {

		// initialize beans
		Map<String, Object> beans = new HashMap<String, Object>();
		beans.put("flightLines", packages.getFlightLines());
		beans.put("exporter", this);

		// run xls transformation
		XLSTransformer transformer = new XLSTransformer();
		transformer.transformXLS(proposalTemplateFileName, beans, proposalOutputFileName);
	}

	/**
	 * @return Map of xml field name and local bean property for this buy type
	 */
	private Map<String, String> getAudienceReportColumnMap() {

		if(audienceReportColumnMap == null) {

			audienceReportColumnMap = new LinkedHashMap<String, String>();

			audienceReportColumnMap.put("Additional_Cost__c", "additionalCost");
			audienceReportColumnMap.put("Comments__c", "comments");
			audienceReportColumnMap.put("CPP_0d__c", "cPP0d");
			audienceReportColumnMap.put("Discount__c", "discount");
			audienceReportColumnMap.put("In_Mkt_Imps__c", "inMktImps");
			audienceReportColumnMap.put("In_Mkt_Perc_Comp__c", "inMktPercComp");
			audienceReportColumnMap.put("In_Mkt_TRP__c", "inMktTRP");
			audienceReportColumnMap.put("Net_Amount_Value__c", "netAmountValue");
			audienceReportColumnMap.put("Number_of_Panels__c", "numberofPanels");
			audienceReportColumnMap.put("Package_Flight__r/Campaign_End_Date__c", "packageFlight_CampaignEndDate");
			audienceReportColumnMap.put("Package_Flight__r/Campaign_Start_Date__c", "packageFlight_CampaignStartDate");
			audienceReportColumnMap.put("Package_Flight__r/Division__c", "packageFlight_Division");
			audienceReportColumnMap.put("Package_Flight__r/Duration_And_Type__c", "packageFlight_DurationAndType");
			audienceReportColumnMap.put("Package_Flight__r/Flight_Comments__c", "packageFlight_FlightComments");
			audienceReportColumnMap.put("Package_Flight__r/Market_Name__c", "packageFlight_MarketName");
			audienceReportColumnMap.put("Package_Flight__r/Market_Type__c", "packageFlight_MarketType");
			audienceReportColumnMap.put("Package_Flight__r/Media_Category__c", "packageFlight_MediaCategory");
			audienceReportColumnMap.put("Package_Flight__r/Name", "packageFlight_Name");
			audienceReportColumnMap.put("Package_Flight__r/Package_Comments__c", "packageFlight_PackageComments");
			audienceReportColumnMap.put("Package_Flight__r/Package_Name__c", "packageFlight_PackageName");
			audienceReportColumnMap.put("Package_Flight__r/Target__c", "packageFlight_Target");
			audienceReportColumnMap.put("Package_Flight__r/Target_Population__c", "packageFlight_TargetPopulation");
			audienceReportColumnMap.put("Plan_Imps_Avg_Frequency__c", "planImpsAvgFrequency");
			audienceReportColumnMap.put("Plan_Imps_Reach_Perc__c", "planImpsReachPerc");
			audienceReportColumnMap.put("PlanTRP__c", "planTRP");
			audienceReportColumnMap.put("Production__c", "production");
			audienceReportColumnMap.put("Target_In_Market_Imps_000__c", "targetInMarketImps000");
			audienceReportColumnMap.put("Target_Total_Imps_000__c", "targetTotalImps000");
			audienceReportColumnMap.put("Tax_Amt__c", "taxAmt");
			audienceReportColumnMap.put("Timing__c", "timing");
			audienceReportColumnMap.put("Total_Imps__c", "totalImps");
			audienceReportColumnMap.put("Total_Price_0d__c", "totalPrice0d");
			audienceReportColumnMap.put("TotalInMarketCPM_0d__c", "totalInMarketCPM0d");
			audienceReportColumnMap.put("Weekly_In_Market_Target_Imps_000__c", "weeklyInMarketTargetImps000");
			audienceReportColumnMap.put("Weekly_Total_18_Imps__c", "weeklyTotal18Imps");
			audienceReportColumnMap.put("Weekly_Total_18_Imps__c", "weeklyTotal18Imps");
			audienceReportColumnMap.put("Weekly_Total_18_Imps_000__c", "weeklyTotal18Imps000");
			audienceReportColumnMap.put("Weekly_Total_Target_Imps_000__c", "weeklyTotalTargetImps000");
			audienceReportColumnMap.put("WeeklyInMarketImps__c", "weeklyInMarketImps");
			audienceReportColumnMap.put("WeeklyMarketImps__c", "weeklyMarketImps");
			audienceReportColumnMap.put("X4_Wk_Avg_Rate_per_Panel__c", "x4WkAvgRateperPanel");
			audienceReportColumnMap.put("X4_Wk_Base_Rate__c", "x4WkBaseRate");
			audienceReportColumnMap.put("X4_Wk_Floor__c", "x4WkFloor");
			audienceReportColumnMap.put("X4_Wk_Proposed_Price__c", "x4WkProposedPrice");
		}

		return audienceReportColumnMap;
	}

	/**
	 * @return Map of xml field name and local bean property for this buy type
	 */
	private Map<String, String> getLocationReportColumnMap() {

		if(locationReportColumnMap == null) {

			locationReportColumnMap = new LinkedHashMap<String, String>();

			locationReportColumnMap.put("Additional_Cost__c", "additionalCost");
			locationReportColumnMap.put("Average_Daily_Spots__c", "averageDailySpots");
			locationReportColumnMap.put("City__c", "city");
			locationReportColumnMap.put("Comments__c", "comments");
			locationReportColumnMap.put("County__c", "county");
			locationReportColumnMap.put("CPP_0d__c", "cPP0d");
			locationReportColumnMap.put("Current_Copy__c", "currentCopy");
			locationReportColumnMap.put("Discount__c", "discount");
			locationReportColumnMap.put("Embellishments__c", "embellishments");
			locationReportColumnMap.put("Face_Direction__c", "faceDirection");
			locationReportColumnMap.put("Facing__c", "facing");
			locationReportColumnMap.put("Illumination__c", "illumination");
			locationReportColumnMap.put("Illumination_yn__c", "illuminationyn");
			locationReportColumnMap.put("In_Mkt_Imps__c", "inMktImps");
			locationReportColumnMap.put("In_Mkt_Perc_Comp__c", "inMktPercComp");
			locationReportColumnMap.put("In_Mkt_TRP__c", "inMktTRP");
			locationReportColumnMap.put("Location__Latitude__s", "locationLatitudes");
			locationReportColumnMap.put("Location__Longitude__s", "locationLongitudes");
			locationReportColumnMap.put("Location_Description__c", "locationDescription");
			locationReportColumnMap.put("MapLocation_Number__c", "mapLocationNumber");
			locationReportColumnMap.put("Media_Product__c", "mediaProduct");
			locationReportColumnMap.put("Net_Amount_Value__c", "netAmountValue");
			locationReportColumnMap.put("Package_Flight__r/Campaign_End_Date__c", "packageFlight_CampaignEndDate");
			locationReportColumnMap.put("Package_Flight__r/Campaign_Start_Date__c", "packageFlight_CampaignStartDate");
			locationReportColumnMap.put("Package_Flight__r/Division__c", "packageFlight_Division");
			locationReportColumnMap.put("Package_Flight__r/Duration_And_Type__c", "packageFlight_DurationAndType");
			locationReportColumnMap.put("Package_Flight__r/Flight_Comments__c", "packageFlight_FlightComments");
			locationReportColumnMap.put("Package_Flight__r/Market_Name__c", "packageFlight_MarketName");
			locationReportColumnMap.put("Package_Flight__r/Market_Type__c", "packageFlight_MarketType");
			locationReportColumnMap.put("Package_Flight__r/Media_Category__c", "packageFlight_MediaCategory");
			locationReportColumnMap.put("Package_Flight__r/Name", "packageFlight_Name");
			locationReportColumnMap.put("Package_Flight__r/Package_Comments__c", "packageFlight_PackageComments");
			locationReportColumnMap.put("Package_Flight__r/Package_Name__c", "packageFlight_PackageName");
			locationReportColumnMap.put("Package_Flight__r/Target__c", "packageFlight_Target");
			locationReportColumnMap.put("Package_Flight__r/Target_Population__c", "packageFlight_TargetPopulation");
			locationReportColumnMap.put("Package_Flight__r/Type__c", "packageFlight_Type");
			locationReportColumnMap.put("Panel_Id_Label__c", "panelIdLabel");
			locationReportColumnMap.put("Plan_Imps_Avg_Frequency__c", "planImpsAvgFrequency");
			locationReportColumnMap.put("Plan_Imps_Reach_Perc__c", "planImpsReachPerc");
			locationReportColumnMap.put("PlanTRP__c", "planTRP");
			locationReportColumnMap.put("Production__c", "production");
			locationReportColumnMap.put("Ride_Order__c", "rideOrder");
			locationReportColumnMap.put("State__c", "state");
			locationReportColumnMap.put("TAB_Id__c", "tABId");
			locationReportColumnMap.put("Target_In_Market_Imps_000__c", "targetInMarketImps000");
			locationReportColumnMap.put("Target_Total_Imps_000__c", "targetTotalImps000");
			locationReportColumnMap.put("Tax_Amt__c", "taxAmt");
			locationReportColumnMap.put("Timing__c", "timing");
			locationReportColumnMap.put("Total_Imps__c", "totalImps");
			locationReportColumnMap.put("Total_Price_0d__c", "totalPrice0d");
			locationReportColumnMap.put("TotalInMarketCPM_0d__c", "totalInMarketCPM0d");
			locationReportColumnMap.put("Unit_Size__c", "unitSize");
			locationReportColumnMap.put("Weekly_In_Market_Target_Imps_000__c", "weeklyInMarketTargetImps000");
			locationReportColumnMap.put("Weekly_Total_18_Imps__c", "weeklyTotal18Imps");
			locationReportColumnMap.put("Weekly_Total_18_Imps_000__c", "weeklyTotal18Imps000");
			locationReportColumnMap.put("Weekly_Total_Target_Imps_000__c", "weeklyTotalTargetImps000");
			locationReportColumnMap.put("WeeklyInMarketImps__c", "weeklyInMarketImps");
			locationReportColumnMap.put("WeeklyMarketImps__c", "weeklyMarketImps");
			locationReportColumnMap.put("X4_Wk_Avg_Rate_per_Panel__c", "x4WkAvgRateperPanel");
			locationReportColumnMap.put("X4_Wk_Base_Rate__c", "x4WkBaseRate");
			locationReportColumnMap.put("X4_Wk_Floor__c", "x4WkFloor");
			locationReportColumnMap.put("X4_Wk_Proposed_Price__c", "x4WkProposedPrice");
			locationReportColumnMap.put("Zip__c", "zip");
		}

		return locationReportColumnMap;
	}

	/**
	 * @return Map of xml field name and local bean property for this buy type
	 */
	private Map<String, String> getRotaryReportColumnMap() {

		if(rotaryReportColumnMap == null) {

			rotaryReportColumnMap = new LinkedHashMap<String, String>();

			rotaryReportColumnMap.put("CPP_0d__c", "cPP0d");
			rotaryReportColumnMap.put("In_Mkt_Imps__c", "inMktImps");
			rotaryReportColumnMap.put("In_Mkt_TRP__c", "inMktTRP");
			rotaryReportColumnMap.put("Network_Name__c", "networkName");
			rotaryReportColumnMap.put("Network_Name__c", "networkName");
			rotaryReportColumnMap.put("Number_of_Panels__c", "numberofPanels");
			rotaryReportColumnMap.put("Package_Flight__r/Campaign_End_Date__c", "packageFlight_CampaignEndDate");
			rotaryReportColumnMap.put("Package_Flight__r/Campaign_Start_Date__c", "packageFlight_CampaignStartDate");
			rotaryReportColumnMap.put("Package_Flight__r/Division__c", "packageFlight_Division");
			rotaryReportColumnMap.put("Package_Flight__r/Duration_And_Type__c", "packageFlight_DurationAndType");
			rotaryReportColumnMap.put("Package_Flight__r/Flight_Comments__c", "packageFlight_FlightComments");
			rotaryReportColumnMap.put("Package_Flight__r/Name", "packageFlight_Name");
			rotaryReportColumnMap.put("Package_Flight__r/Package_Name__c", "packageFlight_PackageName");
			rotaryReportColumnMap.put("Package_Flight__r/Target__c", "packageFlight_Target");
			rotaryReportColumnMap.put("Plan_Imps_Avg_Frequency__c", "planImpsAvgFrequency");
			rotaryReportColumnMap.put("Plan_Imps_Reach_Perc__c", "planImpsReachPerc");
			rotaryReportColumnMap.put("PlanTRP__c", "planTRP");
			rotaryReportColumnMap.put("Total_Imps__c", "totalImps");
			rotaryReportColumnMap.put("Total_Price_0d__c", "totalPrice0d");
			rotaryReportColumnMap.put("TotalInMarketCPM_0d__c", "totalInMarketCPM0d");
			rotaryReportColumnMap.put("Weekly_Total_18_Imps__c", "weeklyTotal18Imps");
			rotaryReportColumnMap.put("X4_Wk_Proposed_Price__c", "x4WkProposedPrice");
		}

		return rotaryReportColumnMap;
	}

	/**
	 * @return Map of xml field name and local bean property for this buy type
	 */
	private Map<String, String> getNetworkReportColumnMap() {

		if(networkReportColumnMap == null) {

			networkReportColumnMap = new LinkedHashMap<String, String>();

			networkReportColumnMap.put("Average_Daily_Spots__c", "averageDailySpots");
			networkReportColumnMap.put("CPP_0d__c", "cPP0d");
			networkReportColumnMap.put("Discount__c", "discount");
			networkReportColumnMap.put("In_Mkt_Imps__c", "inMktImps");
			networkReportColumnMap.put("In_Mkt_TRP__c", "inMktTRP");
			networkReportColumnMap.put("In_Mkt_Perc_Comp__c", "inMktPercComp");
			networkReportColumnMap.put("Additional_Cost__c", "additionalCost");
			networkReportColumnMap.put("Network_Description__c", "networkDescription");
			networkReportColumnMap.put("Network_Name__c", "networkName");
			networkReportColumnMap.put("Network_Notes__c", "networkNotes");
			networkReportColumnMap.put("Number_of_Panels__c", "numberofPanels");
			networkReportColumnMap.put("Package_Flight__r/Campaign_End_Date__c", "packageFlight_CampaignEndDate");
			networkReportColumnMap.put("Package_Flight__r/Campaign_Start_Date__c", "packageFlight_CampaignStartDate");
			networkReportColumnMap.put("Package_Flight__r/Division__c", "packageFlight_Division");
			networkReportColumnMap.put("Package_Flight__r/Duration_And_Type__c", "packageFlight_DurationAndType");
			networkReportColumnMap.put("Package_Flight__r/Flight_Comments__c", "packageFlight_FlightComments");
			networkReportColumnMap.put("Package_Flight__r/Media_Category__c", "packageFlight_MediaCategory");
			networkReportColumnMap.put("Package_Flight__r/Name", "packageFlight_Name");
			networkReportColumnMap.put("Package_Flight__r/Package_Comments__c", "packageFlight_PackageComments");
			networkReportColumnMap.put("Package_Flight__r/Package_Name__c", "packageFlight_PackageName");
			networkReportColumnMap.put("Package_Flight__r/Target__c", "packageFlight_Target");
			networkReportColumnMap.put("Package_Flight__r/Target_Population__c", "packageFlight_TargetPopulation");
			networkReportColumnMap.put("Plan_Imps_Avg_Frequency__c", "planImpsAvgFrequency");
			networkReportColumnMap.put("Plan_Imps_Reach_Perc__c", "planImpsReachPerc");
			networkReportColumnMap.put("PlanTRP__c", "planTRP");
			networkReportColumnMap.put("Total_Imps__c", "totalImps");
			networkReportColumnMap.put("Total_Price_0d__c", "totalPrice0d");
			networkReportColumnMap.put("TotalInMarketCPM_0d__c", "totalInMarketCPM0d");
			networkReportColumnMap.put("Weekly_Total_18_Imps__c", "weeklyTotal18Imps");
			networkReportColumnMap.put("X4_Wk_Base_Rate__c", "x4WkBaseRate");
			networkReportColumnMap.put("X4_Wk_Floor__c", "x4WkFloor");
			networkReportColumnMap.put("X4_Wk_Proposed_Price__c", "x4WkProposedPrice");
		}

		return networkReportColumnMap;
	}

	/**
	 * @return Map of xml field name and local bean property for this buy type
	 */
	private Map<String, String> getNetworkDetailReportColumnMap() {

		if(networkDetailReportColumnMap == null) {

			networkDetailReportColumnMap = new LinkedHashMap<String, String>();

			networkDetailReportColumnMap.put("Parent_Flight_Line__c", "parentFlightLine");
			networkDetailReportColumnMap.put("MapLocation_Number__c", "mapLocationNumber");
			networkDetailReportColumnMap.put("Location_Description__c", "locationDescription");
			networkDetailReportColumnMap.put("Panel_Id_Label__c", "panelIdLabel");
			networkDetailReportColumnMap.put("TAB_Id__c", "tABId");
			networkDetailReportColumnMap.put("Face_Direction__c", "faceDirection");
			networkDetailReportColumnMap.put("Weekly_Total_18_Imps__c", "weeklyTotal18Imps");
			networkDetailReportColumnMap.put("In_Mkt_Imps__c", "inMktImps");
			networkDetailReportColumnMap.put("Total_Imps__c", "totalImps");
			networkDetailReportColumnMap.put("In_Mkt_TRP__c", "inMktTRP");
			networkDetailReportColumnMap.put("In_Mkt_Perc_Comp__c", "inMktPercComp");
			networkDetailReportColumnMap.put("PlanTRP__c", "planTRP");
			networkDetailReportColumnMap.put("Plan_Imps_Reach_Perc__c", "planImpsReachPerc");
			networkDetailReportColumnMap.put("Plan_Imps_Avg_Frequency__c", "planImpsAvgFrequency");
		}

		return networkDetailReportColumnMap;
	}

	/**
	 * @return Map of xml field name and local bean property for this buy type
	 */
	private Map<String, String> getFlightSummaryReportColumnMap() {

		if(flightSummaryReportColumnMap == null) {

			flightSummaryReportColumnMap = new LinkedHashMap<String, String>();

			flightSummaryReportColumnMap.put("Weekly_Total_18_Imps__c", "weeklyTotal18Imps");
			flightSummaryReportColumnMap.put("Weekly_Total_18_Imps_000__c", "weeklyTotal18Imps000");
			flightSummaryReportColumnMap.put("In_Mkt_Imps__c", "inMktImps");
			flightSummaryReportColumnMap.put("Target_In_Market_Imps_000__c", "targetInMarketImps000");
			flightSummaryReportColumnMap.put("Total_Imps__c", "totalImps");
			flightSummaryReportColumnMap.put("Target_Total_Imps_000__c", "targetTotalImps000");
			flightSummaryReportColumnMap.put("WeeklyMarketImps__c", "weeklyMarketImps");
			flightSummaryReportColumnMap.put("Weekly_Total_Target_Imps_000__c", "weeklyTotalTargetImps000");
			flightSummaryReportColumnMap.put("WeeklyInMarketImps__c", "weeklyInMarketImps");
			flightSummaryReportColumnMap.put("Weekly_In_Market_Target_Imps_000__c", "weeklyInMarketTargetImps000");
			flightSummaryReportColumnMap.put("In_Mkt_TRP__c", "inMktTRP");
			flightSummaryReportColumnMap.put("PlanTRP__c", "planTRP");
			flightSummaryReportColumnMap.put("Plan_Imps_Reach_Perc__c", "planImpsReachPerc");
			flightSummaryReportColumnMap.put("Plan_Imps_Avg_Frequency__c", "planImpsAvgFrequency");
			flightSummaryReportColumnMap.put("Net_Amount_Value__c", "netAmountValue");
			flightSummaryReportColumnMap.put("Total_Price_0d__c", "totalPrice0d");
			flightSummaryReportColumnMap.put("TotalInMarketCPM_0d__c", "totalInMarketCPM0d");
			flightSummaryReportColumnMap.put("CPP_0d__c", "cPP0d");
			flightSummaryReportColumnMap.put("Production__c", "production");
			flightSummaryReportColumnMap.put("Additional_Cost__c", "additionalCost");
			flightSummaryReportColumnMap.put("Tax_Amt__c", "taxAmt");
			flightSummaryReportColumnMap.put("Discount__c", "discount");
		}

		return flightSummaryReportColumnMap;
	}

	/**
	 * @return Map of xml field name and local bean property for this buy type
	 */
	private Map<String, String> getMarketSummaryReportColumnMap() {

		if(marketSummaryReportColumnMap == null) {

			marketSummaryReportColumnMap = new LinkedHashMap<String, String>();

			marketSummaryReportColumnMap.put("Weekly_Total_18_Imps__c", "packageFlight_packageMarket_weeklyTotal18Imps");
			marketSummaryReportColumnMap.put("In_Mkt_Imps__c", "packageFlight_packageMarket_inMktImps");
			marketSummaryReportColumnMap.put("Total_Imps__c", "packageFlight_packageMarket_targetTotalImps");
			marketSummaryReportColumnMap.put("In_Mkt_TRP__c", "packageFlight_packageMarket_inMktTRP");
			marketSummaryReportColumnMap.put("Plan_TRP__c", "packageFlight_packageMarket_planTRP");
			marketSummaryReportColumnMap.put("Total_Price_0d__c", "packageFlight_packageMarket_totalPrice");
			marketSummaryReportColumnMap.put("TotalInMarketCPM_0d__c", "packageFlight_packageMarket_cPM");
			marketSummaryReportColumnMap.put("CPP_0d__c", "packageFlight_packageMarket_cPP");
			marketSummaryReportColumnMap.put("Plan_Imps_Reach_Perc__c", "packageFlight_packageMarket_reach");
			marketSummaryReportColumnMap.put("Plan_Imps_Avg_Frequency__c", "packageFlight_packageMarket_freq");
			
		}

		return marketSummaryReportColumnMap;
	}

	/**
	 * @return Map of xml field name and local bean property for this buy type
	 */
	private Map<String, String> getPackageSummaryReportColumnMap() {

		if(packageSummaryReportColumnMap == null) {

			packageSummaryReportColumnMap = new LinkedHashMap<String, String>();

			packageSummaryReportColumnMap.put("Weekly_Total_18_Imps__c", "packageFlight_packageMarket_package_weeklyTotal18Imps");
			packageSummaryReportColumnMap.put("In_Mkt_Imps__c", "packageFlight_packageMarket_package_inMktImps");
			packageSummaryReportColumnMap.put("Total_Imps__c", "packageFlight_packageMarket_package_targetTotalImps");
			packageSummaryReportColumnMap.put("In_Mkt_TRP__c", "packageFlight_packageMarket_package_inMktTRP");
			packageSummaryReportColumnMap.put("Plan_TRP__c", "packageFlight_packageMarket_package_planTRP");
			packageSummaryReportColumnMap.put("Total_Price_0d__c", "packageFlight_packageMarket_package_totalPrice");
			packageSummaryReportColumnMap.put("TotalInMarketCPM_0d__c", "packageFlight_packageMarket_package_cPM");
			packageSummaryReportColumnMap.put("CPP_0d__c", "packageFlight_packageMarket_package_cPP");
		}

		return packageSummaryReportColumnMap;
	}

	/**
	 * Property getter/setter. Please see property doc. for more info.
	 */
	private String getDataSourceFileName() {
		return dataSourceFileName;
	}

	/**
	 * Property getter/setter. Please see property doc. for more info.
	 */
	private void setDataSourceFileName(String dataSourceFileName) {
		this.dataSourceFileName = dataSourceFileName;
	}

	/**
	 * Property getter/setter. Please see property doc. for more info.
	 */
	private PDFCombinerFile getPdfCombinerFile() {
		return pdfCombinerFile;
	}

	/**
	 * Property getter/setter. Please see property doc. for more info.
	 */
	private void setPdfCombinerFile(PDFCombinerFile pdfCombinerFile) {
		this.pdfCombinerFile = pdfCombinerFile;
	}

	/**
	 * Property getter/setter. Please see property doc. for more info.
	 */
	private HashMap<String, String> getFlightLineColumnLabelHashMap() {
		return flightLineColumnLabelHashMap;
	}

	/**
	 * Property getter/setter. Please see property doc. for more info.
	 */
	private void setFlightLineColumnLabelHashMap(
			HashMap<String, String> flightLineColumnLabelHashMap) {
		this.flightLineColumnLabelHashMap = flightLineColumnLabelHashMap;
	}

	/**
	 * Property getter/setter. Please see property doc. for more info.
	 */
	private String getMapPanelOrderPrefDataSourceFileName() {
		return mapPanelOrderPrefDataSourceFileName;
	}

	/**
	 * Property getter/setter. Please see property doc. for more info.
	 */
	private void setMapPanelOrderPrefDataSourceFileName(String mapPanelOrderPrefDataSourceFileName) {
		this.mapPanelOrderPrefDataSourceFileName = mapPanelOrderPrefDataSourceFileName;
	}

	/**
	 * Property getter/setter. Please see property doc. for more info.
	 */
	public String getDisclaimerDataSourceFileName() {
		return disclaimerDataSourceFileName;
	}

	/**
	 * Property getter/setter. Please see property doc. for more info.
	 */
	public void setDisclaimerDataSourceFileName(
			String disclaimerDataSourceFileName) {
		this.disclaimerDataSourceFileName = disclaimerDataSourceFileName;
	}

	/**
	 * Property getter/setter. Please see property doc. for more info.
	 */
	public boolean isExcludeNetworkDetails() {
		return excludeNetworkDetails;
	}

	/**
	 * Property getter/setter. Please see property doc. for more info.
	 */
	public void setExcludeNetworkDetails(boolean excludeNetworkDetails) {
		this.excludeNetworkDetails = excludeNetworkDetails;
	}
}
