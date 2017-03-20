package com.github.devnied.emvnfccard.provider;

import org.apache.commons.lang3.StringUtils;
import org.fest.assertions.Assertions;

import com.github.devnied.emvnfccard.parser.IProvider;

import fr.devnied.bitlib.BytesUtils;

public class ProviderSelectPaymentEnvTest implements IProvider {

	/**
	 * Expected data
	 */
	private String expectedData = StringUtils.EMPTY;

	/**
	 * Returned data
	 */
	private String returnedData = StringUtils.EMPTY;

	@Override
	public byte[] transceive(final byte[] pCommand) {
		Assertions.assertThat(BytesUtils.bytesToStringNoSpace(pCommand)).isEqualTo(expectedData);
		return returnedData != null ? BytesUtils.fromString(returnedData) : null;
	}

	/**
	 * Setter for the field expectedData
	 * 
	 * @param expectedData
	 *            the expectedData to set
	 */
	public void setExpectedData(final String expectedData) {
		this.expectedData = expectedData;
	}

	/**
	 * Setter for the field returnedData
	 * 
	 * @param returnedData
	 *            the returnedData to set
	 */
	public void setReturnedData(final String returnedData) {
		this.returnedData = returnedData;
	}

}
