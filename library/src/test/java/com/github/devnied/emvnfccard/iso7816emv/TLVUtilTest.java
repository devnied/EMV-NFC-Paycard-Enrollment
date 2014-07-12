package com.github.devnied.emvnfccard.iso7816emv;

import java.io.ByteArrayInputStream;

import org.fest.assertions.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import com.github.devnied.emvnfccard.enums.TagTypeEnum;
import com.github.devnied.emvnfccard.enums.TagValueTypeEnum;
import com.github.devnied.emvnfccard.utils.TLVUtil;

import fr.devnied.bitlib.BytesUtils;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ TLVUtil.class })
public class TLVUtilTest {

	private static final byte[] DATA = BytesUtils.fromString("70 63 61 13 4f 09 a0 00 00 03 15 10 10 05 28 50"
			+ "03 50 49 4e 87 01 01 61 15 4f 07 a0 00 00 00 04 30 60 50 07 4d 41 45 53 54 52 4f 87 01 02 61 1d"
			+ "4f 07 a0 00 00 00 04 80 02 50 0f 53 65 63 75 72 65 43 6f 64 65 20 41 75 74 68 87 01 00 61 16 4f"
			+ "07 a0 00 00 03 15 60 20 50 08 43 68 69 70 6b 6e 69 70 87 01 00");

	@Test
	public void test() {

		String expResult = "\n 70 63 -- Record Template (EMV Proprietary)\n" //
				+ "      61 13 -- Application Template\n" //
				+ "            4F 09 -- Application Identifier (AID) - card\n" //
				+ "                  A0 00 00 03 15 10 10 05 28 (BINARY)\n" //
				+ "            50 03 -- Application Label\n" //
				+ "                  50 49 4E (=PIN)\n" //
				+ "            87 01 -- Application Priority Indicator\n"//
				+ "                  01 (BINARY)\n" //
				+ "      61 15 -- Application Template\n"//
				+ "            4F 07 -- Application Identifier (AID) - card\n"//
				+ "                  A0 00 00 00 04 30 60 (BINARY)\n" //
				+ "            50 07 -- Application Label\n"//
				+ "                  4D 41 45 53 54 52 4F (=MAESTRO)\n" //
				+ "            87 01 -- Application Priority Indicator\n"//
				+ "                  02 (BINARY)\n" //
				+ "      61 1D -- Application Template\n"//
				+ "            4F 07 -- Application Identifier (AID) - card\n"//
				+ "                  A0 00 00 00 04 80 02 (BINARY)\n" //
				+ "            50 0F -- Application Label\n"//
				+ "                  53 65 63 75 72 65 43 6F 64 65 20 41 75 74 68 (=SecureCode Auth)\n" //
				+ "            87 01 -- Application Priority Indicator\n" //
				+ "                  00 (BINARY)\n"//
				+ "      61 16 -- Application Template\n" //
				+ "            4F 07 -- Application Identifier (AID) - card\n"//
				+ "                  A0 00 00 03 15 60 20 (BINARY)\n" //
				+ "            50 08 -- Application Label\n"//
				+ "                  43 68 69 70 6B 6E 69 70 (=Chipknip)\n"//
				+ "            87 01 -- Application Priority Indicator\n" //
				+ "                  00 (BINARY)";

		Assertions.assertThat(TLVUtil.prettyPrintAPDUResponse(DATA)).isEqualTo(expResult);
	}

	/**
	 * @throws Exception
	 * 
	 */
	@Test
	public void testSearchTagById() throws Exception {

		ITag tag = (ITag) Whitebox.invokeMethod(TLVUtil.class, "searchTagById", BytesUtils.fromString("9F6B"));
		Assertions.assertThat(tag).isEqualTo(EMVTags.TRACK2_DATA);
		tag = (ITag) Whitebox.invokeMethod(TLVUtil.class, "searchTagById", BytesUtils.fromString("FFFF"));
		Assertions.assertThat(tag.getName()).isEqualTo("[UNKNOWN TAG]");
		Assertions.assertThat(tag.getDescription()).isEqualTo("");
		Assertions.assertThat(tag.getTagBytes()).isEqualTo(BytesUtils.fromString("FFFF"));
		Assertions.assertThat(tag.getNumTagBytes()).isEqualTo(2);
		Assertions.assertThat(tag.isConstructed()).isEqualTo(true);
		Assertions.assertThat(tag.getTagValueType()).isEqualTo(TagValueTypeEnum.BINARY);
		Assertions.assertThat(tag.getType()).isEqualTo(TagTypeEnum.CONSTRUCTED);
	}

	/**
	 * @throws Exception
	 * 
	 */
	@Test
	public void testSearchTagByIdIn() throws Exception {

		ByteArrayInputStream in = new ByteArrayInputStream(BytesUtils.fromString("9F6B"));

		ITag tag = (ITag) Whitebox.invokeMethod(TLVUtil.class, "searchTagById", in);
		Assertions.assertThat(tag).isEqualTo(EMVTags.TRACK2_DATA);

		in = new ByteArrayInputStream(BytesUtils.fromString("FFFF"));
		tag = (ITag) Whitebox.invokeMethod(TLVUtil.class, "searchTagById", in);
		Assertions.assertThat(tag.getName()).isEqualTo("[UNKNOWN TAG]");
		Assertions.assertThat(tag.getDescription()).isEqualTo("");
		Assertions.assertThat(tag.getTagBytes()).isEqualTo(BytesUtils.fromString("FFFF"));
		Assertions.assertThat(tag.getNumTagBytes()).isEqualTo(2);
		Assertions.assertThat(tag.isConstructed()).isEqualTo(true);
		Assertions.assertThat(tag.getTagValueType()).isEqualTo(TagValueTypeEnum.BINARY);
		Assertions.assertThat(tag.getType()).isEqualTo(TagTypeEnum.CONSTRUCTED);
	}

	@Test
	public void testGetFormattedTagAndLength() throws Exception {

		byte[] data = BytesUtils.fromString("9f6b01");
		Assertions.assertThat(TLVUtil.getFormattedTagAndLength(data, 1)).isEqualTo(" 9F 6B 01 -- Track 2 Data");
	}

	@Test
	public void testListTLV() throws Exception {
		Assertions.assertThat(TLVUtil.getlistTLV(DATA, EMVTags.APPLICATION_TEMPLATE, false).size()).isEqualTo(12);
		Assertions.assertThat(TLVUtil.getlistTLV(DATA, EMVTags.RECORD_TEMPLATE, false).size()).isEqualTo(4);
		Assertions.assertThat(TLVUtil.getlistTLV(DATA, EMVTags.TRANSACTION_CURRENCY_CODE, false).size()).isEqualTo(0);
	}
}
