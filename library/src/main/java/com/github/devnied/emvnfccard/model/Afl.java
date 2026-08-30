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

import java.io.Serializable;

/**
 * Class used to describe Application file locator
 * 
 * @author MILLAU Julien
 * 
 */
public class Afl implements Serializable {

	/**
	 * Generated serial UID
	 */
	private static final long serialVersionUID = -6174118920873566497L;

	/**
	 * SFI
	 */
	private int sfi;

	/**
	 * record
	 */
	private int firstRecord;

	/**
	 * Last record
	 */
	private int lastRecord;

	/**
	 * Number of records, starting from {@link #firstRecord}, that take part in
	 * the offline data authentication (4th byte of the AFL entry)
	 */
	private int offlineAuthenticationRecords;

	/**
	 * Method used to get the field sfi
	 * 
	 * @return the sfi
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
	 * Method used to get the field firstRecord
	 * 
	 * @return the firstRecord
	 */
	public int getFirstRecord() {
		return firstRecord;
	}

	/**
	 * Setter for the field firstRecord
	 * 
	 * @param firstRecord
	 *            the firstRecord to set
	 */
	public void setFirstRecord(final int firstRecord) {
		this.firstRecord = firstRecord;
	}

	/**
	 * Method used to get the field lastRecord
	 * 
	 * @return the lastRecord
	 */
	public int getLastRecord() {
		return lastRecord;
	}

	/**
	 * Setter for the field lastRecord
	 * 
	 * @param lastRecord
	 *            the lastRecord to set
	 */
	public void setLastRecord(final int lastRecord) {
		this.lastRecord = lastRecord;
	}

	/**
	 * Method used to know if any of the records of this AFL entry takes part in
	 * the offline data authentication.
	 *
	 * @return true if {@link #getOfflineAuthenticationRecords()} is not zero
	 */
	public boolean isOfflineAuthentication() {
		return offlineAuthenticationRecords > 0;
	}

	/**
	 * Setter for the field offlineAuthentication
	 *
	 * @param offlineAuthentication
	 *            the offlineAuthentication to set
	 * @deprecated the 4th byte of an AFL entry is the number of records taking
	 *             part in the offline data authentication, not a flag (EMV 4.3
	 *             Book 3 section 10.2). Use
	 *             {@link #setOfflineAuthenticationRecords(int)} instead, this
	 *             method keeps no more than the fact that the count is not
	 *             zero.
	 */
	@Deprecated
	public void setOfflineAuthentication(final boolean offlineAuthentication) {
		setOfflineAuthenticationRecords(offlineAuthentication ? 1 : 0);
	}

	/**
	 * Method used to get the field offlineAuthenticationRecords
	 *
	 * @return the number of records taking part in the offline data
	 *         authentication
	 */
	public int getOfflineAuthenticationRecords() {
		return offlineAuthenticationRecords;
	}

	/**
	 * Setter for the field offlineAuthenticationRecords
	 *
	 * @param offlineAuthenticationRecords
	 *            the offlineAuthenticationRecords to set
	 */
	public void setOfflineAuthenticationRecords(final int offlineAuthenticationRecords) {
		this.offlineAuthenticationRecords = offlineAuthenticationRecords;
	}

	/**
	 * Method used to know if the AFL entry is valid: EMV 4.3 Book 3 section
	 * 10.2 restricts the SFI to the range 1 to 30 and asks for a record range
	 * that is not empty.
	 *
	 * @return true if the entry describes a readable range of records
	 */
	public boolean isValid() {
		return sfi >= 1 && sfi <= 30 && firstRecord >= 1 && lastRecord >= firstRecord;
	}

}
