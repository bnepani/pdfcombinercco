package com.appirio.report;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

public class Reporter {

	// unique identifier for generated files
	private String uniqueId = null;

	// generated pdf directory (generated at runtime)
	private String generatedReportDir = null;

	/**
	 * Return combined Report file name
	 * @throws IOException 
	 */
	public String getGeneratedReport(String extension) throws IOException {
		return this.getGeneratedReportDir() + File.separator + this.getUniqueId() + "." + extension;
	}

	/**
	 * @param fieldNames
	 * @param fieldLabels
	 * @return
	 */
	protected HashMap<String, String> getKeyValueHashMap(String fieldNames, String fieldLabels) {

		List<String> fieldNamesList = Arrays.asList(fieldNames.split("\\|"));
		List<String> fieldLabelsList = Arrays.asList(fieldLabels.split("\\|"));
		HashMap<String, String> keyValueHashMap = new LinkedHashMap<String, String>();

		for (int i = 0; i < fieldNamesList.size(); i++) {
			keyValueHashMap.put(fieldNamesList.get(i), fieldLabelsList.get(i)); 
			// is there a clearer way?
		}
		return keyValueHashMap;
	}

	/**
	 * Returns the path to generated report dir defined in environment variables
	 * @throws IOException 
	 */
	private String getGeneratedReportDir() throws IOException {
		if (generatedReportDir == null) {
			generatedReportDir = System.getenv("GENERATED_PDFS_DIR") + File.separator + this.getUniqueId();

			if (!new File(generatedReportDir).mkdirs()) {
				throw new IOException("Could not create working directory " + generatedReportDir);
			}
		}
		System.out.println("********** generatedPDFDir  " + generatedReportDir);
		return generatedReportDir;
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
 }
