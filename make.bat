@echo off
rem --warning-mode=(all,none,summary)

if "%1"=="clean" (
.\gradlew --stacktrace --info --warning-mode summary clean jpackage
) else (
.\gradlew --stacktrace --info --warning-mode summary jpackage
)

