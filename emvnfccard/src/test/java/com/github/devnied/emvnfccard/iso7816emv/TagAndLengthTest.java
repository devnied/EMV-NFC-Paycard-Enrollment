package com.github.devnied.emvnfccard.iso7816emv;

import org.fest.assertions.Assertions;
import org.junit.Test;

import fr.devnied.bitlib.BytesUtils;

public class TagAndLengthTest {

	@Test
	public void testMethod() {

		TagAndLength tal = new TagAndLength(EmvTags.AID_CARD, 2);

		Assertions.assertThat(tal.getBytes()).isEqualTo(BytesUtils.fromString("4F02"));
		Assertions.assertThat(tal.getLength()).isEqualTo(2);
		Assertions.assertThat(tal.getTag()).isEqualTo(EmvTags.AID_CARD);

		Assertions
				.assertThat(tal.toString())
				.isEqualTo(
						"Tag[4F] Name=Application Identifier (AID) - card, TagType=PRIMITIVE, ValueType=BINARY, Class=APPLICATION length: 2");

	}
}
