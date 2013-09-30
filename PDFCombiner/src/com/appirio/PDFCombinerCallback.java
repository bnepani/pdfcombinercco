package com.appirio;

import java.io.Serializable;

/**
 * @author jesus
 *
 * This class receives a callback url and parameters to send back to the server
 * when the pdf generation is complete.
 */
public class PDFCombinerCallback implements Serializable {
	private static final long serialVersionUID = 343452324L;
	private String callbackUrl;
	private String callbackContents;
	public PDFCombinerCallback() {
		super();
	}
	public String getCallbackUrl() {
		return callbackUrl;
	}
	public void setCallbackUrl(String callbackUrl) {
		this.callbackUrl = callbackUrl;
	}
	public String getCallbackContents() {
		return callbackContents;
	}
	public void setCallbackContents(String callbackContents) {
		this.callbackContents = callbackContents;
	}
}
