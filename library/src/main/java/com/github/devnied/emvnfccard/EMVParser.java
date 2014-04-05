package com.github.devnied.emvnfccard;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.devnied.emvnfccard.enums.CommandEnum;
import com.github.devnied.emvnfccard.enums.EMVCardTypeEnum;
import com.github.devnied.emvnfccard.model.EMVCard;
import com.github.devnied.emvnfccard.parser.IProvider;
import com.github.devnied.emvnfccard.parser.impl.DefaultEmvParser;
import com.github.devnied.emvnfccard.utils.CommandApdu;
import com.github.devnied.emvnfccard.utils.ResponseApdu;
import com.github.devnied.emvnfccard.utils.TLVUtils;

import fr.devnied.bitlib.BytesUtils;

/**
 * EMV Parser find the correct parser to read the card
 * 
 * @author julien Millau
 * 
 */
public class EMVParser {

	/**
	 * Class Logger
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(EMVParser.class);

	/**
	 * PPSE directory "2PAY.SYS.DDF01"
	 */
	private static final byte[] PPSE = BytesUtils.fromString("32 50 41 59 2e 53 59 53 2e 44 44 46 30 31");

	/**
	 * PSE directory "1PAY.SYS.DDF01"
	 */
	private static final byte[] PSE = BytesUtils.fromString("31 50 41 59 2e 53 59 53 2e 44 44 46 30 31");

	/**
	 * Provider
	 */
	private IProvider provider;

	/**
	 * use contact less mode
	 */
	private boolean contactLess;

	/**
	 * Constructor
	 * 
	 * @param pProvider
	 *            provider to launch command
	 */
	public EMVParser(final IProvider pProvider, final boolean pContactLess) {
		provider = pProvider;
		contactLess = pContactLess;
	}

	/**
	 * Method used to read a EMV card
	 * 
	 * @param pProvider
	 *            provider to send command to the card
	 * @return data read from card or null if any provider match the card type
	 */
	public EMVCard readEmvCard() {
		// use PSE first
		EMVCard card = selectPSE();
		// Find with AID
		if (card == null) {
			card = findWithAID();
			if (card == null) {
				LOGGER.info("Card type not found");
			}
		}
		return card;
	}

	/**
	 * Select AID with PSE directory
	 * 
	 * @return card read
	 */
	private EMVCard selectPSE() {
		EMVCard card = null;
		// Select the PPSE or PSE directory
		byte[] data = provider.transceive(new CommandApdu(CommandEnum.SELECT, contactLess ? PPSE : PSE, 0).toBytes());
		if (ResponseApdu.isSucceed(data)) {

			if (contactLess) { // Select PPSE
				// Extract label
				String val = StringUtils.substringAfter(BytesUtils.bytesToStringNoSpace(data), "BF0C");
				data = BytesUtils.fromString(val);

			} else { // Select PSE
				// Extract SFI
				int sfi = TLVUtils.getIntValue(data, TLVUtils.SFI);
				// Get the PSE record
				data = provider.transceive(new CommandApdu(CommandEnum.READ_RECORD, sfi, sfi << 3 | 4, 0).toBytes());
				if (data[data.length - 2] == (byte) 0x6C) {
					data = provider.transceive(new CommandApdu(CommandEnum.READ_RECORD, sfi, sfi << 3 | 4, data[data.length - 1])
							.toBytes());
				}
			}
			// Extract Aid and contactless
			if (contactLess || !contactLess && ResponseApdu.isSucceed(data)) {
				String label = null;
				byte[] labelByte = TLVUtils.getArrayValue(data, TLVUtils.APPLICATION_LABEL);
				if (labelByte != null) {
					label = new String(labelByte);
				}
				// Get Card
				card = getCard(TLVUtils.getHexaValue(data, TLVUtils.AID), label);
			}

		} else if (LOGGER.isDebugEnabled()) {
			if (contactLess) {
				LOGGER.debug("PPSE not found -> Use kown AID");
			} else {
				LOGGER.debug("PSE not found -> Use kown AID");
			}
		}

		return card;
	}

	/**
	 * Find card type with AID
	 * 
	 * @return Card read
	 */
	private EMVCard findWithAID() {
		EMVCard card = null;
		// Get each card from enum
		for (EMVCardTypeEnum type : EMVCardTypeEnum.values()) {
			card = getCard(type.getAid(), type.getScheme());
			if (card != null) {
				break;
			}
		}
		return card;
	}

	/**
	 * Read Card with parameter AID
	 * 
	 * @param pAid
	 *            card AID
	 * @param pScheme
	 *            card scheme (Name)
	 * @return card read or null
	 */
	private EMVCard getCard(final String pAid, final String pScheme) {
		EMVCard ret = null;
		// Select AID
		byte[] data = provider.transceive(new CommandApdu(CommandEnum.SELECT, BytesUtils.fromString(pAid), 0).toBytes());
		// check response
		if (ResponseApdu.isSucceed(data)) {
			// Get AID
			String aid = TLVUtils.getHexaValue(data, TLVUtils.DF_NAME);
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Card type :" + pScheme + " with Aid:" + aid);
			}
			// Parse select response
			ret = DefaultEmvParser.getInstance().parse(data, provider);
			if (ret != null) {
				ret.setAid(aid);
				if (pScheme != null) {
					ret.setCardLabel(pScheme);
				}
				EMVCardTypeEnum type = EMVCardTypeEnum.getCardTypeByAid(aid);
				// Get real type for french card
				if (type == EMVCardTypeEnum.CB) {
					type = EMVCardTypeEnum.getCardTypeByCardNumber(ret.getCardNumber());
				}
				ret.setType(type);
			}
		}
		return ret;
	}
}
