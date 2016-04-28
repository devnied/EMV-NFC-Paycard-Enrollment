package com.pro100svitlo.creditCardNfcReader.model;

/**
 * Class used to describe Application file locator
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
