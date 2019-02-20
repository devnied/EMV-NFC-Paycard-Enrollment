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
package com.github.devnied.emvnfccard.parser.apdu.impl;

import com.github.devnied.emvnfccard.model.enums.IKeyEnum;
import com.github.devnied.emvnfccard.parser.apdu.annotation.AnnotationData;
import com.github.devnied.emvnfccard.utils.EnumUtils;
import fr.devnied.bitlib.BitUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Calendar;
import java.util.Date;

/**
 * Factory to parse data
 *
 * @author MILLAU Julien
 */
public final class DataFactory {

	/**
	 * Logger of this class
	 */
	public static final Logger LOGGER = LoggerFactory.getLogger(DataFactory.class.getName());

	/**
	 * Constant for EN1545-1 (Date format)
	 */
	public static final int BCD_DATE = 1;

	public static final int CPCL_DATE = 2;

	/**
	 * Half byte size
	 */
	public static final int HALF_BYTE_SIZE = 4;

	/**
	 * BCD format
	 */
	public static final String BCD_FORMAT = "BCD_Format";

	/**
	 * Method to get a date from the bytes array
	 *
	 * @param pAnnotation
	 *            annotation data
	 * @param pBit
	 *            table bytes
	 * @return The date read of null
	 */
	private static Date getDate(final AnnotationData pAnnotation, final BitUtils pBit) {
		Date date = null;
		if (pAnnotation.getDateStandard() == BCD_DATE) {
			date = pBit.getNextDate(pAnnotation.getSize(), pAnnotation.getFormat(), true);
		} else if (pAnnotation.getDateStandard() == CPCL_DATE) {
			date = calculateCplcDate(pBit.getNextByte(pAnnotation.getSize()));
		} else {
			date = pBit.getNextDate(pAnnotation.getSize(), pAnnotation.getFormat());
		}
		return date;
	}


	/**
	 * Takes a date value as used in CPLC Date fields (represented by 2 bytes)
	 *
	 * @param dateBytes raw bytes
	 * @throws IllegalArgumentException when the data size is wrong
	 * @return the date
	 */
	public static Date calculateCplcDate(byte[] dateBytes)
			throws IllegalArgumentException {
		if (dateBytes == null || dateBytes.length != 2) {
			throw new IllegalArgumentException(
					"Error! CLCP Date values consist always of exactly 2 bytes");
		}
		// Check empty date
		if (dateBytes[0] == 0 && dateBytes[1] == 0){
			return null;
		}
		// current time
		Calendar now = Calendar.getInstance();

		int currenctYear = now.get(Calendar.YEAR);
		int startYearOfCurrentDecade = currenctYear - (currenctYear % 10);

		int days = 100 * (dateBytes[0] & 0xF) + 10 * (0xF & dateBytes[1] >>> 4) + (dateBytes[1] & 0xF);

		if (days > 366) {
			throw new IllegalArgumentException(
					"Invalid date (or are we parsing it wrong??)");
		}

		Calendar calculatedDate = Calendar.getInstance();
		calculatedDate.clear();
		int year = startYearOfCurrentDecade + (0xF & dateBytes[0] >>> 4);
		calculatedDate.set(Calendar.YEAR, year);
		calculatedDate.set(Calendar.DAY_OF_YEAR, days);
		while (calculatedDate.after(now)) {
			year = year - 10;
			calculatedDate.clear();
			calculatedDate.set(Calendar.YEAR, year);
			calculatedDate.set(Calendar.DAY_OF_YEAR, days);
		}
		return calculatedDate.getTime();
	}

	/**
	 * This method is used to get an integer
	 *
	 * @param pAnnotation
	 *            annotation
	 * @param pBit
	 *            bit array
	 * @return the integer value
	 */
	private static int getInteger(final AnnotationData pAnnotation, final BitUtils pBit) {
		return pBit.getNextInteger(pAnnotation.getSize());
	}

	/**
	 * Method to read and object from the bytes tab
	 *
	 * @param pAnnotation
	 *            all information data
	 * @param pBit
	 *            bytes tab
	 * @return an object
	 */
	public static Object getObject(final AnnotationData pAnnotation, final BitUtils pBit) {
		Object obj = null;
		Class<?> clazz = pAnnotation.getField().getType();

		if (clazz.equals(Integer.class)) {
			obj = getInteger(pAnnotation, pBit);
		} else if (clazz.equals(Float.class)) {
			obj = getFloat(pAnnotation, pBit);
		} else if (clazz.equals(String.class)) {
			obj = getString(pAnnotation, pBit);
		} else if (clazz.equals(Date.class)) {
			obj = getDate(pAnnotation, pBit);
		} else if (clazz.isEnum()) {
			obj = getEnum(pAnnotation, pBit);
		}
		return obj;
	}

	/**
	 * Method use to get float
	 *
	 * @param pAnnotation
	 *            annotation
	 * @param pBit
	 *            bit utils
	 * @return
	 */
	private static Float getFloat(final AnnotationData pAnnotation, final BitUtils pBit) {
		Float ret = null;

		if (BCD_FORMAT.equals(pAnnotation.getFormat())) {
			ret = Float.parseFloat(pBit.getNextHexaString(pAnnotation.getSize()));
		} else {
			ret = (float) getInteger(pAnnotation, pBit);
		}

		return ret;
	}

	/**
	 * This method is used to get an enum with his key
	 *
	 * @param pAnnotation
	 *            annotation
	 * @param pBit
	 *            bit array
	 */
	@SuppressWarnings("unchecked")
	private static IKeyEnum getEnum(final AnnotationData pAnnotation, final BitUtils pBit) {
		int val = 0;
		try {
			val = Integer.parseInt(pBit.getNextHexaString(pAnnotation.getSize()), pAnnotation.isReadHexa() ? 16 : 10);
		} catch (NumberFormatException nfe) {
			// do nothing
		}
		return EnumUtils.getValue(val, (Class<? extends IKeyEnum>) pAnnotation.getField().getType());
	}

	/**
	 * This method get a string (Hexa or ASCII) from a bit table
	 *
	 * @param pAnnotation
	 *            annotation data
	 * @param pBit
	 *            bit table
	 * @return A string
	 */
	private static String getString(final AnnotationData pAnnotation, final BitUtils pBit) {
		String obj = null;

		if (pAnnotation.isReadHexa()) {
			obj = pBit.getNextHexaString(pAnnotation.getSize());
		} else {
			obj = pBit.getNextString(pAnnotation.getSize()).trim();
		}

		return obj;
	}

	/**
	 * Private constructor
	 */
	private DataFactory() {
	}
}
