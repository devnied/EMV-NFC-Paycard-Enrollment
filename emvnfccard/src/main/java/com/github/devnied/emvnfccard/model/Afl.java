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
package com.github.devnied.emvnfccard.model;

/**
 * Class used to describe Application file locator
 * 
 * @author MILLAU Julien
 * 
 */
public class Afl {

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
	 * Offline authentication
	 */
	private boolean offlineAuthentication;

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
	 * Method used to get the field offlineAuthentication
	 * 
	 * @return the offlineAuthentication
	 */
	public boolean isOfflineAuthentication() {
		return offlineAuthentication;
	}

	/**
	 * Setter for the field offlineAuthentication
	 * 
	 * @param offlineAuthentication
	 *            the offlineAuthentication to set
	 */
	public void setOfflineAuthentication(final boolean offlineAuthentication) {
		this.offlineAuthentication = offlineAuthentication;
	}

}
