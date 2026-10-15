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
    "app/src/main/java/com/example/ui/editor/components/dialogs/EditorFormatModals.kt",
    "app/src/main/java/com/example/ui/editor/components/dialogs/InsertTableDialog.kt",
    "app/src/main/java/com/example/ui/editor/components/dialogs/TableEditDialogs.kt",
    "app/src/main/java/com/example/ui/editor/components/dialogs/DocumentSaveDialogs.kt",
    "app/src/main/java/com/example/ui/editor/components/dialogs/ExportPdfDialog.kt",
    "app/src/main/java/com/example/ui/editor/components/review/CommentsDialog.kt",
    "app/src/main/java/com/example/ui/home/components/HomeDialogs.kt",
    "app/src/main/java/com/example/ui/editor/components/EditorTopAppBar.kt",
    "app/src/main/java/com/example/ui/editor/components/EditorQuickFormatBar.kt",
]

# Files strictly enforced for Phase 2 (Canvas, Toolbars, Blocks & Ribbon)
PHASE_2_FILES = [
    "app/src/main/java/com/example/ui/editor/components/WordRibbon.kt",
    "app/src/main/java/com/example/ui/editor/components/WordRibbonContent.kt",
    "app/src/main/java/com/example/ui/editor/components/ribbon/DrawRibbonControls.kt",
    "app/src/main/java/com/example/ui/editor/components/DrawingBlockView.kt",
    "app/src/main/java/com/example/ui/editor/components/blocks/DrawingCanvasArea.kt",
    "app/src/main/java/com/example/ui/editor/components/toolbar/ParagraphFormattingBar.kt",
    "app/src/main/java/com/example/ui/editor/components/toolbar/ParagraphFormattingDropdowns.kt",
    "app/src/main/java/com/example/ui/editor/components/toolbar/CharacterFormattingBar.kt",
    "app/src/main/java/com/example/ui/editor/components/toolbar/CharacterFormattingMenus.kt",
    "app/src/main/java/com/example/ui/editor/components/toolbar/ColorFormattingMenus.kt",
    "app/src/main/java/com/example/ui/editor/components/TableBlockView.kt",
    "app/src/main/java/com/example/ui/editor/components/TableHeaderBar.kt",
    "app/src/main/java/com/example/ui/editor/components/ShapeBlockView.kt",
    "app/src/main/java/com/example/ui/editor/components/dialogs/VectorShapeGalleryDialog.kt",
    "app/src/main/java/com/example/ui/editor/components/dialogs/VectorShapeMiniPreview.kt",
    "app/src/main/java/com/example/ui/editor/components/dialogs/VectorShapeModels.kt",
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
]

# Files strictly enforced for Phase 3 (ViewModel Coordinator, Engine & Helpers)
PHASE_3_FILES = [
    "app/src/main/java/com/example/ui/editor/EditorViewModel.kt",
    "app/src/main/java/com/example/ui/editor/viewmodel/EditorFormattingManager.kt",
    "app/src/main/java/com/example/ui/editor/viewmodel/EditorParagraphFormattingManager.kt",
    "app/src/main/java/com/example/ui/editor/viewmodel/EditorTableManager.kt",
    "app/src/main/java/com/example/ui/editor/viewmodel/EditorTableCellFormatManager.kt",
    "app/src/main/java/com/example/ui/editor/viewmodel/EditorBlockManager.kt",
    "app/src/main/java/com/example/ui/editor/viewmodel/EditorBlockUtils.kt",
    "app/src/main/java/com/example/ui/editor/viewmodel/EditorMediaBlockManager.kt",
    "app/src/main/java/com/example/ui/editor/viewmodel/EditorExportManager.kt",
    "app/src/main/java/com/example/ui/editor/viewmodel/EditorHistoryManager.kt",
    "app/src/main/java/com/example/ui/editor/viewmodel/EditorReviewManager.kt",
    "app/src/main/java/com/example/ui/editor/viewmodel/EditorReviewAndSecurityManager.kt",
    "app/src/main/java/com/example/ui/editor/viewmodel/EditorTextCanvasManager.kt",
    "app/src/main/java/com/example/ui/editor/viewmodel/EditorTextCanvasReconciler.kt",
    "app/src/main/java/com/example/ui/editor/viewmodel/EditorTtsManager.kt",
    "app/src/main/java/com/example/ui/home/HomeViewModel.kt",
    "app/src/main/java/com/example/ui/home/HomeTemplateManager.kt",
    "app/src/main/java/com/example/ui/home/HomeExportImportHelper.kt",
    "app/src/main/java/com/example/ui/home/HomeState.kt",
    "app/src/main/java/com/example/ui/home/HomeDocumentFilter.kt",
    "app/src/main/java/com/example/editor/engine/EditorEngineIntegration.kt",
    "app/src/main/java/com/example/editor/engine/ContinuousAnnotatedTextBuilder.kt",
    "app/src/main/java/com/example/editor/engine/SpellCheckEngine.kt",
    "app/src/main/java/com/example/editor/engine/SpanOperations.kt",
    "app/src/main/java/com/example/editor/engine/ActiveFormatting.kt",
    "app/src/main/java/com/example/editor/engine/SpanShiftOperations.kt",
    "app/src/main/java/com/example/editor/engine/commands/StyleCommands.kt",
    "app/src/main/java/com/example/editor/engine/commands/ListAndCustomStyleCommands.kt",
]

# Files strictly enforced for Phase 4 (Document Export & I/O Services)
PHASE_4_FILES = [
    "app/src/main/java/com/example/core/layout/PaginationEngine.kt",
    "app/src/main/java/com/example/core/layout/PaginationPageBuilder.kt",
    "app/src/main/java/com/example/core/layout/PaginationBlockLayouter.kt",
    "app/src/main/java/com/example/core/layout/PaginationTextBreaker.kt",
    "app/src/main/java/com/example/data/service/docx/DocxPackageReader.kt",
    "app/src/main/java/com/example/data/service/docx/DocxParagraphParser.kt",
    "app/src/main/java/com/example/data/service/docx/DocxRunParser.kt",
    "app/src/main/java/com/example/data/service/docx/DocxXmlUtils.kt",
    "app/src/main/java/com/example/data/service/docx/DocxBodyXmlBuilder.kt",
    "app/src/main/java/com/example/data/service/docx/DocxTableXmlBuilder.kt",
    "app/src/main/java/com/example/data/service/docx/DocxSectionXmlBuilder.kt",
    "app/src/main/java/com/example/data/service/docx/DocxRunSegmenter.kt",
    "app/src/main/java/com/example/data/service/docx/DocxPackageWriter.kt",
    "app/src/main/java/com/example/data/service/PdfPageItemRenderer.kt",
    "app/src/main/java/com/example/data/service/PdfSpecialItemRenderer.kt",
    "app/src/main/java/com/example/data/service/PdfTableAndImageRenderer.kt",
    "app/src/main/java/com/example/data/service/PdfExportServiceImpl.kt",
    "app/src/main/java/com/example/data/service/PdfDocumentGenerator.kt",
    "app/src/main/java/com/example/data/service/WordPrintDocumentAdapter.kt",
    "app/src/main/java/com/example/data/service/MarkdownAndEpubExportHelper.kt",
    "app/src/main/java/com/example/data/repository/DocumentRepositoryImpl.kt",
    "app/src/main/java/com/example/data/repository/DocumentStandardTemplates.kt",
    "app/src/main/java/com/example/data/repository/DocumentShowcaseTemplates.kt",
    "app/src/main/java/com/example/data/repository/DocumentTemplateFactory.kt",
    "app/src/main/java/com/example/data/repository/DocumentSnapshotManager.kt",
    "app/src/main/java/com/example/core/layout/PaginationParagraphLayouter.kt",
    "app/src/main/java/com/example/data/repository/DocumentUrduTemplate.kt",
    "app/src/main/java/com/example/data/storage/DocumentPlainTextImporter.kt",
    "app/src/main/java/com/example/data/storage/DocumentBlockJsonSerializer.kt",
    "app/src/main/java/com/example/data/storage/DocumentParagraphJsonDeserializer.kt",
    "app/src/main/java/com/example/data/storage/DocumentStorageManager.kt",
    "app/src/main/java/com/example/data/storage/DocumentJsonSerializer.kt",
    "app/src/main/java/com/example/data/storage/DocumentBlockJsonDeserializer.kt",
    "app/src/main/java/com/example/data/storage/DocumentJsonDeserializer.kt",
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
    proc2_ok, proc2_violations = check_file_group("Procedure 2 / Phase 2: Canvas, Toolbars, Blocks & Ribbon", PHASE_2_FILES)
    proc3_ok, proc3_violations = check_file_group("Procedure 3 / Phase 3: Core Engine & ViewModel Coordinator", PHASE_3_FILES)
    proc4_ok, proc4_violations = check_file_group("Procedure 4 / Phase 4: Document Export & I/O Services", PHASE_4_FILES)
    
    total_violations = proc1_violations + proc2_violations + proc3_violations + proc4_violations
    
    print("-" * 65)
    if proc1_ok and proc2_ok and proc3_ok and proc4_ok:
        print("🎉 Procedure 1, 2, 3 & 4 Status: 100% COMPLIANT! All monitored files are < 300 lines.")
    else:
        print(f"❌ Violations Found: {len(total_violations)}")

    # Audit rest of repository to inform developer of pending procedures
    print("\n[Repository Audit] Files pending future procedures (> 300 lines):")
    pending_count = 0
    enforced_set = set(PHASE_1_FILES + PHASE_2_FILES + PHASE_3_FILES + PHASE_4_FILES)
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

    print(f"\nTotal files pending future procedures: {pending_count}")
    print("=" * 65)

    if total_violations:
        print("❌ Build check failed due to regression violations!")
        sys.exit(1)
    else:
        print("✅ Procedure 1, 2, 3 & 4 Anti-Regression verification passed successfully!")
        sys.exit(0)

if __name__ == "__main__":
    main()
