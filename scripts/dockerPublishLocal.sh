#!/usr/bin/env bash
set -e
# Import the project environment variables

./scripts/setup.sc

MOD=Docker sbt "server/Docker/publishLocal"
