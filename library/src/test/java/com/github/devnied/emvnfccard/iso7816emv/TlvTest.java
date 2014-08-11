package com.github.devnied.emvnfccard.iso7816emv;

import org.fest.assertions.Assertions;
import org.junit.Assert;
import org.junit.Test;

public class TlvTest {

	@Test
	public void test() {
		TLV tlv = new TLV(EmvTags.ACQUIRER_IDENTIFIER, 2, new byte[] { 0x02 }, new byte[] { 0x50, 0x43 });
		Assertions.assertThat(tlv.getLength()).isEqualTo(2);
		Assertions.assertThat(tlv.getValueBytes()).isEqualTo(new byte[] { 0x50, 0x43 });
		Assertions.assertThat(tlv.getRawEncodedLengthBytes()).isEqualTo(new byte[] { 0x02 });
		Assertions.assertThat(tlv.getTagBytes()).isEqualTo(EmvTags.ACQUIRER_IDENTIFIER.getTagBytes());

		tlv.setLength(3);
		Assertions.assertThat(tlv.getLength()).isEqualTo(3);
		tlv.setRawEncodedLengthBytes(new byte[] { 0x04 });
		Assertions.assertThat(tlv.getRawEncodedLengthBytes()).isEqualTo(new byte[] { 0x04 });
		tlv.setTag(EmvTags.AID_CARD);
		Assertions.assertThat(tlv.getTag()).isEqualTo(EmvTags.AID_CARD);
		tlv.setValueBytes(new byte[] { 0x08 });
		Assertions.assertThat(tlv.getValueBytes()).isEqualTo(new byte[] { 0x08 });

		try {
			new TLV(EmvTags.ACQUIRER_IDENTIFIER, 2, new byte[] { 0x02 }, new byte[] { 0x50 });
			Assert.fail();
		} catch (IllegalArgumentException iae) {
			Assert.assertTrue(true);
		}

		try {
			new TLV(EmvTags.ACQUIRER_IDENTIFIER, 2, new byte[] { 0x02 }, null);
			Assert.fail();
		} catch (IllegalArgumentException iae) {
			Assert.assertTrue(true);
		}
	}

}
