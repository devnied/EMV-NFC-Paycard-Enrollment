package com.github.devnied.emvnfccard.provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.devnied.emvnfccard.parser.IProvider;
import com.github.devnied.emvnfccard.utils.TlvUtil;

import fr.devnied.bitlib.BytesUtils;

public class PpseProviderLockedCardTest implements IProvider {

	private static final Logger LOGGER = LoggerFactory.getLogger(PpseProviderLockedCardTest.class);

	private int step;

	@Override
	public byte[] transceive(final byte[] pCommand) {
		String response = null;
		LOGGER.debug("send: " + BytesUtils.bytesToString(pCommand));
		switch (step++) {

		case 0:
			response = "6F 57 84 0E 32 50 41 59 2E 53 59 53 2E 44 44 46 30 31 A5 45 BF 0C 42 61 10 4F 07 A0 00 00 00 42 10 10 50 02 43 42 87 01 01 9F 2A 08 03 00 00 00 00 00 00 00 61 18 4F 07 A0 00 00 00 03 10 10 50 0A 56 49 53 41 20 44 45 42 49 54 87 01 02 9F 2A 08 03 00 00 00 00 00 00 00 90 00";
			break;
		default:
			response = "69 85";
		}

		LOGGER.debug("resp: " + response);
		byte[] ret = BytesUtils.fromString(response);
		try {
			LOGGER.debug(TlvUtil.prettyPrintAPDUResponse(ret));
		} catch (Exception e) {
		}
		return ret;
	}

}
