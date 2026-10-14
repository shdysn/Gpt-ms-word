#!/usr/bin/env bash
# 300-Line Codebase Limit & Anti-Regression Enforcer Script
set -e

DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT_DIR="$(dirname "$DIR")"

cd "$ROOT_DIR"
python3 "$DIR/check_file_limits.py"
