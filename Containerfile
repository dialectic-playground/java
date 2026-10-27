# Toolchain image for dialectic-playground/java: Java (container-first; see tools/dp-run).
#
# Java 25 (LTS, Eclipse Temurin); build and package tool: Maven.
# Official base image pinned by multi-architecture index digest, plus make and python3 (for the
# curriculum's black-box runner) and unzip (the Maven Wrapper verifies the checksum of the .zip
# distribution and falls back to the .tar.gz without it). Dependabot (docker) refreshes the digest; moving to a new
# release line is a deliberate change (keep `make toolchain` and the native CI in step).
FROM docker.io/library/maven:3-eclipse-temurin-25@sha256:9b5f82b7166db6112eb123ba310a9e010ec3fe95022ac0728f491121563dfbab

# Debian keeps only the current version of each package, so apt versions cannot be pinned
# reproducibly; the base image is pinned by digest instead.
# hadolint ignore=DL3008
RUN apt-get update \
    && apt-get install -y --no-install-recommends make python3 unzip \
    && rm -rf /var/lib/apt/lists/*

# The JVM takes user.home from the passwd entry, which `podman --userns=keep-id` points at the
# working directory; MAVEN_OPTS keeps Maven's ~/.m2 (local repository, wrapper distributions)
# in the writable cache instead of the project.
ENV MAVEN_CONFIG=/dp-cache/home/.m2 \
    MAVEN_OPTS=-Duser.home=/dp-cache/home \
    DP_IN_CONTAINER=1
