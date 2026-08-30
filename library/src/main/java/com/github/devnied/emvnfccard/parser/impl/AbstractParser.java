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
package com.github.devnied.emvnfccard.parser.impl;

import com.github.devnied.emvnfccard.enums.CommandEnum;
import com.github.devnied.emvnfccard.enums.EmvCardScheme;
import com.github.devnied.emvnfccard.enums.SwEnum;
import com.github.devnied.emvnfccard.exception.CommunicationException;
import com.github.devnied.emvnfccard.iso7816emv.BerTlvInputStream;
import com.github.devnied.emvnfccard.iso7816emv.EmvTags;
import com.github.devnied.emvnfccard.iso7816emv.ITag;
import com.github.devnied.emvnfccard.iso7816emv.TLV;
import com.github.devnied.emvnfccard.iso7816emv.TagAndLength;
import com.github.devnied.emvnfccard.model.Application;
import com.github.devnied.emvnfccard.model.ApplicationInterchangeProfile;
import com.github.devnied.emvnfccard.model.ApplicationUsageControl;
import com.github.devnied.emvnfccard.model.CVM;
import com.github.devnied.emvnfccard.model.CardTransactionQualifiers;
import com.github.devnied.emvnfccard.model.DataEnvelopes;
import com.github.devnied.emvnfccard.model.EmvTransactionRecord;
import com.github.devnied.emvnfccard.model.MagStripeData;
import com.github.devnied.emvnfccard.model.OfflineDataAuthentication;
import com.github.devnied.emvnfccard.model.RecordData;
import com.github.devnied.emvnfccard.model.Service;
import com.github.devnied.emvnfccard.model.TransactionLog;
import com.github.devnied.emvnfccard.model.enums.KernelEnum;
import com.github.devnied.emvnfccard.model.enums.CountryCodeEnum;
import com.github.devnied.emvnfccard.model.enums.CurrencyEnum;
import com.github.devnied.emvnfccard.parser.EmvTemplate;
import com.github.devnied.emvnfccard.parser.IParser;
import com.github.devnied.emvnfccard.utils.CommandApdu;
import com.github.devnied.emvnfccard.utils.CvmUtils;
import com.github.devnied.emvnfccard.utils.EmvDataUtils;
import com.github.devnied.emvnfccard.utils.EnumUtils;
import com.github.devnied.emvnfccard.utils.ResponseUtils;
import com.github.devnied.emvnfccard.utils.TlvUtil;
import com.github.devnied.emvnfccard.utils.TrackUtils;
import fr.devnied.bitlib.BytesUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.ref.WeakReference;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Abstract Parser with provider attribute
 *
 * @author MILLAU Julien
 *
 */
public abstract class AbstractParser implements IParser {

	/**
	 * Class Logger
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(AbstractParser.class);

	/**
	 * Unknown response
	 */
	public static final int UNKNOW = -1;

	/**
	 * P2 of the SELECT command asking for the next occurrence of a partial AID
	 * (EMV 4.3 Book 1 section 11.3.2)
	 */
	public static final int SELECT_NEXT_OCCURRENCE = 0x02;

	/**
	 * Length of the Log Entry (tag '9F4D'): the SFI of the transaction log
	 * file followed by its number of records (EMV 4.3 Book 3 Annex D4)
	 */
	public static final int LOG_ENTRY_SIZE = 2;

	/**
	 * Highest Short File Identifier, it is coded on 5 bits (EMV 4.3 Book 3
	 * section 10.2)
	 */
	public static final int MAX_SFI = 30;

	/**
	 * Lowest Short File Identifier a transaction log may use. EMV 4.4 Book 3
	 * Annex D4: "The SFI shall be in the range 11 to 30".
	 */
	public static final int MIN_LOG_SFI = 11;

	/**
	 * Highest length of a Dedicated File Name, and of an ADF Name followed by
	 * its Extended Selection, in the data field of a SELECT command
	 */
	public static final int MAX_ADF_NAME_LENGTH = 16;

	/**
	 * Length of PUNATC(Track2) (tag '9F66'), 2 bytes for the 13 positions of
	 * the discretionary data field of Track 2 Data (EMV Contactless Book C-2
	 * v2.11 A.1.128 and section 4.7)
	 */
	public static final int PUNATC_TRACK2_LENGTH = 2;

	/**
	 * Length of the Card Transaction Qualifiers (tag '9F6C') of Kernel 3, 2
	 * bytes (EMV Contactless Book C-3 Annex A)
	 */
	public static final int CARD_TRANSACTION_QUALIFIERS_LENGTH = 2;

	/**
	 * Highest nesting of constructed data objects walked when the raw data
	 * objects of a response are collected, see
	 * {@link #collectRawDataObjects(byte[], Map)}
	 */
	public static final int MAX_RAW_DATA_OBJECT_DEPTH = 16;

	/**
	 * Amount from which the Amount, Authorised of a Visa transaction log record
	 * carries an artefact that has to be removed
	 */
	private static final long VISA_AMOUNT_ARTEFACT = 1500000000L;

	/**
	 * Length of the status bytes SW1 SW2 that end a response
	 */
	private static final int STATUS_WORD_LENGTH = 2;

	/**
	 * Card provider used
	 */
	protected final WeakReference<EmvTemplate> template;

	/**
	 * The same template, held strongly. {@link #template} is kept because it
	 * is protected, so parsers written outside of this library read it, but it
	 * could be cleared while the parser was still in use: nothing else held
	 * the template when the caller held only the parser. Every dereference
	 * inside this library goes through {@link #getTemplate()} instead.
	 */
	private final EmvTemplate strongTemplate;

	/**
	 * Default protected constructor
	 *
	 * @param pTemplate
	 *            Emv template
	 */
	protected AbstractParser(EmvTemplate pTemplate) {
		strongTemplate = pTemplate;
		template = new WeakReference<EmvTemplate>(pTemplate);
	}

	/**
	 * Get the template this parser was built with.
	 *
	 * @return the template, null only when the parser was built with null
	 */
	protected EmvTemplate getTemplate() {
		return strongTemplate;
	}

	/**
	 * Method used to read the status bytes SW1 SW2 that end a response of the
	 * card, as 4 upper-case hexadecimal characters ('9000', '6A82'...).
	 *
	 * @param pResponse
	 *            response of the card
	 * @return the status word or null when the response holds none (null or
	 *         shorter than 2 bytes)
	 */
	protected static String statusWordOf(final byte[] pResponse) {
		if (pResponse == null || pResponse.length < STATUS_WORD_LENGTH) {
			return null;
		}
		return BytesUtils.bytesToStringNoSpace(
				Arrays.copyOfRange(pResponse, pResponse.length - STATUS_WORD_LENGTH, pResponse.length))
				.toUpperCase(Locale.ENGLISH);
	}

	/**
	 * Method used to name a tag the way the raw data objects are keyed: its
	 * bytes in upper-case hexadecimal without space ('5A', '9F26', 'DF64').
	 *
	 * @param pTag
	 *            tag
	 * @return the hexadecimal name of the tag or null
	 */
	protected static String hexTag(final ITag pTag) {
		if (pTag == null || pTag.getTagBytes() == null) {
			return null;
		}
		return BytesUtils.bytesToStringNoSpace(pTag.getTagBytes()).toUpperCase(Locale.ENGLISH);
	}

	/**
	 * Method used to keep every primitive BER-TLV data object of a card
	 * response, known or unknown tag, keyed by its hexadecimal tag.
	 * <p>
	 * The constructed data objects (the templates '6F', 'A5', 'BF0C', '77',
	 * '70', '73'...) are walked through and never stored themselves; the first
	 * occurrence of a tag is kept, in reading order. The traversal is tolerant:
	 * it stops at the first object that cannot be read and never goes deeper
	 * than {@link #MAX_RAW_DATA_OBJECT_DEPTH} templates.
	 * </p>
	 *
	 * @param pData
	 *            raw card response (status bytes removed)
	 * @param pDataObjects
	 *            map to fill, see {@link Application#getRawDataObjects()}
	 */
	protected static void collectRawDataObjects(final byte[] pData, final Map<String, byte[]> pDataObjects) {
		collectRawDataObjects(pData, pDataObjects, 0);
	}

	/**
	 * Recursive part of {@link #collectRawDataObjects(byte[], Map)}
	 *
	 * @param pData
	 *            content of a response or of a template
	 * @param pDataObjects
	 *            map to fill
	 * @param pDepth
	 *            current nesting
	 */
	private static void collectRawDataObjects(final byte[] pData, final Map<String, byte[]> pDataObjects,
			final int pDepth) {
		if (pData == null || pData.length == 0 || pDataObjects == null) {
			return;
		}
		if (pDepth > MAX_RAW_DATA_OBJECT_DEPTH) {
			LOGGER.warn("Data objects nested deeper than " + MAX_RAW_DATA_OBJECT_DEPTH + ", the rest is not collected");
			return;
		}
		List<TLV> list;
		try {
			// One level only, the templates are walked below
			list = TlvUtil.getlistTLV(pData, (ITag) null, true);
		} catch (RuntimeException e) {
			// A response that is not BER-TLV at all carries no data object
			LOGGER.debug("Unreadable data objects: " + e.getMessage(), e);
			return;
		}
		for (TLV tlv : list) {
			if (tlv == null || tlv.getTag() == null) {
				break;
			}
			if (tlv.getTag().isConstructed()) {
				collectRawDataObjects(tlv.getValueBytes(), pDataObjects, pDepth + 1);
			} else {
				String key = hexTag(tlv.getTag());
				if (key != null && !pDataObjects.containsKey(key)) {
					pDataObjects.put(key, tlv.getValueBytes());
				}
			}
		}
	}

	/**
	 * Select application with AID or RID
	 *
	 * @param pAid
	 *            byte array containing AID or RID
	 * @return response byte array
	 * @throws CommunicationException communication error
	 */
	protected byte[] selectAID(final byte[] pAid) throws CommunicationException {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Select AID: " + BytesUtils.bytesToString(pAid));
		}
		return getTemplate().getProvider().transceive(new CommandApdu(CommandEnum.SELECT, pAid, 0).toBytes());
	}

	/**
	 * Select the application described by the parameter.
	 * <p>
	 * When the directory entry provides an Extended Selection (tag '9F29'), EMV
	 * Contactless Book B requires the terminal to append its value to the ADF
	 * Name in the data field of the SELECT command.
	 * </p>
	 *
	 * @param pApplication
	 *            application to select
	 * @return response byte array
	 * @throws CommunicationException communication error
	 */
	protected byte[] selectApplication(final Application pApplication) throws CommunicationException {
		byte[] aid = pApplication.getAid();
		byte[] extendedSelection = pApplication.getExtendedSelection();
		boolean extended = extendedSelection != null && extendedSelection.length > 0
				&& getTemplate().getConfig().extendedSelectionSupported;
		// The data field of a SELECT carries at most 16 bytes (EMV 4.3 Book 1
		// Table 40), and EMV Contactless Book B bounds the ADF Name plus the
		// Extended Selection by the same 16 bytes
		if (extended && aid != null && aid.length + extendedSelection.length > MAX_ADF_NAME_LENGTH) {
			LOGGER.warn("The Extended Selection would make the name longer than " + MAX_ADF_NAME_LENGTH
					+ " bytes, it is not appended");
			pApplication.setExtendedSelectionTooLong(true);
			extended = false;
		}
		if (extended) {
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Extended selection: " + BytesUtils.bytesToString(extendedSelection));
			}
			aid = ArrayUtils.addAll(aid, extendedSelection);
		}
		// The name actually sent to the card is kept on the application
		pApplication.setSelectedName(aid);
		if (!pApplication.isNextOccurrence()) {
			byte[] response = selectAID(aid);
			// EMV Contactless Book B only asks for the Extended Selection to be
			// appended when the reader supports it: a card that rejects the
			// concatenated name is still selectable with its bare ADF Name
			if (extended && !ResponseUtils.contains(response, SwEnum.SW_9000, SwEnum.SW_6285)
					&& !isApplicationBlocked(response)) {
				LOGGER.warn("Extended selection refused by the card, retry with the ADF Name alone");
				pApplication.setExtendedSelectionRefused(true);
				pApplication.setSelectedName(pApplication.getAid());
				response = selectAID(pApplication.getAid());
			}
			return response;
		}
		// EMV 4.3 Book 1 section 12.3.2: the applications matching a partial
		// AID are enumerated by repeating the SELECT with P2 = '02'
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Select next occurrence of AID: " + BytesUtils.bytesToString(aid));
		}
		return getTemplate().getProvider().transceive(
				new CommandApdu(CommandEnum.SELECT, CommandEnum.SELECT.getP1(), SELECT_NEXT_OCCURRENCE, aid, 0).toBytes());
	}

	/**
	 * Method used to know if the card answered that the application has been
	 * invalidated ('6283'), it has to be removed from the candidate list (EMV
	 * 4.3 Book 1 section 12.4).
	 *
	 * @param pResponse
	 *            response to the SELECT command
	 * @return true if the application is blocked
	 */
	protected boolean isApplicationBlocked(final byte[] pResponse) {
		return ResponseUtils.contains(pResponse, SwEnum.SW_6283);
	}

	/**
	 * Method used to know if the card answered a warning status ('62xx' or
	 * '63xx') to a command.
	 * <p>
	 * EMV 4.3 Book 1 section 12.3.3 step 7 separates it from an error when the
	 * applications matching a partial AID are enumerated: on '9000', '62xx' or
	 * '63xx' "the terminal returns to step 3", on anything else it stops.
	 * </p>
	 *
	 * @param pResponse
	 *            response to the command
	 * @return true if the card answered a warning status
	 */
	protected boolean isWarning(final byte[] pResponse) {
		if (pResponse == null || pResponse.length < 2) {
			return false;
		}
		int sw1 = pResponse[pResponse.length - 2] & 0xFF;
		return sw1 == 0x62 || sw1 == 0x63;
	}

	/**
	 * Method used to know if the card answered '6A81' (function not supported)
	 * to the SELECT command, which EMV 4.3 Book 1 reads as a blocked card.
	 * <p>
	 * The specification asks the terminal to terminate the card session in that
	 * case. This library deliberately keeps reading the remaining applications
	 * instead: it only extracts public data, and a card may answer '6A81' for
	 * other reasons, for instance when it does not implement the occurrence
	 * management of the SELECT command.
	 * </p>
	 *
	 * @param pResponse
	 *            response to the SELECT command
	 * @return true if the card reports that the function is not supported
	 */
	protected boolean isCardBlocked(final byte[] pResponse) {
		return ResponseUtils.contains(pResponse, SwEnum.SW_6A81);
	}

	/**
	 * Method used to extract application label
	 *
	 * @param pData
	 * 			raw response data
	 *
	 * @return decoded application label or null
	 */
	protected String extractApplicationLabel(final byte[] pData) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Extract Application label");
		}
		String label = null;
		// Get Preferred name first
		byte[] preferred = TlvUtil.getValue(pData, EmvTags.APPLICATION_PREFERRED_NAME);
		byte[] labelByte = preferred;
		// Get Application label
		if (labelByte == null) {
			labelByte = TlvUtil.getValue(pData, EmvTags.APPLICATION_LABEL);
		}
		// The Application Label is limited to the common character set and the
		// Application Preferred Name to the part of ISO/IEC 8859 the Issuer
		// Code Table Index designates (EMV 4.3 Book 3 section 4.3). Neither is
		// encoded in whatever the platform happens to default to, and the
		// fully decoded name is available from
		// Application#getApplicationPreferredName().
		if (labelByte != null) {
			// The Application Preferred Name uses the part of ISO/IEC 8859 the
			// Issuer Code Table Index designates, the Application Label the
			// common character set alone (EMV 4.3 Book 3 section 4.3). The
			// index travels in the very same response
			label = EmvDataUtils.decodeText(labelByte, preferred != null
					? TlvUtil.getValue(pData, EmvTags.ISSUER_CODE_TABLE_INDEX) : null);
		}
		return label;
	}

	/**
	 * Extract bank data (BIC and IBAN)
	 *
	 * @param pData
	 *            card data
	 */
	protected void extractBankData(final byte[] pData) {
		// Extract BIC data
		byte[] bic = TlvUtil.getValue(pData, EmvTags.BANK_IDENTIFIER_CODE);
		if (bic != null) {
			getTemplate().getCard().setBic(EmvDataUtils.decodeText(bic, 0));
		}
		// Extract IBAN
		byte[] iban = TlvUtil.getValue(pData, EmvTags.IBAN);
		if (iban != null) {
			getTemplate().getCard().setIban(EmvDataUtils.decodeText(iban, 0));
		}
	}

	/**
	 * Extract card holder lastname and firstname
	 *
	 * @param pData
	 *            card data
	 */
	protected void extractCardHolderName(final byte[] pData) {
		// Extract Card Holder name (if exist), the Cardholder Name Extended
		// (tag '9F0B') carries the whole name when it exceeds 26 characters:
		// it replaces a name already read from the truncated Cardholder Name
		// (tag '5F20'), even when the card sends it in a later response
		byte[] cardHolderByte = TlvUtil.getValue(pData, EmvTags.CARDHOLDER_NAME_EXTENDED);
		if (cardHolderByte == null) {
			// The name is read from every response of the card, only the first
			// one found is kept so that a later record cannot mix its value
			// with the one already extracted
			if (getTemplate().getCard().getHolderLastname() != null) {
				return;
			}
			cardHolderByte = TlvUtil.getValue(pData, EmvTags.CARDHOLDER_NAME);
		}
		if (cardHolderByte != null) {
			// The name is a format 'ans' data object, it is decoded with the
			// EMV common character set like every other text of the card and
			// not with whatever the platform default happens to be
			String[] name = StringUtils.split(StringUtils.trimToEmpty(EmvDataUtils.decodeText(cardHolderByte, 0)),
					TrackUtils.CARD_HOLDER_NAME_SEPARATOR);
			if (name != null && name.length > 0) {
				// Both parts are always set together, a name without separator
				// leaves the first name empty instead of keeping a stale one
				getTemplate().getCard().setHolderLastname(StringUtils.trimToNull(name[0]));
				// A name may hold more than one separator, a suffix for
				// instance: everything after the first part is the first name
				// rather than being dropped
				getTemplate().getCard().setHolderFirstname(name.length > 1
						? StringUtils.trimToNull(StringUtils.join(ArrayUtils.subarray(name, 1, name.length), " "))
						: null);
			}
		}
	}

	/**
	 * Method used to extract Log Entry from Select response
	 *
	 * @param pSelectResponse
	 *            select response
	 * @return byte array
	 */
	protected byte[] getLogEntry(final byte[] pSelectResponse) {
		return TlvUtil.getValue(pSelectResponse, EmvTags.LOG_ENTRY, EmvTags.VISA_LOG_ENTRY);
	}

	/**
	 * Method used to extract every additional data object defined by the EMV
	 * specification from a card response (SELECT response, GET PROCESSING
	 * OPTIONS response or record).<br>
	 * The method is cumulative: it can safely be called with every response
	 * read from the card, a data object already extracted is never overwritten.
	 *
	 * @param pData
	 *            raw card response
	 * @param pApplication
	 *            application to fill
	 */
	protected void extractExtendedData(final byte[] pData, final Application pApplication) {
		if (pData == null || pApplication == null) {
			return;
		}
		// Every primitive data object of the response is kept raw, whatever
		// its tag: the typed getters below are the decoded view of the known
		// ones, the proprietary ones are only reachable this way
		collectRawDataObjects(pData, pApplication.getRawDataObjects());
		// The Issuer Code Table Index drives the decoding of every text data
		// object, so it has to be read first
		if (pApplication.getIssuerCodeTableIndex() == UNKNOW) {
			pApplication.setIssuerCodeTableIndex(
					EmvDataUtils.getNumericIntValue(TlvUtil.getValue(pData, EmvTags.ISSUER_CODE_TABLE_INDEX), UNKNOW));
		}
		// The Issuer Code Table Index only governs the Application Preferred
		// Name, and the card may send it after that name: keep the raw value
		// and decode it once the whole application has been read
		if (pApplication.getRawApplicationPreferredName() == null) {
			pApplication.setRawApplicationPreferredName(TlvUtil.getValue(pData, EmvTags.APPLICATION_PREFERRED_NAME));
		}
		if (pApplication.getPan() == null) {
			pApplication.setPan(EmvDataUtils.getNumericValue(TlvUtil.getValue(pData, EmvTags.PAN)));
		}
		if (pApplication.getPanSequenceNumber() == UNKNOW) {
			pApplication.setPanSequenceNumber(
					EmvDataUtils.getNumericIntValue(TlvUtil.getValue(pData, EmvTags.PAN_SEQUENCE_NUMBER), UNKNOW));
		}
		if (pApplication.getExpirationDate() == null) {
			pApplication.setExpirationDate(EmvDataUtils.parseDate(TlvUtil.getValue(pData, EmvTags.APP_EXPIRATION_DATE)));
		}
		if (pApplication.getEffectiveDate() == null) {
			pApplication.setEffectiveDate(EmvDataUtils.parseDate(TlvUtil.getValue(pData, EmvTags.APP_EFFECTIVE_DATE)));
		}
		if (pApplication.getIssuerCountryCode() == null) {
			pApplication.setIssuerCountryCode(extractIssuerCountryCode(pData));
		}
		// The codes as the card sent them, kept apart from the enumerations
		// which only know the ISO 3166-1 and ISO 4217 values
		if (pApplication.getIssuerCountryCodeNumeric() == UNKNOW) {
			pApplication.setIssuerCountryCodeNumeric(
					EmvDataUtils.getNumericIntValue(TlvUtil.getValue(pData, EmvTags.ISSUER_COUNTRY_CODE), UNKNOW));
		}
		if (pApplication.getIssuerCountryCodeAlpha3() == null) {
			pApplication.setIssuerCountryCodeAlpha3(
					EmvDataUtils.decodeText(TlvUtil.getValue(pData, EmvTags.ISSUER_COUNTRY_CODE_ALPHA3), 0));
		}
		if (pApplication.getIssuerCountryCodeAlpha2() == null) {
			pApplication.setIssuerCountryCodeAlpha2(
					EmvDataUtils.decodeText(TlvUtil.getValue(pData, EmvTags.ISSUER_COUNTRY_CODE_ALPHA2), 0));
		}
		if (pApplication.getCurrency() == null) {
			int currency = EmvDataUtils.getNumericIntValue(TlvUtil.getValue(pData, EmvTags.APPLICATION_CURRENCY_CODE), UNKNOW);
			if (currency != UNKNOW) {
				pApplication.setCurrency(EnumUtils.find(currency, CurrencyEnum.class));
			}
		}
		if (pApplication.getCurrencyCode() == UNKNOW) {
			pApplication.setCurrencyCode(
					EmvDataUtils.getNumericIntValue(TlvUtil.getValue(pData, EmvTags.APPLICATION_CURRENCY_CODE), UNKNOW));
		}
		if (pApplication.getCurrencyExponent() == UNKNOW) {
			pApplication.setCurrencyExponent(
					EmvDataUtils.getNumericIntValue(TlvUtil.getValue(pData, EmvTags.APP_CURRENCY_EXPONENT), UNKNOW));
		}
		if (pApplication.getApplicationVersionNumber() == UNKNOW) {
			pApplication.setApplicationVersionNumber(
					(int) EmvDataUtils.getBinaryValue(TlvUtil.getValue(pData, EmvTags.APP_VERSION_NUMBER_CARD), UNKNOW));
		}
		if (pApplication.getServiceCode() == null) {
			String serviceCode = EmvDataUtils.getNumericValue(TlvUtil.getValue(pData, EmvTags.SERVICE_CODE));
			if (serviceCode != null && serviceCode.length() >= 3) {
				pApplication.setServiceCode(new Service(serviceCode.substring(serviceCode.length() - 3)));
			}
		}
		// The three digits themselves, the Service bean loses the ones its
		// enumerations do not know
		if (pApplication.getServiceCodeDigits() == null) {
			String serviceCode = EmvDataUtils.getNumericValue(TlvUtil.getValue(pData, EmvTags.SERVICE_CODE));
			if (serviceCode != null && serviceCode.length() >= 3) {
				pApplication.setServiceCodeDigits(serviceCode.substring(serviceCode.length() - 3));
			}
		}
		if (pApplication.getLanguagePreferences().isEmpty()) {
			pApplication.setLanguagePreferences(
					EmvDataUtils.parseLanguagePreference(TlvUtil.getValue(pData, EmvTags.LANGUAGE_PREFERENCE)));
		}
		if (pApplication.getIssuerUrl() == null) {
			// The Issuer URL and the Payment Account Reference are ASCII, the
			// Issuer Code Table Index does not apply to them
			pApplication.setIssuerUrl(EmvDataUtils.decodeText(TlvUtil.getValue(pData, EmvTags.ISSUER_URL), 0));
		}
		if (pApplication.getInterchangeProfile() == null) {
			byte[] aip = TlvUtil.getValue(pData, EmvTags.APPLICATION_INTERCHANGE_PROFILE);
			if (aip != null && aip.length >= 2) {
				pApplication.setInterchangeProfile(new ApplicationInterchangeProfile(aip));
			}
		}
		if (pApplication.getUsageControl() == null) {
			byte[] auc = TlvUtil.getValue(pData, EmvTags.APP_USAGE_CONTROL);
			if (auc != null && auc.length >= 2) {
				pApplication.setUsageControl(new ApplicationUsageControl(auc));
			}
		}
		// Data objects a mobile wallet personalizes on a tokenized credential
		if (pApplication.getPaymentAccountReference() == null) {
			pApplication.setPaymentAccountReference(
					EmvDataUtils.decodeText(TlvUtil.getValue(pData, EmvTags.PAYMENT_ACCOUNT_REFERENCE), 0));
		}
		if (pApplication.getTokenRequestorId() == null) {
			pApplication.setTokenRequestorId(EmvDataUtils.getNumericValue(TlvUtil.getValue(pData, EmvTags.TOKEN_REQUESTOR_ID)));
		}
		if (pApplication.getLast4DigitsOfPan() == null) {
			pApplication.setLast4DigitsOfPan(EmvDataUtils.getNumericValue(TlvUtil.getValue(pData, EmvTags.LAST_4_DIGITS_OF_PAN)));
		}
		if (pApplication.getCvmList().isEmpty()) {
			byte[] rawCvmList = TlvUtil.getValue(pData, EmvTags.CVM_LIST);
			List<CVM> cvmList = CvmUtils.parseCvmList(rawCvmList);
			if (!cvmList.isEmpty()) {
				pApplication.setCvmList(cvmList);
				// The 8 bytes header of the tag '8E' holds the two amounts the
				// "under X" and "over Y" conditions of the rules refer to
				pApplication.setCvmAmountX(CvmUtils.getAmountX(rawCvmList));
				pApplication.setCvmAmountY(CvmUtils.getAmountY(rawCvmList));
			}
		}
		// The Issuer Action Codes state the conditions under which the issuer
		// wants the transaction denied, sent online or approved offline. They
		// are bitmaps built on the Terminal Verification Results layout (EMV
		// 4.3 Book 3 Annex C5)
		if (pApplication.getIssuerActionCodeDenial() == null) {
			pApplication.setIssuerActionCodeDenial(TlvUtil.getValue(pData, EmvTags.ISSUER_ACTION_CODE_DENIAL));
		}
		if (pApplication.getIssuerActionCodeOnline() == null) {
			pApplication.setIssuerActionCodeOnline(TlvUtil.getValue(pData, EmvTags.ISSUER_ACTION_CODE_ONLINE));
		}
		if (pApplication.getIssuerActionCodeDefault() == null) {
			pApplication.setIssuerActionCodeDefault(TlvUtil.getValue(pData, EmvTags.ISSUER_ACTION_CODE_DEFAULT));
		}
		// Capability data objects whose meaning depends on the payment system,
		// they are exposed raw
		if (pApplication.getCardTransactionQualifiers() == null) {
			pApplication.setCardTransactionQualifiers(TlvUtil.getValue(pData, EmvTags.MAG_STRIPE_APP_VERSION_NUMBER_CARD));
		}
		if (pApplication.getFormFactorIndicator() == null) {
			pApplication.setFormFactorIndicator(TlvUtil.getValue(pData, EmvTags.VLP_ISSUER_AUTHORISATION_CODE));
		}
		if (pApplication.getApplicationCapabilitiesInformation() == null) {
			pApplication.setApplicationCapabilitiesInformation(
					TlvUtil.getValue(pData, EmvTags.APPLICATION_CAPABILITIES_INFORMATION));
		}
		// The Kernel Identifier is normally read from the payment directory,
		// which is the one EMV Contactless Book B uses for the Combination
		// Selection, but a card may also put it in the File Control Information
		// of its application. The one already read wins, and the test is on the
		// raw value: a domestic identifier names no Short Kernel Identifier, so
		// keying this on the identifier alone would let the ADF erase it
		if (pApplication.getKernelIdentifier() == null) {
			byte[] kernelIdentifier = TlvUtil.getValue(pData, EmvTags.KERNEL_IDENTIFIER);
			if (kernelIdentifier != null) {
				pApplication.setKernelIdentifier(kernelIdentifier);
			}
		}
		// The Application Priority Indicator is optional in the File Control
		// Information too, and an application read by AID never goes through a
		// directory: without this the priority is the default one and the
		// cardholder confirmation flag is silently false
		if (!pApplication.isPriorityKnown()) {
			byte[] priority = TlvUtil.getValue(pData, EmvTags.APPLICATION_PRIORITY_INDICATOR);
			if (priority != null && priority.length > 0) {
				pApplication.setApplicationPriorityIndicator(priority[0]);
			}
		}
		if (pApplication.getIssuerApplicationData() == null) {
			pApplication.setIssuerApplicationData(TlvUtil.getValue(pData, EmvTags.ISSUER_APPLICATION_DATA));
		}
		if (pApplication.getIssuerIdentificationNumber() == null) {
			pApplication.setIssuerIdentificationNumber(
					EmvDataUtils.getNumericValue(TlvUtil.getValue(pData, EmvTags.ISSUER_IDENTIFICATION_NUMBER)));
		}
		// The Issuer Identification Number Extended is the eight digit form of
		// the same number, EMV 4.4 Book 3 Annex A1: "cards may have both the IIN
		// and IINE data objects present". They are read separately, the extended
		// one is never rebuilt from the truncated one
		if (pApplication.getIssuerIdentificationNumberExtended() == null) {
			pApplication.setIssuerIdentificationNumberExtended(EmvDataUtils
					.getNumericValue(TlvUtil.getValue(pData, EmvTags.ISSUER_IDENTIFICATION_NUMBER_EXTENDED)));
		}
		if (pApplication.getApplicationSelectionRegisteredProprietaryData() == null) {
			pApplication.setApplicationSelectionRegisteredProprietaryData(
					TlvUtil.getValue(pData, EmvTags.APPLICATION_SELECTION_REGISTERED_PROPRIETARY_DATA));
		}
		if (pApplication.getApplicationDiscretionaryData() == null) {
			pApplication.setApplicationDiscretionaryData(TlvUtil.getValue(pData, EmvTags.APP_DISCRETIONARY_DATA));
		}
		// Velocity checking parameters: the issuer thresholds the number of
		// consecutive offline transactions is to be read against
		if (pApplication.getLowerConsecutiveOfflineLimit() == UNKNOW) {
			pApplication.setLowerConsecutiveOfflineLimit(
					(int) EmvDataUtils.getBinaryValue(TlvUtil.getValue(pData, EmvTags.LOWER_CONSEC_OFFLINE_LIMIT), UNKNOW));
		}
		if (pApplication.getUpperConsecutiveOfflineLimit() == UNKNOW) {
			pApplication.setUpperConsecutiveOfflineLimit(
					(int) EmvDataUtils.getBinaryValue(TlvUtil.getValue(pData, EmvTags.UPPER_CONSEC_OFFLINE_LIMIT), UNKNOW));
		}
		if (pApplication.getReferenceCurrencies().isEmpty()) {
			extractReferenceCurrencies(pData, pApplication);
		}
		// The Application Label of the File Control Information (tag '50') on
		// its own: getApplicationLabel() prefers the Application Preferred Name
		if (pApplication.getFciApplicationLabel() == null) {
			pApplication.setFciApplicationLabel(EmvDataUtils.decodeText(TlvUtil.getValue(pData, EmvTags.APPLICATION_LABEL), 0));
		}
		// The per application reading of the texts extractBankData and
		// extractCardHolderName put on the card: the first value found wins
		// here where the card level ones are overwritten by later applications
		if (pApplication.getBic() == null) {
			pApplication.setBic(EmvDataUtils.decodeText(TlvUtil.getValue(pData, EmvTags.BANK_IDENTIFIER_CODE), 0));
		}
		if (pApplication.getIban() == null) {
			pApplication.setIban(EmvDataUtils.decodeText(TlvUtil.getValue(pData, EmvTags.IBAN), 0));
		}
		if (pApplication.getCardholderName() == null) {
			pApplication.setCardholderName(
					StringUtils.trimToNull(EmvDataUtils.decodeText(TlvUtil.getValue(pData, EmvTags.CARDHOLDER_NAME), 0)));
		}
		if (pApplication.getCardholderNameExtended() == null) {
			pApplication.setCardholderNameExtended(StringUtils
					.trimToNull(EmvDataUtils.decodeText(TlvUtil.getValue(pData, EmvTags.CARDHOLDER_NAME_EXTENDED), 0)));
		}
		// Tag '9F66' as the card answered it, whatever its length: 2 bytes is
		// PUNATC(Track2) on Kernel 2 (read by extractMagStripeData), 4 bytes
		// the Terminal Transaction Qualifiers a Kernel 3 card echoes back
		if (pApplication.getTerminalTransactionQualifiersEcho() == null) {
			pApplication.setTerminalTransactionQualifiersEcho(TlvUtil.getValue(pData, EmvTags.TERMINAL_TRANSACTION_QUALIFIERS));
		}
		// The kernel the payment system tags of this application are read
		// against, see resolveKernel: it may only be known once the Kernel
		// Identifier above has been read, so it is refreshed on every response
		KernelEnum kernel = resolveKernel(pApplication);
		pApplication.setResolvedKernel(kernel);
		// The Card Transaction Qualifiers only exist on Kernel 3 (Visa), where
		// they are 2 bytes long: the same tag is the Mag-stripe Application
		// Version Number on Kernel 2
		if (KernelEnum.VISA == kernel && pApplication.getCardTransactionQualifiersDecoded() == null) {
			byte[] ctq = pApplication.getCardTransactionQualifiers();
			if (ctq != null && ctq.length == CARD_TRANSACTION_QUALIFIERS_LENGTH) {
				pApplication.setCardTransactionQualifiersDecoded(new CardTransactionQualifiers(ctq));
			}
		}
		extractDataObjectLists(pData, pApplication);
		extractOfflineDataAuthentication(pData, pApplication);
		extractMagStripeData(pData, pApplication);
		// Bank data is card wide, not application wide
		extractBankData(pData);
		extractCardHolderName(pData);
	}

	/**
	 * Method used to derive the kernel from the AID when the card announced none.
	 * <p>
	 * EMV Contactless Book B section 3.3: "If the Kernel Identifier is absent
	 * from a Directory Entry, Entry Point bases its kernel decision upon the ADF
	 * Name: For a JCB ADF Name, it will use Kernel 5. For a MasterCard ADF Name,
	 * it will use Kernel 2. For a Visa ADF Name, it will use Kernel 3 (or Kernel
	 * 1: see requirement 3.3.3.6). For an American Express ADF Name, it will use
	 * Kernel 4."
	 * </p>
	 *
	 * @param pApplication
	 *            application to complete
	 */
	protected void deriveKernelFromAid(final Application pApplication) {
		if (pApplication == null || pApplication.getShortKernelId() != UNKNOW || pApplication.getAid() == null) {
			return;
		}
		// EMV Contactless Book B only allows the AID to give a default value for
		// the Requested Kernel ID when the card expressed no preference: a card
		// that did ask for a kernel must not be answered another one
		if (pApplication.isKernelRequestedByCard()) {
			return;
		}
		KernelEnum kernel = kernelFromAid(pApplication.getAid());
		if (kernel != null) {
			pApplication.setShortKernelId(kernel.getKey());
			pApplication.setKernelDerived(true);
		}
	}

	/**
	 * Method used to read the default Requested Kernel ID of an AID, the table
	 * of EMV Contactless Book B section 3.3.
	 *
	 * @param pAid
	 *            Application Identifier of the application
	 * @return the kernel the AID implies, or null when it implies none
	 */
	protected KernelEnum kernelFromAid(final byte[] pAid) {
		if (pAid == null) {
			return null;
		}
		EmvCardScheme scheme = EmvCardScheme.getCardTypeByAid(BytesUtils.bytesToStringNoSpace(pAid));
		KernelEnum kernel = null;
		if (EmvCardScheme.MASTER_CARD == scheme) {
			kernel = KernelEnum.MASTERCARD;
		} else if (EmvCardScheme.VISA == scheme) {
			kernel = KernelEnum.VISA;
		} else if (EmvCardScheme.AMERICAN_EXPRESS == scheme) {
			kernel = KernelEnum.AMERICAN_EXPRESS;
		} else if (EmvCardScheme.JCB == scheme) {
			kernel = KernelEnum.JCB;
		} else if (EmvCardScheme.DISCOVER == scheme) {
			kernel = KernelEnum.DISCOVER;
		} else if (EmvCardScheme.UNIONPAY == scheme) {
			kernel = KernelEnum.UNIONPAY;
		}
		return kernel;
	}

	/**
	 * Method used to know which kernel the data objects of an application have
	 * to be read against.
	 * <p>
	 * A number of tags of the payment system range '9F50' to '9F7F' were
	 * assigned twice, and the kernel is what tells the two readings apart. EMV
	 * Contactless Book B fixes the kernel before the first GET PROCESSING
	 * OPTIONS: the Kernel Identifier of the payment directory names it, and the
	 * AID gives a default value when the card expressed no preference.
	 * </p>
	 * <p>
	 * This is not {@link #deriveKernelFromAid(Application)}: it answers while an
	 * application is being read, which is before the kernel is stored on it, and
	 * it stores nothing.
	 * </p>
	 *
	 * @param pApplication
	 *            application being read
	 * @return the kernel, or null when neither the card nor the AID names one
	 */
	protected KernelEnum resolveKernel(final Application pApplication) {
		if (pApplication == null) {
			return null;
		}
		KernelEnum kernel = pApplication.getKernel();
		if (kernel != null) {
			return kernel;
		}
		// A card that did ask for a kernel must not be answered another one:
		// a domestic Kernel Identifier names no international kernel, and the
		// AID may not overrule it
		if (pApplication.isKernelRequestedByCard()) {
			return null;
		}
		return kernelFromAid(pApplication.getAid());
	}

	/**
	 * Method used to extract the Application Reference Currency (tag '9F3B')
	 * and its exponents (tag '9F43'), which are paired positionally.
	 *
	 * @param pData
	 *            raw card response
	 * @param pApplication
	 *            application to fill
	 */
	protected void extractReferenceCurrencies(final byte[] pData, final Application pApplication) {
		byte[] currencies = TlvUtil.getValue(pData, EmvTags.APP_REFERENCE_CURRENCY);
		if (currencies == null || currencies.length < 2) {
			return;
		}
		byte[] exponents = TlvUtil.getValue(pData, EmvTags.APP_REFERENCE_CURRECY_EXPONENT);
		List<CurrencyEnum> list = new ArrayList<CurrencyEnum>();
		List<Integer> exponentList = new ArrayList<Integer>();
		// Every code as the card sent it, the zero and unknown ones included,
		// only filled once
		List<Integer> codes = new ArrayList<Integer>();
		// The dictionary describes one to four currencies, each on 2 bytes with
		// one exponent byte. A card declaring more is reported as it answered:
		// the loop is bounded by the response either way
		for (int i = 0; i + 1 < currencies.length; i += 2) {
			int code = EmvDataUtils.getNumericIntValue(new byte[] { currencies[i], currencies[i + 1] }, UNKNOW);
			if (code != UNKNOW) {
				codes.add(code);
			}
			if (code == UNKNOW || code == 0) {
				continue;
			}
			CurrencyEnum currency = EnumUtils.find(code, CurrencyEnum.class);
			if (currency == null) {
				continue;
			}
			list.add(currency);
			int index = i / 2;
			exponentList.add(exponents != null && index < exponents.length
					? EmvDataUtils.getNumericIntValue(new byte[] { exponents[index] }, UNKNOW) : UNKNOW);
		}
		pApplication.setReferenceCurrencies(list);
		pApplication.setReferenceCurrencyExponents(exponentList);
		if (pApplication.getReferenceCurrencyCodes().isEmpty()) {
			pApplication.setReferenceCurrencyCodes(codes);
		}
	}

	/**
	 * Method used to extract the data object lists the card carries.
	 * <p>
	 * They describe what the application would demand of a terminal running a
	 * transaction. This library reads them and never executes them: every
	 * command they feed is one it does not send.
	 * </p>
	 *
	 * @param pData
	 *            raw card response
	 * @param pApplication
	 *            application to fill
	 */
	protected void extractDataObjectLists(final byte[] pData, final Application pApplication) {
		// The raw value of each list is kept next to the parsed one: the parsed
		// list is transient, and a truncated list still has a raw value
		if (pApplication.getRawCdol1() == null) {
			byte[] cdol1 = TlvUtil.getValue(pData, EmvTags.CDOL1);
			pApplication.setRawCdol1(cdol1);
			if (pApplication.getCdol1().isEmpty()) {
				pApplication.setCdol1(TlvUtil.parseTagAndLength(cdol1));
			}
		} else if (pApplication.getCdol1().isEmpty()) {
			pApplication.setCdol1(TlvUtil.parseTagAndLength(TlvUtil.getValue(pData, EmvTags.CDOL1)));
		}
		if (pApplication.getRawCdol2() == null) {
			byte[] cdol2 = TlvUtil.getValue(pData, EmvTags.CDOL2);
			pApplication.setRawCdol2(cdol2);
			if (pApplication.getCdol2().isEmpty()) {
				pApplication.setCdol2(TlvUtil.parseTagAndLength(cdol2));
			}
		} else if (pApplication.getCdol2().isEmpty()) {
			pApplication.setCdol2(TlvUtil.parseTagAndLength(TlvUtil.getValue(pData, EmvTags.CDOL2)));
		}
		if (pApplication.getRawDdol() == null) {
			byte[] ddol = TlvUtil.getValue(pData, EmvTags.DDOL);
			pApplication.setRawDdol(ddol);
			if (pApplication.getDdol().isEmpty()) {
				pApplication.setDdol(TlvUtil.parseTagAndLength(ddol));
			}
		} else if (pApplication.getDdol().isEmpty()) {
			pApplication.setDdol(TlvUtil.parseTagAndLength(TlvUtil.getValue(pData, EmvTags.DDOL)));
		}
		if (pApplication.getRawTdol() == null) {
			byte[] tdol = TlvUtil.getValue(pData, EmvTags.TDOL);
			pApplication.setRawTdol(tdol);
			if (pApplication.getTdol().isEmpty()) {
				pApplication.setTdol(TlvUtil.parseTagAndLength(tdol));
			}
		} else if (pApplication.getTdol().isEmpty()) {
			pApplication.setTdol(TlvUtil.parseTagAndLength(TlvUtil.getValue(pData, EmvTags.TDOL)));
		}
	}

	/**
	 * Method used to extract the credentials of the offline data
	 * authentication. Nothing is verified, see
	 * {@link OfflineDataAuthentication}.
	 *
	 * @param pData
	 *            raw card response
	 * @param pApplication
	 *            application to fill
	 */
	protected void extractOfflineDataAuthentication(final byte[] pData, final Application pApplication) {
		OfflineDataAuthentication oda = pApplication.getOfflineDataAuthentication();
		if (oda.getCertificationAuthorityPublicKeyIndex() == UNKNOW) {
			oda.setCertificationAuthorityPublicKeyIndex(
					(int) EmvDataUtils.getBinaryValue(TlvUtil.getValue(pData, EmvTags.CA_PUBLIC_KEY_INDEX_CARD), UNKNOW));
		}
		if (oda.getIssuerPublicKeyCertificate() == null) {
			oda.setIssuerPublicKeyCertificate(TlvUtil.getValue(pData, EmvTags.ISSUER_PUBLIC_KEY_CERT));
		}
		if (oda.getIssuerPublicKeyRemainder() == null) {
			oda.setIssuerPublicKeyRemainder(TlvUtil.getValue(pData, EmvTags.ISSUER_PUBLIC_KEY_REMAINDER));
		}
		if (oda.getIssuerPublicKeyExponent() == null) {
			oda.setIssuerPublicKeyExponent(TlvUtil.getValue(pData, EmvTags.ISSUER_PUBLIC_KEY_EXP));
		}
		if (oda.getSignedStaticApplicationData() == null) {
			oda.setSignedStaticApplicationData(TlvUtil.getValue(pData, EmvTags.SIGNED_STATIC_APP_DATA));
		}
		if (oda.getIccPublicKeyCertificate() == null) {
			oda.setIccPublicKeyCertificate(TlvUtil.getValue(pData, EmvTags.ICC_PUBLIC_KEY_CERT));
		}
		if (oda.getIccPublicKeyExponent() == null) {
			oda.setIccPublicKeyExponent(TlvUtil.getValue(pData, EmvTags.ICC_PUBLIC_KEY_EXP));
		}
		if (oda.getIccPublicKeyRemainder() == null) {
			oda.setIccPublicKeyRemainder(TlvUtil.getValue(pData, EmvTags.ICC_PUBLIC_KEY_REMAINDER));
		}
		if (oda.getStaticDataAuthenticationTagList() == null) {
			oda.setStaticDataAuthenticationTagList(TlvUtil.getValue(pData, EmvTags.SDA_TAG_LIST));
		}
		if (oda.getSignedDynamicApplicationData() == null) {
			oda.setSignedDynamicApplicationData(TlvUtil.getValue(pData, EmvTags.SIGNED_DYNAMIC_APPLICATION_DATA));
		}
		if (oda.getIccPinEnciphermentPublicKeyCertificate() == null) {
			oda.setIccPinEnciphermentPublicKeyCertificate(
					TlvUtil.getValue(pData, EmvTags.ICC_PIN_ENCIPHERMENT_PUBLIC_KEY_CERT));
		}
		if (oda.getIccPinEnciphermentPublicKeyExponent() == null) {
			oda.setIccPinEnciphermentPublicKeyExponent(TlvUtil.getValue(pData, EmvTags.ICC_PIN_ENCIPHERMENT_PUBLIC_KEY_EXP));
		}
		if (oda.getIccPinEnciphermentPublicKeyRemainder() == null) {
			oda.setIccPinEnciphermentPublicKeyRemainder(TlvUtil.getValue(pData, EmvTags.ICC_PIN_ENCIPHERMENT_PUBLIC_KEY_REM));
		}
	}

	/**
	 * Method used to extract the data objects the card was personalised with
	 * for the contactless Mag-stripe Mode of EMV Contactless Book C-2 v2.11.
	 * <p>
	 * The profile belongs to Kernel 2, and the extraction is skipped for any
	 * other kernel: tag '9F66' is PUNATC(Track2) for Kernel 2 and the reader
	 * sourced Terminal Transaction Qualifiers for Kernel 3, tag '9F69' is the
	 * UDOL for Kernel 2 and the Card Authentication Related Data for Kernel 3.
	 * Both readings of '9F69' are card sourced and read from a record, so
	 * nothing but the kernel tells them apart.
	 * </p>
	 * <p>
	 * The Mag-stripe Application Version Number a MasterCard card is said to
	 * answer at tag '9F6C' is deliberately not read here: no public
	 * specification defines a card sourced data object at that tag, only the
	 * Card Transaction Qualifiers of Kernel 3, and both readings are two bytes
	 * long. The raw value stays available as
	 * {@link Application#getCardTransactionQualifiers()}.
	 * </p>
	 * <p>
	 * The COMPUTE CRYPTOGRAPHIC CHECKSUM command that produces the CVC3 is
	 * outside of the read only scope of this library and is never sent: the two
	 * CVC3 data objects are only picked up when a response read for another
	 * reason already carried them.
	 * </p>
	 *
	 * @param pData
	 *            raw card response
	 * @param pApplication
	 *            application to fill
	 */
	protected void extractMagStripeData(final byte[] pData, final Application pApplication) {
		if (KernelEnum.MASTERCARD != resolveKernel(pApplication)) {
			return;
		}
		MagStripeData magStripe = pApplication.getMagStripeData();
		// Tag '9F6C' on Kernel 2 is the Mag-stripe Application Version Number
		// (Card), it is only reached here for the MasterCard kernel
		if (magStripe.getApplicationVersionNumber() == UNKNOW) {
			magStripe.setApplicationVersionNumber((int) EmvDataUtils
					.getBinaryValue(TlvUtil.getValue(pData, EmvTags.MAG_STRIPE_APP_VERSION_NUMBER_CARD), UNKNOW));
		}
		if (magStripe.getTrack1Data() == null) {
			magStripe.setTrack1Data(TlvUtil.getValue(pData, EmvTags.TRACK1_DATA));
		}
		if (magStripe.getTrack2Data() == null) {
			magStripe.setTrack2Data(TlvUtil.getValue(pData, EmvTags.TRACK2_DATA));
		}
		if (magStripe.getPcvc3Track1() == null) {
			magStripe.setPcvc3Track1(TlvUtil.getValue(pData, EmvTags.PCVC3_TRACK1));
		}
		if (magStripe.getPunatcTrack1() == null) {
			magStripe.setPunatcTrack1(TlvUtil.getValue(pData, EmvTags.PUNATC_TRACK1));
		}
		if (magStripe.getNatcTrack1() == UNKNOW) {
			magStripe.setNatcTrack1((int) EmvDataUtils.getBinaryValue(TlvUtil.getValue(pData, EmvTags.NATC_TRACK1), UNKNOW));
		}
		if (magStripe.getPcvc3Track2() == null) {
			magStripe.setPcvc3Track2(TlvUtil.getValue(pData, EmvTags.PCVC3_TRACK2));
		}
		if (magStripe.getPunatcTrack2() == null) {
			// EMV Contactless Book C-2 v2.11 A.1.128 gives PUNATC(Track2) a
			// length of 2. A four bytes '9F66' is the Terminal Transaction
			// Qualifiers a reader pushes in the data field of its GET
			// PROCESSING OPTIONS, which a card may well echo back
			byte[] punatc = TlvUtil.getValue(pData, EmvTags.TERMINAL_TRANSACTION_QUALIFIERS);
			if (punatc != null && punatc.length == PUNATC_TRACK2_LENGTH) {
				magStripe.setPunatcTrack2(punatc);
			}
		}
		if (magStripe.getNatcTrack2() == UNKNOW) {
			magStripe.setNatcTrack2((int) EmvDataUtils.getBinaryValue(TlvUtil.getValue(pData, EmvTags.NATC_TRACK2), UNKNOW));
		}
		if (magStripe.getRawUdol() == null) {
			byte[] udol = TlvUtil.getValue(pData, EmvTags.UDOL);
			if (udol != null) {
				magStripe.setUdol(udol, TlvUtil.parseTagAndLength(udol));
			}
		}
		if (magStripe.getCvc3Track1() == null) {
			magStripe.setCvc3Track1(TlvUtil.getValue(pData, EmvTags.CVC3_TRACK1));
		}
		if (magStripe.getCvc3Track2() == null) {
			magStripe.setCvc3Track2(TlvUtil.getValue(pData, EmvTags.CVC3_TRACK2));
		}
	}

	/**
	 * Method used to decode the text data objects that depend on the Issuer
	 * Code Table Index (tag '9F11').<br>
	 * It has to be called once every response of the application has been read,
	 * a card is free to send the index after the name it applies to.
	 *
	 * @param pApplication
	 *            application to complete
	 */
	protected void decodeTextData(final Application pApplication) {
		if (pApplication == null || pApplication.getRawApplicationPreferredName() == null) {
			return;
		}
		pApplication.setApplicationPreferredName(EmvDataUtils.decodeText(pApplication.getRawApplicationPreferredName(),
				pApplication.getIssuerCodeTableIndex()));
	}

	/**
	 * Method used to extract the issuer country, the numeric form (tag '5F28')
	 * is used first then the alpha3 form (tag '5F56').
	 *
	 * @param pData
	 *            raw card response
	 * @return the issuer country or null
	 */
	protected CountryCodeEnum extractIssuerCountryCode(final byte[] pData) {
		CountryCodeEnum ret = null;
		int country = EmvDataUtils.getNumericIntValue(TlvUtil.getValue(pData, EmvTags.ISSUER_COUNTRY_CODE), UNKNOW);
		if (country != UNKNOW) {
			ret = EnumUtils.find(country, CountryCodeEnum.class);
		}
		// The numeric form is preferred, but a card may send a code that is not
		// in ISO 3166-1, or one of the codes it reserves: the alphabetic forms
		// are then read instead of reporting no country at all
		if (ret == null) {
			byte[] alpha3 = TlvUtil.getValue(pData, EmvTags.ISSUER_COUNTRY_CODE_ALPHA3);
			if (alpha3 != null && alpha3.length >= 3) {
				ret = CountryCodeEnum.getCountry(EmvDataUtils.decodeText(alpha3, 0));
			}
		}
		if (ret == null) {
			byte[] alpha2 = TlvUtil.getValue(pData, EmvTags.ISSUER_COUNTRY_CODE_ALPHA2);
			if (alpha2 != null && alpha2.length >= 2) {
				ret = CountryCodeEnum.getCountryByAlpha2(EmvDataUtils.decodeText(alpha2, 0));
			}
		}
		return ret;
	}

	/**
	 * Method used to send a GET DATA command and extract the value of the
	 * requested tag.
	 * <p>
	 * A data object is optional for the card, and a card that does not hold the
	 * one being asked for answers a status word instead of a value: 'function
	 * not supported', 'referenced data not found', 'instruction code not
	 * supported' and the like, one per card operating system. None of them is a
	 * failure of the reading, they are what a card says when it has nothing to
	 * say: the method reports the absence with a null value, traces the status
	 * word at debug level only and lets the caller carry on with the next data
	 * object. The exchange itself failing is another matter, it is reported as
	 * a {@link CommunicationException} like everywhere else.
	 * </p>
	 *
	 * @param pTag
	 *            the tag to read, its identifier is used as P1/P2
	 * @return the raw value of the tag or null when the card does not provide
	 *         it
	 * @throws CommunicationException
	 *             communication error
	 */
	protected byte[] getData(final ITag pTag) throws CommunicationException {
		return getData(pTag, null);
	}

	/**
	 * Method used to send a GET DATA command and extract the value of the
	 * requested tag, recording the status word the card answered on the
	 * application, see {@link Application#getGetDataStatusWords()}.<br>
	 * Same behaviour as {@link #getData(ITag)} otherwise.
	 *
	 * @param pTag
	 *            the tag to read, its identifier is used as P1/P2
	 * @param pApplication
	 *            application the status word is recorded on, may be null
	 * @return the raw value of the tag or null when the card does not provide
	 *         it
	 * @throws CommunicationException
	 *             communication error
	 */
	protected byte[] getData(final ITag pTag, final Application pApplication) throws CommunicationException {
		byte[] tagBytes = pTag.getTagBytes();
		if (tagBytes.length != 2) {
			throw new IllegalArgumentException("GET DATA requires a 2 bytes tag: " + pTag);
		}
		byte[] data = getTemplate().getProvider()
				.transceive(new CommandApdu(CommandEnum.GET_DATA, tagBytes[0] & 0xFF, tagBytes[1] & 0xFF, 0).toBytes());
		if (pApplication != null) {
			String sw = statusWordOf(data);
			if (sw != null) {
				pApplication.getGetDataStatusWords().put(hexTag(pTag), sw);
			}
		}
		if (ResponseUtils.isSucceedOrWarning(data)) {
			byte[] dataField = ResponseUtils.getDataField(data);
			byte[] value = TlvUtil.getValue(dataField, pTag);
			// Cards answer a GET DATA with the value alone as often as with the
			// data object. When the response holds no readable data object at
			// all, it is the bare value: reporting the tag absent would throw
			// away what the card did send. CPLCUtils has always read the tag
			// '9F7F' that way, this only generalises it to the other tags
			if (value == null && dataField.length > 0 && TlvUtil.getNextTLV(new BerTlvInputStream(dataField)) == null) {
				LOGGER.debug("The card answered " + pTag.getName() + " without its tag, the value is read as it is");
				return dataField;
			}
			return value;
		}
		if (LOGGER.isDebugEnabled()) {
			SwEnum status = SwEnum.getSW(data);
			LOGGER.debug("The card does not provide " + pTag.getName() + ": "
					+ (status != null ? status.getDetail() : "unknown status word"));
		}
		return null;
	}

	/**
	 * Method used to read one Standalone Data Storage data envelope (a tag of
	 * the range '9F70' to '9F79') with the GET DATA command.
	 *
	 * @param pTag
	 *            the envelope to read
	 * @return the raw value of the envelope or null when the card does not
	 *         provide it
	 * @throws CommunicationException
	 *             communication error
	 */
	protected byte[] getDataEnvelope(final ITag pTag) throws CommunicationException {
		return getDataEnvelope(pTag, null);
	}

	/**
	 * Method used to read one Standalone Data Storage data envelope (a tag of
	 * the range '9F70' to '9F79') with the GET DATA command, recording the
	 * status word on the application and the tag in
	 * {@link DataEnvelopes#getOversizedTags()} when the value exceeds
	 * {@link DataEnvelopes#MAX_LENGTH}.
	 *
	 * @param pTag
	 *            the envelope to read
	 * @param pApplication
	 *            application read, may be null
	 * @return the raw value of the envelope or null when the card does not
	 *         provide it
	 * @throws CommunicationException
	 *             communication error
	 */
	protected byte[] getDataEnvelope(final ITag pTag, final Application pApplication) throws CommunicationException {
		byte[] value = getData(pTag, pApplication);
		// EMV Contactless Book C-2 v2.12 section 3.5.2: "The length of the data
		// is variable. The maximum length is implementation specific, and is
		// between 32 and 192 bytes." A card answering more is reported as it
		// answered, a reader says what it saw
		if (value != null && value.length > DataEnvelopes.MAX_LENGTH) {
			LOGGER.warn("The card answered " + value.length + " bytes for " + pTag.getName() + ", more than the "
					+ DataEnvelopes.MAX_LENGTH + " bytes a data envelope holds");
			if (pApplication != null) {
				List<String> oversized = pApplication.getDataEnvelopes().getOversizedTags();
				String key = hexTag(pTag);
				if (key != null && !oversized.contains(key)) {
					oversized.add(key);
				}
			}
		}
		return value;
	}

	/**
	 * Method used to read the Standalone Data Storage data envelopes of an
	 * application, the ten tags '9F70' to '9F79' of EMV Contactless Book C-2
	 * (Kernel 2).
	 * <p>
	 * Section 3.5.2 of Book C-2 v2.12: "SDS uses dedicated commands (GET DATA,
	 * PUT DATA) for explicit reading and writing of data. It introduces a range
	 * of payment system tags ('9F70' to '9F79') for the reading and writing of
	 * non-payment data [...]. The whole range is freely readable using the GET
	 * DATA command." Reading needs neither a secure channel nor an
	 * authentication: the secure messaging the same section requires is for the
	 * PUT DATA command, which this library never sends.
	 * </p>
	 * <p>
	 * The extraction is skipped for any other kernel. The range '9F50' to
	 * '9F7F' is reserved for the payment systems (EMV 4.4 Book 3 Annex B) and
	 * is interpreted within the context of the application RID, so the ten tags
	 * carry another meaning elsewhere: Book C-3 (Kernel 3) has the Kernel read
	 * none of them with GET DATA, and '9F79' is the Electronic Cash Balance of
	 * the applications that implement electronic cash.
	 * </p>
	 * <p>
	 * A card that does not implement Standalone Data Storage answers a status
	 * word per tag and nothing is stored, see {@link #getData(ITag)}: the ten
	 * commands are only sent when {@code Config.readExtendedData} is turned on.
	 * </p>
	 *
	 * @param pApplication
	 *            application to fill
	 * @throws CommunicationException
	 *             communication error
	 */
	protected void extractDataEnvelopes(final Application pApplication) throws CommunicationException {
		if (pApplication == null || KernelEnum.MASTERCARD != resolveKernel(pApplication)) {
			return;
		}
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Get the Standalone Data Storage data envelopes");
		}
		DataEnvelopes envelopes = pApplication.getDataEnvelopes();
		envelopes.setProtectedDataEnvelope1(getDataEnvelope(EmvTags.PROTECTED_DATA_ENVELOPE_1, pApplication));
		envelopes.setProtectedDataEnvelope2(getDataEnvelope(EmvTags.PROTECTED_DATA_ENVELOPE_2, pApplication));
		envelopes.setProtectedDataEnvelope3(getDataEnvelope(EmvTags.PROTECTED_DATA_ENVELOPE_3, pApplication));
		envelopes.setProtectedDataEnvelope4(getDataEnvelope(EmvTags.PROTECTED_DATA_ENVELOPE_4, pApplication));
		envelopes.setProtectedDataEnvelope5(getDataEnvelope(EmvTags.PROTECTED_DATA_ENVELOPE_5, pApplication));
		envelopes.setUnprotectedDataEnvelope1(getDataEnvelope(EmvTags.UNPROTECTED_DATA_ENVELOPE_1, pApplication));
		envelopes.setUnprotectedDataEnvelope2(getDataEnvelope(EmvTags.UNPROTECTED_DATA_ENVELOPE_2, pApplication));
		envelopes.setUnprotectedDataEnvelope3(getDataEnvelope(EmvTags.UNPROTECTED_DATA_ENVELOPE_3, pApplication));
		envelopes.setUnprotectedDataEnvelope4(getDataEnvelope(EmvTags.UNPROTECTED_DATA_ENVELOPE_4, pApplication));
		// On an application that implements electronic cash the tag '9F79' is
		// the Electronic Cash Balance and holds an amount, not an envelope:
		// getOfflineBalance(Application) already read it as such and reading it
		// again as an envelope would report proprietary data the card never
		// meant.
		// The guard is reachable even though this method already returned for
		// every kernel other than Kernel 2: resolveKernel answers with the kernel
		// the card announced in its tag '9F2A', which is independent of the
		// scheme isElectronicCash derives from the AID. A UnionPay application
		// announcing Kernel 2 satisfies both, see
		// DataEnvelopesTest#testElectronicCashKeepsTheTag9F79
		if (!isElectronicCash(pApplication)) {
			envelopes.setUnprotectedDataEnvelope5(getDataEnvelope(EmvTags.UNPROTECTED_DATA_ENVELOPE_5, pApplication));
		}
	}

	/**
	 * Method used to read the Last Online ATC Register (tag '9F13'). Combined
	 * with the ATC it gives the number of offline transactions performed since
	 * the last online authorization.
	 *
	 * @return the last online ATC or {@link #UNKNOW}
	 * @throws CommunicationException
	 *             communication error
	 */
	protected int getLastOnlineTransactionCounter() throws CommunicationException {
		return getLastOnlineTransactionCounter(null);
	}

	/**
	 * Method used to read the Last Online ATC Register (tag '9F13'), recording
	 * the status word of the GET DATA on the application.
	 *
	 * @param pApplication
	 *            application read, may be null
	 * @return the last online ATC or {@link #UNKNOW}
	 * @throws CommunicationException
	 *             communication error
	 */
	protected int getLastOnlineTransactionCounter(final Application pApplication) throws CommunicationException {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Get Last Online ATC Register");
		}
		return (int) EmvDataUtils.getBinaryValue(getData(EmvTags.LAST_ONLINE_ATC_REGISTER, pApplication), UNKNOW);
	}

	/**
	 * Method used to read the balance still available for offline (electronic
	 * cash) payments.
	 * <p>
	 * The data object is proprietary to each payment system:
	 * </p>
	 * <ul>
	 * <li>'9F79' Electronic Cash Balance, used by the Chinese UnionPay (PBOC)
	 * applications. Other payment systems reuse the same tag, but only the
	 * UnionPay AIDs are recognised here, see
	 * {@link #isElectronicCash(Application)}</li>
	 * <li>'9F50' Offline Accumulator Balance, used by the M/Chip (MasterCard)
	 * kernel</li>
	 * </ul>
	 * Both are encoded as a 6 bytes packed BCD amount expressed in the minor
	 * unit of the application currency.
	 *
	 * @param pApplication
	 *            application, its currency exponent is used to scale the amount
	 * @return the offline balance or null when the card does not support
	 *         offline payments
	 * @throws CommunicationException
	 *             communication error
	 */
	protected BigDecimal getOfflineBalance(final Application pApplication) throws CommunicationException {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Get offline (electronic cash) balance");
		}
		byte[] balance = null;
		String tag = null;
		// '9F79' only holds an amount on the applications that implement
		// electronic cash, it is the generic Unprotected Data Envelope 5 of the
		// EMV Book 3 cards and reading it as money would report an amount the
		// card never meant
		if (isElectronicCash(pApplication)) {
			balance = getData(EmvTags.UNPROTECTED_DATA_ENVELOPE_5, pApplication);
			tag = hexTag(EmvTags.UNPROTECTED_DATA_ENVELOPE_5);
		}
		if (balance == null) {
			balance = getData(EmvTags.OFFLINE_ACCUMULATOR_BALANCE, pApplication);
			tag = hexTag(EmvTags.OFFLINE_ACCUMULATOR_BALANCE);
		}
		// The raw value and the tag it came from are kept even when the value
		// is not the n12 amount parseOfflineBalance accepts
		if (pApplication != null && balance != null) {
			pApplication.setRawOfflineBalance(balance);
			pApplication.setOfflineBalanceTag(tag);
		}
		return parseOfflineBalance(balance, pApplication);
	}

	/**
	 * Method used to know if the application is an electronic cash (purse)
	 * application, the only kind that exposes its balance in the tag '9F79'.
	 *
	 * @param pApplication
	 *            application read
	 * @return true if the application implements electronic cash
	 */
	protected boolean isElectronicCash(final Application pApplication) {
		if (pApplication == null || pApplication.getAid() == null) {
			return false;
		}
		// The Chinese UnionPay (PBOC) applications are the ones that define the
		// tag '9F79' as the Electronic Cash Balance
		return EmvCardScheme.UNIONPAY == EmvCardScheme.getCardTypeByAid(BytesUtils.bytesToStringNoSpace(pApplication.getAid()));
	}

	/**
	 * Method used to decode an offline balance (packed BCD amount) with the
	 * exponent of the application currency.
	 *
	 * @param pBalance
	 *            raw amount
	 * @param pApplication
	 *            application holding the currency exponent
	 * @return the decoded amount or null
	 */
	protected BigDecimal parseOfflineBalance(final byte[] pBalance, final Application pApplication) {
		// Both tags are numbered in the range reserved for the payment systems,
		// a card of another scheme may answer something that is not an amount:
		// only accept the n12 encoding the balance is specified with
		if (pBalance == null || pBalance.length != 6) {
			return null;
		}
		String value = EmvDataUtils.getNumericValue(pBalance);
		if (value == null) {
			return null;
		}
		int exponent = pApplication != null ? pApplication.getCurrencyExponent() : UNKNOW;
		if (exponent == UNKNOW || exponent < 0 || exponent > 4) {
			// ISO 4217 default, used by all the currencies but a few
			exponent = 2;
		}
		try {
			// The balance holds up to 12 digits, more than a float can carry
			return new BigDecimal(new BigInteger(value), exponent);
		} catch (NumberFormatException e) {
			LOGGER.warn("Unable to read the offline balance: {}", value);
			return null;
		}
	}

	/**
	 * Method used to get Transaction counter
	 *
	 * @return the number of card transaction
	 * @throws CommunicationException communication error
	 */
	protected int getTransactionCounter() throws CommunicationException {
		return getTransactionCounter(null);
	}

	/**
	 * Method used to get the Application Transaction Counter (tag '9F36'),
	 * recording the status word of the GET DATA on the application.
	 *
	 * @param pApplication
	 *            application read, may be null
	 * @return the number of card transaction or {@link #UNKNOW}
	 * @throws CommunicationException
	 *             communication error
	 */
	protected int getTransactionCounter(final Application pApplication) throws CommunicationException {
		int ret = UNKNOW;
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Get Transaction Counter ATC");
		}
		// Extract ATC
		ret = readCounter(getData(EmvTags.APP_TRANSACTION_COUNTER, pApplication), EmvTags.APP_TRANSACTION_COUNTER);
		return ret;
	}

	/**
	 * Method used to get the number of pin try left
	 *
	 * @return the number of pin try left
	 * @throws CommunicationException communication error
	 */
	protected int getLeftPinTry() throws CommunicationException {
		return getLeftPinTry(null);
	}

	/**
	 * Method used to get the number of pin try left (tag '9F17'), recording
	 * the status word of the GET DATA on the application.
	 *
	 * @param pApplication
	 *            application read, may be null
	 * @return the number of pin try left or {@link #UNKNOW}
	 * @throws CommunicationException
	 *             communication error
	 */
	protected int getLeftPinTry(final Application pApplication) throws CommunicationException {
		int ret = UNKNOW;
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Get Left PIN try");
		}
		// Left PIN try command, extract the PIN try counter
		ret = readCounter(getData(EmvTags.PIN_TRY_COUNTER, pApplication), EmvTags.PIN_TRY_COUNTER);
		return ret;
	}

	/**
	 * Method used to read a counter a card answered to a GET DATA.
	 * <p>
	 * A counter is a binary number of one to four bytes. A card answering an
	 * empty value, or more bytes than a counter holds, must not stop the whole
	 * reading: {@code BytesUtils.byteArrayToInt} rejects both with an
	 * {@link IllegalArgumentException}, which nothing on the path of
	 * {@link com.github.devnied.emvnfccard.parser.EmvTemplate#readEmvCard()}
	 * catches.
	 * </p>
	 *
	 * @param pValue
	 *            value the card answered, may be null
	 * @param pTag
	 *            tag read, named in the warning
	 * @return the counter or {@link #UNKNOW}
	 */
	protected static int readCounter(final byte[] pValue, final ITag pTag) {
		if (pValue == null) {
			return UNKNOW;
		}
		if (pValue.length < 1 || pValue.length > 4) {
			LOGGER.warn("Unreadable " + pTag.getName() + ", " + pValue.length + " bytes: "
					+ BytesUtils.bytesToStringNoSpace(pValue));
			return UNKNOW;
		}
		return BytesUtils.byteArrayToInt(pValue);
	}

	/**
	 * Method used to get log format
	 *
	 * @return list of tag and length for the log format
	 * @throws CommunicationException communication error
	 */
	protected List<TagAndLength> getLogFormat() throws CommunicationException {
		return getLogFormat(new TransactionLog(), null);
	}

	/**
	 * Method used to get the log format (tag '9F4F'), keeping its raw value,
	 * its parsed entries and the status word of the GET DATA on the transaction
	 * log, and the status word on the application when one is given.
	 *
	 * @param pLog
	 *            transaction log being read, may be null
	 * @param pApplication
	 *            application read, may be null
	 * @return list of tag and length for the log format, never null
	 * @throws CommunicationException
	 *             communication error
	 */
	protected List<TagAndLength> getLogFormat(final TransactionLog pLog, final Application pApplication)
			throws CommunicationException {
		List<TagAndLength> ret = new ArrayList<TagAndLength>();
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("GET log format");
		}
		// Get log format
		byte[] data = getTemplate().getProvider().transceive(new CommandApdu(CommandEnum.GET_DATA, 0x9F, 0x4F, 0).toBytes());
		String sw = statusWordOf(data);
		if (pLog != null) {
			pLog.setLogFormatStatusWord(sw);
		}
		if (pApplication != null && sw != null) {
			pApplication.getGetDataStatusWords().put(hexTag(EmvTags.LOG_FORMAT), sw);
		}
		if (ResponseUtils.isSucceedOrWarning(data)) {
			byte[] rawLogFormat = TlvUtil.getValue(ResponseUtils.getDataField(data), EmvTags.LOG_FORMAT);
			ret = TlvUtil.parseTagAndLength(rawLogFormat);
			if (pLog != null) {
				pLog.setRawLogFormat(rawLogFormat);
				pLog.setLogFormat(ret);
			}
		} else {
			LOGGER.warn("No Log format found");
		}
		return ret;
	}

	/**
	 * Method used to name the tag the Log Entry was read from: '9F4D' (Log
	 * Entry) or 'DF60' (Visa Log Entry), whichever {@link #getLogEntry(byte[])}
	 * found first in the File Control Information.
	 *
	 * @param pFci
	 *            File Control Information of the application, may be null
	 * @param pLogEntry
	 *            value the Log Entry was read as
	 * @return the hexadecimal tag or null when it cannot be told
	 */
	protected String findLogEntryTag(final byte[] pFci, final byte[] pLogEntry) {
		if (pFci == null || pLogEntry == null) {
			return null;
		}
		for (TLV tlv : TlvUtil.getlistTLV(pFci, EmvTags.LOG_ENTRY, EmvTags.VISA_LOG_ENTRY)) {
			if (tlv != null && Arrays.equals(tlv.getValueBytes(), pLogEntry)) {
				return hexTag(tlv.getTag());
			}
		}
		return null;
	}

	/**
	 * Method used to keep on a transaction the data the Log Format entries
	 * carry that the typed fields of {@link EmvTransactionRecord} lose: the
	 * numeric codes behind the enumerations (currency '5F2A', terminal country
	 * '9F1A', transaction type '9C') and the value of every entry that maps to
	 * no field ('9F36', '9F26', '9F10', '9F52', 'DF3E', '9F7C'...), see
	 * {@link EmvTransactionRecord#getOtherTags()}.
	 * <p>
	 * The record is walked entry by entry, the reading stops at the first entry
	 * the record is too short for.
	 * </p>
	 *
	 * @param pRecord
	 *            transaction to complete
	 * @param pData
	 *            data field of the log record
	 * @param pLogFormat
	 *            entries of the Log Format
	 */
	protected void fillRecordDetails(final EmvTransactionRecord pRecord, final byte[] pData,
			final List<TagAndLength> pLogFormat) {
		if (pRecord == null || pData == null || pLogFormat == null) {
			return;
		}
		int offset = 0;
		for (TagAndLength tal : pLogFormat) {
			if (tal == null || tal.getTag() == null) {
				break;
			}
			int length = tal.getLength();
			if (length < 0 || offset + length > pData.length) {
				LOGGER.debug("Log record shorter than its Log Format, the entry " + tal.getTag().getName() + " is not read");
				break;
			}
			byte[] value = Arrays.copyOfRange(pData, offset, offset + length);
			offset += length;
			String key = hexTag(tal.getTag());
			if (key == null) {
				continue;
			}
			if (key.equals(hexTag(EmvTags.TRANSACTION_CURRENCY_CODE))) {
				pRecord.setCurrencyCode(EmvDataUtils.getNumericIntValue(value, UNKNOW));
			} else if (key.equals(hexTag(EmvTags.TERMINAL_COUNTRY_CODE))) {
				pRecord.setTerminalCountryCodeNumeric(EmvDataUtils.getNumericIntValue(value, UNKNOW));
			} else if (key.equals(hexTag(EmvTags.TRANSACTION_TYPE))) {
				if (value.length > 0) {
					pRecord.setTransactionTypeCode(value[0] & 0xFF);
				}
			} else if (key.equals(hexTag(EmvTags.AMOUNT_AUTHORISED_NUMERIC))) {
				// The amount as decoded, before the Visa artefact is removed
				pRecord.setOriginalAmount(pRecord.getAmount());
			} else if (!key.equals(hexTag(EmvTags.CRYPTOGRAM_INFORMATION_DATA))
					&& !key.equals(hexTag(EmvTags.TRANSACTION_DATE)) && !key.equals(hexTag(EmvTags.TRANSACTION_TIME))) {
				// Every entry the bean has no field for, first occurrence kept
				if (!pRecord.getOtherTags().containsKey(key)) {
					pRecord.getOtherTags().put(key, value);
				}
			}
		}
	}

	/**
	 * Method used to extract log entry from card
	 *
	 * @param pLogEntry
	 *            log entry position
	 * @return list of transaction records
	 * @throws CommunicationException communication error
	 */
	protected List<EmvTransactionRecord> extractLogEntry(final byte[] pLogEntry) throws CommunicationException {
		return extractLogEntry(pLogEntry, null);
	}

	/**
	 * Method used to extract log entry from card, keeping every detail of the
	 * reading on the application, see {@link Application#getTransactionLog()}:
	 * the Log Entry and the tag it came from, the Log Format and its status
	 * word, every READ RECORD sent with its status word, the records left out
	 * because their amount is empty, the records that could not be parsed.
	 * <p>
	 * The list returned is the same as the one of
	 * {@link #extractLogEntry(byte[])}.
	 * </p>
	 *
	 * @param pLogEntry
	 *            log entry position
	 * @param pApplication
	 *            application read, may be null
	 * @return list of transaction records
	 * @throws CommunicationException
	 *             communication error
	 */
	protected List<EmvTransactionRecord> extractLogEntry(final byte[] pLogEntry, final Application pApplication)
			throws CommunicationException {
		List<EmvTransactionRecord> listRecord = new ArrayList<EmvTransactionRecord>();
		TransactionLog log = new TransactionLog();
		log.setRawLogEntry(pLogEntry);
		if (pApplication != null) {
			pApplication.setTransactionLog(log);
			log.setLogEntryTag(findLogEntryTag(pApplication.getRawFci(), pLogEntry));
		}
		if (pLogEntry != null && pLogEntry.length >= LOG_ENTRY_SIZE) {
			log.setSfi(pLogEntry[0] & 0xFF);
			log.setDeclaredRecordCount(pLogEntry[1] & 0xFF);
		}
		// The Log Entry is 2 bytes long: the SFI of the transaction log file
		// then its number of records (EMV 4.3 Book 3 Annex D4). A card sending
		// a shorter value only means that its transaction log cannot be read,
		// the specification asks the terminal to ignore the formatting error
		// of the tag '9F4D' and to carry on (EMV 4.3 Book 3 section 7.5)
		if (pLogEntry != null && pLogEntry.length < LOG_ENTRY_SIZE) {
			LOGGER.warn("Malformed Log Entry, the transaction log is not read: " + BytesUtils.bytesToStringNoSpace(pLogEntry));
			return listRecord;
		}
		// If log entry is defined
		if (getTemplate().getConfig().readTransactions && pLogEntry != null) {
			List<TagAndLength> tals = getLogFormat(log, pApplication);
			if (tals != null && !tals.isEmpty()) {
				int sfi = pLogEntry[0] & 0xFF;
				int records = pLogEntry[1] & 0xFF;
				// A Short File Identifier is coded on 5 bits (EMV 4.3 Book 3
				// section 10.2, and Annex D4 restricts the log file to 11..30):
				// a larger one would be truncated into the P2 byte and address
				// another file
				// EMV 4.4 Book 3 Annex D4 states it outright: "The SFI shall be
				// in the range 11 to 30". The files 1 to 10 are the ones the
				// specification itself governs, reading one of them as a
				// transaction log turns ordinary application data into
				// transactions that never happened
				if (sfi < MIN_LOG_SFI || sfi > MAX_SFI) {
					LOGGER.warn("Invalid SFI in the Log Entry, the transaction log is not read: " + sfi);
					return listRecord;
				}
				log.setRead(true);
				// read all records
				for (int rec = 1; rec <= records; rec++) {
					byte[] response = getTemplate().getProvider()
							.transceive(new CommandApdu(CommandEnum.READ_RECORD, rec, sfi << 3 | 4, 0).toBytes());
					// A transaction log record is not BER-TLV encoded, it
					// follows the Log Format: the status bytes are not part
					// of it either
					byte[] dataField = ResponseUtils.getDataField(response);
					// Every exchange is kept, the one that ends the loop too
					log.getRecords().add(new RecordData(sfi, rec, dataField, statusWordOf(response)));
					// Extract data
					if (ResponseUtils.isSucceedOrWarning(response)) {
						try {
							EmvTransactionRecord record = new EmvTransactionRecord();
							record.parse(dataField, tals);
							record.setRaw(dataField);
							record.setRecordNumber(rec);
							fillRecordDetails(record, dataField, tals);

							if (record.getAmount() != null) {
								// The amount as decoded, before any correction
								record.setOriginalAmount(record.getAmount());
								// Fix artifact in EMV VISA card
								if (record.getAmount() >= VISA_AMOUNT_ARTEFACT) {
									record.setAmount(record.getAmount() - VISA_AMOUNT_ARTEFACT);
								}

								// Skip transaction with null amount: an empty
								// log slot, kept apart from the transactions
								if (record.getAmount() <= 1) {
									if (record.getCurrency() == null) {
										record.setCurrency(CurrencyEnum.XXX);
									}
									log.getSkippedTransactions().add(record);
									continue;
								}
							}

							// Unknown currency
							if (record.getCurrency() == null) {
								record.setCurrency(CurrencyEnum.XXX);
							}
							listRecord.add(record);
						} catch (Exception e) {
							LOGGER.error("Error in transaction format: " + e.getMessage(), e);
							log.getUnparsedRecords().add(dataField);
						}
					} else {
						// No more transaction log or transaction disabled
						break;
					}
				}
			}
		}
		return listRecord;
	}

}
