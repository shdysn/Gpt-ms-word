# Module Instructions & Registry

> **IMPORTANT INSTRUCTION FOR ALL DEVELOPERS & AGENTS:**
> **DO NOT REMOVE, MERGE AWAY, OR STRIP ANY OF THE MODULES LISTED BELOW IN FUTURE ITERATIONS.**
> See `ARCHITECTURE.md` for complete architecture and extension specifications.

## 📦 Registered Modules

### 1. `com.example.core`
- **`core.layout`**: Multi-page pagination engine, margins, page dimensions, orientation (`PageLayout`, `PaginationEngine`, `PaginationModel`).
- **`core.typography`**: Multilingual Arabic & Urdu Noto fonts, BiDi evaluation, text styling (`DocumentFontManager`, `BidiUtils`).
- **`core.error`**: Domain error and status result types (`DocumentError`).

### 2. `com.example.data`
- **`data.model`**: Rich document models, paragraph blocks, tables, images, format types (`RichDocument`, `DocumentEntity`, `DocumentFormatType`).
- **`data.local`**: Room SQLite database, DocumentDao, user settings preference manager (`AppDatabase`, `DocumentDao`, `PreferencesManager`).
- **`data.storage`**: File system JSON document serialization and asset storage (`DocumentStorageManager`).
- **`data.repository`**: Central document repository and template library (`DocumentRepository`, `DocumentRepositoryImpl`).
- **`data.service` & `data.service.docx`**: Native OpenXML `.docx` reader & writer, Android PDF printer (`DocxPackageReader`, `DocxPackageWriter`, `DocxService`, `PdfExportServiceImpl`).

### 3. `com.example.editor.engine`
- **`AutosaveController`**: Debounced background autosave controller with dirty state detection.
- **`UndoRedoManager`**: Transaction history stack for undo and redo operations.
- **`EditorCommand`**: Command pattern actions for atomic editor modifications.
- **`SpanOperations`**: Rich inline text span formatting algorithms.
- **`EditorEngineIntegration`**: Engine coordination bridge.

### 4. `com.example.ui.editor`
- **`EditorScreen`**: Main coordinator screen for the document canvas, scaffold, and toolbars.
- **`EditorViewModel`**: ViewModel maintaining editor state, active formats, and commands.
- **`components`**:
  - `EditorTopAppBar`: Document title, autosave chip, undo/redo, view mode, search toggle, overflow menu.
  - `WordRibbon`: Office-style ribbon bar (Home, Insert, Layout, Review, View).
  - `RibbonControls`: Modular controls for Insert, Layout, Review, and View ribbon tabs.
  - `FormattingToolbar`: Quick text and paragraph styling controls.
  - `DocumentCanvas` & `UnifiedTextCanvas`: Infinite and paginated paper renderers.
  - `BlockItemView`, `TableBlockView`, `ImageBlockView`: Document block renderers.
  - `DocumentRulerBar`: Page ruler.
  - `EditorStatusBar`: Status bar for pages, words, zoom, and language.
  - `FindReplaceBar`: Search and replace overlay.
  - `EditorRecoveryBanner`: Crash and unexpected session recovery banner.
  - `EditorDialogsHost`: Dedicated host coordinating all editor dialogs and modals.
  - `EditorDialogs`, `InsertDialogs`, `AdvancedFeaturesDialogs`: Specialized modal dialogs.

### 5. `com.example.ui.home`
- **`HomeScreen`**: Document gallery dashboard with search, filter, and sorting.
- **`HomeViewModel`**: Document listing and template creation state management.
- **`components`**: Grid cards, list rows, template cards, dialogs, and sharing utilities.

### 6. `com.example.ui.navigation`
- **`NavGraph` & `Screen`**: Type-safe Compose navigation between Home and Editor.

### 7. `com.example.ui.theme`
- **`Color`, `Theme`, `Type`**: Material 3 typography and Microsoft Word-inspired color schemes.

---
**Guideline for future updates:** Always build into or extend this modular hierarchy. Never delete modules.
