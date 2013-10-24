package com.appirio.report;

import com.appirio.report.ValueFormatters.NumberFormatEnum;

/**
 * Flight bean class. For more info on this class, check salesforce custom object page.
 */
public class Flight {
	private Market market;

	private String id;
	private String campaignEndDate;
	private String campaignStartDate;
	private String division;
	private String durationAndType;
	private String flightComments;
	private String marketName;
	private String marketType;
	private String mediaCategory;
	private String name;
	private String packageComments;
	private String packageName;
	private String type;
	private String target;
	private String targetPopulation;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Market getMarket() {
		return market;
	}

	public void setMarket(Market market) {
		this.market = market;
	}

	public String getCampaignEndDate() {
		return ValueFormatters.formatDate(campaignEndDate);
	}

	public void setCampaignEndDate(String campaignEndDate) {
		this.campaignEndDate = campaignEndDate;
	}

	public String getCampaignStartDate() {
		return ValueFormatters.formatDate(campaignStartDate);
	}

	public void setCampaignStartDate(String campaignStartDate) {
		this.campaignStartDate = campaignStartDate;
	}

	public String getDivision() {
		return division;
	}

	public void setDivision(String division) {
		this.division = division;
	}

	public String getDurationAndType() {
		return durationAndType;
	}

	public void setDurationAndType(String durationAndType) {
		this.durationAndType = durationAndType;
	}

	public String getFlightComments() {
		return flightComments;
	}

	public void setFlightComments(String flightComments) {
		this.flightComments = flightComments;
	}

	public String getMarketName() {
		return marketName;
	}

	public void setMarketName(String marketName) {
		this.marketName = marketName;
	}

	public String getMarketType() {
		return marketType;
	}

	public void setMarketType(String marketType) {
		this.marketType = marketType;
	}

	public String getMediaCategory() {
		return mediaCategory;
	}

	public void setMediaCategory(String mediaCategory) {
		this.mediaCategory = mediaCategory;
	}

	public String getPackageComments() {
		return packageComments;
	}

	public void setPackageComments(String packageComments) {
		this.packageComments = packageComments;
	}

	public String getPackageName() {
		return packageName;
	}

	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}

	public String getTarget() {
		return target;
	}

	public void setTarget(String target) {
		this.target = target;
	}

	public String getTargetPopulation() {
		return ValueFormatters.formatNumber(NumberFormatEnum.NumberWithoutFractionDecimalType, targetPopulation);
	}

	public void setTargetPopulation(String targetPopulation) {
		this.targetPopulation = targetPopulation;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
}