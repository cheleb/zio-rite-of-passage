#!/usr/bin/env bash
set -e
# Import the project environment variables

./scripts/setup.sc

INIT=Docker sbt "server/Docker/publishLocal"
