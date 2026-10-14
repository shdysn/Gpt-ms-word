#!/usr/bin/env python3
"""
300-Line Codebase Limit & Anti-Regression Enforcer
Ensures all modularized files (Phase 1, Phase 2, Phase 3, Phase 4) stay strictly under 300 lines.
When data is imported or merged from GitHub, this script guarantees that files do not exceed limits.
"""

import sys
import os

MAX_LINES = 300

# Files strictly enforced for Phase 1 (UI Screens & Dialogs)
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

# Files strictly enforced for Phase 2 (Canvas, Toolbars, Blocks, Ribbon & Home VM)
PHASE_2_FILES = [
    "app/src/main/java/com/example/ui/editor/components/WordRibbon.kt",
    "app/src/main/java/com/example/ui/editor/components/WordRibbonContent.kt",
    "app/src/main/java/com/example/ui/editor/components/DrawingBlockView.kt",
    "app/src/main/java/com/example/ui/editor/components/blocks/DrawingCanvasArea.kt",
    "app/src/main/java/com/example/ui/editor/components/toolbar/ParagraphFormattingBar.kt",
    "app/src/main/java/com/example/ui/editor/components/toolbar/ParagraphFormattingDropdowns.kt",
    "app/src/main/java/com/example/ui/editor/components/toolbar/CharacterFormattingBar.kt",
    "app/src/main/java/com/example/ui/editor/components/toolbar/CharacterFormattingMenus.kt",
    "app/src/main/java/com/example/ui/editor/components/canvas/PaginatedSheetView.kt",
    "app/src/main/java/com/example/ui/editor/components/canvas/SinglePageSheet.kt",
    "app/src/main/java/com/example/ui/editor/components/canvas/PageHeaderFooter.kt",
    "app/src/main/java/com/example/ui/editor/components/canvas/PaginatedItemRenderer.kt",
    "app/src/main/java/com/example/ui/editor/components/canvas/PaginatedSpecialItemRenderer.kt",
    "app/src/main/java/com/example/ui/editor/components/blocks/ParagraphBlockView.kt",
    "app/src/main/java/com/example/ui/editor/components/blocks/ParagraphPrefixView.kt",
    "app/src/main/java/com/example/ui/editor/components/blocks/ParagraphPillsView.kt",
    "app/src/main/java/com/example/ui/editor/components/blocks/SpecialBlocksView.kt",
    "app/src/main/java/com/example/ui/editor/components/blocks/SignatureBlockItem.kt",
    "app/src/main/java/com/example/ui/home/HomeViewModel.kt",
    "app/src/main/java/com/example/ui/home/HomeState.kt",
    "app/src/main/java/com/example/ui/home/HomeDocumentFilter.kt",
]

def count_lines(filepath):
    try:
        with open(filepath, "r", encoding="utf-8", errors="ignore") as f:
            return sum(1 for _ in f)
    except Exception as e:
        print(f"Error reading {filepath}: {e}")
        return -1

def check_file_group(group_name, file_list):
    print(f"\n[{group_name}] Auditing files:")
    all_ok = True
    violations = []
    for path in file_list:
        if not os.path.exists(path):
            print(f"  ⚠️  MISSING: {path}")
            violations.append((path, "FILE_NOT_FOUND"))
            all_ok = False
            continue
        lines = count_lines(path)
        if lines > MAX_LINES:
            print(f"  ❌ VIOLATION: {path} has {lines} lines (Max: {MAX_LINES})")
            violations.append((path, lines))
            all_ok = False
        else:
            print(f"  ✅ PASS: {path} ({lines} lines)")
    return all_ok, violations

def main():
    print("=" * 65)
    print(" 🛡️  SmartWord 300-Line Codebase Limit & Anti-Regression Enforcer")
    print("=" * 65)
    
    proc1_ok, proc1_violations = check_file_group("Procedure 1 / Phase 1: UI Screens & Dialogs", PHASE_1_FILES)
    proc2_ok, proc2_violations = check_file_group("Procedure 2 / Phase 2: Canvas, Toolbars, Blocks & ViewModels", PHASE_2_FILES)
    
    total_violations = proc1_violations + proc2_violations
    
    print("-" * 65)
    if proc1_ok and proc2_ok:
        print("🎉 Procedure 1 & 2 Status: 100% COMPLIANT! All monitored files are < 300 lines.")
    else:
        print(f"❌ Violations Found: {len(total_violations)}")

    # Audit rest of repository to inform developer of pending procedures
    print("\n[Repository Audit] Files pending future procedures (> 300 lines):")
    pending_count = 0
    enforced_set = set(PHASE_1_FILES + PHASE_2_FILES)
    for root, _, files in os.walk("app/src/main/java"):
        for file in files:
            if file.endswith(".kt"):
                full_path = os.path.join(root, file)
                if full_path in enforced_set:
                    continue
                lines = count_lines(full_path)
                if lines > MAX_LINES:
                    pending_count += 1
                    print(f"  ⏳ {lines:4d} lines: {full_path}")

    print(f"\nTotal files pending future procedures (Procedure 3, 4): {pending_count}")
    print("=" * 65)

    if total_violations:
        print("❌ Build check failed due to regression violations!")
        sys.exit(1)
    else:
        print("✅ Procedure 1 & 2 Anti-Regression verification passed successfully!")
        sys.exit(0)

if __name__ == "__main__":
    main()
