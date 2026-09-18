#!/usr/bin/env bash
# Build sideloadable Android APKs for sandglass. Usage: scripts/build-release.sh [version]
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"

VERSION="${1:-}"
if [[ -z "$VERSION" ]]; then
  VERSION="$(git describe --tags --always --dirty 2>/dev/null || true)"
fi
if [[ -z "$VERSION" ]]; then
  VERSION="dev"
fi
if [[ "$VERSION" != v* && "$VERSION" != dev* ]]; then
  VERSION="v${VERSION}"
fi

OUT="${ROOT}/dist"
rm -rf "$OUT"
mkdir -p "$OUT"

chmod +x gradlew
./gradlew assembleDebug --stacktrace

APK_DIR="${ROOT}/app/build/outputs/apk/debug"
UNIVERSAL=""
ARM64=""

if [[ -f "${APK_DIR}/app-universal-debug.apk" ]]; then
  UNIVERSAL="${APK_DIR}/app-universal-debug.apk"
elif [[ -f "${APK_DIR}/app-debug.apk" ]]; then
  UNIVERSAL="${APK_DIR}/app-debug.apk"
fi

if [[ -f "${APK_DIR}/app-arm64-v8a-debug.apk" ]]; then
  ARM64="${APK_DIR}/app-arm64-v8a-debug.apk"
fi

if [[ -z "$UNIVERSAL" ]]; then
  echo "No debug APK found under ${APK_DIR}" >&2
  find "${ROOT}/app/build/outputs" -name '*.apk' -print >&2 || true
  exit 1
fi

cp "$UNIVERSAL" "${OUT}/sandglass-${VERSION}-android-universal-debug.apk"
if [[ -n "$ARM64" ]]; then
  cp "$ARM64" "${OUT}/sandglass-${VERSION}-android-arm64-v8a-debug.apk"
fi

(cd "$OUT" && sha256sum -- * > SHA256SUMS)
ls -la "$OUT"
