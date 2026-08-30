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

import fr.devnied.bitlib.BytesUtils;

/**
 * Application Usage Control (tag '9F07').<br>
 * Indicates issuer specified restrictions on the geographic usage and services
 * allowed for the application (EMV 4.3 Book 3, Annex C2).
 *
 * @author MILLAU Julien
 *
 */
public class ApplicationUsageControl extends AbstractData {

	/**
	 * Generated serial UID
	 */
	private static final long serialVersionUID = -7118111954418038430L;

	/**
	 * Raw AUC value
	 */
	private final byte[] raw;

	private final boolean validForDomesticCash;
	private final boolean validForInternationalCash;
	private final boolean validForDomesticGoods;
	private final boolean validForInternationalGoods;
	private final boolean validForDomesticServices;
	private final boolean validForInternationalServices;
	private final boolean validAtAtm;
	private final boolean validAtTerminalsOtherThanAtm;
	private final boolean domesticCashbackAllowed;
	private final boolean internationalCashbackAllowed;

	/**
	 * Constructor using the 2 bytes AUC value
	 *
	 * @param pRaw
	 *            raw AUC value (2 bytes)
	 */
	public ApplicationUsageControl(final byte[] pRaw) {
		if (pRaw == null || pRaw.length < 2) {
			throw new IllegalArgumentException("Application Usage Control must be 2 bytes long");
		}
		raw = new byte[] { pRaw[0], pRaw[1] };
		validForDomesticCash = BytesUtils.matchBitByBitIndex(raw[0], 7);
		validForInternationalCash = BytesUtils.matchBitByBitIndex(raw[0], 6);
		validForDomesticGoods = BytesUtils.matchBitByBitIndex(raw[0], 5);
		validForInternationalGoods = BytesUtils.matchBitByBitIndex(raw[0], 4);
		validForDomesticServices = BytesUtils.matchBitByBitIndex(raw[0], 3);
		validForInternationalServices = BytesUtils.matchBitByBitIndex(raw[0], 2);
		validAtAtm = BytesUtils.matchBitByBitIndex(raw[0], 1);
		validAtTerminalsOtherThanAtm = BytesUtils.matchBitByBitIndex(raw[0], 0);
		domesticCashbackAllowed = BytesUtils.matchBitByBitIndex(raw[1], 7);
		internationalCashbackAllowed = BytesUtils.matchBitByBitIndex(raw[1], 6);
	}

	/**
	 * Get the field raw
	 *
	 * @return the raw AUC value
	 */
	public byte[] getRaw() {
		return new byte[] { raw[0], raw[1] };
	}

	public boolean isValidForDomesticCash() {
		return validForDomesticCash;
	}

	public boolean isValidForInternationalCash() {
		return validForInternationalCash;
	}

	public boolean isValidForDomesticGoods() {
		return validForDomesticGoods;
	}

	public boolean isValidForInternationalGoods() {
		return validForInternationalGoods;
	}

	public boolean isValidForDomesticServices() {
		return validForDomesticServices;
	}

	public boolean isValidForInternationalServices() {
		return validForInternationalServices;
	}

	public boolean isValidAtAtm() {
		return validAtAtm;
	}

	public boolean isValidAtTerminalsOtherThanAtm() {
		return validAtTerminalsOtherThanAtm;
	}

	public boolean isDomesticCashbackAllowed() {
		return domesticCashbackAllowed;
	}

	public boolean isInternationalCashbackAllowed() {
		return internationalCashbackAllowed;
	}

	/**
	 * Indicate if the application can be used outside of the issuer country.
	 *
	 * @return true if at least one international usage is allowed
	 */
	public boolean isInternationalUsageAllowed() {
		return validForInternationalCash || validForInternationalGoods || validForInternationalServices;
	}

}
