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

