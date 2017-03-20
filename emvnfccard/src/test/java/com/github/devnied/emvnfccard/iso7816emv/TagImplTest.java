package com.github.devnied.emvnfccard.iso7816emv;

import org.fest.assertions.Assertions;
import org.junit.Assert;
import org.junit.Test;

import com.github.devnied.emvnfccard.enums.TagValueTypeEnum;
import com.github.devnied.emvnfccard.iso7816emv.ITag.Class;
import com.github.devnied.emvnfccard.iso7816emv.impl.TagImpl;

public class TagImplTest {

	@Test
	public void testMethod() {

		TagImpl tag = new TagImpl("90", TagValueTypeEnum.MIXED, "", "description test");
		TagImpl tag2 = new TagImpl("9000", TagValueTypeEnum.MIXED, "", "description test");

		Assertions.assertThat(tag.toString()).isEqualTo(
				"Tag[90] Name=, TagType=PRIMITIVE, ValueType=MIXED, Class=CONTEXT_SPECIFIC");
		Assertions.assertThat(tag.getTagClass()).isEqualTo(Class.CONTEXT_SPECIFIC);

		// Equals test
		Assertions.assertThat(tag.equals(null)).isFalse();
		Assertions.assertThat(tag.equals(tag2)).isFalse();
		Assertions.assertThat(tag.equals(tag)).isTrue();

	}

	@Test
	public void testConstructor() {
		try {
			new TagImpl((byte[]) null, TagValueTypeEnum.MIXED, "", "description test");
			Assert.fail();
		} catch (IllegalArgumentException iae) {
			Assert.assertTrue(true);
		}

		try {
			new TagImpl(new byte[] {}, TagValueTypeEnum.MIXED, "", "description test");
			Assert.fail();
		} catch (IllegalArgumentException iae) {
			Assert.assertTrue(true);
		}

		try {
			new TagImpl(new byte[] { 0x34 }, null, "", "description test");
			Assert.fail();
		} catch (IllegalArgumentException iae) {
			Assert.assertTrue(true);
		}

	}

}
