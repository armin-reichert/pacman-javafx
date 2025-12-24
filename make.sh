#!/bin/sh

# --warning-mode=(all,none,summary)

if [ "$1" = "clean" ]; then
  ./gradlew --info --warning-mode summary clean jpackage
else
  ./gradlew --info --warning-mode summary jpackage
fi

