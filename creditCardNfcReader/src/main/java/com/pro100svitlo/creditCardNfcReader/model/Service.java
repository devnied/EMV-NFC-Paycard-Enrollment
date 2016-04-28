package com.pro100svitlo.creditCardNfcReader.model;

import com.pro100svitlo.creditCardNfcReader.utils.BytesUtils;
import com.pro100svitlo.creditCardNfcReader.model.enums.ServiceCode1Enum;
import com.pro100svitlo.creditCardNfcReader.model.enums.ServiceCode2Enum;
import com.pro100svitlo.creditCardNfcReader.model.enums.ServiceCode3Enum;
import com.pro100svitlo.creditCardNfcReader.utils.EnumUtils;

import org.apache.commons.lang3.StringUtils;

import fr.devnied.bitlib.BitUtils;

/**
 * Track 2 service
 * 
 */
public class Service extends AbstractData{

	/**
	 * Generated serial UID
	 */
	private static final long serialVersionUID = 5154895810563519768L;

	/**
	 * Service code 1
	 */
	private ServiceCode1Enum serviceCode1;

	/**
	 * Service code 2
	 */
	private ServiceCode2Enum serviceCode2;

	/**
	 * Service code 3
	 */
	private ServiceCode3Enum serviceCode3;

	/**
	 * Constructor with service bytes array parameter
	 * 
	 * @param pData
	 *            service as byte array
	 */
	public Service(final String pData) {
		if (pData != null && pData.length() == 3) {
			BitUtils bit = new BitUtils(BytesUtils.fromString(StringUtils.rightPad(pData, 4, "0")));
			serviceCode1 = EnumUtils.getValue(bit.getNextInteger(4), ServiceCode1Enum.class);
			serviceCode2 = EnumUtils.getValue(bit.getNextInteger(4), ServiceCode2Enum.class);
			serviceCode3 = EnumUtils.getValue(bit.getNextInteger(4), ServiceCode3Enum.class);
		}
	}

	/**
	 * Method used to get the field serviceCode1
	 * 
	 * @return the serviceCode1
	 */
	public ServiceCode1Enum getServiceCode1() {
		return serviceCode1;
	}

	/**
	 * Method used to get the field serviceCode2
	 * 
	 * @return the serviceCode2
	 */
	public ServiceCode2Enum getServiceCode2() {
		return serviceCode2;
	}

	/**
	 * Method used to get the field serviceCode3
	 * 
	 * @return the serviceCode3
	 */
	public ServiceCode3Enum getServiceCode3() {
		return serviceCode3;
	}

}
