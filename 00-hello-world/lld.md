<!-- START doctoc generated TOC please keep comment here to allow auto update -->
<!-- DON'T EDIT THIS SECTION, INSTEAD RE-RUN doctoc TO UPDATE -->

**Table of Contents** _generated with [DocToc](https://github.com/thlorenz/doctoc)_

- [Low-Level Design (LLD) — Standard Terminal Hello World](#low-level-design-lld--standard-terminal-hello-world)
  - [1. Source layout](#1-source-layout)
  - [2. Data types](#2-data-types)
    - [2.1 `OptionValue(String option, String value)`](#21-optionvaluestring-option-string-value)
    - [2.2 `ParsedArguments(Action action, Optional<OptionValue> name, Optional<OptionValue> count, Optional<String> firstOperand)`](#22-parsedargumentsaction-action-optionaloptionvalue-name-optionaloptionvalue-count-optionalstring-firstoperand)
    - [2.3 `Config(String name, int count)`](#23-configstring-name-int-count)
    - [2.4 `Outcome(String stdout, String stderr, int exitStatus)`](#24-outcomestring-stdout-string-stderr-int-exitstatus)
    - [2.5 Error type: `UsageException extends Exception`](#25-error-type-usageexception-extends-exception)
  - [3. Module and function specifications](#3-module-and-function-specifications)
    - [3.1 `OptionParser.parse`](#31-optionparserparse)
    - [3.2 `Validator.validate`, `Validator.name`, `Validator.count`](#32-validatorvalidate-validatorname-validatorcount)
    - [3.3 `Greeting.line`, `Greeting.text`](#33-greetingline-greetingtext)
    - [3.4 `Cli.run`](#34-clirun)
    - [3.5 `ConsoleOutput.emit`](#35-consoleoutputemit)
    - [3.6 `WindowsCommandLine.arguments`, `WindowsCommandLine.reconcile`](#36-windowscommandlinearguments-windowscommandlinereconcile)
    - [3.7 `DpHello.main`](#37-dphellomain)
  - [4. Algorithms](#4-algorithms)
  - [5. Platform-specific details](#5-platform-specific-details)
  - [6. Resource management](#6-resource-management)
  - [7. Sequence of one invocation](#7-sequence-of-one-invocation)
  - [8. Design-to-code checklist](#8-design-to-code-checklist)

<!-- END doctoc generated TOC please keep comment here to allow auto update -->

# Low-Level Design (LLD) — Standard Terminal Hello World

| Field                   | Value                                                 |
| :---------------------- | :---------------------------------------------------- |
| Project                 | `00-hello-world`                                      |
| Tools                   | dp-hello                                              |
| Implementation language | Java 25 (Maven 3.10.0, JUnit 5)                       |
| Author                  | Brandon Perkins (with Claude)                         |
| Version / date          | 1.0 — 2026-10-13                                      |
| Status                  | Approved                                              |
| Inputs                  | `hld.md`, `specs/SRS.md`, `shared/CLI-CONVENTIONS.md` |

## 1. Source layout

Package `io.github.dialecticplayground.hello` (abbreviated `…/hello/` below).

```text
00-hello-world/
├── pom.xml                         # pinned plugins, javac -Xlint:all -Werror, Spotless, jar manifest
├── mvnw, .mvn/wrapper/             # Maven Wrapper: Maven 3.10.0, SHA-256 checked
├── Makefile                        # DP_* hooks; builds the launcher target/bin/dp-hello
└── src/
    ├── main/java/…/hello/
    │   ├── DpHello.java            # main: I/O shell
    │   ├── Cli.java                # pure core; help and version texts
    │   ├── OptionParser.java       # CONV-07 … CONV-12 scanner
    │   ├── Validator.java          # FR-03, FR-05, FR-09
    │   ├── Greeting.java           # pure greeting construction
    │   ├── ConsoleOutput.java      # UTF-8 bytes to raw streams
    │   ├── WindowsCommandLine.java # UTF-16 arguments on Windows (FFM)
    │   ├── ParsedArguments.java, OptionValue.java, Config.java, Outcome.java
    │   └── UsageException.java
    └── test/java/…/hello/          # JUnit 5: one *Test class per production class
```

## 2. Data types

### 2.1 `OptionValue(String option, String value)`

An option's value with the option as written (`-n`, `--name`, `-c`, `--count`), quoted by the
"invalid argument" diagnostic (CONV-13). Supports FR-03, FR-05.

### 2.2 `ParsedArguments(Action action, Optional<OptionValue> name, Optional<OptionValue> count, Optional<String> firstOperand)`

| Field           | Default | Invariant                                      | SRS            |
| :-------------- | :------ | :--------------------------------------------- | :------------- |
| `action`        | `GREET` | `HELP` / `VERSION` results carry no values     | FR-07, FR-08   |
| `name`, `count` | empty   | the last occurrence only, not yet validated    | FR-06, CONV-11 |
| `firstOperand`  | empty   | the first operand, including tokens after `--` | FR-09, CONV-09 |

### 2.3 `Config(String name, int count)`

Validated request. The compact constructor enforces `!name.isEmpty()` and `1 ≤ count ≤ 1000`
(throws `IllegalArgumentException`, a programming error that `Validator` never triggers).
Constants: `DEFAULT_NAME = "World"`, `DEFAULT_COUNT = 1`, `MIN_COUNT = 1`, `MAX_COUNT = 1000`.

### 2.4 `Outcome(String stdout, String stderr, int exitStatus)`

Constants `SUCCESS = 0`, `USAGE_ERROR = 2`; factories `success(stdout)` and `usageError(error)`.

### 2.5 Error type: `UsageException extends Exception`

Checked; no stack trace or suppression is recorded. `getMessage()` is the CONV-13 text;
`diagnostic()` returns `dp-hello: <message>\nTry 'dp-hello --help' for more information.\n`.
Exit status 2 for every variant.

| Variant (message)                                           | Raised by                           | Exit status |
| :---------------------------------------------------------- | :---------------------------------- | :---------- |
| `invalid option -- 'x'`                                     | `OptionParser.shortOption`          | 2           |
| `unrecognized option '<token>'`                             | `OptionParser.longOption`           | 2           |
| `option requires an argument -- 'n'` / `'c'`                | `OptionParser.shortOption`          | 2           |
| `option '--name'` / `'--count' requires an argument`        | `OptionParser.longOption`           | 2           |
| `option '--help'` / `'--version' doesn't allow an argument` | `OptionParser.longOption`           | 2           |
| `invalid argument '<value>' for '<option>'`                 | `Validator.name`, `Validator.count` | 2           |
| `extra operand '<operand>'`                                 | `Validator.validate`                | 2           |

## 3. Module and function specifications

### 3.1 `OptionParser.parse`

- **Signature:** `static ParsedArguments parse(List<String> arguments) throws UsageException`
- **Responsibility:** one left-to-right scan that checks option syntax and records values.
- **Preconditions:** `arguments` non-null, elements non-null.
- **Postconditions:** `HELP`/`VERSION` as soon as reached; otherwise `GREET` with the last
  values and first operand.
- **Errors:** the syntax variants of §2.5, at the first offending token.
- **Algorithm:** for each token, in order:
  1. after `--`, or if the token is `-` or does not start with `-`: remember it if it is the
     first operand;
  2. `--`: end option processing;
  3. `--NAME[=VALUE]` (`longOption`): split at the first `=`; `help`/`version` with a value →
     "doesn't allow"; without → return immediately; `name`/`count` take the attached value or
     the next token (none → "requires an argument"); any other name → "unrecognized option"
     with the whole token (no abbreviations);
  4. `-X…` (`shortOption`): read the first code point (so `-😀` is reported whole); not `n`/`c` →
     "invalid option"; the value is the rest of the token, or the next token (none → "option
     requires an argument").
     Values are stored with `record`, replacing earlier ones.
- **Complexity:** O(total argument length) time, O(1) extra memory.
- **Satisfies:** FR-02, FR-04, FR-06, FR-07, FR-08, FR-10, CONV-07 … CONV-12.
- **Unit tests:** `OptionParserTest` (all 15 tests).

### 3.2 `Validator.validate`, `Validator.name`, `Validator.count`

- **Signatures:** `static Config validate(ParsedArguments)`, `static String name(OptionValue)`,
  `static int count(OptionValue)`, all `throws UsageException`.
- **Responsibility:** reject operands and invalid winning values after the scan.
- **Algorithm:** operand present → "extra operand"; name empty → invalid argument; count: empty
  or any character outside `0`–`9` → invalid; strip leading zeros; more than four significant
  digits → invalid (no overflow for any length); parse; outside 1–1000 → invalid.
- **Complexity:** O(length of the values).
- **Satisfies:** FR-03, FR-04, FR-05, FR-06, FR-09.
- **Unit tests:** `ValidatorTest.defaults`, `nameIsUsedVerbatim`,
  `emptyNameIsRejectedWithOptionAsWritten`, `validCounts`, `invalidCounts`,
  `operandIsRejectedBeforeValues`, `nameIsCheckedBeforeCount`, `validRequest`.

### 3.3 `Greeting.line`, `Greeting.text`

- **Signatures:** `static String line(String name)`, `static String text(Config config)`.
- **Responsibility:** pure construction of `Hello, NAME!\n`, repeated COUNT times.
- **Complexity:** O(count × name length) time and memory (≤ 1000 lines).
- **Satisfies:** FR-01, FR-02, FR-04, NFR-05.
- **Unit tests:** `GreetingTest.lineEndsInLf`, `textRepeatsTheLine`, `configRejectsInvalidValues`.

### 3.4 `Cli.run`

- **Signature:** `static Outcome run(List<String> arguments)`
- **Responsibility:** parse, validate, and build the outcome; map `UsageException` to status 2.
- **Postconditions:** stdout is empty whenever the status is 2 (FR-11); stderr is empty on success.
- **Satisfies:** FR-01 … FR-11, CONV-01, CONV-02, CONV-13, CONV-14, CONV-23.
- **Unit tests:** `CliTest.greetings`, `helpMatchesTheUserManual`, `version`,
  `usageErrorsWriteNothingToStdout`.

### 3.5 `ConsoleOutput.emit`

- **Signature:** `static int emit(Outcome outcome, OutputStream stdout, OutputStream stderr)`
- **Responsibility:** write each non-empty text as UTF-8 bytes in one `write` call, then flush.
- **Errors:** an `IOException` (for example EPIPE) ends output and returns `WRITE_FAILED` (141)
  with no diagnostic (CONV-25).
- **Satisfies:** NFR-01, CONV-03, CONV-05, CONV-25.
- **Unit tests:** `ConsoleOutputTest.writesUtf8BytesWithLfOnly`,
  `usageErrorGoesToStderrWithItsStatus`, `brokenPipeEndsQuietly`.

### 3.6 `WindowsCommandLine.arguments`, `WindowsCommandLine.reconcile`

- **Signatures:** `static List<String> arguments(String[] launcherArguments)`,
  `static List<String> reconcile(List<String> decoded, List<String> wide)`.
- **Responsibility:** return the arguments as Unicode text.
- **Algorithm:** if `os.name` does not start with `Windows`, return the JVM's arguments. On
  Windows, `wideArguments` loads `kernel32.dll`/`shell32.dll` in a confined arena, calls
  `GetCommandLineW` and `CommandLineToArgvW`, decodes each entry as UTF-16LE, and frees the
  vector with `LocalFree`. `reconcile` keeps the last `decoded.size()` wide entries, provided
  there is at least one leading launcher entry and every pure-ASCII wide entry equals the
  corresponding JVM argument; otherwise it returns `decoded`. Any `Throwable` from the native
  calls also returns `decoded`.
- **Satisfies:** FR-02, UR-09, CONV-05.
- **Unit tests:** `WindowsCommandLineTest.takesTheProgramArgumentsFromTheEndOfTheWideVector`,
  `keepsTheLauncherArgumentsWhenTheVectorsDisagree`, `noArguments`,
  `argumentsPassThroughUnchangedOffWindows`, `unicodeNameSurvivesTheWindowsLauncher` (Windows).

### 3.7 `DpHello.main`

Calls `WindowsCommandLine.arguments`, `Cli.run`, and `ConsoleOutput.emit` with
`new FileOutputStream(FileDescriptor.out/err)`, then `System.exit(status)`. Covered by P01–P10.

## 4. Algorithms

The only non-trivial algorithm is the scan (§3.1). It is a single pass with an index cursor
so that options taking a separate value can consume the next token. Validating after the scan
(rather than inside it, as getopt-based tools do) is what CONV-12 requires: `--count 0 --help`
prints the help. COUNT parsing strips leading zeros and bounds the number of digits before
calling `Integer.parseInt`, so no input can overflow or throw.

## 5. Platform-specific details

| Concern                  | Linux                                         | macOS        | Windows                                                 | Abstraction                                                               |
| :----------------------- | :-------------------------------------------- | :----------- | :------------------------------------------------------ | :------------------------------------------------------------------------ |
| argv decoding by the JVM | locale charset (`sun.jnu.encoding`)           | always UTF-8 | ANSI code page                                          | `LC_ALL=C.UTF-8` in the `#!` line (POSIX); `WindowsCommandLine` (Windows) |
| Starting the launcher    | kernel runs `env -S LC_ALL=C.UTF-8 java -jar` | same         | runner (or Git Bash) resolves `java` from the `#!` line | `Makefile` `SHEBANG`                                                      |
| stdout/stderr bytes      | raw fd                                        | raw fd       | raw handle, no CRLF translation                         | `ConsoleOutput`                                                           |
| Native access warning    | —                                             | —            | suppressed by manifest `Enable-Native-Access`           | `pom.xml`                                                                 |

## 6. Resource management

No files, sockets, or processes are opened. The two `FileOutputStream`s wrap the standard
descriptors and are not closed (the process exits). On Windows the FFM `Arena` is closed by
try-with-resources (unloading the library lookups), and the `CommandLineToArgvW` vector is
released with `LocalFree` in a `finally` block. Peak output buffer: 1000 × (8 + name length)
characters plus its UTF-8 copy.

## 7. Sequence of one invocation

Public test P06, `dp-hello -c2 -n Ken`:

1. `DpHello.main(["-c2", "-n", "Ken"])`; `WindowsCommandLine.arguments` returns the same list.
2. `Cli.run` → `OptionParser.parse`: `-c2` → `shortOption` records count `-c`=`2`; `-n` →
   `shortOption` takes `Ken` from the next token; result `GREET`.
3. `Validator.validate`: no operand; name `Ken`; count `2` → `Config("Ken", 2)`.
4. `Greeting.text` → `"Hello, Ken!\nHello, Ken!\n"`; `Outcome.success(...)`.
5. `ConsoleOutput.emit` writes 24 bytes to stdout; nothing to stderr; returns 0.
6. `System.exit(0)`.

## 8. Design-to-code checklist

| HLD component        | LLD section | Source file(s)                                                  | Unit-test file(s)             |
| :------------------- | :---------- | :-------------------------------------------------------------- | :---------------------------- |
| `OptionParser`       | §3.1        | `OptionParser.java`, `ParsedArguments.java`, `OptionValue.java` | `OptionParserTest.java`       |
| `Validator`          | §3.2        | `Validator.java`, `Config.java`                                 | `ValidatorTest.java`          |
| `Greeting`           | §3.3        | `Greeting.java`                                                 | `GreetingTest.java`           |
| `Cli`                | §3.4        | `Cli.java`, `Outcome.java`, `UsageException.java`               | `CliTest.java`                |
| `ConsoleOutput`      | §3.5        | `ConsoleOutput.java`                                            | `ConsoleOutputTest.java`      |
| `WindowsCommandLine` | §3.6        | `WindowsCommandLine.java`                                       | `WindowsCommandLineTest.java` |
| `DpHello`            | §3.7        | `DpHello.java`                                                  | black-box P01–P10             |
