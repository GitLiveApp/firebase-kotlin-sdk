#!/usr/bin/env bash
# Runs ConsoleCheckAndroidTest twice on the connected emulator with the APK installed once, so the second launch
# finalizes and uploads the session the first one recorded (the emulator-runner action runs its script line by line,
# hence this file).
set -euxo pipefail
APK=$(find firebase-crashlytics/build/outputs/apk -name "*androidTest*.apk" | head -1)
ls -la "$APK"
adb install -r "$APK"
adb shell setprop log.tag.FirebaseCrashlytics VERBOSE
for run in first second; do
  adb logcat -c
  adb shell am instrument -w -e class dev.gitlive.firebase.crashlytics.ConsoleCheckAndroidTest dev.gitlive.firebase.crashlytics.test/androidx.test.runner.AndroidJUnitRunner || true
  echo "===== $run run logcat"
  adb logcat -d | grep -i "console-check\|FirebaseCrashlytics\|AndroidRuntime" | tail -250 || true
done
