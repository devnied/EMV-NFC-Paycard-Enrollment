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

import fr.devnied.bitlib.BytesUtils;

/**
 * Application Interchange Profile (tag '82').<br>
 * Indicates the capabilities of the card to support specific functions in the
 * application (EMV 4.3 Book 3, Annex C1).
 *
 * @author MILLAU Julien
 *
 */
public class ApplicationInterchangeProfile extends AbstractData {

	/**
	 * Generated serial UID
	 */
	private static final long serialVersionUID = 5077495861395930041L;

	/**
	 * Raw AIP value
	 */
	private final byte[] raw;

	/**
	 * SDA supported
	 */
	private final boolean sdaSupported;

	/**
	 * DDA supported
	 */
	private final boolean ddaSupported;

	/**
	 * Cardholder verification is supported
	 */
	private final boolean cardholderVerificationSupported;

	/**
	 * Terminal risk management is to be performed
	 */
	private final boolean terminalRiskManagementToBePerformed;

	/**
	 * Issuer authentication is supported
	 */
	private final boolean issuerAuthenticationSupported;

	/**
	 * On device cardholder verification is supported (CDCVM)
	 */
	private final boolean onDeviceCardholderVerificationSupported;

	/**
	 * CDA supported
	 */
	private final boolean cdaSupported;

	/**
	 * EMV (contact/contactless) mode supported.<br>
	 * Byte 2 bit 8 is reserved for the contactless specifications, so its
	 * meaning belongs to the kernel the application is processed on.
	 */
	private final boolean emvModeSupported;

	/**
	 * XDA supported, the Extended Data Authentication introduced by EMV 4.4
	 * (Book 3 Annex C1). The bit was reserved for future use in EMV 4.3.
	 */
	private final boolean xdaSupported;

	/**
	 * Relay resistance protocol supported.<br>
	 * Byte 2 of the profile belongs to the contactless specifications: this is
	 * the reading of the MasterCard kernel (EMV Contactless Book C-2, Annex
	 * A.1.16), another kernel may use the bit differently.
	 */
	private final boolean relayResistanceSupported;

	/**
	 * Constructor using the 2 bytes AIP value
	 *
	 * @param pRaw
	 *            raw AIP value (2 bytes)
	 */
	public ApplicationInterchangeProfile(final byte[] pRaw) {
		if (pRaw == null || pRaw.length < 2) {
			throw new IllegalArgumentException("Application Interchange Profile must be 2 bytes long");
		}
		raw = new byte[] { pRaw[0], pRaw[1] };
		sdaSupported = BytesUtils.matchBitByBitIndex(raw[0], 6);
		ddaSupported = BytesUtils.matchBitByBitIndex(raw[0], 5);
		cardholderVerificationSupported = BytesUtils.matchBitByBitIndex(raw[0], 4);
		terminalRiskManagementToBePerformed = BytesUtils.matchBitByBitIndex(raw[0], 3);
		issuerAuthenticationSupported = BytesUtils.matchBitByBitIndex(raw[0], 2);
		onDeviceCardholderVerificationSupported = BytesUtils.matchBitByBitIndex(raw[0], 1);
		cdaSupported = BytesUtils.matchBitByBitIndex(raw[0], 0);
		xdaSupported = BytesUtils.matchBitByBitIndex(raw[0], 7);
		emvModeSupported = BytesUtils.matchBitByBitIndex(raw[1], 7);
		relayResistanceSupported = BytesUtils.matchBitByBitIndex(raw[1], 0);
	}

	/**
	 * Indicate if the Extended Data Authentication of EMV 4.4 is supported.
	 *
	 * @return true if XDA is supported
	 */
	public boolean isXdaSupported() {
		return xdaSupported;
	}

	/**
	 * Indicate if the card supports the relay resistance protocol, which
	 * measures the round trip time of an exchange to detect a relayed
	 * transaction.<br>
	 * The library reports the capability and never runs the protocol: it
	 * requires the card to generate and sign a nonce.
	 *
	 * @return true if the relay resistance protocol is supported
	 */
	public boolean isRelayResistanceSupported() {
		return relayResistanceSupported;
	}

	/**
	 * Get the field raw
	 *
	 * @return the raw AIP value
	 */
	public byte[] getRaw() {
		return new byte[] { raw[0], raw[1] };
	}

	/**
	 * Indicate if Static Data Authentication is supported
	 *
	 * @return true if SDA is supported
	 */
	public boolean isSdaSupported() {
		return sdaSupported;
	}

	/**
	 * Indicate if Dynamic Data Authentication is supported
	 *
	 * @return true if DDA is supported
	 */
	public boolean isDdaSupported() {
		return ddaSupported;
	}

	/**
	 * Indicate if cardholder verification is supported
	 *
	 * @return true if cardholder verification is supported
	 */
	public boolean isCardholderVerificationSupported() {
		return cardholderVerificationSupported;
	}

	/**
	 * Indicate if terminal risk management is to be performed
	 *
	 * @return true if terminal risk management is to be performed
	 */
	public boolean isTerminalRiskManagementToBePerformed() {
		return terminalRiskManagementToBePerformed;
	}

	/**
	 * Indicate if issuer authentication is supported
	 *
	 * @return true if issuer authentication is supported
	 */
	public boolean isIssuerAuthenticationSupported() {
		return issuerAuthenticationSupported;
	}

	/**
	 * Indicate if on device cardholder verification (CDCVM) is supported
	 *
	 * @return true if on device cardholder verification is supported
	 * <p>
	 * Byte 1 bit 2 is "Reserved for use by the EMV Contactless Specifications"
	 * for EMV 4.4 Book 3 Annex C1: this is the reading of the Kernel 2 (EMV
	 * Contactless Book C-2 Annex A.1.16). The Kernel 3 leaves the same bit
	 * reserved, so the answer only means something on a Mastercard
	 * application, like {@link #isEmvModeSupported()} and
	 * {@link #isRelayResistanceSupported()}.
	 * </p>
	 */
	public boolean isOnDeviceCardholderVerificationSupported() {
		return onDeviceCardholderVerificationSupported;
	}

	/**
	 * Indicate if Combined DDA/Application cryptogram generation is supported
	 *
	 * @return true if CDA is supported
	 */
	public boolean isCdaSupported() {
		return cdaSupported;
	}

	/**
	 * Indicate if EMV mode is supported
	 *
	 * @return true if EMV mode is supported
	 */
	public boolean isEmvModeSupported() {
		return emvModeSupported;
	}

}
