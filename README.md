# Overview

Stax2 API is an extension to standard [Stax](http://en.wikipedia.org/wiki/StAX) API ("STandard Api for Xml procesing"),
pull-parser API of JDK, added in JDK 6.

## Status

[![Build Status](https://travis-ci.org/FasterXML/stax2-api.svg)](https://travis-ci.org/FasterXML/stax2-api)


## Support

Stax2 API is implemented natively by following Stax XML implementations:

 * [Aalto](../../../aalto-xml) (performance-oriented, also non-blocking/async)
 * [Woodstox](../../../woodstox) (full-featured, most compliant XML package on Java platform)

In addition, Stax2 API contains wrapper implementations that can be used to support most of API on
regular Stax implementations like Sun SJSXP (default Stax implementation bundled with JDK 1.6 and 1.7).

## License

API is licensed under standard BSD license, which is compatible with all Free and Open Software (OSS) licenses.

## Java package

API classes are in package `org.codehaus.stax2`.

Stax2 API also contains partial Reference Implementation of many of API classes, under
`org.codehaus.stax2.ri`: intention is to make it as painless as possible for Java Stax parser
to implement full Stax2 support.

## Usage

Maven dependency is defined as:

    <dependency>
      <groupId>org.codehaus.woodstox</groupId>
      <artifactId>stax2-api</artifactId>
      <version>3.1.4</version>
    </dependency>

Jars can be found from [Central Maven repo](http://repo1.maven.org/maven2/org/codehaus/woodstox/stax2-api/).
