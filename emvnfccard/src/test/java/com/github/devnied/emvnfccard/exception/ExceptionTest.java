package com.github.devnied.emvnfccard.exception;

import org.fest.assertions.Assertions;
import org.junit.Test;

public class ExceptionTest {

	@Test
	public void testCommunicationException() {
		try {
			throw new CommunicationException("test");
		} catch (CommunicationException e) {
			Assertions.assertThat(e.getMessage()).isEqualTo("test");
		}
	}

	@Test
	public void testTLVException() {
		try {
			throw new TlvException("test");
		} catch (TlvException e) {
			Assertions.assertThat(e.getMessage()).isEqualTo("test");
		}
	}

}
