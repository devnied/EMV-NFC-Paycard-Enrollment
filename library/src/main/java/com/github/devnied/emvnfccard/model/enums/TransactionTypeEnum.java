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
 * Transaction type
 * 
 * @author Millau Julien
 * 
 */
public enum TransactionTypeEnum implements IKeyEnum {

	/**
	 * '00' for a purchase transaction
	 */
	PURCHASE(0x00),
	/**
	 * '01' Cach advance
	 */
	CASH_ADVANCE(0x01),
	/**
	 * '09' for a purchase with cashback
	 */
	CASHBACK(0x09),
	/**
	 * '20' for a refund transaction
	 */
	REFUND(0x20);

	/**
	 * Value
	 */
	private final int value;

	/**
	 * Constructor using field
	 * 
	 * @param value
	 */
	private TransactionTypeEnum(final int value) {
		this.value = value;
	}

	@Override
	public int getKey() {
		return value;
	}
}
