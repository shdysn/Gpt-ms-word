# Smart Word — Android Word Processor

> ### ⚠️ CRITICAL IMPORT & PERSISTENCE DIRECTIVE (براہِ کرم احتیاط فرمائیں)
> **MANDATORY POLICY FOR FUTURE IMPORTS, AGENTS, AND DEVELOPERS:**
>
> 1. **DO NOT RESET, OVERWRITE, OR ROLL BACK CHANGES ON IMPORT:**  
>    Jab bhi yeh project future mein AI Studio, GitHub, ya kisi bhi platform par import kiya jaye, **tamam mojooda modules, refactoring, aur bug fixes permanent rehne chahiye**. Unko reset, wipe, ya original repository state par revert **HARGIZ NA KIYA JAYE** jab tak user explicitly hukam na de.
>
> 2. **DO NOT REMOVE OR MERGE EXISTING MODULES:**  
>    All extracted modular components (`EditorTopAppBar.kt`, `EditorDialogsHost.kt`, `RibbonControls.kt`, `EditorRecoveryBanner.kt`, `PaginationEngine.kt`, etc.) must remain distinct and under 500 lines per file. Never consolidate them back into monoliths.
>
> 3. **PRESERVE ALL ENHANCED FEATURE FIXES:**  
>    - **Interactive Table of Contents (TOC):** Dynamic real page numbers computed from pagination engine + jump-to-heading click navigation.
>    - **Interactive Hyperlinks:** Smart contextual "Open Link" pill launching the web browser via `Intent.ACTION_VIEW`.
>    - **Full-Document Read Aloud (TTS):** Sequential speech synthesis covering paragraphs, table cells, note shapes, image captions, and signatures.
>    - **2-Column Layout:** Layout line-breaking and OpenXML `<w:cols w:num="2" w:space="720"/>` import/export.
>    - **Offline-First Room & OpenXML Persistence:** Real `.docx` and `.pdf` generation without mock fallbacks.

---

## 📖 Overview

**Smart Word** is a full-featured, offline-first Microsoft Word-inspired document processor built natively for Android using Jetpack Compose and Material Design 3.

### 🌟 Core Capabilities
- **Microsoft Office Ribbon Toolbar:** Dedicated **Home**, **Insert**, **Layout**, **Review**, and **View** tabs.
- **Rich Typographic Styling:** Full font family management, bold, italic, underline, strikethrough, highlights, text colors, bullet lists, numbered sequences, and paragraph alignments.
- **Multilingual RTL Support:** Native support for Arabic and Urdu with bundled Google Noto Nastaliq Urdu and Noto Sans Arabic fonts.
- **Tables, Shapes & Drawings:** In-cell table editing, note callout boxes, photo insertion with aspect ratio controls, and freehand SVG digital signatures.
- **Pagination & Page Layout:** Real-time typographically accurate multi-page breaks, page sizes (A4, Letter, Legal), custom margins, orientations, watermarks, headers, and footers.
- **File Interoperability:** Native Microsoft Word OpenXML (`.docx`) file import and export, ISO-standard PDF generation, and Android Print framework integration.
- **Safety & Resilience:** Autosave controller with dirty-state tracking, crash recovery draft restoration, and undo/redo transaction history stack.

---

## 🏛️ Modular Architecture

This project strictly adheres to a decoupled MVVM architecture. Detailed module contracts are documented in [`ARCHITECTURE.md`](./ARCHITECTURE.md) and [`MODULES.md`](./MODULES.md).

```
app/src/main/java/com/example/
├── core/
│   ├── layout/            # PaginationEngine, PageLayout, PageDimensions
│   ├── typography/        # DocumentFontManager, BidiUtils (Urdu & Arabic)
│   └── error/             # DocumentResult, DocumentError
├── data/
│   ├── local/             # Room SQLite Database & DAOs
│   ├── model/             # RichDocument, ParagraphBlock, TableBlock, Spans
│   ├── repository/        # DocumentRepository & Template Library
│   ├── service/           # PDF Export, Android Printing
│   └── service/docx/      # OpenXML (.docx) Package Reader & Writer
├── editor/
│   └── engine/            # AutosaveController, UndoRedoManager, SpanOperations
└── ui/
    ├── editor/            # EditorScreen, EditorViewModel
    │   └── components/    # EditorTopAppBar, WordRibbon, RibbonControls,
    │                      # EditorDialogsHost, DocumentCanvas, etc.
    ├── home/              # HomeScreen, HomeViewModel, Template Gallery
    ├── navigation/        # Type-safe Navigation Compose routes
    └── theme/             # Material 3 typography and Office color palette
```

---

## 🔧 Build & Development Environment

- **Language:** Kotlin 2.2.10
- **Android Gradle Plugin (AGP):** 9.1.1
- **Gradle Version:** 9.3.1
- **Compile SDK:** 36 | **Target SDK:** 36 | **Min SDK:** 24
- **JVM Target:** Java 11 / JDK 21
- **Testing:** Local JVM Robolectric tests (`gradle :app:testDebugUnitTest`)

---

## 🔒 Secret & Configuration Policy
- Sensitive credentials and keys are wired exclusively via the **Secrets Gradle Plugin** using `.env` / `.env.example`.
- Never commit `.env` containing sensitive keys.
- Do not use or create `local.properties` in AI Studio.

---

## 🗺️ Implementation Roadmap & Phased Directives (فیچرز کا لائحہ عمل و روڈ میپ)

> **DEVELOPER DIRECTIVE FOR FUTURE IMPORT / SESSIONS:**  
> Jab bhi yeh app dobara import ki jaye ya mazeed taraqqi di jaye, mandarja zail phases ko ba-tartib complete kiya jaye:

### ✅ Phase 1: Visual Polish, Watermark & Navigation (COMPLETED)
- [x] **Watermark System:** Full diagonal (-45°) and horizontal watermarks with presets (`CONFIDENTIAL`, `DRAFT`, `URGENT`, `SAMPLE`) and custom Urdu/English text, rendered on canvas and exported into PDF.
- [x] **Page Borders & Background Colors:** Single Thin, Thick, Double, Dashed borders and Ivory/Cream/Parchment/Dark paper presets.
- [x] **Document Outline Navigation Sheet (`DocumentOutlineSheet.kt`):** Semantic headings hierarchy tree (Title, H1, H2, H3), section word counts, page numbers, search filter, and 1-click jump navigation.
- [x] **Live Status Bar:** Clickable page counter (`Page X of Y`), live word & character statistics, estimated reading time (`~X min`), and RTL language indicator.

---

### ⏳ Phase 2: Drawing, Inking & Vector Shapes (Next Phase / اگلا مرحلہ)
- [ ] **Draw Ribbon Tab (Freehand Inking):**
  - Add dedicated **Draw** tab in `WordRibbon.kt` and `RibbonControls.kt`.
  - Tools: Pen, Highlighter, Eraser, Stroke Width Slider (1pt–8pt), and Color Swatches.
  - Touch & Stylus / S-Pen drawing canvas layer over pages with stroke point serialization.
  - Export drawn ink strokes into PDF canvas (`PdfExportServiceImpl`) and OOXML drawing markup.
- [ ] **Vector Shapes Gallery:**
  - Support arbitrary geometric shapes (Rectangle, Rounded Rectangle, Oval/Circle, Right/Left/Up/Down Arrows, Star, Diamond, Callout Speech Bubble).
  - Configurable fill color, outline stroke thickness, rotation, and interior editable text.

---

### ⏳ Phase 3: Layout Refinement, Text Wrapping & Advanced Headers (مرحلہ 3)
- [ ] **Image Text Wrapping:**
  - Reflow text around images with wrapping modes: `IN_LINE`, `SQUARE`, `BEHIND_TEXT`, `IN_FRONT_OF_TEXT`.
  - Floating image positioning with gesture-based drag & resize handles.
- [ ] **Floating Text Boxes:**
  - Draggable, resizable text containers that can be placed anywhere on the sheet canvas.
- [ ] **Advanced Header & Footer Customization:**
  - **Different First Page:** Suppress headers/footers on title or cover pages.
  - **Different Odd & Even Pages:** Dual-page gutter headers for professional book and booklet publishing.
  - Extended page numbering: Roman numerals (`i, ii, iii`), Letters (`A, B, C`), and `Page X of Y`.

---

### ⏳ Phase 4: Real-time Typo Squiggles, OCR Scanner & Extended Formats (مرحلہ 4)
- [ ] **Real-time Spellcheck Squiggly Lines:**
  - Visual wavy red underline below misspelled words directly inside `BasicTextField` during live typing.
  - Quick popup with correction suggestions on tapping misspelled words.
- [ ] **Camera OCR Document Scanner:**
  - Camera capture to scan printed physical papers directly into editable document text using on-device text recognition.
- [ ] **Extended File Formats & Cloud Backup:**
  - Markdown (`.md`) import/export.
  - Plain Text (`.txt`) import/export.
  - EPUB e-book export.
  - Optional Google Drive / Cloud storage save action.

---

## 📦 300-Line Codebase Modularization Plan (تمام فائلز کو 300 لائنز سے کم کرنے کا روڈ میپ)

> ### 🎯 DIRECTIVE FOR AI AGENT / DEVELOPER:
> **Jab bhi user kisi bhi phase ka zikr kare (maslan: "Phase 1", "Phase 2", "Phase 3", ya "Phase 4" apply karo), to foran us specific phase ki tamam files ko mutaliqa subfolders mein extract aur modularize karein:**
> 
> - **Jab "Phase 1" ka zikr ho:** UI Screens aur Dialogs (8 Files) par foran kaam karein.
> - **Jab "Phase 2" ka zikr ho:** Editor Canvas, Block Views aur Toolbars (8 Files) par foran kaam karein.
> - **Jab "Phase 3" ka zikr ho:** ViewModel (Coordinator + 5 Managers) aur Engine (5 Files) par foran kaam karein.
> - **Jab "Phase 4" ka zikr ho:** Data Layer, Storage, DOCX اور PDF Services (7-8 Files) par foran kaam karein.
> 
> **Standard Rules:**
> 1. Har nayi aur purani file ka size strictly **200 se 280 lines (<300 lines)** ke darmiyan rakhna lazmi hai.
> 2. **Zero Regression Guarantee:** UI, designs, icons, fonts, features, formatting, aur tests 100% waise hi kaam karte rahein.
> 3. Har phase ke dauran aur aakhir mein `compile_applet` aur `gradle :app:testDebugUnitTest` run kar ke tasdeeq ki jaye.

---

### 🔹 Phase 1: UI Screens & Complex Dialogs ✅ [COMPLETED - 100% VERIFIED]
Phase 1 ki tamam 8 bari UI aur dialog files ko modularize kar ke har file ko **300 lines se kam** (106 - 275 lines) kar diya gaya hai:

- [x] **`SmartWritingDialog.kt`**: 106 lines (Modularized scaffold)
- [x] **`EditorScreen.kt`**: 265 lines (Editor root scaffold)
- [x] **`HomeScreen.kt`**: 261 lines (Home dashboard & template grid)
- [x] **`DocumentOutlineSheet.kt`**: 208 lines + `OutlineHeadingComponents.kt` (255 lines)
- [x] **`SecurityDialogs.kt`**: 128 lines + `DigitalSignatureDialog.kt` (241 lines)
- [x] **`DocumentElementsDialogs.kt`**: 126 lines + `WatermarkDialog.kt` (133 lines) + `VersionHistoryDialog.kt` (130 lines)
- [x] **`TypographyAndStyleDialogs.kt`**: 226 lines + `ParagraphSpacingAndIndentsDialog.kt` (134 lines)
- [x] **`PageLayoutDialog.kt`**: 259 lines + `PageLayoutPaperTab.kt` (215 lines) + `PageLayoutMarginsSection.kt` (146 lines)
- [x] **`EditorQuickFormatBar.kt`**: 131 lines
- [x] **`EditorTopAppBar.kt`**: 248 lines
- [x] **`HomeDialogs.kt`**: 264 lines

#### 🛡️ GitHub Import & Sync Anti-Regression Protection:
Jab bhi GitHub se data import kiya jaye ya repo pull/merge ho, to files limit size back na ho:
1. **GitHub CI Integration:** `.github/workflows/build-apk.yml` mein `Verify 300-Line Codebase Limit & Anti-Regression` step shamil hai jo har push/PR par limits check karta hai.
2. **Automated Verification Script:** `./scripts/check_file_limits.sh` (or `python3 scripts/check_file_limits.py`) jo foran check karta hai ke koi file 300 lines se exceed to nahi hui.
3. **Gradle Task:** `gradle :app:checkFileLimits` jo build/test se pehle run ho kar violation par build rok deta hai.

---

### 🔹 Phase 2: Editor Canvas, Blocks & Toolbars (جب "Phase 2" کا ذکر ہو تو یہ لاگو کریں)
Jab user **"Phase 2"** bole, to mandarja zail 8 canvas aur editor formatting files ko modularize karein:

1. **`ParagraphBlockView.kt` (423 lines)** → Split into text rendering, selection handles, and bidi/RTL layout composables under `components/blocks/`.
2. **`SpecialBlocksView.kt` (340 lines)** → Split horizontal line dividers, date/time blocks, and footnote blocks.
3. **`DrawingBlockView.kt` (321 lines)** → Extract touch canvas, stroke rendering, and tool selection.
4. **`PaginatedItemRenderer.kt` (367 lines)** & **`PaginatedSheetView.kt` (348 lines)** → Separate page margin clipping and page divider decorations under `components/canvas/`.
5. **`CharacterFormattingBar.kt` (391 lines)** & **`ParagraphFormattingBar.kt` (336 lines)** → Separate button groups and popup menus under `components/toolbar/`.
6. **`WordRibbon.kt` (322 lines)** → Extract `RibbonTabRow.kt` and `RibbonOverflowBar.kt`.

---

### 🔹 Phase 3: Core Engine & ViewModel Decomposition (جب "Phase 3" کا ذکر ہو تو یہ لاگو کریں)
Jab user **"Phase 3"** bole, to ViewModel aur Core Engine files ko modularize karein:

1. **`EditorViewModel.kt` (2,907 lines - Central Coordinator):**
   Decompose into dedicated delegate managers under `ui/editor/viewmodel/`:
   - `EditorFormattingManager.kt` (~250 lines): Bold, italic, color, fonts, alignments, lists.
   - `EditorTableManager.kt` (~250 lines): Add/remove rows/cols, merge cells, cell border styles.
   - `EditorBlockManager.kt` (~250 lines): Insert paragraph, image, shape, split blocks, delete block.
   - `EditorExportManager.kt` (~200 lines): Dispatch export to PDF, DOCX, EPUB, TXT, Print.
   - `EditorHistoryManager.kt` (~200 lines): Undo/Redo stack, autosave ticks, crash recovery snapshots.
   - Keep `EditorViewModel.kt` (~250 lines) as lightweight unified state coordinator.
2. **`HomeViewModel.kt` (333 lines)** → Extract `HomeTemplateManager.kt`.
3. **`EditorEngineIntegration.kt` (403 lines)**, **`SpanOperations.kt` (367 lines)**, **`StyleCommands.kt` (366 lines)** → Clean modular helpers under 250 lines.

---

### 🔹 Phase 4: Data Layer, Storage, DOCX & PDF Services (جب "Phase 4" کا ذکر ہو تو یہ لاگو کریں)
Jab user **"Phase 4"** bole, to heavy data aur export pipelines ko modularize karein:

1. **`PdfExportServiceImpl.kt` (1,396 lines)** → Separate Canvas text renderer, Table grid renderer, Shape/Image renderer.
2. **`DocxPackageWriter.kt` (1,114 lines)** → Separate document.xml writer, styles.xml writer, relationships writer.
3. **`DocumentStorageManager.kt` (1,068 lines)** → Separate CacheManager, FileExportHelper, ThumbnailGenerator.
4. **`DocxPackageReader.kt` (872 lines)** → Split paragraph parser, table parser, shape/run parser.
5. **`DocumentRepositoryImpl.kt` (854 lines)** → Split local queries, mutation handlers, and template seeders.
6. **`PaginationEngine.kt` (650 lines)** → Split height calculation, page break detector, and 2-column layout math.
7. **`RichDocument.kt` (408 lines)** → Separate block models, span models, and style enums.


