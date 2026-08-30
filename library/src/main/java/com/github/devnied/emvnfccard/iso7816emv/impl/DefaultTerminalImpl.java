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
package com.github.devnied.emvnfccard.iso7816emv.impl;

import java.security.SecureRandom;
import java.util.Arrays;
import java.text.SimpleDateFormat;
import java.util.Currency;
import java.util.Date;
import java.util.Locale;

import com.github.devnied.emvnfccard.enums.TagValueTypeEnum;
import com.github.devnied.emvnfccard.iso7816emv.EmvTags;
import com.github.devnied.emvnfccard.iso7816emv.ITerminal;
import com.github.devnied.emvnfccard.iso7816emv.TagAndLength;
import com.github.devnied.emvnfccard.iso7816emv.TerminalTransactionQualifiers;
import com.github.devnied.emvnfccard.model.enums.CountryCodeEnum;
import com.github.devnied.emvnfccard.model.enums.CurrencyEnum;
import com.github.devnied.emvnfccard.model.enums.TransactionTypeEnum;

import fr.devnied.bitlib.BytesUtils;

/**
 * Factory to create default terminal implementation
 * <p>
 * The terminal country code and the transaction currency it advertises have a
 * real impact on what a card accepts to return: a lot of domestic applications
 * (Interac in Canada, girocard in Germany, UnionPay in China, ...) refuse to
 * process a transaction, or return a reduced set of data, when the terminal
 * does not look like a domestic one. Use {@link #setCountryCode(CountryCodeEnum)}
 * or {@link #DefaultTerminalImpl(CountryCodeEnum)} to read such a card.
 * </p>
 *
 * @author Millau Julien
 *
 */
public final class DefaultTerminalImpl implements ITerminal {

	/**
	 * Random
	 */
	private static final SecureRandom random = new SecureRandom();

	/**
	 * Number of decimals used when the currency is unknown to the runtime, it
	 * is the one of most of the ISO 4217 currencies
	 */
	private static final int DEFAULT_CURRENCY_EXPONENT = 2;

	/**
	 * Country code
	 */
	private CountryCodeEnum countryCode = CountryCodeEnum.FR;

	/**
	 * Default constructor, the terminal is located in France
	 */
	public DefaultTerminalImpl() {
	}

	/**
	 * Constructor using the country the terminal is located in
	 *
	 * @param pCountryCode
	 *            country of the terminal, the transaction currency is derived
	 *            from it
	 */
	public DefaultTerminalImpl(final CountryCodeEnum pCountryCode) {
		setCountryCode(pCountryCode);
	}

	/**
	 * Method used to construct value from tag and length
	 *
	 * @param pTagAndLength
	 *            tag and length value
	 * @return tag value in byte
	 */
	@Override
	public byte[] constructValue(final TagAndLength pTagAndLength) {
		// The length of a data object list entry is a single byte (EMV 4.3
		// Book 3 section 5.4), which TlvUtil reads as such, so it is already
		// bounded to 255 before reaching here
		byte ret[] = new byte[Math.max(pTagAndLength.getLength(), 0)];
		byte val[] = null;
		if (pTagAndLength.getTag() == EmvTags.TERMINAL_TRANSACTION_QUALIFIERS) {
			TerminalTransactionQualifiers terminalQual = new TerminalTransactionQualifiers();
			// Byte 1 bit 7 (contactless VSDC of the legacy VCPS) is reserved
			// for future use and must be left to 0: a reader that claims it
			// together with the qVSDC support of bit 6 is not conforming, and
			// the card is free to fall back on a flow this library does not
			// implement
			terminalQual.setContactEMVsupported(true);
			terminalQual.setMagneticStripeSupported(true);
			terminalQual.setContactlessEMVmodeSupported(true);
			terminalQual.setOnlinePINsupported(true);
			terminalQual.setReaderIsOfflineOnly(false);
			terminalQual.setSignatureSupported(true);
			terminalQual.setContactChipOfflinePINsupported(true);
			terminalQual.setIssuerUpdateProcessingSupported(true);
			terminalQual.setConsumerDeviceCVMsupported(true);
			val = terminalQual.getBytes();
		} else if (pTagAndLength.getTag() == EmvTags.TERMINAL_COUNTRY_CODE) {
			// The country code is a n3 encoded on 2 bytes, the length the DOL
			// asks for is applied by copyWithDolPadding: building the value on
			// that length would leave an odd number of digits and be rejected
			val = numeric(countryCode.getNumeric());
		} else if (pTagAndLength.getTag() == EmvTags.TRANSACTION_CURRENCY_CODE) {
			val = numeric(getCurrency().getISOCodeNumeric());
		} else if (pTagAndLength.getTag() == EmvTags.TRANSACTION_CURRENCY_EXP) {
			val = new byte[] { (byte) getCurrencyExponent() };
		} else if (pTagAndLength.getTag() == EmvTags.TRANSACTION_DATE) {
			// The locale is forced: the default one of the device may use
			// another calendar (Buddhist, Japanese imperial) or another set of
			// digits, and the card expects the Gregorian date in BCD
			SimpleDateFormat sdf = new SimpleDateFormat("yyMMdd", Locale.ENGLISH);
			val = BytesUtils.fromString(sdf.format(new Date()));
		} else if (pTagAndLength.getTag() == EmvTags.TRANSACTION_TIME) {
			SimpleDateFormat sdf = new SimpleDateFormat("HHmmss", Locale.ENGLISH);
			val = BytesUtils.fromString(sdf.format(new Date()));
		} else if (pTagAndLength.getTag() == EmvTags.TRANSACTION_TYPE || pTagAndLength.getTag() == EmvTags.TERMINAL_TRANSACTION_TYPE) {
			val = new byte[] { (byte) TransactionTypeEnum.PURCHASE.getKey() };
		} else if (pTagAndLength.getTag() == EmvTags.AMOUNT_AUTHORISED_NUMERIC) {
			val = BytesUtils.fromString("01");
		} else if (pTagAndLength.getTag() == EmvTags.TERMINAL_TYPE) {
			// '22' = attended merchant terminal, offline with online
			// capability (EMV 4.3 Book 4 Annex A1)
			val = new byte[] { 0x22 };
		} else if (pTagAndLength.getTag() == EmvTags.TERMINAL_CAPABILITIES) {
			// EMV 4.3 Book 4 Annex A2:
			// byte 1 'E0' : manual key entry, magnetic stripe, IC with contacts
			// byte 2 'E0' : plaintext PIN for ICC verification, enciphered PIN
			// for online verification, signature. The Terminal Transaction
			// Qualifiers built above announce online PIN, the two have to agree
			// byte 3 'C0' : SDA and DDA. A terminal that is not online only
			// (here the type is '22') "shall be capable of performing SDA,
			// DDA" (Book 4 section 6.3.2), declaring '00' contradicts the
			// terminal type
			val = new byte[] { (byte) 0xE0, (byte) 0xE0, (byte) 0xC0 };
		} else if (pTagAndLength.getTag() == EmvTags.ADDITIONAL_TERMINAL_CAPABILITIES) {
			// EMV 4.3 Book 4 Annex A3:
			// byte 1 '60' : goods and services, the transaction type sent in
			// the tag '9C' is a purchase
			// byte 2 '00' : no cash deposit (the remaining bits are RFU)
			// byte 3 'B0' : numeric, command and function keys
			// byte 4 'B3' : print and display for the attendant, display for
			// the cardholder, code tables 10 and 9. The "print or electronic,
			// attendant" bit is required as soon as the terminal declares the
			// signature in its Terminal Capabilities (Book 4 Annex A2)
			// byte 4 'B1' : the code table 9 is declared, the code table 10 is
			// not, because the runtime does not provide ISO/IEC 8859-10 and
			// the parser would fall back on the common character set
			// byte 5 'FF' : code tables 8 to 1
			val = new byte[] { 0x60, 0x00, (byte) 0xB0, (byte) 0xB1, (byte) 0xFF };
		} else if (pTagAndLength.getTag() == EmvTags.DS_REQUESTED_OPERATOR_ID) {
			val = BytesUtils.fromString("7A45123EE59C7F40");
		} else if (pTagAndLength.getTag() == EmvTags.UNPREDICTABLE_NUMBER) {
			random.nextBytes(ret);
		} else if (pTagAndLength.getTag() == EmvTags.MERCHANT_TYPE_INDICATOR) {
			val = new byte[] { 0x01 };
		} else if (pTagAndLength.getTag() == EmvTags.TERMINAL_TRANSACTION_INFORMATION) {
			val = new byte[] { (byte) 0xC0, (byte) 0x80, 0 };
		} else if (pTagAndLength.getTag() == EmvTags.APP_VERSION_NUMBER_TERMINAL) {
			// '008C' is the Visa application version. The Application Version
			// Number is assigned by each payment system and configured per
			// AID, it is only informational here since the library never runs
			// a GENERATE AC
			val = new byte[] { 0x00, (byte) 0x8C };
		} else if (pTagAndLength.getTag() == EmvTags.POINT_OF_SERVICE_ENTRY_MODE) {
			// 07 = contactless integrated circuit card
			val = new byte[] { 0x07 };
		} else if (pTagAndLength.getTag() == EmvTags.MERCHANT_CATEGORY_CODE) {
			// 5999 = miscellaneous and specialty retail store
			val = BytesUtils.fromString("5999");
		} else if (pTagAndLength.getTag() == EmvTags.TRANSACTION_CATEGORY_CODE) {
			val = new byte[] { 0x00 };
		} else if (pTagAndLength.getTag() == EmvTags.TERMINAL_IDENTIFICATION
				|| pTagAndLength.getTag() == EmvTags.INTERFACE_DEVICE_SERIAL_NUMBER) {
			// Both are ASCII, a terminal that has no identity fills them with
			// the character '0'
			Arrays.fill(ret, (byte) 0x30);
		} else if (pTagAndLength.getTag() == EmvTags.TRANSACTION_SEQUENCE_COUNTER) {
			val = new byte[] { 0x00, 0x01 };
		}
		if (val != null) {
			copyWithDolPadding(val, ret, pTagAndLength);
		}
		return ret;
	}

	/**
	 * Copy the value of a data object into the buffer the DOL asks for,
	 * applying the padding rules of EMV 4.3 Book 3 section 5.4:
	 * <ul>
	 * <li>a numeric (format n) value is right justified, padded with
	 * hexadecimal zeroes on the left, and its leftmost digits are truncated
	 * when it is too long</li>
	 * <li>any other value is left justified, padded with hexadecimal zeroes on
	 * the right, and its rightmost bytes are truncated when it is too long</li>
	 * </ul>
	 *
	 * @param pValue
	 *            value of the data object
	 * @param pBuffer
	 *            buffer of the length the DOL asks for
	 * @param pTagAndLength
	 *            tag and length requested by the card
	 */
	private static void copyWithDolPadding(final byte[] pValue, final byte[] pBuffer, final TagAndLength pTagAndLength) {
		int length = Math.min(pValue.length, pBuffer.length);
		if (length == 0) {
			return;
		}
		if (pTagAndLength.getTag().getTagValueType() == TagValueTypeEnum.NUMERIC) {
			// Keep the rightmost bytes of the value, right justified
			System.arraycopy(pValue, pValue.length - length, pBuffer, pBuffer.length - length, length);
		} else {
			// Keep the leftmost bytes of the value, left justified
			System.arraycopy(pValue, 0, pBuffer, 0, length);
		}
	}

	/**
	 * Method used to encode a numeric (format n) value on a whole number of
	 * bytes, the packed BCD encoding every data element of that format uses.
	 *
	 * @param pValue
	 *            value to encode
	 * @return the BCD encoded value
	 */
	private static byte[] numeric(final int pValue) {
		String digits = String.valueOf(pValue);
		if (digits.length() % 2 != 0) {
			digits = "0" + digits;
		}
		return BytesUtils.fromString(digits);
	}

	/**
	 * Method used to get the currency the terminal advertises, it is derived
	 * from the terminal country.
	 *
	 * @return the transaction currency
	 */
	public CurrencyEnum getCurrency() {
		return CurrencyEnum.find(countryCode, CurrencyEnum.EUR);
	}

	/**
	 * Method used to get the exponent of the transaction currency, sent in the
	 * tag '5F36' (Transaction Currency Exponent), which "indicates the implied
	 * position of the decimal point from the right of the transaction amount
	 * represented according to ISO 4217".
	 * <p>
	 * Most currencies have 2 decimals, but not all of them: the yen and the
	 * won have none, the dinars have 3.
	 * </p>
	 *
	 * @return the number of decimals of the transaction currency
	 */
	public int getCurrencyExponent() {
		try {
			// The ISO 4217 minor units are maintained by the runtime, the
			// currency list of the library only holds a display format
			int digits = Currency.getInstance(getCurrency().name()).getDefaultFractionDigits();
			if (digits >= 0) {
				return digits;
			}
		} catch (IllegalArgumentException e) {
			// The runtime does not know this currency code (a withdrawn code,
			// an Android version with an older ISO 4217 table, ...)
		}
		return DEFAULT_CURRENCY_EXPONENT;
	}

	/**
	 * Method used to get the field countryCode
	 *
	 * @return the countryCode
	 */
	public CountryCodeEnum getCountryCode() {
		return countryCode;
	}

	/**
	 * Setter for the field countryCode
	 *
	 * @param countryCode
	 *            the countryCode to set
	 */
	public void setCountryCode(final CountryCodeEnum countryCode) {
		if (countryCode != null) {
			this.countryCode = countryCode;
		}
	}

	@Override
	public String toString() {
		return String.format(Locale.ENGLISH, "DefaultTerminalImpl[country=%s, currency=%s]", countryCode, getCurrency());
	}

}
