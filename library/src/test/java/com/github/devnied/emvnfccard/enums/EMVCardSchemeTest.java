package com.github.devnied.emvnfccard.enums;

import org.fest.assertions.Assertions;
import org.junit.Test;

public class EMVCardSchemeTest {

	@Test
	public void tesCardType() throws Exception {
		Assertions.assertThat(EMVCardScheme.getCardTypeByCardNumber("4000000000000000")).isEqualTo(EMVCardScheme.VISA);
		Assertions.assertThat(EMVCardScheme.getCardTypeByCardNumber("5000000000000000")).isEqualTo(EMVCardScheme.MASTER_CARD1);
		Assertions.assertThat(EMVCardScheme.getCardTypeByCardNumber("6200000000000000")).isEqualTo(EMVCardScheme.UNIONPAY);
		Assertions.assertThat(EMVCardScheme.getCardTypeByCardNumber(null)).isEqualTo(null);
	}

}
