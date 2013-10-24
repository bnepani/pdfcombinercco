package com.appirio.report;

import com.appirio.report.ValueFormatters.NumberFormatEnum;

/**
 * Package bean class. For more info on this class, check salesforce custom object page.
 */
public class Package {
	private String id;
	private String packageName;
	private String weeklyTotal18Imps;
	private String inMktImps;
	private String targetTotalImps;
	private String inMktTRP;
	private String planTRP;
	private String totalPrice;
	private String cPM;
	private String cPP;

	private Market market;

	public Market getMarket() {
		return market;
	}

	public void setMarket(Market market) {
		this.market = market;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getPackageName() {
		return packageName;
	}

	public void setPackageName(String packageName) {
		this.packageName = packageName;
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
}
