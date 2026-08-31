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

/**
 * One READ RECORD command sent to the card and the answer it got, kept
 * verbatim.
 * <p>
 * EMV 4.4 Book 3 section 6.5.11 defines the command: P1 is the record number,
 * P2 codes the Short File Identifier on its five high order bits ("SFI &lt;&lt;
 * 3 | 100b"), and the response data field is the record itself, followed by
 * the status bytes SW1 SW2.
 * </p>
 *
 * @author MILLAU Julien
 *
 */
public class RecordData extends AbstractData {

	/**
	 * Generated serial UID
	 */
	private static final long serialVersionUID = -4258932131585047012L;

	/**
	 * Short File Identifier the record was read from, the five high order bits
	 * of P2 of the READ RECORD command (P2 &gt;&gt; 3), or
	 * {@link AbstractData#UNKNOWN}
	 */
	private int sfi = UNKNOWN;

	/**
	 * Record number, P1 of the READ RECORD command, or
	 * {@link AbstractData#UNKNOWN}
	 */
	private int recordNumber = UNKNOWN;

	/**
	 * Data field of the response, the status bytes removed. An empty array
	 * when the card answered a status word alone, null when no answer was
	 * received.
	 */
	private byte[] raw;

	/**
	 * Status bytes SW1 SW2 of the response, four upper-case hexadecimal
	 * characters (e.g. "9000", "6A83"), null when no answer was received
	 */
	private String statusWord;

	/**
	 * Default constructor
	 */
	public RecordData() {
		super();
	}

	/**
	 * Constructor using fields
	 *
	 * @param pSfi
	 *            Short File Identifier the record was read from
	 * @param pRecordNumber
	 *            record number
	 * @param pRaw
	 *            data field of the response, status bytes removed
	 * @param pStatusWord
	 *            status bytes SW1 SW2, four hexadecimal characters
	 */
	public RecordData(final int pSfi, final int pRecordNumber, final byte[] pRaw, final String pStatusWord) {
		super();
		sfi = pSfi;
		recordNumber = pRecordNumber;
		raw = pRaw;
		statusWord = pStatusWord;
	}

	/**
	 * Get the Short File Identifier the record was read from (P2 of READ
	 * RECORD &gt;&gt; 3)
	 *
	 * @return the sfi or {@link AbstractData#UNKNOWN}
	 */
	public int getSfi() {
		return sfi;
	}

	/**
	 * Setter for the field sfi
	 *
	 * @param sfi
	 *            the sfi to set
	 */
	public void setSfi(final int sfi) {
		this.sfi = sfi;
	}

	/**
	 * Get the record number (P1 of READ RECORD)
	 *
	 * @return the recordNumber or {@link AbstractData#UNKNOWN}
	 */
	public int getRecordNumber() {
		return recordNumber;
	}

	/**
	 * Setter for the field recordNumber
	 *
	 * @param recordNumber
	 *            the recordNumber to set
	 */
	public void setRecordNumber(final int recordNumber) {
		this.recordNumber = recordNumber;
	}

	/**
	 * Get the data field of the response, the status bytes removed
	 *
	 * @return the record bytes, an empty array when the card answered a status
	 *         word alone, null when nothing was answered
	 */
	public byte[] getRaw() {
		return raw;
	}

	/**
	 * Setter for the field raw
	 *
	 * @param raw
	 *            the raw to set
	 */
	public void setRaw(final byte[] raw) {
		this.raw = raw;
	}

	/**
	 * Get the status bytes SW1 SW2 of the response
	 *
	 * @return four upper-case hexadecimal characters (e.g. "9000", "6A83") or
	 *         null when nothing was answered
	 */
	public String getStatusWord() {
		return statusWord;
	}

	/**
	 * Setter for the field statusWord
	 *
	 * @param statusWord
	 *            the statusWord to set
	 */
	public void setStatusWord(final String statusWord) {
		this.statusWord = statusWord;
	}

}
