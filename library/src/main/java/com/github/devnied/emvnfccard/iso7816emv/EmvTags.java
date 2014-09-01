/*
 * Copyright 2010 sasc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.devnied.emvnfccard.iso7816emv;

import java.lang.reflect.Field;
import java.util.LinkedHashMap;

import com.github.devnied.emvnfccard.enums.TagValueTypeEnum;
import com.github.devnied.emvnfccard.iso7816emv.impl.TagImpl;

/**
 * http://www.emvlab.org/emvtags/all/
 * 
 * 
 * The coding of primitive context-specific class data objects in the ranges '80' to '9E' and '9F00' to '9F4F' is reserved for EMV
 * specification. The coding of primitive context-specific class data objects in the range '9F50' to '9F7F' is reserved for the
 * payment systems.
 * 
 * @author sasc
 */
public final class EmvTags {

	private static LinkedHashMap<ByteArrayWrapper, ITag> tags = new LinkedHashMap<ByteArrayWrapper, ITag>();

	// @formatter:off
	// One byte tags
	// 7816-4 Interindustry data object for tag allocation authority
	public static final ITag UNIVERSAL_TAG_FOR_OID = new TagImpl("06", TagValueTypeEnum.BINARY, "Object Identifier (OID)", "Universal tag for OID");
	public static final ITag COUNTRY_CODE = new TagImpl("41", TagValueTypeEnum.NUMERIC, "Country Code", "Country code (encoding specified in ISO 3166-1) and optional national data");
	public static final ITag ISSUER_IDENTIFICATION_NUMBER = new TagImpl("42", TagValueTypeEnum.NUMERIC, "Issuer Identification Number (IIN)", "The number that identifies the major industry and the card issuer and that forms the first part of the Primary Account Number (PAN)");

	// 7816-4 Interindustry data objects for application identification and selection
	public static final ITag AID_CARD = new TagImpl("4f", TagValueTypeEnum.BINARY, "Application Identifier (AID) - card", "Identifies the application as described in ISO/IEC 7816-5");
	public static final ITag APPLICATION_LABEL = new TagImpl("50", TagValueTypeEnum.TEXT, "Application Label", "Mnemonic associated with the AID according to ISO/IEC 7816-5");
	public static final ITag PATH = new TagImpl("51", TagValueTypeEnum.BINARY, "File reference data element", "ISO-7816 Path");
	public static final ITag COMMAND_APDU = new TagImpl("52", TagValueTypeEnum.BINARY, "Command APDU", "");
	public static final ITag DISCRETIONARY_DATA_OR_TEMPLATE = new TagImpl("53", TagValueTypeEnum.BINARY, "Discretionary data (or template)", "");
	public static final ITag APPLICATION_TEMPLATE = new TagImpl("61", TagValueTypeEnum.BINARY, "Application Template", "Contains one or more data objects relevant to an application directory entry according to ISO/IEC 7816-5");
	public static final ITag FCI_TEMPLATE = new TagImpl("6f", TagValueTypeEnum.BINARY, "File Control Information (FCI) Template", "Set of file control parameters and file management data (according to ISO/IEC 7816-4)");
	public static final ITag DD_TEMPLATE = new TagImpl("73", TagValueTypeEnum.BINARY, "Directory Discretionary Template", "Issuer discretionary part of the directory according to ISO/IEC 7816-5");
	public static final ITag DEDICATED_FILE_NAME = new TagImpl("84", TagValueTypeEnum.BINARY, "Dedicated File (DF) Name", "Identifies the name of the DF as described in ISO/IEC 7816-4");
	public static final ITag SFI = new TagImpl("88", TagValueTypeEnum.BINARY, "Short File Identifier (SFI)", "Identifies the SFI to be used in the commands related to a given AEF or DDF. The SFI data object is a binary field with the three high order bits set to zero");

	public static final ITag FCI_PROPRIETARY_TEMPLATE = new TagImpl("a5", TagValueTypeEnum.BINARY, "File Control Information (FCI) Proprietary Template", "Identifies the data object proprietary to this specification in the FCI template according to ISO/IEC 7816-4");
	public static final ITag ISSUER_URL = new TagImpl("5f50", TagValueTypeEnum.TEXT, "Issuer URL", "The URL provides the location of the Issuerâ€™s Library Server on the Internet");

	// EMV
	public static final ITag TRACK_2_EQV_DATA = new TagImpl("57", TagValueTypeEnum.BINARY, "Track 2 Equivalent Data", "Contains the data elements of track 2 according to ISO/IEC 7813, excluding start sentinel, end sentinel, and Longitudinal Redundancy Check (LRC)");
	public static final ITag PAN = new TagImpl("5a", TagValueTypeEnum.NUMERIC, "Application Primary Account Number (PAN)", "Valid cardholder account number");
	public static final ITag RECORD_TEMPLATE = new TagImpl("70", TagValueTypeEnum.BINARY, "Record Template (EMV Proprietary)", "Template proprietary to the EMV specification");
	public static final ITag ISSUER_SCRIPT_TEMPLATE_1 = new TagImpl("71", TagValueTypeEnum.BINARY, "Issuer Script Template 1", "Contains proprietary issuer data for transmission to the ICC before the second GENERATE AC command");
	public static final ITag ISSUER_SCRIPT_TEMPLATE_2 = new TagImpl("72", TagValueTypeEnum.BINARY, "Issuer Script Template 2", "Contains proprietary issuer data for transmission to the ICC after the second GENERATE AC command");
	public static final ITag RESPONSE_MESSAGE_TEMPLATE_2 = new TagImpl("77", TagValueTypeEnum.BINARY, "Response Message Template Format 2", "Contains the data objects (with tags and lengths) returned by the ICC in response to a command");
	public static final ITag RESPONSE_MESSAGE_TEMPLATE_1 = new TagImpl("80", TagValueTypeEnum.BINARY, "Response Message Template Format 1", "Contains the data objects (without tags and lengths) returned by the ICC in response to a command");
	public static final ITag AMOUNT_AUTHORISED_BINARY = new TagImpl("81", TagValueTypeEnum.BINARY, "Amount, Authorised (Binary)", "Authorised amount of the transaction (excluding adjustments)");
	public static final ITag APPLICATION_INTERCHANGE_PROFILE = new TagImpl("82", TagValueTypeEnum.BINARY, "Application Interchange Profile", "Indicates the capabilities of the card to support specific functions in the application");
	public static final ITag COMMAND_TEMPLATE = new TagImpl("83", TagValueTypeEnum.BINARY, "Command Template", "Identifies the data field of a command message");
	public static final ITag ISSUER_SCRIPT_COMMAND = new TagImpl("86", TagValueTypeEnum.BINARY, "Issuer Script Command", "Contains a command for transmission to the ICC");
	public static final ITag APPLICATION_PRIORITY_INDICATOR = new TagImpl("87", TagValueTypeEnum.BINARY, "Application Priority Indicator", "Indicates the priority of a given application or group of applications in a directory");
	public static final ITag AUTHORISATION_CODE = new TagImpl("89", TagValueTypeEnum.BINARY, "Authorisation Code", "Value generated by the authorisation authority for an approved transaction");
	public static final ITag AUTHORISATION_RESPONSE_CODE = new TagImpl("8a", TagValueTypeEnum.TEXT, "Authorisation Response Code", "Code that defines the disposition of a message");
	public static final ITag CDOL1 = new TagImpl("8c", TagValueTypeEnum.DOL, "Card Risk Management Data Object List 1 (CDOL1)", "List of data objects (tag and length) to be passed to the ICC in the first GENERATE AC command");
	public static final ITag CDOL2 = new TagImpl("8d", TagValueTypeEnum.DOL, "Card Risk Management Data Object List 2 (CDOL2)", "List of data objects (tag and length) to be passed to the ICC in the second GENERATE AC command");
	public static final ITag CVM_LIST = new TagImpl("8e", TagValueTypeEnum.BINARY, "Cardholder Verification Method (CVM) List", "Identifies a method of verification of the cardholder supported by the application");
	public static final ITag CA_PUBLIC_KEY_INDEX_CARD = new TagImpl("8f", TagValueTypeEnum.BINARY, "Certification Authority Public Key Index - card", "Identifies the certification authorityâ€™s public key in conjunction with the RID");
	public static final ITag ISSUER_PUBLIC_KEY_CERT = new TagImpl("90", TagValueTypeEnum.BINARY, "Issuer Public Key Certificate", "Issuer public key certified by a certification authority");
	public static final ITag ISSUER_AUTHENTICATION_DATA = new TagImpl("91", TagValueTypeEnum.BINARY, "Issuer Authentication Data", "Data sent to the ICC for online issuer authentication");
	public static final ITag ISSUER_PUBLIC_KEY_REMAINDER = new TagImpl("92", TagValueTypeEnum.BINARY, "Issuer Public Key Remainder", "Remaining digits of the Issuer Public Key Modulus");
	public static final ITag SIGNED_STATIC_APP_DATA = new TagImpl("93", TagValueTypeEnum.BINARY, "Signed Static Application Data", "Digital signature on critical application parameters for SDA");
	public static final ITag APPLICATION_FILE_LOCATOR = new TagImpl("94", TagValueTypeEnum.BINARY, "Application File Locator (AFL)", "Indicates the location (SFI, range of records) of the AEFs related to a given application");
	public static final ITag TERMINAL_VERIFICATION_RESULTS = new TagImpl("95", TagValueTypeEnum.BINARY, "Terminal Verification Results (TVR)", "Status of the different functions as seen from the terminal");
	public static final ITag TDOL = new TagImpl("97", TagValueTypeEnum.BINARY, "Transaction Certificate Data Object List (TDOL)", "List of data objects (tag and length) to be used by the terminal in generating the TC Hash Value");
	public static final ITag TC_HASH_VALUE = new TagImpl("98", TagValueTypeEnum.BINARY, "Transaction Certificate (TC) Hash Value", "Result of a hash function specified in Book 2, Annex B3.1");
	public static final ITag TRANSACTION_PIN_DATA = new TagImpl("99", TagValueTypeEnum.BINARY, "Transaction Personal Identification Number (PIN) Data", "Data entered by the cardholder for the purpose of the PIN verification");
	public static final ITag TRANSACTION_DATE = new TagImpl("9a", TagValueTypeEnum.NUMERIC, "Transaction Date", "Local date that the transaction was authorised");
	public static final ITag TRANSACTION_STATUS_INFORMATION = new TagImpl("9b", TagValueTypeEnum.BINARY, "Transaction Status Information", "Indicates the functions performed in a transaction");
	public static final ITag TRANSACTION_TYPE = new TagImpl("9c", TagValueTypeEnum.NUMERIC, "Transaction Type", "Indicates the type of financial transaction, represented by the first two digits of ISO 8583:1987 Processing Code");
	public static final ITag DDF_NAME = new TagImpl("9d", TagValueTypeEnum.BINARY, "Directory Definition File (DDF) Name", "Identifies the name of a DF associated with a directory");
	// Two byte tags
	public static final ITag CARDHOLDER_NAME = new TagImpl("5f20", TagValueTypeEnum.TEXT, "Cardholder Name", "Indicates cardholder name according to ISO 7813");
	public static final ITag APP_EXPIRATION_DATE = new TagImpl("5f24", TagValueTypeEnum.NUMERIC, "Application Expiration Date", "Date after which application expires");
	public static final ITag APP_EFFECTIVE_DATE = new TagImpl("5f25", TagValueTypeEnum.NUMERIC, "Application Effective Date", "Date from which the application may be used");
	public static final ITag ISSUER_COUNTRY_CODE = new TagImpl("5f28", TagValueTypeEnum.NUMERIC, "Issuer Country Code", "Indicates the country of the issuer according to ISO 3166");
	public static final ITag TRANSACTION_CURRENCY_CODE = new TagImpl("5f2a", TagValueTypeEnum.TEXT, "Transaction Currency Code", "Indicates the currency code of the transaction according to ISO 4217");
	public static final ITag LANGUAGE_PREFERENCE = new TagImpl("5f2d", TagValueTypeEnum.TEXT, "Language Preference", "1â€“4 languages stored in order of preference, each represented by 2 alphabetical characters according to ISO 639");
	public static final ITag SERVICE_CODE = new TagImpl("5f30", TagValueTypeEnum.NUMERIC, "Service Code", "Service code as defined in ISO/IEC 7813 for track 1 and track 2");
	public static final ITag PAN_SEQUENCE_NUMBER = new TagImpl("5f34", TagValueTypeEnum.NUMERIC, "Application Primary Account Number (PAN) Sequence Number", "Identifies and differentiates cards with the same PAN");
	public static final ITag TRANSACTION_CURRENCY_EXP = new TagImpl("5f36", TagValueTypeEnum.NUMERIC, "Transaction Currency Exponent", "Indicates the implied position of the decimal point from the right of the transaction amount represented according to ISO 4217");
	public static final ITag IBAN = new TagImpl("5f53", TagValueTypeEnum.BINARY, "International Bank Account Number (IBAN)", "Uniquely identifies the account of a customer at a financial institution as defined in ISO 13616");
	public static final ITag BANK_IDENTIFIER_CODE = new TagImpl("5f54", TagValueTypeEnum.MIXED, "Bank Identifier Code (BIC)", "Uniquely identifies a bank as defined in ISO 9362");
	public static final ITag ISSUER_COUNTRY_CODE_ALPHA2 = new TagImpl("5f55", TagValueTypeEnum.TEXT, "Issuer Country Code (alpha2 format)", "Indicates the country of the issuer as defined in ISO 3166 (using a 2 character alphabetic code)");
	public static final ITag ISSUER_COUNTRY_CODE_ALPHA3 = new TagImpl("5f56", TagValueTypeEnum.TEXT, "Issuer Country Code (alpha3 format)", "Indicates the country of the issuer as defined in ISO 3166 (using a 3 character alphabetic code)");
	public static final ITag ACQUIRER_IDENTIFIER = new TagImpl("9f01", TagValueTypeEnum.NUMERIC, "Acquirer Identifier", "Uniquely identifies the acquirer within each payment system");
	public static final ITag AMOUNT_AUTHORISED_NUMERIC = new TagImpl("9f02", TagValueTypeEnum.NUMERIC, "Amount, Authorised (Numeric)", "Authorised amount of the transaction (excluding adjustments)");
	public static final ITag AMOUNT_OTHER_NUMERIC = new TagImpl("9f03", TagValueTypeEnum.NUMERIC, "Amount, Other (Numeric)", "Secondary amount associated with the transaction representing a cashback amount");
	public static final ITag AMOUNT_OTHER_BINARY = new TagImpl("9f04", TagValueTypeEnum.NUMERIC, "Amount, Other (Binary)", "Secondary amount associated with the transaction representing a cashback amount");
	public static final ITag APP_DISCRETIONARY_DATA = new TagImpl("9f05", TagValueTypeEnum.BINARY, "Application Discretionary Data", "Issuer or payment system specified data relating to the application");
	public static final ITag AID_TERMINAL = new TagImpl("9f06", TagValueTypeEnum.BINARY, "Application Identifier (AID) - terminal", "Identifies the application as described in ISO/IEC 7816-5");
	public static final ITag APP_USAGE_CONTROL = new TagImpl("9f07", TagValueTypeEnum.BINARY, "Application Usage Control", "Indicates issuerâ€™s specified restrictions on the geographic usage and services allowed for the application");
	public static final ITag APP_VERSION_NUMBER_CARD = new TagImpl("9f08", TagValueTypeEnum.BINARY, "Application Version Number - card", "Version number assigned by the payment system for the application");
	public static final ITag APP_VERSION_NUMBER_TERMINAL = new TagImpl("9f09", TagValueTypeEnum.BINARY, "Application Version Number - terminal", "Version number assigned by the payment system for the application");
	public static final ITag CARDHOLDER_NAME_EXTENDED = new TagImpl("9f0b", TagValueTypeEnum.TEXT, "Cardholder Name Extended", "Indicates the whole cardholder name when greater than 26 characters using the same coding convention as in ISO 7813");
	public static final ITag ISSUER_ACTION_CODE_DEFAULT = new TagImpl("9f0d", TagValueTypeEnum.BINARY, "Issuer Action Code - Default", "Specifies the issuerâ€™s conditions that cause a transaction to be rejected if it might have been approved online, but the terminal is unable to process the transaction online");
	public static final ITag ISSUER_ACTION_CODE_DENIAL = new TagImpl("9f0e", TagValueTypeEnum.BINARY, "Issuer Action Code - Denial", "Specifies the issuerâ€™s conditions that cause the denial of a transaction without attempt to go online");
	public static final ITag ISSUER_ACTION_CODE_ONLINE = new TagImpl("9f0f", TagValueTypeEnum.BINARY, "Issuer Action Code - Online", "Specifies the issuerâ€™s conditions that cause a transaction to be transmitted online");
	public static final ITag ISSUER_APPLICATION_DATA = new TagImpl("9f10", TagValueTypeEnum.BINARY, "Issuer Application Data", "Contains proprietary application data for transmission to the issuer in an online transaction");
	public static final ITag ISSUER_CODE_TABLE_INDEX = new TagImpl("9f11", TagValueTypeEnum.NUMERIC, "Issuer Code Table Index", "Indicates the code table according to ISO/IEC 8859 for displaying the Application Preferred Name");
	public static final ITag APP_PREFERRED_NAME = new TagImpl("9f12", TagValueTypeEnum.TEXT, "Application Preferred Name", "Preferred mnemonic associated with the AID");
	public static final ITag LAST_ONLINE_ATC_REGISTER = new TagImpl("9f13", TagValueTypeEnum.BINARY, "Last Online Application Transaction Counter (ATC) Register", "ATC value of the last transaction that went online");
	public static final ITag LOWER_CONSEC_OFFLINE_LIMIT = new TagImpl("9f14", TagValueTypeEnum.BINARY, "Lower Consecutive Offline Limit", "Issuer-specified preference for the maximum number of consecutive offline transactions for this ICC application allowed in a terminal with online capability");
	public static final ITag MERCHANT_CATEGORY_CODE = new TagImpl("9f15", TagValueTypeEnum.NUMERIC, "Merchant Category Code", "Classifies the type of business being done by the merchant, represented according to ISO 8583:1993 for Card Acceptor Business Code");
	public static final ITag MERCHANT_IDENTIFIER = new TagImpl("9f16", TagValueTypeEnum.TEXT, "Merchant Identifier", "When concatenated with the Acquirer Identifier, uniquely identifies a given merchant");
	public static final ITag PIN_TRY_COUNTER = new TagImpl("9f17", TagValueTypeEnum.BINARY, "Personal Identification Number (PIN) Try Counter", "Number of PIN tries remaining");
	public static final ITag ISSUER_SCRIPT_IDENTIFIER = new TagImpl("9f18", TagValueTypeEnum.BINARY, "Issuer Script Identifier", "Identification of the Issuer Script");
	public static final ITag TERMINAL_COUNTRY_CODE = new TagImpl("9f1a", TagValueTypeEnum.TEXT, "Terminal Country Code", "Indicates the country of the terminal, represented according to ISO 3166");
	public static final ITag TERMINAL_FLOOR_LIMIT = new TagImpl("9f1b", TagValueTypeEnum.BINARY, "Terminal Floor Limit", "Indicates the floor limit in the terminal in conjunction with the AID");
	public static final ITag TERMINAL_IDENTIFICATION = new TagImpl("9f1c", TagValueTypeEnum.TEXT, "Terminal Identification", "Designates the unique location of a terminal at a merchant");
	public static final ITag TERMINAL_RISK_MANAGEMENT_DATA = new TagImpl("9f1d", TagValueTypeEnum.BINARY, "Terminal Risk Management Data", "Application-specific value used by the card for risk management purposes");
	public static final ITag INTERFACE_DEVICE_SERIAL_NUMBER = new TagImpl("9f1e", TagValueTypeEnum.TEXT, "Interface Device (IFD) Serial Number", "Unique and permanent serial number assigned to the IFD by the manufacturer");
	public static final ITag TRACK1_DISCRETIONARY_DATA = new TagImpl("9f1f", TagValueTypeEnum.TEXT, "[Magnetic Stripe] Track 1 Discretionary Data", "Discretionary part of track 1 according to ISO/IEC 7813");
	public static final ITag TRACK2_DISCRETIONARY_DATA = new TagImpl("9f20", TagValueTypeEnum.TEXT, "[Magnetic Stripe] Track 2 Discretionary Data", "Discretionary part of track 2 according to ISO/IEC 7813");
	public static final ITag TRANSACTION_TIME = new TagImpl("9f21", TagValueTypeEnum.NUMERIC, "Transaction Time (HHMMSS)", "Local time that the transaction was authorised");
	public static final ITag CA_PUBLIC_KEY_INDEX_TERMINAL = new TagImpl("9f22", TagValueTypeEnum.BINARY, "Certification Authority Public Key Index - Terminal", "Identifies the certification authorityâ€™s public key in conjunction with the RID");
	public static final ITag UPPER_CONSEC_OFFLINE_LIMIT = new TagImpl("9f23", TagValueTypeEnum.BINARY, "Upper Consecutive Offline Limit", "Issuer-specified preference for the maximum number of consecutive offline transactions for this ICC application allowed in a terminal without online capability");
	public static final ITag APP_CRYPTOGRAM = new TagImpl("9f26", TagValueTypeEnum.BINARY, "Application Cryptogram", "Cryptogram returned by the ICC in response of the GENERATE AC command");
	public static final ITag CRYPTOGRAM_INFORMATION_DATA = new TagImpl("9f27", TagValueTypeEnum.BINARY, "Cryptogram Information Data", "Indicates the type of cryptogram and the actions to be performed by the terminal");
	public static final ITag ICC_PIN_ENCIPHERMENT_PUBLIC_KEY_CERT = new TagImpl("9f2d", TagValueTypeEnum.BINARY, "ICC PIN Encipherment Public Key Certificate", "ICC PIN Encipherment Public Key certified by the issuer");
	public static final ITag ICC_PIN_ENCIPHERMENT_PUBLIC_KEY_EXP = new TagImpl("9f2e", TagValueTypeEnum.BINARY, "ICC PIN Encipherment Public Key Exponent", "ICC PIN Encipherment Public Key Exponent used for PIN encipherment");
	public static final ITag ICC_PIN_ENCIPHERMENT_PUBLIC_KEY_REM = new TagImpl("9f2f", TagValueTypeEnum.BINARY, "ICC PIN Encipherment Public Key Remainder", "Remaining digits of the ICC PIN Encipherment Public Key Modulus");
	public static final ITag ISSUER_PUBLIC_KEY_EXP = new TagImpl("9f32", TagValueTypeEnum.BINARY, "Issuer Public Key Exponent", "Issuer public key exponent used for the verification of the Signed Static Application Data and the ICC Public Key Certificate");
	public static final ITag TERMINAL_CAPABILITIES = new TagImpl("9f33", TagValueTypeEnum.BINARY, "Terminal Capabilities", "Indicates the card data input, CVM, and security capabilities of the terminal");
	public static final ITag CVM_RESULTS = new TagImpl("9f34", TagValueTypeEnum.BINARY, "Cardholder Verification (CVM) Results", "Indicates the results of the last CVM performed");
	public static final ITag TERMINAL_TYPE = new TagImpl("9f35", TagValueTypeEnum.NUMERIC, "Terminal Type", "Indicates the environment of the terminal, its communications capability, and its operational control");
	public static final ITag APP_TRANSACTION_COUNTER = new TagImpl("9f36", TagValueTypeEnum.BINARY, "Application Transaction Counter (ATC)", "Counter maintained by the application in the ICC (incrementing the ATC is managed by the ICC)");
	public static final ITag UNPREDICTABLE_NUMBER = new TagImpl("9f37", TagValueTypeEnum.BINARY, "Unpredictable Number", "Value to provide variability and uniqueness to the generation of a cryptogram");
	public static final ITag PDOL = new TagImpl("9f38", TagValueTypeEnum.DOL, "Processing Options Data Object List (PDOL)", "Contains a list of terminal resident data objects (tags and lengths) needed by the ICC in processing the GET PROCESSING OPTIONS command");
	public static final ITag POINT_OF_SERVICE_ENTRY_MODE = new TagImpl("9f39", TagValueTypeEnum.NUMERIC, "Point-of-Service (POS) Entry Mode", "Indicates the method by which the PAN was entered, according to the first two digits of the ISO 8583:1987 POS Entry Mode");
	public static final ITag AMOUNT_REFERENCE_CURRENCY = new TagImpl("9f3a", TagValueTypeEnum.BINARY, "Amount, Reference Currency", "Authorised amount expressed in the reference currency");
	public static final ITag APP_REFERENCE_CURRENCY = new TagImpl("9f3b", TagValueTypeEnum.NUMERIC, "Application Reference Currency", "1â€“4 currency codes used between the terminal and the ICC when the Transaction Currency Code is different from the Application Currency Code; each code is 3 digits according to ISO 4217");
	public static final ITag TRANSACTION_REFERENCE_CURRENCY_CODE = new TagImpl("9f3c", TagValueTypeEnum.NUMERIC, "Transaction Reference Currency Code", "Code defining the common currency used by the terminal in case the Transaction Currency Code is different from the Application Currency Code");
	public static final ITag TRANSACTION_REFERENCE_CURRENCY_EXP = new TagImpl("9f3d", TagValueTypeEnum.NUMERIC, "Transaction Reference Currency Exponent", "Indicates the implied position of the decimal point from the right of the transaction amount, with the Transaction Reference Currency Code represented according to ISO 4217");
	public static final ITag ADDITIONAL_TERMINAL_CAPABILITIES = new TagImpl("9f40", TagValueTypeEnum.BINARY, "Additional Terminal Capabilities", "Indicates the data input and output capabilities of the terminal");
	public static final ITag TRANSACTION_SEQUENCE_COUNTER = new TagImpl("9f41", TagValueTypeEnum.NUMERIC, "Transaction Sequence Counter", "Counter maintained by the terminal that is incremented by one for each transaction");
	public static final ITag APPLICATION_CURRENCY_CODE = new TagImpl("9f42", TagValueTypeEnum.NUMERIC, "Application Currency Code", "Indicates the currency in which the account is managed according to ISO 4217");
	public static final ITag APP_REFERENCE_CURRECY_EXPONENT = new TagImpl("9f43", TagValueTypeEnum.NUMERIC, "Application Reference Currency Exponent", "Indicates the implied position of the decimal point from the right of the amount, for each of the 1â€“4 reference currencies represented according to ISO 4217");
	public static final ITag APP_CURRENCY_EXPONENT = new TagImpl("9f44", TagValueTypeEnum.NUMERIC, "Application Currency Exponent", "Indicates the implied position of the decimal point from the right of the amount represented according to ISO 4217");
	public static final ITag DATA_AUTHENTICATION_CODE = new TagImpl("9f45", TagValueTypeEnum.BINARY, "Data Authentication Code", "An issuer assigned value that is retained by the terminal during the verification process of the Signed Static Application Data");
	public static final ITag ICC_PUBLIC_KEY_CERT = new TagImpl("9f46", TagValueTypeEnum.BINARY, "ICC Public Key Certificate", "ICC Public Key certified by the issuer");
	public static final ITag ICC_PUBLIC_KEY_EXP = new TagImpl("9f47", TagValueTypeEnum.BINARY, "ICC Public Key Exponent", "ICC Public Key Exponent used for the verification of the Signed Dynamic Application Data");
	public static final ITag ICC_PUBLIC_KEY_REMAINDER = new TagImpl("9f48", TagValueTypeEnum.BINARY, "ICC Public Key Remainder", "Remaining digits of the ICC Public Key Modulus");
	public static final ITag DDOL = new TagImpl("9f49", TagValueTypeEnum.DOL, "Dynamic Data Authentication Data Object List (DDOL)", "List of data objects (tag and length) to be passed to the ICC in the INTERNAL AUTHENTICATE command");
	public static final ITag SDA_TAG_LIST = new TagImpl("9f4a", TagValueTypeEnum.BINARY, "Static Data Authentication Tag List", "List of tags of primitive data objects defined in this specification whose value fields are to be included in the Signed Static or Dynamic Application Data");
	public static final ITag SIGNED_DYNAMIC_APPLICATION_DATA = new TagImpl("9f4b", TagValueTypeEnum.BINARY, "Signed Dynamic Application Data", "Digital signature on critical application parameters for DDA or CDA");
	public static final ITag ICC_DYNAMIC_NUMBER = new TagImpl("9f4c", TagValueTypeEnum.BINARY, "ICC Dynamic Number", "Time-variant number generated by the ICC, to be captured by the terminal");
	public static final ITag LOG_ENTRY = new TagImpl("9f4d", TagValueTypeEnum.BINARY, "Log Entry", "Provides the SFI of the Transaction Log file and its number of records");
	public static final ITag MERCHANT_NAME_AND_LOCATION = new TagImpl("9f4e", TagValueTypeEnum.TEXT, "Merchant Name and Location", "Indicates the name and location of the merchant");
	public static final ITag LOG_FORMAT = new TagImpl("9f4f", TagValueTypeEnum.DOL, "Log Format", "List (in tag and length format) of data objects representing the logged data elements that are passed to the terminal when a transaction log record is read");

	public static final ITag FCI_ISSUER_DISCRETIONARY_DATA = new TagImpl("bf0c", TagValueTypeEnum.BINARY, "File Control Information (FCI) Issuer Discretionary Data", "Issuer discretionary part of the FCI (e.g. O/S Manufacturer proprietary data)");
	public static final ITag VISA_LOG_ENTRY = new TagImpl("df60", TagValueTypeEnum.BINARY, "VISA Log Entry", "");
	// '9F50' to '9F7F' are reserved for the payment systems (proprietary)

	// The following tags are specified in EMV Contactless (Book A)

	// The Track 1 Data may be present in the file read using the READ
	// RECORD command during a mag-stripe mode transaction. It is made up of
	// the following sub-fields:
	// +------------------------+--------------+--------------------+
	// | Data Field | Length | Format |
	// +------------------------+--------------+--------------------+
	// | Format Code | 1 | '42' |
	// | Primary Account Number | var up to 19 | digits |
	// | Field Separator | 1 | '5E' |
	// | Name | 2-26 | (see ISO/IEC 7813) |
	// | Field Separator | 1 | '5E' |
	// | Expiry Date | 4 | YYMM |
	// | Service Code | 3 | digits |
	// | Discretionary Data | var. | ans |
	// +------------------------+--------------+--------------------+
	// BER-TLV[56, 29 (raw 29), 42 xx xx xx xx xx xx xx xx xx xx xx xx xx xx xx xx 5e 20 2f 5e xx xx xx xx 32 30 31 30 31 30 30 30
	// 30 30 30 30 30 30 30 30]
	// BER-TLV[56, 34 (raw 34), 42 XX XX XX XX XX XX XX XX XX XX XX XX XX XX XX XX 5e 20 2f 5e YY YY MM MM 32 30 31 30 30 30 30 30
	// 30 30 30 30 30 30 30 30 30 30 30 30 30 30 30 30 30 30 30
	public static final ITag TRACK1_DATA = new TagImpl("56", TagValueTypeEnum.BINARY, "Track 1 Data", "Track 1 Data contains the data objects of the track 1 according to [ISO/IEC 7813] Structure B, excluding start sentinel, end sentinel and LRC.");

	public static final ITag TERMINAL_TRANSACTION_QUALIFIERS = new TagImpl("9f66", TagValueTypeEnum.BINARY, "Terminal Transaction Qualifiers", "Provided by the reader in the GPO command and used by the card to determine processing choices based on reader functionality");
	// The Track 2 Data is present in the file read using the READ RECORD command
	// during a mag-stripe mode transaction. It is made up of the following
	// sub-fields (same as tag 57):
	//
	// +------------------------+-----------------------+-----------+
	// | Data Field | Length | Format |
	// +------------------------+-----------------------+-----------+
	// | Primary Account Number | var. up to 19 nibbles | n |
	// | Field Separator | 1 nibble | b ('D') |
	// | Expiry Date | 2 | n (YYMM) |
	// | Service Code | 3 nibbles | n |
	// | Discretionary Data | var. | n |
	// | Padding if needed | 1 nibble | b ('F') |
	// +------------------------+-----------------------+-----------+

	// 9f6b 13 BB BB BB BB BB BB BB BB dY YM M2 01 00 00 00 00 00 00 0f
	public static final ITag TRACK2_DATA = new TagImpl("9f6b", TagValueTypeEnum.BINARY, "Track 2 Data", "Track 2 Data contains the data objects of the track 2 according to [ISO/IEC 7813] Structure B, excluding start sentinel, end sentinel and LRC.");
	public static final ITag VLP_ISSUER_AUTHORISATION_CODE = new TagImpl("9f6e", TagValueTypeEnum.BINARY, "Visa Low-Value Payment (VLP) Issuer Authorisation Code", "");

	// These are specified in EMV Contactless (Book B)
	public static final ITag EXTENDED_SELECTION = new TagImpl("9f29", TagValueTypeEnum.BINARY, "Indicates the card's preference for the kernel on which the contactless application can be processed", "");
	public static final ITag KERNEL_IDENTIFIER = new TagImpl("9f2a", TagValueTypeEnum.BINARY, "The value to be appended to the ADF Name in the data field of the SELECT command, if the Extended Selection Support flag is present and set to 1", "");
	// MasterCard Tags

	public static final ITag MASTERCARD_UPPER_OFFLINE_AMOUNT = new TagImpl("9f52", TagValueTypeEnum.BINARY, "Upper Cumulative Domestic Offline Transaction Amount", "Issuer specified data element indicating the required maximum cumulative offline amount allowed for the application before the transaction goes online.");

	// //EMV-CAP tags (see also VISA Tags)
	// //9f55 01 c0
	// //9f55 01 00
	// public static final Tag TAG_9f55 = new TagImpl("9f55", TagValueTypeEnum.BINARY, "?", "");

	// EMV Cap
	// 9f56 0c 0f00007fffffe00000000000
	// 9f56 1d 00007fffffe00000000000000000000000000000000000000000000000
	// 9f56 0b 00 00 7f ff ff 00 00 00 00 00 00
	public static final ITag TAG_9f56 = new TagImpl("9f56", TagValueTypeEnum.BINARY, "?", "");

	// Example: BER-TLV[9f6c, 02 (raw 02), 0001]
	public static final ITag MAG_STRIPE_APP_VERSION_NUMBER_CARD = new TagImpl("9f6c", TagValueTypeEnum.BINARY, "Mag Stripe Application Version Number (Card)", "Must be personalized with the value 0x0001");

	// Transaction log data
	// df3e 01 01
	public static final ITag TAG_df3e = new TagImpl("df3e", TagValueTypeEnum.BINARY, "?", "");

	// TODO are these issuer specific?
	// Card from portugal (TODO find issuer code)
	// a000000004 Unhandled tags:
	//
	// df48 02 0620
	// df40 01 00
	// df27 08 0103000000000000
	// df28 10 ffffffffffffffffffffffffffffffff
	// df47 01 01
	// df49 0d 48000001011900000000000000
	// df44 28 00000000000000000000202020202020202020202020202020202020202020202020202020202020
	// df45 22 00000000202020202020202020202020202020202020202020202020202020202020
	// df46 03 000000
	//
	//
	// 501649ff20 Unhandled tags:
	//
	// df48 02 0620
	// df40 01 00
	// df27 08 0100000000000000
	// df28 10 ffffffffffffffffffffffffffffffff
	// df47 01 01
	// df49 0d 00000001010000000000000000

	// TODO refactor to Kernel 2 Tags
	// These are specified in EMV Contactless (Book C-2) "MasterCard"
	public static final ITag OFFLINE_ACCUMULATOR_BALANCE = new TagImpl("9f50", TagValueTypeEnum.BINARY, "Offline Accumulator Balance", "Represents the amount of offline spending available in the Card.");
	// 9f51 03 9f 37 04
	public static final ITag DRDOL = new TagImpl("9f51", TagValueTypeEnum.BINARY, "DRDOL", "A data object in the Card that provides the Kernel with a list of data objects that must be passed to the Card in the data field of the RECOVER AC command");
	public static final ITag TRANSACTION_CATEGORY_CODE = new TagImpl("9f53", TagValueTypeEnum.BINARY, "Transaction Category Code", "");
	public static final ITag DS_ODS_CARD = new TagImpl("9f54", TagValueTypeEnum.BINARY, "DS ODS Card", "");
	public static final ITag MOBILE_SUPPORT_INDICATOR = new TagImpl("9f55", TagValueTypeEnum.BINARY, "Mobile Support Indicator", "");
	public static final ITag DSDOL = new TagImpl("9f5b", TagValueTypeEnum.BINARY, "DSDOL", "");
	public static final ITag DS_REQUESTED_OPERATOR_ID = new TagImpl("9f5c", TagValueTypeEnum.BINARY, "DS Requested Operator ID", "");
	// 9f5d 01 01
	public static final ITag APPLICATION_CAPABILITIES_INFORMATION = new TagImpl("9f5d", TagValueTypeEnum.BINARY, "Application Capabilities Information", "Lists a number of card features beyond regular payment");
	public static final ITag DS_ID = new TagImpl("9f5e", TagValueTypeEnum.BINARY, "Data Storage Identifier", "Constructed as follows: Application PAN (without any 'F' padding) || Application PAN Sequence Number (+ zero padding)");
	public static final ITag DS_SLOT_AVAILABILITY = new TagImpl("9f5f", TagValueTypeEnum.BINARY, "DS Slot Availability", "");
	public static final ITag CVC3_TRACK1 = new TagImpl("9f60", TagValueTypeEnum.BINARY, "CVC3 (Track1)", "The CVC3 (Track1) is a 2-byte cryptogram returned by the Card in the response to the COMPUTE CRYPTOGRAPHIC CHECKSUM command.");
	public static final ITag CVC3_TRACK2 = new TagImpl("9f61", TagValueTypeEnum.BINARY, "CVC3 (Track2)", "The CVC3 (Track2) is a 2-byte cryptogram returned by the Card in the response to the COMPUTE CRYPTOGRAPHIC CHECKSUM command.");
	// 9f62 06 00 00 00 00 00 0e
	// 9f62 06 00 00 00 03 80 00
	public static final ITag PCVC3_TRACK1 = new TagImpl("9f62", TagValueTypeEnum.BINARY, "Track 1 bit map for CVC3", "PCVC3(Track1) indicates to the Kernel the positions in the discretionary data field of the Track 1 Data where the CVC3 (Track1) digits must be copied");
	// 9f63 06 00 00 00 00 07 f0
	// 9f63 06 00 00 00 00 07 8e
	public static final ITag PUNTAC_TRACK1 = new TagImpl("9f63", TagValueTypeEnum.BINARY, "Track 1 bit map for UN and ATC", "PUNATC(Track1) indicates to the Kernel the positions in the discretionary data field of Track 1 Data where the Unpredictable Number (Numeric) digits and Application Transaction Counter digits have to be copied.");
	// 9f64 01 03
	// 9f64 01 04
	public static final ITag NATC_TRACK1 = new TagImpl("9f64", TagValueTypeEnum.BINARY, "Track 1 number of ATC digits", "The value of NATC(Track1) represents the number of digits of the Application Transaction Counter to be included in the discretionary data field of Track 1 Data");
	// 9f65 02 000e
	// 9f65 02 0070
	public static final ITag PCVC_TRACK2 = new TagImpl("9f65", TagValueTypeEnum.BINARY, "Track 2 bit map for CVC3", "PCVC3(Track2) indicates to the Kernel the positions in the discretionary data field of the Track 2 Data where the CVC3 (Track2) digits must be copied");
	// 9f66 02 07f0
	// 9f66 02 1e0e
	public static final ITag NATC_TRACK2 = new TagImpl("9f67", TagValueTypeEnum.BINARY, "Track 2 number of ATC digits", "The value of NATC(Track2) represents the number of digits of the Application Transaction Counter to be included in the discretionary data field of Track 2 Data");
	public static final ITag UDOL = new TagImpl("9f69", TagValueTypeEnum.BINARY, "UDOL", "");
	public static final ITag UNPREDICTABLE_NUMBER_NUMERIC = new TagImpl("9f6a", TagValueTypeEnum.BINARY, "Unpredictable Number (Numeric)", "");
	public static final ITag MAG_STRIPE_APP_VERSION_NUMBER_READER = new TagImpl("9f6d", TagValueTypeEnum.BINARY, "Mag-stripe Application Version Number (Reader)", "");
	public static final ITag DS_SLOT_MANAGEMENT_CONTROL = new TagImpl("9f6f", TagValueTypeEnum.BINARY, "DS Slot Management Control", "");
	public static final ITag PROTECTED_DATA_ENVELOPE_1 = new TagImpl("9f70", TagValueTypeEnum.BINARY, "Protected Data Envelope 1", "");
	public static final ITag PROTECTED_DATA_ENVELOPE_2 = new TagImpl("9f71", TagValueTypeEnum.BINARY, "Protected Data Envelope 2", "");
	public static final ITag PROTECTED_DATA_ENVELOPE_3 = new TagImpl("9f72", TagValueTypeEnum.BINARY, "Protected Data Envelope 3", "");
	public static final ITag PROTECTED_DATA_ENVELOPE_4 = new TagImpl("9f73", TagValueTypeEnum.BINARY, "Protected Data Envelope 4", "");
	public static final ITag PROTECTED_DATA_ENVELOPE_5 = new TagImpl("9f74", TagValueTypeEnum.BINARY, "Protected Data Envelope 5", "");
	public static final ITag UNPROTECTED_DATA_ENVELOPE_1 = new TagImpl("9f75", TagValueTypeEnum.BINARY, "Unprotected Data Envelope 1", "");
	public static final ITag UNPROTECTED_DATA_ENVELOPE_2 = new TagImpl("9f76", TagValueTypeEnum.BINARY, "Unprotected Data Envelope 2", "");
	public static final ITag UNPROTECTED_DATA_ENVELOPE_3 = new TagImpl("9f77", TagValueTypeEnum.BINARY, "Unprotected Data Envelope 3", "");
	public static final ITag UNPROTECTED_DATA_ENVELOPE_4 = new TagImpl("9f78", TagValueTypeEnum.BINARY, "Unprotected Data Envelope 4", "");
	public static final ITag UNPROTECTED_DATA_ENVELOPE_5 = new TagImpl("9f79", TagValueTypeEnum.BINARY, "Unprotected Data Envelope 5", "");
	public static final ITag MERCHANT_CUSTOM_DATA = new TagImpl("9f7c", TagValueTypeEnum.BINARY, "Merchant Custom Data", "");
	public static final ITag DS_SUMMARY_1 = new TagImpl("9f7d", TagValueTypeEnum.BINARY, "DS Summary 1", "");
	public static final ITag DS_UNPREDICTABLE_NUMBER = new TagImpl("9f7f", TagValueTypeEnum.BINARY, "DS Unpredictable Number", "");

	public static final ITag POS_CARDHOLDER_INTERACTION_INFORMATION = new TagImpl("df4b", TagValueTypeEnum.BINARY, "POS Cardholder Interaction Information", "");
	public static final ITag DS_DIGEST_H = new TagImpl("df61", TagValueTypeEnum.BINARY, "DS Digest H", "");
	public static final ITag DS_ODS_INFO = new TagImpl("df62", TagValueTypeEnum.BINARY, "DS ODS Info", "");
	public static final ITag DS_ODS_TERM = new TagImpl("df63", TagValueTypeEnum.BINARY, "DS ODS Term", "");
	public static final ITag BALANCE_READ_BEFORE_GEN_AC = new TagImpl("df8104", TagValueTypeEnum.BINARY, "Balance Read Before Gen AC", "");
	public static final ITag BALANCE_READ_AFTER_GEN_AC = new TagImpl("df8105", TagValueTypeEnum.BINARY, "Balance Read After Gen AC", "");
	public static final ITag DATA_NEEDED = new TagImpl("df8106", TagValueTypeEnum.BINARY, "Data Needed", "");
	public static final ITag CDOL1_RELATED_DATA = new TagImpl("df8107", TagValueTypeEnum.BINARY, "CDOL1 Related Data", "");
	public static final ITag DS_AC_TYPE = new TagImpl("df8108", TagValueTypeEnum.BINARY, "DS AC Type", "");
	public static final ITag DS_INPUT_TERM = new TagImpl("df8109", TagValueTypeEnum.BINARY, "DS Input (Term)", "");
	public static final ITag DS_ODS_INFO_FOR_READER = new TagImpl("df810a", TagValueTypeEnum.BINARY, "DS ODS Info For Reader", "");
	public static final ITag DS_SUMMARY_STATUS = new TagImpl("df810b", TagValueTypeEnum.BINARY, "DS Summary Status", "");
	public static final ITag KERNEL_ID = new TagImpl("df810c", TagValueTypeEnum.BINARY, "Kernel ID", "");
	public static final ITag DSVN_TERM = new TagImpl("df810d", TagValueTypeEnum.BINARY, "DSVN Term", "");
	public static final ITag POST_GEN_AC_PUT_DATA_STATUS = new TagImpl("df810e", TagValueTypeEnum.BINARY, "Post-Gen AC Put Data Status", "");
	public static final ITag PRE_GEN_AC_PUT_DATA_STATUS = new TagImpl("df810f", TagValueTypeEnum.BINARY, "Pre-Gen AC Put Data Status", "");
	public static final ITag PROCEED_TO_WRITE_FIRST_FLAG = new TagImpl("df8110", TagValueTypeEnum.BINARY, "Proceed To First Write Flag", "");
	public static final ITag PDOL_RELATED_DATA = new TagImpl("df8111", TagValueTypeEnum.BINARY, "PDOL Related Data", "");
	public static final ITag TAGS_TO_READ = new TagImpl("df8112", TagValueTypeEnum.BINARY, "Tags To Read", "");
	public static final ITag DRDOL_RELATED_DATA = new TagImpl("df8113", TagValueTypeEnum.BINARY, "DRDOL Related Data", "");
	public static final ITag REFERENCE_CONTROL_PARAMETER = new TagImpl("df8114", TagValueTypeEnum.BINARY, "Reference Control Parameter", "");
	public static final ITag ERROR_INDICATION = new TagImpl("df8115", TagValueTypeEnum.BINARY, "Error Indication", "");
	public static final ITag USER_INTERFACE_REQUEST_DATA = new TagImpl("df8116", TagValueTypeEnum.BINARY, "User Interface Request Data", "");
	public static final ITag CARD_DATA_INPUT_CAPABILITY = new TagImpl("df8117", TagValueTypeEnum.BINARY, "Card Data Input Capability", "");
	public static final ITag CMV_CAPABILITY_CMV_REQUIRED = new TagImpl("df8118", TagValueTypeEnum.BINARY, "CVM Capability - CVM Required", "");
	public static final ITag CMV_CAPABILITY_NO_CMV_REQUIRED = new TagImpl("df8119", TagValueTypeEnum.BINARY, "CVM Capability - No CVM Required", "");
	public static final ITag DEFAULT_UDOL = new TagImpl("df811a", TagValueTypeEnum.BINARY, "Default UDOL", "");
	public static final ITag KERNEL_CONFIGURATION = new TagImpl("df811b", TagValueTypeEnum.BINARY, "Kernel Configuration", "");
	public static final ITag MAX_LIFETIME_TORN_TRANSACTION_LOG_REC = new TagImpl("df811c", TagValueTypeEnum.BINARY, "Max Lifetime of Torn Transaction Log Record", "");
	public static final ITag MAX_NUMBER_TORN_TRANSACTION_LOG_REC = new TagImpl("df811d", TagValueTypeEnum.BINARY, "Max Number of Torn Transaction Log Records", "");
	public static final ITag MAG_STRIPE_CMV_CAPABILITY_CMV_REQUIRED = new TagImpl("df811e", TagValueTypeEnum.BINARY, "Mag-stripe CVM Capability – CVM Required", "");
	public static final ITag SECURITY_CAPABILITY = new TagImpl("df811f", TagValueTypeEnum.BINARY, "Security Capability", "");
	public static final ITag TERMINAL_ACTION_CODE_DEFAULT = new TagImpl("df8120", TagValueTypeEnum.BINARY, "Terminal Action Code – Default", "");
	public static final ITag TERMINAL_ACTION_CODE_DENIAL = new TagImpl("df8121", TagValueTypeEnum.BINARY, "Terminal Action Code – Denial", "");
	public static final ITag TERMINAL_ACTION_CODE_ONLINE = new TagImpl("df8122", TagValueTypeEnum.BINARY, "Terminal Action Code – Online", "");
	public static final ITag READER_CONTACTLESS_FLOOR_LIMIT = new TagImpl("df8123", TagValueTypeEnum.BINARY, "Reader Contactless Floor Limit", "");
	public static final ITag READER_CL_TRANSACTION_LIMIT_NO_CMV = new TagImpl("df8124", TagValueTypeEnum.BINARY, "Reader Contactless Transaction Limit (No On-device CVM)", "");
	public static final ITag READER_CL_TRANSACTION_LIMIT_CVM = new TagImpl("df8125", TagValueTypeEnum.BINARY, "Reader Contactless Transaction Limit (On-device CVM)", "");
	public static final ITag READER_CMV_REQUIRED_LIMIT = new TagImpl("df8126", TagValueTypeEnum.BINARY, "Reader CVM Required Limit", "");
	public static final ITag TIME_OUT_VALUE = new TagImpl("df8127", TagValueTypeEnum.BINARY, "TIME_OUT_VALUE", "");
	public static final ITag IDS_STATUS = new TagImpl("df8128", TagValueTypeEnum.BINARY, "IDS Status", "");
	public static final ITag OUTCOME_PARAMETER_SET = new TagImpl("df8129", TagValueTypeEnum.BINARY, "Outcome Parameter Set", "");
	public static final ITag DD_CARD_TRACK1 = new TagImpl("df812a", TagValueTypeEnum.BINARY, "DD Card (Track1)", "");
	public static final ITag DD_CARD_TRACK2 = new TagImpl("df812b", TagValueTypeEnum.BINARY, "DD Card (Track2)", "");
	public static final ITag MAG_STRIPE_CMV_CAPABILITY_NO_CMV_REQ = new TagImpl("df812c", TagValueTypeEnum.BINARY, "Mag-stripe CVM Capability – No CVM Required", "");
	public static final ITag MESSAGE_HOLD_TIME = new TagImpl("df812d", TagValueTypeEnum.BINARY, "Message Hold Time", "");

	public static final ITag TORN_RECORD = new TagImpl("ff8101", TagValueTypeEnum.BINARY, "Torn Record", "");
	public static final ITag TAGS_TO_WRITE_BEFORE_GEN_AC = new TagImpl("ff8102", TagValueTypeEnum.BINARY, "Tags To Write Before Gen AC", "");
	public static final ITag TAGS_TO_WRITE_AFTER_GEN_AC = new TagImpl("ff8103", TagValueTypeEnum.BINARY, "Tags To Write After Gen AC", "");
	public static final ITag DATA_TO_SEND = new TagImpl("ff8104", TagValueTypeEnum.BINARY, "Data To Send", "");
	public static final ITag DATA_RECORD = new TagImpl("ff8105", TagValueTypeEnum.BINARY, "Data Record", "");
	public static final ITag DISCRETIONARY_DATA = new TagImpl("ff8106", TagValueTypeEnum.BINARY, "Discretionary Data", "");

	// @formatter:on

	/**
	 * If the tag is not found, this method returns the "[UNHANDLED TAG]" containing 'tagBytes'
	 * 
	 * @param tagBytes
	 * @return
	 */
	public static ITag getNotNull(final byte[] tagBytes) {
		ITag tag = find(tagBytes);
		if (tag == null) {
			tag = createUnknownTag(tagBytes);
		}
		return tag;
	}

	public static ITag createUnknownTag(final byte[] tagBytes) {
		return new TagImpl(tagBytes, TagValueTypeEnum.BINARY, "[UNKNOWN TAG]", "");
	}

	/**
	 * Returns null if Tag not found
	 */
	public static ITag find(final byte[] tagBytes) {
		return tags.get(ByteArrayWrapper.wrapperAround(tagBytes));
	}

	private static void addTag(final ITag tag) {
		// Use 'wrapper around', since the underlaying byte-array will not be changed in this case
		ByteArrayWrapper baw = ByteArrayWrapper.wrapperAround(tag.getTagBytes());
		if (tags.containsKey(baw)) {
			throw new IllegalArgumentException("Tag already added " + tag);
		}
		tags.put(baw, tag);
	}

	static {
		Field[] fields;

		fields = EmvTags.class.getFields();
		for (Field f : fields) {
			if (f.getType() == ITag.class) {
				try {
					ITag t = (ITag) f.get(null);
					addTag(t);
				} catch (IllegalAccessException ex) {
					throw new RuntimeException(ex);
				}
			}
		}
	}

	/**
	 * Private constructor
	 */
	private EmvTags() {
	}
}