package com.github.devnied.emvnfccard;

import org.fest.assertions.Assertions;
import org.junit.Test;

import com.github.devnied.emvnfccard.enums.EmvCardScheme;
import com.github.devnied.emvnfccard.exception.CommunicationException;
import com.github.devnied.emvnfccard.model.Application;
import com.github.devnied.emvnfccard.model.EmvCard;
import com.github.devnied.emvnfccard.parser.EmvTemplate;
import com.github.devnied.emvnfccard.parser.impl.EmvParser;
import com.github.devnied.emvnfccard.provider.ScriptedProvider;

import fr.devnied.bitlib.BytesUtils;

/**
 * A card exposing several applications must be reported coherently: the scheme
 * has to describe the account the card number belongs to.
 * <p>
 * No capture of src/test/resources/data exposes two applications of different
 * schemes that both answer a track, so the card is assembled. The line drawn
 * here: everything that is card <i>content</i> — the directory entries, the
 * Application Interchange Profile, the Application File Locator and the track
 * — is copied verbatim from the MasterCardPpse and VisaCardPpse captures, and
 * only the File Control Information envelopes are built from the structure the
 * specification defines, holding nothing but the AID and the label.
 * </p>
 */
public class MultiApplicationCardTest {

	/** Directory entry of the MasterCard application of the MasterCardPpse capture */
	private static final String MASTERCARD_ENTRY = "61 1E 4F 07 A0 00 00 00 04 10 10 87 01 01 50 0A 4D 41 53 54 45 52 43 41 52 44"
			+ " 9F 28 03 40 02 00";

	/** Directory entry of the Visa application of the VisaCardPpse capture */
	private static final String VISA_ENTRY = "61 12 4F 07 A0 00 00 00 03 10 10 50 04 56 49 53 41 87 01 02";

	/** File Control Information envelope carrying the MasterCard AID and label */
	private static final String MASTERCARD_FCI = "6F 1A 84 07 A0 00 00 00 04 10 10 A5 0F 50 0A 4D 41 53 54 45 52 43 41 52 44"
			+ " 87 01 01 90 00";

	/** File Control Information envelope carrying the Visa AID and label */
	private static final String VISA_FCI = "6F 14 84 07 A0 00 00 00 03 10 10 A5 09 50 04 56 49 53 41 87 01 02 90 00";

	/** Interchange profile and file locator of the MasterCardPpse card */
	private static final String MASTERCARD_GPO = "77 0A 82 02 19 80 94 04 10 01 01 01 90 00";

	/** Record of the MasterCardPpse card, it carries the track 2 equivalent data */
	private static final String MASTERCARD_RECORD = "77 38 9F 10 07 06 01 1A 23 80 40 04 57 13 55 99 99 99 99 99 99 99 D1 50 92"
			+ " FF FF FF FF FF FF FF 0F 82 02 20 00 9F 36 02 02 8F 9F 26 08 FF FF FF FF FF FF FF FF 9F 6C 02 10 00 90 00";

	/**
	 * The card number is the one of the first application that carried a
	 * track, so the scheme must be that application's too. Reporting the
	 * scheme of the last application read would describe another account.
	 */
	@Test
	public void testSchemeDescribesTheAccountThatWasRead() throws CommunicationException {
		ScriptedProvider provider = new ScriptedProvider()
				.on("00A404000E325041592E5359532E444446303100",
						"6F 49 84 0E 32 50 41 59 2E 53 59 53 2E 44 44 46 30 31 A5 37 BF 0C 34 " + MASTERCARD_ENTRY
								+ " " + VISA_ENTRY + " 90 00")
				// The MasterCard application has the highest priority and
				// answers the track, in the SFI 2 its own file locator points to
				.on("00A4040007A000000004101000", MASTERCARD_FCI)
				.on("80A8000002830000", MASTERCARD_GPO)
				.on("00B2011400", MASTERCARD_RECORD)
				// The Visa application is read next and answers no track
				.on("00A4040007A000000003101000", VISA_FCI)
				.on("80CA9F1700", "9F 17 01 03 90 00").on("80CA9F3600", "9F 36 02 00 2C 90 00");

		EmvCard card = EmvTemplate.Builder().setProvider(provider)
				.setConfig(EmvTemplate.Config().setReadAt(false).setReadTransactions(false)).build().readEmvCard();

		Assertions.assertThat(card.getApplications()).hasSize(2);
		Assertions.assertThat(card.getCardNumber()).isEqualTo("5599999999999999");
		// A MasterCard number must not be reported under the Visa scheme
		Assertions.assertThat(card.getType()).isEqualTo(EmvCardScheme.MASTER_CARD);
		// Where the type came from: the MasterCard application
		Assertions.assertThat(card.getAidType()).isEqualTo(EmvCardScheme.MASTER_CARD);
		Assertions.assertThat(BytesUtils.bytesToStringNoSpace(card.getTypeAid())).isEqualTo("A0000000041010");
		Assertions.assertThat(card.getApplications().get(0).getAidScheme()).isEqualTo(EmvCardScheme.MASTER_CARD);
		Assertions.assertThat(card.getApplications().get(1).getAidScheme()).isEqualTo(EmvCardScheme.VISA);
		Assertions.assertThat(card.getIdentification().getScheme()).isEqualTo(EmvCardScheme.MASTER_CARD);
		Assertions.assertThat(card.getApplications().get(1).getIdentification().getScheme()).isEqualTo(EmvCardScheme.VISA);
	}

	/**
	 * The Application File Locator is card controlled. A malformed one must
	 * not be able to turn a single tap into thousands of exchanges.
	 */
	@Test
	public void testTheRecordReadingIsBounded() throws CommunicationException {
		// One AFL entry declaring the 255 records of the SFI 1, which is the
		// encoding of EMV 4.3 Book 3 section 10.2, answered '6A83'
		ScriptedProvider provider = new ScriptedProvider()
				.on("00A404000E325041592E5359532E444446303100",
						"6F 1B 84 0E 32 50 41 59 2E 53 59 53 2E 44 44 46 30 31 A5 09 BF 0C 06 61 04 4F 02 A0 00 90 00")
				.on("00A4040002A00000", "6F 07 84 02 A0 00 A5 01 50 90 00")
				.on("80A8000002830000", "80 06 3C 00 08 01 FF 00 90 00");

		EmvCard card = EmvTemplate.Builder().setProvider(provider)
				.setConfig(EmvTemplate.Config().setReadAt(false).setReadTransactions(false)).build().readEmvCard();

		int readRecords = 0;
		for (String command : provider.getReceived()) {
			if (command.startsWith("00B2")) {
				readRecords++;
			}
		}
		Assertions.assertThat(readRecords).isLessThanOrEqualTo(EmvParser.MAX_RECORDS);
		// The bound and every record it let through are reported
		Assertions.assertThat(card.getApplications()).hasSize(1);
		Application app = card.getApplications().get(0);
		Assertions.assertThat(app.isAflTruncated()).isTrue();
		Assertions.assertThat(app.getRecordsRead()).isEqualTo(EmvParser.MAX_RECORDS);
		Assertions.assertThat(app.getRecords()).hasSize(EmvParser.MAX_RECORDS);
		Assertions.assertThat(app.getRecords().get(0).getSfi()).isEqualTo(1);
		Assertions.assertThat(app.getRecords().get(0).getRecordNumber()).isEqualTo(1);
		Assertions.assertThat(app.getRecords().get(0).getStatusWord()).isEqualTo("6A83");
		Assertions.assertThat(app.getRecords().get(EmvParser.MAX_RECORDS - 1).getRecordNumber()).isEqualTo(EmvParser.MAX_RECORDS);
		Assertions.assertThat(BytesUtils.bytesToStringNoSpace(app.getRawAfl())).isEqualTo("0801FF00");
		Assertions.assertThat(app.getAfl()).hasSize(1);
		Assertions.assertThat(app.getAfl().get(0).getLastRecord()).isEqualTo(255);
	}
}
