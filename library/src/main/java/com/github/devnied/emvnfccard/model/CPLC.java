package com.github.devnied.emvnfccard.model;

import java.io.Serializable;
import java.util.Date;

import com.github.devnied.emvnfccard.parser.apdu.annotation.Data;
import com.github.devnied.emvnfccard.parser.apdu.impl.AbstractByteBean;
import com.github.devnied.emvnfccard.parser.apdu.impl.DataFactory;

/**
 * Card Production Life-Cycle Data (CPLC) as defined by the Global Platform Card
 * Specification (GPCS)
 * 
 * @author MILLAU Julien
 *
 */
public class CPLC extends AbstractByteBean<CPLC> implements Serializable {
	
	
	/**
	 * Serila Version UID
	 */
	private static final long serialVersionUID = -7955013273912185280L;
	
	
	public static final int SIZE = 42;
	
	
	@Data(index = 1, size = 16)
	private String ic_fabricator;
	
	@Data(index = 2, size = 16)
	private String ic_type;
	
	@Data(index = 3, size = 16)
	private String os;
	
	@Data(index = 4, size = 16, dateStandard = DataFactory.CPCL_DATE)
	private Date os_release_date;
	
	@Data(index = 5, size = 16)
	private String os_release_level;
	
	@Data(index = 6, size = 16, dateStandard = DataFactory.CPCL_DATE)
	private Date ic_fabric_date;
	
	@Data(index = 7, size = 32)
	private String ic_serial_number;
	
	@Data(index = 8, size = 16)
	private String ic_batch_id;
	
	@Data(index = 9, size = 16)
	private String ic_module_fabricator;
	
	@Data(index = 10, size = 16, dateStandard = DataFactory.CPCL_DATE)
	private Date ic_packaging_date;
	
	@Data(index = 11, size = 16)
	private String icc_manufacturer;
	
	@Data(index = 12, size = 16, dateStandard = DataFactory.CPCL_DATE)
	private Date ic_embedding_date;
	
	@Data(index = 13, size = 16)
	private String preperso_id;
	
	@Data(index = 14, size = 16, dateStandard = DataFactory.CPCL_DATE)
	private Date preperso_date;
	
	@Data(index = 15, size = 32)
	private String preperso_equipment;
	
	@Data(index = 16, size = 16)
	private String perso_id;
	
	@Data(index = 17, size = 16, dateStandard = DataFactory.CPCL_DATE)
	private Date perso_date;
	
	@Data(index = 18, size = 32)
	private String perso_equipment;
	
	

	/**
	 * Get the field ic_fabricator
	 * @return the ic_fabricator
	 */
	public String getIc_fabricator() {
		return ic_fabricator;
	}

	/**
	 * Get the field ic_type
	 * @return the ic_type
	 */
	public String getIc_type() {
		return ic_type;
	}

	/**
	 * Get the field os
	 * @return the os
	 */
	public String getOs() {
		return os;
	}

	/**
	 * Get the field os_release_date
	 * @return the os_release_date
	 */
	public Date getOs_release_date() {
		return os_release_date;
	}

	/**
	 * Get the field os_release_level
	 * @return the os_release_level
	 */
	public String getOs_release_level() {
		return os_release_level;
	}

	/**
	 * Get the field ic_fabric_date
	 * @return the ic_fabric_date
	 */
	public Date getIc_fabric_date() {
		return ic_fabric_date;
	}

	/**
	 * Get the field ic_serial_number
	 * @return the ic_serial_number
	 */
	public String getIc_serial_number() {
		return ic_serial_number;
	}

	/**
	 * Get the field ic_batch_id
	 * @return the ic_batch_id
	 */
	public String getIc_batch_id() {
		return ic_batch_id;
	}

	/**
	 * Get the field ic_module_fabricator
	 * @return the ic_module_fabricator
	 */
	public String getIc_module_fabricator() {
		return ic_module_fabricator;
	}

	/**
	 * Get the field ic_packaging_date
	 * @return the ic_packaging_date
	 */
	public Date getIc_packaging_date() {
		return ic_packaging_date;
	}

	/**
	 * Get the field icc_manufacturer
	 * @return the icc_manufacturer
	 */
	public String getIcc_manufacturer() {
		return icc_manufacturer;
	}

	/**
	 * Get the field ic_embedding_date
	 * @return the ic_embedding_date
	 */
	public Date getIc_embedding_date() {
		return ic_embedding_date;
	}

	/**
	 * Get the field preperso_id
	 * @return the preperso_id
	 */
	public String getPreperso_id() {
		return preperso_id;
	}

	/**
	 * Get the field preperso_date
	 * @return the preperso_date
	 */
	public Date getPreperso_date() {
		return preperso_date;
	}

	/**
	 * Get the field preperso_equipment
	 * @return the preperso_equipment
	 */
	public String getPreperso_equipment() {
		return preperso_equipment;
	}

	/**
	 * Get the field perso_id
	 * @return the perso_id
	 */
	public String getPerso_id() {
		return perso_id;
	}

	/**
	 * Get the field perso_date
	 * @return the perso_date
	 */
	public Date getPerso_date() {
		return perso_date;
	}

	/**
	 * Get the field perso_equipment
	 * @return the perso_equipment
	 */
	public String getPerso_equipment() {
		return perso_equipment;
	}

}
