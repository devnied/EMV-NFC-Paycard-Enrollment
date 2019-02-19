package com.github.devnied.emvnfccard.utils;

import java.text.SimpleDateFormat;

import org.fest.assertions.Assertions;
import org.junit.Test;

import com.github.devnied.emvnfccard.model.CPLC;

import fr.devnied.bitlib.BytesUtils;


public final class CPLCUtilsTest {
	
	
	@Test
	public void testEmptyCPLC(){
		CPLC cplc = CPLCUtils.parse(null);		
		Assertions.assertThat(cplc).isNull();
	}
	
	@Test
	public void testErrorCPLC(){
		CPLC cplc = CPLCUtils.parse(BytesUtils.fromString("69 85"));		
		Assertions.assertThat(cplc).isNull();
	}
	
	@Test
	public void testRawDataCPLC(){
		SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
		// interpret as raw data
		CPLC cplc = CPLCUtils.parse(BytesUtils.fromString("47 90 50 40 47 91 81 02 31 00 83 58 00 11 68 91 45 81 48 12 83 65 00 00 00 00 01 2F 31 30 31 31 36 38 00 00 00 00 00 00 00 00 90 00"));		
		Assertions.assertThat(cplc).isNotNull();
		Assertions.assertThat(cplc.getIcFabricator()).isEqualTo(0x4790);
		Assertions.assertThat(cplc.getIcType()).isEqualTo(0x5040);
		Assertions.assertThat(cplc.getOs()).isEqualTo(0x4791);
		Assertions.assertThat(cplc.getOsReleaseLevel()).isEqualTo(0x3100);
		Assertions.assertThat(cplc.getIcSerialNumber()).isEqualTo(0x00116891);
		Assertions.assertThat(cplc.getIcBatchId()).isEqualTo(0x4581);
		Assertions.assertThat(cplc.getIcModuleFabricator()).isEqualTo(0x4812);
		Assertions.assertThat(cplc.getIccManufacturer()).isEqualTo(0x0000);
		Assertions.assertThat(cplc.getIcEmbeddingDate()).isNull();
		Assertions.assertThat(cplc.getPrepersoId()).isEqualTo(0x012F);
		Assertions.assertThat(cplc.getPrepersoEquipment()).isEqualTo(0x31313638);
		Assertions.assertThat(cplc.getPersoId()).isEqualTo(0x0000);
		Assertions.assertThat(cplc.getPersoEquipment()).isEqualTo(0x0000);
		// Date will change each 10 years
		Assertions.assertThat(format.format(cplc.getOsReleaseDate())).isEqualTo("12/04/2018");
		Assertions.assertThat(format.format(cplc.getIcFabricDate())).isEqualTo("24/12/2018");
		Assertions.assertThat(format.format(cplc.getPrepersoDate())).isEqualTo("10/05/2013");
		Assertions.assertThat(format.format(cplc.getIcPackagingDate())).isEqualTo("31/12/2018");
		Assertions.assertThat(cplc.getPersoDate()).isNull();
		
	}
	
	@Test
	public void testPrependedCPLC(){
		// prepended with CPLC tag
		CPLC cplc = CPLCUtils.parse(BytesUtils.fromString("9F 7F 2A 47 90 50 40 47 91 81 02 31 00 83 58 00 11 68 91 45 81 48 12 83 65 00 00 00 00 01 2F 31 30 31 31 36 38 00 00 00 00 00 00 00 00 90 00"));		
		Assertions.assertThat(cplc).isNotNull();
		Assertions.assertThat(cplc.getIcFabricator()).isEqualTo(0x4790);
		Assertions.assertThat(cplc.getIcType()).isEqualTo(0x5040);
		Assertions.assertThat(cplc.getOs()).isEqualTo(0x4791);
		Assertions.assertThat(cplc.getOsReleaseLevel()).isEqualTo(0x3100);
		Assertions.assertThat(cplc.getIcSerialNumber()).isEqualTo(0x00116891);
		Assertions.assertThat(cplc.getIcBatchId()).isEqualTo(0x4581);
		Assertions.assertThat(cplc.getIcModuleFabricator()).isEqualTo(0x4812);
		Assertions.assertThat(cplc.getIccManufacturer()).isEqualTo(0x0000);
		Assertions.assertThat(cplc.getIcEmbeddingDate()).isNull();
		Assertions.assertThat(cplc.getPrepersoId()).isEqualTo(0x012F);
		Assertions.assertThat(cplc.getPrepersoEquipment()).isEqualTo(0x31313638);
		Assertions.assertThat(cplc.getPersoId()).isEqualTo(0x0000);
		Assertions.assertThat(cplc.getPersoEquipment()).isEqualTo(0x0000);
	}
}