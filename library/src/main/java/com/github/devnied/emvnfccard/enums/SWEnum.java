package com.github.devnied.emvnfccard.enums;

/**
 * List of status word (last 2 bytes) in APDU response
 * 
 * @author julien Millau
 * 
 */
public enum SWEnum {

	SW_SUCCESS(new byte[] { (byte) 0x90, (byte) 0x00 }, "success"),
	SW_DATA_FAILURE(new byte[] { (byte) 0x62, (byte) 0x81 }, "success, response can be fetched by GET RESPONSE"),
	SW_FILEEND_REACHED(new byte[] { (byte) 0x62, (byte) 0x82 }, "file end reached, could not read LE bytes"),
	SW_FILE_LOCKED(new byte[] { (byte) 0x62, (byte) 0x83 }, "file is locked"),
	SW_FILEINFO_ISO_FAILURE(new byte[] { (byte) 0x62, (byte) 0x84 }, "file info FCI is not ISO conform"),
	SW_MEMORY_ERROR(new byte[] { (byte) 0x65, (byte) 0x81 }, "memory error"),
	SW_LENGTH_ERROR(new byte[] { (byte) 0x67, (byte) 0x00 }, "length error, lc or le incorrect"),
	SW_FUNC_CLASS_BYTE_NOT_SUPPORTED(new byte[] { (byte) 0x68, (byte) 0x00 }, "function in class byte not supported"),
	SW_LOGIC_CHAN_NOT_SUPPORTED(new byte[] { (byte) 0x68, (byte) 0x81 }, "logical channels not supported"),
	SW_SEC_MSG_NOT_SUPPORTED(new byte[] { (byte) 0x68, (byte) 0x82 }, "secure messaging not supported"),
	SW_CMD_NOT_ALLOWED(new byte[] { (byte) 0x69, (byte) 0x00 }, "command not allowed"),
	SW_CMD_INCOMPATIBLE(new byte[] { (byte) 0x69, (byte) 0x81 }, "command incompatible with file system structure"),
	SW_SEC_STATE_NOT_FULFILLED(new byte[] { (byte) 0x69, (byte) 0x82 }, "security state not fulfilled"),
	SW_AUTH_METHOD_LOCKED(new byte[] { (byte) 0x69, (byte) 0x83 }, "authentication method is locked"),
	SW_REFERENCED_DATA_LOCKED(new byte[] { (byte) 0x69, (byte) 0x84 }, "referenced data is locked"),
	SW_USAGE_COND_NOT_FULFILLED(new byte[] { (byte) 0x69, (byte) 0x85 }, "usage conditions are not fulfilled"),
	SW_CMD_NOT_ALLOWED_NO_EF_SEL(new byte[] { (byte) 0x69, (byte) 0x86 }, "command not allowed (no EF selected)"),
	SW_INCORRECT_PARAMS(new byte[] { (byte) 0x6A, (byte) 0x00 }, "incorrect parameters P1/P2"),
	SW_INCORRECT_DATA(new byte[] { (byte) 0x6A, (byte) 0x80 }, "incorrect data for command"),
	SW_FUNC_NOT_SUPPORTED(new byte[] { (byte) 0x6A, (byte) 0x81 }, "function is not supported"),
	SW_FILE_NOT_FOUND(new byte[] { (byte) 0x6A, (byte) 0x82 }, "file not found"),
	SW_RECORD_NOT_FOUND(new byte[] { (byte) 0x6A, (byte) 0x83 }, "record not found"),
	SW_REFERENCED_DATA_NOT_FOUND(new byte[] { (byte) 0x6A, (byte) 0x88 }, "referenced data (data objects) not found"),
	SW_INCORRECT_PARAMETERS_P1_P2(new byte[] { (byte) 0x6A, (byte) 0x86 }, "incorrect parameters p1/p2"),
	SW_CMD_CLASS_NOT_SUPPORTED(new byte[] { (byte) 0x6E, (byte) 0x00 }, "class not supported"),
	SW_CMD_ABORTED_UNKNOWN_ERR(new byte[] { (byte) 0x6F, (byte) 0x00 }, "command aborted"),
	SW_INS_NOT_SUPPORTED(new byte[] { (byte) 0x6D, (byte) 0x00 }, "Instruction not supported"),
	SW_COMMAND_NOT_ALLOWED(new byte[] { (byte) 0x69, (byte) 0x86 }, "Command not allowed");

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
	private SWEnum(final byte[] pStatus, final String pDetail) {
		status = pStatus;
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
