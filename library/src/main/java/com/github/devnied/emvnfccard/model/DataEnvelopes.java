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
import java.util.ArrayList;
import java.util.List;

/**
 * Standalone Data Storage of EMV Contactless Book C-2 (Kernel 2): the ten data
 * envelopes a card holds beside its payment data, and the only card resident
 * data objects a terminal reads with the GET DATA command on that kernel.
 * <p>
 * Book C-2 v2.12 section 3.5.2 "Standalone Data Storage": "SDS uses dedicated
 * commands (GET DATA, PUT DATA) for explicit reading and writing of data. It
 * introduces a range of payment system tags ('9F70' to '9F79') for the reading
 * and writing of non-payment data, so that they can be included in Tags To
 * Read, Tags To Write Before Gen AC, or Tags To Write After Gen AC. The whole
 * range is freely readable using the GET DATA command. Writing is done using a
 * PUT DATA command without secure messaging, for tags '9F75' to '9F79'. Writing
 * to the tags '9F70' to '9F74' requires secure messaging and is outside the
 * scope of this specification. The length of the data is variable. The maximum
 * length is implementation specific, and is between 32 and 192 bytes."
 * </p>
 * <p>
 * The word "Protected" therefore qualifies the <b>writing</b> only: the whole
 * range is read without any authentication, without any secure channel and
 * without the cardholder verification. This library only reads, so the
 * asymmetry costs it nothing: the PUT DATA command that the second half of the
 * quotation describes is never sent.
 * </p>
 * <p>
 * The content is opaque on purpose. Book C-2 v2.12 A.1.107: "The Protected Data
 * Envelopes contain proprietary information from the issuer, payment system or
 * third party", and A.1.159 says the same of the unprotected ones. No public
 * specification states how to decode a byte of it, so each envelope is exposed
 * exactly as the card sent it.
 * </p>
 * <p>
 * The envelopes are read only for an application whose kernel is Kernel 2. The
 * range '9F50' to '9F7F' is reserved for the payment systems (EMV 4.4 Book 3
 * Annex B), so the same tags mean something else on another kernel: '9F79' for
 * instance is the Electronic Cash Balance of the UnionPay (PBOC) applications,
 * which {@link com.github.devnied.emvnfccard.parser.impl.AbstractParser}
 * decodes as an amount instead.
 * </p>
 *
 * @author MILLAU Julien
 *
 */
public class DataEnvelopes extends AbstractData {

	/**
	 * Generated serial UID
	 */
	private static final long serialVersionUID = 6113045897354711683L;

	/**
	 * Highest length an envelope may reach.<br>
	 * Book C-2 v2.12 A.1.107 gives the length of a data envelope as "var. up to
	 * 192" and section 3.5.2 adds that "The maximum length is implementation
	 * specific, and is between 32 and 192 bytes": a card answering more than
	 * this is answering something that is not an envelope.
	 */
	public static final int MAX_LENGTH = 192;

	/**
	 * Protected Data Envelope 1 (tag '9F70'), raw value.<br>
	 * Book C-2 v2.12 A.1.107: "The Protected Data Envelopes contain proprietary
	 * information from the issuer, payment system or third party. The Protected
	 * Data Envelope can be retrieved with the GET DATA command. Updating the
	 * Protected Data Envelope with the PUT DATA command requires secure
	 * messaging and is outside the scope of this specification." Format b,
	 * length var. up to 192, no template.
	 */
	private byte[] protectedDataEnvelope1;

	/**
	 * Protected Data Envelope 2 (tag '9F71'), raw value.<br>
	 * Book C-2 v2.12 A.1.108: "Same as Protected Data Envelope 1."
	 */
	private byte[] protectedDataEnvelope2;

	/**
	 * Protected Data Envelope 3 (tag '9F72'), raw value.<br>
	 * Book C-2 v2.12 A.1.109: "Same as Protected Data Envelope 1."
	 */
	private byte[] protectedDataEnvelope3;

	/**
	 * Protected Data Envelope 4 (tag '9F73'), raw value.<br>
	 * Book C-2 v2.12 A.1.110: "Same as Protected Data Envelope 1."
	 */
	private byte[] protectedDataEnvelope4;

	/**
	 * Protected Data Envelope 5 (tag '9F74'), raw value.<br>
	 * Book C-2 v2.12 A.1.111: "Same as Protected Data Envelope 1."
	 */
	private byte[] protectedDataEnvelope5;

	/**
	 * Unprotected Data Envelope 1 (tag '9F75'), raw value.<br>
	 * Book C-2 v2.12 A.1.159: "The Unprotected Data Envelopes contain
	 * proprietary information from the issuer, payment system or third party.
	 * Unprotected Data Envelopes can be retrieved with the GET DATA command and
	 * can be updated with the PUT DATA (CLA='80') command without secure
	 * messaging." Format b, length var. up to 192, no template.
	 */
	private byte[] unprotectedDataEnvelope1;

	/**
	 * Unprotected Data Envelope 2 (tag '9F76'), raw value.<br>
	 * Book C-2 v2.12 A.1.160: "Same as Unprotected Data Envelope 1."
	 */
	private byte[] unprotectedDataEnvelope2;

	/**
	 * Unprotected Data Envelope 3 (tag '9F77'), raw value.<br>
	 * Book C-2 v2.12 A.1.161: "Same as Unprotected Data Envelope 1."
	 */
	private byte[] unprotectedDataEnvelope3;

	/**
	 * Unprotected Data Envelope 4 (tag '9F78'), raw value.<br>
	 * Book C-2 v2.12 A.1.162: "Same as Unprotected Data Envelope 1."
	 * <p>
	 * It is the one envelope a card may size apart from the nine others: the
	 * SDS Scheme Indicator of the Application Capabilities Information (tag
	 * '9F5D' byte 3) has a value '00001000' reading "All SDS tags 32 bytes
	 * except '9F78' which is 64 bytes".
	 * </p>
	 */
	private byte[] unprotectedDataEnvelope4;

	/**
	 * Unprotected Data Envelope 5 (tag '9F79'), raw value.<br>
	 * Book C-2 v2.12 A.1.163: "Same as Unprotected Data Envelope 1."
	 * <p>
	 * The same tag is the Electronic Cash Balance of the applications that
	 * implement electronic cash, where it holds a 6 bytes packed BCD amount and
	 * not an envelope: this field is left null for them and the amount is
	 * reported by {@link Application#getOfflineBalance()} instead.
	 * </p>
	 */
	private byte[] unprotectedDataEnvelope5;
	/**
	 * Hexadecimal tags ('9F70' to '9F79') of the envelopes whose value
	 * exceeded {@link #MAX_LENGTH}; the value is stored anyway. Never null.
	 */
	private List<String> oversizedTags = new ArrayList<String>();


	/**
	 * Get the field protectedDataEnvelope1
	 *
	 * @return the Protected Data Envelope 1 (tag '9F70') or null
	 */
	public byte[] getProtectedDataEnvelope1() {
		return copy(protectedDataEnvelope1);
	}

	/**
	 * Setter for the field protectedDataEnvelope1
	 *
	 * @param protectedDataEnvelope1
	 *            the protectedDataEnvelope1 to set
	 */
	public void setProtectedDataEnvelope1(final byte[] protectedDataEnvelope1) {
		this.protectedDataEnvelope1 = copy(protectedDataEnvelope1);
	}

	/**
	 * Get the field protectedDataEnvelope2
	 *
	 * @return the Protected Data Envelope 2 (tag '9F71') or null
	 */
	public byte[] getProtectedDataEnvelope2() {
		return copy(protectedDataEnvelope2);
	}

	/**
	 * Setter for the field protectedDataEnvelope2
	 *
	 * @param protectedDataEnvelope2
	 *            the protectedDataEnvelope2 to set
	 */
	public void setProtectedDataEnvelope2(final byte[] protectedDataEnvelope2) {
		this.protectedDataEnvelope2 = copy(protectedDataEnvelope2);
	}

	/**
	 * Get the field protectedDataEnvelope3
	 *
	 * @return the Protected Data Envelope 3 (tag '9F72') or null
	 */
	public byte[] getProtectedDataEnvelope3() {
		return copy(protectedDataEnvelope3);
	}

	/**
	 * Setter for the field protectedDataEnvelope3
	 *
	 * @param protectedDataEnvelope3
	 *            the protectedDataEnvelope3 to set
	 */
	public void setProtectedDataEnvelope3(final byte[] protectedDataEnvelope3) {
		this.protectedDataEnvelope3 = copy(protectedDataEnvelope3);
	}

	/**
	 * Get the field protectedDataEnvelope4
	 *
	 * @return the Protected Data Envelope 4 (tag '9F73') or null
	 */
	public byte[] getProtectedDataEnvelope4() {
		return copy(protectedDataEnvelope4);
	}

	/**
	 * Setter for the field protectedDataEnvelope4
	 *
	 * @param protectedDataEnvelope4
	 *            the protectedDataEnvelope4 to set
	 */
	public void setProtectedDataEnvelope4(final byte[] protectedDataEnvelope4) {
		this.protectedDataEnvelope4 = copy(protectedDataEnvelope4);
	}

	/**
	 * Get the field protectedDataEnvelope5
	 *
	 * @return the Protected Data Envelope 5 (tag '9F74') or null
	 */
	public byte[] getProtectedDataEnvelope5() {
		return copy(protectedDataEnvelope5);
	}

	/**
	 * Setter for the field protectedDataEnvelope5
	 *
	 * @param protectedDataEnvelope5
	 *            the protectedDataEnvelope5 to set
	 */
	public void setProtectedDataEnvelope5(final byte[] protectedDataEnvelope5) {
		this.protectedDataEnvelope5 = copy(protectedDataEnvelope5);
	}

	/**
	 * Get the field unprotectedDataEnvelope1
	 *
	 * @return the Unprotected Data Envelope 1 (tag '9F75') or null
	 */
	public byte[] getUnprotectedDataEnvelope1() {
		return copy(unprotectedDataEnvelope1);
	}

	/**
	 * Setter for the field unprotectedDataEnvelope1
	 *
	 * @param unprotectedDataEnvelope1
	 *            the unprotectedDataEnvelope1 to set
	 */
	public void setUnprotectedDataEnvelope1(final byte[] unprotectedDataEnvelope1) {
		this.unprotectedDataEnvelope1 = copy(unprotectedDataEnvelope1);
	}

	/**
	 * Get the field unprotectedDataEnvelope2
	 *
	 * @return the Unprotected Data Envelope 2 (tag '9F76') or null
	 */
	public byte[] getUnprotectedDataEnvelope2() {
		return copy(unprotectedDataEnvelope2);
	}

	/**
	 * Setter for the field unprotectedDataEnvelope2
	 *
	 * @param unprotectedDataEnvelope2
	 *            the unprotectedDataEnvelope2 to set
	 */
	public void setUnprotectedDataEnvelope2(final byte[] unprotectedDataEnvelope2) {
		this.unprotectedDataEnvelope2 = copy(unprotectedDataEnvelope2);
	}

	/**
	 * Get the field unprotectedDataEnvelope3
	 *
	 * @return the Unprotected Data Envelope 3 (tag '9F77') or null
	 */
	public byte[] getUnprotectedDataEnvelope3() {
		return copy(unprotectedDataEnvelope3);
	}

	/**
	 * Setter for the field unprotectedDataEnvelope3
	 *
	 * @param unprotectedDataEnvelope3
	 *            the unprotectedDataEnvelope3 to set
	 */
	public void setUnprotectedDataEnvelope3(final byte[] unprotectedDataEnvelope3) {
		this.unprotectedDataEnvelope3 = copy(unprotectedDataEnvelope3);
	}

	/**
	 * Get the field unprotectedDataEnvelope4
	 *
	 * @return the Unprotected Data Envelope 4 (tag '9F78') or null
	 */
	public byte[] getUnprotectedDataEnvelope4() {
		return copy(unprotectedDataEnvelope4);
	}

	/**
	 * Setter for the field unprotectedDataEnvelope4
	 *
	 * @param unprotectedDataEnvelope4
	 *            the unprotectedDataEnvelope4 to set
	 */
	public void setUnprotectedDataEnvelope4(final byte[] unprotectedDataEnvelope4) {
		this.unprotectedDataEnvelope4 = copy(unprotectedDataEnvelope4);
	}

	/**
	 * Get the field unprotectedDataEnvelope5
	 *
	 * @return the Unprotected Data Envelope 5 (tag '9F79') or null
	 */
	public byte[] getUnprotectedDataEnvelope5() {
		return copy(unprotectedDataEnvelope5);
	}

	/**
	 * Setter for the field unprotectedDataEnvelope5
	 *
	 * @param unprotectedDataEnvelope5
	 *            the unprotectedDataEnvelope5 to set
	 */
	public void setUnprotectedDataEnvelope5(final byte[] unprotectedDataEnvelope5) {
		this.unprotectedDataEnvelope5 = copy(unprotectedDataEnvelope5);
	}

	/**
	 * Method used to know whether the card answered at least one envelope.
	 *
	 * @return true if at least one of the ten envelopes was read
	 */
	public boolean isPresent() {
		return protectedDataEnvelope1 != null || protectedDataEnvelope2 != null || protectedDataEnvelope3 != null
				|| protectedDataEnvelope4 != null || protectedDataEnvelope5 != null || unprotectedDataEnvelope1 != null
				|| unprotectedDataEnvelope2 != null || unprotectedDataEnvelope3 != null || unprotectedDataEnvelope4 != null
				|| unprotectedDataEnvelope5 != null;
	}

	/**
	 * An envelope holds up to 192 bytes, it is never handed out by reference
	 *
	 * @param pValue
	 *            value to copy
	 * @return a copy of the value, or null
	 */
	private static byte[] copy(final byte[] pValue) {
		return pValue != null ? Arrays.copyOf(pValue, pValue.length) : null;
	}

	/**
	 * Hexadecimal tags ('9F70' to '9F79') of the envelopes whose value
	 * exceeded {@link #MAX_LENGTH}; the value is stored anyway. Never null.
	 *
	 * @return the tags of the oversized envelopes, never null
	 */
	public List<String> getOversizedTags() {
		return oversizedTags;
	}

	/**
	 * Setter for the field oversizedTags
	 *
	 * @param oversizedTags
	 *            the oversizedTags to set, null resets to an empty list
	 */
	public void setOversizedTags(final List<String> oversizedTags) {
		this.oversizedTags = oversizedTags != null ? oversizedTags : new ArrayList<String>();
	}

}
