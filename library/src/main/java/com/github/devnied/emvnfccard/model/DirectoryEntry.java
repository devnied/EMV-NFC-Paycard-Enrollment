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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.github.devnied.emvnfccard.model.enums.FileReferenceEnum;
import com.github.devnied.emvnfccard.model.enums.KernelEnum;
import com.github.devnied.emvnfccard.model.enums.KernelTypeEnum;

/**
 * One entry of a payment system directory, the Application Template (tag '61')
 * of the Payment System Environment (PSE) or of the Proximity Payment System
 * Environment (PPSE).
 * <p>
 * The two environments carry the very same template in two different places:
 * </p>
 * <ul>
 * <li>PSE (contact): the entry sits in a record of the Payment System Directory,
 * a linear file read with READ RECORD. EMV 4.4 Book 1 section 12.2.3: "A record
 * may have several directory entries, but a directory entry shall always be
 * encapsulated in its entirety in a single record. Each record in the Payment
 * System Directory is a constructed data object, and the value field is
 * comprised of one or more directory entries", and Table 11 shows the record as
 * a tag '70' holding one or more tags '61'.</li>
 * <li>PPSE (contactless): there is no directory file at all, the entries sit in
 * the File Control Information Issuer Discretionary Data (tag 'BF0C') of the
 * answer to the SELECT of '2PAY.SYS.DDF01'. EMV Contactless Book B v2.12 Table
 * 3-2: "'6F' FCI Template M / '84' DF Name ('2PAY.SYS.DDF01') O6 / 'A5' FCI
 * Proprietary Template M / 'BF0C' FCI Issuer Discretionary Data M / '61'
 * Directory Entry M".</li>
 * </ul>
 * <p>
 * The content of an ADF entry is given by EMV 4.4 Book 1 Table 12: "'4F' 5-16
 * ADF Name M / '50' 1-16 Application Label M / '9F12' 1-16 Application Preferred
 * Name O / '87' 1 Application Priority Indicator O / '73' var. Directory
 * Discretionary Template O / '9F0A' var. Application Selection Registered
 * Proprietary Data O", the content of a DDF entry by Table 16: "'9D' 5-16 DDF
 * Name M / '73' var. Directory Discretionary Template O". The contactless entry
 * adds the Kernel Identifier (tag '9F2A') and the Extended Selection (tag
 * '9F29') of EMV Contactless Book B Table A-1.
 * </p>
 * <p>
 * The selection of the library only needs the ADF Name of an entry, this bean
 * keeps the whole entry: it is what the card answered.
 * </p>
 *
 * @author MILLAU Julien
 *
 */
public class DirectoryEntry extends AbstractData {

	/**
	 * Generated serial UID
	 */
	private static final long serialVersionUID = -5142723451930374431L;

	/**
	 * Mask of the priority order, the bits 4 to 1 of the Application Priority
	 * Indicator (tag '87')
	 */
	private static final int PRIORITY_MASK = 0x0F;

	/**
	 * Mask of the cardholder confirmation flag, the bit 8 of the Application
	 * Priority Indicator (tag '87')
	 */
	private static final int CARDHOLDER_CONFIRMATION_MASK = 0x80;

	/**
	 * Mask of the Short Kernel Identifier, the bits 6 to 1 of the first byte of
	 * the Kernel Identifier (tag '9F2A')
	 */
	private static final int SHORT_KERNEL_ID_MASK = 0x3F;

	/**
	 * Mask of the kernel type, the bits 8 and 7 of the first byte of the Kernel
	 * Identifier (tag '9F2A')
	 */
	private static final int KERNEL_TYPE_MASK = 0xC0;

	/**
	 * Number of bits the kernel type is shifted by in the first byte of the
	 * Kernel Identifier
	 */
	private static final int KERNEL_TYPE_SHIFT = 6;

	/**
	 * Length of the Requested Kernel ID of a domestic kernel: the Short Kernel
	 * ID followed by the two first bytes of the Extended Kernel ID
	 */
	private static final int DOMESTIC_REQUESTED_KERNEL_ID_LENGTH = 3;

	/**
	 * Highest short EF identifier, ISO/IEC 7816-4 codes it on five bits in the
	 * range 1 to 30
	 */
	private static final int MAX_SHORT_FILE_IDENTIFIER = 30;

	/**
	 * Number of bits a short EF identifier is shifted by in a File reference
	 * data element of one byte: it is held by the bits 8 to 4
	 */
	private static final int SHORT_FILE_IDENTIFIER_SHIFT = 3;

	/**
	 * Application Dedicated File (ADF) Name (tag '4F'), the AID of the
	 * application the entry lists.<br>
	 * Mandatory in an ADF directory entry, null in a DDF one.
	 */
	private byte[] aid;

	/**
	 * Directory Definition File (DDF) Name (tag '9D'), the name of the DF of a
	 * sub directory.<br>
	 * EMV 4.4 Book 1 section 12.2.3 forbids it in a Payment System Directory
	 * ("Payment Systems Directory records shall not contain any entries for
	 * DDFs"), it is only expected in the proprietary directories of Annex C4.
	 */
	private byte[] ddfName;

	/**
	 * Application Label (tag '50'), the mnemonic associated with the AID.<br>
	 * Decoded with the common character set of ISO/IEC 8859.
	 */
	private String applicationLabel;

	/**
	 * Raw value of the Application Priority Indicator (tag '87'), or
	 * {@link AbstractData#UNKNOWN} when the entry carries none
	 */
	private int applicationPriorityIndicator = UNKNOWN;

	/**
	 * Priority order of the entry, bits 4 to 1 of the Application Priority
	 * Indicator (tag '87'): 1 is the highest priority and 0 means that no
	 * priority is assigned. {@link AbstractData#UNKNOWN} when the entry carries
	 * no indicator.
	 */
	private int priority = UNKNOWN;

	/**
	 * Bit 8 of the Application Priority Indicator (tag '87').<br>
	 * In a Payment System Directory entry it tells that the application cannot
	 * be selected without the confirmation of the cardholder (EMV 4.4 Book 1
	 * Table 13). In a PPSE directory entry the bits 8 to 5 are each RFU (EMV
	 * Contactless Book B Table 3-3), so the bit means nothing there: see
	 * {@link #isCardholderConfirmationRequired()}.
	 */
	private boolean cardholderConfirmationFlag;

	/**
	 * File reference data element (tag '51'), raw value.<br>
	 * ISO/IEC 7816-4 section 8.2.2.3 makes it the element that turns the
	 * selection of the entry into a selection by path: "If an application
	 * identifier data object is part of an application template together with a
	 * file reference data object, the value field of which consists of two or
	 * more bytes, then the selection by path shall be performed".
	 */
	private byte[] path;

	/**
	 * Nature of the File reference data element (tag '51'), given by its length
	 * alone. Null when the entry carries no '51', or when its one byte value
	 * does not code a short EF identifier.
	 */
	private FileReferenceEnum pathType;

	/**
	 * Short EF identifier held by a File reference data element (tag '51') of
	 * one byte, in the range 1 to 30, or {@link AbstractData#UNKNOWN}
	 */
	private int shortFileIdentifier = UNKNOWN;

	/**
	 * File identifier held by a File reference data element (tag '51') of two
	 * bytes, or {@link AbstractData#UNKNOWN}
	 */
	private int fileIdentifier = UNKNOWN;

	/**
	 * Last byte of a qualified path (tag '51' of an odd length greater than
	 * two), which ISO/IEC 7816-4 section 8.3 asks to use as P1 of the SELECT
	 * commands, or {@link AbstractData#UNKNOWN}
	 */
	private int pathSelectionParameter = UNKNOWN;

	/**
	 * Kernel Identifier (tag '9F2A'), the preference of the card for the kernel
	 * on which the contactless application can be processed.<br>
	 * EMV Contactless Book B Table A-1 gives it a length of "1 or 3-8", never
	 * two.
	 */
	private byte[] kernelIdentifier;

	/**
	 * Type of kernel, bits 8 and 7 of the first byte of the Kernel Identifier
	 * (tag '9F2A'), or null when the entry carries no identifier
	 */
	private KernelTypeEnum kernelType;

	/**
	 * Short Kernel ID, bits 6 to 1 of the first byte of the Kernel Identifier
	 * (tag '9F2A'), or {@link AbstractData#UNKNOWN}.<br>
	 * A value of zero means that "the kernel is associated with the
	 * corresponding ADF Name" (EMV Contactless Book B Table 3-4).
	 */
	private int shortKernelId = UNKNOWN;

	/**
	 * Requested Kernel ID derived from the Kernel Identifier (tag '9F2A') as
	 * described by EMV Contactless Book B section 3.3.2: the first byte alone
	 * for an international kernel (and for the RFU type), the three first bytes
	 * for a domestic one.<br>
	 * Null when the card expressed no usable preference: no Kernel Identifier,
	 * an empty one, a domestic one shorter than three bytes (the entry is then
	 * rejected by Entry Point) or a domestic one whose Short Kernel ID is zero
	 * (the derivation is then "out of scope of this specification").
	 */
	private byte[] requestedKernelId;

	/**
	 * Extended Selection (tag '9F29'), the value to be appended to the ADF Name
	 * in the data field of the SELECT command when the reader supports it.<br>
	 * EMV Contactless Book B Table A-1 bounds it: "Length of Extended Selection
	 * + Length of ADF Name &lt;= 16".
	 */
	private byte[] extendedSelection;

	/**
	 * Application Selection Registered Proprietary Data (tag '9F0A'), raw
	 * value.<br>
	 * A sequence of triplets, a two byte Proprietary Data Identifier registered
	 * by EMVCo, a one byte length and a proprietary value (EMV Contactless Book
	 * B requirement 3.3.1.2).
	 */
	private byte[] applicationSelectionRegisteredProprietaryData;

	/**
	 * Directory Discretionary Template (tag '73'), raw value: the issuer
	 * discretionary part of the directory entry.<br>
	 * EMV 4.4 Book 1 section 12.2.3 makes it the only container of additional
	 * elements: "No additional data elements shall be present in the Payment
	 * System Directory Record (tag '70') other than those contained in template
	 * '73'".
	 */
	private byte[] directoryDiscretionaryTemplate;

	/**
	 * Dedicated File (DF) Name (tag '84') of the directory the entry was read
	 * from: '1PAY.SYS.DDF01' for the PSE, '2PAY.SYS.DDF01' for the PPSE, the
	 * name of the DDF for a sub directory.
	 */
	private byte[] dedicatedFileName;

	/**
	 * Short File Identifier (tag '88') of the Payment System Directory the
	 * entry was read from, or {@link AbstractData#UNKNOWN} when the entry does
	 * not come from a directory file.<br>
	 * EMV 4.4 Book 1 section 12.2.3: "A Payment System Directory is a linear EF
	 * file identified by a SFI in the range 1 to 10. The SFI for the Payment
	 * System Directory AEF is contained in the FCI of the DDF to which the
	 * directory is attached".
	 */
	private int directorySfi = UNKNOWN;

	/**
	 * Number of the record the entry was read in, or
	 * {@link AbstractData#UNKNOWN} when the entry does not come from a
	 * directory file.<br>
	 * EMV 4.4 Book 1 section 12.3.2 reads the directory "beginning with record
	 * number 1".
	 */
	private int recordNumber = UNKNOWN;

	/**
	 * True when the entry was read in a record of a Payment System Directory
	 * file (tag '70', READ RECORD), false when it was read in the File Control
	 * Information Issuer Discretionary Data (tag 'BF0C') of a SELECT response,
	 * which is how the PPSE carries its entries.
	 */
	private boolean readFromDirectoryFile;
	/**
	 * Value bytes of the Application Template (tag '61') the entry was read
	 * from, as answered. Null when unknown.
	 */
	private byte[] raw;

	/**
	 * Application Preferred Name (tag '9F12') of the entry, raw value. Null
	 * when the entry carries none.
	 */
	private byte[] rawApplicationPreferredName;

	/**
	 * Application Preferred Name (tag '9F12') of the entry, decoded with
	 * the Issuer Code Table Index (tag '9F11') of the directory FCI when it
	 * has one, with the EMV common character set otherwise. Null when the
	 * entry carries none.
	 */
	private String applicationPreferredName;

	/**
	 * Every primitive data object of the entry, recursively (the content of
	 * the Directory Discretionary Template '73' included), keyed by
	 * upper-case hexadecimal tag, the first occurrence kept. The typed
	 * getters stay the decoded view. Never null.
	 */
	private Map<String, byte[]> dataObjects = new LinkedHashMap<String, byte[]>();

	/**
	 * Second and following ADF Names (tag '4F') found in the same entry;
	 * the first one alone is {@link #getAid()}. Never null.
	 */
	private List<byte[]> additionalAdfNames = new ArrayList<byte[]>();


	/**
	 * Method used to get the field aid, the Application Dedicated File (ADF)
	 * Name (tag '4F')
	 *
	 * @return the aid or null
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
		this.aid = aid;
	}

	/**
	 * Method used to get the field ddfName, the Directory Definition File (DDF)
	 * Name (tag '9D')
	 *
	 * @return the ddfName or null
	 */
	public byte[] getDdfName() {
		return ddfName;
	}

	/**
	 * Setter for the field ddfName
	 *
	 * @param ddfName
	 *            the ddfName to set
	 */
	public void setDdfName(final byte[] ddfName) {
		this.ddfName = ddfName;
	}

	/**
	 * Method used to know whether the entry references a sub directory instead
	 * of an application
	 *
	 * @return true when the entry carries a DDF Name and no ADF Name
	 */
	public boolean isDirectoryDefinitionFile() {
		return aid == null && ddfName != null;
	}

	/**
	 * Method used to get the field applicationLabel, the Application Label (tag
	 * '50')
	 *
	 * @return the applicationLabel or null
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
	 * Method used to get the raw value of the Application Priority Indicator
	 * (tag '87')
	 *
	 * @return the indicator, or {@link AbstractData#UNKNOWN} when the entry
	 *         carries none
	 */
	public int getApplicationPriorityIndicator() {
		return applicationPriorityIndicator;
	}

	/**
	 * Setter for the Application Priority Indicator (tag '87'), it holds both
	 * the priority order (bits 4 to 1) and, in a Payment System Directory
	 * entry, the flag telling that the application may not be selected without
	 * the confirmation of the cardholder (bit 8).
	 *
	 * @param pIndicator
	 *            raw value of the tag '87'
	 */
	public void setApplicationPriorityIndicator(final byte pIndicator) {
		applicationPriorityIndicator = pIndicator & 0xFF;
		priority = pIndicator & PRIORITY_MASK;
		cardholderConfirmationFlag = (pIndicator & CARDHOLDER_CONFIRMATION_MASK) != 0;
	}

	/**
	 * Method used to get the field priority, the bits 4 to 1 of the Application
	 * Priority Indicator (tag '87')
	 *
	 * @return the priority, 1 being the highest and 0 meaning that no priority
	 *         is assigned, or {@link AbstractData#UNKNOWN}
	 */
	public int getPriority() {
		return priority;
	}

	/**
	 * Method used to know whether the entry carries an Application Priority
	 * Indicator (tag '87').<br>
	 * EMV Contactless Book B requirement 3.3.1.1: "If the Application Priority
	 * Indicator is absent from a Directory Entry, then Entry Point shall assume
	 * b4-b1 to have a value of 0000b".
	 *
	 * @return true when the card provided an Application Priority Indicator
	 */
	public boolean isPriorityKnown() {
		return applicationPriorityIndicator != UNKNOWN;
	}

	/**
	 * Method used to get the bit 8 of the Application Priority Indicator (tag
	 * '87') as read, whatever the meaning the interface gives it
	 *
	 * @return true when the bit 8 of the indicator is set
	 */
	public boolean isCardholderConfirmationFlag() {
		return cardholderConfirmationFlag;
	}

	/**
	 * Method used to know whether the application may not be selected without
	 * the confirmation of the cardholder.
	 * <p>
	 * The two interfaces do not code the Application Priority Indicator the
	 * same way. EMV 4.4 Book 1 Table 13 (contact): "b8 = 1 : Application cannot
	 * be selected without confirmation by the cardholder". EMV Contactless Book
	 * B Table 3-3 (PPSE): "b8-b5 = xxxx : Each bit RFU". The flag is therefore
	 * only reported for an entry read in a Payment System Directory file.
	 * </p>
	 *
	 * @return true when the entry comes from a Payment System Directory and its
	 *         bit 8 is set
	 */
	public boolean isCardholderConfirmationRequired() {
		return readFromDirectoryFile && cardholderConfirmationFlag;
	}

	/**
	 * Method used to get the field path, the File reference data element (tag
	 * '51')
	 *
	 * @return the path or null
	 */
	public byte[] getPath() {
		return path;
	}

	/**
	 * Setter for the File reference data element (tag '51'), whose nature is
	 * given by its length alone (ISO/IEC 7816-4 section 5.3.1.2, Table 10)
	 *
	 * @param pPath
	 *            raw value of the tag '51'
	 */
	public void setPath(final byte[] pPath) {
		path = pPath;
		pathType = null;
		shortFileIdentifier = UNKNOWN;
		fileIdentifier = UNKNOWN;
		pathSelectionParameter = UNKNOWN;
		if (pPath == null) {
			return;
		}
		if (pPath.length == 0) {
			// "An empty data object references the MF"
			pathType = FileReferenceEnum.MASTER_FILE;
		} else if (pPath.length == 1) {
			// "bits 8 to 4 of the data element are not all equal and bits 3 to
			// 1 are set to 000": the identifier is a number from 1 to 30
			int identifier = (pPath[0] & 0xFF) >> SHORT_FILE_IDENTIFIER_SHIFT;
			if ((pPath[0] & 0x07) == 0 && identifier >= 1 && identifier <= MAX_SHORT_FILE_IDENTIFIER) {
				pathType = FileReferenceEnum.SHORT_FILE_IDENTIFIER;
				shortFileIdentifier = identifier;
			}
		} else if (pPath.length == 2) {
			// "If the length is two, then the data element is a file
			// identifier"
			pathType = FileReferenceEnum.FILE_IDENTIFIER;
			fileIdentifier = (pPath[0] & 0xFF) << 8 | pPath[1] & 0xFF;
		} else if (pPath.length % 2 == 0) {
			// The path is absolute when it starts with the identifier of the
			// MF, relative otherwise
			pathType = pPath[0] == 0x3F && pPath[1] == 0x00 ? FileReferenceEnum.ABSOLUTE_PATH
					: FileReferenceEnum.RELATIVE_PATH;
		} else {
			// "If the length is odd, then the path is qualified [...] followed
			// by a byte to use as P1 in one or more SELECT commands"
			pathType = FileReferenceEnum.QUALIFIED_PATH;
			pathSelectionParameter = pPath[pPath.length - 1] & 0xFF;
		}
	}

	/**
	 * Method used to get the field pathType
	 *
	 * @return the nature of the File reference data element, or null
	 */
	public FileReferenceEnum getPathType() {
		return pathType;
	}

	/**
	 * Method used to get the field shortFileIdentifier
	 *
	 * @return the short EF identifier of a one byte File reference data
	 *         element, or {@link AbstractData#UNKNOWN}
	 */
	public int getShortFileIdentifier() {
		return shortFileIdentifier;
	}

	/**
	 * Method used to get the field fileIdentifier
	 *
	 * @return the file identifier of a two bytes File reference data element,
	 *         or {@link AbstractData#UNKNOWN}
	 */
	public int getFileIdentifier() {
		return fileIdentifier;
	}

	/**
	 * Method used to get the field pathSelectionParameter
	 *
	 * @return the byte to use as P1 of the SELECT commands of a qualified path,
	 *         or {@link AbstractData#UNKNOWN}
	 */
	public int getPathSelectionParameter() {
		return pathSelectionParameter;
	}

	/**
	 * Method used to know whether the entry asks for a selection by path.<br>
	 * ISO/IEC 7816-4 section 8.2.2.3 only does so when the File reference data
	 * element holds "two or more bytes", a shorter one leaves the selection by
	 * DF name in place.
	 *
	 * @return true when the entry carries a File reference data element of two
	 *         bytes or more
	 */
	public boolean isSelectionByPath() {
		return path != null && path.length >= 2;
	}

	/**
	 * Method used to get the field kernelIdentifier, the Kernel Identifier (tag
	 * '9F2A')
	 *
	 * @return the kernelIdentifier or null
	 */
	public byte[] getKernelIdentifier() {
		return kernelIdentifier;
	}

	/**
	 * Setter for the Kernel Identifier (tag '9F2A'), whose first byte holds the
	 * kernel type on its bits 8 and 7 and the Short Kernel ID on its bits 6 to
	 * 1 (EMV Contactless Book B Table 3-4)
	 *
	 * @param pKernelIdentifier
	 *            raw value of the tag '9F2A'
	 */
	public void setKernelIdentifier(final byte[] pKernelIdentifier) {
		kernelIdentifier = pKernelIdentifier;
		kernelType = null;
		shortKernelId = UNKNOWN;
		requestedKernelId = null;
		if (pKernelIdentifier == null || pKernelIdentifier.length == 0) {
			// "If the length of the Kernel Identifier value field is zero, then
			// Entry Point shall use a default value for the Requested Kernel
			// ID, based on the matching AID": the default belongs to the
			// reader, not to the card, so nothing is reported here
			return;
		}
		kernelType = KernelTypeEnum.find((pKernelIdentifier[0] & KERNEL_TYPE_MASK) >> KERNEL_TYPE_SHIFT);
		shortKernelId = pKernelIdentifier[0] & SHORT_KERNEL_ID_MASK;
		if (kernelType == KernelTypeEnum.INTERNATIONAL || kernelType == KernelTypeEnum.RFU) {
			// "Requested Kernel ID is equal to the value of byte 1 of the
			// Kernel Identifier"
			requestedKernelId = new byte[] { pKernelIdentifier[0] };
		} else if (shortKernelId != 0 && pKernelIdentifier.length >= DOMESTIC_REQUESTED_KERNEL_ID_LENGTH) {
			// "the Requested Kernel ID is equal to value of the byte 1 to byte
			// 3 of the Kernel Identifier". A domestic identifier shorter than
			// three bytes has its entry rejected by Entry Point, and a Short
			// Kernel ID of zero leaves the derivation out of scope
			requestedKernelId = Arrays.copyOf(pKernelIdentifier, DOMESTIC_REQUESTED_KERNEL_ID_LENGTH);
		}
	}

	/**
	 * Method used to get the field kernelType
	 *
	 * @return the type of kernel the card asks for, or null when the entry
	 *         carries no Kernel Identifier
	 */
	public KernelTypeEnum getKernelType() {
		return kernelType;
	}

	/**
	 * Method used to get the field shortKernelId
	 *
	 * @return the Short Kernel ID, or {@link AbstractData#UNKNOWN}
	 */
	public int getShortKernelId() {
		return shortKernelId;
	}

	/**
	 * Method used to get the field requestedKernelId
	 *
	 * @return the Requested Kernel ID, or null when the card expressed no
	 *         usable preference
	 */
	public byte[] getRequestedKernelId() {
		return requestedKernelId;
	}

	/**
	 * Method used to get the contactless kernel the entry names.<br>
	 * Only an international kernel is named by its Short Kernel ID alone, a
	 * domestic one is identified by the whole {@link #getRequestedKernelId()}.
	 *
	 * @return the kernel, or null when the entry names none
	 */
	public KernelEnum getKernel() {
		return kernelType == KernelTypeEnum.INTERNATIONAL ? KernelEnum.find(shortKernelId) : null;
	}

	/**
	 * Method used to get the field extendedSelection, the Extended Selection
	 * (tag '9F29')
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
	 * Method used to get the field
	 * applicationSelectionRegisteredProprietaryData, the ASRPD (tag '9F0A')
	 *
	 * @return the applicationSelectionRegisteredProprietaryData or null
	 */
	public byte[] getApplicationSelectionRegisteredProprietaryData() {
		return applicationSelectionRegisteredProprietaryData;
	}

	/**
	 * Setter for the field applicationSelectionRegisteredProprietaryData
	 *
	 * @param applicationSelectionRegisteredProprietaryData
	 *            the applicationSelectionRegisteredProprietaryData to set
	 */
	public void setApplicationSelectionRegisteredProprietaryData(
			final byte[] applicationSelectionRegisteredProprietaryData) {
		this.applicationSelectionRegisteredProprietaryData = applicationSelectionRegisteredProprietaryData;
	}

	/**
	 * Method used to get the field directoryDiscretionaryTemplate, the issuer
	 * discretionary part of the entry (tag '73')
	 *
	 * @return the directoryDiscretionaryTemplate or null
	 */
	public byte[] getDirectoryDiscretionaryTemplate() {
		return directoryDiscretionaryTemplate;
	}

	/**
	 * Setter for the field directoryDiscretionaryTemplate
	 *
	 * @param directoryDiscretionaryTemplate
	 *            the directoryDiscretionaryTemplate to set
	 */
	public void setDirectoryDiscretionaryTemplate(final byte[] directoryDiscretionaryTemplate) {
		this.directoryDiscretionaryTemplate = directoryDiscretionaryTemplate;
	}

	/**
	 * Method used to get the field dedicatedFileName, the DF Name (tag '84') of
	 * the directory the entry was read from
	 *
	 * @return the dedicatedFileName or null
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
	 * Method used to get the field directorySfi
	 *
	 * @return the SFI of the directory file the entry was read from, or
	 *         {@link AbstractData#UNKNOWN}
	 */
	public int getDirectorySfi() {
		return directorySfi;
	}

	/**
	 * Setter for the field directorySfi
	 *
	 * @param directorySfi
	 *            the directorySfi to set
	 */
	public void setDirectorySfi(final int directorySfi) {
		this.directorySfi = directorySfi;
	}

	/**
	 * Method used to get the field recordNumber
	 *
	 * @return the number of the record the entry was read in, or
	 *         {@link AbstractData#UNKNOWN}
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
	 * Method used to know whether the entry was read in a record of a Payment
	 * System Directory file
	 *
	 * @return true when the entry comes from a directory file, false when it
	 *         comes from the FCI Issuer Discretionary Data of a SELECT response
	 */
	public boolean isReadFromDirectoryFile() {
		return readFromDirectoryFile;
	}

	/**
	 * Setter for the field readFromDirectoryFile
	 *
	 * @param readFromDirectoryFile
	 *            the readFromDirectoryFile to set
	 */
	public void setReadFromDirectoryFile(final boolean readFromDirectoryFile) {
		this.readFromDirectoryFile = readFromDirectoryFile;
	}

	/**
	 * Value bytes of the Application Template (tag '61') the entry was read
	 * from, as answered. Null when unknown.
	 *
	 * @return the raw Application Template, or null
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
	 * Application Preferred Name (tag '9F12') of the entry, raw value. Null
	 * when the entry carries none.
	 *
	 * @return the raw Application Preferred Name, or null
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
	 * Application Preferred Name (tag '9F12') of the entry, decoded with
	 * the Issuer Code Table Index (tag '9F11') of the directory FCI when it
	 * has one, with the EMV common character set otherwise. Null when the
	 * entry carries none.
	 *
	 * @return the Application Preferred Name, or null
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
	 * Every primitive data object of the entry, recursively (the content of
	 * the Directory Discretionary Template '73' included), keyed by
	 * upper-case hexadecimal tag, the first occurrence kept. The typed
	 * getters stay the decoded view. Never null.
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

	/**
	 * Second and following ADF Names (tag '4F') found in the same entry;
	 * the first one alone is {@link #getAid()}. Never null.
	 *
	 * @return the additional ADF Names, never null
	 */
	public List<byte[]> getAdditionalAdfNames() {
		return additionalAdfNames;
	}

	/**
	 * Setter for the field additionalAdfNames
	 *
	 * @param additionalAdfNames
	 *            the additionalAdfNames to set, null resets to an empty list
	 */
	public void setAdditionalAdfNames(final List<byte[]> additionalAdfNames) {
		this.additionalAdfNames = additionalAdfNames != null ? additionalAdfNames : new ArrayList<byte[]>();
	}

}
