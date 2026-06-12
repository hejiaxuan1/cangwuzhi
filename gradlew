#!/bin/sh

GRADLE_DIST="/c/Users/16333/.gradle/wrapper/dists/gradle-8.7-bin/bhs2wmbdwecv87pi65oeuq5iu/gradle-8.7/bin/gradle"

if [ ! -x "$GRADLE_DIST" ]; then
  echo "ERROR: Local Gradle 8.7 distribution was not found at:"
  echo "$GRADLE_DIST"
  exit 1
fi

exec "$GRADLE_DIST" "$@"
