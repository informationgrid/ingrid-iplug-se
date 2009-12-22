package de.ingrid.admin;

import java.text.DecimalFormat;

public class StringUtils {

	private static final DecimalFormat FORMAT = new DecimalFormat(
			"0000000000.####");

	public static boolean isEmpty(final String s) {
		if (null == s || s.length() <= 0) {
			return true;
		}
		return false;
	}

	public static boolean isEmptyOrWhiteSpace(final String s) {
		if (!isEmpty(s)) {
			if (s.trim().length() > 0) {
				return false;
			}
		}
		return true;
	}

	/**
	 * This method fills a number with leading zeros. So we get a number with
	 * ten digits before the point a and four digits past the point
	 * 
	 * @param number
	 *            The number to pad.
	 * @return The padded number as a string.
	 */
	public static String padding(double number) {
		return FORMAT.format(number);
	}
}
