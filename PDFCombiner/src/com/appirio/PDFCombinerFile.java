package com.appirio;

import java.util.List;

/**
 * @author jesus
 *
 * This class represents a file that is to be combined in pdf generation process.
 */
public class PDFCombinerFile implements java.io.Serializable {
	private static final long serialVersionUID = 6562013515134452505L;
	private String fileName;
	private String pathOnClient;
	private String salesforceUrl;
	private String title;
	private String description;
	private boolean showTitle;
	private int numberOfPages;
	private int startPageNumber;
	private boolean showAppendixEntry;	// defaults to true. true = show this entry in appendix list, false = otherwise.
	private String tableOfContentsRawText;	// if isProposalReport = true, this property will contain a raw string of proposal report contents that will be shown in its section
	private boolean isProposalReport;		// true = this is a proposal report, false = otherwise. this property is filled automatically in pdfcombiner
	// field names, labels and types. define one entry for each report level
	private List<String> fieldNamesPipeDelimited;
	private List<String> fieldLabelsPipeDelimited;
	private List<String> fieldTypesPipeDelimited;
	private List<String> fieldTotalsPipeDelimited;
	private String buyType;
	public PDFCombinerFile() {
		super();
	}
	public PDFCombinerFile(String fileName, String salesforceId,
			String title, String description, boolean showTitle) {
		super();
		this.fileName = fileName;
		this.title = title;
		this.description = description;
		this.setShowTitle(showTitle);
	}
	public PDFCombinerFile(String fileName, String salesforceId,
			String title, String description, boolean showTitle, String buyType) {
		super();
		this.fileName = fileName;
		this.title = title;
		this.description = description;
		this.setShowTitle(showTitle);
		this.buyType = buyType;
	}
	public String getBuyType() {
		return buyType;
	}
	public void setBuyType(String buyType) {
		this.buyType = buyType;
		System.out.println("********** setBuyType "+buyType);
	}
	public String getFileName() {
		return fileName;
	}
	public void setFileName(String fileName) {
		System.out.println("********** fileName "+fileName);
		this.fileName = fileName;
	}
	public String getTitle() {
		return title;
	}
	public String getDescription() {
		return description;
	}
	public void setTitle(String title) {
		System.out.println("********** setTitle "+title);
		this.title = title;
	}
	public void setDescription(String description) {
		System.out.println("********** description "+description);
		this.description = description;
	}
	public String getSalesforceUrl() {
		return salesforceUrl;
	}
	public void setSalesforceUrl(String salesforceUrl) {
		System.out.println("********** SalesforceUrl "+salesforceUrl);
		this.salesforceUrl = salesforceUrl;
	}
	public String getPathOnClient() {
		return pathOnClient;
	}
	public void setPathOnClient(String pathOnClient) {
		System.out.println("********** setPathOnClient "+pathOnClient);
		this.pathOnClient = pathOnClient;
	}
	public List<String> getFieldNamesPipeDelimited() {
		System.out.println(" getFieldNamesPipeDelimited " +fieldNamesPipeDelimited);
		return fieldNamesPipeDelimited;
	}
	public void setFieldNamesPipeDelimited(List<String> fieldNamesPipeDelimited) {
		System.out.println("********** setFieldNamesPipeDelimited "+fieldNamesPipeDelimited);
		this.fieldNamesPipeDelimited = fieldNamesPipeDelimited;
	}
	public List<String> getFieldLabelsPipeDelimited() {
		System.out.println(" ************ fieldLabelsPipeDelimited " +fieldLabelsPipeDelimited);
		return fieldLabelsPipeDelimited;
	}
	public void setFieldLabelsPipeDelimited(List<String> fieldLabelsPipeDelimited) {
		System.out.println("********** setFieldLabelsPipeDelimited "+fieldLabelsPipeDelimited);
		this.fieldLabelsPipeDelimited = fieldLabelsPipeDelimited;
	}
	public List<String> getFieldTypesPipeDelimited() {
		return fieldTypesPipeDelimited;
	}
	public void setFieldTypesPipeDelimited(List<String> fieldTypesPipeDelimited) {
		System.out.println("********** setFieldTypesPipeDelimited "+fieldTypesPipeDelimited);
		this.fieldTypesPipeDelimited = fieldTypesPipeDelimited;
	}
	public List<String> getFieldTotalsPipeDelimited() {
		return fieldTotalsPipeDelimited;
	}
	public void setFieldTotalsPipeDelimited(List<String> fieldTotalsPipeDelimited) {
		System.out.println("********** setFieldTotalsPipeDelimited "+fieldTotalsPipeDelimited);
		this.fieldTotalsPipeDelimited = fieldTotalsPipeDelimited;
	}
	public int getNumberOfPages() {
		return numberOfPages;
	}
	public void setNumberOfPages(Integer numberOfPages) {
		System.out.println("********** setNumberOfPages "+numberOfPages);
		this.numberOfPages = numberOfPages;
	}
	public int getStartPageNumber() {
		return startPageNumber;
	}
	public void setStartPageNumber(Integer startPageNumber) {
		System.out.println("********** startPageNumber "+startPageNumber);
		this.startPageNumber = startPageNumber;
	}
	public boolean isShowTitle() {
		return showTitle;
	}
	public void setShowTitle(boolean showTitle) {
		System.out.println("********** setShowTitle "+showTitle);
		this.showTitle = showTitle;
	}
	public boolean isShowAppendixEntry() {
		return showAppendixEntry;
	}
	public void setShowAppendixEntry(boolean showAppendixEntry) {
		System.out.println("********** setShowAppendixEntry "+showAppendixEntry);
		this.showAppendixEntry = showAppendixEntry;
	}
	public String getTableOfContentsRawText() {
		return tableOfContentsRawText;
	}
	public void setTableOfContentsRawText(String tableOfContentsRawText) {
		System.out.println("********** tableOfContentsRawText "+tableOfContentsRawText);
		this.tableOfContentsRawText = tableOfContentsRawText;
	}
	public boolean isProposalReport() {
		return isProposalReport;
	}
	public void setIsProposalReport(boolean isProposalReport) {
		System.out.println("********** setIsProposalReport "+isProposalReport);
		this.isProposalReport = isProposalReport;
	}
}