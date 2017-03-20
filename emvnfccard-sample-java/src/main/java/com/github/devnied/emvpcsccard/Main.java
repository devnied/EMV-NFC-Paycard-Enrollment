package com.github.devnied.emvpcsccard;

import java.util.List;

import javax.smartcardio.Card;
import javax.smartcardio.CardChannel;
import javax.smartcardio.CardException;
import javax.smartcardio.CardTerminal;
import javax.smartcardio.TerminalFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.devnied.emvnfccard.exception.CommunicationException;
import com.github.devnied.emvnfccard.parser.EmvParser;

public class Main {

	/**
	 * Class logger
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

	public static void main(final String[] args) throws CardException, CommunicationException {

		TerminalFactory factory = TerminalFactory.getDefault();
		List<CardTerminal> terminals = factory.terminals().list();
		LOGGER.info("Terminals: " + terminals);

		if (terminals != null && !terminals.isEmpty()) {
			// Use the first terminal
			CardTerminal terminal = terminals.get(0);

			// Connect with the card
			Card card = terminal.connect("*");
			LOGGER.info("card: " + card);
			CardChannel channel = card.getBasicChannel();

			PcscProvider provider = new PcscProvider(channel);
			EmvParser parser = new EmvParser(provider, false);
			parser.readEmvCard();

			// Disconnect the card
			card.disconnect(false);
		} else {
			LOGGER.error("No pcsc terminal found");
		}

	}
}
