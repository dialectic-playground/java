# 00-hello-world: Standard Terminal Hello World, in Java 25.
#
# Extends the universal build contract in shared/templates/BASE-MAKEFILE:
#
#   make all | install | uninstall | test | check | lint | format | clean | distclean
#   make test-hidden | grade | help
#
# Toolchain: Java 25 and Maven 3.10.0 through the Maven Wrapper (./mvnw, pinned with a
# checksum in .mvn/wrapper/maven-wrapper.properties), so the container and the native builds
# use the same Maven. pom.xml pins every plugin version; sources are in src/main/java, JUnit 5
# tests in src/test/java. Lint is javac -Xlint:all -Werror plus Spotless (google-java-format).
#
# Build: `mvn package` makes the executable jar target/dp-hello.jar. The launcher
# target/bin/dp-hello (BIN_DIR) is that jar with a `#!/usr/bin/env -S ... java -jar` line
# prepended (the zip format tolerates a prefix). On Linux and macOS the line also sets
# LC_ALL=C.UTF-8, because the JVM decodes argv with the locale's charset and the black-box
# runner passes no locale; on Windows (where the runner resolves the #! line itself) it does
# not, and the program re-reads the UTF-16 command line instead (WindowsCommandLine).
#
# Install: the jar goes to $(LIBDIR)/java/00-hello-world/dp-hello.jar, and a `#!/bin/sh`
# launcher that runs it with `java -jar` (absolute path baked in) goes to $(BINDIR)/dp-hello.
# Uninstall removes both.
#
# Container-first: goals run in the toolchain container built from the repository's
# Containerfile (podman, then docker, then local; override with DP_RUNTIME).

PROJECT := 00-hello-world
TOOLS   := dp-hello

# Path to the sdlc-curriculum checkout (the specs, test suites, and runner live there).
CURRICULUM_ROOT ?= ../sdlc-curriculum

MAVEN ?= ./mvnw --batch-mode --no-transfer-progress
JAR := target/dp-hello.jar

# Directory that contains the built executables after `make all`.
BIN_DIR := target/bin

ifeq ($(OS),Windows_NT)
  SHEBANG := \#!/usr/bin/env -S java -jar
else
  SHEBANG := \#!/usr/bin/env -S LC_ALL=C.UTF-8 java -jar
endif

# Where `make install` puts the payload (see the header).
INSTALL_DIR = $(LIBDIR)/java/$(PROJECT)

define DP_BUILD
$(MAVEN) --quiet package -DskipTests
mkdir -p $(BIN_DIR)
{ printf '%s\n' '$(SHEBANG)'; cat $(JAR); } > $(BIN_DIR)/dp-hello
chmod +x $(BIN_DIR)/dp-hello
endef

DP_UNIT_TEST = $(MAVEN) test

DP_LINT = $(MAVEN) --quiet spotless:check test-compile

DP_FORMAT = $(MAVEN) --quiet spotless:apply

# The launcher exports LC_ALL for the JVM's argv decoding (see the header) and
# MSYS2_ARG_CONV_EXCL so that Git Bash on Windows passes arguments to java.exe unconverted.
define DP_INSTALL
rm -rf "$(INSTALL_DIR)"
mkdir -p "$(INSTALL_DIR)" "$(BINDIR)"
cp $(JAR) "$(INSTALL_DIR)/dp-hello.jar"
printf '#!/bin/sh\nLC_ALL=C.UTF-8 MSYS2_ARG_CONV_EXCL="*"\nexport LC_ALL MSYS2_ARG_CONV_EXCL\nexec java -jar %s "$$@"\n' \
	"'$(INSTALL_DIR)/dp-hello.jar'" > "$(BINDIR)/dp-hello"
chmod +x "$(BINDIR)/dp-hello"
"$(BINDIR)/dp-hello" --version
endef

define DP_UNINSTALL
rm -f "$(BINDIR)/dp-hello"
rm -rf "$(INSTALL_DIR)"
rmdir "$(LIBDIR)/java" "$(LIBDIR)" 2>/dev/null || true
endef

DP_CLEAN = rm -rf target

# Maven keeps its dependency cache and wrapper distributions in ~/.m2, outside the project,
# so there is nothing more to remove here.
DP_DISTCLEAN = rm -rf target

include $(CURRICULUM_ROOT)/shared/templates/BASE-MAKEFILE
