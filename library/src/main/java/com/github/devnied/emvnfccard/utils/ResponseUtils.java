package com.github.devnied.emvnfccard.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.devnied.emvnfccard.enums.SwEnum;

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
		SwEnum val = SwEnum.getSW(pByte);
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Response Status : " + (val != null ? val.getDetail() : "Unknow"));
		}
		return val != null && val == SwEnum.SW_9000;
	}

	/**
	 * Private constructor
	 */
	private ResponseUtils() {
	}

}
