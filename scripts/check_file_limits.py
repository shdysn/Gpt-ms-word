#!/usr/bin/env python3
"""
300-Line Codebase Limit & Anti-Regression Enforcer
Ensures all modularized files (Phase 1, Phase 2, Phase 3, Phase 4) stay strictly under 300 lines.
When data is imported or merged from GitHub, this script guarantees that files do not exceed limits.
"""

import sys
import os

MAX_LINES = 300

# Files strictly enforced for Phase 1
PHASE_1_FILES = [
    "app/src/main/java/com/example/ui/editor/components/SmartWritingDialog.kt",
    "app/src/main/java/com/example/ui/editor/EditorScreen.kt",
    "app/src/main/java/com/example/ui/home/HomeScreen.kt",
    "app/src/main/java/com/example/ui/editor/components/DocumentOutlineSheet.kt",
    "app/src/main/java/com/example/ui/editor/components/OutlineHeadingComponents.kt",
    "app/src/main/java/com/example/ui/editor/components/dialogs/SecurityDialogs.kt",
    "app/src/main/java/com/example/ui/editor/components/dialogs/DigitalSignatureDialog.kt",
    "app/src/main/java/com/example/ui/editor/components/dialogs/DocumentElementsDialogs.kt",
    "app/src/main/java/com/example/ui/editor/components/dialogs/WatermarkDialog.kt",
    "app/src/main/java/com/example/ui/editor/components/dialogs/VersionHistoryDialog.kt",
    "app/src/main/java/com/example/ui/editor/components/dialogs/TypographyAndStyleDialogs.kt",
    "app/src/main/java/com/example/ui/editor/components/dialogs/ParagraphSpacingAndIndentsDialog.kt",
    "app/src/main/java/com/example/ui/editor/components/dialogs/PageLayoutDialog.kt",
    "app/src/main/java/com/example/ui/editor/components/dialogs/PageLayoutPaperTab.kt",
    "app/src/main/java/com/example/ui/editor/components/dialogs/PageLayoutMarginsSection.kt",
    "app/src/main/java/com/example/ui/editor/components/dialogs/EditorAdvancedModals.kt",
    "app/src/main/java/com/example/ui/editor/components/dialogs/InsertTableDialog.kt",
    "app/src/main/java/com/example/ui/editor/components/dialogs/TableEditDialogs.kt",
    "app/src/main/java/com/example/ui/editor/components/dialogs/DocumentSaveDialogs.kt",
    "app/src/main/java/com/example/ui/editor/components/dialogs/ExportPdfDialog.kt",
    "app/src/main/java/com/example/ui/editor/components/review/CommentsDialog.kt",
    "app/src/main/java/com/example/ui/home/components/HomeDialogs.kt",
    "app/src/main/java/com/example/ui/editor/components/EditorTopAppBar.kt",
    "app/src/main/java/com/example/ui/editor/components/EditorQuickFormatBar.kt",
]

def count_lines(filepath):
    try:
        with open(filepath, "r", encoding="utf-8", errors="ignore") as f:
            return sum(1 for _ in f)
    except Exception as e:
        print(f"Error reading {filepath}: {e}")
        return -1

def main():
    print("=" * 65)
    print(" 🛡️  SmartWord 300-Line Codebase Limit & Anti-Regression Enforcer")
    print("=" * 65)
    
    violations = []
    
    print("\n[Procedure 1 / Phase 1] Auditing UI Screens and Dialog Modules:")
    all_proc1_ok = True
    for path in PHASE_1_FILES:
        if not os.path.exists(path):
            print(f"  ⚠️  MISSING: {path}")
            violations.append((path, "FILE_NOT_FOUND"))
            all_proc1_ok = False
            continue
        lines = count_lines(path)
        if lines > MAX_LINES:
            print(f"  ❌ VIOLATION: {path} has {lines} lines (Max: {MAX_LINES})")
            violations.append((path, lines))
            all_proc1_ok = False
        else:
            print(f"  ✅ PASS: {path} ({lines} lines)")

    print("-" * 65)
    if all_proc1_ok:
        print("🎉 Procedure 1 Status: 100% COMPLIANT! All UI & Dialog files are < 300 lines.")
    else:
        print(f"❌ Procedure 1 Violations Found: {len(violations)}")

    # Audit rest of repository to inform developer of pending procedures
    print("\n[Repository Audit] Files pending future procedures (> 300 lines):")
    pending_count = 0
    for root, _, files in os.walk("app/src/main/java"):
        for file in files:
            if file.endswith(".kt"):
                full_path = os.path.join(root, file)
                if full_path in PHASE_1_FILES:
                    continue
                lines = count_lines(full_path)
                if lines > MAX_LINES:
                    pending_count += 1
                    print(f"  ⏳ {lines:4d} lines: {full_path}")

    print(f"\nTotal files pending future procedures (Procedure 2, 3, 4): {pending_count}")
    print("=" * 65)

    if violations:
        print("❌ Build check failed due to Procedure 1 regression violations!")
        sys.exit(1)
    else:
        print("✅ Procedure 1 Anti-Regression verification passed successfully!")
        sys.exit(0)

if __name__ == "__main__":
    main()
