package com.appirio;

import java.util.List;

/**
 * @author jesus
 * This is an argument object that is passed from server to PDFCombiner application.
 * Defines titles, file names, among other pdf generation parameters.
 */
public class PDFCombinerArguments implements java.io.Serializable {
	private static final long serialVersionUID = 6562013515134802505L;

	private boolean exportToPdf;
	
	private boolean exportToXls;

	private String attachmentsUrl;

	private String sessionId;

	private String title;

	private String subTitle;

	private String outputFileName;

	private boolean showTableOfContents;

	private boolean showPageNumbering;

	private boolean showTimeAndDateStamp;
	
	private boolean showTotalProgramSummary;

	private boolean showIndividualMarketSummary;

	private boolean showIndividualFlightSummary;
	
	private boolean showCoverPage;

	private String versionNumber;

	private String dateTimeStamp;
	
	private String packageSummaryUrl;

	public boolean isShowCoverPage() {
		return showCoverPage;
	}

	public void setShowCoverPage(boolean showCoverPage) {
		this.showCoverPage = showCoverPage;
	}

	private String email;

	private List<PDFCombinerFile> contents;

	private List<PDFCombinerFile> appendixes;

	private List<PDFCombinerContentEntry> pdfCombinerContentEntryList;

	private String clientCompanyName;

	private String clientContactInformation;

	private String agencyName;

	private String agencyContactInformation;

	private String marketName;

	private String marketContactInformation;

	private String insertContentVersionUrl;

	private String contentDocumentId;

	private boolean includeOutdoorVocabularyTermsDoc;

	private boolean includeResearchToolsDoc;

	private boolean includeServiceGuaranteeDoc;

	private boolean includeProductionSpecificationDoc;

	private String disclaimerUrl;

	private String shippingInstructionsUrl;

	private String mapPanelOrderPrefUrl;

	private PDFCombinerCallback pdfCombinerCallback;
	
	private boolean excludeFlightLines;

	public boolean isExcludeFlightLines() {
		return excludeFlightLines;
	}

	public void setExcludeFlightLines(boolean excludeFlightLines) {
		this.excludeFlightLines = excludeFlightLines;
	}

	public PDFCombinerArguments() {
		super();

		this.setShowPageNumbering(true);
		this.setShowTableOfContents(true);
		this.setShowTimeAndDateStamp(true);
	}

	public List<PDFCombinerFile> getContents() {
		return this.contents;
	}

	public String getTitle() {
		return title;
	}

	public String getSubTitle() {
		return subTitle;
	}

	public void setTitle(String title) {
		this.title = title;
		System.out.println("********** title "+title);
	}

	public void setSubTitle(String subTitle) {
		this.subTitle = subTitle;
		System.out.println("********** subTitle "+subTitle);
	}

	public String getClientCompanyName() {
		return clientCompanyName;
	}

	public void setClientCompanyName(String clientCompanyName) {
		this.clientCompanyName = clientCompanyName;
		System.out.println("********** clientCompanyName "+clientCompanyName);
	}

	public String getClientContactInformation() {
		return clientContactInformation;
	}

	public String getAgencyName() {
		return agencyName;
	}

	public String getAgencyContactInformation() {
		return agencyContactInformation;
	}

	public String getMarketName() {
		return marketName;
	}

	public String getMarketContactInformation() {
		return marketContactInformation;
	}

	public void setClientContactInformation(String clientContactInformation) {
		this.clientContactInformation = clientContactInformation;
		System.out.println("********** clientContactInformation "+clientContactInformation);
	}

	public void setAgencyName(String agencyName) {
		this.agencyName = agencyName;
		System.out.println("********** agencyName "+agencyName);
	}

	public void setAgencyContactInformation(String agencyContactInformation) {
		this.agencyContactInformation = agencyContactInformation;
		System.out.println("********** agencyContactInformation "+agencyContactInformation);
	}

	public void setMarketName(String marketName) {
		this.marketName = marketName;
		System.out.println("********** marketName "+marketName);
	}

	public void setMarketContactInformation(String marketContactInformation) {
		this.marketContactInformation = marketContactInformation;
		System.out.println("********** marketContactInformation "+marketContactInformation);
	}

	public boolean isShowTableOfContents() {
		return showTableOfContents;
	}

	public boolean isShowPageNumbering() {
		return showPageNumbering;
	}

	public boolean isShowTimeAndDateStamp() {
		return showTimeAndDateStamp;
	}

	public void setShowTableOfContents(boolean showTableOfContents) {
		this.showTableOfContents = showTableOfContents;
		System.out.println("********** showTableOfContents "+showTableOfContents);
	}

	public void setShowPageNumbering(boolean showPageNumbering) {
		this.showPageNumbering = showPageNumbering;
		System.out.println("********** showPageNumbering "+showPageNumbering);
	}

	public void setShowTimeAndDateStamp(boolean showTimeAndDateStamp) {
		this.showTimeAndDateStamp = showTimeAndDateStamp;
		System.out.println("********** setShowTimeAndDateStamp "+showTimeAndDateStamp);
	}

	public List<PDFCombinerFile> getAppendixes() {
		return appendixes;
	}

	public String getSessionId() {
		return sessionId;
	}

	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
		System.out.println("********** sessionId "+sessionId);
	}

	public String getAttachmentsUrl() {
		return attachmentsUrl;
	}

	public void setAttachmentsUrl(String attachmentsUrl) {
		this.attachmentsUrl = attachmentsUrl;
		System.out.println("********** attachmentsUrl "+attachmentsUrl);
	}

	public String getOutputFileName() {
		return outputFileName;
	}

	public void setOutputFileName(String outputFileName) {
		this.outputFileName = outputFileName;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
		System.out.println("********** email "+email);
	}

	public void setContents(List<PDFCombinerFile> contents) {
		this.contents = contents;
		System.out.println("********** contents "+contents);
	}

	public void setAppendixes(List<PDFCombinerFile> appendixes) {
		this.appendixes = appendixes;
		System.out.println("********** appendixes "+appendixes);
	}

	public String getInsertContentVersionUrl() {
		return insertContentVersionUrl;
	}

	public void setInsertContentVersionUrl(String insertContentVersionUrl) {
		this.insertContentVersionUrl = insertContentVersionUrl;
		System.out.println("********** insertContentVersionUrl "+insertContentVersionUrl);
	}

	public String getContentDocumentId() {
		return contentDocumentId;
	}

	public void setContentDocumentId(String contentDocumentId) {
		this.contentDocumentId = contentDocumentId;
		System.out.println("********** contentDocumentId "+contentDocumentId);
	}

	public boolean isIncludeOutdoorVocabularyTermsDoc() {
		return includeOutdoorVocabularyTermsDoc;
	}

	public void setIncludeOutdoorVocabularyTermsDoc(
			boolean includeOutdoorVocabularyTermsDoc) {
		this.includeOutdoorVocabularyTermsDoc = includeOutdoorVocabularyTermsDoc;
		System.out.println("********** includeOutdoorVocabularyTermsDoc "+includeOutdoorVocabularyTermsDoc);
	}

	public boolean isIncludeResearchToolsDoc() {
		return includeResearchToolsDoc;
	}

	public void setIncludeResearchToolsDoc(boolean includeResearchToolsDoc) {
		this.includeResearchToolsDoc = includeResearchToolsDoc;
		System.out.println("********** includeResearchToolsDoc "+includeResearchToolsDoc);
	}

	public boolean isIncludeServiceGuaranteeDoc() {
		return includeServiceGuaranteeDoc;
	}

	public void setIncludeServiceGuaranteeDoc(boolean includeServiceGuaranteeDoc) {
		this.includeServiceGuaranteeDoc = includeServiceGuaranteeDoc;
		System.out.println("********** includeServiceGuaranteeDoc "+includeServiceGuaranteeDoc);
	}

	public boolean isIncludeProductionSpecificationDoc() {
		return includeProductionSpecificationDoc;
	}

	public void setIncludeProductionSpecificationDoc(
			boolean includeProductionSpecificationDoc) {
		this.includeProductionSpecificationDoc = includeProductionSpecificationDoc;
		System.out.println("********** includeProductionSpecificationDoc "+includeProductionSpecificationDoc);
	}

	public PDFCombinerCallback getPdfCombinerCallback() {
		return pdfCombinerCallback;
	}

	public void setPdfCombinerCallback(PDFCombinerCallback pdfCombinerCallback) {
		this.pdfCombinerCallback = pdfCombinerCallback;
		System.out.println("********** pdfCombinerCallback "+pdfCombinerCallback);
	}

	public boolean isShowTotalProgramSummary() {
		return showTotalProgramSummary;
	}

	public void setShowTotalProgramSummary(boolean showTotalProgramSummary) {
		this.showTotalProgramSummary = showTotalProgramSummary;
		System.out.println("********** showTotalProgramSummary "+showTotalProgramSummary);
	}

	public boolean isShowIndividualMarketSummary() {
		return showIndividualMarketSummary;
	}

	public void setShowIndividualMarketSummary(boolean showIndividualMarketSummary) {
		this.showIndividualMarketSummary = showIndividualMarketSummary;
		System.out.println("********** showIndividualMarketSummary "+showIndividualMarketSummary);
	}

	public boolean isShowIndividualFlightSummary() {
		return showIndividualFlightSummary;
	}

	public void setShowIndividualFlightSummary(boolean showIndividualFlightSummary) {
		this.showIndividualFlightSummary = showIndividualFlightSummary;
		System.out.println("********** showIndividualFlightSummary "+showIndividualFlightSummary);
	}

	public boolean isExportToPdf() {
		return exportToPdf;
	}

	public void setExportToPdf(boolean exportToPdf) {
		this.exportToPdf = exportToPdf;
		System.out.println("********** exportToPdf "+exportToPdf);
	}

	public boolean isExportToXls() {
		return exportToXls;
	}

	public void setExportToXls(boolean exportToXls) {
		this.exportToXls = exportToXls;
		System.out.println("********** exportToXls "+exportToXls);
	}

	/*
	 * Refresh page numbering. Takes the number of pages to set the start page number for each file.
	 * Example:
	 * pdfCombinerFile.getNumberOfPages()		pdfCombinerFile.getStartPageNumber()
	 * 		1										1
	 * 		3										2
	 * 		3										5
	 * 		1										8
	 */
	public void refreshPageNumbering(int startPageNumber) {
		if(this.appendixes != null) {
			for(PDFCombinerFile pdfCombinerFile : appendixes) {
				pdfCombinerFile.setStartPageNumber(startPageNumber);
				startPageNumber = startPageNumber + pdfCombinerFile.getNumberOfPages();
			}
		}
	}

	public String getDisclaimerUrl() {
		return disclaimerUrl;
	}

	public void setDisclaimerUrl(String disclaimerUrl) {
		this.disclaimerUrl = disclaimerUrl;
		System.out.println("********** disclaimerUrl "+disclaimerUrl);
	}

	public List<PDFCombinerContentEntry> getPdfCombinerContentEntryList() {
		return pdfCombinerContentEntryList;
	}

	public void setPdfCombinerContentEntryList(
			List<PDFCombinerContentEntry> pdfCombinerContentEntryList) {
		this.pdfCombinerContentEntryList = pdfCombinerContentEntryList;
		System.out.println("********** pdfCombinerContentEntryList "+pdfCombinerContentEntryList);
	}

	public String getMapPanelOrderPrefUrl() {
		return mapPanelOrderPrefUrl;
	}

	public void setMapPanelOrderPrefUrl(String mapPanelOrderPrefUrl) {
		this.mapPanelOrderPrefUrl = mapPanelOrderPrefUrl;
		System.out.println("********** shippingInstructionsUrl "+shippingInstructionsUrl);
	}

	public String getShippingInstructionsUrl() {
		return shippingInstructionsUrl;
	}

	public void setShippingInstructionsUrl(String shippingInstructionsUrl) {
		this.shippingInstructionsUrl = shippingInstructionsUrl;
		System.out.println("********** shippingInstructionsUrl "+shippingInstructionsUrl);
	}

	public String getVersionNumber() {
		return versionNumber;
	}

	public void setVersionNumber(String versionNumber) {
		this.versionNumber = versionNumber;
	}

	public String getDateTimeStamp() {
		return dateTimeStamp;
	}

	public void setDateTimeStamp(String dateTimeStamp) {
		this.dateTimeStamp = dateTimeStamp;
	}

	public String getPackageSummaryUrl() {
		return packageSummaryUrl;
	}

	public void setPackageSummaryUrl(String packageSummaryUrl) {
		this.packageSummaryUrl = packageSummaryUrl;
	}
}