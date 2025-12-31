#!/usr/bin/env bash
set -e
#
# This script is used to run the fullstack server
#
./scripts/setup.sc -- app

docker-compose up -d

INIT=FullStack sbt -mem 4096 "server/run"
