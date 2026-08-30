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
 * Cardholder Verification Method (CVM) code.<br>
 * Bits 6 to 1 of the first byte of a CV Rule (EMV 4.3 Book 3, Annex C3, and
 * EMV 4.4 Book 3, Annex C3 Table 43 for the biometric methods).
 *
 * @author MILLAU Julien
 *
 */
public enum CvmMethodEnum implements IKeyEnum {

	/**
	 * Fail CVM processing
	 */
	FAIL(0x00, "Fail CVM processing"),

	/**
	 * Plaintext PIN verification performed by ICC (offline PIN)
	 */
	PLAINTEXT_PIN(0x01, "Plaintext PIN verification performed by ICC"),

	/**
	 * Enciphered PIN verified online
	 */
	ENCIPHERED_PIN_ONLINE(0x02, "Enciphered PIN verified online"),

	/**
	 * Plaintext PIN verification performed by ICC and signature
	 */
	PLAINTEXT_PIN_AND_SIGNATURE(0x03, "Plaintext PIN verification performed by ICC and signature"),

	/**
	 * Enciphered PIN verification performed by ICC (offline enciphered PIN)
	 */
	ENCIPHERED_PIN_OFFLINE(0x04, "Enciphered PIN verification performed by ICC"),

	/**
	 * Enciphered PIN verification performed by ICC and signature
	 */
	ENCIPHERED_PIN_OFFLINE_AND_SIGNATURE(0x05, "Enciphered PIN verification performed by ICC and signature"),

	/**
	 * Facial biometric verified offline by the ICC (EMV 4.4)
	 */
	FACIAL_BIOMETRIC_OFFLINE(0x06, "Facial biometric verified offline by ICC"),

	/**
	 * Facial biometric verified online (EMV 4.4)
	 */
	FACIAL_BIOMETRIC_ONLINE(0x07, "Facial biometric verified online"),

	/**
	 * Finger biometric verified offline by the ICC (EMV 4.4)
	 */
	FINGER_BIOMETRIC_OFFLINE(0x08, "Finger biometric verified offline by ICC"),

	/**
	 * Finger biometric verified online (EMV 4.4)
	 */
	FINGER_BIOMETRIC_ONLINE(0x09, "Finger biometric verified online"),

	/**
	 * Palm biometric verified offline by the ICC (EMV 4.4)
	 */
	PALM_BIOMETRIC_OFFLINE(0x0A, "Palm biometric verified offline by ICC"),

	/**
	 * Palm biometric verified online (EMV 4.4)
	 */
	PALM_BIOMETRIC_ONLINE(0x0B, "Palm biometric verified online"),

	/**
	 * Iris biometric verified offline by the ICC (EMV 4.4)
	 */
	IRIS_BIOMETRIC_OFFLINE(0x0C, "Iris biometric verified offline by ICC"),

	/**
	 * Iris biometric verified online (EMV 4.4)
	 */
	IRIS_BIOMETRIC_ONLINE(0x0D, "Iris biometric verified online"),

	/**
	 * Voice biometric verified offline by the ICC (EMV 4.4)
	 */
	VOICE_BIOMETRIC_OFFLINE(0x0E, "Voice biometric verified offline by ICC"),

	/**
	 * Voice biometric verified online (EMV 4.4)
	 */
	VOICE_BIOMETRIC_ONLINE(0x0F, "Voice biometric verified online"),


	/**
	 * Signature (paper)
	 */
	SIGNATURE(0x1E, "Signature (paper)"),

	/**
	 * No CVM required
	 */
	NO_CVM(0x1F, "No CVM required"),

	/**
	 * Value reserved for use by the individual payment systems
	 */
	PAYMENT_SYSTEM_SPECIFIC(0x20, "Reserved for use by the individual payment system"),

	/**
	 * Value reserved for use by the issuer
	 */
	ISSUER_SPECIFIC(0x3E, "Reserved for use by the issuer"),

	/**
	 * This value is not available for use
	 */
	NOT_AVAILABLE(0x3F, "Not available for use");

	/**
	 * CVM code
	 */
	private final int key;

	/**
	 * CVM label
	 */
	private final String label;

	/**
	 * Constructor using fields
	 *
	 * @param pKey
	 *            CVM code
	 * @param pLabel
	 *            CVM label
	 */
	private CvmMethodEnum(final int pKey, final String pLabel) {
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

	/**
	 * Indicate whether this method requires a PIN entry
	 *
	 * @return true if the method is a PIN based verification
	 */
	public boolean isPin() {
		return this == PLAINTEXT_PIN || this == ENCIPHERED_PIN_ONLINE || this == PLAINTEXT_PIN_AND_SIGNATURE
				|| this == ENCIPHERED_PIN_OFFLINE || this == ENCIPHERED_PIN_OFFLINE_AND_SIGNATURE;
	}

	@Override
	public String toString() {
		return label;
	}

}
