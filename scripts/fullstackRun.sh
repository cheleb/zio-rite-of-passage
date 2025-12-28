#!/usr/bin/env bash
set -e
#
# This script is used to run the fullstack server
#  See getoptions.sh for the command line options
#    or run `./scripts/fullstackRun.sh -h`
#

# Import the project environment variables

MOD=FullStack sbt -mem 4096 "server/run"
