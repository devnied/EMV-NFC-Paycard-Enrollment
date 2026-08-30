/*
 * Copyright (C) 2019 MILLAU Julien
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.devnied.emvnfccard.utils;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.devnied.bitlib.BytesUtils;

/**
 * Utility class used to decode the value of EMV data objects: text (taking the
 * Issuer Code Table Index into account), packed BCD numbers and dates.
 *
 * @author MILLAU Julien
 *
 */
public final class EmvDataUtils {

	/**
	 * Class logger
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(EmvDataUtils.class);

	/**
	 * Default character set used when the card does not provide an Issuer Code
	 * Table Index (tag '9F11'). The EMV specification mandates the common
	 * character set (a subset of ISO/IEC 8859-1).
	 */
	public static final String DEFAULT_CHARSET = "ISO-8859-1";

	/**
	 * Highest ISO/IEC 8859 part number
	 */
	private static final int MAX_CODE_TABLE_INDEX = 16;

	/**
	 * Method used to get the charset name matching an Issuer Code Table Index
	 * (tag '9F11'). <br>
	 * The index is the part number of the ISO/IEC 8859 standard, so a card
	 * issued in Russia usually returns 5 (ISO/IEC 8859-5, Cyrillic), a card
	 * issued in Greece 7 (Greek) and a card issued in Turkey 9 (Latin-5).
	 *
	 * @param pIssuerCodeTableIndex
	 *            raw value of the tag '9F11' (may be null)
	 * @return the charset name to use to decode the Application Preferred Name
	 */
	public static String getCharsetName(final byte[] pIssuerCodeTableIndex) {
		if (pIssuerCodeTableIndex == null || pIssuerCodeTableIndex.length == 0) {
			return DEFAULT_CHARSET;
		}
		return getCharsetName(readCodeTableIndex(pIssuerCodeTableIndex));
	}

	/**
	 * Method used to read an Issuer Code Table Index. The tag '9F11' is coded
	 * n 2, so a card using the part 10 of the standard sends '10', which is ten
	 * and not sixteen.
	 *
	 * @param pIssuerCodeTableIndex
	 *            raw value of the tag '9F11'
	 * @return the index, or 0 when the value is not two decimal digits
	 */
	private static int readCodeTableIndex(final byte[] pIssuerCodeTableIndex) {
		if (pIssuerCodeTableIndex == null || pIssuerCodeTableIndex.length == 0) {
			return 0;
		}
		int high = (pIssuerCodeTableIndex[0] & 0xF0) >> 4;
		int low = pIssuerCodeTableIndex[0] & 0x0F;
		return high > 9 || low > 9 ? 0 : high * 10 + low;
	}

	/**
	 * Method used to get the charset name matching an Issuer Code Table Index
	 *
	 * @param pIssuerCodeTableIndex
	 *            value of the tag '9F11'
	 * @return the charset name to use to decode the Application Preferred Name
	 */
	public static String getCharsetName(final int pIssuerCodeTableIndex) {
		if (pIssuerCodeTableIndex < 1 || pIssuerCodeTableIndex > MAX_CODE_TABLE_INDEX) {
			return DEFAULT_CHARSET;
		}
		String charset = "ISO-8859-" + pIssuerCodeTableIndex;
		try {
			// Not all JVM (mainly Android) provide every part of ISO/IEC 8859
			new String(new byte[0], charset);
		} catch (UnsupportedEncodingException e) {
			LOGGER.warn("Unsupported charset {}, fallback to {}", charset, DEFAULT_CHARSET);
			charset = DEFAULT_CHARSET;
		}
		return charset;
	}

	/**
	 * Method used to decode a text data object using the card Issuer Code Table
	 * Index.
	 *
	 * @param pValue
	 *            raw value of the data object
	 * @param pIssuerCodeTableIndex
	 *            raw value of the tag '9F11' (may be null)
	 * @return the decoded and trimmed text or null
	 */
	public static String decodeText(final byte[] pValue, final byte[] pIssuerCodeTableIndex) {
		return decodeText(pValue, readCodeTableIndex(pIssuerCodeTableIndex));
	}

	/**
	 * Method used to decode a text data object using the card Issuer Code Table
	 * Index.
	 *
	 * @param pValue
	 *            raw value of the data object
	 * @param pIssuerCodeTableIndex
	 *            value of the tag '9F11', any value out of the ISO/IEC 8859
	 *            range falls back on the EMV common character set
	 * @return the decoded and trimmed text or null
	 */
	public static String decodeText(final byte[] pValue, final int pIssuerCodeTableIndex) {
		if (pValue == null || pValue.length == 0) {
			return null;
		}
		String charset = getCharsetName(pIssuerCodeTableIndex);
		String ret;
		try {
			ret = new String(pValue, charset);
		} catch (UnsupportedEncodingException e) {
			LOGGER.warn("Unable to decode text with charset {}", charset);
			ret = new String(pValue);
		}
		return StringUtils.trimToNull(removeControlChars(ret));
	}

	/**
	 * Remove the control characters a card may use to pad a text data object
	 *
	 * @param pValue
	 *            text to clean
	 * @return the cleaned text
	 */
	private static String removeControlChars(final String pValue) {
		StringBuilder builder = new StringBuilder(pValue.length());
		for (int i = 0; i < pValue.length(); i++) {
			char c = pValue.charAt(i);
			if (c >= 0x20 && c != 0x7F) {
				builder.append(c);
			}
		}
		return builder.toString();
	}

	/**
	 * Method used to convert a packed BCD data object (n format) to its decimal
	 * string representation.
	 *
	 * @param pValue
	 *            raw value of the data object
	 * @return the decimal representation without any padding or null
	 */
	public static String getNumericValue(final byte[] pValue) {
		if (pValue == null || pValue.length == 0) {
			return null;
		}
		// Trailing 'F' nibbles are used as padding (ex: PAN in tag '5A')
		String value = StringUtils.trimToNull(StringUtils.stripEnd(
				BytesUtils.bytesToStringNoSpace(pValue).toUpperCase(Locale.ENGLISH), "F"));
		if (value == null) {
			return null;
		}
		// Every nibble of an n or cn data object is a decimal digit (EMV 4.3
		// Book 3 section 4.3). Handing back the hexadecimal as if it were the
		// number would turn a corrupted field into a plausible looking card
		// number: reading nothing is recoverable, reading a wrong PAN is not
		for (int i = 0; i < value.length(); i++) {
			char c = value.charAt(i);
			if (c < '0' || c > '9') {
				LOGGER.warn("Not a packed BCD value: {}", value);
				return null;
			}
		}
		return value;
	}

	/**
	 * Method used to convert a packed BCD data object (n format) to an int.
	 *
	 * @param pValue
	 *            raw value of the data object
	 * @param pDefault
	 *            value returned when the data object cannot be decoded
	 * @return the int value of the data object
	 */
	public static int getNumericIntValue(final byte[] pValue, final int pDefault) {
		String value = getNumericValue(pValue);
		if (value == null) {
			return pDefault;
		}
		try {
			return Integer.parseInt(value);
		} catch (NumberFormatException e) {
			LOGGER.warn("Unable to read numeric value: {}", value);
			return pDefault;
		}
	}

	/**
	 * Method used to convert a binary data object (b format) to a long.
	 *
	 * @param pValue
	 *            raw value of the data object
	 * @param pDefault
	 *            value returned when the data object is null or too long
	 * @return the long value of the data object
	 */
	public static long getBinaryValue(final byte[] pValue, final long pDefault) {
		if (pValue == null || pValue.length == 0 || pValue.length > 8) {
			return pDefault;
		}
		long ret = 0;
		for (byte element : pValue) {
			ret = ret << 8 | element & 0xFF;
		}
		return ret;
	}

	/**
	 * Method used to decode an EMV date (format YYMMDD, packed BCD) as found in
	 * the tags '5F24' (Application Expiration Date) and '5F25' (Application
	 * Effective Date).
	 *
	 * @param pValue
	 *            raw value of the data object
	 * @return the decoded date or null if the value is not a valid date
	 */
	public static Date parseDate(final byte[] pValue) {
		String value = pValue != null ? BytesUtils.bytesToStringNoSpace(pValue) : null;
		if (value == null || value.length() < 6) {
			return null;
		}
		int year;
		int month;
		int day;
		try {
			year = Integer.parseInt(value.substring(0, 2));
			month = Integer.parseInt(value.substring(2, 4));
			day = Integer.parseInt(value.substring(4, 6));
		} catch (NumberFormatException e) {
			LOGGER.warn("Unparsable EMV date: {}", value);
			return null;
		}
		if (month < 1 || month > 12) {
			LOGGER.warn("Invalid month in EMV date: {}", value);
			return null;
		}
		// An EMV date is a Gregorian one: the default calendar of the device
		// may be another one (Buddhist in Thailand, imperial in Japan) and
		// would shift the year by centuries
		Calendar calendar = new GregorianCalendar();
		calendar.clear();
		// EMV dates use a 2 digits year, the sliding window is [1950, 2049]
		calendar.set(Calendar.YEAR, year >= 50 ? 1900 + year : 2000 + year);
		calendar.set(Calendar.MONTH, month - 1);
		if (day == 0) {
			// A lot of cards pad the day with '00'. The last day of the month
			// is the reading EMV 4.3 Book 3 Annex A gives an expiration date,
			// and the one the rest of the library already relies on
			day = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
		} else if (day > calendar.getActualMaximum(Calendar.DAY_OF_MONTH)) {
			// A day the month does not have is not padding, it is a date the
			// card did not encode: rounding it would invent one
			LOGGER.warn("Invalid day in EMV date: {}", value);
			return null;
		}
		calendar.set(Calendar.DAY_OF_MONTH, day);
		return calendar.getTime();
	}

	/**
	 * Method used to decode the Language Preference (tag '5F2D'): 1 to 4
	 * languages stored in order of preference, each represented by 2
	 * alphabetical characters according to ISO 639.
	 *
	 * @param pValue
	 *            raw value of the tag '5F2D'
	 * @return the list of lower case ISO 639 language codes (never null)
	 */
	public static List<String> parseLanguagePreference(final byte[] pValue) {
		List<String> ret = new ArrayList<String>();
		if (pValue != null) {
			String value = new String(pValue).toLowerCase(Locale.ENGLISH);
			for (int i = 0; i + 1 < value.length(); i += 2) {
				String language = value.substring(i, i + 2);
				// Unused entries are padded with hexadecimal zeroes or spaces
				if (StringUtils.isAlpha(language) && !ret.contains(language)) {
					ret.add(language);
				}
			}
		}
		return ret;
	}

	/**
	 * Private constructor
	 */
	private EmvDataUtils() {
	}

}
