#!/usr/bin/env bash

. ./scripts/target/build-env.sh

echo "Starting npm dev server for client"
echo " * SCALA_VERSION=$SCALA_VERSION"
rm -f $MAIN_JS_PATH
touch $NPM_DEV_PATH

sleep 3

cd modules/app
npm run dev
