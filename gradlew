#!/bin/bash
GRADLE_USER_HOME="${GRADLE_USER_HOME:-${HOME}/.gradle}"
GRADLE_HOME="${GRADLE_USER_HOME}/wrapper/dists/gradle-8.5-bin"
GRADLE="${GRADLE_HOME}/gradle-8.5/bin/gradle"

if [ ! -f "$GRADLE" ]; then
    echo "Gradle not found, downloading..."
    mkdir -p /tmp/gradle-download
    cd /tmp/gradle-download
    curl -sL https://services.gradle.org/distributions/gradle-8.5-bin.zip -o gradle.zip
    unzip -q gradle.zip
    mkdir -p "${GRADLE_HOME}"
    cp -r gradle-8.5/* "${GRADLE_HOME}/gradle-8.5/"
fi

exec "$GRADLE" "$@"
