#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

JAVA_VERSION_OUTPUT="$(java -version 2>&1 | head -n 1 || true)"
JAVA_MAJOR="$(echo "$JAVA_VERSION_OUTPUT" | sed -E 's/.*version "([0-9]+).*/\1/' || true)"

if [[ -n "${JAVA_MAJOR}" ]] && (( JAVA_MAJOR > 17 )); then
  echo "Detected Java ${JAVA_MAJOR}, but this backend currently requires Java 17 for Gradle 7.6 compatibility."
  echo "Please switch your shell to Java 17 (set JAVA_HOME to a JDK 17 installation), then run again."
  echo "Current java -version: ${JAVA_VERSION_OUTPUT}"
  exit 1
fi

if [[ -f .env ]]; then
  set -a
  # shellcheck disable=SC1091
  source .env
  set +a
  echo "Loaded environment from backend/.env"
else
  echo "No backend/.env found. Using existing shell environment."
  echo "Tip: cp backend/.env.example backend/.env"
fi

if [[ -x ./gradlew ]]; then
  ./gradlew bootRun
else
  bash gradlew bootRun
fi
