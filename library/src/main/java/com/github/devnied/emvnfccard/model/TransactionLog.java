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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.github.devnied.emvnfccard.iso7816emv.TagAndLength;

/**
 * Details of the reading of the transaction log of an application.
 * <p>
 * EMV 4.4 Book 3 Annex A1 defines the Log Entry (tag '9F4D'): "Provides the
 * SFI of the Transaction Log file and its number of records", two bytes found
 * in the FCI Issuer Discretionary Data of the application. Visa cards may
 * carry it under the proprietary tag 'DF60'. The Log Format (tag '9F4F'),
 * obtained with GET DATA, is the "List (in tag and length format) of data
 * objects representing the logged data elements that are passed to the
 * terminal when a transaction log record is read", and each record of the log
 * file is read with READ RECORD and parsed with that format into an
 * {@link EmvTransactionRecord}.
 * </p>
 *
 * @author MILLAU Julien
 *
 */
public class TransactionLog extends AbstractData {

	/**
	 * Generated serial UID
	 */
	private static final long serialVersionUID = -8462719203485102261L;

	/**
	 * Value of the Log Entry as used: tag '9F4D' or Visa 'DF60', whichever was
	 * found first in the FCI. Null when the FCI carries none.
	 */
	private byte[] rawLogEntry;

	/**
	 * Tag the Log Entry was read from, '9F4D' or 'DF60'. Null when the FCI
	 * carries none.
	 */
	private String logEntryTag;

	/**
	 * Byte 1 of the Log Entry, the SFI of the log file, kept even when it
	 * lies outside the range 1 to 30. {@link AbstractData#UNKNOWN} when no
	 * Log Entry was found.
	 */
	private int sfi = UNKNOWN;

	/**
	 * Byte 2 of the Log Entry, the number of records the card declares, or
	 * {@link AbstractData#UNKNOWN}
	 */
	private int declaredRecordCount = UNKNOWN;

	/**
	 * Value of the Log Format (tag '9F4F') answered to GET DATA, null when
	 * refused or not requested
	 */
	private byte[] rawLogFormat;

	/**
	 * Log Format parsed into tags and lengths.<br>
	 * The list is transient: a tag and length pair is not serializable, and
	 * the identity of a tag is what the parser compares, so a deserialized one
	 * would not be the constant it was read as. {@link #rawLogFormat} keeps
	 * the bytes.
	 */
	private transient List<TagAndLength> logFormat = new ArrayList<TagAndLength>();

	/**
	 * Status bytes SW1 SW2 of the GET DATA '9F4F', four upper-case
	 * hexadecimal characters, null when not sent
	 */
	private String logFormatStatusWord;

	/**
	 * Every READ RECORD sent to the log file, raw data field and status word,
	 * including the one that ended the loop. Never null.
	 */
	private List<RecordData> records = new ArrayList<RecordData>();

	/**
	 * Records parsed but left out of the transactions of the application
	 * because their amount was at most 1 (empty log slots), in log order.
	 * Never null.
	 */
	private List<EmvTransactionRecord> skippedTransactions = new ArrayList<EmvTransactionRecord>();

	/**
	 * Raw log records whose parsing threw an exception. Never null.
	 */
	private List<byte[]> unparsedRecords = new ArrayList<byte[]>();

	/**
	 * True when the log file was actually read: the reading of the
	 * transactions was on, the Log Entry present and valid, and the Log Format
	 * obtained
	 */
	private boolean read;

	/**
	 * Get the value of the Log Entry as used (tag '9F4D' or Visa 'DF60')
	 *
	 * @return the raw Log Entry, or null when the FCI carries none
	 */
	public byte[] getRawLogEntry() {
		return rawLogEntry;
	}

	/**
	 * Setter for the field rawLogEntry
	 *
	 * @param rawLogEntry
	 *            the rawLogEntry to set
	 */
	public void setRawLogEntry(final byte[] rawLogEntry) {
		this.rawLogEntry = rawLogEntry;
	}

	/**
	 * Get the tag the Log Entry was read from
	 *
	 * @return '9F4D' or 'DF60', or null when the FCI carries none
	 */
	public String getLogEntryTag() {
		return logEntryTag;
	}

	/**
	 * Setter for the field logEntryTag
	 *
	 * @param logEntryTag
	 *            the logEntryTag to set
	 */
	public void setLogEntryTag(final String logEntryTag) {
		this.logEntryTag = logEntryTag;
	}

	/**
	 * Get the SFI of the log file, byte 1 of the Log Entry, kept even when
	 * outside the range 1 to 30
	 *
	 * @return the sfi, or {@link AbstractData#UNKNOWN}
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
	 * Get the number of records the card declares, byte 2 of the Log Entry
	 *
	 * @return the declared record count, or {@link AbstractData#UNKNOWN}
	 */
	public int getDeclaredRecordCount() {
		return declaredRecordCount;
	}

	/**
	 * Setter for the field declaredRecordCount
	 *
	 * @param declaredRecordCount
	 *            the declaredRecordCount to set
	 */
	public void setDeclaredRecordCount(final int declaredRecordCount) {
		this.declaredRecordCount = declaredRecordCount;
	}

	/**
	 * Get the value of the Log Format (tag '9F4F') answered to GET DATA
	 *
	 * @return the raw Log Format, or null when refused
	 */
	public byte[] getRawLogFormat() {
		return rawLogFormat;
	}

	/**
	 * Setter for the field rawLogFormat
	 *
	 * @param rawLogFormat
	 *            the rawLogFormat to set
	 */
	public void setRawLogFormat(final byte[] rawLogFormat) {
		this.rawLogFormat = rawLogFormat;
	}

	/**
	 * Get the Log Format (tag '9F4F') parsed into tags and lengths
	 *
	 * @return the list, never null, unmodifiable
	 */
	public List<TagAndLength> getLogFormat() {
		return Collections.unmodifiableList(logFormat != null ? logFormat : new ArrayList<TagAndLength>());
	}

	/**
	 * Setter for the field logFormat
	 *
	 * @param logFormat
	 *            the list to set
	 */
	public void setLogFormat(final List<TagAndLength> logFormat) {
		this.logFormat = logFormat != null ? new ArrayList<TagAndLength>(logFormat) : new ArrayList<TagAndLength>();
	}

	/**
	 * Get the status bytes SW1 SW2 of the GET DATA '9F4F'
	 *
	 * @return four upper-case hexadecimal characters, or null when not sent
	 */
	public String getLogFormatStatusWord() {
		return logFormatStatusWord;
	}

	/**
	 * Setter for the field logFormatStatusWord
	 *
	 * @param logFormatStatusWord
	 *            the logFormatStatusWord to set
	 */
	public void setLogFormatStatusWord(final String logFormatStatusWord) {
		this.logFormatStatusWord = logFormatStatusWord;
	}

	/**
	 * Get every READ RECORD sent to the log file, including the one that ended
	 * the loop
	 *
	 * @return the records, never null
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
	 * Get the records parsed but left out of the transactions of the
	 * application because their amount was at most 1 (empty log slots)
	 *
	 * @return the skipped transactions in log order, never null
	 */
	public List<EmvTransactionRecord> getSkippedTransactions() {
		return skippedTransactions;
	}

	/**
	 * Setter for the field skippedTransactions
	 *
	 * @param skippedTransactions
	 *            the skippedTransactions to set, null resets to an empty list
	 */
	public void setSkippedTransactions(final List<EmvTransactionRecord> skippedTransactions) {
		this.skippedTransactions = skippedTransactions != null ? skippedTransactions
				: new ArrayList<EmvTransactionRecord>();
	}

	/**
	 * Get the raw log records whose parsing threw an exception
	 *
	 * @return the unparsed records, never null
	 */
	public List<byte[]> getUnparsedRecords() {
		return unparsedRecords;
	}

	/**
	 * Setter for the field unparsedRecords
	 *
	 * @param unparsedRecords
	 *            the unparsedRecords to set, null resets to an empty list
	 */
	public void setUnparsedRecords(final List<byte[]> unparsedRecords) {
		this.unparsedRecords = unparsedRecords != null ? unparsedRecords : new ArrayList<byte[]>();
	}

	/**
	 * Method used to know whether the log file was actually read
	 *
	 * @return true when the reading of the transactions was on, the Log Entry
	 *         present and valid, and the Log Format obtained
	 */
	public boolean isRead() {
		return read;
	}

	/**
	 * Setter for the field read
	 *
	 * @param read
	 *            the read to set
	 */
	public void setRead(final boolean read) {
		this.read = read;
	}

}
