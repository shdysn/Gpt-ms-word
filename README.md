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

## 📦 300-Line Codebase Modularization & Anti-Regression Procedures (تمام فائلز کو 300 لائنز سے کم رکھنے کا باقاعدہ طریقہ کار)

> ### 🎯 DIRECTIVE FOR AI AGENT / DEVELOPER:
> **Jab bhi user kisi bhi procedure ka zikr kare (maslan: "Procedure 1", "Procedure 2", "Procedure 3", ya "Procedure 4" apply karo — ya purane lafz "Phase" se pukaare), to foran us specific procedure ki tamam files ko mutaliqa subfolders mein extract aur modularize karein:**
> 
> - **Jab "Procedure 1" (ya "Phase 1") ka zikr ho:** UI Screens aur Dialogs (8 Files) [COMPLETED]
> - **Jab "Procedure 2" (ya "Phase 2") ka zikr ho:** Editor Canvas, Block Views aur Toolbars (8 Files)
> - **Jab "Procedure 3" (ya "Phase 3") ka zikr ho:** ViewModel Coordinator aur Core Engine (5 Files)
> - **Jab "Procedure 4" (ya "Phase 4") ka zikr ho:** Data Layer, Storage, DOCX اور PDF Services (7-8 Files)
> 
> **Standard Rules & Guarantees:**
> 1. Har nayi aur purani file ka size strictly **200 se 280 lines (<300 lines)** ke darmiyan rakhna lazmi hai.
> 2. **Zero Regression Guarantee:** UI, designs, icons, fonts, features, formatting, aur tests 100% waise hi kaam karte rahein.
> 3. Har procedure ke dauran aur aakhir mein `compile_applet` aur `gradle :app:testDebugUnitTest` run kar ke tasdeeq ki jaye.

---

### 🔹 Procedure 1: UI Screens & Complex Dialogs ✅ [COMPLETED & VERIFIED]
Procedure 1 ki tamam 8 bari UI aur dialog files ko modularize kar ke har file ko **300 lines se kam** (106 - 275 lines) kar diya gaya hai:

- [x] **`SmartWritingDialog.kt`**: 106 lines (Modularized clean dialog scaffold)
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

---

### 🔹 Procedure 2: Editor Canvas, Blocks & Toolbars (جب "Procedure 2" کا ذکر ہو تو یہ لاگو کریں)
Jab user **"Procedure 2"** (ya "Phase 2") bole, to mandarja zail 8 canvas aur editor formatting files ko extract karein:

1. **`ParagraphBlockView.kt` (423 lines → 3 Files):**
   - Extract `ParagraphTextRenderer.kt` (~150 lines): AnnotatedString generation, span styles, BiDi RTL layout.
   - Extract `ParagraphSelectionOverlay.kt` (~130 lines): Touch handles, cursor drawing, inline floating actions.
   - Keep `ParagraphBlockView.kt` (~140 lines): Block container, focus handling, drag handles.
2. **`SpecialBlocksView.kt` (340 lines → 4 Files):**
   - Extract `DividerBlockView.kt` (~95 lines): Horizontal line dividers, styles, colors.
   - Extract `DateTimeBlockView.kt` (~105 lines): Localized date/time stamps and chips.
   - Extract `FootnoteBlockView.kt` (~115 lines): Superscript index and footnote text input.
   - Keep `SpecialBlocksView.kt` (~85 lines) as clean composable dispatcher.
3. **`DrawingBlockView.kt` (321 lines → 3 Files):**
   - Extract `DrawingCanvasSurface.kt` (~160 lines): Touch path capture, bezier stroke rendering.
   - Extract `DrawingToolPalette.kt` (~130 lines): Pen, highlighter, eraser, stroke width, color picker.
   - Keep `DrawingBlockView.kt` (~95 lines): Block frame and action buttons.
4. **`PaginatedItemRenderer.kt` (367 lines) & `PaginatedSheetView.kt` (348 lines):**
   - Extract `PageDividerDecoration.kt` (~120 lines): Page drop shadows, page margins clipping, page number watermark.
   - Extract `PaginatedBlockDispatcher.kt` (~160 lines): Routing blocks to specific page sub-renderers.
   - Keep both original files strictly between 180 and 220 lines.
5. **`CharacterFormattingBar.kt` (391 lines) & `ParagraphFormattingBar.kt` (336 lines):**
   - Extract `FontFamilyDropdownMenu.kt` (~140 lines): Font list, preview chips, Urdu/Arabic fonts.
   - Extract `ListStyleDropdownMenu.kt` (~120 lines): Bullet types, numbered list types.
   - Extract `ColorPickerBottomSheet.kt` (~140 lines): Palette grids, custom color input.
   - Keep both toolbar bars under 220 lines.
6. **`WordRibbon.kt` (322 lines → 3 Files):**
   - Extract `RibbonTabRow.kt` (~130 lines): Home, Insert, Draw, Layout, Review, View tabs.
   - Extract `RibbonOverflowBar.kt` (~120 lines): Overflow more menu, collapse/expand toggle.
   - Keep `WordRibbon.kt` (~110 lines).

---

### 🔹 Procedure 3: Core Engine & ViewModel Coordinator [COMPLETED & VERIFIED]
Procedure 3 ki تمام ViewModel اور Core Engine files کو modularize کر کے ہر file کو **300 lines سے کم** (45 - 291 lines) کر دیا گیا ہے:

- [x] **`EditorViewModel.kt`**: 212 lines (Coordinator) + specialized delegate managers under `ui/editor/viewmodel/`:
   - `EditorFormattingManager.kt` (228 lines)
   - `EditorParagraphFormattingManager.kt` (222 lines)
   - `EditorTableManager.kt` (138 lines)
   - `EditorTableCellFormatManager.kt` (145 lines)
   - `EditorBlockManager.kt` (245 lines) + `EditorBlockUtils.kt` (71 lines)
   - `EditorMediaBlockManager.kt` (191 lines)
   - `EditorExportManager.kt` (201 lines)
   - `EditorHistoryManager.kt` (189 lines)
   - `EditorReviewManager.kt` (104 lines)
   - `EditorReviewAndSecurityManager.kt` (291 lines)
   - `EditorTextCanvasManager.kt` (270 lines) + `EditorTextCanvasReconciler.kt` (127 lines)
   - `EditorTtsManager.kt` (151 lines)
- [x] **`HomeViewModel.kt`**: 168 lines + `HomeTemplateManager.kt` (46 lines) + `HomeExportImportHelper.kt` (84 lines)
- [x] **Core Engine & Commands**:
   - `EditorEngineIntegration.kt` (225 lines) + `ContinuousAnnotatedTextBuilder.kt` (152 lines) + `SpellCheckEngine.kt` (52 lines)
   - `SpanOperations.kt` (218 lines) + `ActiveFormatting.kt` (72 lines) + `SpanShiftOperations.kt` (124 lines)
   - `StyleCommands.kt` (154 lines) + `ListAndCustomStyleCommands.kt` (218 lines)

---

### 🔹 Procedure 4: Data Layer, Storage, DOCX & PDF Services ✅ [COMPLETED & VERIFIED]
Procedure 4 ki تمام Data Layer, Storage, DOCX aur PDF Export files ko modularize kar ke har file ko **300 lines se kam** (40 - 276 lines) kar diya gaya hai:

1. **PDF Export & Printing (`com.example.data.service`):**
   - `PdfExportServiceImpl.kt` (221 lines)
   - `PdfPageItemRenderer.kt` (234 lines)
   - `PdfSpecialItemRenderer.kt` (223 lines)
   - `PdfTableAndImageRenderer.kt` (152 lines)
   - `PdfDocumentGenerator.kt` (185 lines)
   - `WordPrintDocumentAdapter.kt` (132 lines)
   - `MarkdownAndEpubExportHelper.kt` (201 lines)
2. **OpenXML / DOCX Processing (`com.example.data.service.docx`):**
   - `DocxPackageReader.kt` (266 lines)
   - `DocxParagraphParser.kt` (187 lines) + `DocxRunParser.kt` (148 lines)
   - `DocxPackageWriter.kt` (153 lines) + `DocxBodyXmlBuilder.kt` (209 lines)
   - `DocxTableXmlBuilder.kt` (84 lines) + `DocxSectionXmlBuilder.kt` (53 lines)
   - `DocxRunSegmenter.kt` (107 lines) + `DocxXmlUtils.kt` (68 lines) + `DocxTableParser.kt` (132 lines)
3. **Document Storage & JSON Serializers (`com.example.data.storage`):**
   - `DocumentStorageManager.kt` (263 lines)
   - `DocumentBlockJsonSerializer.kt` (219 lines) + `DocumentBlockJsonDeserializer.kt` (212 lines)
   - `DocumentJsonSerializer.kt` (109 lines) + `DocumentJsonDeserializer.kt` (182 lines)
   - `DocumentParagraphJsonDeserializer.kt` (100 lines) + `DocumentPlainTextImporter.kt` (52 lines)
4. **Repository & Templates (`com.example.data.repository`):**
   - `DocumentRepositoryImpl.kt` (276 lines) + `DocumentRepository.kt` (75 lines)
   - `DocumentStandardTemplates.kt` (170 lines) + `DocumentShowcaseTemplates.kt` (155 lines)
   - `DocumentUrduTemplate.kt` (169 lines) + `DocumentSnapshotManager.kt` (93 lines) + `DocumentTemplateFactory.kt` (40 lines)
5. **Pagination Engine (`com.example.core.layout`):**
   - `PaginationEngine.kt` (218 lines)
   - `PaginationPageBuilder.kt` (107 lines) + `PaginationBlockLayouter.kt` (119 lines)
   - `PaginationParagraphLayouter.kt` (140 lines) + `PaginationTextBreaker.kt` (100 lines)
6. **Data Models (`com.example.data.model`):**
   - `RichDocument.kt` (42 lines) + `BlockElements.kt` (210 lines) + `ParagraphBlock.kt` (56 lines) + `DocumentEnums.kt` (103 lines)

---

## 🔄 GitHub Import & Auto-Fix Procedure (اگلے امپورٹ پر فوری فکس کا طریقہ کار)

Jab bhi project GitHub (`shdysn/Gpt-ms-word` ya kisi branch) se dobara import ya pull kiya jaye, to mandarja zail steps follow karein ta k tamam files foran safe aur 300 lines ke andar fix ho jayein:

### Step 1: Status Audit Run Karein
Terminal mein ye command chalayein:
```bash
./scripts/check_file_limits.sh
```
Ya Gradle ke zariye:
```bash
gradle :app:checkFileLimits
```
Ye script tamam files scan kar ke report karega ke Procedure 1 ki files theek hain aur aglay procedures ki konsi files 300 lines se bari hain.

### Step 2: Next Procedure Trigger Karein
AI Agent ya developer ko sirf aglay procedure ka naam bole:
- **`Procedure 2 apply karo`** (Canvas, Blocks & Toolbars)
- **`Procedure 3 apply karo`** (ViewModel Coordinator & Engine)
- **`Procedure 4 apply karo`** (PDF, DOCX & Storage Services)

### Step 3: CI/CD Automatic Verification
- `.github/workflows/build-apk.yml` mein `Verify 300-Line Codebase Limit & Anti-Regression` step shamil hai jo GitHub par har push ya pull request par automatically file size limits enforce karta hai aur kisi file ko 300 lines se upar jane nahi deta.
- Har tabdeeli ke baad `compile_applet` aur `gradle :app:testDebugUnitTest` run ho kar ensure karta hai ke 100% features, UI aur tests theek chal rahe hain.



