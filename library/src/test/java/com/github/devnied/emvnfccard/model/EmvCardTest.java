package com.github.devnied.emvnfccard.model;

import java.util.Arrays;
import java.util.Date;

import org.fest.assertions.Assertions;
import org.junit.Test;

import fr.devnied.bitlib.BytesUtils;

public class EmvCardTest {

	@Test
	public void testCard() {
		EmvCard card = new EmvCard();
		Application app = new Application();
		app.setAid(BytesUtils.fromString("0000"));
		app.setApplicationLabel("VISA");
		app.setLeftPinTry(3);
		app.setTransactionCounter(10);
		card.getApplications().add(app);
		EmvTrack2 track2 = new EmvTrack2();
		track2.setCardNumber("12345678");
		card.setTrack2(track2);
		card.setHolderFirstname("FirstName");
		card.setHolderLastname("Lastname");
		card.setAtrDescription(Arrays.asList("test", "ok"));

		Assertions.assertThat(card.getAtrDescription()).isEqualTo(Arrays.asList("test", "ok"));

		EmvCard card2 = new EmvCard();
		card2.setTrack2(track2);

		// Check equals
		Assertions.assertThat(card.equals(card2)).isTrue();
	}

	@Test
	public void testCardTrack2() {
		EmvCard card = new EmvCard();
		Application app = new Application();
		app.setAid(BytesUtils.fromString("0000"));
		app.setAid(null);
		app.setApplicationLabel("VISA");
		app.setLeftPinTry(3);
		app.setTransactionCounter(10);
		card.getApplications().add(app);
		EmvTrack2 track2 = new EmvTrack2();
		track2.setCardNumber("12345678");
		card.setTrack2(track2);
		card.setHolderFirstname("FirstName");
		card.setHolderLastname("Lastname");

		Assertions.assertThat(card.getCardNumber()).isEqualTo("12345678");
		Assertions.assertThat(card.getApplications().get(0).getAid()).isNotNull();
	}

	@Test
	public void testCardTrack1() {
		Date date = new Date();
		EmvCard card = new EmvCard();
		Application app = new Application();
		app.setAid(BytesUtils.fromString("0000"));
		app.setApplicationLabel("VISA");
		app.setLeftPinTry(3);
		app.setTransactionCounter(10);
		card.getApplications().add(app);
		EmvTrack1 track1 = new EmvTrack1();
		track1.setCardNumber("12345678");
		track1.setExpireDate(date);
		track1.setHolderFirstname("John");
		track1.setHolderLastname("Doe");
		card.setTrack1(track1);
		card.setAtrDescription(Arrays.asList("test", "ok"));

		Assertions.assertThat(card.getAtrDescription()).isEqualTo(Arrays.asList("test", "ok"));

		EmvCard card2 = new EmvCard();
		card2.setTrack1(track1);

		// Check equals
		Assertions.assertThat(card.equals(card2)).isTrue();
		Assertions.assertThat(card.getExpireDate()).isEqualTo(date);
		Assertions.assertThat(card.getCardNumber()).isEqualTo("12345678");
		Assertions.assertThat(card.getHolderFirstname()).isEqualTo("John");
		Assertions.assertThat(card.getHolderLastname()).isEqualTo("Doe");
	}

	@Test
	public void testApplication() {
		Application app = new Application();
		app.setPriority(1);

		Application app2 = new Application();
		app2.setPriority(2);

		Assertions.assertThat(app2.compareTo(app)).isPositive();

		Application app3 = new Application();
		app3.setPriority(1);

		Assertions.assertThat(app3.compareTo(app)).isZero();
	}

}
