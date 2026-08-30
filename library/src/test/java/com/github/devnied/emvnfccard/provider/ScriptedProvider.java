package com.github.devnied.emvnfccard.provider;

import java.util.LinkedHashMap;
import java.util.Map;

import com.github.devnied.emvnfccard.exception.CommunicationException;
import com.github.devnied.emvnfccard.parser.IProvider;

import fr.devnied.bitlib.BytesUtils;

/**
 * Provider answering a scripted response per command, used to exercise the
 * structural rules of the specification (directory navigation, selection
 * options) that no recorded trace of src/test/resources/data covers.
 * <p>
 * The responses a test registers here are ISO 7816-4 / EMV structures built for
 * the rule under test, they are not a capture of a real card.
 * </p>
 *
 * @author MILLAU julien
 *
 */
public class ScriptedProvider implements IProvider {

	/**
	 * Command (hexadecimal, no space) to response
	 */
	private final Map<String, String> responses = new LinkedHashMap<String, String>();

	/**
	 * Command prefix (hexadecimal, no space) to response
	 */
	private final Map<String, String> prefixes = new LinkedHashMap<String, String>();

	/**
	 * Commands received, in order
	 */
	private final java.util.List<String> received = new java.util.ArrayList<String>();

	/**
	 * Register the response to a command
	 *
	 * @param pCommand
	 *            command APDU
	 * @param pResponse
	 *            response APDU
	 * @return this provider
	 */
	public ScriptedProvider on(final String pCommand, final String pResponse) {
		responses.put(normalize(pCommand), normalize(pResponse));
		return this;
	}

	/**
	 * Register the response to every command beginning with the parameter, used
	 * for the commands whose content cannot be predicted: the data field of a
	 * GPO holds the terminal date, time and unpredictable number
	 *
	 * @param pPrefix
	 *            beginning of the command APDU
	 * @param pResponse
	 *            response APDU
	 * @return this provider
	 */
	public ScriptedProvider onPrefix(final String pPrefix, final String pResponse) {
		prefixes.put(normalize(pPrefix), normalize(pResponse));
		return this;
	}

	@Override
	public byte[] transceive(final byte[] pCommand) throws CommunicationException {
		String command = BytesUtils.bytesToStringNoSpace(pCommand);
		received.add(command);
		String response = responses.get(command);
		if (response != null) {
			return BytesUtils.fromString(response);
		}
		for (Map.Entry<String, String> entry : prefixes.entrySet()) {
			if (command.startsWith(entry.getKey())) {
				return BytesUtils.fromString(entry.getValue());
			}
		}
		// An unscripted command is answered as a card that does not hold what
		// the terminal asks for: '6A83' (record not found) ends a READ RECORD
		// loop, '6A82' (file not found) answers everything else
		boolean readRecord = pCommand.length > 1 && pCommand[1] == (byte) 0xB2;
		return BytesUtils.fromString(readRecord ? "6A83" : "6A82");
	}

	@Override
	public byte[] getAt() {
		return BytesUtils.fromString("3B 65 00 00 20 63 CB A0 00");
	}

	/**
	 * Get the commands received so far
	 *
	 * @return the received commands, hexadecimal without space
	 */
	public java.util.List<String> getReceived() {
		return received;
	}

	private static String normalize(final String pValue) {
		return pValue.replaceAll("[^0-9A-Fa-f]", "").toUpperCase(java.util.Locale.ENGLISH);
	}

}
