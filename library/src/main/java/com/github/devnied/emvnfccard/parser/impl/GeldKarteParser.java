/*
 * Copyright (C) 2015 MILLAU Julien
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
package com.github.devnied.emvnfccard.parser.impl;

import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import com.github.devnied.emvnfccard.enums.EmvCardScheme;
import com.github.devnied.emvnfccard.model.Application;
import com.github.devnied.emvnfccard.parser.EmvTemplate;

/**
 * GeldKarte parser
 * 
 * @author MILLAU Julien
 *
 */
public class GeldKarteParser extends AbstractParser {

	public GeldKarteParser(EmvTemplate pTemplate) {
		super(pTemplate);
	}

	@Override
	public Pattern getId() {
		return Pattern.compile(StringUtils.deleteWhitespace(EmvCardScheme.GELDKARTE.getAid()[0]) + ".*");
	}

	@Override
	public boolean parse(Application pApplication) {
		// TODO Auto-generated method stub
		return false;
	}

}
