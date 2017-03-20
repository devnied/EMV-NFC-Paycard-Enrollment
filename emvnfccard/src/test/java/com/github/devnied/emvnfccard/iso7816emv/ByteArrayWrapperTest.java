package com.github.devnied.emvnfccard.iso7816emv;

import org.fest.assertions.Assertions;
import org.junit.Assert;
import org.junit.Test;

public class ByteArrayWrapperTest {

	@Test
	public void test() {
		ByteArrayWrapper baw = ByteArrayWrapper.wrapperAround(new byte[] { 0x45, 0x66 });
		ByteArrayWrapper baw2 = ByteArrayWrapper.wrapperAround(new byte[] { 0x45, 0x66 });
		ByteArrayWrapper baw3 = ByteArrayWrapper.wrapperAround(new byte[] { 0x66 });

		Assertions.assertThat(baw.equals(baw2)).isTrue();
		Assertions.assertThat(baw.hashCode()).isEqualTo(baw2.hashCode());
		Assertions.assertThat(baw.equals(baw3)).isFalse();
		Assertions.assertThat(baw2.equals(baw3)).isFalse();
		Assertions.assertThat(baw2.equals(null)).isFalse();

		try {
			ByteArrayWrapper.wrapperAround(null);
			Assert.fail();
		} catch (Exception e) {

		}
	}

}
