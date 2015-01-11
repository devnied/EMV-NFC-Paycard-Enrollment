package com.github.devnied.emvnfccard.iso7816emv;

import org.fest.assertions.Assertions;
import org.junit.Test;

import fr.devnied.bitlib.BytesUtils;

public class TerminalTransactionQualifiersTest {

	@Test
	public void test() {
		TerminalTransactionQualifiers qualifier = new TerminalTransactionQualifiers();
		qualifier.setConsumerDeviceCVMsupported(true);
		Assertions.assertThat(qualifier.consumerDeviceCVMsupported()).isEqualTo(true);
		Assertions.assertThat(BytesUtils.bytesToStringNoSpace(qualifier.getBytes())).isEqualTo("00004000");
		qualifier.setContactChipOfflinePINsupported(true);
		Assertions.assertThat(qualifier.contactChipOfflinePINsupported()).isEqualTo(true);
		Assertions.assertThat(BytesUtils.bytesToStringNoSpace(qualifier.getBytes())).isEqualTo("00204000");
		qualifier.setContactEMVsupported(true);
		Assertions.assertThat(qualifier.contactEMVsupported()).isEqualTo(true);
		Assertions.assertThat(BytesUtils.bytesToStringNoSpace(qualifier.getBytes())).isEqualTo("10204000");
		qualifier.setContactlessEMVmodeSupported(true);
		Assertions.assertThat(qualifier.contactlessEMVmodeSupported()).isEqualTo(true);
		Assertions.assertThat(BytesUtils.bytesToStringNoSpace(qualifier.getBytes())).isEqualTo("30204000");
		qualifier.setMagneticStripeSupported(true);
		Assertions.assertThat(qualifier.contactlessMagneticStripeSupported()).isEqualTo(true);
		Assertions.assertThat(BytesUtils.bytesToStringNoSpace(qualifier.getBytes())).isEqualTo("B0204000");
		qualifier.setContactlessVSDCsupported(true);
		Assertions.assertThat(qualifier.contactlessVSDCsupported()).isEqualTo(true);
		Assertions.assertThat(BytesUtils.bytesToStringNoSpace(qualifier.getBytes())).isEqualTo("D0204000");
		qualifier.setCvmRequired(true);
		Assertions.assertThat(qualifier.cvmRequired()).isEqualTo(true);
		Assertions.assertThat(BytesUtils.bytesToStringNoSpace(qualifier.getBytes())).isEqualTo("D0604000");
		qualifier.setIssuerUpdateProcessingSupported(true);
		Assertions.assertThat(qualifier.issuerUpdateProcessingSupported()).isEqualTo(true);
		Assertions.assertThat(BytesUtils.bytesToStringNoSpace(qualifier.getBytes())).isEqualTo("D060C000");
		qualifier.setOnlineCryptogramRequired(true);
		Assertions.assertThat(qualifier.onlineCryptogramRequired()).isEqualTo(true);
		Assertions.assertThat(BytesUtils.bytesToStringNoSpace(qualifier.getBytes())).isEqualTo("D0E0C000");
		qualifier.setOnlinePINsupported(true);
		Assertions.assertThat(qualifier.onlinePINsupported()).isEqualTo(true);
		Assertions.assertThat(BytesUtils.bytesToStringNoSpace(qualifier.getBytes())).isEqualTo("D4E0C000");
		qualifier.setReaderIsOfflineOnly(true);
		Assertions.assertThat(qualifier.readerIsOfflineOnly()).isEqualTo(true);
		Assertions.assertThat(BytesUtils.bytesToStringNoSpace(qualifier.getBytes())).isEqualTo("DCE0C000");
		qualifier.setSignatureSupported(true);
		Assertions.assertThat(qualifier.signatureSupported()).isEqualTo(true);
		Assertions.assertThat(BytesUtils.bytesToStringNoSpace(qualifier.getBytes())).isEqualTo("DEE0C000");

	}

}
