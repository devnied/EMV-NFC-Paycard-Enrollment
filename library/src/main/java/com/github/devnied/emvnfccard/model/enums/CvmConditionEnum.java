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
package com.github.devnied.emvnfccard.model.enums;

/**
 * Cardholder Verification Method (CVM) condition code.<br>
 * Second byte of a CV Rule (EMV 4.3 Book 3, Annex C3).
 *
 * @author MILLAU Julien
 *
 */
public enum CvmConditionEnum implements IKeyEnum {

	ALWAYS(0x00, "Always"), //
	UNATTENDED_CASH(0x01, "If unattended cash"), //
	NOT_UNATTENDED_NOT_MANUAL_NOT_CASHBACK(0x02,
			"If not unattended cash and not manual cash and not purchase with cashback"), //
	TERMINAL_SUPPORTS_CVM(0x03, "If terminal supports the CVM"), //
	MANUAL_CASH(0x04, "If manual cash"), //
	PURCHASE_WITH_CASHBACK(0x05, "If purchase with cashback"), //
	UNDER_X(0x06, "If transaction is in the application currency and is under X value"), //
	OVER_X(0x07, "If transaction is in the application currency and is over X value"), //
	UNDER_Y(0x08, "If transaction is in the application currency and is under Y value"), //
	OVER_Y(0x09, "If transaction is in the application currency and is over Y value");

	/**
	 * Condition code
	 */
	private final int key;

	/**
	 * Condition label
	 */
	private final String label;

	/**
	 * Constructor using fields
	 *
	 * @param pKey
	 *            condition code
	 * @param pLabel
	 *            condition label
	 */
	private CvmConditionEnum(final int pKey, final String pLabel) {
		key = pKey;
		label = pLabel;
	}

	@Override
	public int getKey() {
		return key;
	}

	/**
	 * Get the field label
	 *
	 * @return the label
	 */
	public String getLabel() {
		return label;
	}

	@Override
	public String toString() {
		return label;
	}

}
