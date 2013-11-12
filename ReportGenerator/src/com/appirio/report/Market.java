package com.appirio.report;

import com.appirio.report.ValueFormatters.NumberFormatEnum;

/**
 * Market bean class. For more info on this class, check salesforce custom object page.
 */
public class Market {
	private String id;
	private String marketName;
	private String weeklyTotal18Imps;
	private String inMktImps;
	private String targetTotalImps;
	private String inMktTRP;
	private String planTRP;
	private String totalPrice;
	private String cPM;
	private String cPP;
	private String reach;
	private String freq;

	private Package thePackage;

	public Package getPackage() {
		return thePackage;
	}

	public void setPackage(Package thePackage) {
		this.thePackage = thePackage;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getMarketName() {
		return marketName;
	}

	public void setMarketName(String marketName) {
		this.marketName = marketName;
	}

	public String getWeeklyTotal18Imps() {
		return ValueFormatters.formatNumber(NumberFormatEnum.NumberWithoutFractionDecimalType, weeklyTotal18Imps);
	}

	public void setWeeklyTotal18Imps(String weeklyTotal18Imps) {
		this.weeklyTotal18Imps = weeklyTotal18Imps;
	}

	public String getInMktImps() {
		return ValueFormatters.formatNumber(NumberFormatEnum.NumberWithoutFractionDecimalType, inMktImps);
	}

	public void setInMktImps(String inMktImps) {
		this.inMktImps = inMktImps;
	}

	public String getTargetTotalImps() {
		return ValueFormatters.formatNumber(NumberFormatEnum.NumberWithoutFractionDecimalType, targetTotalImps);
	}

	public void setTargetTotalImps(String targetTotalImps) {
		this.targetTotalImps = targetTotalImps;
	}

	public String getInMktTRP() {
		return ValueFormatters.formatNumber(NumberFormatEnum.NumberWithFractionDecimalType, inMktTRP);
	}

	public void setInMktTRP(String inMktTRP) {
		this.inMktTRP = inMktTRP;
	}

	public String getPlanTRP() {
		return ValueFormatters.formatNumber(NumberFormatEnum.NumberWithOneDecimalType, planTRP);
	}

	public void setPlanTRP(String planTRP) {
		this.planTRP = planTRP;
	}

	public String getTotalPrice() {
		return ValueFormatters.formatNumber(NumberFormatEnum.CurrencyWithoutFractionDecimalType, totalPrice);
	}

	public void setTotalPrice(String totalPrice) {
		this.totalPrice = totalPrice;
	}

	public String getcPM() {
		return ValueFormatters.formatNumber(NumberFormatEnum.CurrencyWithFractionDecimalType, cPM);
	}

	public void setcPM(String cPM) {
		this.cPM = cPM;
	}

	public String getcPP() {
		return ValueFormatters.formatNumber(NumberFormatEnum.CurrencyWithoutFractionDecimalType, cPP);
	}

	public void setcPP(String cPP) {
		this.cPP = cPP;
	}

	public String getReach() {
		return ValueFormatters.formatNumber(NumberFormatEnum.NumberWithOneDecimalType, reach);
	}

	public void setReach(String reach) {
		this.reach = reach;
	}
	
	public String getFreq() {
		return ValueFormatters.formatNumber(NumberFormatEnum.NumberWithOneDecimalType, freq);
	}

	public void setFreq(String freq) {
		this.freq = freq;
	}
	
	public Package getThePackage() {
		return thePackage;
	}

	public void setThePackage(Package thePackage) {
		this.thePackage = thePackage;
	}
}