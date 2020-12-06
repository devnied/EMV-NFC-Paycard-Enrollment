## EMV NFC Paycard Enrollment ![CI](https://github.com/devnied/EMV-NFC-Paycard-Enrollment/workflows/CI/badge.svg) [![Coverage Status](https://coveralls.io/repos/github/devnied/EMV-NFC-Paycard-Enrollment/badge.svg?branch=master)](https://coveralls.io/github/devnied/EMV-NFC-Paycard-Enrollment?branch=master) [![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.devnied.emvnfccard/library/badge.svg?style=flat)](https://maven-badges.herokuapp.com/maven-central/com.github.devnied.emvnfccard/library) [![License](http://img.shields.io/:license-apache-blue.svg)](http://www.apache.org/licenses/LICENSE-2.0.html)
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
		; 
// Create Parser
EmvTemplate parser = EmvTemplate.Builder() //
		.setProvider(provider) // Define provider
		.setConfig(config) // Define config
		//.setTerminal(terminal) (optional) you can define a custom terminal implementation to create APDU
		.build();
		
// Read card
EMVCard card = parser.readEmvCard();
```
card object contains all data read (Aid, card number, expiration date, card type, transactions history)

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
  <version>3.0.1</version>
</dependency>
```

### Gradle
```groovy
dependencies {
	compile 'com.github.devnied.emvnfccard:library:3.0.1'
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

Copyright 2020 Millau Julien.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this work except in compliance with the License.
You may obtain a copy of the License in the LICENSE file, or at:

  [http://www.apache.org/licenses/LICENSE-2.0](http://www.apache.org/licenses/LICENSE-2.0)

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
