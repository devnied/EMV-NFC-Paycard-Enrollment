package com.pro100svitlo.creditCardNfcReader.utils;

import com.pro100svitlo.creditCardNfcReader.enums.SwEnum;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

import fr.devnied.bitlib.BytesUtils;


/**
 * Method used to manipulate response from APDU command
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
			byte[] ooo = Arrays.copyOfRange(pByte, pByte.length - 2, pByte.length);
			String q = BytesUtils.bytesToStringNoSpace(ooo);
					LOGGER.debug("Response Status <"
							+ q + "> : "
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
