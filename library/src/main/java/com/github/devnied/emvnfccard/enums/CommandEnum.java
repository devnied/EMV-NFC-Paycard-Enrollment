package com.github.devnied.emvnfccard.enums;

/**
 * Enum which define all EMV apdu
 * 
 * @author MILLAU Julien
 * 
 */
public enum CommandEnum {

	/**
	 * Select command
	 */
	SELECT(0x00, 0xA4, 0x04, 0x00),

	/**
	 * Read record command
	 */
	READ_RECORD(0x00, 0xB2, 0x00, 0x00),

	/**
	 * GPO Command
	 */
	GPO(0x80, 0xA8, 0x00, 0x00),

	/**
	 * GPO Command
	 */
	GET_DATA(0x80, 0xCA, 0x00, 0x00);

	/**
	 * Class byte
	 */
	private int cla = 0x00;

	/**
	 * Instruction byte
	 */
	private int ins = 0x00;

	/**
	 * Parameter 1 byte
	 */
	private int p1 = 0x00;

	/**
	 * Parameter 2 byte
	 */
	private int p2 = 0x00;

	/**
	 * Constructor using field
	 * 
	 * @param cla
	 *            class
	 * @param ins
	 *            instruction
	 * @param p1
	 *            parameter 1
	 * @param p2
	 *            parameter 2
	 */
	private CommandEnum(final int cla, final int ins, final int p1, final int p2) {
		this.cla = cla;
		this.ins = ins;
		this.p1 = p1;
		this.p2 = p2;
	}

	/**
	 * Method used to get the field cla
	 * 
	 * @return the cla
	 */
	public int getCla() {
		return cla;
	}

	/**
	 * Setter for the field cla
	 * 
	 * @param cla
	 *            the cla to set
	 */
	public void setCla(final int cla) {
		this.cla = cla;
	}

	/**
	 * Method used to get the field ins
	 * 
	 * @return the ins
	 */
	public int getIns() {
		return ins;
	}

	/**
	 * Setter for the field ins
	 * 
	 * @param ins
	 *            the ins to set
	 */
	public void setIns(final int ins) {
		this.ins = ins;
	}

	/**
	 * Method used to get the field p1
	 * 
	 * @return the p1
	 */
	public int getP1() {
		return p1;
	}

	/**
	 * Setter for the field p1
	 * 
	 * @param p1
	 *            the p1 to set
	 */
	public void setP1(final int p1) {
		this.p1 = p1;
	}

	/**
	 * Method used to get the field p2
	 * 
	 * @return the p2
	 */
	public int getP2() {
		return p2;
	}

	/**
	 * Setter for the field p2
	 * 
	 * @param p2
	 *            the p2 to set
	 */
	public void setP2(final int p2) {
		this.p2 = p2;
	}

}
