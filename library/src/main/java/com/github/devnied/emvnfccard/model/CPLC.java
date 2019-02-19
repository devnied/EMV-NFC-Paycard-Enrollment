/*
 * Copyright (C) 2019 MILLAU Julien
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
	private Integer ic_fabricator;
	
	@Data(index = 2, size = 16)
	private Integer ic_type;
	
	@Data(index = 3, size = 16)
	private Integer os;
	
	@Data(index = 4, size = 16, dateStandard = DataFactory.CPCL_DATE)
	private Date os_release_date;
	
	@Data(index = 5, size = 16)
	private Integer os_release_level;
	
	@Data(index = 6, size = 16, dateStandard = DataFactory.CPCL_DATE)
	private Date ic_fabric_date;
	
	@Data(index = 7, size = 32)
	private Integer ic_serial_number;
	
	@Data(index = 8, size = 16)
	private Integer ic_batch_id;
	
	@Data(index = 9, size = 16)
	private Integer ic_module_fabricator;
	
	@Data(index = 10, size = 16, dateStandard = DataFactory.CPCL_DATE)
	private Date ic_packaging_date;
	
	@Data(index = 11, size = 16)
	private Integer icc_manufacturer;
	
	@Data(index = 12, size = 16, dateStandard = DataFactory.CPCL_DATE)
	private Date ic_embedding_date;
	
	@Data(index = 13, size = 16)
	private Integer preperso_id;
	
	@Data(index = 14, size = 16, dateStandard = DataFactory.CPCL_DATE)
	private Date preperso_date;
	
	@Data(index = 15, size = 32)
	private Integer preperso_equipment;
	
	@Data(index = 16, size = 16)
	private Integer perso_id;
	
	@Data(index = 17, size = 16, dateStandard = DataFactory.CPCL_DATE)
	private Date perso_date;
	
	@Data(index = 18, size = 32)
	private Integer perso_equipment;
	
	

	/**
	 * Get the field ic_fabricator
	 * @return the ic_fabricator
	 */
	public Integer getIcFabricator() {
		return ic_fabricator;
	}

	/**
	 * Get the field ic_type
	 * @return the ic_type
	 */
	public Integer getIcType() {
		return ic_type;
	}

	/**
	 * Get the field os
	 * @return the os
	 */
	public Integer getOs() {
		return os;
	}

	/**
	 * Get the field os_release_date
	 * @return the os_release_date
	 */
	public Date getOsReleaseDate() {
		return os_release_date;
	}

	/**
	 * Get the field os_release_level
	 * @return the os_release_level
	 */
	public Integer getOsReleaseLevel() {
		return os_release_level;
	}

	/**
	 * Get the field ic_fabric_date
	 * @return the ic_fabric_date
	 */
	public Date getIcFabricDate() {
		return ic_fabric_date;
	}

	/**
	 * Get the field ic_serial_number
	 * @return the ic_serial_number
	 */
	public Integer getIcSerialNumber() {
		return ic_serial_number;
	}

	/**
	 * Get the field ic_batch_id
	 * @return the ic_batch_id
	 */
	public Integer getIcBatchId() {
		return ic_batch_id;
	}

	/**
	 * Get the field ic_module_fabricator
	 * @return the ic_module_fabricator
	 */
	public Integer getIcModuleFabricator() {
		return ic_module_fabricator;
	}

	/**
	 * Get the field ic_packaging_date
	 * @return the ic_packaging_date
	 */
	public Date getIcPackagingDate() {
		return ic_packaging_date;
	}

	/**
	 * Get the field icc_manufacturer
	 * @return the icc_manufacturer
	 */
	public Integer getIccManufacturer() {
		return icc_manufacturer;
	}

	/**
	 * Get the field ic_embedding_date
	 * @return the ic_embedding_date
	 */
	public Date getIcEmbeddingDate() {
		return ic_embedding_date;
	}

	/**
	 * Get the field preperso_id
	 * @return the preperso_id
	 */
	public Integer getPrepersoId() {
		return preperso_id;
	}

	/**
	 * Get the field preperso_date
	 * @return the preperso_date
	 */
	public Date getPrepersoDate() {
		return preperso_date;
	}

	/**
	 * Get the field preperso_equipment
	 * @return the preperso_equipment
	 */
	public Integer getPrepersoEquipment() {
		return preperso_equipment;
	}

	/**
	 * Get the field perso_id
	 * @return the perso_id
	 */
	public Integer getPersoId() {
		return perso_id;
	}

	/**
	 * Get the field perso_date
	 * @return the perso_date
	 */
	public Date getPersoDate() {
		return perso_date;
	}

	/**
	 * Get the field perso_equipment
	 * @return the perso_equipment
	 */
	public Integer getPersoEquipment() {
		return perso_equipment;
	}

}
