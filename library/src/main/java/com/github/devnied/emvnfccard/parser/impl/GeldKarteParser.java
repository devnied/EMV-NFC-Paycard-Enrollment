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

import com.github.devnied.emvnfccard.enums.CommandEnum;
import com.github.devnied.emvnfccard.enums.EmvCardScheme;
import com.github.devnied.emvnfccard.exception.CommunicationException;
import com.github.devnied.emvnfccard.model.Application;
import com.github.devnied.emvnfccard.model.enums.ApplicationStepEnum;
import com.github.devnied.emvnfccard.parser.EmvTemplate;
import com.github.devnied.emvnfccard.utils.CommandApdu;
import com.github.devnied.emvnfccard.utils.ResponseUtils;

/**
 * GeldKarte parser <br/>
 * Documentation: <br/>
 * - ftp://ftp.ccc.de/documentation/cards/geldkarte.pdf -
 * 
 * @author MILLAU Julien
 *
 */
public class GeldKarteParser extends AbstractParser {

	/**
	 * Geldkarte pattern
	 */
	private static final Pattern PATTERN = Pattern.compile(StringUtils.deleteWhitespace(EmvCardScheme.GELDKARTE.getAid()[0]) + ".*");

	public GeldKarteParser(EmvTemplate pTemplate) {
		super(pTemplate);
	}

	@Override
	public Pattern getId() {
		return PATTERN;
	}

	@Override
	public boolean parse(Application pApplication) throws CommunicationException {
		byte[] data = selectMasterFile();
		// TODO improve step
		pApplication.setReadingStep(ApplicationStepEnum.SELECTED);
		// Check response
		if (ResponseUtils.isSucceed(data)) {
			data = selectEF();
			if (ResponseUtils.isSucceed(data)) {
				extractEF_ID();
			}
		}
		return false;
	}

	/**
	 * Select the MasterFile
	 * 
	 * @return Card response data
	 * @throws CommunicationException
	 */
	protected byte[] selectMasterFile() throws CommunicationException {
		// 00A4000C
		return template.get().getProvider().transceive(new CommandApdu(CommandEnum.SELECT, 0x00, 0x0C, 0).toBytes());
	}

	/**
	 * Select EF
	 * 
	 * @return
	 * @throws CommunicationException
	 */
	protected byte[] selectEF() throws CommunicationException {
		// 00A4020C020003
		return template.get().getProvider()
				.transceive(new CommandApdu(CommandEnum.SELECT, 0x02, 0x0C, new byte[] { 0x00, 0x03 }, 0).toBytes());
	}

	/**
	 * Method used to extract Ef_iD record
	 * 
	 * @throws CommunicationException
	 */
	protected void extractEF_ID() throws CommunicationException {
		// 00B2010400
		byte[] data = template.get().getProvider().transceive(new CommandApdu(CommandEnum.READ_RECORD, 0x01, 0x04, 0).toBytes());
		if (ResponseUtils.isSucceed(data)) {

		}
	}
}
