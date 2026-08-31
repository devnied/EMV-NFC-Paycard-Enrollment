package com.github.devnied.emvnfccard;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.List;

import org.fest.assertions.Assertions;
import org.junit.Test;

import com.github.devnied.emvnfccard.iso7816emv.EmvTags;
import com.github.devnied.emvnfccard.model.Application;
import com.github.devnied.emvnfccard.model.CPLC;
import com.github.devnied.emvnfccard.model.EmvCard;
import com.github.devnied.emvnfccard.model.enums.CountryCodeEnum;
import com.github.devnied.emvnfccard.model.enums.KernelEnum;
import com.github.devnied.emvnfccard.parser.EmvTemplate;
import com.github.devnied.emvnfccard.parser.impl.EmvParser;
import com.github.devnied.emvnfccard.provider.ScriptedProvider;
import com.github.devnied.emvnfccard.model.AbstractData;
import com.github.devnied.emvnfccard.model.EmvTrack1;
import com.github.devnied.emvnfccard.utils.EmvDataUtils;
import com.github.devnied.emvnfccard.utils.TrackUtils;
import com.github.devnied.emvnfccard.parser.apdu.impl.DataFactory;
import com.github.devnied.emvnfccard.utils.TagValueDecoder;
import com.github.devnied.emvnfccard.utils.CPLCUtils;
import com.github.devnied.emvnfccard.utils.ResponseUtils;
import com.github.devnied.emvnfccard.utils.TlvUtil;
import com.github.devnied.emvnfccard.utils.reflect.ReflectionTestUtils;

import fr.devnied.bitlib.BytesUtils;

/**
 * What the library does with a card that does not respect the specification.
 * <p>
 * The encodings below are those of the specification, and the card content is
 * taken from the captures of <code>src/test/resources/data</code>: the
 * responses are the real ones, shortened or given another status word, which is
 * what a malformed card does.
 * </p>
 */
public class MalformedCardToleranceTest {

	/**
	 * Response to the GPO of the VisaCardPpse capture. The '77' template
	 * announces '38' bytes and holds the Issuer Application Data, the Track 2
	 * Equivalent Data, the Application Interchange Profile, the ATC, the
	 * Application Cryptogram and the tag '9F6C'.
	 */
	private static final String GPO = "7738"//
			+ "9F1007060 11A23804004"//
			+ "571349999999999999 99D15092FFFFFFFFFFFFFF0F"//
			+ "82022000"//
			+ "9F360202 8F"//
			+ "9F2608FFFFFFFFFFFFFFFF"//
			+ "9F6C021000";

	/**
	 * PPSE FCI of the same capture, its two applications are listed inline
	 */
	private static final String PPSE_FCI = "6F 3B 84 0E 32 50 41 59 2E 53 59 53 2E 44 44 46 30 31 A5 29 BF 0C 26"
			+ " 61 10 4F 07 A0 00 00 00 42 10 10 50 02 43 42 87 01 01"
			+ " 61 12 4F 07 A0 00 00 00 03 10 10 50 04 56 49 53 41 87 01 02 90 00";

	/**
	 * Response of the same capture to the SELECT of 'A0000000421010', it carries
	 * a PDOL ('9F38') so the GPO is sent with a data field first
	 */
	private static final String SELECT_AID = "6F 37 84 07 A0 00 00 00 42 10 10 A5 2C 9F 38 18 9F 66 04 9F 02 06"
			+ " 9F 03 06 9F 1A 02 95 05 5F 2A 02 9A 03 9C 01 9F 37 04 BF 0C 0E DF 60 02 0B 1E DF 61 01 03"
			+ " 9F 4D 02 0B 1E 90 00";

	/**
	 * Track 2 Equivalent Data of the same capture
	 */
	private static final String TRACK2 = "571349999999999999 99D15092FFFFFFFFFFFFFF0F";

	/**
	 * The '38' bytes the template announces, cut after the Application
	 * Interchange Profile: '23' bytes are sent instead
	 */
	private static final String TRUNCATED_GPO = "7738"//
			+ "9F1007060 11A23804004"//
			+ "571349999999999999 99D15092FFFFFFFFFFFFFF0F"//
			+ "82022000";

	/**
	 * A template whose announced length overruns the response is read up to
	 * what the card sent: the length of a constructed object is redundant with
	 * the lengths of the objects it holds, so nothing is guessed by clamping it.
	 * A reader that gives up loses the whole response instead.
	 */
	@Test
	public void testTruncatedTemplateIsRead() {
		byte[] complete = BytesUtils.fromString(GPO + "9000");
		byte[] truncated = BytesUtils.fromString(TRUNCATED_GPO + "9000");

		// The card number and the AIP are read from both
		for (byte[] response : new byte[][] { complete, truncated }) {
			Assertions.assertThat(BytesUtils.bytesToStringNoSpace(TlvUtil.getValue(response, EmvTags.TRACK_2_EQV_DATA)))
					.isEqualTo("4999999999999999D15092FFFFFFFFFFFFFF0F");
			Assertions.assertThat(BytesUtils.bytesToStringNoSpace(TlvUtil.getValue(response,
					EmvTags.APPLICATION_INTERCHANGE_PROFILE))).isEqualTo("2000");
			Assertions.assertThat(BytesUtils.bytesToStringNoSpace(TlvUtil.getValue(response, EmvTags.ISSUER_APPLICATION_DATA)))
					.isEqualTo("06011A23804004");
		}
		// What the card did not send is not reported
		Assertions.assertThat(TlvUtil.getValue(truncated, EmvTags.APP_CRYPTOGRAM)).isNull();
	}

	/**
	 * Record of the VisaCardPpse3 capture up to its card number: the template
	 * announces 'B3' bytes and holds the Issuer Public Key Certificate, then the
	 * Application Primary Account Number, its expiration date, the issuer
	 * country and the tag '9F69'
	 */
	private static final String CERTIFICATE_RECORD = "7081B39F468190514B757E0611C0700FB54D7D6FC0073CAFE6C4D4CD54B16ADF"
			+ "7E6B8D3D1DEC5FD559B7A507F8B552BAE8F7DAB5FFFB19AB104AFD47E9A9088E9D9A8F73AC273AB71D5F9BE4579BA7F8FB4C"
			+ "96E78FD054A25E125255658A8DC0C8226D61C4F7C44570EC02E0070F6ECF8D76A38F3796BBFDA586AE605D86A489330618DD"
			+ "A23AB2900AE6A1C6F3EE21BEFA556DE3262D33";

	/**
	 * The status word closes a response, it is not part of its data field
	 * (ISO/IEC 7816-4 section 5.1). Parsing the two together lets a data object
	 * whose declared length overruns what the card sent be completed with the
	 * status bytes: the card number of the capture below, cut from 8 bytes to 6,
	 * used to be reported as the complete '4999999999999000'.
	 */
	@Test
	public void testTheStatusWordIsNotReadAsData() {
		// The record cut in the middle of its card number, two bytes short
		byte[] response = BytesUtils.fromString(CERTIFICATE_RECORD + "5A08" + "499999999999" + "9000");
		byte[] dataField = ResponseUtils.getDataField(response);

		Assertions.assertThat(TlvUtil.getValue(dataField, EmvTags.PAN)).isNull();
		// What the card did send is still read: the clamp of the truncated
		// template is what makes the certificate reachable at all
		Assertions.assertThat(BytesUtils.bytesToStringNoSpace(TlvUtil.getValue(dataField, EmvTags.ICC_PUBLIC_KEY_CERT)))
				.hasSize(288);
		// A card number one byte short is not completed either
		Assertions.assertThat(TlvUtil.getValue(
				ResponseUtils.getDataField(BytesUtils.fromString(CERTIFICATE_RECORD + "5A08" + "49999999999999" + "9000")),
				EmvTags.PAN)).isNull();
		// And the whole value when the card did send it
		Assertions.assertThat(BytesUtils.bytesToStringNoSpace(TlvUtil.getValue(
				ResponseUtils.getDataField(BytesUtils.fromString(CERTIFICATE_RECORD + "5A08" + "4999999999999999" + "9000")),
				EmvTags.PAN))).isEqualTo("4999999999999999");
	}

	/**
	 * The same record read from a card: no card number is reported rather than
	 * one the card never sent. The PPSE and the SELECT responses are those of the
	 * VisaCardPpse3 capture, the Application File Locator is built to point at
	 * the single record under test.
	 */
	@Test
	public void testNoCardNumberIsFabricatedFromTheStatusWord() throws Exception {
		ScriptedProvider provider = new ScriptedProvider()
				.on("00A404000E325041592E5359532E444446303100",
						"6F 2A 84 0E 32 50 41 59 2E 53 59 53 2E 44 44 46 30 31 A5 18 BF 0C 15"
								+ " 61 0C 4F 07 A0 00 00 00 42 10 10 87 01 01 BF 63 04 DF 20 01 80 90 00")
				.on("00A4040007A000000042101000",
						"6F 28 84 07 A0 00 00 00 42 10 10 A5 1D 87 01 01 9F 38 0C 9F 66 04 9F 02 06 9F 37 04"
								+ " 5F 2A 02 BF 0C 08 9F 5A 05 31 08 26 08 26 90 00")
				// The Application Interchange Profile of the capture, and an
				// Application File Locator for the record 1 of the SFI 1
				.onPrefix("80A80000", "77 0A 82 02 20 00 94 04 08 01 01 00 90 00")
				.on("00B2010C00", CERTIFICATE_RECORD + "5A08" + "499999999999" + "9000");

		EmvCard card = EmvTemplate.Builder().setProvider(provider)
				.setConfig(EmvTemplate.Config().setReadAt(false).setReadTransactions(false).setReadCplc(false))
				.build().readEmvCard();

		Assertions.assertThat(card.getCardNumber()).isNull();
		Assertions.assertThat(card.getApplications()).hasSize(1);
		// The application was read, it is the card number that the card did not
		// send whole
		Assertions.assertThat(card.getApplications().get(0).getOfflineDataAuthentication().isPresent()).isTrue();
	}

	/**
	 * A primitive value is not clamped: half of a Track 2 Equivalent Data is
	 * half of a card number, and returning it would report an account number
	 * the card never sent. Only the objects that are complete are reported.
	 */
	@Test
	public void testTruncatedValueIsNotFabricated() {
		// The same response cut in the middle of the Track 2 Equivalent Data
		byte[] response = BytesUtils.fromString("7738 9F1007060 11A23804004 571349999999 9000");

		Assertions.assertThat(TlvUtil.getValue(response, EmvTags.TRACK_2_EQV_DATA)).isNull();
		// The object that precedes it is still read
		Assertions.assertThat(BytesUtils.bytesToStringNoSpace(TlvUtil.getValue(response, EmvTags.ISSUER_APPLICATION_DATA)))
				.isEqualTo("06011A23804004");
	}

	/**
	 * A response that is not BER-TLV encoded at all is still reported as such:
	 * the transaction log records of the VisaCardPpse capture are formatted
	 * according to the Log Format, not as data objects, and their bytes must
	 * not be printed as a truncated template. The status word that closes the
	 * response is not part of the record and is still reported.
	 */
	@Test
	public void testDataThatIsNotTlvIsNotReadAsATruncatedTemplate() {
		String printed = TlvUtil.prettyPrintAPDUResponse(BytesUtils.fromString("00000000460040025009781403162090 00"));

		// Not one line of a data object tree
		Assertions.assertThat(printed).excludes("[UNKNOWN TAG]");
		// The status word closing the response is still worth showing
		Assertions.assertThat(printed).isEqualTo("\n90 00 -- Command successfully executed (OK)");
	}

	/**
	 * ISO/IEC 7816-4:2005 section 5.1.3 classifies '62XX' and '63XX' under
	 * "process completed" and never requires their data field to be absent, so
	 * a warning response that carries one is read. Its Table 6 gives '6282' as
	 * "end of file or record reached before reading Ne bytes", the answer of a
	 * card whose record is shorter than the length asked for by a READ RECORD
	 * with Le = '00'.
	 */
	@Test
	public void testWarningStatusesCarryData() {
		Assertions.assertThat(ResponseUtils.isSucceedOrWarning(BytesUtils.fromString("9000"))).isTrue();
		for (String warning : new String[] { "6200", "6281", "6282", "6283", "6300", "6381", "63C3" }) {
			// A record followed by the warning is read
			Assertions.assertThat(ResponseUtils.isSucceedOrWarning(BytesUtils.fromString("7000" + warning))).as(warning)
					.isTrue();
			// The status word alone carries nothing to parse
			Assertions.assertThat(ResponseUtils.isSucceedOrWarning(BytesUtils.fromString(warning))).as(warning).isFalse();
			// The strict form is unchanged, it is what the public API promises
			Assertions.assertThat(ResponseUtils.isSucceed(BytesUtils.fromString("7000" + warning))).as(warning).isFalse();
		}
		// An error stays an error, '6A83' in particular ends the reading of a
		// payment directory (EMV 4.3 Book 1 section 12.3.2). '90XX' other than
		// '9000' is proprietary, and every value of SwEnum that uses it reports
		// a failure: it is not read as a success
		for (String error : new String[] { "6A83", "6A82", "6700", "6985", "6F00", "9004", "9008", "9080" }) {
			Assertions.assertThat(ResponseUtils.isSucceedOrWarning(BytesUtils.fromString("7000" + error))).as(error).isFalse();
		}
		Assertions.assertThat(ResponseUtils.isSucceedOrWarning(null)).isFalse();
		Assertions.assertThat(ResponseUtils.isSucceedOrWarning(new byte[] { 0x62 })).isFalse();
	}

	/**
	 * A warning status with no data field must not be read as a success: the
	 * GET PROCESSING OPTIONS cascade has to see the failure to retry the
	 * command without its PDOL, and a card that answers a bare warning to the
	 * first form is only readable through the second.
	 * <p>
	 * Every byte of card content below comes from the VisaCardPpse capture, the
	 * status word of the first GPO is the adversarial part.
	 * </p>
	 */
	@Test
	public void testWarningWithoutDataDoesNotSkipTheGpoRetry() throws Exception {
		for (String firstGpoStatus : new String[] { "6283", "6700" }) {
			ScriptedProvider provider = new ScriptedProvider()
					.on("00A404000E325041592E5359532E444446303100", PPSE_FCI)
					.on("00A4040007A000000042101000", SELECT_AID)
					// The data field of a GPO holds the terminal date, time and
					// unpredictable number, so it is matched by prefix
					.onPrefix("80A8000023", firstGpoStatus)
					// The card answers the GPO sent without the PDOL
					.on("80A8000002830000", GPO + "9000");

			EmvCard card = EmvTemplate.Builder().setProvider(provider)
					.setConfig(EmvTemplate.Config().setReadAt(false).setReadTransactions(false).setReadCplc(false))
					.build().readEmvCard();

			Assertions.assertThat(provider.getReceived()).as(firstGpoStatus).contains("80A8000002830000");
			Assertions.assertThat(card.getCardNumber()).as(firstGpoStatus).isEqualTo("4999999999999999");
		}
	}

	/**
	 * The records of a payment system directory are read "until the card
	 * returns SW1 SW2 = '6A83'" (EMV 4.3 Book 1 section 12.3.2), and the same
	 * section asks the terminal to give up the directory on any other status. A
	 * card answering a bare warning to every record must not be asked for all
	 * MAX_RECORD_SFI of them.
	 */
	@Test
	public void testBareWarningEndsTheDirectoryReading() throws Exception {
		ScriptedProvider provider = new ScriptedProvider()
				.on("00A404000E315041592E5359532E444446303100",
						"6F 15 84 0E 31 50 41 59 2E 53 59 53 2E 44 44 46 30 31 A5 03 88 01 01 90 00")
				.onPrefix("00B2", "6283");
		EmvTemplate parser = EmvTemplate.Builder().setProvider(provider)
				.setConfig(EmvTemplate.Config().setContactLess(false)).build();

		byte[] fci = ReflectionTestUtils.invokeMethod(parser, "selectPaymentEnvironment");
		ReflectionTestUtils.invokeMethod(parser, "parseFCIProprietaryTemplate", fci);

		int records = 0;
		for (String command : provider.getReceived()) {
			if (command.startsWith("00B2")) {
				records++;
			}
		}
		Assertions.assertThat(records).isEqualTo(1);
	}

	/**
	 * The record of a card answering a warning status is parsed: this is the
	 * record of the VisaCardPpse capture, answered with '6282' instead of
	 * '9000'.
	 */
	@Test
	public void testRecordAnsweredWithAWarningIsRead() throws Exception {
		ScriptedProvider provider = new ScriptedProvider()
				.on("00A404000E325041592E5359532E444446303100", "6A82")
				.on("00A4040005A00000000300", "6F 10 84 07 A0 00 00 00 03 10 10 A5 05 50 03 56 49 53 90 00")
				.on("00A4040205A00000000300", "6A81")
				// The GPO gives the Application File Locator only
				.on("80A8000002830000", "77 0A 82 02 20 00 94 04 08 01 01 00 90 00")
				// ... and the record ends with a warning status
				.on("00B2010C00", "70 15 " + TRACK2 + " 62 82");

		EmvTemplate parser = EmvTemplate.Builder().setProvider(provider)
				.setConfig(EmvTemplate.Config().setReadAt(false).setReadTransactions(false).setReadCplc(false)).build();

		Assertions.assertThat(parser.readEmvCard().getCardNumber()).isEqualTo("4999999999999999");
	}

	/**
	 * The Short File Identifier of a payment directory is coded on five bits, a
	 * larger one cannot be read. The applications the response already lists
	 * are kept: a card that announces an unusable identifier is not a card
	 * without application.
	 */
	@Test
	public void testInvalidDirectorySfiKeepsTheApplicationsOfTheResponse() throws Exception {
		ScriptedProvider provider = new ScriptedProvider();
		EmvTemplate parser = EmvTemplate.Builder().setProvider(provider).build();
		// The FCI of the VisaCardPpse capture with an SFI of 31 added: the
		// entries are those of EMV Contactless Book B Table 3-2 and the '88' the
		// one EMV 4.3 Book 1 Table 43 defines for the FCI of the PSE
		byte[] fci = BytesUtils.fromString("6F 3E 84 0E 325041592E5359532E4444463031 A5 2C 88 01 1F"
				+ " BF 0C 26 61 10 4F 07 A0000000421010 50 02 4342 87 01 01"
				+ " 61 12 4F 07 A0000000031010 50 04 56495341 87 01 02 90 00");

		List<Application> apps = ReflectionTestUtils.invokeMethod(parser, "parseFCIProprietaryTemplate", fci);

		Assertions.assertThat(apps).hasSize(2);
		Assertions.assertThat(BytesUtils.bytesToStringNoSpace(apps.get(0).getAid())).isEqualTo("A0000000421010");
		Assertions.assertThat(apps.get(1).getApplicationLabel()).isEqualTo("VISA");
		// No READ RECORD was sent, the identifier could not address a file
		Assertions.assertThat(provider.getReceived()).isEmpty();
	}

	/**
	 * A directory entry describes one application (EMV 4.3 Book 1 Table 47). On a
	 * second ADF Name the reading of the entry stops: EMV Contactless Book B
	 * Table 3-2 notes that "the order of data elements within the FCI may vary",
	 * so nothing that follows the ambiguity can be attributed to either
	 * application. One application is reported, never two sharing a name.
	 */
	@Test
	public void testTwoAdfNamesInOneDirectoryEntry() {
		EmvTemplate parser = EmvTemplate.Builder().setProvider(new ScriptedProvider()).build();
		List<Application> apps = ReflectionTestUtils.invokeMethod(parser, "getApplicationTemplate", BytesUtils
				.fromString("61 1B 4F 07 A0000000421010 4F 07 A0000000031010 50 04 56495341 87 01 02"));

		Assertions.assertThat(apps).hasSize(1);
		Assertions.assertThat(BytesUtils.bytesToStringNoSpace(apps.get(0).getAid())).isEqualTo("A0000000421010");
		// The label and the priority sit after the second name, they describe
		// neither application for certain and are left out: the priority is the
		// default of an application whose indicator was never read
		Assertions.assertThat(apps.get(0).getApplicationLabel()).isNull();
		Assertions.assertThat(apps.get(0).isPriorityKnown()).isFalse();
	}

	/**
	 * The same entry with a label and a priority of its own before the second ADF
	 * Name: they belong to the first application and must not be replaced by
	 * those of the second.
	 */
	@Test
	public void testTheFirstApplicationKeepsItsOwnLabel() {
		EmvTemplate parser = EmvTemplate.Builder().setProvider(new ScriptedProvider()).build();
		List<Application> apps = ReflectionTestUtils.invokeMethod(parser, "getApplicationTemplate",
				BytesUtils.fromString("61 23 4F 07 A0000000421010 50 02 4342 87 01 01"
						+ " 4F 07 A0000000031010 50 04 56495341 87 01 02"));

		Assertions.assertThat(apps).hasSize(1);
		Assertions.assertThat(BytesUtils.bytesToStringNoSpace(apps.get(0).getAid())).isEqualTo("A0000000421010");
		Assertions.assertThat(apps.get(0).getApplicationLabel()).isEqualTo("CB");
		Assertions.assertThat(apps.get(0).getPriority()).isEqualTo(1);
	}

	/**
	 * EMV Contactless Book B codes the Kernel Identifier on one byte, or three
	 * and more, but never two. The first byte carries the kernel type and the
	 * Short Kernel Identifier whatever follows it, so a two bytes identifier is
	 * read rather than dropped.
	 */
	@Test
	public void testKernelIdentifierOfTwoBytes() {
		Application app = new Application();
		app.setKernelIdentifier(BytesUtils.fromString("0200"));

		Assertions.assertThat(app.getShortKernelId()).isEqualTo(2);
		Assertions.assertThat(app.getKernel()).isEqualTo(KernelEnum.MASTERCARD);
		Assertions.assertThat(app.getKernelIdentifier()).isEqualTo(BytesUtils.fromString("0200"));
	}

	/**
	 * The Dedicated File Name is mandatory in the response to a SELECT (EMV 4.3
	 * Book 1 Table 45). A card that leaves it out keeps the name it was
	 * selected with, which is the only name known for the application: erasing
	 * it would report an application without AID.
	 */
	@Test
	public void testSelectResponseWithoutDedicatedFileName() throws Exception {
		ScriptedProvider provider = new ScriptedProvider()
				// The FCI holds the label but no '84'
				.on("00A4040007A000000003101000", "6F 08 A5 06 50 04 56495341 90 00")
				.on("80A8000002830000", "77 0A 82 02 20 00 94 04 08 01 01 00 90 00")
				.on("00B2010C00", "70 15 " + TRACK2 + " 90 00");
		EmvTemplate template = EmvTemplate.Builder().setProvider(provider)
				.setConfig(EmvTemplate.Config().setReadAt(false).setReadTransactions(false)).build();
		EmvParser parser = new EmvParser(template);

		Application app = new Application();
		app.setAid(BytesUtils.fromString("A0000000031010"));
		Assertions.assertThat((Boolean) ReflectionTestUtils.invokeMethod(parser, "parse", app)).isTrue();

		Assertions.assertThat(BytesUtils.bytesToStringNoSpace(app.getAid())).isEqualTo("A0000000031010");
		Assertions.assertThat(app.isDedicatedFileNameMismatch()).isFalse();
		Assertions.assertThat(app.getApplicationLabel()).isEqualTo("VISA");
	}

	/**
	 * EMV 4.3 Book 1 draws the line between the two selection paths. Section
	 * 12.3.3 step 3, selecting from the list of AIDs: "The DF Name will either be
	 * identical to the AID (including the length), or the DF Name will start with
	 * the AID but will be longer". Section 12.4, the final SELECT of an ADF Name
	 * read from a directory: "the AID used in the final SELECT command exactly
	 * matches the DF Name (tag '84') returned by the ICC in the FCI".
	 * <p>
	 * The mismatch is only ever reported, never acted upon: the application is
	 * read either way.
	 * </p>
	 */
	@Test
	public void testDedicatedFileNameIsCheckedAgainstTheSelectionPath() {
		EmvParser parser = new EmvParser(EmvTemplate.Builder().setProvider(new ScriptedProvider()).build());

		// A Registered Application Provider Identifier was selected, the card
		// completes it with its own extension
		Application partial = new Application();
		partial.setAid(BytesUtils.fromString("A000000003"));
		ReflectionTestUtils.invokeMethod(parser, "checkDedicatedFileName", partial, "A0000000031010");
		Assertions.assertThat(partial.isDedicatedFileNameMismatch()).isFalse();

		// The full AID of a directory entry was selected, the name has to be it
		Application full = new Application();
		full.setAid(BytesUtils.fromString("A0000000031010"));
		ReflectionTestUtils.invokeMethod(parser, "checkDedicatedFileName", full, "A0000000031010");
		Assertions.assertThat(full.isDedicatedFileNameMismatch()).isFalse();

		ReflectionTestUtils.invokeMethod(parser, "checkDedicatedFileName", full, "A000000003101001");
		Assertions.assertThat(full.isDedicatedFileNameMismatch()).isTrue();

		// ... unless the occurrences of a partial AID are being enumerated
		Application occurrence = new Application();
		occurrence.setAid(BytesUtils.fromString("A0000000031010"));
		occurrence.setNextOccurrence(true);
		ReflectionTestUtils.invokeMethod(parser, "checkDedicatedFileName", occurrence, "A000000003101001");
		Assertions.assertThat(occurrence.isDedicatedFileNameMismatch()).isFalse();

		// A name that describes another application is reported in every case
		Application other = new Application();
		other.setAid(BytesUtils.fromString("A000000003"));
		ReflectionTestUtils.invokeMethod(parser, "checkDedicatedFileName", other, "A0000000041010");
		Assertions.assertThat(other.isDedicatedFileNameMismatch()).isTrue();
	}

	/**
	 * The issuer country is read from the numeric form (tag '5F28') first, then
	 * from the alpha3 form (tag '5F56') and the alpha2 one (tag '5F55'). ISO
	 * 3166-1 reserves the codes 900 and above for private use, so a card using
	 * one of them is only identified by its alphabetic codes.
	 */
	@Test
	public void testIssuerCountryFallsBackOnTheAlphabeticCodes() {
		EmvParser parser = new EmvParser(EmvTemplate.Builder().setProvider(new ScriptedProvider()).build());

		// 250 is France, the code the VisaCardPpse card returns
		Assertions.assertThat((CountryCodeEnum) ReflectionTestUtils.invokeMethod(parser, "extractIssuerCountryCode",
				BytesUtils.fromString("5F28020250"))).isEqualTo(CountryCodeEnum.FR);
		// 999 is not assigned, the alpha3 form answers
		Assertions.assertThat((CountryCodeEnum) ReflectionTestUtils.invokeMethod(parser, "extractIssuerCountryCode",
				BytesUtils.fromString("5F2802 0999 5F5603 465241"))).isEqualTo(CountryCodeEnum.FR);
		// ... and the alpha2 one when it is the only one present
		Assertions.assertThat((CountryCodeEnum) ReflectionTestUtils.invokeMethod(parser, "extractIssuerCountryCode",
				BytesUtils.fromString("5F5502 4652"))).isEqualTo(CountryCodeEnum.FR);
		Assertions.assertThat((CountryCodeEnum) ReflectionTestUtils.invokeMethod(parser, "extractIssuerCountryCode",
				BytesUtils.fromString("5F2802 0999"))).isNull();

		// On the application, the code the card sent is kept next to the
		// country the alphabetic form resolved
		Application app = new Application();
		ReflectionTestUtils.invokeMethod(parser, "extractExtendedData", BytesUtils.fromString("5F2802 0999 5F5603 465241"), app);
		Assertions.assertThat(app.getIssuerCountryCode()).isEqualTo(CountryCodeEnum.FR);
		Assertions.assertThat(app.getIssuerCountryCodeNumeric()).isEqualTo(999);
		Assertions.assertThat(app.getIssuerCountryCodeAlpha3()).isEqualTo("FRA");
		Assertions.assertThat(app.getIssuerCountryCodeAlpha2()).isNull();
	}

	/**
	 * The alpha2 lookup the tag '5F55' needs, which no card could resolve
	 * before: the country codes were only compared with their alpha3 form.
	 */
	@Test
	public void testCountryLookupByAlpha2() {
		Assertions.assertThat(CountryCodeEnum.getCountryByAlpha2("FR")).isEqualTo(CountryCodeEnum.FR);
		Assertions.assertThat(CountryCodeEnum.getCountryByAlpha2("fr")).isEqualTo(CountryCodeEnum.FR);
		Assertions.assertThat(CountryCodeEnum.getCountryByAlpha2("FRA")).isNull();
		Assertions.assertThat(CountryCodeEnum.getCountryByAlpha2(null)).isNull();
	}

	/**
	 * The Card Production Life Cycle data is read from a GET DATA answer of the
	 * right length: a card answering that length without the tag '9F7F' used to
	 * make the reading fail with a NullPointerException, on the very first
	 * command of a card read.
	 */
	@Test
	public void testCplcWithoutItsTag() {
		// The CPLC of the VisaCardPpse capture
		String cplc = "479050371671925948041046950007547149479202171673926416741291000000740000000000000000";
		Assertions.assertThat(CPLCUtils.parse(BytesUtils.fromString("9F7F2A" + cplc + "9000"))).isNotNull();

		// The same length, another tag
		Assertions.assertThat(CPLCUtils.parse(BytesUtils.fromString("9F7E2A" + cplc + "9000"))).isNull();
		// The same length, not TLV at all
		CPLC unreadable = CPLCUtils.parse(BytesUtils.fromString("FFFFFF" + cplc + "9000"));
		Assertions.assertThat(unreadable).isNull();
	}

	/**
	 * A packed BCD data object holding a nibble that is not a decimal digit is
	 * reported as unreadable. The bytes below are the Track 2 Equivalent Data
	 * of the MasterCardPpse capture with one nibble turned into an 'A': a
	 * shortened or hexadecimal card number handed over as if the card had sent
	 * it is worse than no card number at all.
	 */
	@Test
	public void testCorruptedPackedBcdIsNotReadAsANumber() {
		Assertions.assertThat(EmvDataUtils.getNumericValue(BytesUtils.fromString("5599999999999A99"))).isNull();
		// The same value as the card really sent it, trailing 'F' padding
		// included, is still read
		Assertions.assertThat(EmvDataUtils.getNumericValue(BytesUtils.fromString("5599999999999999")))
				.isEqualTo("5599999999999999");
	}

	/**
	 * EMV 4.4 Book 3 Annex A gives a date the format YYMMDD. A lot of cards pad
	 * the day with '00' and the last day of the month is the reading of an
	 * expiration date, but a day the month does not have is not padding: it is
	 * a date the card never encoded and rounding it would invent one.
	 */
	@Test
	public void testOutOfRangeDayIsNotRounded() {
		Assertions.assertThat(EmvDataUtils.parseDate(BytesUtils.fromString("251200"))).isNotNull();
		Assertions.assertThat(EmvDataUtils.parseDate(BytesUtils.fromString("251132"))).isNull();
		Assertions.assertThat(EmvDataUtils.parseDate(BytesUtils.fromString("251131"))).isNull();
	}

	/**
	 * A response that holds readable data objects followed by one byte that is
	 * not a data object keeps what was read of it. Only a response that never
	 * looked like BER-TLV, a transaction log record for instance, prints
	 * nothing at all.
	 */
	@Test
	public void testTrailingPaddingDoesNotDiscardWhatWasRead() {
		String printed = TlvUtil.prettyPrintAPDUResponse(BytesUtils.fromString("700C5A031234565F2403251231FF9000"));

		Assertions.assertThat(printed).contains("5A 03 -- Application Primary Account Number (PAN)");
		Assertions.assertThat(printed).contains("5F 24 03 -- Application Expiration Date");
	}

	/**
	 * The shortest data object the specification defines is a one byte tag
	 * followed by a length of '00' (EMV 4.4 Book 3 Annex B: "If L = '00', the
	 * value field is not present"). A card sending one as the last object of a
	 * record used to lose it.
	 */
	@Test
	public void testDataObjectWithAnEmptyValueIsRead() {
		Assertions.assertThat(TlvUtil.getlistTLV(BytesUtils.fromString("5A00"), EmvTags.PAN)).hasSize(1);
	}

	/**
	 * ISO/IEC 7816-4 Annex D.1 lets 'FF' bytes appear between data objects
	 * without any meaning, and a card padding its record with them must not
	 * make the objects before them unreadable.
	 */
	@Test
	public void testTrailingFfPaddingIsNotReadAsATag() {
		Assertions.assertThat(TlvUtil.getlistTLV(BytesUtils.fromString("5A03123456FFFFFF"), EmvTags.PAN)).hasSize(1);
	}

	/**
	 * A card answering a GET DATA with the value alone, instead of the value
	 * inside its data object, is answering the value. The response below is
	 * the PIN try counter of the InteractPpse capture, stripped of its tag and
	 * length.
	 */
	@Test
	public void testGetDataAnsweredWithoutItsTagIsRead() throws Exception {
		ScriptedProvider provider = new ScriptedProvider().on("80CA9F1700", "039000");
		EmvParser parser = new EmvParser(EmvTemplate.Builder().setProvider(provider).build());

		Object value = ReflectionTestUtils.invokeMethod(parser, "getData", EmvTags.PIN_TRY_COUNTER);

		Assertions.assertThat((byte[]) value).isEqualTo(new byte[] { 0x03 });
	}

	/**
	 * A counter is a binary number of one to four bytes. A card answering an
	 * empty value, or more bytes than a counter holds, leaves the counter
	 * unknown: it must not throw and end the reading of the whole card.
	 */
	@Test
	public void testCounterOfTheWrongLengthDoesNotStopTheReading() throws Exception {
		ScriptedProvider provider = new ScriptedProvider().on("80CA9F1700", "9F17009000");
		EmvParser parser = new EmvParser(EmvTemplate.Builder().setProvider(provider).build());

		Object left = ReflectionTestUtils.invokeMethod(parser, "getLeftPinTry");

		Assertions.assertThat((Integer) left).isEqualTo(AbstractData.UNKNOWN);
	}

	/**
	 * EMV 4.4 Book 3 Annex D4 states it outright: "The SFI shall be in the
	 * range 11 to 30". A Log Entry naming one of the files the specification
	 * itself governs is refused, reading it would turn ordinary application
	 * data into transactions that never happened.
	 */
	@Test
	public void testLogEntryOutsideTheLogFileRangeIsRefused() throws Exception {
		ScriptedProvider provider = new ScriptedProvider().on("80CA9F4F00", "9F4F10 9F02069F27019F1A025F2A029A039C01 9000");
		EmvTemplate template = EmvTemplate.Builder().setProvider(provider)
				.setConfig(EmvTemplate.Config().setReadTransactions(true)).build();
		EmvParser parser = new EmvParser(template);

		Object records = ReflectionTestUtils.invokeMethod(parser, "extractLogEntry", (Object) new byte[] { 0x02, 0x1E });

		Assertions.assertThat((List<?>) records).isEmpty();
		// No READ RECORD was sent against the file the specification governs
		Assertions.assertThat(provider.getReceived()).excludes("00B2011400");
	}

	/**
	 * A track whose expiration field holds a month of '55' has no readable
	 * date: it must not be rolled over into the plausible looking date a
	 * lenient parser would build, and the card number the track carries is
	 * kept all the same.
	 */
	@Test
	public void testTrackKeepsItsCardNumberWhenTheDateIsUnreadable() {
		EmvTrack1 track1 = TrackUtils.extractTrack1Data("%B4111111111111111^DOE/JOHN^99552011003?".getBytes());

		Assertions.assertThat(track1).isNotNull();
		Assertions.assertThat(track1.getCardNumber()).isEqualTo("4111111111111111");
		Assertions.assertThat(track1.getExpireDate()).isNull();
	}


	/**
	 * A card is free to answer a template inside a template, and the reader
	 * stops following at {@link TlvUtil#MAX_DEPTH}. Saying nothing at that
	 * point would read as an empty template, when the card did send something.
	 */
	@Test
	public void testNestingCapIsReported() {
		// A '70' holding a '70' holding a '70' ... 20 deep, the innermost one
		// carrying a PAN, wrapped the way EMV 4.4 Book 3 Annex B nests
		// constructed data objects
		byte[] value = BytesUtils.fromString("5A03123456");
		for (int i = 0; i < 20; i++) {
			byte[] wrapped = new byte[value.length + 2];
			wrapped[0] = 0x70;
			wrapped[1] = (byte) value.length;
			System.arraycopy(value, 0, wrapped, 2, value.length);
			value = wrapped;
		}

		String printed = TlvUtil.prettyPrintAPDUResponse(value);

		Assertions.assertThat(printed).contains("nested deeper than " + TlvUtil.MAX_DEPTH);
	}

	/**
	 * A Response Message Template Format 1 holds the answer of a command with
	 * neither tags nor lengths, so its layout is the one of the command that
	 * was sent. The printer names what it may hold and commits to neither: the
	 * response to a GENERATE AC with a 7 bytes Issuer Application Data is 18
	 * bytes long, exactly the shape of an interchange profile followed by four
	 * file locator entries.
	 */
	@Test
	public void testResponseTemplateFormat1IsNotAttributedToOneCommand() {
		List<String> lines = TagValueDecoder.decode(EmvTags.RESPONSE_MESSAGE_TEMPLATE_1,
				BytesUtils.fromString("40 00 01 FFFFFFFFFFFFFFFF 06011A23804004"));

		Assertions.assertThat(lines.get(0)).contains("layout is the one of the command that was sent");
		Assertions.assertThat(lines.toString()).contains("GET PROCESSING OPTIONS");
		Assertions.assertThat(lines.toString()).contains("GENERATE AC");
		// Neither reading is printed as a decoded value
		Assertions.assertThat(lines.toString()).excludes("byte 1 b");
		Assertions.assertThat(lines.toString()).excludes("SFI ");
	}

	/**
	 * A date the card wrote is read on a Gregorian calendar whatever the device
	 * runs on: the default calendar of a Thai locale counts the years of the
	 * Buddhist era and would shift every date of the card by five centuries.
	 */
	@Test
	public void testDatesDoNotFollowTheCalendarOfTheDevice() {
		Locale previous = Locale.getDefault();
		try {
			Locale.setDefault(new Locale("th", "TH"));
			Calendar calendar = new GregorianCalendar();
			calendar.setTime(EmvDataUtils.parseDate(BytesUtils.fromString("251231")));
			Assertions.assertThat(calendar.get(Calendar.YEAR)).isEqualTo(2025);

			// The card production life cycle date of the GlobalPlatform
			// specification is built the same way, from a year digit and a day
			// of year
			Calendar cplc = new GregorianCalendar();
			cplc.setTime(DataFactory.calculateCplcDate(new byte[] { (byte) 0x81, 0x02 }));
			Assertions.assertThat(cplc.get(Calendar.YEAR) % 10).isEqualTo(8);
			Assertions.assertThat(cplc.get(Calendar.YEAR)).isGreaterThan(1900);
		} finally {
			Locale.setDefault(previous);
		}
	}

}
