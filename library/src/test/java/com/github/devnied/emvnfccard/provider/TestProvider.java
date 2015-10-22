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
				if (!line.startsWith("#")) {
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
			throw new CommunicationException("No response found");
		}
		byte[] ret = BytesUtils.fromString(response);
		LOGGER.debug("resp: " + BytesUtils.bytesToString(ret));
		try {
			LOGGER.debug(TlvUtil.prettyPrintAPDUResponse(ret));
		} catch (Exception e) {
		}
		return ret;
	}
	
	@Override
	public byte[] getAt() {
		return BytesUtils.fromString("3B 65 00 00 20 63 CB A0 00");
	}

}
