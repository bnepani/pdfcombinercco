package com.appirio.report;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;

public class MapPanelOrderPreference {
	private String sortSequence;
	private String flight;
	private String panel;

	public MapPanelOrderPreference() {
		//System.out.println("MapPanelOrderPreference()");
	}

	public String getSortSequence() {

		Integer returnValue = 0;

		//System.out.println("   getSortSequence() sortSequence: " + sortSequence);

		try {
			if(this.sortSequence != null) {
				// Use NumberFormat to format and parse numbers for the US locale
				NumberFormat nf = NumberFormat.getNumberInstance(Locale.US); // Get a NumberFormat instance for US locale

				// set return value to be as int value
				returnValue = nf.parse(this.sortSequence).intValue();
			}
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} // Parse strings according to locale

		return returnValue.toString();
	}

	public void setSortSequence(String sortSequence) {
		//System.out.println("   setSortSequence() sortSequence: " + sortSequence);
		this.sortSequence = sortSequence;
	}

	public String getFlight() {
		return flight;
	}

	public void setFlight(String flight) {
		this.flight = flight;
	}

	public String getPanel() {
		return panel;
	}

	public void setPanel(String panel) {
		this.panel = panel;
	}
}
