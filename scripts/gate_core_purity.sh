#!/usr/bin/env bash
# :launchcore 纯净度门禁 —— core 模块禁止出现任何 Android UI 依赖。
# 设计决策见 PROJECT.md「架构决策 ADR-4」。CI 与本地都可执行：
#   bash scripts/gate_core_purity.sh
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
SRC="$ROOT/launchcore/src"
BUILD_FILE="$ROOT/launchcore/build.gradle.kts"

fail=0

violations=$(grep -rnE \
  "import (android\.widget\.Toast|android\.app\.Activity|androidx\.activity|androidx\.compose)" \
  "$SRC" 2>/dev/null || true)
if [ -n "$violations" ]; then
  echo "::error title=launchcore purity gate::Android UI imports found in :launchcore"
  echo "$violations"
  fail=1
fi

if grep -qE "kotlin\.compose|compose[[:space:]]*=[[:space:]]*true" "$BUILD_FILE" 2>/dev/null; then
  echo "::error title=launchcore purity gate::Compose must never be enabled in :launchcore"
  fail=1
fi

if [ "$fail" -ne 0 ]; then
  echo "launchcore purity gate: FAILED (see PROJECT.md ADR-4)"
  exit 1
fi

echo "launchcore purity gate: OK"
