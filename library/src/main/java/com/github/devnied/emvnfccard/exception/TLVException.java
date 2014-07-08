package com.github.devnied.emvnfccard.exception;

/**
 * Exception during TLV reading
 * 
 * @author julien
 * 
 */
public class TLVException extends RuntimeException {

	/**
	 * Generated serial ID
	 */
	private static final long serialVersionUID = -970100072282593424L;

	public TLVException(final String pCause) {
		super(pCause);
	}

}
