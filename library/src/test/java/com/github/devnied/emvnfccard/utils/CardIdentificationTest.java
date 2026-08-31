package com.github.devnied.emvnfccard.utils;

import java.util.Arrays;

import org.fest.assertions.Assertions;
import org.junit.Test;

import com.github.devnied.emvnfccard.enums.EmvCardScheme;
import com.github.devnied.emvnfccard.exception.CommunicationException;
import com.github.devnied.emvnfccard.iso7816emv.EmvTags;
import com.github.devnied.emvnfccard.iso7816emv.ITag;
import com.github.devnied.emvnfccard.model.Application;
import com.github.devnied.emvnfccard.model.CardIdentification;
import com.github.devnied.emvnfccard.model.EmvCard;
import com.github.devnied.emvnfccard.model.enums.CountryCodeEnum;
import com.github.devnied.emvnfccard.model.enums.IdentificationConfidenceEnum;
import com.github.devnied.emvnfccard.model.enums.IssuerIdentifierSourceEnum;
import com.github.devnied.emvnfccard.parser.EmvTemplate;
import com.github.devnied.emvnfccard.provider.TestProvider;

/**
 * Identification of the payment network and of the issuer, asserted on the
 * recorded traces.
 * <p>
 * The point of the class under test is that it stops where the card stops. The
 * cases below are chosen so that each level of
 * {@link IdentificationConfidenceEnum} is reached by a real capture, and no
 * test asserts a bank name: none is derivable from a card without the register
 * of ISO/IEC 7812-1, which is not public and is not shipped here.
 * </p>
 */
public class CardIdentificationTest {

	private EmvCard read(final String pTrace) throws CommunicationException {
		return EmvTemplate.Builder() //
				.setProvider(new TestProvider(pTrace)) //
				.setConfig(EmvTemplate.Config().setContactLess(true).setReadCplc(true)) //
				.build().readEmvCard();
	}

	/**
	 * The MasterCardPpse2 capture is the one trace that carries an Issuer
	 * Identification Number. Its answer to the SELECT of the AID holds "BF 0C
	 * 43 42 03 52 82 08", the tag '42' of three bytes inside the File Control
	 * Information Issuer Discretionary Data, which is exactly where EMV 4.4
	 * Book 3 Annex A1 puts it: "Template: 'BF0C' or '73' | Tag: '42' | Length:
	 * 3".
	 */
	@Test
	public void testIssuerIdentificationNumberReadFromTheFci() throws CommunicationException {
		EmvCard card = read("MasterCardPpse2");
		Application app = card.getApplications().get(0);

		Assertions.assertThat(app.getIssuerIdentificationNumber()).isEqualTo("528208");
		// The card carries no tag '9F0C', the two forms are kept apart and the
		// extended one is never rebuilt from the truncated one
		Assertions.assertThat(app.getIssuerIdentificationNumberExtended()).isNull();

		CardIdentification identification = CardIdentificationUtils.identify(card);
		Assertions.assertThat(identification.getIssuerIdentificationNumber()).isEqualTo("528208");
		Assertions.assertThat(identification.getIssuerIdentificationNumberSource())
				.isEqualTo(IssuerIdentifierSourceEnum.ISSUER_IDENTIFICATION_NUMBER);
		Assertions.assertThat(identification.getIssuerIdentificationNumberSource().isCardResident()).isTrue();
		// A number is known, the institution behind it is not
		Assertions.assertThat(identification.getConfidence()).isEqualTo(IdentificationConfidenceEnum.ISSUER_RANGE);
		Assertions.assertThat(identification.isIssuerNamed()).isFalse();
	}

	/**
	 * The same capture shows why the number the card asserts and the number the
	 * account number implies have to be reported apart: its tag '42' reads
	 * 528208 while its track data, "56 2A 42 35 32 30 30 ...", spells the
	 * account number 5200000000000000, whose first six digits are 520000.
	 * <p>
	 * The card is not rejected for it. EMV 4.4 Book 3 section 10.2 lists the
	 * tag '42' among the objects for which the terminal "shall ignore the
	 * formatting error and continue processing", so the disagreement is
	 * reported and nothing more.
	 * </p>
	 */
	@Test
	public void testIssuerIdentificationNumberDisagreeingWithTheAccountNumber() throws CommunicationException {
		CardIdentification identification = CardIdentificationUtils.identify(read("MasterCardPpse2"));

		Assertions.assertThat(identification.getCardIssuerIdentificationNumber()).isEqualTo("528208");
		Assertions.assertThat(identification.getPanLegacyIssuerIdentificationNumber()).isEqualTo("520000");
		Assertions.assertThat(identification.getPanIssuerIdentificationNumber()).isEqualTo("52000000");
		Assertions.assertThat(identification.isIssuerIdentificationNumberMatchingPan()).isFalse();
	}

	/**
	 * The German girocard of the GeldKartePpse trace is the only capture whose
	 * issuer is actually named: its File Control Information holds "5F 54 0B 4E
	 * 4F 4C 41 44 45 32 31 53 48 4F" and "5F 53 16 44 45 30 31 ...", the Bank
	 * Identifier Code and the International Bank Account Number.
	 * <p>
	 * EMV 4.4 Book 3 Annex A1: "Bank Identifier Code (BIC) | Uniquely
	 * identifies a bank as defined in ISO 9362 | Tag: '5F54' | Length: 8 or
	 * 11". The value read is eleven characters long.
	 * </p>
	 */
	@Test
	public void testIssuerNamedByTheCard() throws CommunicationException {
		CardIdentification identification = CardIdentificationUtils.identify(read("GeldKartePpse"));

		Assertions.assertThat(identification.getBic()).isEqualTo("NOLADE21SHO");
		Assertions.assertThat(identification.getBic()).hasSize(11);
		Assertions.assertThat(identification.getIban()).isEqualTo("DE01130510300000000000");
		Assertions.assertThat(identification.getConfidence()).isEqualTo(IdentificationConfidenceEnum.ISSUER_NAMED);
		Assertions.assertThat(identification.isIssuerNamed()).isTrue();
		// The scheme is domestic, which says where the network runs and not who
		// issued the card: it never feeds the confidence
		Assertions.assertThat(identification.getScheme()).isEqualTo(EmvCardScheme.GIROCARD);
		Assertions.assertThat(identification.getSchemeCountry()).isEqualTo(CountryCodeEnum.DE);
	}

	/**
	 * The UK issued card of the VisaCardPpse3 trace carries no Issuer
	 * Identification Number, only an Issuer Country Code: its record holds "5F
	 * 28 02 08 26", 826 being the ISO 3166 code of the United Kingdom. Its
	 * account number is "5A 08 49 99 99 99 99 99 99 99".
	 * <p>
	 * The only number that can be reported is then a prefix of the account
	 * number, and it is reported as such.
	 * </p>
	 */
	@Test
	public void testIssuerIdentificationDerivedFromTheAccountNumber() throws CommunicationException {
		EmvCard card = read("VisaCardPpse3");
		CardIdentification identification = CardIdentificationUtils.identify(card);

		Assertions.assertThat(identification.getCardIssuerIdentificationNumber()).isNull();
		Assertions.assertThat(identification.getCardIssuerIdentificationNumberExtended()).isNull();
		Assertions.assertThat(identification.getIssuerCountry()).isEqualTo(CountryCodeEnum.GB);
		Assertions.assertThat(identification.getIssuerIdentificationNumberSource())
				.isEqualTo(IssuerIdentifierSourceEnum.PAN);
		// Nothing card resident asserted the number, so nothing can be compared
		// against the account number either
		Assertions.assertThat(identification.getIssuerIdentificationNumberSource().isCardResident()).isFalse();
		Assertions.assertThat(identification.isIssuerIdentificationNumberMatchingPan()).isFalse();
		// Both lengths are offered, the card discloses no boundary inside its
		// account number
		Assertions.assertThat(identification.getPanIssuerIdentificationNumber()).isEqualTo("49999999");
		Assertions.assertThat(identification.getPanLegacyIssuerIdentificationNumber()).isEqualTo("499999");
	}

	/**
	 * The same capture is co-badged: the application it selects is the French
	 * domestic one, "4F 07 A0 00 00 00 42 10 10", while its account number
	 * begins with a 4. The identification reports the scheme the card announced
	 * through its Application Identifier and says so, {@link EmvCard#getType()}
	 * answering the other question.
	 */
	@Test
	public void testSchemeComesFromTheApplicationIdentifier() throws CommunicationException {
		EmvCard card = read("VisaCardPpse3");
		CardIdentification identification = CardIdentificationUtils.identify(card);

		Assertions.assertThat(identification.getScheme()).isEqualTo(EmvCardScheme.CB);
		Assertions.assertThat(identification.isSchemeFromAid()).isTrue();
		Assertions.assertThat(identification.getSchemeCountry()).isEqualTo(CountryCodeEnum.FR);
		Assertions.assertThat(card.getType()).isEqualTo(EmvCardScheme.VISA);
	}

	/**
	 * A description matched for the Answer To Reset or the Answer To Select
	 * identifies nothing on its own.
	 * <p>
	 * It is not a statement of the issuer, and over the contactless interface
	 * it is not even a statement of the card: PC/SC Part 3 section 3.1.3.2.3
	 * requires that "For contactless ICCs, the IFD subsystem must construct an
	 * ATR from the fixed elements that identify the cards", its Table 3-5 being
	 * titled "ATR Returned by IFD Handler for Contactless Smart Card ICCs".
	 * </p>
	 */
	@Test
	public void testAtrDescriptionAloneIdentifiesNothing() {
		EmvCard card = new EmvCard();
		card.setAtrDescription(Arrays.asList("CB Visa Banque Populaire (France)"));

		CardIdentification identification = CardIdentificationUtils.identify(card);

		Assertions.assertThat(identification.getAtrDescriptions()).containsOnly("CB Visa Banque Populaire (France)");
		Assertions.assertThat(identification.getConfidence()).isEqualTo(IdentificationConfidenceEnum.NONE);
		Assertions.assertThat(identification.getIssuerIdentificationNumberSource())
				.isEqualTo(IssuerIdentifierSourceEnum.NONE);
		Assertions.assertThat(identification.getIssuerIdentificationNumber()).isNull();
	}

	/**
	 * An identification of nothing is still an identification, never a null.
	 */
	@Test
	public void testEmptyIdentification() {
		CardIdentification identification = CardIdentificationUtils.identify(null);

		Assertions.assertThat(identification).isNotNull();
		Assertions.assertThat(identification.getConfidence()).isEqualTo(IdentificationConfidenceEnum.NONE);
		Assertions.assertThat(identification.getAtrDescriptions()).isEmpty();
		Assertions.assertThat(identification.getScheme()).isNull();
	}

	/**
	 * The Issuer Identification Number Extended (tag '9F0C') wins over the
	 * Issuer Identification Number (tag '42') when a card carries both, which
	 * EMV 4.4 Book 3 Annex A1 allows: "While the first 6 digits of the IINE
	 * (tag '9F0C') and IIN (tag '42') are the same and there is no need to have
	 * both data objects on the card, cards may have both the IIN and IINE data
	 * objects present."
	 * <p>
	 * No capture holds a tag '9F0C', and no eight digit number is invented for
	 * the sake of the test: the value used is the one the MasterCardPpse2 card
	 * really carries, in the six digit form the tag also accepts, EMV 4.4 Book
	 * 3 Annex A1 coding the tag "n 6 or 8" over a length of "var. 3 or 4".
	 * </p>
	 */
	@Test
	public void testExtendedIssuerIdentificationNumberIsPreferred() {
		Application application = new Application();
		application.setIssuerIdentificationNumber("528208");
		application.setIssuerIdentificationNumberExtended("528208");

		CardIdentification identification = CardIdentificationUtils.identify(null, application);

		Assertions.assertThat(identification.getIssuerIdentificationNumberSource())
				.isEqualTo(IssuerIdentifierSourceEnum.ISSUER_IDENTIFICATION_NUMBER_EXTENDED);
		Assertions.assertThat(identification.getIssuerIdentificationNumber()).isEqualTo("528208");
		Assertions.assertThat(identification.getConfidence()).isEqualTo(IdentificationConfidenceEnum.ISSUER_RANGE);
	}

	/**
	 * The tag '9F0C' was missing from the tag dictionary, so a card sending it
	 * had its eight digit Issuer Identification Number reported as an unknown
	 * tag. EMV 4.4 Book 3 Annex A1 defines it as "Issuer Identification Number
	 * Extended (IINE) | Format: n 6 or 8 | Template: 'BF0C' or '73' | Tag:
	 * '9F0C' | Length: var. 3 or 4".
	 */
	@Test
	public void testExtendedIssuerIdentificationNumberTagIsKnown() {
		ITag tag = EmvTags.find(new byte[] { (byte) 0x9F, (byte) 0x0C });

		Assertions.assertThat(tag).isNotNull();
		Assertions.assertThat(tag).isEqualTo(EmvTags.ISSUER_IDENTIFICATION_NUMBER_EXTENDED);
		Assertions.assertThat(tag.getName()).isEqualTo("Issuer Identification Number Extended (IINE)");
		Assertions.assertThat(tag.getNumTagBytes()).isEqualTo(2);
		Assertions.assertThat(tag.isConstructed()).isFalse();
	}

	/**
	 * The levels are ordered, so that a caller can require a minimum without
	 * enumerating them.
	 */
	@Test
	public void testConfidenceLevelsAreOrdered() {
		Assertions.assertThat(IdentificationConfidenceEnum.ISSUER_NAMED.getKey())
				.isGreaterThan(IdentificationConfidenceEnum.ISSUER_RANGE.getKey());
		Assertions.assertThat(IdentificationConfidenceEnum.ISSUER_RANGE.getKey())
				.isGreaterThan(IdentificationConfidenceEnum.COUNTRY.getKey());
		Assertions.assertThat(IdentificationConfidenceEnum.COUNTRY.getKey())
				.isGreaterThan(IdentificationConfidenceEnum.SCHEME.getKey());
		Assertions.assertThat(IdentificationConfidenceEnum.SCHEME.getKey())
				.isGreaterThan(IdentificationConfidenceEnum.NONE.getKey());

		Assertions.assertThat(IdentificationConfidenceEnum.ISSUER_NAMED
				.isAtLeast(IdentificationConfidenceEnum.ISSUER_RANGE)).isTrue();
		Assertions.assertThat(IdentificationConfidenceEnum.SCHEME
				.isAtLeast(IdentificationConfidenceEnum.ISSUER_RANGE)).isFalse();
		Assertions.assertThat(IdentificationConfidenceEnum.NONE.isAtLeast(null)).isTrue();
		Assertions.assertThat(EnumUtils.find(4, IdentificationConfidenceEnum.class))
				.isEqualTo(IdentificationConfidenceEnum.ISSUER_NAMED);
		Assertions.assertThat(EnumUtils.find(3, IssuerIdentifierSourceEnum.class))
				.isEqualTo(IssuerIdentifierSourceEnum.ISSUER_IDENTIFICATION_NUMBER_EXTENDED);
	}

}
