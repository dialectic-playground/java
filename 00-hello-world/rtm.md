<!-- START doctoc generated TOC please keep comment here to allow auto update -->
<!-- DON'T EDIT THIS SECTION, INSTEAD RE-RUN doctoc TO UPDATE -->

**Table of Contents** _generated with [DocToc](https://github.com/thlorenz/doctoc)_

- [Requirements Traceability Matrix (RTM) — Standard Terminal Hello World](#requirements-traceability-matrix-rtm--standard-terminal-hello-world)
  - [1. User requirements to system requirements](#1-user-requirements-to-system-requirements)
  - [2. Forward traceability](#2-forward-traceability)
  - [3. Backward traceability (test → requirement)](#3-backward-traceability-test-%E2%86%92-requirement)
  - [4. Coverage summary](#4-coverage-summary)
  - [5. Gaps and justifications](#5-gaps-and-justifications)

<!-- END doctoc generated TOC please keep comment here to allow auto update -->

# Requirements Traceability Matrix (RTM) — Standard Terminal Hello World

| Field          | Value                         |
| :------------- | :---------------------------- |
| Project        | `00-hello-world`              |
| Tools          | dp-hello                      |
| Author         | Brandon Perkins (with Claude) |
| Version / date | 1.0 — 2026-10-13              |

Code paths are relative to `src/main/java/io/github/dialecticplayground/hello/`; unit tests are
in the matching `src/test/java/…/hello/*Test.java` classes. P01–P10 are the public black-box
tests, run by `make test` on Linux, macOS, and Windows.

## 1. User requirements to system requirements

| User requirement (URD) | Derived system requirements (SRS) |
| :--------------------- | :-------------------------------- |
| UR-01                  | FR-01                             |
| UR-02                  | FR-02                             |
| UR-03                  | FR-04                             |
| UR-04                  | FR-07                             |
| UR-05                  | FR-08                             |
| UR-06                  | FR-03, FR-05, FR-09, FR-10, FR-11 |
| UR-07                  | FR-01, SRS §3.7                   |
| UR-08                  | NFR-01, CONV-03                   |
| UR-09                  | FR-02, CONV-05                    |
| UR-10                  | FR-06, FR-10, CONV-07 … CONV-15   |
| UR-11                  | NFR-02                            |
| UR-12                  | NFR-06                            |

## 2. Forward traceability

| Req. ID           | Summary                                  | Design (HLD/LLD)         | Code (file::function)                                                                                | Verification (unit / black-box)                                                                                                                     | Status   |
| :---------------- | :--------------------------------------- | :----------------------- | :--------------------------------------------------------------------------------------------------- | :-------------------------------------------------------------------------------------------------------------------------------------------------- | :------- |
| UR-01             | Classic greeting                         | HLD §3.3                 | via FR-01                                                                                            | P01                                                                                                                                                 | Verified |
| UR-02             | Choose the name                          | HLD §3.3                 | via FR-02                                                                                            | P02, P03, P04                                                                                                                                       | Verified |
| UR-03             | Repeat the greeting                      | HLD §3.3                 | via FR-04                                                                                            | P05, P06                                                                                                                                            | Verified |
| UR-04             | Help screen                              | HLD §3.2                 | via FR-07                                                                                            | P07                                                                                                                                                 | Verified |
| UR-05             | Version query                            | HLD §3.2                 | via FR-08                                                                                            | P08                                                                                                                                                 | Verified |
| UR-06             | Clear usage errors, distinct status      | HLD §6                   | via FR-03, FR-05, FR-09 … FR-11                                                                      | P09, P10                                                                                                                                            | Verified |
| UR-07             | Status 0 on success                      | HLD §6                   | via FR-01                                                                                            | P01–P08                                                                                                                                             | Verified |
| UR-08             | Identical bytes on all OSes              | HLD §5                   | via NFR-01                                                                                           | P01–P10 on 3 OSes (CI)                                                                                                                              | Verified |
| UR-09             | Spaces and non-ASCII names               | HLD §5                   | via FR-02, CONV-05                                                                                   | `GreetingTest.lineEndsInLf`, `WindowsCommandLineTest.unicodeNameSurvivesTheWindowsLauncher`                                                         | Verified |
| UR-10             | Common CLI conventions                   | HLD §2                   | via CONV-07 … CONV-15                                                                                | `OptionParserTest`                                                                                                                                  | Verified |
| UR-11             | Instant start                            | HLD §7                   | via NFR-02                                                                                           | MTP §4.4 measurement                                                                                                                                | Verified |
| UR-12             | Standard install/uninstall               | HLD §4 D8                | via NFR-06                                                                                           | `make roundtrip` (CI)                                                                                                                               | Verified |
| FR-01             | Default greeting                         | HLD §3.3, LLD §3.3, §3.4 | `Validator.java::validate`, `Greeting.java::text`                                                    | `ValidatorTest.defaults`, `CliTest.greetings`, P01                                                                                                  | Verified |
| FR-02             | Named greeting, verbatim                 | LLD §3.1, §3.3           | `OptionParser.java::longOption`, `::shortOption`, `Greeting.java::line`                              | `OptionParserTest.separateValue`, `attachedValues`, `valuesMayStartWithDashOrBeEmpty`, `ValidatorTest.nameIsUsedVerbatim`, P02, P03, P04            | Verified |
| FR-03             | Empty name rejected                      | LLD §3.2                 | `Validator.java::name`                                                                               | `ValidatorTest.emptyNameIsRejectedWithOptionAsWritten`, `CliTest.usageErrorsWriteNothingToStdout`                                                   | Verified |
| FR-04             | Repetition 1–1000, leading zeros         | LLD §3.2, §3.3           | `Validator.java::count`, `Greeting.java::text`                                                       | `ValidatorTest.validCounts`, `GreetingTest.textRepeatsTheLine`, P05, P06                                                                            | Verified |
| FR-05             | Invalid count rejected                   | LLD §3.2                 | `Validator.java::count`                                                                              | `ValidatorTest.invalidCounts`, `CliTest.usageErrorsWriteNothingToStdout`                                                                            | Verified |
| FR-06             | Last occurrence wins                     | LLD §3.1                 | `OptionParser.java::record`                                                                          | `OptionParserTest.lastOccurrenceWins`                                                                                                               | Verified |
| FR-07             | Help, scan order                         | LLD §3.1, §3.4           | `OptionParser.java::longOption`, `Cli.java::HELP`                                                    | `OptionParserTest.helpAndVersionStopTheScan`, `CliTest.helpMatchesTheUserManual`, P07                                                               | Verified |
| FR-08             | Version                                  | LLD §3.4                 | `Cli.java::VERSION`                                                                                  | `CliTest.version`, P08                                                                                                                              | Verified |
| FR-09             | Operands rejected after the scan         | LLD §3.1, §3.2           | `OptionParser.java::scan`, `Validator.java::validate`                                                | `OptionParserTest.operandsAreCollectedNotRejected`, `terminatorMakesEverythingAfterItAnOperand`, `ValidatorTest.operandIsRejectedBeforeValues`, P10 | Verified |
| FR-10             | Unknown and malformed options            | LLD §3.1                 | `OptionParser.java::longOption`, `::shortOption`                                                     | `OptionParserTest.unknownShortOption`, `unknownLongOptionQuotesWholeToken`, `missingArguments`, `flagsRejectValues`, P09                            | Verified |
| FR-11             | Nothing on stdout on error               | HLD §3.3, LLD §3.4       | `Cli.java::run`, `Outcome.java::usageError`                                                          | `CliTest.usageErrorsWriteNothingToStdout`, P09, P10                                                                                                 | Verified |
| NFR-01            | Byte-identical, LF only                  | HLD §5, LLD §3.5         | `ConsoleOutput.java::emit`                                                                           | `ConsoleOutputTest.writesUtf8BytesWithLfOnly`, P01–P10 on 3 OSes                                                                                    | Verified |
| NFR-02            | < 300 ms; memory < 32 MiB above baseline | HLD §7                   | whole program                                                                                        | MTP §4.4 measurement                                                                                                                                | Verified |
| NFR-03            | No crash or stack trace                  | HLD §6                   | `Cli.java::run`, `Validator.java::count`, `ConsoleOutput.java::emit`                                 | `ValidatorTest.invalidCounts`, `OptionParserTest.unknownShortOption`, `ConsoleOutputTest.brokenPipeEndsQuietly`                                     | Verified |
| NFR-04            | Separate units                           | HLD §3.2                 | `OptionParser`, `Validator`, `Greeting`, `ConsoleOutput`                                             | Design review (this RTM, LLD §8)                                                                                                                    | Verified |
| NFR-05            | Pure, tested greeting                    | LLD §3.3                 | `Greeting.java::line`, `::text`                                                                      | `GreetingTest`                                                                                                                                      | Verified |
| NFR-06            | Every BASE-MAKEFILE target               | HLD §4 D8                | `Makefile` hooks                                                                                     | CI `make check roundtrip` (container + 3 OSes)                                                                                                      | Verified |
| NFR-07            | No files, env, subprocesses              | HLD §7                   | whole program                                                                                        | Inspection: no `java.io.File*` input, `System.getenv`, or `ProcessBuilder` in `src/main`                                                            | Verified |
| CONV-01           | Canonical name                           | LLD §2.5                 | `Cli.java::PROGRAM`                                                                                  | `CliTest.usageErrorsWriteNothingToStdout`, P09                                                                                                      | Verified |
| CONV-02           | Version line                             | LLD §3.4                 | `Cli.java::VERSION`                                                                                  | `CliTest.version`, P08                                                                                                                              | Verified |
| CONV-03           | LF only, binary output                   | HLD §5                   | `ConsoleOutput.java::emit`                                                                           | `ConsoleOutputTest.writesUtf8BytesWithLfOnly`, P01–P10 on Windows                                                                                   | Verified |
| CONV-05           | UTF-8 arguments and output               | HLD §5, LLD §3.6         | `WindowsCommandLine.java::arguments`, `::reconcile`, `Makefile::SHEBANG`, `ConsoleOutput.java::emit` | `WindowsCommandLineTest` (all), `ConsoleOutputTest.writesUtf8BytesWithLfOnly`                                                                       | Verified |
| CONV-06           | Locale-independent                       | HLD §5                   | `Makefile::SHEBANG` (forces `C.UTF-8`)                                                               | P01–P10 (empty environment)                                                                                                                         | Verified |
| CONV-07           | Short options, attached values           | LLD §3.1                 | `OptionParser.java::shortOption`                                                                     | `OptionParserTest.attachedValues`, `bundledShortOptionConsumesRestOfToken`, P06                                                                     | Verified |
| CONV-08           | Long options, no abbreviation            | LLD §3.1                 | `OptionParser.java::longOption`                                                                      | `OptionParserTest.unknownLongOptionQuotesWholeToken`, P04                                                                                           | Verified |
| CONV-09           | `--` terminator                          | LLD §3.1                 | `OptionParser.java::scan`                                                                            | `OptionParserTest.terminatorMakesEverythingAfterItAnOperand`                                                                                        | Verified |
| CONV-10           | Operands, `-`                            | LLD §3.1                 | `OptionParser.java::isOperand`                                                                       | `OptionParserTest.operandsAreCollectedNotRejected`, P10                                                                                             | Verified |
| CONV-11           | Repetition                               | LLD §3.1                 | `OptionParser.java::record`                                                                          | `OptionParserTest.lastOccurrenceWins`                                                                                                               | Verified |
| CONV-12           | Scan order (v1.4.0)                      | HLD §4 D3                | `OptionParser.java::scan`, `Validator.java::validate`                                                | `OptionParserTest.helpAndVersionStopTheScan`, `syntaxErrorBeforeHelpWins`, `CliTest.helpMatchesTheUserManual`                                       | Verified |
| CONV-13           | Exact usage messages                     | LLD §2.5                 | `OptionParser.java`, `Validator.java`, `UsageException.java::invalidArgument`                        | `OptionParserTest`, `ValidatorTest`, P09, P10                                                                                                       | Verified |
| CONV-14           | "Try" line, status 2                     | LLD §2.5                 | `UsageException.java::diagnostic`, `Outcome.java::usageError`                                        | `CliTest.usageErrorsWriteNothingToStdout`, P09, P10                                                                                                 | Verified |
| CONV-15           | Values quoted verbatim                   | LLD §2.5                 | `UsageException.java::invalidArgument`                                                               | `ValidatorTest.invalidCounts`, `emptyNameIsRejectedWithOptionAsWritten`                                                                             | Verified |
| CONV-18           | Exit statuses 0 / 2                      | HLD §6                   | `Outcome.java`                                                                                       | `CliTest` (all), P01–P10                                                                                                                            | Verified |
| CONV-22           | `--help`, `--version` take no argument   | LLD §3.1                 | `OptionParser.java::longOption`                                                                      | `OptionParserTest.flagsRejectValues`                                                                                                                | Verified |
| CONV-23           | Help text verbatim                       | LLD §3.4                 | `Cli.java::HELP`                                                                                     | `CliTest.helpMatchesTheUserManual`, P07                                                                                                             | Verified |
| CONV-24           | No crash or stack trace                  | HLD §6                   | `Cli.java::run`                                                                                      | as NFR-03                                                                                                                                           | Verified |
| CONV-25           | EPIPE ends quietly                       | LLD §3.5                 | `ConsoleOutput.java::emit`                                                                           | `ConsoleOutputTest.brokenPipeEndsQuietly`                                                                                                           | Verified |
| CONV-26           | Stdin not read                           | HLD §3.1                 | whole program (no stdin access)                                                                      | Inspection; P01–P10 (stdin is the null device)                                                                                                      | Verified |
| CONV-27 … CONV-30 | Black-box harness conditions             | HLD §5                   | `Makefile` (`BIN_DIR`, `#!` launcher)                                                                | P01–P10                                                                                                                                             | Verified |
| CONV-31           | Memory above the JVM baseline            | HLD §7                   | whole program                                                                                        | MTP §4.4 measurement                                                                                                                                | Verified |

## 3. Backward traceability (test → requirement)

| Test                                                                                                                | Type                    | Requirement(s) verified                           |
| :------------------------------------------------------------------------------------------------------------------ | :---------------------- | :------------------------------------------------ |
| P01                                                                                                                 | black-box, public       | FR-01, UR-01, UR-07                               |
| P02, P03                                                                                                            | black-box, public       | FR-02, UR-02                                      |
| P04                                                                                                                 | black-box, public       | FR-02, CONV-08                                    |
| P05                                                                                                                 | black-box, public       | FR-04, UR-03                                      |
| P06                                                                                                                 | black-box, public       | FR-04, CONV-07                                    |
| P07                                                                                                                 | black-box, public       | FR-07, CONV-23                                    |
| P08                                                                                                                 | black-box, public       | FR-08, CONV-02                                    |
| P09                                                                                                                 | black-box, public       | FR-10, FR-11, CONV-13, CONV-14                    |
| P10                                                                                                                 | black-box, public       | FR-09, FR-11, CONV-10                             |
| `OptionParserTest.noArgumentsRecordsNothing`, `separateValue`, `attachedValues`, `valuesMayStartWithDashOrBeEmpty`  | unit                    | FR-02, CONV-07, CONV-08                           |
| `OptionParserTest.bundledShortOptionConsumesRestOfToken`                                                            | unit                    | CONV-07                                           |
| `OptionParserTest.lastOccurrenceWins`                                                                               | unit                    | FR-06, CONV-11                                    |
| `OptionParserTest.operandsAreCollectedNotRejected`, `terminatorMakesEverythingAfterItAnOperand`                     | unit                    | FR-09, CONV-09, CONV-10                           |
| `OptionParserTest.helpAndVersionStopTheScan`, `syntaxErrorBeforeHelpWins`                                           | unit                    | FR-07, FR-08, CONV-12                             |
| `OptionParserTest.unknownShortOption`, `unknownLongOptionQuotesWholeToken`, `missingArguments`, `flagsRejectValues` | unit                    | FR-10, CONV-13, CONV-22                           |
| `ValidatorTest.defaults`, `validRequest`, `nameIsUsedVerbatim`                                                      | unit                    | FR-01, FR-02                                      |
| `ValidatorTest.emptyNameIsRejectedWithOptionAsWritten`                                                              | unit                    | FR-03, CONV-15                                    |
| `ValidatorTest.validCounts`, `invalidCounts`                                                                        | unit                    | FR-04, FR-05, CONV-15, NFR-03                     |
| `ValidatorTest.operandIsRejectedBeforeValues`, `nameIsCheckedBeforeCount`                                           | unit                    | FR-09, CONV-12                                    |
| `GreetingTest.lineEndsInLf`, `textRepeatsTheLine`, `configRejectsInvalidValues`                                     | unit                    | FR-01, FR-02, FR-04, NFR-05                       |
| `CliTest.greetings`, `helpMatchesTheUserManual`, `version`, `usageErrorsWriteNothingToStdout`                       | unit (end to end, pure) | FR-01 … FR-11, CONV-01, CONV-02, CONV-14, CONV-23 |
| `ConsoleOutputTest.writesUtf8BytesWithLfOnly`, `usageErrorGoesToStderrWithItsStatus`                                | unit                    | NFR-01, CONV-03, CONV-05                          |
| `ConsoleOutputTest.brokenPipeEndsQuietly`                                                                           | unit                    | CONV-25, NFR-03                                   |
| `WindowsCommandLineTest` (5 tests; one runs on Windows only)                                                        | unit / integration      | CONV-05, UR-09                                    |

## 4. Coverage summary

| Category                       | Total | Designed | Implemented | Verified |
| :----------------------------- | :---- | :------- | :---------- | :------- |
| User requirements (UR)         | 12    | 12       | 12          | 12       |
| Functional (FR)                | 11    | 11       | 11          | 11       |
| Non-functional (NFR)           | 7     | 7        | 7           | 7        |
| Conventions (CONV, applicable) | 25    | 25       | 25          | 25       |

## 5. Gaps and justifications

No applicable requirement is unverified. CONV-04, CONV-16, CONV-17, and CONV-19 … CONV-21 do
not apply: `dp-hello` copies no input and handles no files or paths. NFR-02 and CONV-31 are
verified by measurement (master test plan §4.4) rather than by an automated test.
