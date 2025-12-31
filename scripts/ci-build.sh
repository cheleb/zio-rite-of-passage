#!/usr/bin/env bash
set -e

./scripts/setup.sc -- app

INIT=Docker sbt -mem 4096 "server/compile"

cd modules/app

npm run build

