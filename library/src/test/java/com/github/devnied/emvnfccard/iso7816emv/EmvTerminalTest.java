package com.github.devnied.emvnfccard.iso7816emv;

import org.fest.assertions.Assertions;
import org.junit.Test;

import com.github.devnied.emvnfccard.iso7816emv.impl.DefaultTerminalImpl;

import fr.devnied.bitlib.BytesUtils;

public class EmvTerminalTest {

	private ITerminal terminal = new DefaultTerminalImpl();

	@Test
	public void testTerminalCapability() {
		Assertions.assertThat(terminal.constructValue(new TagAndLength(EmvTags.ADDITIONAL_TERMINAL_CAPABILITIES, 1))).isEqualTo(
				new byte[] { (byte) 0x8e });
		Assertions.assertThat(terminal.constructValue(new TagAndLength(EmvTags.ADDITIONAL_TERMINAL_CAPABILITIES, 2))).isEqualTo(
				new byte[] { (byte) 0x8e, 0x00 });
	}

	@Test
	public void testAmountAuthorized() {
		Assertions.assertThat(terminal.constructValue(new TagAndLength(EmvTags.AMOUNT_AUTHORISED_NUMERIC, 1))).isEqualTo(
				new byte[] { (byte) 0x01 });
		Assertions.assertThat(terminal.constructValue(new TagAndLength(EmvTags.AMOUNT_AUTHORISED_NUMERIC, 2))).isEqualTo(
				new byte[] { 0x00, 0x01 });
	}

	@Test
	public void testTerminalCountryCode() {
		Assertions.assertThat(terminal.constructValue(new TagAndLength(EmvTags.TERMINAL_COUNTRY_CODE, 2))).isEqualTo(
				new byte[] { 0x02, 0x50 });
		Assertions.assertThat(terminal.constructValue(new TagAndLength(EmvTags.TERMINAL_COUNTRY_CODE, 4))).isEqualTo(
				new byte[] { 0x00, 0x00, 0x02, 0x50 });
	}

	@Test
	public void testTerminalType() {
		Assertions.assertThat(terminal.constructValue(new TagAndLength(EmvTags.TERMINAL_TYPE, 1))).isEqualTo(new byte[] { 0x22 });
	}

	@Test
	public void testTerminalCapabilities() {
		Assertions.assertThat(terminal.constructValue(new TagAndLength(EmvTags.TERMINAL_CAPABILITIES, 2))).isEqualTo(
				new byte[] { (byte) 0xE0, (byte) 0xA0 });
		Assertions.assertThat(terminal.constructValue(new TagAndLength(EmvTags.TERMINAL_CAPABILITIES, 4))).isEqualTo(
				new byte[] { 0, (byte) 0xE0, (byte) 0xA0, 0 });
	}

	@Test
	public void test_DS_REQUESTED_OPERATOR_ID() {
		Assertions.assertThat(terminal.constructValue(new TagAndLength(EmvTags.DS_REQUESTED_OPERATOR_ID, 2))).isEqualTo(
				BytesUtils.fromString("7A45"));
		Assertions.assertThat(terminal.constructValue(new TagAndLength(EmvTags.DS_REQUESTED_OPERATOR_ID, 8))).isEqualTo(
				BytesUtils.fromString("7A45123EE59C7F40"));
	}

	@Test
	public void test_MERCHANT_TYPE_INDICATOR() {
		Assertions.assertThat(terminal.constructValue(new TagAndLength(EmvTags.MERCHANT_TYPE_INDICATOR, 1))).isEqualTo(
				BytesUtils.fromString("01"));
	}

	@Test
	public void test_TERMINAL_TRANSACTION_INFORMATION() {
		Assertions.assertThat(terminal.constructValue(new TagAndLength(EmvTags.TERMINAL_TRANSACTION_INFORMATION, 3))).isEqualTo(
				BytesUtils.fromString("C08000"));
	}

	@Test
	public void test_TERMINAL_TRANSACTION_TYPE() {
		Assertions.assertThat(terminal.constructValue(new TagAndLength(EmvTags.TERMINAL_TRANSACTION_TYPE, 1))).isEqualTo(
				BytesUtils.fromString("00"));
	}
}
