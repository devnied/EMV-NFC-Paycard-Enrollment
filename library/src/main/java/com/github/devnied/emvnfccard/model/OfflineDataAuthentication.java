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

import java.util.Arrays;

/**
 * Credentials the card was personalised with for the offline data
 * authentication of EMV 4.3 Book 2.
 * <p>
 * <b>Nothing here is verified.</b> The library never performs a cryptographic
 * operation and never asks the card to sign anything: it reports which
 * certificates the card carries, under which certification authority key it
 * was signed, and the sizes of the keys involved. Verifying a signature needs
 * the certification authority public keys, which the payment systems do not
 * publish.
 * </p>
 * <p>
 * The lengths are read from the certificates themselves: EMV 4.3 Book 2 makes
 * the Issuer Public Key Certificate exactly as long as the certification
 * authority key modulus it was signed with, and the ICC Public Key Certificate
 * exactly as long as the issuer key modulus.
 * </p>
 *
 * @author MILLAU Julien
 *
 */
public class OfflineDataAuthentication extends AbstractData {

	/**
	 * Generated serial UID
	 */
	private static final long serialVersionUID = -4162414310355131169L;

	/**
	 * Certification Authority Public Key Index (tag '8F')
	 */
	private int certificationAuthorityPublicKeyIndex = UNKNOWN;

	/**
	 * Issuer Public Key Certificate (tag '90')
	 */
	private byte[] issuerPublicKeyCertificate;

	/**
	 * Issuer Public Key Remainder (tag '92')
	 */
	private byte[] issuerPublicKeyRemainder;

	/**
	 * Issuer Public Key Exponent (tag '9F32')
	 */
	private byte[] issuerPublicKeyExponent;

	/**
	 * Signed Static Application Data (tag '93'), the SDA signature
	 */
	private byte[] signedStaticApplicationData;

	/**
	 * ICC Public Key Certificate (tag '9F46')
	 */
	private byte[] iccPublicKeyCertificate;

	/**
	 * ICC Public Key Exponent (tag '9F47')
	 */
	private byte[] iccPublicKeyExponent;

	/**
	 * ICC Public Key Remainder (tag '9F48')
	 */
	private byte[] iccPublicKeyRemainder;

	/**
	 * Static Data Authentication Tag List (tag '9F4A')
	 */
	private byte[] staticDataAuthenticationTagList;

	/**
	 * Signed Dynamic Application Data (tag '9F4B')
	 */
	private byte[] signedDynamicApplicationData;

	/**
	 * ICC PIN Encipherment Public Key Certificate (tag '9F2D')
	 */
	private byte[] iccPinEnciphermentPublicKeyCertificate;

	/**
	 * ICC PIN Encipherment Public Key Exponent (tag '9F2E')
	 */
	private byte[] iccPinEnciphermentPublicKeyExponent;

	/**
	 * ICC PIN Encipherment Public Key Remainder (tag '9F2F')
	 */
	private byte[] iccPinEnciphermentPublicKeyRemainder;

	/**
	 * Method used to know if the card carries at least one authentication
	 * credential
	 *
	 * @return true if something was personalised for the offline data
	 *         authentication
	 */
	public boolean isPresent() {
		return certificationAuthorityPublicKeyIndex != UNKNOWN || issuerPublicKeyCertificate != null
				|| signedStaticApplicationData != null || iccPublicKeyCertificate != null;
	}

	/**
	 * Method used to know if the card is personalised for the Static Data
	 * Authentication, which needs the Signed Static Application Data.
	 *
	 * @return true if SDA can be performed by a terminal holding the keys
	 */
	public boolean isStaticAuthenticationPersonalised() {
		return signedStaticApplicationData != null;
	}

	/**
	 * Method used to know if the card holds its own key pair, which the Dynamic
	 * Data Authentication and the Combined Data Authentication both need.
	 *
	 * @return true if the card carries an ICC public key certificate and its
	 *         exponent
	 */
	public boolean isDynamicAuthenticationPersonalised() {
		return iccPublicKeyCertificate != null && iccPublicKeyExponent != null;
	}

	/**
	 * Length in bytes of the certification authority public key modulus the
	 * card was signed under, read from the length of the Issuer Public Key
	 * Certificate.
	 *
	 * @return the length in bytes, or {@link #UNKNOWN}
	 */
	public int getCertificationAuthorityKeyLength() {
		return issuerPublicKeyCertificate != null ? issuerPublicKeyCertificate.length : UNKNOWN;
	}

	/**
	 * Length in bytes of the issuer public key modulus, read from the length of
	 * the ICC Public Key Certificate, or from the Signed Static Application
	 * Data on a card personalised for the static authentication only: EMV 4.3
	 * Book 2 makes both of them exactly as long as that modulus.
	 *
	 * @return the length in bytes, or {@link #UNKNOWN}
	 */
	public int getIssuerKeyLength() {
		if (iccPublicKeyCertificate != null) {
			return iccPublicKeyCertificate.length;
		}
		return signedStaticApplicationData != null ? signedStaticApplicationData.length : UNKNOWN;
	}

	public int getCertificationAuthorityPublicKeyIndex() {
		return certificationAuthorityPublicKeyIndex;
	}

	public void setCertificationAuthorityPublicKeyIndex(final int pIndex) {
		certificationAuthorityPublicKeyIndex = pIndex;
	}

	public byte[] getIssuerPublicKeyCertificate() {
		return copy(issuerPublicKeyCertificate);
	}

	public void setIssuerPublicKeyCertificate(final byte[] pValue) {
		issuerPublicKeyCertificate = copy(pValue);
	}

	public byte[] getIssuerPublicKeyRemainder() {
		return copy(issuerPublicKeyRemainder);
	}

	public void setIssuerPublicKeyRemainder(final byte[] pValue) {
		issuerPublicKeyRemainder = copy(pValue);
	}

	public byte[] getIssuerPublicKeyExponent() {
		return copy(issuerPublicKeyExponent);
	}

	public void setIssuerPublicKeyExponent(final byte[] pValue) {
		issuerPublicKeyExponent = copy(pValue);
	}

	public byte[] getSignedStaticApplicationData() {
		return copy(signedStaticApplicationData);
	}

	public void setSignedStaticApplicationData(final byte[] pValue) {
		signedStaticApplicationData = copy(pValue);
	}

	public byte[] getIccPublicKeyCertificate() {
		return copy(iccPublicKeyCertificate);
	}

	public void setIccPublicKeyCertificate(final byte[] pValue) {
		iccPublicKeyCertificate = copy(pValue);
	}

	public byte[] getIccPublicKeyExponent() {
		return copy(iccPublicKeyExponent);
	}

	public void setIccPublicKeyExponent(final byte[] pValue) {
		iccPublicKeyExponent = copy(pValue);
	}

	public byte[] getIccPublicKeyRemainder() {
		return copy(iccPublicKeyRemainder);
	}

	public void setIccPublicKeyRemainder(final byte[] pValue) {
		iccPublicKeyRemainder = copy(pValue);
	}

	public byte[] getStaticDataAuthenticationTagList() {
		return copy(staticDataAuthenticationTagList);
	}

	public void setStaticDataAuthenticationTagList(final byte[] pValue) {
		staticDataAuthenticationTagList = copy(pValue);
	}

	public byte[] getSignedDynamicApplicationData() {
		return copy(signedDynamicApplicationData);
	}

	public void setSignedDynamicApplicationData(final byte[] pValue) {
		signedDynamicApplicationData = copy(pValue);
	}

	public byte[] getIccPinEnciphermentPublicKeyCertificate() {
		return copy(iccPinEnciphermentPublicKeyCertificate);
	}

	public void setIccPinEnciphermentPublicKeyCertificate(final byte[] pValue) {
		iccPinEnciphermentPublicKeyCertificate = copy(pValue);
	}

	public byte[] getIccPinEnciphermentPublicKeyExponent() {
		return copy(iccPinEnciphermentPublicKeyExponent);
	}

	public void setIccPinEnciphermentPublicKeyExponent(final byte[] pValue) {
		iccPinEnciphermentPublicKeyExponent = copy(pValue);
	}

	public byte[] getIccPinEnciphermentPublicKeyRemainder() {
		return copy(iccPinEnciphermentPublicKeyRemainder);
	}

	public void setIccPinEnciphermentPublicKeyRemainder(final byte[] pValue) {
		iccPinEnciphermentPublicKeyRemainder = copy(pValue);
	}

	/**
	 * The certificates are large, they are never handed out by reference
	 *
	 * @param pValue
	 *            value to copy
	 * @return a copy of the value, or null
	 */
	private static byte[] copy(final byte[] pValue) {
		return pValue != null ? Arrays.copyOf(pValue, pValue.length) : null;
	}

}
