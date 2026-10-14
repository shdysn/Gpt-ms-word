# Smart Word — Modular Architecture & Instructions

## ⚠️ MANDATORY RULE: MODULE PERSISTENCE POLICY
> **DO NOT REMOVE ANY MODULES IN FUTURE VERSIONS.**
> Every module documented below is an essential, foundational component of the Smart Word processor.
> When introducing new features, improvements, or refactoring:
> 1. **Preserve existing modules and public contracts.**
> 2. **Extend or add new modular components** rather than deleting, replacing, or consolidating existing ones.
> 3. **Keep source files strictly modular and maintainable** (strictly under 300 lines per file, adhering to Procedure 1, 2, 3, and 4 modularization standards).
> 4. **Run `./scripts/check_file_limits.sh` or `gradle :app:checkFileLimits`** upon any GitHub import to prevent regressions.

---

## 1. System Architecture Overview

Smart Word is engineered as a clean, decoupled, layered Android architecture following MVVM (Model-View-ViewModel) and unidirectional data flow (UDF).

```
                      ┌────────────────────────────────────────┐
                      │             UI Layer (M3)              │
                      │  HomeScreen  │  EditorScreen & Ribbon  │
                      └───────────────────┬────────────────────┘
                                          │ StateFlow & Events
                      ┌───────────────────▼────────────────────┐
                      │            ViewModel Layer             │
                      │    HomeViewModel  │  EditorViewModel   │
                      └───────────────────┬────────────────────┘
                                          │ Coroutines & Contracts
                      ┌───────────────────▼────────────────────┐
                      │            Repository Layer            │
                      │           DocumentRepository           │
                      └─────────┬───────────────────┬──────────┘
                                │                   │
       ┌────────────────────────▼────────┐   ┌──────▼───────────────────────────┐
       │     Local Persistence Module    │   │     Export & Service Module      │
       │   Room (SQLite) + File Storage  │   │  OpenXML (.docx) & PDF Exporters │
       └─────────────────────────────────┘   └──────────────────────────────────┘
```

---

## 2. Module Registry & Responsibilities

### Module A: Core Modules (`com.example.core`)
Independent, framework-agnostic business engines and utilities.
- **`core.layout`**:
  - `PageLayout.kt`: Page dimensions (A4, Letter, Legal), margins, orientations, and layout presets.
  - `PaginationEngine.kt`: Real-time typographically accurate multi-page break computation based on font metrics.
  - `PaginationModel.kt`: Page data structures holding rendered content items, headers, and footers.
- **`core.typography`**:
  - `DocumentFontManager.kt`: Typography manager supporting system and bundled fonts (including Noto Arabic and Noto Nastaliq Urdu).
  - `BidiUtils.kt`: Bi-directional text detection, RTL script evaluation, and alignment management.
- **`core.error`**:
  - `DocumentError.kt`: Standardized domain error and result types (`DocumentResult`).

### Module B: Data & Storage Modules (`com.example.data`)
Data layer responsible for domain models, Room persistence, file I/O, and external document serialization.
- **`data.model`**:
  - `RichDocument.kt`: Complete document model (`ParagraphBlock`, `TableBlock`, `ImageBlock`, spans, styles).
  - `DocumentEntity.kt`: Room database entity representation.
  - `DocumentFormatType.kt`: Format enumerations (`SMART_DOC`, `DOCX`, `PDF`, `TXT`).
- **`data.local`**:
  - `AppDatabase.kt`: Room SQLite database instance.
  - `DocumentDao.kt`: Data access object for document CRUD and query operations.
  - `PreferencesManager.kt`: User settings and editor view preferences via DataStore.
- **`data.storage`**:
  - `DocumentStorageManager.kt`: Internal app-specific storage manager for rich JSON document snapshots and cached media.
- **`data.repository`**:
  - `DocumentRepository.kt` & `DocumentRepositoryImpl.kt`: Single source of truth coordinating database, storage, templates, and autosave.
- **`data.service` & `data.service.docx`**:
  - `DocxPackageReader.kt` & `DocxPackageWriter.kt`: Native OpenXML `.docx` ZIP compression, parser, and generator without heavy 3rd-party dependencies.
  - `DocxService.kt`: High-level Word document conversion interface.
  - `PdfExportServiceImpl.kt`: Android Native `PdfDocument` generation and Android Print framework integration.

### Module C: Editor Engine Modules (`com.example.editor.engine`)
State mutations, transaction history, and autosave coordination.
- `UndoRedoManager.kt`: History state stack supporting granular undo/redo operations.
- `AutosaveController.kt`: Debounced background snapshot manager with dirty-state tracking.
- `EditorCommand.kt`: Command pattern abstraction for text, formatting, and block operations.
- `SpanOperations.kt`: Character-level span slicing, merging, overlapping style normalization.
- `EditorEngineIntegration.kt`: Bridge orchestrating commands and editor engine workflows.

### Module D: UI Feature Modules (`com.example.ui`)
Modular Jetpack Compose presentation layer adhering to Material Design 3.
- **`ui.editor`**:
  - `EditorScreen.kt`: Core coordinator for the document canvas, scaffold, and toolbars.
  - `EditorViewModel.kt`: Rich state holder managing UI flows, formatting selection, and active document state.
- **`ui.editor.components`**:
  - `EditorTopAppBar.kt`: Top navigation bar with title rename, autosave indicator, and overflow actions.
  - `WordRibbon.kt`: Classic Microsoft Word ribbon bar supporting Home, Insert, Layout, Review, and View tabs.
  - `RibbonControls.kt`: Modular tab controls for Insert, Layout, Review, and View actions.
  - `FormattingToolbar.kt`: Quick-formatting controls (Bold, Italic, Underline, Strikethrough, Color, Alignment, Lists, Indents).
  - `DocumentCanvas.kt` & `UnifiedTextCanvas.kt`: Scalable paper canvas with zoom and pan gestures.
  - `BlockItemView.kt`, `TableBlockView.kt`, `ImageBlockView.kt`: Modular block renderers.
  - `DocumentRulerBar.kt`: Interactive typographic page margin ruler.
  - `EditorStatusBar.kt`: Status indicator for page count, word count, zoom level, and language.
  - `FindReplaceBar.kt`: Regex and case-sensitive search-and-replace overlay bar.
  - `EditorRecoveryBanner.kt`: Crash recovery alert for restoring unsaved draft sessions.
  - `EditorDialogsHost.kt`: Centralized host for all editor dialogs, decoupling modal rendering from the canvas.
  - `EditorDialogs.kt`, `InsertDialogs.kt`, `AdvancedFeaturesDialogs.kt`: Specialized modal dialogs.
- **`ui.home`**:
  - `HomeScreen.kt`: Document library dashboard.
  - `HomeViewModel.kt`: Search, filter, sort, and template instantiation logic.
  - `components/`: `DocumentGridCard.kt`, `DocumentItemRow.kt`, `TemplateCard.kt`, `HomeDialogs.kt`.
- **`ui.navigation`**:
  - `NavGraph.kt` & `Screen.kt`: Type-safe Jetpack Compose routing.
- **`ui.theme`**:
  - `Color.kt`, `Theme.kt`, `Type.kt`: Office-inspired Material 3 theme palette.

---

## 3. Rules for Future Modifications

1. **Do Not Delete Modules**: All listed modules must remain in place.
2. **Keep Files Below 500 Lines**: When extending a component, extract sub-views and dialogs into dedicated files in the corresponding package.
3. **No Mock Data for Existing Services**: The app uses real OpenXML DOCX parsing, real Android printing, real Room SQLite storage, and Android Text-to-Speech / Speech-to-Text. Preserve real implementations.
4. **Offline-First & Local-Native**: Core editor features operate completely locally on device without external network dependencies.
