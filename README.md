## EMV NFC Paycard Enrollment ![CI](https://github.com/devnied/EMV-NFC-Paycard-Enrollment/workflows/CI/badge.svg) [![Coverage Status](https://coveralls.io/repos/github/devnied/EMV-NFC-Paycard-Enrollment/badge.svg?branch=master)](https://coveralls.io/github/devnied/EMV-NFC-Paycard-Enrollment?branch=master)
### Description
Java library used to read and extract public data from NFC EMV credit cards.<br/>
<br/>
Android sample app available on Play store.

<a href="https://play.google.com/store/apps/details?id=com.github.devnied.emvnfccard.pro&utm_source=global_co&utm_medium=prtnr&utm_content=Mar2515&utm_campaign=PartBadge&pcampaignid=MKT-AC-global-none-all-co-pr-py-PartBadges-Oct1515-1"><img height="60px" alt="Get it on Google Play" src="https://play.google.com/intl/en_us/badges/images/apps/en-play-badge.png" /></a>

### Getting started

First you need to create a custom Provider to exchange APDU with an NFC EMV credit card ([sample here](https://github.com/devnied/EMV-NFC-Paycard-Enrollment/blob/master/sample-pcsc/src/main/java/com/github/devnied/emvpcsccard/PcscProvider.java)).
```java
public class YourProvider implements IProvider {

  @Override
  public byte[] transceive(final byte[] pCommand) {
	 // implement this
  }

  @Override
  public byte[] getAt() {
     // implement this to get card ATR (Answer To Reset) or ATS (Answer To Select)
  }

}
```

After that, create an instance of a parser and read the card.
```java
// Create provider
IProvider provider = new YourProvider();
// Define config
Config config = EmvTemplate.Config()
		.setContactLess(true) // Enable contact less reading (default: true)
		.setReadAllAids(true) // Read all aids in card (default: true)
		.setReadTransactions(true) // Read all transactions (default: true)
		.setReadCplc(false) // Read and extract CPCLC data (default: false)
		.setRemoveDefaultParsers(false) // Remove default parsers for GeldKarte and EmvCard (default: false)
		.setReadAt(true) // Read and extract ATR/ATS and description
		.setReadAllRecords(true) // Read the whole application file, not only up to the card number (default: true)
		.setReadExtendedData(false) // Read the optional GET DATA objects: offline balance, last online ATC (default: false)
		.setTerminalCountryCode(CountryCodeEnum.FR) // Country the terminal pretends to be located in (default: FR)
		; 
// Create Parser
EmvTemplate parser = EmvTemplate.Builder() //
		.setProvider(provider) // Define provider
		.setConfig(config) // Define config
		//.setTerminal(terminal) (optional) you can define a custom terminal implementation to create APDU
		.build();
		
// Read card
EmvCard card = parser.readEmvCard();
```
card object contains all data read (Aid, card number, expiration date, card type, transactions history)

### Data read from the card

On top of the card number, the expiration date and the transaction history, each
`Application` of the card exposes the data objects defined by the EMV specification:

| Data | Tag | Getter |
|---|---|---|
| Application PAN and sequence number | `5A`, `5F34` | `getPan()`, `getPanSequenceNumber()` |
| Effective and expiration dates | `5F25`, `5F24` | `getEffectiveDate()`, `getExpirationDate()` |
| Issuer country | `5F28`, `5F56` | `getIssuerCountryCode()` |
| Application currency and exponent | `9F42`, `9F44` | `getCurrency()`, `getCurrencyExponent()` |
| Application preferred name | `9F12` | `getApplicationPreferredName()` |
| Issuer code table index | `9F11` | `getIssuerCodeTableIndex()` |
| Language preference | `5F2D` | `getLanguagePreferences()` |
| Service code | `5F30` | `getServiceCode()` |
| Issuer URL | `5F50` | `getIssuerUrl()` |
| Application version number | `9F08` | `getApplicationVersionNumber()` |
| Application Interchange Profile | `82` | `getInterchangeProfile()` |
| Application Usage Control | `9F07` | `getUsageControl()` |
| Cardholder Verification Method list | `8E` | `getCvmList()`, `getCvmAmountX()`, `getCvmAmountY()` |
| Issuer Action Codes | `9F0E`, `9F0F`, `9F0D` | `getIssuerActionCodeDenial()`, `getIssuerActionCodeOnline()`, `getIssuerActionCodeDefault()` |
| Kernel capability data objects | `9F6C`, `9F6E`, `9F5D` | `getCardTransactionQualifiers()`, `getFormFactorIndicator()`, `getApplicationCapabilitiesInformation()` |
| Contactless kernel | `9F2A` | `getKernel()`, `getShortKernelId()`, `isKernelDerived()` |
| Offline data authentication credentials | `8F`, `90`, `92`, `93`, `9F32`, `9F46`, `9F47`, `9F48`, `9F4A`, `9F4B` | `getOfflineDataAuthentication()` |
| Contactless mag-stripe profile (Kernel 2) | `56`, `9F60`, `9F61`, `9F62`, `9F63`, `9F64`, `9F65`, `9F66`, `9F67`, `9F69`, `9F6B` | `getMagStripeData()` |
| Issuer Application Data | `9F10` | `getIssuerApplicationData()` |
| Issuer Identification Number | `42` | `getIssuerIdentificationNumber()` |
| Consecutive offline limits | `9F14`, `9F23` | `getLowerConsecutiveOfflineLimit()`, `getUpperConsecutiveOfflineLimit()` |
| Application reference currencies | `9F3B`, `9F43` | `getReferenceCurrencies()`, `getReferenceCurrencyExponents()` |
| Application discretionary data | `9F05` | `getApplicationDiscretionaryData()` |
| Data object lists the card carries | `8C`, `8D`, `9F49`, `97` | `getCdol1()`, `getCdol2()`, `getDdol()`, `getTdol()` |
| ATC and last online ATC | `9F36`, `9F13` | `getTransactionCounter()`, `getLastOnlineTransactionCounter()` |
| Offline (electronic cash) balance | `9F79`, `9F50` | `getOfflineBalance()` (a `BigDecimal`, the amount carries up to 12 digits) |
| Tokenized credential (mobile wallet) | `9F19`, `9F24`, `9F25` | `isTokenized()`, `getPaymentAccountReference()`, `getLast4DigitsOfPan()` |

The `Application Interchange Profile`, the `Application Usage Control` and the
`CVM list` are decoded into objects, so you can directly ask whether the card
supports CDA, whether it may be used abroad or which cardholder verification
methods it accepts:

```java
Application app = card.getApplications().get(0);

app.getInterchangeProfile().isCdaSupported();
app.getUsageControl().isInternationalUsageAllowed();
for (CVM cvm : app.getCvmList()) {
	System.out.println(cvm.getMethod() + " - " + cvm.getCondition());
}
```

The Issuer Action Codes and the kernel capability data objects are exposed raw:
the first ones are bitmaps built on the Terminal Verification Results layout
(EMV 4.3 Book 3 Annex C5), and the meaning of the tags `9F6C` and `9F6E`
depends on the payment system (Card Transaction Qualifiers and Form Factor
Indicator for Visa, Mag Stripe Application Version Number and Third Party Data
for MasterCard), so the library does not guess which one a card meant.

### Contactless mag-stripe profile

A card personalised for the Mag-stripe Mode of EMV Contactless Book C-2 v2.11
(Kernel 2, the MasterCard applications) carries the bitmaps that tell a reader
where to write the dynamic card verification code, the unpredictable number and
the application transaction counter inside the discretionary data field of its
tracks. `getMagStripeData()` decodes them into positions:

```java
MagStripeData magStripe = app.getMagStripeData();

magStripe.isMandatoryDataPresent();                        // Table 6.4
magStripe.isConsistent();                                  // S7.22
magStripe.getUnpredictableNumberDigitCount();              // nUN
magStripe.getCvc3Track2Positions();                        // PCVC3(Track2)
magStripe.getApplicationTransactionCounterTrack2Positions();
```

Two of these tags were assigned twice, and only the kernel tells the readings
apart: `9F66` is PUNATC(Track2) for Kernel 2 and the reader sourced Terminal
Transaction Qualifiers for Kernel 3, `9F69` is the UDOL for Kernel 2 and the
Card Authentication Related Data for Kernel 3. EMV Contactless Book B fixes the
kernel before the first GET PROCESSING OPTIONS, from the Kernel Identifier of
the payment directory or, when the card expressed no preference, from the AID,
so the profile is only read for an application whose kernel is the MasterCard
one. An application whose kernel is neither announced nor implied by its AID —
a domestic scheme, typically — is left alone rather than read against a
dictionary that may not be its own.

The two CVC3 (tags `9F60` and `9F61`) are only returned by the COMPUTE
CRYPTOGRAPHIC CHECKSUM command, which this library never sends: they are picked
up only when a response read for another reason already carried them.

Mag-stripe Mode is gone from Book C-2 v2.12 (May 2026), which implements
Specification Bulletin No. 317 "Mag-stripe Mode Implementation Option
Deprecated". Cards personalised for it are still in the field, so the library
keeps reading them and everything above is sourced to v2.11.

### What this library will never do

It reads the public data of a card. It never asks the card to authenticate
anything, and never makes it change state. One nuance: the GET PROCESSING
OPTIONS command that starts any read does make a contactless card do
cryptographic work of its own accord — several recorded traces answer it with
an Application Cryptogram (tag `9F26`) nobody asked for — but the library never
requests one, never sends a GENERATE AC, and discards what it does not need.
That boundary is deliberate, and it rules out a large part of the EMV
specifications:

**Commands that are never sent.** GENERATE AC (it increments the Application
Transaction Counter and consumes the offline counters), INTERNAL AUTHENTICATE,
VERIFY (it decrements the PIN Try Counter and can block an application for
good — the remaining tries are read with GET DATA instead), GET CHALLENGE, PIN
CHANGE/UNBLOCK, EXTERNAL AUTHENTICATE, APPLICATION BLOCK and CARD BLOCK,
EXCHANGE RELAY RESISTANCE DATA, RECOVER AC, COMPUTE CRYPTOGRAPHIC CHECKSUM, PUT
DATA, and the issuer scripts. The data objects that only exist to feed those
commands — the CDOLs, the DDOL, the mag stripe bitmaps — are read and reported,
never executed.

**Terminal data that is never invented.** The Terminal Verification Results
(`95`), the Transaction Status Information (`9B`) and the CVM Results (`9F34`)
are produced by a terminal during a transaction; the card never holds them. The
all-zero `95` this library answers when a card asks for one in its PDOL is the
honest value, not a placeholder.

**Decisions that are never taken.** Terminal action analysis, terminal risk
management (floor limits, random selection, velocity checking), cardholder
verification method selection, and the combination selection of the contactless
Entry Point. They all end on a verdict about a transaction that is not
happening. The library reports the card's own parameters — the Issuer Action
Codes, the consecutive offline limits, the CVM list and its amounts — and stops
there.

**Offline data authentication is reported, not verified.** The certificates,
the certification authority key index and the key lengths are exposed through
`getOfflineDataAuthentication()`. Verifying a signature needs the certification
authority public keys, which the payment systems do not publish, and the
dynamic schemes additionally need the card to sign a challenge.

**One hard ceiling.** A card that protects its data behind the Kernel 8 secure
channel cannot be read at all: establishing the channel requires the card to
perform a key agreement. That is a limit of any read-only tool, not a missing
feature.

### Application selection

The library follows the application selection rules of EMV 4.3 Book 1 (sections
12.2 to 12.4) and of EMV Contactless Book B:

- the **Application Priority Indicator** (tag `87`) is decoded into the priority
  order (bits 4 to 1) and the cardholder confirmation flag (bit 8);
  an application without priority is proposed last;
- a payment directory record that carries a **DDF Name** (tag `9D`) references a
  sub directory whose records are read too, so the applications it holds are not
  missed;
- the **Extended Selection** (tag `9F29`) is appended to the ADF Name in the data
  field of the SELECT command when the card asks for it, with a fallback on the
  bare ADF Name if the card refuses it (`setExtendedSelectionSupported(false)`
  disables it entirely);
- the **Kernel Identifier** (tag `9F2A`) is kept on the `Application`;
- when the applications are looked up with a partial AID (a RID), the SELECT is
  repeated with the *next occurrence* option (P2 = `02`) so every application
  built on that RID is seen;
- an application answering `6283` (invalidated) or `6A81` (function not
  supported) to the SELECT is **blocked**: it is skipped and flagged through
  `Application.isBlocked()`. Book 1 asks the terminal to terminate the session
  on `6A81`; this library keeps reading the remaining applications instead,
  since it only extracts public data.

`9F79` is only read on the applications that define it as the Electronic Cash
Balance (UnionPay/PBOC); on any other card that tag is the generic *Unprotected
Data Envelope 5* and reading it as money would report an amount the card never
meant.

Values requested through a DOL are padded following EMV 4.3 Book 3 section 5.4:
a numeric value is right justified and padded with zeroes on the left, any other
format is left justified and padded on the right.

### Country specificities

Some cards only answer, or only answer completely, to a terminal that looks
domestic. `setTerminalCountryCode` sets both the terminal country (tag `9F1A`)
and the transaction currency (tag `5F2A`) the library announces, which is what
Interac (Canada), girocard (Germany) or UnionPay (China) applications look at:

```java
Config config = EmvTemplate.Config().setTerminalCountryCode(CountryCodeEnum.CA);
```

Text data objects are decoded with the character set the issuer asks for through
the Issuer Code Table Index (tag `9F11`), so a card issued in Turkey
(ISO/IEC 8859-9), in Russia (ISO/IEC 8859-5) or in Greece (ISO/IEC 8859-7)
returns a readable application name instead of mojibake.

`EmvCardScheme` also knows the AID of the domestic schemes and the country they
are operated in:

```java
EmvCardScheme.getCardTypeByAid("D27600002547410100"); // GIROCARD
EmvCardScheme.GIROCARD.getCountry();                  // DE
EmvCardScheme.GIROCARD.isDomestic();                  // true
```

Supported domestic schemes: CB (France), girocard and GeldKarte (Germany),
Interac and The Exchange Network (Canada), Dankort/PBS (Denmark),
PagoBANCOMAT (Italy), Bankaxept (Norway), Chipknip (Netherlands), LINK and
Midland (United Kingdom), UnionPay (China), JCB (Japan), RuPay (India),
Mir and ПРО100 (Russia), Простiр (Ukraine), Troy (Turkey), Elo, Banrisul and
Bradesco (Brazil), Verve, InterSwitch and eTranzact (Nigeria), SPAN
(Saudi Arabia), Meeza (Egypt).

#### Android usage
For android, you can create a provider with [IsoDep](https://developer.android.com/reference/android/nfc/tech/IsoDep) class:
```java
public class Provider implements IProvider {

	private IsoDep mTagCom;

	@Override
	public byte[] transceive(final byte[] pCommand) throws CommunicationException {
		
		byte[] response;
		try {
			// send command to emv card
			response = mTagCom.transceive(pCommand);
		} catch (IOException e) {
			throw new CommunicationException(e.getMessage());
		}

		return response;
	}

	@Override
	public byte[] getAt() {
        // For NFC-A
		return mTagCom.getHistoricalBytes();
		// For NFC-B
        // return mTagCom.getHiLayerResponse();
	}


	public void setmTagCom(final IsoDep mTagCom) {
		this.mTagCom = mTagCom;
	}

}
```

### Screens

[![Sample demo](https://raw.githubusercontent.com/devnied/EMV-NFC-Paycard-Enrollment/master/images/demo.gif)](https://raw.githubusercontent.com/devnied/EMV-NFC-Paycard-Enrollment/master/images/demo.gif)

## Download

### Maven
```xml
<dependency>
  <groupId>com.github.devnied.emvnfccard</groupId>
  <artifactId>library</artifactId>
  <version>3.2.0</version>
</dependency>
```

### Gradle
```groovy
dependencies {
	compile 'com.github.devnied.emvnfccard:library:3.2.0'
}
```

### JAR
You can download this library on [Maven central](http://search.maven.org/#search%7Cga%7C1%7Cemvnfccard) or in Github [release tab](https://github.com/devnied/EMV-NFC-Paycard-Enrollment/releases)

## Dependencies

If you are not using Maven or some other dependency management tool that can understand Maven repositories, the list below is what you need to run EMV-NFC-Paycard-Enrollment.

## Build
**To build the project launch:**
```xml
mvn clean install
```
## Bugs

Please report bugs and feature requests to the GitHub issue tracker.<br/>
Forks and Pull Requests are also welcome.

## Author

**Millau Julien**

+ [http://twitter.com/devnied](http://twitter.com/devnied)
+ [http://github.com/devnied](http://github.com/devnied)


## Copyright and license

Copyright 2020-2026 Millau Julien.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this work except in compliance with the License.
You may obtain a copy of the License in the LICENSE file, or at:

  [http://www.apache.org/licenses/LICENSE-2.0](http://www.apache.org/licenses/LICENSE-2.0)

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
