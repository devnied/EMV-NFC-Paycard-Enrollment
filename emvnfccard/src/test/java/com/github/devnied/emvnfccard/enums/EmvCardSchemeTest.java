package com.github.devnied.emvnfccard.enums;

import org.fest.assertions.Assertions;
import org.junit.Test;

public class EmvCardSchemeTest {

	@Test
	public void testCardType() throws Exception {
		Assertions.assertThat(EmvCardScheme.getCardTypeByCardNumber("4000000000000000")).isEqualTo(EmvCardScheme.VISA);
		Assertions.assertThat(EmvCardScheme.getCardTypeByCardNumber("5100000000000000")).isEqualTo(EmvCardScheme.MASTER_CARD);
		Assertions.assertThat(EmvCardScheme.getCardTypeByCardNumber("6200000000000000")).isEqualTo(EmvCardScheme.UNIONPAY);
		Assertions.assertThat(EmvCardScheme.getCardTypeByCardNumber(null)).isEqualTo(null);
	}

	@Test
	public void testAid() throws Exception {
		Assertions.assertThat(EmvCardScheme.getCardTypeByAid("A0 00 00 00 03 ")).isEqualTo(EmvCardScheme.VISA);
		Assertions.assertThat(EmvCardScheme.getCardTypeByAid("A0 00 00 00 03 9989")).isEqualTo(EmvCardScheme.VISA);
		Assertions.assertThat(EmvCardScheme.getCardTypeByAid("A0 00 00 00 04")).isEqualTo(EmvCardScheme.MASTER_CARD);
		Assertions.assertThat(EmvCardScheme.getCardTypeByAid("A0 00 00 00 05")).isEqualTo(EmvCardScheme.MASTER_CARD);
		Assertions.assertThat(EmvCardScheme.getCardTypeByAid("A0 00 00 03 33")).isEqualTo(EmvCardScheme.UNIONPAY);
		Assertions.assertThat(EmvCardScheme.getCardTypeByAid(null)).isEqualTo(null);
	}

	@Test
	public void testMethod() throws Exception {
		Assertions.assertThat(EmvCardScheme.VISA.getAid()[0]).isEqualTo("A0 00 00 00 03");
		Assertions.assertThat(EmvCardScheme.VISA.getAidByte()[0]).isEqualTo(new byte[] { (byte) 0xA0, 0, 0, 0, 0x03 });
		Assertions.assertThat(EmvCardScheme.VISA.getName()).isEqualTo("VISA");
	}

}
