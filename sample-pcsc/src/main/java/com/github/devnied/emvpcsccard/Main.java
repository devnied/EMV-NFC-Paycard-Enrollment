package com.github.devnied.emvpcsccard;

import java.util.List;

import javax.smartcardio.Card;
import javax.smartcardio.CardException;
import javax.smartcardio.CardTerminal;
import javax.smartcardio.TerminalFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.devnied.emvnfccard.exception.CommunicationException;
import com.github.devnied.emvnfccard.model.Application;
import com.github.devnied.emvnfccard.model.CVM;
import com.github.devnied.emvnfccard.model.EmvCard;
import com.github.devnied.emvnfccard.model.enums.CountryCodeEnum;
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
						.setReadCplc(true)
						.setReadAllRecords(true) // Read the whole application file (CVM list, issuer country, ...)
						.setReadExtendedData(true) // Read the optional GET DATA objects (offline balance, last online ATC)
						// Domestic cards (Interac, girocard, UnionPay, ...) expect a domestic terminal
						.setTerminalCountryCode(CountryCodeEnum.FR);
				
				// Create Parser
				EmvTemplate parser = EmvTemplate.Builder() //
						.setProvider(provider) // Define provider
						.setConfig(config) // Define config
						//.setTerminal(terminal) (optional) you can define a custom terminal implementation to create APDU
						.build();
				
				// Read card
				EmvCard emvCard = parser.readEmvCard();
				
				LOGGER.info(emvCard.toString());
				printApplications(emvCard);

				// Disconnect the card
				card.disconnect(false);
			}
		} else {
			LOGGER.error("No pcsc terminal found");
		}

	}

	/**
	 * Print the data objects read on top of the card number and of the
	 * expiration date.
	 *
	 * @param pCard
	 *            card read
	 */
	private static void printApplications(final EmvCard pCard) {
		for (Application app : pCard.getApplications()) {
			LOGGER.info("Application {} ({})", app.getApplicationLabel(), app.getApplicationPreferredName());
			LOGGER.info("  Priority           : {}{}", app.getPriority(),
					app.isCardholderConfirmationRequired() ? " (cardholder confirmation required)" : "");
			if (app.isBlocked()) {
				LOGGER.info("  Blocked            : the issuer blocked this application");
			}
			LOGGER.info("  PAN                : {} (sequence {})", app.getPan(), app.getPanSequenceNumber());
			LOGGER.info("  Valid              : {} -> {}", app.getEffectiveDate(), app.getExpirationDate());
			LOGGER.info("  Issuer country     : {}", app.getIssuerCountryCode());
			LOGGER.info("  Currency           : {} (exponent {})", app.getCurrency(), app.getCurrencyExponent());
			LOGGER.info("  Languages          : {}", app.getLanguagePreferences());
			LOGGER.info("  Issuer URL         : {}", app.getIssuerUrl());
			LOGGER.info("  Version            : {}", app.getApplicationVersionNumber());
			LOGGER.info("  Interchange profile: {}", app.getInterchangeProfile());
			LOGGER.info("  Usage control      : {}", app.getUsageControl());
			LOGGER.info("  Offline balance    : {}", app.getOfflineBalance());
			LOGGER.info("  ATC / last online  : {} / {} ({} offline transactions)", app.getTransactionCounter(),
					app.getLastOnlineTransactionCounter(), app.getConsecutiveOfflineTransactions());
			if (app.isTokenized()) {
				LOGGER.info("  Tokenized card     : requestor {}, PAR {}, funding PAN ****{}", app.getTokenRequestorId(),
						app.getPaymentAccountReference(), app.getLast4DigitsOfPan());
			}
			for (CVM cvm : app.getCvmList()) {
				LOGGER.info("  CVM                : {} - {}", cvm.getMethod(), cvm.getCondition());
			}
		}
	}
}
