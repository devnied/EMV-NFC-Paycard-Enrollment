/*
 * Copyright (C) 2019 MILLAU Julien
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.devnied.emvnfccard.utils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.devnied.emvnfccard.enums.EmvCardScheme;
import com.github.devnied.emvnfccard.iso7816emv.EmvTags;
import com.github.devnied.emvnfccard.iso7816emv.ITag;
import com.github.devnied.emvnfccard.model.Afl;
import com.github.devnied.emvnfccard.model.CPLC;
import com.github.devnied.emvnfccard.model.CVM;
import com.github.devnied.emvnfccard.model.EmvTrack1;
import com.github.devnied.emvnfccard.model.EmvTrack2;
import com.github.devnied.emvnfccard.model.enums.CountryCodeEnum;
import com.github.devnied.emvnfccard.model.enums.CurrencyEnum;
import com.github.devnied.emvnfccard.model.enums.KernelEnum;
import com.github.devnied.emvnfccard.model.enums.KernelTypeEnum;
import com.github.devnied.emvnfccard.model.enums.ServiceCode1Enum;
import com.github.devnied.emvnfccard.model.enums.ServiceCode2Enum;
import com.github.devnied.emvnfccard.model.enums.ServiceCode3Enum;

import fr.devnied.bitlib.BytesUtils;

/**
 * Utility class used to turn the raw value of an EMV data object into the human
 * readable lines {@link TlvUtil#prettyPrintAPDUResponse(byte[])} appends under
 * the hexadecimal dump.
 * <p>
 * The contract is deliberately narrow, because the only caller is a debug
 * printer: {@link #decode(ITag, byte[])} never throws, never returns null and
 * never replaces the raw value, it only adds lines. A data object this class
 * does not know, or a value it cannot make sense of, simply produces nothing
 * and the printer keeps the output it had.
 * </p>
 * <p>
 * The decoded text is English only, like every other prose surface of the
 * library ({@link com.github.devnied.emvnfccard.enums.SwEnum#getDetail()},
 * {@link com.github.devnied.emvnfccard.model.enums.CvmMethodEnum#getLabel()}):
 * it is read in a log next to English tag names, not by a cardholder.
 * </p>
 * <p>
 * Where a bit is reserved for the contactless kernels and two of them give it
 * opposite meanings, both readings are printed, each prefixed by its kernel. A
 * printer reading a bare TLV has no way to know which kernel produced it, and
 * a single unqualified label would be wrong for one scheme out of two.
 * </p>
 * <p>
 * One method of this class is not a printing one:
 * {@link #parseAflEntries(byte[])} splits an Application File Locator, and
 * {@link com.github.devnied.emvnfccard.parser.impl.EmvParser#parseAflEntries(byte[])}
 * delegates to it. The reader and the printer must never disagree on where a
 * record is, so the split is written once and lives with the decoding of the
 * data object it belongs to.
 * </p>
 *
 * @author MILLAU Julien
 *
 */
public final class TagValueDecoder {

	/**
	 * Class logger
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(TagValueDecoder.class);

	/**
	 * Highest number of lines a single data object may contribute. A 252 bytes
	 * long CVM list holds 122 rules and an Application File Locator of the same
	 * size holds 62 entries: printed in full they bury the response they are
	 * part of.
	 */
	public static final int MAX_DECODED_LINES = 32;

	/**
	 * Line appended in place of the entries left out once
	 * {@link #MAX_DECODED_LINES} is reached
	 */
	private static final String TRUNCATED = "... more entries not listed";

	/**
	 * Size of an Application File Locator entry (EMV 4.3 Book 3 section 10.2)
	 */
	private static final int AFL_ENTRY_SIZE = 4;

	/**
	 * Size of the amount X and Y header of a CVM list (tag '8E')
	 */
	private static final int CVM_LIST_HEADER_SIZE = 8;

	/**
	 * Length of a Registered Application Provider Identifier, the first part of
	 * an AID (ISO/IEC 7816-5)
	 */
	private static final int RID_LENGTH = 5;

	/**
	 * Meaning given to a bit the specification reserves. Only the bits that are
	 * set are printed, so this line always reports an anomaly: the card, or the
	 * reading of it, does not follow the specification.
	 */
	private static final String RFU_SET = "RFU bit set, the specification requires it to be 0";

	/**
	 * Value the service code enums use where the digit carries no qualifier
	 */
	private static final String NO_QUALIFIER = "None";

	// The bit tables below read from the most significant bit of the first byte
	// down to the least significant bit of the last one: the entry of index i
	// is the bit 8 - i % 8 of the byte i / 8 + 1. A null entry is a bit the
	// specification reserves. A meaning the contactless kernels disagree on is
	// prefixed by the kernel it belongs to, [K2] Mastercard, [K3] Visa, [K4]
	// American Express, [K6] Discover, [K7] UnionPay: a printer reading a bare
	// data object cannot know which kernel produced it.

	/**
	 * Application Interchange Profile, tag '82' (EMV 4.4 Book 3 Annex C1 table
	 * 41). Byte 1 b8 is reserved before EMV 4.4, and the bits Book 3 reserves
	 * for the contactless kernels are given opposite meanings by Kernel 2 and
	 * Kernel 3, so both are printed.
	 */
	private static final String[] AIP_BITS = { //
	"XDA supported (reserved before EMV 4.4)", //
			"SDA supported", //
			"DDA supported", //
			"cardholder verification is supported", //
			"terminal risk management is to be performed", //
			"issuer authentication is supported", //
			"[K2] on device cardholder verification supported, reserved for the kernels otherwise", //
			"CDA supported", //
			"[K2] EMV mode supported, [K3] mag-stripe mode supported, [K4] EMV and mag-stripe modes", //
			"[K4] contactless mobile supported, reserved for the kernels otherwise", //
			"[K4] host card emulation supported, reserved for the kernels otherwise", //
			null, null, null, null, //
			"[K2] relay resistance protocol supported, reserved for the kernels otherwise" };

	/**
	 * Application Usage Control, tag '9F07' (EMV 4.4 Book 3 Annex C2 table 42,
	 * unchanged since EMV 4.3)
	 */
	private static final String[] AUC_BITS = { //
	"valid for domestic cash transactions", //
			"valid for international cash transactions", //
			"valid for domestic goods", //
			"valid for international goods", //
			"valid for domestic services", //
			"valid for international services", //
			"valid at ATMs", //
			"valid at terminals other than ATMs", //
			"domestic cashback allowed", //
			"international cashback allowed", //
			null, null, null, null, null, null };

	/**
	 * Transaction Status Information, tag '9B' (EMV 4.4 Book 3 Annex C5)
	 */
	private static final String[] TSI_BITS = { //
	"offline data authentication was performed", //
			"cardholder verification was performed", //
			"card risk management was performed", //
			"issuer authentication was performed", //
			"terminal risk management was performed", //
			"script processing was performed", //
			null, null, null, null, null, null, null, null, null, null };

	/**
	 * Terminal Verification Results, tag '95' (EMV 4.4 Book 3 Annex C6). The
	 * Issuer Action Codes '9F0D', '9F0E' and '9F0F' have the very same layout
	 * (EMV 4.4 Book 3 section 10.7).
	 */
	private static final String[] TVR_BITS = { //
	"offline data authentication was not performed", //
			"SDA failed", //
			"ICC data missing", //
			"card appears on the terminal exception file", //
			"DDA failed", //
			"CDA failed", //
			"SDA selected", //
			"XDA selected (EMV 4.4)", //
			"the card and the terminal have different application versions", //
			"expired application", //
			"application not yet effective", //
			"requested service not allowed for the card product", //
			"new card", //
			null, //
			"biometric performed and successful (EMV 4.4)", //
			"biometric template format not supported (EMV 4.4)", //
			"cardholder verification was not successful", //
			"unrecognised CVM", //
			"PIN try limit exceeded", //
			"PIN entry required and the PIN pad is not present or not working", //
			"PIN entry required, PIN pad present, but the PIN was not entered", //
			"online CVM captured (EMV 4.4), online PIN entered (EMV 4.3)", //
			"biometric required but the capture device is not working (EMV 4.4)", //
			"biometric required, capture device present, but the subtype was bypassed (EMV 4.4)", //
			"transaction exceeds the floor limit", //
			"lower consecutive offline limit exceeded", //
			"upper consecutive offline limit exceeded", //
			"transaction selected randomly for online processing", //
			"merchant forced the transaction online", //
			"biometric try limit exceeded (EMV 4.4)", //
			"a selected biometric type is not supported (EMV 4.4)", //
			"XDA signature verification failed (EMV 4.4)", //
			"default TDOL used", //
			"issuer authentication failed", //
			"script processing failed before the final GENERATE AC", //
			"script processing failed after the final GENERATE AC", //
			"[K2] relay resistance threshold exceeded, reserved for the kernels otherwise", //
			"CA ECC key missing (EMV 4.4), [K2] relay resistance time limits exceeded", //
			"ECC key recovery failed (EMV 4.4), [K2] relay resistance performed, high bit", //
			"[K2] relay resistance performed, low bit, reserved for the kernels otherwise" };

	/**
	 * Outcome of the relay resistance protocol, tag '95' byte 5 b2-b1, read by
	 * the Kernel 2 alone (EMV Contactless Book C-2)
	 */
	private static final String[] RELAY_RESISTANCE = { //
	"relay resistance protocol not supported", //
			"relay resistance protocol not performed", //
			"relay resistance protocol performed", //
			"RFU" };

	/**
	 * Terminal Capabilities, tag '9F33' (EMV 4.4 Book 4 Annex A2)
	 */
	private static final String[] TERMINAL_CAPABILITIES_BITS = { //
	"manual key entry", //
			"magnetic stripe", //
			"IC with contacts", //
			null, null, null, null, null, //
			"plaintext PIN for ICC verification", //
			"enciphered PIN for online verification", //
			"signature", //
			"enciphered PIN for offline verification (RSA)", //
			"no CVM required", //
			"online biometric (EMV 4.4)", //
			"offline biometric (EMV 4.4)", //
			"enciphered PIN for offline verification (ECC) (EMV 4.4)", //
			"SDA", //
			"DDA", //
			"card capture", //
			null, //
			"CDA", //
			"XDA (EMV 4.4)", //
			null, null };

	/**
	 * Additional Terminal Capabilities, tag '9F40' (EMV 4.4 Book 4 Annex A3)
	 */
	private static final String[] ADDITIONAL_TERMINAL_CAPABILITIES_BITS = { //
	"cash", "goods", "services", "cashback", "inquiry", "transfer", "payment", "administrative", //
			"cash deposit", null, null, null, null, null, null, null, //
			"numeric keys", "alphabetic and special characters keys", "command keys", "function keys", //
			null, null, null, null, //
			"print or electronic, attendant", "print or electronic, cardholder", //
			"display, attendant", "display, cardholder", null, null, //
			"code table 10 (ISO/IEC 8859-10)", "code table 9 (ISO/IEC 8859-9)", //
			"code table 8 (ISO/IEC 8859-8)", "code table 7 (ISO/IEC 8859-7)", //
			"code table 6 (ISO/IEC 8859-6)", "code table 5 (ISO/IEC 8859-5)", //
			"code table 4 (ISO/IEC 8859-4)", "code table 3 (ISO/IEC 8859-3)", //
			"code table 2 (ISO/IEC 8859-2)", "code table 1 (ISO/IEC 8859-1)" };

	/**
	 * Terminal Transaction Qualifiers, tag '9F66' (EMV Contactless Book A and
	 * Book C-3). The Kernel 7 publishes a table of its own that contradicts
	 * them on five bits, which is why they carry both readings. On the Kernel 2
	 * the very same tag is the PUNATC(Track2), a mask of digit positions, which
	 * this printer does not decode.
	 */
	private static final String[] TTQ_BITS = { //
	"[K3, K6] mag-stripe mode supported, [K7] RFU", //
			"[K7] full transaction flow supported on the contactless interface, RFU otherwise", //
			"EMV mode supported", //
			"EMV contact chip supported", //
			"offline only reader", //
			"online PIN supported", //
			"signature supported", //
			"[Book A] offline data authentication for online authorisations supported, RFU otherwise", //
			"online cryptogram required", //
			"CVM required", //
			"[K3, K6] contact chip offline PIN supported, [K7] RFU", //
			null, null, null, null, null, //
			"[K3, K6] issuer update processing supported, [K7] RFU", //
			"consumer device CVM supported", //
			null, null, //
			"[K6] consumer device CVM required, RFU otherwise", //
			null, null, null, //
			"[K7] fDDA v1.0 supported, RFU otherwise", //
			null, null, null, null, null, null, null };

	/**
	 * Card Transaction Qualifiers, tag '9F6C' (EMV Contactless Book C-3). The
	 * Kernel 7 reads four of these bits differently and the Kernel 2 does not
	 * define the tag at all in the book that could be read.
	 */
	private static final String[] CTQ_BITS = { //
	"[K3] online PIN required", //
			"[K3] signature required", //
			"[K3] go online if offline data authentication fails and the reader is online capable", //
			"[K3] switch interface if offline data authentication fails, [K7] terminate the transaction", //
			"[K3] go online if the application has expired", //
			"[K3] switch interface for cash transactions, [K7] RFU", //
			"[K3] switch interface for cashback transactions, [K7] RFU", //
			null, //
			"[K3] consumer device CVM performed (a Kernel 7 card leaves this bit clear)", //
			"[K3] the card supports issuer update processing at the point of sale, [K7] RFU", //
			null, null, null, null, null, null };

	/**
	 * Second byte of the Application Capabilities Information, tag '9F5D' (EMV
	 * Contactless Book C-2)
	 */
	private static final String[] ACI_BYTE_2_BITS = { //
	null, null, null, null, null, //
			"[K2] support for field off detection", //
			"[K2] support for balance reading", //
			"[K2] CDA supported over TC, ARQC and AAC" };

	/**
	 * Type of cryptogram, tag '9F27' b8-b7 (EMV 4.4 Book 3 table 15)
	 */
	private static final String[] CRYPTOGRAM_TYPES = { //
	"AAC, application authentication cryptogram, the transaction is declined", //
			"TC, transaction certificate, the transaction is approved offline", //
			"ARQC, authorisation request cryptogram, the transaction goes online", //
			"RFU" };

	/**
	 * Reason or advice code, tag '9F27' b3-b1 (EMV 4.4 Book 3 table 15)
	 */
	private static final String[] ADVICE_CODES = { //
	"no information given", //
			"service not allowed", //
			"PIN try limit exceeded", //
			"issuer authentication failed", //
			"RFU", "RFU", "RFU", "RFU" };

	/**
	 * Cardholder verification methods of the codes '00' to '0F' (EMV 4.4 Book 3
	 * Annex C3 table 43), the codes above being ranges
	 */
	private static final String[] CVM_METHODS = { //
	"fail CVM processing", //
			"plaintext PIN verification performed by the card", //
			"enciphered PIN verified online", //
			"plaintext PIN verification performed by the card and signature", //
			"enciphered PIN verification performed by the card", //
			"enciphered PIN verification performed by the card and signature", //
			"facial biometric verified offline by the card (EMV 4.4)", //
			"facial biometric verified online (EMV 4.4)", //
			"finger biometric verified offline by the card (EMV 4.4)", //
			"finger biometric verified online (EMV 4.4)", //
			"palm biometric verified offline by the card (EMV 4.4)", //
			"palm biometric verified online (EMV 4.4)", //
			"iris biometric verified offline by the card (EMV 4.4)", //
			"iris biometric verified online (EMV 4.4)", //
			"voice biometric verified offline by the card (EMV 4.4)", //
			"voice biometric verified online (EMV 4.4)" };

	/**
	 * Conditions of the codes '00' to '09' (EMV 4.4 Book 3 Annex C3 table 44)
	 */
	private static final String[] CVM_CONDITIONS = { //
	"always", //
			"if unattended cash", //
			"if not unattended cash and not manual cash and not purchase with cashback", //
			"if the terminal supports the CVM", //
			"if manual cash", //
			"if purchase with cashback", //
			"if in the application currency and under X value", //
			"if in the application currency and over X value", //
			"if in the application currency and under Y value", //
			"if in the application currency and over Y value" };

	/**
	 * Terminal types, tag '9F35' (EMV 4.4 Book 4 table 24)
	 */
	private static final Map<Integer, String> TERMINAL_TYPES = buildTerminalTypes();

	/**
	 * Point of service entry modes, tag '9F39'. EMV takes the two digits from
	 * the ISO 8583 data element 22 without reproducing its table.
	 */
	private static final Map<Integer, String> POS_ENTRY_MODES = buildPosEntryModes();

	/**
	 * Transaction types, tag '9C', the first two digits of the ISO 8583
	 * processing code. EMV leaves the values to the payment systems.
	 */
	private static final Map<Integer, String> TRANSACTION_TYPES = buildTransactionTypes();

	/**
	 * Authorisation response codes, tag '8A'. EMV defines the offline ones, the
	 * others come from the payment system (EMV 4.4 Book 4 Annex A4).
	 */
	private static final Map<String, String> AUTHORISATION_RESPONSE_CODES = buildAuthorisationResponseCodes();

	// Identifier of every decoder, used as the value of the dispatch table.
	// An int is used rather than an enum or a strategy object so that the
	// dispatch stays a single switch and the miss path allocates nothing.
	private static final int DEC_AID = 1;
	private static final int DEC_DF_NAME = 2;
	private static final int DEC_PAN = 3;
	private static final int DEC_PAN_SEQUENCE = 4;
	private static final int DEC_DATE = 5;
	private static final int DEC_TIME = 6;
	private static final int DEC_AMOUNT_NUMERIC = 7;
	private static final int DEC_AMOUNT_BINARY = 8;
	private static final int DEC_CURRENCY = 9;
	private static final int DEC_CURRENCY_EXPONENT = 10;
	private static final int DEC_COUNTRY_NUMERIC = 11;
	private static final int DEC_COUNTRY_ALPHA2 = 12;
	private static final int DEC_COUNTRY_ALPHA3 = 13;
	private static final int DEC_SERVICE_CODE = 14;
	private static final int DEC_TRACK1 = 15;
	private static final int DEC_TRACK2 = 16;
	private static final int DEC_LANGUAGE = 17;
	private static final int DEC_CODE_TABLE = 18;
	private static final int DEC_COUNTER = 19;
	private static final int DEC_AFL = 20;
	private static final int DEC_SFI = 21;
	private static final int DEC_PRIORITY = 22;
	private static final int DEC_CVM_LIST = 23;
	private static final int DEC_LOG_ENTRY = 24;
	private static final int DEC_VERSION_NUMBER = 25;
	private static final int DEC_CPLC = 26;
	private static final int DEC_IIN = 27;
	private static final int DEC_MERCHANT_CATEGORY = 28;
	private static final int DEC_AIP = 29;
	private static final int DEC_AUC = 30;
	private static final int DEC_TSI = 31;
	private static final int DEC_TVR = 32;
	private static final int DEC_TERMINAL_CAPABILITIES = 33;
	private static final int DEC_ADDITIONAL_TERMINAL_CAPABILITIES = 34;
	private static final int DEC_TTQ = 35;
	private static final int DEC_CTQ = 36;
	private static final int DEC_CID = 37;
	private static final int DEC_CVM_RESULTS = 38;
	private static final int DEC_TERMINAL_TYPE = 39;
	private static final int DEC_POS_ENTRY_MODE = 40;
	private static final int DEC_TRANSACTION_TYPE = 41;
	private static final int DEC_AUTHORISATION_RESPONSE = 42;
	private static final int DEC_KERNEL_IDENTIFIER = 43;
	private static final int DEC_APPLICATION_CAPABILITIES = 44;
	private static final int DEC_FORM_FACTOR = 45;
	private static final int DEC_RESPONSE_TEMPLATE_1 = 46;

	/**
	 * Data object to decoder table. The key is the tag itself: {@code TagImpl}
	 * compares and hashes on the tag bytes, so an unknown tag manufactured by
	 * {@link EmvTags#createUnknownTag(byte[])} still finds its decoder, and
	 * looking one up costs no allocation.
	 */
	private static final Map<ITag, Integer> DECODERS = buildDecoders();

	/**
	 * Method used to build the data object to decoder table
	 *
	 * @return the dispatch table
	 */
	private static Map<ITag, Integer> buildDecoders() {
		Map<ITag, Integer> map = new HashMap<ITag, Integer>();
		// @formatter:off
		map.put(EmvTags.AID_CARD,                        Integer.valueOf(DEC_AID));
		map.put(EmvTags.AID_TERMINAL,                    Integer.valueOf(DEC_AID));
		map.put(EmvTags.DEDICATED_FILE_NAME,             Integer.valueOf(DEC_DF_NAME));
		map.put(EmvTags.DDF_NAME,                        Integer.valueOf(DEC_DF_NAME));
		map.put(EmvTags.PAN,                             Integer.valueOf(DEC_PAN));
		map.put(EmvTags.PAN_SEQUENCE_NUMBER,             Integer.valueOf(DEC_PAN_SEQUENCE));
		map.put(EmvTags.APP_EXPIRATION_DATE,             Integer.valueOf(DEC_DATE));
		map.put(EmvTags.APP_EFFECTIVE_DATE,              Integer.valueOf(DEC_DATE));
		map.put(EmvTags.TRANSACTION_DATE,                Integer.valueOf(DEC_DATE));
		map.put(EmvTags.TRANSACTION_TIME,                Integer.valueOf(DEC_TIME));
		map.put(EmvTags.AMOUNT_AUTHORISED_NUMERIC,       Integer.valueOf(DEC_AMOUNT_NUMERIC));
		map.put(EmvTags.AMOUNT_OTHER_NUMERIC,            Integer.valueOf(DEC_AMOUNT_NUMERIC));
		map.put(EmvTags.AMOUNT_REFERENCE_CURRENCY,       Integer.valueOf(DEC_AMOUNT_BINARY));
		map.put(EmvTags.AMOUNT_AUTHORISED_BINARY,        Integer.valueOf(DEC_AMOUNT_BINARY));
		map.put(EmvTags.AMOUNT_OTHER_BINARY,             Integer.valueOf(DEC_AMOUNT_BINARY));
		map.put(EmvTags.TRANSACTION_CURRENCY_CODE,       Integer.valueOf(DEC_CURRENCY));
		map.put(EmvTags.APPLICATION_CURRENCY_CODE,       Integer.valueOf(DEC_CURRENCY));
		map.put(EmvTags.APP_REFERENCE_CURRENCY,          Integer.valueOf(DEC_CURRENCY));
		map.put(EmvTags.TRANSACTION_REFERENCE_CURRENCY_CODE, Integer.valueOf(DEC_CURRENCY));
		map.put(EmvTags.TRANSACTION_CURRENCY_EXP,        Integer.valueOf(DEC_CURRENCY_EXPONENT));
		map.put(EmvTags.APP_CURRENCY_EXPONENT,           Integer.valueOf(DEC_CURRENCY_EXPONENT));
		map.put(EmvTags.APP_REFERENCE_CURRECY_EXPONENT,  Integer.valueOf(DEC_CURRENCY_EXPONENT));
		map.put(EmvTags.TRANSACTION_REFERENCE_CURRENCY_EXP, Integer.valueOf(DEC_CURRENCY_EXPONENT));
		map.put(EmvTags.ISSUER_COUNTRY_CODE,             Integer.valueOf(DEC_COUNTRY_NUMERIC));
		map.put(EmvTags.TERMINAL_COUNTRY_CODE,           Integer.valueOf(DEC_COUNTRY_NUMERIC));
		map.put(EmvTags.COUNTRY_CODE,                    Integer.valueOf(DEC_COUNTRY_NUMERIC));
		map.put(EmvTags.ISSUER_COUNTRY_CODE_ALPHA2,      Integer.valueOf(DEC_COUNTRY_ALPHA2));
		map.put(EmvTags.ISSUER_COUNTRY_CODE_ALPHA3,      Integer.valueOf(DEC_COUNTRY_ALPHA3));
		map.put(EmvTags.SERVICE_CODE,                    Integer.valueOf(DEC_SERVICE_CODE));
		map.put(EmvTags.TRACK1_DATA,                     Integer.valueOf(DEC_TRACK1));
		map.put(EmvTags.TRACK_2_EQV_DATA,                Integer.valueOf(DEC_TRACK2));
		map.put(EmvTags.TRACK2_DATA,                     Integer.valueOf(DEC_TRACK2));
		map.put(EmvTags.LANGUAGE_PREFERENCE,             Integer.valueOf(DEC_LANGUAGE));
		map.put(EmvTags.ISSUER_CODE_TABLE_INDEX,         Integer.valueOf(DEC_CODE_TABLE));
		map.put(EmvTags.APP_TRANSACTION_COUNTER,         Integer.valueOf(DEC_COUNTER));
		map.put(EmvTags.LAST_ONLINE_ATC_REGISTER,        Integer.valueOf(DEC_COUNTER));
		map.put(EmvTags.PIN_TRY_COUNTER,                 Integer.valueOf(DEC_COUNTER));
		map.put(EmvTags.TRANSACTION_SEQUENCE_COUNTER,    Integer.valueOf(DEC_COUNTER));
		map.put(EmvTags.APPLICATION_FILE_LOCATOR,        Integer.valueOf(DEC_AFL));
		map.put(EmvTags.SFI,                             Integer.valueOf(DEC_SFI));
		map.put(EmvTags.APPLICATION_PRIORITY_INDICATOR,  Integer.valueOf(DEC_PRIORITY));
		map.put(EmvTags.CVM_LIST,                        Integer.valueOf(DEC_CVM_LIST));
		map.put(EmvTags.LOG_ENTRY,                       Integer.valueOf(DEC_LOG_ENTRY));
		map.put(EmvTags.APP_VERSION_NUMBER_CARD,         Integer.valueOf(DEC_VERSION_NUMBER));
		map.put(EmvTags.APP_VERSION_NUMBER_TERMINAL,     Integer.valueOf(DEC_VERSION_NUMBER));
		map.put(EmvTags.DS_UNPREDICTABLE_NUMBER,         Integer.valueOf(DEC_CPLC));
		map.put(EmvTags.ISSUER_IDENTIFICATION_NUMBER,    Integer.valueOf(DEC_IIN));
		map.put(EmvTags.MERCHANT_CATEGORY_CODE,          Integer.valueOf(DEC_MERCHANT_CATEGORY));
		map.put(EmvTags.TERMINAL_FLOOR_LIMIT,            Integer.valueOf(DEC_AMOUNT_BINARY));
		map.put(EmvTags.LOWER_CONSEC_OFFLINE_LIMIT,      Integer.valueOf(DEC_COUNTER));
		map.put(EmvTags.UPPER_CONSEC_OFFLINE_LIMIT,      Integer.valueOf(DEC_COUNTER));
		map.put(EmvTags.APPLICATION_INTERCHANGE_PROFILE, Integer.valueOf(DEC_AIP));
		map.put(EmvTags.APP_USAGE_CONTROL,               Integer.valueOf(DEC_AUC));
		map.put(EmvTags.TRANSACTION_STATUS_INFORMATION,  Integer.valueOf(DEC_TSI));
		map.put(EmvTags.TERMINAL_VERIFICATION_RESULTS,   Integer.valueOf(DEC_TVR));
		map.put(EmvTags.ISSUER_ACTION_CODE_DEFAULT,      Integer.valueOf(DEC_TVR));
		map.put(EmvTags.ISSUER_ACTION_CODE_DENIAL,       Integer.valueOf(DEC_TVR));
		map.put(EmvTags.ISSUER_ACTION_CODE_ONLINE,       Integer.valueOf(DEC_TVR));
		map.put(EmvTags.TERMINAL_CAPABILITIES,           Integer.valueOf(DEC_TERMINAL_CAPABILITIES));
		map.put(EmvTags.ADDITIONAL_TERMINAL_CAPABILITIES, Integer.valueOf(DEC_ADDITIONAL_TERMINAL_CAPABILITIES));
		map.put(EmvTags.TERMINAL_TRANSACTION_QUALIFIERS, Integer.valueOf(DEC_TTQ));
		map.put(EmvTags.MAG_STRIPE_APP_VERSION_NUMBER_CARD, Integer.valueOf(DEC_CTQ));
		map.put(EmvTags.CRYPTOGRAM_INFORMATION_DATA,     Integer.valueOf(DEC_CID));
		map.put(EmvTags.CVM_RESULTS,                     Integer.valueOf(DEC_CVM_RESULTS));
		map.put(EmvTags.TERMINAL_TYPE,                   Integer.valueOf(DEC_TERMINAL_TYPE));
		map.put(EmvTags.POINT_OF_SERVICE_ENTRY_MODE,     Integer.valueOf(DEC_POS_ENTRY_MODE));
		map.put(EmvTags.TRANSACTION_TYPE,                Integer.valueOf(DEC_TRANSACTION_TYPE));
		map.put(EmvTags.AUTHORISATION_RESPONSE_CODE,     Integer.valueOf(DEC_AUTHORISATION_RESPONSE));
		map.put(EmvTags.KERNEL_IDENTIFIER,               Integer.valueOf(DEC_KERNEL_IDENTIFIER));
		map.put(EmvTags.APPLICATION_CAPABILITIES_INFORMATION, Integer.valueOf(DEC_APPLICATION_CAPABILITIES));
		map.put(EmvTags.VLP_ISSUER_AUTHORISATION_CODE,   Integer.valueOf(DEC_FORM_FACTOR));
		map.put(EmvTags.RESPONSE_MESSAGE_TEMPLATE_1,     Integer.valueOf(DEC_RESPONSE_TEMPLATE_1));
		map.put(EmvTags.VISA_LOG_ENTRY,                  Integer.valueOf(DEC_LOG_ENTRY));
		// @formatter:on
		return Collections.unmodifiableMap(map);
	}

	/**
	 * Method used to build the terminal type table
	 *
	 * @return the terminal types, by their two decimal digits
	 */
	private static Map<Integer, String> buildTerminalTypes() {
		Map<Integer, String> map = new HashMap<Integer, String>();
		// @formatter:off
		map.put(Integer.valueOf(11), "attended, financial institution controlled, online only");
		map.put(Integer.valueOf(12), "attended, financial institution controlled, offline with online capability");
		map.put(Integer.valueOf(13), "attended, financial institution controlled, offline only");
		map.put(Integer.valueOf(14), "unattended, financial institution controlled, online only");
		map.put(Integer.valueOf(15), "unattended, financial institution controlled, offline with online capability");
		map.put(Integer.valueOf(16), "unattended, financial institution controlled, offline only");
		map.put(Integer.valueOf(21), "attended, merchant controlled, online only");
		map.put(Integer.valueOf(22), "attended, merchant controlled, offline with online capability");
		map.put(Integer.valueOf(23), "attended, merchant controlled, offline only");
		map.put(Integer.valueOf(24), "unattended, merchant controlled, online only");
		map.put(Integer.valueOf(25), "unattended, merchant controlled, offline with online capability");
		map.put(Integer.valueOf(26), "unattended, merchant controlled, offline only");
		map.put(Integer.valueOf(34), "unattended, cardholder controlled, online only");
		map.put(Integer.valueOf(35), "unattended, cardholder controlled, offline with online capability");
		map.put(Integer.valueOf(36), "unattended, cardholder controlled, offline only");
		// @formatter:on
		return Collections.unmodifiableMap(map);
	}

	/**
	 * Method used to build the point of service entry mode table
	 *
	 * @return the entry modes, by their two decimal digits
	 */
	private static Map<Integer, String> buildPosEntryModes() {
		Map<Integer, String> map = new HashMap<Integer, String>();
		// @formatter:off
		map.put(Integer.valueOf(0),  "unknown or unspecified");
		map.put(Integer.valueOf(1),  "manual key entry");
		map.put(Integer.valueOf(2),  "magnetic stripe read");
		map.put(Integer.valueOf(3),  "bar code read");
		map.put(Integer.valueOf(4),  "optical character read");
		map.put(Integer.valueOf(5),  "integrated circuit card read, contact chip");
		map.put(Integer.valueOf(7),  "contactless chip read, EMV mode");
		map.put(Integer.valueOf(80), "magnetic stripe fallback after an unsuccessful chip read");
		map.put(Integer.valueOf(90), "magnetic stripe read, unaltered track data");
		map.put(Integer.valueOf(91), "contactless read with the magnetic stripe data rules");
		map.put(Integer.valueOf(95), "integrated circuit card read, the card verification value may be unreliable");
		// @formatter:on
		return Collections.unmodifiableMap(map);
	}

	/**
	 * Method used to build the transaction type table
	 *
	 * @return the transaction types, by their two decimal digits
	 */
	private static Map<Integer, String> buildTransactionTypes() {
		Map<Integer, String> map = new HashMap<Integer, String>();
		// @formatter:off
		map.put(Integer.valueOf(0),  "purchase of goods or services");
		map.put(Integer.valueOf(1),  "cash withdrawal");
		map.put(Integer.valueOf(9),  "purchase with cashback");
		map.put(Integer.valueOf(17), "cash disbursement");
		map.put(Integer.valueOf(20), "refund");
		// @formatter:on
		return Collections.unmodifiableMap(map);
	}

	/**
	 * Method used to build the authorisation response code table
	 *
	 * @return the codes EMV itself defines
	 */
	private static Map<String, String> buildAuthorisationResponseCodes() {
		Map<String, String> map = new HashMap<String, String>();
		// @formatter:off
		map.put("Y1", "offline approved");
		map.put("Z1", "offline declined");
		map.put("Y3", "unable to go online, offline approved");
		map.put("Z3", "unable to go online, offline declined");
		// @formatter:on
		return Collections.unmodifiableMap(map);
	}

	/**
	 * Method used to decode the value of a data object.
	 *
	 * @param pTag
	 *            tag of the data object
	 * @param pValue
	 *            raw value of the data object
	 * @return one line per decoded fact, in card order, empty when the data
	 *         object is unknown or its value cannot be read (never null)
	 */
	public static List<String> decode(final ITag pTag, final byte[] pValue) {
		if (pTag == null || pValue == null || pValue.length == 0) {
			return Collections.emptyList();
		}
		Integer decoder = DECODERS.get(pTag);
		if (decoder == null) {
			return Collections.emptyList();
		}
		List<String> ret = new ArrayList<String>();
		try {
			dispatch(decoder.intValue(), pValue, ret);
		} catch (RuntimeException e) {
			// A pretty printer is not worth an exception: the raw value has
			// already been written by the caller, so dropping the decoded
			// lines is the whole cost of a data object this class reads wrong
			LOGGER.debug("Unable to decode the value of the tag {}", BytesUtils.bytesToStringNoSpace(pTag.getTagBytes()), e);
			return Collections.emptyList();
		}
		return ret;
	}

	/**
	 * Method used to call the decoder matching the identifier
	 *
	 * @param pDecoder
	 *            decoder identifier
	 * @param pValue
	 *            raw value of the data object
	 * @param pOut
	 *            decoded lines
	 */
	private static void dispatch(final int pDecoder, final byte[] pValue, final List<String> pOut) {
		switch (pDecoder) {
		case DEC_AID:
			decodeAid(pValue, pOut);
			break;
		case DEC_DF_NAME:
			decodeDedicatedFileName(pValue, pOut);
			break;
		case DEC_PAN:
			decodePan(pValue, pOut);
			break;
		case DEC_PAN_SEQUENCE:
			decodeNumeric(pValue, "PAN sequence number", pOut);
			break;
		case DEC_DATE:
			decodeDate(pValue, pOut);
			break;
		case DEC_TIME:
			decodeTime(pValue, pOut);
			break;
		case DEC_AMOUNT_NUMERIC:
			decodeNumericAmount(pValue, pOut);
			break;
		case DEC_AMOUNT_BINARY:
			decodeBinaryAmount(pValue, pOut);
			break;
		case DEC_CURRENCY:
			decodeCurrency(pValue, pOut);
			break;
		case DEC_CURRENCY_EXPONENT:
			decodeCurrencyExponent(pValue, pOut);
			break;
		case DEC_COUNTRY_NUMERIC:
			decodeCountryNumeric(pValue, pOut);
			break;
		case DEC_COUNTRY_ALPHA2:
			decodeCountry(CountryCodeEnum.getCountryByAlpha2(EmvDataUtils.decodeText(pValue, 0)), pOut);
			break;
		case DEC_COUNTRY_ALPHA3:
			decodeCountry(CountryCodeEnum.getCountry(EmvDataUtils.decodeText(pValue, 0)), pOut);
			break;
		case DEC_SERVICE_CODE:
			decodeServiceCode(EmvDataUtils.getNumericValue(pValue), pOut);
			break;
		case DEC_TRACK1:
			decodeTrack1(pValue, pOut);
			break;
		case DEC_TRACK2:
			decodeTrack2(pValue, pOut);
			break;
		case DEC_LANGUAGE:
			decodeLanguagePreference(pValue, pOut);
			break;
		case DEC_CODE_TABLE:
			decodeIssuerCodeTableIndex(pValue, pOut);
			break;
		case DEC_COUNTER:
			decodeCounter(pValue, pOut);
			break;
		case DEC_AFL:
			decodeApplicationFileLocator(pValue, pOut);
			break;
		case DEC_SFI:
			decodeShortFileIdentifier(pValue, pOut);
			break;
		case DEC_PRIORITY:
			decodeApplicationPriorityIndicator(pValue, pOut);
			break;
		case DEC_CVM_LIST:
			decodeCvmList(pValue, pOut);
			break;
		case DEC_LOG_ENTRY:
			decodeLogEntry(pValue, pOut);
			break;
		case DEC_VERSION_NUMBER:
			decodeVersionNumber(pValue, pOut);
			break;
		case DEC_CPLC:
			decodeCplc(pValue, pOut);
			break;
		case DEC_IIN:
			decodeNumeric(pValue, "issuer identification number", pOut);
			break;
		case DEC_MERCHANT_CATEGORY:
			decodeNumeric(pValue, "merchant category code", pOut);
			break;
		case DEC_AIP:
			appendBits(pOut, pValue, AIP_BITS);
			break;
		case DEC_AUC:
			appendBits(pOut, pValue, AUC_BITS);
			break;
		case DEC_TSI:
			appendBits(pOut, pValue, TSI_BITS);
			break;
		case DEC_TVR:
			decodeTerminalVerificationResults(pValue, pOut);
			break;
		case DEC_TERMINAL_CAPABILITIES:
			appendBits(pOut, pValue, TERMINAL_CAPABILITIES_BITS);
			break;
		case DEC_ADDITIONAL_TERMINAL_CAPABILITIES:
			appendBits(pOut, pValue, ADDITIONAL_TERMINAL_CAPABILITIES_BITS);
			break;
		case DEC_TTQ:
			// The Kernel 2 reads the same tag as the PUNATC(Track2), a mask of
			// digit positions the book that could be read does not describe
			// precisely enough to decode, so a reserved bit here is as likely
			// to be a Kernel 2 value as an anomaly
			appendBits(pOut, pValue, TTQ_BITS, 1, false);
			break;
		case DEC_CTQ:
			decodeCardTransactionQualifiers(pValue, pOut);
			break;
		case DEC_CID:
			decodeCryptogramInformationData(pValue, pOut);
			break;
		case DEC_CVM_RESULTS:
			decodeCvmResults(pValue, pOut);
			break;
		case DEC_TERMINAL_TYPE:
			decodeTerminalType(pValue, pOut);
			break;
		case DEC_POS_ENTRY_MODE:
			decodePosEntryMode(pValue, pOut);
			break;
		case DEC_TRANSACTION_TYPE:
			decodeTransactionType(pValue, pOut);
			break;
		case DEC_AUTHORISATION_RESPONSE:
			decodeAuthorisationResponseCode(pValue, pOut);
			break;
		case DEC_KERNEL_IDENTIFIER:
			decodeKernelIdentifier(pValue, pOut);
			break;
		case DEC_APPLICATION_CAPABILITIES:
			decodeApplicationCapabilities(pValue, pOut);
			break;
		case DEC_FORM_FACTOR:
			decodeFormFactorIndicator(pValue, pOut);
			break;
		case DEC_RESPONSE_TEMPLATE_1:
			decodeResponseTemplate1(pValue, pOut);
			break;
		default:
			break;
		}
	}

	/**
	 * Method used to decode the part of a status word that
	 * {@link com.github.devnied.emvnfccard.enums.SwEnum} can only describe with
	 * a placeholder: the families '61XX', '6CXX' and '63CX' carry a number in
	 * SW2 that the enum, which is a table of constants, writes as "XX".
	 *
	 * @param pStatusWord
	 *            the two bytes of the status word
	 * @return the decoded lines, empty when SW2 carries no number (never null)
	 */
	public static List<String> decodeStatusWord(final byte[] pStatusWord) {
		if (pStatusWord == null || pStatusWord.length != 2) {
			return Collections.emptyList();
		}
		int sw1 = pStatusWord[0] & 0xFF;
		int sw2 = pStatusWord[1] & 0xFF;
		List<String> ret = new ArrayList<String>(1);
		if (sw1 == 0x61) {
			// A card answering '6100' has 256 bytes left, the length of a short
			// APDU is written on one byte and 0 stands for its maximum
			ret.add((sw2 == 0 ? 256 : sw2) + " bytes are still available");
		} else if (sw1 == 0x6C) {
			ret.add("the command has to be sent again with Le = " + (sw2 == 0 ? 256 : sw2));
		} else if (sw1 == 0x63 && (sw2 & 0xF0) == 0xC0) {
			int tries = sw2 & 0x0F;
			ret.add(tries + (tries == 1 ? " try" : " tries") + " left");
		}
		return ret;
	}

	/**
	 * Method used to parse every entry of an Application File Locator, the
	 * invalid ones included.
	 * <p>
	 * EMV 4.3 Book 3 section 10.2 codes each entry on 4 bytes: the SFI in the
	 * five high order bits of the first byte, the first and last record
	 * numbers, then the number of records taking part in the offline data
	 * authentication. The locator is reported as the card sent it:
	 * {@link Afl#isValid()} tells the entries apart. A trailing incomplete
	 * entry is ignored.
	 * </p>
	 *
	 * @param pAfl
	 *            AFL data (value of the tag '94', or bytes 3 and following of
	 *            the Response Message Template Format 1), may be null
	 * @return every entry in card order, never null
	 */
	public static List<Afl> parseAflEntries(final byte[] pAfl) {
		List<Afl> list = new ArrayList<Afl>();
		if (pAfl == null) {
			return list;
		}
		for (int i = 0; i + AFL_ENTRY_SIZE <= pAfl.length; i += AFL_ENTRY_SIZE) {
			Afl afl = new Afl();
			afl.setSfi((pAfl[i] & 0xFF) >> 3);
			afl.setFirstRecord(pAfl[i + 1] & 0xFF);
			afl.setLastRecord(pAfl[i + 2] & 0xFF);
			// The 4th byte is the number of records taking part in the offline
			// data authentication, not a flag (EMV 4.3 Book 3 section 10.2)
			afl.setOfflineAuthenticationRecords(pAfl[i + 3] & 0xFF);
			list.add(afl);
		}
		return list;
	}

	/**
	 * Method used to decode an Application Identifier: the RID identifies the
	 * application provider, the PIX the application it defines (ISO/IEC
	 * 7816-5).
	 *
	 * @param pValue
	 *            raw value of the data object
	 * @param pOut
	 *            decoded lines
	 */
	private static void decodeAid(final byte[] pValue, final List<String> pOut) {
		if (pValue.length < RID_LENGTH) {
			return;
		}
		StringBuilder buf = new StringBuilder();
		buf.append("RID ").append(BytesUtils.bytesToString(subarray(pValue, 0, RID_LENGTH)));
		if (pValue.length > RID_LENGTH) {
			buf.append(", PIX ").append(BytesUtils.bytesToString(subarray(pValue, RID_LENGTH, pValue.length)));
		}
		EmvCardScheme scheme = EmvCardScheme.getCardTypeByAid(BytesUtils.bytesToStringNoSpace(pValue));
		if (scheme != null) {
			buf.append(", ").append(scheme.getName());
		}
		add(pOut, buf.toString());
	}

	/**
	 * Method used to decode a Dedicated File name, which holds either an
	 * application identifier or the name of a payment system directory
	 * ("1PAY.SYS.DDF01", "2PAY.SYS.DDF01").
	 *
	 * @param pValue
	 *            raw value of the data object
	 * @param pOut
	 *            decoded lines
	 */
	private static void decodeDedicatedFileName(final byte[] pValue, final List<String> pOut) {
		String name = isPrintable(pValue) ? EmvDataUtils.decodeText(pValue, 0) : null;
		if (name != null) {
			add(pOut, "name " + name);
		} else {
			decodeAid(pValue, pOut);
		}
	}

	/**
	 * Method used to decode a Primary Account Number. The value is compressed
	 * numeric: the digits are left justified and padded with trailing 'F'
	 * nibbles, which {@link EmvDataUtils#getNumericValue(byte[])} removes.
	 *
	 * @param pValue
	 *            raw value of the data object
	 * @param pOut
	 *            decoded lines
	 */
	private static void decodePan(final byte[] pValue, final List<String> pOut) {
		String pan = EmvDataUtils.getNumericValue(pValue);
		if (pan == null) {
			return;
		}
		StringBuilder buf = new StringBuilder();
		buf.append("PAN ").append(pan).append(", ").append(pan.length()).append(" digits");
		EmvCardScheme scheme = EmvCardScheme.getCardTypeByCardNumber(pan);
		if (scheme != null) {
			buf.append(", ").append(scheme.getName());
		}
		add(pOut, buf.toString());
	}

	/**
	 * Method used to decode a packed BCD data object as a plain number
	 *
	 * @param pValue
	 *            raw value of the data object
	 * @param pLabel
	 *            name of the decoded value
	 * @param pOut
	 *            decoded lines
	 */
	private static void decodeNumeric(final byte[] pValue, final String pLabel, final List<String> pOut) {
		String value = EmvDataUtils.getNumericValue(pValue);
		if (value != null) {
			add(pOut, pLabel + " " + value);
		}
	}

	/**
	 * Method used to decode an EMV date (YYMMDD, packed BCD)
	 *
	 * @param pValue
	 *            raw value of the data object
	 * @param pOut
	 *            decoded lines
	 */
	private static void decodeDate(final byte[] pValue, final List<String> pOut) {
		Date date = EmvDataUtils.parseDate(pValue);
		if (date != null) {
			add(pOut, formatDate(date));
		}
	}

	/**
	 * Method used to decode an EMV time (HHMMSS, packed BCD). The value is
	 * formatted by hand rather than with a {@code SimpleDateFormat}, which is
	 * neither thread safe nor locale neutral.
	 *
	 * @param pValue
	 *            raw value of the data object
	 * @param pOut
	 *            decoded lines
	 */
	private static void decodeTime(final byte[] pValue, final List<String> pOut) {
		if (pValue.length < 3) {
			return;
		}
		int hour = bcd(pValue[0]);
		int minute = bcd(pValue[1]);
		int second = bcd(pValue[2]);
		if (hour < 0 || hour > 23 || minute < 0 || minute > 59 || second < 0 || second > 59) {
			return;
		}
		add(pOut, pad2(hour) + ":" + pad2(minute) + ":" + pad2(second));
	}

	/**
	 * Method used to decode a numeric amount (n 12), expressed in the minor
	 * unit of the currency of the transaction
	 *
	 * @param pValue
	 *            raw value of the data object
	 * @param pOut
	 *            decoded lines
	 */
	private static void decodeNumericAmount(final byte[] pValue, final List<String> pOut) {
		String digits = EmvDataUtils.getNumericValue(pValue);
		if (digits == null || !StringUtils.isNumeric(digits)) {
			return;
		}
		add(pOut, "amount " + stripLeadingZeroes(digits) + " in the minor unit of the currency");
	}

	/**
	 * Method used to decode a binary amount (b, 4 bytes)
	 *
	 * @param pValue
	 *            raw value of the data object
	 * @param pOut
	 *            decoded lines
	 */
	private static void decodeBinaryAmount(final byte[] pValue, final List<String> pOut) {
		long amount = EmvDataUtils.getBinaryValue(pValue, -1);
		if (amount >= 0) {
			add(pOut, "amount " + amount + " in the minor unit of the currency");
		}
	}

	/**
	 * Method used to decode an ISO 4217 currency code (n 3)
	 *
	 * @param pValue
	 *            raw value of the data object
	 * @param pOut
	 *            decoded lines
	 */
	private static void decodeCurrency(final byte[] pValue, final List<String> pOut) {
		int code = EmvDataUtils.getNumericIntValue(pValue, -1);
		if (code < 0) {
			return;
		}
		CurrencyEnum currency = EnumUtils.find(code, CurrencyEnum.class);
		if (currency != null) {
			add(pOut, code + " " + currency.getISOCodeAlpha() + " (" + currency.getName() + ")");
		} else {
			add(pOut, code + ", no ISO 4217 currency with this code");
		}
	}

	/**
	 * Method used to decode a currency exponent, the number of digits of the
	 * amounts that come after the decimal point
	 *
	 * @param pValue
	 *            raw value of the data object
	 * @param pOut
	 *            decoded lines
	 */
	private static void decodeCurrencyExponent(final byte[] pValue, final List<String> pOut) {
		int exponent = EmvDataUtils.getNumericIntValue(pValue, -1);
		if (exponent >= 0) {
			add(pOut, "amounts have " + exponent + " decimal digit" + (exponent == 1 ? "" : "s"));
		}
	}

	/**
	 * Method used to decode an ISO 3166-1 numeric country code (n 3)
	 *
	 * @param pValue
	 *            raw value of the data object
	 * @param pOut
	 *            decoded lines
	 */
	private static void decodeCountryNumeric(final byte[] pValue, final List<String> pOut) {
		int code = EmvDataUtils.getNumericIntValue(pValue, -1);
		if (code < 0) {
			return;
		}
		CountryCodeEnum country = EnumUtils.find(code, CountryCodeEnum.class);
		if (country != null) {
			add(pOut, code + " " + country.getAlpha2() + " (" + country.getName() + ")");
		} else {
			add(pOut, code + ", no ISO 3166-1 country with this code");
		}
	}

	/**
	 * Method used to render a country found by its alphabetic code
	 *
	 * @param pCountry
	 *            country, may be null
	 * @param pOut
	 *            decoded lines
	 */
	private static void decodeCountry(final CountryCodeEnum pCountry, final List<String> pOut) {
		if (pCountry != null) {
			add(pOut, pCountry.getAlpha2() + " (" + pCountry.getName() + ")");
		}
	}

	/**
	 * Method used to decode a service code (ISO/IEC 7813). The three digits
	 * give the interchange rules, the authorisation processing and the services
	 * the card is allowed.
	 *
	 * @param pDigits
	 *            the digits of the service code, may be null
	 * @param pOut
	 *            decoded lines
	 */
	private static void decodeServiceCode(final String pDigits, final List<String> pOut) {
		if (pDigits == null || pDigits.length() < 3 || !StringUtils.isNumeric(pDigits)) {
			return;
		}
		// A service code read from the tag '5F30' is the three last digits, the
		// card left pads the data object with a zero
		String digits = pDigits.substring(pDigits.length() - 3);
		ServiceCode1Enum first = EnumUtils.find(digits.charAt(0) - '0', ServiceCode1Enum.class);
		ServiceCode2Enum second = EnumUtils.find(digits.charAt(1) - '0', ServiceCode2Enum.class);
		ServiceCode3Enum third = EnumUtils.find(digits.charAt(2) - '0', ServiceCode3Enum.class);
		add(pOut, "service code " + digits);
		if (first != null) {
			// The enums write "None" where the digit carries no qualifier,
			// which reads as a failure of the printer rather than as a value
			String technology = first.getTechnology();
			add(pOut, "digit 1: " + first.getInterchange()
					+ (NO_QUALIFIER.equals(technology) ? "" : ", " + technology));
		}
		if (second != null) {
			add(pOut, "digit 2: authorisation " + second.getAuthorizationProcessing().toLowerCase(Locale.ENGLISH));
		}
		if (third != null) {
			String pin = third.getPinRequirements();
			add(pOut, "digit 3: " + third.getAllowedServices() + ", "
					+ (NO_QUALIFIER.equals(pin) ? "no PIN requirement stated" : pin));
		}
	}

	/**
	 * Method used to decode track 1 data (ISO/IEC 7813)
	 *
	 * @param pValue
	 *            raw value of the data object
	 * @param pOut
	 *            decoded lines
	 */
	private static void decodeTrack1(final byte[] pValue, final List<String> pOut) {
		EmvTrack1 track = TrackUtils.extractTrack1Data(pValue);
		if (track == null) {
			return;
		}
		if (StringUtils.isNotBlank(track.getCardNumber())) {
			add(pOut, "PAN " + track.getCardNumber());
		}
		String name = StringUtils.trimToNull(StringUtils.defaultString(track.getHolderLastname()) + " "
				+ StringUtils.defaultString(track.getHolderFirstname()));
		if (name != null) {
			add(pOut, "cardholder " + name);
		}
		if (track.getExpireDate() != null) {
			add(pOut, "expires " + formatYearMonth(track.getExpireDate()));
		}
		decodeServiceCode(track.getServiceCode(), pOut);
	}

	/**
	 * Method used to decode track 2 equivalent data (ISO/IEC 7813 without the
	 * sentinels and the redundancy check)
	 *
	 * @param pValue
	 *            raw value of the data object
	 * @param pOut
	 *            decoded lines
	 */
	private static void decodeTrack2(final byte[] pValue, final List<String> pOut) {
		EmvTrack2 track = TrackUtils.extractTrack2EquivalentData(pValue);
		if (track == null) {
			return;
		}
		if (StringUtils.isNotBlank(track.getCardNumber())) {
			add(pOut, "PAN " + track.getCardNumber());
		}
		if (track.getExpireDate() != null) {
			add(pOut, "expires " + formatYearMonth(track.getExpireDate()));
		}
		decodeServiceCode(track.getServiceCode(), pOut);
	}

	/**
	 * Method used to decode the language preference (tag '5F2D')
	 *
	 * @param pValue
	 *            raw value of the data object
	 * @param pOut
	 *            decoded lines
	 */
	private static void decodeLanguagePreference(final byte[] pValue, final List<String> pOut) {
		List<String> languages = EmvDataUtils.parseLanguagePreference(pValue);
		if (!languages.isEmpty()) {
			add(pOut, "languages " + StringUtils.join(languages, ", ") + " (ISO 639, most preferred first)");
		}
	}

	/**
	 * Method used to decode the issuer code table index (tag '9F11'), the part
	 * number of the ISO/IEC 8859 standard the issuer texts are encoded with
	 *
	 * @param pValue
	 *            raw value of the data object
	 * @param pOut
	 *            decoded lines
	 */
	private static void decodeIssuerCodeTableIndex(final byte[] pValue, final List<String> pOut) {
		int index = bcd(pValue[0]);
		if (index < 0) {
			return;
		}
		add(pOut, "part " + index + " of ISO/IEC 8859, read as " + EmvDataUtils.getCharsetName(pValue));
	}

	/**
	 * Method used to decode a binary counter
	 *
	 * @param pValue
	 *            raw value of the data object
	 * @param pOut
	 *            decoded lines
	 */
	private static void decodeCounter(final byte[] pValue, final List<String> pOut) {
		long value = EmvDataUtils.getBinaryValue(pValue, -1);
		// A single digit reads the same in the hexadecimal dump right above, so
		// repeating it in decimal only adds a line
		if (value >= 10 || value >= 0 && pValue.length > 1) {
			add(pOut, "= " + value + " in decimal");
		}
	}

	/**
	 * Method used to decode an Application File Locator
	 *
	 * @param pValue
	 *            raw value of the data object
	 * @param pOut
	 *            decoded lines
	 */
	private static void decodeApplicationFileLocator(final byte[] pValue, final List<String> pOut) {
		for (Afl afl : parseAflEntries(pValue)) {
			StringBuilder buf = new StringBuilder();
			buf.append("SFI ").append(afl.getSfi()).append(", record");
			if (afl.getFirstRecord() == afl.getLastRecord()) {
				buf.append(' ').append(afl.getFirstRecord());
			} else {
				buf.append("s ").append(afl.getFirstRecord()).append(" to ").append(afl.getLastRecord());
			}
			if (afl.getOfflineAuthenticationRecords() > 0) {
				buf.append(", ").append(afl.getOfflineAuthenticationRecords())
						.append(" record(s) used for offline data authentication");
			}
			if (!afl.isValid()) {
				buf.append(" (invalid entry)");
			}
			add(pOut, buf.toString());
		}
		if (pValue.length % AFL_ENTRY_SIZE != 0) {
			add(pOut, "the locator ends with " + pValue.length % AFL_ENTRY_SIZE + " byte(s) that are not an entry");
		}
	}

	/**
	 * Method used to decode a short file identifier (tag '88'), which
	 * identifies the elementary file holding the directory of a payment system
	 * environment
	 *
	 * @param pValue
	 *            raw value of the data object
	 * @param pOut
	 *            decoded lines
	 */
	private static void decodeShortFileIdentifier(final byte[] pValue, final List<String> pOut) {
		int sfi = pValue[0] & 0xFF;
		if ((sfi & 0xE0) != 0) {
			add(pOut, "the three high order bits of a short file identifier must be zero");
			return;
		}
		String range = "";
		if (sfi >= 1 && sfi <= 10) {
			range = ", governed by the EMV specification";
		} else if (sfi >= 11 && sfi <= 20) {
			range = ", payment system specific";
		} else if (sfi >= 21 && sfi <= 30) {
			range = ", issuer specific";
		} else {
			range = ", outside the range 1 to 30";
		}
		add(pOut, "SFI " + sfi + range);
	}

	/**
	 * Method used to decode the application priority indicator (tag '87')
	 *
	 * @param pValue
	 *            raw value of the data object
	 * @param pOut
	 *            decoded lines
	 */
	private static void decodeApplicationPriorityIndicator(final byte[] pValue, final List<String> pOut) {
		if ((pValue[0] & 0x80) != 0) {
			add(pOut, "b8 -- the application cannot be selected without the confirmation of the cardholder");
		}
		if ((pValue[0] & 0x70) != 0) {
			add(pOut, "b7-b5 -- " + RFU_SET);
		}
		int priority = pValue[0] & 0x0F;
		add(pOut, priority == 0 ? "no priority assigned" : "priority " + priority + " of 15, 1 being the highest");
	}

	/**
	 * Method used to decode a Cardholder Verification Method list (tag '8E')
	 *
	 * @param pValue
	 *            raw value of the data object
	 * @param pOut
	 *            decoded lines
	 */
	private static void decodeCvmList(final byte[] pValue, final List<String> pOut) {
		if (pValue.length <= CVM_LIST_HEADER_SIZE) {
			return;
		}
		long amountX = CvmUtils.getAmountX(pValue);
		long amountY = CvmUtils.getAmountY(pValue);
		if (amountX >= 0 && amountY >= 0) {
			add(pOut, "amount X " + amountX + ", amount Y " + amountY);
		}
		int index = 1;
		for (CVM cvm : CvmUtils.parseCvmList(pValue)) {
			StringBuilder buf = new StringBuilder();
			buf.append("rule ").append(index++).append(": ");
			// The labels come from the tables of this class rather than from
			// CvmMethodEnum and CvmConditionEnum, which are the typed reading
			// of a rule: the enums stop at the codes EMV 4.3 defined and leave
			// everything else null, where a printer has to name the biometric
			// methods of EMV 4.4, the codes reserved for the payment systems
			// and for the issuer, and the reserved condition ranges
			buf.append(cvmRuleMethodLabel(cvm.getRawMethod()));
			buf.append(", ");
			buf.append(cvmConditionLabel(cvm.getRawCondition()));
			if (cvm.isApplyNextIfUnsuccessful()) {
				buf.append(", apply the next rule if this one is unsuccessful");
			} else {
				buf.append(", fail the cardholder verification if this one is unsuccessful");
			}
			add(pOut, buf.toString());
		}
	}

	/**
	 * Method used to decode the log entry (tag '9F4D'), which tells where the
	 * transaction log of the application is
	 *
	 * @param pValue
	 *            raw value of the data object
	 * @param pOut
	 *            decoded lines
	 */
	private static void decodeLogEntry(final byte[] pValue, final List<String> pOut) {
		if (pValue.length < 2) {
			return;
		}
		add(pOut, "SFI " + (pValue[0] & 0xFF) + ", " + (pValue[1] & 0xFF) + " records at most");
	}

	/**
	 * Method used to decode an application version number, which the payment
	 * system defines and which is read as two binary coded digits per byte
	 *
	 * @param pValue
	 *            raw value of the data object
	 * @param pOut
	 *            decoded lines
	 */
	private static void decodeVersionNumber(final byte[] pValue, final List<String> pOut) {
		add(pOut, "version " + BytesUtils.bytesToStringNoSpace(pValue));
	}

	/**
	 * Method used to decode the Card Production Life Cycle data of the Global
	 * Platform Card Specification. The tag '9F7F' is shared with the DS
	 * Unpredictable Number of the Kernel 2, which is 4 bytes long, so only a
	 * value of exactly {@link CPLC#SIZE} bytes is read as a life cycle.
	 *
	 * @param pValue
	 *            raw value of the data object
	 * @param pOut
	 *            decoded lines
	 */
	private static void decodeCplc(final byte[] pValue, final List<String> pOut) {
		if (pValue.length != CPLC.SIZE) {
			return;
		}
		CPLC cplc = CPLCUtils.parse(pValue);
		if (cplc == null) {
			return;
		}
		addIfNotNull(pOut, "IC fabricator ", cplc.getIcFabricator(), 2);
		addIfNotNull(pOut, "IC type ", cplc.getIcType(), 2);
		addIfNotNull(pOut, "operating system ", cplc.getOs(), 2);
		addDate(pOut, "operating system released on ", cplc.getOsReleaseDate());
		addIfNotNull(pOut, "operating system release level ", cplc.getOsReleaseLevel(), 2);
		addDate(pOut, "IC fabricated on ", cplc.getIcFabricDate());
		addIfNotNull(pOut, "IC serial number ", cplc.getIcSerialNumber(), 4);
		addIfNotNull(pOut, "IC batch ", cplc.getIcBatchId(), 2);
		addIfNotNull(pOut, "IC module fabricator ", cplc.getIcModuleFabricator(), 2);
		addDate(pOut, "IC packaged on ", cplc.getIcPackagingDate());
		addIfNotNull(pOut, "card manufacturer ", cplc.getIccManufacturer(), 2);
		addDate(pOut, "IC embedded on ", cplc.getIcEmbeddingDate());
		addIfNotNull(pOut, "pre-personalisation by ", cplc.getPrepersoId(), 2);
		addDate(pOut, "pre-personalised on ", cplc.getPrepersoDate());
		addIfNotNull(pOut, "pre-personalisation equipment ", cplc.getPrepersoEquipment(), 4);
		addIfNotNull(pOut, "personalisation by ", cplc.getPersoId(), 2);
		addDate(pOut, "personalised on ", cplc.getPersoDate());
		addIfNotNull(pOut, "personalisation equipment ", cplc.getPersoEquipment(), 4);
		if (StringUtils.isNotBlank(cplc.getParseError())) {
			add(pOut, "the rest of the life cycle could not be read: " + cplc.getParseError());
		}
	}

	/**
	 * Method used to add a line holding a numeric field of the card production
	 * life cycle, which the Global Platform specification writes in hexadecimal
	 *
	 * @param pOut
	 *            decoded lines
	 * @param pLabel
	 *            name of the field
	 * @param pValue
	 *            value of the field, may be null
	 */
	private static void addIfNotNull(final List<String> pOut, final String pLabel, final Integer pValue,
			final int pBytes) {
		if (pValue != null) {
			// The specification writes these fields at a fixed width and a
			// support engineer greps a card database with what is printed, so
			// the leading zeroes of '0047' are part of the value
			String hex = Integer.toHexString(pValue.intValue()).toUpperCase(Locale.ENGLISH);
			StringBuilder buf = new StringBuilder(pLabel).append('\'');
			for (int i = hex.length(); i < pBytes * 2; i++) {
				buf.append('0');
			}
			add(pOut, buf.append(hex).append('\'').toString());
		}
	}

	/**
	 * Method used to add a line holding a date of the card production life
	 * cycle
	 *
	 * @param pOut
	 *            decoded lines
	 * @param pLabel
	 *            name of the field
	 * @param pDate
	 *            value of the field, may be null
	 */
	private static void addDate(final List<String> pOut, final String pLabel, final Date pDate) {
		if (pDate != null) {
			add(pOut, pLabel + formatDate(pDate));
		}
	}

	/**
	 * Method used to render the bits of a bitmap data object. Only the bits
	 * that are set are printed: a data object where every meaning is "not
	 * supported" is noise, and the raw value is right above anyway.
	 *
	 * @param pOut
	 *            decoded lines
	 * @param pValue
	 *            raw value of the data object
	 * @param pTable
	 *            meaning of each bit, most significant bit of the first byte
	 *            first, null where the specification defines none
	 */
	private static void appendBits(final List<String> pOut, final byte[] pValue, final String[] pTable) {
		appendBits(pOut, pValue, pTable, 1, true);
	}

	/**
	 * Method used to render the bits of a bitmap, or of a part of one.
	 *
	 * @param pOut
	 *            decoded lines
	 * @param pValue
	 *            raw value of the data object
	 * @param pTable
	 *            meaning of each bit
	 * @param pFirstByte
	 *            number of the byte of the value the table starts at, 1 for a
	 *            table covering the whole data object
	 * @param pReportReserved
	 *            true to report a reserved bit that is set as an anomaly, which
	 *            only makes sense where the reading of the data object is
	 *            certain
	 */
	private static void appendBits(final List<String> pOut, final byte[] pValue, final String[] pTable,
			final int pFirstByte, final boolean pReportReserved) {
		int expected = pTable.length / 8;
		// A partial bitmap is a guess: half of a Terminal Verification Result
		// says nothing about the bits the card did not send
		if (pValue.length < pFirstByte - 1 + expected) {
			return;
		}
		boolean anyBitSet = false;
		for (int i = 0; i < pTable.length; i++) {
			int bit = 8 - i % 8;
			int index = pFirstByte - 1 + i / 8;
			if ((pValue[index] & 1 << bit - 1) != 0) {
				anyBitSet = true;
				if (pTable[i] == null && !pReportReserved) {
					continue;
				}
				add(pOut, "byte " + (index + 1) + " b" + bit + " -- " + (pTable[i] != null ? pTable[i] : RFU_SET));
			}
		}
		// Said of the value and not of the lines: a bitmap whose only set bits
		// are ones this reading leaves out is not a bitmap where nothing is set
		if (!anyBitSet && pFirstByte == 1) {
			add(pOut, "all bits clear");
		}
		if (pFirstByte == 1 && pValue.length > expected) {
			add(pOut, "the value is " + pValue.length + " bytes, this data object is defined as " + expected);
		}
	}

	/**
	 * Method used to read a field spanning several bits of a byte
	 *
	 * @param pByte
	 *            byte holding the field
	 * @param pHighBit
	 *            most significant bit of the field, 8 being the most
	 *            significant bit of the byte
	 * @param pLowBit
	 *            least significant bit of the field
	 * @return the value of the field
	 */
	private static int field(final byte pByte, final int pHighBit, final int pLowBit) {
		return (pByte & 0xFF) >> pLowBit - 1 & (1 << pHighBit - pLowBit + 1) - 1;
	}

	/**
	 * Method used to name the content of a Response Message Template Format 1.
	 * <p>
	 * The template holds the answer of a command with neither tags nor lengths,
	 * so its layout is the one of the command that was sent (EMV 4.4 Book 3
	 * section 6.5.8.4 for the GET PROCESSING OPTIONS, section 6.5.5 for the
	 * GENERATE AC). A printer reading a bare response cannot know which command
	 * produced it, and the two layouts share plausible lengths: naming both is
	 * the most that can be said without inventing the answer.
	 * </p>
	 *
	 * @param pValue
	 *            raw value of the data object
	 * @param pOut
	 *            decoded lines
	 */
	private static void decodeResponseTemplate1(final byte[] pValue, final List<String> pOut) {
		add(pOut, "the value has neither tags nor lengths, its layout is the one of the command that was sent");
		if (pValue.length >= 2 && (pValue.length - 2) % AFL_ENTRY_SIZE == 0) {
			add(pOut, "a GET PROCESSING OPTIONS answers the application interchange profile then the file locator");
		}
		if (pValue.length >= 11) {
			add(pOut, "a GENERATE AC answers the cryptogram information data, the counter, the cryptogram "
					+ "and the issuer application data");
		}
	}

	/**
	 * Method used to decode the tag '9F6C', which the Kernel 3 reads as the
	 * Card Transaction Qualifiers and the Kernel 2 as the mag-stripe
	 * application version number of the card. Both readings are printed and
	 * neither reports a reserved bit: a bit that means nothing to one kernel is
	 * an ordinary value of the other, and accusing a sound card of breaking the
	 * specification is worse than saying less.
	 *
	 * @param pValue
	 *            raw value of the data object
	 * @param pOut
	 *            decoded lines
	 */
	private static void decodeCardTransactionQualifiers(final byte[] pValue, final List<String> pOut) {
		if (pValue.length != 2) {
			return;
		}
		add(pOut, "[K2] mag-stripe application version number " + BytesUtils.bytesToStringNoSpace(pValue));
		appendBits(pOut, pValue, CTQ_BITS, 1, false);
	}

	/**
	 * Method used to decode a Terminal Verification Result, or one of the
	 * Issuer Action Codes that share its layout. The two low order bits of the
	 * last byte are a field for the Kernel 2 and two reserved bits for the
	 * Book 3, so the field is named on top of the bits.
	 *
	 * @param pValue
	 *            raw value of the data object
	 * @param pOut
	 *            decoded lines
	 */
	private static void decodeTerminalVerificationResults(final byte[] pValue, final List<String> pOut) {
		appendBits(pOut, pValue, TVR_BITS);
		if (pValue.length >= 5) {
			int relayResistance = field(pValue[4], 2, 1);
			if (relayResistance != 0) {
				add(pOut, "[K2] byte 5 b2-b1 = " + relayResistance + " -- " + RELAY_RESISTANCE[relayResistance]);
			}
		}
	}

	/**
	 * Method used to decode the Cryptogram Information Data (tag '9F27')
	 *
	 * @param pValue
	 *            raw value of the data object
	 * @param pOut
	 *            decoded lines
	 */
	private static void decodeCryptogramInformationData(final byte[] pValue, final List<String> pOut) {
		int type = field(pValue[0], 8, 7);
		add(pOut, "b8-b7 -- " + CRYPTOGRAM_TYPES[type]);
		if ((pValue[0] & 0x10) != 0) {
			add(pOut, "b4 -- advice required");
		}
		int payment = field(pValue[0], 6, 5);
		if (payment != 0) {
			// The specification names the field but leaves the three non zero
			// values to the payment system, so there is nothing to translate
			add(pOut, "b6-b5 = " + payment + " -- payment system specific cryptogram");
		}
		add(pOut, "b3-b1 -- " + ADVICE_CODES[field(pValue[0], 3, 1)]);
	}

	/**
	 * Method used to decode the Cardholder Verification Method Results (tag
	 * '9F34'): the rule the terminal applied and how it ended.
	 *
	 * @param pValue
	 *            raw value of the data object
	 * @param pOut
	 *            decoded lines
	 */
	private static void decodeCvmResults(final byte[] pValue, final List<String> pOut) {
		if (pValue.length < 3) {
			return;
		}
		int method = pValue[0] & 0x3F;
		// The Kernel 2 reuses the code of the plaintext PIN for the
		// verification a consumer device performs on its own
		add(pOut, "method: "
				+ (method == 1 ? "[Book 3] plaintext PIN verified by the card, [K2] on device CVM performed"
						: cvmMethodLabel(method)));
		if ((pValue[0] & 0x40) != 0) {
			add(pOut, "the rule asked to apply the succeeding one if this method is unsuccessful");
		}
		if ((pValue[0] & 0x80) != 0) {
			add(pOut, "byte 1 b8 -- " + RFU_SET);
		}
		add(pOut, "condition: " + cvmConditionLabel(pValue[1] & 0xFF));
		add(pOut, "result: " + cvmResultLabel(pValue[2] & 0xFF));
	}

	/**
	 * Method used to name a CVM code, as coded in the six low order bits of the
	 * first byte of a Cardholder Verification Rule
	 *
	 * @param pCode
	 *            CVM code
	 * @return the name of the method
	 */
	private static String cvmRuleMethodLabel(final int pCode) {
		// EMV 4.4 Book 3 Annex C3 gives '3F' to "no CVM performed" in the CVM
		// Results and marks it "not available for use" in a rule, so a card
		// listing it is telling something a printer must not smooth over
		return pCode == 0x3F ? "not available for use as a rule, the card should not list it" : cvmMethodLabel(pCode);
	}

	/**
	 * Method used to name a CVM code, as coded in the six low order bits of the
	 * first byte of a Cardholder Verification Rule
	 *
	 * @param pCode
	 *            CVM code
	 * @return the name of the method
	 */
	private static String cvmMethodLabel(final int pCode) {
		if (pCode < CVM_METHODS.length) {
			return CVM_METHODS[pCode];
		}
		if (pCode <= 0x1D) {
			return "reserved for future use by the EMV specification";
		}
		if (pCode == 0x1E) {
			return "signature";
		}
		if (pCode == 0x1F) {
			return "no CVM required";
		}
		if (pCode <= 0x2F) {
			return "reserved for use by the individual payment systems";
		}
		if (pCode <= 0x3E) {
			return "reserved for use by the issuer";
		}
		return "no CVM performed";
	}

	/**
	 * Method used to name a CVM condition code
	 *
	 * @param pCode
	 *            condition code, the second byte of a Cardholder Verification
	 *            Rule
	 * @return the condition
	 */
	private static String cvmConditionLabel(final int pCode) {
		if (pCode < CVM_CONDITIONS.length) {
			return CVM_CONDITIONS[pCode];
		}
		return pCode <= 0x7F ? "RFU" : "reserved for use by the individual payment systems";
	}

	/**
	 * Method used to name the outcome of a cardholder verification
	 *
	 * @param pCode
	 *            third byte of the CVM Results
	 * @return the outcome
	 */
	private static String cvmResultLabel(final int pCode) {
		switch (pCode) {
		case 0:
			return "unknown";
		case 1:
			return "failed";
		case 2:
			return "successful";
		default:
			return "not defined by EMV (" + hexByte(pCode) + ")";
		}
	}

	/**
	 * Method used to decode the Application Capabilities Information (tag
	 * '9F5D', 3 bytes) or the Available Offline Spending Amount (6 bytes),
	 * which share the tag and are told apart by their length.
	 *
	 * @param pValue
	 *            raw value of the data object
	 * @param pOut
	 *            decoded lines
	 */
	private static void decodeApplicationCapabilities(final byte[] pValue, final List<String> pOut) {
		if (pValue.length == 6) {
			decodeNumericAmount(pValue, pOut);
			add(pOut, "read as the Available Offline Spending Amount, which is 6 bytes long");
			return;
		}
		if (pValue.length != 3) {
			return;
		}
		add(pOut, "[K2] application capabilities information version " + field(pValue[0], 8, 5));
		int storage = field(pValue[0], 4, 1);
		add(pOut, "[K2] " + (storage == 0 ? "data storage not supported" : "data storage version " + storage));
		appendBits(pOut, pValue, ACI_BYTE_2_BITS, 2, true);
		add(pOut, "[K2] secure data storage scheme indicator " + hexByte(pValue[2]));
	}

	/**
	 * Method used to decode a Kernel Identifier (tag '9F2A'). The first byte
	 * holds the kind of kernel and the short kernel identifier; a domestic
	 * kernel is named by the three bytes as a whole.
	 *
	 * @param pValue
	 *            raw value of the data object
	 * @param pOut
	 *            decoded lines
	 */
	private static void decodeKernelIdentifier(final byte[] pValue, final List<String> pOut) {
		int type = field(pValue[0], 8, 7);
		KernelTypeEnum kernelType = EnumUtils.find(type, KernelTypeEnum.class);
		add(pOut, "kernel type " + type + (kernelType != null ? " (" + kernelType.getName() + ")" : ""));
		int shortId = field(pValue[0], 6, 1);
		KernelEnum kernel = KernelEnum.find(shortId);
		if (shortId == 0) {
			add(pOut, "no kernel preference, the one of the application identifier is used");
		} else {
			add(pOut, "short kernel identifier " + shortId + (kernel != null ? " (" + kernel.getName() + ")" : ""));
		}
		if (type != 0 && pValue.length >= 3) {
			add(pOut, "extended kernel identifier " + BytesUtils.bytesToString(subarray(pValue, 1, 3)));
		}
	}

	/**
	 * Method used to decode the tag '9F6E', which three kernels use for three
	 * different data objects. The length tells the card ones apart; the reader
	 * one of the Kernel 4 shares its length with the Form Factor Indicator and
	 * is only named.
	 *
	 * @param pValue
	 *            raw value of the data object
	 * @param pOut
	 *            decoded lines
	 */
	private static void decodeFormFactorIndicator(final byte[] pValue, final List<String> pOut) {
		if (pValue.length == 4) {
			int technology = field(pValue[3], 4, 1);
			add(pOut, "[K3] form factor indicator, payment technology "
					+ (technology == 0 ? "proximity contactless interface (ISO/IEC 14443)" : hexByte(technology)));
			add(pOut, "[K4] the same 4 bytes read from a reader are its enhanced contactless capabilities");
		} else if (pValue.length >= 5) {
			// The country code is what tells a reader whether the Kernel 2
			// reading holds at all: a value ISO 3166-1 does not assign means
			// the card is answering something else under the same tag
			List<String> country = new ArrayList<String>();
			decodeCountryNumeric(subarray(pValue, 0, 2), country);
			add(pOut, "[K2] third party data, country " + (country.isEmpty() ? "not readable" : country.get(0)));
			boolean deviceType = (pValue[2] & 0x80) == 0;
			add(pOut, "[K2] unique identifier " + BytesUtils.bytesToString(subarray(pValue, 2, 4)));
			if (deviceType && pValue.length >= 6) {
				add(pOut, "[K2] device type " + getSafePrintable(subarray(pValue, 4, 6)));
			}
		}
	}

	/**
	 * Method used to decode a terminal type (tag '9F35')
	 *
	 * @param pValue
	 *            raw value of the data object
	 * @param pOut
	 *            decoded lines
	 */
	private static void decodeTerminalType(final byte[] pValue, final List<String> pOut) {
		int type = bcd(pValue[0]);
		if (type < 0) {
			return;
		}
		String label = TERMINAL_TYPES.get(Integer.valueOf(type));
		add(pOut, "terminal type " + type + (label != null ? ", " + label : ", not defined by EMV"));
		if (type >= 14 && type <= 16) {
			// EMV 4.4 Book 4: the terminal types '14', '15' and '16' with a
			// cash disbursement capability are ATMs, the others are not
			add(pOut, "an ATM when the terminal is capable of cash disbursement");
		}
	}

	/**
	 * Method used to decode a point of service entry mode (tag '9F39')
	 *
	 * @param pValue
	 *            raw value of the data object
	 * @param pOut
	 *            decoded lines
	 */
	private static void decodePosEntryMode(final byte[] pValue, final List<String> pOut) {
		int mode = bcd(pValue[0]);
		if (mode < 0) {
			return;
		}
		String label = POS_ENTRY_MODES.get(Integer.valueOf(mode));
		add(pOut, "entry mode " + pad2(mode) + (label != null ? ", " + label : ""));
		// EMV takes the two digits from the ISO 8583 data element 22 without
		// reproducing its table, so the meanings above are the ones of ISO 8583
		add(pOut, "the values are those of the ISO 8583 point of service entry mode, not of EMV");
	}

	/**
	 * Method used to decode a transaction type (tag '9C')
	 *
	 * @param pValue
	 *            raw value of the data object
	 * @param pOut
	 *            decoded lines
	 */
	private static void decodeTransactionType(final byte[] pValue, final List<String> pOut) {
		int type = bcd(pValue[0]);
		if (type < 0) {
			return;
		}
		String label = TRANSACTION_TYPES.get(Integer.valueOf(type));
		add(pOut, "transaction type " + pad2(type) + ", "
				+ (label != null ? label : "defined by the payment system, not by EMV"));
	}

	/**
	 * Method used to decode an authorisation response code (tag '8A'), two
	 * characters of which EMV defines the offline ones alone
	 *
	 * @param pValue
	 *            raw value of the data object
	 * @param pOut
	 *            decoded lines
	 */
	private static void decodeAuthorisationResponseCode(final byte[] pValue, final List<String> pOut) {
		if (pValue.length != 2) {
			return;
		}
		String code = getSafePrintable(pValue);
		String label = AUTHORISATION_RESPONSE_CODES.get(code);
		add(pOut, code + ", " + (label != null ? label : "response of the payment system, not defined by EMV"));
	}

	/**
	 * Method used to read a value as printable characters
	 *
	 * @param pValue
	 *            value to read
	 * @return the printable form of the value
	 */
	private static String getSafePrintable(final byte[] pValue) {
		return TlvUtil.getSafePrintChars(pValue);
	}

	/**
	 * Method used to add a line to the decoded output, keeping it within
	 * {@link #MAX_DECODED_LINES}
	 *
	 * @param pOut
	 *            decoded lines
	 * @param pLine
	 *            line to add
	 */
	private static void add(final List<String> pOut, final String pLine) {
		if (pOut.size() < MAX_DECODED_LINES) {
			pOut.add(pLine);
		} else if (pOut.size() == MAX_DECODED_LINES) {
			pOut.add(TRUNCATED);
		}
	}

	/**
	 * Method used to format a date, by hand rather than with a
	 * {@code SimpleDateFormat}, which is neither thread safe nor locale neutral
	 *
	 * @param pDate
	 *            date to format
	 * @return the date as yyyy-MM-dd
	 */
	private static String formatDate(final Date pDate) {
		Calendar calendar = new GregorianCalendar();
		calendar.setTime(pDate);
		return calendar.get(Calendar.YEAR) + "-" + pad2(calendar.get(Calendar.MONTH) + 1) + "-"
				+ pad2(calendar.get(Calendar.DAY_OF_MONTH));
	}

	/**
	 * Method used to format the year and the month of a date. A track expiry
	 * date has no day, {@link TrackUtils} sets it to the last day of the month.
	 *
	 * @param pDate
	 *            date to format
	 * @return the date as yyyy-MM
	 */
	private static String formatYearMonth(final Date pDate) {
		Calendar calendar = new GregorianCalendar();
		calendar.setTime(pDate);
		return calendar.get(Calendar.YEAR) + "-" + pad2(calendar.get(Calendar.MONTH) + 1);
	}

	/**
	 * Method used to left pad a number with a zero
	 *
	 * @param pValue
	 *            value to pad
	 * @return the padded value
	 */
	private static String pad2(final int pValue) {
		return pValue < 10 ? "0" + pValue : Integer.toString(pValue);
	}

	/**
	 * Method used to render a byte value the way a specification writes it
	 *
	 * @param pValue
	 *            value to render
	 * @return the value as two hexadecimal digits between quotes
	 */
	private static String hexByte(final int pValue) {
		String hex = Integer.toHexString(pValue & 0xFF).toUpperCase(Locale.ENGLISH);
		return "'" + (hex.length() < 2 ? "0" + hex : hex) + "'";
	}

	/**
	 * Method used to read a byte holding two binary coded digits
	 *
	 * @param pValue
	 *            byte to read
	 * @return the value of the two digits, or -1 when one of the nibbles is not
	 *         a digit
	 */
	private static int bcd(final byte pValue) {
		int high = (pValue & 0xF0) >> 4;
		int low = pValue & 0x0F;
		if (high > 9 || low > 9) {
			return -1;
		}
		return high * 10 + low;
	}

	/**
	 * Method used to remove the leading zeroes a fixed length numeric data
	 * object is padded with
	 *
	 * @param pValue
	 *            value to strip
	 * @return the value without its leading zeroes
	 */
	private static String stripLeadingZeroes(final String pValue) {
		String ret = StringUtils.stripStart(pValue, "0");
		return ret.isEmpty() ? "0" : ret;
	}

	/**
	 * Method used to know if a value is made of printable characters alone
	 *
	 * @param pValue
	 *            value to test
	 * @return true when every byte is a printable ASCII character
	 */
	private static boolean isPrintable(final byte[] pValue) {
		for (byte element : pValue) {
			if (element < 0x20 || element >= 0x7F) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Method used to copy a part of an array
	 *
	 * @param pValue
	 *            array to copy
	 * @param pFrom
	 *            first index, included
	 * @param pTo
	 *            last index, excluded
	 * @return the copied part
	 */
	private static byte[] subarray(final byte[] pValue, final int pFrom, final int pTo) {
		byte[] ret = new byte[pTo - pFrom];
		System.arraycopy(pValue, pFrom, ret, 0, ret.length);
		return ret;
	}

	/**
	 * Private constructor
	 */
	private TagValueDecoder() {
	}

}
