package com.github.devnied.emvnfccard;

import java.util.List;

import org.fest.assertions.Assertions;
import org.junit.Test;

import com.github.devnied.emvnfccard.exception.CommunicationException;
import com.github.devnied.emvnfccard.iso7816emv.TagAndLength;
import com.github.devnied.emvnfccard.model.Application;
import com.github.devnied.emvnfccard.parser.EmvTemplate;
import com.github.devnied.emvnfccard.parser.impl.EmvParser;
import com.github.devnied.emvnfccard.provider.ScriptedProvider;
import com.github.devnied.emvnfccard.enums.TagValueTypeEnum;
import com.github.devnied.emvnfccard.iso7816emv.EmvTags;
import com.github.devnied.emvnfccard.model.DirectoryEntry;
import com.github.devnied.emvnfccard.model.enums.CvmMethodEnum;
import com.github.devnied.emvnfccard.utils.CvmUtils;
import com.github.devnied.emvnfccard.utils.ResponseUtils;
import com.github.devnied.emvnfccard.utils.TlvUtil;
import com.github.devnied.emvnfccard.utils.reflect.ReflectionTestUtils;

import fr.devnied.bitlib.BytesUtils;

/**
 * Encoding rules the specification states, asserted on the encodings it
 * defines. No card content is involved: every byte string below is a data
 * object list or a command header, whose shape comes from the specification
 * and not from a capture.
 */
public class SpecConformanceTest {

	/**
	 * The length of a data object list entry is a single byte (EMV 4.3 Book 3
	 * section 5.4). It is not a BER length, so a value of '82' asks for 130
	 * bytes and does not introduce a two bytes long form.
	 */
	@Test
	public void testDataObjectListLengthIsASingleByte() {
		List<TagAndLength> list = TlvUtil.parseTagAndLength(BytesUtils.fromString("9F4E82 9F0206"));

		Assertions.assertThat(list).hasSize(2);
		Assertions.assertThat(list.get(0).getLength()).isEqualTo(130);
		Assertions.assertThat(list.get(1).getLength()).isEqualTo(6);
	}

	/**
	 * The length of the command template is a BER length (EMV 4.3 Book 3
	 * Annex B2): from 128 bytes on it takes the long form. Written on a single
	 * byte, a total of 134 would reach the card as '86', announcing six length
	 * bytes that are not there.
	 */
	@Test
	public void testCommandTemplateLengthTakesTheLongForm() throws CommunicationException {
		ScriptedProvider provider = new ScriptedProvider();
		EmvParser parser = new EmvParser(EmvTemplate.Builder().setProvider(provider).build());

		// A list asking for 129 bytes then 6 more, 135 in total
		ReflectionTestUtils.invokeMethod(parser, "getGetProcessingOptions",
				(Object) BytesUtils.fromString("9F4C81 9F0206"));

		String sent = provider.getReceived().get(0);
		// 80 A8 00 00 <Lc> 83 81 87 ...
		Assertions.assertThat(sent).startsWith("80A80000");
		Assertions.assertThat(sent.substring(10, 16)).isEqualTo("838187");

		// The command template sent is reported on the application, long
		// form included
		Application app = new Application();
		ReflectionTestUtils.invokeMethod(parser, "getGetProcessingOptions", BytesUtils.fromString("9F4C81 9F0206"), app);
		Assertions.assertThat(app.getGpoCommandTemplate()).hasSize(3 + 135);
		Assertions.assertThat(app.getGpoCommandTemplate()[0]).isEqualTo((byte) 0x83);
		Assertions.assertThat(app.getGpoCommandTemplate()[1]).isEqualTo((byte) 0x81);
		Assertions.assertThat(app.getGpoCommandTemplate()[2]).isEqualTo((byte) 0x87);
		Assertions.assertThat(new java.util.ArrayList<String>(app.getPdolTerminalValues().keySet())).containsExactly("9F4C", "9F02");
		Assertions.assertThat(app.getPdolTerminalValues().get("9F4C")).hasSize(129);
		// Lc is 138 ('8A'), the 3 bytes of tag and length plus the 135 of values
		Assertions.assertThat(provider.getReceived().get(1))
				.isEqualTo("80A80000" + "8A" + BytesUtils.bytesToStringNoSpace(app.getGpoCommandTemplate()) + "00");
	}

	/**
	 * The data field of a SELECT carries at most 16 bytes (EMV 4.3 Book 1
	 * Table 40), and EMV Contactless Book B bounds the ADF Name plus its
	 * Extended Selection by the same 16 bytes.
	 */
	@Test
	public void testExtendedSelectionCannotOverflowTheCommand() throws CommunicationException {
		ScriptedProvider provider = new ScriptedProvider();
		EmvParser parser = new EmvParser(EmvTemplate.Builder().setProvider(provider).build());
		Application app = new Application();
		// 7 bytes of name and 10 of extended selection would make 17
		app.setAid(BytesUtils.fromString("A0000000031010"));
		app.setExtendedSelection(BytesUtils.fromString("00112233445566778899"));

		ReflectionTestUtils.invokeMethod(parser, "selectApplication", app);

		// The bare name is selected, the extended selection is dropped
		Assertions.assertThat(provider.getReceived()).hasSize(1);
		Assertions.assertThat(provider.getReceived().get(0)).isEqualTo("00A4040007A000000003101000");
	}

	/**
	 * EMV 4.4 Book 3 Annex A1 gives the Track 2 Discretionary Data ('9F20') the
	 * format cn, the compressed numeric of the card number, not an
	 * alphanumeric text. The declared type drives both the pretty printer and
	 * the value the terminal builds for a data object list.
	 */
	@Test
	public void testTrack2DiscretionaryDataIsCompressedNumeric() {
		Assertions.assertThat(EmvTags.TRACK2_DISCRETIONARY_DATA.getTagValueType()).isEqualTo(TagValueTypeEnum.NUMERIC);
	}

	/**
	 * EMV 4.4 Book 3 Annex C3 Table 43 added ten biometric methods to the CVM
	 * codes of EMV 4.3, from '06' facial verified offline to '0F' voice
	 * verified online. A card listing one of them in its CVM List names a
	 * method the specification defines, not an unknown one.
	 */
	@Test
	public void testBiometricCvmMethodsAreKnown() {
		Assertions.assertThat(CvmUtils.parseRule((byte) 0x06, (byte) 0x00).getMethod())
				.isEqualTo(CvmMethodEnum.FACIAL_BIOMETRIC_OFFLINE);
		Assertions.assertThat(CvmUtils.parseRule((byte) 0x0F, (byte) 0x00).getMethod())
				.isEqualTo(CvmMethodEnum.VOICE_BIOMETRIC_ONLINE);
		// A biometric method is not a PIN one
		Assertions.assertThat(CvmMethodEnum.FINGER_BIOMETRIC_ONLINE.isPin()).isFalse();
	}

	/**
	 * ISO/IEC 7816-4 section 5.1.3 lists '61XX' under normal processing: the
	 * card only says that more data is waiting. A response still carrying it
	 * has had its GET RESPONSE chain cut short, and the data it gathered is
	 * read rather than dropped.
	 */
	@Test
	public void testChainedResponseStatusIsNotAFailure() {
		Assertions.assertThat(ResponseUtils.isSucceedOrWarning(BytesUtils.fromString("5A03123456610A"))).isTrue();
		// A status word alone carries nothing to read
		Assertions.assertThat(ResponseUtils.isSucceedOrWarning(BytesUtils.fromString("610A"))).isFalse();
	}

	/**
	 * EMV 4.3 Book 1 Table 47 lists the data objects a directory entry
	 * describes its application with. The Directory Discretionary Template
	 * ('73') holds issuer proprietary data instead, so a '87' the issuer put
	 * there is not the priority of the application. The Application Selection
	 * Registered Proprietary Data is the exception, EMV 4.4 Book 1 Table 12
	 * nesting it in that very template.
	 */
	@Test
	public void testDirectoryDiscretionaryTemplateDoesNotFeedTheEntry() {
		EmvTemplate template = EmvTemplate.Builder().setProvider(new ScriptedProvider()).build();
		List<DirectoryEntry> entries = ReflectionTestUtils.invokeMethod(template, "parseDirectoryEntries",
				BytesUtils.fromString("61 12 4F 07 A0 00 00 00 04 10 10 87 01 01 73 04 87 01 07 00"));

		Assertions.assertThat(entries).hasSize(1);
		// The priority is the one of the entry, not the '87' of the template
		Assertions.assertThat(entries.get(0).getPriority()).isEqualTo(1);
	}


	/**
	 * The two constants that used to carry a misspelled name are kept as
	 * deprecated aliases of the corrected ones: a caller naming the former
	 * spelling still compiles, and the dictionary still holds one tag per set
	 * of tag bytes.
	 */
	@Test
	@SuppressWarnings("deprecation")
	public void testDeprecatedTagSpellingsStillNameTheSameTag() {
		Assertions.assertThat(EmvTags.PUNTAC_TRACK1).isSameAs(EmvTags.PUNATC_TRACK1);
		Assertions.assertThat(EmvTags.PCVC_TRACK2).isSameAs(EmvTags.PCVC3_TRACK2);
		Assertions.assertThat(EmvTags.find(BytesUtils.fromString("9F63"))).isSameAs(EmvTags.PUNATC_TRACK1);
		Assertions.assertThat(EmvTags.find(BytesUtils.fromString("9F65"))).isSameAs(EmvTags.PCVC3_TRACK2);
	}

}
