#!/usr/bin/env bash
set -e
#
# This script is used to run the fullstack server
#
INIT=FullStack sbt -mem 4096 "server/run"
