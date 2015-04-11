/*
 * Copyright (C) 2014 MILLAU Julien
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

import java.util.List;

/**
 * Class used to describe Application
 *
 * @author MILLAU Julien
 *
 */
public class Application extends AbstractData {

	/**
	 * Generated serial UID
	 */
	private static final long serialVersionUID = 2917341864815087679L;

	/**
	 * Card AID
	 */
	private byte[] aid;

	/**
	 * Extended card aid
	 */
	private byte[] extendedAid;

	/**
	 * Application label
	 */
	private String applicationLabel;

	/**
	 * Transaction counter ATC
	 */
	private int transactionCounter;

	/**
	 * Left PIN try
	 */
	private int leftPinTry;

	/**
	 * List of issued payment
	 */
	private List<EmvTransactionRecord> listTransactions;

	/**
	 * Method used to get the field applicationLabel
	 *
	 * @return the applicationLabel
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
	 * Method used to get the field transactionCounter
	 *
	 * @return the transactionCounter
	 */
	public int getTransactionCounter() {
		return transactionCounter;
	}

	/**
	 * Setter for the field transactionCounter
	 *
	 * @param transactionCounter
	 *            the transactionCounter to set
	 */
	public void setTransactionCounter(final int transactionCounter) {
		this.transactionCounter = transactionCounter;
	}

	/**
	 * Method used to get the field leftPinTry
	 *
	 * @return the leftPinTry
	 */
	public int getLeftPinTry() {
		return leftPinTry;
	}

	/**
	 * Setter for the field leftPinTry
	 *
	 * @param leftPinTry
	 *            the leftPinTry to set
	 */
	public void setLeftPinTry(final int leftPinTry) {
		this.leftPinTry = leftPinTry;
	}

	/**
	 * Method used to get the field listTransactions
	 *
	 * @return the listTransactions
	 */
	public List<EmvTransactionRecord> getListTransactions() {
		return listTransactions;
	}

	/**
	 * Setter for the field listTransactions
	 *
	 * @param listTransactions
	 *            the listTransactions to set
	 */
	public void setListTransactions(final List<EmvTransactionRecord> listTransactions) {
		this.listTransactions = listTransactions;
	}

	/**
	 * Method used to get the field aid
	 *
	 * @return the aid
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
	 * Method used to get the field extendedAid
	 *
	 * @return the extendedAid
	 */
	public byte[] getExtendedAid() {
		return extendedAid;
	}

	/**
	 * Setter for the field extendedAid
	 *
	 * @param extendedAid
	 *            the extendedAid to set
	 */
	public void setExtendedAid(final byte[] extendedAid) {
		this.extendedAid = extendedAid;
	}


}
