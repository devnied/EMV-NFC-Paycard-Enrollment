package com.github.devnied.emvnfccard.parser.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.fest.assertions.Assertions;
import org.junit.Test;

import com.github.devnied.emvnfccard.exception.CommunicationException;
import com.github.devnied.emvnfccard.model.AbstractData;
import com.github.devnied.emvnfccard.model.Application;
import com.github.devnied.emvnfccard.model.EmvCard;
import com.github.devnied.emvnfccard.model.EmvTransactionRecord;
import com.github.devnied.emvnfccard.model.RecordData;
import com.github.devnied.emvnfccard.model.TransactionLog;
import com.github.devnied.emvnfccard.model.enums.CurrencyEnum;
import com.github.devnied.emvnfccard.parser.EmvTemplate;
import com.github.devnied.emvnfccard.parser.IProvider;
import com.github.devnied.emvnfccard.provider.ScriptedProvider;
import com.github.devnied.emvnfccard.provider.TestProvider;

import fr.devnied.bitlib.BytesUtils;

/**
 * Everything the reading of the transaction log (Log Entry '9F4D' / 'DF60',
 * Log Format '9F4F', READ RECORD of the log file) learns is kept on the
 * application, see {@link TransactionLog}, and the transactions carry the
 * codes their enumerations lose. The list of transactions itself is the same
 * as before.
 */
public class TransactionLogExposureTestP2 {

	/**
	 * GET DATA of the Log Format
	 */
	private static final String LOG_FORMAT_COMMAND = "80CA9F4F00";

	/**
	 * The Log Format of the Visa cards of the recorded traces
	 */
	private static final String VISA_LOG_FORMAT = "9F4F 10 9F02 06 9F27 01 9F1A 02 5F2A 02 9A 03 9C 01 9000";

	/**
	 * The Log Format of the MasterCardPpse3 trace, with the entries the
	 * transaction bean has no field for
	 */
	private static final String MASTERCARD_LOG_FORMAT = "9F4F 1A 9F27 01 9F02 06 5F2A 02 9A 03 9F36 02 9F52 06 DF3E 01 9F21 03 9F7C 14 9000";

	/**
	 * First log record of the MasterCardPpse3 trace
	 */
	private static final String MASTERCARD_RECORD = "40 000000002200 0949 110112 00E0 619022210000 00 502300 0000000000000000000000000000000000000000";

	/**
	 * File Control Information of the VisaCardPpse card: the Visa Log Entry
	 * ('DF60') comes before the Log Entry ('9F4D'), both with the same value
	 */
	private static final String VISA_FCI = "6F 37 84 07 A0 00 00 00 42 10 10 A5 2C 9F 38 18 9F 66 04 9F 02 06 9F 03 06 9F 1A 02 95 05 5F 2A 02 9A 03 9C 01 9F 37 04 BF 0C 0E DF 60 02 0B 1E DF 61 01 03 9F 4D 02 0B 1E";

	private EmvParser parser(final IProvider pProvider, final boolean pReadTransactions) {
		return new EmvParser(EmvTemplate.Builder().setProvider(pProvider)
				.setConfig(EmvTemplate.Config().setReadTransactions(pReadTransactions)).build());
	}

	private static String hex(final byte[] pValue) {
		return pValue == null ? null : BytesUtils.bytesToStringNoSpace(pValue);
	}

	/**
	 * Every READ RECORD of the log is kept with its status word, the empty
	 * slots go to the skipped transactions, the Visa artefact correction keeps
	 * the amount it corrected, and the numeric codes are kept next to the
	 * enumerations.
	 */
	@Test
	public void testEveryExchangeOfTheLogIsKept() throws CommunicationException {
		ScriptedProvider provider = new ScriptedProvider().on(LOG_FORMAT_COMMAND, VISA_LOG_FORMAT)
				// An empty slot: amount 1
				.on("00B2015C00", "000000000001 40 0250 0978 140316 00 9000")
				// The Visa artefact, a currency ISO 4217 does not assign
				.on("00B2025C00", "001500001024 40 0250 0999 140316 00 9000")
				// A regular transaction
				.on("00B2035C00", "000000004600 40 0250 0978 140316 20 9000");
		// The fourth record is answered '6A83' by the provider
		Application app = new Application();
		app.setRawFci(BytesUtils.fromString("9F4D 02 0B 04"));

		List<EmvTransactionRecord> list = parser(provider, true).extractLogEntry(BytesUtils.fromString("0B04"), app);

		TransactionLog log = app.getTransactionLog();
		Assertions.assertThat(log).isNotNull();
		Assertions.assertThat(hex(log.getRawLogEntry())).isEqualTo("0B04");
		Assertions.assertThat(log.getLogEntryTag()).isEqualTo("9F4D");
		Assertions.assertThat(log.getSfi()).isEqualTo(11);
		Assertions.assertThat(log.getDeclaredRecordCount()).isEqualTo(4);
		Assertions.assertThat(log.isRead()).isTrue();
		Assertions.assertThat(hex(log.getRawLogFormat())).isEqualTo("9F02069F27019F1A025F2A029A039C01");
		Assertions.assertThat(log.getLogFormat()).hasSize(6);
		Assertions.assertThat(log.getLogFormatStatusWord()).isEqualTo("9000");
		Assertions.assertThat(app.getGetDataStatusWords().get("9F4F")).isEqualTo("9000");

		// The four exchanges, the one that ended the loop included
		Assertions.assertThat(log.getRecords()).hasSize(4);
		RecordData first = log.getRecords().get(0);
		Assertions.assertThat(first.getSfi()).isEqualTo(11);
		Assertions.assertThat(first.getRecordNumber()).isEqualTo(1);
		Assertions.assertThat(first.getStatusWord()).isEqualTo("9000");
		Assertions.assertThat(hex(first.getRaw())).isEqualTo("000000000001400250097814031600");
		RecordData last = log.getRecords().get(3);
		Assertions.assertThat(last.getRecordNumber()).isEqualTo(4);
		Assertions.assertThat(last.getStatusWord()).isEqualTo("6A83");
		Assertions.assertThat(last.getRaw()).isEmpty();

		// The list returned is the one of before: the empty slot is left out
		Assertions.assertThat(list).hasSize(2);
		EmvTransactionRecord corrected = list.get(0);
		Assertions.assertThat(corrected.getRecordNumber()).isEqualTo(2);
		Assertions.assertThat(hex(corrected.getRaw())).isEqualTo("001500001024400250099914031600");
		Assertions.assertThat(corrected.getAmount()).isEqualTo(1024f);
		Assertions.assertThat(corrected.getOriginalAmount()).isEqualTo(1500001024f);
		Assertions.assertThat(corrected.getCurrency()).isEqualTo(CurrencyEnum.XXX);
		Assertions.assertThat(corrected.getCurrencyCode()).isEqualTo(999);
		Assertions.assertThat(corrected.getTerminalCountryCodeNumeric()).isEqualTo(250);
		Assertions.assertThat(corrected.getTransactionTypeCode()).isZero();
		// Every entry of this Log Format maps to a field
		Assertions.assertThat(corrected.getOtherTags()).isEmpty();
		EmvTransactionRecord regular = list.get(1);
		Assertions.assertThat(regular.getRecordNumber()).isEqualTo(3);
		Assertions.assertThat(regular.getAmount()).isEqualTo(4600f);
		Assertions.assertThat(regular.getOriginalAmount()).isEqualTo(4600f);
		Assertions.assertThat(regular.getCurrency()).isEqualTo(CurrencyEnum.EUR);
		Assertions.assertThat(regular.getCurrencyCode()).isEqualTo(978);
		Assertions.assertThat(regular.getTransactionTypeCode()).isEqualTo(0x20);

		// The empty slot
		Assertions.assertThat(log.getSkippedTransactions()).hasSize(1);
		EmvTransactionRecord skipped = log.getSkippedTransactions().get(0);
		Assertions.assertThat(skipped.getRecordNumber()).isEqualTo(1);
		Assertions.assertThat(skipped.getAmount()).isEqualTo(1f);
		Assertions.assertThat(skipped.getOriginalAmount()).isEqualTo(1f);
		Assertions.assertThat(skipped.getCurrency()).isEqualTo(CurrencyEnum.EUR);
		Assertions.assertThat(log.getUnparsedRecords()).isEmpty();
	}

	/**
	 * The one argument overload keeps the very same behaviour, it only has no
	 * application to report to.
	 */
	@Test
	public void testTheHistoricalOverloadIsUnchanged() throws CommunicationException {
		ScriptedProvider provider = new ScriptedProvider().on(LOG_FORMAT_COMMAND, VISA_LOG_FORMAT)
				.on("00B2015C00", "000000000001 40 0250 0978 140316 00 9000")
				.on("00B2025C00", "001500001024 40 0250 0999 140316 00 9000");
		EmvParser parser = parser(provider, true);

		List<EmvTransactionRecord> list = parser.extractLogEntry(BytesUtils.fromString("0B02"));

		Assertions.assertThat(list).hasSize(1);
		Assertions.assertThat(list.get(0).getAmount()).isEqualTo(1024f);
		Assertions.assertThat(list.get(0).getCurrency()).isEqualTo(CurrencyEnum.XXX);
		Assertions.assertThat(provider.getReceived()).isEqualTo(Arrays.asList(LOG_FORMAT_COMMAND, "00B2015C00", "00B2025C00"));
		// The log format on its own
		Assertions.assertThat(parser.getLogFormat()).hasSize(6);
	}

	/**
	 * The entries of the Log Format the bean has no field for are kept raw, in
	 * Log Format order: the MasterCardPpse3 card logs the ATC, the tags '9F52',
	 * 'DF3E' and '9F7C' on top of the standard data.
	 */
	@Test
	public void testTheOtherEntriesOfTheLogFormatAreKept() throws CommunicationException {
		ScriptedProvider provider = new ScriptedProvider().on(LOG_FORMAT_COMMAND, MASTERCARD_LOG_FORMAT)
				.on("00B2015C00", MASTERCARD_RECORD + " 9000");
		Application app = new Application();

		List<EmvTransactionRecord> list = parser(provider, true).extractLogEntry(BytesUtils.fromString("0B01"), app);

		Assertions.assertThat(list).hasSize(1);
		EmvTransactionRecord record = list.get(0);
		Assertions.assertThat(record.getAmount()).isEqualTo(2200f);
		Assertions.assertThat(record.getOriginalAmount()).isEqualTo(2200f);
		Assertions.assertThat(record.getCurrencyCode()).isEqualTo(949);
		Assertions.assertThat(record.getCurrency()).isEqualTo(CurrencyEnum.TRY);
		// Neither the terminal country nor the transaction type are logged
		Assertions.assertThat(record.getTerminalCountryCodeNumeric()).isEqualTo(AbstractData.UNKNOWN);
		Assertions.assertThat(record.getTransactionTypeCode()).isEqualTo(AbstractData.UNKNOWN);
		Assertions.assertThat(new ArrayList<String>(record.getOtherTags().keySet())).containsExactly("9F36", "9F52", "DF3E",
				"9F7C");
		Assertions.assertThat(hex(record.getOtherTags().get("9F36"))).isEqualTo("00E0");
		Assertions.assertThat(hex(record.getOtherTags().get("9F52"))).isEqualTo("619022210000");
		Assertions.assertThat(hex(record.getOtherTags().get("DF3E"))).isEqualTo("00");
		Assertions.assertThat(record.getOtherTags().get("9F7C")).hasSize(20);
		Assertions.assertThat(app.getTransactionLog().getLogFormat()).hasSize(9);
		// The FCI is unknown here, so is the tag the Log Entry came from
		Assertions.assertThat(app.getTransactionLog().getLogEntryTag()).isNull();
	}

	/**
	 * A record shorter than its Log Format never aborts the reading: whatever
	 * it holds is reported, either as a transaction whose details stop at the
	 * missing entry or as an unparsed record.
	 */
	@Test
	public void testRecordShorterThanTheLogFormat() throws CommunicationException {
		ScriptedProvider provider = new ScriptedProvider().on(LOG_FORMAT_COMMAND, MASTERCARD_LOG_FORMAT)
				// Only the cryptogram information, the amount and the currency
				.on("00B2015C00", "40 000000002200 0949 9000")
				.on("00B2025C00", MASTERCARD_RECORD + " 9000");
		Application app = new Application();

		List<EmvTransactionRecord> list = parser(provider, true).extractLogEntry(BytesUtils.fromString("0B02"), app);

		TransactionLog log = app.getTransactionLog();
		Assertions.assertThat(log.getRecords()).hasSize(2);
		Assertions.assertThat(log.getRecords().get(0).getRaw()).hasSize(9);
		// The second record is read whatever happened to the first one
		Assertions.assertThat(list).isNotEmpty();
		Assertions.assertThat(list.get(list.size() - 1).getRecordNumber()).isEqualTo(2);
		if (list.size() == 2) {
			// Parsed: the details stop at the first missing entry
			EmvTransactionRecord truncated = list.get(0);
			Assertions.assertThat(truncated.getCurrencyCode()).isEqualTo(949);
			Assertions.assertThat(truncated.getOtherTags()).isEmpty();
			Assertions.assertThat(log.getUnparsedRecords()).isEmpty();
		} else {
			Assertions.assertThat(log.getUnparsedRecords()).hasSize(1);
			Assertions.assertThat(hex(log.getUnparsedRecords().get(0))).isEqualTo("400000000022000949");
		}
	}

	/**
	 * The tag the Log Entry was read from is the first one found in the File
	 * Control Information, the way the reading looks it up.
	 */
	@Test
	public void testTheTagOfTheLogEntryIsNamed() throws CommunicationException {
		ScriptedProvider provider = new ScriptedProvider();
		EmvParser parser = parser(provider, false);
		byte[] logEntry = BytesUtils.fromString("0B1E");

		// The Visa Log Entry comes first in the VisaCardPpse card
		Assertions.assertThat(parser.findLogEntryTag(BytesUtils.fromString(VISA_FCI), logEntry)).isEqualTo("DF60");
		Assertions.assertThat(parser.findLogEntryTag(BytesUtils.fromString("BF0C 05 9F4D 02 0B1E"), logEntry)).isEqualTo("9F4D");
		// A value that is not the one of the FCI, or no FCI at all
		Assertions.assertThat(parser.findLogEntryTag(BytesUtils.fromString(VISA_FCI), BytesUtils.fromString("0B10"))).isNull();
		Assertions.assertThat(parser.findLogEntryTag(null, logEntry)).isNull();
		Assertions.assertThat(parser.findLogEntryTag(BytesUtils.fromString(VISA_FCI), null)).isNull();

		// Through the reading, with the transactions turned off: nothing is
		// asked to the card but the log is described
		Application app = new Application();
		app.setRawFci(BytesUtils.fromString(VISA_FCI));
		Assertions.assertThat(parser.extractLogEntry(logEntry, app)).isEmpty();
		Assertions.assertThat(provider.getReceived()).isEmpty();
		TransactionLog log = app.getTransactionLog();
		Assertions.assertThat(log.getLogEntryTag()).isEqualTo("DF60");
		Assertions.assertThat(log.getSfi()).isEqualTo(11);
		Assertions.assertThat(log.getDeclaredRecordCount()).isEqualTo(30);
		Assertions.assertThat(log.isRead()).isFalse();
		Assertions.assertThat(log.getRawLogFormat()).isNull();
		Assertions.assertThat(log.getLogFormatStatusWord()).isNull();
	}

	/**
	 * A malformed Log Entry, an invalid SFI or a refused Log Format leave the
	 * log described but unread, without a single exception.
	 */
	@Test
	public void testTheLogIsDescribedEvenWhenItCannotBeRead() throws CommunicationException {
		// One byte: the card is not even asked for its Log Format
		ScriptedProvider malformed = new ScriptedProvider();
		Application app = new Application();
		Assertions.assertThat(parser(malformed, true).extractLogEntry(BytesUtils.fromString("0B"), app)).isEmpty();
		Assertions.assertThat(malformed.getReceived()).isEmpty();
		TransactionLog log = app.getTransactionLog();
		Assertions.assertThat(hex(log.getRawLogEntry())).isEqualTo("0B");
		Assertions.assertThat(log.getSfi()).isEqualTo(AbstractData.UNKNOWN);
		Assertions.assertThat(log.getDeclaredRecordCount()).isEqualTo(AbstractData.UNKNOWN);
		Assertions.assertThat(log.isRead()).isFalse();
		Assertions.assertThat(log.getRecords()).isEmpty();

		// An SFI of 31 does not fit the 5 bits of P2: the Log Format is read,
		// the file is not
		ScriptedProvider invalidSfi = new ScriptedProvider().on(LOG_FORMAT_COMMAND, VISA_LOG_FORMAT);
		app = new Application();
		Assertions.assertThat(parser(invalidSfi, true).extractLogEntry(BytesUtils.fromString("1F01"), app)).isEmpty();
		Assertions.assertThat(invalidSfi.getReceived()).isEqualTo(Arrays.asList(LOG_FORMAT_COMMAND));
		log = app.getTransactionLog();
		Assertions.assertThat(log.getSfi()).isEqualTo(31);
		Assertions.assertThat(log.getDeclaredRecordCount()).isEqualTo(1);
		Assertions.assertThat(log.getLogFormat()).hasSize(6);
		Assertions.assertThat(log.isRead()).isFalse();
		Assertions.assertThat(log.getRecords()).isEmpty();

		// A card refusing the GET DATA of the Log Format
		ScriptedProvider refused = new ScriptedProvider().on(LOG_FORMAT_COMMAND, "6A88");
		app = new Application();
		Assertions.assertThat(parser(refused, true).extractLogEntry(BytesUtils.fromString("0B01"), app)).isEmpty();
		log = app.getTransactionLog();
		Assertions.assertThat(log.getLogFormatStatusWord()).isEqualTo("6A88");
		Assertions.assertThat(app.getGetDataStatusWords().get("9F4F")).isEqualTo("6A88");
		Assertions.assertThat(log.getRawLogFormat()).isNull();
		Assertions.assertThat(log.getLogFormat()).isEmpty();
		Assertions.assertThat(log.isRead()).isFalse();
		Assertions.assertThat(refused.getReceived()).isEqualTo(Arrays.asList(LOG_FORMAT_COMMAND));

		// No Log Entry at all: the log is described as absent
		ScriptedProvider none = new ScriptedProvider();
		app = new Application();
		Assertions.assertThat(parser(none, true).extractLogEntry(null, app)).isEmpty();
		Assertions.assertThat(none.getReceived()).isEmpty();
		Assertions.assertThat(app.getTransactionLog()).isNotNull();
		Assertions.assertThat(app.getTransactionLog().getRawLogEntry()).isNull();
		Assertions.assertThat(app.getTransactionLog().getLogEntryTag()).isNull();
		Assertions.assertThat(app.getTransactionLog().isRead()).isFalse();

		// The first READ RECORD refused, like the VisaCardNullTransaction card
		ScriptedProvider empty = new ScriptedProvider().on(LOG_FORMAT_COMMAND, VISA_LOG_FORMAT).on("00B2015C00", "6985");
		app = new Application();
		Assertions.assertThat(parser(empty, true).extractLogEntry(BytesUtils.fromString("0B0A"), app)).isEmpty();
		log = app.getTransactionLog();
		Assertions.assertThat(log.isRead()).isTrue();
		Assertions.assertThat(log.getRecords()).hasSize(1);
		Assertions.assertThat(log.getRecords().get(0).getStatusWord()).isEqualTo("6985");
		Assertions.assertThat(log.getRecords().get(0).getRaw()).isEmpty();
	}

	/**
	 * The status word of every GET DATA is recorded on the application, the
	 * values read are the ones of before.
	 */
	@Test
	public void testTheStatusWordOfEveryGetDataIsRecorded() throws CommunicationException {
		ScriptedProvider provider = new ScriptedProvider().on("80CA9F1700", "9F17 01 03 9000")
				// The ATC is refused
				.on("80CA9F3600", "6985").on("80CA9F1300", "9F13 02 0010 9000");
		EmvParser parser = parser(provider, true);
		Application app = new Application();

		Assertions.assertThat(parser.getLeftPinTry(app)).isEqualTo(3);
		Assertions.assertThat(parser.getTransactionCounter(app)).isEqualTo(AbstractParser.UNKNOW);
		Assertions.assertThat(parser.getLastOnlineTransactionCounter(app)).isEqualTo(16);
		// A tag the card does not hold, answered '6A82' by the provider
		Assertions.assertThat(parser.getData(com.github.devnied.emvnfccard.iso7816emv.EmvTags.OFFLINE_ACCUMULATOR_BALANCE, app))
				.isNull();

		Assertions.assertThat(new ArrayList<String>(app.getGetDataStatusWords().keySet())).containsExactly("9F17", "9F36", "9F13",
				"9F50");
		Assertions.assertThat(app.getGetDataStatusWords().get("9F17")).isEqualTo("9000");
		Assertions.assertThat(app.getGetDataStatusWords().get("9F36")).isEqualTo("6985");
		Assertions.assertThat(app.getGetDataStatusWords().get("9F13")).isEqualTo("9000");
		Assertions.assertThat(app.getGetDataStatusWords().get("9F50")).isEqualTo("6A82");

		// The overloads without application read the same values
		Assertions.assertThat(parser.getLeftPinTry()).isEqualTo(3);
		Assertions.assertThat(parser.getTransactionCounter()).isEqualTo(AbstractParser.UNKNOW);
		Assertions.assertThat(parser.getLastOnlineTransactionCounter()).isEqualTo(16);

		// A provider answering nothing at all records no status word
		IProvider silent = new IProvider() {
			@Override
			public byte[] transceive(final byte[] pCommand) {
				return null;
			}

			@Override
			public byte[] getAt() {
				return null;
			}
		};
		Application none = new Application();
		Assertions.assertThat(parser(silent, true).getLeftPinTry(none)).isEqualTo(AbstractParser.UNKNOW);
		Assertions.assertThat(none.getGetDataStatusWords()).isEmpty();
	}

	/**
	 * The GeldKartePpse trace, read by the GeldKarte parser: the log of the
	 * girocard application is declared (SFI 25, 10 records) and its first
	 * record is refused.
	 */
	@Test
	public void testGeldKarteTrace() throws CommunicationException {
		EmvCard card = EmvTemplate.Builder().setProvider(new TestProvider("GeldKartePpse"))
				.setConfig(EmvTemplate.Config().setContactLess(true).setReadCplc(true)).build().readEmvCard();
		Application app = card.getApplications().get(0);

		TransactionLog log = app.getTransactionLog();
		Assertions.assertThat(log).isNotNull();
		Assertions.assertThat(hex(log.getRawLogEntry())).isEqualTo("190A");
		Assertions.assertThat(log.getLogEntryTag()).isEqualTo("9F4D");
		Assertions.assertThat(log.getSfi()).isEqualTo(25);
		Assertions.assertThat(log.getDeclaredRecordCount()).isEqualTo(10);
		Assertions.assertThat(hex(log.getRawLogFormat())).isEqualTo("9F02069F27019F1A025F2A029A039C01");
		Assertions.assertThat(log.getLogFormat()).hasSize(6);
		Assertions.assertThat(log.getLogFormatStatusWord()).isEqualTo("9000");
		Assertions.assertThat(log.isRead()).isTrue();
		Assertions.assertThat(log.getRecords()).hasSize(1);
		Assertions.assertThat(log.getRecords().get(0).getSfi()).isEqualTo(25);
		Assertions.assertThat(log.getRecords().get(0).getRecordNumber()).isEqualTo(1);
		Assertions.assertThat(log.getRecords().get(0).getStatusWord()).isEqualTo("6985");
		Assertions.assertThat(log.getSkippedTransactions()).isEmpty();
		Assertions.assertThat(log.getUnparsedRecords()).isEmpty();
		// The GET DATA sent for this application
		Assertions.assertThat(app.getGetDataStatusWords().get("9F17")).isEqualTo("9000");
		Assertions.assertThat(app.getGetDataStatusWords().get("9F36")).isEqualTo("6985");
		Assertions.assertThat(app.getGetDataStatusWords().get("9F4F")).isEqualTo("9000");
		// The purse transaction of EF_BLOG is still the only one
		Assertions.assertThat(app.getListTransactions()).hasSize(1);
	}

}
