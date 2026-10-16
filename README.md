<!-- START doctoc generated TOC please keep comment here to allow auto update -->
<!-- DON'T EDIT THIS SECTION, INSTEAD RE-RUN doctoc TO UPDATE -->

**Table of Contents** _generated with [DocToc](https://github.com/thlorenz/doctoc)_

- [java](#java)
  - [Java at a glance](#java-at-a-glance)
  - [Toolchain](#toolchain)
    - [Running the tools](#running-the-tools)
  - [Layout](#layout)
  - [Development](#development)
  - [Implementing a project](#implementing-a-project)
  - [Contributing](#contributing)
  - [License](#license)

<!-- END doctoc generated TOC please keep comment here to allow auto update -->

<p align="center">
  <picture>
    <source media="(prefers-color-scheme: dark)" srcset="https://raw.githubusercontent.com/dialectic-playground/.github/main/assets/java/dist/logo-dark.svg">
    <img alt="dialectic playground / java" src="https://raw.githubusercontent.com/dialectic-playground/.github/main/assets/java/dist/logo.svg" height="96">
  </picture>
</p>

# java

Java implementation of the Dialectic Playground SDLC curriculum.

📖 **Documentation:** <https://dialectic-playground.github.io/docs/reference/languages/java/>

This repository implements the fifty-three projects of the
[SDLC curriculum](https://github.com/dialectic-playground/sdlc-curriculum) in Java. Each project is
specified to the byte by the curriculum and verified by its black-box tests on Linux, macOS,
and Windows.

**Status:** Implemented projects: 00-hello-world.

## Java at a glance

| Aspect                  | Java                                                     |
| :---------------------- | :------------------------------------------------------- |
| File extension          | `.java`                                                  |
| CLI classification      | Included                                                 |
| Image category          | First-Class                                              |
| Access to OS primitives | Medium-High (abstracted via ProcessBuilder)              |
| Entry-point mechanism   | JVM host maps system args into public static void main() |

## Toolchain

|                      |                                                                                                              |
| :------------------- | :----------------------------------------------------------------------------------------------------------- |
| Release line         | Java 25 (LTS, Eclipse Temurin)                                                                               |
| Build / package tool | Maven                                                                                                        |
| Container image      | `maven:3-eclipse-temurin-25`, pinned by digest ([`Containerfile`](Containerfile))                            |
| Native CI            | actions/setup-java (Temurin 25, Maven repository cache)                                                      |
| Maven                | 3.10.0 via each project's Maven Wrapper (`./mvnw`, checksum-pinned), the same version as the container image |
| Unit tests           | JUnit 5 (Jupiter), run by Maven Surefire                                                                     |
| Lint                 | `javac -Xlint:all -Werror` and Spotless `check` (google-java-format 1.36.1)                                  |
| Format               | Spotless `apply` (google-java-format 1.36.1)                                                                 |

`make toolchain` verifies that the container and native toolchains are on this release line.
Each project's `pom.xml` pins every plugin version (Maven's equivalent of a lockfile), and the
Maven Enforcer fails the build on any other Maven or Java release line, so the container and
native builds use identical tools. The container image adds `unzip` (the wrapper verifies the
checksum of the `.zip` Maven distribution) and points Maven's `user.home` at the writable cache.

### Running the tools

`make all` leaves each tool in the project's `target/bin/` as an executable jar with a `#!`
line prepended (`#!/usr/bin/env -S LC_ALL=C.UTF-8 java -jar` on Linux and macOS, so the JVM
decodes UTF-8 arguments whatever the caller's locale; `#!/usr/bin/env -S java -jar` on
Windows). `make install` copies the jar to `$(LIBDIR)/java/<project>/` and writes a `#!/bin/sh`
launcher to `$(BINDIR)`; `java` (25) must be on `PATH`. On Windows, run the tools from Git
Bash or MSYS2, which honor `#!` lines. The programs re-read the UTF-16 command line on Windows,
so non-ASCII arguments arrive intact even outside the ANSI code page.

## Layout

| Path                                 | Contents                                                                                                          |
| :----------------------------------- | :---------------------------------------------------------------------------------------------------------------- |
| `00-hello-world/` … `52-spacecraft/` | One workspace per curriculum project: `Makefile` (with `DP_*` hooks), HLD, LLD, RTM, master test plan, and `src/` |
| `sdlc-curriculum/`                   | The curriculum, as a git submodule pinned to a release tag                                                        |
| `Makefile`                           | `make check` (repository, toolchain, implemented projects), `toolchain`, `projects`, `roundtrip`                  |
| `Containerfile`, `.devcontainer/`    | The toolchain image and the matching dev container                                                                |
| `tools/dp-run`, `tools/container.mk` | Shared container tooling (copies of the canonical files in `dialectic-playground/.github`)                        |
| `.github/workflows/ci.yml`           | CI: container and native (Linux, macOS, Windows) `make` targets, shared-tooling check, DCO                        |

## Development

Container-first: `make` targets and `tools/dp-run <command>` run in the toolchain container
via Podman, then Docker, then the local system. `DP_RUNTIME=podman|docker|local` overrides
the choice.

```sh
git clone --recurse-submodules https://github.com/dialectic-playground/java.git
cd java
make check                    # repository, toolchain, and every implemented project
DP_RUNTIME=local make check   # the same with your native toolchain
make -C 00-hello-world help   # a project's configuration and hooks
```

## Implementing a project

1. Complete the project's `hld.md` and `lld.md` before writing code.
2. Write the implementation and its unit tests under the project's `src/` (Maven layout:
   `src/main/java`, `src/test/java`), copy `pom.xml`, `mvnw`, and `.mvn/` from
   `00-hello-world`, and fill in `BIN_DIR` and the `DP_*` hooks in the project's `Makefile`.
3. Pass `make -C <project> test` (container) and `DP_RUNTIME=local make -C <project> test`.
4. Add the project directory to `IMPLEMENTED` in the root `Makefile`. CI then builds, lints,
   and tests it in the container and natively on all three operating systems.
5. Keep `rtm.md` and `master-test-plan.md` current.

Specifications: [curriculum reference](https://dialectic-playground.github.io/docs/reference/curriculum/). Language setup:
[Set up a language repository](https://dialectic-playground.github.io/docs/how-to/set-up-a-language-repository/).

## Contributing

See the [organization contributing guide](https://github.com/dialectic-playground/.github/blob/main/CONTRIBUTING.md).
Pull requests are squash-merged, titles follow Conventional Commits, and every commit needs a
DCO sign-off.

## License

[MIT](LICENSE)
