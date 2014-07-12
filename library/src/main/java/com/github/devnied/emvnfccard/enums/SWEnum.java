package com.github.devnied.emvnfccard.enums;

import fr.devnied.bitlib.BytesUtils;

/**
 * List of status word (last 2 bytes) in APDU response
 * 
 * @author MILLAU Julien
 * 
 */
public enum SWEnum {

	SW_SUCCESS("9000", "success"),
	SW_DATA_FAILURE("6281", "success, response can be fetched by GET RESPONSE"),
	SW_FILEEND_REACHED("6282", "file end reached, could not read LE bytes"),
	SW_FILE_LOCKED("6283", "file is locked"),
	SW_FILEINFO_ISO_FAILURE("6284", "file info FCI is not ISO conform"),
	SW_MEMORY_ERROR("6581", "memory error"),
	SW_LENGTH_ERROR("6700", "length error, lc or le incorrect"),
	SW_FUNC_CLASS_BYTE_NOT_SUPPORTED("6800", "function in class byte not supported"),
	SW_LOGIC_CHAN_NOT_SUPPORTED("6881", "logical channels not supported"),
	SW_SEC_MSG_NOT_SUPPORTED("6882", "secure messaging not supported"),
	SW_CMD_NOT_ALLOWED("6900", "command not allowed"),
	SW_CMD_INCOMPATIBLE("6981", "command incompatible with file system structure"),
	SW_SEC_STATE_NOT_FULFILLED("6982", "security state not fulfilled"),
	SW_AUTH_METHOD_LOCKED("6983", "authentication method is locked"),
	SW_REFERENCED_DATA_LOCKED("6984", "referenced data is locked"),
	SW_USAGE_COND_NOT_FULFILLED("6985", "usage conditions are not fulfilled"),
	SW_CMD_NOT_ALLOWED_NO_EF_SEL("6986", "command not allowed (no EF selected)"),
	SW_INCORRECT_PARAMS("6A00", "incorrect parameters P1/P2"),
	SW_INCORRECT_DATA("6A80", "incorrect data for command"),
	SW_FUNC_NOT_SUPPORTED("6A81", "function is not supported"),
	SW_FILE_NOT_FOUND("6A82", "file not found"),
	SW_RECORD_NOT_FOUND("6A83", "record not found"),
	SW_REFERENCED_DATA_NOT_FOUND("6A88", "referenced data (data objects) not found"),
	SW_INCORRECT_PARAMETERS_P1_P2("6A86", "incorrect parameters p1/p2"),
	SW_CMD_CLASS_NOT_SUPPORTED("6E00", "class not supported"),
	SW_CMD_ABORTED_UNKNOWN_ERR("6F00", "command aborted"),
	SW_INS_NOT_SUPPORTED("6D00", "Instruction not supported"),
	SW_COMMAND_NOT_ALLOWED("6986", "Command not allowed");

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
	private SWEnum(final String pStatus, final String pDetail) {
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
	public static SWEnum getSW(final byte[] pData) {
		SWEnum ret = null;
		if (pData != null && pData.length >= 2) {
			for (SWEnum val : values()) {
				if (pData[pData.length - 2] == val.status[0] && pData[pData.length - 1] == val.status[1]) {
					ret = val;
					break;
				}
			}
		}
		return ret;
	}

}
