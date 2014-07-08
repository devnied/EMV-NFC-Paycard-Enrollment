package com.github.devnied.emvnfccard;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.devnied.emvnfccard.enums.CommandEnum;
import com.github.devnied.emvnfccard.enums.EMVCardTypeEnum;
import com.github.devnied.emvnfccard.exception.CommunicationException;
import com.github.devnied.emvnfccard.iso7816emv.EMVTags;
import com.github.devnied.emvnfccard.model.EMVCard;
import com.github.devnied.emvnfccard.parser.IProvider;
import com.github.devnied.emvnfccard.parser.impl.DefaultEmvParser;
import com.github.devnied.emvnfccard.utils.CommandApdu;
import com.github.devnied.emvnfccard.utils.ResponseUtils;
import com.github.devnied.emvnfccard.utils.TLVUtil;

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
	 * @param pContactLess
	 *            boolean to indicate if the EMV card is contact less or not
	 */
	public EMVParser(final IProvider pProvider, final boolean pContactLess) {
		provider = pProvider;
		contactLess = pContactLess;
	}

	/**
	 * Method used to read a EMV card
	 * 
	 * @return data read from card or null if any provider match the card type
	 */
	public EMVCard readEmvCard() throws CommunicationException {
		// use PSE first
		EMVCard card = selectPSEPPSE();
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
	 * Select AID with PSE/PPSE directory
	 * 
	 * @return read card
	 */
	private EMVCard selectPSEPPSE() throws CommunicationException {
		EMVCard card = null;
		if (contactLess) {
			LOGGER.info("Select PPSE");
		} else {
			LOGGER.info("Select PSE");
		}
		// Select the PPSE or PSE directory
		byte[] data = provider.transceive(new CommandApdu(CommandEnum.SELECT, contactLess ? PPSE : PSE, 0).toBytes());
		if (ResponseUtils.isSucceed(data)) {

			if (!contactLess) { // Select PSE
				// Extract SFI
				int sfi = BytesUtils.byteArrayToInt(TLVUtil.getValue(data, EMVTags.SFI));
				// Get the PSE record
				data = provider.transceive(new CommandApdu(CommandEnum.READ_RECORD, sfi, sfi << 3 | 4, 0).toBytes());
				if (data[data.length - 2] == (byte) 0x6C) {
					data = provider.transceive(new CommandApdu(CommandEnum.READ_RECORD, sfi, sfi << 3 | 4, data[data.length - 1])
							.toBytes());
				}
			}
			// Extract Aid and contactless
			if (contactLess || !contactLess && ResponseUtils.isSucceed(data)) {
				String label = null;
				byte[] labelByte = TLVUtil.getValue(data, EMVTags.APPLICATION_LABEL);
				if (labelByte != null) {
					label = new String(labelByte);
				}
				// Get Card
				card = getCard(BytesUtils.bytesToStringNoSpace(TLVUtil.getValue(data, EMVTags.AID_CARD)), label);
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
	private EMVCard findWithAID() throws CommunicationException {
		EMVCard card = null;
		LOGGER.info("Find with AID");
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
	private EMVCard getCard(final String pAid, final String pScheme) throws CommunicationException {
		EMVCard ret = null;
		// Select AID
		byte[] data = provider.transceive(new CommandApdu(CommandEnum.SELECT, BytesUtils.fromString(pAid), 0).toBytes());
		// check response
		if (ResponseUtils.isSucceed(data)) {
			// Get AID
			String aid = BytesUtils.bytesToStringNoSpace(TLVUtil.getValue(data, EMVTags.DEDICATED_FILE_NAME));
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
					if (type != null) {
						LOGGER.info("Find type by card number type:" + type.getScheme());
					}
				}
				ret.setType(type);
			}
		}
		return ret;
	}
}
