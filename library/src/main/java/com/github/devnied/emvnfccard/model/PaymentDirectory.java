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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * A directory selected while reading the card: the Proximity Payment System
 * Environment ('2PAY.SYS.DDF01'), the Payment System Environment
 * ('1PAY.SYS.DDF01') or one of the Directory Definition Files (DDF) they
 * list.
 * <p>
 * EMV 4.4 Book 1 section 12.2 describes the structure: the SELECT of the
 * directory answers a File Control Information (FCI) template (tag '6F')
 * whose proprietary template (tag 'A5') carries the Short File Identifier
 * (tag '88') of the Payment System Directory file, and each record of that
 * file (tag '70') holds Application Templates (tag '61'). The PPSE of EMV
 * Contactless Book B carries its Application Templates in the FCI Issuer
 * Discretionary Data (tag 'BF0C') instead of a file.
 * </p>
 *
 * @author MILLAU Julien
 *
 */
public class PaymentDirectory extends AbstractData {

	/**
	 * Generated serial UID
	 */
	private static final long serialVersionUID = 5177893648321058774L;

	/**
	 * DF Name (tag '84') of the directory FCI: '2PAY.SYS.DDF01',
	 * '1PAY.SYS.DDF01' or the DDF Name (tag '9D') the directory was selected
	 * with. Null when unknown.
	 */
	private byte[] name;

	/**
	 * Data field of the SELECT response, the whole FCI template (tag '6F'),
	 * status bytes removed. Null when the SELECT failed.
	 */
	private byte[] rawFci;

	/**
	 * Status bytes SW1 SW2 of the SELECT of this directory, four upper-case
	 * hexadecimal characters, null when unknown
	 */
	private String selectStatusWord;

	/**
	 * Depth of the directory: 0 for the PPSE or the PSE, one more for each
	 * DDF level below it
	 */
	private int depth;

	/**
	 * Raw value of the Short File Identifier (tag '88') as answered in the
	 * FCI, null when absent
	 */
	private byte[] rawSfi;

	/**
	 * Decoded Short File Identifier (tag '88'), kept even when it lies outside
	 * the range 1 to 30. {@link AbstractData#UNKNOWN} when absent or longer
	 * than four bytes.
	 */
	private int sfi = UNKNOWN;

	/**
	 * True when the SFI was in the range 1 to 30 and the directory file was
	 * read
	 */
	private boolean sfiValid;

	/**
	 * Every READ RECORD sent to the directory file, in order, including the
	 * one that ended the reading (its raw data is empty and its status word is
	 * the ending status). Never null.
	 */
	private List<RecordData> records = new ArrayList<RecordData>();

	/**
	 * Status bytes SW1 SW2 of the READ RECORD that ended the reading of the
	 * directory: '6A83' (record not found) is the normal end, anything else is
	 * unexpected. Null when the loop ended by the record limit or when no file
	 * was read.
	 */
	private String endStatusWord;

	/**
	 * True when the maximum number of records was read without the card
	 * answering a terminating status
	 */
	private boolean recordLimitReached;

	/**
	 * DDF Names (tag '9D') collected in the records of this directory. Never
	 * null.
	 */
	private List<byte[]> subDirectoryNames = new ArrayList<byte[]>();

	/**
	 * Language Preference (tag '5F2D') carried by the directory FCI itself,
	 * ISO 639 codes. Never null.
	 */
	private List<String> languagePreferences = new ArrayList<String>();

	/**
	 * Issuer Code Table Index (tag '9F11') carried by the directory FCI
	 * itself, or {@link AbstractData#UNKNOWN}
	 */
	private int issuerCodeTableIndex = UNKNOWN;

	/**
	 * Every primitive data object of the directory FCI outside the Application
	 * Templates (tag '61'), keyed by upper-case hexadecimal tag ('84', '88',
	 * '5F2D', '9F11', '9F4D', '9F38', 'DF61', unknown tags included), the
	 * first occurrence kept. Never null.
	 */
	private Map<String, byte[]> dataObjects = new LinkedHashMap<String, byte[]>();

	/**
	 * Get the DF Name (tag '84') of the directory FCI
	 *
	 * @return '2PAY.SYS.DDF01', '1PAY.SYS.DDF01' or the DDF Name (tag '9D')
	 *         selected, null when unknown
	 */
	public byte[] getName() {
		return name;
	}

	/**
	 * Setter for the field name
	 *
	 * @param name
	 *            the name to set
	 */
	public void setName(final byte[] name) {
		this.name = name;
	}

	/**
	 * Get the data field of the SELECT response (the whole '6F' template)
	 *
	 * @return the FCI bytes, or null when the SELECT failed
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
	 * Get the status bytes SW1 SW2 of the SELECT of this directory
	 *
	 * @return four upper-case hexadecimal characters, or null when unknown
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
	 * Get the depth of the directory
	 *
	 * @return 0 for the PPSE or the PSE, one more for each DDF level
	 */
	public int getDepth() {
		return depth;
	}

	/**
	 * Setter for the field depth
	 *
	 * @param depth
	 *            the depth to set
	 */
	public void setDepth(final int depth) {
		this.depth = depth;
	}

	/**
	 * Get the raw value of the Short File Identifier (tag '88') as answered
	 *
	 * @return the raw value, or null when absent
	 */
	public byte[] getRawSfi() {
		return rawSfi;
	}

	/**
	 * Setter for the field rawSfi
	 *
	 * @param rawSfi
	 *            the rawSfi to set
	 */
	public void setRawSfi(final byte[] rawSfi) {
		this.rawSfi = rawSfi;
	}

	/**
	 * Get the decoded Short File Identifier (tag '88'), even when it lies
	 * outside the range 1 to 30
	 *
	 * @return the sfi, or {@link AbstractData#UNKNOWN} when absent or longer
	 *         than four bytes
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
	 * Method used to know whether the SFI was in the range 1 to 30 and the
	 * directory file was read
	 *
	 * @return true when the directory file was read
	 */
	public boolean isSfiValid() {
		return sfiValid;
	}

	/**
	 * Setter for the field sfiValid
	 *
	 * @param sfiValid
	 *            the sfiValid to set
	 */
	public void setSfiValid(final boolean sfiValid) {
		this.sfiValid = sfiValid;
	}

	/**
	 * Get every READ RECORD sent to the directory file, in order, including
	 * the one that ended the reading
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
	 * Get the status bytes SW1 SW2 of the READ RECORD that ended the reading
	 * of the directory
	 *
	 * @return '6A83' for a normal end, anything else being unexpected, or null
	 *         when the loop ended by the record limit or no file was read
	 */
	public String getEndStatusWord() {
		return endStatusWord;
	}

	/**
	 * Setter for the field endStatusWord
	 *
	 * @param endStatusWord
	 *            the endStatusWord to set
	 */
	public void setEndStatusWord(final String endStatusWord) {
		this.endStatusWord = endStatusWord;
	}

	/**
	 * Method used to know whether the maximum number of records was read
	 * without a terminating status
	 *
	 * @return true when the record limit stopped the reading
	 */
	public boolean isRecordLimitReached() {
		return recordLimitReached;
	}

	/**
	 * Setter for the field recordLimitReached
	 *
	 * @param recordLimitReached
	 *            the recordLimitReached to set
	 */
	public void setRecordLimitReached(final boolean recordLimitReached) {
		this.recordLimitReached = recordLimitReached;
	}

	/**
	 * Get the DDF Names (tag '9D') collected in the records of this directory
	 *
	 * @return the names, never null
	 */
	public List<byte[]> getSubDirectoryNames() {
		return subDirectoryNames;
	}

	/**
	 * Setter for the field subDirectoryNames
	 *
	 * @param subDirectoryNames
	 *            the subDirectoryNames to set, null resets to an empty list
	 */
	public void setSubDirectoryNames(final List<byte[]> subDirectoryNames) {
		this.subDirectoryNames = subDirectoryNames != null ? subDirectoryNames : new ArrayList<byte[]>();
	}

	/**
	 * Get the Language Preference (tag '5F2D') carried by the directory FCI
	 * itself
	 *
	 * @return the ISO 639 codes, never null
	 */
	public List<String> getLanguagePreferences() {
		return languagePreferences;
	}

	/**
	 * Setter for the field languagePreferences
	 *
	 * @param languagePreferences
	 *            the languagePreferences to set, null resets to an empty list
	 */
	public void setLanguagePreferences(final List<String> languagePreferences) {
		this.languagePreferences = languagePreferences != null ? languagePreferences : new ArrayList<String>();
	}

	/**
	 * Get the Issuer Code Table Index (tag '9F11') carried by the directory
	 * FCI itself
	 *
	 * @return the index, or {@link AbstractData#UNKNOWN}
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
	 * Get every primitive data object of the directory FCI outside the
	 * Application Templates (tag '61'), keyed by upper-case hexadecimal tag,
	 * the first occurrence kept
	 *
	 * @return the data objects, never null
	 */
	public Map<String, byte[]> getDataObjects() {
		return dataObjects;
	}

	/**
	 * Setter for the field dataObjects
	 *
	 * @param dataObjects
	 *            the dataObjects to set, null resets to an empty map
	 */
	public void setDataObjects(final Map<String, byte[]> dataObjects) {
		this.dataObjects = dataObjects != null ? dataObjects : new LinkedHashMap<String, byte[]>();
	}

}
