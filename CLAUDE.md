# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Stax2 API is an extension to the standard Java StAX (JSR-173) XML processing API. It defines interfaces and abstract classes that XML parsers (Woodstox, Aalto) implement. This is a **pure API library** with no external dependencies — it only depends on JDK `javax.xml` classes.

## Build Commands

```bash
# Build and verify
./mvnw clean verify

# Install to local Maven repository
./mvnw clean install

# Build with Java 9+ module-info support
./mvnw -P moditect clean verify

# CI build command
./mvnw -B -q -ff -ntp verify
```

## Testing

Unit tests use **JUnit 5** (`junit-jupiter`, test scope; version `${version.junit5}` from `oss-parent`) and live under `src/test/java`. Test classes go in the same package as the code under test so they can reach package-private classes (e.g. `org.codehaus.stax2.ri.typed.ValueEncoderFactoryTest`). Most behavior is still exercised mainly by consumer libraries (Woodstox, Aalto); add tests here for `ri` implementation fixes.

```bash
./mvnw test
./mvnw test -Dtest=ClassName
./mvnw test -Dtest=ClassName#methodName
```

## Architecture

- **`org.codehaus.stax2`** — Core API interfaces extending StAX 1.0 (`XMLStreamReader2`, `XMLStreamWriter2`, `XMLInputFactory2`, `XMLOutputFactory2`)
- **`org.codehaus.stax2.typed`** — Typed Access API for efficient XML-to-Java type conversion without manual string parsing (`TypedXMLStreamReader`, `TypedXMLStreamWriter`, `Base64Variants`)
- **`org.codehaus.stax2.validation`** — Bi-directional validation framework supporting both read and write-side validation (`XMLValidator`, `XMLValidationSchema`)
- **`org.codehaus.stax2.evt`** — Extended event model (`XMLEvent2`, `XMLEventFactory2`)
- **`org.codehaus.stax2.io`** — I/O abstractions (`Stax2Source`/`Stax2Result` variants for files, byte arrays, strings, URLs)
- **`org.codehaus.stax2.osgi`** — OSGi service provider interfaces
- **`org.codehaus.stax2.ri`** — Reference implementations and adapters that parser libraries can extend (includes `ri.dom`, `ri.evt`, `ri.typed` subpackages)

## Build Details

- **Java baseline:** Java 8 source/target (since 4.3)
- **Packaging:** OSGi bundle (via `maven-bundle-plugin`)
- **Module name:** `org.codehaus.stax2`
- **Parent POM:** `com.fasterxml:oss-parent`
- **CI:** GitHub Actions, Java 8 on Ubuntu
