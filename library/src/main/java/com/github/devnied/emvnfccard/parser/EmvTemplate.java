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
import com.github.devnied.emvnfccard.enums.SwEnum;
import com.github.devnied.emvnfccard.exception.CommunicationException;
import com.github.devnied.emvnfccard.iso7816emv.BerTlvInputStream;
import org.apache.commons.lang3.ArrayUtils;
import com.github.devnied.emvnfccard.iso7816emv.EmvTags;
import com.github.devnied.emvnfccard.iso7816emv.ITag;
import com.github.devnied.emvnfccard.iso7816emv.ITerminal;
import com.github.devnied.emvnfccard.iso7816emv.TLV;
import com.github.devnied.emvnfccard.iso7816emv.impl.DefaultTerminalImpl;
import com.github.devnied.emvnfccard.model.AbstractData;
import com.github.devnied.emvnfccard.model.Application;
import com.github.devnied.emvnfccard.model.DirectoryEntry;
import com.github.devnied.emvnfccard.model.EmvCard;
import com.github.devnied.emvnfccard.model.PaymentDirectory;
import com.github.devnied.emvnfccard.model.RecordData;
import com.github.devnied.emvnfccard.model.enums.ApplicationStepEnum;
import com.github.devnied.emvnfccard.model.enums.CardStateEnum;
import com.github.devnied.emvnfccard.model.enums.CountryCodeEnum;
import com.github.devnied.emvnfccard.parser.impl.AbstractParser;
import com.github.devnied.emvnfccard.parser.impl.EmvParser;
import com.github.devnied.emvnfccard.parser.impl.GeldKarteParser;
import com.github.devnied.emvnfccard.parser.impl.ProviderWrapper;
import com.github.devnied.emvnfccard.utils.*;
import fr.devnied.bitlib.BytesUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

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
	 * Maximum number of records read in a payment system directory. The
	 * records are numbered from 1 (EMV 4.3 Book 1 section 12.3.2), so the
	 * records 1 to {@value #MAX_RECORD_SFI} are read.
	 */
	public static final int MAX_RECORD_SFI = 16;

	/**
	 * Maximum depth of the payment directory tree, a Directory Definition File
	 * may reference another one and a broken card could loop forever
	 */
	public static final int MAX_DDF_DEPTH = 4;

	/**
	 * Maximum number of sub directories selected while walking the payment
	 * directory tree, bounding its breadth as well as its depth
	 */
	public static final int MAX_DDF_COUNT = 16;

	/**
	 * Maximum number of occurrences read when the applications are looked up
	 * with a partial AID
	 */
	public static final int MAX_AID_OCCURRENCES = 8;

	/**
	 * Length in bytes of a Registered Application Provider Identifier, an AID
	 * of exactly this length is a partial AID that may match several
	 * applications of the card
	 */
	public static final int RID_LENGTH = 5;

	/**
	 * Highest length of the Short File Identifier (tag '88') this library reads,
	 * the identifier being one byte
	 */
	public static final int SFI_MAX_LENGTH = 4;

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
		 * Boolean used to indicate that the whole Application File Locator must
		 * be read instead of stopping as soon as the card number is known.<br>
		 * Reading every record exposes the cardholder name, the CVM list, the
		 * application usage control, the issuer country, the issuer URL, ...
		 */
		public boolean readAllRecords = true;

		/**
		 * Boolean used to indicate that the optional data objects only
		 * available through the GET DATA command must be read (last online ATC,
		 * offline balance, ...).<br>
		 * It costs additional APDU exchanges, and some cards answer with an
		 * error to unsupported GET DATA.
		 */
		public boolean readExtendedData;

		/**
		 * Boolean used to indicate that the reader supports the Extended
		 * Selection of EMV Contactless Book B: when the payment directory entry
		 * provides a value for the tag '9F29', it is appended to the ADF Name
		 * in the data field of the SELECT command.<br>
		 * The parser falls back on the bare ADF Name when the card refuses the
		 * concatenated one.
		 */
		public boolean extendedSelectionSupported = true;

		/**
		 * Country the terminal has to pretend to be located in (default:
		 * France).<br>
		 * Domestic applications (Interac in Canada, girocard in Germany,
		 * UnionPay in China, ...) often refuse to process a transaction coming
		 * from a foreign terminal, setting the right country is required to
		 * read them. It is ignored when a custom terminal is provided to the
		 * builder.
		 */
		public CountryCodeEnum terminalCountryCode;

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

		/**
		 * Setter for the field readAllRecords (default true)
		 *
		 * @param readAllRecords
		 *            the readAllRecords to set
		 * @return the config instance
		 */
		public Config setReadAllRecords(boolean readAllRecords) {
			this.readAllRecords = readAllRecords;
			return this;
		}

		/**
		 * Setter for the field readExtendedData (default false)
		 *
		 * @param readExtendedData
		 *            the readExtendedData to set
		 * @return the config instance
		 */
		public Config setReadExtendedData(boolean readExtendedData) {
			this.readExtendedData = readExtendedData;
			return this;
		}

		/**
		 * Setter for the field extendedSelectionSupported (default true)
		 *
		 * @param extendedSelectionSupported
		 *            the extendedSelectionSupported to set
		 * @return the config instance
		 */
		public Config setExtendedSelectionSupported(boolean extendedSelectionSupported) {
			this.extendedSelectionSupported = extendedSelectionSupported;
			return this;
		}

		/**
		 * Setter for the field terminalCountryCode (default France)
		 *
		 * @param terminalCountryCode
		 *            the country the terminal has to pretend to be located in
		 * @return the config instance
		 */
		public Config setTerminalCountryCode(CountryCodeEnum terminalCountryCode) {
			this.terminalCountryCode = terminalCountryCode;
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
			// Set default terminal implementation, located in the country the
			// config asks for
			if (terminal == null) {
				terminal = new DefaultTerminalImpl(config != null ? config.terminalCountryCode : null);
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
			// The bytes the provider answers are kept as they are, next to the
			// hexadecimal string they have always been reported as
			byte[] at = provider.getAt();
			card.setRawAt(at);
			card.setAt(BytesUtils.bytesToStringNoSpace(at));
			card.setAtrDescription(config.contactLess ? AtrUtils.getDescriptionFromAts(card.getAt()) : AtrUtils.getDescription(card.getAt()));
			// A description that stays empty from here on means "looked up,
			// unknown", not "not looked up"
			card.setAtLookedUp(true);
		}
		// use PSE first
		if (!readWithPSE()) {
			// Find with AID
			readWithAID();
		}

		// Everything the card disclosed is known: identify it, on the card
		// itself (its first application) and on each of its applications
		identifyCard();

		return card;
	}

	/**
	 * Method used to compute the identification of the card and of each of its
	 * applications once the reading is over, so that the result is persisted
	 * with the card instead of being computed again by every caller.
	 * <p>
	 * The card identification is the one of
	 * {@link CardIdentificationUtils#identify(EmvCard)}, the one of an
	 * application is {@link CardIdentificationUtils#identify(EmvCard, Application)}.
	 * </p>
	 */
	protected void identifyCard() {
		try {
			card.setIdentification(CardIdentificationUtils.identify(card));
			for (Application application : card.getApplications()) {
				application.setIdentification(CardIdentificationUtils.identify(card, application));
			}
		} catch (RuntimeException e) {
			// The identification is a summary of what was read, it must never
			// cost the reading itself
			LOGGER.warn("Unable to identify the card: " + e.getMessage(), e);
		}
	}

	/**
	 *
	 * Try to read generic infos about the SmartCard as defined in the
	 * "GlobalPlatform Card Specification" (GPCS).
	 * @throws CommunicationException communication error
	 *
	 */
	protected void readCPLCInfos() throws CommunicationException {
		byte[] response = provider.transceive(new CommandApdu(CommandEnum.GET_DATA, 0x9F, 0x7F, null, 0).toBytes());
		// The whole response is kept, whatever its length: an answer that
		// CPLCUtils rejects (error status, wrong length, missing tag) is still
		// visible
		card.setRawCplcResponse(response);
		card.setCplcStatusWord(getStatusWord(response));
		card.setCplc(CPLCUtils.parse(response));
	}

	/**
	 * Method used to read the status word SW1 SW2 that closes a response.
	 *
	 * @param pResponse
	 *            response to a command, status bytes included
	 * @return the status word as 4 upper-case hexadecimal characters (e.g.
	 *         "9000", "6A83"), or null when the response is null or shorter
	 *         than two bytes
	 */
	public static String getStatusWord(final byte[] pResponse) {
		if (pResponse == null || pResponse.length < 2) {
			return null;
		}
		return BytesUtils.bytesToStringNoSpace(Arrays.copyOfRange(pResponse, pResponse.length - 2, pResponse.length))
				.toUpperCase(Locale.ENGLISH);
	}

	/**
	 * Method used to collect every primitive data object a BER-TLV structure
	 * holds, recursively, keyed by the upper-case hexadecimal tag of the object.
	 * <p>
	 * The first occurrence of a tag is kept, and the content of the constructed
	 * data objects listed in parameter is not walked: the entries (tag '61') of
	 * a payment directory are modelled apart from the directory itself.
	 * </p>
	 *
	 * @param pData
	 *            data to walk, may be null or malformed
	 * @param pSkipped
	 *            constructed tags whose content is left out
	 * @return the primitive data objects found, in document order, never null
	 */
	public static Map<String, byte[]> collectPrimitiveDataObjects(final byte[] pData, final ITag... pSkipped) {
		Map<String, byte[]> ret = new LinkedHashMap<String, byte[]>();
		collectPrimitiveDataObjects(pData, pSkipped, ret, 0);
		return ret;
	}

	/**
	 * Recursive implementation of
	 * {@link #collectPrimitiveDataObjects(byte[], ITag...)}
	 *
	 * @param pData
	 *            data to walk
	 * @param pSkipped
	 *            constructed tags whose content is left out
	 * @param pResult
	 *            map to fill
	 * @param pDepth
	 *            current nesting depth, bounded like TlvUtil does
	 */
	private static void collectPrimitiveDataObjects(final byte[] pData, final ITag[] pSkipped,
			final Map<String, byte[]> pResult, final int pDepth) {
		if (pData == null || pData.length == 0 || pDepth > TlvUtil.MAX_DEPTH) {
			return;
		}
		try {
			BerTlvInputStream stream = new BerTlvInputStream(pData);
			while (stream.available() > 0) {
				TLV tlv = TlvUtil.getNextTLV(stream);
				if (tlv == null || tlv.getTag() == null) {
					break;
				}
				if (tlv.getTag().isConstructed()) {
					boolean skipped = false;
					if (pSkipped != null) {
						for (ITag tag : pSkipped) {
							if (tag != null && tag == tlv.getTag()) {
								skipped = true;
								break;
							}
						}
					}
					if (!skipped) {
						collectPrimitiveDataObjects(tlv.getValueBytes(), pSkipped, pResult, pDepth + 1);
					}
				} else {
					String key = BytesUtils.bytesToStringNoSpace(tlv.getTagBytes()).toUpperCase(Locale.ENGLISH);
					if (!pResult.containsKey(key)) {
						pResult.put(key, tlv.getValueBytes());
					}
				}
			}
		} catch (RuntimeException e) {
			// A malformed structure only stops the collection, the objects
			// already found are kept
			LOGGER.debug("Unable to walk the data objects: " + e.getMessage(), e);
		}
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
		card.setPaymentEnvironmentStatusWord(getStatusWord(data));
		if (ResponseUtils.isSucceed(data)) {
			// Parse FCI Template, without the status bytes: they are not part of
			// the File Control Information and must not be parsed as such
			byte[] fci = ResponseUtils.getDataField(data);
			// The directory itself is modelled: its File Control Information,
			// its records and everything read around them
			PaymentDirectory directory = new PaymentDirectory();
			directory.setDepth(0);
			// A copy: the model must never hand out the shared constant
			directory.setName((config.contactLess ? PPSE : PSE).clone());
			directory.setRawFci(fci);
			directory.setSelectStatusWord(getStatusWord(data));
			card.getApplications().addAll(parseFCIProprietaryTemplate(fci, 0, new HashSet<String>(), directory));
			Collections.sort(card.getApplications());
			// For each application
			for (Application app : card.getApplications()) {
				// Same rule as the lookup by AID: a parser that matches the AID
				// but fails to read the application does not shadow the generic
				// one that comes after it
				boolean status = parse(app);
				if (!ret && status) {
					ret = status;
					if (!config.readAllAids) {
						break;
					}
				}
			}
			// The payment directory itself may carry the language preference
			// and the issuer code table index of the card. They are applied
			// once the applications have been read, so that the value an
			// application states for itself always wins over the directory one
			applyDirectoryPreferences(data, card.getApplications());
			if (!ret) {
				card.setState(CardStateEnum.LOCKED);
				// The status word that made the card locked is the one the
				// highest priority application answered to its SELECT
				if (!card.getApplications().isEmpty()) {
					card.setLockedStatusWord(card.getApplications().get(0).getSelectStatusWord());
				}
			}
		} else if (LOGGER.isDebugEnabled()) {
			LOGGER.debug((config.contactLess ? "PPSE" : "PSE") + " not found -> Use kown AID");
		}

		return ret;
	}

	/**
	 * Method used to carry the preferences the payment directory states over to
	 * the applications it lists.<br>
	 * The File Control Information of the directory may hold the Language
	 * Preference (tag '5F2D') and the Issuer Code Table Index (tag '9F11'),
	 * which used to be read only from the applications themselves.
	 *
	 * @param pData
	 *            File Control Information of the payment directory
	 * @param pApplications
	 *            applications found in that directory
	 */
	protected void applyDirectoryPreferences(final byte[] pData, final List<Application> pApplications) {
		List<String> languages = EmvDataUtils.parseLanguagePreference(TlvUtil.getValue(pData, EmvTags.LANGUAGE_PREFERENCE));
		int codeTable = EmvDataUtils.getNumericIntValue(TlvUtil.getValue(pData, EmvTags.ISSUER_CODE_TABLE_INDEX),
				AbstractParser.UNKNOW);
		for (Application application : pApplications) {
			if (!languages.isEmpty() && application.getLanguagePreferences().isEmpty()) {
				application.setLanguagePreferences(languages);
				application.setLanguagePreferencesFromDirectory(true);
			}
			if (codeTable != AbstractParser.UNKNOW && application.getIssuerCodeTableIndex() == AbstractParser.UNKNOW) {
				application.setIssuerCodeTableIndex(codeTable);
				application.setIssuerCodeTableIndexFromDirectory(true);
				// The preferred name was decoded without an index, the
				// directory supplies one: decode it again
				if (application.getRawApplicationPreferredName() != null) {
					application.setApplicationPreferredName(
							EmvDataUtils.decodeText(application.getRawApplicationPreferredName(), codeTable));
				}
			}
		}
		// The Application Preferred Name a directory entry carries was decoded
		// with the common character set, the directory names the code table
		// it is meant to be displayed with: decode it again
		if (codeTable != AbstractParser.UNKNOW) {
			for (DirectoryEntry entry : card.getDirectoryEntries()) {
				if (entry.getRawApplicationPreferredName() != null) {
					entry.setApplicationPreferredName(EmvDataUtils.decodeText(entry.getRawApplicationPreferredName(), codeTable));
				}
			}
		}
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
		return parseFCIProprietaryTemplate(pData, 0, new HashSet<String>());
	}

	/**
	 * Method used to parse FCI Proprietary Template
	 *
	 * @param pData
	 *            data to parse
	 * @param pDepth
	 *            current depth in the directory tree, a payment directory may
	 *            reference sub directories (DDF) that have to be read too
	 * @param pVisited
	 *            names of the directories already selected, a card whose
	 *            directories reference each other must not be walked twice
	 * @return the list of EMV application in the card
	 * @throws CommunicationException communication error
	 */
	protected List<Application> parseFCIProprietaryTemplate(final byte[] pData, final int pDepth, final Set<String> pVisited)
			throws CommunicationException {
		// The directory is modelled even through this entry point, its
		// selection status is unknown here
		PaymentDirectory directory = new PaymentDirectory();
		directory.setDepth(pDepth);
		directory.setRawFci(pData);
		return parseFCIProprietaryTemplate(pData, pDepth, pVisited, directory);
	}

	/**
	 * Method used to parse FCI Proprietary Template, filling the model of the
	 * directory it describes.
	 * <p>
	 * On top of the applications it returns, the method records on the
	 * {@link PaymentDirectory} in parameter everything the directory carried:
	 * its DF Name (tag '84'), its Short File Identifier (tag '88', raw and
	 * decoded, valid or not), every primitive data object of its File Control
	 * Information outside the Application Templates (tag '61'), its Language
	 * Preference (tag '5F2D') and Issuer Code Table Index (tag '9F11'), each
	 * READ RECORD sent to the directory file with the status word that ended
	 * the reading, and the DDF names (tag '9D') its records listed. The
	 * directory is added to {@link EmvCard#getPaymentDirectories()}.
	 * </p>
	 *
	 * @param pData
	 *            data to parse
	 * @param pDepth
	 *            current depth in the directory tree, a payment directory may
	 *            reference sub directories (DDF) that have to be read too
	 * @param pVisited
	 *            names of the directories already selected, a card whose
	 *            directories reference each other must not be walked twice
	 * @param pDirectory
	 *            model of the directory being parsed, its name and selection
	 *            outcome are set by the caller (a fresh one is used when null)
	 * @return the list of EMV application in the card
	 * @throws CommunicationException communication error
	 */
	protected List<Application> parseFCIProprietaryTemplate(final byte[] pData, final int pDepth, final Set<String> pVisited,
			final PaymentDirectory pDirectory) throws CommunicationException {
		List<Application> ret = new ArrayList<Application>();
		PaymentDirectory directory = pDirectory != null ? pDirectory : new PaymentDirectory();
		directory.setDepth(pDepth);
		if (directory.getRawFci() == null) {
			directory.setRawFci(pData);
		}
		// Added before its sub directories are walked, so that the list of
		// directories follows the selection order
		card.getPaymentDirectories().add(directory);
		// The directory being parsed is itself visited: a card whose entry
		// references the directory it sits in would otherwise have the whole
		// directory read a second time, and report every application twice
		byte[] name = TlvUtil.getValue(pData, EmvTags.DEDICATED_FILE_NAME);
		if (name != null && name.length > 0) {
			pVisited.add(BytesUtils.bytesToStringNoSpace(name));
			// The DF Name the card answers is the name of the directory, the
			// one it was selected with is only kept when the card sends none
			directory.setName(name);
		}
		// Everything the File Control Information carries outside the entries,
		// the entries themselves being modelled by DirectoryEntry
		directory.getDataObjects().putAll(collectPrimitiveDataObjects(pData, EmvTags.APPLICATION_TEMPLATE));
		directory.getLanguagePreferences()
				.addAll(EmvDataUtils.parseLanguagePreference(TlvUtil.getValue(pData, EmvTags.LANGUAGE_PREFERENCE)));
		directory.setIssuerCodeTableIndex(
				EmvDataUtils.getNumericIntValue(TlvUtil.getValue(pData, EmvTags.ISSUER_CODE_TABLE_INDEX), AbstractData.UNKNOWN));
		// Get SFI
		byte[] data = TlvUtil.getValue(pData, EmvTags.SFI);
		directory.setRawSfi(data);
		int sfi = 0;
		// A Short File Identifier is one byte. BytesUtils.byteArrayToInt throws
		// on an empty value and on more than 4 bytes, and that exception would
		// escape readEmvCard()
		if (data != null && data.length >= 1 && data.length <= SFI_MAX_LENGTH) {
			sfi = BytesUtils.byteArrayToInt(data);
			// The value is reported as the card sent it, valid or not
			directory.setSfi(sfi);
			// The identifier is shifted into P2, where it is coded on five bits
			// (ISO/IEC 7816-4 Table 47), and EMV 4.3 Book 1 section 12.2.3 goes
			// further: "A Payment System Directory is a linear EF file
			// identified by a SFI in the range 1 to 10". The wider ISO range is
			// accepted because a card mis-declaring its directory is common and
			// reading a file that holds no directory entry costs nothing, but a
			// value that cannot even be put in P2 is refused.
			if (sfi < 1 || sfi > AbstractParser.MAX_SFI) {
				LOGGER.warn("Invalid Short File Identifier for the payment directory: " + sfi);
				sfi = 0;
			}
		} else if (data != null) {
			LOGGER.warn("The Short File Identifier of the payment directory is " + data.length + " bytes long");
		}
		directory.setSfiValid(sfi > 0);

		// Check SFI
		if (sfi > 0) {
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("SFI found:" + sfi);
			}
			List<byte[]> subDirectories = new ArrayList<byte[]>();
			boolean ended = false;
			// EMV 4.3 Book 1 section 12.3.2 (and Figure 17): the records of a
			// payment system directory are read "beginning with record number
			// 1 and continuing with successive records until the card returns
			// SW1 SW2 = '6A83'". ISO 7816-4 reserves the record number '00',
			// it designates the current record and the selection by SFI has
			// just reset the record pointer.
			for (int rec = 1; rec <= MAX_RECORD_SFI; rec++) {
				data = provider.transceive(new CommandApdu(CommandEnum.READ_RECORD, rec, sfi << 3 | 4, 0).toBytes());
				// Every exchange is kept, the terminating one included
				directory.getRecords().add(new RecordData(sfi, rec, ResponseUtils.getDataField(data), getStatusWord(data)));
				// Check response
				if (ResponseUtils.isSucceedOrWarning(data)) {
					byte[] record = ResponseUtils.getDataField(data);
					// The entries the record holds are recorded on the card as
					// they are parsed, but the directory they were read in is
					// only known here
					int firstEntry = card.getDirectoryEntries().size();
					// Get applications Tags
					ret.addAll(getApplicationTemplate(record));
					stampDirectoryEntries(firstEntry, name, sfi, rec);
					// A payment directory entry may reference a sub directory
					for (TLV tlv : TlvUtil.getlistTLV(record, EmvTags.DDF_NAME)) {
						subDirectories.add(tlv.getValueBytes());
					}
				} else {
					// '6A83' is the end of the directory. The specification
					// asks the terminal to clear the candidate list on any
					// other error, this library deliberately keeps the entries
					// already read: it only extracts public data and the
					// applications found so far are valid.
					if (!ResponseUtils.isEquals(data, SwEnum.SW_6A83)) {
						LOGGER.warn("Directory record " + rec + " of the SFI " + sfi
								+ " ended the reading with an unexpected status");
					}
					directory.setEndStatusWord(getStatusWord(data));
					ended = true;
					// No more records
					break;
				}
			}
			// The card never answered the end of its directory
			directory.setRecordLimitReached(!ended);
			directory.getSubDirectoryNames().addAll(subDirectories);
			// Selecting a sub directory changes the current file, so it can
			// only be done once every record of this directory has been read
			ret.addAll(readDirectoryDefinitionFiles(subDirectories, pDepth, pVisited));
		} else {
			// The entries are those of the FCI Issuer Discretionary Data, not
			// those of a directory file: there is neither a SFI nor a record
			// number to stamp them with
			int firstEntry = card.getDirectoryEntries().size();
			// Read Application template
			ret.addAll(getApplicationTemplate(pData));
			stampDirectoryEntries(firstEntry, name, AbstractData.UNKNOWN, AbstractData.UNKNOWN);
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("(FCI) Issuer Discretionary Data is already present");
			}
		}
		return ret;
	}

	/**
	 * Method used to carry the context a directory entry was read in over to
	 * the entries that have just been added to the card.<br>
	 * A directory entry (tag '61') holds no reference to the directory it sits
	 * in: the DF Name (tag '84') and the Short File Identifier (tag '88') are
	 * those of the File Control Information of the PSE or of the DDF the
	 * directory is attached to, and the record number is the one of the READ
	 * RECORD that returned the entry.
	 *
	 * @param pFirstEntry
	 *            index of the first entry to stamp in
	 *            {@link EmvCard#getDirectoryEntries()}
	 * @param pDirectoryName
	 *            DF Name (tag '84') of the directory the entries were read from
	 * @param pSfi
	 *            SFI (tag '88') of the directory file, or
	 *            {@link AbstractData#UNKNOWN} when the entries were read in the
	 *            FCI Issuer Discretionary Data
	 * @param pRecordNumber
	 *            number of the record the entries were read in, or
	 *            {@link AbstractData#UNKNOWN}
	 */
	protected void stampDirectoryEntries(final int pFirstEntry, final byte[] pDirectoryName, final int pSfi,
			final int pRecordNumber) {
		List<DirectoryEntry> entries = card.getDirectoryEntries();
		for (int i = Math.max(pFirstEntry, 0); i < entries.size(); i++) {
			DirectoryEntry entry = entries.get(i);
			entry.setDedicatedFileName(pDirectoryName);
			entry.setDirectorySfi(pSfi);
			entry.setRecordNumber(pRecordNumber);
			entry.setReadFromDirectoryFile(pSfi != AbstractData.UNKNOWN);
		}
	}

	/**
	 * Method used to follow the Directory Definition File (DDF) entries of a
	 * payment directory record.<br>
	 * As described in EMV 4.3 Book 1 section 12.2.2, a directory entry holding
	 * a DDF Name (tag '9D') references a sub directory whose own records list
	 * more applications.
	 *
	 * @param pDdfNames
	 *            DDF names collected in the records of the current directory
	 * @param pDepth
	 *            current depth in the directory tree
	 * @param pVisited
	 *            names of the directories already selected
	 * @return the applications found in the sub directories
	 * @throws CommunicationException communication error
	 */
	protected List<Application> readDirectoryDefinitionFiles(final List<byte[]> pDdfNames, final int pDepth,
			final Set<String> pVisited) throws CommunicationException {
		List<Application> ret = new ArrayList<Application>();
		if (pDdfNames == null || pDdfNames.isEmpty()) {
			return ret;
		}
		if (pDepth >= MAX_DDF_DEPTH) {
			LOGGER.warn("Maximum payment directory depth reached, the sub directories are not read");
			card.setDirectoryDepthLimitReached(true);
			card.getUnreadDdfNames().addAll(pDdfNames);
			return ret;
		}
		for (int i = 0; i < pDdfNames.size(); i++) {
			byte[] ddfName = pDdfNames.get(i);
			// The depth alone does not bound the traversal: directories that
			// reference each other would still be walked K^MAX_DDF_DEPTH times
			if (pVisited.size() >= MAX_DDF_COUNT) {
				LOGGER.warn("Maximum number of payment directories reached, the remaining ones are not read");
				card.setDirectoryCountLimitReached(true);
				card.getUnreadDdfNames().addAll(pDdfNames.subList(i, pDdfNames.size()));
				break;
			}
			if (!pVisited.add(BytesUtils.bytesToStringNoSpace(ddfName))) {
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("Directory already read:" + BytesUtils.bytesToStringNoSpace(ddfName));
				}
				card.getUnreadDdfNames().add(ddfName);
				continue;
			}
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("DDF found:" + BytesUtils.bytesToStringNoSpace(ddfName));
			}
			byte[] response = provider.transceive(new CommandApdu(CommandEnum.SELECT, ddfName, 0).toBytes());
			// The sub directory is modelled whether its selection succeeded or
			// not, a refused DDF is still something the card listed
			PaymentDirectory directory = new PaymentDirectory();
			directory.setDepth(pDepth + 1);
			directory.setName(ddfName);
			directory.setSelectStatusWord(getStatusWord(response));
			if (ResponseUtils.isSucceed(response)) {
				byte[] fci = ResponseUtils.getDataField(response);
				directory.setRawFci(fci);
				// parseFCIProprietaryTemplate adds the directory to the card
				ret.addAll(parseFCIProprietaryTemplate(fci, pDepth + 1, pVisited, directory));
			} else {
				card.getPaymentDirectories().add(directory);
			}
		}
		return ret;
	}

	/**
	 * Method used to get the application list from a directory entry.
	 * <p>
	 * On top of the AID and of the application label, the entry may carry:
	 * </p>
	 * <ul>
	 * <li>the Application Priority Indicator (tag '87'), which holds the
	 * priority order and the cardholder confirmation flag</li>
	 * <li>the Kernel Identifier (tag '9F2A'), the card preference for the
	 * kernel the application is to be processed on</li>
	 * <li>the Extended Selection (tag '9F29'), to be appended to the ADF Name
	 * in the data field of the SELECT command</li>
	 * </ul>
	 *
	 * <p>
	 * The entries themselves are modelled by {@link DirectoryEntry} and are
	 * added to {@link EmvCard#getDirectoryEntries()} as they are parsed: an
	 * entry carries more than the selection needs, and a directory may list an
	 * entry that describes no application at all.
	 * </p>
	 *
	 * @param pData
	 *            FCI proprietary template data
	 * @return the application data (Aid,extended Aid, ...)
	 */
	protected List<Application> getApplicationTemplate(final byte[] pData) {
		List<Application> ret = new ArrayList<Application>();
		for (DirectoryEntry entry : parseDirectoryEntries(pData)) {
			card.getDirectoryEntries().add(entry);
			Application application = toApplication(entry);
			if (application != null) {
				ret.add(application);
			}
		}
		return ret;
	}

	/**
	 * Method used to parse the directory entries (tag '61') a payment system
	 * directory holds.
	 * <p>
	 * EMV 4.4 Book 1 Table 12 gives the content of an ADF directory entry:
	 * "'4F' 5-16 ADF Name M / '50' 1-16 Application Label M / '87' 1
	 * Application Priority Indicator O / '73' var. Directory Discretionary
	 * Template O / '9F0A' var. Application Selection Registered Proprietary
	 * Data O", Table 16 that of a DDF one: "'9D' 5-16 DDF Name M / '73' var.
	 * Directory Discretionary Template O". EMV Contactless Book B Table A-1
	 * adds the Kernel Identifier (tag '9F2A') and the Extended Selection (tag
	 * '9F29') of a PPSE entry, and ISO/IEC 7816-4 Table 91 the File reference
	 * data element (tag '51') of an application template.
	 * </p>
	 *
	 * @param pData
	 *            FCI proprietary template data, or the content of a record of
	 *            the payment system directory
	 * @return the entries the data holds, in the order the card listed them
	 */
	/**
	 * Tags an application directory entry describes an application with (EMV
	 * 4.3 Book 1 Table 47 and EMV Contactless Book B Table 3-2)
	 */
	private static final ITag[] DIRECTORY_ENTRY_TAGS = { EmvTags.AID_CARD, EmvTags.DDF_NAME, EmvTags.APPLICATION_LABEL,
			EmvTags.APPLICATION_PRIORITY_INDICATOR, EmvTags.PATH, EmvTags.KERNEL_IDENTIFIER, EmvTags.EXTENDED_SELECTION,
			EmvTags.APPLICATION_SELECTION_REGISTERED_PROPRIETARY_DATA, EmvTags.APPLICATION_PREFERRED_NAME };

	/**
	 * Method used to read the data objects an application directory entry
	 * describes its application with.
	 * <p>
	 * They are the immediate children of the Application Template. Looking for
	 * them at any depth would read the Directory Discretionary Template ('73')
	 * as well, whose content is issuer proprietary: a '87' the issuer put there
	 * is not the priority of the application, and a '9F2A' is not its kernel.
	 * The Application Selection Registered Proprietary Data is the exception,
	 * EMV 4.4 Book 1 Table 12 nesting it in that very template. A card that
	 * nests the others one level deeper is still read, the search falling back
	 * on the whole entry when the surface holds nothing.
	 * </p>
	 *
	 * @param pEntry
	 *            value of one Application Template
	 * @return the data objects in card order, never null
	 */
	protected List<TLV> directoryEntryObjects(final byte[] pEntry) {
		List<TLV> ret = new ArrayList<TLV>();
		BerTlvInputStream stream = new BerTlvInputStream(pEntry);
		while (stream.available() > 0) {
			TLV tlv = TlvUtil.getNextTLV(stream);
			if (tlv == null) {
				break;
			}
			if (ArrayUtils.contains(DIRECTORY_ENTRY_TAGS, tlv.getTag())) {
				ret.add(tlv);
			}
		}
		if (ret.isEmpty()) {
			LOGGER.debug("No directory entry data object at the surface of the entry, the whole entry is searched");
			return TlvUtil.getlistTLV(pEntry, DIRECTORY_ENTRY_TAGS);
		}
		// EMV 4.4 Book 1 Table 12 puts the Application Selection Registered
		// Proprietary Data inside the Directory Discretionary Template, so
		// this one is the exception: it is looked for at any depth
		if (!containsTag(ret, EmvTags.APPLICATION_SELECTION_REGISTERED_PROPRIETARY_DATA)) {
			ret.addAll(TlvUtil.getlistTLV(pEntry, EmvTags.APPLICATION_SELECTION_REGISTERED_PROPRIETARY_DATA));
		}
		return ret;
	}

	/**
	 * Method used to know if a data object of the parameter list carries the
	 * tag
	 *
	 * @param pList
	 *            data objects read
	 * @param pTag
	 *            tag to find
	 * @return true when one of the data objects carries the tag
	 */
	private static boolean containsTag(final List<TLV> pList, final ITag pTag) {
		for (TLV tlv : pList) {
			if (tlv.getTag() == pTag) {
				return true;
			}
		}
		return false;
	}

	protected List<DirectoryEntry> parseDirectoryEntries(final byte[] pData) {
		List<DirectoryEntry> ret = new ArrayList<DirectoryEntry>();
		// Search Application template
		List<TLV> listTlv = TlvUtil.getlistTLV(pData, EmvTags.APPLICATION_TEMPLATE);
		// For each application template
		for (TLV tlv : listTlv) {
			DirectoryEntry entry = new DirectoryEntry();
			// The whole entry is kept as the card sent it, with every primitive
			// data object it holds (the content of the Directory Discretionary
			// Template included): the typed getters are the decoded view
			entry.setRaw(tlv.getValueBytes());
			entry.getDataObjects().putAll(collectPrimitiveDataObjects(tlv.getValueBytes()));
			// Get AID, Kernel_Identifier and application label
			List<TLV> listTlvData = directoryEntryObjects(tlv.getValueBytes());
			// Set once a second ADF Name has been met: nothing that follows it
			// can be attributed to either application
			boolean ambiguous = false;
			// For each data
			for (TLV data : listTlvData) {
				if (data.getTag() == EmvTags.AID_CARD && entry.getAid() != null) {
					// One directory entry describes one application (EMV 4.3
					// Book 1 Table 47). Reading past a second ADF Name would
					// attribute the label and the priority that follow it to the
					// application already added, and EMV Contactless Book B
					// Table 3-2 warns that "the order of data elements within
					// the FCI may vary": nothing after the ambiguity can be
					// attributed to either application. The additional names
					// are kept apart, the first one alone is the AID.
					if (!ambiguous) {
						LOGGER.warn("Several ADF Names in a single directory entry, the first one is kept alone");
					}
					ambiguous = true;
					entry.getAdditionalAdfNames().add(data.getValueBytes());
					continue;
				}
				if (ambiguous) {
					continue;
				}
				if (data.getTag() == EmvTags.APPLICATION_PRIORITY_INDICATOR) {
					if (data.getValueBytes().length > 0) {
						entry.setApplicationPriorityIndicator(data.getValueBytes()[0]);
					}
				} else if (data.getTag() == EmvTags.APPLICATION_LABEL) {
					// EMV 4.3 Book 1 section 12.4 displays "the Application Label,
					// if present, by using the common character set of ISO/IEC
					// 8859": the platform default charset of the device has
					// nothing to do with it
					entry.setApplicationLabel(EmvDataUtils.decodeText(data.getValueBytes(), 0));
				} else if (data.getTag() == EmvTags.KERNEL_IDENTIFIER) {
					entry.setKernelIdentifier(data.getValueBytes());
				} else if (data.getTag() == EmvTags.EXTENDED_SELECTION) {
					entry.setExtendedSelection(data.getValueBytes());
				} else if (data.getTag() == EmvTags.APPLICATION_SELECTION_REGISTERED_PROPRIETARY_DATA) {
					entry.setApplicationSelectionRegisteredProprietaryData(data.getValueBytes());
				} else if (data.getTag() == EmvTags.PATH) {
					entry.setPath(data.getValueBytes());
				} else if (data.getTag() == EmvTags.DDF_NAME) {
					entry.setDdfName(data.getValueBytes());
				} else if (data.getTag() == EmvTags.APPLICATION_PREFERRED_NAME) {
					// Decoded with the common character set for now, the
					// directory FCI may name a code table once it is applied
					// (see applyDirectoryPreferences)
					entry.setRawApplicationPreferredName(data.getValueBytes());
					entry.setApplicationPreferredName(EmvDataUtils.decodeText(data.getValueBytes(), 0));
				} else if (entry.getAid() == null) {
					entry.setAid(data.getValueBytes());
				}
			}
			// The Directory Discretionary Template is read apart: naming it in
			// the list above would stop the search at the template itself and
			// hide the Application Selection Registered Proprietary Data that
			// EMV 4.4 Book 1 Table 12 nests in it
			entry.setDirectoryDiscretionaryTemplate(TlvUtil.getValue(tlv.getValueBytes(), EmvTags.DD_TEMPLATE));
			// An ADF Name is mandatory in an ADF entry and a DDF Name in a DDF
			// one: an entry with neither describes nothing
			if (entry.getAid() != null || entry.getDdfName() != null) {
				ret.add(entry);
			} else {
				LOGGER.warn("Directory entry without ADF Name nor DDF Name, it is ignored");
				// Ignored for the selection, but still something the card
				// listed: it stays reachable apart from the directory entries
				card.getIgnoredDirectoryEntries().add(entry);
			}
		}
		return ret;
	}

	/**
	 * Method used to build the application a directory entry describes.<br>
	 * The application only keeps what the selection uses, it holds the entry it
	 * comes from so that the rest of it stays reachable.
	 *
	 * @param pEntry
	 *            directory entry to convert
	 * @return the application, or null when the entry describes none
	 */
	protected Application toApplication(final DirectoryEntry pEntry) {
		if (pEntry == null || pEntry.getAid() == null) {
			// A directory entry holding a DDF Name (tag '9D') references a sub
			// directory, not an application: it is followed by
			// readDirectoryDefinitionFiles
			return null;
		}
		Application application = new Application();
		application.setDirectoryEntry(pEntry);
		application.setAid(pEntry.getAid());
		if (pEntry.getApplicationLabel() != null) {
			application.setApplicationLabel(pEntry.getApplicationLabel());
		}
		if (pEntry.isPriorityKnown()) {
			application.setApplicationPriorityIndicator((byte) pEntry.getApplicationPriorityIndicator());
		}
		if (pEntry.getKernelIdentifier() != null) {
			application.setKernelIdentifier(pEntry.getKernelIdentifier());
		}
		if (pEntry.getExtendedSelection() != null) {
			application.setExtendedSelection(pEntry.getExtendedSelection());
		}
		if (pEntry.getApplicationSelectionRegisteredProprietaryData() != null) {
			application.setApplicationSelectionRegisteredProprietaryData(
					pEntry.getApplicationSelectionRegisteredProprietaryData());
		}
		// The Issuer Identification Number has two homes on a card, EMV 4.4
		// Book 3 Annex A1 giving both the tag '42' and the tag '9F0C' the
		// "Template: 'BF0C' or '73'". The directory entry carries the second
		// one, the File Control Information of the SELECT carries the first:
		// reading it here is what makes the number available for a card that
		// only publishes it in its directory
		byte[] discretionary = pEntry.getDirectoryDiscretionaryTemplate();
		if (discretionary != null) {
			application.setIssuerIdentificationNumber(EmvDataUtils
					.getNumericValue(TlvUtil.getValue(discretionary, EmvTags.ISSUER_IDENTIFICATION_NUMBER)));
			application.setIssuerIdentificationNumberExtended(EmvDataUtils
					.getNumericValue(TlvUtil.getValue(discretionary, EmvTags.ISSUER_IDENTIFICATION_NUMBER_EXTENDED)));
		}
		return application;
	}

	/**
	 * Read EMV card with AID
	 */
	protected void readWithAID() throws CommunicationException {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Try to read card with AID");
		}
		// The payment environment could not be read, the applications are
		// looked up with the list of AIDs
		card.setReadWithAid(true);
		// Names of the applications already read. An AID they start with
		// would select one of them again: the full AIDs of a scheme are
		// covered by its RID once the occurrences of the RID are enumerated
		Set<String> known = new HashSet<String>();
		// A card that answers a RID without naming its Dedicated File keeps the
		// RID as its name, so the full AID built on it is not recognised as
		// known: the same file would be reported twice under its two names
		Map<String, Set<String>> knownFci = new HashMap<String, Set<String>>();
		// Names the card answered for an application that could not be kept.
		// Matched exactly, never as a prefix: recording one must not suppress
		// a shorter AID of the list, which designates another application
		Set<String> unusable = new HashSet<String>();
		boolean found = false;
		// Test each card from know EMV AID
		for (EmvCardScheme type : EmvCardScheme.values()) {
			for (byte[] aid : type.getAidByte()) {
				if (isKnownApplication(known, aid) || unusable.contains(BytesUtils.bytesToStringNoSpace(aid))) {
					continue;
				}
				Application app = new Application();
				app.setAid(aid);
				// The AID asked for stays known once the DF Name the card
				// answers has replaced getAid()
				app.setRequestedAid(aid);
				app.setApplicationLabel(type.getName());
				if (!parse(app)) {
					// Selected but unreadable: its name is recorded all the
					// same, otherwise the other AIDs of the scheme select it
					// and read it again for a result that is dropped
					if (app.getReadingStep() != ApplicationStepEnum.NOT_SELECTED) {
						unusable.add(BytesUtils.bytesToStringNoSpace(app.getAid()));
					}
					continue;
				}
				if (isSameFile(knownFci, app)) {
					// Same file under another name: recorded as if it had been
					// added, so the rest of the list is skipped as it would be
					known.add(BytesUtils.bytesToStringNoSpace(app.getAid()));
					continue;
				}
				if (!found) {
					// Remove previously added Application template
					card.getApplications().clear();
					found = true;
				}
				// Add Application
				card.getApplications().add(app);
				// A partial AID may match several applications of the card
				readNextOccurrences(aid, type.getName());
				for (Application application : card.getApplications()) {
					known.add(BytesUtils.bytesToStringNoSpace(application.getAid()));
					String fci = fciKey(application);
					String selected = BytesUtils.bytesToStringNoSpace(application.getRequestedAid());
					if (fci != null && selected.length() > 0) {
						Set<String> names = knownFci.get(fci);
						if (names == null) {
							names = new HashSet<String>();
							knownFci.put(fci, names);
						}
						names.add(selected);
					}
				}
				// A card holds applications of several schemes (a domestic one
				// next to an international one is the common case), the
				// lookup only stops at the first one when a single application
				// is asked for
				if (!config.readAllAids) {
					return;
				}
			}
		}
		if (found) {
			// Same order as the directory path, see readNextOccurrences
			Collections.sort(card.getApplications());
		}
	}

	/**
	 * Method used to know if the same file was already reported under another
	 * name.
	 * <p>
	 * Answering the same File Control Information is not enough: a card that
	 * names no Dedicated File could answer identical bytes for two distinct
	 * applications, and dropping one would lose an account. Only a name that
	 * extends the one already read, or that it extends, designates the same
	 * file.
	 * </p>
	 *
	 * @param pKnownFci
	 *            File Control Information already read, mapped to every AID it
	 *            was selected with
	 * @param pApplication
	 *            application just read
	 * @return true if the application duplicates one already read
	 */
	private static boolean isSameFile(final Map<String, Set<String>> pKnownFci, final Application pApplication) {
		String fci = fciKey(pApplication);
		if (fci == null) {
			return false;
		}
		Set<String> names = pKnownFci.get(fci);
		String requested = BytesUtils.bytesToStringNoSpace(pApplication.getRequestedAid());
		if (names == null || requested.length() == 0) {
			return false;
		}
		for (String read : names) {
			if (requested.startsWith(read) || read.startsWith(requested)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Method used to build the key identifying the file an application was
	 * read from.
	 *
	 * @param pApplication
	 *            application read
	 * @return the File Control Information as hexadecimal, null when none
	 */
	private static String fciKey(final Application pApplication) {
		byte[] fci = pApplication != null ? pApplication.getRawFci() : null;
		return fci != null && fci.length > 0 ? BytesUtils.bytesToStringNoSpace(fci) : null;
	}

	/**
	 * Method used to know if an AID of the list designates an application that
	 * has already been read, exactly or as a prefix of its name.
	 *
	 * @param pKnown
	 *            names of the applications already read, hexadecimal
	 * @param pAid
	 *            AID or RID about to be selected
	 * @return true if the selection could only answer an application already
	 *         read
	 */
	private static boolean isKnownApplication(final Set<String> pKnown, final byte[] pAid) {
		if (pAid == null || pKnown.isEmpty()) {
			return false;
		}
		String aid = BytesUtils.bytesToStringNoSpace(pAid);
		for (String name : pKnown) {
			if (name != null && name.startsWith(aid)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Method used to enumerate the applications matching a partial AID.
	 * <p>
	 * EMV 4.3 Book 1 section 12.3.2 asks the terminal to repeat the SELECT
	 * command with the "next occurrence" option until the card answers that
	 * there is no further application, otherwise only the first application
	 * built on the RID is seen.
	 * </p>
	 * <p>
	 * <b>Two known limitations.</b> Each occurrence is read in full before the
	 * next one is asked for, where section 11.3.5 only guarantees the
	 * enumeration for "repeated issuing of the same command with no intervening
	 * application level commands": a card that resets its occurrence pointer
	 * when it receives a GET PROCESSING OPTIONS answers the first application
	 * again, and the duplicate check below then ends the enumeration. That check
	 * keys on the Dedicated File Name, so a card that returns none cannot have
	 * its occurrences told apart either. Both cases degrade to the first
	 * application of the RID, never to a wrong one.
	 * </p>
	 *
	 * @param pAid
	 *            partial AID that matched
	 * @param pLabel
	 *            default label of the scheme
	 * @throws CommunicationException communication error
	 */
	protected void readNextOccurrences(final byte[] pAid, final String pLabel) throws CommunicationException {
		// A full AID identifies a single application, there is nothing to
		// enumerate
		if (!config.readAllAids || pAid == null || pAid.length != RID_LENGTH) {
			return;
		}
		Set<String> known = new HashSet<String>();
		for (Application application : card.getApplications()) {
			known.add(BytesUtils.bytesToStringNoSpace(application.getAid()));
		}
		for (int i = 0; i < MAX_AID_OCCURRENCES; i++) {
			Application app = new Application();
			app.setAid(pAid);
			app.setRequestedAid(pAid);
			app.setApplicationLabel(pLabel);
			app.setNextOccurrence(true);
			if (!parse(app)) {
				// EMV 4.3 Book 1 section 12.3.3 step 7: "If the ICC returns
				// SW1 SW2 = '9000', '62xx', or '63xx', the terminal returns to
				// step 3. If it returns a different SW1 SW2, the terminal goes
				// to step 5", that is to say a warning means that this
				// occurrence cannot be used, not that the card has no further
				// one. '6283' (blocked application) is one of those warnings.
				if (app.isBlocked() || app.isSelectWarning()) {
					if (LOGGER.isDebugEnabled()) {
						LOGGER.debug("Occurrence answered a warning status, the enumeration continues");
					}
					continue;
				}
				break;
			}
			// ISO 7816-4 only asks the card to answer '6A82' when it manages
			// occurrences: a card that ignores the option answers the same ADF
			// over and over, reading it again would duplicate the application
			// and waste the contactless session
			if (!known.add(BytesUtils.bytesToStringNoSpace(app.getAid()))) {
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("The card does not manage AID occurrences, stop enumerating");
				}
				break;
			}
			card.getApplications().add(app);
		}
		// EMV 4.3 Book 1 section 12.4: the applications are offered "in priority
		// sequence as indicated by the Application Priority Indicator with the
		// highest priority application offered first". The directory path sorts
		// the list in readWithPSE, the list of AIDs has to do the same
		Collections.sort(card.getApplications());
	}

	/**
	 * Method used to submit an application to the first parser that handles its
	 * AID
	 *
	 * @param pApplication
	 *            application to read
	 * @return true if a parser could read the application
	 * @throws CommunicationException communication error
	 */
	private boolean parse(final Application pApplication) throws CommunicationException {
		String applicationAid = BytesUtils.bytesToStringNoSpace(pApplication.getAid());
		for (IParser impl : parsers) {
			// Keep trying the parsers that handle the AID until one of them
			// manages to read the application
			if (impl.getId() != null && impl.getId().matcher(applicationAid).matches() && impl.parse(pApplication)) {
				return true;
			}
		}
		return false;
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
