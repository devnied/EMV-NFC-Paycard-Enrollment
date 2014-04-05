package com.github.devnied.emvnfccard.utils;

/**
 * Method used to manipulate response from APDU command
 * 
 * @author julien Millau
 * 
 */
public final class ResponseApdu {

	/**
	 * Method used to check if the last command return SW1SW2 == 9000
	 * 
	 * @param pByte
	 *            response to the last command
	 * @return true if the status is 9000 false otherwise
	 */
	public static boolean isSucceed(final byte[] pByte) {
		return pByte != null && pByte[pByte.length - 2] == (byte) 0x90 && pByte[pByte.length - 1] == 0;
	}

	/**
	 * Private constructor
	 */
	private ResponseApdu() {
	}

}
