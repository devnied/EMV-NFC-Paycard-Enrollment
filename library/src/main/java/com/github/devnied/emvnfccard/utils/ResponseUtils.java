/*
 * Copyright (C) 2013 MILLAU Julien
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

import com.github.devnied.emvnfccard.enums.SwEnum;

import fr.devnied.bitlib.BytesUtils;

/**
 * Method used to manipulate response from APDU command
 * 
 * @author MILLAU Julien
 * 
 */
public final class ResponseUtils {

	/**
	 * Class logger
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(ResponseUtils.class);

	/**
	 * Method used to check if the last command return SW1SW2 == 9000
	 * 
	 * @param pByte
	 *            response to the last command
	 * @return true if the status is 9000 false otherwise
	 */
	public static boolean isSucceed(final byte[] pByte) {
		return isEquals(pByte, SwEnum.SW_9000);
	}

	/**
	 * Method used to check equality with the last command return SW1SW2 == pEnum
	 * 
	 * @param pByte
	 *            response to the last command
	 * @param pEnum
	 *            response to check
	 * @return true if the response of the last command is equals to pEnum
	 */
	public static boolean isEquals(final byte[] pByte, final SwEnum pEnum) {
		SwEnum val = SwEnum.getSW(pByte);
		if (LOGGER.isDebugEnabled() && pByte != null) {
			LOGGER.debug("Response Status <"
					+ BytesUtils.bytesToStringNoSpace(Arrays.copyOfRange(pByte, pByte.length - 2, pByte.length)) + "> : "
					+ (val != null ? val.getDetail() : "Unknow"));
		}
		return val != null && val == pEnum;
	}

	/**
	 * Private constructor
	 */
	private ResponseUtils() {
	}

}
