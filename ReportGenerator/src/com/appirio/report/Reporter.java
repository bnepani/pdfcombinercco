package com.appirio.report;

import static net.sf.dynamicreports.report.builder.DynamicReports.cmp;
import static net.sf.dynamicreports.report.builder.DynamicReports.col;
import static net.sf.dynamicreports.report.builder.DynamicReports.field;
import static net.sf.dynamicreports.report.builder.DynamicReports.grp;
import static net.sf.dynamicreports.report.builder.DynamicReports.report;
import static net.sf.dynamicreports.report.builder.DynamicReports.sbt;
import static net.sf.dynamicreports.report.builder.DynamicReports.stl;
import static net.sf.dynamicreports.report.builder.DynamicReports.type;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import net.sf.dynamicreports.jasper.base.export.JasperXlsExporter;
import net.sf.dynamicreports.jasper.builder.JasperReportBuilder;
import net.sf.dynamicreports.jasper.builder.export.Exporters;
import net.sf.dynamicreports.jasper.builder.export.JasperXlsExporterBuilder;
import net.sf.dynamicreports.report.base.AbstractScriptlet;
import net.sf.dynamicreports.report.base.expression.AbstractSimpleExpression;
import net.sf.dynamicreports.report.base.expression.AbstractValueFormatter;
import net.sf.dynamicreports.report.builder.DynamicReports;
import net.sf.dynamicreports.report.builder.FieldBuilder;
import net.sf.dynamicreports.report.builder.Units;
import net.sf.dynamicreports.report.builder.column.ComponentColumnBuilder;
import net.sf.dynamicreports.report.builder.column.TextColumnBuilder;
import net.sf.dynamicreports.report.builder.component.HorizontalListBuilder;
import net.sf.dynamicreports.report.builder.component.SubreportBuilder;
import net.sf.dynamicreports.report.builder.component.VerticalListBuilder;
import net.sf.dynamicreports.report.builder.datatype.BigDecimalType;
import net.sf.dynamicreports.report.builder.group.ColumnGroupBuilder;
import net.sf.dynamicreports.report.builder.group.CustomGroupBuilder;
import net.sf.dynamicreports.report.builder.style.PaddingBuilder;
import net.sf.dynamicreports.report.builder.style.PenBuilder;
import net.sf.dynamicreports.report.builder.style.ReportStyleBuilder;
import net.sf.dynamicreports.report.builder.style.StyleBuilder;
import net.sf.dynamicreports.report.constant.GroupHeaderLayout;
import net.sf.dynamicreports.report.constant.HorizontalAlignment;
import net.sf.dynamicreports.report.constant.PageOrientation;
import net.sf.dynamicreports.report.definition.ReportParameters;
import net.sf.dynamicreports.report.exception.DRException;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.data.JRXmlDataSource;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.appirio.PDFCombinerContentEntry;
import com.appirio.PDFCombinerFile;

/**
 * @author jesus
 * 
 *         This generates a Report in PDF or XLS format. Uses xml as a
 *         datasource and can be defined which fields and summary options to
 *         use. Uses DynamicReports java library
 *         (http://www.dynamicreports.org/)
 */
public class Reporter {

	private static final String QUERY_RESULT_RECORDS_WITH_CHILD_FLIGHT_LINES = "/QueryResult/records[Child_Flight_Lines__r = boolean(1) and RecordTypeId = boolean(0)]";

	private static final String QUERY_RESULT_RECORDS_UNIQUE_PACKAGE = "/QueryResult/records/Package_Flight__r/Package_Market__r/Package__r[not(@url=preceding::Package_Flight__r/Package_Market__r/Package__r/@url)]";

	private static final String QUERY_RESULT_RECORDS_UNIQUE_MARKET = "/QueryResult/records/Package_Flight__r/Package_Market__r[not(@url=preceding::Package_Flight__r/Package_Market__r/@url)]";
	
	private static final String QUERY_RESULT_RECORDS_UNIQUE_FLIGHT = "/QueryResult/records/Package_Flight__r[not(@url=preceding::Package_Flight__r/@url)]";

	private static final String QUERY_RESULT_RECORDS_RECORD_TYPE_ID_BOOLEAN_1 = "/QueryResult/records[RecordTypeId = boolean(1)]";

	private static final String QUERY_RESULT_RECORDS_RECORD_TYPE_ID_BOOLEAN_0 = "/QueryResult/records[RecordTypeId = boolean(0)]";

	// unique identifier for generated files
	private String uniqueId = null;

	// generated pdf directory (generated at runtime)
	private String generatedPDFDir = null;

	// page number where shipping instructions start
	private Integer shippingInstructionsPageNumber = null;

	// page number where shipping instructions start
	private boolean doesShippingInstructionsExists = false;

	// map panel order pref file name
	private String mapPanelOrderPrefDataSourceFileName;
	
	private String detailDataSourceFileName;

	// summary level enum
	private enum SummaryLevelEnum { Market, Package };

	// column hashmap (key = column name, value = label)
	private HashMap<String, String> flightLineColumnLabelHashMap = null;

	private Set<String> autoDisclaimersSet = new HashSet<String>();
	DisclaimerStore disclaimerStore = new DisclaimerStore();
	
	private Set<String> packageIds = new LinkedHashSet<String>();
	private Map<String, Set<String>> divisionsMap = new HashMap<String, Set<String>>();
	private Map<String, Set<String>> marketNamesMap = new HashMap<String, Set<String>>();
	private Map<String, Set<String>> marketTypesMap = new HashMap<String, Set<String>>();
	private Map<String, Set<String>> packageNamesMap = new HashMap<String, Set<String>>();
	private Map<String, List<String>> flightNamesMap = new HashMap<String, List<String>>();
	private Map<String, Set<String>> flightTypesMap = new HashMap<String, Set<String>>();
	private Map<String, Set<String>> startDatesMap = new HashMap<String, Set<String>>();
	private Map<String, Set<String>> endDatesMap = new HashMap<String, Set<String>>();
	private Map<String, Set<String>> durationsMap = new HashMap<String, Set<String>>();
	private Map<String, Set<String>> targetsMap = new HashMap<String, Set<String>>();
	private Map<String, Set<String>> targetPopulationsMap = new HashMap<String, Set<String>>();
	private Map<String, Set<String>> packageCommentsMap = new HashMap<String, Set<String>>();
	private Map<String, List<String>> flightCommentsMap = new HashMap<String, List<String>>();

	private StyleBuilder flightHeaderStyle;

	private StyleBuilder columnTitleStyle;

	private boolean locationMapExists = false;

	// record count (flight lines in total)
	private Integer recordCount = 0;

	public List<DisclaimerStore.DisclaimerWrapper> validAllDisclaimersList = new ArrayList<DisclaimerStore.DisclaimerWrapper> ();
	public List<String> validDisclaimersListDisplay = new ArrayList<String>();
	/*
	 * public void readfile(String fileName){ BufferedReader br = null; try {
	 * String sCurrentLine; br = new BufferedReader(new FileReader(fileName));
	 * System.out.println("--------------Reading Start----------"); while
	 * ((sCurrentLine = br.readLine()) != null) {
	 * System.out.println(sCurrentLine); }
	 * System.out.println("--------------Reading End----------"); } catch
	 * (IOException e) { e.printStackTrace(); } finally { try { if (br != null)
	 * br.close(); } catch (IOException ex) { ex.printStackTrace(); } } }
	 */
	public Reporter(String dataSourceFileName,
			String disclaimerDataSourceFileName,
			String mapPanelOrderPrefDataSourceFileName,
			String shippingInstructionsDataSourceFileName,
			Boolean excludeNetworkDetails,
			Boolean showTotalProgramSummary,
			Boolean showIndividualMarketSummary,
			Boolean showIndividualFlightSummary,
			PDFCombinerFile pdfCombinerFile, boolean exportAsPdf,
			boolean exportAsExcel, List<PDFCombinerFile> appendixes) throws Exception {
		System.out.println("Reporter: begin");

		System.out
				.println("   --- Information for repeating report using ReporterTest ---");
		System.out.println("   dataSourceFileName : " + dataSourceFileName);
		System.out.println("   disclaimerDataSourceFileName : "
				+ disclaimerDataSourceFileName);
		System.out.println("   shippingInstructionsDataSourceFileName : "
				+ shippingInstructionsDataSourceFileName);
		System.out.println("   mapPanelOrderPrefDataSourceFileName: "
				+ mapPanelOrderPrefDataSourceFileName);
		System.out.println("   excludeNetworkDetails : "
				+ excludeNetworkDetails);
		System.out.println("   showTotalProgramSummary : "
				+ showTotalProgramSummary);
		System.out.println("   showIndividualMarketSummary : "
				+ showIndividualMarketSummary);
		System.out.println("   showIndividualFlightSummary : "
				+ showIndividualFlightSummary);
		System.out.println("   getFieldNamesPipeDelimited : "
				+ pdfCombinerFile.getFieldNamesPipeDelimited());
		System.out.println("   getFieldLabelsPipeDelimited: "
				+ pdfCombinerFile.getFieldLabelsPipeDelimited());
		System.out.println("   getFieldTypesPipeDelimited : "
				+ pdfCombinerFile.getFieldTypesPipeDelimited());
		System.out.println("   exportAsPdf : " + exportAsPdf);
		System.out.println("   exportAsExcel : " + exportAsExcel);

		locationMapExists = hasLocationMapAttachments(appendixes);
		
		// set flight line column - label
		setFlightLineColumnLabelHashMap(getKeyValueHashMap(pdfCombinerFile
				.getFieldNamesPipeDelimited().get(0), pdfCombinerFile
				.getFieldLabelsPipeDelimited().get(0)));

		// set flight column - label
		/*setFlightColumnLabelHashMap(getKeyValueHashMap(pdfCombinerFile
				.getFieldNamesPipeDelimited().get(1), pdfCombinerFile
				.getFieldLabelsPipeDelimited().get(1)));*/

		// set the file name for map panel order pref
		setMapPanelOrderPrefDataSourceFileName(mapPanelOrderPrefDataSourceFileName);
		setDetailDataSourceFileName(dataSourceFileName);

		//
		System.out.println("   ------- labels -----------");
		for (String label : pdfCombinerFile.getFieldLabelsPipeDelimited()) {
			System.out.println("   label: " + label);
		}

		try {
			PenBuilder grayThinLine = stl.penThin().setLineColor(
					Color.LIGHT_GRAY);

			PaddingBuilder padding = stl.padding(Units.inch(0.03));

			StyleBuilder boldStyle = stl.style().bold();
			if (exportAsExcel) {
				boldStyle = stl.style().bold().setFontSize(9)/*.setFontName("Arial")*/;
			}
			StyleBuilder boldRightAlignStyle = stl.style(boldStyle)
					.setHorizontalAlignment(HorizontalAlignment.RIGHT);
			StyleBuilder boldCenteredStyle = stl.style(boldStyle);
			columnTitleStyle = stl.style(boldCenteredStyle)
					.setPadding(padding).setBorder(grayThinLine).setFontSize(8)
					.setForegroundColor(new Color(255, 255, 255))
					.setBackgroundColor(new Color(0, 153, 216))
					.setHorizontalAlignment(HorizontalAlignment.CENTER);
			if (exportAsExcel) {
				columnTitleStyle = stl.style(boldCenteredStyle)
						.setPadding(padding).setFontSize(10)
						//.setFontName("Arial")
						.setForegroundColor(new Color(255, 255, 255))
						.setBackgroundColor(new Color(0, 153, 216))
						.setHorizontalAlignment(HorizontalAlignment.CENTER);
			}
			StyleBuilder groupHeaderStyleNew = stl.style().setFontSize(0)
					.setBackgroundColor(new Color(255, 255, 255));

			setFlightHeaderStyle(stl.style(boldRightAlignStyle)
					.setLeftPadding(Units.inch(0.5)).setPadding(padding)
					.setFontSize(8));
			if (exportAsExcel) {
				flightHeaderStyle = stl.style(boldRightAlignStyle)
						.setLeftPadding(Units.inch(0.5)).setPadding(padding)
						.setFontSize(10)/*.setFontName("Arial")*/;
			}
			StyleBuilder flightHeaderValueStyle = stl.style()
					.setPadding(padding).setFontSize(8);
			if (exportAsExcel) {
				flightHeaderValueStyle = stl.style().setPadding(padding)
						.setFontSize(10)/*.setFontName("Arial")*/;
			}
			StyleBuilder columnStyle = stl.style().setPadding(padding)
					.setBorder(grayThinLine).setFontSize(8);
			if (exportAsExcel) {
				columnStyle = stl.style().setPadding(padding).setFontSize(10)
						/*.setFontName("Arial")*/;
			}
			StyleBuilder summaryStyle = stl.style().setPadding(padding).setFontSize(10);
			if(exportAsExcel) {
				summaryStyle = stl.style().setPadding(padding).setFontSize(9)/*.setFontName("Arial")*/;
			}
			// readfile(dataSourceFileName);
			// ================ define report
			JRXmlDataSource dataSource = new JRXmlDataSource(
					dataSourceFileName, "/QueryResult/records");

			// count records
			recordCount = 0;
			while(dataSource.next()) {
				recordCount++;
			}
			dataSource.moveFirst();

			new DisclaimersDataExpression(disclaimerDataSourceFileName);
			// disclaimer DataSources
			JRXmlDataSource afterLocationListPageDisclaimerDataSource = new JRXmlDataSource(
					disclaimerDataSourceFileName,
					"/QueryResult/records[Output_Location__c='After Location List Page']");
			JRXmlDataSource afterRailStationDisplaySummaryDisclaimerDataSource = new JRXmlDataSource(
					disclaimerDataSourceFileName,
					"/QueryResult/records[Output_Location__c='After Rail Station Display Summary']");
			JRXmlDataSource afterRailTransitPosterSummaryDisclaimerDataSource = new JRXmlDataSource(
					disclaimerDataSourceFileName,
					"/QueryResult/records[Output_Location__c='After Rail Transit Poster Summary']");
			JRXmlDataSource afterSummaryPageDisclaimerDataSource = new JRXmlDataSource(
					disclaimerDataSourceFileName,
					"/QueryResult/records[Output_Location__c='After Summary Page']");
			JRXmlDataSource afterTaxiDisplaySummaryDisclaimerDataSource = new JRXmlDataSource(
					disclaimerDataSourceFileName,
					"/QueryResult/records[Output_Location__c='After Taxi Display Summary']");
			JRXmlDataSource programSummaryDisclaimerDataSource = new JRXmlDataSource(
					disclaimerDataSourceFileName,
					"/QueryResult/records[Output_Location__c='Program Summary']");
			JRXmlDataSource programSummaryIncludingIndividualMarketSummaryPagesDisclaimerDataSource = new JRXmlDataSource(
					disclaimerDataSourceFileName,
					"/QueryResult/records[contains(Output_Location__c, '(including individual market summary pages)')]");

			JRXmlDataSource commentDataSource = new JRXmlDataSource(
					dataSourceFileName,
					"/QueryResult/records/Package_Flight__r/Flight_Comments__c");
			JRXmlDataSource flightCommentDataSource = new JRXmlDataSource(
					dataSourceFileName,
					"/QueryResult/records/Package_Flight__r/Package_Comments__c");

			// JRXmlDataSource audienceDataSource = new
			// JRXmlDataSource(dataSourceFileName,
			// "/QueryResult/records[Package_Flight__r/Flight_Comments__c='Audience']");
			// JRXmlDataSource locationDataSource = new
			// JRXmlDataSource(dataSourceFileName,
			// "/QueryResult/records[Package_Flight__r/Flight_Comments__c='Location']");
			// create report
			
/*			JasperReportBuilder b = report()
					.setPageFormat(Units.inch(11.7), Units.inch(7.5),
							PageOrientation.PORTRAIT)
					.setPageMargin(DynamicReports.margin(20))
			// .setColumnTitleStyle(columnTitleStyle)
					.setDataSource(dataSource);
*/		
			//Anjali 6 sept- increased this to accomodate max 34 cols in pdf. else they get wrapped up.	
			JasperReportBuilder b = report()
					.setPageFormat(Units.inch(23.4), Units.inch(16.5),
							PageOrientation.PORTRAIT)
					.setPageMargin(DynamicReports.margin(20))
			// .setColumnTitleStyle(columnTitleStyle)
					.setDataSource(dataSource);

			// create an expression that gets information for a previous record
			// at market flight level
			PackageMarketFlightPreviousRecordExpression packageMarketFlightPreviousRecordExpression = new PackageMarketFlightPreviousRecordExpression();
			PreviousFlightRecordHadExpression previousFlightRecordHadExpression = new PreviousFlightRecordHadExpression(
					packageMarketFlightPreviousRecordExpression);
			PreviousFlightRecordDidntHaveExpression previousFlightRecordDidntHaveExpression = new PreviousFlightRecordDidntHaveExpression(
					packageMarketFlightPreviousRecordExpression);

			// information for a previous record (flight line)
			FlightLinePreviousRecordExpression flightLinePreviousRecordExpression = new FlightLinePreviousRecordExpression();

			// indicates that report has a market level defined
			boolean hasMarketLevel = false;

			// add a couple of lines as a first summary to create space between
			// detail groups
			// and summaries
			// b.summary(cmp.text(""), cmp.text(""));

			// add detail level
			Integer fieldLevel = 1;
			// ============ create a vertical table of flight titles: begin
			boolean isDetail = fieldLevel - 1 == 0;
			/*
			 * JasperReportBuilder audienceReport = report()
			 * .setDataSource(audienceDataSource); JasperReportBuilder
			 * locationReport = report() .setDataSource(locationDataSource);
			 */
			if (isDetail) {
				/*
				 * String textValue = ""; if((pdfCombinerFile.getTitle() != null
				 * && pdfCombinerFile.getTitle() != "")){ textValue +=
				 * pdfCombinerFile.getTitle() + "\n"; }
				 * if(pdfCombinerFile.getDescription() != null &&
				 * pdfCombinerFile.getDescription() != ""){ textValue +=
				 * pdfCombinerFile.getDescription(); }
				 */
				System.out.println("   creating header");
				b.pageHeader(
						cmp.horizontalList().add(
								cmp.image("/app/public/images/CCOA-logo.png")
										.setFixedDimension(244, 22)),
						// cmp.text(textValue).setStyle(titleStyle),
						cmp.text(""));
				/*
				 * audienceReport.title(cmp.horizontalList().add(cmp.image(
				 * "/app/public/images/CPQ-logo.jpg").setFixedDimension(244,
				 * 22)), //cmp.text(textValue).setStyle(titleStyle),
				 * cmp.text(""), createVerticalTableNew(flightHeaderStyle,
				 * flightHeaderValueStyle, headerFieldNamesArray,
				 * headerFieldLabelsArray, Units.inch(2),
				 * pdfCombinerFile.getFieldNamesPipeDelimited(), fieldLevel),
				 * cmp.text(""));
				 * locationReport.title(cmp.horizontalList().add(cmp
				 * .image("/app/public/images/CPQ-logo.jpg"
				 * ).setFixedDimension(244, 22)),
				 * //cmp.text(textValue).setStyle(titleStyle), cmp.text(""),
				 * createVerticalTableNew(flightHeaderStyle,
				 * flightHeaderValueStyle, headerFieldNamesArray,
				 * headerFieldLabelsArray, Units.inch(2),
				 * pdfCombinerFile.getFieldNamesPipeDelimited(), fieldLevel),
				 * cmp.text(""));
				 */
				// addDataLevel(b, pdfCombinerFile, fieldLevel - 1,
				// showTotalProgramSummary, showIndividualMarketSummary,
				// showIndividualFlightSummary,
				// mapPanelOrderPrefDataSourceFileName);
			}

			// data
			// addDataLevel(b, pdfCombinerFile, fieldLevel - 1,
			// showTotalProgramSummary, showIndividualMarketSummary,
			// showIndividualFlightSummary, mapPanelOrderPrefDataSourceFileName,
			// columnTitleStyle);

			/*
			 * addDataLevel(audienceReport, pdfCombinerFile, fieldLevel - 1,
			 * showTotalProgramSummary, showIndividualMarketSummary,
			 * showIndividualFlightSummary, mapPanelOrderPrefDataSourceFileName,
			 * columnTitleStyle); addDataLevel(locationReport, pdfCombinerFile,
			 * fieldLevel - 1, showTotalProgramSummary,
			 * showIndividualMarketSummary, showIndividualFlightSummary,
			 * mapPanelOrderPrefDataSourceFileName, columnTitleStyle);
			 * 
			 * SubreportBuilder audienceSubreport =
			 * cmp.subreport(audienceReport); SubreportBuilder locationSubreport
			 * = cmp.subreport(locationReport); b.summary(audienceSubreport);
			 * b.summary(locationSubreport);
			 */
			if (isDetail) {
				// String[] fieldNamesArray = new String[]{ "Division__c",
				// "MarketName__c", "MarketTypeName__c", "Flight_Name__c",
				// "Campaign_Start_Date__c", "Campaign_End_Date__c",
				// "Campaign_Weeks__c", "Target__c", "Target_Population__c"};
				// String[] fieldLabelsArray = new String[]{ "Division",
				// "Market Name", "Market Type", "Flight Name", "Start Date",
				// "End Date", "Weeks", "Target", "Target Population"};
				/*
				 * String textValue = ""; if((pdfCombinerFile.getTitle() != null
				 * && pdfCombinerFile.getTitle() != "")){ textValue +=
				 * pdfCombinerFile.getTitle() + "\n"; }
				 * if(pdfCombinerFile.getDescription() != null &&
				 * pdfCombinerFile.getDescription() != ""){ textValue +=
				 * pdfCombinerFile.getDescription(); } b.pageHeader(
				 * cmp.horizontalList
				 * ().add(cmp.image("/app/public/images/CPQ-logo.jpg"
				 * ).setFixedDimension(244, 22)),
				 * cmp.text(textValue).setStyle(titleStyle),
				 * //createVerticalTable(flightHeaderStyle,
				 * flightHeaderValueStyle, fieldNamesArray, fieldLabelsArray,
				 * Units.inch(2)), cmp.text(""), cmp.text(""));
				 */
			}
			// ============ create a vertical table of flight titles: end

			// add flight fields
			FieldBuilder<String> marketIdField = field("Package_Flight__r/Package_Market__r/Id", String.class);
			FieldBuilder<String> packageIdField = field("Package_Flight__r/Package_Market__r/Package__r/Id", String.class);
			FieldBuilder<String> flightIdField = field("Package_Flight__r/Id", String.class);
			FieldBuilder<String> flightTypeField = field("Package_Flight__r/Type__c", String.class);
			FieldBuilder<String> mediaCategoryField = field("Package_Flight__r/Media_Category__c", String.class);
			FieldBuilder<String> flightCommentsField = field("Package_Flight__r/Flight_Comments__c", String.class);
			FieldBuilder<String> flightDivisionField = field("Package_Flight__r/Division__c", String.class);
			FieldBuilder<String> flightPackageNameField = field("Package_Flight__r/Package_Name__c", String.class);
			FieldBuilder<String> flightMarketNameField = field("Package_Flight__r/Market_Name__c", String.class);
			FieldBuilder<String> flightMarketTypeField = field("Package_Flight__r/Market_Type__c", String.class);
			FieldBuilder<String> flightNameField = field("Package_Flight__r/Name", String.class);
			FieldBuilder<String> flightStartDateField = field("Package_Flight__r/Campaign_Start_Date__c", String.class);
			FieldBuilder<String> flightEndDateField = field("Package_Flight__r/Campaign_End_Date__c", String.class);
			FieldBuilder<String> flightDurationField = field("Package_Flight__r/Duration_And_Type__c", String.class);
			FieldBuilder<String> flightTargetField = field("Package_Flight__r/Target__c", String.class);
			FieldBuilder<Integer> flightTargetPopulationField = field("Package_Flight__r/Target_Population__c", Integer.class);
			FieldBuilder<String> flightPackageCommentsField = field("Package_Flight__r/Package_Comments__c", String.class);
			b.addField(marketIdField);
			b.addField(packageIdField);
			b.addField(flightIdField);
			b.addField(flightTypeField);
			b.addField(mediaCategoryField);
			b.addField(flightCommentsField);
			b.addField(flightDivisionField);
			b.addField(flightPackageNameField);
			b.addField(flightMarketNameField);
			b.addField(flightMarketTypeField);
			b.addField(flightNameField);
			b.addField(flightStartDateField);
			b.addField(flightEndDateField);
			b.addField(flightDurationField);
			b.addField(flightTargetField);
			b.addField(flightTargetPopulationField);
			b.addField(flightPackageCommentsField);

			// group by flight
			System.out.println("   creating group");
			CustomGroupBuilder group = grp.group(new FlightIdGroupExpression());
			group.setHeaderLayout(GroupHeaderLayout.EMPTY);
			group.setStyle(groupHeaderStyleNew);
			group.startInNewPage();

			// ignore pagination when exporting to excel
			if(exportAsExcel) {
				b.ignorePagination();
			}

			// add a page header with information from flight for Audience
			// report that will be shown
			// when group changes
			// b.pageHeader(createVerticalTable(flightHeaderStyle,
			// flightHeaderValueStyle, headerFieldNamesArray,
			// headerFieldLabelsArray,
			// Units.inch(2)).setPrintWhenGroupChanges(group));

			PackageChangedExpression packageHeaderChangedExpression = new PackageChangedExpression(packageMarketFlightPreviousRecordExpression);
			
			// =================== previous values observers: begin
			PackageMarketFlightPreviousRecordHadAudienceExpression packageMarketFlightPreviousRecordHadAudienceExpression = new PackageMarketFlightPreviousRecordHadAudienceExpression(
					packageMarketFlightPreviousRecordExpression);
			PackageMarketFlightPreviousRecordHadLocationExpression packageMarketFlightPreviousRecordHadLocationExpression = new PackageMarketFlightPreviousRecordHadLocationExpression(
					packageMarketFlightPreviousRecordExpression);
			PackageMarketFlightPreviousRecordHadRotaryExpression packageMarketFlightPreviousRecordHadRotaryExpression = new PackageMarketFlightPreviousRecordHadRotaryExpression(
					packageMarketFlightPreviousRecordExpression);
			PackageMarketFlightPreviousRecordHadNetworkExpression packageMarketFlightPreviousRecordHadNetworkExpression = new PackageMarketFlightPreviousRecordHadNetworkExpression(
					packageMarketFlightPreviousRecordExpression);
			// =================== previous values observers: end
  
			// add group
			b.addGroup(group);
			FlightNameReportScriptlet flightNameSubreportScriptlet = new FlightNameReportScriptlet();
			b.scriptlets(flightNameSubreportScriptlet);

			// =================== report flight headers: begin
			JRXmlDataSource packageHeaderDataSource = new JRXmlDataSource(
					dataSourceFileName, QUERY_RESULT_RECORDS_UNIQUE_FLIGHT);
			new FlightDataExpression(packageHeaderDataSource);
			SubreportBuilder packageHeaderSubreport = cmp
					.subreport(
							new PackageHeaderSubreportExpression(
									flightHeaderStyle, flightHeaderValueStyle))
					.setDataSource(packageHeaderDataSource)
					.setPrintWhenExpression(packageHeaderChangedExpression)
					.removeLineWhenBlank();
			// =================== report flight headers: end
			
			if(exportAsExcel) {
				// create a vertical list of Valid disclaimer reports as per rules
				ValidDisclaimerExistsExpression validDisclaimerExistsExpression =  new ValidDisclaimerExistsExpression();
				VerticalListBuilder validDisclaimerReports = cmp.verticalList();
				validDisclaimerReports.setPrintWhenExpression(validDisclaimerExistsExpression);
				validDisclaimerReports.add(cmp.text("Important Notes:").setStyle(boldStyle).removeLineWhenBlank());
				validDisclaimerReports.add(cmp.text(new DisclaimerExpression()).setStyle(summaryStyle));
				b.summary(validDisclaimerReports);
			}
			
			VerticalListBuilder flightDisclaimerReports = cmp.verticalList();
			flightDisclaimerReports.add(cmp.text("Important Notes:").setStyle(boldStyle));

			// =================== begin
			// If PackageMarketFlight.Buy type is ""Location"", all Disclaimers
			// with
			// this value should be rendered rt after the Flight Details
			System.out.println("   creating subreport");
			SubreportBuilder afterLocationListPageDisclaimerSubreport = cmp
					.subreport(new DisclaimerSubreportExpression())
					.setDataSource(afterLocationListPageDisclaimerDataSource)
					.removeLineWhenBlank();

			// set information for a previous record at market flight level
			CustomGroupBuilder packageMarketFlightPreviousRecordCustomGroupBuilder = grp.group(packageMarketFlightPreviousRecordExpression);
			b.addGroup(packageMarketFlightPreviousRecordCustomGroupBuilder);

			// wrap the subreport inside horizontal list builder to control when it should be rendered/printed
			HorizontalListBuilder afterLocationListPageDisclaimer = cmp
					.horizontalList();
			afterLocationListPageDisclaimer
					.add(afterLocationListPageDisclaimerSubreport);
			afterLocationListPageDisclaimer
					.setPrintWhenExpression(packageMarketFlightPreviousRecordHadLocationExpression);
			flightDisclaimerReports.add(afterLocationListPageDisclaimer.setStyle(summaryStyle));
			// =================== end

			// =================== audience subreport: begin
			JRXmlDataSource audienceDataSource = new JRXmlDataSource(
					dataSourceFileName,
					QUERY_RESULT_RECORDS_RECORD_TYPE_ID_BOOLEAN_0);
			SubreportBuilder audienceSubreport = cmp
					.subreport(
							new AudienceSubreportExpression(
									dataSourceFileName,
									packageMarketFlightPreviousRecordExpression,
									columnTitleStyle, columnStyle,
									exportAsExcel, false))
					.setDataSource(audienceDataSource)
					.setPrintWhenExpression(
							packageMarketFlightPreviousRecordHadAudienceExpression)
					.removeLineWhenBlank();
			// =================== audience subreport: end

			// =================== audience subreport summary: begin
			JRXmlDataSource audienceFlightSummaryDataSource = new JRXmlDataSource(
					dataSourceFileName,
					QUERY_RESULT_RECORDS_RECORD_TYPE_ID_BOOLEAN_1);
			SubreportBuilder audienceFlightSummarySubreport = cmp
					.subreport(
							new AudienceSubreportExpression(
									dataSourceFileName,
									packageMarketFlightPreviousRecordExpression,
									columnTitleStyle, columnStyle,
									exportAsExcel, true))
					.setDataSource(audienceFlightSummaryDataSource)
					.setPrintWhenExpression(
							packageMarketFlightPreviousRecordHadAudienceExpression)
					.removeLineWhenBlank();
			// =================== audience subreport summary: end

			// =================== audience subreport market summary: begin
			JRXmlDataSource audienceMarketSummaryDataSource = new JRXmlDataSource(
					dataSourceFileName, QUERY_RESULT_RECORDS_UNIQUE_MARKET);
			SubreportBuilder audienceMarketSummarySubreport = cmp
					.subreport(
							new AudienceSubreportExpression(
									dataSourceFileName,
									packageMarketFlightPreviousRecordExpression,
									columnTitleStyle, columnStyle,
									exportAsExcel, true,
									SummaryLevelEnum.Market))
					.setDataSource(audienceMarketSummaryDataSource)
					.setPrintWhenExpression(
							packageMarketFlightPreviousRecordHadAudienceExpression)
					.removeLineWhenBlank();
			// =================== audience subreport summary: end

			// =================== audience subreport package summary: begin
			JRXmlDataSource audiencePackageSummaryDataSource = new JRXmlDataSource(
					dataSourceFileName, QUERY_RESULT_RECORDS_UNIQUE_PACKAGE);
			SubreportBuilder audiencePackageSummarySubreport = cmp
					.subreport(
							new AudienceSubreportExpression(
									dataSourceFileName,
									packageMarketFlightPreviousRecordExpression,
									columnTitleStyle, columnStyle,
									exportAsExcel, true,
									SummaryLevelEnum.Package))
					.setDataSource(audiencePackageSummaryDataSource)
					.setPrintWhenExpression(
							packageMarketFlightPreviousRecordHadAudienceExpression)
					.removeLineWhenBlank();
			// =================== audience subreport summary: end

			// =================== location subreport: begin
			JRXmlDataSource locationDataSource = new JRXmlDataSource(
					dataSourceFileName,
					QUERY_RESULT_RECORDS_RECORD_TYPE_ID_BOOLEAN_0);
			SubreportBuilder locationSubreport = cmp
					.subreport(
							new LocationSubreportExpression(
									dataSourceFileName,
									packageMarketFlightPreviousRecordExpression,
									columnTitleStyle, columnStyle,
									exportAsExcel, false, locationMapExists))
					.setDataSource(locationDataSource)
					.setPrintWhenExpression(
							packageMarketFlightPreviousRecordHadLocationExpression)
					.removeLineWhenBlank();
			// =================== location subreport: end

			// =================== location summary subreport: begin
			JRXmlDataSource locationFlightSummaryDataSource = new JRXmlDataSource(
					dataSourceFileName,
					QUERY_RESULT_RECORDS_RECORD_TYPE_ID_BOOLEAN_1);
			SubreportBuilder locationFlightSummarySubreport = cmp
					.subreport(
							new LocationSubreportExpression(
									dataSourceFileName,
									packageMarketFlightPreviousRecordExpression,
									columnTitleStyle, columnStyle,
									exportAsExcel, true, locationMapExists))
					.setDataSource(locationFlightSummaryDataSource)
					.setPrintWhenExpression(
							packageMarketFlightPreviousRecordHadLocationExpression)
					.removeLineWhenBlank();
			// =================== location subreport: end

			// =================== location market summary subreport: begin
			JRXmlDataSource locationMarketSummaryDataSource = new JRXmlDataSource(
					dataSourceFileName, QUERY_RESULT_RECORDS_UNIQUE_MARKET);
			SubreportBuilder locationMarketSummarySubreport = cmp
					.subreport(
							new LocationSubreportExpression(
									dataSourceFileName,
									packageMarketFlightPreviousRecordExpression,
									columnTitleStyle, columnStyle,
									exportAsExcel, true, locationMapExists,
									SummaryLevelEnum.Market))
					.setDataSource(locationMarketSummaryDataSource)
					.setPrintWhenExpression(
							packageMarketFlightPreviousRecordHadLocationExpression)
					.removeLineWhenBlank();
			// =================== location market summary subreport: end

			// =================== location package summary subreport: begin
			JRXmlDataSource locationPackageSummaryDataSource = new JRXmlDataSource(
					dataSourceFileName, QUERY_RESULT_RECORDS_UNIQUE_PACKAGE);
			SubreportBuilder locationPackageSummarySubreport = cmp
					.subreport(
							new LocationSubreportExpression(
									dataSourceFileName,
									packageMarketFlightPreviousRecordExpression,
									columnTitleStyle, columnStyle,
									exportAsExcel, true, locationMapExists,
									SummaryLevelEnum.Package))
					.setDataSource(locationPackageSummaryDataSource)
					.setPrintWhenExpression(
							packageMarketFlightPreviousRecordHadLocationExpression)
					.removeLineWhenBlank();
			// =================== location market summary subreport: end

			// =================== rotary subreport: begin
			JRXmlDataSource rotaryDataSource = new JRXmlDataSource(
					dataSourceFileName,
					QUERY_RESULT_RECORDS_RECORD_TYPE_ID_BOOLEAN_0);
			SubreportBuilder rotarySubreport = cmp
					.subreport(
							new RotarySubreportExpression(
									dataSourceFileName,
									packageMarketFlightPreviousRecordExpression,
									columnTitleStyle, columnStyle,
									exportAsExcel, false))
					.setDataSource(rotaryDataSource)
					.setPrintWhenExpression(
							packageMarketFlightPreviousRecordHadRotaryExpression)
					.removeLineWhenBlank();
			// =================== rotary subreport: end

			// =================== rotary summary subreport: begin
			JRXmlDataSource rotaryFlightSummaryDataSource = new JRXmlDataSource(
					dataSourceFileName,
					QUERY_RESULT_RECORDS_RECORD_TYPE_ID_BOOLEAN_1);
			SubreportBuilder rotaryFlightSummarySubreport = cmp
					.subreport(
							new RotarySubreportExpression(
									dataSourceFileName,
									packageMarketFlightPreviousRecordExpression,
									columnTitleStyle, columnStyle,
									exportAsExcel, true))
					.setDataSource(rotaryFlightSummaryDataSource)
					.setPrintWhenExpression(
							packageMarketFlightPreviousRecordHadRotaryExpression)
					.removeLineWhenBlank();
			// =================== rotary summary subreport: end

			// =================== rotary market summary subreport: begin
			JRXmlDataSource rotaryMarketSummaryDataSource = new JRXmlDataSource(
					dataSourceFileName, QUERY_RESULT_RECORDS_UNIQUE_MARKET);
			SubreportBuilder rotaryMarketSummarySubreport = cmp
					.subreport(
							new RotarySubreportExpression(
									dataSourceFileName,
									packageMarketFlightPreviousRecordExpression,
									columnTitleStyle, columnStyle,
									exportAsExcel, true,
									SummaryLevelEnum.Market))
					.setDataSource(rotaryMarketSummaryDataSource)
					.setPrintWhenExpression(
							packageMarketFlightPreviousRecordHadRotaryExpression)
					.removeLineWhenBlank();
			// =================== rotary market summary subreport: end

			// =================== rotary market summary subreport: begin
			JRXmlDataSource rotaryPackageSummaryDataSource = new JRXmlDataSource(
					dataSourceFileName, QUERY_RESULT_RECORDS_UNIQUE_PACKAGE);
			SubreportBuilder rotaryPackageSummarySubreport = cmp
					.subreport(
							new RotarySubreportExpression(
									dataSourceFileName,
									packageMarketFlightPreviousRecordExpression,
									columnTitleStyle, columnStyle,
									exportAsExcel, true,
									SummaryLevelEnum.Package))
					.setDataSource(rotaryPackageSummaryDataSource)
					.setPrintWhenExpression(
							packageMarketFlightPreviousRecordHadRotaryExpression)
					.removeLineWhenBlank();
			// =================== rotary market summary subreport: end

			// =================== network subreport: begin
			JRXmlDataSource networkDataSource = new JRXmlDataSource(
					dataSourceFileName,
					QUERY_RESULT_RECORDS_WITH_CHILD_FLIGHT_LINES);
			SubreportBuilder networkSubreport = cmp
					.subreport(
							new NetworkSubreportExpression(
									dataSourceFileName,
									packageMarketFlightPreviousRecordExpression,
									flightLinePreviousRecordExpression,
									columnTitleStyle, columnStyle,
									exportAsExcel, false, excludeNetworkDetails))
					.setPrintWhenExpression(
							packageMarketFlightPreviousRecordHadNetworkExpression)
					.setDataSource(networkDataSource).removeLineWhenBlank();
			// =================== network subreport: end

			// =================== network subreport: begin
			JRXmlDataSource networkFlightSummaryDataSource = new JRXmlDataSource(
					dataSourceFileName,
					QUERY_RESULT_RECORDS_RECORD_TYPE_ID_BOOLEAN_1);
			SubreportBuilder networkFlightSummarySubreport = cmp
					.subreport(
							new NetworkSubreportExpression(
									dataSourceFileName,
									packageMarketFlightPreviousRecordExpression,
									null, columnTitleStyle, columnStyle,
									exportAsExcel, true, true))
					.setPrintWhenExpression(
							packageMarketFlightPreviousRecordHadNetworkExpression)
					.setDataSource(networkFlightSummaryDataSource)
					.removeLineWhenBlank();
			// =================== network subreport: end

			// =================== network market summary subreport: begin
			JRXmlDataSource networkMarketSummaryDataSource = new JRXmlDataSource(
					dataSourceFileName, QUERY_RESULT_RECORDS_UNIQUE_MARKET);
			SubreportBuilder networkMarketSummarySubreport = cmp
					.subreport(
							new NetworkSubreportExpression(
									dataSourceFileName,
									packageMarketFlightPreviousRecordExpression,
									null, columnTitleStyle, columnStyle,
									exportAsExcel, true, true,
									SummaryLevelEnum.Market))
					.setDataSource(networkMarketSummaryDataSource)
					.setPrintWhenExpression(
							packageMarketFlightPreviousRecordHadNetworkExpression)
					.removeLineWhenBlank();
			// =================== network market summary subreport: end

			// =================== network package summary subreport: begin
			JRXmlDataSource networkPackageSummaryDataSource = new JRXmlDataSource(
					dataSourceFileName, QUERY_RESULT_RECORDS_UNIQUE_PACKAGE);
			SubreportBuilder networkPackageSummarySubreport = cmp
					.subreport(
							new NetworkSubreportExpression(
									dataSourceFileName,
									packageMarketFlightPreviousRecordExpression,
									null, columnTitleStyle, columnStyle,
									exportAsExcel, true, true,
									SummaryLevelEnum.Package))
					.setDataSource(networkPackageSummaryDataSource)
					.setPrintWhenExpression(
							packageMarketFlightPreviousRecordHadNetworkExpression)
					.removeLineWhenBlank();
			// =================== network market summary subreport: end

			// =================== subreport and summaries positioning: begin
			if(exportAsPdf) {
				group.footer(packageHeaderSubreport);
				
				group.footer(audienceSubreport);
				if(showIndividualFlightSummary) group.footer(audienceFlightSummarySubreport);
				if(showIndividualMarketSummary) group.footer(audienceMarketSummarySubreport);
				if(showTotalProgramSummary) group.footer(audiencePackageSummarySubreport);
				group.footer(locationSubreport);
				if(showIndividualFlightSummary) group.footer(locationFlightSummarySubreport);
				if(showIndividualMarketSummary) group.footer(locationMarketSummarySubreport);
				if(showTotalProgramSummary) group.footer(locationPackageSummarySubreport);
				group.footer(rotarySubreport);
				if(showIndividualFlightSummary) group.footer(rotaryFlightSummarySubreport);
				if(showIndividualMarketSummary) group.footer(rotaryMarketSummarySubreport);
				if(showTotalProgramSummary) group.footer(rotaryPackageSummarySubreport);
				group.footer(networkSubreport);
				if(showIndividualFlightSummary) group.footer(networkFlightSummarySubreport);
				if(showIndividualMarketSummary) group.footer(networkMarketSummarySubreport);
				if(showTotalProgramSummary) group.footer(networkPackageSummarySubreport);
			} else {
				if(showTotalProgramSummary) group.footer(audiencePackageSummarySubreport);
				if(showIndividualMarketSummary) group.footer(audienceMarketSummarySubreport);
				if(showIndividualFlightSummary) group.footer(audienceFlightSummarySubreport);
				group.footer(audienceSubreport);
				if(showTotalProgramSummary) group.footer(locationPackageSummarySubreport);
				if(showIndividualMarketSummary) group.footer(locationMarketSummarySubreport);
				if(showIndividualFlightSummary) group.footer(locationFlightSummarySubreport);
				group.footer(locationSubreport);
				if(showTotalProgramSummary) group.footer(rotaryPackageSummarySubreport);
				if(showIndividualMarketSummary) group.footer(rotaryMarketSummarySubreport);
				if(showIndividualFlightSummary) group.footer(rotaryFlightSummarySubreport);
				group.footer(rotarySubreport);
				if(showTotalProgramSummary) group.footer(networkPackageSummarySubreport);
				if(showIndividualMarketSummary) group.footer(networkMarketSummarySubreport);
				if(showIndividualFlightSummary) group.footer(networkFlightSummarySubreport);
				group.footer(networkSubreport);
			}
			// =================== subreport and summaries positioning: end

			// =================== begin
			// Only Print "Rail Station Display Summary" Disclaimer after the
			// Flight Details (i.e. Package Flight Lines,
			// where PackageMarketFlight.Buy Type is ""Network/Custom"" and
			// PackageMarketFlight.MediaCategory is ""Commuter Rail""
			SubreportBuilder afterRailStationDisplaySummaryDisclaimerSubreport = cmp
					.subreport(new DisclaimerSubreportExpression())
					.setDataSource(
							afterRailStationDisplaySummaryDisclaimerDataSource)
					.removeLineWhenBlank();

			// wrap the subreport inside horizontal list builder to control when
			// it should be rendered/printed
			HorizontalListBuilder afterRailStationDisplaySummaryDisclaimer = cmp
					.horizontalList();
			afterRailStationDisplaySummaryDisclaimer
					.add(afterRailStationDisplaySummaryDisclaimerSubreport);
			afterRailStationDisplaySummaryDisclaimer
					.setPrintWhenExpression(new PackageMarketFlightPreviousRecordHadBuyTypeNetworkOrCustomAndMediaCategoryIsCommuterRailExpression(
							packageMarketFlightPreviousRecordExpression));
			flightDisclaimerReports
					.add(afterRailStationDisplaySummaryDisclaimer);
			// =================== end

			// =================== begin
			// Only Print "Rail Transit Poster Summary" Disclaimer after the
			// Flight Details (i.e. Package Flight Lines,
			// where PackageMarketFlight.Buy Type is ""Network/Custom"" and
			// PackageMarketFlight.MediaCategory is ""Bus/Transit""
			SubreportBuilder afterRailTransitPosterSummaryDisclaimerReport = cmp
					.subreport(new DisclaimerSubreportExpression())
					.setDataSource(
							afterRailTransitPosterSummaryDisclaimerDataSource)
					.removeLineWhenBlank();

			// wrap the subreport inside horizontal list builder to control when
			// it should be rendered/printed
			HorizontalListBuilder afterRailTransitPosterSummaryDisclaimer = cmp
					.horizontalList();
			afterRailTransitPosterSummaryDisclaimer
					.add(afterRailTransitPosterSummaryDisclaimerReport);
			afterRailTransitPosterSummaryDisclaimer
					.setPrintWhenExpression(new PackageMarketFlightPreviousRecordHadBuyTypeNetworkOrCustomAndMediaCategoryIsBusOrTransitExpression(
							packageMarketFlightPreviousRecordExpression));
			flightDisclaimerReports
					.add(afterRailTransitPosterSummaryDisclaimer);
			// =================== end

			// =================== begin
			// Only Print "Taxi Display Summary" Disclaimer after the Flight
			// Details (i.e. Package Flight Lines,
			// where PackageMarketFlight.Buy Type is ""Network/Custom"" and
			// PackageMarketFlight.MediaCategory is ""Mobile Billboard""
			SubreportBuilder afterTaxiDisplaySummaryDisclaimerDisclaimerReport = cmp
					.subreport(new DisclaimerSubreportExpression())
					.setDataSource(afterTaxiDisplaySummaryDisclaimerDataSource)
					.removeLineWhenBlank();

			// wrap the subreport inside horizontal list builder to control when
			// it should be rendered/printed
			HorizontalListBuilder afterTaxiDisplaySummaryDisclaimer = cmp
					.horizontalList();
			afterTaxiDisplaySummaryDisclaimer
					.add(afterTaxiDisplaySummaryDisclaimerDisclaimerReport);
			afterTaxiDisplaySummaryDisclaimer
					.setPrintWhenExpression(new PackageMarketFlightPreviousRecordHadBuyTypeNetworkOrCustomAndMediaCategoryIsMobileBillboardExpression(
							packageMarketFlightPreviousRecordExpression));
			flightDisclaimerReports.add(afterTaxiDisplaySummaryDisclaimer);
			// =================== end

			// =================== begin
			// All Disclaimers with ""Program Summary (Including individual
			// Market)"" should be
			// a) rendered at the end of every Market Summary Section and also
			// b) at the end of the Plan.
			if (isMarketLevel(pdfCombinerFile, fieldLevel - 1)) {

				// this report has a market level
				hasMarketLevel = true;

				// =================== begin
				// create a vertical list of disclaimer reports
				VerticalListBuilder marketDisclaimerReports = cmp
						.verticalList();
				marketDisclaimerReports.add(cmp.text("Important Notes:")
						.setStyle(boldStyle));

				// global disclaimers that should be rendered when matches one
				// of the specified conditions at
				// PreviousFlightRecordHadExpression
				marketDisclaimerReports
						.setPrintWhenExpression(previousFlightRecordDidntHaveExpression);
				// anjali commented 4Aug
				// group.footer(marketDisclaimerReports);
				// =================== end

				// a) rendered at the end of every Market Summary Section
				SubreportBuilder programSummaryIncludingIndividualMarketSummaryPagesDisclaimerSubreport = cmp
						.subreport(new DisclaimerSubreportExpression())
						.setDataSource(
								programSummaryIncludingIndividualMarketSummaryPagesDisclaimerDataSource)
						.removeLineWhenBlank();
				// anjali commented 4Aug

				// group.footer(programSummaryIncludingIndividualMarketSummaryPagesDisclaimerSubreport);

				// b) at the end of the Plan.
				// anjali commented

				// b.summary(programSummaryIncludingIndividualMarketSummaryPagesDisclaimerSubreport);
			}
			// =================== end

			// =================== begin
			// Flight level disclaimers that should be rendered when matches one
			// of the specified conditions at PreviousFlightRecordHadExpression
			flightDisclaimerReports
					.setPrintWhenExpression(previousFlightRecordHadExpression);

			// add disclaimer reports to group footer
			// Anjali commented 4 Aug group.footer(flightDisclaimerReports);
			// =================== end

			if (!hasMarketLevel) {
				// =================== begin
				// create a vertical list of disclaimer reports
				VerticalListBuilder globalDisclaimerReports = cmp
						.verticalList();
				globalDisclaimerReports.add(cmp.text("Important Notes:")
						.setStyle(boldStyle));

				// global disclaimers that should be rendered when matches one
				// of the specified conditions at
				// PreviousFlightRecordHadExpression
				globalDisclaimerReports
						.setPrintWhenExpression(previousFlightRecordDidntHaveExpression);
				// Anjali commented - 3 Aug
				// b.summary(globalDisclaimerReports);
				// =================== end
			}

			// =================== begin
			// All Disclaimers with ""ProgramSummary"" should be rendered at the
			// end of the Plan,
			// after all Flights are rendered.
			SubreportBuilder programSummaryDisclaimerSubreport = cmp
					.subreport(new DisclaimerSubreportExpression())
					.setDataSource(programSummaryDisclaimerDataSource)
					.removeLineWhenBlank();
			// Anjali commented - 3 Aug
			// b.summary(programSummaryDisclaimerSubreport);
			// =================== end

			// =================== begin
			// Only Print this Disclaimer at the very end after the entire Plan
			// is rendered.
			SubreportBuilder afterSummaryPageDisclaimerSubreport = cmp
					.subreport(new DisclaimerSubreportExpression())
					.setDataSource(afterSummaryPageDisclaimerDataSource)
					.removeLineWhenBlank();
			// Anjali commented - 3 Aug
			// b.summary(afterSummaryPageDisclaimerSubreport);
			// =================== end

			// styles
			b.highlightDetailEvenRows();
			b.setColumnStyle(columnStyle);
			// b.setSummaryStyle(groupHeaderStyle);
			b.setSubtotalStyle(columnStyle);
			// b.setGroupStyle(groupHeaderStyle);
			b.setGroupStyle(groupHeaderStyleNew);

			// =================== begin
			// Shipping instructions is part of the Plan that is displayed after all Plan Data is rendered in PDF
			JRXmlDataSource shippingInstructionsDataSource = null;
			if (shippingInstructionsDataSourceFileName != null) {
				shippingInstructionsDataSource = new JRXmlDataSource(
						shippingInstructionsDataSourceFileName,
						"/QueryResult/records");
				this.setDoesShippingInstructionsExists(shippingInstructionsDataSource.next());
				shippingInstructionsDataSource.moveFirst();
				ShippingInstructionsSubreportExpression shippingInstructionsSubreportExpression = new ShippingInstructionsSubreportExpression(this);
				SubreportBuilder shippingInstructionsSubreport = cmp
						.subreport(shippingInstructionsSubreportExpression)
						.setDataSource(shippingInstructionsDataSource)
						.removeLineWhenBlank();

				b.summary(shippingInstructionsSubreport);
				System.out.println(" doesShippingInstructionsExists "  + doesShippingInstructionsExists);
				if(doesShippingInstructionsExists) {
					VerticalListBuilder autoValidDisclaimerReports = cmp.verticalList();
					b.summary(cmp.text(""));
					b.summary(cmp.text(""));
					autoValidDisclaimerReports.add(cmp.text("Important Notes:").setStyle(boldStyle).removeLineWhenBlank());
					autoValidDisclaimerReports.add(cmp.text(new AutoDisclaimerExpression()).setStyle(summaryStyle));
					b.summary(autoValidDisclaimerReports);
				}
			}
			// =================== end

			// throw an exception if internal use fields are not set
			if (!hasRequiredInternalUseFields(b)) {
				throw new Exception("Required fields missing" + StringUtils.join(getRequiredMissingFieldsList(b), ", "));
			}

			// export
			if (exportAsPdf) {
				b.toPdf(Exporters.pdfExporter(getGeneratedReport("pdf")));
			}
			if (exportAsExcel) {
				JasperXlsExporterBuilder xlsExporterBuilder = Exporters.xlsExporter(getGeneratedReport("xls"));
				JasperXlsExporter xlsExporter = xlsExporterBuilder.getExporter();
				//xlsExporter.setParameter("net.sf.jasperreports.export.xls.wrap.text", false);
				xlsExporter.setWhitePageBackground(Boolean.FALSE);
				//b.toXls(Exporters.xlsExporter(getGeneratedReport("xls")));
				b.toXls(xlsExporterBuilder);
			}

			// As DynamicReports doesn't have a method/property that return Table of Contents (TOC) entries for more info see here:
			// http://www.dynamicreports.org/forum/viewtopic.php?f=1&t=416&sid=25edfbfe63ade19e637c1a79f5974ee1
			// We'll do an additional export in csv format after having exported to usual pdf/xls export formats.
			// This way other clients can pull report as csv and grab manually the TOC entries.
			b.setTableOfContents(true);
			b.rebuild();
			b.toCsv(Exporters.csvExporter(getGeneratedReport("csv")));
		} catch (JRException e) {
			System.out.println("Exception in Reporter: "+e.getMessage());
			e.printStackTrace();
			throw new Exception("Exception while creating jasper report: "+e.getMessage(), e);
		} catch (ParserConfigurationException e) {
			System.out.println("Exception in Reporter: "+e.getMessage());
			e.printStackTrace();
			throw new Exception("Exception while parsing, wrong parser config: "+e.getMessage(), e);
		} catch (SAXException e) {
			System.out.println("Exception in Reporter: "+e.getMessage());
			e.printStackTrace();
			throw new Exception("Exception while parsing xml: "+e.getMessage(), e);
		} catch (IOException e) {
			System.out.println("Exception in Reporter: "+e.getMessage());
			e.printStackTrace();
			throw new Exception("Exception during file I/O: "+e.getMessage(), e);
		} catch (ParseException e) {
			System.out.println("Exception in Reporter: "+e.getMessage());
			e.printStackTrace();
			throw new Exception("Exception while parsing text: "+e.getMessage(), e);
		} catch (DRException e) {
			System.out.println("Exception in Reporter: "+e.getMessage());
			e.printStackTrace();
			throw new Exception("Exception while creating dynamic jasper report: "+e.getMessage(), e);
		}
	}

	public Reporter() {
		// TODO Auto-generated constructor stub
	}

	private boolean hasRequiredInternalUseFields(JasperReportBuilder report) {
		return getRequiredMissingFieldsList(report).size() == 0;
	}

	private List<String> getRequiredMissingFieldsList(JasperReportBuilder report) {

		List<String> requiredFieldsList = new ArrayList<String>();
		requiredFieldsList.add("Package_Flight__r/Id");
		requiredFieldsList.add("Package_Flight__r/Name");
		requiredFieldsList.add("Package_Flight__r/Type__c");
		requiredFieldsList.add("Package_Flight__r/Package_Name__c");
		requiredFieldsList.add("Package_Flight__r/Media_Category__c");
		requiredFieldsList.add("Package_Flight__r/Package_Market__r/Id");
		requiredFieldsList.add("Package_Flight__r/Package_Market__r/Package__r/Id");

		for (Integer i = 0; i < report.getReport().getFields().size(); i++) {

			// searched field name
			String fieldName = report.getReport().getFields().get(i).getName();

			// check if searched field name is in required field list
			if (requiredFieldsList.contains(fieldName)) {
				// as this field exists, remove it from the required field list
				requiredFieldsList.remove(report.getReport().getFields().get(i)
						.getName());
			}
		}

		return requiredFieldsList;
	}

	// returns true if first fieldname from array has three parts and
	// penultimate part is Package_Market__r
	// this indicates that is a market level field
	// sample for true case: Package_Flight__r/Package_Market__r/Market_Name__c
	// sample for false case: Package_Flight__r/Id
	public static boolean isMarketLevel(PDFCombinerFile pdfCombinerFile,
			Integer levelIndex) {
		String[] fieldNamesArray = pdfCombinerFile.getFieldNamesPipeDelimited()
				.get(levelIndex).split("\\|");

		Boolean returnValue = false;

		if (fieldNamesArray.length > 0) {
			String fieldName = fieldNamesArray[0];
			String[] fieldNameParts = fieldName.split("\\/");
			boolean hasThreeParts = fieldNameParts.length == 3;
			boolean penultimatePartIsPackageMarketRelation = hasThreeParts
					&& fieldNameParts[1].equals("Package_Market__r");
			returnValue = penultimatePartIsPackageMarketRelation;
		}

		return returnValue;
	}

	/*
	 * Returns the corresponding summary field name related to the specified flight line field name.
	 */
	private String getSummaryFieldNameRelatedToFlightLineFieldName(SummaryLevelEnum summaryLevel, String flightLineFieldName) {

		String summaryFieldName = flightLineFieldName;

		if(summaryLevel == null) {
			summaryFieldName = flightLineFieldName;
		}
		else if(summaryLevel.equals(SummaryLevelEnum.Market)) {
			if(flightLineFieldName.equals("Weekly_Total_18_Imps__c")) {
				summaryFieldName = "Weekly_Total_18_Imps__c";
			} else if(flightLineFieldName.equals("In_Mkt_Imps__c")) {
				summaryFieldName = "In_Mkt_Imps__c";
			} else if(flightLineFieldName.equals("Total_Imps__c")) {
				summaryFieldName = "Target_Total_Imps__c";
			} else if(flightLineFieldName.equals("In_Mkt_TRP__c")) {
				summaryFieldName = "In_Mkt_TRP__c";
			} else if(flightLineFieldName.equals("PlanTRP__c")) {
				summaryFieldName = "Plan_TRP__c";
			} else if(flightLineFieldName.equals("Discount__c")) { // this field doesn't exist in flight lines. Just used it here to get related field
				summaryFieldName = "Discount__c";
			} else if(flightLineFieldName.equals("Total_Price_0d__c")) {
				summaryFieldName = "Total_Price__c";
			} else if(flightLineFieldName.equals("TotalInMarketCPM_0d__c")) {
				summaryFieldName = "CPM__c";
			} else if(flightLineFieldName.equals("CPP_0d__c")) {
				summaryFieldName = "CPP__c";
			}
		} else if(summaryLevel.equals(SummaryLevelEnum.Package)) {
			if(flightLineFieldName.equals("Weekly_Total_18_Imps__c")) {
				summaryFieldName = "Weekly_Total_18_Imps__c";
			} else if(flightLineFieldName.equals("In_Mkt_Imps__c")) {
				summaryFieldName = "In_Mkt_Imps__c";
			} else if(flightLineFieldName.equals("Total_Imps__c")) {
				summaryFieldName = "Target_Total_Imps__c";
			} else if(flightLineFieldName.equals("In_Mkt_TRP__c")) {
				summaryFieldName = "In_Mkt_TRP__c";
			} else if(flightLineFieldName.equals("PlanTRP__c")) {
				summaryFieldName = "Plan_TRP__c";
			} else if(flightLineFieldName.equals("Discount__c")) { // this field doesn't exist in flight lines. Just used it here to get related field
				summaryFieldName = "Discount__c";
			} else if(flightLineFieldName.equals("Total_Price_0d__c")) {
				summaryFieldName = "Total_Price__c";
			} else if(flightLineFieldName.equals("TotalInMarketCPM_0d__c")) {
				summaryFieldName = "CPM__c";
			} else if(flightLineFieldName.equals("CPP_0d__c")) {
				summaryFieldName = "CPP__c";
			}
		}

		return summaryFieldName;
	}

	/*
	 * add summary field
	 */
	private void addSummaryField(SummaryLevelEnum summaryLevel, JasperReportBuilder report) {

		// by default, point to a column we know that wont exist
		String summaryField = "Package_Flight__r/Name";

		// determine which field will be used for specified summary level
		if(summaryLevel == SummaryLevelEnum.Market) {
			summaryField = "MarketName__c";
		} else if(summaryLevel == SummaryLevelEnum.Package) {
			summaryField = "Package_Name__c";
		}

		// add column
		report.addColumn(col
				.column("Summary", summaryField, type.stringType())
				.setHorizontalAlignment(HorizontalAlignment.LEFT));
	}

 private class ValidDisclaimerExistsExpression extends AbstractSimpleExpression<Boolean> {
	
	private static final long serialVersionUID = 1632549537807166153L;
	
	public ValidDisclaimerExistsExpression() {
		
	}
	
	@Override
	public Boolean evaluate(ReportParameters reportParameters) {
		System.out.println("******** ValidDisclaimerExistsExpression evaluate.validDisclaimersListDisplay exists " + validDisclaimersListDisplay.size() + " validAllDisclaimersList size " +	validAllDisclaimersList.size());
		return validAllDisclaimersList != null && validAllDisclaimersList.size() >0;
	}
 }
	private class PackageMarketFlightPreviousRecordExpression extends
			AbstractSimpleExpression<String> {
		private static final long serialVersionUID = 549979415L;

		private Integer m_reportRowNumber = 0;

		private String lastMarketId = null;

		private String currentMarketId = null;

		private String lastPackageId = null;

		private String currentPackageId = null;

		String m_lastIdValue = null;

		String m_lastBuyTypeValue = null;

		String m_lastMediaCategoryValue = null;

		String m_lastMarketNameValue = null;

		// true = all records have been evaluated 
		private boolean eof = false;

		@Override
		public String evaluate(ReportParameters reportParameters) {

			Integer reportRowNumber = reportParameters.getReportRowNumber();

			if(!eof) {
				eof = reportRowNumber == getRecordCount() - 1;
				System.out.println("   check eof: reportRowNumber, this.m_reportRowNumber, eof " + reportRowNumber + " " + this.m_reportRowNumber + " " + eof);
			}

			if (reportRowNumber > this.m_reportRowNumber) {
				// System.out.println("PackageMarketFlightPreviousRecordExpression.evaluate");

				this.m_reportRowNumber = reportRowNumber;

				// try to get market id value (if exception is raised that's because
				// the field doesn't exist)
				// so in this case just continue...
				try {
					System.out.println("   try to get market id value");
					String idValue = reportParameters
							.getValue("Package_Flight__r/Package_Market__r/Id");
					setLastMarketId(idValue);
					System.out.println("   market id value-> " + idValue);
				} catch (Exception e) {
					e.printStackTrace();
				}

				// try to get package id value (if exception is raised that's because
				// the field doesn't exist)
				// so in this case just continue...
				try {
					System.out.println("   try to get package id value");
					String idValue = reportParameters
							.getValue("Package_Flight__r/Package_Market__r/Package__r/Id");
					setLastPackageId(idValue);
					System.out.println("   package id value-> " + idValue);
				} catch (Exception e) {
					e.printStackTrace();
				}

				// try to get id value (if exception is raised that's because
				// the field doesn't exist)
				// so in this case just continue...
				try {
					String idValue = reportParameters
							.getValue("Package_Flight__r/Id");
					this.m_lastIdValue = idValue;
					System.out.println("idValue->" + idValue);
				} catch (Exception e) {
					e.printStackTrace();
				}

				// try to get buy type value (if exception is raised that's
				// because the field doesn't exist)
				// so in this case just continue...
				try {
					String buyTypeValue = reportParameters
							.getValue("Package_Flight__r/Type__c");
					this.m_lastBuyTypeValue = buyTypeValue;
					System.out.println("buyTypeValue->" + buyTypeValue);
				} catch (Exception e) {
					e.printStackTrace();
				}

				// try to get media category value (if exception is raised
				// that's because the field doesn't exist)
				// so in this case just continue...Media_Category__c
				try {
					String mediaCategoryValue = reportParameters
							.getValue("Package_Flight__r/Media_Category__c");
					this.m_lastMediaCategoryValue = mediaCategoryValue;
					System.out.println("mediaCategoryValue->"
							+ m_lastMediaCategoryValue);
				} catch (Exception e) {
					System.out
							.println("PackageMarketFlightPreviousRecordExpression  mediaCategoryValue excep "
									+ e);
				}

				// Disclaimer change-anjali
				try {
					String marketValue = reportParameters
							.getValue("Package_Flight__r/Market_Name__c");
					this.m_lastMarketNameValue = marketValue;
					System.out.println("marketValue->" + m_lastMarketNameValue);
				} catch (Exception e) {
					System.out
							.println("PackageMarketFlightPreviousRecordExpression marketValue excep "
									+ e);
				}
			} else {
				try {
					String idValue = reportParameters.getValue("Package_Flight__r/Package_Market__r/Id");
					setCurrentMarketId(idValue);
					System.out.println("   current and last market id -> " + idValue + " " + this.getLastMarketId());
				} catch (Exception e) {
					e.printStackTrace();
				}
				try {
					String idValue = reportParameters.getValue("Package_Flight__r/Package_Market__r/Package__r/Id");
					setCurrentPackageId(idValue);
					System.out.println("   current and last package id -> " + idValue + " " + this.getLastPackageId());
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			return null;
		}

		public String getId() {
			return this.m_lastIdValue;
		}

		public Boolean isBuyTypeLocation() {
			return this.m_lastBuyTypeValue != null
					&& this.m_lastBuyTypeValue.equals("Location");
		}

		public Boolean isBuyTypeRotary() {
			return this.m_lastBuyTypeValue != null
					&& this.m_lastBuyTypeValue.equals("Rotary");
		}

		public Boolean isBuyTypeAudience() {
			return this.m_lastBuyTypeValue != null
					&& this.m_lastBuyTypeValue.equals("Audience");
		}

		public Boolean isBuyTypeNetwork() {
			return this.m_lastBuyTypeValue != null
					&& this.m_lastBuyTypeValue.equals("Network/Custom");
		}

		public Boolean isCommuterRailMediaCategory() {
			return this.m_lastMediaCategoryValue != null
					&& this.m_lastMediaCategoryValue.equals("Commuter Rail");
		}

		public Boolean isBusOrTransitMediaCategory() {
			return this.m_lastMediaCategoryValue != null
					&& this.m_lastMediaCategoryValue.equals("Bus/Transit");
		}

		public Boolean isMobileBillboardMediaCategory() {
			return this.m_lastMediaCategoryValue != null
					&& this.m_lastMediaCategoryValue.equals("Mobile Billboard");
		}
		
		public Boolean isDigitalMediaCategory() {
			return this.m_lastMediaCategoryValue != null
					&& this.m_lastMediaCategoryValue.equals("Digital");
		}

		public String getLastMarketId() {
			return lastMarketId;
		}

		public void setLastMarketId(String lastMarketId) {
			this.lastMarketId = lastMarketId;
		}

		public String getCurrentMarketId() {
			return currentMarketId;
		}

		public void setCurrentMarketId(String currentMarketId) {
			this.currentMarketId = currentMarketId;
		}

		/*
		 * Returns true if eof (end of records) or current market id is different than the last market id value
		 */
		public boolean isMarketIdChanged() {
			return eof || !getCurrentMarketId().equals(getLastMarketId());
		}

		public String getLastPackageId() {
			return lastPackageId;
		}

		public void setLastPackageId(String lastPackageId) {
			this.lastPackageId = lastPackageId;
		}

		public String getCurrentPackageId() {
			return currentPackageId;
		}

		public void setCurrentPackageId(String currentPackageId) {
			this.currentPackageId = currentPackageId;
		}

		/*
		 * Returns true if eof (end of records) or current market id is different than the last market id value
		 */
		public boolean isPackageIdChanged() {
			return eof || !getCurrentPackageId().equals(getLastPackageId());
		}
	}

	private class PackageMarketFlightPreviousRecordHadAudienceExpression extends
			AbstractSimpleExpression<Boolean> {

		private static final long serialVersionUID = 1632549537807166153L;
		private PackageMarketFlightPreviousRecordExpression m_groupByExpression;

		public PackageMarketFlightPreviousRecordHadAudienceExpression(
				PackageMarketFlightPreviousRecordExpression groupByExpression) {
			m_groupByExpression = groupByExpression;
		}

		@Override
		public Boolean evaluate(ReportParameters reportParameters) {
			return this.m_groupByExpression.isBuyTypeAudience();
		}
	}

	private class PackageMarketFlightPreviousRecordHadLocationExpression extends
			AbstractSimpleExpression<Boolean> {

		private static final long serialVersionUID = 1635849537807166153L;
		private PackageMarketFlightPreviousRecordExpression m_groupByExpression;

		public PackageMarketFlightPreviousRecordHadLocationExpression(
				PackageMarketFlightPreviousRecordExpression groupByExpression) {
			m_groupByExpression = groupByExpression;
		}

		@Override
		public Boolean evaluate(ReportParameters reportParameters) {
			return this.m_groupByExpression.isBuyTypeLocation();
		}
	}

	private class PackageMarketFlightPreviousRecordHadRotaryExpression extends
			AbstractSimpleExpression<Boolean> {

		private static final long serialVersionUID = 1632549537807166153L;
		private PackageMarketFlightPreviousRecordExpression m_groupByExpression;

		public PackageMarketFlightPreviousRecordHadRotaryExpression(
				PackageMarketFlightPreviousRecordExpression groupByExpression) {
			m_groupByExpression = groupByExpression;
		}

		@Override
		public Boolean evaluate(ReportParameters reportParameters) {
			return this.m_groupByExpression.isBuyTypeRotary();
		}
	}

	private class PackageMarketFlightPreviousRecordHadNetworkExpression extends
			AbstractSimpleExpression<Boolean> {

		private static final long serialVersionUID = 1632549537807166153L;
		private PackageMarketFlightPreviousRecordExpression m_groupByExpression;

		public PackageMarketFlightPreviousRecordHadNetworkExpression(
				PackageMarketFlightPreviousRecordExpression groupByExpression) {
			m_groupByExpression = groupByExpression;
		}

		@Override
		public Boolean evaluate(ReportParameters reportParameters) {
			return this.m_groupByExpression.isBuyTypeNetwork();
		}
	}

	private class PackageMarketFlightPreviousRecordHadBuyTypeNetworkOrCustomAndMediaCategoryIsCommuterRailExpression
			extends AbstractSimpleExpression<Boolean> {

		private static final long serialVersionUID = 1635849537807166153L;
		private PackageMarketFlightPreviousRecordExpression m_groupByExpression;

		public PackageMarketFlightPreviousRecordHadBuyTypeNetworkOrCustomAndMediaCategoryIsCommuterRailExpression(
				PackageMarketFlightPreviousRecordExpression groupByExpression) {
			m_groupByExpression = groupByExpression;
		}

		@Override
		public Boolean evaluate(ReportParameters reportParameters) {
			return this.m_groupByExpression.isCommuterRailMediaCategory();
		}
	}

	/*
	 * private class PackageMarketFlightAudienceExpression extends
	 * AbstractSimpleExpression<Boolean> {
	 * 
	 * private static final long serialVersionUID = 1635849537807166153L;
	 * private PackageMarketFlightPreviousRecordExpression m_groupByExpression;
	 * 
	 * public PackageMarketFlightAudienceExpression(
	 * PackageMarketFlightPreviousRecordExpression groupByExpression) {
	 * m_groupByExpression = groupByExpression; }
	 * 
	 * @Override public Boolean evaluate(ReportParameters reportParameters) {
	 * return this.m_groupByExpression.isBuyTypeAudience(); } }
	 */

	private class PackageMarketFlightPreviousRecordHadBuyTypeNetworkOrCustomAndMediaCategoryIsBusOrTransitExpression
			extends AbstractSimpleExpression<Boolean> {

		private static final long serialVersionUID = 1635849537807166154L;
		private PackageMarketFlightPreviousRecordExpression m_groupByExpression;

		public PackageMarketFlightPreviousRecordHadBuyTypeNetworkOrCustomAndMediaCategoryIsBusOrTransitExpression(
				PackageMarketFlightPreviousRecordExpression groupByExpression) {
			m_groupByExpression = groupByExpression;
		}

		@Override
		public Boolean evaluate(ReportParameters reportParameters) {
			return this.m_groupByExpression.isBusOrTransitMediaCategory();
		}
	}

	private class PackageMarketFlightPreviousRecordHadBuyTypeNetworkOrCustomAndMediaCategoryIsMobileBillboardExpression
			extends AbstractSimpleExpression<Boolean> {

		private static final long serialVersionUID = 1635849537807166154L;
		private PackageMarketFlightPreviousRecordExpression m_groupByExpression;

		public PackageMarketFlightPreviousRecordHadBuyTypeNetworkOrCustomAndMediaCategoryIsMobileBillboardExpression(
				PackageMarketFlightPreviousRecordExpression groupByExpression) {
			m_groupByExpression = groupByExpression;
		}

		@Override
		public Boolean evaluate(ReportParameters reportParameters) {
			return this.m_groupByExpression.isMobileBillboardMediaCategory();
		}
	}

	private class PreviousFlightRecordHadExpression extends
			AbstractSimpleExpression<Boolean> {

		private static final long serialVersionUID = 163584443634L;
		private PackageMarketFlightPreviousRecordExpression m_groupByExpression;

		public PreviousFlightRecordHadExpression(
				PackageMarketFlightPreviousRecordExpression groupByExpression) {
			m_groupByExpression = groupByExpression;
		}

		@Override
		public Boolean evaluate(ReportParameters reportParameters) {
			return this.m_groupByExpression.isMobileBillboardMediaCategory()
					|| this.m_groupByExpression.isBusOrTransitMediaCategory()
					|| this.m_groupByExpression.isCommuterRailMediaCategory()
					|| this.m_groupByExpression.isBuyTypeLocation();
		}
	}

	private class PreviousFlightRecordDidntHaveExpression extends
			AbstractSimpleExpression<Boolean> {

		private static final long serialVersionUID = 163584443634L;
		private PackageMarketFlightPreviousRecordExpression m_groupByExpression;

		public PreviousFlightRecordDidntHaveExpression(
				PackageMarketFlightPreviousRecordExpression groupByExpression) {
			m_groupByExpression = groupByExpression;
		}

		@Override
		public Boolean evaluate(ReportParameters reportParameters) {
			return !(this.m_groupByExpression.isMobileBillboardMediaCategory()
					|| this.m_groupByExpression.isBusOrTransitMediaCategory()
					|| this.m_groupByExpression.isCommuterRailMediaCategory() || this.m_groupByExpression
						.isBuyTypeLocation());
		}
	}

	private void addFlightFields(JasperReportBuilder report) {
		FieldBuilder<String> flightTypeField = field("Type__c", String.class);
		FieldBuilder<String> mediaCategoryField = field("Media_Category__c", String.class);
		FieldBuilder<String> flightCommentsField = field("Flight_Comments__c", String.class);
		FieldBuilder<String> flightDivisionField = field("Division__c", String.class);
		FieldBuilder<String> flightPackageNameField = field("Package_Name__c", String.class);
		FieldBuilder<String> flightMarketNameField = field("Market_Name__c", String.class);
		FieldBuilder<String> flightMarketTypeField = field("Market_Type__c", String.class);
		FieldBuilder<String> flightNameField = field("Name", String.class);
		FieldBuilder<String> flightStartDateField = field("Campaign_Start_Date__c", String.class);
		FieldBuilder<String> flightEndDateField = field("Campaign_End_Date__c", String.class);
		FieldBuilder<String> flightDurationField = field("Duration_And_Type__c", String.class);
		FieldBuilder<String> flightTargetField = field("Target__c", String.class);
		FieldBuilder<Integer> flightTargetPopulationField = field("Target_Population__c", Integer.class);
		FieldBuilder<String> flightPackageCommentsField = field("Package_Comments__c", String.class);
		report.addField(flightTypeField);
		report.addField(mediaCategoryField);
		report.addField(flightCommentsField);
		report.addField(flightDivisionField);
		report.addField(flightPackageNameField);
		report.addField(flightMarketNameField);
		report.addField(flightMarketTypeField);
		report.addField(flightNameField);
		report.addField(flightStartDateField);
		report.addField(flightEndDateField);
		report.addField(flightDurationField);
		report.addField(flightTargetField);
		report.addField(flightTargetPopulationField);
		report.addField(flightPackageCommentsField);
	}
	
	private void addFlightHeader(JasperReportBuilder report, String packageId, ReportStyleBuilder flightHeaderStyle, ReportStyleBuilder flightHeaderValueStyle) {
		String[] headerFieldNamesArray1 = new String[6];
		String[] headerFieldLabelsArray1 = new String[6];
		String[] headerFieldNamesArray2 = new String[6];
		String[] headerFieldLabelsArray2 = new String[6];
		int arr1Idx = 0;
		int arr2Idx = 0;
		// iterate on the flight line column label hashmap (user selected and ordered fields) 
        for(String key : getFlightLineColumnLabelHashMap().keySet()) {
        	if (key.equals("Package_Flight__r/Division__c")) {
    			headerFieldNamesArray1[arr1Idx] = "Division__c";
    			headerFieldLabelsArray1[arr1Idx] = getFlightLineColumnLabelHashMap().get("Package_Flight__r/Division__c");
    			arr1Idx++;
    		}
    		if (key.equals("Package_Flight__r/Market_Name__c")) {
    			headerFieldNamesArray1[arr1Idx] = "Market_Name__c";
    			headerFieldLabelsArray1[arr1Idx] = getFlightLineColumnLabelHashMap().get("Package_Flight__r/Market_Name__c");
    			arr1Idx++;
    		}
    		if (key.equals("Package_Flight__r/Market_Type__c")) {
    			headerFieldNamesArray1[arr1Idx] = "Market_Type__c";
    			headerFieldLabelsArray1[arr1Idx] = getFlightLineColumnLabelHashMap().get("Package_Flight__r/Market_Type__c");
    			arr1Idx++;
    		}
    		if (key.equals("Package_Flight__r/Package_Name__c")) {
    			headerFieldNamesArray1[arr1Idx] = "Package_Name__c";
    			headerFieldLabelsArray1[arr1Idx] = getFlightLineColumnLabelHashMap().get("Package_Flight__r/Package_Name__c");
    			arr1Idx++;
    		}
    		if (key.equals("Package_Flight__r/Name")) {
    			headerFieldNamesArray1[arr1Idx] = "Name";
    			headerFieldLabelsArray1[arr1Idx] = getFlightLineColumnLabelHashMap().get("Package_Flight__r/Name");
    			arr1Idx++;
    		}
    		if (key.equals("Package_Flight__r/Type__c")) {
    			headerFieldNamesArray1[arr1Idx] = "Type__c";
    			headerFieldLabelsArray1[arr1Idx] = getFlightLineColumnLabelHashMap().get("Package_Flight__r/Type__c");
    			arr1Idx++;
    		}    		
    		if (key.equals("Package_Flight__r/Campaign_Start_Date__c")) {
    			headerFieldNamesArray2[arr2Idx] = "Campaign_Start_Date__c";
    			headerFieldLabelsArray2[arr2Idx] = getFlightLineColumnLabelHashMap().get("Package_Flight__r/Campaign_Start_Date__c");
    			arr2Idx++;
    		}
    		if (key.equals("Package_Flight__r/Campaign_End_Date__c")) {
    			headerFieldNamesArray2[arr2Idx] = "Campaign_End_Date__c";
    			headerFieldLabelsArray2[arr2Idx] = getFlightLineColumnLabelHashMap().get("Package_Flight__r/Campaign_End_Date__c");
    			arr2Idx++;
    		}
    		if (key.equals("Package_Flight__r/Duration_And_Type__c")) {
    			headerFieldNamesArray2[arr2Idx] = "Duration_And_Type__c";
    			headerFieldLabelsArray2[arr2Idx] = getFlightLineColumnLabelHashMap().get("Package_Flight__r/Duration_And_Type__c");
    			arr2Idx++;
    		}
    		if (key.equals("Package_Flight__r/Target__c")) {
    			headerFieldNamesArray2[arr2Idx] = "Target__c";
    			headerFieldLabelsArray2[arr2Idx] = getFlightLineColumnLabelHashMap().get("Package_Flight__r/Target__c");
    			arr2Idx++;
    		}
    		if (key.equals("Package_Flight__r/Target_Population__c")) {
    			headerFieldNamesArray2[arr2Idx] = "Target_Population__c";
    			headerFieldLabelsArray2[arr2Idx] = getFlightLineColumnLabelHashMap().get("Package_Flight__r/Target_Population__c");
    			arr2Idx++;
    		}
        }
		for(int i=arr1Idx; i<headerFieldNamesArray1.length; i++) {
			headerFieldNamesArray1[i] = "";
			headerFieldLabelsArray1[i] = "";
		}
		for(int i=arr2Idx; i<headerFieldNamesArray2.length; i++) {
			headerFieldNamesArray2[i] = "";
			headerFieldLabelsArray2[i] = "";
		}
		report.title(createVerticalTable(flightHeaderStyle, flightHeaderValueStyle,
				headerFieldNamesArray1, headerFieldLabelsArray1,
				headerFieldNamesArray2, headerFieldLabelsArray2, Units.inch(2), packageId));
	}
	
	private class PackageHeaderSubreportExpression extends
			AbstractSimpleExpression<JasperReportBuilder> {

		private static final long serialVersionUID = -4488328880017058659L;
		
		private StyleBuilder flightHeaderStyle;

		private StyleBuilder flightHeaderValueStyle;
		
		public PackageHeaderSubreportExpression(StyleBuilder flightHeaderStyle, StyleBuilder flightHeaderValueStyle) {
			setFlightHeaderStyle(flightHeaderStyle);
			setFlightHeaderValueStyle(flightHeaderValueStyle);
		}

		@Override
		public JasperReportBuilder evaluate(ReportParameters reportParameters) {
			JasperReportBuilder report = report();

			// ======================================================== begin
			// add flight fields
			report.addField(field("Package_Market__r/Package__r/Id", type.stringType()));
			String packageId = reportParameters.getValue("Package_Flight__r/Package_Market__r/Package__r/Id");
			addFlightFields(report);
			// add flight headers
			addFlightHeader(report, packageId, getFlightHeaderStyle(), getFlightHeaderValueStyle());
			// ======================================================== end
			
			// add a blank line at the end
			report.addLastPageFooter(cmp.text(""));

			// return report
			return report;
		}

		public StyleBuilder getFlightHeaderStyle() {
			return flightHeaderStyle;
		}

		public void setFlightHeaderStyle(StyleBuilder flightHeaderStyle) {
			this.flightHeaderStyle = flightHeaderStyle;
		}

		public StyleBuilder getFlightHeaderValueStyle() {
			return flightHeaderValueStyle;
		}

		public void setFlightHeaderValueStyle(StyleBuilder flightHeaderValueStyle) {
			this.flightHeaderValueStyle = flightHeaderValueStyle;
		}
	}
	
	private class PackageFooterSubreportExpression extends
			AbstractSimpleExpression<JasperReportBuilder> {

		private static final long serialVersionUID = 8852584488641186565L;

		public PackageFooterSubreportExpression() {
			
		}

		@Override
		public JasperReportBuilder evaluate(ReportParameters reportParameters) {
			JasperReportBuilder report = report();
			StyleBuilder boldStyle = stl.style().bold();
			StyleBuilder summaryStyle = stl.style().setPadding(Units.inch(0.03)).setFontSize(10);
			
			// ======================================================== begin
			report.addField(field("Id", type.stringType()));
			
			String packageId = reportParameters.getValue("Id");
			VerticalListBuilder flightCommentReports = cmp.verticalList();
			flightCommentReports.add(createCommentTable(packageId).removeLineWhenBlank());
			flightCommentReports.add(cmp.text(""));
			report.summary(flightCommentReports);
				
			// create a vertical list of Valid disclaimer reports as per rules
			ValidDisclaimerExistsExpression validDisclaimerExistsExpression =  new ValidDisclaimerExistsExpression();
			VerticalListBuilder validDisclaimerReports = cmp.verticalList();
			validDisclaimerReports.setPrintWhenExpression(validDisclaimerExistsExpression);
			validDisclaimerReports.add(cmp.text("Important Notes:").setStyle(boldStyle).removeLineWhenBlank());
			validDisclaimerReports.add(cmp.text(new DisclaimerExpression()).setStyle(summaryStyle));
			report.summary(validDisclaimerReports);
			// ======================================================== end
			
			// add a blank line at the end
			report.addLastPageFooter(cmp.text(""));

			// return report
			return report;
		}
	}

	private class AudienceSubreportExpression extends
			AbstractSimpleExpression<JasperReportBuilder> {

		private static final long serialVersionUID = 1656551952324L;

		private PackageMarketFlightPreviousRecordExpression packageMarketFlightPreviousRecordExpression;

		private StyleBuilder columnTitleStyle;

		private StyleBuilder columnStyle;

		private boolean exportAsExcel;

		private boolean showSummaryHeaders;

		private String dataSourceFileName;

		private SummaryLevelEnum summaryLevel;

		public AudienceSubreportExpression(
				String dataSourceFileName,
				PackageMarketFlightPreviousRecordExpression packageMarketFlightPreviousRecordExpression,
				StyleBuilder columnTitleStyle, StyleBuilder columnStyle,
				boolean exportAsExcel, boolean showSummaryHeaders) {
			this(dataSourceFileName, packageMarketFlightPreviousRecordExpression, columnTitleStyle,
					columnStyle, exportAsExcel, showSummaryHeaders, null);
		}

		public AudienceSubreportExpression(
				String dataSourceFileName,
				PackageMarketFlightPreviousRecordExpression packageMarketFlightPreviousRecordExpression,
				StyleBuilder columnTitleStyle, StyleBuilder columnStyle,
				boolean exportAsExcel, boolean showSummaryHeaders,
				SummaryLevelEnum summaryLevel) {
			setDataSourceFileName(dataSourceFileName);
			setPackageMarketFlightPreviousRecordExpression(packageMarketFlightPreviousRecordExpression);
			setColumnTitleStyle(columnTitleStyle);
			setColumnStyle(columnStyle);
			setExportAsExcel(exportAsExcel);
			setShowSummaryHeaders(showSummaryHeaders);
			setSummaryLevel(summaryLevel);
		}

		@Override
		public JasperReportBuilder evaluate(ReportParameters reportParameters) {
			JasperReportBuilder report = report();

			// add columns
			addColumns(report, getFlightLineColumnLabelHashMap());
			
			// add fields
			report.addField(field("Package_Flight__r/Id", type.stringType()));
			report.addField(field("Id", type.stringType()));

			// style
			if(!isExportAsExcel()) {
				report.highlightDetailEvenRows();
			}
			report.setColumnStyle(getColumnStyle());
			report.setColumnTitleStyle(getColumnTitleStyle());
			// filter
			if(getSummaryLevel() == null) {
				report.setFilterExpression(new FilterByFlightIdExpression(getPackageMarketFlightPreviousRecordExpression()));
			} else if(getSummaryLevel() == SummaryLevelEnum.Market) {
				// set filter by market
				report.setFilterExpression(new FilterByMarketIdExpression(getPackageMarketFlightPreviousRecordExpression()));
			} else if(getSummaryLevel() == SummaryLevelEnum.Package) {
				// set filter by market
				report.setFilterExpression(new FilterByPackageIdExpression(getPackageMarketFlightPreviousRecordExpression()));
				if(!isExportAsExcel()) {
					createPackageCommentsAndDisclamersFooter(getDataSourceFileName(), report);
				}
			}

			// add a blank line at the end
			report.addLastPageFooter(cmp.text(""));

			// return report
			return report;
			
		}
		
		private JasperReportBuilder addColumns(JasperReportBuilder report, Map<String, String> fieldMap) {

			// types
			CurrencyWithFractionDecimalType currencyWithFractionDecimalType = new CurrencyWithFractionDecimalType();
			CurrencyWithoutFractionDecimalType currencyWithoutFractionDecimalType = new CurrencyWithoutFractionDecimalType();

			// indicates (summary mode only) that first column has been overriden with Summary column
			boolean firstColumnOverriden = false;

			for(String key : fieldMap.keySet()) {
				if (isExportAsExcel()) {
					report.setIgnorePageWidth(true);
					if(key.equals("Package_Flight__r/Package_Name__c")) {
						if(!isShowSummaryHeaders() || firstColumnOverriden) {
							report.addColumn(col.column(
									this.isShowSummaryHeaders() ? "" : getFlightLineColumnLabelHashMap().get("Package_Flight__r/Package_Name__c"),
									this.isShowSummaryHeaders() ? "Parent_Flight_Line__c" : "Package_Flight__r/Package_Name__c", type.stringType())
									.setWidth(Units.inch(1.32)));
						} else {
							if(!firstColumnOverriden) {
								addSummaryField(getSummaryLevel(), report);
								firstColumnOverriden = true;
							}
						}
					}
					if(key.equals("Package_Flight__r/Name")) {
						if(!isShowSummaryHeaders() || firstColumnOverriden) {
							report.addColumn(col.column(
									this.isShowSummaryHeaders() ? "" : getFlightLineColumnLabelHashMap().get("Package_Flight__r/Name"),
									this.isShowSummaryHeaders() ? "Parent_Flight_Line__c" : "Package_Flight__r/Name", type.stringType())
									.setWidth(Units.inch(1.32)));
						} else {
							if(!firstColumnOverriden) {
								addSummaryField(getSummaryLevel(), report);
								firstColumnOverriden = true;
							}
						}
					}
					if(key.equals("Package_Flight__r/Division__c")) {
						if(!isShowSummaryHeaders() || firstColumnOverriden) {
							report.addColumn(col.column(
									this.isShowSummaryHeaders() ? "" : getFlightLineColumnLabelHashMap().get("Package_Flight__r/Division__c"),
									this.isShowSummaryHeaders() ? "Parent_Flight_Line__c" : "Package_Flight__r/Division__c", type.stringType())
									.setWidth(Units.inch(1.32)));
						} else {
							if(!firstColumnOverriden) {
								addSummaryField(getSummaryLevel(), report);
								firstColumnOverriden = true;
							}
						}
					}
					if(key.equals("Package_Flight__r/Market_Name__c")) {
						if(!isShowSummaryHeaders() || firstColumnOverriden) {
							report.addColumn(col.column(
									this.isShowSummaryHeaders() ? "" :	getFlightLineColumnLabelHashMap().get("Package_Flight__r/Market_Name__c"),
									this.isShowSummaryHeaders() ? "Parent_Flight_Line__c" : "Package_Flight__r/Market_Name__c", type.stringType())
									.setWidth(Units.inch(1.32)));
						} else {
							if(!firstColumnOverriden) {
								addSummaryField(getSummaryLevel(), report);
								firstColumnOverriden = true;
							}
						}
					}
					if (key.equals("Package_Flight__r/Market_Type__c")) {
						if(!isShowSummaryHeaders() || firstColumnOverriden) {
							report.addColumn(col.column(
									this.isShowSummaryHeaders() ? "" : getFlightLineColumnLabelHashMap().get("Package_Flight__r/Market_Type__c"),
									this.isShowSummaryHeaders() ? "Parent_Flight_Line__c" : "Package_Flight__r/Market_Type__c",
									type.stringType()).setWidth(Units.inch(1.32)));
						} else {
							if(!firstColumnOverriden) {
								addSummaryField(getSummaryLevel(), report);
								firstColumnOverriden = true;
							}
						}
					}
					if(key.equals("Package_Flight__r/Campaign_Start_Date__c")) {
						if(!isShowSummaryHeaders() || firstColumnOverriden) {
							report.addColumn(col.column(
									this.isShowSummaryHeaders() ? "" : getFlightLineColumnLabelHashMap().get("Package_Flight__r/Campaign_Start_Date__c"),
									this.isShowSummaryHeaders() ? "Parent_Flight_Line__c" : "Package_Flight__r/Campaign_Start_Date__c",	type.stringType())
									.setHorizontalAlignment(HorizontalAlignment.CENTER).setWidth(Units.inch(1.32)).setValueFormatter(new ValueFormatter()));
						} else {
							if(!firstColumnOverriden) {
								addSummaryField(getSummaryLevel(), report);
								firstColumnOverriden = true;
							}
						}
					}
					if(key.equals("Package_Flight__r/Campaign_End_Date__c")) {
						if(!isShowSummaryHeaders() || firstColumnOverriden) {
							report.addColumn(col.column(
									this.isShowSummaryHeaders() ? "" : getFlightLineColumnLabelHashMap().get("Package_Flight__r/Campaign_End_Date__c"),
									this.isShowSummaryHeaders() ? "Parent_Flight_Line__c" : "Package_Flight__r/Campaign_End_Date__c", type.stringType())
									.setHorizontalAlignment(HorizontalAlignment.CENTER).setWidth(Units.inch(1.32)).setValueFormatter(new ValueFormatter()));
						} else {
							if(!firstColumnOverriden) {
								addSummaryField(getSummaryLevel(), report);
								firstColumnOverriden = true;
							}
						}
					}
					if(key.equals("Package_Flight__r/Duration_And_Type__c")) {
						if(!isShowSummaryHeaders() || firstColumnOverriden) {
							report.addColumn(col.column(
									this.isShowSummaryHeaders() ? "" : getFlightLineColumnLabelHashMap().get("Package_Flight__r/Duration_And_Type__c"),
									this.isShowSummaryHeaders() ? "Parent_Flight_Line__c" : "Package_Flight__r/Duration_And_Type__c", type.stringType())
									.setWidth(Units.inch(1.32)));
						} else {
							if(!firstColumnOverriden) {
								addSummaryField(getSummaryLevel(), report);
								firstColumnOverriden = true;
							}
						}
					}
					if(key.equals("Package_Flight__r/Target__c")) {
						if(!isShowSummaryHeaders() || firstColumnOverriden) {
							report.addColumn(col.column(
									this.isShowSummaryHeaders() ? "" : getFlightLineColumnLabelHashMap().get("Package_Flight__r/Target__c"),
									this.isShowSummaryHeaders() ? "Parent_Flight_Line__c" : "Package_Flight__r/Target__c", type.stringType())
									.setWidth(Units.inch(1.32)));
						} else {
							if(!firstColumnOverriden) {
								addSummaryField(getSummaryLevel(), report);
								firstColumnOverriden = true;
							}
						}
					}
					if(key.equals("Package_Flight__r/Target_Population__c")) {
						if(!isShowSummaryHeaders() || firstColumnOverriden) {
							report.addColumn(col.column(
									this.isShowSummaryHeaders() ? "" : getFlightLineColumnLabelHashMap().get("Package_Flight__r/Target_Population__c"),
									this.isShowSummaryHeaders() ? "OB_Summ_Num__c" : "Package_Flight__r/Target_Population__c",
									type.bigDecimalType()).setWidth(Units.inch(1.32)));
						} else {
							if(!firstColumnOverriden) {
								addSummaryField(getSummaryLevel(), report);
								firstColumnOverriden = true;
							}
						}
					}
					if (key.equals("Package_Flight__r/Flight_Comments__c")) {
						if(!isShowSummaryHeaders() || firstColumnOverriden) {
							report.addColumn(col.column(
									this.isShowSummaryHeaders() ? "" :	getFlightLineColumnLabelHashMap().get("Package_Flight__r/Flight_Comments__c"),
									this.isShowSummaryHeaders() ? "Parent_Flight_Line__c" : "Package_Flight__r/Flight_Comments__c", type.stringType())
									.setWidth(Units.inch(1.32)));
						} else {
							if(!firstColumnOverriden) {
								addSummaryField(getSummaryLevel(), report);
								firstColumnOverriden = true;
							}
						}
					}
					if (key.equals("Package_Flight__r/Package_Comments__c")) {
						if(!isShowSummaryHeaders() || firstColumnOverriden) {
							report.addColumn(col.column(
									this.isShowSummaryHeaders() ? "" :	getFlightLineColumnLabelHashMap().get("Package_Flight__r/Package_Comments__c"),
									this.isShowSummaryHeaders() ? "Parent_Flight_Line__c" : "Package_Flight__r/Package_Comments__c", type.stringType())
									.setWidth(Units.inch(1.32)));
						} else {
							if(!firstColumnOverriden) {
								addSummaryField(getSummaryLevel(), report);
								firstColumnOverriden = true;
							}
						}
					}
					if(key.equals("Weekly_Total_18_Imps__c")) {
						TextColumnBuilder<BigDecimal> weeklyTotal18ImpsColumn = col.column(
								getFlightLineColumnLabelHashMap().get("Weekly_Total_18_Imps__c"),
								getSummaryFieldNameRelatedToFlightLineFieldName(getSummaryLevel(), "Weekly_Total_18_Imps__c"),
								type.bigDecimalType()).setHorizontalAlignment(HorizontalAlignment.RIGHT).setPattern("#,###");
						weeklyTotal18ImpsColumn.setWidth(Units.inch(1.32));
						report.addColumn(weeklyTotal18ImpsColumn);
					}
				}
				if(key.equals("Weekly_Total_18_Imps__c")) {
					TextColumnBuilder<BigDecimal> weeklyTotal18ImpsColumn = col.column(
							getFlightLineColumnLabelHashMap().get("Weekly_Total_18_Imps__c"), "Weekly_Total_18_Imps__c",
							type.bigDecimalType()).setHorizontalAlignment(HorizontalAlignment.RIGHT).setPattern("#,###");
					if (isExportAsExcel()) {
						weeklyTotal18ImpsColumn.setWidth(Units.inch(1.32));
					} else {
						weeklyTotal18ImpsColumn.setWidth(Units.inch(3));
					}
					report.addColumn(weeklyTotal18ImpsColumn);
				}
				if (key.equals("Total_Imps__c")) {
					TextColumnBuilder<BigDecimal> totalImpsColumn = col.column(
							getFlightLineColumnLabelHashMap().get("Total_Imps__c"),
							getSummaryFieldNameRelatedToFlightLineFieldName(getSummaryLevel(), "Total_Imps__c"),
							type.bigDecimalType()).setHorizontalAlignment(HorizontalAlignment.RIGHT).setPattern("#,###");
					if (isExportAsExcel()) {
						totalImpsColumn.setWidth(Units.inch(1.32));
					} else {
						totalImpsColumn.setWidth(Units.inch(3));
					}
					report.addColumn(totalImpsColumn);
				}
				if(key.equals("Total_Price_0d__c")) {
					TextColumnBuilder<BigDecimal> totalPriceColumn = col.column(
							getFlightLineColumnLabelHashMap().get("Total_Price_0d__c"),
							getSummaryFieldNameRelatedToFlightLineFieldName(getSummaryLevel(), "Total_Price_0d__c"),
							currencyWithoutFractionDecimalType).setHorizontalAlignment(HorizontalAlignment.RIGHT);
					if (isExportAsExcel()) {
						totalPriceColumn.setWidth(Units.inch(1.32));
					} else {
						totalPriceColumn.setWidth(Units.inch(2.5));
					}
					report.addColumn(totalPriceColumn);
				}
				if(key.equals("Media_Category__c")) {
					if(!isShowSummaryHeaders() || firstColumnOverriden) {
						TextColumnBuilder<String> mediaTypeColumn = col.column(
								this.isShowSummaryHeaders() ? "" : getFlightLineColumnLabelHashMap().get("Media_Category__c"), 
								this.isShowSummaryHeaders() ? "Parent_Flight_Line__c" : "Media_Category__c",
								type.stringType());
						if (isExportAsExcel()) {
							mediaTypeColumn.setWidth(Units.inch(1.32));
						} else {
							mediaTypeColumn.setWidth(Units.inch(2.0));
						}
						report.addColumn(mediaTypeColumn);
					} else {
						if(!firstColumnOverriden) {
							addSummaryField(getSummaryLevel(), report);
							firstColumnOverriden = true;
						}
					}
				}
				if(key.equals("Number_of_Panels__c")) {
					if(!isShowSummaryHeaders() || firstColumnOverriden) {
						TextColumnBuilder<BigDecimal> noOfPanelsColumn = col.column(
								this.isShowSummaryHeaders() ? "" : getFlightLineColumnLabelHashMap().get("Number_of_Panels__c"),
								this.isShowSummaryHeaders() ? "OB_Summ_Num__c" : "Number_of_Panels__c",
								type.bigDecimalType()).setHorizontalAlignment(HorizontalAlignment.RIGHT).setPattern("#,###");
						report.addColumn(noOfPanelsColumn);
					} else {
						if(!firstColumnOverriden) {
							addSummaryField(getSummaryLevel(), report);
							firstColumnOverriden = true;
						}
					}
				}
				if(key.equals("In_Mkt_Imps__c")) {
					TextColumnBuilder<BigDecimal> targetInMarketImpsColumn = col.column(
							getFlightLineColumnLabelHashMap().get("In_Mkt_Imps__c"),
							getSummaryFieldNameRelatedToFlightLineFieldName(getSummaryLevel(), "In_Mkt_Imps__c"),
							type.bigDecimalType()).setHorizontalAlignment(HorizontalAlignment.RIGHT).setPattern("#,###");
					if (isExportAsExcel()) {
						targetInMarketImpsColumn.setWidth(Units.inch(1.32));
					} else {
						targetInMarketImpsColumn.setWidth(Units.inch(3));
					}
					report.addColumn(targetInMarketImpsColumn);
				}
				if(key.equals("In_Mkt_TRP__c")) {
					TextColumnBuilder<Double> weekklyTRPColumn = col.column(
							getFlightLineColumnLabelHashMap().get("In_Mkt_TRP__c"),
							getSummaryFieldNameRelatedToFlightLineFieldName(getSummaryLevel(), "In_Mkt_TRP__c"),
							type.doubleType()).setHorizontalAlignment(HorizontalAlignment.RIGHT).setPattern("##0.0");
					if (isExportAsExcel()) {
						weekklyTRPColumn.setWidth(Units.inch(1.32));
					} else {
						weekklyTRPColumn.setWidth(Units.inch(3));
					}
					report.addColumn(weekklyTRPColumn);
				}
				if(key.equals("PlanTRP__c")) {
					TextColumnBuilder<Double> planTRPColumn = col.column(
							getFlightLineColumnLabelHashMap().get("PlanTRP__c"),
							getSummaryFieldNameRelatedToFlightLineFieldName(getSummaryLevel(), "PlanTRP__c"),
							type.doubleType()).setHorizontalAlignment(HorizontalAlignment.RIGHT).setPattern("##0.0");
					if (isExportAsExcel()) {
						planTRPColumn.setWidth(Units.inch(1.32));
					} else {
						planTRPColumn.setWidth(Units.inch(2));
					}
					report.addColumn(planTRPColumn);
				}
				if(key.equals("Plan_Imps_Reach_Perc__c")) {
					if(!isShowSummaryHeaders() || firstColumnOverriden) {
						TextColumnBuilder<Double> planImpsReachPercColumn = col.column(
								getFlightLineColumnLabelHashMap().get("Plan_Imps_Reach_Perc__c"), "Plan_Imps_Reach_Perc__c",
								type.percentageType()).setHorizontalAlignment(HorizontalAlignment.RIGHT).setPattern("##0.0");
						if (isExportAsExcel()) {
							planImpsReachPercColumn.setWidth(Units.inch(1.32));
						} else {
							planImpsReachPercColumn.setWidth(Units.inch(1.5));
						}
						report.addColumn(planImpsReachPercColumn);
					} else {
						if(!firstColumnOverriden) {
							addSummaryField(getSummaryLevel(), report);
							firstColumnOverriden = true;
						}
					}
				}
				if(key.equals("Plan_Imps_Avg_Frequency__c")) {
					if(!isShowSummaryHeaders() || firstColumnOverriden) {
						TextColumnBuilder<Double> frequencyColumn = col.column(
								getFlightLineColumnLabelHashMap().get("Plan_Imps_Avg_Frequency__c"), "Plan_Imps_Avg_Frequency__c",
								type.doubleType()).setHorizontalAlignment(HorizontalAlignment.RIGHT).setPattern("##0.0");
						if (isExportAsExcel()) {
							frequencyColumn.setWidth(Units.inch(1.32));
						}
						report.addColumn(frequencyColumn);
					} else {
						if(!firstColumnOverriden) {
							addSummaryField(getSummaryLevel(), report);
							firstColumnOverriden = true;
						}
					}
				}
				if(key.equals("X4_Wk_Proposed_Price__c")) {
					if(!isShowSummaryHeaders() || firstColumnOverriden) {
						TextColumnBuilder<BigDecimal> X4WkProposedPriceColumn = col.column(
								this.isShowSummaryHeaders() ? "" : getFlightLineColumnLabelHashMap().get("X4_Wk_Proposed_Price__c"),
								this.isShowSummaryHeaders() ? "OB_Summ_Num__c" : "X4_Wk_Proposed_Price__c",
								currencyWithoutFractionDecimalType).setHorizontalAlignment(HorizontalAlignment.RIGHT);
						if (isExportAsExcel()) {
							X4WkProposedPriceColumn.setWidth(Units.inch(1.32));
						} else {
							X4WkProposedPriceColumn.setWidth(Units.inch(2.5));
						}
						report.addColumn(X4WkProposedPriceColumn);
					} else {
						if(!firstColumnOverriden) {
							addSummaryField(getSummaryLevel(), report);
							firstColumnOverriden = true;
						}
					}						
				}
				if(key.equals("TotalInMarketCPM_0d__c")) {
					TextColumnBuilder<BigDecimal> totalInMarketCPM0dColumn = col.column(
							getFlightLineColumnLabelHashMap().get("TotalInMarketCPM_0d__c"),
							getSummaryFieldNameRelatedToFlightLineFieldName(getSummaryLevel(), "TotalInMarketCPM_0d__c"),
							currencyWithFractionDecimalType).setHorizontalAlignment(HorizontalAlignment.RIGHT);
					if (isExportAsExcel()) {
						totalInMarketCPM0dColumn.setWidth(Units.inch(1.32));
					} else {
						totalInMarketCPM0dColumn.setWidth(Units.inch(2.5));
					}
					report.addColumn(totalInMarketCPM0dColumn);
				}
				if(key.equals("CPP_0d__c")) {
					TextColumnBuilder<BigDecimal> cppColumn = col.column(
							getFlightLineColumnLabelHashMap().get("CPP_0d__c"),
							getSummaryFieldNameRelatedToFlightLineFieldName(getSummaryLevel(), "CPP_0d__c"),
							currencyWithoutFractionDecimalType).setHorizontalAlignment(HorizontalAlignment.RIGHT);
					if (isExportAsExcel()) {
						cppColumn.setWidth(Units.inch(1.32));
					} else {
						cppColumn.setWidth(Units.inch(2.5));
					}
					report.addColumn(cppColumn);
				}
				if(key.equals("Comments__c")) {
					if(!isShowSummaryHeaders() || firstColumnOverriden) {
						TextColumnBuilder<String> commentsColumn = col.column(
								this.isShowSummaryHeaders() ? "" : getFlightLineColumnLabelHashMap().get("Comments__c"),
								this.isShowSummaryHeaders() ? "Parent_Flight_Line__c" : "Comments__c",
								type.stringType()).setHorizontalAlignment(HorizontalAlignment.LEFT);
						if (isExportAsExcel()) {
							commentsColumn.setWidth(Units.inch(1.32));
						} else {
							commentsColumn.setWidth(Units.inch(3.5));
						}
						report.addColumn(commentsColumn);
					} else {
						if(!firstColumnOverriden) {
							addSummaryField(getSummaryLevel(), report);
							firstColumnOverriden = true;
						}
					}
				}
				if (key.equals("Timing__c")) {
					if(!isShowSummaryHeaders() || firstColumnOverriden) {
						TextColumnBuilder<String> timingColumn = col.column(
								this.isShowSummaryHeaders() ? "" : getFlightLineColumnLabelHashMap().get("Timing__c"),
								this.isShowSummaryHeaders() ? "Parent_Flight_Line__c" : "Timing__c",
								type.stringType()).setHorizontalAlignment(HorizontalAlignment.LEFT);
						if (isExportAsExcel()) {
							timingColumn.setWidth(Units.inch(1.32));
						} else {
							timingColumn.setWidth(Units.inch(3));
						}
						report.addColumn(timingColumn);
					} else {
						if(!firstColumnOverriden) {
							addSummaryField(getSummaryLevel(), report);
							firstColumnOverriden = true;
						}
					}
				}
				if (key.equals("Weekly_Total_18_Imps_000__c")) {
					if(!isShowSummaryHeaders() || firstColumnOverriden) {
						TextColumnBuilder<BigDecimal> weeklyTotal18Imps000Column = col.column(
								getFlightLineColumnLabelHashMap().get("Weekly_Total_18_Imps_000__c"), "Weekly_Total_18_Imps_000__c",
								type.bigDecimalType()).setHorizontalAlignment(HorizontalAlignment.RIGHT).setPattern("#,###");
						if (isExportAsExcel()) {
							weeklyTotal18Imps000Column.setWidth(Units.inch(1.32));
						} else {
							weeklyTotal18Imps000Column.setWidth(Units.inch(3));
						}
						report.addColumn(weeklyTotal18Imps000Column);
					} else {
						if(!firstColumnOverriden) {
							addSummaryField(getSummaryLevel(), report);
							firstColumnOverriden = true;
						}
					}
				}
				if (key.equals("WeeklyMarketImps__c")) {
					if(!isShowSummaryHeaders() || firstColumnOverriden) {
						TextColumnBuilder<BigDecimal> weeklyTotalTargetImpsColumn = col.column(
								getFlightLineColumnLabelHashMap().get("WeeklyMarketImps__c"), "WeeklyMarketImps__c",
								type.bigDecimalType()).setHorizontalAlignment(HorizontalAlignment.RIGHT).setPattern("#,###");
						if (isExportAsExcel()) {
							weeklyTotalTargetImpsColumn.setWidth(Units.inch(1.32));
						} else {
							weeklyTotalTargetImpsColumn.setWidth(Units.inch(3));
						}
						report.addColumn(weeklyTotalTargetImpsColumn);
					} else {
						if(!firstColumnOverriden) {
							addSummaryField(getSummaryLevel(), report);
							firstColumnOverriden = true;
						}
					}
				}
				if (key.equals("Weekly_Total_Target_Imps_000__c")) {
					if(!isShowSummaryHeaders() || firstColumnOverriden) {
						TextColumnBuilder<BigDecimal> weeklyTotalTargetImps000Column = col.column(
								getFlightLineColumnLabelHashMap().get("Weekly_Total_Target_Imps_000__c"), "Weekly_Total_Target_Imps_000__c",
								type.bigDecimalType()).setHorizontalAlignment(HorizontalAlignment.RIGHT).setPattern("#,###");
						if (isExportAsExcel()) {
							weeklyTotalTargetImps000Column.setWidth(Units.inch(1.32));
						} else {
							weeklyTotalTargetImps000Column.setWidth(Units.inch(3));
						}
						report.addColumn(weeklyTotalTargetImps000Column);
					} else {
						if(!firstColumnOverriden) {
							addSummaryField(getSummaryLevel(), report);
							firstColumnOverriden = true;
						}
					}
				}
				if (key.equals("WeeklyInMarketImps__c")) {
					if(!isShowSummaryHeaders() || firstColumnOverriden) {
						TextColumnBuilder<BigDecimal> weeklyInMarketTargetImpsColumn = col.column(
								getFlightLineColumnLabelHashMap().get("WeeklyInMarketImps__c"), "WeeklyInMarketImps__c",
								type.bigDecimalType()).setHorizontalAlignment(HorizontalAlignment.RIGHT).setPattern("#,###");
						if (isExportAsExcel()) {
							weeklyInMarketTargetImpsColumn.setWidth(Units.inch(1.32));
						} else {
							weeklyInMarketTargetImpsColumn.setWidth(Units.inch(3));
						}
						report.addColumn(weeklyInMarketTargetImpsColumn);
					} else {
						if(!firstColumnOverriden) {
							addSummaryField(getSummaryLevel(), report);
							firstColumnOverriden = true;
						}
					}
				}
				if (key.equals("Weekly_In_Market_Target_Imps_000__c")) {
					if(!isShowSummaryHeaders() || firstColumnOverriden) {
						TextColumnBuilder<BigDecimal> weeklyInMarketTargetImps000Column = col.column(
								getFlightLineColumnLabelHashMap().get("Weekly_In_Market_Target_Imps_000__c"), "Weekly_In_Market_Target_Imps_000__c",
								type.bigDecimalType()).setHorizontalAlignment(HorizontalAlignment.RIGHT).setPattern("#,###");
						if (isExportAsExcel()) {
							weeklyInMarketTargetImps000Column.setWidth(Units.inch(1.32));
						} else {
							weeklyInMarketTargetImps000Column.setWidth(Units.inch(3));
						}
						report.addColumn(weeklyInMarketTargetImps000Column);
					} else {
						if(!firstColumnOverriden) {
							addSummaryField(getSummaryLevel(), report);
							firstColumnOverriden = true;
						}
					}
				}
				if (key.equals("Target_In_Market_Imps_000__c")) {
					if(!isShowSummaryHeaders() || firstColumnOverriden) {
						TextColumnBuilder<BigDecimal> targetInMarketImps000Column = col.column(
								getFlightLineColumnLabelHashMap().get("Target_In_Market_Imps_000__c"), "Target_In_Market_Imps_000__c",
								type.bigDecimalType()).setHorizontalAlignment(HorizontalAlignment.RIGHT).setPattern("#,###");
						if (isExportAsExcel()) {
							targetInMarketImps000Column.setWidth(Units.inch(1.32));
						} else {
							targetInMarketImps000Column.setWidth(Units.inch(3));
						}
						report.addColumn(targetInMarketImps000Column);
					} else {
						if(!firstColumnOverriden) {
							addSummaryField(getSummaryLevel(), report);
							firstColumnOverriden = true;
						}
					}
				}
				if (key.equals("Target_Total_Imps_000__c")) {
					if(!isShowSummaryHeaders() || firstColumnOverriden) {
						TextColumnBuilder<BigDecimal> totalImps000Column = col.column(getFlightLineColumnLabelHashMap().get("Target_Total_Imps_000__c"), "Target_Total_Imps_000__c",
								type.bigDecimalType()).setHorizontalAlignment(HorizontalAlignment.RIGHT).setPattern("#,###");
						if (isExportAsExcel()) {
							totalImps000Column.setWidth(Units.inch(1.32));
						} else {
							totalImps000Column.setWidth(Units.inch(3));
						}
						report.addColumn(totalImps000Column);
					} else {
						if(!firstColumnOverriden) {
							addSummaryField(getSummaryLevel(), report);
							firstColumnOverriden = true;
						}
					}
				}
				if (key.equals("X4_Wk_Avg_Rate_per_Panel__c")) {
					if(!isShowSummaryHeaders() || firstColumnOverriden) {
						TextColumnBuilder<String> x4WkAverageRatePanelColumn = col.column(
								this.isShowSummaryHeaders() ? "" : getFlightLineColumnLabelHashMap().get("X4_Wk_Avg_Rate_per_Panel__c"),
								this.isShowSummaryHeaders() ? "Parent_Flight_Line__c" : "X4_Wk_Avg_Rate_per_Panel__c",
								type.stringType()).setHorizontalAlignment(HorizontalAlignment.RIGHT);
						if (isExportAsExcel()) {
							x4WkAverageRatePanelColumn.setWidth(Units.inch(1.32));
						} else {
							x4WkAverageRatePanelColumn.setWidth(Units.inch(1.5));
						}
						report.addColumn(x4WkAverageRatePanelColumn);
					} else {
						if(!firstColumnOverriden) {
							addSummaryField(getSummaryLevel(), report);
							firstColumnOverriden = true;
						}
					}
				}
				if (key.equals("Net_Amount_Value__c")) {
					if(!isShowSummaryHeaders() || firstColumnOverriden) {
						TextColumnBuilder<BigDecimal> subTotalPriceColumn = col.column(getFlightLineColumnLabelHashMap().get("Net_Amount_Value__c"),
								"Net_Amount_Value__c", currencyWithoutFractionDecimalType).setHorizontalAlignment(HorizontalAlignment.RIGHT);
						if (isExportAsExcel()) {
							subTotalPriceColumn.setWidth(Units.inch(1.32));
						} else {
							subTotalPriceColumn.setWidth(Units.inch(2.5));
						}
						report.addColumn(subTotalPriceColumn);
					} else {
						if(!firstColumnOverriden) {
							addSummaryField(getSummaryLevel(), report);
							firstColumnOverriden = true;
						}
					}
				}
				if (key.equals("In_Mkt_Perc_Comp__c")) {
					if(!isShowSummaryHeaders() || firstColumnOverriden) {
						TextColumnBuilder<BigDecimal> inMktPercCompColumn = col.column(
								this.isShowSummaryHeaders() ? "" : getFlightLineColumnLabelHashMap().get("In_Mkt_Perc_Comp__c"),
								this.isShowSummaryHeaders() ? "OB_Summ_Num__c" : "In_Mkt_Perc_Comp__c",
								type.bigDecimalType()).setHorizontalAlignment(HorizontalAlignment.LEFT).setPattern("#,##0.00");
						if (isExportAsExcel()) {
							inMktPercCompColumn.setWidth(Units.inch(1.32));
						} else {
							inMktPercCompColumn.setWidth(Units.inch(1.5));
						}
						report.addColumn(inMktPercCompColumn);
					} else {
						if(!firstColumnOverriden) {
							addSummaryField(getSummaryLevel(), report);
							firstColumnOverriden = true;
						}
					}
				}
				if (key.equals("Production__c")) {
					if(!isShowSummaryHeaders() || firstColumnOverriden) {
						TextColumnBuilder<BigDecimal> productionColumn = col.column(
								getFlightLineColumnLabelHashMap().get("Production__c"), "Production__c",
								currencyWithFractionDecimalType).setHorizontalAlignment(HorizontalAlignment.RIGHT);
						if (isExportAsExcel()) {
							productionColumn.setWidth(Units.inch(1.32));
						} else {
							productionColumn.setWidth(Units.inch(1.5));
						}
						report.addColumn(productionColumn);
					} else {
						if(!firstColumnOverriden) {
							addSummaryField(getSummaryLevel(), report);
							firstColumnOverriden = true;
						}
					}
				}
				if (key.equals("Additional_Cost__c")) {
					if(!isShowSummaryHeaders() || firstColumnOverriden) {
						TextColumnBuilder<BigDecimal> additionalCostColumn = col.column(
								getFlightLineColumnLabelHashMap().get("Additional_Cost__c"), "Additional_Cost__c",
								currencyWithFractionDecimalType).setHorizontalAlignment(HorizontalAlignment.RIGHT);
						if (isExportAsExcel()) {
							additionalCostColumn.setWidth(Units.inch(1.32));
						} else {
							additionalCostColumn.setWidth(Units.inch(1.5));
						}
						report.addColumn(additionalCostColumn);
					} else {
						if(!firstColumnOverriden) {
							addSummaryField(getSummaryLevel(), report);
							firstColumnOverriden = true;
						}
					}
				}
				if (key.equals("Tax_Amt__c")) {
					if(!isShowSummaryHeaders() || firstColumnOverriden) {
						TextColumnBuilder<BigDecimal> taxAmtColumn = col.column(
								getFlightLineColumnLabelHashMap().get("Tax_Amt__c"), "Tax_Amt__c",
								currencyWithFractionDecimalType).setHorizontalAlignment(HorizontalAlignment.RIGHT);
						if (isExportAsExcel()) {
							taxAmtColumn.setWidth(Units.inch(1.32));
						} else {
							taxAmtColumn.setWidth(Units.inch(1.5));
						}
						report.addColumn(taxAmtColumn);
					} else {
						if(!firstColumnOverriden) {
							addSummaryField(getSummaryLevel(), report);
							firstColumnOverriden = true;
						}
					}
				}
				if (key.equals("Discount__c")) {
					TextColumnBuilder<Double> discountColumn = col.column(
							getFlightLineColumnLabelHashMap().get("Discount__c"),
							getSummaryFieldNameRelatedToFlightLineFieldName(getSummaryLevel(), "Discount__c"),
							type.percentageType()).setHorizontalAlignment(HorizontalAlignment.RIGHT).setPattern("##0.0");
					if (isExportAsExcel()) {
						discountColumn.setWidth(Units.inch(1.32));
					} else {
						discountColumn.setWidth(Units.inch(1.5));
					}
					report.addColumn(discountColumn);
				}
			}
			if (isExportAsExcel()) {
				if (fieldMap.containsKey("X4_Wk_Base_Rate__c")) {
					if(!isShowSummaryHeaders() || firstColumnOverriden) {
						TextColumnBuilder<BigDecimal> x4WkBaseRateColumn = col.column(
								this.isShowSummaryHeaders() ? "" : getFlightLineColumnLabelHashMap().get("X4_Wk_Base_Rate__c"),
								this.isShowSummaryHeaders() ? "OB_Summ_Num__c" : "X4_Wk_Base_Rate__c", currencyWithoutFractionDecimalType)
								.setHorizontalAlignment(HorizontalAlignment.RIGHT).setWidth(Units.inch(1.32));
						report.addColumn(x4WkBaseRateColumn);
					} else {
						if(!firstColumnOverriden) {
							addSummaryField(getSummaryLevel(), report);
							firstColumnOverriden = true;
						}
					}
				}
				if (fieldMap.containsKey("X4_Wk_Floor__c")) {
					if(!isShowSummaryHeaders() || firstColumnOverriden) {
						TextColumnBuilder<BigDecimal> x4WkFloorColumn = col.column(
								this.isShowSummaryHeaders() ? "" : getFlightLineColumnLabelHashMap().get("X4_Wk_Floor__c"),
								this.isShowSummaryHeaders() ? "OB_Summ_Num__c" : "X4_Wk_Floor__c", currencyWithoutFractionDecimalType)
								.setHorizontalAlignment(HorizontalAlignment.RIGHT).setWidth(Units.inch(1.32));
						report.addColumn(x4WkFloorColumn);
					} else {
						if(!firstColumnOverriden) {
							addSummaryField(getSummaryLevel(), report);
							firstColumnOverriden = true;
						}
					}
				}
			}
			// return report
			return report;
		}
	
		public PackageMarketFlightPreviousRecordExpression getPackageMarketFlightPreviousRecordExpression() {
			return packageMarketFlightPreviousRecordExpression;
		}

		public void setPackageMarketFlightPreviousRecordExpression(
				PackageMarketFlightPreviousRecordExpression packageMarketFlightPreviousRecordExpression) {
			this.packageMarketFlightPreviousRecordExpression = packageMarketFlightPreviousRecordExpression;
		}

		public StyleBuilder getColumnTitleStyle() {
			return columnTitleStyle;
		}

		public void setColumnTitleStyle(StyleBuilder columnTitleStyle) {
			this.columnTitleStyle = columnTitleStyle;
		}

		public StyleBuilder getColumnStyle() {
			return columnStyle;
		}

		public void setColumnStyle(StyleBuilder columnStyle) {
			this.columnStyle = columnStyle;
		}

		public boolean isExportAsExcel() {
			return exportAsExcel;
		}

		public void setExportAsExcel(boolean exportAsExcel) {
			this.exportAsExcel = exportAsExcel;
		}

		public boolean isShowSummaryHeaders() {
			return showSummaryHeaders;
		}

		public void setShowSummaryHeaders(boolean showSummaryHeaders) {
			this.showSummaryHeaders = showSummaryHeaders;
		}

		public SummaryLevelEnum getSummaryLevel() {
			return summaryLevel;
		}

		public void setSummaryLevel(SummaryLevelEnum summaryLevel) {
			this.summaryLevel = summaryLevel;
		}

		public void setDataSourceFileName(String dataSourceFileName) {
			this.dataSourceFileName = dataSourceFileName;
		}

		public String getDataSourceFileName() {
			return dataSourceFileName;
		}
	

	}
	private class LocationSubreportExpression extends
			AbstractSimpleExpression<JasperReportBuilder> {

		private static final long serialVersionUID = 1656551952324L;

		private PackageMarketFlightPreviousRecordExpression packageMarketFlightPreviousRecordExpression;

		private StyleBuilder columnTitleStyle;

		private StyleBuilder columnStyle;

		private boolean exportAsExcel;

		private boolean showSummaryHeaders;

		private String dataSourceFileName;

		private boolean locationMapExists;

		private SummaryLevelEnum summaryLevel;

		public LocationSubreportExpression(
				String dataSourceFileName,
				PackageMarketFlightPreviousRecordExpression packageMarketFlightPreviousRecordExpression,
				StyleBuilder columnTitleStyle, StyleBuilder columnStyle,
				boolean exportAsExcel, boolean showSummaryHeaders,
				boolean locationMapExists) {
			this(dataSourceFileName, packageMarketFlightPreviousRecordExpression, columnTitleStyle,
					columnStyle, exportAsExcel, showSummaryHeaders,
					locationMapExists, null);
		}

		public LocationSubreportExpression(
				String dataSourceFileName,
				PackageMarketFlightPreviousRecordExpression packageMarketFlightPreviousRecordExpression,
				StyleBuilder columnTitleStyle, StyleBuilder columnStyle,
				boolean exportAsExcel, boolean showSummaryHeaders,
				boolean locationMapExists, SummaryLevelEnum summaryLevel) {
			setDataSourceFileName(dataSourceFileName);
			setPackageMarketFlightPreviousRecordExpression(packageMarketFlightPreviousRecordExpression);
			setColumnTitleStyle(columnTitleStyle);
			setColumnStyle(columnStyle);
			setExportAsExcel(exportAsExcel);
			setShowSummaryHeaders(showSummaryHeaders);
			setLocationMapExists(locationMapExists);
			setSummaryLevel(summaryLevel);
		}
		
		@Override
		public JasperReportBuilder evaluate(ReportParameters reportParameters) {

			JasperReportBuilder report = report();

			// add columns
			addColumns(report, getFlightLineColumnLabelHashMap());

			// add fields
			report.addField(field("Package_Flight__r/Id", type.stringType()));
			report.addField(field("Id", type.stringType()));

			// style
			if(!isExportAsExcel()) {
				report.highlightDetailEvenRows();
			}
			report.setColumnStyle(getColumnStyle());
			report.setColumnTitleStyle(getColumnTitleStyle());
			
			// filter
			if(getSummaryLevel() == null) {
				report.setFilterExpression(new FilterByFlightIdExpression(getPackageMarketFlightPreviousRecordExpression()));
			} else if(getSummaryLevel() == SummaryLevelEnum.Market) {
				// set filter by market
				report.setFilterExpression(new FilterByMarketIdExpression(getPackageMarketFlightPreviousRecordExpression()));
			} else if(getSummaryLevel() == SummaryLevelEnum.Package) {
				// set filter by market
				report.setFilterExpression(new FilterByPackageIdExpression(getPackageMarketFlightPreviousRecordExpression()));
				if(!isExportAsExcel()) {
					createPackageCommentsAndDisclamersFooter(getDataSourceFileName(), report);
				}
			}

			// add a blank line at the end
			report.addLastPageFooter(cmp.text(""));

			return report;
		}

		private JasperReportBuilder addColumns(JasperReportBuilder report, Map<String, String> fieldMap) {

			CurrencyWithoutFractionDecimalType currencyWithoutFractionDecimalType = new CurrencyWithoutFractionDecimalType();

			CurrencyWithFractionDecimalType currencyWithFractionDecimalType = new CurrencyWithFractionDecimalType();

			// indicates (summary mode only) that first column has been overriden with Summary column
			boolean firstColumnOverriden = false;

			for(String key : fieldMap.keySet()) {
				if (isLocationMapExists()) {
					if (key.equals("MapLocation_Number__c")) {
						if(!isShowSummaryHeaders() || firstColumnOverriden) {
							try {
								TextColumnBuilder<Integer> mapLocNumberColumn = col.column(
										getFlightLineColumnLabelHashMap().get("MapLocation_Number__c"),
										new MapLocationNumberExpressionColumn(getMapPanelOrderPrefDataSourceFileName()))
										.setHorizontalAlignment(HorizontalAlignment.RIGHT);
								if (isExportAsExcel()) {
									mapLocNumberColumn.setWidth(Units.inch(1.32));
								} else {
									mapLocNumberColumn.setWidth(Units.inch(2));
								}
								report.addColumn(mapLocNumberColumn);
								report.sortBy(mapLocNumberColumn);
								report.addField(field("MapLocation_Number__c", type.stringType()));
							} catch (ParserConfigurationException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							} catch (SAXException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							} catch (ParseException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						} else {
							if(!firstColumnOverriden) {
								addSummaryField(getSummaryLevel(), report);
								firstColumnOverriden = true;
							}
						}
					}
				}
				if (isExportAsExcel()) {
					report.setIgnorePageWidth(true);
					if (key.equals("Package_Flight__r/Division__c")) {
						if(!isShowSummaryHeaders() || firstColumnOverriden) {
                            report.addColumn(col.column(
                                    this.isShowSummaryHeaders() ? "" : getFlightLineColumnLabelHashMap().get("Package_Flight__r/Division__c"),
                                    this.isShowSummaryHeaders() ? "Parent_Flight_Line__c" : "Package_Flight__r/Division__c",
                                    type.stringType()).setWidth(Units.inch(1.32)));
                        } else {
                            if(!firstColumnOverriden) {
                                addSummaryField(getSummaryLevel(), report);
                                firstColumnOverriden = true;
                            }
                        }
					}
					if (key.equals("Package_Flight__r/Market_Name__c")) {
						if(!isShowSummaryHeaders() || firstColumnOverriden) {
                            report.addColumn(col.column(
                                    this.isShowSummaryHeaders() ? "" : getFlightLineColumnLabelHashMap().get("Package_Flight__r/Market_Name__c"),
                                    this.isShowSummaryHeaders() ? "Parent_Flight_Line__c" : "Package_Flight__r/Market_Name__c",
                                    type.stringType()).setWidth(Units.inch(1.32)));
                        } else {
                            if(!firstColumnOverriden) {
                                addSummaryField(getSummaryLevel(), report);
                                firstColumnOverriden = true;
                            }
                        }
					}
					if (key.equals("Package_Flight__r/Market_Type__c")) {
						if(!isShowSummaryHeaders() || firstColumnOverriden) {
                            report.addColumn(col.column(
                                    this.isShowSummaryHeaders() ? "" : getFlightLineColumnLabelHashMap().get("Package_Flight__r/Market_Type__c"),
                                    this.isShowSummaryHeaders() ? "Parent_Flight_Line__c" : "Package_Flight__r/Market_Type__c",
                                    type.stringType()).setWidth(Units.inch(1.32)));
                        } else {
                            if(!firstColumnOverriden) {
                                addSummaryField(getSummaryLevel(), report);
                                firstColumnOverriden = true;
                            }
                        }
					}
					if (key.equals("Package_Flight__r/Package_Name__c")) {
						if(!isShowSummaryHeaders() || firstColumnOverriden) {
                            report.addColumn(col.column(
                                    this.isShowSummaryHeaders() ? "" : getFlightLineColumnLabelHashMap().get("Package_Flight__r/Package_Name__c"),
                                    this.isShowSummaryHeaders() ? "Parent_Flight_Line__c" : "Package_Flight__r/Package_Name__c",
                                    type.stringType()).setWidth(Units.inch(1.32)));
                        } else {
                            if(!firstColumnOverriden) {
                                addSummaryField(getSummaryLevel(), report);
                                firstColumnOverriden = true;
                            }
                        }
					}
					if (key.equals("Package_Flight__r/Name")) {
						if(!isShowSummaryHeaders() || firstColumnOverriden) {
                            report.addColumn(col.column(
                                    this.isShowSummaryHeaders() ? "" : getFlightLineColumnLabelHashMap().get("Package_Flight__r/Name"),
                                    this.isShowSummaryHeaders() ? "Parent_Flight_Line__c" : "Package_Flight__r/Name",
                                    type.stringType()).setWidth(Units.inch(1.32)));
                        } else {
                            if(!firstColumnOverriden) {
                                addSummaryField(getSummaryLevel(), report);
                                firstColumnOverriden = true;
                            }
                        }
					}
					if (key.equals("Package_Flight__r/Type__c")) {
						if(!isShowSummaryHeaders() || firstColumnOverriden) {
                            report.addColumn(col.column(
                                    this.isShowSummaryHeaders() ? "" : getFlightLineColumnLabelHashMap().get("Package_Flight__r/Type__c"),
                                    this.isShowSummaryHeaders() ? "Parent_Flight_Line__c" : "Package_Flight__r/Type__c",
                                    type.stringType()).setWidth(Units.inch(1.32)));
                        } else {
                            if(!firstColumnOverriden) {
                                addSummaryField(getSummaryLevel(), report);
                                firstColumnOverriden = true;
                            }
                        }
					}
					if (key.equals("Package_Flight__r/Campaign_Start_Date__c")) {
						if(!isShowSummaryHeaders() || firstColumnOverriden) {
                            report.addColumn(col.column(
                                    this.isShowSummaryHeaders() ? "" : getFlightLineColumnLabelHashMap().get("Package_Flight__r/Campaign_Start_Date__c"),
                                    this.isShowSummaryHeaders() ? "Parent_Flight_Line__c" : "Package_Flight__r/Campaign_Start_Date__c",
                                    type.stringType()).setHorizontalAlignment(HorizontalAlignment.CENTER).setWidth(Units.inch(1.32)).setValueFormatter(new ValueFormatter()));
                        } else {
                            if(!firstColumnOverriden) {
                                addSummaryField(getSummaryLevel(), report);
                                firstColumnOverriden = true;
                            }
                        }
					}
					if (key.equals("Package_Flight__r/Campaign_End_Date__c")) {
						if(!isShowSummaryHeaders() || firstColumnOverriden) {
                            report.addColumn(col.column(
                                    this.isShowSummaryHeaders() ? "" : getFlightLineColumnLabelHashMap().get("Package_Flight__r/Campaign_End_Date__c"),
                                    this.isShowSummaryHeaders() ? "Parent_Flight_Line__c" : "Package_Flight__r/Campaign_End_Date__c",
                                    type.stringType()).setHorizontalAlignment(HorizontalAlignment.CENTER).setWidth(Units.inch(1.32)).setValueFormatter(new ValueFormatter()));
                        } else {
                            if(!firstColumnOverriden) {
                                addSummaryField(getSummaryLevel(), report);
                                firstColumnOverriden = true;
                            }
                        }
					}
					if (key.equals("Package_Flight__r/Duration_And_Type__c")) {
						if(!isShowSummaryHeaders() || firstColumnOverriden) {
                            report.addColumn(col.column(
                                    this.isShowSummaryHeaders() ? "" : getFlightLineColumnLabelHashMap().get("Package_Flight__r/Duration_And_Type__c"),
                                    this.isShowSummaryHeaders() ? "Parent_Flight_Line__c" : "Package_Flight__r/Duration_And_Type__c",
                                    type.stringType()).setWidth(Units.inch(1.32)));
                        } else {
                            if(!firstColumnOverriden) {
                                addSummaryField(getSummaryLevel(), report);
                                firstColumnOverriden = true;
                            }
                        }
					}
					if (key.equals("Package_Flight__r/Target__c")) {
						if(!isShowSummaryHeaders() || firstColumnOverriden) {
                            report.addColumn(col.column(
                                    this.isShowSummaryHeaders() ? "" : getFlightLineColumnLabelHashMap().get("Package_Flight__r/Target__c"),
                                    this.isShowSummaryHeaders() ? "Parent_Flight_Line__c" : "Package_Flight__r/Target__c",
                                    type.stringType()).setHorizontalAlignment(HorizontalAlignment.CENTER).setWidth(Units.inch(1.32)));
                        } else {
                            if(!firstColumnOverriden) {
                                addSummaryField(getSummaryLevel(), report);
                                firstColumnOverriden = true;
                            }
                        }
					}
					if(key.equals("Package_Flight__r/Target_Population__c")) {
						if(!isShowSummaryHeaders() || firstColumnOverriden) {
                            report.addColumn(col.column(
                                    this.isShowSummaryHeaders() ? "" : getFlightLineColumnLabelHashMap().get("Package_Flight__r/Target_Population__c"),
                                    this.isShowSummaryHeaders() ? "OB_Summ_Num__c" : "Package_Flight__r/Target_Population__c",
                                    type.bigDecimalType()).setWidth(Units.inch(1.32)));
                        } else {
                            if(!firstColumnOverriden) {
                                addSummaryField(getSummaryLevel(), report);
                                firstColumnOverriden = true;
                            }
                        }
					}
					if (key.equals("Package_Flight__r/Flight_Comments__c")) {
						if(!isShowSummaryHeaders() || firstColumnOverriden) {
                            report.addColumn(col.column(
                                    this.isShowSummaryHeaders() ? "" : getFlightLineColumnLabelHashMap().get("Package_Flight__r/Flight_Comments__c"),
                                    this.isShowSummaryHeaders() ? "Parent_Flight_Line__c" : "Package_Flight__r/Flight_Comments__c",
                                    type.stringType()).setWidth(Units.inch(1.32)));
                        } else {
                            if(!firstColumnOverriden) {
                                addSummaryField(getSummaryLevel(), report);
                                firstColumnOverriden = true;
                            }
                        }
					}
					if (key.equals("Package_Flight__r/Package_Comments__c")) {
						if(!isShowSummaryHeaders() || firstColumnOverriden) {
                            report.addColumn(col.column(
                                    this.isShowSummaryHeaders() ? "" : getFlightLineColumnLabelHashMap().get("Package_Flight__r/Package_Comments__c"),
                                    this.isShowSummaryHeaders() ? "Parent_Flight_Line__c" : "Package_Flight__r/Package_Comments__c",
                                    type.stringType()).setWidth(Units.inch(1.32)));
                        } else {
                            if(!firstColumnOverriden) {
                                addSummaryField(getSummaryLevel(), report);
                                firstColumnOverriden = true;
                            }
                        }
					}
				}
				if (key.equals("Media_Category__c")) {
					if(!isShowSummaryHeaders() || firstColumnOverriden) {
                        TextColumnBuilder<String> mediaTypeColumn = col.column(
                                this.isShowSummaryHeaders() ? "" : getFlightLineColumnLabelHashMap().get("Media_Category__c"), 
                                this.isShowSummaryHeaders() ? "Parent_Flight_Line__c" : "Media_Category__c",
                                type.stringType());
                        if (isExportAsExcel()) {
                            mediaTypeColumn.setWidth(Units.inch(1.32));
                        } else {
                            mediaTypeColumn.setWidth(Units.inch(2.0));
                        }
                        report.addColumn(mediaTypeColumn);
                    } else {
                        if(!firstColumnOverriden) {
                            addSummaryField(getSummaryLevel(), report);
                            firstColumnOverriden = true;
                        }
                    }
				}
				if (key.equals("Panel_Id_Label__c")) {
					if(!isShowSummaryHeaders() || firstColumnOverriden) {
                        TextColumnBuilder<String> panelIdColumn = col.column(
                                this.isShowSummaryHeaders() ? "" : getFlightLineColumnLabelHashMap().get("Panel_Id_Label__c"),
                                this.isShowSummaryHeaders() ? "Parent_Flight_Line__c" : "Panel_Id_Label__c",
                                type.stringType()).setHorizontalAlignment(HorizontalAlignment.RIGHT);
                        if (isExportAsExcel()) {
                            panelIdColumn.setWidth(Units.inch(1.32));
                        } else {
                            panelIdColumn.setWidth(Units.inch(1.5));
                        }
                        report.addColumn(panelIdColumn);
                    } else {
                        if(!firstColumnOverriden) {
                            addSummaryField(getSummaryLevel(), report);
                            firstColumnOverriden = true;
                        }
                    }
				}
				if (key.equals("TAB_Id__c")) {
					if(!isShowSummaryHeaders() || firstColumnOverriden) {
                        TextColumnBuilder<String> tabIdColumn = col.column(
                                this.isShowSummaryHeaders() ? "" : getFlightLineColumnLabelHashMap().get("TAB_Id__c"),
                                this.isShowSummaryHeaders() ? "Parent_Flight_Line__c" : "TAB_Id__c",
                                type.stringType()).setHorizontalAlignment(HorizontalAlignment.RIGHT);
                        if (isExportAsExcel()) {
                            tabIdColumn.setWidth(Units.inch(1.32));
                        } else {
                            tabIdColumn.setWidth(Units.inch(1.5));
                        }
                        report.addColumn(tabIdColumn);
                    } else {
                        if(!firstColumnOverriden) {
                            addSummaryField(getSummaryLevel(), report);
                            firstColumnOverriden = true;
                        }
                    }
				}
				if (key.equals("Location_Description__c")) {
					if(!isShowSummaryHeaders() || firstColumnOverriden) {
                        TextColumnBuilder<String> descriptionTextColumn = col.column(
                                this.isShowSummaryHeaders() ? "" : getFlightLineColumnLabelHashMap().get("Location_Description__c"),
                                this.isShowSummaryHeaders() ? "Parent_Flight_Line__c" : "Location_Description__c",
                                type.stringType()).setHorizontalAlignment(HorizontalAlignment.LEFT);
                        if (isExportAsExcel()) {
                            descriptionTextColumn.setWidth(Units.inch(1.32));
                        } else {
                            descriptionTextColumn.setWidth(Units.inch(3.5));
                        }
                        report.addColumn(descriptionTextColumn);
                    } else {
                        if(!firstColumnOverriden) {
                            addSummaryField(getSummaryLevel(), report);
                            firstColumnOverriden = true;
                        }
                    }
				}
				if (key.equals("Face_Direction__c")) {
					if(!isShowSummaryHeaders() || firstColumnOverriden) {
                        TextColumnBuilder<String> flightLineTextColumn = col.column(
                                this.isShowSummaryHeaders() ? "" : getFlightLineColumnLabelHashMap().get("Face_Direction__c"),
                                this.isShowSummaryHeaders() ? "Parent_Flight_Line__c" : "Face_Direction__c",
                                type.stringType()).setHorizontalAlignment(HorizontalAlignment.LEFT);
                        report.addColumn(flightLineTextColumn);
                        if (isExportAsExcel()) {
                            flightLineTextColumn.setWidth(Units.inch(1.32));
                        }
                    } else {
                        if(!firstColumnOverriden) {
                            addSummaryField(getSummaryLevel(), report);
                            firstColumnOverriden = true;
                        }
                    }
				}
				if (key.equals("Weekly_Total_18_Imps__c")) {
					TextColumnBuilder<BigDecimal> weeklyTotal18ImpsColumn = col.column(
							getFlightLineColumnLabelHashMap().get("Weekly_Total_18_Imps__c"),
							getSummaryFieldNameRelatedToFlightLineFieldName(getSummaryLevel(), "Weekly_Total_18_Imps__c"),
							type.bigDecimalType()).setHorizontalAlignment(HorizontalAlignment.RIGHT).setPattern("#,###");
					if (isExportAsExcel()) {
						weeklyTotal18ImpsColumn.setWidth(Units.inch(1.32));
					} else {
						weeklyTotal18ImpsColumn.setWidth(Units.inch(3));
					}
					report.addColumn(weeklyTotal18ImpsColumn);
				}
				if (key.equals("Weekly_Total_18_Imps_000__c")) {
					if(!isShowSummaryHeaders() || firstColumnOverriden) {
                        TextColumnBuilder<BigDecimal> weeklyTotal18Imps000Column = col.column(
                                getFlightLineColumnLabelHashMap().get("Weekly_Total_18_Imps_000__c"),	"Weekly_Total_18_Imps_000__c",
                                type.bigDecimalType()).setHorizontalAlignment(HorizontalAlignment.RIGHT).setPattern("#,###");
                        if (isExportAsExcel()) {
                            weeklyTotal18Imps000Column.setWidth(Units.inch(1.32));
                        } else {
                            weeklyTotal18Imps000Column.setWidth(Units.inch(3));
                        }
                        report.addColumn(weeklyTotal18Imps000Column);
                    } else {
                        if(!firstColumnOverriden) {
                            addSummaryField(getSummaryLevel(), report);
                            firstColumnOverriden = true;
                        }
                    }
				}
				if (key.equals("In_Mkt_Imps__c")) {
					TextColumnBuilder<BigDecimal> targetInMarketImpsColumn = col.column(
							getFlightLineColumnLabelHashMap().get("In_Mkt_Imps__c"),
							getSummaryFieldNameRelatedToFlightLineFieldName(getSummaryLevel(), "In_Mkt_Imps__c"),
							type.bigDecimalType()).setHorizontalAlignment(HorizontalAlignment.RIGHT).setPattern("#,###");
					if (isExportAsExcel()) {
						targetInMarketImpsColumn.setWidth(Units.inch(1.32));
					} else {
						targetInMarketImpsColumn.setWidth(Units.inch(3));
					}
					report.addColumn(targetInMarketImpsColumn);
				}
				if (key.equals("Target_In_Market_Imps_000__c")) {
					if(!isShowSummaryHeaders() || firstColumnOverriden) {
                        TextColumnBuilder<BigDecimal> targetInMarketImps000Column = col.column(
                                getFlightLineColumnLabelHashMap().get("Target_In_Market_Imps_000__c"), "Target_In_Market_Imps_000__c",
                                type.bigDecimalType()).setHorizontalAlignment(HorizontalAlignment.RIGHT).setPattern("#,###");
                        if (isExportAsExcel()) {
                            targetInMarketImps000Column.setWidth(Units.inch(1.32));
                        } else {
                            targetInMarketImps000Column.setWidth(Units.inch(3));
                        }
                        report.addColumn(targetInMarketImps000Column);
                    } else {
                        if(!firstColumnOverriden) {
                            addSummaryField(getSummaryLevel(), report);
                            firstColumnOverriden = true;
                        }
                    }
				}
				if (key.equals("Total_Imps__c")) {
					TextColumnBuilder<BigDecimal> totalImpsColumn = col.column(
							getFlightLineColumnLabelHashMap().get("Total_Imps__c"),
							getSummaryFieldNameRelatedToFlightLineFieldName(getSummaryLevel(), "Total_Imps__c"),
							type.bigDecimalType()).setHorizontalAlignment(HorizontalAlignment.RIGHT).setPattern("#,###");
					if (isExportAsExcel()) {
						totalImpsColumn.setWidth(Units.inch(1.32));
					} else {
						totalImpsColumn.setWidth(Units.inch(3));
					}
					report.addColumn(totalImpsColumn);
				}
				if (key.equals("Target_Total_Imps_000__c")) {
					if(!isShowSummaryHeaders() || firstColumnOverriden) {
                        TextColumnBuilder<BigDecimal> totalImps000Column = col.column(
                                getFlightLineColumnLabelHashMap().get("Target_Total_Imps_000__c"), "Target_Total_Imps_000__c",
                                type.bigDecimalType()).setHorizontalAlignment(HorizontalAlignment.RIGHT).setPattern("#,###");
                        if (isExportAsExcel()) {
                            totalImps000Column.setWidth(Units.inch(1.32));
                        } else {
                            totalImps000Column.setWidth(Units.inch(3));
                        }
                        report.addColumn(totalImps000Column);
                    } else {
                        if(!firstColumnOverriden) {
                            addSummaryField(getSummaryLevel(), report);
                            firstColumnOverriden = true;
                        }
                    }
				}
				if (key.equals("WeeklyMarketImps__c")) {
					if(!isShowSummaryHeaders() || firstColumnOverriden) {
                        TextColumnBuilder<BigDecimal> weeklyTotalTargetImpsColumn = col.column(
                                getFlightLineColumnLabelHashMap().get("WeeklyMarketImps__c"), "WeeklyMarketImps__c",
                                type.bigDecimalType()).setHorizontalAlignment(HorizontalAlignment.RIGHT).setPattern("#,###");
                        if (isExportAsExcel()) {
                            weeklyTotalTargetImpsColumn.setWidth(Units.inch(1.32));
                        } else {
                            weeklyTotalTargetImpsColumn.setWidth(Units.inch(3));
                        }
                        report.addColumn(weeklyTotalTargetImpsColumn);
                    } else {
                        if(!firstColumnOverriden) {
                            addSummaryField(getSummaryLevel(), report);
                            firstColumnOverriden = true;
                        }
                    }
				}
				if (key.equals("Weekly_Total_Target_Imps_000__c")) {
					if(!isShowSummaryHeaders() || firstColumnOverriden) {
                        TextColumnBuilder<BigDecimal> weeklyTotalTargetImps000Column = col.column(
                                getFlightLineColumnLabelHashMap().get("Weekly_Total_Target_Imps_000__c"), "Weekly_Total_Target_Imps_000__c",
                                type.bigDecimalType()).setHorizontalAlignment(HorizontalAlignment.RIGHT).setPattern("#,###");
                        if (isExportAsExcel()) {
                            weeklyTotalTargetImps000Column.setWidth(Units.inch(1.32));
                        } else {
                            weeklyTotalTargetImps000Column.setWidth(Units.inch(3));
                        }
                        report.addColumn(weeklyTotalTargetImps000Column);
                    } else {
                        if(!firstColumnOverriden) {
                            addSummaryField(getSummaryLevel(), report);
                            firstColumnOverriden = true;
                        }
                    }
				}
				if (key.equals("WeeklyInMarketImps__c")) {
					if(!isShowSummaryHeaders() || firstColumnOverriden) {
                        TextColumnBuilder<BigDecimal> weeklyInMarketTargetImpsColumn = col.column(
                                getFlightLineColumnLabelHashMap().get("WeeklyInMarketImps__c"), "WeeklyInMarketImps__c",
                                type.bigDecimalType()).setHorizontalAlignment(HorizontalAlignment.RIGHT).setPattern("#,###");
                        if (isExportAsExcel()) {
                            weeklyInMarketTargetImpsColumn.setWidth(Units.inch(1.32));
                        } else {
                            weeklyInMarketTargetImpsColumn.setWidth(Units.inch(3));
                        }
                        report.addColumn(weeklyInMarketTargetImpsColumn);
                    } else {
                        if(!firstColumnOverriden) {
                            addSummaryField(getSummaryLevel(), report);
                            firstColumnOverriden = true;
                        }
                    }
				}
				if (key.equals("Weekly_In_Market_Target_Imps_000__c")) {
					if(!isShowSummaryHeaders() || firstColumnOverriden) {
                        TextColumnBuilder<BigDecimal> weeklyInMarketTargetImps000Column = col.column(
                                getFlightLineColumnLabelHashMap().get("Weekly_In_Market_Target_Imps_000__c"), "Weekly_In_Market_Target_Imps_000__c",
                                type.bigDecimalType()).setHorizontalAlignment(HorizontalAlignment.RIGHT).setPattern("#,###");
                        if (isExportAsExcel()) {
                            weeklyInMarketTargetImps000Column.setWidth(Units.inch(1.32));
                        } else {
                            weeklyInMarketTargetImps000Column.setWidth(Units.inch(3));
                        }
                        report.addColumn(weeklyInMarketTargetImps000Column);
                    } else {
                        if(!firstColumnOverriden) {
                            addSummaryField(getSummaryLevel(), report);
                            firstColumnOverriden = true;
                        }
                    }
				}
				if (key.equals("In_Mkt_TRP__c")) {
					TextColumnBuilder<Double> weekklyTRPColumn = col.column(
							getFlightLineColumnLabelHashMap().get("In_Mkt_TRP__c"),
							getSummaryFieldNameRelatedToFlightLineFieldName(getSummaryLevel(), "In_Mkt_TRP__c"),
							type.doubleType()).setHorizontalAlignment(HorizontalAlignment.RIGHT).setPattern("##0.0");
					if (isExportAsExcel()) {
						weekklyTRPColumn.setWidth(Units.inch(1.32));
					} else {
						weekklyTRPColumn.setWidth(Units.inch(3));
					}
					report.addColumn(weekklyTRPColumn);
				}
				if (key.equals("PlanTRP__c")) {
					TextColumnBuilder<Double> planTRPColumn = col.column(
							getFlightLineColumnLabelHashMap().get("PlanTRP__c"),
							getSummaryFieldNameRelatedToFlightLineFieldName(getSummaryLevel(), "PlanTRP__c"),
							type.doubleType()).setHorizontalAlignment(HorizontalAlignment.RIGHT).setPattern("##0.0");
					if (isExportAsExcel()) {
						planTRPColumn.setWidth(Units.inch(1.32));
					} else {
						planTRPColumn.setWidth(Units.inch(2));
					}
					report.addColumn(planTRPColumn);
				}
				if (key.equals("Plan_Imps_Reach_Perc__c")) {
					if(!isShowSummaryHeaders() || firstColumnOverriden) {
                        TextColumnBuilder<Double> planImpsReachPercColumn = col.column(
                                getFlightLineColumnLabelHashMap().get("Plan_Imps_Reach_Perc__c"), "Plan_Imps_Reach_Perc__c",
                                type.percentageType()).setHorizontalAlignment(HorizontalAlignment.RIGHT).setPattern("##0.0");
                        if (isExportAsExcel()) {
                            planImpsReachPercColumn.setWidth(Units.inch(1.32));
                        } else {
                            planImpsReachPercColumn.setWidth(Units.inch(1.5));
                        }
                        report.addColumn(planImpsReachPercColumn);
                    } else {
                        if(!firstColumnOverriden) {
                            addSummaryField(getSummaryLevel(), report);
                            firstColumnOverriden = true;
                        }
                    }
				}
				if (key.equals("Plan_Imps_Avg_Frequency__c")) {
					if(!isShowSummaryHeaders() || firstColumnOverriden) {
                        TextColumnBuilder<Double> frequencyColumn = col.column(
                                getFlightLineColumnLabelHashMap().get("Plan_Imps_Avg_Frequency__c"),	"Plan_Imps_Avg_Frequency__c",
                                type.doubleType()).setHorizontalAlignment(HorizontalAlignment.RIGHT).setPattern("##0.0");
                        if (isExportAsExcel()) {
                            frequencyColumn.setWidth(Units.inch(1.32));
                        }
                        report.addColumn(frequencyColumn);
                    } else {
                        if(!firstColumnOverriden) {
                            addSummaryField(getSummaryLevel(), report);
                            firstColumnOverriden = true;
                        }
                    }
				}
				if (key.equals("X4_Wk_Proposed_Price__c")) {
					if(!isShowSummaryHeaders() || firstColumnOverriden) {
                        TextColumnBuilder<BigDecimal> X4WkProposedPriceColumn = col.column(
                                this.isShowSummaryHeaders() ? "" : getFlightLineColumnLabelHashMap().get("X4_Wk_Proposed_Price__c"),
                                this.isShowSummaryHeaders() ? "OB_Summ_Num__c" : "X4_Wk_Proposed_Price__c",
                                currencyWithoutFractionDecimalType).setHorizontalAlignment(HorizontalAlignment.RIGHT);
                        if (isExportAsExcel()) {
                            X4WkProposedPriceColumn.setWidth(Units.inch(1.32));
                        } else {
                            X4WkProposedPriceColumn.setWidth(Units.inch(2.5));
                        }
                        report.addColumn(X4WkProposedPriceColumn);
                    } else {
                        if(!firstColumnOverriden) {
                            addSummaryField(getSummaryLevel(), report);
                            firstColumnOverriden = true;
                        }
                    }
				}
				if (key.equals("Net_Amount_Value__c")) {
					if(!isShowSummaryHeaders() || firstColumnOverriden) {
                        TextColumnBuilder<BigDecimal> subTotalPriceColumn = col.column(
                                getFlightLineColumnLabelHashMap().get("Net_Amount_Value__c"), "Net_Amount_Value__c",
                                currencyWithoutFractionDecimalType).setHorizontalAlignment(HorizontalAlignment.RIGHT);
                        if (isExportAsExcel()) {
                            subTotalPriceColumn.setWidth(Units.inch(1.32));
                        } else {
                            subTotalPriceColumn.setWidth(Units.inch(2.5));
                        }
                        report.addColumn(subTotalPriceColumn);
                    } else {
                        if(!firstColumnOverriden) {
                            addSummaryField(getSummaryLevel(), report);
                            firstColumnOverriden = true;
                        }
                    }
				}
				if (key.equals("Total_Price_0d__c")) {
					TextColumnBuilder<BigDecimal> totalPriceColumn = col.column(
							getFlightLineColumnLabelHashMap().get("Total_Price_0d__c"),
							getSummaryFieldNameRelatedToFlightLineFieldName(getSummaryLevel(), "Total_Price_0d__c"),
							currencyWithoutFractionDecimalType).setHorizontalAlignment(HorizontalAlignment.RIGHT);
					if (isExportAsExcel()) {
						totalPriceColumn.setWidth(Units.inch(1.32));
					} else {
						totalPriceColumn.setWidth(Units.inch(2.5));
					}
					report.addColumn(totalPriceColumn);
				}
				if (key.equals("TotalInMarketCPM_0d__c")) {
					TextColumnBuilder<BigDecimal> totalInMarketCPM0dColumn = col.column(
							getFlightLineColumnLabelHashMap().get("TotalInMarketCPM_0d__c"),
							getSummaryFieldNameRelatedToFlightLineFieldName(getSummaryLevel(), "TotalInMarketCPM_0d__c"),
							currencyWithFractionDecimalType).setHorizontalAlignment(HorizontalAlignment.RIGHT);
					if (isExportAsExcel()) {
						totalInMarketCPM0dColumn.setWidth(Units.inch(1.32));
					} else {
						totalInMarketCPM0dColumn.setWidth(Units.inch(2.5));
					}
					report.addColumn(totalInMarketCPM0dColumn);
				}
				if (key.equals("CPP_0d__c")) {
					TextColumnBuilder<BigDecimal> cppColumn = col.column(
							getFlightLineColumnLabelHashMap().get("CPP_0d__c"),
							getSummaryFieldNameRelatedToFlightLineFieldName(getSummaryLevel(), "CPP_0d__c"),
							currencyWithoutFractionDecimalType).setHorizontalAlignment(HorizontalAlignment.RIGHT);
					if (isExportAsExcel()) {
						cppColumn.setWidth(Units.inch(1.32));
					} else {
						cppColumn.setWidth(Units.inch(2.5));
					}
					report.addColumn(cppColumn);
				}
				if (key.equals("Unit_Size__c")) {
					if(!isShowSummaryHeaders() || firstColumnOverriden) {
                        TextColumnBuilder<String> unitSizeColumn = col.column(
                                this.isShowSummaryHeaders() ? "" : getFlightLineColumnLabelHashMap().get("Unit_Size__c"),
                                this.isShowSummaryHeaders() ? "Parent_Flight_Line__c" : "Unit_Size__c",
                                type.stringType()).setHorizontalAlignment(HorizontalAlignment.RIGHT);
                        if (isExportAsExcel()) {
                            unitSizeColumn.setWidth(Units.inch(1.32));
                        } else {
                            unitSizeColumn.setWidth(Units.inch(1.5));
                        }
                        report.addColumn(unitSizeColumn);
                    } else {
                        if(!firstColumnOverriden) {
                            addSummaryField(getSummaryLevel(), report);
                            firstColumnOverriden = true;
                        }
                    }
				}
				if (key.equals("Illumination_yn__c")) {
					if(!isShowSummaryHeaders() || firstColumnOverriden) {
                        TextColumnBuilder<String> illuminationColumn = col.column(
                                this.isShowSummaryHeaders() ? "" : getFlightLineColumnLabelHashMap().get("Illumination_yn__c"),
                                this.isShowSummaryHeaders() ? "Parent_Flight_Line__c" : "Illumination_yn__c",
                                type.stringType()).setHorizontalAlignment(HorizontalAlignment.LEFT);
                        if (isExportAsExcel()) {
                            illuminationColumn.setWidth(Units.inch(1.32));
                        } else {
                            illuminationColumn.setWidth(Units.inch(1));
                        }
                        report.addColumn(illuminationColumn);
                    } else {
                        if(!firstColumnOverriden) {
                            addSummaryField(getSummaryLevel(), report);
                            firstColumnOverriden = true;
                        }
                    }
				}
				if (key.equals("Comments__c")) {
					if(!isShowSummaryHeaders() || firstColumnOverriden) {
                        TextColumnBuilder<String> commentsColumn = col.column(
                                this.isShowSummaryHeaders() ? "" : getFlightLineColumnLabelHashMap().get("Comments__c"),
                                this.isShowSummaryHeaders() ? "Parent_Flight_Line__c" : "Comments__c",
                                type.stringType()).setHorizontalAlignment(HorizontalAlignment.LEFT);
                        if (isExportAsExcel()) {
                            commentsColumn.setWidth(Units.inch(1.32));
                        } else {
                            commentsColumn.setWidth(Units.inch(3.5));
                        }
                        report.addColumn(commentsColumn);
                    } else {
                        if(!firstColumnOverriden) {
                            addSummaryField(getSummaryLevel(), report);
                            firstColumnOverriden = true;
                        }
                    }
				}
				if (key.equals("Timing__c")) {
					if(!isShowSummaryHeaders() || firstColumnOverriden) {
                        TextColumnBuilder<String> timingColumn = col.column(
                                this.isShowSummaryHeaders() ? "" : getFlightLineColumnLabelHashMap().get("Timing__c"),
                                this.isShowSummaryHeaders() ? "Parent_Flight_Line__c" : "Timing__c",
                                type.stringType()).setHorizontalAlignment(HorizontalAlignment.LEFT);
                        if (isExportAsExcel()) {
                            timingColumn.setWidth(Units.inch(1.32));
                        } else {
                            timingColumn.setWidth(Units.inch(3));
                        }
                        report.addColumn(timingColumn);
                    } else {
                        if(!firstColumnOverriden) {
                            addSummaryField(getSummaryLevel(), report);
                            firstColumnOverriden = true;
                        }
                    }
				}
				if (key.equals("In_Mkt_Perc_Comp__c")) {
					if(!isShowSummaryHeaders() || firstColumnOverriden) {
                        TextColumnBuilder<BigDecimal> inMktPercCompColumn = col.column(
                                this.isShowSummaryHeaders() ? "" : getFlightLineColumnLabelHashMap().get("In_Mkt_Perc_Comp__c"),
                                this.isShowSummaryHeaders() ? "OB_Summ_Num__c" : "In_Mkt_Perc_Comp__c",
                                type.bigDecimalType()).setHorizontalAlignment(HorizontalAlignment.LEFT).setPattern("#,##0.00");
                        if (isExportAsExcel()) {
                            inMktPercCompColumn.setWidth(Units.inch(1.32));
                        } else {
                            inMktPercCompColumn.setWidth(Units.inch(1.5));
                        }
                        report.addColumn(inMktPercCompColumn);
                    } else {
                        if(!firstColumnOverriden) {
                            addSummaryField(getSummaryLevel(), report);
                            firstColumnOverriden = true;
                        }
                    }
				}
				if (key.equals("X4_Wk_Avg_Rate_per_Panel__c")) {
					if(!isShowSummaryHeaders() || firstColumnOverriden) {
                        TextColumnBuilder<String> x4WkAverageRatePanelColumn = col.column(
                                this.isShowSummaryHeaders() ? "" : getFlightLineColumnLabelHashMap().get("X4_Wk_Avg_Rate_per_Panel__c"),
                                this.isShowSummaryHeaders() ? "Parent_Flight_Line__c" : "X4_Wk_Avg_Rate_per_Panel__c",
                                type.stringType()).setHorizontalAlignment(HorizontalAlignment.RIGHT);
                        if (isExportAsExcel()) {
                            x4WkAverageRatePanelColumn.setWidth(Units.inch(1.32));
                        } else {
                            x4WkAverageRatePanelColumn.setWidth(Units.inch(1.5));
                        }
                        report.addColumn(x4WkAverageRatePanelColumn);
                    } else {
                        if(!firstColumnOverriden) {
                            addSummaryField(getSummaryLevel(), report);
                            firstColumnOverriden = true;
                        }
                    }
				}
				if (key.equals("Production__c")) {
					if(!isShowSummaryHeaders() || firstColumnOverriden) {
                        TextColumnBuilder<BigDecimal> productionColumn = col.column(
                                getFlightLineColumnLabelHashMap().get("Production__c"), "Production__c",
                                currencyWithFractionDecimalType).setHorizontalAlignment(HorizontalAlignment.RIGHT);
                        if (isExportAsExcel()) {
                            productionColumn.setWidth(Units.inch(1.32));
                        } else {
                            productionColumn.setWidth(Units.inch(1.5));
                        }
                        report.addColumn(productionColumn);
                    } else {
                        if(!firstColumnOverriden) {
                            addSummaryField(getSummaryLevel(), report);
                            firstColumnOverriden = true;
                        }
                    }
				}
				if (key.equals("Additional_Cost__c")) {
					if(!isShowSummaryHeaders() || firstColumnOverriden) {
                        TextColumnBuilder<BigDecimal> additionalCostColumn = col.column(
                                getFlightLineColumnLabelHashMap().get("Additional_Cost__c"), "Additional_Cost__c",
                                currencyWithFractionDecimalType).setHorizontalAlignment(HorizontalAlignment.RIGHT);
                        if (isExportAsExcel()) {
                            additionalCostColumn.setWidth(Units.inch(1.32));
                        } else {
                            additionalCostColumn.setWidth(Units.inch(1.5));
                        }
                        report.addColumn(additionalCostColumn);
                    } else {
                        if(!firstColumnOverriden) {
                            addSummaryField(getSummaryLevel(), report);
                            firstColumnOverriden = true;
                        }
                    }
				}
				if (key.equals("Tax_Amt__c")) {
					if(!isShowSummaryHeaders() || firstColumnOverriden) {
                        TextColumnBuilder<BigDecimal> taxAmtColumn = col.column(
                                getFlightLineColumnLabelHashMap().get("Tax_Amt__c"), "Tax_Amt__c",
                                currencyWithFractionDecimalType).setHorizontalAlignment(HorizontalAlignment.RIGHT);
                        if (isExportAsExcel()) {
                            taxAmtColumn.setWidth(Units.inch(1.32));
                        } else {
                            taxAmtColumn.setWidth(Units.inch(1.5));
                        }
                        report.addColumn(taxAmtColumn);
                    } else {
                        if(!firstColumnOverriden) {
                            addSummaryField(getSummaryLevel(), report);
                            firstColumnOverriden = true;
                        }
                    }
				}
				if (key.equals("Location__Longitude__s")) {
					if(!isShowSummaryHeaders() || firstColumnOverriden) {
                        TextColumnBuilder<String> locationLangitudeColumn = col.column(
                                this.isShowSummaryHeaders() ? "" : getFlightLineColumnLabelHashMap().get("Location__Longitude__s"),
                                this.isShowSummaryHeaders() ? "Parent_Flight_Line__c" : "Location__Longitude__s",
                                type.stringType()).setHorizontalAlignment(HorizontalAlignment.LEFT);
                        if (isExportAsExcel()) {
                            locationLangitudeColumn.setWidth(Units.inch(1.32));
                        } else {
                            locationLangitudeColumn.setWidth(Units.inch(1));
                        }
                        report.addColumn(locationLangitudeColumn);
                    } else {
                        if(!firstColumnOverriden) {
                            addSummaryField(getSummaryLevel(), report);
                            firstColumnOverriden = true;
                        }
                    }
				}
				if (key.equals("Location__Latitude__s")) {
					if(!isShowSummaryHeaders() || firstColumnOverriden) {
                        TextColumnBuilder<String> locationLatitudeColumn = col.column(
                                this.isShowSummaryHeaders() ? "" : getFlightLineColumnLabelHashMap().get("Location__Latitude__s"),
                                this.isShowSummaryHeaders() ? "Parent_Flight_Line__c" : "Location__Latitude__s",
                                type.stringType()).setHorizontalAlignment(HorizontalAlignment.LEFT);
                        if (isExportAsExcel()) {
                            locationLatitudeColumn.setWidth(Units.inch(1.32));
                        } else {
                            locationLatitudeColumn.setWidth(Units.inch(1));
                        }
                        report.addColumn(locationLatitudeColumn);
                    } else {
                        if(!firstColumnOverriden) {
                            addSummaryField(getSummaryLevel(), report);
                            firstColumnOverriden = true;
                        }
                    }
				}
				if (key.equals("Embellishments__c")) {
					if(!isShowSummaryHeaders() || firstColumnOverriden) {
                        TextColumnBuilder<String> embellishmentsColumn = col.column(
                                this.isShowSummaryHeaders() ? "" : getFlightLineColumnLabelHashMap().get("Embellishments__c"),
                                this.isShowSummaryHeaders() ? "Parent_Flight_Line__c" : "Embellishments__c",
                                type.stringType()).setHorizontalAlignment(HorizontalAlignment.LEFT);
                        if (isExportAsExcel()) {
                            embellishmentsColumn.setWidth(Units.inch(1.32));
                        } else {
                            embellishmentsColumn.setWidth(Units.inch(1));
                        }
                        report.addColumn(embellishmentsColumn);
                    } else {
                        if(!firstColumnOverriden) {
                            addSummaryField(getSummaryLevel(), report);
                            firstColumnOverriden = true;
                        }
                    }
				}
				if (key.equals("Illumination__c")) {
					if(!isShowSummaryHeaders() || firstColumnOverriden) {
                        TextColumnBuilder<BigDecimal> illuminationColumn = col.column(
                                this.isShowSummaryHeaders() ? "" : getFlightLineColumnLabelHashMap().get("Illumination__c"),
                                this.isShowSummaryHeaders() ? "OB_Summ_Num__c" : "Illumination__c",
                                type.bigDecimalType()).setHorizontalAlignment(HorizontalAlignment.RIGHT).setPattern("#,###");
                        if (isExportAsExcel()) {
                            illuminationColumn.setWidth(Units.inch(1.32));
                        } else {
                            illuminationColumn.setWidth(Units.inch(1));
                        }
                        report.addColumn(illuminationColumn);
                    } else {
                        if(!firstColumnOverriden) {
                            addSummaryField(getSummaryLevel(), report);
                            firstColumnOverriden = true;
                        }
                    }
				}
				if (key.equals("Current_Copy__c")) {
					if(!isShowSummaryHeaders() || firstColumnOverriden) {
                        TextColumnBuilder<String> currentCopyColumn = col.column(
                                this.isShowSummaryHeaders() ? "" : getFlightLineColumnLabelHashMap().get("Current_Copy__c"),
                                this.isShowSummaryHeaders() ? "Parent_Flight_Line__c" : "Current_Copy__c",
                                type.stringType()).setHorizontalAlignment(HorizontalAlignment.LEFT);
                        if (isExportAsExcel()) {
                            currentCopyColumn.setWidth(Units.inch(1.32));
                        } else {
                            currentCopyColumn.setWidth(Units.inch(1));
                        }
                        report.addColumn(currentCopyColumn);
                    } else {
                        if(!firstColumnOverriden) {
                            addSummaryField(getSummaryLevel(), report);
                            firstColumnOverriden = true;
                        }
                    }
				}
				if (key.equals("City__c")) {
					if(!isShowSummaryHeaders() || firstColumnOverriden) {
                        TextColumnBuilder<String> cityColumn = col.column(
                                this.isShowSummaryHeaders() ? "" : getFlightLineColumnLabelHashMap().get("City__c"),
                                this.isShowSummaryHeaders() ? "Parent_Flight_Line__c" : "City__c",
                                type.stringType()).setHorizontalAlignment(HorizontalAlignment.LEFT);
                        if (isExportAsExcel()) {
                            cityColumn.setWidth(Units.inch(1.32));
                        } else {
                            cityColumn.setWidth(Units.inch(1.5));
                        }
                        report.addColumn(cityColumn);
                    } else {
                        if(!firstColumnOverriden) {
                            addSummaryField(getSummaryLevel(), report);
                            firstColumnOverriden = true;
                        }
                    }
				}
				if (key.equals("County__c")) {
					if(!isShowSummaryHeaders() || firstColumnOverriden) {
                        TextColumnBuilder<String> countryColumn = col.column(
                                this.isShowSummaryHeaders() ? "" : getFlightLineColumnLabelHashMap().get("County__c"),
                                this.isShowSummaryHeaders() ? "Parent_Flight_Line__c" : "County__c",
                                type.stringType()).setHorizontalAlignment(HorizontalAlignment.LEFT);
                        if (isExportAsExcel()) {
                            countryColumn.setWidth(Units.inch(1.32));
                        } else {
                            countryColumn.setWidth(Units.inch(1.5));
                        }
                        report.addColumn(countryColumn);
                    } else {
                        if(!firstColumnOverriden) {
                            addSummaryField(getSummaryLevel(), report);
                            firstColumnOverriden = true;
                        }
                    }
				}
				if (key.equals("State__c")) {
					if(!isShowSummaryHeaders() || firstColumnOverriden) {
                        TextColumnBuilder<String> stateColumn = col.column(
                                this.isShowSummaryHeaders() ? "" : getFlightLineColumnLabelHashMap().get("State__c"),
                                this.isShowSummaryHeaders() ? "Parent_Flight_Line__c" : "State__c",
                                type.stringType()).setHorizontalAlignment(HorizontalAlignment.LEFT);
                        if (isExportAsExcel()) {
                            stateColumn.setWidth(Units.inch(1.32));
                        } else {
                            stateColumn.setWidth(Units.inch(1.5));
                        }
                        report.addColumn(stateColumn);
                    } else {
                        if(!firstColumnOverriden) {
                            addSummaryField(getSummaryLevel(), report);
                            firstColumnOverriden = true;
                        }
                    }
				}
				if (key.equals("Zip__c")) {
					if(!isShowSummaryHeaders() || firstColumnOverriden) {
                        TextColumnBuilder<String> zipColumn = col.column(
                                this.isShowSummaryHeaders() ? "" : getFlightLineColumnLabelHashMap().get("Zip__c"),
                                this.isShowSummaryHeaders() ? "Parent_Flight_Line__c" : "Zip__c",
                                type.stringType()).setHorizontalAlignment(HorizontalAlignment.LEFT);
                        if (isExportAsExcel()) {
                            zipColumn.setWidth(Units.inch(1.32));
                        } else {
                            zipColumn.setWidth(Units.inch(1.5));
                        }
                        report.addColumn(zipColumn);
                    } else {
                        if(!firstColumnOverriden) {
                            addSummaryField(getSummaryLevel(), report);
                            firstColumnOverriden = true;
                        }
                    }
				}
				if (key.equals("Media_Product__c")) {
					if(!isShowSummaryHeaders() || firstColumnOverriden) {
                        TextColumnBuilder<String> mediaProductColumn = col.column(
                                this.isShowSummaryHeaders() ? "" : getFlightLineColumnLabelHashMap().get("Media_Product__c"),
                                this.isShowSummaryHeaders() ? "Parent_Flight_Line__c" : "Media_Product__c",
                                type.stringType()).setHorizontalAlignment(HorizontalAlignment.LEFT);
                        if (isExportAsExcel()) {
                            mediaProductColumn.setWidth(Units.inch(1.32));
                        } else {
                            mediaProductColumn.setWidth(Units.inch(1.5));
                        }
                        report.addColumn(mediaProductColumn);;
                    } else {
                        if(!firstColumnOverriden) {
                            addSummaryField(getSummaryLevel(), report);
                            firstColumnOverriden = true;
                        }
                    }
				}
				if (key.equals("Ride_Order__c")) {
					if(!isShowSummaryHeaders() || firstColumnOverriden) {
                        TextColumnBuilder<String> rideOrderColumn = col.column(
                                this.isShowSummaryHeaders() ? "" : getFlightLineColumnLabelHashMap().get("Ride_Order__c"),
                                this.isShowSummaryHeaders() ? "Parent_Flight_Line__c" : "Ride_Order__c",
                                type.stringType()).setHorizontalAlignment(HorizontalAlignment.LEFT);
                        if (isExportAsExcel()) {
                            rideOrderColumn.setWidth(Units.inch(1.32));
                        } else {
                            rideOrderColumn.setWidth(Units.inch(1.5));
                        }
                        report.addColumn(rideOrderColumn);
                    } else {
                        if(!firstColumnOverriden) {
                            addSummaryField(getSummaryLevel(), report);
                            firstColumnOverriden = true;
                        }
                    }
				}
				if (key.equals("Facing__c")) {
					if(!isShowSummaryHeaders() || firstColumnOverriden) {
                        TextColumnBuilder<String> faceSideColumn = col.column(
                                this.isShowSummaryHeaders() ? "" : getFlightLineColumnLabelHashMap().get("Facing__c"),
                                this.isShowSummaryHeaders() ? "Parent_Flight_Line__c" : "Facing__c",
                                type.stringType()).setHorizontalAlignment(HorizontalAlignment.LEFT);
                        if (isExportAsExcel()) {
                            faceSideColumn.setWidth(Units.inch(1.32));
                        } else {
                            faceSideColumn.setWidth(Units.inch(1.5));
                        }
                        report.addColumn(faceSideColumn);
                    } else {
                        if(!firstColumnOverriden) {
                            addSummaryField(getSummaryLevel(), report);
                            firstColumnOverriden = true;
                        }
                    }
				}
				if (getPackageMarketFlightPreviousRecordExpression().isDigitalMediaCategory()) {
					if (key.equals("Average_Daily_Spots__c")) {
                        if(!isShowSummaryHeaders() || firstColumnOverriden) {
                            TextColumnBuilder<String> averageDailySpotsColumn = col.column(
                                    this.isShowSummaryHeaders() ? "" : getFlightLineColumnLabelHashMap().get("Average_Daily_Spots__c"),
                                    this.isShowSummaryHeaders() ? "Parent_Flight_Line__c" : "Average_Daily_Spots__c",
                                    type.stringType()).setHorizontalAlignment(HorizontalAlignment.RIGHT);
                            if (isExportAsExcel()) {
                                averageDailySpotsColumn.setWidth(Units.inch(1.32));
                            } else {
                                averageDailySpotsColumn.setWidth(Units.inch(1.5));
                            }
                            report.addColumn(averageDailySpotsColumn);
                        } else {
                            if(!firstColumnOverriden) {
                                addSummaryField(getSummaryLevel(), report);
                                firstColumnOverriden = true;
                            }
                        }
					}
				}
				if (key.equals("Discount__c")) {
					TextColumnBuilder<Double> discountColumn = col.column(
							getFlightLineColumnLabelHashMap().get("Discount__c"),
							getSummaryFieldNameRelatedToFlightLineFieldName(getSummaryLevel(), "Discount__c"),
							type.percentageType()).setHorizontalAlignment(HorizontalAlignment.RIGHT).setPattern("##0.0");
					if (isExportAsExcel()) {
						discountColumn.setWidth(Units.inch(1.32));
					} else {
						discountColumn.setWidth(Units.inch(1.5));
					}
					report.addColumn(discountColumn);
				}
			}
			if (isExportAsExcel()) {
				if (fieldMap.containsKey("X4_Wk_Base_Rate__c")) {
					if(!isShowSummaryHeaders() || firstColumnOverriden) {
	                    report.addColumn(col.column(
	                            this.isShowSummaryHeaders() ? "" : getFlightLineColumnLabelHashMap().get("X4_Wk_Base_Rate__c"),
	                            this.isShowSummaryHeaders() ? "OB_Summ_Num__c" : "X4_Wk_Base_Rate__c",
	                            currencyWithoutFractionDecimalType).setHorizontalAlignment(HorizontalAlignment.RIGHT).setWidth(Units.inch(1.32)));
	                } else {
	                    if(!firstColumnOverriden) {
	                        addSummaryField(getSummaryLevel(), report);
	                        firstColumnOverriden = true;
	                    }
	                }
				}
				if (fieldMap.containsKey("X4_Wk_Floor__c")) {
					if(!isShowSummaryHeaders() || firstColumnOverriden) {
	                    report.addColumn(col.column(
	                            this.isShowSummaryHeaders() ? "" : getFlightLineColumnLabelHashMap().get("X4_Wk_Floor__c"),
	                            this.isShowSummaryHeaders() ? "OB_Summ_Num__c" : "X4_Wk_Floor__c",
	                            currencyWithoutFractionDecimalType).setHorizontalAlignment(HorizontalAlignment.RIGHT).setWidth(Units.inch(1.32)));
	                } else {
	                    if(!firstColumnOverriden) {
	                        addSummaryField(getSummaryLevel(), report);
	                        firstColumnOverriden = true;
	                    }
	                }
				}
			}
			// return report
			return report;
		}
	

		public PackageMarketFlightPreviousRecordExpression getPackageMarketFlightPreviousRecordExpression() {
			return packageMarketFlightPreviousRecordExpression;
		}

		public void setPackageMarketFlightPreviousRecordExpression(
				PackageMarketFlightPreviousRecordExpression packageMarketFlightPreviousRecordExpression) {
			this.packageMarketFlightPreviousRecordExpression = packageMarketFlightPreviousRecordExpression;
		}

		public StyleBuilder getColumnTitleStyle() {
			return columnTitleStyle;
		}

		public void setColumnTitleStyle(StyleBuilder columnTitleStyle) {
			this.columnTitleStyle = columnTitleStyle;
		}

		public StyleBuilder getColumnStyle() {
			return columnStyle;
		}

		public void setColumnStyle(StyleBuilder columnStyle) {
			this.columnStyle = columnStyle;
		}

		public boolean isExportAsExcel() {
			return exportAsExcel;
		}

		public void setExportAsExcel(boolean exportAsExcel) {
			this.exportAsExcel = exportAsExcel;
		}

		public boolean isLocationMapExists() {
			return locationMapExists;
		}

		public void setLocationMapExists(boolean mapExists) {
			this.locationMapExists = mapExists;
		}
		
		public boolean isShowSummaryHeaders() {
			return showSummaryHeaders;
		}

		public void setShowSummaryHeaders(boolean showSummaryHeaders) {
			this.showSummaryHeaders = showSummaryHeaders;
		}

		public SummaryLevelEnum getSummaryLevel() {
			return summaryLevel;
		}

		public void setSummaryLevel(SummaryLevelEnum summaryLevel) {
			this.summaryLevel = summaryLevel;
		}

		public void setDataSourceFileName(String dataSourceFileName) {
			this.dataSourceFileName = dataSourceFileName;
		}

		public String getDataSourceFileName() {
			return dataSourceFileName;
		}
	}

	private class RotarySubreportExpression extends
			AbstractSimpleExpression<JasperReportBuilder> {

		private static final long serialVersionUID = 1656551952324L;

		private PackageMarketFlightPreviousRecordExpression packageMarketFlightPreviousRecordExpression;

		private StyleBuilder columnTitleStyle;

		private StyleBuilder columnStyle;

		private boolean exportAsExcel;

		private boolean showSummaryHeaders;

		private String dataSourceFileName;

		private SummaryLevelEnum summaryLevel;

		public RotarySubreportExpression(
				String dataSourceFileName,
				PackageMarketFlightPreviousRecordExpression packageMarketFlightPreviousRecordExpression,
				StyleBuilder columnTitleStyle, StyleBuilder columnStyle,
				boolean exportAsExcel, boolean showSummaryHeaders) {
			this(dataSourceFileName, packageMarketFlightPreviousRecordExpression, columnTitleStyle,
					columnStyle, exportAsExcel, showSummaryHeaders, null);
		}

		public RotarySubreportExpression(
				String dataSourceFileName,
				PackageMarketFlightPreviousRecordExpression packageMarketFlightPreviousRecordExpression,
				StyleBuilder columnTitleStyle, StyleBuilder columnStyle,
				boolean exportAsExcel, boolean showSummaryHeaders,
				SummaryLevelEnum summaryLevel) {
			setDataSourceFileName(dataSourceFileName);
			setPackageMarketFlightPreviousRecordExpression(packageMarketFlightPreviousRecordExpression);
			setColumnTitleStyle(columnTitleStyle);
			setColumnStyle(columnStyle);
			setExportAsExcel(exportAsExcel);
			setShowSummaryHeaders(showSummaryHeaders);
			setSummaryLevel(summaryLevel);
		}

		@Override
		public JasperReportBuilder evaluate(ReportParameters reportParameters) {
			JasperReportBuilder report = report();

			// add columns
			addColumns(report, getFlightLineColumnLabelHashMap());

			// add fields
			report.addField(field("Package_Flight__r/Id", type.stringType()));
			report.addField(field("Id", type.stringType()));

			// style
			report.highlightDetailEvenRows();
			report.setColumnStyle(getColumnStyle());
			report.setColumnTitleStyle(getColumnTitleStyle());

			// filter
			if(getSummaryLevel() == null) {
				report.setFilterExpression(new FilterByFlightIdExpression(getPackageMarketFlightPreviousRecordExpression()));
			} else if(getSummaryLevel() == SummaryLevelEnum.Market) {
				// set filter by market
				report.setFilterExpression(new FilterByMarketIdExpression(getPackageMarketFlightPreviousRecordExpression()));
			} else if(getSummaryLevel() == SummaryLevelEnum.Package) {
				// set filter by package
				report.setFilterExpression(new FilterByPackageIdExpression(getPackageMarketFlightPreviousRecordExpression()));
				if(!isExportAsExcel()) {
					createPackageCommentsAndDisclamersFooter(getDataSourceFileName(), report);
				}
			}

			// add a blank line at the end
			report.addLastPageFooter(cmp.text(""));

			return report;
		}
		
		private JasperReportBuilder addColumns(JasperReportBuilder report, Map<String, String> fieldMap) {

			// types
			CurrencyWithoutFractionDecimalType currencyWithoutFractionDecimalType = new CurrencyWithoutFractionDecimalType();

			// indicates (summary mode only) that first column has been overriden with Summary column
			boolean firstColumnOverriden = false;

			for(String key : fieldMap.keySet()) {
				if (isExportAsExcel()) {
					if(key.equals("Package_Flight__r/Package_Name__c")) {
                        if(!isShowSummaryHeaders() || firstColumnOverriden) {
                            report.addColumn(col.column(
                                    this.isShowSummaryHeaders() ? ""	: getFlightLineColumnLabelHashMap().get("Package_Flight__r/Package_Name__c"),
                                    "Package_Flight__r/Package_Name__c", type.stringType()));
                        } else {
                            if(!firstColumnOverriden) {
                                addSummaryField(getSummaryLevel(), report);
                                firstColumnOverriden = true;
                            }
                        }
					}
					if(key.equals("Package_Flight__r/Name")) {
                        if(!isShowSummaryHeaders() || firstColumnOverriden) {
                            report.addColumn(col.column(
                                    this.isShowSummaryHeaders() ? "" : getFlightLineColumnLabelHashMap().get("Package_Flight__r/Name"),
                                    "Package_Flight__r/Name", type.stringType()));
                        } else {
                            if(!firstColumnOverriden) {
                                addSummaryField(getSummaryLevel(), report);
                                firstColumnOverriden = true;
                            }
                        }
					}
					if(key.equals("Package_Flight__r/Division__c")) {
                        if(!isShowSummaryHeaders() || firstColumnOverriden) {
                            report.addColumn(col.column(
                                    this.isShowSummaryHeaders() ? "" : getFlightLineColumnLabelHashMap().get("Package_Flight__r/Division__c"),
                                    "Package_Flight__r/Division__c", type.stringType()));
                        } else {
                            if(!firstColumnOverriden) {
                                addSummaryField(getSummaryLevel(), report);
                                firstColumnOverriden = true;
                            }
                        }
					}
					if(key.equals("Network_Name__c")) {
                        if(!isShowSummaryHeaders() || firstColumnOverriden) {
                            report.addColumn(col.column(
                                    this.isShowSummaryHeaders() ? "" : getFlightLineColumnLabelHashMap().get("Network_Name__c"),
                                    "Network_Name__c", type.stringType()));
                        } else {
                            if(!firstColumnOverriden) {
                                addSummaryField(getSummaryLevel(), report);
                                firstColumnOverriden = true;
                            }
                        }
					}
					if(key.equals("Package_Flight__r/Target__c")) {
                        if(!isShowSummaryHeaders() || firstColumnOverriden) {
                            report.addColumn(col.column(
                                    this.isShowSummaryHeaders() ? "" : getFlightLineColumnLabelHashMap().get("Package_Flight__r/Target__c"),
                                    "Package_Flight__r/Target__c", type.stringType()).setHorizontalAlignment(HorizontalAlignment.CENTER));
                        } else {
                            if(!firstColumnOverriden) {
                                addSummaryField(getSummaryLevel(), report);
                                firstColumnOverriden = true;
                            }
                        }
					}
					if(key.equals("Package_Flight__r/Duration_And_Type__c")) {
                        if(!isShowSummaryHeaders() || firstColumnOverriden) {
                            report.addColumn(col.column(
                                    this.isShowSummaryHeaders() ? "" : getFlightLineColumnLabelHashMap().get("Package_Flight__r/Duration_And_Type__c"),
                                    "Package_Flight__r/Duration_And_Type__c", type.stringType()).setHorizontalAlignment(HorizontalAlignment.CENTER));
                        } else {
                            if(!firstColumnOverriden) {
                                addSummaryField(getSummaryLevel(), report);
                                firstColumnOverriden = true;
                            }
                        }
					}
					if(key.equals("Package_Flight__r/Campaign_Start_Date__c")) {
                        if(!isShowSummaryHeaders() || firstColumnOverriden) {
                            report.addColumn(col.column(
                                    this.isShowSummaryHeaders() ? "" : getFlightLineColumnLabelHashMap().get("Package_Flight__r/Campaign_Start_Date__c"),
                                    "Package_Flight__r/Campaign_Start_Date__c",	type.stringType()).setHorizontalAlignment(HorizontalAlignment.CENTER));
                        } else {
                            if(!firstColumnOverriden) {
                                addSummaryField(getSummaryLevel(), report);
                                firstColumnOverriden = true;
                            }
                        }
					}
					if(key.equals("Package_Flight__r/Campaign_End_Date__c")) {
                        if(!isShowSummaryHeaders() || firstColumnOverriden) {
                            report.addColumn(col.column(
                                    this.isShowSummaryHeaders() ? "" : getFlightLineColumnLabelHashMap().get("Package_Flight__r/Campaign_End_Date__c"),
                                    "Package_Flight__r/Campaign_End_Date__c", type.stringType()).setHorizontalAlignment(HorizontalAlignment.CENTER));
                        } else {
                            if(!firstColumnOverriden) {
                                addSummaryField(getSummaryLevel(), report);
                                firstColumnOverriden = true;
                            }
                        }
					}
				}
	
				// add columns
				if (!this.isExportAsExcel()) {
					if(key.equals("Network_Name__c")) {
                        if(!isShowSummaryHeaders() || firstColumnOverriden) {
                            report.addColumn(col.column(
                                    this.isShowSummaryHeaders() ? "": getFlightLineColumnLabelHashMap().get("Network_Name__c"),
                                            "Network_Name__c",	type.stringType()));
                        } else {
                            if(!firstColumnOverriden) {
                                addSummaryField(getSummaryLevel(), report);
                                firstColumnOverriden = true;
                            }
                        }
					}
				}
				if(key.equals("Number_of_Panels__c")) {
                    if(!isShowSummaryHeaders() || firstColumnOverriden) {
                        report.addColumn(col.column(
                                this.isShowSummaryHeaders() ? "" : getFlightLineColumnLabelHashMap().get("Number_of_Panels__c"),
                                "Number_of_Panels__c", type.integerType()).setHorizontalAlignment(HorizontalAlignment.CENTER));
                    } else {
                        if(!firstColumnOverriden) {
                            addSummaryField(getSummaryLevel(), report);
                            firstColumnOverriden = true;
                        }
                    }
				}
				if(key.equals("Weekly_Total_18_Imps__c")) {
					report.addColumn(col.column(
							this.isShowSummaryHeaders() ? "" : getFlightLineColumnLabelHashMap().get("Weekly_Total_18_Imps__c"),
							getSummaryFieldNameRelatedToFlightLineFieldName(getSummaryLevel(), "Weekly_Total_18_Imps__c"), type.integerType()).setHorizontalAlignment(HorizontalAlignment.CENTER));
				}
				if(key.equals("In_Mkt_Imps__c")) {
					report.addColumn(col.column(
							this.isShowSummaryHeaders() ? "" : getFlightLineColumnLabelHashMap().get("In_Mkt_Imps__c"),
							getSummaryFieldNameRelatedToFlightLineFieldName(getSummaryLevel(), "In_Mkt_Imps__c"), type.doubleType()).setHorizontalAlignment(HorizontalAlignment.CENTER));
				}
				if(key.equals("Total_Imps__c")) {
					report.addColumn(col.column(
							this.isShowSummaryHeaders() ? "" : getFlightLineColumnLabelHashMap().get("Total_Imps__c"),
							getSummaryFieldNameRelatedToFlightLineFieldName(getSummaryLevel(), "Total_Imps__c"), type.doubleType()).setHorizontalAlignment(HorizontalAlignment.CENTER));
				}
				if(key.equals("In_Mkt_TRP__c")) {
					report.addColumn(col.column(
							this.isShowSummaryHeaders() ? "" : getFlightLineColumnLabelHashMap().get("In_Mkt_TRP__c"),
							getSummaryFieldNameRelatedToFlightLineFieldName(getSummaryLevel(), "In_Mkt_TRP__c"), type.doubleType()).setHorizontalAlignment(HorizontalAlignment.CENTER));
				}
				if(key.equals("PlanTRP__c")) {
					report.addColumn(col.column(
							this.isShowSummaryHeaders() ? "" : getFlightLineColumnLabelHashMap().get("PlanTRP__c"),
							getSummaryFieldNameRelatedToFlightLineFieldName(getSummaryLevel(), "PlanTRP__c"), type.doubleType()).setHorizontalAlignment(HorizontalAlignment.CENTER));
				}
				if(key.equals("Plan_Imps_Reach_Perc__c")) {
					report.addColumn(col.column(
							this.isShowSummaryHeaders() ? "" : getFlightLineColumnLabelHashMap().get("Plan_Imps_Reach_Perc__c"),
							"Plan_Imps_Reach_Perc__c", type.percentageType()).setHorizontalAlignment(HorizontalAlignment.CENTER)
							.setPattern("##0.0"));
				}
				if(key.equals("Plan_Imps_Avg_Frequency__c")) {
                    if(!isShowSummaryHeaders() || firstColumnOverriden) {
                        report.addColumn(col.column(
                                this.isShowSummaryHeaders() ? "" : getFlightLineColumnLabelHashMap().get("Plan_Imps_Avg_Frequency__c"),
                                "Plan_Imps_Avg_Frequency__c", type.doubleType()).setHorizontalAlignment(HorizontalAlignment.CENTER));
                    } else {
                        if(!firstColumnOverriden) {
                            addSummaryField(getSummaryLevel(), report);
                            firstColumnOverriden = true;
                        }
                    }
				}
				if(key.equals("X4_Wk_Proposed_Price__c")) {
                    if(!isShowSummaryHeaders() || firstColumnOverriden) {
                        report.addColumn(col.column(
                                this.isShowSummaryHeaders() ? "" : getFlightLineColumnLabelHashMap().get("X4_Wk_Proposed_Price__c"),
                                "X4_Wk_Proposed_Price__c", currencyWithoutFractionDecimalType).setHorizontalAlignment(HorizontalAlignment.CENTER));
                    } else {
                        if(!firstColumnOverriden) {
                            addSummaryField(getSummaryLevel(), report);
                            firstColumnOverriden = true;
                        }
                    }
				}
				if(key.equals("Total_Price_0d__c")) {
					report.addColumn(col.column(
							this.isShowSummaryHeaders() ? "" : getFlightLineColumnLabelHashMap().get("Total_Price_0d__c"),
							getSummaryFieldNameRelatedToFlightLineFieldName(getSummaryLevel(), "Total_Price_0d__c"), currencyWithoutFractionDecimalType).setHorizontalAlignment(HorizontalAlignment.CENTER));
				}
				if(key.equals("TotalInMarketCPM_0d__c")) {
					report.addColumn(col.column(
							this.isShowSummaryHeaders() ? "" : getFlightLineColumnLabelHashMap().get("TotalInMarketCPM_0d__c"),
							getSummaryFieldNameRelatedToFlightLineFieldName(getSummaryLevel(), "TotalInMarketCPM_0d__c"), currencyWithoutFractionDecimalType).setHorizontalAlignment(HorizontalAlignment.CENTER));
				}
				if(key.equals("CPP_0d__c")) {
					report.addColumn(col.column(
							this.isShowSummaryHeaders() ? "" : getFlightLineColumnLabelHashMap().get("CPP_0d__c"),
							getSummaryFieldNameRelatedToFlightLineFieldName(getSummaryLevel(), "CPP_0d__c"), currencyWithoutFractionDecimalType).setHorizontalAlignment(HorizontalAlignment.CENTER));
				}
				if(key.equals("Package_Flight__r/Flight_Comments__c")) {
                    if(!isShowSummaryHeaders() || firstColumnOverriden) {
                        report.addColumn(col.column(this.isShowSummaryHeaders() ? ""
                                : getFlightLineColumnLabelHashMap().get("Package_Flight__r/Flight_Comments__c"),
                                "Package_Flight__r/Flight_Comments__c", type.stringType()));
                    } else {
                        if(!firstColumnOverriden) {
                            addSummaryField(getSummaryLevel(), report);
                            firstColumnOverriden = true;
                        }
                    }
				}
			}
			// return report
			return report;
		}

		public PackageMarketFlightPreviousRecordExpression getPackageMarketFlightPreviousRecordExpression() {
			return packageMarketFlightPreviousRecordExpression;
		}

		public void setPackageMarketFlightPreviousRecordExpression(
				PackageMarketFlightPreviousRecordExpression packageMarketFlightPreviousRecordExpression) {
			this.packageMarketFlightPreviousRecordExpression = packageMarketFlightPreviousRecordExpression;
		}

		public StyleBuilder getColumnTitleStyle() {
			return columnTitleStyle;
		}

		public void setColumnTitleStyle(StyleBuilder columnTitleStyle) {
			this.columnTitleStyle = columnTitleStyle;
		}

		public StyleBuilder getColumnStyle() {
			return columnStyle;
		}

		public void setColumnStyle(StyleBuilder columnStyle) {
			this.columnStyle = columnStyle;
		}

		public boolean isExportAsExcel() {
			return exportAsExcel;
		}

		public void setExportAsExcel(boolean exportAsExcel) {
			this.exportAsExcel = exportAsExcel;
		}

		public boolean isShowSummaryHeaders() {
			return showSummaryHeaders;
		}

		public void setShowSummaryHeaders(boolean showSummaryHeaders) {
			this.showSummaryHeaders = showSummaryHeaders;
		}

		public SummaryLevelEnum getSummaryLevel() {
			return summaryLevel;
		}

		public void setSummaryLevel(SummaryLevelEnum summaryLevel) {
			this.summaryLevel = summaryLevel;
		}

		public void setDataSourceFileName(String dataSourceFileName) {
			this.dataSourceFileName = dataSourceFileName;
		}

		public String getDataSourceFileName() {
			return dataSourceFileName;
		}
	}

	private class NetworkSubreportExpression extends
			AbstractSimpleExpression<JasperReportBuilder> {

		private static final long serialVersionUID = 1656551952324L;

		private PackageMarketFlightPreviousRecordExpression packageMarketFlightPreviousRecordExpression;

		private FlightLinePreviousRecordExpression flightLinePreviousRecordExpression;

		private StyleBuilder columnTitleStyle;

		private StyleBuilder columnStyle;

		private boolean exportAsExcel;

		private boolean showSummaryHeaders;

		private String dataSourceFileName;

		private SummaryLevelEnum summaryLevel;
		
		private boolean excludeNetworkDetails;

		public NetworkSubreportExpression(
				String dataSourceFileName,
				PackageMarketFlightPreviousRecordExpression packageMarketFlightPreviousRecordExpression,
				FlightLinePreviousRecordExpression flightLinePreviousRecordExpression,
				StyleBuilder columnTitleStyle, StyleBuilder columnStyle,
				boolean exportAsExcel, boolean showSummaryHeaders,
				boolean excludeNetworkDetails) {
			this(dataSourceFileName,
					packageMarketFlightPreviousRecordExpression,
					flightLinePreviousRecordExpression, columnTitleStyle,
					columnStyle, exportAsExcel, showSummaryHeaders,
					excludeNetworkDetails, null);
		}

		public NetworkSubreportExpression(
				String dataSourceFileName,
				PackageMarketFlightPreviousRecordExpression packageMarketFlightPreviousRecordExpression,
				FlightLinePreviousRecordExpression flightLinePreviousRecordExpression,
				StyleBuilder columnTitleStyle, StyleBuilder columnStyle,
				boolean exportAsExcel, boolean showSummaryHeaders,
				boolean excludeNetworkDetails, SummaryLevelEnum summaryLevel) {
			setDataSourceFileName(dataSourceFileName);
			setPackageMarketFlightPreviousRecordExpression(packageMarketFlightPreviousRecordExpression);
			setFlightLinePreviousRecordExpression(flightLinePreviousRecordExpression);
			setColumnTitleStyle(columnTitleStyle);
			setColumnStyle(columnStyle);
			setExportAsExcel(exportAsExcel);
			setShowSummaryHeaders(showSummaryHeaders);
			setExcludeNetworkDetails(excludeNetworkDetails);
			setSummaryLevel(summaryLevel);
		}

		@Override
		public JasperReportBuilder evaluate(ReportParameters reportParameters) {
			JasperReportBuilder report = report();

			// network detail dataset
			JRXmlDataSource networkDetailDataSource = null;
			try {
				networkDetailDataSource = new JRXmlDataSource(
						getDataSourceFileName(), "/QueryResult/records/Child_Flight_Lines__r/records");
			} catch (JRException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			report.title(cmp.text(""));

			if(!isExcludeNetworkDetails()) {
				// attach observer to group
				CustomGroupBuilder flightLinePreviousRecordCustomGroupBuilder = grp.group(getFlightLinePreviousRecordExpression());
				flightLinePreviousRecordCustomGroupBuilder.setHeaderLayout(GroupHeaderLayout.EMPTY);
				flightLinePreviousRecordCustomGroupBuilder.setPadding(0);
				report.addGroup(flightLinePreviousRecordCustomGroupBuilder);
				
				// create column group to re-render headers with each row
				ColumnGroupBuilder flightLineCurrentRecordColumnGroupBuilder = grp.group(col.column("Id", "Id", type.stringType()));
				flightLineCurrentRecordColumnGroupBuilder.setHeaderLayout(GroupHeaderLayout.EMPTY);
				flightLineCurrentRecordColumnGroupBuilder.showColumnHeaderAndFooter();
				flightLineCurrentRecordColumnGroupBuilder.setPadding(0);
				report.groupBy(flightLineCurrentRecordColumnGroupBuilder).setShowColumnTitle(false);
				
				// network detail sub-report
				SubreportBuilder networkDetailSubreport = cmp
						.subreport(
								new NetworkDetailSubreportExpression(
										getFlightLinePreviousRecordExpression(),
										columnTitleStyle, columnStyle,
										exportAsExcel, false))
						.setDataSource(networkDetailDataSource);
				
				report.columns().detailFooter(cmp.text(""), networkDetailSubreport);
			}

			// add columns
			addColumns(report, getFlightLineColumnLabelHashMap());
			
			// add fields
			report.addField(field("Package_Flight__r/Id", type.stringType()));
			report.addField(field("Package_Flight__r/Type__c", type.stringType()));
			report.addField(field("Id", type.stringType()));
			report.addField(field("Parent_Flight_Line__c", type.stringType()));
			report.addField(field("Package_Flight__r/Package_Market__r/Package__r/Id", type.stringType()));

			// filter
			if(getSummaryLevel() == null) {
				report.setFilterExpression(new FilterByFlightIdExpression(getPackageMarketFlightPreviousRecordExpression()));
			} else if(getSummaryLevel() == SummaryLevelEnum.Market) {
				// set filter by market
				report.setFilterExpression(new FilterByMarketIdExpression(getPackageMarketFlightPreviousRecordExpression()));
			} else if(getSummaryLevel() == SummaryLevelEnum.Package) {
				// set filter by market
				report.setFilterExpression(new FilterByPackageIdExpression(getPackageMarketFlightPreviousRecordExpression()));
				if(!isExportAsExcel()) {
					createPackageCommentsAndDisclamersFooter(getDataSourceFileName(), report);
				}
			}

			// style
			report.setColumnTitleStyle(getColumnTitleStyle());
			report.setColumnStyle(getColumnStyle());

			// add a blank line at the end
			report.addLastPageFooter(cmp.text(""));

			// return report
			return report;
		}
		
		private JasperReportBuilder addColumns(JasperReportBuilder report, Map<String, String> fieldMap) {
		
			// types
			CurrencyWithFractionDecimalType currencyWithFractionDecimalType = new CurrencyWithFractionDecimalType();
			CurrencyWithoutFractionDecimalType currencyWithoutFractionDecimalType = new CurrencyWithoutFractionDecimalType();

			// indicates (summary mode only) that first column has been overriden with Summary column
			boolean firstColumnOverriden = false;

			for(String key : fieldMap.keySet()) {
				// ================================= add columns: begin
				if (isExportAsExcel()) {
					report.setIgnorePageWidth(true);
					if(key.equals("Package_Flight__r/Package_Name__c")) {
                        if(!isShowSummaryHeaders() || firstColumnOverriden) {
							report.addColumn(col.column(
									this.isShowSummaryHeaders() ? "" : getFlightLineColumnLabelHashMap().get("Package_Flight__r/Package_Name__c"),
									this.isShowSummaryHeaders() ? "Parent_Flight_Line__c" : "Package_Flight__r/Package_Name__c", type.stringType()).setWidth(Units.inch(1.32)));
                        } else {
                            if(!firstColumnOverriden) {
                                addSummaryField(getSummaryLevel(), report);
                                firstColumnOverriden = true;
                            }
                        }
					}
					if(key.equals("Package_Flight__r/Name")) {
                        if(!isShowSummaryHeaders() || firstColumnOverriden) {
                            report.addColumn(col.column(
                                    this.isShowSummaryHeaders() ? "" : getFlightLineColumnLabelHashMap().get("Package_Flight__r/Name"),
                                    this.isShowSummaryHeaders() ? "Parent_Flight_Line__c" : "Package_Flight__r/Name", type.stringType()).setWidth(Units.inch(1.32)));
                        } else {
                            if(!firstColumnOverriden) {
                                addSummaryField(getSummaryLevel(), report);
                                firstColumnOverriden = true;
                            }
                        }
					}
					if(key.equals("Package_Flight__r/Division__c")) {
                        if(!isShowSummaryHeaders() || firstColumnOverriden) {
                            report.addColumn(col.column(
                                    this.isShowSummaryHeaders() ? "" : getFlightLineColumnLabelHashMap().get("Package_Flight__r/Division__c"),
                                    this.isShowSummaryHeaders() ? "Parent_Flight_Line__c" : "Package_Flight__r/Division__c", type.stringType()).setWidth(Units.inch(1.32)));
                        } else {
                            if(!firstColumnOverriden) {
                                addSummaryField(getSummaryLevel(), report);
                                firstColumnOverriden = true;
                            }
                        }
					}
					if(key.equals("Network_Name__c")) {
                        if(!isShowSummaryHeaders() || firstColumnOverriden) {
                            report.addColumn(col.column(
                                    this.isShowSummaryHeaders() ? "" : getFlightLineColumnLabelHashMap().get("Network_Name__c"),
                                    this.isShowSummaryHeaders() ? "Parent_Flight_Line__c" : "Network_Name__c", type.stringType()).setWidth(Units.inch(1.32)));
                        } else {
                            if(!firstColumnOverriden) {
                                addSummaryField(getSummaryLevel(), report);
                                firstColumnOverriden = true;
                            }
                        }
					}
					if(key.equals("Number_of_Panels__c")) {
                        if(!isShowSummaryHeaders() || firstColumnOverriden) {
                            report.addColumn(col.column(getFlightLineColumnLabelHashMap().get("Number_of_Panels__c"), "Number_of_Panels__c",
                                    type.integerType()).setPattern("#,###").setHorizontalAlignment(HorizontalAlignment.RIGHT).setWidth(Units.inch(1.32)));
                        } else {
                            if(!firstColumnOverriden) {
                                addSummaryField(getSummaryLevel(), report);
                                firstColumnOverriden = true;
                            }
                        }
					}
					if(key.equals("Package_Flight__r/Target__c")) {
                        if(!isShowSummaryHeaders() || firstColumnOverriden) {
                            report.addColumn(col.column(
                                    this.isShowSummaryHeaders() ? "" : getFlightLineColumnLabelHashMap().get("Package_Flight__r/Target__c"),
                                    this.isShowSummaryHeaders() ? "Parent_Flight_Line__c" : "Package_Flight__r/Target__c", type.stringType())
                                    .setHorizontalAlignment(HorizontalAlignment.CENTER).setWidth(Units.inch(1.32)));
                        } else {
                            if(!firstColumnOverriden) {
                                addSummaryField(getSummaryLevel(), report);
                                firstColumnOverriden = true;
                            }
                        }
					}
					if(key.equals("Package_Flight__r/Target_Population__c")) {
						if(!isShowSummaryHeaders() || firstColumnOverriden) {
							report.addColumn(col.column(
									this.isShowSummaryHeaders() ? "" : getFlightLineColumnLabelHashMap().get("Package_Flight__r/Target_Population__c"),
									this.isShowSummaryHeaders() ? "OB_Summ_Num__c" : "Package_Flight__r/Target_Population__c",
									type.bigDecimalType()).setWidth(Units.inch(1.32)));
						} else {
							if(!firstColumnOverriden) {
								addSummaryField(getSummaryLevel(), report);
								firstColumnOverriden = true;
							}
						}
					}
					if(key.equals("Package_Flight__r/Campaign_Start_Date__c")) {
                        if(!isShowSummaryHeaders() || firstColumnOverriden) {
                            report.addColumn(col.column(
                                    this.isShowSummaryHeaders() ? "" : getFlightLineColumnLabelHashMap().get("Package_Flight__r/Campaign_Start_Date__c"),
                                    this.isShowSummaryHeaders() ? "Parent_Flight_Line__c" : "Package_Flight__r/Campaign_Start_Date__c", type.stringType())
                                    .setValueFormatter(new ValueFormatter()).setHorizontalAlignment(HorizontalAlignment.CENTER).setWidth(Units.inch(1.32)));					
                        } else {
                            if(!firstColumnOverriden) {
                                addSummaryField(getSummaryLevel(), report);
                                firstColumnOverriden = true;
                            }
                        }
					}
					if(key.equals("Package_Flight__r/Campaign_End_Date__c")) {
                        if(!isShowSummaryHeaders() || firstColumnOverriden) {
                            report.addColumn(col.column(
                                    this.isShowSummaryHeaders() ? "" : getFlightLineColumnLabelHashMap().get("Package_Flight__r/Campaign_End_Date__c"),
                                    this.isShowSummaryHeaders() ? "Parent_Flight_Line__c" : "Package_Flight__r/Campaign_End_Date__c", type.stringType())
                                    .setValueFormatter(new ValueFormatter()).setHorizontalAlignment(HorizontalAlignment.CENTER).setWidth(Units.inch(1.32)));					
                        } else {
                            if(!firstColumnOverriden) {
                                addSummaryField(getSummaryLevel(), report);
                                firstColumnOverriden = true;
                            }
                        }
					}
					if(key.equals("Package_Flight__r/Duration_And_Type__c")) {
                        if(!isShowSummaryHeaders() || firstColumnOverriden) {
                            report.addColumn(col.column(
                                    this.isShowSummaryHeaders() ? "" : getFlightLineColumnLabelHashMap().get("Package_Flight__r/Duration_And_Type__c"),
                                    this.isShowSummaryHeaders() ? "Parent_Flight_Line__c" : "Package_Flight__r/Duration_And_Type__c", type.stringType()).setWidth(Units.inch(1.32)));
                        } else {
                            if(!firstColumnOverriden) {
                                addSummaryField(getSummaryLevel(), report);
                                firstColumnOverriden = true;
                            }
                        }
					}
					if (key.equals("Media_Category__c")) {
                        if(!isShowSummaryHeaders() || firstColumnOverriden) {
                            report.addColumn(col.column(
                                    this.isShowSummaryHeaders() ? "" : getFlightLineColumnLabelHashMap().get("Media_Category__c"), 
                                    this.isShowSummaryHeaders() ? "Parent_Flight_Line__c" : "Media_Category__c",
                                    type.stringType()).setHorizontalAlignment(HorizontalAlignment.LEFT).setWidth(Units.inch(1.32)));
                        } else {
                            if(!firstColumnOverriden) {
                                addSummaryField(getSummaryLevel(), report);
                                firstColumnOverriden = true;
                            }
                        }
					}
					if(key.equals("Weekly_Total_18_Imps__c")) {
						report.addColumn(col.column(getFlightLineColumnLabelHashMap().get("Weekly_Total_18_Imps__c"),
								getSummaryFieldNameRelatedToFlightLineFieldName(getSummaryLevel(), "Weekly_Total_18_Imps__c"),
								type.integerType()).setPattern("#,###").setHorizontalAlignment(HorizontalAlignment.RIGHT).setWidth(Units.inch(1.32)));
					}
					if(key.equals("In_Mkt_Imps__c")) {
						report.addColumn(col.column(getFlightLineColumnLabelHashMap().get("In_Mkt_Imps__c"),
								getSummaryFieldNameRelatedToFlightLineFieldName(getSummaryLevel(), "In_Mkt_Imps__c"),
								type.doubleType()).setPattern("#,###").setHorizontalAlignment(HorizontalAlignment.RIGHT).setWidth(Units.inch(1.32)));
					}
					if(key.equals("Total_Imps__c")) {
						report.addColumn(col.column(getFlightLineColumnLabelHashMap().get("Total_Imps__c"),
								getSummaryFieldNameRelatedToFlightLineFieldName(getSummaryLevel(), "Total_Imps__c"),
								type.doubleType()).setPattern("#,###").setHorizontalAlignment(HorizontalAlignment.RIGHT).setWidth(Units.inch(1.32)));
					}
					if(key.equals("In_Mkt_TRP__c")) {
						report.addColumn(col.column(getFlightLineColumnLabelHashMap().get("In_Mkt_TRP__c"),
								getSummaryFieldNameRelatedToFlightLineFieldName(getSummaryLevel(), "In_Mkt_TRP__c"),
								type.doubleType()).setPattern("##0.0").setHorizontalAlignment(HorizontalAlignment.RIGHT).setWidth(Units.inch(1.32)));
					}
					if(key.equals("PlanTRP__c")) {
						report.addColumn(col.column(getFlightLineColumnLabelHashMap().get("PlanTRP__c"),
								getSummaryFieldNameRelatedToFlightLineFieldName(getSummaryLevel(), "PlanTRP__c"),
								type.doubleType()).setPattern("##0.0").setHorizontalAlignment(HorizontalAlignment.RIGHT).setWidth(Units.inch(1.32)));
					}
					if(key.equals("Plan_Imps_Reach_Perc__c")) {
                        if(!isShowSummaryHeaders() || firstColumnOverriden) {
                            report.addColumn(col.column(getFlightLineColumnLabelHashMap().get("Plan_Imps_Reach_Perc__c"), "Plan_Imps_Reach_Perc__c",
                                    type.percentageType()).setPattern("##0.0").setHorizontalAlignment(HorizontalAlignment.RIGHT).setWidth(Units.inch(1.32)));
                        } else {
                            if(!firstColumnOverriden) {
                                addSummaryField(getSummaryLevel(), report);
                                firstColumnOverriden = true;
                            }
                        }
					}
					if(key.equals("Plan_Imps_Avg_Frequency__c")) {
                        if(!isShowSummaryHeaders() || firstColumnOverriden) {
                            report.addColumn(col.column(getFlightLineColumnLabelHashMap().get("Plan_Imps_Avg_Frequency__c"), "Plan_Imps_Avg_Frequency__c",
                                    type.doubleType()).setPattern("##0.0").setHorizontalAlignment(HorizontalAlignment.RIGHT).setWidth(Units.inch(1.32)));
                        } else {
                            if(!firstColumnOverriden) {
                                addSummaryField(getSummaryLevel(), report);
                                firstColumnOverriden = true;
                            }
                        }
					}
					if(key.equals("X4_Wk_Proposed_Price__c")) {
                        if(!isShowSummaryHeaders() || firstColumnOverriden) {
                            report.addColumn(col.column(
                                    this.isShowSummaryHeaders() ? "" : getFlightLineColumnLabelHashMap().get("X4_Wk_Proposed_Price__c"),
                                    this.isShowSummaryHeaders() ? "OB_Summ_Num__c" : "X4_Wk_Proposed_Price__c", currencyWithoutFractionDecimalType)
                                    .setHorizontalAlignment(HorizontalAlignment.RIGHT).setWidth(Units.inch(1.32)));
                        } else {
                            if(!firstColumnOverriden) {
                                addSummaryField(getSummaryLevel(), report);
                                firstColumnOverriden = true;
                            }
                        }
					}
					if(key.equals("Total_Price_0d__c")) {
						report.addColumn(col.column(getFlightLineColumnLabelHashMap().get("Total_Price_0d__c"),
								getSummaryFieldNameRelatedToFlightLineFieldName(getSummaryLevel(), "Total_Price_0d__c"),
								currencyWithoutFractionDecimalType).setHorizontalAlignment(HorizontalAlignment.RIGHT).setWidth(Units.inch(1.32)));
					}
					if(key.equals("TotalInMarketCPM_0d__c")) {
						report.addColumn(col.column(getFlightLineColumnLabelHashMap().get("TotalInMarketCPM_0d__c"),
								getSummaryFieldNameRelatedToFlightLineFieldName(getSummaryLevel(), "TotalInMarketCPM_0d__c"),
								currencyWithFractionDecimalType).setHorizontalAlignment(HorizontalAlignment.RIGHT).setWidth(Units.inch(1.32)));
					}
					if(key.equals("CPP_0d__c")) {
						report.addColumn(col.column(getFlightLineColumnLabelHashMap().get("CPP_0d__c"),
								getSummaryFieldNameRelatedToFlightLineFieldName(getSummaryLevel(), "CPP_0d__c"),
								currencyWithoutFractionDecimalType).setHorizontalAlignment(HorizontalAlignment.RIGHT).setWidth(Units.inch(1.32)));
					}
					if(key.equals("Package_Flight__r/Flight_Comments__c")) {
                        if(!isShowSummaryHeaders() || firstColumnOverriden) {
                            report.addColumn(col.column(
                                    this.isShowSummaryHeaders() ? "" : getFlightLineColumnLabelHashMap().get("Package_Flight__r/Flight_Comments__c"),
                                    this.isShowSummaryHeaders() ? "Parent_Flight_Line__c" : "Package_Flight__r/Flight_Comments__c", type.stringType()).setWidth(Units.inch(1.32)));
                        } else {
                            if(!firstColumnOverriden) {
                                addSummaryField(getSummaryLevel(), report);
                                firstColumnOverriden = true;
                            }
                        }
					}
					if(key.equals("Package_Flight__r/Package_Comments__c")) {
                        if(!isShowSummaryHeaders() || firstColumnOverriden) {
                            report.addColumn(col.column(
                                    this.isShowSummaryHeaders() ? "" : getFlightLineColumnLabelHashMap().get("Package_Flight__r/Package_Comments__c"),
                                    this.isShowSummaryHeaders() ? "Parent_Flight_Line__c" : "Package_Flight__r/Package_Comments__c", type.stringType()).setWidth(Units.inch(1.32)));
                        } else {
                            if(!firstColumnOverriden) {
                                addSummaryField(getSummaryLevel(), report);
                                firstColumnOverriden = true;
                            }
                        }
					}
					if(key.equals("Network_Description__c")) {
                        if(!isShowSummaryHeaders() || firstColumnOverriden) {
                            report.addColumn(col.column(
                                    this.isShowSummaryHeaders() ? "" : getFlightLineColumnLabelHashMap().get("Network_Description__c"),
                                    this.isShowSummaryHeaders() ? "Parent_Flight_Line__c" : "Network_Description__c", type.stringType()).setWidth(Units.inch(1.32)));
                        } else {
                            if(!firstColumnOverriden) {
                                addSummaryField(getSummaryLevel(), report);
                                firstColumnOverriden = true;
                            }
                        }
					}
					if(key.equals("Network_Notes__c")) {
                        if(!isShowSummaryHeaders() || firstColumnOverriden) {
                            report.addColumn(col.column(
                                    this.isShowSummaryHeaders() ? "" : getFlightLineColumnLabelHashMap().get("Network_Notes__c"),
                                    this.isShowSummaryHeaders() ? "Parent_Flight_Line__c" : "Network_Notes__c", type.stringType()).setWidth(Units.inch(1.32)));
                        } else {
                            if(!firstColumnOverriden) {
                                addSummaryField(getSummaryLevel(), report);
                                firstColumnOverriden = true;
                            }
                        }
					}
					if(getPackageMarketFlightPreviousRecordExpression().isDigitalMediaCategory()) {
						if(key.equals("Average_Daily_Spots__c")) {
                            if(!isShowSummaryHeaders() || firstColumnOverriden) {
                                report.addColumn(col.column(
                                        this.isShowSummaryHeaders() ? "" : getFlightLineColumnLabelHashMap().get("Average_Daily_Spots__c"),
                                        this.isShowSummaryHeaders() ? "Parent_Flight_Line__c" : "Average_Daily_Spots__c", type.stringType())
                                        .setHorizontalAlignment(HorizontalAlignment.RIGHT).setWidth(Units.inch(1.32)));
                            } else {
                                if(!firstColumnOverriden) {
                                    addSummaryField(getSummaryLevel(), report);
                                    firstColumnOverriden = true;
                                }
                            }
						}
						if(key.equals("Hours_of_Operation__c")) {
                            if(!isShowSummaryHeaders() || firstColumnOverriden) {
                                report.addColumn(col.column(
                                        this.isShowSummaryHeaders() ? "" : getFlightLineColumnLabelHashMap().get("Hours_of_Operation__c"),
                                        this.isShowSummaryHeaders() ? "Parent_Flight_Line__c" : "Hours_of_Operation__c", type.stringType())
                                        .setHorizontalAlignment(HorizontalAlignment.RIGHT).setWidth(Units.inch(1.32)));
                            } else {
                                if(!firstColumnOverriden) {
                                    addSummaryField(getSummaryLevel(), report);
                                    firstColumnOverriden = true;
                                }
                            }
						}
					}
					if (key.equals("Discount__c")) {
						report.addColumn(col.column(getFlightLineColumnLabelHashMap().get("Discount__c"),
								getSummaryFieldNameRelatedToFlightLineFieldName(getSummaryLevel(), "Discount__c"),
								type.percentageType()).setHorizontalAlignment(HorizontalAlignment.RIGHT)
								.setWidth(Units.inch(1.32)).setPattern("##0.0"));
					}
				}

				if(!this.isExportAsExcel()) {
					if(key.equals("Network_Name__c")) {
                        if(!isShowSummaryHeaders() || firstColumnOverriden) {
                            report.addColumn(col.column(
                                    this.isShowSummaryHeaders() ? "" : getFlightLineColumnLabelHashMap().get("Network_Name__c"),
                                    this.isShowSummaryHeaders() ? "Parent_Flight_Line__c" : "Network_Name__c", type.stringType()));
                        } else {
                            if(!firstColumnOverriden) {
                                addSummaryField(getSummaryLevel(), report);
                                firstColumnOverriden = true;
                            }
                        }
					}
					if(key.equals("Number_of_Panels__c")) {
                        if(!isShowSummaryHeaders() || firstColumnOverriden) {
                            report.addColumn(col.column(getFlightLineColumnLabelHashMap().get("Number_of_Panels__c"), "Number_of_Panels__c",
                                    type.integerType()).setPattern("#,###").setHorizontalAlignment(HorizontalAlignment.RIGHT));
                        } else {
                            if(!firstColumnOverriden) {
                                addSummaryField(getSummaryLevel(), report);
                                firstColumnOverriden = true;
                            }
                        }
					}
					if (key.equals("Media_Category__c")) {
                        if(!isShowSummaryHeaders() || firstColumnOverriden) {
                            report.addColumn(col.column(
                                    this.isShowSummaryHeaders() ? "" : getFlightLineColumnLabelHashMap().get("Media_Category__c"), 
                                    this.isShowSummaryHeaders() ? "Parent_Flight_Line__c" : "Media_Category__c",
                                    type.stringType()).setHorizontalAlignment(HorizontalAlignment.LEFT).setWidth(Units.inch(2.0)));
                        } else {
                            if(!firstColumnOverriden) {
                                addSummaryField(getSummaryLevel(), report);
                                firstColumnOverriden = true;
                            }
                        }
					}
					if(key.equals("Weekly_Total_18_Imps__c")) {
						report.addColumn(col.column(getFlightLineColumnLabelHashMap().get("Weekly_Total_18_Imps__c"),
								getSummaryFieldNameRelatedToFlightLineFieldName(getSummaryLevel(), "Weekly_Total_18_Imps__c"),
								type.integerType()).setPattern("#,###").setHorizontalAlignment(HorizontalAlignment.RIGHT));
					}
					if(key.equals("In_Mkt_Imps__c")) {
						report.addColumn(col.column(getFlightLineColumnLabelHashMap().get("In_Mkt_Imps__c"),
								getSummaryFieldNameRelatedToFlightLineFieldName(getSummaryLevel(), "In_Mkt_Imps__c"),
								type.doubleType()).setPattern("#,###").setHorizontalAlignment(HorizontalAlignment.RIGHT));
					}
					if(key.equals("Total_Imps__c")) {
						report.addColumn(col.column(getFlightLineColumnLabelHashMap().get("Total_Imps__c"),
								getSummaryFieldNameRelatedToFlightLineFieldName(getSummaryLevel(), "Total_Imps__c"),
								type.doubleType()).setPattern("#,###").setHorizontalAlignment(HorizontalAlignment.RIGHT));
					}
					if(key.equals("In_Mkt_TRP__c")) {
						report.addColumn(col.column(getFlightLineColumnLabelHashMap().get("In_Mkt_TRP__c"),
								getSummaryFieldNameRelatedToFlightLineFieldName(getSummaryLevel(), "In_Mkt_TRP__c"),
								type.doubleType()).setPattern("##0.0").setHorizontalAlignment(HorizontalAlignment.RIGHT));
					}
					if(key.equals("PlanTRP__c")) {
						report.addColumn(col.column(getFlightLineColumnLabelHashMap().get("PlanTRP__c"),
								getSummaryFieldNameRelatedToFlightLineFieldName(getSummaryLevel(), "PlanTRP__c"),
								type.doubleType()).setPattern("##0.0").setHorizontalAlignment(HorizontalAlignment.RIGHT));
					}
					if(key.equals("Plan_Imps_Reach_Perc__c")) {
                        if(!isShowSummaryHeaders() || firstColumnOverriden) {
                            report.addColumn(col.column(getFlightLineColumnLabelHashMap().get("Plan_Imps_Reach_Perc__c"), "Plan_Imps_Reach_Perc__c",
                                    type.percentageType()).setPattern("##0.0").setHorizontalAlignment(HorizontalAlignment.RIGHT));
                        } else {
                            if(!firstColumnOverriden) {
                                addSummaryField(getSummaryLevel(), report);
                                firstColumnOverriden = true;
                            }
                        }
					}
					if(key.equals("Plan_Imps_Avg_Frequency__c")) {
                        if(!isShowSummaryHeaders() || firstColumnOverriden) {
                            report.addColumn(col.column(getFlightLineColumnLabelHashMap().get("Plan_Imps_Avg_Frequency__c"), "Plan_Imps_Avg_Frequency__c",
                                    type.doubleType()).setHorizontalAlignment(HorizontalAlignment.RIGHT));
                        } else {
                            if(!firstColumnOverriden) {
                                addSummaryField(getSummaryLevel(), report);
                                firstColumnOverriden = true;
                            }
                        }
					}
					if(key.equals("X4_Wk_Proposed_Price__c")) {
                        if(!isShowSummaryHeaders() || firstColumnOverriden) {
                            report.addColumn(col.column(
                                    this.isShowSummaryHeaders() ? "" : getFlightLineColumnLabelHashMap().get("X4_Wk_Proposed_Price__c"),
                                    this.isShowSummaryHeaders() ? "OB_Summ_Num__c" : "X4_Wk_Proposed_Price__c",
                                    currencyWithoutFractionDecimalType).setHorizontalAlignment(HorizontalAlignment.RIGHT));
                        } else {
                            if(!firstColumnOverriden) {
                                addSummaryField(getSummaryLevel(), report);
                                firstColumnOverriden = true;
                            }
                        }
					}
					if(key.equals("Total_Price_0d__c")) {
						report.addColumn(col.column(getFlightLineColumnLabelHashMap().get("Total_Price_0d__c"),
								getSummaryFieldNameRelatedToFlightLineFieldName(getSummaryLevel(), "Total_Price_0d__c"),
								currencyWithoutFractionDecimalType).setHorizontalAlignment(HorizontalAlignment.RIGHT));
					}
					if(key.equals("TotalInMarketCPM_0d__c")) {
						report.addColumn(col.column(getFlightLineColumnLabelHashMap().get("TotalInMarketCPM_0d__c"),
								getSummaryFieldNameRelatedToFlightLineFieldName(getSummaryLevel(), "TotalInMarketCPM_0d__c"),
								currencyWithFractionDecimalType).setHorizontalAlignment(HorizontalAlignment.RIGHT));
					}
					if(key.equals("CPP_0d__c")) {
						report.addColumn(col.column(getFlightLineColumnLabelHashMap().get("CPP_0d__c"),
								getSummaryFieldNameRelatedToFlightLineFieldName(getSummaryLevel(), "CPP_0d__c"),
								currencyWithoutFractionDecimalType).setHorizontalAlignment(HorizontalAlignment.RIGHT));
					}
					if(key.equals("Network_Description__c")) {
                        if(!isShowSummaryHeaders() || firstColumnOverriden) {
                            report.addColumn(col.column(
                                    this.isShowSummaryHeaders() ? "" : getFlightLineColumnLabelHashMap().get("Network_Description__c"),
                                    this.isShowSummaryHeaders() ? "Parent_Flight_Line__c" : "Network_Description__c", type.stringType())
                                    .setWidth(Units.inch(3.5)));
                        } else {
                            if(!firstColumnOverriden) {
                                addSummaryField(getSummaryLevel(), report);
                                firstColumnOverriden = true;
                            }
                        }
					}
					if(key.equals("Network_Notes__c")) {
                        if(!isShowSummaryHeaders() || firstColumnOverriden) {
                            report.addColumn(col.column(
                                    this.isShowSummaryHeaders() ? "" : getFlightLineColumnLabelHashMap().get("Network_Notes__c"),
                                    this.isShowSummaryHeaders() ? "Parent_Flight_Line__c" : "Network_Notes__c", type.stringType()));
                        } else {
                            if(!firstColumnOverriden) {
                                addSummaryField(getSummaryLevel(), report);
                                firstColumnOverriden = true;
                            }
                        }
					}
					if(getPackageMarketFlightPreviousRecordExpression().isDigitalMediaCategory()) {
						if(key.equals("Average_Daily_Spots__c")) {
                            if(!isShowSummaryHeaders() || firstColumnOverriden) {
                                report.addColumn(col.column(
                                        this.isShowSummaryHeaders() ? "" : getFlightLineColumnLabelHashMap().get("Average_Daily_Spots__c"),
                                        this.isShowSummaryHeaders() ? "Parent_Flight_Line__c" : "Average_Daily_Spots__c", type.stringType())
                                        .setHorizontalAlignment(HorizontalAlignment.RIGHT));
                            } else {
                                if(!firstColumnOverriden) {
                                    addSummaryField(getSummaryLevel(), report);
                                    firstColumnOverriden = true;
                                }
                            }
						}
						if(key.equals("Hours_of_Operation__c")) {
                            if(!isShowSummaryHeaders() || firstColumnOverriden) {
                                report.addColumn(col.column(
                                        this.isShowSummaryHeaders() ? "" : getFlightLineColumnLabelHashMap().get("Hours_of_Operation__c"),
                                        this.isShowSummaryHeaders() ? "Parent_Flight_Line__c" : "Hours_of_Operation__c", type.stringType())
                                        .setHorizontalAlignment(HorizontalAlignment.RIGHT).setWidth(Units.inch(1.32)));
                            } else {
                                if(!firstColumnOverriden) {
                                    addSummaryField(getSummaryLevel(), report);
                                    firstColumnOverriden = true;
                                }
                            }
						}
					}
					if (key.equals("Discount__c")) {
						report.addColumn(col.column(getFlightLineColumnLabelHashMap().get("Discount__c"),
								getSummaryFieldNameRelatedToFlightLineFieldName(getSummaryLevel(), "Discount__c"),
								type.percentageType()).setHorizontalAlignment(HorizontalAlignment.RIGHT)
								.setWidth(Units.inch(1.5)).setPattern("##0.0"));
					}
				}
			}
			if (isExportAsExcel()) {
				if (fieldMap.containsKey("X4_Wk_Base_Rate__c")) {
	                if(!isShowSummaryHeaders() || firstColumnOverriden) {
	                    report.addColumn(col.column(
	                            this.isShowSummaryHeaders() ? "" : getFlightLineColumnLabelHashMap().get("X4_Wk_Base_Rate__c"),
	                            this.isShowSummaryHeaders() ? "OB_Summ_Num__c" : "X4_Wk_Base_Rate__c", currencyWithoutFractionDecimalType)
	                            .setHorizontalAlignment(HorizontalAlignment.RIGHT).setWidth(Units.inch(1.32)));
	                } else {
	                    if(!firstColumnOverriden) {
	                        addSummaryField(getSummaryLevel(), report);
	                        firstColumnOverriden = true;
	                    }
	                }
				}
				if (fieldMap.containsKey("X4_Wk_Floor__c")) {
	                if(!isShowSummaryHeaders() || firstColumnOverriden) {
	                    report.addColumn(col.column(
	                            this.isShowSummaryHeaders() ? "" : getFlightLineColumnLabelHashMap().get("X4_Wk_Floor__c"),
	                            this.isShowSummaryHeaders() ? "OB_Summ_Num__c" : "X4_Wk_Floor__c", currencyWithoutFractionDecimalType)
	                            .setHorizontalAlignment(HorizontalAlignment.RIGHT).setWidth(Units.inch(1.32)));
	                } else {
	                    if(!firstColumnOverriden) {
	                        addSummaryField(getSummaryLevel(), report);
	                        firstColumnOverriden = true;
	                    }
	                }
				}
			}
			// ================================= add columns: end
			return report;
		}

		public PackageMarketFlightPreviousRecordExpression getPackageMarketFlightPreviousRecordExpression() {
			return packageMarketFlightPreviousRecordExpression;
		}

		public void setPackageMarketFlightPreviousRecordExpression(
				PackageMarketFlightPreviousRecordExpression packageMarketFlightPreviousRecordExpression) {
			this.packageMarketFlightPreviousRecordExpression = packageMarketFlightPreviousRecordExpression;
		}

		public StyleBuilder getColumnTitleStyle() {
			return columnTitleStyle;
		}

		public void setColumnTitleStyle(StyleBuilder columnTitleStyle) {
			this.columnTitleStyle = columnTitleStyle;
		}

		public StyleBuilder getColumnStyle() {
			return columnStyle;
		}

		public void setColumnStyle(StyleBuilder columnStyle) {
			this.columnStyle = columnStyle;
		}

		public boolean isExportAsExcel() {
			return exportAsExcel;
		}

		public void setExportAsExcel(boolean exportAsExcel) {
			this.exportAsExcel = exportAsExcel;
		}

		public boolean isShowSummaryHeaders() {
			return showSummaryHeaders;
		}

		public void setShowSummaryHeaders(boolean showSummaryHeaders) {
			this.showSummaryHeaders = showSummaryHeaders;
		}

		public String getDataSourceFileName() {
			return dataSourceFileName;
		}

		public void setDataSourceFileName(String dataSourceFileName) {
			this.dataSourceFileName = dataSourceFileName;
		}

		public FlightLinePreviousRecordExpression getFlightLinePreviousRecordExpression() {
			return flightLinePreviousRecordExpression;
		}

		public void setFlightLinePreviousRecordExpression(
				FlightLinePreviousRecordExpression theFlightLinePreviousRecordExpression) {
			this.flightLinePreviousRecordExpression = theFlightLinePreviousRecordExpression;
		}

		public SummaryLevelEnum getSummaryLevel() {
			return summaryLevel;
		}

		public void setSummaryLevel(SummaryLevelEnum summaryLevel) {
			this.summaryLevel = summaryLevel;
		}

		public boolean isExcludeNetworkDetails() {
			return excludeNetworkDetails;
		}

		public void setExcludeNetworkDetails(boolean excludeNetworkDetails) {
			this.excludeNetworkDetails = excludeNetworkDetails;
		}
	}

	@SuppressWarnings("serial")
	private class NetworkDetailSubreportExpression extends AbstractSimpleExpression<JasperReportBuilder> {

		private FlightLinePreviousRecordExpression flightLinePreviousRecordExpression;

		private StyleBuilder columnTitleStyle;

		private StyleBuilder columnStyle;

		private boolean exportAsExcel;

		private boolean showSummaryHeaders;

		public NetworkDetailSubreportExpression(
				FlightLinePreviousRecordExpression flightLinePreviousRecordExpression,
				StyleBuilder columnTitleStyle, StyleBuilder columnStyle,
				boolean exportAsExcel, boolean showSummaryHeaders) {
			setFlightLinePreviousRecordExpression(flightLinePreviousRecordExpression);
			setColumnTitleStyle(columnTitleStyle);
			setColumnStyle(columnStyle);
			setExportAsExcel(exportAsExcel);
			setShowSummaryHeaders(showSummaryHeaders);
		}

		@Override
		public JasperReportBuilder evaluate(ReportParameters reportParameters) {
			JasperReportBuilder report = report();

			// add fields
			report.addField(field("Package_Flight__r/Id", type.stringType()));
			report.addField(field("Package_Flight__r/Type__c", type.stringType()));

			// add group fields
			report.addField(field("Parent_Flight_Line__c", type.stringType()));
			report.addField(field("Network_Name__c", type.stringType()));

			addColumns(report, getFlightLineColumnLabelHashMap());
			// style
			if(!isExportAsExcel()) {
				report.highlightDetailEvenRows();
			}
			report.setColumnTitleStyle(getColumnTitleStyle());
			report.setColumnStyle(getColumnStyle());

			// filter
			report.setFilterExpression(new FilterByDetailFlightLineIdExpression(getFlightLinePreviousRecordExpression()));

			// add a blank line at the end
			report.addLastPageFooter(cmp.text(""));
			// return report
			return report;
		}

		private JasperReportBuilder addColumns(JasperReportBuilder report, Map<String, String> fieldMap) {

			for(String key : fieldMap.keySet()) {
			// ================================= add columns: begin
			// network detail columns (Excel)
			if(this.isExportAsExcel()) {
				report.setIgnorePageWidth(true);
				TextColumnBuilder<Integer> mapLocNumberColumn;
				if(key.equals("MapLocation_Number__c")) {
					try {
						mapLocNumberColumn = col.column(
								isShowSummaryHeaders() ? "" : getFlightLineColumnLabelHashMap().get("MapLocation_Number__c"),
								new MapLocationNumberExpressionColumn(getMapPanelOrderPrefDataSourceFileName()))
								.setHorizontalAlignment(HorizontalAlignment.RIGHT).setWidth(Units.inch(1.32));
						report.addColumn(mapLocNumberColumn);
						report.sortBy(mapLocNumberColumn);
						report.addField(field("MapLocation_Number__c", type.stringType()));
					} catch (ParserConfigurationException e) {
	                    // TODO Auto-generated catch block
	                    e.printStackTrace();
	                } catch (SAXException e) {
	                    // TODO Auto-generated catch block
	                    e.printStackTrace();
	                } catch (IOException e) {
	                    // TODO Auto-generated catch block
	                    e.printStackTrace();
	                } catch (ParseException e) {
	                    // TODO Auto-generated catch block
	                    e.printStackTrace();
	                }
				}
				if(key.equals("MarketName__c")) {
					report.addColumn(
						col.column(
							this.isShowSummaryHeaders() ? "" : getFlightLineColumnLabelHashMap().get("MarketName__c"),
							"MarketName__c", type.stringType()).setWidth(Units.inch(1.32)));
				}
				if(key.equals("Panel_Id_Label__c")) {
					report.addColumn(col.column(
							this.isShowSummaryHeaders() ? "" : getFlightLineColumnLabelHashMap().get("Panel_Id_Label__c"),
							"Panel_Id_Label__c", type.stringType())
							.setHorizontalAlignment(HorizontalAlignment.RIGHT).setWidth(Units.inch(1.32)));
				}
				if(key.equals("TAB_Id__c")) {
					report.addColumn(col.column(
							this.isShowSummaryHeaders() ? "" : getFlightLineColumnLabelHashMap().get("TAB_Id__c"),
							"TAB_Id__c", type.stringType())
							.setHorizontalAlignment(HorizontalAlignment.RIGHT).setWidth(Units.inch(1.32)));
				}
				if(key.equals("Location_Description__c")) {
					report.addColumn(col.column(
							this.isShowSummaryHeaders() ? "" : getFlightLineColumnLabelHashMap().get("Location_Description__c"),
							"Location_Description__c", type.stringType()).setWidth(Units.inch(1.32)));
				}
				// TODO: network detail columns. implement "Media Product" column in salesforce object: PFL. Add formula field (Panel__r.Quattro_Media_Product__c). Add to fieldset
				if(key.equals("Quattro_Media_Product__c")) {
					report.addColumn(col.column(
							this.isShowSummaryHeaders() ? "" : getFlightLineColumnLabelHashMap().get("Quattro_Media_Product__c"),
							"Quattro_Media_Product__c", type.stringType())
							.setHorizontalAlignment(HorizontalAlignment.CENTER).setWidth(Units.inch(1.32)));
				}
				if(key.equals("Face_Direction__c")) {
					report.addColumn(col.column(
							this.isShowSummaryHeaders() ? "" : getFlightLineColumnLabelHashMap().get("Face_Direction__c"),
							"Face_Direction__c", type.stringType())
							.setHorizontalAlignment(HorizontalAlignment.LEFT).setWidth(Units.inch(1.32)));
				}
				if(key.equals("Unit_Size__c")) {
					report.addColumn(col.column(
							this.isShowSummaryHeaders() ? "" : getFlightLineColumnLabelHashMap().get("Unit_Size__c"),
							"Unit_Size__c", type.stringType())
							.setHorizontalAlignment(HorizontalAlignment.RIGHT).setWidth(Units.inch(1.32)));
				}
	            if(key.equals("Weekly_Total_18_Imps__c")) {
	                report.addColumn(col.column(
	                        this.isShowSummaryHeaders() ? "" : getFlightLineColumnLabelHashMap().get("Weekly_Total_18_Imps__c"),
	                        "Weekly_Total_18_Imps__c", type.integerType()).setPattern("#,###")
	                        .setHorizontalAlignment(HorizontalAlignment.RIGHT).setWidth(Units.inch(1.32)));
	            }
	            if(key.equals("In_Mkt_Imps__c")) {
	                report.addColumn(col.column(
	                        this.isShowSummaryHeaders() ? "" : getFlightLineColumnLabelHashMap().get("In_Mkt_Imps__c"),
	                        "In_Mkt_Imps__c", type.doubleType()).setPattern("#,###")
	                        .setHorizontalAlignment(HorizontalAlignment.RIGHT).setWidth(Units.inch(1.32)));
	            }
	            if(key.equals("Total_Imps__c")) {
	                report.addColumn(col.column(
	                        this.isShowSummaryHeaders() ? "" : getFlightLineColumnLabelHashMap().get("Total_Imps__c"),
	                        "Total_Imps__c", type.doubleType()).setPattern("#,###")
	                        .setHorizontalAlignment(HorizontalAlignment.RIGHT).setWidth(Units.inch(1.32)));
	            }
	            if(key.equals("In_Mkt_TRP__c")) {
	                report.addColumn(col.column(
	                        this.isShowSummaryHeaders() ? "" : getFlightLineColumnLabelHashMap().get("In_Mkt_TRP__c"),
	                        "In_Mkt_TRP__c", type.doubleType()).setPattern("##0.0")
	                        .setHorizontalAlignment(HorizontalAlignment.RIGHT).setWidth(Units.inch(1.32)));
	            }
	            if(key.equals("PlanTRP__c")) {
	                report.addColumn(col.column(
	                        this.isShowSummaryHeaders() ? "" : getFlightLineColumnLabelHashMap().get("PlanTRP__c"),
	                        "PlanTRP__c", type.doubleType()).setPattern("##0.0")
	                        .setHorizontalAlignment(HorizontalAlignment.RIGHT).setWidth(Units.inch(1.32)));
	            }
	            if(key.equals("Plan_Imps_Reach_Perc__c")) {
	                report.addColumn(col.column(
	                		this.isShowSummaryHeaders() ? "" : getFlightLineColumnLabelHashMap().get("Plan_Imps_Reach_Perc__c"),
	                        "Plan_Imps_Reach_Perc__c", type.percentageType()).setPattern("##0.0")
	                        .setHorizontalAlignment(HorizontalAlignment.RIGHT).setWidth(Units.inch(1.32)));
	            }
	            if(key.equals("Plan_Imps_Avg_Frequency__c")) {
	                report.addColumn(col.column(
	                        this.isShowSummaryHeaders() ? "" : getFlightLineColumnLabelHashMap().get("Plan_Imps_Avg_Frequency__c"),
	                        "Plan_Imps_Avg_Frequency__c", type.doubleType()).setPattern("##0.0")
	                        .setHorizontalAlignment(HorizontalAlignment.RIGHT).setWidth(Units.inch(1.32)));
	            }
	            if (key.equals("Illumination__c")) {
					report.addColumn(col.column(
							this.isShowSummaryHeaders() ? "" : getFlightLineColumnLabelHashMap().get("Illumination__c"),
							this.isShowSummaryHeaders() ? "OB_Summ_Num__c" : "Illumination__c",	type.bigDecimalType())
							.setHorizontalAlignment(HorizontalAlignment.RIGHT).setPattern("#,###").setWidth(Units.inch(1.32)));
				}
			}

			// network detail columns (PDF)
			if(!this.isExportAsExcel()) {
				TextColumnBuilder<Integer> mapLocNumberColumn;
				if(key.equals("MapLocation_Number__c")) {
					try {
						mapLocNumberColumn = col.column(
								isShowSummaryHeaders() ? "" : getFlightLineColumnLabelHashMap().get("MapLocation_Number__c"),
								new MapLocationNumberExpressionColumn(getMapPanelOrderPrefDataSourceFileName()))
								.setHorizontalAlignment(HorizontalAlignment.RIGHT).setWidth(Units.inch(2));
						report.addColumn(mapLocNumberColumn);
						report.sortBy(mapLocNumberColumn);
						report.addField(field("MapLocation_Number__c",
								type.stringType()));
					} catch (ParserConfigurationException e) {
	                    // TODO Auto-generated catch block
	                    e.printStackTrace();
	                } catch (SAXException e) {
	                    // TODO Auto-generated catch block
	                    e.printStackTrace();
	                } catch (IOException e) {
	                    // TODO Auto-generated catch block
	                    e.printStackTrace();
	                } catch (ParseException e) {
	                    // TODO Auto-generated catch block
	                    e.printStackTrace();
	                }
				}
				if(key.equals("MarketName__c")) {
					report.addColumn(col.column(
							this.isShowSummaryHeaders() ? "" : getFlightLineColumnLabelHashMap().get("MarketName__c"),
							"MarketName__c", type.stringType()));
				}
				if(key.equals("Location_Description__c")) {
					report.addColumn(col.column(
							this.isShowSummaryHeaders() ? "" : getFlightLineColumnLabelHashMap().get("Location_Description__c"),
							"Location_Description__c", type.stringType()).setWidth(Units.inch(3.5)));
				}
				if(key.equals("Panel_Id_Label__c")) {
					report.addColumn(col.column(
							this.isShowSummaryHeaders() ? "" : getFlightLineColumnLabelHashMap().get("Panel_Id_Label__c"),
							"Panel_Id_Label__c", type.stringType())
							.setHorizontalAlignment(HorizontalAlignment.RIGHT));
				}
				if(key.equals("TAB_Id__c")) {
					report.addColumn(col.column(
							this.isShowSummaryHeaders() ? "" : getFlightLineColumnLabelHashMap().get("TAB_Id__c"),
							"TAB_Id__c", type.stringType())
							.setHorizontalAlignment(HorizontalAlignment.RIGHT));
				}
				// TODO: network detail columns. implement "Media Product" column in salesforce object: PFL. Add formula field (Panel__r.Quattro_Media_Product__c). Add to fieldset
				if(key.equals("Quattro_Media_Product__c")) {
					report.addColumn(col.column(
							this.isShowSummaryHeaders() ? "" : getFlightLineColumnLabelHashMap().get("Quattro_Media_Product__c"),
							"Quattro_Media_Product__c", type.stringType())
							.setHorizontalAlignment(HorizontalAlignment.CENTER));
				}
				if(key.equals("Face_Direction__c")) {
					report.addColumn(col.column(
							this.isShowSummaryHeaders() ? "" : getFlightLineColumnLabelHashMap().get("Face_Direction__c"),
							"Face_Direction__c", type.stringType()).setHorizontalAlignment(HorizontalAlignment.LEFT));
				}
				if(key.equals("Unit_Size__c")) {
					report.addColumn(col.column(
							this.isShowSummaryHeaders() ? "" : getFlightLineColumnLabelHashMap().get("Unit_Size__c"),
							"Unit_Size__c", type.stringType()).setHorizontalAlignment(HorizontalAlignment.RIGHT));
				}
				if(key.equals("Weekly_Total_18_Imps__c")) {
					report.addColumn(col.column(
							this.isShowSummaryHeaders() ? "" : getFlightLineColumnLabelHashMap().get("Weekly_Total_18_Imps__c"),
							"Weekly_Total_18_Imps__c", type.integerType()).setPattern("#,###")
							.setHorizontalAlignment(HorizontalAlignment.RIGHT));
				}
				if(key.equals("In_Mkt_Imps__c")) {
					report.addColumn(col.column(
							this.isShowSummaryHeaders() ? "" : getFlightLineColumnLabelHashMap().get("In_Mkt_Imps__c"),
							"In_Mkt_Imps__c", type.doubleType()).setPattern("#,###")
							.setHorizontalAlignment(HorizontalAlignment.RIGHT));
				}
				if(key.equals("Total_Imps__c")) {
					report.addColumn(col.column(
							this.isShowSummaryHeaders() ? "" : getFlightLineColumnLabelHashMap().get("Total_Imps__c"),
							"Total_Imps__c", type.doubleType()).setPattern("#,###")
							.setHorizontalAlignment(HorizontalAlignment.RIGHT));
				}
				if(key.equals("In_Mkt_TRP__c")) {
					report.addColumn(col.column(
							this.isShowSummaryHeaders() ? "" : getFlightLineColumnLabelHashMap().get("In_Mkt_TRP__c"),
							"In_Mkt_TRP__c", type.doubleType()).setPattern("##0.0")
							.setHorizontalAlignment(HorizontalAlignment.RIGHT));
				}
				if(key.equals("PlanTRP__c")) {
					report.addColumn(col.column(
							this.isShowSummaryHeaders() ? "" : getFlightLineColumnLabelHashMap().get("PlanTRP__c"),
							"PlanTRP__c", type.doubleType()).setPattern("##0.0")
							.setHorizontalAlignment(HorizontalAlignment.RIGHT));
				}
				if(key.equals("Plan_Imps_Reach_Perc__c")) {
					report.addColumn(col
							.column(this.isShowSummaryHeaders() ? "" : getFlightLineColumnLabelHashMap().get("Plan_Imps_Reach_Perc__c"),
							"Plan_Imps_Reach_Perc__c", type.percentageType()).setPattern("##0.0")
							.setHorizontalAlignment(HorizontalAlignment.RIGHT));
				}
				if(key.equals("Plan_Imps_Avg_Frequency__c")) {
					report.addColumn(col.column(
							this.isShowSummaryHeaders() ? "" : getFlightLineColumnLabelHashMap().get("Plan_Imps_Avg_Frequency__c"),
							"Plan_Imps_Avg_Frequency__c", type.doubleType()).setPattern("##0.0")
							.setHorizontalAlignment(HorizontalAlignment.RIGHT));
				}
				if (key.equals("Illumination__c")) {
					report.addColumn(col.column(
							this.isShowSummaryHeaders() ? "" : getFlightLineColumnLabelHashMap().get("Illumination__c"),
							this.isShowSummaryHeaders() ? "OB_Summ_Num__c" : "Illumination__c",	type.bigDecimalType())
							.setHorizontalAlignment(HorizontalAlignment.RIGHT).setPattern("#,###").setWidth(Units.inch(1)));
				}
			}
			// ================================= add columns: end
			}
			return report;
		}

		public FlightLinePreviousRecordExpression getFlightLinePreviousRecordExpression() {
			return flightLinePreviousRecordExpression;
		}

		public void setFlightLinePreviousRecordExpression(
				FlightLinePreviousRecordExpression flightLinePreviousRecordExpression) {
			this.flightLinePreviousRecordExpression = flightLinePreviousRecordExpression;
		}

		public StyleBuilder getColumnTitleStyle() {
			return columnTitleStyle;
		}

		public void setColumnTitleStyle(StyleBuilder columnTitleStyle) {
			this.columnTitleStyle = columnTitleStyle;
		}

		public StyleBuilder getColumnStyle() {
			return columnStyle;
		}

		public void setColumnStyle(StyleBuilder columnStyle) {
			this.columnStyle = columnStyle;
		}

		public boolean isExportAsExcel() {
			return exportAsExcel;
		}

		public void setExportAsExcel(boolean exportAsExcel) {
			this.exportAsExcel = exportAsExcel;
		}

		public boolean isShowSummaryHeaders() {
			return showSummaryHeaders;
		}

		public void setShowSummaryHeaders(boolean showSummaryHeaders) {
			this.showSummaryHeaders = showSummaryHeaders;
		}
	}

	@SuppressWarnings("serial")
	private class FilterByDetailFlightLineIdExpression extends AbstractSimpleExpression<Boolean> {

		private FlightLinePreviousRecordExpression flightLinePreviousRecordExpression;

		public FilterByDetailFlightLineIdExpression(
				FlightLinePreviousRecordExpression flightLinePreviousRecordExpression) {
			System.out.println("   FilterByDetailFlightLineIdExpression()");
			setFlightLinePreviousRecordExpression(flightLinePreviousRecordExpression);
		}

		@Override
		public Boolean evaluate(ReportParameters reportParameters) {
			System.out.println("   FilterByDetailFlightLineIdExpression evaluate begin");

			String flightLineIdValue = getFlightLinePreviousRecordExpression().getId();

			String parentIdValue = reportParameters.getValue("Parent_Flight_Line__c");

			Boolean returnValue = parentIdValue.equals(flightLineIdValue);

			System.out.println("   FilterByDetailFlightLineIdExpression returnValue, flightLineIdValue, parentIdValue: " + returnValue + ", " + flightLineIdValue + ", " + parentIdValue);

			System.out.println("   FilterByDetailFlightLineIdExpression evaluate: end");

			return returnValue;
		}

		public FlightLinePreviousRecordExpression getFlightLinePreviousRecordExpression() {
			return flightLinePreviousRecordExpression;
		}

		public void setFlightLinePreviousRecordExpression(
				FlightLinePreviousRecordExpression flightLinePreviousRecordExpression) {
			this.flightLinePreviousRecordExpression = flightLinePreviousRecordExpression;
		}
	}

	private class FlightLinePreviousRecordExpression extends
			AbstractSimpleExpression<String> {
		private static final long serialVersionUID = 549979415L;

		private Integer m_reportRowNumber = 0;

		String m_lastIdValue = null;

		@Override
		public String evaluate(ReportParameters reportParameters) {

			System.out.println("FlightLinePreviousRecordExpression.evaluate");

			Integer reportRowNumber = reportParameters.getReportRowNumber();

			System.out.println("   reportRowNumber: " + reportRowNumber);

			if (reportRowNumber > this.m_reportRowNumber) {

				this.m_reportRowNumber = reportRowNumber;

				// try to get id value (if exception is raised that's because
				// the field doesn't exist)
				// so in this case just continue...
				try {
					System.out.println("in FlightLinePreviousRecordExpression.evaluate ... get Id field value.");
					String idValue = reportParameters.getValue("Id");
					this.m_lastIdValue = idValue;
					System.out.println("Id -> " + idValue);
				} catch (Exception e) {
					System.out.println("Couldn't get Id field value.");
					e.printStackTrace();
				}

			}

			return "id: " + this.getId();
		}

		public String getId() {
			return this.m_lastIdValue;
		}
	}

	@SuppressWarnings({ "serial", "unused" })
	private class FilterByFlightLineIdExpression extends
			AbstractSimpleExpression<Boolean> {

		@Override
		public Boolean evaluate(ReportParameters reportParameters) {

			String parentIdValue = reportParameters
					.getValue("Parent_Flight_Line__c");
			String buyTypeValue = reportParameters
					.getValue("Package_Flight__r/Type__c");

			System.out.println("----------------------");
			System.out.println("parentIdValue: " + parentIdValue);
			System.out.println("buyTypeValue: " + buyTypeValue);

			Boolean returnValue = buyTypeValue.equals("Network/Custom") && parentIdValue == null;

			System.out.println("returnValue: " + returnValue);

			return returnValue;
		}
	}
	
	private class ValueFormatter extends AbstractValueFormatter<String, String> {
		/**
		 * 
		 */
		private static final long serialVersionUID = 6709813022873698966L;

		public String format(String value, ReportParameters reportParameters) {
			System.out.println("ValueFormatter, evaluate(). value: " + value);
			try {
				// get current date as yyyy-mm-dd
				SimpleDateFormat parser = new SimpleDateFormat("yyyy-MM-dd");
				java.util.Date inputDate = parser.parse(value);
				SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy");
				System.out.println(formatter.format(inputDate));
				System.out.println("********* InputDate " + inputDate);
				System.out.println(" formatted date " + formatter.format(inputDate));
				return formatter.format(inputDate);
			} catch (ParseException e) {
				System.out.println(" Some exception occurred formatting date " + e);
				e.printStackTrace();
			}
			return null;	
		}
	}

	private class CurrencyWithoutFractionDecimalType extends BigDecimalType {
		private static final long serialVersionUID = 1L;

		@Override
		public String getPattern() {
			return "$#,###";
		}
	}
	private class CurrencyWithFractionDecimalType extends BigDecimalType {
		private static final long serialVersionUID = 1L;

		@Override
		public String getPattern() {
			return "$#,##0.00";
		}
	}
	
	private class FlightNameReportScriptlet extends AbstractScriptlet {

		@Override
		public void afterDetailEval(ReportParameters reportParameters) {
			super.afterDetailEval(reportParameters);
			String division = reportParameters.getValue("Package_Flight__r/Division__c");
			String flightName = reportParameters.getValue("Package_Flight__r/Name");
			String mediaCategory = "";
			try {
				mediaCategory = reportParameters.getValue("Package_Flight__r/Media_Category__c");
				// System.out.println(" FlightNameReportScriptlet mediaCategory " + mediaCategory);
			} catch (Exception e) {
				System.out.println(" exc getting flight line Media_Category__c " + e);
			}
			System.out.println(" FlightNameReportScriptlet calling valid disclaimers for flight division "	+ division + " media category " + mediaCategory);
			List<DisclaimerStore.DisclaimerWrapper> validDisclaimersList = disclaimerStore.getValidDisclaimers2(flightName, division, mediaCategory);
			System.out.println(" FlightNameReportScriptlet calling auto disclaimers ");
			Set<String> autoDisclaimerSet = disclaimerStore.getAutoDisclaimers(flightName, division, mediaCategory);
			validAllDisclaimersList.addAll(validDisclaimersList);
			autoDisclaimersSet.addAll(autoDisclaimerSet);
		}

		@Override
		public void afterGroupInit(String groupName,
				ReportParameters reportParameters) {
			super.afterGroupInit(groupName, reportParameters);
			for (String adiscl : autoDisclaimersSet) {
				System.out.println(" ******* auto disclaimers  " + adiscl);
			}
		}
	}

	@SuppressWarnings("serial")
	private class FilterByMarketIdExpression extends AbstractSimpleExpression<Boolean> {

		private PackageMarketFlightPreviousRecordExpression packageMarketFlightPreviousRecordExpression;

		public FilterByMarketIdExpression(PackageMarketFlightPreviousRecordExpression packageMarketFlightPreviousRecordExpression) {
			setPackageMarketFlightPreviousRecordExpression(packageMarketFlightPreviousRecordExpression);
		}

		@Override
		public Boolean evaluate(ReportParameters reportParameters) {

			// filter by market
			try {

				String lastMarketId = this.getPackageMarketFlightPreviousRecordExpression().getLastMarketId();

				String lastFlightId = this.getPackageMarketFlightPreviousRecordExpression().getId();

				String marketId = reportParameters.getValue("Id");

				System.out.println("FilterByMarketIdExpression.evaluate marketId, lastMarketId, lastFlightId = " + marketId + " " + lastMarketId + " " + lastFlightId);

				if(this.getPackageMarketFlightPreviousRecordExpression().isMarketIdChanged()) {
					return marketId.equals(lastMarketId);
				} else {
					return false;
				}

			} catch (Exception e) {
				System.out.println("FilterByMarketIdExpression.evaluate exception. ");
				e.printStackTrace();

				return false;
			}
		}

		public PackageMarketFlightPreviousRecordExpression getPackageMarketFlightPreviousRecordExpression() {
			return packageMarketFlightPreviousRecordExpression;
		}

		public void setPackageMarketFlightPreviousRecordExpression(
				PackageMarketFlightPreviousRecordExpression packageMarketFlightPreviousRecordExpression) {
			this.packageMarketFlightPreviousRecordExpression = packageMarketFlightPreviousRecordExpression;
		}
		
	}

	@SuppressWarnings("serial")
	private class FilterByPackageIdExpression extends AbstractSimpleExpression<Boolean> {

		private PackageMarketFlightPreviousRecordExpression packageMarketFlightPreviousRecordExpression;

		public FilterByPackageIdExpression(PackageMarketFlightPreviousRecordExpression packageMarketFlightPreviousRecordExpression) {
			setPackageMarketFlightPreviousRecordExpression(packageMarketFlightPreviousRecordExpression);
		}

		@Override
		public Boolean evaluate(ReportParameters reportParameters) {

			// filter by package
			try {

				String lastPackageId = this.getPackageMarketFlightPreviousRecordExpression().getLastPackageId();

				String lastFlightId = this.getPackageMarketFlightPreviousRecordExpression().getId();

				String packageId = reportParameters.getValue("Id");

				System.out.println("FilterByPackageIdExpression.evaluate packageId, lastPackageId, lastFlightId = " + packageId + " " + lastPackageId + " " + lastFlightId);

				if(this.getPackageMarketFlightPreviousRecordExpression().isPackageIdChanged()) {
					return packageId.equals(lastPackageId);
				} else {
					return false;
				}

			} catch (Exception e) {
				System.out.println("FilterByPackageIdExpression.evaluate exception. ");
				e.printStackTrace();

				return false;
			}
		}

		public PackageMarketFlightPreviousRecordExpression getPackageMarketFlightPreviousRecordExpression() {
			return packageMarketFlightPreviousRecordExpression;
		}

		public void setPackageMarketFlightPreviousRecordExpression(
				PackageMarketFlightPreviousRecordExpression packageMarketFlightPreviousRecordExpression) {
			this.packageMarketFlightPreviousRecordExpression = packageMarketFlightPreviousRecordExpression;
		}
		
	}

	private class FilterByFlightIdExpression extends
			AbstractSimpleExpression<Boolean> {
		private static final long serialVersionUID = 16566633224L;

		private PackageMarketFlightPreviousRecordExpression packageMarketFlightPreviousRecordExpression;

		public FilterByFlightIdExpression(
				PackageMarketFlightPreviousRecordExpression packageMarketFlightPreviousRecordExpression) {
			setPackageMarketFlightPreviousRecordExpression(packageMarketFlightPreviousRecordExpression);
		}

		@Override
		public Boolean evaluate(ReportParameters reportParameters) {
			return reportParameters.getValue("Package_Flight__r/Id").equals(
					getPackageMarketFlightPreviousRecordExpression().getId());
		}

		public PackageMarketFlightPreviousRecordExpression getPackageMarketFlightPreviousRecordExpression() {
			return packageMarketFlightPreviousRecordExpression;
		}

		public void setPackageMarketFlightPreviousRecordExpression(
				PackageMarketFlightPreviousRecordExpression packageMarketFlightPreviousRecordExpression) {
			this.packageMarketFlightPreviousRecordExpression = packageMarketFlightPreviousRecordExpression;
		}
	}
	
	private class PackageChangedExpression extends AbstractSimpleExpression<Boolean> {
		private static final long serialVersionUID = 2447421076494269295L;

		private PackageMarketFlightPreviousRecordExpression packageMarketFlightPreviousRecordExpression;
		
		private Boolean isPackageChanged = true;

		public PackageChangedExpression(
				PackageMarketFlightPreviousRecordExpression packageMarketFlightPreviousRecordExpression) {
			setPackageMarketFlightPreviousRecordExpression(packageMarketFlightPreviousRecordExpression);
		}

		@Override
		public Boolean evaluate(ReportParameters reportParameters) {
			Boolean returnValue = isPackageChanged;
			try {
				String lastPackageId = this.getPackageMarketFlightPreviousRecordExpression().getLastPackageId();

				String packageId = reportParameters.getValue("Package_Flight__r/Package_Market__r/Package__r/Id");

				if(this.getPackageMarketFlightPreviousRecordExpression().isPackageIdChanged()) {
					returnValue = isPackageChanged;
					isPackageChanged = !packageId.equals(lastPackageId);
				} else {
					isPackageChanged = false;
				}
			} catch (Exception e) {
				System.out.println("FilterByPackageNameExpression.evaluate exception. ");
				e.printStackTrace();
				returnValue = false;
			}
			return returnValue;
		}

		public PackageMarketFlightPreviousRecordExpression getPackageMarketFlightPreviousRecordExpression() {
			return packageMarketFlightPreviousRecordExpression;
		}

		public void setPackageMarketFlightPreviousRecordExpression(
				PackageMarketFlightPreviousRecordExpression packageMarketFlightPreviousRecordExpression) {
			this.packageMarketFlightPreviousRecordExpression = packageMarketFlightPreviousRecordExpression;
		}
	}

	private class DisclaimerSubreportExpression extends
			AbstractSimpleExpression<JasperReportBuilder> {

		private static final long serialVersionUID = 1L;

		@Override
		public JasperReportBuilder evaluate(ReportParameters reportParameters) {
			// PenBuilder grayThinLine = stl.penThin().setLineColor(Color.LIGHT_GRAY);

			PaddingBuilder padding = stl.padding(Units.inch(0.03));

			StyleBuilder columnStyle = stl.style().setPadding(padding)
			// .setBorder(grayThinLine)
					.setFontSize(8);

			JasperReportBuilder report = report();

			// create scriptlet that will concatenate every row
			DisclaimerSubreportScriptlet disclaimerSubreportScriptlet = new DisclaimerSubreportScriptlet();
			report.scriptlets(disclaimerSubreportScriptlet);

			// area for showing all joined disclaimers (come from scriptlet report)
			//report.pageFooter(cmp.text(new DisclaimerSubreportFooterExpressionColumn()).setFixedRows(10));

			// add field
			report.addField(field("Disclaimer__c", type.stringType()));
			report.setColumnStyle(columnStyle);
			return report;
		}
	}

	private class DisclaimerSubreportScriptlet extends AbstractScriptlet {
		// anjali disclaimer change
		// private List<String> disclaimerList = new ArrayList<String>();
		private Set<String> disclaimerList = new HashSet<String>();

		private DisclaimerSubreportScriptlet() {
			super("DisclaimerSubreportScriptlet");
		}

		@Override
		public void afterDetailEval(ReportParameters reportParameters) {
			super.afterDetailEval(reportParameters);
			String item = reportParameters.getValue("Disclaimer__c");
			if (item != null) {
				disclaimerList.add(item);
			}
		}
	}

	private class ShippingInstructionsSubreportExpression extends
			AbstractSimpleExpression<JasperReportBuilder> {

		public ShippingInstructionsSubreportExpression(Reporter reporter) {
			super();
			this.setReporter(reporter);
		}

		private static final long serialVersionUID = 8861654532L;

		private Reporter reporter;

		@Override
		public JasperReportBuilder evaluate(ReportParameters reportParameters) {

			System.out
					.println("   ShippingInstructionsSubreportExpression... getPageNumber(): "
							+ reportParameters.getPageNumber());

			// pass page number to reporter
			this.getReporter().setShippingInstructionsPageNumber(
					reportParameters.getPageNumber());

			// styles
			/* StyleBuilder boldStyle = stl.style().bold();
			StyleBuilder pageHeaderStyle = stl.style(boldStyle).setFontSize(10);
			StyleBuilder columnTitleStyle = stl.style().setFontSize(8);
			StyleBuilder columnStyle = stl.style(boldStyle).setFontSize(8); */

			// styles
			StyleBuilder boldStyle = stl.style().bold().setFontSize(9)/*.setFontName("Arial")*/;
			StyleBuilder pageHeaderStyle = stl.style(boldStyle).setFontSize(10);
			StyleBuilder columnTitleStyle = stl.style().setFontSize(9)/*.setFontName("Arial")*/;

			// field reference
			FieldBuilder<String> nameField = field("Name",
					type.stringType());
			FieldBuilder<String> addresseeField = field("Addressee__c",
					type.stringType());
			FieldBuilder<String> attentionField = field("Attention__c",
					type.stringType());
			FieldBuilder<String> address1Field = field("Address1__c",
					type.stringType());
			FieldBuilder<String> address2Field = field("Address2__c",
					type.stringType());
			FieldBuilder<String> cityField = field("City__c", type.stringType());
			FieldBuilder<String> stateField = field("State__c",
					type.stringType());
			FieldBuilder<String> zipCodeField = field("Zip_Code__c",
					type.stringType());
			FieldBuilder<String> countryField = field("Country__c",
					type.stringType());
			FieldBuilder<String> phoneField = field("Phone__c",
					type.stringType());
			FieldBuilder<String> receiving_TimeField = field(
					"Receiving_Time__c", type.stringType());
			FieldBuilder<String> notesField = field("Notes__c",
					type.stringType());

			// header list at the top for division and addressee
			VerticalListBuilder headerList = cmp.verticalList(cmp.text(""), cmp
					.text(nameField).setStyle(boldStyle), cmp
					.text(addresseeField).setStyle(columnTitleStyle));

			// rest of the fields
			VerticalListBuilder nameList = cmp.verticalList(
					cmp.text("Attention:").setStyle(columnTitleStyle) // attentionField
					, cmp.text("Address:").setStyle(columnTitleStyle) // address1Field
					, cmp.text("").setStyle(columnTitleStyle) // address2Field
					, cmp.text("").setStyle(columnTitleStyle) // cityField
																// stateField
																// zipCodeField
					, cmp.text("").setStyle(columnTitleStyle) //
					, cmp.text("Phone:").setStyle(columnTitleStyle) // phoneField
					, cmp.text("Time:").setStyle(columnTitleStyle) // receiving_TimeField
					, cmp.text("Note:").setStyle(columnTitleStyle) // notesField
					);
			VerticalListBuilder valueList = cmp.verticalList();

			// .setStyle(columnStyle)
			valueList.add(cmp.text(attentionField).setStyle(columnTitleStyle)); // Attention:
			valueList.add(cmp.text(address1Field).setStyle(columnTitleStyle)); // Address:
			valueList.add(cmp.text(address2Field).setStyle(columnTitleStyle)); //
			valueList.add(cmp.horizontalList(
					cmp.text(cityField).setStyle(columnTitleStyle)
							.setFixedWidth(Units.cm(2)),
					cmp.text(stateField).setStyle(columnTitleStyle)
							.setFixedWidth(Units.cm(1)),
					cmp.text(zipCodeField).setStyle(columnTitleStyle)
							.setFixedWidth(Units.cm(4))));
			valueList.add(cmp.text(countryField).setStyle(columnTitleStyle)); // Phone:
			valueList.add(cmp.text(phoneField).setStyle(columnTitleStyle)); // Phone:
			valueList.add(cmp.text(receiving_TimeField).setStyle(
					columnTitleStyle)); // Time:
			valueList.add(cmp.text(notesField).setStyle(columnTitleStyle)); // Note:

			// create a column for names and another column for values
			ComponentColumnBuilder nameColumn = col.componentColumn(nameList)
					.setFixedWidth(Units.inch(1));
			ComponentColumnBuilder valueColumn = col.componentColumn(valueList);

			// create report definition
			JasperReportBuilder report = report();
			try {
				report.setTitleOnANewPage(true)
						.addPageHeader(
								cmp.text("Shipping Instructions").setStyle(
										pageHeaderStyle))
						.addDetailHeader(headerList)
						.columns(nameColumn, valueColumn);
			} catch (Exception ex) {
				System.out.println(" Some error occurred while creating shipping instructions sub report " +  ex.getMessage());
				ex.printStackTrace();
			}

			// return report
			return report;
		}

		public Reporter getReporter() {
			return reporter;
		}

		public void setReporter(Reporter reporter) {
			this.reporter = reporter;
		}
	}

	private VerticalListBuilder createVerticalTable(ReportStyleBuilder groupHeaderStyle, ReportStyleBuilder groupValueStyle,
			String[] fieldNamesArray1, String[] fieldLabelsArray1,
			String[] fieldNamesArray2, String[] fieldLabelsArray2, Integer leftColumnWidth, String packageId) {
		VerticalListBuilder verticalList;
		verticalList = cmp.verticalList();
		for (Integer index = 0; index < fieldNamesArray1.length; index++) {
			System.out.println("      creating vertical table element. label: "
					+ fieldLabelsArray1[index] + " name: " + fieldNamesArray1[index]);
			verticalList.add(createHorizontalKeyValueList(
					fieldLabelsArray1[index], new ValueExpression(packageId, fieldNamesArray1[index]),
					fieldLabelsArray2[index], new ValueExpression(packageId, fieldNamesArray2[index]),
					groupHeaderStyle, groupValueStyle, leftColumnWidth));
		}
		verticalList.add(cmp.text(""));
		return verticalList;
	}

	private void createPackageCommentsAndDisclamersFooter(String dataSourceFileName, JasperReportBuilder report) {
		JRXmlDataSource packageFooterDataSource = null;
		try {
			packageFooterDataSource = new JRXmlDataSource(dataSourceFileName, QUERY_RESULT_RECORDS_UNIQUE_FLIGHT);
		} catch (JRException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		SubreportBuilder packageFooterSubreport = cmp
				.subreport(new PackageFooterSubreportExpression())
				.setDataSource(packageFooterDataSource)
				.removeLineWhenBlank();
		report.summary(packageFooterSubreport);
	}
	
	private VerticalListBuilder createCommentTable(String packageId) {
		VerticalListBuilder verticalList;
		verticalList = cmp.verticalList();
		//for (String packageId : packageIds) {
			Set<String> packageNames = packageNamesMap.get(packageId);
			Set<String> packageComments = packageCommentsMap.get(packageId);
			List<String> flightNames = flightNamesMap.get(packageId);
			List<String> flightComments = flightCommentsMap.get(packageId);
			if(!(packageComments.isEmpty() || flightComments.isEmpty())) {
				verticalList.add(cmp.text("Comments:").setStyle(stl.style().bold()).removeLineWhenBlank());
				Iterator<String> packageNamesIterator = packageNames.iterator();
				Iterator<String> packageCommentsIterator = packageComments.iterator();
				Iterator<String> flightNamesIterator = flightNames.iterator();
				Iterator<String> flightCommentsIterator = flightComments.iterator();
				while (packageCommentsIterator.hasNext()) {
					String packageName = (String) packageNamesIterator.next();
					String packageComment = (String) packageCommentsIterator.next();
					verticalList.add(cmp.text(packageName).setStyle(stl.style().bold()).removeLineWhenBlank());
					verticalList.add(cmp.text(packageComment).removeLineWhenBlank());
					while (flightCommentsIterator.hasNext()) {
						String flightName = (String) flightNamesIterator.next();
						String flightComment = (String) flightCommentsIterator.next();
						if(StringUtils.isNotEmpty(flightComment)) {
							verticalList.add(cmp.text(flightName).setStyle(stl.style().bold()).removeLineWhenBlank());
							verticalList.add(cmp.text(flightComment).removeLineWhenBlank());
						}
					}
				}
			}
		//}
		return verticalList;
	}

	private class DisclaimerExpression extends AbstractSimpleExpression<Object> {
		private static final long serialVersionUID = -2468924311042394628L;

		public DisclaimerExpression() {

		}

		public Object evaluate(ReportParameters reportParameters) {
			System.out.println("******** DisclaimerExpression evaluate. getFieldName():validAllDisclaimersList " + validAllDisclaimersList.size() );
			
			String str = "";
			Collections.sort(validAllDisclaimersList);
			  
			for (DisclaimerStore.DisclaimerWrapper discWrapper : validAllDisclaimersList) {
				System.out.println("******** DisclaimerExpression : valid disc " + discWrapper.sequenceInt + " disclaimer " + discWrapper.disclaimerText);
				if(!validDisclaimersListDisplay.contains(discWrapper.disclaimerText)) {
					validDisclaimersListDisplay.add(discWrapper.disclaimerText);
				}
			}
			
			for (String disc : validDisclaimersListDisplay) {
				System.out.println("******** DisclaimerExpression :valid disc " + disc);
				str += disc.trim();
				str += "  ";
			}
			System.out.println("******** disc string " + str + "  length = " + str.length());
			//str =  str == "" ? "" : str.substring(0, str.length() - 2);
			System.out.println("******** valid disc string " + str);
			return str;
		}
	}

	private class AutoDisclaimerExpression extends
			AbstractSimpleExpression<Object> {
		private static final long serialVersionUID = -2468924311042394628L;

		public AutoDisclaimerExpression() {

		}

		public Object evaluate(ReportParameters reportParameters) {
			System.out
					.println("******** AutoDisclaimerExpression evaluate. getFieldName():autoDisclaimersSet  "
							+ autoDisclaimersSet.size());
			String str = "";
			for (String disc : autoDisclaimersSet) {
				System.out.println("******** Auto disc " + disc);
				str += disc.trim();
				str += "  ";
			}
			//str =  str == "" ? "" : str.substring(0, str.length() - 2);
			System.out.println("******** Auto disc " + str);
			return str;
			// return StringUtils.join(autoDisclaimersSet, ',');

		}
	}

	private class ValueExpression extends AbstractSimpleExpression<Object> {
		private static final long serialVersionUID = -2468924311042394628L;

		// store field name
		private String m_fieldName;
		//store package id
		private String m_packageId;

		// create value expression for getting a value by field name, for package id
		public ValueExpression(String packageId, String fieldName) {
			this.setPackageId(packageId);
			this.setFieldName(fieldName);
		}

		public Object evaluate(ReportParameters reportParameters) {
			System.out.println("evaluate. getFieldName(): " + this.getFieldName());
			// reportParameters.
			Object returnValue = "";
			try {
				if (this.getFieldName() == null || this.getFieldName() == "") {
					return "";
				} else if (this.getFieldName() == "Type__c") {
					returnValue = StringUtils.join(flightTypesMap.get(getPackageId()), ", ");
				} else if (this.getFieldName() == "Division__c") {
					returnValue = StringUtils.join(divisionsMap.get(getPackageId()), ", ");
				} else if (this.getFieldName() == "Package_Name__c") {
					returnValue = StringUtils.join(packageNamesMap.get(getPackageId()), ", ");
				} else if (this.getFieldName() == "Market_Name__c") {
					returnValue = StringUtils.join(marketNamesMap.get(getPackageId()), ", ");
				} else if (this.getFieldName() == "Market_Type__c") {
					returnValue = StringUtils.join(marketTypesMap.get(getPackageId()), ", ");
				} else if (this.getFieldName() == "Name") {
					returnValue = StringUtils.join(flightNamesMap.get(getPackageId()), ", ");
				} else if (this.getFieldName() == "Campaign_Start_Date__c") {
					returnValue = StringUtils.join(startDatesMap.get(getPackageId()), ", ");
				} else if (this.getFieldName() == "Campaign_End_Date__c") {
					returnValue = StringUtils.join(endDatesMap.get(getPackageId()), ", ");
				} else if (this.getFieldName() == "Duration_And_Type__c") {
					returnValue = StringUtils.join(durationsMap.get(getPackageId()), ", ");
				} else if (this.getFieldName() == "Target__c") {
					returnValue = StringUtils.join(targetsMap.get(getPackageId()), ", ");
				} else if (this.getFieldName() == "Target_Population__c") {
					returnValue = StringUtils.join(targetPopulationsMap.get(getPackageId()), ", ");
				} else if (this.getFieldName() == "Flight_Comments__c") {
					returnValue = StringUtils.join(flightCommentsMap.get(getPackageId()), ", ");
				} else if (this.getFieldName() == "Package_Comments__c") {
					returnValue = StringUtils.join(packageCommentsMap.get(getPackageId()), ", ");
				} else {
					returnValue = reportParameters.getValue(this.getFieldName());
				}
				System.out.println(" returnValue " + returnValue);
			} catch (Exception ex) {
				System.out.println("   Some exception trying to get field " + this.getFieldName()  + " message: " + ex.getMessage());
			}
			return returnValue;
		}

		public String getFieldName() {
			return m_fieldName;
		}

		public void setFieldName(String m_fieldName) {
			this.m_fieldName = m_fieldName;
		}

		public String getPackageId() {
			return m_packageId;
		}

		public void setPackageId(String m_packageId) {
			this.m_packageId = m_packageId;
		}
	}

	/**
	 * Create a horizontal list
	 */
	private HorizontalListBuilder createHorizontalKeyValueList(
			String text1, ValueExpression valueExpression1,
			String text2, ValueExpression valueExpression2,
			ReportStyleBuilder groupHeaderStyle, ReportStyleBuilder groupValueStyle, Integer leftColumnWidth) {
		HorizontalListBuilder horizontalList = cmp.horizontalList();
		horizontalList.add(cmp.text(text1).setStyle(groupHeaderStyle).setFixedWidth(leftColumnWidth));
		horizontalList.add(cmp.text(valueExpression1).setStyle(groupValueStyle));
		horizontalList.add(cmp.text(text2).setStyle(groupHeaderStyle).setFixedWidth(leftColumnWidth));
		horizontalList.add(cmp.text(valueExpression2).setStyle(groupValueStyle));

		return horizontalList;
	}

	/*
	 * Group expression for Flight Id
	 */
	private class FlightIdGroupExpression extends
			AbstractSimpleExpression<String> {
		private static final long serialVersionUID = 8512811414598238519L;

		@Override
		public String evaluate(ReportParameters reportParameters) {
			try {
				// NOTICE: group by name because we could loose TOC if grouping by id. It means that for
				// generating TOC we need the names. If we put ID, this ID value would
				// be shown in the TOC.
				return " " + reportParameters.getValue("Package_Flight__r/Name") +
						"................................................................................"
						+ "$~" + reportParameters.getValue("Package_Flight__r/Package_Name__c");

			} catch (Exception ex) {
				System.out.println(ex.getStackTrace());
			}
			return null;
		}
	}
	
	private class FlightDataExpression extends AbstractSimpleExpression<Integer> {

		private static final long serialVersionUID = 5879536463855991534L;

		public FlightDataExpression(JRXmlDataSource jrXmlDataSource) throws Exception {
			
			String packageId = "";
			String buyType = "";
			String division = "";
			String packageName = "";
			String marketName = "";
			String marketType = "";
			String flightName = "";
			String startDate = "";
			String endDate = "";
			String duration = "";
			String target = "";
			String targetPopulation = "";
			String flightComment = "";
			String packageComment = "";
			
			Set<String> divisions = null;
			Set<String> marketNames = null;
			Set<String> marketTypes = null;
			Set<String> packageNames = null;
			List<String> flightNames = null;
			Set<String> flightTypes = null;
			Set<String> startDates = null;
			Set<String> endDates = null;
			Set<String> durations = null;
			Set<String> targets = null;
			Set<String> targetPopulations = null;
			List<String> flightComments = null;
			Set<String> packageComments = null;

			while(jrXmlDataSource.next()) {
				Document document = jrXmlDataSource.subDocument();
				
				// optional, but recommended read this - http://stackoverflow.com/questions/13786607/normalization-in-dom-parsing-with-java-how-does-it-work
				document.getDocumentElement().normalize();
	
				System.out.println("Root element :" + document.getDocumentElement().getNodeName());
	
				NodeList flightRecordsList = document.getElementsByTagName("Package_Flight__r");
	
				// get current date as yyyy-mm-dd
				SimpleDateFormat parser = new SimpleDateFormat("yyyy-MM-dd");
				// get current date as MM/dd/yyyy
				SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy");
				
				DecimalFormat df = new DecimalFormat("#,###");
				
				if (flightRecordsList != null && flightRecordsList.getLength() > 0) {
					for (int i = 0; i < flightRecordsList.getLength(); i++) {
						Node nNode = flightRecordsList.item(i);
						System.out.println("\nCurrent Element :" + nNode.getNodeName());
						
						packageId = "";
						buyType = "";
						division = "";
						packageName = "";
						marketName = "";
						marketType = "";
						flightName = "";
						startDate = "";
						endDate = "";
						duration = "";
						target = "";
						targetPopulation = "";
						flightComment = "";
						packageComment = "";
						
						if (nNode.getNodeType() == Node.ELEMENT_NODE) {
							
							Element eElement = (Element) nNode;
	
							NodeList marketList = eElement.getElementsByTagName("Package_Market__r");
							if (marketList != null && marketList.getLength() > 0) {
								Node mNode = marketList.item(0);
								System.out.println("\nCurrent Element :" + mNode.getNodeName());
								if (mNode.getNodeType() == Node.ELEMENT_NODE) {
									Element mElement = (Element) mNode;
									NodeList packageList = mElement.getElementsByTagName("Package__r");
									if (packageList != null && packageList.getLength() > 0) {
										Node pNode = packageList.item(0);
										System.out.println("\nCurrent Element :" + pNode.getNodeName());
										if (pNode.getNodeType() == Node.ELEMENT_NODE) {
											Element gElement = (Element) pNode;
											NodeList idList = gElement.getElementsByTagName("Id");
											if (idList != null && idList.getLength() > 0) {
												packageId = idList.item(0).getTextContent();
												packageIds.add(packageId);
												System.out.println("\nPackage Id :" + packageId);
											}
										}
									}
								}
							}
							divisions = divisionsMap.get(packageId) != null ? divisionsMap.get(packageId) : new LinkedHashSet<String>();
							marketNames = marketNamesMap.get(packageId) != null ? marketNamesMap.get(packageId) : new LinkedHashSet<String>();
							marketTypes = marketTypesMap.get(packageId) != null ? marketTypesMap.get(packageId) : new LinkedHashSet<String>();
							packageNames = packageNamesMap.get(packageId) != null ? packageNamesMap.get(packageId) : new LinkedHashSet<String>();
							flightNames = flightNamesMap.get(packageId) != null ? flightNamesMap.get(packageId) : new LinkedList<String>();
							flightTypes = flightTypesMap.get(packageId) != null ? flightTypesMap.get(packageId) : new LinkedHashSet<String>();
							startDates = startDatesMap.get(packageId) != null ? startDatesMap.get(packageId) : new LinkedHashSet<String>();
							endDates = endDatesMap.get(packageId) != null ? endDatesMap.get(packageId) : new LinkedHashSet<String>();
							durations = durationsMap.get(packageId) != null ? durationsMap.get(packageId) : new LinkedHashSet<String>();
							targets = targetsMap.get(packageId) != null ? targetsMap.get(packageId) : new LinkedHashSet<String>();
							targetPopulations = targetPopulationsMap.get(packageId) != null ? targetPopulationsMap.get(packageId) : new LinkedHashSet<String>();
							flightComments = flightCommentsMap.get(packageId) != null ? flightCommentsMap.get(packageId) : new LinkedList<String>();
							packageComments = packageCommentsMap.get(packageId) != null ? packageCommentsMap.get(packageId) : new LinkedHashSet<String>();
							
							NodeList buyTypeList = eElement.getElementsByTagName("Type__c");
							if (buyTypeList != null && buyTypeList.getLength() > 0) {
								buyType = buyTypeList.item(0).getTextContent();
								flightTypes.add(buyType);
							}
							NodeList divisionList = eElement.getElementsByTagName("Division__c");
							if (divisionList != null && divisionList.getLength() > 0) {
								division = divisionList.item(0).getTextContent();
								divisions.add(division);
							}
							NodeList packageNameList = eElement.getElementsByTagName("Package_Name__c");
							if (packageNameList != null && packageNameList.getLength() > 0) {
								packageName = packageNameList.item(0).getTextContent();
								packageNames.add(packageName);
							}
							NodeList marketNameList = eElement.getElementsByTagName("Market_Name__c");
							if (marketNameList != null && marketNameList.getLength() > 0) {
								marketName = marketNameList.item(0).getTextContent();
								marketNames.add(marketName);
							}
							NodeList marketTypeList = eElement.getElementsByTagName("Market_Type__c");
							if (marketTypeList != null && marketTypeList.getLength() > 0) {
								marketType = marketTypeList.item(0).getTextContent();
								marketTypes.add(marketType);
							}
							NodeList flightNameList = eElement.getElementsByTagName("Name");
							if (flightNameList != null && flightNameList.getLength() > 0) {
								flightName = flightNameList.item(0).getTextContent();
								flightNames.add(flightName);
							}
							NodeList startDateList = eElement.getElementsByTagName("Campaign_Start_Date__c");
							if (startDateList != null && startDateList.getLength() > 0) {
								startDate = formatter.format(parser.parse(startDateList.item(0).getTextContent()));
								startDates.add(startDate);
							}
							NodeList endDateList = eElement.getElementsByTagName("Campaign_End_Date__c");
							if (endDateList != null && endDateList.getLength() > 0) {
								endDate = formatter.format(parser.parse(endDateList.item(0).getTextContent()));
								endDates.add(endDate);
							}
							NodeList durationList = eElement.getElementsByTagName("Duration_And_Type__c");
							if (durationList != null && durationList.getLength() > 0) {
								duration = durationList.item(0).getTextContent();
								durations.add(duration);
							}
							NodeList targetList = eElement.getElementsByTagName("Target__c");
							if (targetList != null && targetList.getLength() > 0) {
								target = targetList.item(0).getTextContent();
								targets.add(target);
							}
							NodeList targetPopulationList = eElement.getElementsByTagName("Target_Population__c");
							if (targetPopulationList != null && targetPopulationList.getLength() > 0) {
								Double temp = Double.parseDouble(targetPopulationList.item(0).getTextContent());
								if(temp != null) {
									targetPopulations.add(df.format(temp));
								}
							}
							NodeList flightCommentsList = eElement.getElementsByTagName("Flight_Comments__c");
							if (flightCommentsList != null && flightCommentsList.getLength() > 0) {
								flightComment = flightCommentsList.item(0).getTextContent();
								flightComments.add(flightComment);
							}
							NodeList packageCommentsList = eElement.getElementsByTagName("Package_Comments__c");
							if (packageCommentsList != null && packageCommentsList.getLength() > 0) {
								packageComment = packageCommentsList.item(0).getTextContent();
								packageComments.add(packageComment);
							}
							divisionsMap.put(packageId, divisions);
							marketNamesMap.put(packageId, marketNames);
							marketTypesMap.put(packageId, marketTypes);
							packageNamesMap.put(packageId, packageNames);
							flightNamesMap.put(packageId, flightNames);
							flightTypesMap.put(packageId, flightTypes);
							startDatesMap.put(packageId, startDates);
							endDatesMap.put(packageId, endDates);
							durationsMap.put(packageId, durations);
							targetsMap.put(packageId, targets);
							targetPopulationsMap.put(packageId, targetPopulations);
							flightCommentsMap.put(packageId, flightComments);
							packageCommentsMap.put(packageId, packageComments);
							
							System.out.println(" ********** creating flight header object : " +
									"buyType " + buyType + 
									"division " + division + 
									"packageName " + packageName + 
									"marketName " + marketName + 
									"marketType " + marketType + 
									"flightName " + flightName + 
									"startDate " + startDate + 
									"endDate " + endDate + 
									"duration " + duration + 
									"target" + target + 
									"targetPopulation " + targetPopulation);
						}
					}
				}
			}
		}

		@Override
		public Integer evaluate(ReportParameters reportParameters) {
			System.out.println(" DisclaimersDataExpression evaluate ");
			return null;
		}
	}

	public class DisclaimersDataExpression extends
			AbstractSimpleExpression<Integer> {

		private static final long serialVersionUID = 616516564L;

		// private Map<String, List<DisclaimerWrapper>> disclaimersMetadataMap =
		// new HashMap<String, List<DisclaimerWrapper>> ();

		// Disclaimer__c ,p.Proposal_Type__c, p.Output_Location__c, p.Notes__c,
		// p.Media_Types__c, p.Markets__c, p.Division__c From
		// //Proposal_Disclaimer__c p order by p.Output_Location__c

		public DisclaimersDataExpression(String disclaimerDataSourceFileName)
				throws ParserConfigurationException, SAXException, IOException,
				ParseException {
			File fXmlFile = new File(disclaimerDataSourceFileName);
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(fXmlFile);
			String division = "";
			String disclaimerText = "";
			String outputLocation = "";
			String mediaType = "";
			String proposalType = "";
			String notes = "";
			String sequence = "";
			String shippingInstr ="";
			

			// optional, but recommended
			// read this -
			// http://stackoverflow.com/questions/13786607/normalization-in-dom-parsing-with-java-how-does-it-work
			doc.getDocumentElement().normalize();

			System.out.println("Root element :"
					+ doc.getDocumentElement().getNodeName());

			NodeList disclaimerRecordsList = doc
					.getElementsByTagName("records");

			// System.out.println("----------Total disclaimers ------------------"
			// + disclaimerRecordsList.getLength());

			// mapPanelOrderPrefHashtable = new LinkedHashMap<String,
			// Integer>();
			if (disclaimerRecordsList != null
					&& disclaimerRecordsList.getLength() > 0) {
				for (int i = 0; i < disclaimerRecordsList.getLength(); i++) {

					Node nNode = disclaimerRecordsList.item(i);
					 disclaimerText = "";
					 outputLocation = "";
					 mediaType = "";
					 division = "";
				    	proposalType = "";
					 notes = "";
					sequence = "";
					shippingInstr ="";
					// System.out.println("\nCurrent Element :" +
					// nNode.getNodeName());

					if (nNode.getNodeType() == Node.ELEMENT_NODE) {
						// System.out.println("=====================");

						Element eElement = (Element) nNode;

						NodeList dcList = eElement
								.getElementsByTagName("Disclaimer__c");
						if (dcList != null && dcList.getLength() > 0) {
							disclaimerText = dcList.item(0).getTextContent();
						}
						NodeList opList = eElement
								.getElementsByTagName("Output_Location__c");
						if (opList != null && opList.getLength() > 0) {
							outputLocation = opList.item(0).getTextContent();
						}
						NodeList medCatList = eElement
								.getElementsByTagName("Media_Types__c");
						if (medCatList != null && medCatList.getLength() > 0) {
							mediaType = medCatList.item(0).getTextContent();
						}
						NodeList propList = eElement
								.getElementsByTagName("Proposal_Type__c");
						if (propList != null && propList.getLength() > 0) {
							proposalType = propList.item(0).getTextContent();
						}
						NodeList mkList = eElement
								.getElementsByTagName("Division__c");
						if (mkList != null && mkList.getLength() > 0) {
							division = mkList.item(0).getTextContent();
						}
						NodeList ntList = eElement
								.getElementsByTagName("Notes__c");
						if (ntList != null && ntList.getLength() > 0) {
							notes = ntList.item(0).getTextContent();
						}

						NodeList seqList = eElement
								.getElementsByTagName("Sequence__c");
						if (seqList != null && seqList.getLength() > 0) {
							sequence = seqList.item(0).getTextContent();
						}
						NodeList shippingInstList = eElement
								.getElementsByTagName("isShippingInstruction__c");
						if (shippingInstList != null && shippingInstList.getLength() > 0) {
							shippingInstr = shippingInstList.item(0).getTextContent();
						}
						System.out
								.println(" ********** creating disclaimer wrapper object: "
										+ " Seq "
										+ sequence
										+ " Disclaimer Txt: "
										+ disclaimerText
										+ " mediaType "
										+ mediaType
										+ " division "
										+ division
										+ " shippingInstr " + shippingInstr);

						disclaimerStore.createDisclaimerWrapper(disclaimerText,
								outputLocation, proposalType, division,
								mediaType, notes, sequence, shippingInstr);

					}
				}
				System.out
						.println(" ************  disclaimerStore disclaimerlist length "
								+ disclaimerStore.getDisclaimersList().size());
			}
		}

		@Override
		public Integer evaluate(ReportParameters reportParameters) {
			System.out.println(" DisclaimersDataExpression evaluate ");
			return null;
		}
	}

	private class MapLocationNumberExpressionColumn extends
			AbstractSimpleExpression<Integer> {

		private static final long serialVersionUID = 616516564L;

		private LinkedHashMap<String, Integer> mapPanelOrderPrefHashtable = null;

		public MapLocationNumberExpressionColumn(
				String mapPanelOrderPrefDataSourceFileName)
				throws ParserConfigurationException, SAXException, IOException,
				ParseException {
			File fXmlFile = new File(mapPanelOrderPrefDataSourceFileName);
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(fXmlFile);

			// optional, but recommended read this - http://stackoverflow.com/questions/13786607/normalization-in-dom-parsing-with-java-how-does-it-work
			doc.getDocumentElement().normalize();

			System.out.println("Root element :"
					+ doc.getDocumentElement().getNodeName());

			NodeList nList = doc.getElementsByTagName("records");

			System.out.println("----------------------------");

			mapPanelOrderPrefHashtable = new LinkedHashMap<String, Integer>();

			for (int temp = 0; temp < nList.getLength(); temp++) {

				Node nNode = nList.item(temp);

				System.out.println("\nCurrent Element :" + nNode.getNodeName());

				if (nNode.getNodeType() == Node.ELEMENT_NODE) {

					Element eElement = (Element) nNode;

					// System.out.println("Staff id : " +
					// eElement.getAttribute("id"));
					System.out.println("Sort_Sequence__c: "
							+ eElement.getElementsByTagName("Sort_Sequence__c")
									.item(0).getTextContent());
					System.out.println("Flight__c: "
							+ eElement.getElementsByTagName("Flight__c")
									.item(0).getTextContent());
					System.out.println("Panel__c: "
							+ eElement.getElementsByTagName("Panel__c").item(0)
									.getTextContent());

					String key = eElement.getElementsByTagName("Flight__c")
							.item(0).getTextContent()
							+ " "
							+ eElement.getElementsByTagName("Panel__c").item(0)
									.getTextContent();

					String stringValue = eElement
							.getElementsByTagName("Sort_Sequence__c").item(0)
							.getTextContent();

					// Use NumberFormat to format and parse numbers for the US
					// locale
					NumberFormat nf = NumberFormat.getNumberInstance(Locale.US); // Get
																					// a
																					// NumberFormat
					Number n = nf.parse(stringValue); // Parse strings according
														// to locale

					// set value
					Integer value = (int) Math.round(n.doubleValue());
					System.out.println("   value: " + value);

					mapPanelOrderPrefHashtable.put(key, n.intValue());
				}
			}
		}

		@Override
		public Integer evaluate(ReportParameters reportParameters) {
			String key = reportParameters.getValue("MapLocation_Number__c");

			System.out.println("      searching MapLocation_Number__c key"
					+ key);
			Integer integerValue = this.mapPanelOrderPrefHashtable.get(key);

			if (integerValue != null) {
				System.out.println("      found integerValue: " + integerValue);
				// return String.valueOf(integerValue);
				return integerValue;
			}

			System.out.println("      key not found " + integerValue);
			return null;
		}
	}

	/**
	 * Add a level of data to the report
	 * 
	 * @throws ParseException
	 * @throws IOException
	 * @throws SAXException
	 * @throws ParserConfigurationException
	 */
	private void addDataLevel(JasperReportBuilder b,
			PDFCombinerFile pdfCombinerFile, Integer levelIndex,
			Boolean showTotalProgramSummary,
			Boolean showIndividualMarketSummary,
			Boolean showIndividualFlightSummary,
			String mapPanelOrderPrefDataSourceFileName,
			StyleBuilder columnTitleStyle) throws ParserConfigurationException,
			SAXException, IOException, ParseException {
		// ================ add columns
		String[] fieldNamesArray = pdfCombinerFile.getFieldNamesPipeDelimited()
				.get(levelIndex).split("\\|");
		String[] fieldLabelsArray = pdfCombinerFile
				.getFieldLabelsPipeDelimited().get(levelIndex).split("\\|");
		String[] fieldTypesArray = pdfCombinerFile.getFieldTypesPipeDelimited()
				.get(levelIndex).split("\\|");

		boolean isDetail = levelIndex == 0;

		// core summary columns that prescence will be detected in the field
		// name list.
		// if this columns exist and a switch to show summaries is set,
		// then a summary for that column will be shown at the report.
		TextColumnBuilder<String> marketNameColumn = null;
		TextColumnBuilder<String> flightNameColumn = null;
		TextColumnBuilder<Double> weekly18Column = null;
		TextColumnBuilder<Double> target_In_Market_ImpsColumn = null;
		TextColumnBuilder<Double> total_ImpsColumn = null;
		TextColumnBuilder<Double> weekly_TRPColumn = null;
		TextColumnBuilder<Double> planTRPColumn = null;
		TextColumnBuilder<Double> plan_Imps_Reach_PercColumn = null;
		TextColumnBuilder<Double> frequencyColumn = null;
		TextColumnBuilder<Double> total_PriceColumn = null;
		TextColumnBuilder<Double> totalInMarketCPMColumn = null;
		TextColumnBuilder<Double> cppColumn = null;
		// for each field name
		for (Integer fieldIndex = 0; fieldIndex < fieldNamesArray.length; fieldIndex++) {

			System.out.println("   creating new column with title, field: "
					+ fieldLabelsArray[fieldIndex] + " "
					+ fieldNamesArray[fieldIndex]);

			TextColumnBuilder lastNumericAddedColumn = null;

			// this fields are shown in header band and if field to add is
			// flagged here
			// just add the field but not the column.
			/*
			 * Boolean headerField =
			 * fieldNamesArray[fieldIndex].equals("Division__c") ||
			 * fieldNamesArray[fieldIndex].equals("MarketName__c") ||
			 * fieldNamesArray[fieldIndex].equals("MarketTypeName__c") ||
			 * fieldNamesArray[fieldIndex].equals("Flight_Name__c") ||
			 * fieldNamesArray[fieldIndex].equals("Campaign_Start_Date__c") ||
			 * fieldNamesArray[fieldIndex].equals("Campaign_End_Date__c") ||
			 * fieldNamesArray[fieldIndex].equals("Campaign_Weeks__c") ||
			 * fieldNamesArray[fieldIndex].equals("Target__c") ||
			 * fieldNamesArray[fieldIndex].equals("Target_Population__c");
			 */

			String[] pageHeaderFieldNamesArray = new String[] {
					"Package_Flight__r/Division__c",
					"Package_Flight__r/Market_key_Name__c",
					"Package_Flight__r/Market_Type__c",
					"Package_Flight__r/Name",
					"Package_Flight__r/Campaign_Start_Date__c",
					"Package_Flight__r/Campaign_End_Date__c",
					"Package_Flight__r/Duration_And_Type__c",
					"Package_Flight__r/Target__c",
					"Package_Flight__r/Target_Population__c" };
			Set<String> pageHeaderFieldNamesSet = new HashSet<String>(
					Arrays.asList(pageHeaderFieldNamesArray));
			Boolean headerField = pageHeaderFieldNamesSet
					.contains(fieldNamesArray[fieldIndex]);
			Boolean commentField = fieldNamesArray[fieldIndex]
					.equals("Package_Flight__r/Flight_Comments__c");
			String[] audienceHeaderFieldNamesArray = new String[] {
					"MediaTypeName__c", "Number_of_Panels__c",
					"Weekly_Total_18_Imps__c", "Target_In_Market_Imps__c",
					"Total_Imps__c", "Weekly_TRP_1d__c", "Plan_TRP_1d__c",
					"Reach_1d__c", "Frequency_1d__c",
					"Planning_Rate_4_Wk_Rate_per_Panel__c",
					"X4_Wk_Proposed_Price__c", "Net_Amount_Value__c",
					"Total_Price_0d__c", "TotalInMarketCPM_0d__c", "CPP_0d__c",
					"Comments__c" };
			String[] locationHeaderFieldNamesArray = new String[] {
					"MapLocation_Number__c", "MarketName__c",
					"MediaTypeName__c", "Panel_Id_Label__c", "TAB_Id__c",
					"Location_Description__c", "Face_Direction__c",
					"Weekly_Total_18_Imps__c", "Target_In_Market_Imps__c",
					"Total_Imps__c", "Weekly_TRP_1d__c", "Plan_TRP_1d__c",
					"Reach_1d__c", "Frequency_1d__c",
					"Planning_Rate_4_Wk_Rate_per_Panel__c",
					"X4_Wk_Proposed_Price__c", "Total_Price_0d__c",
					"TotalInMarketCPM_0d__c", "CPP_0d__c", "Unit_Size__c",
					"Illumination_yn__c", "Comments__c" };
			Set<String> audienceHeaderFieldNamesSet = new HashSet<String>(
					Arrays.asList(audienceHeaderFieldNamesArray));
			Set<String> locationHeaderFieldNamesSet = new HashSet<String>(
					Arrays.asList(locationHeaderFieldNamesArray));
			String[] networkHeaderFieldNamesArray = new String[] {
					"MapLocation_Number__c", "MediaTypeName__c",
					"Panel_Id_Label__c", "TAB_Id__c",
					"Location_Description__c", "Face_Direction__c",
					"Weekly_Total_18_Imps__c", "Target_In_Market_Imps__c",
					"Total_Imps__c", "Weekly_TRP_1d__c", "Plan_TRP_1d__c",
					"Reach_1d__c", "Frequency_1d__c", "Unit_Size__c" };
			Set<String> networkHeaderFieldNamesSet = new HashSet<String>(
					Arrays.asList(networkHeaderFieldNamesArray));

			Boolean notAddField = pdfCombinerFile.getBuyType() != null
					&& ((pdfCombinerFile.getBuyType().equals("Audience") && !audienceHeaderFieldNamesSet
							.contains(fieldNamesArray[fieldIndex]))
							|| (pdfCombinerFile.getBuyType().equals("Location") && !locationHeaderFieldNamesSet
									.contains(fieldNamesArray[fieldIndex])) || (pdfCombinerFile
							.getBuyType().equals("Network") && !networkHeaderFieldNamesSet
							.contains(fieldNamesArray[fieldIndex])));

			// System.out.println("notAddField->" + notAddField);
			// determine which field type is this
			if (fieldTypesArray[fieldIndex].equals("String")) {
				if (isDetail) {
					if (!fieldNamesArray[fieldIndex]
							.equals("MapLocation_Number__c")) {
						TextColumnBuilder<String> newColumn = col.column(
								fieldLabelsArray[fieldIndex],
								fieldNamesArray[fieldIndex], type.stringType());
						if (!headerField && !commentField && !notAddField) {
							newColumn.setTitleStyle(columnTitleStyle);
							b.addColumn(newColumn);
						}
						FieldBuilder<String> newField = field(
								fieldNamesArray[fieldIndex], String.class);
						b.addField(newField);
						if (fieldNamesArray[fieldIndex].equals("MarketName__c")) {
							marketNameColumn = newColumn;
						} else if (fieldNamesArray[fieldIndex]
								.equals("Flight_Name__c")) {
							flightNameColumn = newColumn;
						}
					} else {
						TextColumnBuilder<Integer> newColumn = col.column(
								fieldLabelsArray[fieldIndex],
								new MapLocationNumberExpressionColumn(
										mapPanelOrderPrefDataSourceFileName));
						if (!notAddField) {
							newColumn.setTitleStyle(columnTitleStyle);
							b.addColumn(newColumn);
						}
						b.sortBy(newColumn);
						FieldBuilder<String> newField = field(
								fieldNamesArray[fieldIndex], String.class);
						b.addField(newField);
						/*
						 * TextColumnBuilder<String> newColumn =
						 * col.column(fieldLabelsArray[fieldIndex], new
						 * MapLocationNumberExpressionColumn
						 * (mapPanelOrderPrefDataSourceFileName));
						 * b.addColumn(newColumn); b.sortBy(newColumn);
						 * FieldBuilder<String> newField =
						 * field(fieldNamesArray[fieldIndex], String.class);
						 * b.addField(newField);
						 */
					}
				} else {
					FieldBuilder<String> newField = field(
							fieldNamesArray[fieldIndex], String.class);
					b.addField(newField);
				}
			} else if (fieldTypesArray[fieldIndex].equals("Currency")) {
				if (isDetail) {
					TextColumnBuilder<BigDecimal> newColumn = col.column(
							fieldLabelsArray[fieldIndex],
							fieldNamesArray[fieldIndex], type.bigDecimalType());
					if (!headerField && !notAddField) {
						newColumn.setTitleStyle(columnTitleStyle);
						b.addColumn(newColumn);
					}
					lastNumericAddedColumn = newColumn;
					if (showTotalProgramSummary) {
						// AggregationSubtotalBuilder<Double> newSum =
						// Subtotals.sum(fieldNamesArray[fieldIndex],
						// Double.class, newColumn);
						// b.addSubtotalAtSummary(newSum);
					}
				} else {
					FieldBuilder<BigDecimal> newField = field(
							fieldNamesArray[fieldIndex], BigDecimal.class);
					b.addField(newField);
				}
			} else if (fieldTypesArray[fieldIndex].equals("Double")) {
				if (isDetail) {
					TextColumnBuilder<Double> newColumn = col.column(
							fieldLabelsArray[fieldIndex],
							fieldNamesArray[fieldIndex], type.doubleType());
					if (!headerField && !notAddField) {
						newColumn.setTitleStyle(columnTitleStyle);
						b.addColumn(newColumn);
					} else {
						FieldBuilder<BigDecimal> newField = field(
								fieldNamesArray[fieldIndex], BigDecimal.class);
						b.addField(newField);
					}
					lastNumericAddedColumn = newColumn;
					if (showTotalProgramSummary) {
						// AggregationSubtotalBuilder<Double> newSum =
						// Subtotals.sum(fieldNamesArray[fieldIndex],
						// Double.class, newColumn);
						// b.addSubtotalAtSummary(newSum);
					}
				} else {
					FieldBuilder<BigDecimal> newField = field(
							fieldNamesArray[fieldIndex], BigDecimal.class);
					b.addField(newField);
				}
			} else if (fieldTypesArray[fieldIndex].equals("Integer")) {
				if (isDetail) {
					TextColumnBuilder<Integer> newColumn = col.column(
							fieldLabelsArray[fieldIndex],
							fieldNamesArray[fieldIndex], type.integerType());
					if (!headerField && !notAddField) {
						newColumn.setTitleStyle(columnTitleStyle);
						b.addColumn(newColumn);
					}
					lastNumericAddedColumn = newColumn;
					if (showTotalProgramSummary) {
						// AggregationSubtotalBuilder<Integer> newSum =
						// Subtotals.sum(fieldNamesArray[fieldIndex],
						// Integer.class, newColumn);
						// b.addSubtotalAtSummary(newSum);
					}
				} else {
					FieldBuilder<Integer> newField = field(
							fieldNamesArray[fieldIndex], Integer.class);
					b.addField(newField);
				}
			} else if (fieldTypesArray[fieldIndex].equals("Percent")) {
				if (isDetail) {
					TextColumnBuilder<Double> newColumn = col.column(
							fieldLabelsArray[fieldIndex],
							fieldNamesArray[fieldIndex], type.percentageType());
					lastNumericAddedColumn = newColumn;
					if (!headerField && !notAddField) {
						newColumn.setTitleStyle(columnTitleStyle);
						b.addColumn(newColumn);
					}
				} else {
					FieldBuilder<Double> newField = field(
							fieldNamesArray[fieldIndex], Double.class);
					b.addField(newField);
				}
			}

			// set summary columns
			if (fieldNamesArray[fieldIndex].equals("Weekly_Total_18_Imps__c")) {
				weekly18Column = lastNumericAddedColumn;
			}
			if (fieldNamesArray[fieldIndex].equals("Target_In_Market_Imps__c")) {
				target_In_Market_ImpsColumn = lastNumericAddedColumn;
			}
			if (fieldNamesArray[fieldIndex].equals("Total_Imps__c")) {
				total_ImpsColumn = lastNumericAddedColumn;
			}
			if (fieldNamesArray[fieldIndex].equals("Weekly_TRP_1d__c")) {
				weekly_TRPColumn = lastNumericAddedColumn;
			}
			if (fieldNamesArray[fieldIndex].equals("PlanTRP_1d__c")) {
				planTRPColumn = lastNumericAddedColumn;
			}
			if (fieldNamesArray[fieldIndex].equals("Plan_Imps_Reach_Perc__c")) {
				plan_Imps_Reach_PercColumn = lastNumericAddedColumn;
			}
			if (fieldNamesArray[fieldIndex].equals("Frequency_1d__c")) {
				frequencyColumn = lastNumericAddedColumn;
			}
			if (fieldNamesArray[fieldIndex].equals("Total_Price_0d__c")) {
				total_PriceColumn = lastNumericAddedColumn;
			}
			if (fieldNamesArray[fieldIndex].equals("TotalInMarketCPM__c")) {
				totalInMarketCPMColumn = lastNumericAddedColumn;
			}
			if (fieldNamesArray[fieldIndex].equals("CPP_0d__c")) {
				cppColumn = lastNumericAddedColumn;
			}
		}

		if (showTotalProgramSummary) {
			if (weekly18Column != null) {
				b.subtotalsAtSummary(sbt.sum(weekly18Column));
			}
			if (target_In_Market_ImpsColumn != null) {
				b.subtotalsAtSummary(sbt.sum(target_In_Market_ImpsColumn));
			}
			if (total_ImpsColumn != null) {
				b.subtotalsAtSummary(sbt.sum(total_ImpsColumn));
			}
			if (weekly_TRPColumn != null) {
				b.subtotalsAtSummary(sbt.sum(weekly_TRPColumn));
			}
			if (planTRPColumn != null) {
				b.subtotalsAtSummary(sbt.sum(planTRPColumn));
			}
			if (plan_Imps_Reach_PercColumn != null) {
				b.subtotalsAtSummary(sbt.sum(plan_Imps_Reach_PercColumn));
			}
			if (frequencyColumn != null) {
				b.subtotalsAtSummary(sbt.sum(frequencyColumn));
			}
			if (total_PriceColumn != null) {
				b.subtotalsAtSummary(sbt.sum(total_PriceColumn));
			}
			if (totalInMarketCPMColumn != null) {
				b.subtotalsAtSummary(sbt.sum(totalInMarketCPMColumn));
			}
			if (cppColumn != null) {
				b.subtotalsAtSummary(sbt.sum(cppColumn));
			}
		}

		/* summary by market name */
		if (showIndividualMarketSummary) {
			if (marketNameColumn != null) {
				// ColumnGroupBuilder marketNameGroup =
				// grp.group(marketNameColumn).setHeaderLayout(GroupHeaderLayout.VALUE);
				ColumnGroupBuilder marketNameGroup = grp
						.group(marketNameColumn).setHeaderLayout(
								GroupHeaderLayout.EMPTY);
				marketNameGroup.setAddToTableOfContents(true);
				// marketNameGroup.footer(cmp.text("Market Summary"));
				b.groupBy(marketNameGroup);

				if (weekly18Column != null) {
					b.subtotalsAtGroupFooter(marketNameGroup,
							sbt.sum(weekly18Column));
				}
				if (target_In_Market_ImpsColumn != null) {
					b.subtotalsAtGroupFooter(marketNameGroup,
							sbt.sum(target_In_Market_ImpsColumn));
				}
				if (total_ImpsColumn != null) {
					b.subtotalsAtGroupFooter(marketNameGroup,
							sbt.sum(total_ImpsColumn));
				}
				if (weekly_TRPColumn != null) {
					b.subtotalsAtGroupFooter(marketNameGroup,
							sbt.sum(weekly_TRPColumn));
				}
				if (planTRPColumn != null) {
					b.subtotalsAtGroupFooter(marketNameGroup,
							sbt.sum(planTRPColumn));
				}
				if (plan_Imps_Reach_PercColumn != null) {
					b.subtotalsAtGroupFooter(marketNameGroup,
							sbt.sum(plan_Imps_Reach_PercColumn));
				}
				if (frequencyColumn != null) {
					b.subtotalsAtGroupFooter(marketNameGroup,
							sbt.sum(frequencyColumn));
				}
				if (total_PriceColumn != null) {
					b.subtotalsAtGroupFooter(marketNameGroup,
							sbt.sum(total_PriceColumn));
				}
				if (totalInMarketCPMColumn != null) {
					b.subtotalsAtGroupFooter(marketNameGroup,
							sbt.sum(totalInMarketCPMColumn));
				}
				if (cppColumn != null) {
					b.subtotalsAtGroupFooter(marketNameGroup,
							sbt.sum(cppColumn));
				}
			}
		}

		/* summary by flight name */
		if (showIndividualFlightSummary) {
			if (flightNameColumn != null) {

				// ColumnGroupBuilder flightNameGroup =
				// grp.group(flightNameColumn).setHeaderLayout(GroupHeaderLayout.VALUE);
				ColumnGroupBuilder flightNameGroup = grp
						.group(flightNameColumn).setHeaderLayout(
								GroupHeaderLayout.EMPTY);
				flightNameGroup.setAddToTableOfContents(true);
				// flightNameGroup.footer(cmp.text("Flight Summary"));
				b.groupBy(flightNameGroup);

				if (weekly18Column != null) {
					b.subtotalsAtGroupFooter(flightNameGroup,
							sbt.sum(weekly18Column));
				}
				if (target_In_Market_ImpsColumn != null) {
					b.subtotalsAtGroupFooter(flightNameGroup,
							sbt.sum(target_In_Market_ImpsColumn));
				}
				if (total_ImpsColumn != null) {
					b.subtotalsAtGroupFooter(flightNameGroup,
							sbt.sum(total_ImpsColumn));
				}
				if (weekly_TRPColumn != null) {
					b.subtotalsAtGroupFooter(flightNameGroup,
							sbt.sum(weekly_TRPColumn));
				}
				if (planTRPColumn != null) {
					b.subtotalsAtGroupFooter(flightNameGroup,
							sbt.sum(planTRPColumn));
				}
				if (plan_Imps_Reach_PercColumn != null) {
					b.subtotalsAtGroupFooter(flightNameGroup,
							sbt.sum(plan_Imps_Reach_PercColumn));
				}
				if (frequencyColumn != null) {
					b.subtotalsAtGroupFooter(flightNameGroup,
							sbt.sum(frequencyColumn));
				}
				if (total_PriceColumn != null) {
					b.subtotalsAtGroupFooter(flightNameGroup,
							sbt.sum(total_PriceColumn));
				}
				if (totalInMarketCPMColumn != null) {
					b.subtotalsAtGroupFooter(flightNameGroup,
							sbt.sum(totalInMarketCPMColumn));
				}
				if (cppColumn != null) {
					b.subtotalsAtGroupFooter(flightNameGroup,
							sbt.sum(cppColumn));
				}
			}
		}
	}

	/**
	 * Returns the path to generated pdf dir defined in environment variables
	 * @throws IOException 
	 */
	public String getGeneratedPDFDir() throws IOException {
		if (generatedPDFDir == null) {
			generatedPDFDir = System.getenv("GENERATED_PDFS_DIR") + File.separator + this.getUniqueId();

			if (!new File(generatedPDFDir).mkdirs()) {
				throw new IOException("Could not create working directory " + generatedPDFDir);
			}
		}
		System.out.println("********** generatedPDFDir  " + generatedPDFDir);
		return generatedPDFDir;
	}

	/**
	 * Returns a unique id for this job
	 * 
	 * @return
	 */
	public String getUniqueId() {
		if (uniqueId == null) {
			uniqueId = String.valueOf(Calendar.getInstance().getTimeInMillis());
		}

		return uniqueId;
	}

	/**
	 * Return combined PDF file name
	 * @throws IOException 
	 */
	public String getGeneratedReport(String extension) throws IOException {
		return this.getGeneratedPDFDir() + File.separator + this.getUniqueId() + "." + extension;
	}

	public List<PDFCombinerContentEntry> getPDFCombinerContentEntryList() throws IOException {

		// read file to string
		String encoding = System.getProperty("file.encoding");
		String lineSeparator = "\\n"; // System.getProperty("line.separator");
										// // <========= setting line separator
										// by hand because this doesn't work
		String rawTableOfContentsString = FileUtils.readFileToString(new File(
				getGeneratedReport("csv")), encoding);

		// source lines
		String[] lines = rawTableOfContentsString.split("\\n");
		System.out.println("   lines " + lines);
		// debug info
		System.out.println("   encoding: " + encoding);
		System.out.println("   lineSeparator: " + lineSeparator);
		System.out.println("   line count: " + lines.length);

		List<PDFCombinerContentEntry> pages = getPDFCombinerContentEntryList(lines);

		// boolean hasShippingInstructionsPageNumber =
		// this.getShippingInstructionsPageNumber() != null;
		// if(hasShippingInstructionsPageNumber)
		if (this.doesShippingInstructionsExists) {
			PDFCombinerContentEntry pdfCombinerContentEntry = new PDFCombinerContentEntry();
			pdfCombinerContentEntry.setTitle("Shipping Instructions");
			pdfCombinerContentEntry.setPageNumber(this.getShippingInstructionsPageNumber() + 1);
			// page number seems zero-index based, so adding one to it.
			pages.add(pdfCombinerContentEntry);
		}

		return pages;
	}

	public static List<PDFCombinerContentEntry> getPDFCombinerContentEntryList(
			String[] lines) {

		// pdf combiner entry list
		List<PDFCombinerContentEntry> pdfCombinerContentEntryList = new ArrayList<PDFCombinerContentEntry>();

		// detecting if string has table of contents?
		Boolean hasTOC = false;

		// System.out.println("   rawTableOfContentsString: " +
		// rawTableOfContentsString);

		// last added title
		String lastAddedTitle = null;

		// parse lines
		for (String line : lines) {
			System.out.println(" ************ line " + line);

			boolean startsWithOnlyOneSpace = line.startsWith(" ")
					&& line.substring(1, 2) != " ";
			if (line.startsWith("Table of contents")) {
				hasTOC = true;
			} else if (line.startsWith("Proposal")) {
				break;
			} else if (hasTOC && startsWithOnlyOneSpace) {

				// get page number
				int pageNumber = getPageNumberFromLine(line);

				// get title part
				String lineParts[] = line.split("\\,");
				String finalLine = lineParts[0];
				finalLine = finalLine.replaceAll("null", "").trim();
				System.out.println("   finalLine, pageNumber: " + finalLine
						+ ", " + pageNumber);
				System.out.println("   hasTOC, line: " + hasTOC + "," + line);
				if (lastAddedTitle == null || !finalLine.equals(lastAddedTitle)) {
					// add entry
					PDFCombinerContentEntry pdfCombinerContentEntry = new PDFCombinerContentEntry();
					System.out.println("   finalLine, indexof: " + finalLine
							+ ", " + pageNumber);
					if (finalLine.indexOf("$~") > 0) {
						System.out.println("if");
						String[] arr = finalLine.split("\\$\\~");
						System.out.println("array length " + arr.length);
						if (arr.length > 1) {
							pdfCombinerContentEntry.setTitle(arr[1]);
						}
						pdfCombinerContentEntry.setDescription(arr[0]);
					} else {
						System.out.println("why else");
						pdfCombinerContentEntry.setTitle(finalLine);
					}
					// pdfCombinerContentEntry.setTitle(finalLine);
					pdfCombinerContentEntry.setPageNumber(pageNumber);
					pdfCombinerContentEntryList.add(pdfCombinerContentEntry);

					/*
					 * System.out.println("   finalLine, pageNumber: " +
					 * finalLine + ", " + pageNumber);
					 * 
					 * System.out.println("   hasTOC, line: " + hasTOC + "," +
					 * line);
					 */

					lastAddedTitle = finalLine;
				}
			}
		}

		return pdfCombinerContentEntryList;
	}

	// get numbers from the end of passed input string
	// input example: Package Albaquerque
	// DMA,,,,,,..............................................................................................................................................,,,,,,258
	// return 258
	public static int getPageNumberFromLine(String string) {
		String numberString = "";

		for (int i = string.length(); i > 0; i--) {
			String s = string.substring(i - 1, i);
			// System.out.println("   i, s, numberString: " + i + ", " + s +
			// ", " + numberString);
			if (isIntegerParseInt(s)) {
				numberString = s + numberString;
			} else {
				// no more numbers found
				break;
			}
		}

		if (numberString.length() > 0) {
			return Integer.parseInt(numberString);
		} else {
			return 0;
		}
	}

	public static boolean isIntegerParseInt(String str) {
		try {
			Integer.parseInt(str);
			return true;
		} catch (NumberFormatException nfe) {
		}
		return false;
	}

	public Integer getShippingInstructionsPageNumber() {
		return shippingInstructionsPageNumber;
	}

	public void setShippingInstructionsPageNumber(
			Integer shippingInstructionsPageNumber) {
		this.shippingInstructionsPageNumber = shippingInstructionsPageNumber;
	}

	public boolean isDoesShippingInstructionsExists() {
		return doesShippingInstructionsExists;
	}

	public void setDoesShippingInstructionsExists(
			boolean doesShippingInstructionsExists) {
		this.doesShippingInstructionsExists = doesShippingInstructionsExists;
	}

	private HashMap<String, String> getFlightLineColumnLabelHashMap() {
		return flightLineColumnLabelHashMap;
	}

	private void setFlightLineColumnLabelHashMap(
			HashMap<String, String> flightLineColumnLabelHashMap) {
		this.flightLineColumnLabelHashMap = flightLineColumnLabelHashMap;
	}

	private HashMap<String, String> getKeyValueHashMap(String fieldNames, String fieldLabels) {

		List<String> fieldNamesList = Arrays.asList(fieldNames.split("\\|"));
		List<String> fieldLabelsList = Arrays.asList(fieldLabels.split("\\|"));
		HashMap<String, String> keyValueHashMap = new LinkedHashMap<String, String>();

		for (int i = 0; i < fieldNamesList.size(); i++) {
			keyValueHashMap.put(fieldNamesList.get(i), fieldLabelsList.get(i)); 
			// is there a clearer way?
		}
		return keyValueHashMap;
	}
   
	public String getMapPanelOrderPrefDataSourceFileName() {
		return mapPanelOrderPrefDataSourceFileName;
	}

	public void setMapPanelOrderPrefDataSourceFileName(
			String mapPanelOrderPrefDataSourceFileName) {
		this.mapPanelOrderPrefDataSourceFileName = mapPanelOrderPrefDataSourceFileName;
	}
	
	public String getDetailDataSourceFileName() {
		return detailDataSourceFileName;
	}

	public void setDetailDataSourceFileName(
			String detailDataSrcFileName) {
		this.detailDataSourceFileName = detailDataSrcFileName;
	}
	
	private static boolean hasLocationMapAttachments(List<PDFCombinerFile> appendixes) {
		boolean locationMapExists = false;
		// if it has appendixes
    	if(appendixes != null) {
    		
	    	for(PDFCombinerFile file : appendixes) {
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

	public Integer getRecordCount() {
		return recordCount;
	}

	public void setRecordCount(Integer recordCount) {
		this.recordCount = recordCount;
	}

	public StyleBuilder getFlightHeaderStyle() {
		return flightHeaderStyle;
	}

	public void setFlightHeaderStyle(StyleBuilder flightHeaderStyle) {
		this.flightHeaderStyle = flightHeaderStyle;
	}

	public DisclaimerStore getDisclaimerStore() {
		return disclaimerStore;
	}
 }
