package com.gigassessment.security;

public final class HtmlEscaper {
	private HtmlEscaper() {
	}

	public static String escape(Object value) {
		if (value == null) {
			return "";
		}

		return value.toString()
				.replace("&", "&amp;")
				.replace("<", "&lt;")
				.replace(">", "&gt;")
				.replace("\"", "&quot;")
				.replace("'", "&#39;");
	}
}
