package com.pro100svitlo.creditCardNfcReader.model.enums;

/**
 * Transaction type
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
