#!/bin/bash
set -e
export JAVA_HOME=${JAVA_HOME:-/opt/homebrew/opt/openjdk@17}
export ANDROID_HOME=${ANDROID_HOME:-$HOME/Library/Android/sdk}

if [ -f "./gradlew" ]; then
    ./gradlew clean --no-daemon 2>/dev/null || true
fi

rm -rf build
rm -rf app/build
rm -rf .gradle
rm -rf /tmp/icon_lib
rm -rf *.apk
