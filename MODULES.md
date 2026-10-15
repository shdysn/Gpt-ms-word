# Module Instructions & Registry

> ### ⚠️ CRITICAL IMPORT & REPO INTEGRITY DIRECTIVE (براہِ کرم سختی سے عمل کریں)
> **MANDATORY POLICY FOR FUTURE GITHUB IMPORTS, CI/CD, AGENTS, AND DEVELOPERS:**
>
> 1. **DO NOT RESET, OVERWRITE, OR CONSOLIDATE FILES ON IMPORT:**  
>    Jab bhi yeh project GitHub, AI Studio, ya kisi bhi platform par import kiya jaye, **tamam modular sub-files aur 300-line limit policy permanent rehni chahiye**. Files ko wapas single monolithic file mein merge ya consolidate **HARGIZ NA KIYA JAYE**.
>
> 2. **STRICT 300-LINE FILE SIZE CEILING:**  
>    All active and extracted source files must strictly remain **under 250–300 lines**. Any new feature, dialog, or UI component must be created in a new dedicated sub-file rather than bloated into existing screens or viewmodels.
>
> 3. **PRESERVE ALL EXTRACTED SUB-MODULES:**  
>    Never delete or bypass the dedicated extracted modules listed below.

---

## 📦 Registered Modules

### 1. `com.example.ui.home`
- **`HomeScreen.kt`** (~260 lines): Clean coordinator screen for document gallery.
- **`HomeViewModel.kt`**: Document listing, sorting, filtering, and template creation.
- **`components/`**:
  - `HomeTopAppBar.kt`: Header with App branding, SAF open document, view mode toggle, and settings.
  - `HomeSearchBarAndActions.kt`: Search input bar and New Document / Open DOCX action buttons.
  - `HomeTemplatesCarousel.kt`: Carousel row of Word document templates.
  - `HomeFilterSortBar.kt`: Filter chips (All, Recent, Pinned, Favorites) and Sort order dropdown.
  - `HomeDocumentsGridList.kt`: Optimized LazyColumn list/grid item delegate.
  - `HomeDialogsHost.kt`: Dedicated coordinator for Create, Rename, Delete, and Settings dialogs.
  - `DocumentGridCard.kt`, `DocumentItemRow.kt`, `TemplateCard.kt`, `HomeShareUtils.kt`.

### 2. `com.example.ui.editor`
- **`EditorScreen.kt`** (~260 lines): Coordinator for document editing with edge-to-edge support.
- **`EditorDialogsState.kt`**: Encapsulates 30+ editor modal booleans and selection states.
- **`EditorDialogsWiring.kt`**: Wires dialogs host to ViewModel actions.
- **`components/`**:
  - `EditorTopAppBar.kt`: Document title, autosave chip, undo/redo, view mode, search toggle, overflow menu.
  - `EditorRibbonHost.kt`: Encapsulates WordRibbon tabs and voice/photo launcher delegation.
  - `EditorCanvasContent.kt`: Document canvas viewport and contextual quick format bar container.
  - `EditorQuickFormatBar.kt`: Floating formatting pill (Bold, Italic, Underline, Strikethrough, Highlight, Color).
  - `DocumentOutlineSheet.kt` & `OutlineHeadingComponents.kt`: Semantic headings hierarchy tree, jump navigation, stats chips.
  - `SmartWritingDialog.kt`, `SmartWritingAnalysisTabs.kt`, `SmartWritingPolishTab.kt`, `SmartWritingGrammarTab.kt`, `SmartWritingTranslateTab.kt`: AI Summarizer, Tone Polish, Grammar Scanner, Urdu Translation, and Readability Analytics.
  - `DocumentCanvas.kt` & `UnifiedTextCanvas.kt`: Infinite and paginated paper renderers.
  - `EditorStatusBar.kt`: Page counter, word counter, zoom controls, and RTL indicator.
  - `FindReplaceBar.kt`: Search and replace overlay.
  - `EditorRecoveryBanner.kt`: Crash recovery draft prompt.
  - `EditorDialogsHost.kt`: Root dialog host.
  - `dialogs/`:
    - `PageLayoutDialog.kt` & `PageLayoutPreviewCard.kt`
    - `PageLayoutPaperTab.kt` & `PageLayoutMarginsSection.kt`
    - `TypographyAndStyleDialogs.kt` & `ParagraphSpacingAndIndentsDialog.kt`
    - `DocumentElementsDialogs.kt`, `WatermarkDialog.kt`, `VersionHistoryDialog.kt`
    - `SecurityDialogs.kt` & `DigitalSignatureDialog.kt`
    - `EditorAdvancedModals.kt` & `EditorFormatModals.kt`

### 3. `com.example.core`
- **`core.layout`**: PaginationEngine, PageLayout, PageDimensions, 2-column layout.
- **`core.typography`**: Noto Nastaliq Urdu, Noto Sans Arabic, BiDi evaluation, SignatureUtils.
- **`core.error`**: DocumentError.

### 4. `com.example.data`
- **`data.model`**: RichDocument, ParagraphBlock, TableBlock, ImageBlock, FormatTypes.
- **`data.local`**: Room AppDatabase, DocumentDao, PreferencesManager.
- **`data.storage`**: DocumentStorageManager, JSON Serializers/Deserializers.
- **`data.repository`**: DocumentRepository, DocumentRepositoryImpl, Templates.
- **`data.service` & `docx`**: PdfExportServiceImpl, DocxPackageReader, DocxPackageWriter, DocxService, DocxParagraphParser, DocxRunParser.

### 5. `com.example.editor.engine`
- **`AutosaveController`**: Debounced background autosave.
- **`UndoRedoManager`**: Undo/Redo transaction history.
- **`EditorCommand`**: Atomic command actions.
- **`SpanOperations`**: Inline formatting spans.
