package com.github.devnied.emvnfccard.iso7816emv;

import org.fest.assertions.Assertions;
import org.junit.Test;

import fr.devnied.bitlib.BytesUtils;

public class TerminalTransactionQualifiersTest {

	@Test
	public void test() {
		TerminalTransactionQualifiers qualifier = new TerminalTransactionQualifiers();
		qualifier.setConsumerDeviceCVMsupported(true);
		Assertions.assertThat(BytesUtils.bytesToStringNoSpace(qualifier.getBytes())).isEqualTo("00004000");
		qualifier.setContactChipOfflinePINsupported(true);
		Assertions.assertThat(BytesUtils.bytesToStringNoSpace(qualifier.getBytes())).isEqualTo("00204000");
		qualifier.setContactEMVsupported(true);
		Assertions.assertThat(BytesUtils.bytesToStringNoSpace(qualifier.getBytes())).isEqualTo("10204000");
		qualifier.setContactlessEMVmodeSupported(true);
		Assertions.assertThat(BytesUtils.bytesToStringNoSpace(qualifier.getBytes())).isEqualTo("30204000");
		qualifier.setContactlessMagneticStripeSupported(true);
		Assertions.assertThat(BytesUtils.bytesToStringNoSpace(qualifier.getBytes())).isEqualTo("B0204000");
		qualifier.setContactlessVSDCsupported(true);
		Assertions.assertThat(BytesUtils.bytesToStringNoSpace(qualifier.getBytes())).isEqualTo("D0204000");
		qualifier.setCvmRequired(true);
		Assertions.assertThat(BytesUtils.bytesToStringNoSpace(qualifier.getBytes())).isEqualTo("D0604000");
		qualifier.setIssuerUpdateProcessingSupported(true);
		Assertions.assertThat(BytesUtils.bytesToStringNoSpace(qualifier.getBytes())).isEqualTo("D060C000");
		qualifier.setOnlineCryptogramRequired(true);
		Assertions.assertThat(BytesUtils.bytesToStringNoSpace(qualifier.getBytes())).isEqualTo("D0E0C000");
		qualifier.setOnlinePINsupported(true);
		Assertions.assertThat(BytesUtils.bytesToStringNoSpace(qualifier.getBytes())).isEqualTo("D4E0C000");
		qualifier.setReaderIsOfflineOnly(true);
		Assertions.assertThat(BytesUtils.bytesToStringNoSpace(qualifier.getBytes())).isEqualTo("DCE0C000");
		qualifier.setSignatureSupported(true);
		Assertions.assertThat(BytesUtils.bytesToStringNoSpace(qualifier.getBytes())).isEqualTo("DEE0C000");
	}

}
