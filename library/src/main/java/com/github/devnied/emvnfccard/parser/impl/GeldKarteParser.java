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
package com.github.devnied.emvnfccard.parser.impl;

import com.github.devnied.emvnfccard.enums.CommandEnum;
import com.github.devnied.emvnfccard.enums.EmvCardScheme;
import com.github.devnied.emvnfccard.exception.CommunicationException;
import com.github.devnied.emvnfccard.iso7816emv.EmvTags;
import com.github.devnied.emvnfccard.model.Application;
import com.github.devnied.emvnfccard.model.EmvTrack2;
import com.github.devnied.emvnfccard.model.EmvTransactionRecord;
import com.github.devnied.emvnfccard.model.enums.ApplicationStepEnum;
import com.github.devnied.emvnfccard.model.enums.CardStateEnum;
import com.github.devnied.emvnfccard.model.enums.CurrencyEnum;
import com.github.devnied.emvnfccard.model.enums.TransactionTypeEnum;
import com.github.devnied.emvnfccard.parser.EmvTemplate;
import com.github.devnied.emvnfccard.utils.CommandApdu;
import com.github.devnied.emvnfccard.utils.ResponseUtils;
import com.github.devnied.emvnfccard.utils.TlvUtil;
import fr.devnied.bitlib.BytesUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * GeldKarte parser <br>
 * Documentation: <br>
 * - ftp://ftp.ccc.de/documentation/cards/geldkarte.pdf
 * - http://www.bensin.org/haxor/haxor_src/ic35/crap/stash/scinfo_khf%20-%20geldkarte%20ic35/doku/gk.log
 * - http://ftp.chaos-darmstadt.de/docs/cards/geldkarte.pdf
 * - https://code.google.com/p/android/issues/detail?id=62976
 * - http://www.openscdp.org/scripts/geldkarte/jsdoc/symbols/src/dump_girogo.js.html
 * - http://www.wrankl.de/UThings/Geldkarte.pdf
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
		boolean ret = false;
		// Select AID
		byte[] data = selectAID(pApplication.getAid());
		// Check response
		if (ResponseUtils.isSucceed(data)) {
			pApplication.setReadingStep(ApplicationStepEnum.SELECTED);
			// Get TLV log entry
			byte[] logEntry = getLogEntry(data);
			// Get AID
			pApplication.setAid(TlvUtil.getValue(data, EmvTags.DEDICATED_FILE_NAME));
			// Application label
			pApplication.setApplicationLabel(extractApplicationLabel(data));
			// Add type
			template.get().getCard().setType(EmvCardScheme.getCardTypeByAid(BytesUtils.bytesToStringNoSpace(pApplication.getAid())));
			// Extract Bank data
			extractBankData(data);

			// read Ef_ID
			extractEF_ID(pApplication);

			// Read EF_BETRAG
			readEfBetrag(pApplication);

			// Read EF_BLOG
			readEF_BLOG(pApplication);

			pApplication.setLeftPinTry(getLeftPinTry());
			pApplication.setTransactionCounter(getTransactionCounter());
			// TODO remove this
			pApplication.getListTransactions().addAll(extractLogEntry(logEntry));
			// Update ret
			template.get().getCard().setState(CardStateEnum.ACTIVE);
			ret = true;
		}

		return ret;
	}

	/**
	 * Read EF_BLOG
	 *
	 * @param pApplication emv application
	 * @throws CommunicationException communication error
	 */
	protected void readEF_BLOG(final Application pApplication) throws CommunicationException{
		List<EmvTransactionRecord> list = new ArrayList<EmvTransactionRecord>();
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
		SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
		// Read each records
		for (int i = 1; i < 16 ; i++) {
			byte[] data = template.get().getProvider().transceive(new CommandApdu(CommandEnum.READ_RECORD, i, 0xEC, 0).toBytes());
			// Check response
			if (ResponseUtils.isSucceed(data)) {
				if (data.length < 35){
					continue;
				}
				EmvTransactionRecord record = new EmvTransactionRecord();
				record.setCurrency(CurrencyEnum.EUR);
				record.setTransactionType(getType(data[0]));
				record.setAmount(Float.parseFloat(BytesUtils.bytesToStringNoSpace(Arrays.copyOfRange(data, 21, 24))) / 100L);

				try {
					record.setDate(dateFormat.parse(String.format("%02x.%02x.%02x%02x", data[32], data[31], data[29], data[30])));
					record.setTime(timeFormat.parse(String.format("%02x:%02x:%02x", data[33], data[34], data[35])));
				} catch (ParseException e) {
					LOGGER.error(e.getMessage(), e);
				}
				list.add(record);
			} else {
				break;
			}
		}
		pApplication.setListTransactions(list);
	}

	/**
	 * Method used to get the transaction type
	 * @param logstate the log state
	 * @return the transaction type or null
	 */
	protected TransactionTypeEnum getType(byte logstate) {
		switch ( (logstate & 0x60) >> 5) {
		case 0: return TransactionTypeEnum.LOADED;
		case 1: return TransactionTypeEnum.UNLOADED;
		case 2: return TransactionTypeEnum.PURCHASE;
		case 3: return TransactionTypeEnum.REFUND;
		}
		return null;
    }

	/**
	 * read EF_BETRAG
	 *
	 * @param pApplication EMV application
	 * @throws CommunicationException communication error
	 */
	protected void readEfBetrag(final Application pApplication) throws CommunicationException {
		// 00 B2 01 C4 00
		byte[] data = template.get().getProvider()
				.transceive(new CommandApdu(CommandEnum.READ_RECORD, 0x01, 0xC4, 0).toBytes());
		// Check response
		if (ResponseUtils.isSucceed(data)) {
			pApplication.setAmount(Float.parseFloat(String.format("%02x%02x%02x", data[0], data[1], data[2]))/100.0f);
		}
	}

	/**
	 * Method used to extract Ef_iD record
	 *
	 * @param pApplication EMV application
	 *
	 * @throws CommunicationException communication error
	 */
	protected void extractEF_ID(final Application pApplication) throws CommunicationException {
		// 00B201BC00
		byte[] data = template.get().getProvider().transceive(new CommandApdu(CommandEnum.READ_RECORD, 0x01, 0xBC, 0).toBytes());
		if (ResponseUtils.isSucceed(data)) {
			pApplication.setReadingStep(ApplicationStepEnum.READ);
			// Date
			SimpleDateFormat format = new SimpleDateFormat("MM/yy",Locale.getDefault());

			// Track 2
			EmvTrack2 track2 = new EmvTrack2();
			track2.setCardNumber(BytesUtils.bytesToStringNoSpace(Arrays.copyOfRange(data, 4, 9)));
			try {
				track2.setExpireDate(format.parse(String.format("%02x/%02x", data[11], data[10])));
			} catch (ParseException e) {
				LOGGER.error(e.getMessage(),e);
			}
			template.get().getCard().setTrack2(track2);
		}
	}

}
