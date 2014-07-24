package com.github.devnied.emvnfccard.enums;

import org.fest.assertions.Assertions;
import org.junit.Test;

public class EMVCardSchemeTest {

	@Test
	public void testCardType() throws Exception {
		Assertions.assertThat(EMVCardScheme.getCardTypeByCardNumber("4000000000000000")).isEqualTo(EMVCardScheme.VISA);
		Assertions.assertThat(EMVCardScheme.getCardTypeByCardNumber("5000000000000000")).isEqualTo(EMVCardScheme.MASTER_CARD1);
		Assertions.assertThat(EMVCardScheme.getCardTypeByCardNumber("6200000000000000")).isEqualTo(EMVCardScheme.UNIONPAY);
		Assertions.assertThat(EMVCardScheme.getCardTypeByCardNumber(null)).isEqualTo(null);
	}

	@Test
	public void testAid() throws Exception {
		Assertions.assertThat(EMVCardScheme.getCardTypeByAid("A0 00 00 00 03 ")).isEqualTo(EMVCardScheme.VISA);
		Assertions.assertThat(EMVCardScheme.getCardTypeByAid("A0 00 00 00 03 9989")).isEqualTo(EMVCardScheme.VISA);
		Assertions.assertThat(EMVCardScheme.getCardTypeByAid("A0 00 00 00 04")).isEqualTo(EMVCardScheme.MASTER_CARD1);
		Assertions.assertThat(EMVCardScheme.getCardTypeByAid("A0 00 00 03 33")).isEqualTo(EMVCardScheme.UNIONPAY);
		Assertions.assertThat(EMVCardScheme.getCardTypeByAid(null)).isEqualTo(null);
	}

	@Test
	public void testMethod() throws Exception {
		Assertions.assertThat(EMVCardScheme.VISA.getAid()).isEqualTo("A0 00 00 00 03");
		Assertions.assertThat(EMVCardScheme.VISA.getAidByte()).isEqualTo(new byte[] { (byte) 0xA0, 0, 0, 0, 0x03 });
		Assertions.assertThat(EMVCardScheme.VISA.getName()).isEqualTo("VISA");
	}

}
