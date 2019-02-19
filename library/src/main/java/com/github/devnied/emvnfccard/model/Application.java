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

import java.util.List;

import com.github.devnied.emvnfccard.model.enums.ApplicationStepEnum;

/**
 * Class used to describe Application
 *
 * @author MILLAU Julien
 *
 */
public class Application extends AbstractData implements Comparable<Application> {

	/**
	 * Generated serial UID
	 */
	private static final long serialVersionUID = 2917341864815087679L;

	/**
	 * Card AID
	 */
	private byte[] aid;
	
	/**
	 * Reading step
	 */
	private ApplicationStepEnum readingStep =  ApplicationStepEnum.NOT_SELECTED;

	/**
	 * Application label
	 */
	private String applicationLabel;

	/**
	 * Transaction counter ATC
	 */
	private int transactionCounter = UNKNOWN;

	/**
	 * Left PIN try
	 */
	private int leftPinTry = UNKNOWN;

	/**
	 * Application priority
	 */
	private int priority = 1;
	
	/**
	 * Application amount
	 */
	private float amount = UNKNOWN;

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
		if( aid != null) {
			this.aid = aid;
		}
	}

	/**
	 * Method used to get the field priority
	 *
	 * @return the priority
	 */
	public int getPriority() {
		return priority;
	}

	/**
	 * Setter for the field priority
	 *
	 * @param priority
	 *            the priority to set
	 */
	public void setPriority(final int priority) {
		this.priority = priority;
	}

	@Override
	public int compareTo(final Application arg0) {
		return priority - arg0.getPriority();
	}

	/**
	 * Get the field readingStep
	 * @return the readingStep
	 */
	public ApplicationStepEnum getReadingStep() {
		return readingStep;
	}

	/**
	 * Setter for the field readingStep
	 *
	 * @param readingStep the readingStep to set
	 */
	public void setReadingStep(ApplicationStepEnum readingStep) {
		this.readingStep = readingStep;
	}

	/**
	 * Get the field amount
	 * @return the amount
	 */
	public float getAmount() {
		return amount;
	}

	/**
	 * Setter for the field amount
	 *
	 * @param amount the amount to set
	 */
	public void setAmount(float amount) {
		this.amount = amount;
	}
}
