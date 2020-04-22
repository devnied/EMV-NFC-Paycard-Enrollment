/*
 * Copyright (C) 2019 MILLAU Julien
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.devnied.emvnfccard.parser;

import com.github.devnied.emvnfccard.enums.CommandEnum;
import com.github.devnied.emvnfccard.enums.EmvCardScheme;
import com.github.devnied.emvnfccard.exception.CommunicationException;
import com.github.devnied.emvnfccard.iso7816emv.EmvTags;
import com.github.devnied.emvnfccard.iso7816emv.ITerminal;
import com.github.devnied.emvnfccard.iso7816emv.TLV;
import com.github.devnied.emvnfccard.iso7816emv.impl.DefaultTerminalImpl;
import com.github.devnied.emvnfccard.model.Application;
import com.github.devnied.emvnfccard.model.EmvCard;
import com.github.devnied.emvnfccard.model.enums.CardStateEnum;
import com.github.devnied.emvnfccard.parser.impl.EmvParser;
import com.github.devnied.emvnfccard.parser.impl.GeldKarteParser;
import com.github.devnied.emvnfccard.parser.impl.ProviderWrapper;
import com.github.devnied.emvnfccard.utils.*;
import fr.devnied.bitlib.BytesUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Emv Template.<br>
 * Class used to detect the EMV template of the card and select the right parser
 *
 * @author MILLAU Julien
 *
 */
public class EmvTemplate {

	/**
	 * Class Logger
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(EmvTemplate.class);

	/**
	 * Max record for SFI
	 */
	public static final int MAX_RECORD_SFI = 16;

	/**
	 * PPSE directory "2PAY.SYS.DDF01"
	 */
	private static final byte[] PPSE = "2PAY.SYS.DDF01".getBytes();

	/**
	 * PSE directory "1PAY.SYS.DDF01"
	 */
	private static final byte[] PSE = "1PAY.SYS.DDF01".getBytes();

	/**
	 * EMV Terminal
	 */
	private ITerminal terminal;

	/**
	 * Provider
	 */
	private IProvider provider;

	/**
	 * Parser list
	 */
	private List<IParser> parsers;

	/**
	 * Config
	 */
	private Config config;

	/**
	 * Card data
	 */
	private EmvCard card;

	/**
	 * Create builder
	 *
	 * @return a new instance of builder
	 */
	public static Builder Builder() {
		return new Builder();
	}

	/**
	 * Create a new Config
	 *
	 * @return a new instance of config
	 */
	public static Config Config() {
		return new Config();
	}

	/**
	 * Build a new Config.
	 */
	public static class Config {

		/**
		 * use contact less mode
		 */
		public boolean contactLess = true;

		/**
		 * Boolean to indicate if the parser need to read transaction history
		 */
		public boolean readTransactions = true;

		/**
		 * Boolean used to indicate if you want to read all card aids
		 */
		public boolean readAllAids = true;

		/**
		 * Boolean used to indicate if you want to extract ATS or ATR
		 */
		public boolean readAt = true;

		/**
		 * Boolean used to indicate if you want to read CPLC data
		 */
		public boolean readCplc = false;

		/**
		 * Boolean used to indicate to not add provided parser implementation
		 */
		public boolean removeDefaultParsers;

		/**
		 * Package private. Use {@link #Builder()} to build a new one
		 *
		 */
		Config() {
		}

		/**
		 * Setter for the field contactLess (default true)
		 *
		 * @param contactLess
		 *            the contactLess to set
		 * @return the config instance
		 */
		public Config setContactLess(final boolean contactLess) {
			this.contactLess = contactLess;
			return this;
		}

		/**
		 * Setter for the field readTransactions (default true)
		 *
		 * @param readTransactions
		 *            the readTransactions to set
		 * @return the config instance
		 */
		public Config setReadTransactions(final boolean readTransactions) {
			this.readTransactions = readTransactions;
			return this;
		}

		/**
		 * Setter for the field readAllAids (default true)
		 *
		 * @param readAllAids
		 *            the readAllAids to set
		 * @return the config instance
		 */
		public Config setReadAllAids(final boolean readAllAids) {
			this.readAllAids = readAllAids;
			return this;
		}

		/**
		 * Setter for the field removeDefaultParsers (default false)
		 *
		 * @param removeDefaultParsers
		 *            the removeDefaultParsers to set
		 * @return the config instance
		 */
		public Config setRemoveDefaultParsers(boolean removeDefaultParsers) {
			this.removeDefaultParsers = removeDefaultParsers;
			return this;
		}

		/**
		 * Setter for the field readAt (default true)
		 *
		 * @param readAt
		 *            the readAt to set
		 * @return the config instance
		 */
		public Config setReadAt(boolean readAt) {
			this.readAt = readAt;
			return this;
		}

		/**
		 * Setter for the field readCplc (default true)
		 *
		 * @param readCplc
		 *            the readCplc to set
		 * @return the config instance
		 */
		public Config setReadCplc(boolean readCplc) {
			this.readCplc = readCplc;
			return this;
		}

	}

	/**
	 * Build a new {@link EmvTemplate}.
	 * <p>
	 * Calling {@link #setProvider} is required before calling {@link #build()}.
	 * All other methods are optional.
	 */
	public static class Builder {

		private IProvider provider;
		private ITerminal terminal;
		private Config config;

		/**
		 * Package private. Use {@link #Builder()} to build a new one
		 *
		 */
		Builder() {
		}

		/**
		 * Setter for the field provider
		 *
		 * @param provider
		 *            the provider to set
		 * @return the config instance
		 */
		public Builder setProvider(final IProvider provider) {
			this.provider = provider;
			return this;
		}

		/**
		 * Setter for the field terminal
		 *
		 * @param terminal
		 *            the terminal to set
		 * @return the config instance
		 */
		public Builder setTerminal(final ITerminal terminal) {
			this.terminal = terminal;
			return this;
		}

		/**
		 * Setter for the field config
		 *
		 * @param config
		 *            the config to set
		 * @return the config instance
		 */
		public Builder setConfig(Config config) {
			this.config = config;
			return this;
		}

		/** Create the {@link EmvTemplate} instances. */
		public EmvTemplate build() {
			if (provider == null) {
				throw new IllegalArgumentException("Provider may not be null.");
			}
			// Set default terminal implementation
			if (terminal == null) {
				terminal = new DefaultTerminalImpl();
			}
			return new EmvTemplate(provider, terminal, config);
		}

	}

	/**
	 * Call {@link EmvParser.build()} to create an new instance
	 *
	 * @param pProvider
	 *            provider to launch command and communicate with the card
	 * @param pTerminal
	 *            terminal data
	 * @param pConfig
	 *            parser configuration (Default configuration used if null)
	 */
	private EmvTemplate(final IProvider pProvider, final ITerminal pTerminal, final Config pConfig) {
		provider = new ProviderWrapper(pProvider);
		terminal = pTerminal;
		config = pConfig;
		if (config == null) {
			config = Config();
		}
		parsers = new ArrayList<IParser>();
		if (!config.removeDefaultParsers) {
			addDefaultParsers();
		}
		card = new EmvCard();
	}

	/**
	 * Add default parser implementation
	 */
	private void addDefaultParsers() {
		parsers.add(new GeldKarteParser(this));
		parsers.add(new EmvParser(this));
	}

	/**
	 * Method used to add a list of parser to the current EMV template
	 *
	 * @param pParsers
	 *            parser implementation to add
	 * @return current EmvTemplate
	 */
	public EmvTemplate addParsers(final IParser... pParsers) {
		if (pParsers != null) {
			for (IParser parser : pParsers) {
				parsers.add(0, parser);
			}
		}
		return this;
	}

	/**
	 * Method used to read public data from EMV card
	 *
	 * @return data read from card or null if any provider match the card type
	 * @throws CommunicationException communication error
	 */
	public EmvCard readEmvCard() throws CommunicationException {
		// Read CPLC Infos
		if (config.readCplc){
			readCPLCInfos();
		}

		// Update ATS or ATR
		if (config.readAt){
			card.setAt(BytesUtils.bytesToStringNoSpace(provider.getAt()));
			card.setAtrDescription(config.contactLess ? AtrUtils.getDescriptionFromAts(card.getAt()) : AtrUtils.getDescription(card.getAt()));
		}
		// use PSE first
		if (!readWithPSE()) {
			// Find with AID
			readWithAID();
		}

		return card;
	}

	/**
	 *
	 * Try to read generic infos about the SmartCard as defined in the
	 * "GlobalPlatform Card Specification" (GPCS).
	 * @throws CommunicationException communication error
	 *
	 */
	protected void readCPLCInfos() throws CommunicationException {
		card.setCplc(CPLCUtils.parse(provider.transceive(new CommandApdu(CommandEnum.GET_DATA, 0x9F, 0x7F, null, 0).toBytes())));
	}

	/**
	 * Read EMV card with Payment System Environment or Proximity Payment System
	 * Environment
	 *
	 * @return true is succeed false otherwise
	 * @throws CommunicationException communication error
	 */
	protected boolean readWithPSE() throws CommunicationException {
		boolean ret = false;
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Try to read card with Payment System Environment");
		}
		// Select the payment environment PPSE or PSE directory
		byte[] data = selectPaymentEnvironment();
		if (ResponseUtils.isSucceed(data)) {
			// Parse FCI Template
			card.getApplications().addAll(parseFCIProprietaryTemplate(data));
			Collections.sort(card.getApplications());
			// For each application
			for (Application app : card.getApplications()) {
				boolean status = false;
				String applicationAid = BytesUtils.bytesToStringNoSpace(app.getAid());
				for (IParser impl : parsers) {
					if (impl.getId() != null && impl.getId().matcher(applicationAid).matches()) {
						status = impl.parse(app);
						break;
					}
				}
				if (!ret && status) {
					ret = status;
					if (!config.readAllAids) {
						break;
					}
				}
			}
			if (!ret) {
				card.setState(CardStateEnum.LOCKED);
			}
		} else if (LOGGER.isDebugEnabled()) {
			LOGGER.debug((config.contactLess ? "PPSE" : "PSE") + " not found -> Use kown AID");
		}

		return ret;
	}

	/**
	 * Method used to parse FCI Proprietary Template
	 *
	 * @param pData
	 *            data to parse
	 * @return the list of EMV application in the card
	 * @throws CommunicationException communication error
	 */
	protected List<Application> parseFCIProprietaryTemplate(final byte[] pData) throws CommunicationException {
		List<Application> ret = new ArrayList<Application>();
		// Get SFI
		byte[] data = TlvUtil.getValue(pData, EmvTags.SFI);

		// Check SFI
		if (data != null) {
			int sfi = BytesUtils.byteArrayToInt(data);
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("SFI found:" + sfi);
			}
			// For each records
			for (int rec = 0; rec < MAX_RECORD_SFI; rec++) {
				data = provider.transceive(new CommandApdu(CommandEnum.READ_RECORD, rec, sfi << 3 | 4, 0).toBytes());
				// Check response
				if (ResponseUtils.isSucceed(data)) {
					// Get applications Tags
					ret.addAll(getApplicationTemplate(data));
				} else {
					// No more records
					break;
				}
			}
		} else {
			// Read Application template
			ret.addAll(getApplicationTemplate(pData));
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("(FCI) Issuer Discretionary Data is already present");
			}
		}
		return ret;
	}

	/**
	 * Method used to get the application list, if the Kernel Identifier is
	 * defined, <br>
	 * this value need to be appended to the ADF Name in the data field of <br>
	 * the SELECT command.
	 *
	 * @param pData
	 *            FCI proprietary template data
	 * @return the application data (Aid,extended Aid, ...)
	 */
	protected List<Application> getApplicationTemplate(final byte[] pData) {
		List<Application> ret = new ArrayList<Application>();
		// Search Application template
		List<TLV> listTlv = TlvUtil.getlistTLV(pData, EmvTags.APPLICATION_TEMPLATE);
		// For each application template
		for (TLV tlv : listTlv) {
			Application application = new Application();
			// Get AID, Kernel_Identifier and application label
			List<TLV> listTlvData = TlvUtil.getlistTLV(tlv.getValueBytes(), EmvTags.AID_CARD, EmvTags.APPLICATION_LABEL,
					EmvTags.APPLICATION_PRIORITY_INDICATOR);
			// For each data
			for (TLV data : listTlvData) {
				if (data.getTag() == EmvTags.APPLICATION_PRIORITY_INDICATOR) {
					application.setPriority(BytesUtils.byteArrayToInt(data.getValueBytes()));
				} else if (data.getTag() == EmvTags.APPLICATION_LABEL) {
					application.setApplicationLabel(new String(data.getValueBytes()));
				} else {
					application.setAid(data.getValueBytes());
					ret.add(application);
				}
			}
		}
		return ret;
	}

	/**
	 * Read EMV card with AID
	 */
	protected void readWithAID() throws CommunicationException {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Try to read card with AID");
		}
		// Test each card from know EMV AID
		Application app = new Application();
		for (EmvCardScheme type : EmvCardScheme.values()) {
			for (byte[] aid : type.getAidByte()) {
				app.setAid(aid);
				app.setApplicationLabel(type.getName());
				String applicationAid = BytesUtils.bytesToStringNoSpace(aid);
				for (IParser impl : parsers) {
					if (impl.getId() != null && impl.getId().matcher(applicationAid).matches() && impl.parse(app)) {
						// Remove previously added Application template
						card.getApplications().clear();
						// Add Application
						card.getApplications().add(app);
						return;
					}
				}
			}
		}
	}

	/**
	 * Method used to select payment environment PSE or PPSE
	 *
	 * @return response byte array
	 * @throws CommunicationException communication error
	 */
	protected byte[] selectPaymentEnvironment() throws CommunicationException {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Select " + (config.contactLess ? "PPSE" : "PSE") + " Application");
		}
		// Select the PPSE or PSE directory
		return provider.transceive(new CommandApdu(CommandEnum.SELECT, config.contactLess ? PPSE : PSE, 0).toBytes());
	}

	/**
	 * Method used to get the field card
	 *
	 * @return the card
	 */
	public EmvCard getCard() {
		return card;
	}

	/**
	 * Get the field provider
	 *
	 * @return the provider
	 */
	public IProvider getProvider() {
		return provider;
	}

	/**
	 * Get the field config
	 *
	 * @return the config
	 */
	public Config getConfig() {
		return config;
	}

	/**
	 * Get the field terminal
	 *
	 * @return the terminal
	 */
	public ITerminal getTerminal() {
		return terminal;
	}

	/**
	 * Get the field parsers
	 *
	 * @return the parsers
	 */
	public List<IParser> getParsers() {
		return Collections.unmodifiableList(parsers);
	}

}
