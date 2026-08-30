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

import com.github.devnied.emvnfccard.model.EmvTrack1;
import com.github.devnied.emvnfccard.model.EmvTrack2;
import com.github.devnied.emvnfccard.model.Service;
import fr.devnied.bitlib.BytesUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Extract track data
 *
 * @author MILLAU Julien
 *
 */
public final class TrackUtils {

	/**
	 * Class logger
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(TrackUtils.class);

	/**
	 * Card holder name separator
	 */
	public static final String CARD_HOLDER_NAME_SEPARATOR = "/";

	/**
	 * Track 2 Equivalent pattern
	 */
	private static final Pattern TRACK2_EQUIVALENT_PATTERN = Pattern.compile("([0-9]{1,19})D([0-9]{4})([0-9]{3})?(.*)");

	/**
	 * Track 1 pattern
	 */
	private static final Pattern TRACK1_PATTERN = Pattern
			.compile("%?([A-Z])([0-9]{1,19})(\\?[0-9])?\\^([^\\^]{2,26})\\^([0-9]{4}|\\^)([0-9]{3}|\\^)([^\\?]+)\\??");

	/**
	 * Service code pattern, the three digits field of ISO/IEC 7813. A card that
	 * leaves the field unused fills it with the separator character instead,
	 * which this pattern rejects
	 */
	private static final Pattern SERVICE_CODE_PATTERN = Pattern.compile("[0-9]{3}");

	/**
	 * Discretionary data pattern. EMV 4.4 Book 3 section 4.3 gives the sub-field
	 * the format cn, "two numeric digits (having values in the range Hex '0'-'9')
	 * per byte", so anything left outside that range is padding and not data.
	 */
	private static final Pattern DISCRETIONARY_DATA_PATTERN = Pattern.compile("[0-9]+");

	/**
	 * Number of positions taken by the service code on both tracks, whether the
	 * card filled it with digits or left it unused
	 */
	private static final int SERVICE_CODE_LENGTH = 3;

	/**
	 * Padding of a compressed numeric (cn) data element.<br>
	 * EMV 4.4 Book 3 section 4.3: "cn Compressed numeric data elements consist
	 * of two numeric digits (having values in the range Hex '0'-'9') per byte.
	 * These data elements are left justified and padded with trailing
	 * hexadecimal 'F's."
	 */
	private static final String COMPRESSED_NUMERIC_PADDING = "F";

	/**
	 * Characters a card pads the discretionary data of a track 1 with: the
	 * hexadecimal zero EMV 4.4 Book 3 Annex A2 mandates for a format ans data
	 * element ("A data element in format a, an, or ans is left justified and
	 * padded with trailing hexadecimal zeroes") and the space the tracks met in
	 * the field also use
	 */
	private static final String TRACK1_PADDING = "\0 ";

	/**
	 * Start sentinel of a track 1, ISO/IEC 7813 Table 1 ("SS Start sentinel
	 * %")
	 */
	private static final char TRACK1_START_SENTINEL = '%';

	/**
	 * End sentinel of a track 1, ISO/IEC 7813 Table 1 ("ES End sentinel ?")
	 */
	private static final char TRACK1_END_SENTINEL = '?';

	/**
	 * Extract track 2 Equivalent data
	 *
	 * @param pRawTrack2 Raw track 2 data
	 * @return EmvTrack2 object data or null
	 */
	public static EmvTrack2 extractTrack2EquivalentData(final byte[] pRawTrack2) {
		EmvTrack2 ret = null;

		if (pRawTrack2 != null) {
			EmvTrack2 track2 = new EmvTrack2();
			track2.setRaw(pRawTrack2);
			String data = BytesUtils.bytesToStringNoSpace(pRawTrack2);
			Matcher m = TRACK2_EQUIVALENT_PATTERN.matcher(data);
			// Check pattern
			if (m.find()) {
				// read card number
				track2.setCardNumber(m.group(1));
				// Keep the fields as the card wrote them, before any decoding:
				// the four expiration digits, then everything that follows
				// them (service code positions, discretionary data and the
				// 'F' padding of the data object, one nibble per character)
				track2.setExpirationField(m.group(2));
				String trailing = data.substring(m.end(2));
				track2.setTrailingField(StringUtils.defaultIfEmpty(trailing, null));
				// ISO/IEC 7813 Table 2 gives the service code three positions
				// whatever the card put in them, digits or 'F' nibbles
				track2.setServiceCodeField(StringUtils.defaultIfEmpty(
						trailing.substring(0, Math.min(SERVICE_CODE_LENGTH, trailing.length())), null));
				// Read expire date
				SimpleDateFormat sdf = new SimpleDateFormat("yyMM", Locale.US);
				// Without this a month of '99' rolls over into a plausible
				// looking date the card never expressed
				sdf.setLenient(false);
				try {
					track2.setExpireDate(lastDayOfMonth(sdf.parse(m.group(2))));
				} catch (ParseException e) {
					// The card number, the service code and the discretionary
					// data have already been read: an unreadable expiration
					// date leaves that field empty, it does not throw the
					// whole track away
					LOGGER.warn("Unparsable expire card date, the rest of the track is kept: {}", e.getMessage());
				}
				// Read service
				track2.setService(new Service(m.group(3)));
				track2.setServiceCode(getServiceCode(m.group(3)));
				// Read the discretionary data, which ISO/IEC 7813 puts right
				// after the service code and lets run to the end of the track
				track2.setDiscretionaryData(extractTrack2DiscretionaryData(data, m.end(2) + SERVICE_CODE_LENGTH));
				ret = track2;
			}
		}
		return ret;
	}

	/**
	 * Extract track 1 data
	 *
	 * @param pRawTrack1
	 *            track1 raw data
	 * @return EmvTrack1 object
	 */
	public static EmvTrack1 extractTrack1Data(final byte[] pRawTrack1) {
		EmvTrack1 ret = null;

		if (pRawTrack1 != null) {
			EmvTrack1 track1 = new EmvTrack1();
			track1.setRaw(pRawTrack1);
			String text = new String(pRawTrack1);
			Matcher m = TRACK1_PATTERN.matcher(text);
			// Check pattern
			if (m.find()) {
				// Sentinels of ISO/IEC 7813 Table 1: the start sentinel '%' is
				// the first character the pattern matched, the end sentinel
				// '?' the one closing the discretionary field. Both are
				// optional on a Track 1 Data (tag '56'), EMV Contactless Book
				// C-2 storing the track without them
				track1.setStartSentinel(text.charAt(m.start()) == TRACK1_START_SENTINEL);
				track1.setEndSentinel(m.end() > m.end(7) || text.endsWith(String.valueOf(TRACK1_END_SENTINEL)));
				// Fields as the card wrote them, kept before any decoding so
				// they survive whatever the decoding below rejects
				track1.setOptionalField(m.group(3));
				track1.setNameField(m.group(4));
				track1.setExpirationField(m.group(5));
				track1.setServiceCodeField(m.group(6));
				track1.setRawDiscretionaryField(m.group(7));
				// Set format code
				track1.setFormatCode(m.group(1));
				// Set card number
				track1.setCardNumber(m.group(2));
				// Extract holder name
				String[] name = StringUtils.split(m.group(4).trim(), CARD_HOLDER_NAME_SEPARATOR);
				if (name != null && name.length == 2) {
					track1.setHolderLastname(StringUtils.trimToNull(name[0]));
					track1.setHolderFirstname(StringUtils.trimToNull(name[1]));
				}
				// Read expire date
				SimpleDateFormat sdf = new SimpleDateFormat("yyMM", Locale.US);
				sdf.setLenient(false);
				try {
					track1.setExpireDate(lastDayOfMonth(sdf.parse(m.group(5))));
				} catch (ParseException e) {
					// Same here, the name and the card number already read are
					// worth more than the date that could not be
					LOGGER.warn("Unparsable expire card date, the rest of the track is kept: {}", e.getMessage());
				}
				// Read service
				track1.setService(new Service(m.group(6)));
				track1.setServiceCode(getServiceCode(m.group(6)));
				// Read the discretionary data, the balance of the characters
				// of the track
				track1.setDiscretionaryData(cleanTrack1DiscretionaryData(m.group(7)));
				ret = track1;
			}
		}
		return ret;
	}

	/**
	 * Method used to decode the Track 1 Discretionary Data (tag '9F1F'), the
	 * card resident data object holding the discretionary field of the first
	 * track on its own.
	 * <p>
	 * EMV 4.4 Book 3 Annex A1: "Track 1 Discretionary Data | Discretionary part
	 * of track 1 according to ISO/IEC 7813 | ICC | ans | '70' or '77' | '9F1F'
	 * | var.", EMV Contactless Book C-2 v2.6 A.1.176 bounding the length to
	 * "var. up to 54". The format is ans, one character per byte, so the value
	 * is text, and EMV 4.4 Book 3 Annex A2 pads it: "A data element in format
	 * a, an, or ans is left justified and padded with trailing hexadecimal
	 * zeroes".
	 * </p>
	 *
	 * @param pRawData
	 *            raw value of the tag '9F1F'
	 * @return the discretionary data of the track 1 or null
	 */
	public static String extractTrack1DiscretionaryData(final byte[] pRawData) {
		return cleanTrack1DiscretionaryData(EmvDataUtils.decodeText(pRawData, 0));
	}

	/**
	 * Method used to decode the Track 2 Discretionary Data (tag '9F20'), the
	 * card resident data object holding the discretionary field of the second
	 * track on its own.
	 * <p>
	 * EMV 4.4 Book 3 Annex A1: "Track 2 Discretionary Data | Discretionary part
	 * of track 2 according to ISO/IEC 7813 | ICC | cn | '70' or '77' | '9F20' |
	 * var.", EMV Contactless Book C-2 v2.6 A.1.178 bounding the length to "var.
	 * up to 16". The format is cn and not text: EMV 4.4 Book 3 section 4.3
	 * defines it as "two numeric digits (having values in the range Hex '0'-'9')
	 * per byte. These data elements are left justified and padded with trailing
	 * hexadecimal 'F's."
	 * </p>
	 *
	 * @param pRawData
	 *            raw value of the tag '9F20'
	 * @return the digits of the discretionary data of the track 2 or null
	 */
	public static String extractTrack2DiscretionaryData(final byte[] pRawData) {
		return EmvDataUtils.getNumericValue(pRawData);
	}

	/**
	 * Method used to read the discretionary data of a track 2 at the position
	 * ISO/IEC 7813 gives it, right after the service code, and to drop the
	 * padding of the data object.
	 * <p>
	 * EMV 4.4 Book 3 Annex A1 closes the sub-fields of the Track 2 Equivalent
	 * Data with "Discretionary Data (defined by individual payment systems) |
	 * n, var." then "Pad with one Hex 'F' if needed to ensure whole bytes | b".
	 * The position is read rather than the group of the pattern, so that a card
	 * leaving the service code unused does not shift the field.
	 * </p>
	 *
	 * @param pData
	 *            digits of the track, one per nibble
	 * @param pStart
	 *            first position of the discretionary data
	 * @return the digits of the discretionary data or null
	 */
	private static String extractTrack2DiscretionaryData(final String pData, final int pStart) {
		if (pStart >= pData.length()) {
			return null;
		}
		String value = StringUtils.trimToNull(StringUtils.stripEnd(pData.substring(pStart), COMPRESSED_NUMERIC_PADDING));
		// A card that leaves the service code unset fills those nibbles with the
		// 'F' padding, which the three digits pattern cannot read: the field then
		// starts somewhere inside the padding and what is left is not numeric.
		// Reporting it would hand out padding as if the card had stored data
		return value != null && DISCRETIONARY_DATA_PATTERN.matcher(value).matches() ? value : null;
	}

	/**
	 * Method used to drop the padding a card adds to the discretionary data of
	 * a track 1
	 *
	 * @param pValue
	 *            discretionary data read from the card
	 * @return the discretionary data without its padding or null
	 */
	private static String cleanTrack1DiscretionaryData(final String pValue) {
		return StringUtils.trimToNull(StringUtils.strip(pValue, TRACK1_PADDING));
	}

	/**
	 * Method used to keep the service code of a track only when the card filled
	 * it with the three digits ISO/IEC 7813 asks for
	 *
	 * @param pValue
	 *            content of the service code field of the track
	 * @return the service code or null
	 */
	private static String getServiceCode(final String pValue) {
		return pValue != null && SERVICE_CODE_PATTERN.matcher(pValue).matches() ? pValue : null;
	}

	/**
	 * Method used to truncate a date for the last day of the selected month.
	 * @param date the date to truncate
	 * @return the truncated date
	 */
	private static Date lastDayOfMonth(Date date){
		// A Gregorian calendar for the same reason as the date format above: the
		// default one of the device may be another (Buddhist in Thailand,
		// imperial in Japan) and would report another last day of month
		Calendar calendar = new GregorianCalendar();
		calendar.setTime(date);
		calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
		return DateUtils.truncate(calendar.getTime(), Calendar.DAY_OF_MONTH);
	}

	/**
	 * Private constructor
	 */
	private TrackUtils() {
	}

}
