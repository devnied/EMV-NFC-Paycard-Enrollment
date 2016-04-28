package com.pro100svitlo.creditCardNfcReader.model.enums;

/**
 * Service code, position 1 values.
 */
public enum ServiceCode1Enum implements IKeyEnum {

	INTERNATIONNAL(1, "International interchange", "None"),
	INTERNATIONNAL_ICC(2, "International interchange", "Integrated circuit card"),
	NATIONAL(5, "National interchange", "None"),
	NATIONAL_ICC(6, "National interchange", "Integrated circuit card"),
	PRIVATE(7, "Private", "None");

	private final int value;
	private final String interchange;
	private final String technology;

	/**
	 * Constructor using fields
	 * 
	 * @param value
	 * @param interchange
	 * @param technology
	 */
	private ServiceCode1Enum(final int value, final String interchange, final String technology) {
		this.value = value;
		this.interchange = interchange;
		this.technology = technology;
	}

	/**
	 * Method used to get the field interchange
	 * 
	 * @return the interchange
	 */
	public String getInterchange() {
		return interchange;
	}

	/**
	 * Method used to get the field technology
	 * 
	 * @return the technology
	 */
	public String getTechnology() {
		return technology;
	}

	@Override
	public int getKey() {
		return value;
	}

}