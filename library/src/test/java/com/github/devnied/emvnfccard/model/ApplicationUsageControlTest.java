package com.github.devnied.emvnfccard.model;

import org.fest.assertions.Assertions;
import org.junit.Test;

import fr.devnied.bitlib.BytesUtils;

/**
 * The Application Usage Control is specified by EMV 4.3 Book 3, Annex C2:
 *
 * <pre>
 * byte 1: b8 valid for domestic cash      b7 valid for international cash
 *         b6 valid for domestic goods     b5 valid for international goods
 *         b4 valid for domestic services  b3 valid for international services
 *         b2 valid at ATMs                b1 valid at terminals other than ATMs
 * byte 2: b8 domestic cashback allowed    b7 international cashback allowed
 * </pre>
 */
public class ApplicationUsageControlTest {

	@Test
	public void testAllUsagesAllowed() {
		ApplicationUsageControl auc = new ApplicationUsageControl(BytesUtils.fromString("FFC0"));
		Assertions.assertThat(auc.isValidForDomesticCash()).isTrue();
		Assertions.assertThat(auc.isValidForInternationalCash()).isTrue();
		Assertions.assertThat(auc.isValidForDomesticGoods()).isTrue();
		Assertions.assertThat(auc.isValidForInternationalGoods()).isTrue();
		Assertions.assertThat(auc.isValidForDomesticServices()).isTrue();
		Assertions.assertThat(auc.isValidForInternationalServices()).isTrue();
		Assertions.assertThat(auc.isValidAtAtm()).isTrue();
		Assertions.assertThat(auc.isValidAtTerminalsOtherThanAtm()).isTrue();
		Assertions.assertThat(auc.isDomesticCashbackAllowed()).isTrue();
		Assertions.assertThat(auc.isInternationalCashbackAllowed()).isTrue();
		Assertions.assertThat(auc.isInternationalUsageAllowed()).isTrue();
	}

	@Test
	public void testDomesticOnlyCard() {
		// 'A8 00': domestic cash, domestic goods and domestic services only,
		// the typical usage control of a domestic scheme card
		ApplicationUsageControl auc = new ApplicationUsageControl(BytesUtils.fromString("A800"));
		Assertions.assertThat(auc.isValidForDomesticCash()).isTrue();
		Assertions.assertThat(auc.isValidForInternationalCash()).isFalse();
		Assertions.assertThat(auc.isValidForDomesticGoods()).isTrue();
		Assertions.assertThat(auc.isValidForInternationalGoods()).isFalse();
		Assertions.assertThat(auc.isValidForDomesticServices()).isTrue();
		Assertions.assertThat(auc.isValidForInternationalServices()).isFalse();
		Assertions.assertThat(auc.isInternationalUsageAllowed()).isFalse();
		Assertions.assertThat(auc.isDomesticCashbackAllowed()).isFalse();
		Assertions.assertThat(auc.isInternationalCashbackAllowed()).isFalse();
	}

	@Test
	public void testAtmOnlyCard() {
		// '03 00': valid at ATMs and at the other terminals but for no service
		ApplicationUsageControl auc = new ApplicationUsageControl(BytesUtils.fromString("0300"));
		Assertions.assertThat(auc.isValidAtAtm()).isTrue();
		Assertions.assertThat(auc.isValidAtTerminalsOtherThanAtm()).isTrue();
		Assertions.assertThat(auc.isValidForDomesticGoods()).isFalse();
		Assertions.assertThat(auc.getRaw()).isEqualTo(BytesUtils.fromString("0300"));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testTooShort() {
		new ApplicationUsageControl(BytesUtils.fromString("FF"));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testNull() {
		new ApplicationUsageControl(null);
	}

}
