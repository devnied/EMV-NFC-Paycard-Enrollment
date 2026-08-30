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

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.devnied.emvnfccard.iso7816emv.EmvTags;
import com.github.devnied.emvnfccard.iso7816emv.ITag;
import com.github.devnied.emvnfccard.model.CPLC;


/**
 * Card Production Life-Cycle Data (CPLC) as defined by the Global Platform Card
 * Specification (GPCS)
 * 
 */
public final class CPLCUtils {
	
	
	private static final Logger LOGGER = LoggerFactory.getLogger(CPLCUtils.class);
	
	/**
	 * CPLC TAG. The dictionary is the only place where a tag is named, so the
	 * constant of {@link EmvTags} is used rather than another instance holding
	 * another name for the very same bytes.
	 */
	private static final ITag CPLC_TAG = EmvTags.DS_UNPREDICTABLE_NUMBER;

	/**
	 * Method used to parse and extract CPLC data.
	 * <p>
	 * The 42 bytes of the value are kept in {@link CPLC#getRaw()}. A date
	 * field the card filled with an invalid day of year (Global Platform codes
	 * the dates as YDDD, with DDD at most 366) does not lose the fields decoded
	 * before it: the CPLC is returned partially filled, the fields after the
	 * failing one left null, and the error kept in
	 * {@link CPLC#getParseError()}.
	 * </p>
	 *
	 * @param raw
	 *            the 42 bytes of the value, that value followed by a status
	 *            word, or the whole data object (tag, length, value and status
	 *            word)
	 * @return CPLC data, or null when the data is not a CPLC
	 */
	public static CPLC parse(byte[] raw) {
		CPLC ret = null;
		if (raw != null) {
			byte[] cplc = null;
			// the value of the tag alone, as a TLV parser hands it over
			if (raw.length == CPLC.SIZE) {
				cplc = raw;
			}
			// try to interpret as raw data (not TLV)
			else if (raw.length == CPLC.SIZE + 2) {
				cplc = raw;
			}
			// or maybe it's prepended with CPLC tag:
			else if (raw.length == CPLC.SIZE + 5) {
				cplc = TlvUtil.getValue(raw, CPLC_TAG);
			} else {
				LOGGER.error("CPLC data not valid");
				return null;
			}
			// The length is right but the data may not hold the tag '9F7F', a
			// card is free to answer anything to a GET DATA
			if (cplc == null) {
				LOGGER.error("CPLC data does not hold the tag " + CPLC_TAG.getName());
				return null;
			}
			ret = new CPLC();
			// Keep the value as read, the SW of the raw form left out
			ret.setRaw(Arrays.copyOf(cplc, Math.min(cplc.length, CPLC.SIZE)));
			try {
				ret.parse(cplc, null);
			} catch (IllegalArgumentException e) {
				// A date of the CPLC does not hold a valid day of year: the
				// fields decoded so far are kept, the raw bytes hold the rest
				LOGGER.warn("CPLC data partially decoded: {}", e.getMessage());
				ret.setParseError(e.getMessage());
			}
		}
		return ret;
	}
	
	/**
	 * private constructor
	 */
	private CPLCUtils() {
	}
}