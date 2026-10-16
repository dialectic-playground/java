<!-- START doctoc generated TOC please keep comment here to allow auto update -->
<!-- DON'T EDIT THIS SECTION, INSTEAD RE-RUN doctoc TO UPDATE -->

**Table of Contents** _generated with [DocToc](https://github.com/thlorenz/doctoc)_

- [Master Test Plan (MTP) — Standard Terminal Hello World](#master-test-plan-mtp--standard-terminal-hello-world)
  - [1. Introduction and scope](#1-introduction-and-scope)
    - [1.1 Objectives](#11-objectives)
    - [1.2 In scope / out of scope](#12-in-scope--out-of-scope)
  - [2. Test items](#2-test-items)
  - [3. Test levels and approach](#3-test-levels-and-approach)
  - [4. Test design](#4-test-design)
    - [4.1 Equivalence partitions and boundaries](#41-equivalence-partitions-and-boundaries)
    - [4.2 Additional test cases (beyond the supplied suite)](#42-additional-test-cases-beyond-the-supplied-suite)
    - [4.3 Negative and robustness testing](#43-negative-and-robustness-testing)
    - [4.4 Performance and resource testing](#44-performance-and-resource-testing)
  - [5. Test environments](#5-test-environments)
  - [6. Entry and exit criteria](#6-entry-and-exit-criteria)
  - [7. Defect management](#7-defect-management)
  - [8. Schedule and deliverables](#8-schedule-and-deliverables)
  - [9. Risks and contingencies](#9-risks-and-contingencies)
  - [10. Test summary](#10-test-summary)

<!-- END doctoc generated TOC please keep comment here to allow auto update -->

# Master Test Plan (MTP) — Standard Terminal Hello World

| Field                   | Value                                                  |
| :---------------------- | :----------------------------------------------------- |
| Project                 | `00-hello-world`                                       |
| Tools                   | dp-hello                                               |
| Implementation language | Java 25 (Maven 3.10.0, JUnit 5)                        |
| Author                  | Brandon Perkins (with Claude)                          |
| Version / date          | 1.0 — 2026-10-13                                       |
| Standard                | Structure adapted from IEEE 829 / ISO/IEC/IEEE 29119-3 |

## 1. Introduction and scope

### 1.1 Objectives

"Tested enough" means: every SRS acceptance criterion (FR-01 … FR-11) is exercised by a unit
test of the pure core or a black-box test, every CONV-13 diagnostic is checked byte for byte,
the public and hidden suites pass on Linux, macOS, and Windows, and NFR-02/CONV-31 are measured.

### 1.2 In scope / out of scope

| In scope                                                                            | Out of scope                              |
| :---------------------------------------------------------------------------------- | :---------------------------------------- |
| Every FR/NFR in the SRS and the applicable CONV rules, on Linux, macOS, and Windows | Behavior of the JVM itself (start-up, GC) |
| Argument decoding (UTF-8 on POSIX, UTF-16 on Windows)                               | Terminal rendering of non-ASCII output    |
| Build, lint, format, install, and uninstall hooks                                   | Performance of other Java versions        |

## 2. Test items

| Item                   | Version / location                                             |
| :--------------------- | :------------------------------------------------------------- |
| dp-hello               | `00-hello-world/src/main/java` on branch `feat/00-hello-world` |
| Unit tests             | `00-hello-world/src/test/java` (6 classes, 57 tests)           |
| Public black-box suite | `tests/public/` (10 tests)                                     |
| Hidden black-box suite | `tests/private/` (10 tests, run by CI)                         |

## 3. Test levels and approach

| Level              | Purpose                                                                           | Technique(s)                                             | Tooling                                                 | Owner          |
| :----------------- | :-------------------------------------------------------------------------------- | :------------------------------------------------------- | :------------------------------------------------------ | :------------- |
| Unit               | Parser, validator, greeting, output, Windows argument reconciliation in isolation | Equivalence partitioning, boundary-value analysis        | JUnit 5 (Jupiter) via Maven Surefire, `make test-unit`  | Developer      |
| Integration        | Parser → validator → greeting → outcome without I/O; Windows launcher end to end  | Specification-based (`CliTest`), process test on Windows | JUnit 5                                                 | Developer      |
| System (black-box) | The specified CLI behavior, byte-exact                                            | Specification-based                                      | `make test` → `run-tests.py`                            | Developer      |
| Acceptance         | Hidden suite passes on all OSes                                                   | —                                                        | CI `make check` (`test-hidden`)                         | CI             |
| Static analysis    | Warnings, style                                                                   | —                                                        | `make lint`: `javac -Xlint:all -Werror`, Spotless check | Developer / CI |

## 4. Test design

### 4.1 Equivalence partitions and boundaries

| Input         | Valid partitions                                          | Invalid partitions                                                         | Boundaries                                      |
| :------------ | :-------------------------------------------------------- | :------------------------------------------------------------------------- | :---------------------------------------------- |
| NAME          | ASCII, spaces, non-ASCII (`Zoë`, CJK, emoji), leading `-` | empty                                                                      | `''` vs one character                           |
| COUNT         | digits with value 1–1000, leading zeros                   | empty, non-digits (sign, space, `.`, Unicode digits), 0, > 1000, very long | 0, 1, 1000, 1001, `007`, 26 digits              |
| Option syntax | `-nX`, `-n X`, `--name=X`, `--name X`, repeats            | unknown short/long, abbreviations, missing value, `--help=`                | last token is an option needing a value         |
| Operands      | none                                                      | any word, `-`, anything after `--`                                         | first operand reported                          |
| Scan order    | `--help`/`--version` anywhere after valid syntax          | syntax error before them                                                   | `extra --help`, `--count 0 --help`, `-x --help` |

### 4.2 Additional test cases (beyond the supplied suite)

| ID   | Requirement(s)   | Preconditions      | Steps / command                                               | Expected result                             | Level       |
| :--- | :--------------- | :----------------- | :------------------------------------------------------------ | :------------------------------------------ | :---------- |
| T-01 | FR-02            | —                  | `parse("--name=-x")`, `parse("-n-x")`, `parse("--name","-x")` | name `-x` recorded                          | unit        |
| T-02 | FR-06            | —                  | `parse("-n","A",…,"--name","B","-c0")`                        | last values win                             | unit        |
| T-03 | FR-04            | —                  | `count("007")`, `count("0001000")`                            | 7, 1000                                     | unit        |
| T-04 | FR-05            | —                  | `count("0")`, `"1001"`, `"+1"`, `" 1"`, `"٣"`, 26 digits      | invalid argument, no overflow               | unit        |
| T-05 | FR-03            | —                  | `name(--name, "")`                                            | `invalid argument '' for '--name'`          | unit        |
| T-06 | CONV-12          | —                  | `run("extra","--help")`, `run("--count","0","--help")`        | help, status 0                              | unit        |
| T-07 | CONV-12          | —                  | `run("-x","--help")`                                          | `invalid option -- 'x'`, status 2           | unit        |
| T-08 | FR-09, CONV-09   | —                  | `run("--","-n")`                                              | `extra operand '-n'`                        | unit        |
| T-09 | FR-10            | —                  | `parse("-😀")`                                                | `invalid option -- '😀'` (whole code point) | unit        |
| T-10 | CONV-03, CONV-05 | —                  | `emit(Outcome("Hello, Zoë!\n"…))`                             | bytes `… 0xC3 0xAB ! 0x0A`                  | unit        |
| T-11 | CONV-25          | stream that throws | `emit` to a broken stream                                     | status 141, nothing on stderr               | unit        |
| T-12 | CONV-05          | Windows            | run the program with `--name "Zoë 日本語 😀"`                 | UTF-8 bytes of that name, empty stderr      | integration |
| T-13 | CONV-05          | —                  | `reconcile` with mismatching splits                           | JVM arguments kept                          | unit        |

### 4.3 Negative and robustness testing

Every CONV-13 message is asserted exactly in `OptionParserTest`, `ValidatorTest`, and
`CliTest`, including non-BMP option letters and arbitrarily long digit strings. A broken output
stream is simulated in `ConsoleOutputTest`. Files, permissions, and stdin are not applicable
(the tool reads neither); the black-box runner supplies the null device as stdin.

### 4.4 Performance and resource testing

Measured with `/usr/bin/time -f '%M %e'`, five runs each, on Fedora 44 (x86-64, 8 CPUs) with
OpenJDK 25.0.4, under an empty environment like the runner's. The baseline (CONV-31) is
`java -jar empty.jar`, a jar whose `main` does nothing.

| Command                         | Peak RSS (max) | Mean wall time |
| :------------------------------ | :------------- | :------------- |
| baseline: `java -jar empty.jar` | 37.8 MiB       | 28 ms          |
| `dp-hello`                      | 39.9 MiB       | 42 ms          |
| `dp-hello -c 1000 -n Zoë`       | 40.1 MiB       | 52 ms          |
| `dp-hello --help`               | 39.7 MiB       | 46 ms          |

Peak memory above the baseline is at most 2.3 MiB (limit 32 MiB), and run time is far below the
300 ms interpreted-runtime allowance of NFR-02. No JVM tuning flags are needed.

## 5. Test environments

| Environment  | OS / version                                           | Toolchain                                                | How provisioned                                |
| :----------- | :----------------------------------------------------- | :------------------------------------------------------- | :--------------------------------------------- |
| Local        | Fedora 44 + Podman                                     | Temurin 25, Maven 3.10.0 (container image)               | `make check` (`./Containerfile`)               |
| CI container | ubuntu-latest + Podman                                 | same image                                               | `make check`                                   |
| CI native    | ubuntu-latest, macos-latest, windows-latest (Git Bash) | actions/setup-java Temurin 25; Maven 3.10.0 via `./mvnw` | `make check roundtrip` with `DP_RUNTIME=local` |

## 6. Entry and exit criteria

- **Entry:** HLD and LLD written; `make all` succeeds; `make lint` is clean.
- **Exit:** all unit tests pass; public and hidden suites pass in the container and on all
  three OSes; `make roundtrip` passes natively; `make format` leaves no diff; the RTM shows every
  applicable requirement as Verified.
- **Suspension / resumption:** suspend on a toolchain failure (image, wrapper download);
  resume after the fix is merged and CI is rerun from the start.

## 7. Defect management

Defects are recorded as GitHub issues in `dialectic-playground/java` with the severity below
and the failing command. A fix is accepted only with a new unit test that fails before it, and
it is re-verified by the full CI matrix.

| Severity     | Definition                                                               |
| :----------- | :----------------------------------------------------------------------- |
| 1 — Critical | Crash, stack trace, or wrong stdout/exit status on a mainline path       |
| 2 — Major    | Wrong diagnostic text, or a platform-specific deviation (CRLF, encoding) |
| 3 — Minor    | Documentation, build ergonomics, or performance outside the NFR margin   |

## 8. Schedule and deliverables

| Milestone               | Deliverable                        | Target date |
| :---------------------- | :--------------------------------- | :---------- |
| Design complete         | `hld.md`, `lld.md`                 | 2026-10-13  |
| Implementation complete | `src/`, unit tests, Makefile hooks | 2026-10-13  |
| Verification complete   | RTM all Verified, CI green         | 2026-10-13  |

## 9. Risks and contingencies

| Risk                                           | Mitigation                                                                              |
| :--------------------------------------------- | :-------------------------------------------------------------------------------------- |
| Windows-only failure discovered late           | CI runs all three OSes on every push; a Windows-only unit test covers argument decoding |
| JVM argument decoding depends on the locale    | Launcher forces `C.UTF-8`; black-box tests run with an empty environment                |
| Maven or formatter version drift between hosts | Maven Wrapper with checksum, pinned plugins, Enforcer rules                             |

## 10. Test summary

| Metric                                | Value                                                                                       |
| :------------------------------------ | :------------------------------------------------------------------------------------------ |
| Unit tests (pass / total)             | 57 / 57 on Windows; 56 / 56 plus 1 skipped (Windows-only) elsewhere                         |
| Coverage (line / branch)              | Not instrumented; every production branch except the native-call failure paths is exercised |
| Public black-box (pass / 10)          | 10 / 10 (container, Linux, macOS, Windows)                                                  |
| Hidden black-box (pass / 10, from CI) | 10 / 10                                                                                     |
| Open defects by severity              | 0 / 0 / 0                                                                                   |
