package com.github.devnied.emvnfccard.parser.impl;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Arrays;

import org.fest.assertions.Assertions;
import org.junit.Test;

import com.github.devnied.emvnfccard.enums.EmvCardScheme;
import com.github.devnied.emvnfccard.exception.CommunicationException;
import com.github.devnied.emvnfccard.model.AbstractData;
import com.github.devnied.emvnfccard.model.Application;
import com.github.devnied.emvnfccard.model.EmvCard;
import com.github.devnied.emvnfccard.model.GeldKarteData;
import com.github.devnied.emvnfccard.model.enums.CardStateEnum;
import com.github.devnied.emvnfccard.parser.EmvTemplate;
import com.github.devnied.emvnfccard.provider.ScriptedProvider;
import com.github.devnied.emvnfccard.provider.TestProvider;

import fr.devnied.bitlib.BytesUtils;

/**
 * The GeldKarte purse files (EF_ID, EF_BETRAG, EF_BLOG) are kept whole on the
 * application, decoded field by field, with the status words of the three
 * reads; the selection of the purse is described like any other application.
 */
public class GeldKarteDataTestP2 {

	private static final String PURSE_AID = "D27600002545500200";

	private static String hex(final byte[] pValue) {
		return pValue == null ? null : BytesUtils.bytesToStringNoSpace(pValue);
	}

	/**
	 * The GeldKartePpse trace: a girocard carrying a purse, read by the
	 * GeldKarte parser.
	 */
	@Test
	public void testGeldKarteTrace() throws CommunicationException {
		EmvCard card = EmvTemplate.Builder().setProvider(new TestProvider("GeldKartePpse"))
				.setConfig(EmvTemplate.Config().setContactLess(true).setReadCplc(true)).build().readEmvCard();
		Application app = card.getApplications().get(0);

		// The selection: the purse AID is requested, the card answers the
		// girocard Dedicated File Name
		Assertions.assertThat(app.getSelectStatusWord()).isEqualTo("9000");
		Assertions.assertThat(hex(app.getRequestedAid())).isEqualTo(PURSE_AID);
		Assertions.assertThat(hex(app.getSelectedName())).isEqualTo(PURSE_AID);
		Assertions.assertThat(hex(app.getAid())).isEqualTo("D27600002547410100");
		Assertions.assertThat(app.isExtendedSelectionRefused()).isFalse();
		Assertions.assertThat(app.getRawFci()).isNotNull();
		Assertions.assertThat(hex(app.getRawFci())).startsWith("6F5C8409D27600002547410100");
		Assertions.assertThat(card.getType()).isEqualTo(EmvCardScheme.GIROCARD);
		Assertions.assertThat(card.getAidType()).isEqualTo(EmvCardScheme.GIROCARD);
		Assertions.assertThat(hex(card.getTypeAid())).isEqualTo("D27600002547410100");
		// The texts of the FCI, per application
		Assertions.assertThat(app.getBic()).isEqualTo("NOLADE21SHO");
		Assertions.assertThat(app.getIban()).isEqualTo("DE01130510300000000000");
		Assertions.assertThat(app.getFciApplicationLabel()).isEqualTo("girocard");

		GeldKarteData purse = app.getGeldKarte();
		Assertions.assertThat(purse).isNotNull();
		// EF_ID
		Assertions.assertThat(hex(purse.getRawEfId())).isEqualTo("6725904415000011113D1712130117028045555201510006");
		Assertions.assertThat(purse.getEfIdStatusWord()).isEqualTo("9000");
		Assertions.assertThat(purse.getIndustryKey()).isEqualTo(0x67);
		Assertions.assertThat(purse.getBankSortCode()).isEqualTo("259044");
		Assertions.assertThat(purse.getCardNumber()).isEqualTo("1500001111");
		Assertions.assertThat(purse.getCardNumber()).isEqualTo(card.getTrack2().getCardNumber());
		Assertions.assertThat(purse.getCheckDigit()).isEqualTo(3);
		Assertions.assertThat(new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(purse.getActivationDate())).isEqualTo("2013-01-17");
		Assertions.assertThat(purse.getCountryCode()).isEqualTo(280);
		Assertions.assertThat(purse.getCurrency()).isEqualTo("EUR");
		Assertions.assertThat(purse.getCurrencyUnit()).isEqualTo(1);
		Assertions.assertThat(purse.getChipType()).isEqualTo(0x51);
		Assertions.assertThat(purse.getOsVersion()).isEqualTo(6);
		// EF_BETRAG
		Assertions.assertThat(hex(purse.getRawEfBetrag())).isEqualTo("000683020000020000000000");
		Assertions.assertThat(purse.getEfBetragStatusWord()).isEqualTo("9000");
		Assertions.assertThat(purse.getMaximumBalance()).isEqualTo(new BigDecimal("200.00"));
		Assertions.assertThat(purse.getMaximumTransaction()).isEqualTo(new BigDecimal("200.00"));
		Assertions.assertThat(app.getAmount()).isEqualTo(6.83f);
		// EF_BLOG
		Assertions.assertThat(purse.getRawEfBlogRecords()).hasSize(1);
		Assertions.assertThat(purse.getRawEfBlogRecords().get(0)).hasSize(39);
		Assertions.assertThat(hex(purse.getRawEfBlogRecords().get(0))).startsWith("51000B0001000002672253");
		Assertions.assertThat(purse.getEfBlogEndStatusWord()).isEqualTo("6985");
		Assertions.assertThat(app.getListTransactions()).hasSize(1);
		Assertions.assertThat(app.getListTransactions().get(0).getRaw()).isEqualTo(purse.getRawEfBlogRecords().get(0));
		Assertions.assertThat(app.getListTransactions().get(0).getRecordNumber()).isEqualTo(1);
		// The GET DATA of the purse
		Assertions.assertThat(app.getGetDataStatusWords().get("9F17")).isEqualTo("9000");
		Assertions.assertThat(app.getGetDataStatusWords().get("9F36")).isEqualTo("6985");
		Assertions.assertThat(app.getLeftPinTry()).isEqualTo(3);
		Assertions.assertThat(app.getTransactionCounter()).isEqualTo(AbstractData.UNKNOWN);
	}

	/**
	 * A purse answering a 10 bytes EF_ID and refusing its other files: the
	 * record is kept, the fields it holds are decoded, the rest is unknown and
	 * nothing is thrown.
	 */
	@Test
	public void testShortEfIdAndRefusedFiles() throws CommunicationException {
		ScriptedProvider provider = new ScriptedProvider()
				.on("00A4040009" + PURSE_AID + "00", "6F 0B 84 09 " + PURSE_AID + " 9000")
				.on("00B201BC00", "67 25 90 44 15 00 00 11 11 3D 9000");
		// EF_BETRAG and EF_BLOG are answered '6A83', the GET DATA '6A82'
		EmvTemplate template = EmvTemplate.Builder().setProvider(provider).build();
		Application app = new Application();
		app.setAid(BytesUtils.fromString(PURSE_AID));

		Assertions.assertThat(new GeldKarteParser(template).parse(app)).isTrue();

		Assertions.assertThat(template.getCard().getState()).isEqualTo(CardStateEnum.ACTIVE);
		Assertions.assertThat(app.getSelectStatusWord()).isEqualTo("9000");
		Assertions.assertThat(hex(app.getSelectedName())).isEqualTo(PURSE_AID);
		GeldKarteData purse = app.getGeldKarte();
		Assertions.assertThat(purse).isNotNull();
		Assertions.assertThat(hex(purse.getRawEfId())).isEqualTo("6725904415000011113D");
		Assertions.assertThat(purse.getEfIdStatusWord()).isEqualTo("9000");
		Assertions.assertThat(purse.getIndustryKey()).isEqualTo(0x67);
		Assertions.assertThat(purse.getBankSortCode()).isEqualTo("259044");
		Assertions.assertThat(purse.getCardNumber()).isEqualTo("1500001111");
		Assertions.assertThat(purse.getCheckDigit()).isEqualTo(3);
		Assertions.assertThat(purse.getActivationDate()).isNull();
		Assertions.assertThat(purse.getCountryCode()).isEqualTo(AbstractData.UNKNOWN);
		Assertions.assertThat(purse.getCurrency()).isNull();
		Assertions.assertThat(purse.getCurrencyUnit()).isEqualTo(AbstractData.UNKNOWN);
		Assertions.assertThat(purse.getChipType()).isEqualTo(AbstractData.UNKNOWN);
		Assertions.assertThat(purse.getOsVersion()).isEqualTo(AbstractData.UNKNOWN);
		// The track is not built from a record that short, as before
		Assertions.assertThat(template.getCard().getTrack2()).isNull();
		// The refused files
		Assertions.assertThat(purse.getRawEfBetrag()).isNull();
		Assertions.assertThat(purse.getEfBetragStatusWord()).isEqualTo("6A83");
		Assertions.assertThat(purse.getMaximumBalance()).isNull();
		Assertions.assertThat(purse.getMaximumTransaction()).isNull();
		// The balance keeps its default
		Assertions.assertThat(app.getAmount()).isEqualTo(new Application().getAmount());
		Assertions.assertThat(purse.getRawEfBlogRecords()).isEmpty();
		Assertions.assertThat(purse.getEfBlogEndStatusWord()).isEqualTo("6A83");
		Assertions.assertThat(app.getListTransactions()).isEmpty();
		Assertions.assertThat(app.getGetDataStatusWords().get("9F17")).isEqualTo("6A82");
		Assertions.assertThat(app.getGetDataStatusWords().get("9F36")).isEqualTo("6A82");
		// No Log Entry in that FCI
		Assertions.assertThat(app.getTransactionLog()).isNotNull();
		Assertions.assertThat(app.getTransactionLog().isRead()).isFalse();
	}

	/**
	 * The limits of EF_BETRAG are packed BCD: one that is not is reported as
	 * unknown, the other one is still read. A malformed EF_BLOG record is kept
	 * raw even though it produces no transaction.
	 */
	@Test
	public void testMalformedLimitsAndRecords() throws CommunicationException {
		ScriptedProvider provider = new ScriptedProvider()
				.on("00A4040009" + PURSE_AID + "00", "6F 0B 84 09 " + PURSE_AID + " 9000")
				.on("00B201C400", "00 06 83 02 00 00 AB CD EF 9000")
				// Too short to be a transaction
				.on("00B201EC00", "51 00 0B 00 01 9000")
				.on("00B202EC00", "6985");
		EmvTemplate template = EmvTemplate.Builder().setProvider(provider).build();
		Application app = new Application();
		app.setAid(BytesUtils.fromString(PURSE_AID));

		new GeldKarteParser(template).parse(app);

		GeldKarteData purse = app.getGeldKarte();
		Assertions.assertThat(hex(purse.getRawEfBetrag())).isEqualTo("000683020000ABCDEF");
		Assertions.assertThat(purse.getMaximumBalance()).isEqualTo(new BigDecimal("200.00"));
		Assertions.assertThat(purse.getMaximumTransaction()).isNull();
		Assertions.assertThat(app.getAmount()).isEqualTo(6.83f);
		Assertions.assertThat(purse.getRawEfBlogRecords()).hasSize(1);
		Assertions.assertThat(hex(purse.getRawEfBlogRecords().get(0))).isEqualTo("51000B0001");
		Assertions.assertThat(purse.getEfBlogEndStatusWord()).isEqualTo("6985");
		Assertions.assertThat(app.getListTransactions()).isEmpty();
		Assertions.assertThat(purse.getEfIdStatusWord()).isEqualTo("6A83");
		Assertions.assertThat(purse.getRawEfId()).isNull();
	}

	/**
	 * The decoding of EF_ID on its own: bounds checked, tolerant of values
	 * that are not encoded as documented.
	 */
	@Test
	public void testEfIdDecoding() {
		GeldKarteParser parser = new GeldKarteParser(EmvTemplate.Builder().setProvider(new ScriptedProvider()).build());

		GeldKarteData purse = new GeldKarteData();
		parser.decodeEfId(null, purse);
		parser.decodeEfId(new byte[0], purse);
		parser.decodeEfId(BytesUtils.fromString("67"), null);
		Assertions.assertThat(purse.getIndustryKey()).isEqualTo(AbstractData.UNKNOWN);

		// An activation date and a country that are not BCD
		byte[] record = BytesUtils.fromString("67 25 90 44 15 00 00 11 11 3D 17 12 AB CD EF 0A BC 45 55 52 01 51 00 06");
		parser.decodeEfId(record, purse);
		Assertions.assertThat(purse.getIndustryKey()).isEqualTo(0x67);
		Assertions.assertThat(purse.getActivationDate()).isNull();
		Assertions.assertThat(purse.getCountryCode()).isEqualTo(AbstractData.UNKNOWN);
		Assertions.assertThat(purse.getCurrency()).isEqualTo("EUR");
		Assertions.assertThat(purse.getOsVersion()).isEqualTo(6);

		// A record ending right after the chip type
		GeldKarteData shorter = new GeldKarteData();
		parser.decodeEfId(Arrays.copyOf(record, 22), shorter);
		Assertions.assertThat(shorter.getChipType()).isEqualTo(0x51);
		Assertions.assertThat(shorter.getOsVersion()).isEqualTo(AbstractData.UNKNOWN);

		// The amounts
		Assertions.assertThat(parser.parseBcdAmount(BytesUtils.fromString("020000"))).isEqualTo(new BigDecimal("200.00"));
		Assertions.assertThat(parser.parseBcdAmount(BytesUtils.fromString("ABCDEF"))).isNull();
		Assertions.assertThat(parser.parseBcdAmount(null)).isNull();
		Assertions.assertThat(parser.parseBcdAmount(new byte[0])).isNull();
	}

}
