package com.github.devnied.emvnfccard.model;

import org.fest.assertions.Assertions;
import org.junit.Test;

import fr.devnied.bitlib.BytesUtils;

/**
 * The Application Interchange Profile is specified by EMV 4.3 Book 3, Annex C1.
 */
public class ApplicationInterchangeProfileTest {

	@Test
	public void testVisaAip() {
		// '2000' is the AIP returned by the Visa application of the
		// VisaCardPpse and VisaCardPpse3 traces
		ApplicationInterchangeProfile aip = new ApplicationInterchangeProfile(BytesUtils.fromString("2000"));
		Assertions.assertThat(aip.isSdaSupported()).isFalse();
		Assertions.assertThat(aip.isDdaSupported()).isTrue();
		Assertions.assertThat(aip.isCardholderVerificationSupported()).isFalse();
		Assertions.assertThat(aip.isTerminalRiskManagementToBePerformed()).isFalse();
		Assertions.assertThat(aip.isIssuerAuthenticationSupported()).isFalse();
		Assertions.assertThat(aip.isOnDeviceCardholderVerificationSupported()).isFalse();
		Assertions.assertThat(aip.isCdaSupported()).isFalse();
		Assertions.assertThat(aip.isEmvModeSupported()).isFalse();
		Assertions.assertThat(aip.getRaw()).isEqualTo(BytesUtils.fromString("2000"));
	}

	@Test
	public void testMasterCardAip() {
		// '1980' is the AIP returned by the GPO of the MasterCardPpse and
		// MasterCardPpse2 traces
		ApplicationInterchangeProfile aip = new ApplicationInterchangeProfile(BytesUtils.fromString("1980"));
		Assertions.assertThat(aip.isSdaSupported()).isFalse();
		Assertions.assertThat(aip.isDdaSupported()).isFalse();
		Assertions.assertThat(aip.isCardholderVerificationSupported()).isTrue();
		Assertions.assertThat(aip.isTerminalRiskManagementToBePerformed()).isTrue();
		Assertions.assertThat(aip.isIssuerAuthenticationSupported()).isFalse();
		Assertions.assertThat(aip.isOnDeviceCardholderVerificationSupported()).isFalse();
		Assertions.assertThat(aip.isCdaSupported()).isTrue();
		Assertions.assertThat(aip.isEmvModeSupported()).isTrue();
	}

	@Test
	public void testFormat1Aip() {
		// '7C00' is the AIP of the format 1 GPO response of the VisaCardPpse2
		// and VisaCardAid traces
		ApplicationInterchangeProfile aip = new ApplicationInterchangeProfile(BytesUtils.fromString("7C00"));
		Assertions.assertThat(aip.isSdaSupported()).isTrue();
		Assertions.assertThat(aip.isDdaSupported()).isTrue();
		Assertions.assertThat(aip.isCardholderVerificationSupported()).isTrue();
		Assertions.assertThat(aip.isTerminalRiskManagementToBePerformed()).isTrue();
		Assertions.assertThat(aip.isIssuerAuthenticationSupported()).isTrue();
		Assertions.assertThat(aip.isCdaSupported()).isFalse();
	}

	@Test
	public void testExtraBytesAreIgnored() {
		// Only the 2 first bytes are meaningful
		ApplicationInterchangeProfile aip = new ApplicationInterchangeProfile(BytesUtils.fromString("200099"));
		Assertions.assertThat(aip.getRaw()).isEqualTo(BytesUtils.fromString("2000"));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testTooShort() {
		new ApplicationInterchangeProfile(BytesUtils.fromString("20"));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testNull() {
		new ApplicationInterchangeProfile(null);
	}

}
