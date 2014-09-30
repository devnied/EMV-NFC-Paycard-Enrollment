/*
 * Copyright (C) 2013 MILLAU Julien
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
package com.github.devnied.emvnfccard.iso7816emv;

import java.util.Arrays;

import fr.devnied.bitlib.BytesUtils;

/**
 * This implementation is a mix of EMV & VISA TTQ
 * 
 * VISA: Terminal Transaction Qualifiers (Tag '9F66') is a reader data element indicating capabilities (e.g., MSD or qVSDC) and
 * transaction-specific requirements (e.g., online) of the reader. It is requested by the card in the PDOL and used by the card to
 * determine how to process the transaction (for example, process using MSD or qVSDC, process offline or online).
 * 
 */
public class TerminalTransactionQualifiers {

	private byte[] data = new byte[4];

	public TerminalTransactionQualifiers() {
	}

	public boolean contactlessMagneticStripeSupported() {
		return BytesUtils.matchBitByBitIndex(data[0], 7);
	}

	public boolean contactlessVSDCsupported() {
		return BytesUtils.matchBitByBitIndex(data[0], 6);
	}

	public boolean contactlessEMVmodeSupported() {
		return BytesUtils.matchBitByBitIndex(data[0], 5);
	}

	public boolean contactEMVsupported() {
		return BytesUtils.matchBitByBitIndex(data[0], 4);
	}

	public boolean readerIsOfflineOnly() {
		return BytesUtils.matchBitByBitIndex(data[0], 3);
	}

	public boolean onlinePINsupported() {
		return BytesUtils.matchBitByBitIndex(data[0], 2);
	}

	public boolean signatureSupported() {
		return BytesUtils.matchBitByBitIndex(data[0], 1);
	}

	public boolean onlineCryptogramRequired() {
		return BytesUtils.matchBitByBitIndex(data[1], 7);
	}

	public boolean cvmRequired() {
		return BytesUtils.matchBitByBitIndex(data[1], 6);
	}

	public boolean contactChipOfflinePINsupported() {
		return BytesUtils.matchBitByBitIndex(data[1], 5);
	}

	public boolean issuerUpdateProcessingSupported() {
		return BytesUtils.matchBitByBitIndex(data[2], 7);
	}

	public boolean consumerDeviceCVMsupported() {
		return BytesUtils.matchBitByBitIndex(data[2], 6);
	}

	public void setContactlessMagneticStripeSupported(final boolean value) {
		data[0] = BytesUtils.setBit(data[0], 7, value);
	}

	public void setContactlessVSDCsupported(final boolean value) {
		data[0] = BytesUtils.setBit(data[0], 6, value);
		if (value) {
			/*
			 * A reader that supports contactless VSDC in addition to qVSDC shall not indicate support for qVSDC in the Terminal
			 * Transaction Qualifiers (set byte 1 bit 6 to b'0'). The reader shall restore this bit to b'1' prior to deactivation
			 */
			setContactlessEMVmodeSupported(false);
		}
	}

	public void setContactlessEMVmodeSupported(final boolean value) {
		data[0] = BytesUtils.setBit(data[0], 5, value);
	}

	public void setContactEMVsupported(final boolean value) {
		data[0] = BytesUtils.setBit(data[0], 4, value);
	}

	public void setReaderIsOfflineOnly(final boolean value) {
		data[0] = BytesUtils.setBit(data[0], 3, value);
	}

	public void setOnlinePINsupported(final boolean value) {
		data[0] = BytesUtils.setBit(data[0], 2, value);
	}

	public void setSignatureSupported(final boolean value) {
		data[0] = BytesUtils.setBit(data[0], 1, value);
	}

	public void setOnlineCryptogramRequired(final boolean value) {
		data[1] = BytesUtils.setBit(data[1], 7, value);
	}

	public void setCvmRequired(final boolean value) {
		data[1] = BytesUtils.setBit(data[1], 6, value);
	}

	public void setContactChipOfflinePINsupported(final boolean value) {
		data[1] = BytesUtils.setBit(data[1], 5, value);
	}

	public void setIssuerUpdateProcessingSupported(final boolean value) {
		data[2] = BytesUtils.setBit(data[2], 7, value);
	}

	public void setConsumerDeviceCVMsupported(final boolean value) {
		data[2] = BytesUtils.setBit(data[2], 6, value);
	}

	// The rest of the bits in the second byte are RFU (Reserved for Future Use)

	public byte[] getBytes() {
		return Arrays.copyOf(data, data.length);
	}
}