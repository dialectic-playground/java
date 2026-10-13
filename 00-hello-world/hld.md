# High-Level Design (HLD) — Standard Terminal Hello World

| Field | Value |
| :-- | :-- |
| Project | `00-hello-world` |
| Tools | dp-hello |
| Implementation language | Java 25 (Maven 3.10.0, JUnit 5) |
| Author | Brandon Perkins (with Claude) |
| Version / date | 1.0 — 2026-10-13 |
| Status | Approved |
| Inputs | `specs/URD.md`, `specs/SRS.md`, `specs/user-manual.md`, `shared/CLI-CONVENTIONS.md` |

## 1. Introduction

### 1.1 Scope
`dp-hello` writes `Hello, NAME!` + LF to standard output COUNT times (NAME defaults to `World`,
COUNT to 1, range 1–1000), prints the help and version texts, and reports usage errors with the
exact CONV-13/CONV-14 diagnostics and exit status 2. It reads no input, files, or environment
variables, takes no operands, and starts no processes (SRS §1.2, NFR-07).

### 1.2 Definitions and acronyms
| Term | Meaning |
| :-- | :-- |
| JVM | Java Virtual Machine; the tool runs as `java -jar` on Java 25 |
| Launcher | `target/bin/dp-hello`: the executable jar with a `#!` line prepended |
| Outcome | The stdout text, stderr text, and exit status of one invocation, computed purely |
| Scan | The single left-to-right pass over the arguments (CONV-12) |
| FFM | `java.lang.foreign`, the Foreign Function & Memory API, used to call Win32 |

### 1.3 References
- SRS §3 (interfaces), §4 (FR-01 … FR-11), §5 (NFR-01 … NFR-07), §6 (diagnostics).
- CLI-CONV §2 (CONV-03, CONV-05), §3 (CONV-07 … CONV-12), §4.1 (CONV-13 … CONV-15), §8.
- User manual "HELP OUTPUT" (normative help text).

## 2. Requirements summary

| Area | SRS requirements | Architectural impact |
| :-- | :-- | :-- |
| Option scanning | FR-02, FR-04, FR-06, FR-07, FR-08, FR-10, CONV-07 … CONV-12 | `OptionParser`: hand-written scanner that checks syntax only and records values |
| Validation | FR-03, FR-05, FR-09, FR-11 | `Validator` runs after the scan, so help/version win (CONV-12) |
| Greeting | FR-01, FR-02, FR-04, NFR-05 | `Greeting`: pure functions |
| Diagnostics and status | FR-03, FR-05, FR-09 … FR-11, CONV-13 … CONV-15, CONV-18 | `UsageException` carries the message; `Cli` maps it to stderr + status 2 in one place |
| Byte-exact, LF-only, UTF-8 output | NFR-01, CONV-03, CONV-05 | `ConsoleOutput` writes UTF-8 bytes to raw file-descriptor streams |
| UTF-8 arguments | FR-02, UR-09, CONV-05 | Launcher locale on POSIX; `WindowsCommandLine` on Windows |
| Robustness | NFR-03, CONV-24, CONV-25 | Total parser (only `UsageException` escapes it); write failures end quietly |
| Build | NFR-06 | Maven Wrapper and pinned `pom.xml` behind the Makefile hooks |

## 3. Architectural overview

### 3.1 Context diagram

```text
  argv (Windows: UTF-16 ──▶ ┌──────────────┐ ──▶ stdout (UTF-8, LF only)
        command line)       │   dp-hello   │ ──▶ stderr (diagnostics)
                            │   (JVM 25)   │ ──▶ exit status 0 / 2
                            └──────────────┘
```

No stdin, file system, environment variable, network, or child process is used.

### 3.2 Component (module) decomposition

All classes are in package `io.github.dialecticplayground.hello`.

| Component | Responsibility | Depends on |
| :-- | :-- | :-- |
| `DpHello` | Entry point: wires argument source, core, and output; sets the exit status | `WindowsCommandLine`, `Cli`, `ConsoleOutput` |
| `WindowsCommandLine` | Supplies the arguments as Unicode text (re-reads the UTF-16 command line on Windows) | Win32 via FFM |
| `Cli` | Pure core: arguments → `Outcome`; owns the help/version texts and the error-to-status mapping | `OptionParser`, `Validator`, `Greeting` |
| `OptionParser` | Left-to-right scan: syntax errors, help/version, recorded values, first operand | `ParsedArguments`, `OptionValue`, `UsageException` |
| `Validator` | Operand, name, and count checks on the winning values → `Config` | `Config`, `UsageException` |
| `Greeting` | Pure greeting construction | `Config` |
| `ConsoleOutput` | Encodes an `Outcome` as UTF-8 and writes it to byte streams | — |
| Data types | `ParsedArguments`, `OptionValue`, `Config`, `Outcome`, `UsageException` | — |

### 3.3 Data flow
1. `DpHello.main` passes the JVM's `String[]` to `WindowsCommandLine.arguments`, which returns
   it unchanged except on Windows (§5).
2. `Cli.run` calls `OptionParser.parse`. A syntax error throws `UsageException`; `--help` or
   `--version` ends the scan immediately; otherwise the last name and count values and the
   first operand are returned as `ParsedArguments`.
3. For a greeting, `Validator.validate` checks the operand, then the name, then the count, and
   returns a `Config` or throws `UsageException`.
4. `Greeting.text(config)` builds the complete standard output.
5. `Cli.run` returns an `Outcome`; `ConsoleOutput.emit` writes it, and `DpHello` exits with its
   status. Nothing is written before the outcome is complete (FR-11).

## 4. Key design decisions

| # | Decision | Alternatives considered | Rationale |
| :-- | :-- | :-- | :-- |
| D1 | Pure `Cli.run(List<String>) → Outcome` core with a thin I/O shell | Writing to `System.out` from the logic | End-to-end behavior is unit-testable without processes; FR-11 holds by construction |
| D2 | Usage errors as a checked `UsageException` (no stack trace captured) | Sealed result types; unchecked exceptions | Idiomatic Java; the compiler forces handling; a single catch site in `Cli` |
| D3 | Values recorded during the scan, validated afterwards | Validating values as they are read | CONV-12 (v1.4.0): only syntax errors compete with `--help`/`--version` |
| D4 | Build the whole output in memory, then write once | Streaming line by line | At most 1000 short lines (a few KiB); one write keeps error atomicity simple |
| D5 | Executable jar with a `#!/usr/bin/env -S … java -jar` line prepended | `#!/bin/sh` wrapper with a baked path; jlink/jpackage image; GraalVM native image | One self-contained file; `java -jar` finds the zip directory from the end, so it tolerates the prefix; the runner resolves `env -S` on Windows |
| D6 | The POSIX launcher sets `LC_ALL=C.UTF-8` | Reading `/proc/self/cmdline` | The JVM decodes argv with the locale's charset (ASCII under the runner's empty environment); setting the locale keeps NFR-07 (no file reads) |
| D7 | Windows: re-read `GetCommandLineW` through FFM | Accept the ANSI code page; a native helper binary | The java launcher decodes the command line with the ANSI code page; FFM needs no extra binaries |
| D8 | Maven Wrapper pinned to the container's Maven 3.10.0; every plugin version pinned; the Enforcer checks the Maven and Java lines | The runners' preinstalled Maven | Identical builds in the container and on every native OS |

## 5. Cross-platform strategy

- **Line endings (CONV-03):** all text uses `\n` literally; `ConsoleOutput` writes bytes to
  `FileOutputStream(FileDescriptor.out/err)`, which performs no newline translation on any OS.
- **Encoding (CONV-05):** output is always `String.getBytes(UTF_8)`, never the platform charset.
- **Arguments on Linux and macOS:** the launcher line `#!/usr/bin/env -S LC_ALL=C.UTF-8 java
  -jar` makes the JVM decode argv as UTF-8 whatever the caller's locale (on macOS the JVM always
  uses UTF-8). The installed `#!/bin/sh` launcher exports the same variable.
- **Arguments on Windows:** `WindowsCommandLine`, the only platform-specific module, calls
  `GetCommandLineW` and `CommandLineToArgvW` through FFM and keeps the last N entries, where
  N is the number of arguments the JVM passed to `main`. It falls back to the JVM's arguments
  if the call fails or the two splits disagree. `Zoë` already survives the runners' default
  code page 1252; this also preserves characters outside it (Windows-only unit test). The jar
  manifest declares `Enable-Native-Access: ALL-UNNAMED`, so the JVM prints no warning.
- **Paths (CONV-19 … CONV-21):** not applicable; the tool handles no paths.

## 6. Error handling and diagnostics

Syntax errors (`OptionParser`) and value or operand errors (`Validator`) throw
`UsageException`, whose message is the CONV-13 text without the program prefix. `Cli.run` is
the single place that turns it into an `Outcome`: empty stdout, `dp-hello: <message>` and the
CONV-14 line on stderr, status 2. A failed write (EPIPE, CONV-25) is caught in
`ConsoleOutput.emit`, which returns status 141 without a diagnostic. No other exception can arise
from any argument list, so no stack trace is ever printed (CONV-24).

## 7. Non-functional design

| Quality attribute | SRS reference | How the design achieves it |
| :-- | :-- | :-- |
| Performance / memory | NFR-02, CONV-31 | No dependencies, a dozen small classes, one write. Measured ~45 ms per run and ~2.3 MiB peak RSS above the JVM baseline (master test plan §4.4) |
| Portability | NFR-01 | Raw byte streams, literal LF, UTF-8 encoding; argument handling per §5 |
| Security / input validation | NFR-07 | No file, environment, or process APIs; only the process's own command line is read |
| Maintainability | NFR-04, NFR-05 | One responsibility per class; parser, validator, greeting, and core are pure |
| Build | NFR-06 | `make all install uninstall test check lint format clean distclean` through Maven hooks |

## 8. Risks and open issues

| Risk / issue | Likelihood | Impact | Mitigation |
| :-- | :-- | :-- | :-- |
| Invalid UTF-8 bytes in a POSIX argument become U+FFFD in the JVM | L | L | Documented limitation; CONV-05 defines arguments as UTF-8 text |
| `C.UTF-8` locale missing on an old Linux system | L | M | glibc ≥ 2.35 builds it in; macOS does not need it |
| The Windows java launcher splits the command line differently from `CommandLineToArgvW` (for example its wildcard expansion) | L | L | `reconcile` detects the mismatch and keeps the JVM's arguments |
| JVM start-up time on slow runners | M | L | Measured far below the 300 ms interpreted-runtime allowance |

## 9. Approval

| Role | Name | Date | Decision |
| :-- | :-- | :-- | :-- |
| Author | Brandon Perkins (with Claude) | 2026-10-13 | Submitted |
| Reviewer | Pull-request review and CI | 2026-10-13 | Approved when CI is green |
