package com.github.devnied.emvnfccard.utils;

import com.github.devnied.emvnfccard.enums.TagTypeEnum;
import com.github.devnied.emvnfccard.enums.TagValueTypeEnum;
import com.github.devnied.emvnfccard.iso7816emv.EmvTags;
import com.github.devnied.emvnfccard.iso7816emv.ITag;
import com.github.devnied.emvnfccard.iso7816emv.TagAndLength;
import com.github.devnied.emvnfccard.utils.reflect.ReflectionTestUtils;
import fr.devnied.bitlib.BytesUtils;
import org.fest.assertions.Assertions;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class TlvUtilTest {

	private static final byte[] DATA = BytesUtils.fromString("70 63 61 13 4f 09 a0 00 00 03 15 10 10 05 28 50"
			+ "03 50 49 4e 87 01 01 61 15 4f 07 a0 00 00 00 04 30 60 50 07 4d 41 45 53 54 52 4f 87 01 02 61 1d"
			+ "4f 07 a0 00 00 00 04 80 02 50 0f 53 65 63 75 72 65 43 6f 64 65 20 41 75 74 68 87 01 00 61 16 4f"
			+ "07 a0 00 00 03 15 60 20 50 08 43 68 69 70 6b 6e 69 70 87 01 00 90 00");

	@Test
	public void testPrettyPrint() {

		String expResult = "\n"//
				+ "70 63 -- Record Template (EMV Proprietary)\n" //
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
				+ "                  00 (BINARY)\n" //
				+ "90 00 -- Command successfully executed (OK)";

		Assertions.assertThat(TlvUtil.prettyPrintAPDUResponse(DATA)).isEqualTo(expResult);
	}

	@Test
	public void testPrettyPrintTransactionRecord() {
		// Assertions.assertThat(TlvUtil.prettyPrintAPDUResponse(BytesUtils.fromString(""))).isEqualTo("");
		Assertions.assertThat(TlvUtil.prettyPrintAPDUResponse(BytesUtils.fromString("00 00 00 00 46 00 40 02 50 09 78 14 03 16 20 90 00")))
		.isEqualTo("");
	}

	/**
	 * @throws Exception
	 *
	 */
	@Test
	public void testSearchTagById() throws Exception {

		ITag tag = (ITag) ReflectionTestUtils.invokeMethod(TlvUtil.class, "searchTagById", 0x9F6B);
		Assertions.assertThat(tag).isEqualTo(EmvTags.TRACK2_DATA);
		tag = (ITag) ReflectionTestUtils.invokeMethod(TlvUtil.class, "searchTagById", 0xFFFF);
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


		ITag tag = ReflectionTestUtils.invokeMethod(TlvUtil.class, "searchTagById", 0x9F6B);
		Assertions.assertThat(tag).isEqualTo(EmvTags.TRACK2_DATA);

		tag = (ITag) ReflectionTestUtils.invokeMethod(TlvUtil.class, "searchTagById", 0xFFFF);
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
		Assertions.assertThat(TlvUtil.getFormattedTagAndLength(data, 1)).isEqualTo(" 9F 6B 01 -- Track 2 Data");
	}

	@Test
	public void testListTLV() throws Exception {
		Assertions.assertThat(TlvUtil.getlistTLV(DATA, EmvTags.APPLICATION_TEMPLATE, false).size()).isEqualTo(12);
		Assertions.assertThat(TlvUtil.getlistTLV(DATA, EmvTags.RECORD_TEMPLATE, false).size()).isEqualTo(4);
		Assertions.assertThat(TlvUtil.getlistTLV(DATA, EmvTags.TRANSACTION_CURRENCY_CODE, false).size()).isEqualTo(0);
	}

	@Test
	public void testParseTagAndLength() throws Exception {
		Assertions.assertThat(TlvUtil.parseTagAndLength(null)).isEqualTo(new ArrayList<TagAndLength>());
	}

	@Test
	public void testGetLength() throws Exception {
		Assertions.assertThat(TlvUtil.getLength(null)).isEqualTo(0);
		Assertions.assertThat(TlvUtil.getLength(new ArrayList<TagAndLength>())).isEqualTo(0);
		List<TagAndLength> list = new ArrayList<TagAndLength>();
		list.add(new TagAndLength(EmvTags.AID_CARD, 12));
		list.add(new TagAndLength(EmvTags.AID_TERMINAL, 2));
		Assertions.assertThat(TlvUtil.getLength(list)).isEqualTo(14);
	}

	@Test
	public void testGetTagValueAsString() throws Exception {
		Assertions
		.assertThat(
				(String) ReflectionTestUtils.invokeMethod(TlvUtil.class, "getTagValueAsString", EmvTags.ACQUIRER_IDENTIFIER,
						"56".getBytes())).isEqualTo("NUMERIC");
		Assertions.assertThat(
				(String) ReflectionTestUtils.invokeMethod(TlvUtil.class, "getTagValueAsString", EmvTags.ISSUER_COUNTRY_CODE_ALPHA3,
						"56".getBytes())).isEqualTo("=56");
		Assertions.assertThat(
				(String) ReflectionTestUtils.invokeMethod(TlvUtil.class, "getTagValueAsString", EmvTags.APP_DISCRETIONARY_DATA,
						"56".getBytes())).isEqualTo("BINARY");
		Assertions.assertThat(
				(String) ReflectionTestUtils.invokeMethod(TlvUtil.class, "getTagValueAsString", EmvTags.BANK_IDENTIFIER_CODE,
						"56".getBytes())).isEqualTo("=56");
		Assertions
		.assertThat((String) ReflectionTestUtils.invokeMethod(TlvUtil.class, "getTagValueAsString", EmvTags.DDOL, "56".getBytes()))
		.isEqualTo("");
	}
}
