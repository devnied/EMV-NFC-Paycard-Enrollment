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

import org.apache.commons.lang3.ArrayUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.github.devnied.emvnfccard.model.CVM;
import com.github.devnied.emvnfccard.model.enums.CvmConditionEnum;
import com.github.devnied.emvnfccard.model.enums.CvmMethodEnum;

/**
 * Utility class used to decode the Cardholder Verification Method List (tag
 * '8E').
 *
 * <pre>
 * bytes 1-4  : Amount field X
 * bytes 5-8  : Amount field Y
 * bytes 9..n : a list of 2 bytes Cardholder Verification Rules
 * </pre>
 *
 * @author MILLAU Julien
 *
 */
public final class CvmUtils {

	/**
	 * Size of the amount X and Y header (in bytes)
	 */
	private static final int CVM_LIST_HEADER_SIZE = 8;

	/**
	 * Bit mask used to extract the CVM code from the first rule byte
	 */
	private static final int CVM_CODE_MASK = 0x3F;

	/**
	 * Bit set when the terminal has to apply the succeeding rule if the current
	 * CVM is unsuccessful
	 */
	private static final int APPLY_NEXT_RULE_MASK = 0x40;

	/**
	 * Lower bound of the range reserved for the payment systems
	 */
	private static final int PAYMENT_SYSTEM_RANGE_START = 0x20;

	/**
	 * Lower bound of the range reserved for the issuer
	 */
	private static final int ISSUER_RANGE_START = 0x30;

	/**
	 * Method used to extract the list of Cardholder Verification Rules from a
	 * CVM list (tag '8E').
	 *
	 * @param pCvmList
	 *            raw value of the tag '8E'
	 * @return an immutable list of CV Rules (never null)
	 */
	public static List<CVM> parseCvmList(final byte[] pCvmList) {
		List<CVM> ret = new ArrayList<CVM>();
		if (pCvmList != null && pCvmList.length > CVM_LIST_HEADER_SIZE) {
			for (int i = CVM_LIST_HEADER_SIZE; i + 1 < pCvmList.length; i += 2) {
				ret.add(parseRule(pCvmList[i], pCvmList[i + 1]));
			}
		}
		return Collections.unmodifiableList(ret);
	}

	/**
	 * Method used to extract the amount X of a CVM list (tag '8E'). <br>
	 * The amount is expressed in the minor unit of the application currency.
	 *
	 * @param pCvmList
	 *            raw value of the tag '8E'
	 * @return the amount X or {@link com.github.devnied.emvnfccard.model.AbstractData#UNKNOWN} if it cannot be read
	 */
	public static long getAmountX(final byte[] pCvmList) {
		return readAmount(pCvmList, 0);
	}

	/**
	 * Method used to extract the amount Y of a CVM list (tag '8E'). <br>
	 * The amount is expressed in the minor unit of the application currency.
	 *
	 * @param pCvmList
	 *            raw value of the tag '8E'
	 * @return the amount Y or {@link com.github.devnied.emvnfccard.model.AbstractData#UNKNOWN} if it cannot be read
	 */
	public static long getAmountY(final byte[] pCvmList) {
		return readAmount(pCvmList, 4);
	}

	/**
	 * Read a 4 bytes big endian amount at the given offset
	 *
	 * @param pCvmList
	 *            raw value of the tag '8E'
	 * @param pOffset
	 *            offset of the amount
	 * @return the amount or -1 if it cannot be read
	 */
	private static long readAmount(final byte[] pCvmList, final int pOffset) {
		if (pCvmList == null || pCvmList.length < pOffset + 4) {
			return -1;
		}
		return EmvDataUtils.getBinaryValue(ArrayUtils.subarray(pCvmList, pOffset, pOffset + 4), -1);
	}

	/**
	 * Method used to decode a single 2 bytes Cardholder Verification Rule
	 *
	 * @param pMethod
	 *            first byte of the rule
	 * @param pCondition
	 *            second byte of the rule
	 * @return the decoded rule
	 */
	public static CVM parseRule(final byte pMethod, final byte pCondition) {
		CVM ret = new CVM();
		int method = pMethod & CVM_CODE_MASK;
		int condition = pCondition & 0xFF;
		ret.setRawMethod(method);
		ret.setRawCondition(condition);
		ret.setApplyNextIfUnsuccessful((pMethod & APPLY_NEXT_RULE_MASK) != 0);
		ret.setMethod(findMethod(method));
		ret.setCondition(EnumUtils.find(condition, CvmConditionEnum.class));
		return ret;
	}

	/**
	 * Method used to find the CVM method matching the parameter code.<br>
	 * Codes in the ranges reserved for the payment systems and for the issuer
	 * are mapped to their generic value.
	 *
	 * @param pCode
	 *            CVM code (6 bits)
	 * @return the matching method or null
	 */
	private static CvmMethodEnum findMethod(final int pCode) {
		CvmMethodEnum method = EnumUtils.find(pCode, CvmMethodEnum.class);
		if (method != null) {
			return method;
		}
		if (pCode >= ISSUER_RANGE_START) {
			return CvmMethodEnum.ISSUER_SPECIFIC;
		}
		if (pCode >= PAYMENT_SYSTEM_RANGE_START) {
			return CvmMethodEnum.PAYMENT_SYSTEM_SPECIFIC;
		}
		return null;
	}

	/**
	 * Private constructor
	 */
	private CvmUtils() {
	}

}
