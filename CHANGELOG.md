# Changelog

All notable changes to this project are documented in this file.
The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/)
and this project adheres to [Semantic Versioning](https://semver.org/).

## [3.2.0] - 2026-08-31

### Added
- `Application` now exposes the EMV data objects a card carries: dates, currencies, issuer data, DOLs, issuer action codes and consecutive offline limits.
- `ApplicationInterchangeProfile` (`82`), `ApplicationUsageControl` (`9F07`) and the `CVM` list (`8E`) are decoded into objects instead of raw bytes.
- `OfflineDataAuthentication`: certificates, certification authority key index and key lengths are reported (never verified).
- `MagStripeData`: contactless mag-stripe profile of EMV Contactless Book C-2 v2.11, read only for a Kernel 2 application.
- `CardTransactionQualifiers`: tag `9F6C` decoded for the Visa applications (Kernel 3).
- `DataEnvelopes`: the Standalone Data Storage envelopes of Kernel 2.
- `TransactionLog`: log format, entry count and read status of each application.
- `GeldKarteData`: purse files of the German GeldKarte application.
- `PaymentDirectory` and `DirectoryEntry`: the PPSE/PSE records, DDF sub-directories included.
- `CardIdentification`: what the card discloses about its network and its issuer, with the source and the confidence of the reading.
- `RecordData`: the READ RECORD exchanges kept as sent and received.
- `KernelEnum` and `KernelTypeEnum`: contactless kernel resolved from the Kernel Identifier (`9F2A`) or from the AID.
- Application selection follows EMV 4.3 Book 1 sections 12.2 to 12.4: Application Priority Indicator (`87`), Extended Selection (`9F29`) and next occurrence SELECT on a partial AID.
- An application answering `6283` or `6A81` to the SELECT is skipped and flagged through `Application.isBlocked()`.
- Offline (electronic cash) balance read on the cards that define `9F79`/`9F50` as such.
- Tokenized credentials of a mobile wallet: `isTokenized()`, `getPaymentAccountReference()` and `getLast4DigitsOfPan()`.
- Text data objects decoded with the character set asked for by the Issuer Code Table Index (`9F11`).
- Four configuration options: `setReadAllRecords`, `setReadExtendedData`, `setExtendedSelectionSupported` and `setTerminalCountryCode`.
- Five domestic schemes in `EmvCardScheme` (girocard, Elo, Troy, Chipknip, Diners Club), plus `getCountry()` and `isDomestic()`.
- Eight tags in `EmvTags`, and `TagValueDecoder` to render the raw value of a data object.
- `BerTlvInputStream`, the BER-TLV reader EMV needs, written against the specification.

### Changed
- `smartcard_list.txt` refreshed, which widens the ATR/ATS descriptions returned by `AtrUtils`.
- bit-lib4j updated from 1.6.0 to 1.7.0.

### Fixed
- GET RESPONSE was built with the instruction byte `0C` instead of `C0`, so the data announced by a `61xx` was never read.
- A date or an amount a card encodes out of specification is logged and left empty instead of aborting the read.
- A bare URL in the ATR list is no longer taken for a card description.

### Removed
- The `net.sf.scuba:scuba-smartcards` dependency, replaced by `BerTlvInputStream`.

## [3.1.0] - 2026-02-09

- Releases up to 3.1.0 predate this file; see the
  [release tab](https://github.com/devnied/EMV-NFC-Paycard-Enrollment/releases)
  and the commit history.
