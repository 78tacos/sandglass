#!/usr/bin/env bash
# Install Android SDK Platform 37.0 on GitHub-hosted runners.
# sdkmanager publishes API 37 as platforms;android-37.0, while AGP compileSdk = 37
# still looks for platforms/android-37. See:
# https://github.com/actions/runner-images/issues/13859
set -euo pipefail

: "${ANDROID_HOME:=/usr/local/lib/android/sdk}"
export ANDROID_HOME ANDROID_SDK_ROOT="$ANDROID_HOME"

if [[ -n "${GITHUB_ENV:-}" ]]; then
  echo "ANDROID_HOME=$ANDROID_HOME" >> "$GITHUB_ENV"
  echo "ANDROID_SDK_ROOT=$ANDROID_HOME" >> "$GITHUB_ENV"
fi

SDKMANAGER="$(find "$ANDROID_HOME/cmdline-tools" -name sdkmanager -type f | sort | tail -1)"
if [[ -z "$SDKMANAGER" ]]; then
  echo "sdkmanager not found under $ANDROID_HOME" >&2
  ls -la "$ANDROID_HOME" >&2 || true
  exit 1
fi

set +o pipefail
yes | "$SDKMANAGER" --licenses >/dev/null
set -o pipefail

"$SDKMANAGER" "platforms;android-37.0" "platform-tools"

mkdir -p "$ANDROID_HOME/platforms"
if [[ -d "$ANDROID_HOME/platforms/android-37.0" && ! -e "$ANDROID_HOME/platforms/android-37" ]]; then
  ln -s android-37.0 "$ANDROID_HOME/platforms/android-37"
fi
