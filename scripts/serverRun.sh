#!/usr/bin/env bash
set -e

#INIT=server sbt --client --batch -Dsbt.supershell=false '~server/reStart'
INIT=server sbt '~server/reStart'

