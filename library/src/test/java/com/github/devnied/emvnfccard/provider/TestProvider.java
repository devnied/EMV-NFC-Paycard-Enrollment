package com.github.devnied.emvnfccard.provider;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.Queue;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.fest.assertions.Assertions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.devnied.emvnfccard.exception.CommunicationException;
import com.github.devnied.emvnfccard.parser.IProvider;
import com.github.devnied.emvnfccard.utils.TlvUtil;

import fr.devnied.bitlib.BytesUtils;

/**
 * Provider used to mock card response
 *
 * @author MILLAU julien
 *
 */
public class TestProvider implements IProvider {

	/**
	 * Class logger
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(TestProvider.class);

	/**
	 * Exchange queue
	 */
	private final Queue<String> queue = new LinkedList<String>();

	/**
	 * Test provider constructor
	 *
	 * @param pDataFilename
	 *            filename
	 */
	public TestProvider(final String pDataFilename) {

		// Open file
		InputStream in = TestProvider.class.getResourceAsStream("/data/" + pDataFilename);
		BufferedReader br = null;

		String line = null;
		try {
			br = new BufferedReader(new InputStreamReader(in));
			// Extract line
			while ((line = br.readLine()) != null) {
				// A comment may also be glued at the end of a line, its text
				// must not be mistaken for hexadecimal digits
				int comment = line.indexOf('#');
				if (comment >= 0) {
					line = line.substring(0, comment);
				}
				if (StringUtils.isNotBlank(line)) {
					queue.add(line.replaceAll("[^0-9A-F]+", ""));
				}
			}
		} catch (IOException e) {
			LOGGER.error(e.getMessage(), e);
		} finally {
			IOUtils.closeQuietly(br);
			IOUtils.closeQuietly(in);
		}

	}

	@Override
	public byte[] transceive(final byte[] pCommand) throws CommunicationException {
		LOGGER.debug("send: " + BytesUtils.bytesToString(pCommand));
		String command = queue.poll();

		// Assert command if defined in test data
		if (StringUtils.isNotBlank(command)) {
			Assertions.assertThat(BytesUtils.bytesToStringNoSpace(pCommand)).isEqualTo(command);
		}
		// Get response
		String response = queue.poll();
		if (StringUtils.isBlank(response)) {
			// The trace is over. The parser keeps looking for what the card
			// may still hold (the other AIDs of the list, the GET DATA of an
			// application that exposed no account number...): such a command
			// is answered the way a card that holds nothing more would, any
			// other command past the end of the trace is a failure
			response = pastTheEnd(pCommand);
			if (response == null) {
				throw new CommunicationException("No response found");
			}
		}
		byte[] ret = BytesUtils.fromString(response);
		LOGGER.debug("resp: " + BytesUtils.bytesToString(ret));
		try {
			LOGGER.debug(TlvUtil.prettyPrintAPDUResponse(ret));
		} catch (Exception e) {
		}
		return ret;
	}
	
	/**
	 * Method used to answer a command sent once the trace is exhausted, for
	 * the commands a reader sends to look for data the card may not hold.
	 *
	 * @param pCommand
	 *            command sent
	 * @return '6A82' (file not found) for a SELECT, '6A83' (record not
	 *         found) for a READ RECORD, '6A88' (referenced data not found)
	 *         for a GET DATA, null for any other command
	 */
	private static String pastTheEnd(final byte[] pCommand) {
		if (pCommand == null || pCommand.length < 2) {
			return null;
		}
		switch (pCommand[1] & 0xFF) {
		case 0xA4:
			return "6A82";
		case 0xB2:
			return "6A83";
		case 0xCA:
			return "6A88";
		default:
			return null;
		}
	}

	@Override
	public byte[] getAt() {
		return BytesUtils.fromString("3B 65 00 00 20 63 CB A0 00");
	}

}
