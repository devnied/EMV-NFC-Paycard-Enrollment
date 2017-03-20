package com.github.devnied.emvnfccard.iso7816emv;

import org.fest.assertions.Assertions;
import org.junit.Test;

import fr.devnied.bitlib.BytesUtils;

public class EmvTerminalTest {

	@Test
	public void testTerminalCapability() {
		Assertions.assertThat(EmvTerminal.constructValue(new TagAndLength(EmvTags.ADDITIONAL_TERMINAL_CAPABILITIES, 1))).isEqualTo(
				new byte[] { (byte) 0x8e });
		Assertions.assertThat(EmvTerminal.constructValue(new TagAndLength(EmvTags.ADDITIONAL_TERMINAL_CAPABILITIES, 2))).isEqualTo(
				new byte[] { (byte) 0x8e, 0x00 });
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

	@Test
	public void testTerminalType() {
		Assertions.assertThat(EmvTerminal.constructValue(new TagAndLength(EmvTags.TERMINAL_TYPE, 1))).isEqualTo(new byte[] { 0x22 });
	}

	@Test
	public void testTerminalCapabilities() {
		Assertions.assertThat(EmvTerminal.constructValue(new TagAndLength(EmvTags.TERMINAL_CAPABILITIES, 2))).isEqualTo(
				new byte[] { (byte) 0xE0, (byte) 0xA0 });
		Assertions.assertThat(EmvTerminal.constructValue(new TagAndLength(EmvTags.TERMINAL_CAPABILITIES, 4))).isEqualTo(
				new byte[] { (byte) 0xE0, (byte) 0xA0, 0, 0 });
	}

	@Test
	public void test_DS_REQUESTED_OPERATOR_ID() {
		Assertions.assertThat(EmvTerminal.constructValue(new TagAndLength(EmvTags.DS_REQUESTED_OPERATOR_ID, 2))).isEqualTo(
				BytesUtils.fromString("7345"));
		Assertions.assertThat(EmvTerminal.constructValue(new TagAndLength(EmvTags.DS_REQUESTED_OPERATOR_ID, 8))).isEqualTo(
				BytesUtils.fromString("7345123215904501"));
	}
}
