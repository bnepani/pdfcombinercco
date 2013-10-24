package com.appirio.report;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.beanutils.BeanComparator;

public class Packages {

	/**
	 * flight line list
	 */
	private List<FlightLine> flightLines;

	/**
	 * Sort flight lines using sort key
	 */
	public void sortFlightLines() {
		BeanComparator beanComparator = new BeanComparator("sortKey");
		Collections.sort(getFlightLines(), beanComparator);
	}

	/**
	 * Process flight line. It simply adds flight line to the flight line list.
	 * @param flightLine
	 */
	public void processFlightLine(FlightLine flightLine) {

		// adds flight line to the flight line list
		getFlightLines().add(flightLine);
	}

	/**
	 * @return flight line list
	 */
	public List<FlightLine> getFlightLines() {

		if(flightLines == null) {
			setFlightLines(new ArrayList<FlightLine>());
		}

		return flightLines;
	}

	/**
	 * Set flight line list
	 * @param flightLines
	 */
	private void setFlightLines(List<FlightLine> flightLines) {
		this.flightLines = flightLines;
	}
}
