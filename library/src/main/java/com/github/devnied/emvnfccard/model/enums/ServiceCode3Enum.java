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
package com.github.devnied.emvnfccard.model.enums;

/**
 * Service code, position 2 values.
 */
public enum ServiceCode3Enum implements IKeyEnum {

	NO_RESTRICTION_PIN_REQUIRED(0, "No restrictions", "PIN required"),
	NO_RESTRICTION(1, "No restrictions", "None"),
	GOODS_SERVICES(2, "Goods and services only", "None"),
	ATM_ONLY(3, "ATM only", "PIN required"),
	CASH_ONLY(4, "Cash only", "None"),
	GOODS_SERVICES_PIN_REQUIRED(5, "Goods and services only", "PIN required"),
	NO_RESTRICTION_PIN_IF_PED(6, "No restrictions", "Prompt for PIN if PED present"),
	GOODS_SERVICES_PIN_IF_PED(7, "Goods and services only", "Prompt for PIN if PED present"), ;

	private final int value;
	private final String allowedServices;
	private final String pinRequirements;

	private ServiceCode3Enum(final int value, final String allowedServices, final String pinRequirements) {
		this.value = value;
		this.allowedServices = allowedServices;
		this.pinRequirements = pinRequirements;
	}

	/**
	 * Gets the allowed services.
	 * 
	 * @return Allowed services.
	 */
	public String getAllowedServices() {
		return allowedServices;
	}

	/**
	 * Gets the the PIN requirements.
	 * 
	 * @return PIN requirements.
	 */
	public String getPinRequirements() {
		return pinRequirements;
	}

	@Override
	public int getKey() {
		return value;
	}

}