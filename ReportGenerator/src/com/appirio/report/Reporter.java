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
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
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
import net.sf.dynamicreports.report.builder.datatype.DateType;
import net.sf.dynamicreports.report.builder.group.ColumnGroupBuilder;
import net.sf.dynamicreports.report.builder.group.CustomGroupBuilder;
import net.sf.dynamicreports.report.builder.style.PaddingBuilder;
import net.sf.dynamicreports.report.builder.style.PenBuilder;
import net.sf.dynamicreports.report.builder.style.ReportStyleBuilder;
import net.sf.dynamicreports.report.builder.style.StyleBuilder;
import net.sf.dynamicreports.report.constant.GroupHeaderLayout;
import net.sf.dynamicreports.report.constant.HorizontalAlignment;
import net.sf.dynamicreports.report.constant.PageOrientation;
import net.sf.dynamicreports.report.constant.VerticalAlignment;
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
	

	// column hashmap (key = column name, value = label)
	private HashMap<String, String> flightLineColumnLabelHashMap = null;
	private Set<String> validDisclaimersSet = new HashSet<String>();

	private Set<String> autoDisclaimersSet = new HashSet<String>();
	DisclaimerStore disclaimerStore = new DisclaimerStore();
	//private HashMap<String, String> flightColumnLabelHashMap = null;


	private boolean locationMapExists = false;
	
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
			String packageSummaryDataSourceFileName,
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
		System.out.println("   showTotalProgramSummary : "
				+ showTotalProgramSummary);
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
			StyleBuilder columnTitleStyle = stl.style(boldCenteredStyle)
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
			StyleBuilder titleStyle = stl.style(boldCenteredStyle)
					.setPadding(padding)
					.setVerticalAlignment(VerticalAlignment.MIDDLE)
					.setFontSize(10);
			StyleBuilder groupHeaderStyle = stl.style(boldCenteredStyle)
					.setPadding(padding).setBorder(grayThinLine)
					.setBackgroundColor(new Color(197, 217, 241))
					.setFontSize(10);
			StyleBuilder groupHeaderStyleNew = stl.style().setFontSize(0)
					.setBackgroundColor(new Color(255, 255, 255));

			StyleBuilder flightHeaderStyle = stl.style(boldRightAlignStyle)
					.setLeftPadding(Units.inch(0.5)).setPadding(padding)
					.setFontSize(8);
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

			DisclaimersDataExpression disclaimerCollectionExpression = new DisclaimersDataExpression(disclaimerDataSourceFileName);
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
					.setPageFormat(Units.inch(23), Units.inch(16),
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

			// =================== package summary subreport: begin
			JRXmlDataSource packageSummaryDataSource = new JRXmlDataSource(
					packageSummaryDataSourceFileName, "/QueryResult/records");
			SubreportBuilder packageSummarySubreport = cmp
					.subreport(
							new PackageSummarySubreportExpression(
									packageMarketFlightPreviousRecordExpression,
									columnTitleStyle, columnStyle,
									flightHeaderStyle, flightHeaderValueStyle,
									exportAsExcel, false))
					.setDataSource(packageSummaryDataSource)
					/*
					 * .setPrintWhenExpression( showTotalProgramSummary)
					 */
					.removeLineWhenBlank();
			if (showTotalProgramSummary) {
				if (exportAsExcel) {
					b.pageHeader(packageSummarySubreport);
				} else {
					SubreportBuilder packageSummaryLocationSubreport = cmp
							.subreport(
									new LocationPackageSummarySubreportExpression(
											packageMarketFlightPreviousRecordExpression,
											columnTitleStyle, columnStyle,
											null, null, exportAsExcel))
							.setDataSource(packageSummaryDataSource)
							.setPrintWhenExpression(packageMarketFlightPreviousRecordHadLocationExpression)
							.removeLineWhenBlank();
					b.summary(packageSummaryLocationSubreport);
					
					SubreportBuilder packageSummaryAudienceSubreport = cmp
							.subreport(
									new AudiencePackageSummarySubreportExpression(
											packageMarketFlightPreviousRecordExpression,
											columnTitleStyle, columnStyle, null, null,
											exportAsExcel))
							.setDataSource(packageSummaryDataSource)
							.setPrintWhenExpression(packageMarketFlightPreviousRecordHadAudienceExpression)
							.removeLineWhenBlank();
					b.summary(packageSummaryAudienceSubreport);
					
					SubreportBuilder packageSummaryNetworkSubreport = cmp
							.subreport(
									new NetworkPackageSummarySubreportExpression(
											packageMarketFlightPreviousRecordExpression,
											columnTitleStyle, columnStyle, null, null,
											exportAsExcel))
							.setDataSource(packageSummaryDataSource)
							.setPrintWhenExpression(packageMarketFlightPreviousRecordHadNetworkExpression)
							.removeLineWhenBlank();
					b.summary(packageSummaryNetworkSubreport);
					
					b.summary(packageSummarySubreport);
				}
			}
			// =================== package summary subreport: end
			FlightNameReportScriptlet flightNameSubreportScriptlet = new FlightNameReportScriptlet();
			// report.scriptlets(flightNameSubreportScriptlet);
			b.scriptlets(flightNameSubreportScriptlet);
			// b.groupBy(group);
			//b.summary(cmp.text(""));
			// create a vertical list of disclaimer reports
			if (exportAsPdf) {
			if (commentDataSource.next() || flightCommentDataSource.next()) {
				VerticalListBuilder flightCommentReports = cmp.verticalList();
				// flightCommentReports.add(cmp.text("Comments:").setStyle(boldStyle).removeLineWhenBlank());
					flightCommentReports.add(createCommentTable(
									"Package_Flight__r/Package_Name__c",
									"Package_Flight__r/Name",
									"Package_Flight__r/Flight_Comments__c",
									"Package_Flight__r/Package_Comments__c")
									.removeLineWhenBlank());
					flightCommentReports.add(cmp.text(""));
				b.summary(flightCommentReports);
				//b.summary(cmp.text(""));
				}
			}
			// create a vertical list of Valid disclaimer reports as per rules
			ValidDisclaimerExistsExpression validDisclaimerExistsExpression =  new ValidDisclaimerExistsExpression();
			VerticalListBuilder validDisclaimerReports = cmp.verticalList();
			validDisclaimerReports.setPrintWhenExpression(validDisclaimerExistsExpression);
			validDisclaimerReports.add(cmp.text("Important Notes:").setStyle(boldStyle).removeLineWhenBlank());
			validDisclaimerReports.add(cmp.text(new DisclaimerExpression()).setStyle(summaryStyle));
			//b.summary(cmp.text(""));
			//group.footer(validDisclaimerReports);
			b.summary(validDisclaimerReports);
			//b.summary(cmp.text(""));
			// create a vertical list of disclaimer reports

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
			CustomGroupBuilder packageMarketFlightPreviousRecordCustomGroupBuilder = grp
					.group(packageMarketFlightPreviousRecordExpression);
			b.addGroup(packageMarketFlightPreviousRecordCustomGroupBuilder);

			// wrap the subreport inside horizontal list builder to control when
			// it should be rendered/printed
			HorizontalListBuilder afterLocationListPageDisclaimer = cmp
					.horizontalList();
			afterLocationListPageDisclaimer
					.add(afterLocationListPageDisclaimerSubreport);
			afterLocationListPageDisclaimer
					.setPrintWhenExpression(packageMarketFlightPreviousRecordHadLocationExpression);
			flightDisclaimerReports.add(afterLocationListPageDisclaimer.setStyle(summaryStyle));
			// =================== end

			// =================== audience subreport: begin
			JRXmlDataSource detailDataSource = new JRXmlDataSource(
					dataSourceFileName, "/QueryResult/records[RecordTypeId = boolean(0)]");
			SubreportBuilder audienceSubreport = cmp
					.subreport(
							new AudienceSubreportExpression(
									packageMarketFlightPreviousRecordExpression,
									columnTitleStyle, columnStyle, flightHeaderStyle, flightHeaderValueStyle,
									exportAsExcel, false))
					.setDataSource(detailDataSource)
					.setPrintWhenExpression(
							packageMarketFlightPreviousRecordHadAudienceExpression)
					.removeLineWhenBlank();
			// =================== audience subreport: end

			// =================== audience subreport summary: begin
			JRXmlDataSource audienceSummaryDataSource = new JRXmlDataSource(
					dataSourceFileName, "/QueryResult/records[RecordTypeId = boolean(1)]");
			SubreportBuilder audienceSummarySubreport = cmp
					.subreport(
							new AudienceSubreportExpression(
									packageMarketFlightPreviousRecordExpression,
									columnTitleStyle, columnStyle, null, null,
									exportAsExcel, true))
					.setDataSource(audienceSummaryDataSource)
					.setPrintWhenExpression(
							packageMarketFlightPreviousRecordHadAudienceExpression)
					.removeLineWhenBlank();
			// =================== audience subreport summary: end

			// =================== location subreport: begin
			JRXmlDataSource detailDataSource2 = new JRXmlDataSource(
					dataSourceFileName, "/QueryResult/records[RecordTypeId = boolean(0)]");
			SubreportBuilder locationSubreport = cmp
					.subreport(
							new LocationSubreportExpression(
									packageMarketFlightPreviousRecordExpression,
									columnTitleStyle, columnStyle,
									flightHeaderStyle, flightHeaderValueStyle,
									exportAsExcel, false, locationMapExists))
					.setDataSource(detailDataSource2)
					.setPrintWhenExpression(
							packageMarketFlightPreviousRecordHadLocationExpression)
					.removeLineWhenBlank();
			// =================== location subreport: end

			// =================== location summary subreport: begin
			JRXmlDataSource detailSummaryDataSource = new JRXmlDataSource(
					dataSourceFileName, "/QueryResult/records[RecordTypeId = boolean(1)]");
			SubreportBuilder locationSummarySubreport = cmp
					.subreport(
							new LocationSubreportExpression(
									packageMarketFlightPreviousRecordExpression,
									columnTitleStyle, columnStyle, null, null,
									exportAsExcel, true, locationMapExists))
					.setDataSource(detailSummaryDataSource)
					.setPrintWhenExpression(
							packageMarketFlightPreviousRecordHadLocationExpression)
					.removeLineWhenBlank();
			// =================== location subreport: end

			// =================== rotary subreport: begin
			JRXmlDataSource rotaryDataSource = new JRXmlDataSource(
					dataSourceFileName, "/QueryResult/records[RecordTypeId = boolean(0)]");
			SubreportBuilder rotarySubreport = cmp
					.subreport(
							new RotarySubreportExpression(
									packageMarketFlightPreviousRecordExpression,
									columnTitleStyle, columnStyle, flightHeaderStyle, flightHeaderValueStyle,
									exportAsExcel, false))
					.setDataSource(rotaryDataSource)
					.setPrintWhenExpression(
							packageMarketFlightPreviousRecordHadRotaryExpression)
					.removeLineWhenBlank();
			// =================== rotary subreport: end

			// =================== rotary summary subreport: begin
			JRXmlDataSource rotarySummaryDataSource = new JRXmlDataSource(
					dataSourceFileName, "/QueryResult/records[RecordTypeId = boolean(1)]");
			SubreportBuilder rotarySummarySubreport = cmp
					.subreport(
							new RotarySubreportExpression(
									packageMarketFlightPreviousRecordExpression,
									columnTitleStyle, columnStyle, null, null,
									exportAsExcel, true))
					.setDataSource(rotarySummaryDataSource)
					.setPrintWhenExpression(
							packageMarketFlightPreviousRecordHadRotaryExpression)
					.removeLineWhenBlank();
			// =================== rotary summary subreport: end

			// =================== network subreport: begin
			JRXmlDataSource networkDataSource = new JRXmlDataSource(
					dataSourceFileName, "/QueryResult/records[Child_Flight_Lines__r = boolean(1) and RecordTypeId = boolean(0)]");
			SubreportBuilder networkSubreport = cmp
					.subreport(
							new NetworkSubreportExpression(
									dataSourceFileName,
									packageMarketFlightPreviousRecordExpression,
									flightLinePreviousRecordExpression,
									columnTitleStyle, columnStyle, flightHeaderStyle, flightHeaderValueStyle,
									exportAsExcel, false))
					.setPrintWhenExpression(packageMarketFlightPreviousRecordHadNetworkExpression)
					.setDataSource(networkDataSource)
					.removeLineWhenBlank();
			// =================== network subreport: end

			// =================== network subreport: begin
			JRXmlDataSource networkSummaryDataSource = new JRXmlDataSource(
					dataSourceFileName, "/QueryResult/records[RecordTypeId = boolean(1)]");
			SubreportBuilder networkSummarySubreport = cmp
					.subreport(
							new NetworkSubreportExpression(
									dataSourceFileName,
									packageMarketFlightPreviousRecordExpression,
									null,
									columnTitleStyle, columnStyle, flightHeaderStyle, flightHeaderValueStyle,
									exportAsExcel, true))
					.setPrintWhenExpression(packageMarketFlightPreviousRecordHadNetworkExpression)
					.setDataSource(networkSummaryDataSource)
					.removeLineWhenBlank();
			// =================== network subreport: end

			// =================== subreport and summaries positioning: begin
			if(exportAsPdf) {
				group.footer(audienceSubreport);
				if(showIndividualFlightSummary) group.footer(audienceSummarySubreport);
				group.footer(locationSubreport);
				if(showIndividualFlightSummary) group.footer(locationSummarySubreport);
				group.footer(rotarySubreport);
				if(showIndividualFlightSummary) group.footer(rotarySummarySubreport);
				group.footer(networkSubreport);
				if(showIndividualFlightSummary) group.footer(networkSummarySubreport);
			} else {
				if(showIndividualFlightSummary) group.footer(audienceSummarySubreport);
				group.footer(audienceSubreport);
				if(showIndividualFlightSummary) group.footer(locationSummarySubreport);
				group.footer(locationSubreport);
				if(showIndividualFlightSummary) group.footer(rotarySummarySubreport);
				group.footer(rotarySubreport);
				group.footer(networkSubreport);
				if(showIndividualFlightSummary) group.footer(networkSummarySubreport);
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
			// Shipping instructions is part of the Plan that is displayed after
			// all Plan Data is rendered in PDF
			JRXmlDataSource shippingInstructionsDataSource = null;
			if (shippingInstructionsDataSourceFileName != null) {
				shippingInstructionsDataSource = new JRXmlDataSource(
						shippingInstructionsDataSourceFileName,
						"/QueryResult/records");
				this.setDoesShippingInstructionsExists(shippingInstructionsDataSource
						.next());
				shippingInstructionsDataSource.moveFirst();
				ShippingInstructionsSubreportExpression shippingInstructionsSubreportExpression = new ShippingInstructionsSubreportExpression(
						this);
				SubreportBuilder shippingInstructionsSubreport = cmp
						.subreport(shippingInstructionsSubreportExpression)
						.setDataSource(shippingInstructionsDataSource)
						.removeLineWhenBlank();

				// .setRunToBottom(true);

				//b.summary(cmp.text(""));
				b.summary(shippingInstructionsSubreport);
				System.out.println(" doesShippingInstructionsExists "  +doesShippingInstructionsExists);
				if(doesShippingInstructionsExists) { 
				VerticalListBuilder autoValidDisclaimerReports = cmp
						.verticalList();
				//b.summary(cmp.text(""));
				autoValidDisclaimerReports.add(cmp.text("Important Notes:")
						.setStyle(boldStyle).removeLineWhenBlank());
				autoValidDisclaimerReports.add(cmp
						.text(new AutoDisclaimerExpression()).setStyle(summaryStyle));
				b.summary(autoValidDisclaimerReports);
				}
				// b.summaryOnANewPage();
			}
			// =================== end

			// throw an exception if internal use fields are not set
			if (!hasRequiredInternalUseFields(b)) {
				throw new Exception("Required fields missing"
						+ StringUtils.join(getRequiredMissingFieldsList(b)));
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

			// As DynamicReports doesn't have a method/property that return
			// Table of Contents (TOC) entries
			// for more info see here:
			// http://www.dynamicreports.org/forum/viewtopic.php?f=1&t=416&sid=25edfbfe63ade19e637c1a79f5974ee1
			// We'll do an additional export in csv format after having exported
			// to usual pdf/xls export formats.
			// This way other clients can pull report as csv and grab manually
			// the TOC entries.
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

	private boolean hasRequiredInternalUseFields(JasperReportBuilder report) {
		return getRequiredMissingFieldsList(report).size() == 0;
	}

	private List<String> getRequiredMissingFieldsList(JasperReportBuilder report) {

		List<String> requiredFieldsList = new ArrayList<String>();
		requiredFieldsList.add("Package_Flight__r/Id");
		requiredFieldsList.add("Package_Flight__r/Type__c");
		requiredFieldsList.add("Package_Flight__r/Media_Category__c");

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

	/************************** jitendra *************************/
	
	private class NetworkPackageSummarySubreportExpression extends
			AbstractSimpleExpression<JasperReportBuilder> {

		private static final long serialVersionUID = -2402045955847483179L;

		private PackageMarketFlightPreviousRecordExpression packageMarketFlightPreviousRecordExpression;

		private StyleBuilder columnTitleStyle;

		private StyleBuilder columnStyle;

		private boolean exportAsExcel;

		private StyleBuilder flightHeaderStyle;

		private StyleBuilder flightHeaderValueStyle;

		public NetworkPackageSummarySubreportExpression(
				PackageMarketFlightPreviousRecordExpression packageMarketFlightPreviousRecordExpression,
				StyleBuilder columnTitleStyle, StyleBuilder columnStyle,
				StyleBuilder flightHeaderStyle,
				StyleBuilder flightHeaderValueStyle, boolean exportAsExcel) {
			setPackageMarketFlightPreviousRecordExpression(packageMarketFlightPreviousRecordExpression);
			setColumnTitleStyle(columnTitleStyle);
			setColumnStyle(columnStyle);
			setExportAsExcel(exportAsExcel);
			setFlightHeaderStyle(flightHeaderStyle);
			setFlightHeaderValueStyle(flightHeaderValueStyle);
		}

		@Override
		public JasperReportBuilder evaluate(ReportParameters arg0) {
			JasperReportBuilder report = report();

			// types
			CurrencyWithFractionDecimalType currencyWithFractionDecimalType = new CurrencyWithFractionDecimalType();
			CurrencyWithoutFractionDecimalType currencyWithoutFractionDecimalType = new CurrencyWithoutFractionDecimalType();

			// ================================= add columns: begin
			TextColumnBuilder<String> summaryColumn = col.column("Summary",	"Package_Flight__r/Package_Name__c", type.stringType())
					.setHorizontalAlignment(HorizontalAlignment.LEFT);
			report.addColumn(summaryColumn).setColumnTitleStyle(getColumnTitleStyle());

			if(getFlightLineColumnLabelHashMap().containsKey("Network_Name__c")) {
				report.addColumn(col.column("",	"Network_Name__c", type.stringType()))
						.setColumnTitleStyle(getColumnTitleStyle());
			}
			if(getFlightLineColumnLabelHashMap().containsKey("Number_of_Panels__c")) {
				report.addColumn(col.column("",	"Number_of_Panels__c", type.integerType()).setPattern("#,###")
						.setHorizontalAlignment(HorizontalAlignment.RIGHT));
			}
			if(getFlightLineColumnLabelHashMap().containsKey("Package_Flight__r/Campaign_Start_Date__c")) {
				report.addColumn(col.column("",	"Package_Flight__r/Campaign_Start_Date__c", type.stringType())
						.setHorizontalAlignment(HorizontalAlignment.CENTER));					
			}
			if(getFlightLineColumnLabelHashMap().containsKey("Package_Flight__r/Campaign_End_Date__c")) {
				report.addColumn(col.column("", "Package_Flight__r/Campaign_End_Date__c", type.stringType())
						.setHorizontalAlignment(HorizontalAlignment.CENTER));					
			}
			if(getFlightLineColumnLabelHashMap().containsKey("Weekly_Total_18_Imps__c")) {
				report.addColumn(col.column("",	"Weekly_Total_18_Imps__c", type.integerType())
						.setPattern("#,###").setHorizontalAlignment(HorizontalAlignment.RIGHT));
			}
			if(getFlightLineColumnLabelHashMap().containsKey("In_Mkt_Imps__c")) {
				report.addColumn(col.column("", "In_Mkt_Imps__c", type.doubleType())
						.setPattern("#,###").setHorizontalAlignment(HorizontalAlignment.RIGHT));
			}
			if(getFlightLineColumnLabelHashMap().containsKey("Total_Imps__c")) {
				report.addColumn(col.column("", "Total_Imps__c", type.doubleType())
						.setPattern("#,###").setHorizontalAlignment(HorizontalAlignment.RIGHT));
			}
			if(getFlightLineColumnLabelHashMap().containsKey("In_Mkt_TRP__c")) {
				report.addColumn(col.column("", "In_Mkt_TRP__c", type.doubleType())
						.setPattern("##0.0").setHorizontalAlignment(HorizontalAlignment.RIGHT));
			}
			if(getFlightLineColumnLabelHashMap().containsKey("PlanTRP__c")) {
				report.addColumn(col.column("", "PlanTRP__c", type.doubleType())
						.setPattern("##0.0").setHorizontalAlignment(HorizontalAlignment.RIGHT));
			}
			if(getFlightLineColumnLabelHashMap().containsKey("Plan_Imps_Reach_Perc__c")) {
				report.addColumn(col.column("", "Plan_Imps_Reach_Perc__c", type.percentageType())
						.setPattern("##0.0").setHorizontalAlignment(HorizontalAlignment.RIGHT));
			}
			if(getFlightLineColumnLabelHashMap().containsKey("Plan_Imps_Avg_Frequency__c")) {
				report.addColumn(col.column("", "Plan_Imps_Avg_Frequency__c", type.doubleType())
						.setHorizontalAlignment(HorizontalAlignment.RIGHT));
			}
			if(getFlightLineColumnLabelHashMap().containsKey("X4_Wk_Proposed_Price__c")) {
				report.addColumn(col.column("", "X4_Wk_Proposed_Price__c", currencyWithoutFractionDecimalType)
						.setHorizontalAlignment(HorizontalAlignment.RIGHT));
			}
			if(getFlightLineColumnLabelHashMap().containsKey("Total_Price_0d__c")) {
				report.addColumn(col.column(getFlightLineColumnLabelHashMap().get("Total_Price_0d__c"),	"Total_Price__c",
						currencyWithoutFractionDecimalType).setHorizontalAlignment(HorizontalAlignment.RIGHT));
			}
			if(getFlightLineColumnLabelHashMap().containsKey("TotalInMarketCPM_0d__c")) {
				report.addColumn(col.column("", "TotalInMarketCPM_0d__c", currencyWithFractionDecimalType)
						.setHorizontalAlignment(HorizontalAlignment.RIGHT));
			}
			if(getFlightLineColumnLabelHashMap().containsKey("CPP_0d__c")) {
				report.addColumn(col.column("", "CPP_0d__c", currencyWithoutFractionDecimalType)
						.setHorizontalAlignment(HorizontalAlignment.RIGHT));
			}
			if(getFlightLineColumnLabelHashMap().containsKey("Package_Flight__r/Flight_Comments__c")) {
				report.addColumn(col.column("", "Package_Flight__r/Flight_Comments__c", type.stringType()));
			}
			if(getFlightLineColumnLabelHashMap().containsKey("Network_Description__c")) {
				report.addColumn(col.column("",	"Network_Description__c", type.stringType()).setWidth(Units.inch(2.5)));
			}
			if(getFlightLineColumnLabelHashMap().containsKey("Network_Notes__c")) {
				report.addColumn(col.column("",	"Network_Notes__c", type.stringType()));
			}
			if(getPackageMarketFlightPreviousRecordExpression().isDigitalMediaCategory()) {
				if(getFlightLineColumnLabelHashMap().containsKey("Average_Daily_Spots__c")) {
					report.addColumn(col.column("",	"Average_Daily_Spots__c", type.stringType())
							.setHorizontalAlignment(HorizontalAlignment.RIGHT));
				}
			}
			// ================================= add columns: end

			// style
			report.setColumnStyle(getColumnStyle());

			// add a blank line at the end
			report.addLastPageFooter(cmp.text(""));

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
		
		public StyleBuilder getFlightHeaderStyle() {
			return flightHeaderStyle;
		}

		public void setFlightHeaderStyle(StyleBuilder flightHeaderStyle) {
			this.flightHeaderStyle = flightHeaderStyle;
		}

		public StyleBuilder getFlightHeaderValueStyle() {
			return flightHeaderValueStyle;
		}

		public void setFlightHeaderValueStyle(
				StyleBuilder flightHeaderValueStyle) {
			this.flightHeaderValueStyle = flightHeaderValueStyle;
		}

	}

	private class AudiencePackageSummarySubreportExpression extends
			AbstractSimpleExpression<JasperReportBuilder> {

		private static final long serialVersionUID = 1656551952324L;

		private PackageMarketFlightPreviousRecordExpression packageMarketFlightPreviousRecordExpression;

		private StyleBuilder columnTitleStyle;

		private StyleBuilder columnStyle;

		private boolean exportAsExcel;

		private StyleBuilder flightHeaderStyle;

		private StyleBuilder flightHeaderValueStyle;

		public AudiencePackageSummarySubreportExpression(
				PackageMarketFlightPreviousRecordExpression packageMarketFlightPreviousRecordExpression,
				StyleBuilder columnTitleStyle, StyleBuilder columnStyle,
				StyleBuilder flightHeaderStyle,
				StyleBuilder flightHeaderValueStyle, boolean exportAsExcel) {
			setPackageMarketFlightPreviousRecordExpression(packageMarketFlightPreviousRecordExpression);
			setColumnTitleStyle(columnTitleStyle);
			setColumnStyle(columnStyle);
			setExportAsExcel(exportAsExcel);
			setFlightHeaderStyle(flightHeaderStyle);
			setFlightHeaderValueStyle(flightHeaderValueStyle);
		}

		@Override
		public JasperReportBuilder evaluate(ReportParameters arg0) {
			JasperReportBuilder report = report();

			// types
			CurrencyWithoutFractionDecimalType currencyWithoutFractionDecimalType = new CurrencyWithoutFractionDecimalType();
			
			// build columns that are to be summarized
			String weeklyTotal18ImplsHeaderLabel = "";
			if (exportAsExcel) {
				weeklyTotal18ImplsHeaderLabel = getFlightLineColumnLabelHashMap()
						.get("Weekly_Total_18_Imps__c");
			}

			String weeklyTotalImplsHeaderLabel = "";
			if (exportAsExcel) {
				weeklyTotalImplsHeaderLabel = getFlightLineColumnLabelHashMap()
						.get("Total_Imps__c");
			}

			String weeklyInMktTRPHeaderLabel = "";
			if (exportAsExcel) {
				weeklyInMktTRPHeaderLabel = getFlightLineColumnLabelHashMap()
						.get("In_Mkt_TRP__c");
			}
			String freqHeaderLabel = "";
			if (exportAsExcel) {
				freqHeaderLabel = getFlightLineColumnLabelHashMap().get(
						"Plan_Imps_Avg_Frequency__c");
			}
			String totalPriceHeaderLabel = "";
			if (exportAsExcel) {
				totalPriceHeaderLabel = getFlightLineColumnLabelHashMap().get(
						"Total_Price_0d__c");
			}
			String cppHeaderLabel = "";
			if (exportAsExcel) {
				cppHeaderLabel = getFlightLineColumnLabelHashMap().get(
						"CPP_0d__c");
			}
			String InMktImpsHeaderLabel = "";
			if (exportAsExcel) {
				InMktImpsHeaderLabel = getFlightLineColumnLabelHashMap().get(
						"In_Mkt_Imps__c");
			}
			String planTRPHeaderLabel = "";
			if (exportAsExcel) {
				planTRPHeaderLabel = getFlightLineColumnLabelHashMap().get(
						"PlanTRP__c");
			}
			String reachPercHeaderLabel = "";
			if (exportAsExcel) {
				reachPercHeaderLabel = getFlightLineColumnLabelHashMap().get(
						"Plan_Imps_Reach_Perc__c");
			}
			String X4WkProposedPriceHeaderLabel = "";
			if (exportAsExcel) {
				X4WkProposedPriceHeaderLabel = getFlightLineColumnLabelHashMap()
						.get("X4_Wk_Proposed_Price__c");
			}

			String totalInMarketCPMHeaderLabel = "";
			if (exportAsExcel) {
				totalInMarketCPMHeaderLabel = getFlightLineColumnLabelHashMap()
						.get("TotalInMarketCPM_0d__c");
			}
			// build columns that are to be summarized
			TextColumnBuilder<Integer> weeklyTotal18ImpsColumn = col.column(
					"",
					"Weekly_Total_18_Imps__c", type.integerType())
					.setHorizontalAlignment(HorizontalAlignment.CENTER);
			weeklyTotal18ImpsColumn.setWidth(Units.inch(2.5));
			report.addColumn(weeklyTotal18ImpsColumn);

			TextColumnBuilder<Double> totalImpsColumn = col.column(
					"", "Total_Imps__c",
					type.doubleType()).setHorizontalAlignment(
					HorizontalAlignment.CENTER);
			totalImpsColumn.setWidth(Units.inch(2.5));
			report.addColumn(totalImpsColumn);
			TextColumnBuilder<Double> weekklyTRPColumn = col.column(
					"", "In_Mkt_TRP__c",
					type.doubleType()).setHorizontalAlignment(
					HorizontalAlignment.CENTER);
			TextColumnBuilder<Double> frequencyColumn = col.column(
					"",
					"Plan_Imps_Avg_Frequency__c", type.doubleType())
					.setHorizontalAlignment(HorizontalAlignment.CENTER);
			TextColumnBuilder<BigDecimal> totalPriceColumn = col.column(
					"", "Total_Price__c",
					currencyWithoutFractionDecimalType).setHorizontalAlignment(
					HorizontalAlignment.CENTER);
			totalPriceColumn.setWidth(Units.inch(2.5));
			report.addColumn(totalPriceColumn);
			TextColumnBuilder<BigDecimal> cppColumn = col.column(
					"", "CPP_0d__c",
					currencyWithoutFractionDecimalType).setHorizontalAlignment(
					HorizontalAlignment.CENTER);

			
			TextColumnBuilder<String> summaryColumn = col.column("Summary",
					"Package_Flight__r/Package_Name__c", type.stringType())
					.setHorizontalAlignment(HorizontalAlignment.LEFT);
			// summaryColumn.setWidth(Units.inch(3.5));
			report.addColumn(summaryColumn).setColumnTitleStyle(
					getColumnTitleStyle());
			// add columns
			/*String marketNameLabel = "";
			if (!this.isExportAsExcel()) {
				if (this.isShowSummaryHeaders()) {
					marketNameLabel = "Summary";
				} else {
					marketNameLabel = getFlightLineColumnLabelHashMap().get(
							"MarketName__c");
				}
			}
			if (getFlightLineColumnLabelHashMap().containsKey("MarketName__c")) {
				report.addColumn(
						col.column(marketNameLabel, "MarketName__c",
								type.stringType())).setColumnTitleStyle(
						getColumnTitleStyle());
			}*/
			if (getFlightLineColumnLabelHashMap().containsKey(
					"Package_Flight__r/Media_Category__c")) {
				report.addColumn(
						col.column(
								"",
								"Package_Flight__r/Media_Category__c", type.stringType()))
						.setColumnTitleStyle(getColumnTitleStyle());
			}
			if (getFlightLineColumnLabelHashMap().containsKey(
					"Number_of_Panels__c")) {
				report.addColumn(col.column(
						"",
						"Number_of_Panels__c", type.integerType())
						.setHorizontalAlignment(HorizontalAlignment.CENTER));
			}
			// * ANJALI- 12 Aug added
			if (getFlightLineColumnLabelHashMap().containsKey(
					"Package_Flight__r/Target__c")) {
				report.addColumn(col.column(
						"",
						"Package_Flight__r/Target__c", type.stringType())
						.setHorizontalAlignment(HorizontalAlignment.CENTER));
			}
			if (getFlightLineColumnLabelHashMap().containsKey(
					"Weekly_Total_18_Imps__c")) {
				weeklyTotal18ImpsColumn.setWidth(Units.inch(2.5));
				report.addColumn(weeklyTotal18ImpsColumn);
			}
			if (getFlightLineColumnLabelHashMap().containsKey("In_Mkt_Imps__c")) {
				report.addColumn(col.column(
						"", "In_Mkt_Imps__c",
						type.doubleType()).setHorizontalAlignment(
						HorizontalAlignment.CENTER));
				// 23. {Jesus} "Target Total Imps" and "Sub Total" in Audience
				// should not be rendered on the PDF detail output for Audience.
				// report.addColumn(totalImpsColumn);
			}
			if (getFlightLineColumnLabelHashMap().containsKey("In_Mkt_TRP__c")) {
				report.addColumn(weekklyTRPColumn);
			}
			if (getFlightLineColumnLabelHashMap().containsKey("PlanTRP__c")) {
				report.addColumn(col.column(
						"", "PlanTRP__c",
						type.doubleType()).setHorizontalAlignment(
						HorizontalAlignment.CENTER));
			}
			if (getFlightLineColumnLabelHashMap().containsKey(
					"Plan_Imps_Reach_Perc__c")) {
				report.addColumn(col
						.column("",
								"Plan_Imps_Reach_Perc__c",
								type.percentageType())
						.setHorizontalAlignment(HorizontalAlignment.CENTER)
						// .setPattern("#%"));
						.setPattern("###.#"));
			}
			if (getFlightLineColumnLabelHashMap().containsKey(
					"Plan_Imps_Avg_Frequency__c")) {
				report.addColumn(frequencyColumn);
			}
			if (getFlightLineColumnLabelHashMap().containsKey(
					"X4_Wk_Proposed_Price__c")) {
				report.addColumn(col.column(
						"",
						"X4_Wk_Proposed_Price__c",
						currencyWithoutFractionDecimalType)
						.setHorizontalAlignment(HorizontalAlignment.CENTER));
			}
			
			if (getFlightLineColumnLabelHashMap().containsKey(
					"Total_Price_0d__c")) {
				report.addColumn(totalPriceColumn);
			}
			if (getFlightLineColumnLabelHashMap().containsKey(
					"TotalInMarketCPM_0d__c")) {
				report.addColumn(col
						.column("",
								"TotalInMarketCPM_0d__c",
								currencyWithoutFractionDecimalType)
						.setHorizontalAlignment(HorizontalAlignment.CENTER));
			}
			if (getFlightLineColumnLabelHashMap().containsKey("CPP_0d__c")) {
				report.addColumn(cppColumn);
			}
			if (getFlightLineColumnLabelHashMap().containsKey("Comments__c")) {
				report.addColumn(col.column("",
						"Comments__c", type.stringType()));
			}
			if (isExportAsExcel()) {
				report.addColumn(col
						.column("",
								"Package_Flight__r/Flight_Comments__c",
								type.stringType()).setHorizontalAlignment(
								HorizontalAlignment.CENTER));
			}

			// add fields
			/*report.addField(field("Package_Flight__r/Id", type.stringType()));*/

			// style
			report.highlightDetailEvenRows();
			report.setColumnStyle(getColumnStyle());

			// filter
			/*report.setFilterExpression(new FilterByFlightIdExpression(
					getPackageMarketFlightPreviousRecordExpression()));*/

			// add a blank line at the end
			report.addLastPageFooter(cmp.text(""));

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
		
		public StyleBuilder getFlightHeaderStyle() {
			return flightHeaderStyle;
		}

		public void setFlightHeaderStyle(StyleBuilder flightHeaderStyle) {
			this.flightHeaderStyle = flightHeaderStyle;
		}

		public StyleBuilder getFlightHeaderValueStyle() {
			return flightHeaderValueStyle;
		}

		public void setFlightHeaderValueStyle(
				StyleBuilder flightHeaderValueStyle) {
			this.flightHeaderValueStyle = flightHeaderValueStyle;
		}

	}

	private class LocationPackageSummarySubreportExpression extends
			AbstractSimpleExpression<JasperReportBuilder> {

		private static final long serialVersionUID = 1656551952444L;

		private PackageMarketFlightPreviousRecordExpression packageMarketFlightPreviousRecordExpression;

		private StyleBuilder columnTitleStyle;

		private StyleBuilder columnStyle;

		private boolean exportAsExcel;

		private StyleBuilder flightHeaderStyle;

		private StyleBuilder flightHeaderValueStyle;

		public LocationPackageSummarySubreportExpression(
				PackageMarketFlightPreviousRecordExpression packageMarketFlightPreviousRecordExpression,
				StyleBuilder columnTitleStyle, StyleBuilder columnStyle,
				StyleBuilder flightHeaderStyle,
				StyleBuilder flightHeaderValueStyle, boolean exportAsExcel) {
			setPackageMarketFlightPreviousRecordExpression(packageMarketFlightPreviousRecordExpression);
			setColumnTitleStyle(columnTitleStyle);
			setColumnStyle(columnStyle);
			setExportAsExcel(exportAsExcel);
			setFlightHeaderStyle(flightHeaderStyle);
			setFlightHeaderValueStyle(flightHeaderValueStyle);
		}

		@Override
		public JasperReportBuilder evaluate(ReportParameters arg0) {

			JasperReportBuilder report = report();
			// types
			CurrencyWithoutFractionDecimalType currencyWithoutFractionDecimalType = new CurrencyWithoutFractionDecimalType();

			CurrencyWithFractionDecimalType currencyWithFractionDecimalType = new CurrencyWithFractionDecimalType();

			TextColumnBuilder<String> summaryColumn = col.column("Summary",	"Package_Flight__r/Package_Name__c", type.stringType())
					.setHorizontalAlignment(HorizontalAlignment.LEFT);
			summaryColumn.setWidth(Units.inch(2.0));
			report.addColumn(summaryColumn).setColumnTitleStyle(getColumnTitleStyle());
			System.out.println("Package Summary");
			TextColumnBuilder<BigDecimal> weeklyTotal18ImpsColumn = col.column("", "Weekly_Total_18_Imps__c",
					type.bigDecimalType()).setHorizontalAlignment(HorizontalAlignment.RIGHT).setPattern("#,###");

			TextColumnBuilder<BigDecimal> weeklyTotal18Imps000Column = col.column("", "Weekly_Total_18_Imps_000__c",
					type.bigDecimalType()).setHorizontalAlignment(HorizontalAlignment.RIGHT).setPattern("#,###");

			TextColumnBuilder<BigDecimal> totalImpsColumn = col.column("", "Total_Imps__c",
					type.bigDecimalType()).setHorizontalAlignment(HorizontalAlignment.RIGHT).setPattern("#,###");

			TextColumnBuilder<BigDecimal> totalImps000Column = col.column("", "Target_Total_Imps_000__c",
					type.bigDecimalType()).setHorizontalAlignment(HorizontalAlignment.RIGHT).setPattern("#,###");

			TextColumnBuilder<Double> weekklyTRPColumn = col.column("", "In_Mkt_TRP__c",
					type.doubleType()).setHorizontalAlignment(HorizontalAlignment.RIGHT).setPattern("##0.0");
			
			TextColumnBuilder<Double> frequencyColumn = col.column("", "Plan_Imps_Avg_Frequency__c",
					type.doubleType()).setHorizontalAlignment(HorizontalAlignment.RIGHT).setPattern("##0.0");
			
			TextColumnBuilder<BigDecimal> totalPriceColumn = col.column(
					getFlightLineColumnLabelHashMap().get("Total_Price_0d__c"),	"Total_Price__c",
					currencyWithoutFractionDecimalType).setHorizontalAlignment(HorizontalAlignment.RIGHT);
			
			TextColumnBuilder<BigDecimal> cppColumn = col.column("", "CPP_0d__c",
					currencyWithoutFractionDecimalType).setHorizontalAlignment(HorizontalAlignment.RIGHT);
			
			if (isExportAsExcel()) {
				report.setIgnorePageWidth(true);
				if (getFlightLineColumnLabelHashMap().containsKey("Package_Flight__r/Division__c")) {
					report.addColumn(col.column("", "Package_Flight__r/Division__c", type.stringType()).setWidth(Units.inch(1.32)));
				}
				if (getFlightLineColumnLabelHashMap().containsKey("Package_Flight__r/Market_Name__c")) {
					report.addColumn(col.column("",	"Package_Flight__r/Market_Name__c",	type.stringType()).setWidth(Units.inch(1.32)));
				}
				if (getFlightLineColumnLabelHashMap().containsKey("Package_Flight__r/Market_Type__c")) {
					report.addColumn(col.column("",	"Package_Flight__r/Market_Type__c", type.stringType()).setWidth(Units.inch(1.32)));
				}
				if (getFlightLineColumnLabelHashMap().containsKey("Package_Flight__r/Package_Name__c")) {
					report.addColumn(col.column("", "Package_Flight__r/Package_Name__c", type.stringType()).setWidth(Units.inch(1.32)));
				}
				if (getFlightLineColumnLabelHashMap().containsKey("Package_Flight__r/Name")) {
					report.addColumn(col.column("", "Package_Flight__r/Name", type.stringType()).setWidth(Units.inch(1.32)));
				}
				if (getFlightLineColumnLabelHashMap().containsKey("Package_Flight__r/Type__c")) {
					report.addColumn(col.column("", "Package_Flight__r/Type__c", type.stringType()).setWidth(Units.inch(1.32)));
				}

				if (getFlightLineColumnLabelHashMap().containsKey("Package_Flight__r/Campaign_Start_Date__c")) {
					report.addColumn(col.column("", "Package_Flight__r/Campaign_Start_Date__c",	type.stringType())
							.setHorizontalAlignment(HorizontalAlignment.CENTER).setWidth(Units.inch(1.32)).setValueFormatter(new ValueFormatter()));
				}
				if (getFlightLineColumnLabelHashMap().containsKey("Package_Flight__r/Campaign_End_Date__c")) {
					report.addColumn(col.column("", "Package_Flight__r/Campaign_End_Date__c", type.stringType())
							.setHorizontalAlignment(HorizontalAlignment.CENTER).setWidth(Units.inch(1.32)).setValueFormatter(new ValueFormatter()));
				}
				if (getFlightLineColumnLabelHashMap().containsKey("Package_Flight__r/Duration_And_Type__c")) {
					report.addColumn(col.column("", "Package_Flight__r/Duration_And_Type__c", type.stringType()).setWidth(Units.inch(1.32)));
				}
				if (getFlightLineColumnLabelHashMap().containsKey("Package_Flight__r/Target__c")) {
					report.addColumn(col.column("", "Package_Flight__r/Target__c", type.stringType())
							.setHorizontalAlignment(HorizontalAlignment.CENTER).setWidth(Units.inch(1.32)));
				}
				if(getFlightLineColumnLabelHashMap().containsKey("Package_Flight__r/Target_Population__c")) {
					report.addColumn(col.column("", "Package_Flight__r/Target_Population__c", type.integerType()).setWidth(Units.inch(1.32)));
				} 
			}
			if (getFlightLineColumnLabelHashMap().containsKey("Package_Flight__r/Media_Category__c")) {
				TextColumnBuilder<String> mediaTypeColumn = col.column("", "Package_Flight__r/Media_Category__c",	type.stringType())
						.setHorizontalAlignment(HorizontalAlignment.LEFT).setWidth(Units.inch(2.0));
				report.addColumn(mediaTypeColumn).setColumnTitleStyle(getColumnTitleStyle());
			}
			if (getFlightLineColumnLabelHashMap().containsKey("Panel_Id_Label__c")) {
				TextColumnBuilder<String> panelIdColumn = col.column("", "Panel_Id_Label__c", type.stringType())
						.setHorizontalAlignment(HorizontalAlignment.RIGHT);
				if (isExportAsExcel()) {
					panelIdColumn.setWidth(Units.inch(1.32));
				} else {
					panelIdColumn.setWidth(Units.inch(1.5));
				}
				report.addColumn(panelIdColumn);
			}
			if (getFlightLineColumnLabelHashMap().containsKey("TAB_Id__c")) {
				TextColumnBuilder<String> tabIdColumn = col.column("", "TAB_Id__c",	type.stringType())
						.setHorizontalAlignment(HorizontalAlignment.RIGHT);
				if (isExportAsExcel()) {
					tabIdColumn.setWidth(Units.inch(1.32));
				} else {
					tabIdColumn.setWidth(Units.inch(1.5));
				}
				report.addColumn(tabIdColumn);
			}
			if (getFlightLineColumnLabelHashMap().containsKey("Location_Description__c")) {
				TextColumnBuilder<String> descriptionTextColumn = col.column("", "Location_Description__c",	type.stringType())
						.setHorizontalAlignment(HorizontalAlignment.LEFT);
				if (isExportAsExcel()) {
					descriptionTextColumn.setWidth(Units.inch(1.32));
				} else {
					descriptionTextColumn.setWidth(Units.inch(2.5));
				}
				report.addColumn(descriptionTextColumn);
			}
			if (getFlightLineColumnLabelHashMap().containsKey("Face_Direction__c")) {
				TextColumnBuilder<String> flightLineTextColumn = col.column("", "Face_Direction__c", type.stringType())
						.setHorizontalAlignment(HorizontalAlignment.LEFT);
				report.addColumn(flightLineTextColumn);
				if (isExportAsExcel()) {
					flightLineTextColumn.setWidth(Units.inch(1.32));
				}
			}

			if (getFlightLineColumnLabelHashMap().containsKey("Weekly_Total_18_Imps__c")) {
				if (isExportAsExcel()) {
					weeklyTotal18ImpsColumn.setWidth(Units.inch(1.32));
				} else {
					weeklyTotal18ImpsColumn.setWidth(Units.inch(3));
				}
				report.addColumn(weeklyTotal18ImpsColumn);
			}
			if (getFlightLineColumnLabelHashMap().containsKey("Weekly_Total_18_Imps_000__c")) {
				if (isExportAsExcel()) {
					weeklyTotal18Imps000Column.setWidth(Units.inch(1.32));
				} else {
					weeklyTotal18Imps000Column.setWidth(Units.inch(3));
				}
				report.addColumn(weeklyTotal18Imps000Column);
			}
			if (getFlightLineColumnLabelHashMap().containsKey("In_Mkt_Imps__c")) {
				TextColumnBuilder<BigDecimal> targetInMarketImpsColumn = col.column("", "In_Mkt_Imps__c", type.bigDecimalType())
						.setHorizontalAlignment(HorizontalAlignment.RIGHT).setPattern("#,###");
				if (isExportAsExcel()) {
					targetInMarketImpsColumn.setWidth(Units.inch(1.32));
				} else {
					targetInMarketImpsColumn.setWidth(Units.inch(3));
				}
				report.addColumn(targetInMarketImpsColumn);
			}
			
			if (getFlightLineColumnLabelHashMap().containsKey("Target_In_Market_Imps_000__c")) {
				TextColumnBuilder<BigDecimal> targetInMarketImps000Column = col.column("", "Target_In_Market_Imps_000__c", type.bigDecimalType())
						.setHorizontalAlignment(HorizontalAlignment.RIGHT).setPattern("#,###");
				if (isExportAsExcel()) {
					targetInMarketImps000Column.setWidth(Units.inch(1.32));
				} else {
					targetInMarketImps000Column.setWidth(Units.inch(3));
				}
				report.addColumn(targetInMarketImps000Column);
			}
			if (getFlightLineColumnLabelHashMap().containsKey("Total_Imps__c")) {
				if (isExportAsExcel()) {
					totalImpsColumn.setWidth(Units.inch(1.32));
				} else {
					totalImpsColumn.setWidth(Units.inch(3));
				}
				report.addColumn(totalImpsColumn);
			}
			if (getFlightLineColumnLabelHashMap().containsKey("Target_Total_Imps_000__c")) {
				if (isExportAsExcel()) {
					totalImps000Column.setWidth(Units.inch(1.32));
				} else {
					totalImps000Column.setWidth(Units.inch(3));
				}
				report.addColumn(totalImps000Column);
			}
			
			if (getFlightLineColumnLabelHashMap().containsKey("WeeklyMarketImps__c")) {
				TextColumnBuilder<BigDecimal> weeklyTotalTargetImpsColumn = col.column("", "WeeklyMarketImps__c", type.bigDecimalType())
						.setHorizontalAlignment(HorizontalAlignment.RIGHT).setPattern("#,###");
				if (isExportAsExcel()) {
					weeklyTotalTargetImpsColumn.setWidth(Units.inch(1.32));
				} else {
					weeklyTotalTargetImpsColumn.setWidth(Units.inch(3));
				}
				report.addColumn(weeklyTotalTargetImpsColumn);
			}
			
			if (getFlightLineColumnLabelHashMap().containsKey("Weekly_Total_Target_Imps_000__c")) {
				TextColumnBuilder<BigDecimal> weeklyTotalTargetImps000Column = col.column("", "Weekly_Total_Target_Imps_000__c", type.bigDecimalType())
						.setHorizontalAlignment(HorizontalAlignment.RIGHT).setPattern("#,###");
				if (isExportAsExcel()) {
					weeklyTotalTargetImps000Column.setWidth(Units.inch(1.32));
				} else {
					weeklyTotalTargetImps000Column.setWidth(Units.inch(3));
				}
				report.addColumn(weeklyTotalTargetImps000Column);
			}
			
			if (getFlightLineColumnLabelHashMap().containsKey("WeeklyInMarketImps__c")) {
				TextColumnBuilder<BigDecimal> weeklyInMarketTargetImpsColumn = col.column("", "WeeklyInMarketImps__c", type.bigDecimalType())
						.setHorizontalAlignment(HorizontalAlignment.RIGHT).setPattern("#,###");
				if (isExportAsExcel()) {
					weeklyInMarketTargetImpsColumn.setWidth(Units.inch(1.32));
				} else {
					weeklyInMarketTargetImpsColumn.setWidth(Units.inch(3));
				}
				report.addColumn(weeklyInMarketTargetImpsColumn);
			}
			
			if (getFlightLineColumnLabelHashMap().containsKey("Weekly_In_Market_Target_Imps_000__c")) {
				TextColumnBuilder<BigDecimal> weeklyInMarketTargetImps000Column = col.column("", "Weekly_In_Market_Target_Imps_000__c",	type.bigDecimalType())
						.setHorizontalAlignment(HorizontalAlignment.RIGHT).setPattern("#,###");
				if (isExportAsExcel()) {
					weeklyInMarketTargetImps000Column.setWidth(Units.inch(1.32));
				} else {
					weeklyInMarketTargetImps000Column.setWidth(Units.inch(3));
				}
				report.addColumn(weeklyInMarketTargetImps000Column);
			}
			
			if (getFlightLineColumnLabelHashMap().containsKey("In_Mkt_TRP__c")) {
				if (isExportAsExcel()) {
					weekklyTRPColumn.setWidth(Units.inch(1.32));
				} else {
					weekklyTRPColumn.setWidth(Units.inch(3));
				}
				report.addColumn(weekklyTRPColumn);
			}
			if (getFlightLineColumnLabelHashMap().containsKey("PlanTRP__c")) {
				TextColumnBuilder<Double> planTRPColumn = col.column("", "PlanTRP__c", type.doubleType())
						.setHorizontalAlignment(HorizontalAlignment.RIGHT).setPattern("##0.0");
				if (isExportAsExcel()) {
					planTRPColumn.setWidth(Units.inch(1.32));
				} else {
					planTRPColumn.setWidth(Units.inch(2));
				}
				report.addColumn(planTRPColumn);
			}
			if (getFlightLineColumnLabelHashMap().containsKey("Plan_Imps_Reach_Perc__c")) {
				TextColumnBuilder<Double> planImpsReachPercColumn = col.column("", "Plan_Imps_Reach_Perc__c", type.percentageType())
						.setHorizontalAlignment(HorizontalAlignment.RIGHT).setPattern("##0.0");
				if (isExportAsExcel()) {
					planImpsReachPercColumn.setWidth(Units.inch(1.32));
				} else {
					planImpsReachPercColumn.setWidth(Units.inch(1.5));
				}
				report.addColumn(planImpsReachPercColumn);
			}
			if (getFlightLineColumnLabelHashMap().containsKey("Plan_Imps_Avg_Frequency__c")) {
				if (isExportAsExcel()) {
					frequencyColumn.setWidth(Units.inch(1.32));
				}
				report.addColumn(frequencyColumn);
			}

			if (getFlightLineColumnLabelHashMap().containsKey("X4_Wk_Proposed_Price__c")) {
				TextColumnBuilder<BigDecimal> X4WkProposedPriceColumn = col.column("", "X4_Wk_Proposed_Price__c",
						currencyWithoutFractionDecimalType).setHorizontalAlignment(HorizontalAlignment.RIGHT);
				if (isExportAsExcel()) {
					X4WkProposedPriceColumn.setWidth(Units.inch(1.32));
				} else {
					X4WkProposedPriceColumn.setWidth(Units.inch(2.5));
				}
				report.addColumn(X4WkProposedPriceColumn);
			}
			if (getFlightLineColumnLabelHashMap().containsKey("Net_Amount_Value__c")) {
				TextColumnBuilder<BigDecimal> subTotalPriceColumn = col.column("", "Net_Amount_Value__c",
						currencyWithoutFractionDecimalType).setHorizontalAlignment(HorizontalAlignment.RIGHT);
				if (isExportAsExcel()) {
					subTotalPriceColumn.setWidth(Units.inch(1.32));
				} else {
					subTotalPriceColumn.setWidth(Units.inch(2.5));
				}
				report.addColumn(subTotalPriceColumn);
			}
			
			if (getFlightLineColumnLabelHashMap().containsKey("Total_Price_0d__c")) {
				if (isExportAsExcel()) {
					totalPriceColumn.setWidth(Units.inch(1.32));
				} else {
					totalPriceColumn.setWidth(Units.inch(2.5));
				}
				report.addColumn(totalPriceColumn);
			}
			if (getFlightLineColumnLabelHashMap().containsKey("TotalInMarketCPM_0d__c")) {
				TextColumnBuilder<BigDecimal> totalInMarketCPM0dColumn = col.column("", "TotalInMarketCPM_0d__c",
						currencyWithFractionDecimalType).setHorizontalAlignment(HorizontalAlignment.RIGHT);
				if (isExportAsExcel()) {
					totalInMarketCPM0dColumn.setWidth(Units.inch(1.32));
				} else {
					totalInMarketCPM0dColumn.setWidth(Units.inch(2.5));
				}
				report.addColumn(totalInMarketCPM0dColumn);
			}
			if (getFlightLineColumnLabelHashMap().containsKey("CPP_0d__c")) {
				if (isExportAsExcel()) {
					cppColumn.setWidth(Units.inch(1.32));
				} else {
					cppColumn.setWidth(Units.inch(2.5));
				}
				report.addColumn(cppColumn);
			}
			if (getFlightLineColumnLabelHashMap().containsKey("Unit_Size__c")) {
				TextColumnBuilder<String> unitSizeColumn = col.column("", "Unit_Size__c", type.stringType())
						.setHorizontalAlignment(HorizontalAlignment.RIGHT);
				if (isExportAsExcel()) {
					unitSizeColumn.setWidth(Units.inch(1.32));
				} else {
					unitSizeColumn.setWidth(Units.inch(1.5));
				}
				report.addColumn(unitSizeColumn);
			}
			if (getFlightLineColumnLabelHashMap().containsKey("Illumination_yn__c")) {
				TextColumnBuilder<String> illuminationColumn = col.column("", "Illumination_yn__c",	type.stringType())
						.setHorizontalAlignment(HorizontalAlignment.LEFT);
				if (isExportAsExcel()) {
					illuminationColumn.setWidth(Units.inch(1.32));
				} else {
					illuminationColumn.setWidth(Units.inch(1));
				}
				report.addColumn(illuminationColumn);
			}
			if (getFlightLineColumnLabelHashMap().containsKey("Comments__c")) {
				TextColumnBuilder<String> commentsColumn = col.column("", "Comments__c", type.stringType())
						.setHorizontalAlignment(HorizontalAlignment.LEFT);
				if (isExportAsExcel()) {
					commentsColumn.setWidth(Units.inch(1.32));
				} else {
					commentsColumn.setWidth(Units.inch(3.5));
				}
				report.addColumn(commentsColumn);
			}
			if (isExportAsExcel()) {
				TextColumnBuilder<String> flightCommentsColumn = col.column("", "Package_Flight__r/Flight_Comments__c",	type.stringType())
						.setHorizontalAlignment(HorizontalAlignment.LEFT);
				if (isExportAsExcel()) {
					flightCommentsColumn.setWidth(Units.inch(1.32));
				} else {
					flightCommentsColumn.setWidth(Units.inch(3.5));
				}
				report.addColumn(flightCommentsColumn);
			}

			if (getFlightLineColumnLabelHashMap().containsKey("Timing__c")) {
				TextColumnBuilder<String> timingColumn = col.column("", "Timing__c", type.stringType())
						.setHorizontalAlignment(HorizontalAlignment.LEFT);
				if (isExportAsExcel()) {
					timingColumn.setWidth(Units.inch(1.32));
				} else {
					timingColumn.setWidth(Units.inch(3));
				}
				report.addColumn(timingColumn);
			}
			
			if (getFlightLineColumnLabelHashMap().containsKey("In_Mkt_Perc_Comp__c")) {
				TextColumnBuilder<BigDecimal> inMktPercCompColumn = col.column("", "In_Mkt_Perc_Comp__c", type.bigDecimalType())
						.setHorizontalAlignment(HorizontalAlignment.LEFT).setPattern("#,##0.00");
				if (isExportAsExcel()) {
					inMktPercCompColumn.setWidth(Units.inch(1.32));
				} else {
					inMktPercCompColumn.setWidth(Units.inch(1.5));
				}
				report.addColumn(inMktPercCompColumn);
			}
			
			if (getFlightLineColumnLabelHashMap().containsKey("X4_Wk_Avg_Rate_per_Panel__c")) {
				TextColumnBuilder<String> x4WkAverageRatePanelColumn = col.column("", "X4_Wk_Avg_Rate_per_Panel__c", type.stringType())
						.setHorizontalAlignment(HorizontalAlignment.RIGHT);
				if (isExportAsExcel()) {
					x4WkAverageRatePanelColumn.setWidth(Units.inch(1.32));
				} else {
					x4WkAverageRatePanelColumn.setWidth(Units.inch(1.5));
				}
				report.addColumn(x4WkAverageRatePanelColumn);
			}
			
			if (getFlightLineColumnLabelHashMap().containsKey("Production__c")) {
				TextColumnBuilder<BigDecimal> productionColumn = col.column("", "Production__c",
						currencyWithFractionDecimalType).setHorizontalAlignment(HorizontalAlignment.RIGHT);
				if (isExportAsExcel()) {
					productionColumn.setWidth(Units.inch(1.32));
				} else {
					productionColumn.setWidth(Units.inch(1.5));
				}
				report.addColumn(productionColumn);
			}
			
			if (getFlightLineColumnLabelHashMap().containsKey("Additional_Cost__c")) {
				TextColumnBuilder<BigDecimal> additionalCostColumn = col.column("", "Additional_Cost__c",
						currencyWithFractionDecimalType).setHorizontalAlignment(HorizontalAlignment.RIGHT);
				if (isExportAsExcel()) {
					additionalCostColumn.setWidth(Units.inch(1.32));
				} else {
					additionalCostColumn.setWidth(Units.inch(1.5));
				}
				report.addColumn(additionalCostColumn);
			}
			
			if (getFlightLineColumnLabelHashMap().containsKey("Tax_Amt__c")) {
				TextColumnBuilder<BigDecimal> taxAmtColumn = col.column("", "Tax_Amt__c",
						currencyWithFractionDecimalType).setHorizontalAlignment(HorizontalAlignment.RIGHT);
				if (isExportAsExcel()) {
					taxAmtColumn.setWidth(Units.inch(1.32));
				} else {
					taxAmtColumn.setWidth(Units.inch(1.5));
				}
				report.addColumn(taxAmtColumn);
			}
			
			if (getFlightLineColumnLabelHashMap().containsKey("Location__Longitude__s")) {
				TextColumnBuilder<String> locationLangitudeColumn = col.column("", "Location__Longitude__s", type.stringType())
						.setHorizontalAlignment(HorizontalAlignment.LEFT);
				if (isExportAsExcel()) {
					locationLangitudeColumn.setWidth(Units.inch(1.32));
				} else {
					locationLangitudeColumn.setWidth(Units.inch(1));
				}
				report.addColumn(locationLangitudeColumn);
			}
			
			if (getFlightLineColumnLabelHashMap().containsKey("Location__Latitude__s")) {
				TextColumnBuilder<String> locationLatitudeColumn = col.column("", "Location__Latitude__s", type.stringType())
						.setHorizontalAlignment(HorizontalAlignment.LEFT);
				if (isExportAsExcel()) {
					locationLatitudeColumn.setWidth(Units.inch(1.32));
				} else {
					locationLatitudeColumn.setWidth(Units.inch(1));
				}
				report.addColumn(locationLatitudeColumn);
			}
			
			if (getFlightLineColumnLabelHashMap().containsKey("Embellishments__c")) {
				TextColumnBuilder<String> embellishmentsColumn = col.column("", "Embellishments__c", type.stringType())
						.setHorizontalAlignment(HorizontalAlignment.LEFT);
				if (isExportAsExcel()) {
					embellishmentsColumn.setWidth(Units.inch(1.32));
				} else {
					embellishmentsColumn.setWidth(Units.inch(1));
				}
				report.addColumn(embellishmentsColumn);
			}
			
			if (getFlightLineColumnLabelHashMap().containsKey("Illumination__c")) {
				TextColumnBuilder<BigDecimal> illuminationColumn = col.column("", "Illumination__c",
						currencyWithoutFractionDecimalType).setHorizontalAlignment(HorizontalAlignment.RIGHT);
				if (isExportAsExcel()) {
					illuminationColumn.setWidth(Units.inch(1.32));
				} else {
					illuminationColumn.setWidth(Units.inch(1));
				}
				report.addColumn(illuminationColumn);
			}
			
			if (getFlightLineColumnLabelHashMap().containsKey("Current_Copy__c")) {
				TextColumnBuilder<String> currentCopyColumn = col.column("", "Current_Copy__c",	type.stringType())
						.setHorizontalAlignment(HorizontalAlignment.LEFT);
				if (isExportAsExcel()) {
					currentCopyColumn.setWidth(Units.inch(1.32));
				} else {
					currentCopyColumn.setWidth(Units.inch(1));
				}
				report.addColumn(currentCopyColumn);
			}
			
			if (getFlightLineColumnLabelHashMap().containsKey("City__c")) {
				TextColumnBuilder<String> cityColumn = col.column("", "City__c", type.stringType())
						.setHorizontalAlignment(HorizontalAlignment.LEFT);
				if (isExportAsExcel()) {
					cityColumn.setWidth(Units.inch(1.32));
				} else {
					cityColumn.setWidth(Units.inch(1.5));
				}
				report.addColumn(cityColumn);
			}
			
			if (getFlightLineColumnLabelHashMap().containsKey("County__c")) {
				TextColumnBuilder<String> countryColumn = col.column("", "County__c", type.stringType())
						.setHorizontalAlignment(HorizontalAlignment.LEFT);
				if (isExportAsExcel()) {
					countryColumn.setWidth(Units.inch(1.32));
				} else {
					countryColumn.setWidth(Units.inch(1.5));
				}
				report.addColumn(countryColumn);
			}
			
			if (getFlightLineColumnLabelHashMap().containsKey("State__c")) {
				TextColumnBuilder<String> stateColumn = col.column("", "State__c", type.stringType())
						.setHorizontalAlignment(HorizontalAlignment.LEFT);
				if (isExportAsExcel()) {
					stateColumn.setWidth(Units.inch(1.32));
				} else {
					stateColumn.setWidth(Units.inch(1.5));
				}
				report.addColumn(stateColumn);
			}
			
			if (getFlightLineColumnLabelHashMap().containsKey("Zip__c")) {
				TextColumnBuilder<String> zipColumn = col.column("", "Zip__c", type.stringType())
						.setHorizontalAlignment(HorizontalAlignment.LEFT);
				if (isExportAsExcel()) {
					zipColumn.setWidth(Units.inch(1.32));
				} else {
					zipColumn.setWidth(Units.inch(1.5));
				}
				report.addColumn(zipColumn);
			}
			if (getFlightLineColumnLabelHashMap().containsKey("Media_Product__c")) {
				TextColumnBuilder<String> mediaProductColumn = col.column("", "Media_Product__c", type.stringType())
						.setHorizontalAlignment(HorizontalAlignment.LEFT);
				if (isExportAsExcel()) {
					mediaProductColumn.setWidth(Units.inch(1.32));
				} else {
					mediaProductColumn.setWidth(Units.inch(1.5));
				}
				report.addColumn(mediaProductColumn);;
			}
			
			if (getFlightLineColumnLabelHashMap().containsKey("Ride_Order__c")) {
				TextColumnBuilder<String> rideOrderColumn = col.column("", "Ride_Order__c",	type.stringType())
						.setHorizontalAlignment(HorizontalAlignment.LEFT);
				if (isExportAsExcel()) {
					rideOrderColumn.setWidth(Units.inch(1.32));
				} else {
					rideOrderColumn.setWidth(Units.inch(1.5));
				}
				report.addColumn(rideOrderColumn);
			}
			if (getFlightLineColumnLabelHashMap().containsKey("Facing__c")) {
				TextColumnBuilder<String> faceSideColumn = col.column("", "Facing__c", type.stringType())
						.setHorizontalAlignment(HorizontalAlignment.LEFT);
				if (isExportAsExcel()) {
					faceSideColumn.setWidth(Units.inch(1.32));
				} else {
					faceSideColumn.setWidth(Units.inch(1.5));
				}
				report.addColumn(faceSideColumn);
			}
			if (isExportAsExcel()) {
				if (getFlightLineColumnLabelHashMap().containsKey("X4_Wk_Base_Rate__c")) {
					TextColumnBuilder<BigDecimal> x4WkBaseRateColumn = col.column("", "X4_Wk_Base_Rate__c",
							currencyWithoutFractionDecimalType).setHorizontalAlignment(HorizontalAlignment.RIGHT).setWidth(Units.inch(1.32));
					report.addColumn(x4WkBaseRateColumn);
				}
				if (getFlightLineColumnLabelHashMap().containsKey("X4_Wk_Floor__c")) {
					TextColumnBuilder<BigDecimal> x4WkFloorColumn = col.column("", "X4_Wk_Floor__c",
							currencyWithoutFractionDecimalType).setHorizontalAlignment(HorizontalAlignment.RIGHT).setWidth(Units.inch(1.32));
					report.addColumn(x4WkFloorColumn);
				}
			}
			if(getPackageMarketFlightPreviousRecordExpression().isDigitalMediaCategory()) {
				if (getFlightLineColumnLabelHashMap().containsKey("Average_Daily_Spots__c")) {
					TextColumnBuilder<String> averageDailySpotsColumn = col.column("", "Average_Daily_Spots__c", type.stringType())
							.setHorizontalAlignment(HorizontalAlignment.RIGHT);
					if (isExportAsExcel()) {
						averageDailySpotsColumn.setWidth(Units.inch(1.32));
					} else {
						averageDailySpotsColumn.setWidth(Units.inch(1.5));
					}
					report.addColumn(averageDailySpotsColumn);
				}
			}

			// style
			if (!isExportAsExcel()) {
				report.highlightDetailEvenRows();
			}
			report.setColumnStyle(getColumnStyle());

			// add a blank line at the end
			report.addLastPageFooter(cmp.text(""));

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

		public StyleBuilder getFlightHeaderStyle() {
			return flightHeaderStyle;
		}

		public void setFlightHeaderStyle(StyleBuilder flightHeaderStyle) {
			this.flightHeaderStyle = flightHeaderStyle;
		}

		public StyleBuilder getFlightHeaderValueStyle() {
			return flightHeaderValueStyle;
		}

		public void setFlightHeaderValueStyle(
				StyleBuilder flightHeaderValueStyle) {
			this.flightHeaderValueStyle = flightHeaderValueStyle;
		}
	}

	private class PackageSummarySubreportExpression extends
			AbstractSimpleExpression<JasperReportBuilder> {

		private static final long serialVersionUID = 1656551952524L;

		private PackageMarketFlightPreviousRecordExpression packageMarketFlightPreviousRecordExpression;

		private StyleBuilder columnTitleStyle;

		private StyleBuilder columnStyle;

		private boolean exportAsExcel;

		private boolean showSummaryHeaders;

		private StyleBuilder flightHeaderStyle;

		private StyleBuilder flightHeaderValueStyle;

		public PackageSummarySubreportExpression(
				PackageMarketFlightPreviousRecordExpression packageMarketFlightPreviousRecordExpression,
				StyleBuilder columnTitleStyle, StyleBuilder columnStyle,
				StyleBuilder flightHeaderStyle,
				StyleBuilder flightHeaderValueStyle, boolean exportAsExcel,
				boolean showSummaryHeaders) {
			setPackageMarketFlightPreviousRecordExpression(packageMarketFlightPreviousRecordExpression);
			setColumnTitleStyle(columnTitleStyle);
			setColumnStyle(columnStyle);
			setExportAsExcel(exportAsExcel);
			setShowSummaryHeaders(showSummaryHeaders);
			setFlightHeaderStyle(flightHeaderStyle);
			setFlightHeaderValueStyle(flightHeaderValueStyle);
		}

		@Override
		public JasperReportBuilder evaluate(ReportParameters arg0) {
			JasperReportBuilder report = report();

			// types
			CurrencyWithoutFractionDecimalType currencyWithoutFractionDecimalType = new CurrencyWithoutFractionDecimalType();

			System.out.println("Package summary");
			if (isExportAsExcel()) {
				report.setIgnorePageWidth(true);
			}
			TextColumnBuilder<String> summaryColumn = col.column("Summary",
					"Package_Flight__r/Package_Name__c", type.stringType())
					.setHorizontalAlignment(HorizontalAlignment.LEFT);
			summaryColumn.setWidth(Units.inch(1.32));
			report.addColumn(summaryColumn).setColumnTitleStyle(
					getColumnTitleStyle());
			TextColumnBuilder<BigDecimal> totalPriceColumn = col.column(
					getFlightLineColumnLabelHashMap().get("Total_Price_0d__c"),
					"Total_Price__c", currencyWithoutFractionDecimalType)
					.setHorizontalAlignment(HorizontalAlignment.RIGHT);
			if (getFlightLineColumnLabelHashMap().containsKey(
					"Total_Price_0d__c")) {
				if (isExportAsExcel()) {
					totalPriceColumn.setWidth(Units.inch(1.32));
				} else {
					totalPriceColumn.setWidth(Units.inch(2.5));
				}
				report.addColumn(totalPriceColumn);
			}

			// style
			report.highlightDetailEvenRows();
			report.setColumnStyle(getColumnStyle());

			// filter

			// add a blank line at the end
			report.addLastPageFooter(cmp.text(""));

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

		public StyleBuilder getFlightHeaderStyle() {
			return flightHeaderStyle;
		}

		public void setFlightHeaderStyle(StyleBuilder flightHeaderStyle) {
			this.flightHeaderStyle = flightHeaderStyle;
		}

		public StyleBuilder getFlightHeaderValueStyle() {
			return flightHeaderValueStyle;
		}

		public void setFlightHeaderValueStyle(
				StyleBuilder flightHeaderValueStyle) {
			this.flightHeaderValueStyle = flightHeaderValueStyle;
		}
	}

	/************************** jitendra *****************************/

	
 private class ValidDisclaimerExistsExpression extends AbstractSimpleExpression<Boolean> {
	
	private static final long serialVersionUID = 1632549537807166153L;
	
	public ValidDisclaimerExistsExpression() {
		
	}
	
	@Override
	public Boolean evaluate(ReportParameters reportParameters) {
		System.out
		.println("******** ValidDisclaimerExistsExpression evaluate.validDisclaimersSet exists "
				+ validDisclaimersSet.size());
		return validDisclaimersSet != null && validDisclaimersSet.size() >0;
		
	}
 }
	private class PackageMarketFlightPreviousRecordExpression extends
			AbstractSimpleExpression<String> {
		private static final long serialVersionUID = 549979415L;

		private Integer m_reportRowNumber = 0;

		String m_lastIdValue = null;

		String m_lastBuyTypeValue = null;

		String m_lastMediaCategoryValue = null;

		String m_lastMarketNameValue = null;

		@Override
		public String evaluate(ReportParameters reportParameters) {

			Integer reportRowNumber = reportParameters.getReportRowNumber();

			if (reportRowNumber > this.m_reportRowNumber) {
				// System.out.println("PackageMarketFlightPreviousRecordExpression.evaluate");

				this.m_reportRowNumber = reportRowNumber;

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

	private class AudienceSubreportExpression extends
			AbstractSimpleExpression<JasperReportBuilder> {

		private static final long serialVersionUID = 1656551952324L;

		private PackageMarketFlightPreviousRecordExpression packageMarketFlightPreviousRecordExpression;

		private StyleBuilder columnTitleStyle;

		private StyleBuilder columnStyle;

		private boolean exportAsExcel;

		private boolean showSummaryHeaders;

		private StyleBuilder flightHeaderStyle;

		private StyleBuilder flightHeaderValueStyle;

		public AudienceSubreportExpression(
				PackageMarketFlightPreviousRecordExpression packageMarketFlightPreviousRecordExpression,
				StyleBuilder columnTitleStyle, StyleBuilder columnStyle,
				StyleBuilder flightHeaderStyle, StyleBuilder flightHeaderValueStyle, boolean exportAsExcel, boolean showSummaryHeaders) {
			setPackageMarketFlightPreviousRecordExpression(packageMarketFlightPreviousRecordExpression);
			setColumnTitleStyle(columnTitleStyle);
			setColumnStyle(columnStyle);
			setExportAsExcel(exportAsExcel);
			setShowSummaryHeaders(showSummaryHeaders);
			setFlightHeaderStyle(flightHeaderStyle);
			setFlightHeaderValueStyle(flightHeaderValueStyle);
		}

		@Override
		public JasperReportBuilder evaluate(ReportParameters arg0) {
			JasperReportBuilder report = report();

			// types
			CurrencyWithoutFractionDecimalType currencyWithoutFractionDecimalType = new CurrencyWithoutFractionDecimalType();

			// ======================================================== begin
			if(!this.isShowSummaryHeaders() && !this.isExportAsExcel()) {
				// add flight fields
				addFlightFields(report);

				// header label and field
				String[] audienceHeaderFieldNamesArray = new String[] {
						"Package_Flight__r/Division__c",
						"Package_Flight__r/Market_Type__c",
						"Package_Flight__r/Package_Name__c",
						"Package_Flight__r/Name",
						"Package_Flight__r/Campaign_Start_Date__c",
						"Package_Flight__r/Campaign_End_Date__c",
						"Package_Flight__r/Duration_And_Type__c",
						"Package_Flight__r/Target__c",
						"Package_Flight__r/Target_Population__c" };
				String[] audienceHeaderFieldLabelsArray = new String[] {
						getFlightLineColumnLabelHashMap().get("Package_Flight__r/Division__c"),
						getFlightLineColumnLabelHashMap().get("Package_Flight__r/Market_Type__c"),
						getFlightLineColumnLabelHashMap().get("Package_Flight__r/Package_Name__c"),
						getFlightLineColumnLabelHashMap().get("Package_Flight__r/Name"),
						getFlightLineColumnLabelHashMap().get("Package_Flight__r/Campaign_Start_Date__c"),
						getFlightLineColumnLabelHashMap().get("Package_Flight__r/Campaign_End_Date__c"),
						getFlightLineColumnLabelHashMap().get("Package_Flight__r/Duration_And_Type__c"),
						getFlightLineColumnLabelHashMap().get("Package_Flight__r/Target__c"),
						getFlightLineColumnLabelHashMap().get("Package_Flight__r/Target_Population__c") };
				report.title(createVerticalTable(getFlightHeaderStyle(),
						getFlightHeaderValueStyle(), audienceHeaderFieldNamesArray,
						audienceHeaderFieldLabelsArray, Units.inch(2)));
			}

			// ======================================================== end


			// build columns that are to be summarized
			String weeklyTotal18ImplsHeaderLabel = "";
			if(exportAsExcel) {
				weeklyTotal18ImplsHeaderLabel = getFlightLineColumnLabelHashMap().get(
						"Weekly_Total_18_Imps__c");
			}
			
			String weeklyTotalImplsHeaderLabel = "";
			if(exportAsExcel) {
				weeklyTotalImplsHeaderLabel = getFlightLineColumnLabelHashMap().get(
						"Total_Imps__c");
			}
			
			String weeklyInMktTRPHeaderLabel = "";
			if(exportAsExcel) {
				weeklyInMktTRPHeaderLabel = getFlightLineColumnLabelHashMap().get(
						"In_Mkt_TRP__c");
			}
			String freqHeaderLabel = "";
			if(exportAsExcel) {
				freqHeaderLabel = getFlightLineColumnLabelHashMap().get(
						"Plan_Imps_Avg_Frequency__c");
			}
			String totalPriceHeaderLabel = "";
			if(exportAsExcel) {
				totalPriceHeaderLabel = getFlightLineColumnLabelHashMap().get(
						"Total_Price_0d__c");
			}
			String cppHeaderLabel = "";
			if(exportAsExcel) {
				cppHeaderLabel = getFlightLineColumnLabelHashMap().get(
						"CPP_0d__c");
			}
			String InMktImpsHeaderLabel = "";
			if(exportAsExcel) {
				InMktImpsHeaderLabel = getFlightLineColumnLabelHashMap().get(
						"In_Mkt_Imps__c");
			}
			String planTRPHeaderLabel = "";
			if(exportAsExcel) {
				planTRPHeaderLabel = getFlightLineColumnLabelHashMap().get(
						"PlanTRP__c");
			}
			String reachPercHeaderLabel = "";
			if(exportAsExcel) {
				reachPercHeaderLabel = getFlightLineColumnLabelHashMap().get(
						"Plan_Imps_Reach_Perc__c");
			}
			String X4WkProposedPriceHeaderLabel = "";
			if(exportAsExcel) {
				X4WkProposedPriceHeaderLabel = getFlightLineColumnLabelHashMap().get(
						"X4_Wk_Proposed_Price__c");
			}
			
			String totalInMarketCPMHeaderLabel = "";
			if(exportAsExcel) {
				totalInMarketCPMHeaderLabel = getFlightLineColumnLabelHashMap().get(
						"TotalInMarketCPM_0d__c");
			}
			// build columns that are to be summarized
			TextColumnBuilder<Integer> weeklyTotal18ImpsColumn = col.column(
					this.isShowSummaryHeaders() ? weeklyTotal18ImplsHeaderLabel : getFlightLineColumnLabelHashMap().get(
							"Weekly_Total_18_Imps__c"),
					"Weekly_Total_18_Imps__c", type.integerType())
					.setHorizontalAlignment(HorizontalAlignment.CENTER)
					;
				weeklyTotal18ImpsColumn.setWidth(Units.inch(2.5));
				report.addColumn(weeklyTotal18ImpsColumn);
				
			TextColumnBuilder<Double> totalImpsColumn = col.column(
					this.isShowSummaryHeaders() ? weeklyTotalImplsHeaderLabel : getFlightLineColumnLabelHashMap().get("Total_Imps__c"),
					"Total_Imps__c", type.doubleType())
					.setHorizontalAlignment(HorizontalAlignment.CENTER);
			totalImpsColumn.setWidth(Units.inch(2.5));
			report.addColumn(totalImpsColumn);
			TextColumnBuilder<Double> weekklyTRPColumn = col.column(
					this.isShowSummaryHeaders() ? weeklyInMktTRPHeaderLabel : getFlightLineColumnLabelHashMap().get("In_Mkt_TRP__c"),
					"In_Mkt_TRP__c", type.doubleType())
					.setHorizontalAlignment(HorizontalAlignment.CENTER);
			TextColumnBuilder<Double> frequencyColumn = col.column(
					this.isShowSummaryHeaders() ? freqHeaderLabel : getFlightLineColumnLabelHashMap().get(
							"Plan_Imps_Avg_Frequency__c"),
					"Plan_Imps_Avg_Frequency__c", type.doubleType())
					.setHorizontalAlignment(HorizontalAlignment.CENTER);
			TextColumnBuilder<BigDecimal> totalPriceColumn = col.column(
					this.isShowSummaryHeaders() ? totalPriceHeaderLabel : getFlightLineColumnLabelHashMap().get("Total_Price_0d__c"),
					"Total_Price_0d__c", currencyWithoutFractionDecimalType)
					.setHorizontalAlignment(HorizontalAlignment.CENTER);
			totalPriceColumn.setWidth(Units.inch(2.5));
			report.addColumn(totalPriceColumn);
			TextColumnBuilder<BigDecimal> cppColumn = col.column(
					this.isShowSummaryHeaders() ? cppHeaderLabel : getFlightLineColumnLabelHashMap().get("CPP_0d__c"),
					"CPP_0d__c", currencyWithoutFractionDecimalType)
					.setHorizontalAlignment(HorizontalAlignment.CENTER);

		//	report.setIgnorePageWidth(true);
			
			if (isExportAsExcel()) {
				//report.setIgnorePageWidth(true);
				if(getFlightLineColumnLabelHashMap().containsKey("Package_Flight__r/Package_Name__c")) {
					report.addColumn(col.column(this.isShowSummaryHeaders() ? "Summary" : getFlightLineColumnLabelHashMap().get("Package_Flight__r/Package_Name__c"),
							"Package_Flight__r/Package_Name__c", type.stringType()));
				}
				if(getFlightLineColumnLabelHashMap().containsKey("Package_Flight__r/Name")) {
					report.addColumn(col.column(this.isShowSummaryHeaders() ? "" : getFlightLineColumnLabelHashMap().get("Package_Flight__r/Name"),
							"Package_Flight__r/Name", type.stringType()));
				}
				if(getFlightLineColumnLabelHashMap().containsKey("Package_Flight__r/Division__c")) {
					report.addColumn(col.column(this.isShowSummaryHeaders() ? "" : getFlightLineColumnLabelHashMap().get("Package_Flight__r/Division__c"),
							"Package_Flight__r/Division__c", type.stringType()));
				}
				if(getFlightLineColumnLabelHashMap().containsKey("Package_Flight__r/Market_Name__c")) {
					report.addColumn(col.column(this.isShowSummaryHeaders() ? "" : getFlightLineColumnLabelHashMap().get("Package_Flight__r/Market_Name__c"),
							"Package_Flight__r/Market_Name__c", type.stringType()));
				}
				//I-62210 - Anjali 12 Aug 2013- Market Type not in audience layout, media type shud come at this place instead

				//if(getFlightLineColumnLabelHashMap().containsKey("Package_Flight__r/Market_Type__c")) {
					//report.addColumn(col.column(this.isShowSummaryHeaders() ? "" : getFlightLineColumnLabelHashMap().get("Package_Flight__r/Market_Type__c"),
							//"Package_Flight__r/Market_Type__c", type.stringType()));
				//}
				if(getFlightLineColumnLabelHashMap().containsKey("Package_Flight__r/Campaign_Start_Date__c")) {
					report.addColumn(col.column(this.isShowSummaryHeaders() ? "" : getFlightLineColumnLabelHashMap().get("Package_Flight__r/Campaign_Start_Date__c"),
							"Package_Flight__r/Campaign_Start_Date__c",
							type.stringType())
							.setHorizontalAlignment(HorizontalAlignment.CENTER));
				}
				if(getFlightLineColumnLabelHashMap().containsKey("Package_Flight__r/Campaign_End_Date__c")) {
					report.addColumn(col.column(this.isShowSummaryHeaders() ? "" : getFlightLineColumnLabelHashMap().get("Package_Flight__r/Campaign_End_Date__c"),
							"Package_Flight__r/Campaign_End_Date__c",
							type.stringType())
							.setHorizontalAlignment(HorizontalAlignment.CENTER));
				}
				if(getFlightLineColumnLabelHashMap().containsKey("Package_Flight__r/Duration_And_Type__c")) {
					report.addColumn(col.column(this.isShowSummaryHeaders() ? "" : getFlightLineColumnLabelHashMap().get("Package_Flight__r/Duration_And_Type__c"),
							"Package_Flight__r/Duration_And_Type__c",
							type.stringType())
							.setHorizontalAlignment(HorizontalAlignment.CENTER));
				}
			}

			// add columns
			String marketNameLabel = "";
			if(!this.isExportAsExcel()) {
				if(this.isShowSummaryHeaders()) {
					marketNameLabel = "Summary";
				} else {
					marketNameLabel = getFlightLineColumnLabelHashMap().get("MarketName__c");
				}
			}
			if(getFlightLineColumnLabelHashMap().containsKey("MarketName__c")) {
				report.addColumn(
					col.column(
						marketNameLabel,
						"MarketName__c",
						type.stringType())).setColumnTitleStyle(getColumnTitleStyle());
			}
			if(getFlightLineColumnLabelHashMap().containsKey("Package_Flight__r/Media_Category__c")) {
				report.addColumn(
					col.column(
					this.isShowSummaryHeaders() ?
						"" : getFlightLineColumnLabelHashMap().get("Package_Flight__r/Media_Category__c"),
					"Package_Flight__r/Media_Category__c",
					type.stringType())).setColumnTitleStyle(getColumnTitleStyle());
			}
			if(getFlightLineColumnLabelHashMap().containsKey("Number_of_Panels__c")) {
				report.addColumn(col.column(
						this.isShowSummaryHeaders() ?
							"" : getFlightLineColumnLabelHashMap().get("Number_of_Panels__c"),
						"Number_of_Panels__c",
						type.integerType())
						.setHorizontalAlignment(HorizontalAlignment.CENTER));
			}
			// *	ANJALI- 12 Aug added 
			if(getFlightLineColumnLabelHashMap().containsKey("Package_Flight__r/Target__c")) {
			report.addColumn(col.column(
					this.isShowSummaryHeaders() ? "" : getFlightLineColumnLabelHashMap().get(
							"Package_Flight__r/Target__c"),
					"Package_Flight__r/Target__c", type.stringType())
					.setHorizontalAlignment(HorizontalAlignment.CENTER));
			}
			if(getFlightLineColumnLabelHashMap().containsKey("Weekly_Total_18_Imps__c")) {
				weeklyTotal18ImpsColumn.setWidth(Units.inch(2.5));
				report.addColumn(weeklyTotal18ImpsColumn);
			}
			if(getFlightLineColumnLabelHashMap().containsKey("In_Mkt_Imps__c")) {
				report.addColumn(col.column(
						this.isShowSummaryHeaders() ?
								InMktImpsHeaderLabel : getFlightLineColumnLabelHashMap().get("In_Mkt_Imps__c"),
						"In_Mkt_Imps__c", type.doubleType())
						.setHorizontalAlignment(HorizontalAlignment.CENTER));
			// 23. {Jesus} "Target Total Imps" and "Sub Total" in Audience should not be rendered on the PDF detail output for Audience.
//			report.addColumn(totalImpsColumn);
			}
			if(getFlightLineColumnLabelHashMap().containsKey("In_Mkt_TRP__c")) {
				report.addColumn(weekklyTRPColumn);
			}
			if(getFlightLineColumnLabelHashMap().containsKey("PlanTRP__c")) {
				report.addColumn(col.column(
						this.isShowSummaryHeaders() ?
								planTRPHeaderLabel : getFlightLineColumnLabelHashMap().get("PlanTRP__c"),
						"PlanTRP__c", type.doubleType())
						.setHorizontalAlignment(HorizontalAlignment.CENTER));
			}
			if(getFlightLineColumnLabelHashMap().containsKey("Plan_Imps_Reach_Perc__c")) {
				report.addColumn(col.column(
						this.isShowSummaryHeaders() ?
								reachPercHeaderLabel : getFlightLineColumnLabelHashMap().get("Plan_Imps_Reach_Perc__c"),
						"Plan_Imps_Reach_Perc__c", type.percentageType())
						.setHorizontalAlignment(HorizontalAlignment.CENTER)
					//.setPattern("#%"));
					.setPattern("###.#"));
			}
			if(getFlightLineColumnLabelHashMap().containsKey("Plan_Imps_Avg_Frequency__c")) {
				report.addColumn(frequencyColumn);
			}
			if(getFlightLineColumnLabelHashMap().containsKey("X4_Wk_Proposed_Price__c")) {
				report.addColumn(col.column(
						this.isShowSummaryHeaders() ?
							"" : getFlightLineColumnLabelHashMap().get("X4_Wk_Proposed_Price__c"),
						"X4_Wk_Proposed_Price__c", currencyWithoutFractionDecimalType)
						.setHorizontalAlignment(HorizontalAlignment.CENTER));
			}
			// 23. {Jesus} "Target Total Imps" and "Sub Total" in Audience should not be rendered on the PDF detail output for Audience.
//			report.addColumn(col.column(
//					getFlightLineColumnLabelHashMap()
//							.get("Net_Amount_Value__c"), "Net_Amount_Value__c",
//					type.doubleType())
//					.setHorizontalAlignment(HorizontalAlignment.CENTER));
			if(getFlightLineColumnLabelHashMap().containsKey("Total_Price_0d__c")) {
				report.addColumn(totalPriceColumn);
			}
			if(getFlightLineColumnLabelHashMap().containsKey("TotalInMarketCPM_0d__c")) {
				report.addColumn(col.column(
						this.isShowSummaryHeaders() ?
								totalInMarketCPMHeaderLabel : getFlightLineColumnLabelHashMap().get("TotalInMarketCPM_0d__c"),
						"TotalInMarketCPM_0d__c", currencyWithoutFractionDecimalType)
						.setHorizontalAlignment(HorizontalAlignment.CENTER));
			}
			if(getFlightLineColumnLabelHashMap().containsKey("CPP_0d__c")) {
				report.addColumn(cppColumn);
			}
			if(getFlightLineColumnLabelHashMap().containsKey("Comments__c")) {
				report.addColumn(col.column(
						this.isShowSummaryHeaders() ?
								"" : getFlightLineColumnLabelHashMap().get("Comments__c"),
						"Comments__c", type.stringType()));
			}
			if (isExportAsExcel()) {
				report.addColumn(col.column(this.isShowSummaryHeaders() ? "" : 
					getFlightLineColumnLabelHashMap().get("Package_Flight__r/Flight_Comments__c"),
					"Package_Flight__r/Flight_Comments__c", type.stringType()).setHorizontalAlignment(HorizontalAlignment.CENTER));
				if (getFlightLineColumnLabelHashMap().containsKey("X4_Wk_Base_Rate__c")) {
					TextColumnBuilder<BigDecimal> x4WkBaseRateColumn = col
							.column(this.isShowSummaryHeaders() ? ""
									: getFlightLineColumnLabelHashMap().get("X4_Wk_Base_Rate__c"), "X4_Wk_Base_Rate__c",
									currencyWithoutFractionDecimalType)
									.setHorizontalAlignment(HorizontalAlignment.RIGHT).setWidth(Units.inch(1.32));
					report.addColumn(x4WkBaseRateColumn);
				}
				if (getFlightLineColumnLabelHashMap().containsKey("X4_Wk_Floor__c")) {
					TextColumnBuilder<BigDecimal> x4WkFloorColumn = col
							.column(this.isShowSummaryHeaders() ? ""
									: getFlightLineColumnLabelHashMap().get("X4_Wk_Floor__c"), "X4_Wk_Floor__c",
									currencyWithoutFractionDecimalType)
									.setHorizontalAlignment(HorizontalAlignment.RIGHT).setWidth(Units.inch(1.32));
					report.addColumn(x4WkFloorColumn);
				}
			}
			

			// add fields
			report.addField(field("Package_Flight__r/Id", type.stringType()));

			// style
			report.highlightDetailEvenRows();
			report.setColumnStyle(getColumnStyle());

			// filter
			report.setFilterExpression(new FilterByFlightIdExpression(
					getPackageMarketFlightPreviousRecordExpression()));

			// add a blank line at the end
			report.addLastPageFooter(cmp.text(""));

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
	private class LocationSubreportExpression extends
			AbstractSimpleExpression<JasperReportBuilder> {

		private static final long serialVersionUID = 1656551952324L;

		private PackageMarketFlightPreviousRecordExpression packageMarketFlightPreviousRecordExpression;

		private StyleBuilder columnTitleStyle;

		private StyleBuilder columnStyle;

		private boolean exportAsExcel;

		private boolean showSummaryHeaders;

		private StyleBuilder flightHeaderStyle;

		private StyleBuilder flightHeaderValueStyle;

		private boolean locationMapExists;
		public LocationSubreportExpression(
				PackageMarketFlightPreviousRecordExpression packageMarketFlightPreviousRecordExpression,
				StyleBuilder columnTitleStyle, StyleBuilder columnStyle,
				StyleBuilder flightHeaderStyle,
				StyleBuilder flightHeaderValueStyle, boolean exportAsExcel,
				boolean showSummaryHeaders, boolean locationMapExists) {
			setPackageMarketFlightPreviousRecordExpression(packageMarketFlightPreviousRecordExpression);
			setColumnTitleStyle(columnTitleStyle);
			setColumnStyle(columnStyle);
			setExportAsExcel(exportAsExcel);
			setShowSummaryHeaders(showSummaryHeaders);
			setFlightHeaderStyle(flightHeaderStyle);
			setFlightHeaderValueStyle(flightHeaderValueStyle);
			setLocationMapExists(locationMapExists);
		}
		
		@Override
		public JasperReportBuilder evaluate(ReportParameters arg0) {

			JasperReportBuilder report = report();
			/*Properties properties = new Properties();
            properties.put("net.sf.jasperreports.export.xls.wrap.text", "Boolean.FALSE");
            //rep_properties.put("net.sf.jasperreports.export.xls.detect.cell.type", "Boolean.TRUE");
            //rep_properties.put("net.sf.jasperreports.export.xls.max.rows.per.sheet", "-1");
            properties.put("net.sf.jasperreports.print.keep.full.text","Boolean.TRUE");
            //properties.put("net.sf.jasperreports.text.truncate.suffix"," ");
            properties.put("net.sf.jasperreports.text.truncate.at.char","Boolean.TRUE");
            report.setProperties(properties);*/
			// types
			CurrencyWithoutFractionDecimalType currencyWithoutFractionDecimalType = new CurrencyWithoutFractionDecimalType();

			CurrencyWithFractionDecimalType currencyWithFractionDecimalType = new CurrencyWithFractionDecimalType();
			// ======================================================== begin
			if(!this.isShowSummaryHeaders() && !this.isExportAsExcel()) {

				// add flight fields
				addFlightFields(report);
				
				String[] locationHeaderFieldNamesArray1 = new String[6];
				String[] locationHeaderFieldLabelsArray1 = new String[6];
				int arr1Idx = 0;
				if (getFlightLineColumnLabelHashMap().containsKey("Package_Flight__r/Division__c")) {
					locationHeaderFieldNamesArray1[arr1Idx] = "Package_Flight__r/Division__c";
					locationHeaderFieldLabelsArray1[arr1Idx] = getFlightLineColumnLabelHashMap().get("Package_Flight__r/Division__c");
					arr1Idx++;
				}
				if (getFlightLineColumnLabelHashMap().containsKey("Package_Flight__r/Market_Name__c")) {
					locationHeaderFieldNamesArray1[arr1Idx] = "Package_Flight__r/Market_Name__c";
					locationHeaderFieldLabelsArray1[arr1Idx] = getFlightLineColumnLabelHashMap().get("Package_Flight__r/Market_Name__c");
					arr1Idx++;
				}
				if (getFlightLineColumnLabelHashMap().containsKey("Package_Flight__r/Market_Type__c")) {
					locationHeaderFieldNamesArray1[arr1Idx] = "Package_Flight__r/Market_Type__c";
					locationHeaderFieldLabelsArray1[arr1Idx] = getFlightLineColumnLabelHashMap().get("Package_Flight__r/Market_Type__c");
					arr1Idx++;
				}
				if (getFlightLineColumnLabelHashMap().containsKey("Package_Flight__r/Package_Name__c")) {
					locationHeaderFieldNamesArray1[arr1Idx] = "Package_Flight__r/Package_Name__c";
					locationHeaderFieldLabelsArray1[arr1Idx] = getFlightLineColumnLabelHashMap().get("Package_Flight__r/Package_Name__c");
					arr1Idx++;
				}
				if (getFlightLineColumnLabelHashMap().containsKey("Package_Flight__r/Name")) {
					locationHeaderFieldNamesArray1[arr1Idx] = "Package_Flight__r/Name";
					locationHeaderFieldLabelsArray1[arr1Idx] = getFlightLineColumnLabelHashMap().get("Package_Flight__r/Name");
					arr1Idx++;
				}
				if (getFlightLineColumnLabelHashMap().containsKey("Package_Flight__r/Type__c")) {
					locationHeaderFieldNamesArray1[arr1Idx] = "Package_Flight__r/Type__c";
					locationHeaderFieldLabelsArray1[arr1Idx] = getFlightLineColumnLabelHashMap().get("Package_Flight__r/Type__c");
					arr1Idx++;
				}
				for(int i=arr1Idx; i<locationHeaderFieldNamesArray1.length; i++) {
					locationHeaderFieldNamesArray1[i] = "";
					locationHeaderFieldLabelsArray1[i] = "";
				}

				String[] locationHeaderFieldNamesArray2 = new String[6];
				String[] locationHeaderFieldLabelsArray2 = new String[6];
				int arr2Idx = 0;
				
				if (getFlightLineColumnLabelHashMap().containsKey("Package_Flight__r/Campaign_Start_Date__c")) {
					locationHeaderFieldNamesArray2[arr2Idx] = "Package_Flight__r/Campaign_Start_Date__c";
					locationHeaderFieldLabelsArray2[arr2Idx] = getFlightLineColumnLabelHashMap().get("Package_Flight__r/Campaign_Start_Date__c");
					arr2Idx++;
				}
				if (getFlightLineColumnLabelHashMap().containsKey("Package_Flight__r/Campaign_End_Date__c")) {
					locationHeaderFieldNamesArray2[arr2Idx] = "Package_Flight__r/Campaign_End_Date__c";
					locationHeaderFieldLabelsArray2[arr2Idx] = getFlightLineColumnLabelHashMap().get("Package_Flight__r/Campaign_End_Date__c");
					arr2Idx++;
				}
				if (getFlightLineColumnLabelHashMap().containsKey("Package_Flight__r/Duration_And_Type__c")) {
					locationHeaderFieldNamesArray2[arr2Idx] = "Package_Flight__r/Duration_And_Type__c";
					locationHeaderFieldLabelsArray2[arr2Idx] = getFlightLineColumnLabelHashMap().get("Package_Flight__r/Duration_And_Type__c");
					arr2Idx++;
				}
				if (getFlightLineColumnLabelHashMap().containsKey("Package_Flight__r/Target__c")) {
					locationHeaderFieldNamesArray2[arr2Idx] = "Package_Flight__r/Target__c";
					locationHeaderFieldLabelsArray2[arr2Idx] = getFlightLineColumnLabelHashMap().get("Package_Flight__r/Target__c");
					arr2Idx++;
				}
				if (getFlightLineColumnLabelHashMap().containsKey("Package_Flight__r/Target_Population__c")) {
					locationHeaderFieldNamesArray2[arr2Idx] = "Package_Flight__r/Target_Population__c";
					locationHeaderFieldLabelsArray2[arr2Idx] = getFlightLineColumnLabelHashMap().get("Package_Flight__r/Target_Population__c");
					arr2Idx++;
				}
				for(int i=arr2Idx; i<locationHeaderFieldNamesArray2.length; i++) {
					locationHeaderFieldNamesArray2[i] = "";
					locationHeaderFieldLabelsArray2[i] = "";
				}
				
				/*String[] locationHeaderFieldNamesArray1 = new String[] {
						"Package_Flight__r/Division__c",
						"Package_Flight__r/Market_Name__c",
						"Package_Flight__r/Market_Type__c",
						"Package_Flight__r/Package_Name__c",
						"Package_Flight__r/Name",
						"Package_Flight__r/Type__c" };
				String[] locationHeaderFieldLabelsArray1 = new String[] {
						getFlightLineColumnLabelHashMap().get("Package_Flight__r/Division__c"),
						getFlightLineColumnLabelHashMap().get("Package_Flight__r/Market_Name__c"),
						getFlightLineColumnLabelHashMap().get("Package_Flight__r/Market_Type__c"),
						getFlightLineColumnLabelHashMap().get("Package_Flight__r/Package_Name__c"),
						getFlightLineColumnLabelHashMap().get("Package_Flight__r/Name"),
						getFlightLineColumnLabelHashMap().get("Package_Flight__r/Type__c") };

				String[] locationHeaderFieldNamesArray2 = new String[] {
						"Package_Flight__r/Campaign_Start_Date__c",
						"Package_Flight__r/Campaign_End_Date__c",
						"Package_Flight__r/Duration_And_Type__c",
						"Package_Flight__r/Target__c",
						"Package_Flight__r/Target_Population__c", "" };
				String[] locationHeaderFieldLabelsArray2 = new String[] {
						getFlightLineColumnLabelHashMap().get("Package_Flight__r/Campaign_Start_Date__c"),
						getFlightLineColumnLabelHashMap().get("Package_Flight__r/Campaign_End_Date__c"),
						getFlightLineColumnLabelHashMap().get("Package_Flight__r/Duration_And_Type__c"),
						getFlightLineColumnLabelHashMap().get("Package_Flight__r/Target__c"),
						getFlightLineColumnLabelHashMap().get("Package_Flight__r/Target_Population__c"), ""};*/

				report.title(createVerticalTable2(getFlightHeaderStyle(), getFlightHeaderValueStyle(),
						locationHeaderFieldNamesArray1, locationHeaderFieldLabelsArray1,
						locationHeaderFieldNamesArray2, locationHeaderFieldLabelsArray2, Units.inch(2)));
			}
			// ======================================================== end

			// build columns that are to be summarized
				//Weekly Total 18+ Imps 	
				String weeklyTotal18ImplsHeaderLabel = getFlightLineColumnLabelHashMap().get("Weekly_Total_18_Imps__c");

				String weeklyTotal18Impls000HeaderLabel = getFlightLineColumnLabelHashMap().get("Weekly_Total_18_Imps_000__c");

				String targetTotalImplsHeaderLabel = getFlightLineColumnLabelHashMap().get("Total_Imps__c");
				
				String targetTotalImpls000HeaderLabel = getFlightLineColumnLabelHashMap().get("Target_Total_Imps_000__c");
				
				String weeklyInMktTRPHeaderLabel = getFlightLineColumnLabelHashMap().get("In_Mkt_TRP__c");
									
				String freqHeaderLabel = getFlightLineColumnLabelHashMap().get("Plan_Imps_Avg_Frequency__c");

				String totalPriceHeaderLabel = getFlightLineColumnLabelHashMap().get("Total_Price_0d__c");

				String cppHeaderLabel = getFlightLineColumnLabelHashMap().get("CPP_0d__c");

				String planTRPHeaderLabel = getFlightLineColumnLabelHashMap().get("PlanTRP__c");

				String subTotalHeaderLabel = getFlightLineColumnLabelHashMap().get("Net_Amount_Value__c");

				String totalInMarketCPMHeaderLabel = getFlightLineColumnLabelHashMap().get("TotalInMarketCPM_0d__c");
					
			TextColumnBuilder<BigDecimal> weeklyTotal18ImpsColumn = col.column(weeklyTotal18ImplsHeaderLabel, "Weekly_Total_18_Imps__c",
					type.bigDecimalType()).setHorizontalAlignment(HorizontalAlignment.RIGHT).setPattern("#,###");
			
			TextColumnBuilder<BigDecimal> weeklyTotal18Imps000Column = col.column(weeklyTotal18Impls000HeaderLabel,	"Weekly_Total_18_Imps_000__c",
					type.bigDecimalType()).setHorizontalAlignment(HorizontalAlignment.RIGHT).setPattern("#,###");
						
			TextColumnBuilder<BigDecimal> totalImpsColumn = col.column(targetTotalImplsHeaderLabel, "Total_Imps__c",
					type.bigDecimalType()).setHorizontalAlignment(HorizontalAlignment.RIGHT).setPattern("#,###");
			
			TextColumnBuilder<BigDecimal> totalImps000Column = col.column(targetTotalImpls000HeaderLabel, "Target_Total_Imps_000__c",
					type.bigDecimalType()).setHorizontalAlignment(HorizontalAlignment.RIGHT).setPattern("#,###");
			
			TextColumnBuilder<Double> weekklyTRPColumn = col.column(weeklyInMktTRPHeaderLabel, "In_Mkt_TRP__c",
					type.doubleType()).setHorizontalAlignment(HorizontalAlignment.RIGHT).setPattern("##0.0");
			
			TextColumnBuilder<Double> frequencyColumn = col.column(freqHeaderLabel,	"Plan_Imps_Avg_Frequency__c",
					type.doubleType()).setHorizontalAlignment(HorizontalAlignment.RIGHT).setPattern("##0.0");
			
			TextColumnBuilder<BigDecimal> totalPriceColumn = col.column(totalPriceHeaderLabel, "Total_Price_0d__c",
					currencyWithoutFractionDecimalType).setHorizontalAlignment(HorizontalAlignment.RIGHT);
			
			TextColumnBuilder<BigDecimal> cppColumn = col.column(cppHeaderLabel, "CPP_0d__c",
					currencyWithoutFractionDecimalType).setHorizontalAlignment(HorizontalAlignment.RIGHT);
			
			//report.setIgnorePageWidth(true);

			//if (locationMapExists) {
				if (getFlightLineColumnLabelHashMap().containsKey("MapLocation_Number__c")) {

					try {
						if(!isShowSummaryHeaders()) {
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
						} else {
							TextColumnBuilder<String> summaryColumn = col.column(
									"Summary", "Package_Flight__r/Name", type.stringType())
									.setHorizontalAlignment(HorizontalAlignment.RIGHT);
							if (isExportAsExcel()) {
								summaryColumn.setWidth(Units.inch(1.32));
							} else {
								summaryColumn.setWidth(Units.inch(2));
							}
							report.addColumn(summaryColumn);
						}
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
			//}

			if (isExportAsExcel()) {
				// field("Package_Flight__r/Campaign_Start_Date__c",
				// report.addField(field("Package_Flight__r/Campaign_Start_Date__c",
				// Date.class));
				// report.addField(field("Package_Flight__r/Campaign_End_Date__c",
				// Date.class));
				report.setIgnorePageWidth(true);
				if (getFlightLineColumnLabelHashMap().containsKey("Package_Flight__r/Division__c")) {
					report.addColumn(col.column(
							this.isShowSummaryHeaders() ? "" : getFlightLineColumnLabelHashMap().get("Package_Flight__r/Division__c"),
							this.isShowSummaryHeaders() ? "Parent_Flight_Line__c" : "Package_Flight__r/Division__c",
							type.stringType()).setWidth(Units.inch(1.32)));
				}
				if (getFlightLineColumnLabelHashMap().containsKey("Package_Flight__r/Market_Name__c")) {
					report.addColumn(col.column(
							this.isShowSummaryHeaders() ? "" : getFlightLineColumnLabelHashMap().get("Package_Flight__r/Market_Name__c"),
							this.isShowSummaryHeaders() ? "Parent_Flight_Line__c" : "Package_Flight__r/Market_Name__c",
							type.stringType()).setWidth(Units.inch(1.32)));
				}
				if (getFlightLineColumnLabelHashMap().containsKey("Package_Flight__r/Market_Type__c")) {
					report.addColumn(col.column(
							this.isShowSummaryHeaders() ? "" : getFlightLineColumnLabelHashMap().get("Package_Flight__r/Market_Type__c"),
							this.isShowSummaryHeaders() ? "Parent_Flight_Line__c" : "Package_Flight__r/Market_Type__c",
							type.stringType()).setWidth(Units.inch(1.32)));
				}
				if (getFlightLineColumnLabelHashMap().containsKey("Package_Flight__r/Package_Name__c")) {
					report.addColumn(col.column(
							this.isShowSummaryHeaders() ? "" : getFlightLineColumnLabelHashMap().get("Package_Flight__r/Package_Name__c"),
							this.isShowSummaryHeaders() ? "Parent_Flight_Line__c" : "Package_Flight__r/Package_Name__c",
							type.stringType()).setWidth(Units.inch(1.32)));
				}
				if (getFlightLineColumnLabelHashMap().containsKey("Package_Flight__r/Name")) {
					report.addColumn(col.column(
							this.isShowSummaryHeaders() ? "" : getFlightLineColumnLabelHashMap().get("Package_Flight__r/Name"),
							this.isShowSummaryHeaders() ? "Parent_Flight_Line__c" : "Package_Flight__r/Name",
							type.stringType()).setWidth(Units.inch(1.32)));
				}
				// report.addColumn(col.column("Start Date", new
				// ValueExpression("Package_Flight__r/Campaign_Start_Date__c")));
				// report.addColumn(col.column("End Date", new
				// ValueExpression("Package_Flight__r/Campaign_End_Date__c")));

				if (getFlightLineColumnLabelHashMap().containsKey("Package_Flight__r/Type__c")) {
					report.addColumn(col.column(
							this.isShowSummaryHeaders() ? "" : getFlightLineColumnLabelHashMap().get("Package_Flight__r/Type__c"),
							this.isShowSummaryHeaders() ? "Parent_Flight_Line__c" : "Package_Flight__r/Type__c",
							type.stringType()).setWidth(Units.inch(1.32)));
				}

				if (getFlightLineColumnLabelHashMap().containsKey("Package_Flight__r/Campaign_Start_Date__c")) {
					report.addColumn(col.column(
							this.isShowSummaryHeaders() ? "" : getFlightLineColumnLabelHashMap().get("Package_Flight__r/Campaign_Start_Date__c"),
							this.isShowSummaryHeaders() ? "Parent_Flight_Line__c" : "Package_Flight__r/Campaign_Start_Date__c",
							type.stringType()).setHorizontalAlignment(HorizontalAlignment.CENTER).setWidth(Units.inch(1.32)).setValueFormatter(new ValueFormatter()));
				}
				if (getFlightLineColumnLabelHashMap().containsKey("Package_Flight__r/Campaign_End_Date__c")) {
					report.addColumn(col.column(
							this.isShowSummaryHeaders() ? "" : getFlightLineColumnLabelHashMap().get("Package_Flight__r/Campaign_End_Date__c"),
							this.isShowSummaryHeaders() ? "Parent_Flight_Line__c" : "Package_Flight__r/Campaign_End_Date__c",
							type.stringType()).setHorizontalAlignment(HorizontalAlignment.CENTER).setWidth(Units.inch(1.32)).setValueFormatter(new ValueFormatter()));
				}
				if (getFlightLineColumnLabelHashMap().containsKey("Package_Flight__r/Duration_And_Type__c")) {
					report.addColumn(col.column(
							this.isShowSummaryHeaders() ? "" : getFlightLineColumnLabelHashMap().get("Package_Flight__r/Duration_And_Type__c"),
							this.isShowSummaryHeaders() ? "Parent_Flight_Line__c" : "Package_Flight__r/Duration_And_Type__c",
							type.stringType()).setWidth(Units.inch(1.32)));
				}
				if (getFlightLineColumnLabelHashMap().containsKey("Package_Flight__r/Target__c")) {
					report.addColumn(col.column(
							this.isShowSummaryHeaders() ? "" : getFlightLineColumnLabelHashMap().get("Package_Flight__r/Target__c"),
							this.isShowSummaryHeaders() ? "Parent_Flight_Line__c" : "Package_Flight__r/Target__c",
							type.stringType()).setHorizontalAlignment(HorizontalAlignment.CENTER).setWidth(Units.inch(1.32)));
					//report.addProperty("net.sf.jasperreports.export.html.remove.emtpy.space.between.rows", "true");			
				}
				//report.addProperty("net.sf.jasperreports.export.pdf.detect.cell.type", "true");

				if(getFlightLineColumnLabelHashMap().containsKey("Package_Flight__r/Target_Population__c")) {
					report.addColumn(col.column(
							this.isShowSummaryHeaders() ? "" : getFlightLineColumnLabelHashMap().get("Package_Flight__r/Target_Population__c"),
							this.isShowSummaryHeaders() ? "Parent_Flight_Line__c" : "Package_Flight__r/Target_Population__c",
							type.integerType()).setWidth(Units.inch(1.32)));
				} 
				//report.addProperty("net.sf.jasperreports.export.pdf.detect.cell.type", "true");
			}
			/*report.setParameter("net.sf.jasperreports.export.xls.wrap.text", "Boolean.FALSE");
			report.setParameter("net.sf.jasperreports.print.keep.full.text", "Boolean.TRUE");
			report.setParameter("net.sf.jasperreports.export.xls.white.page.background", "Boolean.FALSE");*/
			//report.addProperty("net.sf.jasperreports.export.pdf.force.linebreak.policy", "true");			
			//report.addProperty("net.sf.jasperreports.export.pdf.wrap.text", "false");
			if (getFlightLineColumnLabelHashMap().containsKey("Package_Flight__r/Media_Category__c")) {
				TextColumnBuilder<String> mediaTypeColumn = col.column(
						this.isShowSummaryHeaders() ? "" : getFlightLineColumnLabelHashMap().get("Package_Flight__r/Media_Category__c"), 
						this.isShowSummaryHeaders() ? "Parent_Flight_Line__c" : "Package_Flight__r/Media_Category__c",
						type.stringType()).setHorizontalAlignment(HorizontalAlignment.LEFT).setWidth(Units.inch(2.0));
				report.addColumn(mediaTypeColumn).setColumnTitleStyle(getColumnTitleStyle());
			}
			/*if (getFlightLineColumnLabelHashMap().containsKey("Number_of_Panels__c")) {
				TextColumnBuilder<BigDecimal> noOfPanelsColumn = col.column(
						this.isShowSummaryHeaders() ? getFlightLineColumnLabelHashMap().get("Number_of_Panels__c")
						: getFlightLineColumnLabelHashMap().get("Number_of_Panels__c"),
						"Number_of_Panels__c", type.bigDecimalType()).setHorizontalAlignment(HorizontalAlignment.RIGHT).setPattern("#,###");
				if (isExportAsExcel()) {
					noOfPanelsColumn.setWidth(Units.inch(1.32));
				}
				noOfPanelsColumn.setWidth(Units.inch(1.5));
				report.addColumn(noOfPanelsColumn);
			}*/
			if (getFlightLineColumnLabelHashMap().containsKey("Panel_Id_Label__c")) {
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
			}
			if (getFlightLineColumnLabelHashMap().containsKey("TAB_Id__c")) {
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
			}
		/*	
			if (getFlightLineColumnLabelHashMap().containsKey(
					"Network_Name__c")) {
				TextColumnBuilder<String> networkNameTextColumn = col.column(
						this.isShowSummaryHeaders() ? getFlightLineColumnLabelHashMap().get(
								"Network_Name__c")
								: getFlightLineColumnLabelHashMap().get(
										"Network_Name__c"),
						"Network_Name__c", type.stringType())
						.setHorizontalAlignment(HorizontalAlignment.LEFT);
				//networkNameTextColumn.setWidth(Units.inch(4));
				// descriptionTextColumn.addProperty("net.sf.jasperreports.export.xls.wrap.text",
				// "true");
				if (isExportAsExcel()) {
					networkNameTextColumn.setWidth(Units.inch(1.32));
				}
				report.addColumn(networkNameTextColumn);
			}*/
			if (getFlightLineColumnLabelHashMap().containsKey("Location_Description__c")) {
				TextColumnBuilder<String> descriptionTextColumn = col.column(
						this.isShowSummaryHeaders() ? "" : getFlightLineColumnLabelHashMap().get("Location_Description__c"),
						this.isShowSummaryHeaders() ? "Parent_Flight_Line__c" : "Location_Description__c",
						type.stringType()).setHorizontalAlignment(HorizontalAlignment.LEFT);
				// descriptionTextColumn.addProperty("net.sf.jasperreports.export.xls.wrap.text", "true");
				if (isExportAsExcel()) {
					descriptionTextColumn.setWidth(Units.inch(1.32));
				} else {
					descriptionTextColumn.setWidth(Units.inch(2.5));
				}
				report.addColumn(descriptionTextColumn);
			}
			if (getFlightLineColumnLabelHashMap().containsKey("Face_Direction__c")) {
				TextColumnBuilder<String> flightLineTextColumn = col.column(
						this.isShowSummaryHeaders() ? "" : getFlightLineColumnLabelHashMap().get("Face_Direction__c"),
						this.isShowSummaryHeaders() ? "Parent_Flight_Line__c" : "Face_Direction__c",
						type.stringType()).setHorizontalAlignment(HorizontalAlignment.LEFT);
				report.addColumn(flightLineTextColumn);
				if (isExportAsExcel()) {
					flightLineTextColumn.setWidth(Units.inch(1.32));
				}
			}

			if (getFlightLineColumnLabelHashMap().containsKey("Weekly_Total_18_Imps__c")) {
				if (isExportAsExcel()) {
					weeklyTotal18ImpsColumn.setWidth(Units.inch(1.32));
				} else {
					weeklyTotal18ImpsColumn.setWidth(Units.inch(3));
				}
				report.addColumn(weeklyTotal18ImpsColumn);
			}
			if (getFlightLineColumnLabelHashMap().containsKey("Weekly_Total_18_Imps_000__c")) {
				if (isExportAsExcel()) {
					weeklyTotal18Imps000Column.setWidth(Units.inch(1.32));
				} else {
					weeklyTotal18Imps000Column.setWidth(Units.inch(3));
				}
				report.addColumn(weeklyTotal18Imps000Column);
			}
			if (getFlightLineColumnLabelHashMap().containsKey("In_Mkt_Imps__c")) {
				TextColumnBuilder<BigDecimal> targetInMarketImpsColumn = col.column(
						getFlightLineColumnLabelHashMap().get("In_Mkt_Imps__c"), "In_Mkt_Imps__c",
						type.bigDecimalType()).setHorizontalAlignment(HorizontalAlignment.RIGHT).setPattern("#,###");
				if (isExportAsExcel()) {
					targetInMarketImpsColumn.setWidth(Units.inch(1.32));
				} else {
					targetInMarketImpsColumn.setWidth(Units.inch(3));
				}
				report.addColumn(targetInMarketImpsColumn);
			}
			
			if (getFlightLineColumnLabelHashMap().containsKey("Target_In_Market_Imps_000__c")) {
				TextColumnBuilder<BigDecimal> targetInMarketImps000Column = col.column(
						getFlightLineColumnLabelHashMap().get("Target_In_Market_Imps_000__c"), "Target_In_Market_Imps_000__c",
						type.bigDecimalType()).setHorizontalAlignment(HorizontalAlignment.RIGHT).setPattern("#,###");
				if (isExportAsExcel()) {
					targetInMarketImps000Column.setWidth(Units.inch(1.32));
				} else {
					targetInMarketImps000Column.setWidth(Units.inch(3));
				}
				report.addColumn(targetInMarketImps000Column);
			}
			if (getFlightLineColumnLabelHashMap().containsKey("Total_Imps__c")) {
				if (isExportAsExcel()) {
					totalImpsColumn.setWidth(Units.inch(1.32));
				} else {
					totalImpsColumn.setWidth(Units.inch(3));
				}
				report.addColumn(totalImpsColumn);
			}
			if (getFlightLineColumnLabelHashMap().containsKey("Target_Total_Imps_000__c")) {
				if (isExportAsExcel()) {
					totalImps000Column.setWidth(Units.inch(1.32));
				} else {
					totalImps000Column.setWidth(Units.inch(3));
				}
				report.addColumn(totalImps000Column);
			}
			
			if (getFlightLineColumnLabelHashMap().containsKey("WeeklyMarketImps__c")) {
				TextColumnBuilder<BigDecimal> weeklyTotalTargetImpsColumn = col.column(
						getFlightLineColumnLabelHashMap().get("WeeklyMarketImps__c"), "WeeklyMarketImps__c",
						type.bigDecimalType()).setHorizontalAlignment(HorizontalAlignment.RIGHT).setPattern("#,###");
				if (isExportAsExcel()) {
					weeklyTotalTargetImpsColumn.setWidth(Units.inch(1.32));
				} else {
					weeklyTotalTargetImpsColumn.setWidth(Units.inch(3));
				}
				report.addColumn(weeklyTotalTargetImpsColumn);
			}
			
			if (getFlightLineColumnLabelHashMap().containsKey("Weekly_Total_Target_Imps_000__c")) {
				TextColumnBuilder<BigDecimal> weeklyTotalTargetImps000Column = col.column(
						getFlightLineColumnLabelHashMap().get("Weekly_Total_Target_Imps_000__c"), "Weekly_Total_Target_Imps_000__c",
						type.bigDecimalType()).setHorizontalAlignment(HorizontalAlignment.RIGHT).setPattern("#,###");
				if (isExportAsExcel()) {
					weeklyTotalTargetImps000Column.setWidth(Units.inch(1.32));
				} else {
					weeklyTotalTargetImps000Column.setWidth(Units.inch(3));
				}
				report.addColumn(weeklyTotalTargetImps000Column);
			}
			
			if (getFlightLineColumnLabelHashMap().containsKey("WeeklyInMarketImps__c")) {
				TextColumnBuilder<BigDecimal> weeklyInMarketTargetImpsColumn = col.column(
						getFlightLineColumnLabelHashMap().get("WeeklyInMarketImps__c"), "WeeklyInMarketImps__c",
						type.bigDecimalType()).setHorizontalAlignment(HorizontalAlignment.RIGHT).setPattern("#,###");
				if (isExportAsExcel()) {
					weeklyInMarketTargetImpsColumn.setWidth(Units.inch(1.32));
				} else {
					weeklyInMarketTargetImpsColumn.setWidth(Units.inch(3));
				}
				report.addColumn(weeklyInMarketTargetImpsColumn);
			}
			
			if (getFlightLineColumnLabelHashMap().containsKey("Weekly_In_Market_Target_Imps_000__c")) {
				TextColumnBuilder<BigDecimal> weeklyInMarketTargetImps000Column = col.column(
						getFlightLineColumnLabelHashMap().get("Weekly_In_Market_Target_Imps_000__c"), "Weekly_In_Market_Target_Imps_000__c",
						type.bigDecimalType()).setHorizontalAlignment(HorizontalAlignment.RIGHT).setPattern("#,###");
				if (isExportAsExcel()) {
					weeklyInMarketTargetImps000Column.setWidth(Units.inch(1.32));
				} else {
					weeklyInMarketTargetImps000Column.setWidth(Units.inch(3));
				}
				report.addColumn(weeklyInMarketTargetImps000Column);
			}
			
			if (getFlightLineColumnLabelHashMap().containsKey("In_Mkt_TRP__c")) {
				if (isExportAsExcel()) {
					weekklyTRPColumn.setWidth(Units.inch(1.32));
				} else {
					weekklyTRPColumn.setWidth(Units.inch(3));
				}
				report.addColumn(weekklyTRPColumn);
			}
			if (getFlightLineColumnLabelHashMap().containsKey("PlanTRP__c")) {
				TextColumnBuilder<Double> planTRPColumn = col.column(planTRPHeaderLabel, "PlanTRP__c",
						type.doubleType()).setHorizontalAlignment(HorizontalAlignment.RIGHT).setPattern("##0.0");
				if (isExportAsExcel()) {
					planTRPColumn.setWidth(Units.inch(1.32));
				} else {
					planTRPColumn.setWidth(Units.inch(2));
				}
				report.addColumn(planTRPColumn);
			}
			if (getFlightLineColumnLabelHashMap().containsKey("Plan_Imps_Reach_Perc__c")) {
				TextColumnBuilder<Double> planImpsReachPercColumn = col.column(
						getFlightLineColumnLabelHashMap().get("Plan_Imps_Reach_Perc__c"), "Plan_Imps_Reach_Perc__c",
						type.percentageType()).setHorizontalAlignment(HorizontalAlignment.RIGHT).setPattern("##0.0");
				if (isExportAsExcel()) {
					planImpsReachPercColumn.setWidth(Units.inch(1.32));
				} else {
					planImpsReachPercColumn.setWidth(Units.inch(1.5));
				}
				report.addColumn(planImpsReachPercColumn);
			}
			if (getFlightLineColumnLabelHashMap().containsKey("Plan_Imps_Avg_Frequency__c")) {
				if (isExportAsExcel()) {
					frequencyColumn.setWidth(Units.inch(1.32));
				}
				report.addColumn(frequencyColumn);
			}

			if (getFlightLineColumnLabelHashMap().containsKey("X4_Wk_Proposed_Price__c")) {
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
			}
			//X4WkProposedPriceHeaderLabel ,subTotalHeaderLabel
			if (getFlightLineColumnLabelHashMap().containsKey("Net_Amount_Value__c")) {
				TextColumnBuilder<BigDecimal> subTotalPriceColumn = col.column(subTotalHeaderLabel, "Net_Amount_Value__c",
						currencyWithoutFractionDecimalType).setHorizontalAlignment(HorizontalAlignment.RIGHT);
				if (isExportAsExcel()) {
					subTotalPriceColumn.setWidth(Units.inch(1.32));
				} else {
					subTotalPriceColumn.setWidth(Units.inch(2.5));
				}
				report.addColumn(subTotalPriceColumn);
			}
			
			if (getFlightLineColumnLabelHashMap().containsKey("Total_Price_0d__c")) {
				if (isExportAsExcel()) {
					totalPriceColumn.setWidth(Units.inch(1.32));
				} else {
					totalPriceColumn.setWidth(Units.inch(2.5));
				}
				report.addColumn(totalPriceColumn);
			}
			if (getFlightLineColumnLabelHashMap().containsKey("TotalInMarketCPM_0d__c")) {
				TextColumnBuilder<BigDecimal> totalInMarketCPM0dColumn = col.column(totalInMarketCPMHeaderLabel, "TotalInMarketCPM_0d__c",
						currencyWithFractionDecimalType).setHorizontalAlignment(HorizontalAlignment.RIGHT);
				if (isExportAsExcel()) {
					totalInMarketCPM0dColumn.setWidth(Units.inch(1.32));
				} else {
					totalInMarketCPM0dColumn.setWidth(Units.inch(2.5));
				}
				report.addColumn(totalInMarketCPM0dColumn);
			}
			if (getFlightLineColumnLabelHashMap().containsKey("CPP_0d__c")) {
				if (isExportAsExcel()) {
					cppColumn.setWidth(Units.inch(1.32));
				} else {
					cppColumn.setWidth(Units.inch(2.5));
				}
				report.addColumn(cppColumn);
			}
			if (getFlightLineColumnLabelHashMap().containsKey("Unit_Size__c")) {
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
			}
			if (getFlightLineColumnLabelHashMap().containsKey("Illumination_yn__c")) {
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
			}
			/*
			if (getFlightLineColumnLabelHashMap().containsKey(
					"Hours_of_Operation__c")) {
				TextColumnBuilder<String> hoursOfOperationColumn = col.column(
						this.isShowSummaryHeaders() ? getFlightLineColumnLabelHashMap().get("Hours_of_Operation__c")
								: getFlightLineColumnLabelHashMap().get("Hours_of_Operation__c"),
								"Hours_of_Operation__c", type.stringType())
						.setHorizontalAlignment(HorizontalAlignment.LEFT);
				if (isExportAsExcel()) {
					hoursOfOperationColumn.setWidth(Units.inch(1.32));
				} else {
					hoursOfOperationColumn.setWidth(Units.inch(2));
				}
				report.addColumn(hoursOfOperationColumn);
			}
			*/
			
			if (getFlightLineColumnLabelHashMap().containsKey("Comments__c")) {
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
			}
			if (isExportAsExcel()) {
				TextColumnBuilder<String> flightCommentsColumn = col.column(
						this.isShowSummaryHeaders() ? "" : getFlightLineColumnLabelHashMap().get("Package_Flight__r/Flight_Comments__c"),
						this.isShowSummaryHeaders() ? "Parent_Flight_Line__c" : "Package_Flight__r/Flight_Comments__c",
						type.stringType()).setHorizontalAlignment(HorizontalAlignment.LEFT);
				if (isExportAsExcel()) {
					flightCommentsColumn.setWidth(Units.inch(1.32));
				} else {
					flightCommentsColumn.setWidth(Units.inch(3.5));
				}
				report.addColumn(flightCommentsColumn);
			}

			if (getFlightLineColumnLabelHashMap().containsKey("Timing__c")) {
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
			}
			
			if (getFlightLineColumnLabelHashMap().containsKey("In_Mkt_Perc_Comp__c")) {
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
			}
			
			if (getFlightLineColumnLabelHashMap().containsKey("X4_Wk_Avg_Rate_per_Panel__c")) {
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
			}
			
			if (getFlightLineColumnLabelHashMap().containsKey("Production__c")) {
				TextColumnBuilder<BigDecimal> productionColumn = col.column(
						getFlightLineColumnLabelHashMap().get("Production__c"), "Production__c",
						currencyWithFractionDecimalType).setHorizontalAlignment(HorizontalAlignment.RIGHT);
				if (isExportAsExcel()) {
					productionColumn.setWidth(Units.inch(1.32));
				} else {
					productionColumn.setWidth(Units.inch(1.5));
				}
				report.addColumn(productionColumn);
			}
			
			if (getFlightLineColumnLabelHashMap().containsKey("Additional_Cost__c")) {
				TextColumnBuilder<BigDecimal> additionalCostColumn = col.column(
						getFlightLineColumnLabelHashMap().get("Additional_Cost__c"), "Additional_Cost__c",
						currencyWithFractionDecimalType).setHorizontalAlignment(HorizontalAlignment.RIGHT);
				if (isExportAsExcel()) {
					additionalCostColumn.setWidth(Units.inch(1.32));
				} else {
					additionalCostColumn.setWidth(Units.inch(1.5));
				}
				report.addColumn(additionalCostColumn);
			}
			
			if (getFlightLineColumnLabelHashMap().containsKey("Tax_Amt__c")) {
				TextColumnBuilder<BigDecimal> taxAmtColumn = col.column(
						getFlightLineColumnLabelHashMap().get("Tax_Amt__c"), "Tax_Amt__c",
						currencyWithFractionDecimalType).setHorizontalAlignment(HorizontalAlignment.RIGHT);
				if (isExportAsExcel()) {
					taxAmtColumn.setWidth(Units.inch(1.32));
				} else {
					taxAmtColumn.setWidth(Units.inch(1.5));
				}
				report.addColumn(taxAmtColumn);
			}
			
			if (getFlightLineColumnLabelHashMap().containsKey("Location__Longitude__s")) {
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
			}
			
			if (getFlightLineColumnLabelHashMap().containsKey("Location__Latitude__s")) {
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
			}
			
			if (getFlightLineColumnLabelHashMap().containsKey("Embellishments__c")) {
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
			}
			
			if (getFlightLineColumnLabelHashMap().containsKey("Illumination__c")) {
				TextColumnBuilder<BigDecimal> illuminationColumn = col.column(
						this.isShowSummaryHeaders() ? "" : getFlightLineColumnLabelHashMap().get("Illumination__c"),
						this.isShowSummaryHeaders() ? "OB_Summ_Num__c" : "Illumination__c",
						currencyWithoutFractionDecimalType).setHorizontalAlignment(HorizontalAlignment.RIGHT);
				if (isExportAsExcel()) {
					illuminationColumn.setWidth(Units.inch(1.32));
				} else {
					illuminationColumn.setWidth(Units.inch(1));
				}
				report.addColumn(illuminationColumn);
			}
			
			if (getFlightLineColumnLabelHashMap().containsKey("Current_Copy__c")) {
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
			}
			
			if (getFlightLineColumnLabelHashMap().containsKey("City__c")) {
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
			}
			
			if (getFlightLineColumnLabelHashMap().containsKey("County__c")) {
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
			}
			
			if (getFlightLineColumnLabelHashMap().containsKey("State__c")) {
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
			}
			
			if (getFlightLineColumnLabelHashMap().containsKey("Zip__c")) {
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
			}
			if (getFlightLineColumnLabelHashMap().containsKey("Media_Product__c")) {
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
			}
			
			if (getFlightLineColumnLabelHashMap().containsKey("Ride_Order__c")) {
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
			}
			if (getFlightLineColumnLabelHashMap().containsKey("Facing__c")) {
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
			}
			if (isExportAsExcel()) {
				if (getFlightLineColumnLabelHashMap().containsKey("X4_Wk_Base_Rate__c")) {
					TextColumnBuilder<BigDecimal> x4WkBaseRateColumn = col.column(
							this.isShowSummaryHeaders() ? "" : getFlightLineColumnLabelHashMap().get("X4_Wk_Base_Rate__c"),
							this.isShowSummaryHeaders() ? "OB_Summ_Num__c" : "X4_Wk_Base_Rate__c",
							currencyWithoutFractionDecimalType).setHorizontalAlignment(HorizontalAlignment.RIGHT).setWidth(Units.inch(1.32));
					report.addColumn(x4WkBaseRateColumn);
				}
				if (getFlightLineColumnLabelHashMap().containsKey("X4_Wk_Floor__c")) {
					TextColumnBuilder<BigDecimal> x4WkFloorColumn = col.column(
							this.isShowSummaryHeaders() ? ""	: getFlightLineColumnLabelHashMap().get("X4_Wk_Floor__c"),
							this.isShowSummaryHeaders() ? "OB_Summ_Num__c" : "X4_Wk_Floor__c",
							currencyWithoutFractionDecimalType).setHorizontalAlignment(HorizontalAlignment.RIGHT).setWidth(Units.inch(1.32));
					report.addColumn(x4WkFloorColumn);
				}
				/*if (getFlightLineColumnLabelHashMap().containsKey("Offering_Discount_Percent__c")) {
					TextColumnBuilder<BigDecimal> offeringDiscountPercentColumn = col
							.column(this.isShowSummaryHeaders() ? getFlightLineColumnLabelHashMap().get("Offering_Discount_Percent__c")
									: getFlightLineColumnLabelHashMap().get("Offering_Discount_Percent__c"), "Offering_Discount_Percent__c",
									type.bigDecimalType()).setPattern("#,##0.00%")
							.setHorizontalAlignment(HorizontalAlignment.RIGHT).setWidth(Units.inch(1.32));
					report.addColumn(offeringDiscountPercentColumn);
				}
				if (getFlightLineColumnLabelHashMap().containsKey("Offering_Discount__c")) {
					TextColumnBuilder<BigDecimal> offeringDiscountColumn = col
							.column(this.isShowSummaryHeaders() ? getFlightLineColumnLabelHashMap().get("Offering_Discount__c")
									: getFlightLineColumnLabelHashMap().get("Offering_Discount__c"), "Offering_Discount__c",
									currencyWithFractionDecimalType)
							.setHorizontalAlignment(HorizontalAlignment.RIGHT).setWidth(Units.inch(1.32));
					report.addColumn(offeringDiscountColumn);
				}*/
			}
			if(getPackageMarketFlightPreviousRecordExpression().isDigitalMediaCategory()) {
				/*if (getFlightLineColumnLabelHashMap().containsKey("Max_of_Spots_per_Loop__c")) {
					TextColumnBuilder<String> maxNoOfSpotsPerLoopColumn = col.column(
							this.isShowSummaryHeaders() ? "" : getFlightLineColumnLabelHashMap().get("Max_of_Spots_per_Loop__c"),
							"Max_of_Spots_per_Loop__c", type.stringType()).setHorizontalAlignment(HorizontalAlignment.RIGHT);
					if (isExportAsExcel()) {
						maxNoOfSpotsPerLoopColumn.setWidth(Units.inch(1.32));
					} else {
						maxNoOfSpotsPerLoopColumn.setWidth(Units.inch(1.5));
					}
					report.addColumn(maxNoOfSpotsPerLoopColumn);
				}*/

				if (getFlightLineColumnLabelHashMap().containsKey("Average_Daily_Spots__c")) {
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
				}
				
				/*if (getFlightLineColumnLabelHashMap().containsKey("Spot_Type__c")) {
					TextColumnBuilder<String> averageDailySpotsColumn = col.column(
							this.isShowSummaryHeaders() ? "" : getFlightLineColumnLabelHashMap().get("Spot_Type__c"),
							"Spot_Type__c", type.stringType()).setHorizontalAlignment(HorizontalAlignment.LEFT);
					if (isExportAsExcel()) {
						averageDailySpotsColumn.setWidth(Units.inch(1.32));
					} else {
						averageDailySpotsColumn.setWidth(Units.inch(1.5));
					}
					report.addColumn(averageDailySpotsColumn);
				}*/
			}
			
			// add fields
			report.addField(field("Package_Flight__r/Id", type.stringType()));

			// style
			if(!isExportAsExcel()) {
				report.highlightDetailEvenRows();
			}
			report.setColumnStyle(getColumnStyle());

			// filter
			report.setFilterExpression(new FilterByFlightIdExpression(
					getPackageMarketFlightPreviousRecordExpression()));

			// add a blank line at the end
			report.addLastPageFooter(cmp.text(""));

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

	private class RotarySubreportExpression extends
			AbstractSimpleExpression<JasperReportBuilder> {

		private static final long serialVersionUID = 1656551952324L;

		private PackageMarketFlightPreviousRecordExpression packageMarketFlightPreviousRecordExpression;

		private StyleBuilder columnTitleStyle;

		private StyleBuilder columnStyle;

		private boolean exportAsExcel;

		private boolean showSummaryHeaders;

		private StyleBuilder flightHeaderStyle;

		private StyleBuilder flightHeaderValueStyle;

		public RotarySubreportExpression(
				PackageMarketFlightPreviousRecordExpression packageMarketFlightPreviousRecordExpression,
				StyleBuilder columnTitleStyle, StyleBuilder columnStyle,
				StyleBuilder flightHeaderStyle, StyleBuilder flightHeaderValueStyle, boolean exportAsExcel, boolean showSummaryHeaders) {
			setPackageMarketFlightPreviousRecordExpression(packageMarketFlightPreviousRecordExpression);
			setColumnTitleStyle(columnTitleStyle);
			setColumnStyle(columnStyle);
			setExportAsExcel(exportAsExcel);
			setShowSummaryHeaders(showSummaryHeaders);
			setFlightHeaderStyle(flightHeaderStyle);
			setFlightHeaderValueStyle(flightHeaderValueStyle);
		}

		@Override
		public JasperReportBuilder evaluate(ReportParameters arg0) {
			JasperReportBuilder report = report();

			// types
			CurrencyWithoutFractionDecimalType currencyWithoutFractionDecimalType = new CurrencyWithoutFractionDecimalType();

			// ======================================================== begin
			if(!this.isShowSummaryHeaders() && !this.isExportAsExcel()) {

				// add flight fields
				addFlightFields(report);

				// report specific header
				String[] rotaryHeaderFieldNamesArray = new String[] {
						"Package_Flight__r/Package_Name__c",
						"Package_Flight__r/Name",
						"Package_Flight__r/Division__c",
						"Package_Flight__r/Campaign_Start_Date__c",
						"Package_Flight__r/Campaign_End_Date__c",
						"Package_Flight__r/Duration_And_Type__c",
						"Package_Flight__r/Target__c"
					};
				String[] rotaryHeaderFieldLabelsArray = new String[] {
						getFlightLineColumnLabelHashMap().get("Package_Flight__r/Package_Name__c"),
						getFlightLineColumnLabelHashMap().get("Package_Flight__r/Name"),
						getFlightLineColumnLabelHashMap().get("Package_Flight__r/Division__c"),
						getFlightLineColumnLabelHashMap().get("Package_Flight__r/Campaign_Start_Date__c"),
						getFlightLineColumnLabelHashMap().get("Package_Flight__r/Campaign_End_Date__c"),
						getFlightLineColumnLabelHashMap().get("Package_Flight__r/Duration_And_Type__c"),
						getFlightLineColumnLabelHashMap().get("Package_Flight__r/Target__c")
					};
				report.title(createVerticalTable(getFlightHeaderStyle(),
						getFlightHeaderValueStyle(), rotaryHeaderFieldNamesArray,
						rotaryHeaderFieldLabelsArray, Units.inch(2)));
			}
			// ======================================================== end

			if (isExportAsExcel()) {
				if(getFlightLineColumnLabelHashMap().containsKey("Package_Flight__r/")) {
					report.addColumn(col.column(
							this.isShowSummaryHeaders() ? "Summary"
									: getFlightLineColumnLabelHashMap().get(
											"Package_Flight__r/Package_Name__c"),
							"Package_Flight__r/Package_Name__c", type.stringType()));
				}
				if(getFlightLineColumnLabelHashMap().containsKey("Package_Flight__r/Name")) {
					report.addColumn(col.column(
							this.isShowSummaryHeaders() ? ""
									: getFlightLineColumnLabelHashMap().get(
											"Package_Flight__r/Name"),
							"Package_Flight__r/Name", type.stringType()));
				}
				if(getFlightLineColumnLabelHashMap().containsKey("Package_Flight__r/Division__c")) {
					report.addColumn(col.column(
							this.isShowSummaryHeaders() ? ""
									: getFlightLineColumnLabelHashMap().get(
											"Package_Flight__r/Division__c"),
							"Package_Flight__r/Division__c", type.stringType()));
				}
				// Excel & PDF
				if(getFlightLineColumnLabelHashMap().containsKey("Network_Name__c")) {
					report.addColumn(col.column(
							this.isShowSummaryHeaders() ? ""
									: getFlightLineColumnLabelHashMap().get(
											"Network_Name__c"),
							"Network_Name__c", type.stringType()));
				}
				if(getFlightLineColumnLabelHashMap().containsKey("Package_Flight__r/Target__c")) {
					report.addColumn(col.column(
							this.isShowSummaryHeaders() ? "" : getFlightLineColumnLabelHashMap().get(
									"Package_Flight__r/Target__c"),
							"Package_Flight__r/Target__c", type.stringType())
							.setHorizontalAlignment(HorizontalAlignment.CENTER));
				}
				if(getFlightLineColumnLabelHashMap().containsKey("Package_Flight__r/Duration_And_Type__c")) {
					report.addColumn(col
							.column(this.isShowSummaryHeaders() ? ""
									: getFlightLineColumnLabelHashMap().get(
											"Package_Flight__r/Duration_And_Type__c"),
									"Package_Flight__r/Duration_And_Type__c",
									type.stringType()).setHorizontalAlignment(
									HorizontalAlignment.CENTER));
				}
				if(getFlightLineColumnLabelHashMap().containsKey("Package_Flight__r/Campaign_Start_Date__c")) {
					report.addColumn(col
							.column(this.isShowSummaryHeaders() ? ""
									: getFlightLineColumnLabelHashMap()
											.get("Package_Flight__r/Campaign_Start_Date__c"),
									"Package_Flight__r/Campaign_Start_Date__c",
									type.stringType()).setHorizontalAlignment(
									HorizontalAlignment.CENTER));
				}
				if(getFlightLineColumnLabelHashMap().containsKey("Package_Flight__r/Campaign_End_Date__c")) {
					report.addColumn(col
							.column(this.isShowSummaryHeaders() ? ""
									: getFlightLineColumnLabelHashMap()
											.get("Package_Flight__r/Campaign_End_Date__c"),
									"Package_Flight__r/Campaign_End_Date__c",
									type.stringType()).setHorizontalAlignment(
									HorizontalAlignment.CENTER));
				}
			}

			// add columns
			String networkNameLabel = "";
			if (!this.isExportAsExcel()) {
				if (this.isShowSummaryHeaders()) {
					networkNameLabel = "Summary";
				} else {
					networkNameLabel = getFlightLineColumnLabelHashMap().get(
							"Network_Name__c");
				}
			}

			if(!this.isExportAsExcel()) {
				if(getFlightLineColumnLabelHashMap().containsKey("Network_Name__c")) {
					report.addColumn(
							col.column(networkNameLabel, "Network_Name__c",
									type.stringType())).setColumnTitleStyle(
							getColumnTitleStyle());
				}
			}

			if(getFlightLineColumnLabelHashMap().containsKey("Number_of_Panels__c")) {
				report.addColumn(col.column(
						this.isShowSummaryHeaders() ? ""
								: getFlightLineColumnLabelHashMap().get(
										"Number_of_Panels__c"),
						"Number_of_Panels__c", type.integerType())
						.setHorizontalAlignment(HorizontalAlignment.CENTER))
						.setColumnTitleStyle(getColumnTitleStyle());
			}
			if(getFlightLineColumnLabelHashMap().containsKey("Weekly_Total_18_Imps__c")) {
				report.addColumn(col.column(
						this.isShowSummaryHeaders() ? ""
								: getFlightLineColumnLabelHashMap().get(
										"Weekly_Total_18_Imps__c"),
						"Weekly_Total_18_Imps__c", type.integerType())
						.setHorizontalAlignment(HorizontalAlignment.CENTER));
			}
			if(getFlightLineColumnLabelHashMap().containsKey("In_Mkt_Imps__c")) {
				report.addColumn(col.column(
						this.isShowSummaryHeaders() ? ""
								: getFlightLineColumnLabelHashMap().get(
										"In_Mkt_Imps__c"), "In_Mkt_Imps__c",
						type.doubleType()).setHorizontalAlignment(
						HorizontalAlignment.CENTER));
			}
			if(getFlightLineColumnLabelHashMap().containsKey("Total_Imps__c")) {
				report.addColumn(col.column(
						this.isShowSummaryHeaders() ? ""
								: getFlightLineColumnLabelHashMap().get(
										"Total_Imps__c"), "Total_Imps__c",
						type.doubleType()).setHorizontalAlignment(
						HorizontalAlignment.CENTER));
			}
			if(getFlightLineColumnLabelHashMap().containsKey("In_Mkt_TRP__c")) {
				report.addColumn(col.column(
						this.isShowSummaryHeaders() ? ""
								: getFlightLineColumnLabelHashMap().get(
										"In_Mkt_TRP__c"), "In_Mkt_TRP__c",
						type.doubleType()).setHorizontalAlignment(
						HorizontalAlignment.CENTER));
			}
			if(getFlightLineColumnLabelHashMap().containsKey("PlanTRP__c")) {
				report.addColumn(col.column(
						this.isShowSummaryHeaders() ? ""
								: getFlightLineColumnLabelHashMap().get(
										"PlanTRP__c"), "PlanTRP__c",
						type.doubleType()).setHorizontalAlignment(
						HorizontalAlignment.CENTER));
			}
			if(getFlightLineColumnLabelHashMap().containsKey("")) {
				report.addColumn(col
						.column(this.isShowSummaryHeaders() ? "Plan_Imps_Reach_Perc__c"
								: getFlightLineColumnLabelHashMap().get(
										"Plan_Imps_Reach_Perc__c"),
								"Plan_Imps_Reach_Perc__c", type.percentageType())
						.setHorizontalAlignment(HorizontalAlignment.CENTER)
						.setPattern("#%"));
			}
			if(getFlightLineColumnLabelHashMap().containsKey("Plan_Imps_Avg_Frequency__c")) {
				report.addColumn(col.column(
						this.isShowSummaryHeaders() ? ""
								: getFlightLineColumnLabelHashMap().get(
										"Plan_Imps_Avg_Frequency__c"),
						"Plan_Imps_Avg_Frequency__c", type.doubleType())
						.setHorizontalAlignment(HorizontalAlignment.CENTER));
			}
			if(getFlightLineColumnLabelHashMap().containsKey("X4_Wk_Proposed_Price__c")) {
				report.addColumn(col.column(
						this.isShowSummaryHeaders() ? ""
								: getFlightLineColumnLabelHashMap().get(
										"X4_Wk_Proposed_Price__c"),
						"X4_Wk_Proposed_Price__c",
						currencyWithoutFractionDecimalType).setHorizontalAlignment(
						HorizontalAlignment.CENTER));
			}
			if(getFlightLineColumnLabelHashMap().containsKey("Total_Price_0d__c")) {
				report.addColumn(col.column(
						this.isShowSummaryHeaders() ? ""
								: getFlightLineColumnLabelHashMap().get(
										"Total_Price_0d__c"), "Total_Price_0d__c",
						currencyWithoutFractionDecimalType).setHorizontalAlignment(
						HorizontalAlignment.CENTER));
			}
			if(getFlightLineColumnLabelHashMap().containsKey("TotalInMarketCPM_0d__c")) {
				report.addColumn(col.column(
						this.isShowSummaryHeaders() ? ""
								: getFlightLineColumnLabelHashMap().get(
										"TotalInMarketCPM_0d__c"),
						"TotalInMarketCPM_0d__c",
						currencyWithoutFractionDecimalType).setHorizontalAlignment(
						HorizontalAlignment.CENTER));
			}
			if(getFlightLineColumnLabelHashMap().containsKey("CPP_0d__c")) {
				report.addColumn(col.column(
						this.isShowSummaryHeaders() ? ""
								: getFlightLineColumnLabelHashMap()
										.get("CPP_0d__c"), "CPP_0d__c",
						currencyWithoutFractionDecimalType).setHorizontalAlignment(
						HorizontalAlignment.CENTER));
			}

			if(this.isExportAsExcel()) {
				if(getFlightLineColumnLabelHashMap().containsKey("Comments__c")) {
					report.addColumn(col.column(this.isShowSummaryHeaders() ? ""
							: getFlightLineColumnLabelHashMap().get("Comments__c"),
							"Comments__c", type.stringType()));
					}
			}

			if(getFlightLineColumnLabelHashMap().containsKey("Package_Flight__r/Flight_Comments__c")) {
				report.addColumn(col.column(this.isShowSummaryHeaders() ? ""
						: getFlightLineColumnLabelHashMap().get("Package_Flight__r/Flight_Comments__c"),
						"Package_Flight__r/Flight_Comments__c", type.stringType()));
			}

			// add fields
			report.addField(field("Package_Flight__r/Id", type.stringType()));

			// style
			report.highlightDetailEvenRows();
			report.setColumnStyle(getColumnStyle());

			// filter
			report.setFilterExpression(new FilterByFlightIdExpression(
					getPackageMarketFlightPreviousRecordExpression()));

			// add a blank line at the end
			report.addLastPageFooter(cmp.text(""));

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

	private class NetworkSubreportExpression extends
			AbstractSimpleExpression<JasperReportBuilder> {

		private static final long serialVersionUID = 1656551952324L;

		private PackageMarketFlightPreviousRecordExpression packageMarketFlightPreviousRecordExpression;

		private FlightLinePreviousRecordExpression flightLinePreviousRecordExpression;

		private StyleBuilder columnTitleStyle;

		private StyleBuilder columnStyle;

		private boolean exportAsExcel;

		private boolean showSummaryHeaders;

		private StyleBuilder flightHeaderStyle;

		private StyleBuilder flightHeaderValueStyle;

		private String dataSourceFileName;

		public NetworkSubreportExpression(
				String dataSourceFileName, PackageMarketFlightPreviousRecordExpression packageMarketFlightPreviousRecordExpression,
				FlightLinePreviousRecordExpression flightLinePreviousRecordExpression, StyleBuilder columnTitleStyle, StyleBuilder columnStyle,
				StyleBuilder flightHeaderStyle,
				StyleBuilder flightHeaderValueStyle, boolean exportAsExcel, boolean showSummaryHeaders) {
			setDataSourceFileName(dataSourceFileName);
			setPackageMarketFlightPreviousRecordExpression(packageMarketFlightPreviousRecordExpression);
			setFlightLinePreviousRecordExpression(flightLinePreviousRecordExpression);
			setColumnTitleStyle(columnTitleStyle);
			setColumnStyle(columnStyle);
			setExportAsExcel(exportAsExcel);
			setFlightHeaderStyle(flightHeaderStyle);
			setFlightHeaderValueStyle(flightHeaderValueStyle);
			setShowSummaryHeaders(showSummaryHeaders);
		}

		@Override
		public JasperReportBuilder evaluate(ReportParameters arg0) {
			JasperReportBuilder report = report();

			// types
			CurrencyWithFractionDecimalType currencyWithFractionDecimalType = new CurrencyWithFractionDecimalType();
			CurrencyWithoutFractionDecimalType currencyWithoutFractionDecimalType = new CurrencyWithoutFractionDecimalType();

			// network detail dataset
			JRXmlDataSource networkDetailDataSource = null;
			try {
				networkDetailDataSource = new JRXmlDataSource(
						getDataSourceFileName(), "/QueryResult/records/Child_Flight_Lines__r/records");
			} catch (JRException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			// summary dataset
			JRXmlDataSource summaryDataSource = null;
			try {
				summaryDataSource = new JRXmlDataSource(
						getDataSourceFileName(), "/QueryResult/records[RecordTypeId = boolean(1)]");
			} catch (JRException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			// add flight fields
			addFlightFields(report);

			report.title(cmp.text(""));

			// ======================================================== begin
			if (!this.isShowSummaryHeaders() && !this.isExportAsExcel()) {

				// report specific header
				String[] networkHeaderFieldNamesArray = new String[] {
						"Package_Flight__r/Package_Name__c",
						"Package_Flight__r/Name",
						"Package_Flight__r/Division__c",
						"Package_Flight__r/Target__c",
						"Package_Flight__r/Duration_And_Type__c" };
				String[] networkHeaderFieldLabelsArray = new String[] {
						getFlightLineColumnLabelHashMap().get("Package_Flight__r/Package_Name__c"),
						getFlightLineColumnLabelHashMap().get("Package_Flight__r/Name"),
						getFlightLineColumnLabelHashMap().get("Package_Flight__r/Division__c"),
						getFlightLineColumnLabelHashMap().get("Package_Flight__r/Target__c"),
						getFlightLineColumnLabelHashMap().get("Package_Flight__r/Duration_And_Type__c") };
				report.title(createVerticalTable(getFlightHeaderStyle(),
						getFlightHeaderValueStyle(),
						networkHeaderFieldNamesArray,
						networkHeaderFieldLabelsArray, Units.inch(2)));
			}
			// ======================================================== end

			SubreportBuilder networkDetailSubreport = null;
			if(!isShowSummaryHeaders()) {
				// attach observer to group
				CustomGroupBuilder flightLinePreviousRecordCustomGroupBuilder = grp.group(getFlightLinePreviousRecordExpression());
				flightLinePreviousRecordCustomGroupBuilder.setHeaderLayout(GroupHeaderLayout.EMPTY);
				report.addGroup(flightLinePreviousRecordCustomGroupBuilder);
	
				// network detail sub-report
				networkDetailSubreport = cmp
						.subreport(
								new NetworkDetailSubreportExpression(
										getFlightLinePreviousRecordExpression(),
										columnTitleStyle, columnStyle,
										exportAsExcel, false))
						.setDataSource(networkDetailDataSource);
			}

			// ================================= add columns: begin
			if (isExportAsExcel()) {
				if(getFlightLineColumnLabelHashMap().containsKey("Package_Flight__r/Package_Name__c")) {
					report.addColumn(col.column(
							this.isShowSummaryHeaders() ? "Summary" : getFlightLineColumnLabelHashMap().get("Package_Flight__r/Package_Name__c"),
							this.isShowSummaryHeaders() ? "Package_Flight__r/Name" : "Package_Flight__r/Package_Name__c", type.stringType()).setWidth(Units.inch(1.32)));
				}
				if(getFlightLineColumnLabelHashMap().containsKey("Package_Flight__r/Name")) {
					report.addColumn(col.column(
							this.isShowSummaryHeaders() ? "" : getFlightLineColumnLabelHashMap().get("Package_Flight__r/Name"),
							this.isShowSummaryHeaders() ? "Parent_Flight_Line__c" : "Package_Flight__r/Name", type.stringType()).setWidth(Units.inch(1.32)));
				}
				if(getFlightLineColumnLabelHashMap().containsKey("Package_Flight__r/Division__c")) {
					report.addColumn(col.column(
							this.isShowSummaryHeaders() ? "" : getFlightLineColumnLabelHashMap().get("Package_Flight__r/Division__c"),
							this.isShowSummaryHeaders() ? "Parent_Flight_Line__c" : "Package_Flight__r/Division__c", type.stringType()).setWidth(Units.inch(1.32)));
				}
				if(getFlightLineColumnLabelHashMap().containsKey("Network_Name__c")) {
					report.addColumn(col.column(
							this.isShowSummaryHeaders() ? "" : getFlightLineColumnLabelHashMap().get("Network_Name__c"),
							this.isShowSummaryHeaders() ? "Parent_Flight_Line__c" : "Network_Name__c", type.stringType()).setWidth(Units.inch(1.32)));
				}
				if(getFlightLineColumnLabelHashMap().containsKey("Number_of_Panels__c")) {
					report.addColumn(col.column(getFlightLineColumnLabelHashMap().get("Number_of_Panels__c"), "Number_of_Panels__c",
							type.integerType()).setPattern("#,###").setHorizontalAlignment(HorizontalAlignment.RIGHT).setWidth(Units.inch(1.32)));
				}
				if(getFlightLineColumnLabelHashMap().containsKey("Package_Flight__r/Target__c")) {
					report.addColumn(col.column(
							this.isShowSummaryHeaders() ? "" : getFlightLineColumnLabelHashMap().get("Package_Flight__r/Target__c"),
							this.isShowSummaryHeaders() ? "Parent_Flight_Line__c" : "Package_Flight__r/Target__c", type.stringType())
							.setHorizontalAlignment(HorizontalAlignment.CENTER).setWidth(Units.inch(1.32)));
				}
				if(getFlightLineColumnLabelHashMap().containsKey("Package_Flight__r/Campaign_Start_Date__c")) {
					report.addColumn(col.column(
							this.isShowSummaryHeaders() ? "" : getFlightLineColumnLabelHashMap().get("Package_Flight__r/Campaign_Start_Date__c"),
							this.isShowSummaryHeaders() ? "Parent_Flight_Line__c" : "Package_Flight__r/Campaign_Start_Date__c", type.stringType())
							.setValueFormatter(new ValueFormatter()).setHorizontalAlignment(HorizontalAlignment.CENTER).setWidth(Units.inch(1.32)));					
				}
				if(getFlightLineColumnLabelHashMap().containsKey("Package_Flight__r/Campaign_End_Date__c")) {
					report.addColumn(col.column(
							this.isShowSummaryHeaders() ? "" : getFlightLineColumnLabelHashMap().get("Package_Flight__r/Campaign_End_Date__c"),
							this.isShowSummaryHeaders() ? "Parent_Flight_Line__c" : "Package_Flight__r/Campaign_End_Date__c", type.stringType())
							.setValueFormatter(new ValueFormatter()).setHorizontalAlignment(HorizontalAlignment.CENTER).setWidth(Units.inch(1.32)));					
				}
				if(getFlightLineColumnLabelHashMap().containsKey("Package_Flight__r/Duration_And_Type__c")) {
					report.addColumn(col.column(
							this.isShowSummaryHeaders() ? "" : getFlightLineColumnLabelHashMap().get("Package_Flight__r/Duration_And_Type__c"),
							this.isShowSummaryHeaders() ? "Parent_Flight_Line__c" : "Package_Flight__r/Duration_And_Type__c", type.stringType()).setWidth(Units.inch(1.32)));
				}
				if(getFlightLineColumnLabelHashMap().containsKey("Weekly_Total_18_Imps__c")) {
					report.addColumn(col.column(getFlightLineColumnLabelHashMap().get("Weekly_Total_18_Imps__c"), "Weekly_Total_18_Imps__c",
							type.integerType()).setPattern("#,###").setHorizontalAlignment(HorizontalAlignment.RIGHT).setWidth(Units.inch(1.32)));
				}
				if(getFlightLineColumnLabelHashMap().containsKey("In_Mkt_Imps__c")) {
					report.addColumn(col.column(getFlightLineColumnLabelHashMap().get("In_Mkt_Imps__c"), "In_Mkt_Imps__c",
							type.doubleType()).setPattern("#,###").setHorizontalAlignment(HorizontalAlignment.RIGHT).setWidth(Units.inch(1.32)));
				}
				if(getFlightLineColumnLabelHashMap().containsKey("Total_Imps__c")) {
					report.addColumn(col.column(getFlightLineColumnLabelHashMap().get("Total_Imps__c"), "Total_Imps__c",
							type.doubleType()).setPattern("#,###").setHorizontalAlignment(HorizontalAlignment.RIGHT).setWidth(Units.inch(1.32)));
				}
				if(getFlightLineColumnLabelHashMap().containsKey("In_Mkt_TRP__c")) {
					report.addColumn(col.column(getFlightLineColumnLabelHashMap().get("In_Mkt_TRP__c"), "In_Mkt_TRP__c",
							type.doubleType()).setPattern("##0.0").setHorizontalAlignment(HorizontalAlignment.RIGHT).setWidth(Units.inch(1.32)));
				}
				if(getFlightLineColumnLabelHashMap().containsKey("PlanTRP__c")) {
					report.addColumn(col.column(getFlightLineColumnLabelHashMap().get("PlanTRP__c"), "PlanTRP__c",
							type.doubleType()).setPattern("##0.0").setHorizontalAlignment(HorizontalAlignment.RIGHT).setWidth(Units.inch(1.32)));
				}
				if(getFlightLineColumnLabelHashMap().containsKey("Plan_Imps_Reach_Perc__c")) {
					report.addColumn(col.column(getFlightLineColumnLabelHashMap().get("Plan_Imps_Reach_Perc__c"), "Plan_Imps_Reach_Perc__c",
							type.percentageType()).setPattern("##0.0").setHorizontalAlignment(HorizontalAlignment.RIGHT).setWidth(Units.inch(1.32)));
				}
				if(getFlightLineColumnLabelHashMap().containsKey("Plan_Imps_Avg_Frequency__c")) {
					report.addColumn(col.column(getFlightLineColumnLabelHashMap().get("Plan_Imps_Avg_Frequency__c"), "Plan_Imps_Avg_Frequency__c",
							type.doubleType()).setPattern("##0.0").setHorizontalAlignment(HorizontalAlignment.RIGHT).setWidth(Units.inch(1.32)));
				}
				if(getFlightLineColumnLabelHashMap().containsKey("X4_Wk_Proposed_Price__c")) {
					report.addColumn(col.column(
							this.isShowSummaryHeaders() ? "" : getFlightLineColumnLabelHashMap().get("X4_Wk_Proposed_Price__c"),
							this.isShowSummaryHeaders() ? "OB_Summ_Num__c" : "X4_Wk_Proposed_Price__c", currencyWithoutFractionDecimalType)
							.setHorizontalAlignment(HorizontalAlignment.RIGHT).setWidth(Units.inch(1.32)));
				}
				if(getFlightLineColumnLabelHashMap().containsKey("Total_Price_0d__c")) {
					report.addColumn(col.column(getFlightLineColumnLabelHashMap().get("Total_Price_0d__c"), "Total_Price_0d__c",
							currencyWithoutFractionDecimalType).setHorizontalAlignment(HorizontalAlignment.RIGHT).setWidth(Units.inch(1.32)));
				}
				if(getFlightLineColumnLabelHashMap().containsKey("TotalInMarketCPM_0d__c")) {
					report.addColumn(col.column(getFlightLineColumnLabelHashMap().get("TotalInMarketCPM_0d__c"), "TotalInMarketCPM_0d__c",
							currencyWithFractionDecimalType).setHorizontalAlignment(HorizontalAlignment.RIGHT).setWidth(Units.inch(1.32)));
				}
				if(getFlightLineColumnLabelHashMap().containsKey("CPP_0d__c")) {
					report.addColumn(col.column(getFlightLineColumnLabelHashMap().get("CPP_0d__c"), "CPP_0d__c",
							currencyWithoutFractionDecimalType).setHorizontalAlignment(HorizontalAlignment.RIGHT).setWidth(Units.inch(1.32)));
				}
				if(getFlightLineColumnLabelHashMap().containsKey("Package_Flight__r/Flight_Comments__c")) {
					report.addColumn(col.column(
							this.isShowSummaryHeaders() ? "" : getFlightLineColumnLabelHashMap().get("Package_Flight__r/Flight_Comments__c"),
							this.isShowSummaryHeaders() ? "Parent_Flight_Line__c" : "Package_Flight__r/Flight_Comments__c", type.stringType()).setWidth(Units.inch(1.32)));
				}
				if(getFlightLineColumnLabelHashMap().containsKey("Network_Description__c")) {
					report.addColumn(col.column(
							this.isShowSummaryHeaders() ? "" : getFlightLineColumnLabelHashMap().get("Network_Description__c"),
							this.isShowSummaryHeaders() ? "Parent_Flight_Line__c" : "Network_Description__c", type.stringType()).setWidth(Units.inch(1.32)));
				}
				if(getFlightLineColumnLabelHashMap().containsKey("Network_Notes__c")) {
					report.addColumn(col.column(
							this.isShowSummaryHeaders() ? "" : getFlightLineColumnLabelHashMap().get("Network_Notes__c"),
							this.isShowSummaryHeaders() ? "Parent_Flight_Line__c" : "Network_Notes__c", type.stringType()).setWidth(Units.inch(1.32)));
				}
				if(getPackageMarketFlightPreviousRecordExpression().isDigitalMediaCategory()) {
					/*if(getFlightLineColumnLabelHashMap().containsKey("Max_of_Spots_per_Loop__c")) {
						report.addColumn(col.column(this.isShowSummaryHeaders() ? ""
								: getFlightLineColumnLabelHashMap().get("Max_of_Spots_per_Loop__c"),
								"Max_of_Spots_per_Loop__c", type.stringType()).setWidth(Units.inch(1.32)));
					}*/
					if(getFlightLineColumnLabelHashMap().containsKey("Average_Daily_Spots__c")) {
						report.addColumn(col.column(
								this.isShowSummaryHeaders() ? "" : getFlightLineColumnLabelHashMap().get("Average_Daily_Spots__c"),
								this.isShowSummaryHeaders() ? "Parent_Flight_Line__c" : "Average_Daily_Spots__c", type.stringType())
								.setHorizontalAlignment(HorizontalAlignment.RIGHT).setWidth(Units.inch(1.32)));
					}
					/*if (getFlightLineColumnLabelHashMap().containsKey("Spot_Type__c")) {
						report.addColumn(col.column(this.isShowSummaryHeaders() ? ""
								: getFlightLineColumnLabelHashMap().get("Spot_Type__c"),
								"Spot_Type__c", type.stringType()).setWidth(Units.inch(1.32)));
					}*/
				}
				if (getFlightLineColumnLabelHashMap().containsKey("X4_Wk_Base_Rate__c")) {
					report.addColumn(col.column(
							this.isShowSummaryHeaders() ? "" : getFlightLineColumnLabelHashMap().get("X4_Wk_Base_Rate__c"),
							this.isShowSummaryHeaders() ? "OB_Summ_Num__c" : "X4_Wk_Base_Rate__c", currencyWithoutFractionDecimalType)
							.setHorizontalAlignment(HorizontalAlignment.RIGHT).setWidth(Units.inch(1.32)));
				}
				if (getFlightLineColumnLabelHashMap().containsKey("X4_Wk_Floor__c")) {
					report.addColumn(col.column(
							this.isShowSummaryHeaders() ? "" : getFlightLineColumnLabelHashMap().get("X4_Wk_Floor__c"),
							this.isShowSummaryHeaders() ? "OB_Summ_Num__c" : "X4_Wk_Floor__c", currencyWithoutFractionDecimalType)
							.setHorizontalAlignment(HorizontalAlignment.RIGHT).setWidth(Units.inch(1.32)));
				}
			}

			if(!this.isExportAsExcel()) {
				if(getFlightLineColumnLabelHashMap().containsKey("Network_Name__c")) {
					report.addColumn(col.column(
							this.isShowSummaryHeaders() ? "Summary" : getFlightLineColumnLabelHashMap().get("Network_Name__c"),
							this.isShowSummaryHeaders() ? "Package_Flight__r/Name" : "Network_Name__c", type.stringType()));
				}
				if(getFlightLineColumnLabelHashMap().containsKey("Number_of_Panels__c")) {
					report.addColumn(col.column(getFlightLineColumnLabelHashMap().get("Number_of_Panels__c"), "Number_of_Panels__c",
							type.integerType()).setPattern("#,###").setHorizontalAlignment(HorizontalAlignment.RIGHT)).setColumnTitleStyle(getColumnTitleStyle());
				}
				if(getFlightLineColumnLabelHashMap().containsKey("Package_Flight__r/Campaign_Start_Date__c")) {
					report.addColumn(col.column(
							this.isShowSummaryHeaders() ? "" : getFlightLineColumnLabelHashMap().get("Package_Flight__r/Campaign_Start_Date__c"),
							this.isShowSummaryHeaders() ? "Parent_Flight_Line__c" : "Package_Flight__r/Campaign_Start_Date__c", type.stringType())
							.setHorizontalAlignment(HorizontalAlignment.CENTER).setValueFormatter(new ValueFormatter()));					
				}
				if(getFlightLineColumnLabelHashMap().containsKey("Package_Flight__r/Campaign_End_Date__c")) {
					report.addColumn(col.column(
							this.isShowSummaryHeaders() ? "" : getFlightLineColumnLabelHashMap().get("Package_Flight__r/Campaign_End_Date__c"),
							this.isShowSummaryHeaders() ? "Parent_Flight_Line__c" : "Package_Flight__r/Campaign_End_Date__c", type.stringType())
							.setHorizontalAlignment(HorizontalAlignment.CENTER).setValueFormatter(new ValueFormatter()));					
				}
				if(getFlightLineColumnLabelHashMap().containsKey("Weekly_Total_18_Imps__c")) {
					report.addColumn(col.column(getFlightLineColumnLabelHashMap().get("Weekly_Total_18_Imps__c"), "Weekly_Total_18_Imps__c",
							type.integerType()).setPattern("#,###").setHorizontalAlignment(HorizontalAlignment.RIGHT));
				}
				if(getFlightLineColumnLabelHashMap().containsKey("In_Mkt_Imps__c")) {
					report.addColumn(col.column(getFlightLineColumnLabelHashMap().get("In_Mkt_Imps__c"), "In_Mkt_Imps__c",
							type.doubleType()).setPattern("#,###").setHorizontalAlignment(HorizontalAlignment.RIGHT));
				}
				if(getFlightLineColumnLabelHashMap().containsKey("Total_Imps__c")) {
					report.addColumn(col.column(getFlightLineColumnLabelHashMap().get("Total_Imps__c"), "Total_Imps__c",
							type.doubleType()).setPattern("#,###").setHorizontalAlignment(HorizontalAlignment.RIGHT));
				}
				if(getFlightLineColumnLabelHashMap().containsKey("In_Mkt_TRP__c")) {
					report.addColumn(col.column(getFlightLineColumnLabelHashMap().get("In_Mkt_TRP__c"), "In_Mkt_TRP__c",
							type.doubleType()).setPattern("##0.0").setHorizontalAlignment(HorizontalAlignment.RIGHT));
				}
				if(getFlightLineColumnLabelHashMap().containsKey("PlanTRP__c")) {
					report.addColumn(col.column(getFlightLineColumnLabelHashMap().get("PlanTRP__c"), "PlanTRP__c",
							type.doubleType()).setPattern("##0.0").setHorizontalAlignment(HorizontalAlignment.RIGHT));
				}
				if(getFlightLineColumnLabelHashMap().containsKey("Plan_Imps_Reach_Perc__c")) {
					report.addColumn(col.column(getFlightLineColumnLabelHashMap().get("Plan_Imps_Reach_Perc__c"), "Plan_Imps_Reach_Perc__c",
							type.percentageType()).setPattern("##0.0").setHorizontalAlignment(HorizontalAlignment.RIGHT));
				}
				if(getFlightLineColumnLabelHashMap().containsKey("Plan_Imps_Avg_Frequency__c")) {
					report.addColumn(col.column(getFlightLineColumnLabelHashMap().get("Plan_Imps_Avg_Frequency__c"), "Plan_Imps_Avg_Frequency__c",
							type.doubleType()).setHorizontalAlignment(HorizontalAlignment.RIGHT));
				}
				if(getFlightLineColumnLabelHashMap().containsKey("X4_Wk_Proposed_Price__c")) {
					report.addColumn(col.column(
							this.isShowSummaryHeaders() ? "" : getFlightLineColumnLabelHashMap().get("X4_Wk_Proposed_Price__c"),
							this.isShowSummaryHeaders() ? "OB_Summ_Num__c" : "X4_Wk_Proposed_Price__c",
							currencyWithoutFractionDecimalType).setHorizontalAlignment(HorizontalAlignment.RIGHT));
				}
				if(getFlightLineColumnLabelHashMap().containsKey("Total_Price_0d__c")) {
					report.addColumn(col.column(getFlightLineColumnLabelHashMap().get("Total_Price_0d__c"), "Total_Price_0d__c",
							currencyWithoutFractionDecimalType).setHorizontalAlignment(HorizontalAlignment.RIGHT));
				}
				if(getFlightLineColumnLabelHashMap().containsKey("TotalInMarketCPM_0d__c")) {
					report.addColumn(col.column(getFlightLineColumnLabelHashMap().get("TotalInMarketCPM_0d__c"), "TotalInMarketCPM_0d__c",
							currencyWithFractionDecimalType).setHorizontalAlignment(HorizontalAlignment.RIGHT));
				}
				if(getFlightLineColumnLabelHashMap().containsKey("CPP_0d__c")) {
					report.addColumn(col.column(getFlightLineColumnLabelHashMap().get("CPP_0d__c"),	"CPP_0d__c",
							currencyWithoutFractionDecimalType).setHorizontalAlignment(HorizontalAlignment.RIGHT));
				}
				if(getFlightLineColumnLabelHashMap().containsKey("Package_Flight__r/Flight_Comments__c")) {
					report.addColumn(col.column(
							this.isShowSummaryHeaders() ? "" : getFlightLineColumnLabelHashMap().get("Package_Flight__r/Flight_Comments__c"),
							this.isShowSummaryHeaders() ? "Parent_Flight_Line__c" : "Package_Flight__r/Flight_Comments__c", type.stringType()));
				}
				if(getFlightLineColumnLabelHashMap().containsKey("Network_Description__c")) {
					report.addColumn(col.column(
							this.isShowSummaryHeaders() ? "" : getFlightLineColumnLabelHashMap().get("Network_Description__c"),
							this.isShowSummaryHeaders() ? "Parent_Flight_Line__c" : "Network_Description__c", type.stringType())
							.setWidth(Units.inch(2.5)));
				}
				if(getFlightLineColumnLabelHashMap().containsKey("Network_Notes__c")) {
					report.addColumn(col.column(
							this.isShowSummaryHeaders() ? "" : getFlightLineColumnLabelHashMap().get("Network_Notes__c"),
							this.isShowSummaryHeaders() ? "Parent_Flight_Line__c" : "Network_Notes__c", type.stringType()));
				}
				if(getPackageMarketFlightPreviousRecordExpression().isDigitalMediaCategory()) {
					/*if(getFlightLineColumnLabelHashMap().containsKey("Max_of_Spots_per_Loop__c")) {
						report.addColumn(col.column(this.isShowSummaryHeaders() ? ""
								: getFlightLineColumnLabelHashMap().get("Max_of_Spots_per_Loop__c"),
								"Spots_Per_Display_Per_Day__c", type.stringType()));
					}*/
					if(getFlightLineColumnLabelHashMap().containsKey("Average_Daily_Spots__c")) {
						report.addColumn(col.column(
							this.isShowSummaryHeaders() ? "" : getFlightLineColumnLabelHashMap().get("Average_Daily_Spots__c"),
							this.isShowSummaryHeaders() ? "Parent_Flight_Line__c" : "Average_Daily_Spots__c", type.stringType())
							.setHorizontalAlignment(HorizontalAlignment.RIGHT));
					}/*
					if (getFlightLineColumnLabelHashMap().containsKey("Spot_Type__c")) {
						report.addColumn(col.column(this.isShowSummaryHeaders() ? ""
								: getFlightLineColumnLabelHashMap().get("Spot_Type__c"),
								"Spot_Type__c", type.stringType()));
					}*/
				}
			}

			// ================================= add columns: end

			if(!isShowSummaryHeaders()) {
				report
					.columns(
							//col.column("Id", "Id", type.stringType())
							//,col.column("Parent_Flight_Line__c", "Parent_Flight_Line__c", type.stringType())
	//						,col.column(getFlightLineColumnLabelHashMap().get("Network_Name__c"), "Network_Name__c", type.stringType())
	//						,col.column(getFlightLineColumnLabelHashMap().get("Number_of_Panels__c"), "Number_of_Panels__c", type.stringType())
							)
					.detailFooter(cmp.text(""), networkDetailSubreport);
			}

			// add fields
			report.addField(field("Package_Flight__r/Id", type.stringType()));
			report.addField(field("Package_Flight__r/Type__c", type.stringType()));
			report.addField(field("Id", type.stringType()));
			report.addField(field("Parent_Flight_Line__c", type.stringType()));

			// filter
			report.setFilterExpression(new FilterByFlightIdExpression(getPackageMarketFlightPreviousRecordExpression()));

			// style
			report.setColumnTitleStyle(getColumnTitleStyle());
			report.setColumnStyle(getColumnStyle());

			// add a blank line at the end
			report.addLastPageFooter(cmp.text(""));

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

		public StyleBuilder getFlightHeaderStyle() {
			return flightHeaderStyle;
		}

		public void setFlightHeaderStyle(StyleBuilder flightHeaderStyle) {
			this.flightHeaderStyle = flightHeaderStyle;
		}

		public StyleBuilder getFlightHeaderValueStyle() {
			return flightHeaderValueStyle;
		}

		public void setFlightHeaderValueStyle(
				StyleBuilder flightHeaderValueStyle) {
			this.flightHeaderValueStyle = flightHeaderValueStyle;
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

			// ================================= add columns: begin
			// TODO: network detail columns (Excel)
			if(this.isExportAsExcel()) {
				TextColumnBuilder<Integer> mapLocNumberColumn;
				if(getFlightLineColumnLabelHashMap().containsKey("MapLocation_Number__c")) {
					try {
						mapLocNumberColumn = col.column(
								isShowSummaryHeaders() ? "Summary" : getFlightLineColumnLabelHashMap().get("MapLocation_Number__c"),
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
				if(getFlightLineColumnLabelHashMap().containsKey("MarketName__c")) {
					report.addColumn(
						col.column(
							this.isShowSummaryHeaders() ? "" : getFlightLineColumnLabelHashMap().get("MarketName__c"),
							"MarketName__c", type.stringType())
							.setWidth(Units.inch(1.32))).setColumnTitleStyle(getColumnTitleStyle());
				}
				if(getFlightLineColumnLabelHashMap().containsKey("Panel_Id_Label__c")) {
					report.addColumn(col.column(
							this.isShowSummaryHeaders() ? "" : getFlightLineColumnLabelHashMap().get("Panel_Id_Label__c"),
							"Panel_Id_Label__c", type.stringType())
							.setHorizontalAlignment(HorizontalAlignment.RIGHT).setWidth(Units.inch(1.32)));
				}
				if(getFlightLineColumnLabelHashMap().containsKey("TAB_Id__c")) {
					report.addColumn(col.column(
							this.isShowSummaryHeaders() ? "" : getFlightLineColumnLabelHashMap().get("TAB_Id__c"),
							"TAB_Id__c", type.stringType())
							.setHorizontalAlignment(HorizontalAlignment.RIGHT).setWidth(Units.inch(1.32)));
				}
				if(getFlightLineColumnLabelHashMap().containsKey("Location_Description__c")) {
					report.addColumn(col.column(
							this.isShowSummaryHeaders() ? "Summary" : getFlightLineColumnLabelHashMap().get("Location_Description__c"),
							"Location_Description__c", type.stringType()).setWidth(Units.inch(1.32)));
				}
				// TODO: network detail columns. implement "Media Product" column in salesforce object: PFL. Add formula field (Panel__r.Quattro_Media_Product__c). Add to fieldset
				if(getFlightLineColumnLabelHashMap().containsKey("Quattro_Media_Product__c")) {
					report.addColumn(col.column(
							this.isShowSummaryHeaders() ? "" : getFlightLineColumnLabelHashMap().get("Quattro_Media_Product__c"),
							"Quattro_Media_Product__c", type.stringType())
							.setHorizontalAlignment(HorizontalAlignment.CENTER).setWidth(Units.inch(1.32)));
				}
				if(getFlightLineColumnLabelHashMap().containsKey("Face_Direction__c")) {
					report.addColumn(col.column(
							this.isShowSummaryHeaders() ? "" : getFlightLineColumnLabelHashMap().get("Face_Direction__c"),
							"Face_Direction__c", type.stringType())
							.setHorizontalAlignment(HorizontalAlignment.LEFT).setWidth(Units.inch(1.32)));
				}
				if(getFlightLineColumnLabelHashMap().containsKey("Unit_Size__c")) {
					report.addColumn(col.column(
							this.isShowSummaryHeaders() ? "" : getFlightLineColumnLabelHashMap().get("Unit_Size__c"),
							"Unit_Size__c", type.stringType())
							.setHorizontalAlignment(HorizontalAlignment.RIGHT).setWidth(Units.inch(1.32)));
				}
	            if(getFlightLineColumnLabelHashMap().containsKey("Weekly_Total_18_Imps__c")) {
	                report.addColumn(col.column(
	                        this.isShowSummaryHeaders() ? "" : getFlightLineColumnLabelHashMap().get("Weekly_Total_18_Imps__c"),
	                        "Weekly_Total_18_Imps__c", type.integerType()).setPattern("#,###")
	                        .setHorizontalAlignment(HorizontalAlignment.RIGHT).setWidth(Units.inch(1.32)));
	            }
	            if(getFlightLineColumnLabelHashMap().containsKey("In_Mkt_Imps__c")) {
	                report.addColumn(col.column(
	                        this.isShowSummaryHeaders() ? "" : getFlightLineColumnLabelHashMap().get("In_Mkt_Imps__c"),
	                        "In_Mkt_Imps__c", type.doubleType()).setPattern("#,###")
	                        .setHorizontalAlignment(HorizontalAlignment.RIGHT).setWidth(Units.inch(1.32)));
	            }
	            if(getFlightLineColumnLabelHashMap().containsKey("Total_Imps__c")) {
	                report.addColumn(col.column(
	                        this.isShowSummaryHeaders() ? "" : getFlightLineColumnLabelHashMap().get("Total_Imps__c"),
	                        "Total_Imps__c", type.doubleType()).setPattern("#,###")
	                        .setHorizontalAlignment(HorizontalAlignment.RIGHT).setWidth(Units.inch(1.32)));
	            }
	            if(getFlightLineColumnLabelHashMap().containsKey("In_Mkt_TRP__c")) {
	                report.addColumn(col.column(
	                        this.isShowSummaryHeaders() ? "" : getFlightLineColumnLabelHashMap().get("In_Mkt_TRP__c"),
	                        "In_Mkt_TRP__c", type.doubleType()).setPattern("##0.0")
	                        .setHorizontalAlignment(HorizontalAlignment.RIGHT).setWidth(Units.inch(1.32)));
	            }
	            if(getFlightLineColumnLabelHashMap().containsKey("PlanTRP__c")) {
	                report.addColumn(col.column(
	                        this.isShowSummaryHeaders() ? "" : getFlightLineColumnLabelHashMap().get("PlanTRP__c"),
	                        "PlanTRP__c", type.doubleType()).setPattern("##0.0")
	                        .setHorizontalAlignment(HorizontalAlignment.RIGHT).setWidth(Units.inch(1.32)));
	            }
	            if(getFlightLineColumnLabelHashMap().containsKey("Plan_Imps_Reach_Perc__c")) {
	                report.addColumn(col.column(
	                		this.isShowSummaryHeaders() ? "" : getFlightLineColumnLabelHashMap().get("Plan_Imps_Reach_Perc__c"),
	                        "Plan_Imps_Reach_Perc__c", type.percentageType()).setPattern("##0.0")
	                        .setHorizontalAlignment(HorizontalAlignment.RIGHT).setWidth(Units.inch(1.32)));
	            }
	            if(getFlightLineColumnLabelHashMap().containsKey("Plan_Imps_Avg_Frequency__c")) {
	                report.addColumn(col.column(
	                        this.isShowSummaryHeaders() ? "" : getFlightLineColumnLabelHashMap().get("Plan_Imps_Avg_Frequency__c"),
	                        "Plan_Imps_Avg_Frequency__c", type.doubleType()).setPattern("##0.0")
	                        .setHorizontalAlignment(HorizontalAlignment.RIGHT).setWidth(Units.inch(1.32)));
	            }
			}

			// TODO: network detail columns (PDF)
			if(!this.isExportAsExcel()) {
				TextColumnBuilder<Integer> mapLocNumberColumn;
				if(getFlightLineColumnLabelHashMap().containsKey("MapLocation_Number__c")) {
					try {
						mapLocNumberColumn = col.column(
								isShowSummaryHeaders() ? "Summary" : getFlightLineColumnLabelHashMap().get("MapLocation_Number__c"),
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
				if(getFlightLineColumnLabelHashMap().containsKey("MarketName__c")) {
					report.addColumn(col.column(
							this.isShowSummaryHeaders() ? "" : getFlightLineColumnLabelHashMap().get("MarketName__c"),
							"MarketName__c", type.stringType())).setColumnTitleStyle(getColumnTitleStyle());
				}
				if(getFlightLineColumnLabelHashMap().containsKey("Location_Description__c")) {
					report.addColumn(col.column(
							this.isShowSummaryHeaders() ? "Summary" : getFlightLineColumnLabelHashMap().get("Location_Description__c"),
							"Location_Description__c", type.stringType()).setWidth(Units.inch(2.5)));
				}
				if(getFlightLineColumnLabelHashMap().containsKey("Panel_Id_Label__c")) {
					report.addColumn(col.column(
							this.isShowSummaryHeaders() ? "" : getFlightLineColumnLabelHashMap().get("Panel_Id_Label__c"),
							"Panel_Id_Label__c", type.stringType())
							.setHorizontalAlignment(HorizontalAlignment.RIGHT));
				}
				if(getFlightLineColumnLabelHashMap().containsKey("TAB_Id__c")) {
					report.addColumn(col.column(
							this.isShowSummaryHeaders() ? "" : getFlightLineColumnLabelHashMap().get("TAB_Id__c"),
							"TAB_Id__c", type.stringType())
							.setHorizontalAlignment(HorizontalAlignment.RIGHT));
				}
				// TODO: network detail columns. implement "Media Product" column in salesforce object: PFL. Add formula field (Panel__r.Quattro_Media_Product__c). Add to fieldset
				if(getFlightLineColumnLabelHashMap().containsKey("Quattro_Media_Product__c")) {
					report.addColumn(col.column(
							this.isShowSummaryHeaders() ? "" : getFlightLineColumnLabelHashMap().get("Quattro_Media_Product__c"),
							"Quattro_Media_Product__c", type.stringType())
							.setHorizontalAlignment(HorizontalAlignment.CENTER));
				}
				if(getFlightLineColumnLabelHashMap().containsKey("Face_Direction__c")) {
					report.addColumn(col.column(
							this.isShowSummaryHeaders() ? "" : getFlightLineColumnLabelHashMap().get("Face_Direction__c"),
							"Face_Direction__c", type.stringType()).setHorizontalAlignment(HorizontalAlignment.LEFT));
				}
				if(getFlightLineColumnLabelHashMap().containsKey("Unit_Size__c")) {
					report.addColumn(col.column(
							this.isShowSummaryHeaders() ? "" : getFlightLineColumnLabelHashMap().get("Unit_Size__c"),
							"Unit_Size__c", type.stringType()).setHorizontalAlignment(HorizontalAlignment.RIGHT));
				}
				if(getFlightLineColumnLabelHashMap().containsKey("Weekly_Total_18_Imps__c")) {
					report.addColumn(col.column(
							this.isShowSummaryHeaders() ? "" : getFlightLineColumnLabelHashMap().get("Weekly_Total_18_Imps__c"),
							"Weekly_Total_18_Imps__c", type.integerType()).setPattern("#,###")
							.setHorizontalAlignment(HorizontalAlignment.RIGHT));
				}
				if(getFlightLineColumnLabelHashMap().containsKey("In_Mkt_Imps__c")) {
					report.addColumn(col.column(
							this.isShowSummaryHeaders() ? "" : getFlightLineColumnLabelHashMap().get("In_Mkt_Imps__c"),
							"In_Mkt_Imps__c", type.doubleType()).setPattern("#,###")
							.setHorizontalAlignment(HorizontalAlignment.RIGHT));
				}
				if(getFlightLineColumnLabelHashMap().containsKey("Total_Imps__c")) {
					report.addColumn(col.column(
							this.isShowSummaryHeaders() ? "" : getFlightLineColumnLabelHashMap().get("Total_Imps__c"),
							"Total_Imps__c", type.doubleType()).setPattern("#,###")
							.setHorizontalAlignment(HorizontalAlignment.RIGHT));
				}
				if(getFlightLineColumnLabelHashMap().containsKey("In_Mkt_TRP__c")) {
					report.addColumn(col.column(
							this.isShowSummaryHeaders() ? "" : getFlightLineColumnLabelHashMap().get("In_Mkt_TRP__c"),
							"In_Mkt_TRP__c", type.doubleType()).setPattern("##0.0")
							.setHorizontalAlignment(HorizontalAlignment.RIGHT));
				}
				if(getFlightLineColumnLabelHashMap().containsKey("PlanTRP__c")) {
					report.addColumn(col.column(
							this.isShowSummaryHeaders() ? "" : getFlightLineColumnLabelHashMap().get("PlanTRP__c"),
							"PlanTRP__c", type.doubleType()).setPattern("##0.0")
							.setHorizontalAlignment(HorizontalAlignment.RIGHT));
				}
				if(getFlightLineColumnLabelHashMap().containsKey("Plan_Imps_Reach_Perc__c")) {
					report.addColumn(col
							.column(this.isShowSummaryHeaders() ? "" : getFlightLineColumnLabelHashMap().get("Plan_Imps_Reach_Perc__c"),
							"Plan_Imps_Reach_Perc__c", type.percentageType()).setPattern("##0.0")
							.setHorizontalAlignment(HorizontalAlignment.RIGHT));
				}
				if(getFlightLineColumnLabelHashMap().containsKey("Plan_Imps_Avg_Frequency__c")) {
					report.addColumn(col.column(
							this.isShowSummaryHeaders() ? "" : getFlightLineColumnLabelHashMap().get("Plan_Imps_Avg_Frequency__c"),
							"Plan_Imps_Avg_Frequency__c", type.doubleType()).setPattern("##0.0")
							.setHorizontalAlignment(HorizontalAlignment.RIGHT));
				}
			}
			// ================================= add columns: end

			// style
			//report.highlightDetailEvenRows();
			report.setColumnTitleStyle(getColumnTitleStyle());
			report.setColumnStyle(getColumnStyle());

			// filter
			report.setFilterExpression(new FilterByDetailFlightLineIdExpression(getFlightLinePreviousRecordExpression()));

			// add a blank line at the end
			report.addLastPageFooter(cmp.text(""));

			// return report
			return report;

			/*
			// types
			CurrencyWithoutFractionDecimalType currencyWithoutFractionDecimalType = new CurrencyWithoutFractionDecimalType();

			// add columns
			if (getFlightLineColumnLabelHashMap().containsKey(
					"Network_Name__c")) {
				report.addColumn(
						col.column(getFlightLineColumnLabelHashMap().get(
								"Network_Name__c"), "Network_Name__c",
								type.stringType())).setColumnTitleStyle(
						getColumnTitleStyle());
			}
			if (getFlightLineColumnLabelHashMap().containsKey(
					"Number_of_Panels__c")) {
				report.addColumn(
						col.column(
								this.isShowSummaryHeaders() ? ""
										: getFlightLineColumnLabelHashMap()
												.get("Number_of_Panels__c"),
								"Number_of_Panels__c", type.integerType())
								.setHorizontalAlignment(
										HorizontalAlignment.CENTER))
						.setColumnTitleStyle(getColumnTitleStyle());
			}
			if (getFlightLineColumnLabelHashMap().containsKey(
					"Campaign_Start_Date__c")) {
				report.addColumn(
						col.column(
								this.isShowSummaryHeaders() ? ""
										: getFlightLineColumnLabelHashMap()
												.get("Campaign_Start_Date__c"),
								"Campaign_Start_Date__c", type.integerType())
								.setHorizontalAlignment(
										HorizontalAlignment.CENTER))
						.setColumnTitleStyle(getColumnTitleStyle());
			}
			if (getFlightLineColumnLabelHashMap().containsKey(
					"Campaign_End_Date__c")) {
				report.addColumn(
						col.column(
								this.isShowSummaryHeaders() ? ""
										: getFlightLineColumnLabelHashMap()
												.get("Campaign_End_Date__c"),
								"Campaign_End_Date__c", type.integerType())
								.setHorizontalAlignment(
										HorizontalAlignment.CENTER))
						.setColumnTitleStyle(getColumnTitleStyle());
			}

			// add fields
			report.addField(field("Package_Flight__r/Id", type.stringType()));
			FieldBuilder<String> parentFlightLineBasedGroupField = field("OB_Parent_Flight_Line_Based_Group__c", type.stringType());
			report.addField(parentFlightLineBasedGroupField);

			// group by 
			report.groupBy(grp.group(parentFlightLineBasedGroupField));

			// style
			report.highlightDetailEvenRows();
			report.setTitleStyle(getColumnTitleStyle());
			report.setColumnStyle(getColumnStyle());

			// filter
			report.setFilterExpression(new FilterByFlightIdExpression(
					getPackageMarketFlightPreviousRecordExpression()));

			// add a blank line at the end
			report.addLastPageFooter(cmp.text(""));

			// return report
			return report;
			*/
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

	private class DateOnlyDate extends AbstractSimpleExpression<String> {
		private static final long serialVersionUID = 1L;

		private String fieldName;

		public DateOnlyDate(String fieldName) {
			setFieldName(fieldName);
		}

		@Override
		public String evaluate(ReportParameters reportParameters) {

			String value = reportParameters.getValue(getFieldName());
			System.out.println("DateOnlyDate, evaluate(). value: " + value);

			try {
				// get current date as yyyy-mm-dd
				SimpleDateFormat parser = new SimpleDateFormat("yyyy-MM-dd");
				java.util.Date inputDate = parser.parse(value);

				System.out.println("** date " + inputDate);


				SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy");

				//java.util.Date inputDate = formatter.parse(inputStringDate);

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

		public String getFieldName() {
			return fieldName;
		}

		public void setFieldName(String fieldName) {
			this.fieldName = fieldName;
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


	private class DateOnlyType extends DateType {
		private static final long serialVersionUID = 1L;

		@Override
		public String getPattern() {
			return "MM-dd-yyyy";
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

			String division = reportParameters
					.getValue("Package_Flight__r/Division__c");
			// System.out.println(" FlightNameReportScriptlet division "
			// +division);
			// packageMarketsSet.add(marketName);

			String marketType = reportParameters
					.getValue("Package_Flight__r/Market_Type__c");
			// System.out.println(" FlightNameReportScriptlet marketType "
			// +marketType);
			// packageMarketTypeSet.add(marketType);

			String flightName = reportParameters
					.getValue("Package_Flight__r/Name");
			// System.out.println(" FlightNameReportScriptlet flightName "
			// +flightName);
			// packageFlightNamesSet.add(flightName);

			// format date
			SimpleDateFormat dateFormatter = new SimpleDateFormat("MM/dd/yyyy");

//			Date startDate = reportParameters
//					.getValue("Package_Flight__r/Campaign_Start_Date__c");
			// System.out.println(" FlightNameReportScriptlet startDate "
			// +startDate);
			// packageFlightStartDatesSet .add(dateFormatter.format(startDate));

//			Date endDate = reportParameters
//					.getValue("Package_Flight__r/Campaign_End_Date__c");
			// System.out.println(" FlightNameReportScriptlet endDate "
			// +endDate);
			// packageFlightEndDatesSet.add(dateFormatter.format(endDate));

			String flightDuration = reportParameters
					.getValue("Package_Flight__r/Duration_And_Type__c");
			// System.out.println(" FlightNameReportScriptlet flightDuration "
			// +flightDuration);
			// packageFlightDurationSet.add(flightDuration);

			String target = reportParameters
					.getValue("Package_Flight__r/Target__c");
			// /System.out.println(" FlightNameReportScriptlet target "
			// +target);
			// packageFlightTargetValueSet.add(target);

			if (reportParameters
					.getValue("Package_Flight__r/Target_Population__c") != null) {
				Integer targetPopulation = (Integer) reportParameters
						.getValue("Package_Flight__r/Target_Population__c");
				System.out.println("  targetPopulation >>>> *************** "
						+ targetPopulation);
				DecimalFormat df = new DecimalFormat("#,###");
				String updatedTargetPopValue = df.format(targetPopulation);
				// System.out.println(" FlightNameReportScriptlet targetPopulation "
				// +targetPopulation);
				// packageFlightTargetPopulationSet.add(updatedTargetPopValue );
			}
			String mediaCategory = "";
			try {
				mediaCategory = reportParameters
						.getValue("Package_Flight__r/Media_Category__c");
				// System.out.println(" FlightNameReportScriptlet mediaCategory "
				// +mediaCategory);

			} catch (Exception e) {
				System.out.println(" exc getting Media_Category__c " + e);
			}

			System.out
					.println(" FlightNameReportScriptlet calling valid disclaimers for flight"
							+ " division "
							+ division
							+ " media category "
							+ mediaCategory);

			Set<String> disclaimerSet = disclaimerStore.getValidDisclaimers(
					flightName, division, mediaCategory);

			System.out
					.println(" FlightNameReportScriptlet calling auto disclaimers ");

			Set<String> autoDisclaimerSet = disclaimerStore.getAutoDisclaimers(
					flightName, division, mediaCategory );

			validDisclaimersSet.addAll(disclaimerSet);
			autoDisclaimersSet.addAll(autoDisclaimerSet);

		}

		@Override
		public void afterGroupInit(String groupName,
				ReportParameters reportParameters) {
			super.afterGroupInit(groupName, reportParameters);
			// System.out.println(" FlightNameReportScriptlet afterGroupInit packageFlightNamesSet "
			// +packageFlightNamesSet.size());
			/*
			 * packageMarketsSet.clear(); packageMarketTypeSet.clear();
			 * packageFlightNamesSet.clear();
			 * packageFlightStartDatesSet.clear();
			 * packageFlightEndDatesSet.clear();
			 * packageFlightDurationSet.clear();
			 * packageFlightTargetValueSet.clear();
			 * packageFlightTargetPopulationSet.clear();
			 */

			for (String discl : validDisclaimersSet) {
				System.out.println(" ******* valid disclaimers  " + discl);

			}
			for (String adiscl : autoDisclaimersSet) {
				System.out.println(" ******* auto disclaimers  " + adiscl);

			}
			// validDisclaimersSet.clear();

		}

		// public String getConcatenatedStr() {
		// System.out.println(" FlightNameReportScriptlet getConcatenatedStr packageFlightNamesInGroup  "
		// +packageFlightNamesSet .size());
		// return StringUtils.join(packageFlightNamesSet , ',');
		// }
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

	private class DisclaimerSubreportExpression extends
			AbstractSimpleExpression<JasperReportBuilder> {

		private static final long serialVersionUID = 1L;

		@Override
		public JasperReportBuilder evaluate(ReportParameters reportParameters) {
			// PenBuilder grayThinLine =
			// stl.penThin().setLineColor(Color.LIGHT_GRAY);

			PaddingBuilder padding = stl.padding(Units.inch(0.03));

			StyleBuilder columnStyle = stl.style().setPadding(padding)
			// .setBorder(grayThinLine)
					.setFontSize(8);

			JasperReportBuilder report = report();

			// create scriptlet that will concatenate every row
			DisclaimerSubreportScriptlet disclaimerSubreportScriptlet = new DisclaimerSubreportScriptlet();
			report.scriptlets(disclaimerSubreportScriptlet);

			// area for showing all joined disclaimers (come from scriptlet
			// report)
			//report.pageFooter(cmp.text(
				//	new DisclaimerSubreportFooterExpressionColumn())
					//.setFixedRows(10));

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

		// return concatenated text
		public String getFooterNote() {
			return StringUtils.join(disclaimerList, ",");
		}
	}

	private class DisclaimerSubreportFooterExpressionColumn extends
			AbstractSimpleExpression<String> {
		private static final long serialVersionUID = 1L;

		@Override
		public String evaluate(ReportParameters reportParameters) {
			return ((DisclaimerSubreportScriptlet) reportParameters
					.getScriptlet("DisclaimerSubreportScriptlet"))
					.getFooterNote();
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
			StyleBuilder columnStyle = stl.style(boldStyle).setFontSize(9);

			// field reference
			FieldBuilder<String> divisionField = field("Division__r/Name",
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
					.text(divisionField).setStyle(boldStyle), cmp
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

	/**
	 * Creates a group using fields types and labels.
	 * 
	 * @return a CustomGroupBuilder group
	 */
	private CustomGroupBuilder createGroup(PDFCombinerFile pdfCombinerFile,
			int fieldLevel, ReportStyleBuilder groupHeaderStyle,
			ReportStyleBuilder groupValueStyle) {
		String[] fieldNamesArray = pdfCombinerFile.getFieldNamesPipeDelimited()
				.get(fieldLevel - 1).split("\\|");
		String[] fieldTypesArray = pdfCombinerFile.getFieldTypesPipeDelimited()
				.get(fieldLevel - 1).split("\\|");
		String[] fieldLabelsArray = pdfCombinerFile
				.getFieldLabelsPipeDelimited().get(fieldLevel - 1).split("\\|");
		CustomGroupBuilder group = grp.group(new GroupExpression(
				fieldNamesArray, fieldTypesArray));
		group.setHeaderLayout(GroupHeaderLayout.EMPTY);
		// try removing gap statement to remove the gap from starting
		// cmp.gap(10, Units.inch(0.25)),
		// group.header(createHorizontalList(groupHeaderStyle, groupValueStyle,
		// fieldNamesArray, fieldLabelsArray));
		// group.reprintHeaderOnEachPage();
		// group.setPadding(100);
		group.setAddToTableOfContents(true);
		return group;
	}

	/**
	 * Create a horizontal list that contains a N vertical list elements inside
	 * 
	 * Equivalent composition that can be accomplished
	 * 
	 * group.header(cmp.horizontalList( cmp.verticalList(
	 * cmp.text("header 1").setStyle(groupHeaderStyle),
	 * cmp.text("value 1").setStyle(groupValueStyle) ), cmp.verticalList(
	 * cmp.text("header 2").setStyle(groupHeaderStyle),
	 * cmp.text("value 2").setStyle(groupValueStyle) ) ) );
	 * 
	 */
	private HorizontalListBuilder createHorizontalList(
			ReportStyleBuilder groupHeaderStyle,
			ReportStyleBuilder groupValueStyle, String[] fieldNamesArray,
			String[] fieldLabelsArray) {
		HorizontalListBuilder horizontalList;
		horizontalList = cmp.horizontalList();
		for (Integer index = 0; index < fieldNamesArray.length; index++) {
			horizontalList.add(createVerticalList(fieldLabelsArray[index],
					new ValueExpression(fieldNamesArray[index]),
					groupHeaderStyle, groupValueStyle));
		}
		return horizontalList;
	}

	//flightHeaderStyle, flightHeaderValueStyle, audienceFlightHeaderFieldList, Units.inch(2)
	private VerticalListBuilder createVerticalFieldTable(JasperReportBuilder report, ReportStyleBuilder groupHeaderStyle,
			ReportStyleBuilder groupValueStyle, Map<String, FieldBuilder<?>> fieldMap, Integer leftColumnWidth) {

		VerticalListBuilder verticalList;
		verticalList = cmp.verticalList();

		for(String fieldName : fieldMap.keySet()) {
			FieldBuilder<?> field = fieldMap.get(fieldName);
			String label = getFlightLineColumnLabelHashMap().get(fieldName);
			verticalList.add(createHorizontalKeyFieldList(
					label, field, groupHeaderStyle,
					groupValueStyle, leftColumnWidth));
		}

		verticalList.add(cmp.text(""));
		return verticalList;
	}

	private VerticalListBuilder createVerticalTable(
			ReportStyleBuilder groupHeaderStyle,
			ReportStyleBuilder groupValueStyle, String[] fieldNamesArray,
			String[] fieldLabelsArray, Integer leftColumnWidth) {
		VerticalListBuilder verticalList;
		verticalList = cmp.verticalList();
		for (Integer index = 0; index < fieldNamesArray.length; index++) {
			System.out.println("      creating vertical table element. label: "
					+ fieldLabelsArray[index] + " name: "
					+ fieldNamesArray[index]);
			verticalList.add(createHorizontalKeyValueList(
					fieldLabelsArray[index], new ValueExpression(
							fieldNamesArray[index]), groupHeaderStyle,
					groupValueStyle, leftColumnWidth));
		}
		verticalList.add(cmp.text(""));
		return verticalList;
	}

	private VerticalListBuilder createVerticalTable2(ReportStyleBuilder groupHeaderStyle, ReportStyleBuilder groupValueStyle,
			String[] fieldNamesArray1, String[] fieldLabelsArray1,
			String[] fieldNamesArray2, String[] fieldLabelsArray2, Integer leftColumnWidth) {
		VerticalListBuilder verticalList;
		verticalList = cmp.verticalList();
		for (Integer index = 0; index < fieldNamesArray1.length; index++) {
			System.out.println("      creating vertical table element. label: "
					+ fieldLabelsArray1[index] + " name: " + fieldNamesArray1[index]);
			verticalList.add(createHorizontalKeyValueList2(
					fieldLabelsArray1[index], new ValueExpression(fieldNamesArray1[index]),
					fieldLabelsArray2[index], new ValueExpression(fieldNamesArray2[index]),
					groupHeaderStyle, groupValueStyle, leftColumnWidth));
		}
		verticalList.add(cmp.text(""));
		return verticalList;
	}

	private VerticalListBuilder createVerticalTableNew(
			ReportStyleBuilder groupHeaderStyle,
			ReportStyleBuilder groupValueStyle, String[] fieldNamesArray,
			String[] fieldLabelsArray, Integer leftColumnWidth,
			List<String> results, int fieldLevel) {
		String result = results.get(fieldLevel - 1);
		Set<String> fieldNamesArraySelected = new HashSet<String>(
				Arrays.asList(result.split("\\|")));
		if (results.size() > fieldLevel) {
			result = results.get(fieldLevel);
			fieldNamesArraySelected.addAll(Arrays.asList(result.split("\\|")));
		}
		VerticalListBuilder verticalList;
		verticalList = cmp.verticalList();
		for (Integer index = 0; index < fieldNamesArray.length; index++) {
			System.out.println("      creating vertical table element. label: "
					+ fieldLabelsArray[index] + " name: "
					+ fieldNamesArray[index]);
			if (fieldNamesArraySelected.contains(fieldNamesArray[index]))
				verticalList.add(createHorizontalKeyValueList(
						fieldLabelsArray[index], new ValueExpression(
								fieldNamesArray[index]), groupHeaderStyle,
						groupValueStyle, leftColumnWidth));
			else
				System.out.println("     Not added in field set");
		}

		/*
		 * fieldNamesArraySelected = new
		 * HashSet<String>(Arrays.asList(result.split("\\|"))); for(Integer
		 * index = 0; index < fieldNamesArray.length; index++) {
		 * System.out.println("      creating vertical table element. label: " +
		 * fieldLabelsArray[index] + " name: " + fieldNamesArray[index]);
		 * if(fieldNamesArraySelected.contains(fieldNamesArray[index]))
		 * verticalList
		 * .add(createHorizontalKeyValueList(fieldLabelsArray[index], new
		 * ValueExpression(fieldNamesArray[index]), groupHeaderStyle,
		 * groupValueStyle, leftColumnWidth)); else
		 * System.out.println("     Not added in field set"); }
		 */
		return verticalList;
	}
	private VerticalListBuilder createCommentTable(String packageName, String flightName, String flightCommentFieldName, String packageCommentFieldName) {
		VerticalListBuilder verticalList;
		verticalList = cmp.verticalList();
		verticalList.add(cmp.text("Comments:").setStyle(stl.style().bold()).removeLineWhenBlank());
		verticalList.add(cmp.text(new ValueExpression(packageName)).setStyle(stl.style().bold()).removeLineWhenBlank());
		verticalList.add(cmp.text(new ValueExpression(packageCommentFieldName)).removeLineWhenBlank());
		verticalList.add(cmp.text(new ValueExpression(flightName)).setStyle(stl.style().bold()).removeLineWhenBlank());
		verticalList.add(cmp.text(new ValueExpression(flightCommentFieldName)).removeLineWhenBlank());
		return verticalList;
	}

	private class DisclaimerExpression extends AbstractSimpleExpression<Object> {
		private static final long serialVersionUID = -2468924311042394628L;

		public DisclaimerExpression() {

		}

		public Object evaluate(ReportParameters reportParameters) {
			System.out
					.println("******** DisclaimerExpression evaluate. getFieldName():validDisclaimersSet  "
							+ validDisclaimersSet.size());
			//return StringUtils.join(validDisclaimersSet, ',');
			
			String str = "";
			for (String disc : validDisclaimersSet) {
				System.out.println("******** valid disc " + disc);
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

		// create value expression for getting a value by field name
		public ValueExpression(String fieldName) {
			this.setFieldName(fieldName);
		}

		public Object evaluate(ReportParameters reportParameters) {
			System.out.println("evaluate. getFieldName(): "
					+ this.getFieldName());
			// reportParameters.
			Object returnValue = "";
			try {
				if (this.getFieldName() == "")
					return "";

				if (this.getFieldName().equals(
						"Package_Flight__r/Campaign_Start_Date__c")
						|| this.getFieldName().equals(
								"Package_Flight__r/Campaign_End_Date__c")) {

					// input date
					String inputStringDate = reportParameters.getValue(this.getFieldName());

					// get current date as yyyy-mm-dd
					SimpleDateFormat parser = new SimpleDateFormat("yyyy-MM-dd");
					java.util.Date inputDate = parser.parse(inputStringDate);

					System.out.println("** date " + inputDate);
					

					SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy");
					System.out.println(formatter.format(inputDate));

					// return
					returnValue = formatter.format(inputDate);

				} else if (this.getFieldName().equals(
						"Package_Flight__r/Target_Population__c")) {
					try {
						if (this.getFieldName() != null
								&& this.getFieldName() != "") {
							Integer temp = (Integer) reportParameters
									.getValue(this.getFieldName());
							System.out
									.println("   checking value target population "
											+ temp);
							if(temp != null) {

							DecimalFormat df = new DecimalFormat("#,###");
							returnValue = df.format(temp);
							}else 
								returnValue = "";
						} else {
							returnValue = "";
						}
					} catch (Exception ex) {
						System.out
								.println("   Error in target population value"
										+ reportParameters.getValue(this
												.getFieldName()));
						returnValue = reportParameters.getValue(this
								.getFieldName());
						ex.printStackTrace();
					}
				}/* else if (this.getFieldName().equals("Package_Flight__r/Duration_And_Type__c")) {
					// format this as number
					System.out.println("   will convert " + this.getFieldName());
					NumberFormat nf = NumberFormat.getNumberInstance(Locale.US); // Get NumberFormat
					nf.setMaximumFractionDigits(0);// set as you need
					String fieldValue = (String) reportParameters.getValue(this.getFieldName());
					if(fieldValue != null) {
						Number n = nf.parse(fieldValue); // Parse strings according to locale
						// set value
						//returnValue = n.intValue();
						returnValue = nf.format(n);
						System.out.println(" *******************************  " );
						System.out.println(" weeks *******************************  " + returnValue);
						System.out.println(" *******************************  " );
					}
				}*/ else {
					returnValue = reportParameters
							.getValue(this.getFieldName());
				}
				System.out.println(" returnValue " + returnValue);
			} catch (Exception ex) {
				System.out.println("   Some exception trying to get field " 
						+ this.getFieldName()  + " message: " + ex.getMessage());
					
			}

			return returnValue;
		}

		public String getFieldName() {
			return m_fieldName;
		}

		public void setFieldName(String m_fieldName) {
			this.m_fieldName = m_fieldName;
		}
	}

	/**
	 * Create a vertical list
	 */
	private VerticalListBuilder createVerticalList(String text,
			ValueExpression valueExpression,
			ReportStyleBuilder groupHeaderStyle,
			ReportStyleBuilder groupValueStyle) {
		VerticalListBuilder verticalList = cmp.verticalList();
		verticalList.add(cmp.text(text).setStyle(groupHeaderStyle));
		verticalList.add(cmp.text(valueExpression).setStyle(groupValueStyle));
		return verticalList;
	}

	/**
	 * Create a horizontal list
	 */
	private HorizontalListBuilder createHorizontalKeyFieldList(String text, FieldBuilder<?> field,
			ReportStyleBuilder groupHeaderStyle,
			ReportStyleBuilder groupValueStyle, Integer leftColumnWidth) {
		HorizontalListBuilder horizontalList = cmp.horizontalList();
		horizontalList.add(cmp.text(text).setStyle(groupHeaderStyle)
				.setFixedWidth(leftColumnWidth));
		horizontalList.add(cmp.text(field)
				.setStyle(groupValueStyle)
				.setHorizontalAlignment(HorizontalAlignment.LEFT));
		return horizontalList;
	}

	/**
	 * Create a horizontal list
	 */
	private HorizontalListBuilder createHorizontalKeyValueList(String text,
			ValueExpression valueExpression,
			ReportStyleBuilder groupHeaderStyle,
			ReportStyleBuilder groupValueStyle, Integer leftColumnWidth) {
		HorizontalListBuilder horizontalList = cmp.horizontalList();
		horizontalList.add(cmp.text(text).setStyle(groupHeaderStyle)
				.setFixedWidth(leftColumnWidth));
		horizontalList.add(cmp.text(valueExpression).setStyle(groupValueStyle));

		horizontalList.add(cmp.text(text).setStyle(groupHeaderStyle)
				.setFixedWidth(leftColumnWidth));
		horizontalList.add(cmp.text(valueExpression).setStyle(groupValueStyle));

		return horizontalList;
	}

	/**
	 * Create a horizontal list
	 */
	private HorizontalListBuilder createHorizontalKeyValueList2(
			String text, ValueExpression valueExpression,
			String text2, ValueExpression valueExpression2,
			ReportStyleBuilder groupHeaderStyle, ReportStyleBuilder groupValueStyle, Integer leftColumnWidth) {
		HorizontalListBuilder horizontalList = cmp.horizontalList();
		horizontalList.add(cmp.text(text).setStyle(groupHeaderStyle).setFixedWidth(leftColumnWidth));
		horizontalList.add(cmp.text(valueExpression).setStyle(groupValueStyle));
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
				return " "
						+ reportParameters.getValue("Package_Flight__r/Name")
						+ "$~"
						+ reportParameters
								.getValue("Package_Flight__r/Package_Name__c");

			} catch (Exception ex) {
				System.out.println(ex.getStackTrace());
			}
			return null;
		}
	}

	/**
	 * Create a custom group expression
	 */
	private class GroupExpression extends AbstractSimpleExpression<String> {
		private static final long serialVersionUID = 8509711415717638519L;
		private String[] m_fieldArray;
		private String[] m_fieldTypeArray;

		public GroupExpression(String[] fieldArray, String[] fieldTypeArray) {
			this.m_fieldArray = fieldArray;
			this.m_fieldTypeArray = fieldTypeArray;
		}

		@Override
		public String evaluate(ReportParameters reportParameters) {

			System.out.println("************ evaluate(): begin");
			System.out.println("   ReportRowNumber: "
					+ reportParameters.getReportRowNumber());

			String groupValue = "";
			String[] groupHeaderFieldNamesArray = new String[] {
					"Package_Flight__r/Name",
					"Package_Flight__r/Market_key_Name__c",
					"Package_Flight__r/Package_Market__r/Package__r/Package_Name__c",
					"Package_Flight__r/Package_Market__r/Market_Name__c" };
			Set<String> groupHeaderFieldNamesSet = new HashSet<String>(
					Arrays.asList(groupHeaderFieldNamesArray));

			for (Integer fieldIndex = 0; fieldIndex < m_fieldArray.length; fieldIndex++) {
				String fieldType = this.m_fieldTypeArray[fieldIndex];
				if (fieldType.equals("String")) {
					String valueName = this.m_fieldArray[fieldIndex];
					// System.out.println("   valueName " + valueName);
					// if(valueName.equals("Package_Flight__r/Name") ||
					// valueName.equals("Package_Flight__r/Market_key_Name__c"))
					if (groupHeaderFieldNamesSet.contains(valueName))
						groupValue = groupValue + " "
								+ reportParameters.getValue(valueName);
				}
			}

			return groupValue;
		}
	}

	private class DisclaimersDataExpression extends
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

	private class DivisionExpressionColumn extends AbstractSimpleExpression<String> {
		private static final long serialVersionUID = -2468924311042394628L;

		// store field name
		private String m_fieldName;

		// create value expression for getting a value by field name
		public DivisionExpressionColumn(String fieldName) {
			this.setFieldName(fieldName);
		}

		public String evaluate(ReportParameters reportParameters) {
			System.out.println("evaluate. getFieldName(): "
					+ this.getFieldName());
			// reportParameters.
			String returnValue = "";
			try {
				
				if (this.getFieldName().equals(
						"Package_Flight__r/Division__c")) {

					returnValue = reportParameters
							.getValue(this.getFieldName());
				}
				System.out.println(" returnValue " + returnValue);
			} catch (Exception ex) {
				returnValue = "Required field " + this.getFieldName()
						+ " not present.";
				System.out.println("   exception trying to get field "
						+ this.getFieldName());
				System.out.println("   message: " + ex.getMessage());
			}

			return returnValue;
		}

		public String getFieldName() {
			return m_fieldName;
		}

		public void setFieldName(String m_fieldName) {
			this.m_fieldName = m_fieldName;
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

			// optional, but recommended
			// read this -
			// http://stackoverflow.com/questions/13786607/normalization-in-dom-parsing-with-java-how-does-it-work
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
			pdfCombinerContentEntry.setPageNumber(this
					.getShippingInstructionsPageNumber() + 1); // page number
																// seems
																// zero-index
																// based, so
																// adding one to
																// it.
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

	/*public HashMap<String, String> getFlightLineColumnLabelHashMap() {
		return flightColumnLabelHashMap;
	}

	public void setFlightColumnLabelHashMap(
			HashMap<String, String> flightColumnLabelHashMap) {
		this.flightColumnLabelHashMap = flightColumnLabelHashMap;
	}*/

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
 }
