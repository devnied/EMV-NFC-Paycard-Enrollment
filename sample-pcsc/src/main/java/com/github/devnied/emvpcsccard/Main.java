package com.github.devnied.emvpcsccard;

import java.util.List;

import javax.smartcardio.Card;
import javax.smartcardio.CardException;
import javax.smartcardio.CardTerminal;
import javax.smartcardio.TerminalFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.devnied.emvnfccard.exception.CommunicationException;
import com.github.devnied.emvnfccard.model.EmvCard;
import com.github.devnied.emvnfccard.parser.EmvTemplate;
import com.github.devnied.emvnfccard.parser.EmvTemplate.Config;

@SuppressWarnings("restriction")
public class Main {

	/**
	 * Class logger
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

	public static void main(final String[] args) throws CardException, CommunicationException {

		TerminalFactory factory = TerminalFactory.getDefault();
		List<CardTerminal> terminals = factory.terminals().list();
		if (terminals.isEmpty()) {
			throw new CardException("No card terminals available");
		}
		LOGGER.info("Terminals: " + terminals);

		if (terminals != null && !terminals.isEmpty()) {
			// Use the first terminal
			CardTerminal terminal = terminals.get(0);

			if (terminal.waitForCardPresent(0)) {
				// Connect with the card
				Card card = terminal.connect("*");
				LOGGER.info("card: " + card);

				// Create provider
				PcscProvider provider = new PcscProvider(card.getBasicChannel());
				
				// Define config
				Config config = EmvTemplate.Config()
						.setContactLess(false) // Enable contact less reading
						.setReadAllAids(true) // Read all aids in card
						.setReadTransactions(true) // Read all transactions
						.setRemoveDefaultParsers(false) // Remove default parsers (GeldKarte and Emv)
						.setReadAt(true)
						.setReadCplc(true); 
				
				// Create Parser
				EmvTemplate parser = EmvTemplate.Builder() //
						.setProvider(provider) // Define provider
						.setConfig(config) // Define config
						//.setTerminal(terminal) (optional) you can define a custom terminal implementation to create APDU
						.build();
				
				// Read card
				EmvCard emvCard = parser.readEmvCard();
				
				LOGGER.info(emvCard.toString());

				// Disconnect the card
				card.disconnect(false);
			}
		} else {
			LOGGER.error("No pcsc terminal found");
		}

	}
}
