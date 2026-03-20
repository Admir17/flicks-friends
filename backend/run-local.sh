#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"


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
