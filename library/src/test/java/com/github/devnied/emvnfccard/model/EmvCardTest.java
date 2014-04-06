package com.github.devnied.emvnfccard.model;

import org.fest.assertions.Assertions;
import org.junit.Test;

public class EmvCardTest {

	@Test
	public void testCard() {
		EMVCard card = new EMVCard();
		card.setAid("0000");
		card.setCardLabel("VISA");
		card.setCardNumber("12345678");
		card.setFisrtName("Test");
		card.setLastName("lastname");

		EMVCard card2 = new EMVCard();
		card2.setCardNumber("12345678");

		// Check equals
		Assertions.assertThat(card.equals(card2)).isTrue();
	}
}
