# Toolchain image for dialectic-playground/java: Java (container-first; see tools/dp-run).
#
# Java 25 (LTS, Eclipse Temurin); build and package tool: Maven.
# Official base image pinned by multi-architecture index digest, plus make and python3 (for the
# curriculum's black-box runner). Dependabot (docker) refreshes the digest; moving to a new
# release line is a deliberate change (keep `make toolchain` and the native CI in step).
FROM docker.io/library/maven:3-eclipse-temurin-25@sha256:0396dcd8cd0d46a0d2026449b714b2a5bbe53cce030975b5a41f8ffa3f5f0525

RUN apt-get update \
    && apt-get install -y --no-install-recommends make python3 \
    && rm -rf /var/lib/apt/lists/*

ENV MAVEN_CONFIG=/dp-cache/home/.m2 \
    DP_IN_CONTAINER=1
