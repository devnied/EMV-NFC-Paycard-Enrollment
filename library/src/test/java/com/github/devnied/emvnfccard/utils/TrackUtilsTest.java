package com.github.devnied.emvnfccard.utils;

import java.text.SimpleDateFormat;

import org.fest.assertions.Assertions;
import org.junit.Test;

import com.github.devnied.emvnfccard.model.EmvCard;
import com.github.devnied.emvnfccard.model.enums.ServiceCode1Enum;
import com.github.devnied.emvnfccard.model.enums.ServiceCode2Enum;
import com.github.devnied.emvnfccard.model.enums.ServiceCode3Enum;

import fr.devnied.bitlib.BytesUtils;

public class TrackUtilsTest {

	@Test
	public void track2Test() {
		EmvCard card = new EmvCard();
		TrackUtils.extractTrack2Data(card,
				BytesUtils.fromString("57 13 55 66 88 77 66 55 66 77 D1 50 62 01 69 28 07 65 90 00 0F"));

		Assertions.assertThat(card).isNotNull();
		Assertions.assertThat(card.getCardNumber()).isEqualTo("5566887766556677");
		SimpleDateFormat sdf = new SimpleDateFormat("MM/yyyy");
		Assertions.assertThat(sdf.format(card.getExpireDate())).isEqualTo("06/2015");
		Assertions.assertThat(card.getService()).isNotNull();
		Assertions.assertThat(card.getService().getServiceCode1()).isEqualTo(ServiceCode1Enum.INTERNATIONNAL_ICC);
		Assertions.assertThat(card.getService().getServiceCode1().getInterchange()).isNotNull();
		Assertions.assertThat(card.getService().getServiceCode1().getTechnology()).isNotNull();
		Assertions.assertThat(card.getService().getServiceCode2()).isEqualTo(ServiceCode2Enum.NORMAL);
		Assertions.assertThat(card.getService().getServiceCode2().getAuthorizationProcessing()).isNotNull();
		Assertions.assertThat(card.getService().getServiceCode3()).isEqualTo(ServiceCode3Enum.NO_RESTRICTION);
		Assertions.assertThat(card.getService().getServiceCode3().getAllowedServices()).isNotNull();
		Assertions.assertThat(card.getService().getServiceCode3().getPinRequirements()).isNotNull();
	}

	@Test
	public void track2Test2() {
		EmvCard card = new EmvCard();
		TrackUtils.extractTrack2Data(card,
				BytesUtils.fromString("57 15 55 55 55 66 88 77 66 55 66 7D 11 05 62 01 69 28 07 65 90 00 0F"));

		Assertions.assertThat(card).isNotNull();
		Assertions.assertThat(card.getCardNumber()).isEqualTo("5555556688776655667");
		SimpleDateFormat sdf = new SimpleDateFormat("MM/yyyy");
		Assertions.assertThat(sdf.format(card.getExpireDate())).isEqualTo("05/2011");
		Assertions.assertThat(card.getService()).isNotNull();
		Assertions.assertThat(card.getService().getServiceCode1()).isEqualTo(ServiceCode1Enum.NATIONAL_ICC);
		Assertions.assertThat(card.getService().getServiceCode2()).isEqualTo(ServiceCode2Enum.BY_ISSUER);
		Assertions.assertThat(card.getService().getServiceCode3()).isEqualTo(ServiceCode3Enum.NO_RESTRICTION_PIN_REQUIRED);
	}

	@Test
	public void track2TestNullService() {
		EmvCard card = new EmvCard();
		TrackUtils.extractTrack2Data(card,
				BytesUtils.fromString("57 15 55 55 55 66 88 77 66 55 66 7D 11 05 FF F1 69 28 07 65 90 00 0F"));

		Assertions.assertThat(card).isNotNull();
		Assertions.assertThat(card.getCardNumber()).isEqualTo("5555556688776655667");
		SimpleDateFormat sdf = new SimpleDateFormat("MM/yyyy");
		Assertions.assertThat(sdf.format(card.getExpireDate())).isEqualTo("05/2011");
		Assertions.assertThat(card.getService()).isNotNull();
		Assertions.assertThat(card.getService().getServiceCode1()).isNull();
		Assertions.assertThat(card.getService().getServiceCode2()).isNull();
		Assertions.assertThat(card.getService().getServiceCode3()).isNull();
	}

}
