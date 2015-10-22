## EMV NFC Paycard Enrollment [![Build Status](https://travis-ci.org/devnied/EMV-NFC-Paycard-Enrollment.png)](https://travis-ci.org/devnied/EMV-NFC-Paycard-Enrollment) [![Coverage Status](https://coveralls.io/repos/devnied/EMV-NFC-Paycard-Enrollment/badge.svg?branch=master)](https://coveralls.io/r/devnied/EMV-NFC-Paycard-Enrollment?branch=master)
### Description
Java library used to read and extract public data from NFC EMV credit cards.<br/>
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.devnied.emvnfccard/library/badge.svg?style=flat)](https://maven-badges.herokuapp.com/maven-central/com.github.devnied.emvnfccard/library)<br/>
<br/>
Android sample app available on Play store.

<a href="https://play.google.com/store/apps/details?id=com.github.devnied.emvnfccard">
  <img alt="EMV NFC paycard reader Demo on Google Play"
         src="http://developer.android.com/images/brand/en_generic_rgb_wo_60.png" />
</a>

### Getting started

First you need to create a custom Provider to exchange APDU with an NFC EMV credit card.
```java
public class YourProvider implements IProvider {

  @Override
  public byte[] transceive(final byte[] pCommand) {
	 // implement this
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
EmvCard emvCard = parser.readEmvCard();
```
card object contains all data read (Aid, card number, expiration date, card type, transactions history)

### Screens

[![Sample demo](https://raw.githubusercontent.com/devnied/EMV-NFC-Paycard-Enrollment/master/images/demo.gif)](https://raw.githubusercontent.com/devnied/EMV-NFC-Paycard-Enrollment/master/images/demo.gif)

## Download

### Maven
```xml
<dependency>
  <groupId>com.github.devnied.emvnfccard</groupId>
  <artifactId>library</artifactId>
  <version>2.1.1</version>
</dependency>
```

### Gradle
```groovy
dependencies {
	compile 'com.github.devnied.emvnfccard:library:2.1.1'
}
```

### JAR
You can download this library on [Maven central](http://search.maven.org/#search%7Cga%7C1%7Cemvnfccard) or in Github [release tab](https://github.com/devnied/EMV-NFC-Paycard-Enrollment/releases)

## Dependencies

If you are not using Maven or some other dependency management tool that can understand Maven repositories, the list below is what you need to run EMV-NFC-Paycard-Enrollment.

**Runtime Dependencies**
* commons-lang3 3.1
* bit-lib4j 1.4.10
* commons-io 2.4
* commons-collections4 4.0

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

Copyright 2014 Millau Julien.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this work except in compliance with the License.
You may obtain a copy of the License in the LICENSE file, or at:

  [http://www.apache.org/licenses/LICENSE-2.0](http://www.apache.org/licenses/LICENSE-2.0)

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

[![Analytics](https://ga-beacon.appspot.com/UA-19411627-6/MV-NFC-Paycard-Enrollment)](https://github.com/igrigorik/ga-beacon)
