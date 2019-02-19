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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.devnied.emvnfccard.enums.TagValueTypeEnum;
import com.github.devnied.emvnfccard.iso7816emv.ITag;
import com.github.devnied.emvnfccard.iso7816emv.impl.TagImpl;
import com.github.devnied.emvnfccard.model.CPLC;


/**
 * Card Production Life-Cycle Data (CPLC) as defined by the Global Platform Card
 * Specification (GPCS)
 * 
 */
public final class CPLCUtils {
	
	
	private static final Logger LOGGER = LoggerFactory.getLogger(CPLCUtils.class);
	
	/**
	 * CPLC TAG
	 */
	private static final ITag CPLC_TAG = new TagImpl("9f7f", TagValueTypeEnum.BINARY, "Card Production Life Cycle Data", "");

	/**
	 * Method used to parse and extract CPLC data
	 * @param raw raw data
	 * @return CPLC data 
	 */
	public static CPLC parse(byte[] raw) {
		CPLC ret = null;
		if (raw != null) {
			byte[] cplc = null;
			// try to interpret as raw data (not TLV)
			if (raw.length == CPLC.SIZE + 2) {
				cplc = raw;
			}
			// or maybe it's prepended with CPLC tag:
			else if (raw.length == CPLC.SIZE + 5) {
				cplc = TlvUtil.getValue(raw, CPLC_TAG);
			} else {
				LOGGER.error("CPLC data not valid");
				return null;
			}
			ret = new CPLC();
			ret.parse(cplc,null);
		}
		return ret;
	}
	
	/**
	 * private constructor
	 */
	private CPLCUtils() {
	}
}