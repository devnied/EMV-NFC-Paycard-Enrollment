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
package com.github.devnied.emvnfccard.parser;

import com.github.devnied.emvnfccard.exception.CommunicationException;
import com.github.devnied.emvnfccard.model.Application;

import java.util.regex.Pattern;

/**
 * Parser Interface
 *
 * @author MILLAU Julien
 *
 */
public interface IParser {

	/**
	 * Get the RID or AID of the application to parse
	 *
	 * @return the RID or AID
	 */
	Pattern getId();

	/**
	 * Parse the card according to the
	 *
	 * @param pApplication
	 *            current application
	 * @return true if the card was read without errors
	 * @throws CommunicationException communication error
	 */
	boolean parse(Application pApplication) throws CommunicationException;

}
