package com.appirio.report;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ValueFormatters {

	public enum NumberFormatEnum {
		CurrencyWithoutFractionDecimalType, CurrencyWithFractionDecimalType, NumberWithoutFractionDecimalType, NumberWithFractionDecimalType, NumberWithOneDecimalType
	}

	public static String formatDate(String value) {

		SimpleDateFormat inputDateFormat = new SimpleDateFormat("yyyy-MM-dd");
		SimpleDateFormat outputDateFormat = new SimpleDateFormat("MM/dd/yyyy");
		String formattedDate = null;

		Date dateDate = null;
		try {
			dateDate = inputDateFormat.parse(value);

			formattedDate = outputDateFormat.format(dateDate);

		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return formattedDate;
	}

	public static String formatNumber(NumberFormatEnum numberFormat, String value) {

		String returnValue = null;

		DecimalFormat decimalFormat = null;

		switch(numberFormat) {
		case NumberWithoutFractionDecimalType:
			decimalFormat = new DecimalFormat("#,###,###,##0");
			break;
		case NumberWithFractionDecimalType:
			decimalFormat = new DecimalFormat("#,###,###,##0.00");
			break;
		case NumberWithOneDecimalType:
			decimalFormat = new DecimalFormat("#,###,###,##0.0");
			break;
		case CurrencyWithoutFractionDecimalType:
			decimalFormat = new DecimalFormat("$#,###,###,##0");
			break;
		case CurrencyWithFractionDecimalType:
			decimalFormat = new DecimalFormat("$#,###,###,##0.00");
			break;
		}

		// Use NumberFormat to format and parse numbers for the US locale
		NumberFormat nf = NumberFormat.getNumberInstance(Locale.US); // Get a NumberFormat instance for US locale

		// set return value to be as formatted double value
		try {
			double doubleValue = nf.parse(value).doubleValue();

			returnValue = decimalFormat.format(doubleValue);

		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return returnValue;
	}
}
