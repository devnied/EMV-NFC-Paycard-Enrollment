package com.github.devnied.emvnfccard.enums;

import org.fest.assertions.Assertions;
import org.junit.Test;

public class SWEnumTest {

	@Test
	public void test() throws Exception {
		Assertions.assertThat(SWEnum.SW_61.getDetail()).isEqualTo(
				"Command successfully executed; 'XX' bytes of data are available and can be requested using GET RESPONSE");
		Assertions.assertThat(SWEnum.SW_61.getStatus()).isEqualTo(new byte[] { 0x61 });
	}

}
