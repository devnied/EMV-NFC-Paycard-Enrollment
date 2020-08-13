package com.github.devnied.emvpcsccard;

import java.nio.ByteBuffer;

import javax.smartcardio.CardChannel;
import javax.smartcardio.CardException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.devnied.emvnfccard.enums.SwEnum;
import com.github.devnied.emvnfccard.exception.CommunicationException;
import com.github.devnied.emvnfccard.parser.IProvider;
import com.github.devnied.emvnfccard.utils.TlvUtil;

import fr.devnied.bitlib.BytesUtils;

public class PcscProvider implements IProvider {

	/**
	 * Class logger
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(PcscProvider.class);

	/**
	 * CardChanel
	 */
	private final CardChannel channel;

	/**
	 * Buffer
	 */
	private final ByteBuffer buffer = ByteBuffer.allocate(1024);

	/**
	 * Constructor using field
	 *
	 * @param pChannel
	 *            card channel
	 */
	public PcscProvider(final CardChannel pChannel) {
		channel = pChannel;
	}

	@Override
	public byte[] transceive(final byte[] pCommand) throws CommunicationException {
		byte[] ret = null;
		buffer.clear();
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("send: " + BytesUtils.bytesToString(pCommand));
		}
		try {
			int nbByte = channel.transmit(ByteBuffer.wrap(pCommand), buffer);
			ret = new byte[nbByte];
			System.arraycopy(buffer.array(), 0, ret, 0, ret.length);
		} catch (CardException e) {
			// Do nothing
		}

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("resp: " + BytesUtils.bytesToString(ret));
			try {
				LOGGER.debug("resp: " + TlvUtil.prettyPrintAPDUResponse(ret));
				SwEnum val = SwEnum.getSW(ret);
				if (val != null) {
					LOGGER.debug("resp: " + val.getDetail());
				}
			} catch (Exception e) {
			}
		}

		return ret;
	}

	@Override
	public byte[] getAt() {
		return channel.getCard().getATR().getBytes();
	}

}
