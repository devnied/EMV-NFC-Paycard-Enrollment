package com.github.devnied.emvnfccard.iso7816emv;

import org.fest.assertions.Assertions;
import org.junit.Test;

public class EmvTerminalTest {

	@Test
	public void testTerminalCapability() {
		Assertions.assertThat(EmvTerminal.constructValue(new TagAndLength(EmvTags.ADDITIONAL_TERMINAL_CAPABILITIES, 1)))
				.isEqualTo(new byte[] { (byte) 0x8e });
		Assertions.assertThat(EmvTerminal.constructValue(new TagAndLength(EmvTags.ADDITIONAL_TERMINAL_CAPABILITIES, 2)))
				.isEqualTo(new byte[] { (byte) 0x8e, 0x00 });
	}

	@Test
	public void testAmountAuthorized() {
		Assertions.assertThat(EmvTerminal.constructValue(new TagAndLength(EmvTags.AMOUNT_AUTHORISED_NUMERIC, 1))).isEqualTo(
				new byte[] { (byte) 0x00 });
		Assertions.assertThat(EmvTerminal.constructValue(new TagAndLength(EmvTags.AMOUNT_AUTHORISED_NUMERIC, 2))).isEqualTo(
				new byte[] { 0x00, 0x00 });
	}

	@Test
	public void testTerminalCountryCode() {
		Assertions.assertThat(EmvTerminal.constructValue(new TagAndLength(EmvTags.TERMINAL_COUNTRY_CODE, 2))).isEqualTo(
				new byte[] { 0x02, 0x50 });
		Assertions.assertThat(EmvTerminal.constructValue(new TagAndLength(EmvTags.TERMINAL_COUNTRY_CODE, 4))).isEqualTo(
				new byte[] { 0x00, 0x00, 0x02, 0x50 });
	}
}
