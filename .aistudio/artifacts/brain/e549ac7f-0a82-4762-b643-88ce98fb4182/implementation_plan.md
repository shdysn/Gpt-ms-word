# Codebase Modularization & Refactoring Plan (لائحہ عمل)

Comprehensive roadmap to safely decompose all files exceeding 300 lines into focused, cohesive modules under 250–300 lines each, grouped into dedicated feature subfolders with continuous compilation and test verification.

## User Review & Critical Decisions

> [!IMPORTANT]
> The refactoring strategy strictly preserves 100% of the existing user-facing UI, features, and business logic while improving build times, token efficiency, and maintainability.

- **Confirmed Organization**: New modular files will be organized into **subfolders by feature module** (e.g., `ui/editor/dialogs/insert/`, `data/service/docx/reader/`, `data/service/pdf/render/`, `ui/editor/viewmodel/`).
- **File Size Target**: Every resulting file will be kept strictly **under 250–300 lines**.
- **Execution Strategy**: Executed in safe, sequential batches. After each batch, the applet will be compiled (`compile_applet`) and tests run (`gradle :app:testDebugUnitTest`) to prevent any regressions.

---

## 1. Overview & Core Concept

- **What It Does**: Refactors the monolithic files in Smart Word into lightweight, single-responsibility components and domain managers.
- **Target Files**: All files currently exceeding 300 lines, prioritized by size:
  1. `EditorViewModel.kt` (2,907 lines)
  2. `InsertDialogs.kt` (1,894 lines)
  3. `PdfExportServiceImpl.kt` (1,396 lines)
  4. `DocxPackageWriter.kt` (1,114 lines)
  5. `DocumentStorageManager.kt` (1,068 lines)
  6. `AdvancedFeaturesDialogs.kt` (1,048 lines)
  7. `BlockItemView.kt` (937 lines)
  8. `EditorCommand.kt` (889 lines)
  9. `DocxPackageReader.kt` (872 lines)
  10. `DocumentRepositoryImpl.kt` (854 lines)
  11. `FormattingToolbar.kt` (786 lines)
  12. `DocumentCanvas.kt` (704 lines)
  13. `EditorDialogsHost.kt` (703 lines)
  14. `RibbonControls.kt` (673 lines)
  15. `SmartWritingDialog.kt` (668 lines)
  16. `PaginationEngine.kt` (650 lines)
- **Key Value**: Ensures all future edits, AI modifications, and feature expansions complete in seconds with zero risk of token exhaustion or code truncation.

---

## 2. User Experience & Visual Design

- **Zero Regression Rule**: The end-user UI remains completely unchanged. Every button, menu, dialog, gesture, and rendering element looks and behaves identically.
- **Visual Stability**: No changes to colors, typography, paddings, animations, or styling tokens.
- **Performance**: Improved runtime performance due to better Composable recomposition scoping when components are isolated in smaller dedicated files.

---

## 3. Key Product Decisions & Trade-Offs

- **Decision 1: Decomposition by Domain Responsibility**
  - *Chosen Approach*: Group components into semantic subfolders (e.g., separating Table, Image, and Shape dialogs into individual files under `ui/editor/dialogs/insert/`).
  - *Why*: Prevents merge collisions, speeds up compile times, and limits any single file to 120–250 lines.
  - *Alternatives Considered*: Keeping all files in one flat folder was rejected as it would create cluttered directory structures.

- **Decision 2: ViewModel Delegation Pattern**
  - *Chosen Approach*: Extract `EditorViewModel` sub-responsibilities into dedicated delegate managers:
    - `EditorFormattingManager` (Bold, italic, color, alignments)
    - `EditorTableManager` (Table row/col addition, cell updates)
    - `EditorExportManager` (PDF, DOCX, EPUB, TXT export dispatch)
    - `EditorHistoryManager` (Undo, redo, snapshots, autosave)
    - `EditorBlockManager` (Paragraph splitting, deletion, reordering)
  - *Why*: Allows `EditorViewModel` to serve as a lightweight coordinator (~250 lines) while isolating complex logic in easily testable components.
  - *Alternatives Considered*: Splitting into multiple independent ViewModels was rejected because Jetpack Compose screen scoping works best with a unified editor screen contract.

- **Decision 3: Phased Batch Execution**
  - *Batch 1*: Heavy UI Dialogs (`InsertDialogs.kt`, `AdvancedFeaturesDialogs.kt`, `EditorDialogsHost.kt`)
  - *Batch 2*: Editor UI Canvas & Toolbars (`FormattingToolbar.kt`, `BlockItemView.kt`, `DocumentCanvas.kt`, `RibbonControls.kt`)
  - *Batch 3*: ViewModel & Commands (`EditorViewModel.kt`, `EditorCommand.kt`)
  - *Batch 4*: Document Export & I/O Services (`PdfExportServiceImpl.kt`, `DocxPackageWriter.kt`, `DocxPackageReader.kt`, `DocumentStorageManager.kt`, `DocumentRepositoryImpl.kt`, `PaginationEngine.kt`)

---

## 4. Technical Architecture & Data Strategy

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                           EditorScreen (UI Layer)                           │
└──────────────────────────────────────┬──────────────────────────────────────┘
                                       │
         ┌─────────────────────────────┼─────────────────────────────┐
         ▼                             ▼                             ▼
┌──────────────────┐          ┌──────────────────┐          ┌──────────────────┐
│   WordRibbon /   │          │  DocumentCanvas  │          │  Dialogs Host    │
│  FormattingBar   │          │  & Block Views   │          │  & Sub-dialogs   │
│ (Subfolder: bar) │          │(Subfolder:canvas)│          │(Subfolder:dialog)│
└────────┬─────────┘          └────────┬─────────┘          └────────┬─────────┘
         │                             │                             │
         └─────────────────────────────┼─────────────────────────────┘
                                       ▼
                     ┌───────────────────────────────────┐
                     │          EditorViewModel          │
                     │       (Unified Coordinator)       │
                     └─────────────────┬─────────────────┘
                                       │ Delegates to
         ┌─────────────────────────────┼─────────────────────────────┐
         ▼                             ▼                             ▼
┌──────────────────┐          ┌──────────────────┐          ┌──────────────────┐
│ FormattingManager│          │   TableManager   │          │  HistoryManager  │
└──────────────────┘          └──────────────────┘          └──────────────────┘
                                       │
                                       ▼
                     ┌───────────────────────────────────┐
                     │        DocumentRepository         │
                     └─────────────────┬─────────────────┘
                                       │
         ┌─────────────────────────────┼─────────────────────────────┐
         ▼                             ▼                             ▼
┌──────────────────┐          ┌──────────────────┐          ┌──────────────────┐
│ PdfExportService │          │   DocxService    │          │  StorageManager  │
│ (renderers split)│          │ (writer/reader)  │          │ (cache/persist)  │
└──────────────────┘          └──────────────────┘          └──────────────────┘
```

### Verification & Quality Gates
- **Compilation Check**: `compile_applet` must succeed after every batch.
- **Unit & Robolectric Test Suite**: `gradle :app:testDebugUnitTest` must pass completely without regressions.
