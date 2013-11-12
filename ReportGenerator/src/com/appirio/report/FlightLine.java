package com.appirio.report;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import com.appirio.report.ValueFormatters.NumberFormatEnum;

/**
 * Flightline bean class. For more info on this class, check salesforce custom object page.
 */
public class FlightLine {
	/**
	 * numerical map location position 
	 */
	private int mapLocationNumberSort;

	/**
	 * child flight lines 
	 */
	private List<FlightLine> childFlightLines;

	private String id;
	private String mapLocationNumber;
	private String locationDescription;
	private String recordTypeId;
	private Flight flight;
	private String additionalCost;
	private String averageDailySpots;
	private String city;
	private String comments;
	private String county;
	private String cPP0d;
	private String currentCopy;
	private String discount;
	private String embellishments;
	private String faceDirection;
	private String facing;
	private String illuminationyn;
	private String illumination;
	private String inMktImps;
	private String inMktPercComp;
	private String inMktTRP;
	private String locationLatitudes;
	private String locationLongitudes;
	private String mediaProduct;
	private String networkDescription;
	private String networkName;
	private String networkNotes;
	private String netAmountValue;
	private String numberofPanels;
	private String panelIdLabel;
	private String parentFlightLine;
	private String planTRP;
	private String planImpsAvgFrequency;
	private String planImpsReachPerc;
	private String production;
	private String rideOrder;
	private String state;
	private String tABId;
	private String targetInMarketImps000;
	private String targetTotalImps000;
	private String taxAmt;
	private String timing;
	private String totalInMarketCPM0d;
	private String totalImps;
	private String totalPrice0d;
	private String unitSize;
	private String weeklyInMarketImps;
	private String weeklyMarketImps;
	private String weeklyInMarketTargetImps000;
	private String weeklyTotal18Imps000;
	private String weeklyTotal18Imps;
	private String weeklyTotalTargetImps000;
	private String x4WkAvgRateperPanel;
	private String x4WkBaseRate;
	private String x4WkFloor;
	private String x4WkProposedPrice;
	private String zip;	

	public Object getValue(String key) {

//		System.out.println("getValue(String key): called. key: " + key);

		// -- flight line
		if(key.equals("additionalCost")) return this.getAdditionalCost();
		if(key.equals("averageDailySpots")) return this.getAverageDailySpots();
		if(key.equals("city")) return this.getCity();
		if(key.equals("comments")) return this.getComments();
		if(key.equals("county")) return this.getCounty();
		if(key.equals("cPP0d")) return this.getcPP0d();
		if(key.equals("currentCopy")) return this.getCurrentCopy();
		if(key.equals("discount")) return this.getDiscount();
		if(key.equals("embellishments")) return this.getEmbellishments();
		if(key.equals("faceDirection")) return this.getFaceDirection();
		if(key.equals("facing")) return this.getFacing();
		if(key.equals("id")) return this.getId();
		if(key.equals("illuminationyn")) return this.getIlluminationyn();
		if(key.equals("illumination")) return this.getIllumination();
		if(key.equals("inMktImps")) return this.getInMktImps();
		if(key.equals("inMktPercComp")) return this.getInMktPercComp();
		if(key.equals("inMktTRP")) return this.getInMktTRP();
		if(key.equals("locationDescription")) return this.getLocationDescription();
		if(key.equals("locationLatitudes")) return this.getLocationLatitudes();
		if(key.equals("locationLongitudes")) return this.getLocationLongitudes();
		if(key.equals("mapLocationNumber")) return this.getMapLocationNumberSort();
		if(key.equals("mediaProduct")) return this.getMediaProduct();
		if(key.equals("networkDescription")) return this.getNetworkDescription();
		if(key.equals("networkName")) return this.getNetworkName();
		if(key.equals("networkNotes")) return this.getNetworkNotes();
		if(key.equals("netAmountValue")) return this.getNetAmountValue();
		if(key.equals("numberofPanels")) return this.getNumberofPanels();
		if(key.equals("panelIdLabel")) return this.getPanelIdLabel();
		if(key.equals("parentFlightLine")) return this.getParentFlightLine();
		if(key.equals("planTRP")) return this.getPlanTRP();
		if(key.equals("planImpsAvgFrequency")) return this.getPlanImpsAvgFrequency();
		if(key.equals("planImpsReachPerc")) return this.getPlanImpsReachPerc();
		if(key.equals("production")) return this.getProduction();
		if(key.equals("recordTypeId")) return this.getRecordTypeId();
		if(key.equals("rideOrder")) return this.getRideOrder();
		if(key.equals("state")) return this.getState();
		if(key.equals("tABId")) return this.gettABId();
		if(key.equals("targetInMarketImps000")) return this.getTargetInMarketImps000();
		if(key.equals("targetTotalImps000")) return this.getTargetTotalImps000();
		if(key.equals("taxAmt")) return this.getTaxAmt();
		if(key.equals("timing")) return this.getTiming();
		if(key.equals("totalInMarketCPM0d")) return this.getTotalInMarketCPM0d();
		if(key.equals("totalImps")) return this.getTotalImps();
		if(key.equals("totalPrice0d")) return this.getTotalPrice0d();
		if(key.equals("unitSize")) return this.getUnitSize();
		if(key.equals("weeklyInMarketImps")) return this.getWeeklyInMarketImps();
		if(key.equals("weeklyMarketImps")) return this.getWeeklyMarketImps();
		if(key.equals("weeklyInMarketTargetImps000")) return this.getWeeklyInMarketTargetImps000();
		if(key.equals("weeklyTotal18Imps000")) return this.getWeeklyTotal18Imps000();
		if(key.equals("weeklyTotal18Imps")) return this.getWeeklyTotal18Imps();
		if(key.equals("weeklyTotalTargetImps000")) return this.getWeeklyTotalTargetImps000();
		if(key.equals("x4WkAvgRateperPanel")) return this.getX4WkAvgRateperPanel();
		if(key.equals("x4WkBaseRate")) return this.getX4WkBaseRate();
		if(key.equals("x4WkFloor")) return this.getX4WkFloor();
		if(key.equals("x4WkProposedPrice")) return this.getX4WkProposedPrice();
		if(key.equals("zip")) return this.getZip();

		// -- flight
		if(key.equals("packageFlight_Id")) return this.getFlight().getId();
		if(key.equals("packageFlight_CampaignEndDate")) return this.getFlight().getCampaignEndDate();
		if(key.equals("packageFlight_CampaignStartDate")) return this.getFlight().getCampaignStartDate();
		if(key.equals("packageFlight_Division")) return this.getFlight().getDivision();
		if(key.equals("packageFlight_DurationAndType")) return this.getFlight().getDurationAndType();
		if(key.equals("packageFlight_FlightComments")) return this.getFlight().getFlightComments();
		if(key.equals("packageFlight_MarketName")) return this.getFlight().getMarketName();
		if(key.equals("packageFlight_MarketType")) return this.getFlight().getMarketType();
		if(key.equals("packageFlight_MediaCategory")) return this.getFlight().getMediaCategory();
		if(key.equals("packageFlight_Name")) return this.getFlight().getName();
		if(key.equals("packageFlight_PackageComments")) return this.getFlight().getPackageComments();
		if(key.equals("packageFlight_PackageName")) return this.getFlight().getPackageName();
		if(key.equals("packageFlight_Target")) return this.getFlight().getTarget();
		if(key.equals("packageFlight_TargetPopulation")) return this.getFlight().getTargetPopulation();
		if(key.equals("packageFlight_Type")) return this.getFlight().getType();

		// market
		if(key.equals("packageFlight_packageMarket_id")) return this.getFlight().getMarket().getId();
		if(key.equals("packageFlight_packageMarket_marketName")) return this.getFlight().getMarket().getMarketName();
		if(key.equals("packageFlight_packageMarket_weeklyTotal18Imps")) return this.getFlight().getMarket().getWeeklyTotal18Imps();
		if(key.equals("packageFlight_packageMarket_inMktImps")) return this.getFlight().getMarket().getInMktImps();
		if(key.equals("packageFlight_packageMarket_targetTotalImps")) return this.getFlight().getMarket().getTargetTotalImps();
		if(key.equals("packageFlight_packageMarket_inMktTRP")) return this.getFlight().getMarket().getInMktTRP();
		if(key.equals("packageFlight_packageMarket_planTRP")) return this.getFlight().getMarket().getPlanTRP();
		if(key.equals("packageFlight_packageMarket_totalPrice")) return this.getFlight().getMarket().getTotalPrice();
		if(key.equals("packageFlight_packageMarket_cPM")) return this.getFlight().getMarket().getcPM();
		if(key.equals("packageFlight_packageMarket_cPP")) return this.getFlight().getMarket().getcPP();
		if(key.equals("packageFlight_packageMarket_reach")) return this.getFlight().getMarket().getReach();
		if(key.equals("packageFlight_packageMarket_freq")) return this.getFlight().getMarket().getFreq();
		

		// -- package
		if(key.equals("packageFlight_packageMarket_package_id")) return this.getFlight().getMarket().getPackage().getId();
		if(key.equals("packageFlight_packageMarket_package_packageName")) return this.getFlight().getMarket().getPackage().getPackageName();
		if(key.equals("packageFlight_packageMarket_package_weeklyTotal18Imps")) return this.getFlight().getMarket().getPackage().getWeeklyTotal18Imps();
		if(key.equals("packageFlight_packageMarket_package_inMktImps")) return this.getFlight().getMarket().getPackage().getInMktImps();
		if(key.equals("packageFlight_packageMarket_package_targetTotalImps")) return this.getFlight().getMarket().getPackage().getTargetTotalImps();
		if(key.equals("packageFlight_packageMarket_package_inMktTRP")) return this.getFlight().getMarket().getPackage().getInMktTRP();
		if(key.equals("packageFlight_packageMarket_package_planTRP")) return this.getFlight().getMarket().getPackage().getPlanTRP();
		if(key.equals("packageFlight_packageMarket_package_totalPrice")) return this.getFlight().getMarket().getPackage().getTotalPrice();
		if(key.equals("packageFlight_packageMarket_package_cPM")) return this.getFlight().getMarket().getPackage().getcPM();
		if(key.equals("packageFlight_packageMarket_package_cPP")) return this.getFlight().getMarket().getPackage().getcPP();

		return null;
	}

	/**
	 * Add child flight line
	 * @param flightLine child flight line
	 */
	public void addChildFlightLine(FlightLine flightLine) {
//		System.out.println("   addChildFlightLine " + flightLine.getMapLocationNumber());
		this.getChildFlightLines().add(flightLine);
	}

	public String getId() {
		return this.id;
	}
	public void setId(String id) {
		this.id = id;
	}

	public String getLocationDescription() {
		return locationDescription;
	}

	public void setLocationDescription(String locationDescription) {
		this.locationDescription = locationDescription;
	}

	public Flight getFlight() {
		return flight;
	}

	public void setFlight(Flight flight) {
		this.flight = flight;
	}

	public String getMapLocationNumber() {
		return mapLocationNumber;
	}

	public void setMapLocationNumber(String mapLocationNumber) {
		this.mapLocationNumber = mapLocationNumber;
	}

	public String getRecordTypeId() {
		return recordTypeId;
	}

	public void setRecordTypeId(String recordTypeId) {
		this.recordTypeId = recordTypeId;
	}

	public int getMapLocationNumberSort() {
		return mapLocationNumberSort;
	}

	public void setMapLocationNumberSort(Integer mapLocationNumberSort) {
		this.mapLocationNumberSort = mapLocationNumberSort;
	}

	/**
	 * @return sort key: package name, market name, flight name, record type id, numeric map location number
	 */
	public String getSortKey() {

		DecimalFormat decimalFormat = new DecimalFormat("000000");

		return this.getFlight().getMarket().getPackage().getPackageName() + "."
				+ this.getFlight().getMarket().getMarketName() + "."
				+ this.getFlight().getName() + "."
				+ this.getRecordTypeId() + "."
				+ decimalFormat.format(this.getMapLocationNumberSort());
	}

	public String getAdditionalCost() {
		return ValueFormatters.formatNumber(NumberFormatEnum.CurrencyWithFractionDecimalType, additionalCost);
	}

	public void setAdditionalCost(String additionalCost) {
		this.additionalCost = additionalCost;
	}

	public String getAverageDailySpots() {
		return averageDailySpots;
	}

	public void setAverageDailySpots(String averageDailySpots) {
		this.averageDailySpots = averageDailySpots;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getComments() {
		return comments;
	}

	public void setComments(String comments) {
		this.comments = comments;
	}

	public String getCounty() {
		return county;
	}

	public void setCounty(String county) {
		this.county = county;
	}

	public String getcPP0d() {
		return ValueFormatters.formatNumber(NumberFormatEnum.CurrencyWithoutFractionDecimalType, cPP0d);
	}

	public void setcPP0d(String cPP0d) {
		this.cPP0d = cPP0d;
	}

	public String getCurrentCopy() {
		return currentCopy;
	}

	public void setCurrentCopy(String currentCopy) {
		this.currentCopy = currentCopy;
	}

	public String getDiscount() {
		return discount;
	}

	public void setDiscount(String discount) {
		this.discount = discount;
	}

	public String getEmbellishments() {
		return embellishments;
	}

	public void setEmbellishments(String embellishments) {
		this.embellishments = embellishments;
	}

	public String getFaceDirection() {
		return faceDirection;
	}

	public void setFaceDirection(String faceDirection) {
		this.faceDirection = faceDirection;
	}

	public String getFacing() {
		return facing;
	}

	public void setFacing(String facing) {
		this.facing = facing;
	}

	public String getIlluminationyn() {
		return illuminationyn;
	}

	public void setIlluminationyn(String illuminationyn) {
		this.illuminationyn = illuminationyn;
	}

	public String getIllumination() {
		return ValueFormatters.formatNumber(NumberFormatEnum.NumberWithoutFractionDecimalType, illumination);
	}

	public void setIllumination(String illumination) {
		this.illumination = illumination;
	}

	public String getInMktImps() {
		return ValueFormatters.formatNumber(NumberFormatEnum.NumberWithoutFractionDecimalType, inMktImps);
	}

	public void setInMktImps(String inMktImps) {
		this.inMktImps = inMktImps;
	}

	public String getInMktPercComp() {
		return ValueFormatters.formatNumber(NumberFormatEnum.NumberWithFractionDecimalType, inMktPercComp);
	}

	public void setInMktPercComp(String inMktPercComp) {
		this.inMktPercComp = inMktPercComp;
	}

	public String getInMktTRP() {
		return ValueFormatters.formatNumber(NumberFormatEnum.NumberWithOneDecimalType, inMktTRP);
	}

	public void setInMktTRP(String inMktTRP) {
		this.inMktTRP = inMktTRP;
	}

	public String getLocationLatitudes() {
		return locationLatitudes;
	}

	public void setLocationLatitudes(String locationLatitudes) {
		this.locationLatitudes = locationLatitudes;
	}

	public String getLocationLongitudes() {
		return locationLongitudes;
	}

	public void setLocationLongitudes(String locationLongitudes) {
		this.locationLongitudes = locationLongitudes;
	}

	public String getMediaProduct() {
		return mediaProduct;
	}

	public void setMediaProduct(String mediaProduct) {
		this.mediaProduct = mediaProduct;
	}

	public String getNetworkDescription() {
		return networkDescription;
	}

	public void setNetworkDescription(String networkDescription) {
		this.networkDescription = networkDescription;
	}

	public String getNetworkName() {
		return networkName;
	}

	public void setNetworkName(String networkName) {
		this.networkName = networkName;
	}

	public String getNetworkNotes() {
		return networkNotes;
	}

	public void setNetworkNotes(String networkNotes) {
		this.networkNotes = networkNotes;
	}

	public String getNetAmountValue() {
		return ValueFormatters.formatNumber(NumberFormatEnum.CurrencyWithoutFractionDecimalType, netAmountValue);
	}

	public void setNetAmountValue(String netAmountValue) {
		this.netAmountValue = netAmountValue;
	}

	public String getNumberofPanels() {
		return ValueFormatters.formatNumber(NumberFormatEnum.NumberWithoutFractionDecimalType, numberofPanels);
	}

	public void setNumberofPanels(String numberofPanels) {
		this.numberofPanels = numberofPanels;
	}

	public String getPanelIdLabel() {
		return panelIdLabel;
	}

	public void setPanelIdLabel(String panelIdLabel) {
		this.panelIdLabel = panelIdLabel;
	}

	public String getPlanTRP() {
		return ValueFormatters.formatNumber(NumberFormatEnum.NumberWithOneDecimalType, planTRP);
	}

	public void setPlanTRP(String planTRP) {
		this.planTRP = planTRP;
	}

	public String getPlanImpsAvgFrequency() {
		return ValueFormatters.formatNumber(NumberFormatEnum.NumberWithOneDecimalType, planImpsAvgFrequency);
	}

	public void setPlanImpsAvgFrequency(String planImpsAvgFrequency) {
		this.planImpsAvgFrequency = planImpsAvgFrequency;
	}

	public String getPlanImpsReachPerc() {
		return ValueFormatters.formatNumber(NumberFormatEnum.NumberWithOneDecimalType, planImpsReachPerc);
	}

	public void setPlanImpsReachPerc(String planImpsReachPerc) {
		this.planImpsReachPerc = planImpsReachPerc;
	}

	public String getProduction() {
		return ValueFormatters.formatNumber(NumberFormatEnum.CurrencyWithFractionDecimalType, production);
	}

	public void setProduction(String production) {
		this.production = production;
	}

	public String getRideOrder() {
		return rideOrder;
	}

	public void setRideOrder(String rideOrder) {
		this.rideOrder = rideOrder;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public String gettABId() {
		return tABId;
	}

	public void settABId(String tABId) {
		this.tABId = tABId;
	}

	public String getTargetInMarketImps000() {
		return ValueFormatters.formatNumber(NumberFormatEnum.NumberWithoutFractionDecimalType, targetInMarketImps000);
	}

	public void setTargetInMarketImps000(String targetInMarketImps000) {
		this.targetInMarketImps000 = targetInMarketImps000;
	}

	public String getTargetTotalImps000() {
		return ValueFormatters.formatNumber(NumberFormatEnum.NumberWithoutFractionDecimalType, targetTotalImps000);
	}

	public void setTargetTotalImps000(String targetTotalImps000) {
		this.targetTotalImps000 = targetTotalImps000;
	}

	public String getTaxAmt() {
		return ValueFormatters.formatNumber(NumberFormatEnum.CurrencyWithFractionDecimalType, taxAmt);
	}

	public void setTaxAmt(String taxAmt) {
		this.taxAmt = taxAmt;
	}

	public String getTiming() {
		return timing;
	}

	public void setTiming(String timing) {
		this.timing = timing;
	}

	public String getTotalInMarketCPM0d() {
		return ValueFormatters.formatNumber(NumberFormatEnum.CurrencyWithFractionDecimalType, totalInMarketCPM0d);
	}

	public void setTotalInMarketCPM0d(String totalInMarketCPM0d) {
		this.totalInMarketCPM0d = totalInMarketCPM0d;
	}

	public String getTotalImps() {
		return ValueFormatters.formatNumber(NumberFormatEnum.NumberWithoutFractionDecimalType, totalImps);
	}

	public void setTotalImps(String totalImps) {
		this.totalImps = totalImps;
	}

	public String getTotalPrice0d() {
		return ValueFormatters.formatNumber(NumberFormatEnum.CurrencyWithoutFractionDecimalType, totalPrice0d);
	}

	public void setTotalPrice0d(String totalPrice0d) {
		this.totalPrice0d = totalPrice0d;
	}

	public String getUnitSize() {
		return unitSize;
	}

	public void setUnitSize(String unitSize) {
		this.unitSize = unitSize;
	}

	public String getWeeklyInMarketImps() {
		return ValueFormatters.formatNumber(NumberFormatEnum.NumberWithoutFractionDecimalType, weeklyInMarketImps);
	}

	public void setWeeklyInMarketImps(String weeklyInMarketImps) {
		this.weeklyInMarketImps = weeklyInMarketImps;
	}

	public String getWeeklyMarketImps() {
		return ValueFormatters.formatNumber(NumberFormatEnum.NumberWithoutFractionDecimalType, weeklyMarketImps);
	}

	public void setWeeklyMarketImps(String weeklyMarketImps) {
		this.weeklyMarketImps = weeklyMarketImps;
	}

	public String getWeeklyInMarketTargetImps000() {
		return ValueFormatters.formatNumber(NumberFormatEnum.NumberWithoutFractionDecimalType, weeklyInMarketTargetImps000);
	}

	public void setWeeklyInMarketTargetImps000(String weeklyInMarketTargetImps000) {
		this.weeklyInMarketTargetImps000 = weeklyInMarketTargetImps000;
	}

	public String getWeeklyTotal18Imps000() {
		return ValueFormatters.formatNumber(NumberFormatEnum.NumberWithoutFractionDecimalType, weeklyTotal18Imps000);
	}

	public void setWeeklyTotal18Imps000(String weeklyTotal18Imps000) {
		this.weeklyTotal18Imps000 = weeklyTotal18Imps000;
	}

	public String getWeeklyTotal18Imps() {
		return ValueFormatters.formatNumber(NumberFormatEnum.NumberWithoutFractionDecimalType, weeklyTotal18Imps);
	}

	public void setWeeklyTotal18Imps(String weeklyTotal18Imps) {
		this.weeklyTotal18Imps = weeklyTotal18Imps;
	}

	public String getWeeklyTotalTargetImps000() {
		return ValueFormatters.formatNumber(NumberFormatEnum.NumberWithoutFractionDecimalType, weeklyTotalTargetImps000);
	}

	public void setWeeklyTotalTargetImps000(String weeklyTotalTargetImps000) {
		this.weeklyTotalTargetImps000 = weeklyTotalTargetImps000;
	}

	public String getX4WkAvgRateperPanel() {
		return x4WkAvgRateperPanel;
	}

	public void setX4WkAvgRateperPanel(String x4WkAvgRateperPanel) {
		this.x4WkAvgRateperPanel = x4WkAvgRateperPanel;
	}

	public String getX4WkBaseRate() {
		return ValueFormatters.formatNumber(NumberFormatEnum.CurrencyWithoutFractionDecimalType, x4WkBaseRate);
	}

	public void setX4WkBaseRate(String x4WkBaseRate) {
		this.x4WkBaseRate = x4WkBaseRate;
	}

	public String getX4WkFloor() {
		return ValueFormatters.formatNumber(NumberFormatEnum.CurrencyWithoutFractionDecimalType, x4WkFloor);
	}

	public void setX4WkFloor(String x4WkFloor) {
		this.x4WkFloor = x4WkFloor;
	}

	public String getX4WkProposedPrice() {
		return ValueFormatters.formatNumber(NumberFormatEnum.CurrencyWithoutFractionDecimalType, x4WkProposedPrice);
	}

	public void setX4WkProposedPrice(String x4WkProposedPrice) {
		this.x4WkProposedPrice = x4WkProposedPrice;
	}

	public String getZip() {
		return zip;
	}

	public void setZip(String zip) {
		this.zip = zip;
	}

	public List<FlightLine> getChildFlightLines() {
		if(childFlightLines == null) {
			childFlightLines = new ArrayList<FlightLine>();
		}
		return childFlightLines;
	}

	public String getParentFlightLine() {
		return parentFlightLine;
	}

	public void setParentFlightLine(String parentFlightLine) {
		this.parentFlightLine = parentFlightLine;
	}
}
