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
package com.github.devnied.emvnfccard.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.github.devnied.emvnfccard.enums.EmvCardScheme;
import com.github.devnied.emvnfccard.iso7816emv.TagAndLength;
import com.github.devnied.emvnfccard.model.enums.ApplicationStepEnum;
import com.github.devnied.emvnfccard.model.enums.CountryCodeEnum;
import com.github.devnied.emvnfccard.model.enums.CurrencyEnum;
import com.github.devnied.emvnfccard.model.enums.KernelEnum;
import com.github.devnied.emvnfccard.model.enums.KernelTypeEnum;

/**
 * Class used to describe Application
 *
 * @author MILLAU Julien
 *
 */
public class Application extends AbstractData implements Comparable<Application> {

	/**
	 * Generated serial UID
	 */
	private static final long serialVersionUID = 2917341864815087679L;

	/**
	 * Mask of the Short Kernel Identifier in the first byte of the Kernel
	 * Identifier (tag '9F2A'), the two high order bits holding the kernel type
	 */
	private static final int SHORT_KERNEL_ID_MASK = 0x3F;

	/**
	 * Mask of the kernel type, the two high order bits of the first byte of the
	 * Kernel Identifier
	 */
	private static final int KERNEL_TYPE_MASK = 0xC0;

	/**
	 * Kernel type of an international kernel, the only one whose Short Kernel
	 * Identifier names a kernel on its own
	 */
	private static final int KERNEL_TYPE_INTERNATIONAL = 0x00;

	/**
	 * Shift bringing the kernel type bits down to the two low order bits
	 */
	private static final int KERNEL_TYPE_SHIFT = 6;

	/**
	 * Length of the Requested Kernel ID of a domestic kernel, the three first
	 * bytes of the Kernel Identifier (EMV Contactless Book B section 3.3.2)
	 */
	private static final int DOMESTIC_REQUESTED_KERNEL_ID_LENGTH = 3;

	/**
	 * Warning status answered to the SELECT of this application
	 */
	private boolean selectWarning;

	/**
	 * Card AID
	 */
	private byte[] aid;

	/**
	 * Reading step
	 */
	private ApplicationStepEnum readingStep =  ApplicationStepEnum.NOT_SELECTED;

	/**
	 * Application label
	 */
	private String applicationLabel;

	/**
	 * Application Preferred Name (tag '9F12'), decoded with the Issuer Code
	 * Table Index
	 */
	private String applicationPreferredName;

	/**
	 * Raw value of the Application Preferred Name (tag '9F12'), kept until the
	 * Issuer Code Table Index that tells how to decode it has been read
	 */
	private byte[] rawApplicationPreferredName;

	/**
	 * Application Primary Account Number (tag '5A')
	 */
	private String pan;

	/**
	 * PAN Sequence Number (tag '5F34'), used to differentiate cards sharing the
	 * same PAN
	 */
	private int panSequenceNumber = UNKNOWN;

	/**
	 * Application Expiration Date (tag '5F24')
	 */
	private Date expirationDate;

	/**
	 * Application Effective Date (tag '5F25')
	 */
	private Date effectiveDate;

	/**
	 * Issuer Country Code (tag '5F28')
	 */
	private CountryCodeEnum issuerCountryCode;

	/**
	 * Application Currency Code (tag '9F42')
	 */
	private CurrencyEnum currency;

	/**
	 * Application Currency Exponent (tag '9F44')
	 */
	private int currencyExponent = UNKNOWN;

	/**
	 * Application Version Number - card (tag '9F08')
	 */
	private int applicationVersionNumber = UNKNOWN;

	/**
	 * Service Code (tag '5F30')
	 */
	private Service serviceCode;

	/**
	 * Language Preference (tag '5F2D'), ISO 639 codes ordered by preference
	 */
	private List<String> languagePreferences = new ArrayList<String>();

	/**
	 * Issuer Code Table Index (tag '9F11')
	 */
	private int issuerCodeTableIndex = UNKNOWN;

	/**
	 * Issuer URL (tag '5F50')
	 */
	private String issuerUrl;

	/**
	 * Application Interchange Profile (tag '82')
	 */
	private ApplicationInterchangeProfile interchangeProfile;

	/**
	 * Application Usage Control (tag '9F07')
	 */
	private ApplicationUsageControl usageControl;

	/**
	 * Cardholder Verification Method list (tag '8E')
	 */
	private List<CVM> cvmList = new ArrayList<CVM>();

	/**
	 * Amount X of the CVM list (tag '8E'), the threshold of the "under X" and
	 * "over X" conditions, in the minor unit of the application currency
	 */
	private long cvmAmountX = UNKNOWN;

	/**
	 * Amount Y of the CVM list (tag '8E'), the threshold of the "under Y" and
	 * "over Y" conditions, in the minor unit of the application currency
	 */
	private long cvmAmountY = UNKNOWN;

	/**
	 * Issuer Action Code - Denial (tag '9F0E'), raw value
	 */
	private byte[] issuerActionCodeDenial;

	/**
	 * Issuer Action Code - Online (tag '9F0F'), raw value
	 */
	private byte[] issuerActionCodeOnline;

	/**
	 * Issuer Action Code - Default (tag '9F0D'), raw value
	 */
	private byte[] issuerActionCodeDefault;

	/**
	 * Card Transaction Qualifiers (tag '9F6C'), raw value
	 */
	private byte[] cardTransactionQualifiers;

	/**
	 * Form Factor Indicator (tag '9F6E'), raw value
	 */
	private byte[] formFactorIndicator;

	/**
	 * Application Capabilities Information (tag '9F5D'), raw value
	 */
	private byte[] applicationCapabilitiesInformation;

	/**
	 * Short Kernel Identifier read from the Kernel Identifier (tag '9F2A'), or
	 * derived from the AID when the card does not provide one
	 */
	private int shortKernelId = UNKNOWN;

	/**
	 * True when the kernel was derived from the AID instead of being stated by
	 * the card
	 */
	private boolean kernelDerived;

	/**
	 * Credentials of the offline data authentication (EMV 4.3 Book 2)
	 */
	private OfflineDataAuthentication offlineDataAuthentication;

	/**
	 * Data objects of the contactless Mag-stripe Mode (EMV Contactless Book C-2
	 * v2.11, Kernel 2)
	 */
	private MagStripeData magStripeData;

	/**
	 * Standalone Data Storage data envelopes (tags '9F70' to '9F79', EMV
	 * Contactless Book C-2 v2.12 section 3.5.2, Kernel 2), the card resident
	 * data objects read with the GET DATA command
	 */
	private DataEnvelopes dataEnvelopes;

	/**
	 * Issuer Application Data (tag '9F10'), raw value
	 */
	private byte[] issuerApplicationData;

	/**
	 * Issuer Identification Number (tag '42'), six digits.<br>
	 * EMV 4.4 Book 3 Annex A1 codes it "n6" over a length of 3 bytes, so the
	 * tag is structurally unable to carry the eight digit Issuer
	 * Identification Number ISO/IEC 7812-1:2017 defines: see
	 * {@link #issuerIdentificationNumberExtended}.
	 */
	private String issuerIdentificationNumber;

	/**
	 * Issuer Identification Number Extended (tag '9F0C'), six or eight
	 * digits.<br>
	 * EMV 4.4 Book 3 Annex A1: "While the first 6 digits of the IINE (tag
	 * '9F0C') and IIN (tag '42') are the same and there is no need to have both
	 * data objects on the card, cards may have both the IIN and IINE data
	 * objects present." The two are therefore kept apart, the card is free to
	 * send either or both.
	 */
	private String issuerIdentificationNumberExtended;

	/**
	 * Application Selection Registered Proprietary Data (tag '9F0A'), raw value
	 */
	private byte[] applicationSelectionRegisteredProprietaryData;

	/**
	 * Lower Consecutive Offline Limit (tag '9F14')
	 */
	private int lowerConsecutiveOfflineLimit = UNKNOWN;

	/**
	 * Upper Consecutive Offline Limit (tag '9F23')
	 */
	private int upperConsecutiveOfflineLimit = UNKNOWN;

	/**
	 * Application Reference Currency (tag '9F3B'), up to four currencies
	 */
	private List<CurrencyEnum> referenceCurrencies = new ArrayList<CurrencyEnum>();

	/**
	 * Application Reference Currency Exponent (tag '9F43'), paired positionally
	 * with {@link #referenceCurrencies}
	 */
	private List<Integer> referenceCurrencyExponents = new ArrayList<Integer>();

	/**
	 * Application Discretionary Data (tag '9F05'), raw value
	 */
	private byte[] applicationDiscretionaryData;

	/**
	 * Card Risk Management Data Object List 1 (tag '8C').<br>
	 * The lists are transient: a tag and length pair is not serializable, and
	 * the identity of a tag is what the parser compares, so a deserialized one
	 * would not be the constant it was read as.
	 */
	private transient List<TagAndLength> cdol1 = new ArrayList<TagAndLength>();

	/**
	 * Card Risk Management Data Object List 2 (tag '8D')
	 */
	private transient List<TagAndLength> cdol2 = new ArrayList<TagAndLength>();

	/**
	 * Dynamic Data Authentication Data Object List (tag '9F49')
	 */
	private transient List<TagAndLength> ddol = new ArrayList<TagAndLength>();

	/**
	 * Transaction Certificate Data Object List (tag '97')
	 */
	private transient List<TagAndLength> tdol = new ArrayList<TagAndLength>();

	/**
	 * True when the Dedicated File Name the card returned does not match the
	 * AID that was selected
	 */
	private boolean dedicatedFileNameMismatch;

	/**
	 * Last Online ATC Register (tag '9F13')
	 */
	private int lastOnlineTransactionCounter = UNKNOWN;

	/**
	 * Offline balance available on the card, when the application supports
	 * offline (electronic cash) payments
	 */
	private BigDecimal offlineBalance;

	/**
	 * Payment Account Reference (tag '9F24'), links every token issued for the
	 * same underlying account
	 */
	private String paymentAccountReference;

	/**
	 * Token Requestor ID (tag '9F19'), only present on the tokenized
	 * credentials of the mobile wallets
	 */
	private String tokenRequestorId;

	/**
	 * Last 4 digits of the funding PAN (tag '9F25') of a tokenized credential
	 */
	private String last4DigitsOfPan;

	/**
	 * Transaction counter ATC
	 */
	private int transactionCounter = UNKNOWN;

	/**
	 * Left PIN try
	 */
	private int leftPinTry = UNKNOWN;

	/**
	 * Application priority, bits 4 to 1 of the Application Priority Indicator
	 * (tag '87'). 1 is the highest priority, 0 means no priority assigned.
	 */
	private int priority = 1;

	/**
	 * Bit 8 of the Application Priority Indicator (tag '87'): the application
	 * may not be selected without the confirmation of the cardholder
	 */
	private boolean cardholderConfirmationRequired;

	/**
	 * True once an Application Priority Indicator has been read from the card
	 */
	private boolean priorityKnown;

	/**
	 * Extended Selection (tag '9F29'), value to append to the ADF Name in the
	 * data field of the SELECT command
	 */
	private byte[] extendedSelection;

	/**
	 * Entry of the payment system directory the application was found in, or
	 * null when the application was found by selecting a known AID: an
	 * application selected that way never goes through a directory.
	 */
	private DirectoryEntry directoryEntry;

	/**
	 * Kernel Identifier (tag '9F2A'), the card preference for the kernel the
	 * contactless application is to be processed on
	 */
	private byte[] kernelIdentifier;

	/**
	 * Indicates that the card answered that the application is blocked
	 */
	private boolean blocked;

	/**
	 * Indicates that the application has to be selected with the "next
	 * occurrence" option, used to enumerate the applications matching a partial
	 * AID
	 */
	private boolean nextOccurrence;
	
	/**
	 * Application amount
	 */
	private float amount = UNKNOWN;

	/**
	 * List of issued payment
	 */
	private List<EmvTransactionRecord> listTransactions;
	/**
	 * True when {@link #getLanguagePreferences()} was supplied by the
	 * directory FCI (Language Preference, tag '5F2D', at directory level)
	 * because the application FCI had none
	 */
	private boolean languagePreferencesFromDirectory;

	/**
	 * True when {@link #getIssuerCodeTableIndex()} was supplied by the
	 * directory FCI (Issuer Code Table Index, tag '9F11', at directory
	 * level)
	 */
	private boolean issuerCodeTableIndexFromDirectory;

	/**
	 * Status bytes SW1 SW2 of the (last) SELECT of this application, four
	 * upper-case hexadecimal characters: '9000', '6283' application
	 * blocked, '6A81' function not supported, '6285', '6985'... Null when
	 * the application was never selected.
	 */
	private String selectStatusWord;

	/**
	 * AID the selection was asked with: the directory ADF Name (tag '4F')
	 * or the scheme AID/RID of the lookup by AID. Unlike {@link #getAid()}
	 * it is never replaced by the DF Name (tag '84'). Null when the
	 * application was never selected.
	 */
	private byte[] requestedAid;

	/**
	 * Dedicated File Name (tag '84') exactly as answered in the FCI of the
	 * application, null when the card returned none ({@link #getAid()} then
	 * keeps the requested AID)
	 */
	private byte[] dedicatedFileName;

	/**
	 * Scheme named by the AID of this application (registered application
	 * provider identifier, EMV 4.4 Book 1 section 12.2.1), before the CB
	 * scheme is replaced by the scheme of the PAN at card level. Null when
	 * the AID names no known scheme.
	 */
	private EmvCardScheme aidScheme;

	/**
	 * Data field of the SELECT response of the application, the File
	 * Control Information template (tag '6F', EMV 4.4 Book 1 section
	 * 11.3.4), status bytes removed. Null when the SELECT failed or was
	 * never sent.
	 */
	private byte[] rawFci;

	/**
	 * Processing Options Data Object List (tag '9F38') raw value, null when
	 * the FCI carries none
	 */
	private byte[] rawPdol;

	/**
	 * Processing Options Data Object List (tag '9F38') parsed into tags and
	 * lengths.<br>The list is transient like the other data object lists: a
	 * tag and length pair is not serializable, {@link #rawPdol} keeps the
	 * bytes.
	 */
	private transient List<TagAndLength> pdol = new ArrayList<TagAndLength>();

	/**
	 * Data field sent in the GET PROCESSING OPTIONS command (Command
	 * Template, tag '83', followed by the terminal values of the PDOL), the
	 * one whose response was parsed. Null when no GPO was sent.
	 */
	private byte[] gpoCommandTemplate;

	/**
	 * Terminal data sent for each PDOL (tag '9F38') entry, keyed by
	 * upper-case hexadecimal tag ('9F66' Terminal Transaction Qualifiers,
	 * '9F1A', '5F2A', '9A', '9C', '9F02', '9F37'...), in PDOL order. Never
	 * null.
	 */
	private Map<String, byte[]> pdolTerminalValues = new LinkedHashMap<String, byte[]>();

	/**
	 * Status bytes SW1 SW2 of the last GET PROCESSING OPTIONS (or of the
	 * READ RECORD fallback when used), four upper-case hexadecimal
	 * characters. Null when none was sent.
	 */
	private String gpoStatusWord;

	/**
	 * True when the GET PROCESSING OPTIONS built from the PDOL (tag '9F38')
	 * was refused and resent with an empty Command Template (tag '83')
	 */
	private boolean gpoRetriedWithoutPdol;

	/**
	 * True when no GET PROCESSING OPTIONS succeeded and the reading fell
	 * back on READ RECORD 1 of SFI 1
	 */
	private boolean gpoReplacedByReadRecord;

	/**
	 * Data field of the response that was parsed as the GET PROCESSING
	 * OPTIONS response (Response Message Template Format 1, tag '80', or
	 * Format 2, tag '77', or the fallback record), status bytes removed.
	 * Null when none.
	 */
	private byte[] rawGpoResponse;

	/**
	 * Application File Locator bytes: value of the tag '94' (Format 2) or
	 * bytes 3 and following of the Response Message Template Format 1 (tag
	 * '80'). Null when none.
	 */
	private byte[] rawAfl;

	/**
	 * Every four byte entry of the Application File Locator (tag '94', EMV
	 * 4.4 Book 3 section 10.2) in card order, invalid entries included
	 * ({@link Afl#isValid()} tells). Never null.
	 */
	private List<Afl> afl = new ArrayList<Afl>();

	/**
	 * Every READ RECORD sent for the Application File Locator (or for the
	 * GPO fallback record), in order, with raw data field and status word;
	 * a record answered with an error has an empty raw data. Never null.
	 */
	private List<RecordData> records = new ArrayList<RecordData>();

	/**
	 * Number of READ RECORD commands sent for the Application File Locator,
	 * 0 when none
	 */
	private int recordsRead;

	/**
	 * True when the maximum number of records stopped the reading before
	 * the end of the Application File Locator
	 */
	private boolean aflTruncated;

	/**
	 * Identification computed at the end of the reading for this
	 * application (AID, PAN, BIC, IBAN, issuer country...). Null before a
	 * read.
	 */
	private CardIdentification identification;

	/**
	 * Type of kernel, bits 8 and 7 of byte 1 of the Kernel Identifier (tag
	 * '9F2A', EMV Contactless Book B Table 3-4), null when no identifier
	 * was read. Set by {@link #setKernelIdentifier(byte[])}.
	 */
	private KernelTypeEnum kernelType;

	/**
	 * Requested Kernel ID of EMV Contactless Book B section 3.3.2, derived
	 * from the Kernel Identifier (tag '9F2A'): byte 1 for an international
	 * or RFU type, bytes 1 to 3 for a domestic type with a non-zero Short
	 * Kernel ID, null otherwise. Set by {@link
	 * #setKernelIdentifier(byte[])}.
	 */
	private byte[] requestedKernelId;

	/**
	 * Bits 6 to 1 of byte 1 of the Kernel Identifier (tag '9F2A') as read,
	 * whatever the kernel type ({@link #getShortKernelId()} stays {@link
	 * AbstractData#UNKNOWN} for a domestic or RFU identifier). {@link
	 * AbstractData#UNKNOWN} when no identifier was read. Set by {@link
	 * #setKernelIdentifier(byte[])}.
	 */
	private int rawShortKernelId = UNKNOWN;

	/**
	 * Every primitive BER-TLV data object found, recursively through the
	 * constructed templates ('6F', 'A5', 'BF0C', '77', '70', '73'...), in
	 * the FCI, the GET PROCESSING OPTIONS response and the records of this
	 * application, keyed by upper-case hexadecimal tag without spaces
	 * ('5A', '9F26', 'DF64'), the first occurrence kept, in reading order.
	 * Never null, the live map is returned.
	 */
	private Map<String, byte[]> rawDataObjects = new LinkedHashMap<String, byte[]>();

	/**
	 * Details of the reading of the transaction log of this application
	 * (Log Entry, tag '9F4D' or 'DF60', and Log Format, tag '9F4F'), null
	 * when the log was never looked for
	 */
	private TransactionLog transactionLog;

	/**
	 * For every GET DATA sent for this application: upper-case hexadecimal
	 * tag ('9F17', '9F36', '9F13', '9F50', '9F79', '9F4F', '9F70'...)
	 * mapped to the status bytes SW1 SW2 (four upper-case hexadecimal
	 * characters, e.g. '9000', '6A88', '6985', '6D00'). Never null.
	 */
	private Map<String, String> getDataStatusWords = new LinkedHashMap<String, String>();

	/**
	 * Raw value answered for the offline balance tag ('9F79' or '9F50'),
	 * kept even when it is not the six byte n12 encoding ({@link
	 * #getOfflineBalance()} then stays null). Null when neither tag
	 * answered a value.
	 */
	private byte[] rawOfflineBalance;

	/**
	 * Tag the balance value was read from: '9F79' (Electronic Cash Balance,
	 * UnionPay) or '9F50' (Offline Accumulator Balance, M/Chip). Null when
	 * neither answered a value.
	 */
	private String offlineBalanceTag;

	/**
	 * Application Currency Code (tag '9F42') numeric ISO 4217 value as
	 * sent, even when the currency enumeration has no value for it ({@link
	 * #getCurrency()} is then null). {@link AbstractData#UNKNOWN} when
	 * absent.
	 */
	private int currencyCode = UNKNOWN;

	/**
	 * Issuer Country Code (tag '5F28') numeric ISO 3166 value as sent, even
	 * when the country enumeration has no value for it. {@link
	 * AbstractData#UNKNOWN} when absent.
	 */
	private int issuerCountryCodeNumeric = UNKNOWN;

	/**
	 * Issuer Country Code (alpha3 format) (tag '5F56') as text, null when
	 * absent
	 */
	private String issuerCountryCodeAlpha3;

	/**
	 * Issuer Country Code (alpha2 format) (tag '5F55') as text, null when
	 * absent
	 */
	private String issuerCountryCodeAlpha2;

	/**
	 * Every two byte code of the Application Reference Currency (tag
	 * '9F3B') in card order, zero and unknown codes included ({@link
	 * #getReferenceCurrencies()} keeps only the known ones). Never null.
	 */
	private List<Integer> referenceCurrencyCodes = new ArrayList<Integer>();

	/**
	 * The three digits of the Service Code (tag '5F30') as sent ({@link
	 * #getServiceCode()} decodes them and loses any digit the enumerations
	 * do not know), null when absent
	 */
	private String serviceCodeDigits;

	/**
	 * Application Label (tag '50') of the application FCI decoded with the
	 * common character set, kept apart from {@link #getApplicationLabel()}
	 * which prefers the Application Preferred Name (tag '9F12'). Null when
	 * absent.
	 */
	private String fciApplicationLabel;

	/**
	 * Bank Identifier Code (tag '5F54') read in the responses of this
	 * application, first found; the card level value keeps its last-wins
	 * rule. Null when absent.
	 */
	private String bic;

	/**
	 * International Bank Account Number (tag '5F53') read in the responses
	 * of this application, first found. Null when absent.
	 */
	private String iban;

	/**
	 * Cardholder Name (tag '5F20') of this application, decoded and
	 * trimmed, undivided, first found; null when absent or empty after
	 * trimming
	 */
	private String cardholderName;

	/**
	 * Cardholder Name Extended (tag '9F0B') of this application, decoded,
	 * first found. Null when absent.
	 */
	private String cardholderNameExtended;

	/**
	 * Value of the tag '9F66' as answered by the card, any length: two
	 * bytes are the PUNATC(Track2) of Kernel 2, four bytes the Terminal
	 * Transaction Qualifiers echoed by a Kernel 3 card ({@link
	 * MagStripeData#getPunatcTrack2()} keeps its two byte rule). Null when
	 * absent.
	 */
	private byte[] terminalTransactionQualifiersEcho;

	/**
	 * Card Transaction Qualifiers (tag '9F6C') decoded, only set when the
	 * resolved kernel is Visa (Kernel 3) and the value is two bytes; {@link
	 * #getCardTransactionQualifiers()} stays raw for every kernel. Null
	 * otherwise.
	 */
	private CardTransactionQualifiers cardTransactionQualifiersDecoded;

	/**
	 * Kernel used to interpret the payment system tags of this application:
	 * the one stated by the card (Kernel Identifier, tag '9F2A') or derived
	 * from the AID. Null when none.
	 */
	private KernelEnum resolvedKernel;

	/**
	 * Card Risk Management Data Object List 1 (tag '8C') raw value, null
	 * when absent
	 */
	private byte[] rawCdol1;

	/**
	 * Card Risk Management Data Object List 2 (tag '8D') raw value, null
	 * when absent
	 */
	private byte[] rawCdol2;

	/**
	 * Dynamic Data Authentication Data Object List (tag '9F49') raw value,
	 * null when absent
	 */
	private byte[] rawDdol;

	/**
	 * Transaction Certificate Data Object List (tag '97') raw value, null
	 * when absent
	 */
	private byte[] rawTdol;

	/**
	 * Data field of the last SELECT sent for this application: the AID
	 * alone, or the AID followed by the Extended Selection (tag '9F29')
	 * when the reader appended it. Null when never selected.
	 */
	private byte[] selectedName;

	/**
	 * True when the SELECT with the Extended Selection (tag '9F29') was
	 * refused and the reading retried with the ADF Name alone
	 */
	private boolean extendedSelectionRefused;

	/**
	 * True when the Extended Selection (tag '9F29') was not appended
	 * because AID and Extended Selection together exceeded 16 bytes (EMV
	 * Contactless Book B Table A-1)
	 */
	private boolean extendedSelectionTooLong;

	/**
	 * GeldKarte purse files (EF_ID, EF_BETRAG, EF_BLOG) of the application,
	 * null for a non GeldKarte application
	 */
	private GeldKarteData geldKarte;


	/**
	 * Method used to get the field applicationLabel
	 *
	 * @return the applicationLabel
	 */
	public String getApplicationLabel() {
		return applicationLabel;
	}

	/**
	 * Setter for the field applicationLabel
	 *
	 * @param applicationLabel
	 *            the applicationLabel to set
	 */
	public void setApplicationLabel(final String applicationLabel) {
		this.applicationLabel = applicationLabel;
	}

	/**
	 * Method used to get the field transactionCounter
	 *
	 * @return the transactionCounter
	 */
	public int getTransactionCounter() {
		return transactionCounter;
	}

	/**
	 * Setter for the field transactionCounter
	 *
	 * @param transactionCounter
	 *            the transactionCounter to set
	 */
	public void setTransactionCounter(final int transactionCounter) {
		this.transactionCounter = transactionCounter;
	}

	/**
	 * Method used to get the field leftPinTry
	 *
	 * @return the leftPinTry
	 */
	public int getLeftPinTry() {
		return leftPinTry;
	}

	/**
	 * Setter for the field leftPinTry
	 *
	 * @param leftPinTry
	 *            the leftPinTry to set
	 */
	public void setLeftPinTry(final int leftPinTry) {
		this.leftPinTry = leftPinTry;
	}

	/**
	 * Method used to get the field listTransactions
	 *
	 * @return the listTransactions
	 */
	public List<EmvTransactionRecord> getListTransactions() {
		return listTransactions;
	}

	/**
	 * Setter for the field listTransactions
	 *
	 * @param listTransactions
	 *            the listTransactions to set
	 */
	public void setListTransactions(final List<EmvTransactionRecord> listTransactions) {
		this.listTransactions = listTransactions;
	}

	/**
	 * Method used to get the field aid
	 *
	 * @return the aid
	 */
	public byte[] getAid() {
		return aid;
	}

	/**
	 * Setter for the field aid
	 *
	 * @param aid
	 *            the aid to set
	 */
	public void setAid(final byte[] aid) {
		if( aid != null) {
			this.aid = aid;
		}
	}

	/**
	 * Method used to get the field priority
	 *
	 * @return the priority
	 */
	public int getPriority() {
		return priority;
	}

	/**
	 * Setter for the field priority
	 *
	 * @param priority
	 *            the priority to set
	 */
	public void setPriority(final int priority) {
		this.priority = priority;
	}

	/**
	 * Setter for the Application Priority Indicator (tag '87'), it holds both
	 * the priority order (bits 4 to 1) and the flag telling that the
	 * application may not be selected without the confirmation of the
	 * cardholder (bit 8), see EMV 4.3 Book 1 section 12.2.3.
	 *
	 * @param pIndicator
	 *            raw value of the tag '87'
	 */
	public void setApplicationPriorityIndicator(final byte pIndicator) {
		priority = pIndicator & 0x0F;
		cardholderConfirmationRequired = (pIndicator & 0x80) != 0;
		priorityKnown = true;
	}

	/**
	 * Method used to know whether the priority comes from the card or is the
	 * default one. An application selected by AID never goes through a payment
	 * directory, so it may carry no Application Priority Indicator at all.
	 *
	 * @return true if the card provided an Application Priority Indicator
	 */
	public boolean isPriorityKnown() {
		return priorityKnown;
	}

	/**
	 * Get the field cardholderConfirmationRequired
	 *
	 * @return true if the application may not be selected without the
	 *         confirmation of the cardholder
	 */
	public boolean isCardholderConfirmationRequired() {
		return cardholderConfirmationRequired;
	}

	/**
	 * Setter for the field cardholderConfirmationRequired
	 *
	 * @param cardholderConfirmationRequired
	 *            the cardholderConfirmationRequired to set
	 */
	public void setCardholderConfirmationRequired(final boolean cardholderConfirmationRequired) {
		this.cardholderConfirmationRequired = cardholderConfirmationRequired;
	}

	/**
	 * Method used to get the entry of the payment system directory the
	 * application was found in.<br>
	 * The application only keeps what the selection needs, the entry keeps
	 * everything the card listed: the Path (tag '51'), the Directory
	 * Discretionary Template (tag '73'), the record the entry was read in.
	 *
	 * @return the directoryEntry, or null when the application was found by
	 *         selecting a known AID
	 */
	public DirectoryEntry getDirectoryEntry() {
		return directoryEntry;
	}

	/**
	 * Setter for the field directoryEntry
	 *
	 * @param directoryEntry
	 *            the directoryEntry to set
	 */
	public void setDirectoryEntry(final DirectoryEntry directoryEntry) {
		this.directoryEntry = directoryEntry;
	}

	/**
	 * Get the Extended Selection (tag '9F29')
	 *
	 * @return the extendedSelection or null
	 */
	public byte[] getExtendedSelection() {
		return extendedSelection;
	}

	/**
	 * Setter for the field extendedSelection
	 *
	 * @param extendedSelection
	 *            the extendedSelection to set
	 */
	public void setExtendedSelection(final byte[] extendedSelection) {
		this.extendedSelection = extendedSelection;
	}

	/**
	 * Get the Kernel Identifier (tag '9F2A')
	 *
	 * @return the kernelIdentifier or null
	 */
	public byte[] getKernelIdentifier() {
		return kernelIdentifier;
	}

	/**
	 * Setter for the field kernelIdentifier
	 *
	 * @param kernelIdentifier
	 *            the kernelIdentifier to set
	 */
	public void setKernelIdentifier(final byte[] kernelIdentifier) {
		this.kernelIdentifier = kernelIdentifier;
		// The derived view of the identifier follows the identifier itself,
		// with the rules of DirectoryEntry.setKernelIdentifier
		kernelType = null;
		requestedKernelId = null;
		rawShortKernelId = UNKNOWN;
		// EMV Contactless Book B: the two high order bits of the first byte
		// give the kernel type and the six others the Short Kernel Identifier.
		// A conforming identifier is one byte, or three and more, but never
		// two: a two byte one is read all the same, the first byte carries the
		// same meaning whatever the length that follows it
		if (kernelIdentifier == null || kernelIdentifier.length == 0) {
			return;
		}
		int type = kernelIdentifier[0] & KERNEL_TYPE_MASK;
		kernelType = KernelTypeEnum.find(type >> KERNEL_TYPE_SHIFT);
		rawShortKernelId = kernelIdentifier[0] & SHORT_KERNEL_ID_MASK;
		if (kernelType == KernelTypeEnum.INTERNATIONAL || kernelType == KernelTypeEnum.RFU) {
			// "Requested Kernel ID is equal to the value of byte 1 of the
			// Kernel Identifier"
			requestedKernelId = new byte[] { kernelIdentifier[0] };
		} else if (rawShortKernelId != 0 && kernelIdentifier.length >= DOMESTIC_REQUESTED_KERNEL_ID_LENGTH) {
			// "the Requested Kernel ID is equal to value of the byte 1 to byte
			// 3 of the Kernel Identifier". A domestic identifier shorter than
			// three bytes has its entry rejected by Entry Point, and a Short
			// Kernel ID of zero leaves the derivation out of scope
			requestedKernelId = Arrays.copyOf(kernelIdentifier, DOMESTIC_REQUESTED_KERNEL_ID_LENGTH);
		}
		// Only the type '00b' names an international kernel. A domestic kernel
		// ('10b' or '11b') is identified by the first three bytes as a whole,
		// and the type '01b' is reserved for future use, its Requested Kernel ID
		// being the whole first byte ('4X', not the kernel 'X'). Reading either
		// as an international kernel would report the wrong one: the identifier
		// is exposed raw and no kernel is claimed.
		if (type != KERNEL_TYPE_INTERNATIONAL) {
			return;
		}
		int identifier = kernelIdentifier[0] & SHORT_KERNEL_ID_MASK;
		// '000000' means that the kernel is the one the ADF Name implies, so
		// the card has said nothing and the AID has to be used instead
		if (identifier != 0) {
			shortKernelId = identifier;
			kernelDerived = false;
		}
	}

	/**
	 * Method used to know if the card asked for a given kernel.
	 * <p>
	 * EMV Contactless Book B builds the Requested Kernel ID from the Kernel
	 * Identifier of the directory entry: "If the length of the Kernel Identifier
	 * value field is zero, then Entry Point shall use a default value for the
	 * Requested Kernel ID, based on the matching AID". A Requested Kernel ID of
	 * zero means in turn that any kernel of the reader suits the card. In both
	 * cases the card expressed no preference and the kernel may be derived from
	 * the AID.
	 * </p>
	 * <p>
	 * A non zero first byte is a request. This library names it when it is one of
	 * the international kernels, and otherwise reports
	 * {@link #getKernelIdentifier()} raw rather than a guess: a domestic kernel
	 * ('10b' or '11b') is identified by the first three bytes of the identifier
	 * as a whole, and the type '01b', which is reserved for future use, gives a
	 * Requested Kernel ID of '4X' that is not the international kernel 'X'.
	 * </p>
	 *
	 * @return true if the card asked for a specific kernel
	 */
	public boolean isKernelRequestedByCard() {
		return kernelIdentifier != null && kernelIdentifier.length > 0 && kernelIdentifier[0] != 0;
	}

	/**
	 * Get the field selectWarning
	 *
	 * @return true if the card answered a warning status ('62xx' or '63xx') to
	 *         the SELECT of this application
	 */
	public boolean isSelectWarning() {
		return selectWarning;
	}

	/**
	 * Setter for the field selectWarning
	 *
	 * @param selectWarning
	 *            the selectWarning to set
	 */
	public void setSelectWarning(final boolean selectWarning) {
		this.selectWarning = selectWarning;
	}

	/**
	 * Get the field blocked
	 *
	 * @return true if the card answered that the application is blocked
	 */
	public boolean isBlocked() {
		return blocked;
	}

	/**
	 * Setter for the field blocked
	 *
	 * @param blocked
	 *            the blocked to set
	 */
	public void setBlocked(final boolean blocked) {
		this.blocked = blocked;
	}

	/**
	 * Get the field nextOccurrence
	 *
	 * @return true if the application has to be selected with the "next
	 *         occurrence" option
	 */
	public boolean isNextOccurrence() {
		return nextOccurrence;
	}

	/**
	 * Setter for the field nextOccurrence
	 *
	 * @param nextOccurrence
	 *            the nextOccurrence to set
	 */
	public void setNextOccurrence(final boolean nextOccurrence) {
		this.nextOccurrence = nextOccurrence;
	}

	@Override
	public int compareTo(final Application arg0) {
		// A priority of 0 means that no priority was assigned to the
		// application, it is selected after the ones that have a priority
		return normalizedPriority() - arg0.normalizedPriority();
	}

	/**
	 * Priority used to order the applications, an application without priority
	 * comes last.
	 *
	 * @return the priority to sort on
	 */
	private int normalizedPriority() {
		return priority == 0 ? Integer.MAX_VALUE : priority;
	}

	/**
	 * Get the field readingStep
	 * @return the readingStep
	 */
	public ApplicationStepEnum getReadingStep() {
		return readingStep;
	}

	/**
	 * Setter for the field readingStep
	 *
	 * @param readingStep the readingStep to set
	 */
	public void setReadingStep(ApplicationStepEnum readingStep) {
		this.readingStep = readingStep;
	}

	/**
	 * Get the field amount
	 * @return the amount
	 */
	public float getAmount() {
		return amount;
	}

	/**
	 * Setter for the field amount
	 *
	 * @param amount the amount to set
	 */
	public void setAmount(float amount) {
		this.amount = amount;
	}

	/**
	 * Get the Application Preferred Name (tag '9F12')
	 *
	 * @return the applicationPreferredName
	 */
	public String getApplicationPreferredName() {
		return applicationPreferredName;
	}

	/**
	 * Setter for the field applicationPreferredName
	 *
	 * @param applicationPreferredName
	 *            the applicationPreferredName to set
	 */
	public void setApplicationPreferredName(final String applicationPreferredName) {
		this.applicationPreferredName = applicationPreferredName;
	}

	/**
	 * Get the raw value of the Application Preferred Name (tag '9F12')
	 *
	 * @return the rawApplicationPreferredName or null
	 */
	public byte[] getRawApplicationPreferredName() {
		return rawApplicationPreferredName;
	}

	/**
	 * Setter for the field rawApplicationPreferredName
	 *
	 * @param rawApplicationPreferredName
	 *            the rawApplicationPreferredName to set
	 */
	public void setRawApplicationPreferredName(final byte[] rawApplicationPreferredName) {
		this.rawApplicationPreferredName = rawApplicationPreferredName;
	}

	/**
	 * Get the Application Primary Account Number (tag '5A')
	 *
	 * @return the pan
	 */
	public String getPan() {
		return pan;
	}

	/**
	 * Setter for the field pan
	 *
	 * @param pan
	 *            the pan to set
	 */
	public void setPan(final String pan) {
		this.pan = pan;
	}

	/**
	 * Get the PAN Sequence Number (tag '5F34')
	 *
	 * @return the panSequenceNumber
	 */
	public int getPanSequenceNumber() {
		return panSequenceNumber;
	}

	/**
	 * Setter for the field panSequenceNumber
	 *
	 * @param panSequenceNumber
	 *            the panSequenceNumber to set
	 */
	public void setPanSequenceNumber(final int panSequenceNumber) {
		this.panSequenceNumber = panSequenceNumber;
	}

	/**
	 * Get the Application Expiration Date (tag '5F24')
	 *
	 * @return the expirationDate
	 */
	public Date getExpirationDate() {
		return expirationDate;
	}

	/**
	 * Setter for the field expirationDate
	 *
	 * @param expirationDate
	 *            the expirationDate to set
	 */
	public void setExpirationDate(final Date expirationDate) {
		this.expirationDate = expirationDate;
	}

	/**
	 * Get the Application Effective Date (tag '5F25')
	 *
	 * @return the effectiveDate
	 */
	public Date getEffectiveDate() {
		return effectiveDate;
	}

	/**
	 * Setter for the field effectiveDate
	 *
	 * @param effectiveDate
	 *            the effectiveDate to set
	 */
	public void setEffectiveDate(final Date effectiveDate) {
		this.effectiveDate = effectiveDate;
	}

	/**
	 * Get the Issuer Country Code (tag '5F28')
	 *
	 * @return the issuerCountryCode
	 */
	public CountryCodeEnum getIssuerCountryCode() {
		return issuerCountryCode;
	}

	/**
	 * Setter for the field issuerCountryCode
	 *
	 * @param issuerCountryCode
	 *            the issuerCountryCode to set
	 */
	public void setIssuerCountryCode(final CountryCodeEnum issuerCountryCode) {
		this.issuerCountryCode = issuerCountryCode;
	}

	/**
	 * Get the Application Currency Code (tag '9F42')
	 *
	 * @return the currency
	 */
	public CurrencyEnum getCurrency() {
		return currency;
	}

	/**
	 * Setter for the field currency
	 *
	 * @param currency
	 *            the currency to set
	 */
	public void setCurrency(final CurrencyEnum currency) {
		this.currency = currency;
	}

	/**
	 * Get the Application Currency Exponent (tag '9F44')
	 *
	 * @return the currencyExponent
	 */
	public int getCurrencyExponent() {
		return currencyExponent;
	}

	/**
	 * Setter for the field currencyExponent
	 *
	 * @param currencyExponent
	 *            the currencyExponent to set
	 */
	public void setCurrencyExponent(final int currencyExponent) {
		this.currencyExponent = currencyExponent;
	}

	/**
	 * Get the Application Version Number (tag '9F08')
	 *
	 * @return the applicationVersionNumber
	 */
	public int getApplicationVersionNumber() {
		return applicationVersionNumber;
	}

	/**
	 * Setter for the field applicationVersionNumber
	 *
	 * @param applicationVersionNumber
	 *            the applicationVersionNumber to set
	 */
	public void setApplicationVersionNumber(final int applicationVersionNumber) {
		this.applicationVersionNumber = applicationVersionNumber;
	}

	/**
	 * Get the Service Code (tag '5F30')
	 *
	 * @return the serviceCode
	 */
	public Service getServiceCode() {
		return serviceCode;
	}

	/**
	 * Setter for the field serviceCode
	 *
	 * @param serviceCode
	 *            the serviceCode to set
	 */
	public void setServiceCode(final Service serviceCode) {
		this.serviceCode = serviceCode;
	}

	/**
	 * Get the Language Preference (tag '5F2D')
	 *
	 * @return the languagePreferences ordered by preference
	 */
	public List<String> getLanguagePreferences() {
		return Collections.unmodifiableList(languagePreferences);
	}

	/**
	 * Setter for the field languagePreferences
	 *
	 * @param languagePreferences
	 *            the languagePreferences to set
	 */
	public void setLanguagePreferences(final List<String> languagePreferences) {
		this.languagePreferences = languagePreferences != null ? languagePreferences : new ArrayList<String>();
	}

	/**
	 * Get the Issuer Code Table Index (tag '9F11')
	 *
	 * @return the issuerCodeTableIndex
	 */
	public int getIssuerCodeTableIndex() {
		return issuerCodeTableIndex;
	}

	/**
	 * Setter for the field issuerCodeTableIndex
	 *
	 * @param issuerCodeTableIndex
	 *            the issuerCodeTableIndex to set
	 */
	public void setIssuerCodeTableIndex(final int issuerCodeTableIndex) {
		this.issuerCodeTableIndex = issuerCodeTableIndex;
	}

	/**
	 * Get the Issuer URL (tag '5F50')
	 *
	 * @return the issuerUrl
	 */
	public String getIssuerUrl() {
		return issuerUrl;
	}

	/**
	 * Setter for the field issuerUrl
	 *
	 * @param issuerUrl
	 *            the issuerUrl to set
	 */
	public void setIssuerUrl(final String issuerUrl) {
		this.issuerUrl = issuerUrl;
	}

	/**
	 * Get the Application Interchange Profile (tag '82')
	 *
	 * @return the interchangeProfile
	 */
	public ApplicationInterchangeProfile getInterchangeProfile() {
		return interchangeProfile;
	}

	/**
	 * Setter for the field interchangeProfile
	 *
	 * @param interchangeProfile
	 *            the interchangeProfile to set
	 */
	public void setInterchangeProfile(final ApplicationInterchangeProfile interchangeProfile) {
		this.interchangeProfile = interchangeProfile;
	}

	/**
	 * Get the Application Usage Control (tag '9F07')
	 *
	 * @return the usageControl
	 */
	public ApplicationUsageControl getUsageControl() {
		return usageControl;
	}

	/**
	 * Setter for the field usageControl
	 *
	 * @param usageControl
	 *            the usageControl to set
	 */
	public void setUsageControl(final ApplicationUsageControl usageControl) {
		this.usageControl = usageControl;
	}

	/**
	 * Get the Cardholder Verification Method list (tag '8E')
	 *
	 * @return the cvmList
	 */
	public List<CVM> getCvmList() {
		return Collections.unmodifiableList(cvmList);
	}

	/**
	 * Setter for the field cvmList
	 *
	 * @param cvmList
	 *            the cvmList to set
	 */
	public void setCvmList(final List<CVM> cvmList) {
		this.cvmList = cvmList != null ? new ArrayList<CVM>(cvmList) : new ArrayList<CVM>();
	}

	/**
	 * Get the amount X of the CVM list (tag '8E')
	 *
	 * @return the amount X or {@link #UNKNOWN}
	 */
	public long getCvmAmountX() {
		return cvmAmountX;
	}

	/**
	 * Setter for the field cvmAmountX
	 *
	 * @param cvmAmountX
	 *            the cvmAmountX to set
	 */
	public void setCvmAmountX(final long cvmAmountX) {
		this.cvmAmountX = cvmAmountX;
	}

	/**
	 * Get the amount Y of the CVM list (tag '8E')
	 *
	 * @return the amount Y or {@link #UNKNOWN}
	 */
	public long getCvmAmountY() {
		return cvmAmountY;
	}

	/**
	 * Setter for the field cvmAmountY
	 *
	 * @param cvmAmountY
	 *            the cvmAmountY to set
	 */
	public void setCvmAmountY(final long cvmAmountY) {
		this.cvmAmountY = cvmAmountY;
	}

	/**
	 * Get the Issuer Action Code - Denial (tag '9F0E'): the conditions under
	 * which the issuer wants the transaction denied without going online. It
	 * is a bitmap built on the Terminal Verification Results layout (EMV 4.3
	 * Book 3 Annex C5).
	 *
	 * @return the raw value or null
	 */
	public byte[] getIssuerActionCodeDenial() {
		return issuerActionCodeDenial;
	}

	/**
	 * Setter for the field issuerActionCodeDenial
	 *
	 * @param issuerActionCodeDenial
	 *            the issuerActionCodeDenial to set
	 */
	public void setIssuerActionCodeDenial(final byte[] issuerActionCodeDenial) {
		this.issuerActionCodeDenial = issuerActionCodeDenial;
	}

	/**
	 * Get the Issuer Action Code - Online (tag '9F0F'): the conditions under
	 * which the issuer wants the transaction to be sent online.
	 *
	 * @return the raw value or null
	 */
	public byte[] getIssuerActionCodeOnline() {
		return issuerActionCodeOnline;
	}

	/**
	 * Setter for the field issuerActionCodeOnline
	 *
	 * @param issuerActionCodeOnline
	 *            the issuerActionCodeOnline to set
	 */
	public void setIssuerActionCodeOnline(final byte[] issuerActionCodeOnline) {
		this.issuerActionCodeOnline = issuerActionCodeOnline;
	}

	/**
	 * Get the Issuer Action Code - Default (tag '9F0D'): the conditions under
	 * which the issuer wants the transaction rejected when the terminal cannot
	 * go online.
	 *
	 * @return the raw value or null
	 */
	public byte[] getIssuerActionCodeDefault() {
		return issuerActionCodeDefault;
	}

	/**
	 * Setter for the field issuerActionCodeDefault
	 *
	 * @param issuerActionCodeDefault
	 *            the issuerActionCodeDefault to set
	 */
	public void setIssuerActionCodeDefault(final byte[] issuerActionCodeDefault) {
		this.issuerActionCodeDefault = issuerActionCodeDefault;
	}

	/**
	 * Get the Card Transaction Qualifiers (tag '9F6C'), the answer of a Visa
	 * application to the Terminal Transaction Qualifiers.<br>
	 * The same tag carries the Mag Stripe Application Version Number on the
	 * MasterCard applications, so the value is exposed raw.
	 *
	 * @return the raw value or null
	 */
	public byte[] getCardTransactionQualifiers() {
		return cardTransactionQualifiers;
	}

	/**
	 * Setter for the field cardTransactionQualifiers
	 *
	 * @param cardTransactionQualifiers
	 *            the cardTransactionQualifiers to set
	 */
	public void setCardTransactionQualifiers(final byte[] cardTransactionQualifiers) {
		this.cardTransactionQualifiers = cardTransactionQualifiers;
	}

	/**
	 * Get the Form Factor Indicator (tag '9F6E'), which describes the device
	 * the credential lives in (card, phone, watch, wearable) and its payment
	 * technology.<br>
	 * The same tag carries the Third Party Data on the MasterCard
	 * applications, so the value is exposed raw.
	 *
	 * @return the raw value or null
	 */
	public byte[] getFormFactorIndicator() {
		return formFactorIndicator;
	}

	/**
	 * Setter for the field formFactorIndicator
	 *
	 * @param formFactorIndicator
	 *            the formFactorIndicator to set
	 */
	public void setFormFactorIndicator(final byte[] formFactorIndicator) {
		this.formFactorIndicator = formFactorIndicator;
	}

	/**
	 * Get the raw value of the tag '9F5D'.
	 * <p>
	 * <b>Its meaning depends on the payment system</b>: on a MasterCard
	 * application it is the Application Capabilities Information, a 3 bytes
	 * bitmap listing the card features beyond regular payment; on a Visa
	 * application it is the Available Offline Spending Amount, a 6 bytes
	 * amount. The length tells them apart, and the value is exposed raw so
	 * that an amount is never reported as a capability.
	 * </p>
	 *
	 * @return the raw value or null
	 */
	public byte[] getApplicationCapabilitiesInformation() {
		return applicationCapabilitiesInformation;
	}

	/**
	 * Setter for the field applicationCapabilitiesInformation
	 *
	 * @param applicationCapabilitiesInformation
	 *            the applicationCapabilitiesInformation to set
	 */
	public void setApplicationCapabilitiesInformation(final byte[] applicationCapabilitiesInformation) {
		this.applicationCapabilitiesInformation = applicationCapabilitiesInformation;
	}

	/**
	 * Get the Short Kernel Identifier of the contactless kernel the application
	 * is meant to be processed on, read from the Kernel Identifier (tag '9F2A')
	 * or derived from the AID when the card provides none.
	 *
	 * @return the short kernel identifier or {@link #UNKNOWN}
	 */
	public int getShortKernelId() {
		return shortKernelId;
	}

	/**
	 * Setter for the field shortKernelId
	 *
	 * @param shortKernelId
	 *            the shortKernelId to set
	 */
	public void setShortKernelId(final int shortKernelId) {
		this.shortKernelId = shortKernelId;
	}

	/**
	 * Get the contactless kernel of the application.
	 *
	 * @return the kernel, or null when the card announced an identifier the
	 *         specification does not name (see {@link #getShortKernelId()})
	 */
	public KernelEnum getKernel() {
		return KernelEnum.find(shortKernelId);
	}

	/**
	 * Method used to know whether the kernel was stated by the card or guessed
	 * from the AID.
	 *
	 * @return true when the kernel was derived from the AID
	 */
	public boolean isKernelDerived() {
		return kernelDerived;
	}

	/**
	 * Setter for the field kernelDerived
	 *
	 * @param kernelDerived
	 *            the kernelDerived to set
	 */
	public void setKernelDerived(final boolean kernelDerived) {
		this.kernelDerived = kernelDerived;
	}

	/**
	 * Get the credentials the card carries for the offline data authentication.
	 * <br>
	 * Nothing is verified, see {@link OfflineDataAuthentication}.
	 *
	 * @return the credentials, never null
	 */
	public OfflineDataAuthentication getOfflineDataAuthentication() {
		if (offlineDataAuthentication == null) {
			offlineDataAuthentication = new OfflineDataAuthentication();
		}
		return offlineDataAuthentication;
	}

	/**
	 * Get the data objects the card carries for the contactless Mag-stripe
	 * Mode of EMV Contactless Book C-2 v2.11.
	 * <p>
	 * The profile belongs to Kernel 2, and two of its tags are read by another
	 * kernel as something else entirely, so it is only filled for an
	 * application whose kernel is the MasterCard one. See
	 * {@link MagStripeData}.
	 * </p>
	 *
	 * @return the mag-stripe data objects, never null
	 */
	public MagStripeData getMagStripeData() {
		if (magStripeData == null) {
			magStripeData = new MagStripeData();
		}
		return magStripeData;
	}

	/**
	 * Get the Standalone Data Storage data envelopes (tags '9F70' to '9F79')
	 * the card answered to the GET DATA command.
	 * <p>
	 * They are the only card resident data objects EMV Contactless Book C-2
	 * v2.12 has the Kernel read with GET DATA, and the whole range is readable
	 * without any authentication. The envelopes are only read for an
	 * application whose kernel is Kernel 2 and only when
	 * {@code Config.readExtendedData} is turned on, see {@link DataEnvelopes}.
	 * </p>
	 *
	 * @return the data envelopes, never null
	 */
	public DataEnvelopes getDataEnvelopes() {
		if (dataEnvelopes == null) {
			dataEnvelopes = new DataEnvelopes();
		}
		return dataEnvelopes;
	}

	/**
	 * Get the Issuer Application Data (tag '9F10').
	 * <p>
	 * The value is exposed raw: its internal layout is proprietary to each
	 * payment system, and the Card Verification Results it carries only mean
	 * something relative to a transaction this library never runs.
	 * </p>
	 *
	 * @return the raw value or null
	 */
	public byte[] getIssuerApplicationData() {
		return issuerApplicationData;
	}

	/**
	 * Setter for the field issuerApplicationData
	 *
	 * @param issuerApplicationData
	 *            the issuerApplicationData to set
	 */
	public void setIssuerApplicationData(final byte[] issuerApplicationData) {
		this.issuerApplicationData = issuerApplicationData;
	}

	/**
	 * Get the Issuer Identification Number (tag '42'), which identifies the
	 * issuer independently of the PAN. It is the only issuer identity a
	 * tokenized credential may expose, since its PAN is a token.
	 *
	 * @return the issuer identification number or null
	 */
	public String getIssuerIdentificationNumber() {
		return issuerIdentificationNumber;
	}

	/**
	 * Setter for the field issuerIdentificationNumber
	 *
	 * @param issuerIdentificationNumber
	 *            the issuerIdentificationNumber to set
	 */
	public void setIssuerIdentificationNumber(final String issuerIdentificationNumber) {
		this.issuerIdentificationNumber = issuerIdentificationNumber;
	}

	/**
	 * Get the Issuer Identification Number Extended (tag '9F0C'), the six or
	 * eight digit form of the Issuer Identification Number.
	 * <p>
	 * ISO/IEC 7812-1:2017 (fifth edition) took the length of an Issuer
	 * Identification Number to eight digits, its Foreword listing among the key
	 * changes of the edition: "Clause 4: revised the length of an IIN to 8
	 * digits (from 6 digits)". The tag '42' of EMV is coded "n6" over three
	 * bytes and cannot hold such a number, which is why EMVCo added this tag.
	 * </p>
	 *
	 * @return the issuer identification number extended or null
	 */
	public String getIssuerIdentificationNumberExtended() {
		return issuerIdentificationNumberExtended;
	}

	/**
	 * Setter for the field issuerIdentificationNumberExtended
	 *
	 * @param issuerIdentificationNumberExtended
	 *            the issuerIdentificationNumberExtended to set
	 */
	public void setIssuerIdentificationNumberExtended(final String issuerIdentificationNumberExtended) {
		this.issuerIdentificationNumberExtended = issuerIdentificationNumberExtended;
	}

	/**
	 * Get the Application Selection Registered Proprietary Data (tag '9F0A'),
	 * exposed raw: its content is registered per proprietary data identifier
	 * and a generic reader cannot decode it.
	 *
	 * @return the raw value or null
	 */
	public byte[] getApplicationSelectionRegisteredProprietaryData() {
		return applicationSelectionRegisteredProprietaryData;
	}

	/**
	 * Setter for the field applicationSelectionRegisteredProprietaryData
	 *
	 * @param pValue
	 *            the value to set
	 */
	public void setApplicationSelectionRegisteredProprietaryData(final byte[] pValue) {
		applicationSelectionRegisteredProprietaryData = pValue;
	}

	/**
	 * Get the Lower Consecutive Offline Limit (tag '9F14'), the number of
	 * consecutive offline transactions the issuer accepts before it would
	 * rather the transaction went online.<br>
	 * Compare it with {@link #getConsecutiveOfflineTransactions()} to know
	 * where the card stands; deciding anything from that comparison is terminal
	 * risk management and is out of the scope of this library.
	 *
	 * @return the limit or {@link #UNKNOWN}
	 */
	public int getLowerConsecutiveOfflineLimit() {
		return lowerConsecutiveOfflineLimit;
	}

	/**
	 * Setter for the field lowerConsecutiveOfflineLimit
	 *
	 * @param lowerConsecutiveOfflineLimit
	 *            the limit to set
	 */
	public void setLowerConsecutiveOfflineLimit(final int lowerConsecutiveOfflineLimit) {
		this.lowerConsecutiveOfflineLimit = lowerConsecutiveOfflineLimit;
	}

	/**
	 * Get the Upper Consecutive Offline Limit (tag '9F23'), the number of
	 * consecutive offline transactions beyond which the issuer wants the
	 * transaction declined when it cannot go online.
	 *
	 * @return the limit or {@link #UNKNOWN}
	 */
	public int getUpperConsecutiveOfflineLimit() {
		return upperConsecutiveOfflineLimit;
	}

	/**
	 * Setter for the field upperConsecutiveOfflineLimit
	 *
	 * @param upperConsecutiveOfflineLimit
	 *            the limit to set
	 */
	public void setUpperConsecutiveOfflineLimit(final int upperConsecutiveOfflineLimit) {
		this.upperConsecutiveOfflineLimit = upperConsecutiveOfflineLimit;
	}

	/**
	 * Get the Application Reference Currency (tag '9F3B'), the currencies the
	 * application may be displayed in on top of its own.
	 *
	 * @return the currencies, never null
	 */
	public List<CurrencyEnum> getReferenceCurrencies() {
		return Collections.unmodifiableList(referenceCurrencies);
	}

	/**
	 * Setter for the field referenceCurrencies
	 *
	 * @param referenceCurrencies
	 *            the currencies to set
	 */
	public void setReferenceCurrencies(final List<CurrencyEnum> referenceCurrencies) {
		this.referenceCurrencies = referenceCurrencies != null ? new ArrayList<CurrencyEnum>(referenceCurrencies)
				: new ArrayList<CurrencyEnum>();
	}

	/**
	 * Get the Application Reference Currency Exponent (tag '9F43'), paired
	 * positionally with {@link #getReferenceCurrencies()}.
	 *
	 * @return the exponents, never null
	 */
	public List<Integer> getReferenceCurrencyExponents() {
		return Collections.unmodifiableList(referenceCurrencyExponents);
	}

	/**
	 * Setter for the field referenceCurrencyExponents
	 *
	 * @param referenceCurrencyExponents
	 *            the exponents to set
	 */
	public void setReferenceCurrencyExponents(final List<Integer> referenceCurrencyExponents) {
		this.referenceCurrencyExponents = referenceCurrencyExponents != null
				? new ArrayList<Integer>(referenceCurrencyExponents) : new ArrayList<Integer>();
	}

	/**
	 * Get the Application Discretionary Data (tag '9F05'), exposed raw: the
	 * specification assigns it no structure.
	 *
	 * @return the raw value or null
	 */
	public byte[] getApplicationDiscretionaryData() {
		return applicationDiscretionaryData;
	}

	/**
	 * Setter for the field applicationDiscretionaryData
	 *
	 * @param applicationDiscretionaryData
	 *            the value to set
	 */
	public void setApplicationDiscretionaryData(final byte[] applicationDiscretionaryData) {
		this.applicationDiscretionaryData = applicationDiscretionaryData;
	}

	/**
	 * Get the Card Risk Management Data Object List 1 (tag '8C'), the data the
	 * card would demand of a terminal asking for a cryptogram.
	 * <p>
	 * Reading the list is reading card personalisation. <b>Executing</b> it
	 * means sending a GENERATE AC, which this library never does.
	 * </p>
	 *
	 * @return the list, never null
	 */
	public List<TagAndLength> getCdol1() {
		return Collections.unmodifiableList(cdol1 != null ? cdol1 : new ArrayList<TagAndLength>());
	}

	/**
	 * Setter for the field cdol1
	 *
	 * @param cdol1
	 *            the list to set
	 */
	public void setCdol1(final List<TagAndLength> cdol1) {
		this.cdol1 = cdol1 != null ? new ArrayList<TagAndLength>(cdol1) : new ArrayList<TagAndLength>();
	}

	/**
	 * Get the Card Risk Management Data Object List 2 (tag '8D')
	 *
	 * @return the list, never null
	 */
	public List<TagAndLength> getCdol2() {
		return Collections.unmodifiableList(cdol2 != null ? cdol2 : new ArrayList<TagAndLength>());
	}

	/**
	 * Setter for the field cdol2
	 *
	 * @param cdol2
	 *            the list to set
	 */
	public void setCdol2(final List<TagAndLength> cdol2) {
		this.cdol2 = cdol2 != null ? new ArrayList<TagAndLength>(cdol2) : new ArrayList<TagAndLength>();
	}

	/**
	 * Get the Dynamic Data Authentication Data Object List (tag '9F49'), the
	 * data the card would demand of an INTERNAL AUTHENTICATE this library never
	 * sends.
	 *
	 * @return the list, never null
	 */
	public List<TagAndLength> getDdol() {
		return Collections.unmodifiableList(ddol != null ? ddol : new ArrayList<TagAndLength>());
	}

	/**
	 * Setter for the field ddol
	 *
	 * @param ddol
	 *            the list to set
	 */
	public void setDdol(final List<TagAndLength> ddol) {
		this.ddol = ddol != null ? new ArrayList<TagAndLength>(ddol) : new ArrayList<TagAndLength>();
	}

	/**
	 * Get the Transaction Certificate Data Object List (tag '97')
	 *
	 * @return the list, never null
	 */
	public List<TagAndLength> getTdol() {
		return Collections.unmodifiableList(tdol != null ? tdol : new ArrayList<TagAndLength>());
	}

	/**
	 * Setter for the field tdol
	 *
	 * @param tdol
	 *            the list to set
	 */
	public void setTdol(final List<TagAndLength> tdol) {
		this.tdol = tdol != null ? new ArrayList<TagAndLength>(tdol) : new ArrayList<TagAndLength>();
	}

	/**
	 * Method used to know whether the Dedicated File Name (tag '84') the card
	 * returned in its File Control Information matches the AID that was
	 * selected, as EMV 4.3 Book 1 section 12.4 requires.<br>
	 * A mismatch is reported and never acted upon: a reader tells what it saw.
	 *
	 * @return true when the card answered a name that does not match
	 */
	public boolean isDedicatedFileNameMismatch() {
		return dedicatedFileNameMismatch;
	}

	/**
	 * Setter for the field dedicatedFileNameMismatch
	 *
	 * @param dedicatedFileNameMismatch
	 *            the value to set
	 */
	public void setDedicatedFileNameMismatch(final boolean dedicatedFileNameMismatch) {
		this.dedicatedFileNameMismatch = dedicatedFileNameMismatch;
	}

	/**
	 * Get the Last Online ATC Register (tag '9F13')
	 *
	 * @return the lastOnlineTransactionCounter
	 */
	public int getLastOnlineTransactionCounter() {
		return lastOnlineTransactionCounter;
	}

	/**
	 * Setter for the field lastOnlineTransactionCounter
	 *
	 * @param lastOnlineTransactionCounter
	 *            the lastOnlineTransactionCounter to set
	 */
	public void setLastOnlineTransactionCounter(final int lastOnlineTransactionCounter) {
		this.lastOnlineTransactionCounter = lastOnlineTransactionCounter;
	}

	/**
	 * Get the offline (electronic cash) balance available on the card
	 *
	 * @return the offlineBalance or null when the card does not provide it
	 */
	public BigDecimal getOfflineBalance() {
		return offlineBalance;
	}

	/**
	 * Setter for the field offlineBalance
	 *
	 * @param offlineBalance
	 *            the offlineBalance to set
	 */
	public void setOfflineBalance(final BigDecimal offlineBalance) {
		this.offlineBalance = offlineBalance;
	}

	/**
	 * Get the Payment Account Reference (tag '9F24')
	 *
	 * @return the paymentAccountReference
	 */
	public String getPaymentAccountReference() {
		return paymentAccountReference;
	}

	/**
	 * Setter for the field paymentAccountReference
	 *
	 * @param paymentAccountReference
	 *            the paymentAccountReference to set
	 */
	public void setPaymentAccountReference(final String paymentAccountReference) {
		this.paymentAccountReference = paymentAccountReference;
	}

	/**
	 * Get the Token Requestor ID (tag '9F19')
	 *
	 * @return the tokenRequestorId
	 */
	public String getTokenRequestorId() {
		return tokenRequestorId;
	}

	/**
	 * Setter for the field tokenRequestorId
	 *
	 * @param tokenRequestorId
	 *            the tokenRequestorId to set
	 */
	public void setTokenRequestorId(final String tokenRequestorId) {
		this.tokenRequestorId = tokenRequestorId;
	}

	/**
	 * Get the last 4 digits of the funding PAN (tag '9F25')
	 *
	 * @return the last4DigitsOfPan
	 */
	public String getLast4DigitsOfPan() {
		return last4DigitsOfPan;
	}

	/**
	 * Setter for the field last4DigitsOfPan
	 *
	 * @param last4DigitsOfPan
	 *            the last4DigitsOfPan to set
	 */
	public void setLast4DigitsOfPan(final String last4DigitsOfPan) {
		this.last4DigitsOfPan = last4DigitsOfPan;
	}

	/**
	 * Method used to know if the application holds a tokenized credential, as
	 * personalized by a mobile wallet, instead of the PAN of a plastic card.
	 *
	 * @return true if the application exposes a token requestor
	 */
	public boolean isTokenized() {
		return tokenRequestorId != null;
	}

	/**
	 * Method used to know the number of offline transactions performed since
	 * the last online transaction.
	 *
	 * @return the number of consecutive offline transactions or
	 *         {@link #UNKNOWN} if the card did not provide the required
	 *         counters
	 */
	public int getConsecutiveOfflineTransactions() {
		if (transactionCounter == UNKNOWN || lastOnlineTransactionCounter == UNKNOWN
				|| lastOnlineTransactionCounter > transactionCounter) {
			return UNKNOWN;
		}
		return transactionCounter - lastOnlineTransactionCounter;
	}

	/**
	 * True when {@link #getLanguagePreferences()} was supplied by the
	 * directory FCI (Language Preference, tag '5F2D', at directory level)
	 * because the application FCI had none
	 *
	 * @return true when the language preferences come from the directory
	 */
	public boolean isLanguagePreferencesFromDirectory() {
		return languagePreferencesFromDirectory;
	}

	/**
	 * Setter for the field languagePreferencesFromDirectory
	 *
	 * @param languagePreferencesFromDirectory
	 *            the languagePreferencesFromDirectory to set
	 */
	public void setLanguagePreferencesFromDirectory(final boolean languagePreferencesFromDirectory) {
		this.languagePreferencesFromDirectory = languagePreferencesFromDirectory;
	}

	/**
	 * True when {@link #getIssuerCodeTableIndex()} was supplied by the
	 * directory FCI (Issuer Code Table Index, tag '9F11', at directory
	 * level)
	 *
	 * @return true when the issuer code table index comes from the directory
	 */
	public boolean isIssuerCodeTableIndexFromDirectory() {
		return issuerCodeTableIndexFromDirectory;
	}

	/**
	 * Setter for the field issuerCodeTableIndexFromDirectory
	 *
	 * @param issuerCodeTableIndexFromDirectory
	 *            the issuerCodeTableIndexFromDirectory to set
	 */
	public void setIssuerCodeTableIndexFromDirectory(final boolean issuerCodeTableIndexFromDirectory) {
		this.issuerCodeTableIndexFromDirectory = issuerCodeTableIndexFromDirectory;
	}

	/**
	 * Status bytes SW1 SW2 of the (last) SELECT of this application, four
	 * upper-case hexadecimal characters: '9000', '6283' application
	 * blocked, '6A81' function not supported, '6285', '6985'... Null when
	 * the application was never selected.
	 *
	 * @return the status word of the SELECT, or null
	 */
	public String getSelectStatusWord() {
		return selectStatusWord;
	}

	/**
	 * Setter for the field selectStatusWord
	 *
	 * @param selectStatusWord
	 *            the selectStatusWord to set
	 */
	public void setSelectStatusWord(final String selectStatusWord) {
		this.selectStatusWord = selectStatusWord;
	}

	/**
	 * AID the selection was asked with: the directory ADF Name (tag '4F')
	 * or the scheme AID/RID of the lookup by AID. Unlike {@link #getAid()}
	 * it is never replaced by the DF Name (tag '84'). Null when the
	 * application was never selected.
	 *
	 * @return the AID the SELECT was sent with, or null
	 */
	public byte[] getRequestedAid() {
		return requestedAid;
	}

	/**
	 * Setter for the field requestedAid
	 *
	 * @param requestedAid
	 *            the requestedAid to set
	 */
	public void setRequestedAid(final byte[] requestedAid) {
		this.requestedAid = requestedAid;
	}

	/**
	 * Dedicated File Name (tag '84') exactly as answered in the FCI of the
	 * application, null when the card returned none ({@link #getAid()} then
	 * keeps the requested AID)
	 *
	 * @return the DF Name as answered, or null
	 */
	public byte[] getDedicatedFileName() {
		return dedicatedFileName;
	}

	/**
	 * Setter for the field dedicatedFileName
	 *
	 * @param dedicatedFileName
	 *            the dedicatedFileName to set
	 */
	public void setDedicatedFileName(final byte[] dedicatedFileName) {
		this.dedicatedFileName = dedicatedFileName;
	}

	/**
	 * Scheme named by the AID of this application (registered application
	 * provider identifier, EMV 4.4 Book 1 section 12.2.1), before the CB
	 * scheme is replaced by the scheme of the PAN at card level. Null when
	 * the AID names no known scheme.
	 *
	 * @return the scheme named by the AID, or null
	 */
	public EmvCardScheme getAidScheme() {
		return aidScheme;
	}

	/**
	 * Setter for the field aidScheme
	 *
	 * @param aidScheme
	 *            the aidScheme to set
	 */
	public void setAidScheme(final EmvCardScheme aidScheme) {
		this.aidScheme = aidScheme;
	}

	/**
	 * Data field of the SELECT response of the application, the File
	 * Control Information template (tag '6F', EMV 4.4 Book 1 section
	 * 11.3.4), status bytes removed. Null when the SELECT failed or was
	 * never sent.
	 *
	 * @return the raw FCI, or null
	 */
	public byte[] getRawFci() {
		return rawFci;
	}

	/**
	 * Setter for the field rawFci
	 *
	 * @param rawFci
	 *            the rawFci to set
	 */
	public void setRawFci(final byte[] rawFci) {
		this.rawFci = rawFci;
	}

	/**
	 * Processing Options Data Object List (tag '9F38') raw value, null when
	 * the FCI carries none
	 *
	 * @return the raw PDOL, or null
	 */
	public byte[] getRawPdol() {
		return rawPdol;
	}

	/**
	 * Setter for the field rawPdol
	 *
	 * @param rawPdol
	 *            the rawPdol to set
	 */
	public void setRawPdol(final byte[] rawPdol) {
		this.rawPdol = rawPdol;
	}

	/**
	 * Get the Processing Options Data Object List (tag '9F38') parsed into
	 * tags and lengths, the data the card asked the terminal for before the
	 * GET PROCESSING OPTIONS
	 *
	 * @return the list, never null, unmodifiable
	 */
	public List<TagAndLength> getPdol() {
		return Collections.unmodifiableList(pdol != null ? pdol : new ArrayList<TagAndLength>());
	}

	/**
	 * Setter for the field pdol
	 *
	 * @param pdol
	 *            the pdol to set
	 */
	public void setPdol(final List<TagAndLength> pdol) {
		this.pdol = pdol != null ? new ArrayList<TagAndLength>(pdol) : new ArrayList<TagAndLength>();
	}

	/**
	 * Data field sent in the GET PROCESSING OPTIONS command (Command
	 * Template, tag '83', followed by the terminal values of the PDOL), the
	 * one whose response was parsed. Null when no GPO was sent.
	 *
	 * @return the GPO data field, or null
	 */
	public byte[] getGpoCommandTemplate() {
		return gpoCommandTemplate;
	}

	/**
	 * Setter for the field gpoCommandTemplate
	 *
	 * @param gpoCommandTemplate
	 *            the gpoCommandTemplate to set
	 */
	public void setGpoCommandTemplate(final byte[] gpoCommandTemplate) {
		this.gpoCommandTemplate = gpoCommandTemplate;
	}

	/**
	 * Terminal data sent for each PDOL (tag '9F38') entry, keyed by
	 * upper-case hexadecimal tag ('9F66' Terminal Transaction Qualifiers,
	 * '9F1A', '5F2A', '9A', '9C', '9F02', '9F37'...), in PDOL order. Never
	 * null.
	 *
	 * @return the terminal values, never null
	 */
	public Map<String, byte[]> getPdolTerminalValues() {
		return pdolTerminalValues;
	}

	/**
	 * Setter for the field pdolTerminalValues
	 *
	 * @param pdolTerminalValues
	 *            the pdolTerminalValues to set, null resets to an empty map
	 */
	public void setPdolTerminalValues(final Map<String, byte[]> pdolTerminalValues) {
		this.pdolTerminalValues = pdolTerminalValues != null ? pdolTerminalValues : new LinkedHashMap<String, byte[]>();
	}

	/**
	 * Status bytes SW1 SW2 of the last GET PROCESSING OPTIONS (or of the
	 * READ RECORD fallback when used), four upper-case hexadecimal
	 * characters. Null when none was sent.
	 *
	 * @return the status word of the GPO, or null
	 */
	public String getGpoStatusWord() {
		return gpoStatusWord;
	}

	/**
	 * Setter for the field gpoStatusWord
	 *
	 * @param gpoStatusWord
	 *            the gpoStatusWord to set
	 */
	public void setGpoStatusWord(final String gpoStatusWord) {
		this.gpoStatusWord = gpoStatusWord;
	}

	/**
	 * True when the GET PROCESSING OPTIONS built from the PDOL (tag '9F38')
	 * was refused and resent with an empty Command Template (tag '83')
	 *
	 * @return true when the GPO was resent without the PDOL values
	 */
	public boolean isGpoRetriedWithoutPdol() {
		return gpoRetriedWithoutPdol;
	}

	/**
	 * Setter for the field gpoRetriedWithoutPdol
	 *
	 * @param gpoRetriedWithoutPdol
	 *            the gpoRetriedWithoutPdol to set
	 */
	public void setGpoRetriedWithoutPdol(final boolean gpoRetriedWithoutPdol) {
		this.gpoRetriedWithoutPdol = gpoRetriedWithoutPdol;
	}

	/**
	 * True when no GET PROCESSING OPTIONS succeeded and the reading fell
	 * back on READ RECORD 1 of SFI 1
	 *
	 * @return true when the GPO was replaced by a READ RECORD
	 */
	public boolean isGpoReplacedByReadRecord() {
		return gpoReplacedByReadRecord;
	}

	/**
	 * Setter for the field gpoReplacedByReadRecord
	 *
	 * @param gpoReplacedByReadRecord
	 *            the gpoReplacedByReadRecord to set
	 */
	public void setGpoReplacedByReadRecord(final boolean gpoReplacedByReadRecord) {
		this.gpoReplacedByReadRecord = gpoReplacedByReadRecord;
	}

	/**
	 * Data field of the response that was parsed as the GET PROCESSING
	 * OPTIONS response (Response Message Template Format 1, tag '80', or
	 * Format 2, tag '77', or the fallback record), status bytes removed.
	 * Null when none.
	 *
	 * @return the raw GPO response, or null
	 */
	public byte[] getRawGpoResponse() {
		return rawGpoResponse;
	}

	/**
	 * Setter for the field rawGpoResponse
	 *
	 * @param rawGpoResponse
	 *            the rawGpoResponse to set
	 */
	public void setRawGpoResponse(final byte[] rawGpoResponse) {
		this.rawGpoResponse = rawGpoResponse;
	}

	/**
	 * Application File Locator bytes: value of the tag '94' (Format 2) or
	 * bytes 3 and following of the Response Message Template Format 1 (tag
	 * '80'). Null when none.
	 *
	 * @return the raw AFL, or null
	 */
	public byte[] getRawAfl() {
		return rawAfl;
	}

	/**
	 * Setter for the field rawAfl
	 *
	 * @param rawAfl
	 *            the rawAfl to set
	 */
	public void setRawAfl(final byte[] rawAfl) {
		this.rawAfl = rawAfl;
	}

	/**
	 * Every four byte entry of the Application File Locator (tag '94', EMV
	 * 4.4 Book 3 section 10.2) in card order, invalid entries included
	 * ({@link Afl#isValid()} tells). Never null.
	 *
	 * @return the AFL entries, never null
	 */
	public List<Afl> getAfl() {
		return afl;
	}

	/**
	 * Setter for the field afl
	 *
	 * @param afl
	 *            the afl to set, null resets to an empty list
	 */
	public void setAfl(final List<Afl> afl) {
		this.afl = afl != null ? afl : new ArrayList<Afl>();
	}

	/**
	 * Every READ RECORD sent for the Application File Locator (or for the
	 * GPO fallback record), in order, with raw data field and status word;
	 * a record answered with an error has an empty raw data. Never null.
	 *
	 * @return the records read, never null
	 */
	public List<RecordData> getRecords() {
		return records;
	}

	/**
	 * Setter for the field records
	 *
	 * @param records
	 *            the records to set, null resets to an empty list
	 */
	public void setRecords(final List<RecordData> records) {
		this.records = records != null ? records : new ArrayList<RecordData>();
	}

	/**
	 * Number of READ RECORD commands sent for the Application File Locator,
	 * 0 when none
	 *
	 * @return the number of READ RECORD sent
	 */
	public int getRecordsRead() {
		return recordsRead;
	}

	/**
	 * Setter for the field recordsRead
	 *
	 * @param recordsRead
	 *            the recordsRead to set
	 */
	public void setRecordsRead(final int recordsRead) {
		this.recordsRead = recordsRead;
	}

	/**
	 * True when the maximum number of records stopped the reading before
	 * the end of the Application File Locator
	 *
	 * @return true when the AFL reading was truncated
	 */
	public boolean isAflTruncated() {
		return aflTruncated;
	}

	/**
	 * Setter for the field aflTruncated
	 *
	 * @param aflTruncated
	 *            the aflTruncated to set
	 */
	public void setAflTruncated(final boolean aflTruncated) {
		this.aflTruncated = aflTruncated;
	}

	/**
	 * Identification computed at the end of the reading for this
	 * application (AID, PAN, BIC, IBAN, issuer country...). Null before a
	 * read.
	 *
	 * @return the identification of the application, or null before a read
	 */
	public CardIdentification getIdentification() {
		return identification;
	}

	/**
	 * Setter for the field identification
	 *
	 * @param identification
	 *            the identification to set
	 */
	public void setIdentification(final CardIdentification identification) {
		this.identification = identification;
	}

	/**
	 * Type of kernel, bits 8 and 7 of byte 1 of the Kernel Identifier (tag
	 * '9F2A', EMV Contactless Book B Table 3-4), null when no identifier
	 * was read. Set by {@link #setKernelIdentifier(byte[])}.
	 *
	 * @return the kernel type, or null
	 */
	public KernelTypeEnum getKernelType() {
		return kernelType;
	}

	/**
	 * Setter for the field kernelType
	 *
	 * @param kernelType
	 *            the kernelType to set
	 */
	public void setKernelType(final KernelTypeEnum kernelType) {
		this.kernelType = kernelType;
	}

	/**
	 * Requested Kernel ID of EMV Contactless Book B section 3.3.2, derived
	 * from the Kernel Identifier (tag '9F2A'): byte 1 for an international
	 * or RFU type, bytes 1 to 3 for a domestic type with a non-zero Short
	 * Kernel ID, null otherwise. Set by {@link
	 * #setKernelIdentifier(byte[])}.
	 *
	 * @return the Requested Kernel ID, or null
	 */
	public byte[] getRequestedKernelId() {
		return requestedKernelId;
	}

	/**
	 * Setter for the field requestedKernelId
	 *
	 * @param requestedKernelId
	 *            the requestedKernelId to set
	 */
	public void setRequestedKernelId(final byte[] requestedKernelId) {
		this.requestedKernelId = requestedKernelId;
	}

	/**
	 * Bits 6 to 1 of byte 1 of the Kernel Identifier (tag '9F2A') as read,
	 * whatever the kernel type ({@link #getShortKernelId()} stays {@link
	 * AbstractData#UNKNOWN} for a domestic or RFU identifier). {@link
	 * AbstractData#UNKNOWN} when no identifier was read. Set by {@link
	 * #setKernelIdentifier(byte[])}.
	 *
	 * @return the Short Kernel ID as read, or {@link AbstractData#UNKNOWN}
	 */
	public int getRawShortKernelId() {
		return rawShortKernelId;
	}

	/**
	 * Setter for the field rawShortKernelId
	 *
	 * @param rawShortKernelId
	 *            the rawShortKernelId to set
	 */
	public void setRawShortKernelId(final int rawShortKernelId) {
		this.rawShortKernelId = rawShortKernelId;
	}

	/**
	 * Every primitive BER-TLV data object found, recursively through the
	 * constructed templates ('6F', 'A5', 'BF0C', '77', '70', '73'...), in
	 * the FCI, the GET PROCESSING OPTIONS response and the records of this
	 * application, keyed by upper-case hexadecimal tag without spaces
	 * ('5A', '9F26', 'DF64'), the first occurrence kept, in reading order.
	 * Never null, the live map is returned.
	 *
	 * @return the raw data objects, never null
	 */
	public Map<String, byte[]> getRawDataObjects() {
		return rawDataObjects;
	}

	/**
	 * Setter for the field rawDataObjects
	 *
	 * @param rawDataObjects
	 *            the rawDataObjects to set, null resets to an empty map
	 */
	public void setRawDataObjects(final Map<String, byte[]> rawDataObjects) {
		this.rawDataObjects = rawDataObjects != null ? rawDataObjects : new LinkedHashMap<String, byte[]>();
	}

	/**
	 * Details of the reading of the transaction log of this application
	 * (Log Entry, tag '9F4D' or 'DF60', and Log Format, tag '9F4F'), null
	 * when the log was never looked for
	 *
	 * @return the transaction log details, or null
	 */
	public TransactionLog getTransactionLog() {
		return transactionLog;
	}

	/**
	 * Setter for the field transactionLog
	 *
	 * @param transactionLog
	 *            the transactionLog to set
	 */
	public void setTransactionLog(final TransactionLog transactionLog) {
		this.transactionLog = transactionLog;
	}

	/**
	 * For every GET DATA sent for this application: upper-case hexadecimal
	 * tag ('9F17', '9F36', '9F13', '9F50', '9F79', '9F4F', '9F70'...)
	 * mapped to the status bytes SW1 SW2 (four upper-case hexadecimal
	 * characters, e.g. '9000', '6A88', '6985', '6D00'). Never null.
	 *
	 * @return the status words of the GET DATA commands, never null
	 */
	public Map<String, String> getGetDataStatusWords() {
		return getDataStatusWords;
	}

	/**
	 * Setter for the field getDataStatusWords
	 *
	 * @param getDataStatusWords
	 *            the getDataStatusWords to set, null resets to an empty map
	 */
	public void setGetDataStatusWords(final Map<String, String> getDataStatusWords) {
		this.getDataStatusWords = getDataStatusWords != null ? getDataStatusWords : new LinkedHashMap<String, String>();
	}

	/**
	 * Raw value answered for the offline balance tag ('9F79' or '9F50'),
	 * kept even when it is not the six byte n12 encoding ({@link
	 * #getOfflineBalance()} then stays null). Null when neither tag
	 * answered a value.
	 *
	 * @return the raw offline balance, or null
	 */
	public byte[] getRawOfflineBalance() {
		return rawOfflineBalance;
	}

	/**
	 * Setter for the field rawOfflineBalance
	 *
	 * @param rawOfflineBalance
	 *            the rawOfflineBalance to set
	 */
	public void setRawOfflineBalance(final byte[] rawOfflineBalance) {
		this.rawOfflineBalance = rawOfflineBalance;
	}

	/**
	 * Tag the balance value was read from: '9F79' (Electronic Cash Balance,
	 * UnionPay) or '9F50' (Offline Accumulator Balance, M/Chip). Null when
	 * neither answered a value.
	 *
	 * @return '9F79', '9F50', or null
	 */
	public String getOfflineBalanceTag() {
		return offlineBalanceTag;
	}

	/**
	 * Setter for the field offlineBalanceTag
	 *
	 * @param offlineBalanceTag
	 *            the offlineBalanceTag to set
	 */
	public void setOfflineBalanceTag(final String offlineBalanceTag) {
		this.offlineBalanceTag = offlineBalanceTag;
	}

	/**
	 * Application Currency Code (tag '9F42') numeric ISO 4217 value as
	 * sent, even when the currency enumeration has no value for it ({@link
	 * #getCurrency()} is then null). {@link AbstractData#UNKNOWN} when
	 * absent.
	 *
	 * @return the numeric currency code, or {@link AbstractData#UNKNOWN}
	 */
	public int getCurrencyCode() {
		return currencyCode;
	}

	/**
	 * Setter for the field currencyCode
	 *
	 * @param currencyCode
	 *            the currencyCode to set
	 */
	public void setCurrencyCode(final int currencyCode) {
		this.currencyCode = currencyCode;
	}

	/**
	 * Issuer Country Code (tag '5F28') numeric ISO 3166 value as sent, even
	 * when the country enumeration has no value for it. {@link
	 * AbstractData#UNKNOWN} when absent.
	 *
	 * @return the numeric issuer country code, or {@link AbstractData#UNKNOWN}
	 */
	public int getIssuerCountryCodeNumeric() {
		return issuerCountryCodeNumeric;
	}

	/**
	 * Setter for the field issuerCountryCodeNumeric
	 *
	 * @param issuerCountryCodeNumeric
	 *            the issuerCountryCodeNumeric to set
	 */
	public void setIssuerCountryCodeNumeric(final int issuerCountryCodeNumeric) {
		this.issuerCountryCodeNumeric = issuerCountryCodeNumeric;
	}

	/**
	 * Issuer Country Code (alpha3 format) (tag '5F56') as text, null when
	 * absent
	 *
	 * @return the alpha-3 issuer country code, or null
	 */
	public String getIssuerCountryCodeAlpha3() {
		return issuerCountryCodeAlpha3;
	}

	/**
	 * Setter for the field issuerCountryCodeAlpha3
	 *
	 * @param issuerCountryCodeAlpha3
	 *            the issuerCountryCodeAlpha3 to set
	 */
	public void setIssuerCountryCodeAlpha3(final String issuerCountryCodeAlpha3) {
		this.issuerCountryCodeAlpha3 = issuerCountryCodeAlpha3;
	}

	/**
	 * Issuer Country Code (alpha2 format) (tag '5F55') as text, null when
	 * absent
	 *
	 * @return the alpha-2 issuer country code, or null
	 */
	public String getIssuerCountryCodeAlpha2() {
		return issuerCountryCodeAlpha2;
	}

	/**
	 * Setter for the field issuerCountryCodeAlpha2
	 *
	 * @param issuerCountryCodeAlpha2
	 *            the issuerCountryCodeAlpha2 to set
	 */
	public void setIssuerCountryCodeAlpha2(final String issuerCountryCodeAlpha2) {
		this.issuerCountryCodeAlpha2 = issuerCountryCodeAlpha2;
	}

	/**
	 * Every two byte code of the Application Reference Currency (tag
	 * '9F3B') in card order, zero and unknown codes included ({@link
	 * #getReferenceCurrencies()} keeps only the known ones). Never null.
	 *
	 * @return the reference currency codes, never null
	 */
	public List<Integer> getReferenceCurrencyCodes() {
		return referenceCurrencyCodes;
	}

	/**
	 * Setter for the field referenceCurrencyCodes
	 *
	 * @param referenceCurrencyCodes
	 *            the referenceCurrencyCodes to set, null resets to an empty list
	 */
	public void setReferenceCurrencyCodes(final List<Integer> referenceCurrencyCodes) {
		this.referenceCurrencyCodes = referenceCurrencyCodes != null ? referenceCurrencyCodes : new ArrayList<Integer>();
	}

	/**
	 * The three digits of the Service Code (tag '5F30') as sent ({@link
	 * #getServiceCode()} decodes them and loses any digit the enumerations
	 * do not know), null when absent
	 *
	 * @return the service code digits, or null
	 */
	public String getServiceCodeDigits() {
		return serviceCodeDigits;
	}

	/**
	 * Setter for the field serviceCodeDigits
	 *
	 * @param serviceCodeDigits
	 *            the serviceCodeDigits to set
	 */
	public void setServiceCodeDigits(final String serviceCodeDigits) {
		this.serviceCodeDigits = serviceCodeDigits;
	}

	/**
	 * Application Label (tag '50') of the application FCI decoded with the
	 * common character set, kept apart from {@link #getApplicationLabel()}
	 * which prefers the Application Preferred Name (tag '9F12'). Null when
	 * absent.
	 *
	 * @return the Application Label of the FCI, or null
	 */
	public String getFciApplicationLabel() {
		return fciApplicationLabel;
	}

	/**
	 * Setter for the field fciApplicationLabel
	 *
	 * @param fciApplicationLabel
	 *            the fciApplicationLabel to set
	 */
	public void setFciApplicationLabel(final String fciApplicationLabel) {
		this.fciApplicationLabel = fciApplicationLabel;
	}

	/**
	 * Bank Identifier Code (tag '5F54') read in the responses of this
	 * application, first found; the card level value keeps its last-wins
	 * rule. Null when absent.
	 *
	 * @return the BIC, or null
	 */
	public String getBic() {
		return bic;
	}

	/**
	 * Setter for the field bic
	 *
	 * @param bic
	 *            the bic to set
	 */
	public void setBic(final String bic) {
		this.bic = bic;
	}

	/**
	 * International Bank Account Number (tag '5F53') read in the responses
	 * of this application, first found. Null when absent.
	 *
	 * @return the IBAN, or null
	 */
	public String getIban() {
		return iban;
	}

	/**
	 * Setter for the field iban
	 *
	 * @param iban
	 *            the iban to set
	 */
	public void setIban(final String iban) {
		this.iban = iban;
	}

	/**
	 * Cardholder Name (tag '5F20') of this application, decoded and
	 * trimmed, undivided, first found; null when absent or empty after
	 * trimming
	 *
	 * @return the cardholder name, or null
	 */
	public String getCardholderName() {
		return cardholderName;
	}

	/**
	 * Setter for the field cardholderName
	 *
	 * @param cardholderName
	 *            the cardholderName to set
	 */
	public void setCardholderName(final String cardholderName) {
		this.cardholderName = cardholderName;
	}

	/**
	 * Cardholder Name Extended (tag '9F0B') of this application, decoded,
	 * first found. Null when absent.
	 *
	 * @return the extended cardholder name, or null
	 */
	public String getCardholderNameExtended() {
		return cardholderNameExtended;
	}

	/**
	 * Setter for the field cardholderNameExtended
	 *
	 * @param cardholderNameExtended
	 *            the cardholderNameExtended to set
	 */
	public void setCardholderNameExtended(final String cardholderNameExtended) {
		this.cardholderNameExtended = cardholderNameExtended;
	}

	/**
	 * Value of the tag '9F66' as answered by the card, any length: two
	 * bytes are the PUNATC(Track2) of Kernel 2, four bytes the Terminal
	 * Transaction Qualifiers echoed by a Kernel 3 card ({@link
	 * MagStripeData#getPunatcTrack2()} keeps its two byte rule). Null when
	 * absent.
	 *
	 * @return the value of the tag '9F66' as answered, or null
	 */
	public byte[] getTerminalTransactionQualifiersEcho() {
		return terminalTransactionQualifiersEcho;
	}

	/**
	 * Setter for the field terminalTransactionQualifiersEcho
	 *
	 * @param terminalTransactionQualifiersEcho
	 *            the terminalTransactionQualifiersEcho to set
	 */
	public void setTerminalTransactionQualifiersEcho(final byte[] terminalTransactionQualifiersEcho) {
		this.terminalTransactionQualifiersEcho = terminalTransactionQualifiersEcho;
	}

	/**
	 * Card Transaction Qualifiers (tag '9F6C') decoded, only set when the
	 * resolved kernel is Visa (Kernel 3) and the value is two bytes; {@link
	 * #getCardTransactionQualifiers()} stays raw for every kernel. Null
	 * otherwise.
	 *
	 * @return the decoded Card Transaction Qualifiers, or null
	 */
	public CardTransactionQualifiers getCardTransactionQualifiersDecoded() {
		return cardTransactionQualifiersDecoded;
	}

	/**
	 * Setter for the field cardTransactionQualifiersDecoded
	 *
	 * @param cardTransactionQualifiersDecoded
	 *            the cardTransactionQualifiersDecoded to set
	 */
	public void setCardTransactionQualifiersDecoded(final CardTransactionQualifiers cardTransactionQualifiersDecoded) {
		this.cardTransactionQualifiersDecoded = cardTransactionQualifiersDecoded;
	}

	/**
	 * Kernel used to interpret the payment system tags of this application:
	 * the one stated by the card (Kernel Identifier, tag '9F2A') or derived
	 * from the AID. Null when none.
	 *
	 * @return the resolved kernel, or null
	 */
	public KernelEnum getResolvedKernel() {
		return resolvedKernel;
	}

	/**
	 * Setter for the field resolvedKernel
	 *
	 * @param resolvedKernel
	 *            the resolvedKernel to set
	 */
	public void setResolvedKernel(final KernelEnum resolvedKernel) {
		this.resolvedKernel = resolvedKernel;
	}

	/**
	 * Card Risk Management Data Object List 1 (tag '8C') raw value, null
	 * when absent
	 *
	 * @return the raw CDOL1, or null
	 */
	public byte[] getRawCdol1() {
		return rawCdol1;
	}

	/**
	 * Setter for the field rawCdol1
	 *
	 * @param rawCdol1
	 *            the rawCdol1 to set
	 */
	public void setRawCdol1(final byte[] rawCdol1) {
		this.rawCdol1 = rawCdol1;
	}

	/**
	 * Card Risk Management Data Object List 2 (tag '8D') raw value, null
	 * when absent
	 *
	 * @return the raw CDOL2, or null
	 */
	public byte[] getRawCdol2() {
		return rawCdol2;
	}

	/**
	 * Setter for the field rawCdol2
	 *
	 * @param rawCdol2
	 *            the rawCdol2 to set
	 */
	public void setRawCdol2(final byte[] rawCdol2) {
		this.rawCdol2 = rawCdol2;
	}

	/**
	 * Dynamic Data Authentication Data Object List (tag '9F49') raw value,
	 * null when absent
	 *
	 * @return the raw DDOL, or null
	 */
	public byte[] getRawDdol() {
		return rawDdol;
	}

	/**
	 * Setter for the field rawDdol
	 *
	 * @param rawDdol
	 *            the rawDdol to set
	 */
	public void setRawDdol(final byte[] rawDdol) {
		this.rawDdol = rawDdol;
	}

	/**
	 * Transaction Certificate Data Object List (tag '97') raw value, null
	 * when absent
	 *
	 * @return the raw TDOL, or null
	 */
	public byte[] getRawTdol() {
		return rawTdol;
	}

	/**
	 * Setter for the field rawTdol
	 *
	 * @param rawTdol
	 *            the rawTdol to set
	 */
	public void setRawTdol(final byte[] rawTdol) {
		this.rawTdol = rawTdol;
	}

	/**
	 * Data field of the last SELECT sent for this application: the AID
	 * alone, or the AID followed by the Extended Selection (tag '9F29')
	 * when the reader appended it. Null when never selected.
	 *
	 * @return the data field of the SELECT, or null
	 */
	public byte[] getSelectedName() {
		return selectedName;
	}

	/**
	 * Setter for the field selectedName
	 *
	 * @param selectedName
	 *            the selectedName to set
	 */
	public void setSelectedName(final byte[] selectedName) {
		this.selectedName = selectedName;
	}

	/**
	 * True when the SELECT with the Extended Selection (tag '9F29') was
	 * refused and the reading retried with the ADF Name alone
	 *
	 * @return true when the extended selection was refused
	 */
	public boolean isExtendedSelectionRefused() {
		return extendedSelectionRefused;
	}

	/**
	 * Setter for the field extendedSelectionRefused
	 *
	 * @param extendedSelectionRefused
	 *            the extendedSelectionRefused to set
	 */
	public void setExtendedSelectionRefused(final boolean extendedSelectionRefused) {
		this.extendedSelectionRefused = extendedSelectionRefused;
	}

	/**
	 * True when the Extended Selection (tag '9F29') was not appended
	 * because AID and Extended Selection together exceeded 16 bytes (EMV
	 * Contactless Book B Table A-1)
	 *
	 * @return true when the extended selection was too long to be appended
	 */
	public boolean isExtendedSelectionTooLong() {
		return extendedSelectionTooLong;
	}

	/**
	 * Setter for the field extendedSelectionTooLong
	 *
	 * @param extendedSelectionTooLong
	 *            the extendedSelectionTooLong to set
	 */
	public void setExtendedSelectionTooLong(final boolean extendedSelectionTooLong) {
		this.extendedSelectionTooLong = extendedSelectionTooLong;
	}

	/**
	 * GeldKarte purse files (EF_ID, EF_BETRAG, EF_BLOG) of the application,
	 * null for a non GeldKarte application
	 *
	 * @return the GeldKarte data, or null
	 */
	public GeldKarteData getGeldKarte() {
		return geldKarte;
	}

	/**
	 * Setter for the field geldKarte
	 *
	 * @param geldKarte
	 *            the geldKarte to set
	 */
	public void setGeldKarte(final GeldKarteData geldKarte) {
		this.geldKarte = geldKarte;
	}

}
