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
import java.util.Collection;
import java.util.Date;
import java.util.List;

import com.github.devnied.emvnfccard.enums.EmvCardScheme;
import com.github.devnied.emvnfccard.model.enums.CardStateEnum;

/**
 * Bean used to describe data in EMV card
 *
 * @author MILLAU Julien
 *
 */
public class EmvCard extends AbstractData {

	/**
	 * Generated serial UID
	 */
	private static final long serialVersionUID = 736740432469989941L;

	/**
	 * CPLC data
	 */
	private CPLC cplc;
	
	/**
	 * Holder Lastname
	 */
	private String holderLastname;

	/**
	 * Holder Firstname
	 */
	private String holderFirstname;

	/**
	 * Card type
	 */
	private EmvCardScheme type;
	
	/**
	 * Card ATS (contact less) or ATR
	 */
	private String at;

	/**
	 * List of Atr description
	 */
	private Collection<String> atrDescription;

	/**
	 * Track 2 data
	 */
	private EmvTrack2 track2;

	/**
	 * Track 1 data
	 */
	private EmvTrack1 track1;

	/**
	 * BIC - Bank Identifier Code
	 */
	private String bic;

	/**
	 * IBAN - International Bank Account Number
	 */
	private String iban;

	/**
	 * Application list
	 */
	private final List<Application> applications = new ArrayList<Application>();

	/**
	 * Entries of the payment system directory, in the order the card listed
	 * them.<br>
	 * They are the Application Templates (tag '61') read in the records of the
	 * Payment System Directory of the PSE, or in the FCI Issuer Discretionary
	 * Data (tag 'BF0C') of the PPSE. An entry carries more than the AID the
	 * selection uses, and a directory may list an entry the terminal does not
	 * select at all, so the list is not the image of {@link #applications}.
	 */
	private final List<DirectoryEntry> directoryEntries = new ArrayList<DirectoryEntry>();

	/**
	 * Card state
	 */
	private CardStateEnum state = CardStateEnum.UNKNOWN;
	/**
	 * Directories selected during the read, in selection order: index 0 is
	 * the PPSE ('2PAY.SYS.DDF01') or the PSE ('1PAY.SYS.DDF01') when its
	 * SELECT succeeded, then the Directory Definition Files (DDF Name, tag
	 * '9D') it lists. Never null, empty when the payment environment was
	 * not read.
	 */
	private List<PaymentDirectory> paymentDirectories = new ArrayList<PaymentDirectory>();

	/**
	 * Status bytes SW1 SW2 answered to the SELECT of the payment
	 * environment ('2PAY.SYS.DDF01' in contactless, '1PAY.SYS.DDF01' in
	 * contact), four upper-case hexadecimal characters. Null when the
	 * reading by the payment environment was not run.
	 */
	private String paymentEnvironmentStatusWord;

	/**
	 * True when the maximum depth of Directory Definition Files (DDF Name,
	 * tag '9D', EMV 4.4 Book 1 section 12.2) stopped the walk of the sub
	 * directories
	 */
	private boolean directoryDepthLimitReached;

	/**
	 * True when the maximum number of Directory Definition Files (DDF Name,
	 * tag '9D') stopped the walk of the sub directories
	 */
	private boolean directoryCountLimitReached;

	/**
	 * DDF Names (tag '9D') listed by the card but not selected: already
	 * visited, or the depth or count limit reached. Never null.
	 */
	private List<byte[]> unreadDdfNames = new ArrayList<byte[]>();

	/**
	 * Application Templates (tag '61') that carried neither an ADF Name
	 * (tag '4F') nor a DDF Name (tag '9D') and therefore describe nothing;
	 * {@link #getDirectoryEntries()} does not list them. Never null.
	 */
	private List<DirectoryEntry> ignoredDirectoryEntries = new ArrayList<DirectoryEntry>();

	/**
	 * When {@link #getState()} is LOCKED: status bytes SW1 SW2 answered to
	 * the SELECT of the highest priority application ('6283' application
	 * blocked, '6A81' card blocked, '6985'...), four upper-case hexadecimal
	 * characters. Null otherwise.
	 */
	private String lockedStatusWord;

	/**
	 * True when the applications were found by the list of known AIDs
	 * (selection by AID, EMV 4.4 Book 1 section 12.3.3) because the payment
	 * environment could not be read
	 */
	private boolean readWithAid;

	/**
	 * First Track 1 Data (tag '56') value that could not be parsed (pattern
	 * mismatch, unusable expiration date '^'...), null when none
	 */
	private byte[] unparsedTrack1;

	/**
	 * First Track 2 Equivalent Data (tag '57') or Track 2 Data (tag '9F6B')
	 * value that could not be parsed, null when none
	 */
	private byte[] unparsedTrack2;

	/**
	 * First Track 1 Discretionary Data (tag '9F1F') received while the card
	 * had no track 1 to attach it to, null when none
	 */
	private byte[] orphanTrack1DiscretionaryData;

	/**
	 * First Track 2 Discretionary Data (tag '9F20') received while the card
	 * had no track 2 to attach it to, null when none
	 */
	private byte[] orphanTrack2DiscretionaryData;

	/**
	 * Bytes returned by the provider for the Answer To Select (ATS
	 * historical bytes or hi-layer response in contactless, ISO 7816-3) or
	 * the Answer To Reset (ATR in contact), the source of {@link #getAt()}.
	 * Null when the provider returned none.
	 */
	private byte[] rawAt;

	/**
	 * True when the lookup of the ATR/ATS description ran; a null or empty
	 * {@link #getAtrDescription()} then means that the answer was looked up
	 * and is unknown
	 */
	private boolean atLookedUp;

	/**
	 * Full response (data and status bytes) to the GET DATA of the Card
	 * Production Life Cycle data (tag '9F7F', Global Platform Card
	 * Specification), stored whatever its length so that a rejected answer
	 * stays visible. Null when the CPLC was not requested.
	 */
	private byte[] rawCplcResponse;

	/**
	 * Status bytes SW1 SW2 of the GET DATA '9F7F' response, four upper-case
	 * hexadecimal characters. Null when the CPLC was not requested.
	 */
	private String cplcStatusWord;

	/**
	 * Scheme resolved from the AID of the application that set {@link
	 * #getType()} (the registered application provider identifier, EMV 4.4
	 * Book 1 section 12.2.1), before the CB scheme is replaced by the
	 * scheme of the PAN. Null when no application set the type.
	 */
	private EmvCardScheme aidType;

	/**
	 * AID (DF Name, tag '84', or requested AID) of the application whose
	 * reading set {@link #getType()}, null when none did
	 */
	private byte[] typeAid;

	/**
	 * Identification of the card computed at the end of the reading from
	 * every clue read (AID, PAN, BIC, IBAN, issuer country, ATR
	 * description...). Null before a read.
	 */
	private CardIdentification identification;


	/**
	 * Method used to get the field holderLastname
	 *
	 * @return the holderLastname
	 */
	public String getHolderLastname() {
		String ret = holderLastname;
		if (ret == null && track1 != null) {
			ret = track1.getHolderLastname();
		}
		return ret;
	}

	/**
	 * Setter for the field holderLastname
	 *
	 * @param holderLastname
	 *            the holderLastname to set
	 */
	public void setHolderLastname(final String holderLastname) {
		this.holderLastname = holderLastname;
	}

	/**
	 * Method used to get the field holderFirstname
	 *
	 * @return the holderFirstname
	 */
	public String getHolderFirstname() {
		String ret = holderFirstname;
		if (ret == null && track1 != null) {
			ret = track1.getHolderFirstname();
		}
		return ret;
	}

	/**
	 * Setter for the field holderFirstname
	 *
	 * @param holderFirstname
	 *            the holderFirstname to set
	 */
	public void setHolderFirstname(final String holderFirstname) {
		this.holderFirstname = holderFirstname;
	}

	/**
	 * Method used to get the field cardNumber
	 *
	 * @return the cardNumber
	 */
	public String getCardNumber() {
		String ret = null;
		if (track2 != null) {
			ret = track2.getCardNumber();
		}
		if (ret == null && track1 != null) {
			ret = track1.getCardNumber();
		}
		return ret;
	}

	/**
	 * Method used to get the field expireDate
	 *
	 * @return the expireDate
	 */
	public Date getExpireDate() {
		Date ret = null;
		if (track2 != null) {
			ret = track2.getExpireDate();
		}
		if (ret == null && track1 != null) {
			ret = track1.getExpireDate();
		}
		return ret;
	}

	/**
	 * Method used to get the field type
	 *
	 * @return the type
	 */
	public EmvCardScheme getType() {
		return type;
	}

	/**
	 * Setter for the field type
	 *
	 * @param type
	 *            the type to set
	 */
	public void setType(final EmvCardScheme type) {
		this.type = type;
	}

	@Override
	public boolean equals(final Object arg0) {
		return arg0 instanceof EmvCard && getCardNumber() != null && getCardNumber().equals(((EmvCard) arg0).getCardNumber());
	}

	/**
	 * Method used to get the field atrDescription
	 *
	 * @return the atrDescription
	 */
	public Collection<String> getAtrDescription() {
		return atrDescription;
	}

	/**
	 * Setter for the field atrDescription
	 *
	 * @param atrDescription
	 *            the atrDescription to set
	 */
	public void setAtrDescription(final Collection<String> atrDescription) {
		this.atrDescription = atrDescription;
	}
	
	/**
	 * Method used to get the field at
	 *
	 * @return the at value
	 */
	public String getAt() {
		return at;
	}

	/**
	 * Setter for the field at
	 *
	 * @param atr
	 *            the at value to set
	 */
	public void setAt(final String at) {
		this.at = at;
	}

	/**
	 * Method used to get the field state
	 *
	 * @return the state
	 */
	public CardStateEnum getState() {
		return state;
	}

	/**
	 * Setter for the field state
	 *
	 * @param state
	 *            the state to set
	 */
	public void setState(final CardStateEnum state) {
		this.state = state;
	}

	/**
	 * Method used to get the field track2
	 *
	 * @return the track2
	 */
	public EmvTrack2 getTrack2() {
		return track2;
	}

	/**
	 * Setter for the field track2
	 *
	 * @param track2
	 *            the track2 to set
	 */
	public void setTrack2(final EmvTrack2 track2) {
		this.track2 = track2;
	}

	/**
	 * Method used to get the field track1
	 *
	 * @return the track1
	 */
	public EmvTrack1 getTrack1() {
		return track1;
	}

	/**
	 * Setter for the field track1
	 *
	 * @param track1
	 *            the track1 to set
	 */
	public void setTrack1(final EmvTrack1 track1) {
		this.track1 = track1;
	}

	/**
	 * Method used to get the field bic
	 *
	 * @return the bic
	 */
	public String getBic() {
		return bic;
	}

	/**
	 * Setter for the field bic
	 *
	 * @param bic
	 *            the bic to set
	 */
	public void setBic(final String bic) {
		this.bic = bic;
	}

	/**
	 * Method used to get the field iban
	 *
	 * @return the iban
	 */
	public String getIban() {
		return iban;
	}

	/**
	 * Setter for the field iban
	 *
	 * @param iban
	 *            the iban to set
	 */
	public void setIban(final String iban) {
		this.iban = iban;
	}

	/**
	 * Method used to get the field applications
	 *
	 * @return the applications
	 */
	public List<Application> getApplications() {
		return applications;
	}

	/**
	 * Method used to get the field directoryEntries
	 *
	 * @return the entries of the payment system directory
	 */
	public List<DirectoryEntry> getDirectoryEntries() {
		return directoryEntries;
	}

	/**
	 * Get the field cplc
	 * @return the cplc
	 */
	public CPLC getCplc() {
		return cplc;
	}

	/**
	 * Setter for the field cplc
	 *
	 * @param cplc the cplc to set
	 */
	public void setCplc(CPLC cplc) {
		this.cplc = cplc;
	}
	

	/**
	 * Directories selected during the read, in selection order: index 0 is
	 * the PPSE ('2PAY.SYS.DDF01') or the PSE ('1PAY.SYS.DDF01') when its
	 * SELECT succeeded, then the Directory Definition Files (DDF Name, tag
	 * '9D') it lists. Never null, empty when the payment environment was
	 * not read.
	 *
	 * @return the directories, never null
	 */
	public List<PaymentDirectory> getPaymentDirectories() {
		return paymentDirectories;
	}

	/**
	 * Setter for the field paymentDirectories
	 *
	 * @param paymentDirectories
	 *            the paymentDirectories to set, null resets to an empty list
	 */
	public void setPaymentDirectories(final List<PaymentDirectory> paymentDirectories) {
		this.paymentDirectories = paymentDirectories != null ? paymentDirectories : new ArrayList<PaymentDirectory>();
	}

	/**
	 * Status bytes SW1 SW2 answered to the SELECT of the payment
	 * environment ('2PAY.SYS.DDF01' in contactless, '1PAY.SYS.DDF01' in
	 * contact), four upper-case hexadecimal characters. Null when the
	 * reading by the payment environment was not run.
	 *
	 * @return the status word, or null when the payment environment was not selected
	 */
	public String getPaymentEnvironmentStatusWord() {
		return paymentEnvironmentStatusWord;
	}

	/**
	 * Setter for the field paymentEnvironmentStatusWord
	 *
	 * @param paymentEnvironmentStatusWord
	 *            the paymentEnvironmentStatusWord to set
	 */
	public void setPaymentEnvironmentStatusWord(final String paymentEnvironmentStatusWord) {
		this.paymentEnvironmentStatusWord = paymentEnvironmentStatusWord;
	}

	/**
	 * True when the maximum depth of Directory Definition Files (DDF Name,
	 * tag '9D', EMV 4.4 Book 1 section 12.2) stopped the walk of the sub
	 * directories
	 *
	 * @return true when the walk of the sub directories was stopped by the depth limit
	 */
	public boolean isDirectoryDepthLimitReached() {
		return directoryDepthLimitReached;
	}

	/**
	 * Setter for the field directoryDepthLimitReached
	 *
	 * @param directoryDepthLimitReached
	 *            the directoryDepthLimitReached to set
	 */
	public void setDirectoryDepthLimitReached(final boolean directoryDepthLimitReached) {
		this.directoryDepthLimitReached = directoryDepthLimitReached;
	}

	/**
	 * True when the maximum number of Directory Definition Files (DDF Name,
	 * tag '9D') stopped the walk of the sub directories
	 *
	 * @return true when the walk of the sub directories was stopped by the count limit
	 */
	public boolean isDirectoryCountLimitReached() {
		return directoryCountLimitReached;
	}

	/**
	 * Setter for the field directoryCountLimitReached
	 *
	 * @param directoryCountLimitReached
	 *            the directoryCountLimitReached to set
	 */
	public void setDirectoryCountLimitReached(final boolean directoryCountLimitReached) {
		this.directoryCountLimitReached = directoryCountLimitReached;
	}

	/**
	 * DDF Names (tag '9D') listed by the card but not selected: already
	 * visited, or the depth or count limit reached. Never null.
	 *
	 * @return the DDF Names not selected, never null
	 */
	public List<byte[]> getUnreadDdfNames() {
		return unreadDdfNames;
	}

	/**
	 * Setter for the field unreadDdfNames
	 *
	 * @param unreadDdfNames
	 *            the unreadDdfNames to set, null resets to an empty list
	 */
	public void setUnreadDdfNames(final List<byte[]> unreadDdfNames) {
		this.unreadDdfNames = unreadDdfNames != null ? unreadDdfNames : new ArrayList<byte[]>();
	}

	/**
	 * Application Templates (tag '61') that carried neither an ADF Name
	 * (tag '4F') nor a DDF Name (tag '9D') and therefore describe nothing;
	 * {@link #getDirectoryEntries()} does not list them. Never null.
	 *
	 * @return the entries that describe nothing, never null
	 */
	public List<DirectoryEntry> getIgnoredDirectoryEntries() {
		return ignoredDirectoryEntries;
	}

	/**
	 * Setter for the field ignoredDirectoryEntries
	 *
	 * @param ignoredDirectoryEntries
	 *            the ignoredDirectoryEntries to set, null resets to an empty list
	 */
	public void setIgnoredDirectoryEntries(final List<DirectoryEntry> ignoredDirectoryEntries) {
		this.ignoredDirectoryEntries = ignoredDirectoryEntries != null ? ignoredDirectoryEntries : new ArrayList<DirectoryEntry>();
	}

	/**
	 * When {@link #getState()} is LOCKED: status bytes SW1 SW2 answered to
	 * the SELECT of the highest priority application ('6283' application
	 * blocked, '6A81' card blocked, '6985'...), four upper-case hexadecimal
	 * characters. Null otherwise.
	 *
	 * @return the status word that locked the card, or null
	 */
	public String getLockedStatusWord() {
		return lockedStatusWord;
	}

	/**
	 * Setter for the field lockedStatusWord
	 *
	 * @param lockedStatusWord
	 *            the lockedStatusWord to set
	 */
	public void setLockedStatusWord(final String lockedStatusWord) {
		this.lockedStatusWord = lockedStatusWord;
	}

	/**
	 * True when the applications were found by the list of known AIDs
	 * (selection by AID, EMV 4.4 Book 1 section 12.3.3) because the payment
	 * environment could not be read
	 *
	 * @return true when the applications were found by the list of AIDs
	 */
	public boolean isReadWithAid() {
		return readWithAid;
	}

	/**
	 * Setter for the field readWithAid
	 *
	 * @param readWithAid
	 *            the readWithAid to set
	 */
	public void setReadWithAid(final boolean readWithAid) {
		this.readWithAid = readWithAid;
	}

	/**
	 * First Track 1 Data (tag '56') value that could not be parsed (pattern
	 * mismatch, unusable expiration date '^'...), null when none
	 *
	 * @return the unparsed Track 1 Data, or null when every track 1 was parsed
	 */
	public byte[] getUnparsedTrack1() {
		return unparsedTrack1;
	}

	/**
	 * Setter for the field unparsedTrack1
	 *
	 * @param unparsedTrack1
	 *            the unparsedTrack1 to set
	 */
	public void setUnparsedTrack1(final byte[] unparsedTrack1) {
		this.unparsedTrack1 = unparsedTrack1;
	}

	/**
	 * First Track 2 Equivalent Data (tag '57') or Track 2 Data (tag '9F6B')
	 * value that could not be parsed, null when none
	 *
	 * @return the unparsed track 2, or null when every track 2 was parsed
	 */
	public byte[] getUnparsedTrack2() {
		return unparsedTrack2;
	}

	/**
	 * Setter for the field unparsedTrack2
	 *
	 * @param unparsedTrack2
	 *            the unparsedTrack2 to set
	 */
	public void setUnparsedTrack2(final byte[] unparsedTrack2) {
		this.unparsedTrack2 = unparsedTrack2;
	}

	/**
	 * First Track 1 Discretionary Data (tag '9F1F') received while the card
	 * had no track 1 to attach it to, null when none
	 *
	 * @return the orphan Track 1 Discretionary Data, or null
	 */
	public byte[] getOrphanTrack1DiscretionaryData() {
		return orphanTrack1DiscretionaryData;
	}

	/**
	 * Setter for the field orphanTrack1DiscretionaryData
	 *
	 * @param orphanTrack1DiscretionaryData
	 *            the orphanTrack1DiscretionaryData to set
	 */
	public void setOrphanTrack1DiscretionaryData(final byte[] orphanTrack1DiscretionaryData) {
		this.orphanTrack1DiscretionaryData = orphanTrack1DiscretionaryData;
	}

	/**
	 * First Track 2 Discretionary Data (tag '9F20') received while the card
	 * had no track 2 to attach it to, null when none
	 *
	 * @return the orphan Track 2 Discretionary Data, or null
	 */
	public byte[] getOrphanTrack2DiscretionaryData() {
		return orphanTrack2DiscretionaryData;
	}

	/**
	 * Setter for the field orphanTrack2DiscretionaryData
	 *
	 * @param orphanTrack2DiscretionaryData
	 *            the orphanTrack2DiscretionaryData to set
	 */
	public void setOrphanTrack2DiscretionaryData(final byte[] orphanTrack2DiscretionaryData) {
		this.orphanTrack2DiscretionaryData = orphanTrack2DiscretionaryData;
	}

	/**
	 * Bytes returned by the provider for the Answer To Select (ATS
	 * historical bytes or hi-layer response in contactless, ISO 7816-3) or
	 * the Answer To Reset (ATR in contact), the source of {@link #getAt()}.
	 * Null when the provider returned none.
	 *
	 * @return the raw ATS or ATR, or null
	 */
	public byte[] getRawAt() {
		return rawAt;
	}

	/**
	 * Setter for the field rawAt
	 *
	 * @param rawAt
	 *            the rawAt to set
	 */
	public void setRawAt(final byte[] rawAt) {
		this.rawAt = rawAt;
	}

	/**
	 * True when the lookup of the ATR/ATS description ran; a null or empty
	 * {@link #getAtrDescription()} then means that the answer was looked up
	 * and is unknown
	 *
	 * @return true when the description lookup ran
	 */
	public boolean isAtLookedUp() {
		return atLookedUp;
	}

	/**
	 * Setter for the field atLookedUp
	 *
	 * @param atLookedUp
	 *            the atLookedUp to set
	 */
	public void setAtLookedUp(final boolean atLookedUp) {
		this.atLookedUp = atLookedUp;
	}

	/**
	 * Full response (data and status bytes) to the GET DATA of the Card
	 * Production Life Cycle data (tag '9F7F', Global Platform Card
	 * Specification), stored whatever its length so that a rejected answer
	 * stays visible. Null when the CPLC was not requested.
	 *
	 * @return the full CPLC response, or null
	 */
	public byte[] getRawCplcResponse() {
		return rawCplcResponse;
	}

	/**
	 * Setter for the field rawCplcResponse
	 *
	 * @param rawCplcResponse
	 *            the rawCplcResponse to set
	 */
	public void setRawCplcResponse(final byte[] rawCplcResponse) {
		this.rawCplcResponse = rawCplcResponse;
	}

	/**
	 * Status bytes SW1 SW2 of the GET DATA '9F7F' response, four upper-case
	 * hexadecimal characters. Null when the CPLC was not requested.
	 *
	 * @return the status word of the CPLC request, or null
	 */
	public String getCplcStatusWord() {
		return cplcStatusWord;
	}

	/**
	 * Setter for the field cplcStatusWord
	 *
	 * @param cplcStatusWord
	 *            the cplcStatusWord to set
	 */
	public void setCplcStatusWord(final String cplcStatusWord) {
		this.cplcStatusWord = cplcStatusWord;
	}

	/**
	 * Scheme resolved from the AID of the application that set {@link
	 * #getType()} (the registered application provider identifier, EMV 4.4
	 * Book 1 section 12.2.1), before the CB scheme is replaced by the
	 * scheme of the PAN. Null when no application set the type.
	 *
	 * @return the scheme named by the AID, or null
	 */
	public EmvCardScheme getAidType() {
		return aidType;
	}

	/**
	 * Setter for the field aidType
	 *
	 * @param aidType
	 *            the aidType to set
	 */
	public void setAidType(final EmvCardScheme aidType) {
		this.aidType = aidType;
	}

	/**
	 * AID (DF Name, tag '84', or requested AID) of the application whose
	 * reading set {@link #getType()}, null when none did
	 *
	 * @return the AID of the application that set the type, or null
	 */
	public byte[] getTypeAid() {
		return typeAid;
	}

	/**
	 * Setter for the field typeAid
	 *
	 * @param typeAid
	 *            the typeAid to set
	 */
	public void setTypeAid(final byte[] typeAid) {
		this.typeAid = typeAid;
	}

	/**
	 * Identification of the card computed at the end of the reading from
	 * every clue read (AID, PAN, BIC, IBAN, issuer country, ATR
	 * description...). Null before a read.
	 *
	 * @return the identification, or null before a read
	 */
	public CardIdentification getIdentification() {
		return identification;
	}

	/**
	 * Setter for the field identification
	 *
	 * @param identification
	 *            the identification to set
	 */
	public void setIdentification(final CardIdentification identification) {
		this.identification = identification;
	}

}
