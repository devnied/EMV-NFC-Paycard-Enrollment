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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Locale;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.devnied.emvnfccard.enums.CommandEnum;
import com.github.devnied.emvnfccard.enums.EmvCardScheme;
import com.github.devnied.emvnfccard.exception.CommunicationException;
import com.github.devnied.emvnfccard.iso7816emv.EmvTags;
import com.github.devnied.emvnfccard.model.Application;
import com.github.devnied.emvnfccard.model.EmvTrack1;
import com.github.devnied.emvnfccard.model.enums.ApplicationStepEnum;
import com.github.devnied.emvnfccard.parser.EmvTemplate;
import com.github.devnied.emvnfccard.utils.CommandApdu;
import com.github.devnied.emvnfccard.utils.ResponseUtils;
import com.github.devnied.emvnfccard.utils.TlvUtil;

import fr.devnied.bitlib.BitUtils;
import fr.devnied.bitlib.BytesUtils;

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
	 * Class Logger
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(GeldKarteParser.class);

	/**
	 * Geldkarte pattern
	 */
	private static final Pattern PATTERN = Pattern.compile(StringUtils.deleteWhitespace(EmvCardScheme.GELDKARTE.getAid()[2]) + ".*");

	public GeldKarteParser(EmvTemplate pTemplate) {
		super(pTemplate);
	}

	@Override
	public Pattern getId() {
		return PATTERN;
	}

	@Override
	public boolean parse(Application pApplication) throws CommunicationException {
		// Select AID
		byte[] data = selectAID(pApplication.getAid());
		// Check response
		if (ResponseUtils.isSucceed(data)) {
			// Get TLV log entry
			byte[] logEntry = getLogEntry(data);
			// Get AID
			pApplication.setAid(TlvUtil.getValue(data, EmvTags.DEDICATED_FILE_NAME));
			// Application label
			pApplication.setApplicationLabel(extractApplicationLabel(data));
			// Add type
			template.get().getCard().setType(EmvCardScheme.getCardTypeByAid(new String(pApplication.getAid())));
			// Extract Bank data
			extractBankData(data);
			
			// read Ef_ID
			extractEF_ID(pApplication);
			
			// Read EF_BETRAG
			readEfBetrag(pApplication);
			
			pApplication.setLeftPinTry(getLeftPinTry());
			pApplication.setTransactionCounter(getTransactionCounter());
			pApplication.setListTransactions(extractLogEntry(logEntry));
			pApplication.setReadingStep(ApplicationStepEnum.SELECTED);
		}

		return false;
	}

	/**
	 * read EF_BETRAG
	 * 
	 * @return
	 * @throws CommunicationException
	 */
	protected void readEfBetrag(final Application pApplication) throws CommunicationException {
		// 00 B2 01 C4 00
		byte[] data = template.get().getProvider()
				.transceive(new CommandApdu(CommandEnum.READ_RECORD, 0x01, 0xC4, 0).toBytes());
		// Check response
		if (ResponseUtils.isSucceed(data)) {
			BitUtils bits = new BitUtils(data);
			pApplication.setAmount(Float.parseFloat(bits.getNextHexaString(3))/100.0);
		}
	}

	/**
	 * Method used to extract Ef_iD record
	 * 
	 * @throws CommunicationException
	 */
	protected void extractEF_ID(final Application pApplication) throws CommunicationException {
		// 00B201BC00
		byte[] data = template.get().getProvider().transceive(new CommandApdu(CommandEnum.READ_RECORD, 0x01, 0xBC, 0).toBytes());
		if (ResponseUtils.isSucceed(data)) {
			// Date
			SimpleDateFormat format = new SimpleDateFormat("MM/yy",Locale.getDefault());
			
			// Track 1
			EmvTrack1 track1 = new EmvTrack1();
			track1.setCardNumber(BytesUtils.bytesToString(Arrays.copyOfRange(data, 4, 9)));
			try {
				track1.setExpireDate(format.parse(BytesUtils.bytesToString(Arrays.copyOfRange(data, 11, 12))+"/"+BytesUtils.bytesToString(Arrays.copyOfRange(data, 10, 11))));
			} catch (ParseException e) {
				LOGGER.error(e.getMessage(),e);
			}
			template.get().getCard().setTrack1(track1);
		}
	}

}
