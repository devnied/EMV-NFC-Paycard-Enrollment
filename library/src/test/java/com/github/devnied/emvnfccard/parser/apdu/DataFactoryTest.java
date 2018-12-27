package com.github.devnied.emvnfccard.parser.apdu;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.fest.assertions.Assertions;
import org.junit.Test;

import com.github.devnied.emvnfccard.parser.apdu.impl.DataFactory;

public class DataFactoryTest {
	
	@Test
	public void testNullCplcDate(){
		Date date = DataFactory.calculateCplcDate(new byte[]{0,0});
		Assertions.assertThat(date).isNull();
	}
	
	@Test
	public void testCplcDate() throws ParseException{
		Date date = DataFactory.calculateCplcDate(new byte[]{(byte) 0x81,0x02});
		Assertions.assertThat(date).isNotNull();
		SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
		Assertions.assertThat(date).isEqualTo(format.parse("12/04/2018"));
	}
	
	@Test
	public void testCplcDate2() throws ParseException{
		Date date = DataFactory.calculateCplcDate(new byte[]{0x60,0x66});
		Assertions.assertThat(date).isNotNull();
		SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
		Assertions.assertThat(date).isEqualTo(format.parse("06/03/2016"));
	}
	
	

}
