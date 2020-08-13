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
				// Read expire date
				SimpleDateFormat sdf = new SimpleDateFormat("yyMM", Locale.getDefault());
				try {
					track2.setExpireDate(lastDayOfMonth(sdf.parse(m.group(2))));
				} catch (ParseException e) {
					LOGGER.error("Unparsable expire card date : {}", e.getMessage());
					return ret;
				}
				// Read service
				track2.setService(new Service(m.group(3)));
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
			Matcher m = TRACK1_PATTERN.matcher(new String(pRawTrack1));
			// Check pattern
			if (m.find()) {
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
				SimpleDateFormat sdf = new SimpleDateFormat("yyMM", Locale.getDefault());
				try {
					track1.setExpireDate(lastDayOfMonth(sdf.parse(m.group(5))));
				} catch (ParseException e) {
					LOGGER.error("Unparsable expire card date : {}", e.getMessage());
					return ret;
				}
				// Read service
				track1.setService(new Service(m.group(6)));
				ret = track1;
			}
		}
		return ret;
	}

	/**
	 * Method used to truncate a date for the last day of the selected month.
	 * @param date the date to truncate
	 * @return the truncated date
	 */
	private static Date lastDayOfMonth(Date date){
		Calendar calendar = Calendar.getInstance();
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
