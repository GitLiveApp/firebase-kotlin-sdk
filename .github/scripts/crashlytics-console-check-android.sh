#!/usr/bin/env bash
# Runs ConsoleCheckAndroidTest twice on the connected emulator with the APK installed once, so the second launch
# finalizes and uploads the session the first one recorded (the emulator-runner action runs its script line by line,
# hence this file). Crashlytics hands non-fatal reports to Google DataTransport, which uploads them from a scheduled
# job some time later, so the emulator is kept alive afterwards and the job is forced to run.
set -euxo pipefail
PKG=dev.gitlive.firebase.crashlytics.test
APK=$(find firebase-crashlytics/build/outputs/apk -name "*androidTest*.apk" | head -1)
ls -la "$APK"
adb install -r "$APK"
adb shell setprop log.tag.FirebaseCrashlytics VERBOSE
for tag in CctTransportBackend Uploader JobInfoScheduler SQLiteEventStore DefaultScheduler; do
  adb shell setprop "log.tag.TransportRuntime.$tag" VERBOSE
done
for run in first second; do
  adb logcat -c
  adb shell am instrument -w -e class dev.gitlive.firebase.crashlytics.ConsoleCheckAndroidTest $PKG/androidx.test.runner.AndroidJUnitRunner || true
  echo "===== $run run logcat"
  adb logcat -d | grep -i "console-check\|FirebaseCrashlytics\|AndroidRuntime\|TransportRuntime" | tail -250 || true
  echo "===== $run run persisted session files (the test APK is debuggable, so run-as can read them)"
  for f in $(adb shell run-as $PKG find files/.crashlytics.v3 -type f | tr -d '\r'); do
    echo "== $f"
    adb shell run-as $PKG cat "$f" | head -c 6000 || true
    echo
  done
done
echo "===== forcing DataTransport's scheduled upload job"
for i in 1 2 3 4; do
  adb shell dumpsys jobscheduler | grep -o "JOB #u0a[0-9]*/[0-9]*: [0-9a-f]* $PKG/[^ ]*" || true
  for id in $(adb shell dumpsys jobscheduler | grep -o "JOB #u0a[0-9]*/[0-9]*: [0-9a-f]* $PKG/" | sed -E 's|JOB #u0a[0-9]*/([0-9]*):.*|\1|'); do
    adb shell cmd jobscheduler run -f $PKG "$id" || true
  done
  sleep 20
  if adb logcat -d | grep -q "TransportRuntime.*Status Code: 200"; then break; fi
done
echo "===== upload logcat"
adb logcat -d | grep -i "TransportRuntime\|FirebaseCrashlytics" | tail -120 || true
