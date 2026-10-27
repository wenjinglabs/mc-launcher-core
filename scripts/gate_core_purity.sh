#!/usr/bin/env bash
# :launchcore 纯净度门禁 —— core 模块禁止出现任何 Android UI 依赖。
# ADR-4/ADR-14：androidx.compose.runtime（纯状态库，无渲染）允许；
# compose.ui / foundation / material / material3 / animation 等渲染层全禁。
# CI 与本地都可执行：bash scripts/gate_core_purity.sh
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
SRC="$ROOT/launchcore/src"
BUILD_FILE="$ROOT/launchcore/build.gradle.kts"

fail=0

ui_imports=$(grep -rnE \
  "import (android\.widget\.Toast|android\.app\.Activity[[:space:]]*$|androidx\.activity)" \
  "$SRC" 2>/dev/null || true)
if [ -n "$ui_imports" ]; then
  echo "::error title=launchcore purity gate::Android UI imports found in :launchcore"
  echo "$ui_imports"
  fail=1
fi

# compose：允许 runtime，禁其余全部渲染层
compose_violations=$(grep -rnE "import androidx\.compose" "$SRC" 2>/dev/null \
  | grep -vE "import androidx\.compose\.runtime" || true)
if [ -n "$compose_violations" ]; then
  echo "::error title=launchcore purity gate::Non-runtime compose imports found (only androidx.compose.runtime is allowed, see ADR-14)"
  echo "$compose_violations"
  fail=1
fi

if grep -qE "kotlin\.compose|compose[[:space:]]*=[[:space:]]*true" "$BUILD_FILE" 2>/dev/null; then
  echo "::error title=launchcore purity gate::Compose must never be enabled in :launchcore"
  fail=1
fi

if [ "$fail" -ne 0 ]; then
  echo "launchcore purity gate: FAILED (see PROJECT.md ADR-4/ADR-14)"
  exit 1
fi

echo "launchcore purity gate: OK"
