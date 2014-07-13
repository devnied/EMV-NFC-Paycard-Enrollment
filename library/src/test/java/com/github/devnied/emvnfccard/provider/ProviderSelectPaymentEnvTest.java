package com.github.devnied.emvnfccard.provider;

import org.fest.assertions.Assertions;

import com.github.devnied.emvnfccard.parser.IProvider;

import fr.devnied.bitlib.BytesUtils;

public class ProviderSelectPaymentEnvTest implements IProvider {

	private String expectedData;

	@Override
	public byte[] transceive(final byte[] pCommand) {
		Assertions.assertThat(BytesUtils.bytesToStringNoSpace(pCommand)).isEqualTo(expectedData);
		return BytesUtils.fromString("");
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

}
