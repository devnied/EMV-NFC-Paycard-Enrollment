package com.github.devnied.emvnfccard;

import java.util.List;
import java.util.Map;

import org.fest.assertions.Assertions;
import org.junit.Test;

import com.github.devnied.emvnfccard.enums.EmvCardScheme;
import com.github.devnied.emvnfccard.exception.CommunicationException;
import com.github.devnied.emvnfccard.iso7816emv.EmvTags;
import com.github.devnied.emvnfccard.model.AbstractData;
import com.github.devnied.emvnfccard.model.Application;
import com.github.devnied.emvnfccard.model.DirectoryEntry;
import com.github.devnied.emvnfccard.model.EmvCard;
import com.github.devnied.emvnfccard.model.PaymentDirectory;
import com.github.devnied.emvnfccard.model.RecordData;
import com.github.devnied.emvnfccard.model.enums.CardStateEnum;
import com.github.devnied.emvnfccard.model.enums.IdentificationConfidenceEnum;
import com.github.devnied.emvnfccard.parser.EmvTemplate;
import com.github.devnied.emvnfccard.parser.impl.EmvParser;
import com.github.devnied.emvnfccard.provider.ScriptedProvider;
import com.github.devnied.emvnfccard.utils.reflect.ReflectionTestUtils;

import fr.devnied.bitlib.BytesUtils;

/**
 * Tests proving that everything the selection, directory and GET PROCESSING
 * OPTIONS steps read from the card lands on the model: the raw exchanges, the
 * status words, the limits that stopped a reading and the values the parser
 * used to drop.
 * <p>
 * The responses below are ISO 7816-4 / EMV structures built for the rule under
 * test, they are not a capture of a real card.
 * </p>
 *
 * @author MILLAU Julien
 *
 */
public class ParserExposureTestP1 {

	/** SELECT of the PPSE "2PAY.SYS.DDF01" */
	private static final String SELECT_PPSE = "00A404000E325041592E5359532E444446303100";

	/** SELECT of the PSE "1PAY.SYS.DDF01" */
	private static final String SELECT_PSE = "00A404000E315041592E5359532E444446303100";

	/** DF Name of the PSE, "1PAY.SYS.DDF01" */
	private static final String PSE_NAME = "315041592E5359532E4444463031";

	/** FCI of a PSE whose directory is in the SFI 1 */
	private static final String PSE_FCI = "6F 15 84 0E 31 50 41 59 2E 53 59 53 2E 44 44 46 30 31 A5 03 88 01 01 90 00";

	/** Track 2 Equivalent Data of the VisaCardPpse capture */
	private static final String TRACK2 = "571349999999999999 99D15092FFFFFFFFFFFFFF0F";

	/** Application Interchange Profile and a locator for the record 1 of the SFI 1 */
	private static final String GPO_FORMAT_2 = "77 0A 82 02 20 00 94 04 08 01 01 00 90 00";

	/** The record the locator above points to, it holds the track */
	private static final String TRACK_RECORD = "70 15 " + TRACK2 + " 90 00";

	/**
	 * A language preference and a code table stated by the directory FCI are
	 * carried over to the applications that state none, and the model says so.
	 */
	@Test
	public void testPreferencesSuppliedByTheDirectoryAreFlagged() throws CommunicationException {
		ScriptedProvider provider = new ScriptedProvider()
				// The PPSE FCI carries '5F2D' = "en" and '9F11' = 1 next to
				// its entry
				.on(SELECT_PPSE, "6F 27 84 0E 32 50 41 59 2E 53 59 53 2E 44 44 46 30 31 A5 15 5F 2D 02 65 6E 9F 11 01 01"
						+ " BF 0C 09 61 07 4F 05 A0 00 00 00 03 90 00")
				// The application FCI carries neither
				.on("00A4040005A00000000300", "6F 10 84 07 A0 00 00 00 03 10 10 A5 05 50 03 56 49 53 90 00")
				.on("80A8000002830000", GPO_FORMAT_2).on("00B2010C00", TRACK_RECORD);

		EmvCard card = EmvTemplate.Builder().setProvider(provider)
				.setConfig(EmvTemplate.Config().setReadAt(false).setReadTransactions(false).setReadAllAids(false)).build()
				.readEmvCard();

		Assertions.assertThat(card.getApplications()).hasSize(1);
		Application app = card.getApplications().get(0);
		Assertions.assertThat(app.getLanguagePreferences()).containsExactly("en");
		Assertions.assertThat(app.isLanguagePreferencesFromDirectory()).isTrue();
		Assertions.assertThat(app.getIssuerCodeTableIndex()).isEqualTo(1);
		Assertions.assertThat(app.isIssuerCodeTableIndexFromDirectory()).isTrue();

		// The directory itself reports what it carried
		Assertions.assertThat(card.getPaymentDirectories()).hasSize(1);
		PaymentDirectory directory = card.getPaymentDirectories().get(0);
		Assertions.assertThat(directory.getLanguagePreferences()).containsExactly("en");
		Assertions.assertThat(directory.getIssuerCodeTableIndex()).isEqualTo(1);
		Assertions.assertThat(directory.getDataObjects().keySet()).containsOnly("84", "5F2D", "9F11");
		Assertions.assertThat(directory.getDataObjects().get("5F2D")).isEqualTo(BytesUtils.fromString("656E"));
		Assertions.assertThat(directory.getDepth()).isZero();
		Assertions.assertThat(directory.getSelectStatusWord()).isEqualTo("9000");
	}

	/**
	 * An application that states its own preferences keeps them, and is not
	 * flagged as served by the directory.
	 */
	@Test
	public void testOwnPreferencesAreNotFlaggedAsSuppliedByTheDirectory() throws CommunicationException {
		ScriptedProvider provider = new ScriptedProvider()
				.on(SELECT_PPSE, "6F 23 84 0E 32 50 41 59 2E 53 59 53 2E 44 44 46 30 31 A5 11 5F 2D 02 65 6E"
						+ " BF 0C 09 61 07 4F 05 A0 00 00 00 03 90 00")
				// The application FCI carries '5F2D' = "fr"
				.on("00A4040005A00000000300",
						"6F 15 84 07 A0 00 00 00 03 10 10 A5 0A 50 03 56 49 53 5F 2D 02 66 72 90 00")
				.on("80A8000002830000", GPO_FORMAT_2).on("00B2010C00", TRACK_RECORD);

		EmvCard card = EmvTemplate.Builder().setProvider(provider)
				.setConfig(EmvTemplate.Config().setReadAt(false).setReadTransactions(false)).build().readEmvCard();

		Application app = card.getApplications().get(0);
		Assertions.assertThat(app.getLanguagePreferences()).containsExactly("fr");
		Assertions.assertThat(app.isLanguagePreferencesFromDirectory()).isFalse();
		Assertions.assertThat(app.isIssuerCodeTableIndexFromDirectory()).isFalse();
	}

	/**
	 * The Application Preferred Name of a directory entry is decoded again with
	 * the code table the directory names, once the directory has been read.
	 */
	@Test
	public void testDirectoryEntryPreferredNameIsDecodedWithTheDirectoryCodeTable() throws CommunicationException {
		ScriptedProvider provider = new ScriptedProvider()
				// The PSE names the code table 5 (ISO 8859-5, Cyrillic) and
				// its entry carries '9F12' = D0 D1 (the letters "аб")
				.on(SELECT_PSE, "6F 19 84 0E 31 50 41 59 2E 53 59 53 2E 44 44 46 30 31 A5 07 88 01 02 9F 11 01 05 90 00")
				// The directory is in the SFI 2, the application file in the SFI 1
				.on("00B2011400", "70 12 61 10 4F 05 A0 00 00 00 03 50 02 43 42 9F 12 02 D0 D1 90 00")
				.on("00A4040005A00000000300", "6F 10 84 07 A0 00 00 00 03 10 10 A5 05 50 03 56 49 53 90 00")
				.on("80A8000002830000", GPO_FORMAT_2).on("00B2010C00", TRACK_RECORD);

		EmvCard card = EmvTemplate.Builder().setProvider(provider)
				.setConfig(EmvTemplate.Config().setContactLess(false).setReadAt(false).setReadTransactions(false)).build()
				.readEmvCard();

		Assertions.assertThat(card.getDirectoryEntries()).hasSize(1);
		DirectoryEntry entry = card.getDirectoryEntries().get(0);
		Assertions.assertThat(entry.getRawApplicationPreferredName()).isEqualTo(BytesUtils.fromString("D0D1"));
		Assertions.assertThat(entry.getApplicationPreferredName()).isEqualTo("\u0430\u0431");
		Assertions.assertThat(entry.getDataObjects().keySet()).containsOnly("4F", "50", "9F12");
		Assertions.assertThat(entry.getRaw()).isEqualTo(BytesUtils.fromString("4F05A000000003500243429F1202D0D1"));
	}

	/**
	 * An entry (tag '61') carrying neither an ADF Name nor a DDF Name describes
	 * nothing: it is kept apart, and the directory entries are unchanged.
	 */
	@Test
	public void testEntryWithoutAnyNameIsKeptApart() {
		EmvTemplate parser = EmvTemplate.Builder().setProvider(new ScriptedProvider()).build();
		List<Application> apps = ReflectionTestUtils.invokeMethod(parser, "getApplicationTemplate",
				BytesUtils.fromString("61 04 50 02 41 42 61 09 4F 07 A0 00 00 00 03 10 10"));

		Assertions.assertThat(apps).hasSize(1);
		Assertions.assertThat(parser.getCard().getDirectoryEntries()).hasSize(1);
		Assertions.assertThat(parser.getCard().getIgnoredDirectoryEntries()).hasSize(1);
		DirectoryEntry ignored = parser.getCard().getIgnoredDirectoryEntries().get(0);
		Assertions.assertThat(ignored.getApplicationLabel()).isEqualTo("AB");
		Assertions.assertThat(ignored.getAid()).isNull();
		Assertions.assertThat(ignored.getDdfName()).isNull();
		Assertions.assertThat(ignored.getRaw()).isEqualTo(BytesUtils.fromString("50024142"));
		Assertions.assertThat(ignored.getDataObjects().keySet()).containsOnly("50");
	}

	/**
	 * The second and following ADF Names of a malformed entry are kept apart,
	 * the first one alone is the AID and nothing after the ambiguity is
	 * attributed (behaviour of the existing getters unchanged).
	 */
	@Test
	public void testAdditionalAdfNamesAreKept() {
		EmvTemplate parser = EmvTemplate.Builder().setProvider(new ScriptedProvider()).build();
		List<DirectoryEntry> entries = ReflectionTestUtils.invokeMethod(parser, "parseDirectoryEntries", BytesUtils
				.fromString("61 24 4F 07 A0000000421010 4F 07 A0000000031010 50 04 56495341 87 01 02 4F 07 A0000000041010"));

		Assertions.assertThat(entries).hasSize(1);
		DirectoryEntry entry = entries.get(0);
		Assertions.assertThat(BytesUtils.bytesToStringNoSpace(entry.getAid())).isEqualTo("A0000000421010");
		Assertions.assertThat(entry.getAdditionalAdfNames()).hasSize(2);
		Assertions.assertThat(BytesUtils.bytesToStringNoSpace(entry.getAdditionalAdfNames().get(0))).isEqualTo("A0000000031010");
		Assertions.assertThat(BytesUtils.bytesToStringNoSpace(entry.getAdditionalAdfNames().get(1))).isEqualTo("A0000000041010");
		Assertions.assertThat(entry.getApplicationLabel()).isNull();
		Assertions.assertThat(entry.isPriorityKnown()).isFalse();
		// The flat view still holds everything, first occurrence of a tag kept
		Assertions.assertThat(entry.getDataObjects().keySet()).containsOnly("4F", "50", "87");
		Assertions.assertThat(BytesUtils.bytesToStringNoSpace(entry.getDataObjects().get("4F"))).isEqualTo("A0000000421010");
	}

	/**
	 * A Short File Identifier out of the range the terminal can address is
	 * reported as sent, and the directory file is not read.
	 */
	@Test
	public void testInvalidDirectorySfiIsReported() throws CommunicationException {
		ScriptedProvider provider = new ScriptedProvider();
		EmvTemplate parser = EmvTemplate.Builder().setProvider(provider).build();
		byte[] fci = BytesUtils.fromString("6F 3E 84 0E 325041592E5359532E4444463031 A5 2C 88 01 1F"
				+ " BF 0C 26 61 10 4F 07 A0000000421010 50 02 4342 87 01 01"
				+ " 61 12 4F 07 A0000000031010 50 04 56495341 87 01 02");

		List<Application> apps = ReflectionTestUtils.invokeMethod(parser, "parseFCIProprietaryTemplate", fci);

		Assertions.assertThat(apps).hasSize(2);
		Assertions.assertThat(provider.getReceived()).isEmpty();
		Assertions.assertThat(parser.getCard().getPaymentDirectories()).hasSize(1);
		PaymentDirectory directory = parser.getCard().getPaymentDirectories().get(0);
		Assertions.assertThat(directory.getRawSfi()).isEqualTo(BytesUtils.fromString("1F"));
		Assertions.assertThat(directory.getSfi()).isEqualTo(31);
		Assertions.assertThat(directory.isSfiValid()).isFalse();
		Assertions.assertThat(directory.getRecords()).isEmpty();
		Assertions.assertThat(directory.getEndStatusWord()).isNull();
		Assertions.assertThat(directory.isRecordLimitReached()).isFalse();
		Assertions.assertThat(BytesUtils.bytesToStringNoSpace(directory.getName())).isEqualTo("325041592E5359532E4444463031");
		Assertions.assertThat(directory.getRawFci()).isEqualTo(fci);
		// The entries are not part of the directory data objects
		Assertions.assertThat(directory.getDataObjects().keySet()).containsOnly("84", "88");

		// A SFI too long to be decoded stays unknown, its raw value is kept
		parser = EmvTemplate.Builder().setProvider(new ScriptedProvider()).build();
		ReflectionTestUtils.invokeMethod(parser, "parseFCIProprietaryTemplate",
				BytesUtils.fromString("6F 0B 84 00 A5 07 88 05 01 02 03 04 05"));
		directory = parser.getCard().getPaymentDirectories().get(0);
		Assertions.assertThat(directory.getRawSfi()).isEqualTo(BytesUtils.fromString("0102030405"));
		Assertions.assertThat(directory.getSfi()).isEqualTo(AbstractData.UNKNOWN);
		Assertions.assertThat(directory.isSfiValid()).isFalse();
	}

	/**
	 * A warning status that carries no data ends the directory reading, the
	 * status is reported as the end of the directory.
	 */
	@Test
	public void testBareWarningEndingTheDirectoryIsReported() throws CommunicationException {
		ScriptedProvider provider = new ScriptedProvider().on(SELECT_PSE, PSE_FCI).onPrefix("00B2", "6283");
		EmvTemplate parser = EmvTemplate.Builder().setProvider(provider)
				.setConfig(EmvTemplate.Config().setContactLess(false)).build();

		byte[] fci = ReflectionTestUtils.invokeMethod(parser, "selectPaymentEnvironment");
		ReflectionTestUtils.invokeMethod(parser, "parseFCIProprietaryTemplate", fci);

		PaymentDirectory directory = parser.getCard().getPaymentDirectories().get(0);
		Assertions.assertThat(directory.isSfiValid()).isTrue();
		Assertions.assertThat(directory.getSfi()).isEqualTo(1);
		Assertions.assertThat(directory.getRecords()).hasSize(1);
		Assertions.assertThat(directory.getRecords().get(0).getSfi()).isEqualTo(1);
		Assertions.assertThat(directory.getRecords().get(0).getRecordNumber()).isEqualTo(1);
		Assertions.assertThat(directory.getRecords().get(0).getRaw()).isEmpty();
		Assertions.assertThat(directory.getRecords().get(0).getStatusWord()).isEqualTo("6283");
		Assertions.assertThat(directory.getEndStatusWord()).isEqualTo("6283");
		Assertions.assertThat(directory.isRecordLimitReached()).isFalse();
	}

	/**
	 * A directory record answered with a warning status and data is read, and
	 * reported with its status word.
	 */
	@Test
	public void testDirectoryRecordAnsweredWithAWarningIsReported() throws CommunicationException {
		ScriptedProvider provider = new ScriptedProvider().on(SELECT_PSE, PSE_FCI)
				.on("00B2010C00", "70 0B 61 09 4F 07 A0 00 00 00 03 10 10 62 82");
		EmvTemplate parser = EmvTemplate.Builder().setProvider(provider)
				.setConfig(EmvTemplate.Config().setContactLess(false)).build();

		byte[] fci = ReflectionTestUtils.invokeMethod(parser, "selectPaymentEnvironment");
		List<Application> apps = ReflectionTestUtils.invokeMethod(parser, "parseFCIProprietaryTemplate", fci);

		Assertions.assertThat(apps).hasSize(1);
		PaymentDirectory directory = parser.getCard().getPaymentDirectories().get(0);
		Assertions.assertThat(directory.getRecords()).hasSize(2);
		RecordData record = directory.getRecords().get(0);
		Assertions.assertThat(record.getStatusWord()).isEqualTo("6282");
		Assertions.assertThat(record.getRaw()).isEqualTo(BytesUtils.fromString("700B61094F07A0000000031010"));
		Assertions.assertThat(directory.getRecords().get(1).getStatusWord()).isEqualTo("6A83");
		Assertions.assertThat(directory.getEndStatusWord()).isEqualTo("6A83");
	}

	/**
	 * A card that never answers the end of its directory is stopped by the
	 * record limit, which is reported.
	 */
	@Test
	public void testDirectoryRecordLimitIsReported() throws CommunicationException {
		ScriptedProvider provider = new ScriptedProvider().on(SELECT_PSE, PSE_FCI)
				// Every record holds the same entry
				.onPrefix("00B2", "70 0B 61 09 4F 07 A0 00 00 00 03 10 10 90 00");
		EmvTemplate parser = EmvTemplate.Builder().setProvider(provider)
				.setConfig(EmvTemplate.Config().setContactLess(false)).build();

		byte[] fci = ReflectionTestUtils.invokeMethod(parser, "selectPaymentEnvironment");
		ReflectionTestUtils.invokeMethod(parser, "parseFCIProprietaryTemplate", fci);

		PaymentDirectory directory = parser.getCard().getPaymentDirectories().get(0);
		Assertions.assertThat(directory.getRecords()).hasSize(EmvTemplate.MAX_RECORD_SFI);
		Assertions.assertThat(directory.isRecordLimitReached()).isTrue();
		Assertions.assertThat(directory.getEndStatusWord()).isNull();
	}

	/**
	 * A chain of Directory Definition Files deeper than MAX_DDF_DEPTH is cut,
	 * the names left unread and the limit are reported.
	 */
	@Test
	public void testDirectoryDepthLimitIsReported() throws CommunicationException {
		ScriptedProvider provider = new ScriptedProvider().on(SELECT_PSE, PSE_FCI)
				// The PSE lists the DDF 1, the DDF n lists the DDF n+1
				.on("00B2010C00", "70 0B 61 09 9D 07 A0 00 00 00 03 00 01 90 00");
		for (int i = 1; i <= EmvTemplate.MAX_DDF_DEPTH + 1; i++) {
			String name = String.format("A00000000300%02d", i);
			String next = String.format("A00000000300%02d", i + 1);
			// Each DDF puts its directory in the SFI i + 1
			provider.on("00A4040007" + name + "00",
					"6F 0E 84 07 " + name + " A5 03 88 01 " + String.format("%02X", i + 1) + " 90 00");
			provider.on(String.format("00B201%02X00", (i + 1) << 3 | 4), "70 0B 61 09 9D 07 " + next + " 90 00");
		}
		EmvTemplate parser = EmvTemplate.Builder().setProvider(provider)
				.setConfig(EmvTemplate.Config().setContactLess(false)).build();

		byte[] fci = ReflectionTestUtils.invokeMethod(parser, "selectPaymentEnvironment");
		ReflectionTestUtils.invokeMethod(parser, "parseFCIProprietaryTemplate", fci);

		EmvCard card = parser.getCard();
		Assertions.assertThat(card.isDirectoryDepthLimitReached()).isTrue();
		Assertions.assertThat(card.isDirectoryCountLimitReached()).isFalse();
		Assertions.assertThat(card.getUnreadDdfNames()).hasSize(1);
		Assertions.assertThat(BytesUtils.bytesToStringNoSpace(card.getUnreadDdfNames().get(0)))
				.isEqualTo(String.format("A00000000300%02d", EmvTemplate.MAX_DDF_DEPTH + 1));
		// The PSE and the DDF 1 to MAX_DDF_DEPTH were selected, in that order
		Assertions.assertThat(card.getPaymentDirectories()).hasSize(EmvTemplate.MAX_DDF_DEPTH + 1);
		for (int i = 0; i <= EmvTemplate.MAX_DDF_DEPTH; i++) {
			PaymentDirectory directory = card.getPaymentDirectories().get(i);
			Assertions.assertThat(directory.getDepth()).isEqualTo(i);
			Assertions.assertThat(directory.getSfi()).isEqualTo(i + 1);
			// The PSE was parsed through the entry point that takes the FCI
			// alone, its selection outcome is unknown there
			Assertions.assertThat(directory.getSelectStatusWord()).isEqualTo(i == 0 ? null : "9000");
			Assertions.assertThat(directory.getSubDirectoryNames()).hasSize(1);
		}
		Assertions.assertThat(BytesUtils.bytesToStringNoSpace(card.getPaymentDirectories().get(1).getName()))
				.isEqualTo("A0000000030001");
	}

	/**
	 * A directory listing more sub directories than MAX_DDF_COUNT is cut, the
	 * names left unread and the limit are reported; a DDF whose SELECT fails
	 * is reported too.
	 */
	@Test
	public void testDirectoryCountLimitIsReported() throws CommunicationException {
		// The visited set holds the PSE itself, so MAX_DDF_COUNT - 1 DDF are
		// selected and the two last ones are left out
		int listed = EmvTemplate.MAX_DDF_COUNT + 1;
		StringBuilder record = new StringBuilder();
		for (int i = 1; i <= listed; i++) {
			record.append("61 09 9D 07 ").append(String.format("A00000000300%02d ", i));
		}
		// 11 bytes per entry, the template takes the long form length
		ScriptedProvider provider = new ScriptedProvider().on(SELECT_PSE, PSE_FCI)
				.on("00B2010C00", "70 81 " + String.format("%02X", listed * 11) + " " + record + "90 00");
		// None of the DDF is selectable: '6A82' answered to every SELECT
		EmvTemplate parser = EmvTemplate.Builder().setProvider(provider)
				.setConfig(EmvTemplate.Config().setContactLess(false)).build();

		byte[] fci = ReflectionTestUtils.invokeMethod(parser, "selectPaymentEnvironment");
		ReflectionTestUtils.invokeMethod(parser, "parseFCIProprietaryTemplate", fci);

		EmvCard card = parser.getCard();
		Assertions.assertThat(card.isDirectoryCountLimitReached()).isTrue();
		Assertions.assertThat(card.isDirectoryDepthLimitReached()).isFalse();
		Assertions.assertThat(card.getUnreadDdfNames()).hasSize(2);
		Assertions.assertThat(BytesUtils.bytesToStringNoSpace(card.getUnreadDdfNames().get(0))).isEqualTo("A0000000030016");
		Assertions.assertThat(BytesUtils.bytesToStringNoSpace(card.getUnreadDdfNames().get(1))).isEqualTo("A0000000030017");
		// The PSE plus every DDF whose SELECT was sent, refused or not
		Assertions.assertThat(card.getPaymentDirectories()).hasSize(EmvTemplate.MAX_DDF_COUNT);
		Assertions.assertThat(card.getPaymentDirectories().get(0).getSubDirectoryNames()).hasSize(listed);
		PaymentDirectory refused = card.getPaymentDirectories().get(1);
		Assertions.assertThat(refused.getDepth()).isEqualTo(1);
		Assertions.assertThat(BytesUtils.bytesToStringNoSpace(refused.getName())).isEqualTo("A0000000030001");
		Assertions.assertThat(refused.getSelectStatusWord()).isEqualTo("6A82");
		Assertions.assertThat(refused.getRawFci()).isNull();
		Assertions.assertThat(refused.getRecords()).isEmpty();
	}

	/**
	 * When both GET PROCESSING OPTIONS fail the parser falls back on the record
	 * 1 of the SFI 1, and the model says which response was parsed.
	 */
	@Test
	public void testGpoReplacedByReadRecordIsReported() throws CommunicationException {
		ScriptedProvider provider = new ScriptedProvider().on(SELECT_PPSE, "6A82")
				// The FCI carries a PDOL asking for the TTQ
				.on("00A4040005A00000000300",
						"6F 16 84 07 A0 00 00 00 03 10 10 A5 0B 50 03 56 49 53 9F 38 03 9F 66 04 90 00")
				// The GPO built from the PDOL, then the one without it, fail
				.onPrefix("80A8000006", "6700").on("80A8000002830000", "6985")
				// The record 1 of the SFI 1 answers the track
				.on("00B2010C00", TRACK_RECORD);

		EmvCard card = EmvTemplate.Builder().setProvider(provider)
				.setConfig(EmvTemplate.Config().setReadAt(false).setReadTransactions(false).setReadAllAids(false)).build()
				.readEmvCard();

		Assertions.assertThat(card.getCardNumber()).isEqualTo("4999999999999999");
		Application app = card.getApplications().get(0);
		Assertions.assertThat(app.getRawPdol()).isEqualTo(BytesUtils.fromString("9F6604"));
		Assertions.assertThat(app.getPdol()).hasSize(1);
		Assertions.assertThat(app.isGpoRetriedWithoutPdol()).isTrue();
		Assertions.assertThat(app.isGpoReplacedByReadRecord()).isTrue();
		Assertions.assertThat(app.getGpoStatusWord()).isEqualTo("9000");
		Assertions.assertThat(app.getRawGpoResponse()).isEqualTo(BytesUtils.fromString("70 15 " + TRACK2));
		// The last GPO sent had an empty command template
		Assertions.assertThat(app.getGpoCommandTemplate()).isEqualTo(BytesUtils.fromString("8300"));
		Assertions.assertThat(app.getPdolTerminalValues()).isEmpty();
		// The fallback record is reported like a record of the locator
		Assertions.assertThat(app.getRecords()).hasSize(1);
		Assertions.assertThat(app.getRecords().get(0).getSfi()).isEqualTo(1);
		Assertions.assertThat(app.getRecords().get(0).getRecordNumber()).isEqualTo(1);
		Assertions.assertThat(app.getRecords().get(0).getStatusWord()).isEqualTo("9000");
		Assertions.assertThat(app.getRawAfl()).isNull();
		Assertions.assertThat(app.getAfl()).isEmpty();
		Assertions.assertThat(card.isReadWithAid()).isTrue();
		Assertions.assertThat(card.getPaymentEnvironmentStatusWord()).isEqualTo("6A82");
		Assertions.assertThat(card.getPaymentDirectories()).isEmpty();
	}

	/**
	 * When even the fallback record fails the status word of that last attempt
	 * is reported.
	 */
	@Test
	public void testFailedGpoStatusWordIsReported() throws CommunicationException {
		ScriptedProvider provider = new ScriptedProvider()
				.on("00A4040007A000000003101000", "6F 10 84 07 A0 00 00 00 03 10 10 A5 05 50 03 56 49 53 90 00")
				.on("80A8000002830000", "6985").on("00B2010C00", "6A83");
		EmvTemplate template = EmvTemplate.Builder().setProvider(provider)
				.setConfig(EmvTemplate.Config().setReadAt(false).setReadTransactions(false)).build();
		EmvParser parser = new EmvParser(template);

		Application app = new Application();
		app.setAid(BytesUtils.fromString("A0000000031010"));
		Assertions.assertThat((Boolean) ReflectionTestUtils.invokeMethod(parser, "parse", app)).isFalse();

		Assertions.assertThat(app.getSelectStatusWord()).isEqualTo("9000");
		Assertions.assertThat(app.getRawFci()).isEqualTo(BytesUtils.fromString("6F 10 84 07 A0 00 00 00 03 10 10 A5 05 50 03 56 49 53"));
		Assertions.assertThat(app.getRawPdol()).isNull();
		Assertions.assertThat(app.getPdol()).isEmpty();
		Assertions.assertThat(app.isGpoRetriedWithoutPdol()).isFalse();
		Assertions.assertThat(app.isGpoReplacedByReadRecord()).isTrue();
		Assertions.assertThat(app.getGpoStatusWord()).isEqualTo("6A83");
		Assertions.assertThat(app.getRawGpoResponse()).isNull();
		Assertions.assertThat(app.getRecords()).hasSize(1);
		Assertions.assertThat(app.getRecords().get(0).getStatusWord()).isEqualTo("6A83");
		Assertions.assertThat(app.getRecords().get(0).getRaw()).isEmpty();
	}

	/**
	 * The terminal values sent for each PDOL entry are reported, and a PDOL
	 * asking for more than a command can carry leaves the template unsent.
	 */
	@Test
	public void testPdolTerminalValuesAreReported() throws CommunicationException {
		ScriptedProvider provider = new ScriptedProvider();
		EmvTemplate template = EmvTemplate.Builder().setProvider(provider).build();
		EmvParser parser = new EmvParser(template);
		Application app = new Application();

		// TTQ, amount, country code, currency code, unpredictable number
		ReflectionTestUtils.invokeMethod(parser, "getGetProcessingOptions",
				BytesUtils.fromString("9F6604 9F0206 9F1A02 5F2A02 9F3704"), app);

		Map<String, byte[]> values = app.getPdolTerminalValues();
		Assertions.assertThat(new java.util.ArrayList<String>(values.keySet())).containsExactly("9F66", "9F02", "9F1A", "5F2A", "9F37");
		Assertions.assertThat(values.get("9F66")).hasSize(4);
		Assertions.assertThat(values.get("9F02")).hasSize(6);
		// The default terminal is located in France and pays in euro
		Assertions.assertThat(BytesUtils.bytesToStringNoSpace(values.get("9F1A"))).isEqualTo("0250");
		Assertions.assertThat(BytesUtils.bytesToStringNoSpace(values.get("5F2A"))).isEqualTo("0978");
		Assertions.assertThat(values.get("9F37")).hasSize(4);
		Assertions.assertThat(app.getGpoCommandTemplate()).hasSize(2 + 4 + 6 + 2 + 2 + 4);
		Assertions.assertThat(BytesUtils.bytesToStringNoSpace(app.getGpoCommandTemplate())).startsWith("8312");
		// The command sent carries that template
		Assertions.assertThat(provider.getReceived().get(0))
				.isEqualTo("80A8000014" + BytesUtils.bytesToStringNoSpace(app.getGpoCommandTemplate()) + "00");

		// A PDOL asking for 510 bytes cannot be sent: the values are built,
		// the template is not
		Application tooLong = new Application();
		byte[] response = ReflectionTestUtils.invokeMethod(parser, "getGetProcessingOptions",
				BytesUtils.fromString("9F4CFF 9F4DFF"), tooLong);
		Assertions.assertThat(BytesUtils.bytesToStringNoSpace(response)).isEqualTo("6700");
		Assertions.assertThat(tooLong.getGpoCommandTemplate()).isNull();
		Assertions.assertThat(new java.util.ArrayList<String>(tooLong.getPdolTerminalValues().keySet())).containsExactly("9F4C", "9F4D");
	}

	/**
	 * A track 2 the card sent but that cannot be parsed is kept as it is, no
	 * card number is made up from it.
	 */
	@Test
	public void testUnparsedTrack2IsKept() throws CommunicationException {
		ScriptedProvider provider = new ScriptedProvider()
				.on(SELECT_PPSE, "6F 20 84 0E 32 50 41 59 2E 53 59 53 2E 44 44 46 30 31 A5 0E BF 0C 0B"
						+ " 61 09 4F 07 A0 00 00 00 03 10 10 90 00")
				.on("00A4040007A000000003101000", "6F 10 84 07 A0 00 00 00 03 10 10 A5 05 50 03 56 49 53 90 00")
				.on("80A8000002830000", GPO_FORMAT_2)
				// A Track 2 Equivalent Data that is not a track at all
				.on("00B2010C00", "70 07 57 05 AA BB CC DD EE 90 00");

		EmvCard card = EmvTemplate.Builder().setProvider(provider)
				.setConfig(EmvTemplate.Config().setReadAt(false).setReadTransactions(false).setReadAllAids(false)).build()
				.readEmvCard();

		Assertions.assertThat(card.getTrack2()).isNull();
		Assertions.assertThat(card.getCardNumber()).isNull();
		Assertions.assertThat(card.getState()).isEqualTo(CardStateEnum.LOCKED);
		Assertions.assertThat(card.getLockedStatusWord()).isEqualTo("9000");
		Assertions.assertThat(card.getUnparsedTrack2()).isEqualTo(BytesUtils.fromString("AABBCCDDEE"));
		Assertions.assertThat(card.getUnparsedTrack1()).isNull();
		// The record itself is reported
		Application app = card.getApplications().get(0);
		Assertions.assertThat(app.getRawAfl()).isEqualTo(BytesUtils.fromString("08010100"));
		Assertions.assertThat(app.getAfl()).hasSize(1);
		Assertions.assertThat(app.getRecordsRead()).isEqualTo(1);
		Assertions.assertThat(app.getRecords()).hasSize(1);
		Assertions.assertThat(app.getRecords().get(0).getRaw()).isEqualTo(BytesUtils.fromString("70075705AABBCCDDEE"));
		Assertions.assertThat(app.isAflTruncated()).isFalse();
	}

	/**
	 * A track 1 whose expiration field is unused ('^') cannot be parsed into
	 * an account, its raw value is kept.
	 */
	@Test
	public void testUnparsedTrack1IsKept() {
		EmvTemplate template = EmvTemplate.Builder().setProvider(new ScriptedProvider()).build();
		EmvParser parser = new EmvParser(template);
		EmvCard card = new EmvCard();
		byte[] track1 = "B4000000000000000^DOE/JOHN^^^000000".getBytes();
		byte[] record = BytesUtils.fromString("56 " + String.format("%02X", track1.length) + " "
				+ BytesUtils.bytesToStringNoSpace(track1));

		Assertions.assertThat((Boolean) ReflectionTestUtils.invokeMethod(parser, "extractTrackData", card, record)).isFalse();
		Assertions.assertThat(card.getTrack1()).isNull();
		Assertions.assertThat(card.getUnparsedTrack1()).isEqualTo(track1);
		Assertions.assertThat(card.getUnparsedTrack2()).isNull();

		// The first unparsable track is the one kept
		byte[] other = BytesUtils.fromString("56 03 41 42 43");
		ReflectionTestUtils.invokeMethod(parser, "extractTrackData", card, other);
		Assertions.assertThat(card.getUnparsedTrack1()).isEqualTo(track1);
	}

	/**
	 * A track discretionary data object received while the card has no track
	 * to attach it to is kept on the card.
	 */
	@Test
	public void testOrphanTrackDiscretionaryDataIsKept() {
		EmvTemplate template = EmvTemplate.Builder().setProvider(new ScriptedProvider()).build();
		EmvParser parser = new EmvParser(template);
		EmvCard card = new EmvCard();

		boolean read = ReflectionTestUtils.invokeMethod(parser, "extractTrackData", card,
				BytesUtils.fromString("70 1A 9F1F0D30313030303030303030303033 9F20070100000000003F"));

		Assertions.assertThat(read).isFalse();
		Assertions.assertThat(card.getTrack1()).isNull();
		Assertions.assertThat(card.getTrack2()).isNull();
		Assertions.assertThat(BytesUtils.bytesToStringNoSpace(card.getOrphanTrack1DiscretionaryData()))
				.isEqualTo("30313030303030303030303033");
		Assertions.assertThat(BytesUtils.bytesToStringNoSpace(card.getOrphanTrack2DiscretionaryData()))
				.isEqualTo("0100000000003F");

		// Once a track exists the data object is attached to it, not to the card
		EmvCard other = new EmvCard();
		ReflectionTestUtils.invokeMethod(parser, "extractTrackData", other, BytesUtils.fromString("70 15 " + TRACK2));
		ReflectionTestUtils.invokeMethod(parser, "extractTrackData", other, BytesUtils.fromString("9F20070100000000003F"));
		Assertions.assertThat(other.getOrphanTrack2DiscretionaryData()).isNull();
		Assertions.assertThat(other.getTrack2().getRawDiscretionaryData()).isNotNull();
	}

	/**
	 * A GET DATA '9F7F' refused by the card leaves the CPLC null, the raw
	 * answer and its status word are still reported.
	 */
	@Test
	public void testRefusedCplcIsReported() throws CommunicationException {
		ScriptedProvider provider = new ScriptedProvider().on("80CA9F7F00", "6985").on(SELECT_PPSE, "6A82");

		EmvCard card = EmvTemplate.Builder().setProvider(provider)
				.setConfig(EmvTemplate.Config().setReadCplc(true).setReadAt(false)).build().readEmvCard();

		Assertions.assertThat(card.getCplc()).isNull();
		Assertions.assertThat(card.getRawCplcResponse()).isEqualTo(BytesUtils.fromString("6985"));
		Assertions.assertThat(card.getCplcStatusWord()).isEqualTo("6985");
		Assertions.assertThat(card.isAtLookedUp()).isFalse();
		Assertions.assertThat(card.getRawAt()).isNull();
		Assertions.assertThat(card.getState()).isEqualTo(CardStateEnum.UNKNOWN);
		// The identification is computed on every card, even an unknown one
		Assertions.assertThat(card.getIdentification()).isNotNull();
		Assertions.assertThat(card.getIdentification().getConfidence()).isEqualTo(IdentificationConfidenceEnum.NONE);
	}

	/**
	 * The name the card answers (tag '84') is reported as it is, null when the
	 * card sent none, and the AID asked for stays known.
	 */
	@Test
	public void testSelectionOutcomeIsReported() throws CommunicationException {
		ScriptedProvider provider = new ScriptedProvider()
				// The FCI holds the label but no '84'
				.on("00A4040007A000000003101000", "6F 08 A5 06 50 04 56495341 90 00")
				.on("80A8000002830000", GPO_FORMAT_2).on("00B2010C00", TRACK_RECORD);
		EmvTemplate template = EmvTemplate.Builder().setProvider(provider)
				.setConfig(EmvTemplate.Config().setReadAt(false).setReadTransactions(false)).build();
		EmvParser parser = new EmvParser(template);

		Application app = new Application();
		app.setAid(BytesUtils.fromString("A0000000031010"));
		Assertions.assertThat((Boolean) ReflectionTestUtils.invokeMethod(parser, "parse", app)).isTrue();

		Assertions.assertThat(BytesUtils.bytesToStringNoSpace(app.getAid())).isEqualTo("A0000000031010");
		Assertions.assertThat(BytesUtils.bytesToStringNoSpace(app.getRequestedAid())).isEqualTo("A0000000031010");
		Assertions.assertThat(app.getDedicatedFileName()).isNull();
		Assertions.assertThat(app.getSelectStatusWord()).isEqualTo("9000");
		Assertions.assertThat(app.getAidScheme()).isEqualTo(EmvCardScheme.VISA);
		Assertions.assertThat(template.getCard().getAidType()).isEqualTo(EmvCardScheme.VISA);
		Assertions.assertThat(BytesUtils.bytesToStringNoSpace(template.getCard().getTypeAid())).isEqualTo("A0000000031010");
		Assertions.assertThat(template.getCard().getType()).isEqualTo(EmvCardScheme.VISA);

		// A partial AID: the name the card answers is longer than the one
		// asked for, both are reported
		provider = new ScriptedProvider()
				.on("00A4040005A00000000300", "6F 10 84 07 A0 00 00 00 03 10 10 A5 05 50 03 56 49 53 90 00")
				.on("80A8000002830000", GPO_FORMAT_2).on("00B2010C00", TRACK_RECORD);
		template = EmvTemplate.Builder().setProvider(provider)
				.setConfig(EmvTemplate.Config().setReadAt(false).setReadTransactions(false)).build();
		parser = new EmvParser(template);
		app = new Application();
		app.setAid(BytesUtils.fromString("A000000003"));
		Assertions.assertThat((Boolean) ReflectionTestUtils.invokeMethod(parser, "parse", app)).isTrue();
		Assertions.assertThat(BytesUtils.bytesToStringNoSpace(app.getRequestedAid())).isEqualTo("A000000003");
		Assertions.assertThat(BytesUtils.bytesToStringNoSpace(app.getDedicatedFileName())).isEqualTo("A0000000031010");
		Assertions.assertThat(BytesUtils.bytesToStringNoSpace(app.getAid())).isEqualTo("A0000000031010");
	}

	/**
	 * The primitive data objects of a structure are collected recursively, the
	 * content of the skipped templates left out, the first occurrence kept,
	 * and a malformed structure never throws.
	 */
	@Test
	public void testPrimitiveDataObjectsAreCollected() {
		Map<String, byte[]> objects = EmvTemplate.collectPrimitiveDataObjects(
				BytesUtils.fromString("6F 1C 84 02 A0 00 A5 13 BF 0C 0C 61 05 4F 03 A0 00 00 9F 4D 02 0B 1E 9F 11 01 01 84 01 FF"),
				EmvTags.APPLICATION_TEMPLATE);
		Assertions.assertThat(new java.util.ArrayList<String>(objects.keySet())).containsExactly("84", "9F4D", "9F11");
		Assertions.assertThat(objects.get("84")).isEqualTo(BytesUtils.fromString("A000"));

		// Without a skipped template the entry content is collected too
		Assertions.assertThat(new java.util.ArrayList<String>(EmvTemplate.collectPrimitiveDataObjects(
				BytesUtils.fromString("BF 0C 07 61 05 4F 03 A0 00 00")).keySet())).containsExactly("4F");

		Assertions.assertThat(EmvTemplate.collectPrimitiveDataObjects(null)).isEmpty();
		Assertions.assertThat(EmvTemplate.collectPrimitiveDataObjects(new byte[0])).isEmpty();
		// A truncated value is not collected, the objects before it are
		Assertions.assertThat(EmvTemplate.collectPrimitiveDataObjects(BytesUtils.fromString("50 02 41 42 57 10 AA")).keySet())
				.containsOnly("50");
	}

	/**
	 * The status word helper reads the two bytes that close a response and
	 * never throws.
	 */
	@Test
	public void testStatusWordHelper() {
		Assertions.assertThat(EmvTemplate.getStatusWord(BytesUtils.fromString("6F 03 84 01 AA 6A 83"))).isEqualTo("6A83");
		Assertions.assertThat(EmvTemplate.getStatusWord(BytesUtils.fromString("90 00"))).isEqualTo("9000");
		Assertions.assertThat(EmvTemplate.getStatusWord(new byte[] { 0x6A })).isNull();
		Assertions.assertThat(EmvTemplate.getStatusWord(new byte[0])).isNull();
		Assertions.assertThat(EmvTemplate.getStatusWord(null)).isNull();
	}

}
