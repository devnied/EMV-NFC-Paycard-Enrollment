/*
 * Copyright (C) 2013 MILLAU Julien
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
package com.github.devnied.emvnfccard.exception;

/**
 * Exception during TLV reading
 * 
 * @author MILLAU Julien
 * 
 */
public class TlvException extends RuntimeException {

	/**
	 * Generated serial ID
	 */
	private static final long serialVersionUID = -970100072282593424L;

	/**
	 * Constructor using field
	 * 
	 * @param pCause
	 *            cause
	 */
	public TlvException(final String pCause) {
		super(pCause);
	}

}
