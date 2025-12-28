#!/usr/bin/env bash

. ./scripts/target/build-env.sh

echo -n "Waiting for dev server to start."

until [ -e $SERVER_DEV_PATH ]; do
    echo -n "."
    sleep 2
done

echo ✅

echo "Starting npm dev server for client"
echo " * SCALA_VERSION=$SCALA_VERSION"
rm -f $MAIN_JS_PATH
touch $NPM_DEV_PATH

cd modules/app
npm run dev
