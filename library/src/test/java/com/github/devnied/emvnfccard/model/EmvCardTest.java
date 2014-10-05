package com.github.devnied.emvnfccard.model;

import java.util.Arrays;

import org.fest.assertions.Assertions;
import org.junit.Test;

public class EmvCardTest {

	@Test
	public void testCard() {
		EmvCard card = new EmvCard();
		card.setAid("0000");
		card.setApplicationLabel("VISA");
		card.setCardNumber("12345678");
		card.setHolderFirstname("FirstName");
		card.setHolderLastname("Lastname");
		card.setAtrDescription(Arrays.asList("test", "ok"));

		Assertions.assertThat(card.getAtrDescription()).isEqualTo(Arrays.asList("test", "ok"));

		EmvCard card2 = new EmvCard();
		card2.setCardNumber("12345678");

		// Check equals
		Assertions.assertThat(card.equals(card2)).isTrue();
	}
}
