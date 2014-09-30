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
package com.github.devnied.emvnfccard.enums;

import fr.devnied.bitlib.BytesUtils;

/**
 * List of status word (last 2 bytes) in APDU response<br/>
 * https://www.eftlab.co.uk/index.php/site-map/knowledge-base/118-apdu-response-list
 * 
 * @author MILLAU Julien
 * 
 */
public enum SwEnum {

	SW_61("61", "Command successfully executed; 'XX' bytes of data are available and can be requested using GET RESPONSE"),
	SW_6200("6200", "No information given (NV-Ram not changed)"),
	SW_6201("6201", "NV-Ram not changed 1"),
	SW_6281("6281", "Part of returned data may be corrupted"),
	SW_6282("6282", "End of file/record reached before reading Le bytes"),
	SW_6283("6283", "Selected file invalidated"),
	SW_6284("6284", "Selected file is not valid. FCI not formated according to ISO"),
	SW_6285("6285", "Selected file is in termination state"),
	SW_62A2("62A2", "Wrong R-MAC"),
	SW_62A4("62A4", "Card locked (during reset( ))"),
	SW_62F1("62F1", "Wrong C-MAC"),
	SW_62F3("62F3", "Internal reset"),
	SW_62F5("62F5", "Default agent locked"),
	SW_62F7("62F7", "Cardholder locked"),
	SW_62F8("62F8", "Basement is current agent"),
	SW_62F9("62F9", "CALC Key Set not unblocked"),
	SW_62("62", "RFU"),
	SW_6300("6300", "No information given (NV-Ram changed)"),
	SW_6381("6381", "File filled up by the last write. Loading/updating is not allowed"),
	SW_6382("6382", "Card key not supported"),
	SW_6383("6383", "Reader key not supported"),
	SW_6384("6384", "Plaintext transmission not supported"),
	SW_6385("6385", "Secured transmission not supported"),
	SW_6386("6386", "Volatile memory is not available"),
	SW_6387("6387", "Non-volatile memory is not available"),
	SW_6388("6388", "Key number not valid"),
	SW_6389("6389", "Key length is not correct"),
	SW_63C0("63C0", "Verify fail, no try left"),
	SW_63C1("63C1", "Verify fail, 1 try left"),
	SW_63C2("63C2", "Verify fail, 2 tries left"),
	SW_63C3("63C3", "Verify fail, 3 tries left"),
	SW_63("63", "RFU"),
	SW_6400("6400", "No information given (NV-Ram not changed)"),
	SW_6401("6401", "Command timeout"),
	SW_64("64", "RFU"),
	SW_6500("6500", "No information given"),
	SW_6501(
			"6501",
			"Write error. Memory failure. There have been problems in writing or reading the EEPROM. Other hardware problems may also bring this error"),
	SW_6581("6581", "Memory failure"),
	SW_65("65", "RFU"),
	SW_6669("6669", "Incorrect Encryption/Decryption Padding"),
	SW_66("66", "-"),
	SW_6700("6700", "Wrong length"),
	SW_67("67", "length incorrect (procedure)(ISO 7816-3)"),
	SW_6800("6800", "No information given (The request function is not supported by the card)"),
	SW_6881("6881", "Logical channel not supported"),
	SW_6882("6882", "Secure messaging not supported"),
	SW_6883("6883", "Last command of the chain expected"),
	SW_6884("6884", "Command chaining not supported"),
	SW_68("68", "RFU"),
	SW_6900("6900", "No information given (Command not allowed)"),
	SW_6981("6981", "Command incompatible with file structure"),
	SW_6982("6982", "Security condition not satisfied"),
	SW_6983("6983", "Authentication method blocked"),
	SW_6984("6984", "Referenced data reversibly blocked (invalidated)"),
	SW_6985("6985", "Conditions of use not satisfied"),
	SW_6986("6986", "Command not allowed (no current EF)"),
	SW_6987("6987", "Expected secure messaging (SM) object missing"),
	SW_6988("6988", "Incorrect secure messaging (SM) data object"),
	SW_6996("6996", "Data must be updated again"),
	SW_69F0("69F0", "Permission Denied"),
	SW_69F1("69F1", "Permission Denied - Missing Privilege"),
	SW_69("69", "RFU"),
	SW_6A00("6A00", "No information given (Bytes P1 and/or P2 are incorrect)"),
	SW_6A80("6A80", "The parameters in the data field are incorrect"),
	SW_6A81("6A81", "Function not supported"),
	SW_6A82("6A82", "File not found"),
	SW_6A83("6A83", "Record not found"),
	SW_6A84("6A84", "There is insufficient memory space in record or file"),
	SW_6A85("6A85", "Lc inconsistent with TLV structure"),
	SW_6A86("6A86", "Incorrect P1 or P2 parameter"),
	SW_6A87("6A87", "Lc inconsistent with P1-P2"),
	SW_6A88("6A88", "Referenced data not found"),
	SW_6A89("6A89", "File already exists"),
	SW_6A8A("6A8A", "DF name already exists"),
	SW_6AF0("6AF0", "Wrong parameter value"),
	SW_6A("6A", "RFU"),
	SW_6B00("6B00", "Wrong parameter(s) P1-P2"),
	SW_6B("6B", "Reference incorrect (procedure byte), (ISO 78163)"),
	SW_6C00("6C00", "Incorrect P3 length"),
	SW_6C("6C", "xx = exact Le"),
	SW_6D00("6D00", "Instruction code not supported or invalid"),
	SW_6D("6D", "Instruction code not programmed or invalid (procedure byte), (ISO 7816-3)"),
	SW_6E00("6E00", "Class not supported"),
	SW_6E("6E", "Instruction class not supported (procedure byte), (ISO 7816-3)"),
	SW_6F00("6F00", "Command aborted - more exact diagnosis not possible (e.g., operating system error)"),
	SW_6FFF("6FFF", "Card dead (overuse, …)"),
	SW_6F("6F", "No precise diagnosis (procedure byte), (ISO 7816-3)"),
	SW_9000("9000", "Command successfully executed (OK)"),
	SW_9004("9004", "PIN not succesfully verified, 3 or more PIN tries left"),
	SW_9008("9008", "Key/file not found"),
	SW_9080("9080", "Unblock Try Counter has reached zero"),
	SW_9101("9101", "States.activity, States.lock Status or States.lockable has wrong value"),
	SW_9102("9102", "Transaction number reached its limit"),
	SW_9210("9210", "No more storage available"),
	SW_9301("9301", "Integrity error"),
	SW_9302("9302", "Candidate S2 invalid"),
	SW_9401("9401", "Candidate currency code does not match purse currency"),
	SW_9402("9402", "Candidate amount too high"),
	SW_9403("9403", "Candidate amount too low"),
	SW_9405("9405", "Problems in the data field"),
	SW_9407("9407", "Bad currency : purse engine has no slot with R3bc currency"),
	SW_9408("9408", "R3bc currency not supported in purse engine"),
	SW_9580("9580", "Bad sequence"),
	SW_9681("9681", "Slave not found"),
	SW_9700("9700", "PIN blocked and Unblock Try Counter is 1 or 2"),
	SW_9702("9702", "Main keys are blocked"),
	SW_9704("9704", "PIN not succesfully verified, 3 or more PIN tries left"),
	SW_9784("9784", "Base key"),
	SW_9785("9785", "Limit exceeded - C-MAC key"),
	SW_9786("9786", "SM error - Limit exceeded - R-MAC key"),
	SW_9787("9787", "Limit exceeded - sequence counter"),
	SW_9788("9788", "Limit exceeded - R-MAC length"),
	SW_9789("9789", "Service not available"),
	SW_9804("9804", "Access conditions not satisfied"),
	SW_9900("9900", "1 PIN try left"),
	SW_9904("9904", "PIN not succesfully verified, 1 PIN try left"),
	SW_9985("9985", "Wrong status - Cardholder lock"),
	SW_9986("9986", "Missing privilege"),
	SW_9987("9987", "PIN is not installed"),
	SW_9988("9988", "Wrong status - R-MAC state"),
	SW_9A00("9A00", "2 PIN try left"),
	SW_9A04("9A04", "PIN not succesfully verified, 2 PIN try left"),
	SW_9A71("9A71", "Wrong parameter value - Double agent AID"),
	SW_9A72("9A72", "Wrong parameter value - Double agent Type"),
	SW_9D05("9D05", "Incorrect certificate type"),
	SW_9D07("9D07", "Incorrect session data size"),
	SW_9D08("9D08", "Incorrect DIR file record size"),
	SW_9D09("9D09", "Incorrect FCI record size"),
	SW_9D0A("9D0A", "Incorrect code size"),
	SW_9D10("9D10", "Insufficient memory to load application"),
	SW_9D11("9D11", "Invalid AID"),
	SW_9D12("9D12", "Duplicate AID"),
	SW_9D13("9D13", "Application previously loaded"),
	SW_9D14("9D14", "Application history list full"),
	SW_9D15("9D15", "Application not open"),
	SW_9D17("9D17", "Invalid offset"),
	SW_9D18("9D18", "Application already loaded"),
	SW_9D19("9D19", "Invalid certificate"),
	SW_9D1A("9D1A", "Invalid signature"),
	SW_9D1B("9D1B", "Invalid KTU"),
	SW_9D1D("9D1D", "MSM controls not set"),
	SW_9D1E("9D1E", "Application signature does not exist"),
	SW_9D1F("9D1F", "KTU does not exist"),
	SW_9D20("9D20", "Application not loaded"),
	SW_9D21("9D21", "Invalid Open command data length"),
	SW_9D30("9D30", "Check data parameter is incorrect (invalid start address)"),
	SW_9D31("9D31", "Check data parameter is incorrect (invalid length)"),
	SW_9D32("9D32", "Check data parameter is incorrect (illegal memory check area)"),
	SW_9D40("9D40", "Invalid MSM Controls ciphertext"),
	SW_9D41("9D41", "MSM controls already set"),
	SW_9D42("9D42", "Set MSM Controls data length less than 2 bytes"),
	SW_9D43("9D43", "Invalid MSM Controls data length"),
	SW_9D44("9D44", "Excess MSM Controls ciphertext"),
	SW_9D45("9D45", "Verification of MSM Controls data failed"),
	SW_9D50("9D50", "Invalid MCD Issuer production ID"),
	SW_9D51("9D51", "Invalid MCD Issuer ID"),
	SW_9D52("9D52", "Invalid set MSM controls data date"),
	SW_9D53("9D53", "Invalid MCD number"),
	SW_9D54("9D54", "Reserved field error"),
	SW_9D55("9D55", "Reserved field error"),
	SW_9D56("9D56", "Reserved field error"),
	SW_9D57("9D57", "Reserved field error"),
	SW_9D60("9D60", "MAC verification failed"),
	SW_9D61("9D61", "Maximum number of unblocks reached"),
	SW_9D62("9D62", "Card was not blocked"),
	SW_9D63("9D63", "Crypto functions not available"),
	SW_9D64("9D64", "No application loaded"),
	SW_9E00("9E00", "PIN not installed"),
	SW_9E04("9E04", "PIN not succesfully verified, PIN not installed"),
	SW_9F00("9F00", "PIN blocked and Unblock Try Counter is 3"),
	SW_9F04("9F04", "PIN not succesfully verified, PIN blocked and Unblock Try Counter is 3");

	/**
	 * Status
	 */
	private final byte[] status;

	/**
	 * Status word detail
	 */
	private final String detail;

	/**
	 * Constructor using field
	 * 
	 * @param pStatus
	 *            status word
	 */
	private SwEnum(final String pStatus, final String pDetail) {
		status = BytesUtils.fromString(pStatus);
		detail = pDetail;
	}

	/**
	 * Method used to get the field status
	 * 
	 * @return the status
	 */
	public byte[] getStatus() {
		return status;
	}

	/**
	 * Method used to get the field detail
	 * 
	 * @return the detail
	 */
	public String getDetail() {
		return detail;
	}

	/**
	 * Method used to get the Status word
	 * 
	 * @param pData
	 *            bytes array
	 * @return the status word
	 */
	public static SwEnum getSW(final byte[] pData) {
		SwEnum ret = null;
		if (pData != null && pData.length >= 2) {
			for (SwEnum val : values()) {
				if (val.status.length == 1 && pData[pData.length - 2] == val.status[0]
						|| pData[pData.length - 2] == val.status[0] && pData[pData.length - 1] == val.status[1]) {
					ret = val;
					break;
				}
			}
		}
		return ret;
	}

}
