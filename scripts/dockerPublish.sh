#!/usr/bin/env bash
set -e
# Import the project environment variables

./scripts/setup.sc

INIT=Docker sbt -mem 4096 "server/Docker/publish"
