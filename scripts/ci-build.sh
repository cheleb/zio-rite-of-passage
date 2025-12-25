#!/usr/bin/env bash
set -e
# Import the project environment variables

./scripts/setup.sc

MOD=Docker sbt -mem 4096 "server/compile"
cd modules/app

npm run build

