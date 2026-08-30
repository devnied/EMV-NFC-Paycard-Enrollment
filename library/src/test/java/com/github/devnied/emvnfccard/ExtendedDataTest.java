package com.github.devnied.emvnfccard;

import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.fest.assertions.Assertions;
import org.junit.Test;

import com.github.devnied.emvnfccard.enums.EmvCardScheme;
import com.github.devnied.emvnfccard.exception.CommunicationException;
import com.github.devnied.emvnfccard.model.AbstractData;
import com.github.devnied.emvnfccard.model.Application;
import com.github.devnied.emvnfccard.model.CardTransactionQualifiers;
import com.github.devnied.emvnfccard.model.EmvCard;
import com.github.devnied.emvnfccard.model.EmvTransactionRecord;
import com.github.devnied.emvnfccard.model.MagStripeData;
import com.github.devnied.emvnfccard.model.enums.CountryCodeEnum;
import com.github.devnied.emvnfccard.model.enums.CurrencyEnum;
import com.github.devnied.emvnfccard.model.enums.CvmMethodEnum;
import com.github.devnied.emvnfccard.model.enums.KernelEnum;
import com.github.devnied.emvnfccard.parser.EmvTemplate;
import com.github.devnied.emvnfccard.parser.impl.EmvParser;
import com.github.devnied.emvnfccard.provider.ScriptedProvider;
import com.github.devnied.emvnfccard.provider.TestProvider;
import com.github.devnied.emvnfccard.utils.EmvDataUtils;
import com.github.devnied.emvnfccard.utils.reflect.ReflectionTestUtils;

import fr.devnied.bitlib.BytesUtils;

/**
 * Data objects extracted on top of the card number and of the expiration date,
 * asserted against the card traces recorded in src/test/resources/data.
 */
public class ExtendedDataTest {

	private EmvCard read(final String pTrace, final boolean pContactLess) throws CommunicationException {
		return EmvTemplate.Builder() //
				.setProvider(new TestProvider(pTrace)) //
				.setConfig(EmvTemplate.Config().setContactLess(pContactLess).setReadCplc(true)) //
				.build().readEmvCard();
	}

	/**
	 * The UK issued Visa of the VisaCardPpse3 trace returns its track 2 in the
	 * GPO response and keeps the PAN, the expiration date and the issuer
	 * country in the records of its Application File Locator.
	 */
	@Test
	public void testRecordsAreReadAfterTheTrack() throws CommunicationException {
		EmvCard card = read("VisaCardPpse3", true);
		Application app = card.getApplications().get(0);

		// Tag '5A' Application Primary Account Number
		Assertions.assertThat(app.getPan()).isEqualTo("4999999999999999");
		// Tag '5F34' Application PAN Sequence Number
		Assertions.assertThat(app.getPanSequenceNumber()).isZero();
		// Tag '5F24' Application Expiration Date
		Assertions.assertThat(new SimpleDateFormat("dd/MM/yyyy", Locale.US).format(app.getExpirationDate())).isEqualTo("30/06/2017");
		// Tag '5F28' Issuer Country Code, 826 is the United Kingdom
		Assertions.assertThat(app.getIssuerCountryCode()).isEqualTo(CountryCodeEnum.GB);
		// Tag '82' Application Interchange Profile
		Assertions.assertThat(app.getInterchangeProfile()).isNotNull();
		Assertions.assertThat(app.getInterchangeProfile().isDdaSupported()).isTrue();
		// The track read from the GPO response must survive the reading of the
		// records that follow it
		Assertions.assertThat(card.getTrack2()).isNotNull();
		Assertions.assertThat(card.getCardNumber()).isEqualTo("4999999999999999");
	}

	/**
	 * Reading only up to the card number leaves the records of the Application
	 * File Locator unread.
	 */
	@Test
	public void testReadAllRecordsDisabled() throws CommunicationException {
		EmvCard card = EmvTemplate.Builder() //
				.setProvider(new TestProvider("VisaCardPpse3")) //
				.setConfig(EmvTemplate.Config().setReadCplc(true).setReadAllRecords(false)) //
				.build().readEmvCard();
		Application app = card.getApplications().get(0);

		Assertions.assertThat(card.getCardNumber()).isEqualTo("4999999999999999");
		Assertions.assertThat(app.getPan()).isNull();
		Assertions.assertThat(app.getIssuerCountryCode()).isNull();
	}

	/**
	 * The French CB/MasterCard of the MasterCardPpse trace declares the code
	 * table 1 (ISO/IEC 8859-1) and prefers French then English.
	 */
	@Test
	public void testFrenchCard() throws CommunicationException {
		EmvCard card = read("MasterCardPpse", true);
		Application app = card.getApplications().get(0);

		Assertions.assertThat(app.getApplicationPreferredName()).isEqualTo("CB");
		Assertions.assertThat(app.getIssuerCodeTableIndex()).isEqualTo(1);
		Assertions.assertThat(app.getLanguagePreferences()).isEqualTo(Arrays.asList("fr", "en"));
		Assertions.assertThat(EmvCardScheme.getCardTypeByAid("A0000000421010")).isEqualTo(EmvCardScheme.CB);
		Assertions.assertThat(EmvCardScheme.CB.getCountry()).isEqualTo(CountryCodeEnum.FR);
	}

	/**
	 * The MasterCardPpse2 trace was recorded on a Turkish card: its
	 * transactions are in Turkish lira and it declares the code table 9, so its
	 * texts have to be decoded with ISO/IEC 8859-9 (Latin-5) and not with the
	 * default ISO/IEC 8859-1.
	 */
	@Test
	public void testTurkishCardUsesTheRightCodeTable() throws CommunicationException {
		EmvCard card = read("MasterCardPpse2", true);
		Application app = card.getApplications().get(0);

		Assertions.assertThat(app.getIssuerCodeTableIndex()).isEqualTo(9);
		Assertions.assertThat(EmvDataUtils.getCharsetName(app.getIssuerCodeTableIndex())).isEqualTo("ISO-8859-9");
		Assertions.assertThat(app.getApplicationPreferredName()).isEqualTo("MASTERCARD");
		Assertions.assertThat(app.getApplicationLabel()).isEqualTo("MASTERCARD");
		// The Application Label of the FCI (tag '50') is kept on its own, the
		// label above prefers the Application Preferred Name (tag '9F12')
		Assertions.assertThat(app.getFciApplicationLabel()).isEqualTo("DEBIT MASTERCARD");
	}

	/**
	 * Interac is the Canadian domestic scheme, the card of the InteractPpse
	 * trace answers 6285 to the SELECT and only speaks English.
	 */
	@Test
	public void testCanadianInteracCard() throws CommunicationException {
		EmvCard card = read("InteractPpse", true);
		Application app = card.getApplications().get(0);

		Assertions.assertThat(card.getType()).isEqualTo(EmvCardScheme.INTERAC);
		Assertions.assertThat(card.getType().getCountry()).isEqualTo(CountryCodeEnum.CA);
		Assertions.assertThat(card.getType().isDomestic()).isTrue();
		Assertions.assertThat(app.getApplicationLabel()).isEqualTo("INTERAC");
		Assertions.assertThat(app.getLanguagePreferences()).isEqualTo(Collections.singletonList("en"));
	}

	/**
	 * The German card of the GeldKartePpse trace is a girocard carrying a
	 * GeldKarte purse: the GeldKarte parser must expose the data objects of the
	 * FCI too.
	 */
	@Test
	public void testGermanGirocard() throws CommunicationException {
		EmvCard card = read("GeldKartePpse", true);
		Application app = card.getApplications().get(0);

		Assertions.assertThat(card.getType()).isEqualTo(EmvCardScheme.GIROCARD);
		Assertions.assertThat(app.getApplicationLabel()).isEqualTo("girocard");
		Assertions.assertThat(app.getLanguagePreferences()).isEqualTo(Arrays.asList("de", "en"));
		// The BIC and the IBAN are also part of the FCI of this card
		Assertions.assertThat(card.getBic()).isEqualTo("NOLADE21SHO");
		Assertions.assertThat(card.getIban()).isEqualTo("DE01130510300000000000");
		// ... and are kept on the application that carried them
		Assertions.assertThat(app.getBic()).isEqualTo("NOLADE21SHO");
		Assertions.assertThat(app.getIban()).isEqualTo("DE01130510300000000000");
		Assertions.assertThat(app.getFciApplicationLabel()).isEqualTo("girocard");
	}

	/**
	 * Reading the whole Application File Locator must not let a later record
	 * replace the account the card presents first: the primary PAN is the one
	 * of the first track found.
	 */
	@Test
	public void testFirstTrackFoundWins() throws CommunicationException {
		// A card whose AFL lists two records holding a track: the live account
		// then a companion one
		ScriptedProvider provider = new ScriptedProvider()
				.on("00A404000E325041592E5359532E444446303100",
						"6F 1B 84 0E 32 50 41 59 2E 53 59 53 2E 44 44 46 30 31 A5 09 BF 0C 06 61 04 4F 02 A0 00 90 00")
				.on("00A4040002A00000", "6F 07 84 02 A0 00 A5 01 50 90 00")
				.on("80A8000002830000", "80 0A 3C 00 08 01 01 00 10 01 01 00 90 00")
				// SFI 1 record 1: the account the card presents
				.on("00B2010C00", "70 12 57 10 49 99 99 99 99 99 99 99 D1 50 92 01 00 00 00 0F 90 00")
				// SFI 2 record 1: a companion account read only because the
				// whole application file is now read
				.on("00B2011400", "70 12 57 10 41 11 11 11 11 11 11 11 D1 20 12 01 00 00 00 0F 90 00")
				.on("80CA9F1700", "9F 17 01 03 90 00").on("80CA9F3600", "9F 36 02 00 2C 90 00");

		EmvCard card = EmvTemplate.Builder().setProvider(provider)
				.setConfig(EmvTemplate.Config().setReadAt(false).setReadTransactions(false)).build().readEmvCard();

		// Both records were read, the first track is the one kept
		Assertions.assertThat(provider.getReceived()).contains("00B2010C00", "00B2011400");
		Assertions.assertThat(card.getCardNumber()).isEqualTo("4999999999999999");
	}

	/**
	 * The tags '9F6C', '9F6E' and '9F5D' have one meaning per contactless
	 * kernel, so the library exposes their raw value. Asserted against the
	 * recorded traces, which happen to carry both readings of '9F6C'.
	 */
	@Test
	public void testKernelCapabilityDataObjects() throws CommunicationException {
		// A Visa card answers its Card Transaction Qualifiers in the GPO
		EmvCard visaCard = read("VisaCardPpse", true);
		Application visa = visaCard.getApplications().get(0);
		Assertions.assertThat(BytesUtils.bytesToStringNoSpace(visa.getCardTransactionQualifiers())).isEqualTo("1000");
		// The first application is the Cartes Bancaires one: neither the card
		// nor the AID names a kernel, so the tag stays raw
		Assertions.assertThat(visa.getResolvedKernel()).isNull();
		Assertions.assertThat(visa.getCardTransactionQualifiersDecoded()).isNull();
		Assertions.assertThat(visa.getTerminalTransactionQualifiersEcho()).isNull();
		// The second one is the Visa application, its qualifiers are decoded
		// against Kernel 3: byte 1 bit 5, switch interface if ODA fails
		Application visaAid = visaCard.getApplications().get(1);
		Assertions.assertThat(visaAid.getResolvedKernel()).isEqualTo(KernelEnum.VISA);
		Assertions.assertThat(visaAid.getCardTransactionQualifiersDecoded()).isNotNull();
		Assertions.assertThat(visaAid.getCardTransactionQualifiersDecoded().isSwitchInterfaceIfOdaFails()).isTrue();
		Assertions.assertThat(visaAid.getCardTransactionQualifiersDecoded().isOnlinePinRequired()).isFalse();

		// On a MasterCard the same tag is the Mag Stripe Application Version
		// Number, and '9F6E' is the Third Party Data
		Application mastercard = read("MasterCardPpse3", true).getApplications().get(0);
		Assertions.assertThat(BytesUtils.bytesToStringNoSpace(mastercard.getCardTransactionQualifiers())).isEqualTo("0001");
		Assertions.assertThat(BytesUtils.bytesToStringNoSpace(mastercard.getFormFactorIndicator())).isEqualTo("02210000303000");
		Assertions.assertThat(mastercard.getResolvedKernel()).isEqualTo(KernelEnum.MASTERCARD);
		Assertions.assertThat(mastercard.getCardTransactionQualifiersDecoded()).isNull();
		Assertions.assertThat(mastercard.getMagStripeData().getApplicationVersionNumber()).isEqualTo(1);
		// Tag '9F66' as answered, PUNATC(Track2) on this kernel
		Assertions.assertThat(BytesUtils.bytesToStringNoSpace(mastercard.getTerminalTransactionQualifiersEcho())).isEqualTo("00FE");
		// Tag '5F56' Issuer Country Code alpha-3 and tag '50' of its FCI
		Assertions.assertThat(mastercard.getIssuerCountryCodeAlpha3()).isEqualTo("CAN");
		Assertions.assertThat(mastercard.getIssuerCountryCodeAlpha2()).isNull();
		Assertions.assertThat(mastercard.getFciApplicationLabel()).isEqualTo("MasterCard");

		// Application Capabilities Information of a MasterCard credential
		Application aci = read("MasterCardPpse2", true).getApplications().get(0);
		Assertions.assertThat(BytesUtils.bytesToStringNoSpace(aci.getApplicationCapabilitiesInformation())).isEqualTo("010300");
	}

	/**
	 * The CVM list (tag '8E') and the Issuer Action Codes are read into the
	 * application. No trace of src/test/resources/data carries them, so the
	 * values below are the ones published for real cards by the relay
	 * protection project of the University of Birmingham
	 * (https://tomchothia.gitlab.io/Relay/payPassABN.txt, an ABN AMRO Maestro
	 * sniffed contactless). That source publishes a decoded tree rather than
	 * the raw record, so each value is replayed in its own TLV.
	 */
	@Test
	public void testCvmAmountsAndIssuerActionCodes() throws CommunicationException {
		EmvParser parser = new EmvParser(EmvTemplate.Builder().setProvider(new ScriptedProvider()).build());
		Application app = new Application();

		ReflectionTestUtils.invokeMethod(parser, "extractExtendedData",
				BytesUtils.fromString("8E 0C 00 00 00 00 00 00 00 00 42 03 1F 03"
						+ " 9F 0D 05 B4 50 84 80 00 9F 0E 05 00 00 18 00 00 9F 0F 05 B4 70 84 80 00"),
				app);

		// This card sets no amount threshold: both are zero, which the parser
		// has to report as such and not as "unknown"
		Assertions.assertThat(app.getCvmAmountX()).isZero();
		Assertions.assertThat(app.getCvmAmountY()).isZero();
		// '4203' enciphered PIN verified online, apply the next rule if it
		// fails, if the terminal supports the CVM. Then '1F03' no CVM required
		Assertions.assertThat(app.getCvmList()).hasSize(2);
		Assertions.assertThat(app.getCvmList().get(0).getMethod()).isEqualTo(CvmMethodEnum.ENCIPHERED_PIN_ONLINE);
		Assertions.assertThat(app.getCvmList().get(0).isApplyNextIfUnsuccessful()).isTrue();
		Assertions.assertThat(app.getCvmList().get(1).getMethod()).isEqualTo(CvmMethodEnum.NO_CVM);
		// The issuer risk policy
		Assertions.assertThat(BytesUtils.bytesToStringNoSpace(app.getIssuerActionCodeDefault())).isEqualTo("B450848000");
		Assertions.assertThat(BytesUtils.bytesToStringNoSpace(app.getIssuerActionCodeDenial())).isEqualTo("0000180000");
		Assertions.assertThat(BytesUtils.bytesToStringNoSpace(app.getIssuerActionCodeOnline())).isEqualTo("B470848000");
	}

	/**
	 * The Application Reference Currency (tag '9F3B') holds 1 to 4 ISO 4217
	 * numeric codes on 2 bytes each, and the Application Reference Currency
	 * Exponent (tag '9F43') one byte per currency, paired positionally. No
	 * trace of src/test/resources/data carries them, so the values below are
	 * the ISO 4217 codes and exponents of the currencies themselves.
	 */
	@Test
	public void testReferenceCurrenciesAndTheirExponents() {
		EmvParser parser = new EmvParser(EmvTemplate.Builder().setProvider(new ScriptedProvider()).build());
		Application app = new Application();

		// 978 euro (2 decimals), 392 yen (none), 840 dollar (2)
		ReflectionTestUtils.invokeMethod(parser, "extractExtendedData",
				BytesUtils.fromString("9F3B 06 0978 0392 0840 9F43 03 02 00 02"), app);

		Assertions.assertThat(app.getReferenceCurrencies()).containsExactly(CurrencyEnum.EUR, CurrencyEnum.JPY,
				CurrencyEnum.USD);
		Assertions.assertThat(app.getReferenceCurrencyExponents()).containsExactly(2, 0, 2);

		// A card that sends the currencies without their exponents
		Application noExponent = new Application();
		ReflectionTestUtils.invokeMethod(parser, "extractExtendedData", BytesUtils.fromString("9F3B 02 0978"), noExponent);
		Assertions.assertThat(noExponent.getReferenceCurrencies()).containsExactly(CurrencyEnum.EUR);
		Assertions.assertThat(noExponent.getReferenceCurrencyExponents()).containsExactly(AbstractData.UNKNOWN);
		Assertions.assertThat(noExponent.getReferenceCurrencyCodes()).containsExactly(978);

		// Every code is kept as sent: 999 is not an ISO 4217 code and 0 is
		// padding, neither is a known currency but both are reported
		Application unknown = new Application();
		ReflectionTestUtils.invokeMethod(parser, "extractExtendedData", BytesUtils.fromString("9F3B 08 0978 0999 0000 0840"),
				unknown);
		Assertions.assertThat(unknown.getReferenceCurrencies()).containsExactly(CurrencyEnum.EUR, CurrencyEnum.USD);
		Assertions.assertThat(unknown.getReferenceCurrencyCodes()).containsExactly(978, 999, 0, 840);
		// A later response never appends to the list
		ReflectionTestUtils.invokeMethod(parser, "extractExtendedData", BytesUtils.fromString("9F3B 02 0392"), unknown);
		Assertions.assertThat(unknown.getReferenceCurrencyCodes()).containsExactly(978, 999, 0, 840);
	}

	/**
	 * EMV Contactless Book B builds the Requested Kernel ID from the Kernel
	 * Identifier of the directory entry, and only lets the AID give a default
	 * value when the card expressed no preference: "If the length of the Kernel
	 * Identifier value field is zero, then Entry Point shall use a default value
	 * for the Requested Kernel ID, based on the matching AID". A card that did
	 * ask for a kernel is not answered another one.
	 */
	@Test
	public void testTheKernelIsDerivedFromTheAidOnlyWhenTheCardAskedForNone() {
		EmvParser parser = new EmvParser(EmvTemplate.Builder().setProvider(new ScriptedProvider()).build());

		// Type '11b', a domestic kernel: it is named by the first three bytes as
		// a whole, so no international kernel is claimed
		Application domestic = kernel(parser, "A0000000041010", "C2FFFF");
		Assertions.assertThat(domestic.isKernelRequestedByCard()).isTrue();
		Assertions.assertThat(domestic.getKernel()).isNull();
		Assertions.assertThat(domestic.isKernelDerived()).isFalse();
		Assertions.assertThat(BytesUtils.bytesToStringNoSpace(domestic.getKernelIdentifier())).isEqualTo("C2FFFF");

		// Type '01b' is reserved for future use: the Requested Kernel ID is the
		// whole first byte, so '43' is not the international kernel 3
		Application rfu = kernel(parser, "A0000000031010", "43");
		Assertions.assertThat(rfu.isKernelRequestedByCard()).isTrue();
		Assertions.assertThat(rfu.getKernel()).isNull();
		Assertions.assertThat(rfu.getShortKernelId()).isEqualTo(AbstractData.UNKNOWN);

		// Type '00b': the Short Kernel Identifier names the kernel
		Application international = kernel(parser, "A0000000031010", "02");
		Assertions.assertThat(international.getKernel()).isEqualTo(KernelEnum.MASTERCARD);
		Assertions.assertThat(international.isKernelDerived()).isFalse();

		// A Requested Kernel ID of zero means that any kernel suits the card, and
		// a Kernel Identifier the card did not send says nothing either: the AID
		// answers in both cases
		for (String identifier : new String[] { null, "00", "0000" }) {
			Application silent = kernel(parser, "A0000000041010", identifier);
			Assertions.assertThat(silent.isKernelRequestedByCard()).as(identifier).isFalse();
			Assertions.assertThat(silent.getKernel()).as(identifier).isEqualTo(KernelEnum.MASTERCARD);
			Assertions.assertThat(silent.isKernelDerived()).as(identifier).isTrue();
		}
	}

	/**
	 * Build an application with the AID and the Kernel Identifier given, and let
	 * the parser complete its kernel
	 *
	 * @param pParser
	 *            parser under test
	 * @param pAid
	 *            AID of the application
	 * @param pKernelIdentifier
	 *            raw Kernel Identifier, null when the card sent none
	 * @return the application
	 */
	private Application kernel(final EmvParser pParser, final String pAid, final String pKernelIdentifier) {
		Application ret = new Application();
		ret.setAid(BytesUtils.fromString(pAid));
		if (pKernelIdentifier != null) {
			ret.setKernelIdentifier(BytesUtils.fromString(pKernelIdentifier));
		}
		ReflectionTestUtils.invokeMethod(pParser, "deriveKernelFromAid", ret);
		return ret;
	}

	/**
	 * The Kernel Identifier of the payment directory is the one EMV Contactless
	 * Book B uses for the Combination Selection: a Kernel Identifier in the File
	 * Control Information of the application must not replace it. A domestic
	 * identifier names no Short Kernel Identifier, so it used to be erased.
	 */
	@Test
	public void testTheKernelIdentifierOfTheDirectoryWins() {
		EmvParser parser = new EmvParser(EmvTemplate.Builder().setProvider(new ScriptedProvider()).build());
		Application app = new Application();
		// What the directory entry announced
		app.setKernelIdentifier(BytesUtils.fromString("820978"));

		// What the File Control Information of the application announces
		ReflectionTestUtils.invokeMethod(parser, "extractExtendedData", BytesUtils.fromString("9F2A0102"), app);

		Assertions.assertThat(BytesUtils.bytesToStringNoSpace(app.getKernelIdentifier())).isEqualTo("820978");
		Assertions.assertThat(app.getKernel()).isNull();

		// An application read by AID never goes through a directory, the
		// identifier of its File Control Information is the only one
		Application byAid = new Application();
		ReflectionTestUtils.invokeMethod(parser, "extractExtendedData", BytesUtils.fromString("9F2A0102"), byAid);
		Assertions.assertThat(byAid.getKernel()).isEqualTo(KernelEnum.MASTERCARD);
	}

	/**
	 * The Log Entry (tag '9F4D') holds the SFI of the transaction log file
	 * followed by its number of records, so it is 2 bytes long (EMV 4.3 Book 3
	 * Annex D4). A card sending a shorter value must not abort the reading of
	 * the card: section 7.5 of the same book asks the terminal to ignore the
	 * formatting error of that data object and to carry on.
	 */
	@Test
	public void testMalformedLogEntryIsIgnored() throws CommunicationException {
		ScriptedProvider provider = new ScriptedProvider();
		EmvParser parser = new EmvParser(EmvTemplate.Builder().setProvider(provider).build());

		List<EmvTransactionRecord> records = ReflectionTestUtils.invokeMethod(parser, "extractLogEntry",
				(Object) BytesUtils.fromString("0B"));

		Assertions.assertThat(records).isEmpty();
		// The card was not even asked for its log format
		Assertions.assertThat(provider.getReceived()).isEmpty();
	}

	/**
	 * The number of offline transactions is only known when the card gives both
	 * the ATC and the last online ATC.
	 */
	@Test
	public void testConsecutiveOfflineTransactions() {
		Application app = new Application();
		Assertions.assertThat(app.getConsecutiveOfflineTransactions()).isEqualTo(AbstractData.UNKNOWN);
		app.setTransactionCounter(1580);
		Assertions.assertThat(app.getConsecutiveOfflineTransactions()).isEqualTo(AbstractData.UNKNOWN);
		app.setLastOnlineTransactionCounter(1575);
		Assertions.assertThat(app.getConsecutiveOfflineTransactions()).isEqualTo(5);
		// An inconsistent pair of counters is not reported
		app.setLastOnlineTransactionCounter(1600);
		Assertions.assertThat(app.getConsecutiveOfflineTransactions()).isEqualTo(AbstractData.UNKNOWN);
	}

	/**
	 * The MasterCardPpse2 card was personalised for the contactless Mag-stripe
	 * Mode of EMV Contactless Book C-2 v2.11 and answers the whole profile in a
	 * single record: "70 6B 9F 6C 02 00 01 9F 62 06 00 00 00 00 0E 00 9F 63 06
	 * 00 00 00 00 00 7E 56 2A [...] 9F 64 01 03 9F 65 02 00 0E 9F 66 02 0E 70 9F
	 * 6B 13 [...] 9F 67 01 03".
	 */
	@Test
	public void testMagStripeProfileOfAMasterCard() throws CommunicationException {
		Application app = read("MasterCardPpse2", true).getApplications().get(0);
		MagStripeData magStripe = app.getMagStripeData();

		// The profile belongs to Kernel 2, which is the kernel of a MasterCard
		// application
		Assertions.assertThat(app.getKernel()).isEqualTo(KernelEnum.MASTERCARD);
		Assertions.assertThat(app.getResolvedKernel()).isEqualTo(KernelEnum.MASTERCARD);
		Assertions.assertThat(magStripe.isPresent()).isTrue();
		// Tag '9F6C' Mag-stripe Application Version Number (Card), 1 on this
		// kernel
		Assertions.assertThat(magStripe.getApplicationVersionNumber()).isEqualTo(1);
		// The four data objects EMV Contactless Book C-2 v2.11 Table 6.4 makes
		// mandatory, and the consistency checks of S7.22
		Assertions.assertThat(magStripe.isMandatoryDataPresent()).isTrue();
		Assertions.assertThat(magStripe.isConsistent()).isTrue();

		// Tag '9F62' PCVC3(Track1) and tag '9F63' PUNATC(Track1), 6 bytes each
		Assertions.assertThat(BytesUtils.bytesToStringNoSpace(magStripe.getPcvc3Track1())).isEqualTo("000000000E00");
		Assertions.assertThat(BytesUtils.bytesToStringNoSpace(magStripe.getPunatcTrack1())).isEqualTo("00000000007E");
		// Tag '9F64' NATC(Track1)
		Assertions.assertThat(magStripe.getNatcTrack1()).isEqualTo(3);
		// Tag '9F65' PCVC3(Track2) and tag '9F66' PUNATC(Track2), 2 bytes each
		Assertions.assertThat(BytesUtils.bytesToStringNoSpace(magStripe.getPcvc3Track2())).isEqualTo("000E");
		Assertions.assertThat(BytesUtils.bytesToStringNoSpace(magStripe.getPunatcTrack2())).isEqualTo("0E70");
		// Tag '9F67' NATC(Track2)
		Assertions.assertThat(magStripe.getNatcTrack2()).isEqualTo(3);
		// nUN, the number of Unpredictable Number (Numeric) digits
		Assertions.assertThat(magStripe.getUnpredictableNumberDigitCount()).isEqualTo(3);

		// Tag '9F6B' Track 2 Data and tag '56' Track 1 Data, the values the
		// bitmaps address the discretionary data field of
		Assertions.assertThat(BytesUtils.bytesToStringNoSpace(magStripe.getTrack2Data()))
				.isEqualTo("5200000000000000D01192020000010000000F");
		Assertions.assertThat(magStripe.getTrack1Data()).hasSize(42);
		// ISO/IEC 7813 Structure B opens on the format code '42'
		Assertions.assertThat(magStripe.getTrack1Data()[0]).isEqualTo((byte) 0x42);

		// The card holds no UDOL, so a reader running the mode would fall back
		// on the Default UDOL. And the two CVC3 are only returned by the
		// COMPUTE CRYPTOGRAPHIC CHECKSUM command, which this library never
		// sends
		Assertions.assertThat(magStripe.getRawUdol()).isNull();
		Assertions.assertThat(magStripe.getUdol()).isEmpty();
		Assertions.assertThat(magStripe.getCvc3Track1()).isNull();
		Assertions.assertThat(magStripe.getCvc3Track2()).isNull();
	}

	/**
	 * The MasterCardPpse3 card answers the same data objects with the bitmaps
	 * laid out the other way around, "9F 62 06 00 00 00 00 07 00 9F 63 06 00 00
	 * 00 00 00 FE [...] 9F 64 01 04 9F 65 02 07 00 9F 66 02 00 FE [...] 9F 67 01
	 * 04": the positions the CVC3 goes to are carried by the first byte of the
	 * bitmap and not by the last one.
	 */
	@Test
	public void testMagStripeBitmapsAreReadAcrossTheirBytes() throws CommunicationException {
		MagStripeData magStripe = read("MasterCardPpse3", true).getApplications().get(0).getMagStripeData();

		Assertions.assertThat(magStripe.isConsistent()).isTrue();
		Assertions.assertThat(BytesUtils.bytesToStringNoSpace(magStripe.getPcvc3Track2())).isEqualTo("0700");
		Assertions.assertThat(BytesUtils.bytesToStringNoSpace(magStripe.getPunatcTrack2())).isEqualTo("00FE");
		Assertions.assertThat(magStripe.getNatcTrack2()).isEqualTo(4);
		// Three CVC3 digits at the positions 9, 10 and 11 of the discretionary
		// data field, seven non-zero bits in PUNATC(Track2) of which four are
		// taken by the counter
		Assertions.assertThat(magStripe.getCvc3Track2Positions()).containsExactly(9, 10, 11);
		Assertions.assertThat(magStripe.getUnpredictableNumberDigitCount()).isEqualTo(3);
		Assertions.assertThat(magStripe.getUnpredictableNumberTrack2Positions()).containsExactly(2, 3, 4);
		Assertions.assertThat(magStripe.getApplicationTransactionCounterTrack2Positions()).containsExactly(5, 6, 7, 8);
	}

	/**
	 * Tag '9F66' is PUNATC(Track2) for Kernel 2 and the reader sourced Terminal
	 * Transaction Qualifiers for Kernel 3, and tag '9F65' is PCVC3(Track2) for
	 * Kernel 2 only. EMV Contactless Book B fixes the kernel before the first
	 * GET PROCESSING OPTIONS, so the tag dictionary of Kernel 2 is only applied
	 * to an application whose kernel is the MasterCard one.
	 * <p>
	 * The File Control Information below is the one the Cartes Bancaires
	 * application of the VisaCardPpse3 capture answers, "6F 10 84 08 A0 00 00 00
	 * 42 00 00 00 A5 04 9F 65 01 FF": a single byte at tag '9F65', where
	 * PCVC3(Track2) is two bytes long. Reading it as a mag-stripe bitmap would
	 * invent a personalisation the card does not have.
	 * </p>
	 */
	@Test
	public void testMagStripeProfileIsNotReadForAnotherKernel() {
		EmvParser parser = new EmvParser(EmvTemplate.Builder().setProvider(new ScriptedProvider()).build());
		Application app = new Application();
		app.setAid(BytesUtils.fromString("A000000042000000"));

		ReflectionTestUtils.invokeMethod(parser, "extractExtendedData",
				BytesUtils.fromString("6F 10 84 08 A0 00 00 00 42 00 00 00 A5 04 9F 65 01 FF"), app);

		// Neither the card nor the AID names a kernel: nothing tells which
		// dictionary tag '9F65' has to be read in
		Assertions.assertThat((KernelEnum) ReflectionTestUtils.invokeMethod(parser, "resolveKernel", app)).isNull();
		Assertions.assertThat(app.getMagStripeData().isPresent()).isFalse();
		Assertions.assertThat(app.getMagStripeData().getPcvc3Track2()).isNull();
	}

	/**
	 * The Terminal Transaction Qualifiers a Kernel 3 reader pushes in the data
	 * field of its GET PROCESSING OPTIONS are 4 bytes long (EMV Contactless Book
	 * C-3 v2.12 Annex A Table A-2), where the PUNATC(Track2) of Kernel 2 is 2
	 * (Book C-2 v2.11 A.1.128). A '9F66' of another length is not the bitmap,
	 * whatever the kernel of the application says.
	 */
	@Test
	public void testPunatcTrack2IsOnlyReadOnItsSpecifiedLength() {
		EmvParser parser = new EmvParser(EmvTemplate.Builder().setProvider(new ScriptedProvider()).build());
		Application app = new Application();
		app.setAid(BytesUtils.fromString("A0000000041010"));

		// The Terminal Transaction Qualifiers this library sends, see
		// EmvTerminalTest, echoed back by a card in a record template
		ReflectionTestUtils.invokeMethod(parser, "extractExtendedData", BytesUtils.fromString("70 07 9F 66 04 B6 20 C0 00"),
				app);
		Assertions.assertThat((KernelEnum) ReflectionTestUtils.invokeMethod(parser, "resolveKernel", app))
				.isEqualTo(KernelEnum.MASTERCARD);
		Assertions.assertThat(app.getMagStripeData().getPunatcTrack2()).isNull();
		// The value is still reported as the card answered it
		Assertions.assertThat(BytesUtils.bytesToStringNoSpace(app.getTerminalTransactionQualifiersEcho())).isEqualTo("B620C000");
		Assertions.assertThat(app.getResolvedKernel()).isEqualTo(KernelEnum.MASTERCARD);

		// The same card answering its bitmap
		ReflectionTestUtils.invokeMethod(parser, "extractExtendedData", BytesUtils.fromString("70 05 9F 66 02 0E 70"), app);
		Assertions.assertThat(BytesUtils.bytesToStringNoSpace(app.getMagStripeData().getPunatcTrack2())).isEqualTo("0E70");
		// The first value found is the one kept
		Assertions.assertThat(BytesUtils.bytesToStringNoSpace(app.getTerminalTransactionQualifiersEcho())).isEqualTo("B620C000");
	}

	/**
	 * The codes the enumerations lose are kept as the card sent them: the
	 * numeric currency and country, the alphabetic countries, the digits of the
	 * service code, and the texts the card level fields overwrite (cardholder
	 * name, BIC, IBAN) are kept per application.
	 */
	@Test
	public void testRawCodesAndTextsArePerApplication() {
		EmvParser parser = new EmvParser(EmvTemplate.Builder().setProvider(new ScriptedProvider()).build());
		Application app = new Application();

		// 999 is neither an ISO 4217 currency nor an ISO 3166-1 country
		ReflectionTestUtils.invokeMethod(parser, "extractExtendedData",
				BytesUtils.fromString("9F42 02 0999 5F28 02 0999 5F56 03 465241 5F55 02 4652 5F30 02 0201"
						+ " 5F20 08 444F452F4A4F484E 9F0B 0B 444F452F4A4F484E2052 20 5F54 0B 4E4F4C41444532315348 4F"
						+ " 5F53 05 4445303131"),
				app);

		Assertions.assertThat(app.getCurrency()).isNull();
		Assertions.assertThat(app.getCurrencyCode()).isEqualTo(999);
		Assertions.assertThat(app.getIssuerCountryCode()).isEqualTo(CountryCodeEnum.FR);
		Assertions.assertThat(app.getIssuerCountryCodeNumeric()).isEqualTo(999);
		Assertions.assertThat(app.getIssuerCountryCodeAlpha3()).isEqualTo("FRA");
		Assertions.assertThat(app.getIssuerCountryCodeAlpha2()).isEqualTo("FR");
		// Service code '201': the three digits behind the Service bean
		Assertions.assertThat(app.getServiceCode()).isNotNull();
		Assertions.assertThat(app.getServiceCodeDigits()).isEqualTo("201");
		Assertions.assertThat(app.getCardholderName()).isEqualTo("DOE/JOHN");
		Assertions.assertThat(app.getCardholderNameExtended()).isEqualTo("DOE/JOHN R");
		Assertions.assertThat(app.getBic()).isEqualTo("NOLADE21SHO");
		Assertions.assertThat(app.getIban()).isEqualTo("DE011");

		// The first value found wins, a later response does not replace it
		ReflectionTestUtils.invokeMethod(parser, "extractExtendedData",
				BytesUtils.fromString("9F42 02 0978 5F20 05 534D495448 5F54 03 414243"), app);
		Assertions.assertThat(app.getCurrencyCode()).isEqualTo(999);
		Assertions.assertThat(app.getCardholderName()).isEqualTo("DOE/JOHN");
		Assertions.assertThat(app.getBic()).isEqualTo("NOLADE21SHO");
		// A name made of the separator alone is no name
		Application slash = new Application();
		ReflectionTestUtils.invokeMethod(parser, "extractExtendedData", BytesUtils.fromString("5F20 03 202F20"), slash);
		Assertions.assertThat(slash.getCardholderName()).isEqualTo("/");
		// Nothing read: everything stays unknown
		Application empty = new Application();
		ReflectionTestUtils.invokeMethod(parser, "extractExtendedData", BytesUtils.fromString("9F2A0102"), empty);
		Assertions.assertThat(empty.getCurrencyCode()).isEqualTo(AbstractData.UNKNOWN);
		Assertions.assertThat(empty.getIssuerCountryCodeNumeric()).isEqualTo(AbstractData.UNKNOWN);
		Assertions.assertThat(empty.getServiceCodeDigits()).isNull();
		Assertions.assertThat(empty.getCardholderName()).isNull();
	}

	/**
	 * The Card Transaction Qualifiers (tag '9F6C') are decoded for Kernel 3
	 * only, where they are 2 bytes long: byte 1 bit 8 online PIN required, byte
	 * 2 bit 7 issuer update processing supported.
	 */
	@Test
	public void testCardTransactionQualifiersAreDecodedForKernel3() {
		EmvParser parser = new EmvParser(EmvTemplate.Builder().setProvider(new ScriptedProvider()).build());
		Application visa = new Application();
		visa.setAid(BytesUtils.fromString("A0000000031010"));

		ReflectionTestUtils.invokeMethod(parser, "extractExtendedData", BytesUtils.fromString("77 05 9F 6C 02 80 40"), visa);

		Assertions.assertThat(visa.getResolvedKernel()).isEqualTo(KernelEnum.VISA);
		CardTransactionQualifiers ctq = visa.getCardTransactionQualifiersDecoded();
		Assertions.assertThat(ctq).isNotNull();
		Assertions.assertThat(BytesUtils.bytesToStringNoSpace(ctq.getRaw())).isEqualTo("8040");
		Assertions.assertThat(ctq.isOnlinePinRequired()).isTrue();
		Assertions.assertThat(ctq.isSignatureRequired()).isFalse();
		Assertions.assertThat(ctq.isConsumerDeviceCvmPerformed()).isFalse();
		Assertions.assertThat(ctq.isIssuerUpdateProcessingSupported()).isTrue();
		// The raw value stays available too
		Assertions.assertThat(BytesUtils.bytesToStringNoSpace(visa.getCardTransactionQualifiers())).isEqualTo("8040");

		// A Cartes Bancaires application names no kernel: raw only
		Application cb = new Application();
		cb.setAid(BytesUtils.fromString("A0000000421010"));
		ReflectionTestUtils.invokeMethod(parser, "extractExtendedData", BytesUtils.fromString("77 05 9F 6C 02 80 40"), cb);
		Assertions.assertThat(cb.getResolvedKernel()).isNull();
		Assertions.assertThat(cb.getCardTransactionQualifiersDecoded()).isNull();
		Assertions.assertThat(BytesUtils.bytesToStringNoSpace(cb.getCardTransactionQualifiers())).isEqualTo("8040");

		// A Visa application answering another length: not the qualifiers
		Application odd = new Application();
		odd.setAid(BytesUtils.fromString("A0000000031010"));
		ReflectionTestUtils.invokeMethod(parser, "extractExtendedData", BytesUtils.fromString("77 04 9F 6C 01 80"), odd);
		Assertions.assertThat(odd.getCardTransactionQualifiersDecoded()).isNull();
		Assertions.assertThat(BytesUtils.bytesToStringNoSpace(odd.getCardTransactionQualifiers())).isEqualTo("80");
	}

	/**
	 * The raw bytes of CDOL1, CDOL2, DDOL and TDOL are kept next to the parsed
	 * lists, even when a list is truncated and cannot be parsed.
	 */
	@Test
	public void testRawDataObjectListsAreKept() {
		// The parser only holds a weak reference to its template: the test
		// keeps it alive, otherwise a garbage collection between two calls
		// leaves the parser without card to fill
		EmvTemplate template = EmvTemplate.Builder().setProvider(new ScriptedProvider()).build();
		EmvParser parser = new EmvParser(template);
		Application app = new Application();

		// The TDOL ends on a tag without its length
		ReflectionTestUtils.invokeMethod(parser, "extractExtendedData",
				BytesUtils.fromString("70 13 8C 03 9F 02 06 8D 02 8A 02 9F 49 03 9F 37 04 97 02 9F 02"), app);

		Assertions.assertThat(BytesUtils.bytesToStringNoSpace(app.getRawCdol1())).isEqualTo("9F0206");
		Assertions.assertThat(app.getCdol1()).hasSize(1);
		Assertions.assertThat(app.getCdol1().get(0).getLength()).isEqualTo(6);
		Assertions.assertThat(BytesUtils.bytesToStringNoSpace(app.getRawCdol2())).isEqualTo("8A02");
		Assertions.assertThat(app.getCdol2()).hasSize(1);
		Assertions.assertThat(BytesUtils.bytesToStringNoSpace(app.getRawDdol())).isEqualTo("9F3704");
		Assertions.assertThat(app.getDdol()).hasSize(1);
		Assertions.assertThat(BytesUtils.bytesToStringNoSpace(app.getRawTdol())).isEqualTo("9F02");
		Assertions.assertThat(app.getTdol()).isEmpty();

		// A later response does not replace a list already read
		ReflectionTestUtils.invokeMethod(parser, "extractExtendedData", BytesUtils.fromString("70 05 8C 03 9F 03 06"), app);
		Assertions.assertThat(BytesUtils.bytesToStringNoSpace(app.getRawCdol1())).isEqualTo("9F0206");
		// Nothing read: nothing kept
		Application none = new Application();
		ReflectionTestUtils.invokeMethod(parser, "extractExtendedData", BytesUtils.fromString("9F2A0102"), none);
		Assertions.assertThat(none.getRawCdol1()).isNull();
		Assertions.assertThat(none.getRawTdol()).isNull();
		// The template is used up to the end, so that it lives up to the end
		Assertions.assertThat(template.getCard()).isNotNull();
	}

}
