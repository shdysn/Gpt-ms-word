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

## 🗺️ Implementation Roadmap: Mobile-First UX/UI Overhaul (جدید موبائل انٹرفیس روڈ میپ)

> **DEVELOPER DIRECTIVE FOR FUTURE IMPORT / SESSIONS (رہنمائی برائے ڈویلپرز):**  
> ایپ کے فنکشنز کو منظم کرنے، اسکرین کی جگہ بچانے، اور پلے اسٹور کی ٹاپ ایپس (MS Word Mobile, Google Docs, WPS Office) سے بہتر اور کشادہ موبائل انٹرفیس فراہم کرنے کے لیے درج ذیل نئے مراحل (Phases) کو بالترتیب نافذ کیا جائے:

### 📱 Phase 1: Minimal Top Bar & Screen Space Reclamation (اوپری پٹی کا خلاصہ اور کینوس کی آزادی) ✅ [COMPLETED]
- [x] **Ultra-Sleek Top App Bar (52dp):** اوپر سے بکھرے ہوئے بٹنوں کو سمیٹ کر صرف `[← Back]`، دستاویز کا ٹائٹل، `[Undo]`، `[Redo]` اور `[⋮]` مینیو رکھنا۔
- [x] **Elimination of Static Top Ribbon:** اسکرین کے اوپر سے بھاری ڈیسک ٹاپ ربن (140dp) کو مکمل طور پر ہٹانا تاکہ لکھنے اور پڑھنے کے لیے موبائل کا 85-90% کینوس آزاد ملے۔
- [x] **Animated Find & Replace Overlay:** فائنڈ اینڈ ریپلیس بار کو مستقل کھلے رہنے کے بجائے ضرورت کے وقت اوپر سے سلائیڈ ہونے والی اوورلے پٹی بنانا۔

---

### 🎛️ Phase 2: One-Thumb Quick Action Bar & Unified Bottom Drawer (کی بورڈ کوئیک بار اور باٹم ڈراور)
- [ ] **Keyboard-Docked Quick Action Bar (44dp):** کی بورڈ کے عین اوپر فوری فارمیٹنگ پٹی:
  - فوری بٹن: `[ B ]` بولڈ، `[ I ]` اٹالک، `[ U ]` انڈر لائن، `[ رنگ ]`، `[ بلٹ لسٹ ]`، `[ ہیڈنگ ]` اور `[ 🎛️ / A▲ ]` مینیو بٹن۔
- [ ] **Unified Expandable Bottom Sheet (مکمل باٹم شیٹ):** `[ 🎛️ ]` دبانے پر نیچے سے انگوٹھے کی پہنچ میں کھلنے والی ماڈرن Material 3 شیٹ، جس میں تمام 40+ ٹولز 4 واضح کیٹیگریز میں منظم ہوں:
  1. **Home (ٹیکسٹ فارمیٹنگ):** اردو نستعلیق و عربی فونٹس پریویو کے ساتھ، سائز سلائیڈر، رنگ، ہائی لائٹ، الائنمنٹ (RTL و LTR)، لائن اسپیسنگ۔
  2. **Insert (شامل کریں):** ٹیبل (Table)، تصاویر، ویکٹر شیپس، ڈیجیٹل دستخط (Signature)، ٹیکسٹ باکس، پیج بریک۔
  3. **Layout (صفحہ بندی):** پیج سائز (A4، Letter)، مارجنز، پیج کا رنگ، واٹر مارک، ہیڈر اور فوٹر۔
  4. **Review & AI (اسمارٹ ٹولز):** AI تحریر سنوار، اردو و انگریزی ترجمہ، گرامر، الفاظ کی گنتی، اسپیچ ٹو ٹیکسٹ (TTS)، کمنٹس۔

---

### 🧩 Phase 3: Context-Aware Quick Formatting for Tables & Media (اسمارٹ سیاق و سباق ٹول بار)
- [ ] **Smart Table Action Bar:** جب صارف ٹیبل کے کسی سیل میں ہو تو کوئیک بار خود بخود ٹیبل کے مخصوص ٹولز میں بدل جائے *(قطار شامل کریں، کالم ڈیلیٹ کریں، سیل بیک گراؤنڈ، بارڈر اسٹائل)*۔
- [ ] **Media & Shape Contextual Controls:** تصویر، شیپ یا ڈیجیٹل دستخط منتخب ہونے پر متعلقہ ٹولز کا فوری پاپ اپ *(سائز، ری سائز، ٹیکسٹ ریپنگ، ڈیلیٹ)*۔

---

### 📄 Phase 4: Floating Status Capsule & Canvas Reflow Modes (فلوٹنگ اسٹیٹس کیپسول اور ری فلو موڈ)
- [ ] **Floating Translucent Status Capsule:** نیچے کی مستقل گرے اسٹیٹس بار کو ہٹا کر کاغذ پر تیرتا ہوا چھوٹا سا کارڈ (`صفحہ 1 از 4 • 320 الفاظ • ✓ محفوظ`) جو ٹائپنگ کے دوران خود بخود فیڈ ہو جائے۔
- [ ] **Mobile Reflow vs. Print Layout:** بغیر زوم کیے موبائل کی چوڑائی کے مطابق آرام دہ مطالعہ کے لیے ری فلو موڈ اور ایک کلک پر A4 پرنٹ پیج ویو۔

---

### 🏠 Phase 5: Modern Home Dashboard & Document Cards (ہوم اسکرین اور فائل مینیجمنٹ)
- [ ] **Elevated Search & Filter Chips:** گوگل ڈاکس کی طرح سرچ بار اور فوری فلٹر چپس (`All`, `Word .docx`, `PDF`, `Urdu/RTL`)۔
- [ ] **Refreshed Template Carousel:** اردو درخواست، سادہ دستاویز، بزنس رپورٹ اور انوائس کے خوبصورت کارڈز۔
- [ ] **Rich File Cards:** ہر ڈاکومنٹ کارڈ پر فارمیٹ کا رنگین بیج (`.docx`, `.pdf`, `.txt`)، فائل سائز اور تاریخ کی صاف نمائش۔

---

## 📦 300-Line Codebase Modularization & Anti-Regression Procedures (تمام فائلز کو 300 لائنز سے کم رکھنے کا باقاعدہ طریقہ کار)

> ### 🎯 DIRECTIVE FOR AI AGENT / DEVELOPER:
> **NOTE: Feature Roadmap "Phases" (فیچرز کا روڈ میپ) aur Code Modularization "Procedures" (کوڈ ماڈیولرائزیشن) دو بالکل الگ الگ چیزیں ہیں:**
> - **Roadmap Phases:** نئی فنکشنلٹیز (Phase 1: Watermarks, Phase 2: Drawing & Vector Shapes, Phase 3: Text Wrapping, Phase 4: OCR Scanner).
> - **Modularization Procedures:** تمام فائلز کو 300 لائنز سے کم رکھنے کے لیے مخصوص ماڈیولز کی تقسیم (Procedure 1: UI Screens & Dialogs, Procedure 2: Canvas & Blocks, Procedure 3: ViewModel & Engine, Procedure 4: Services & I/O).
>
> **Procedure Specifications:**
> - **Procedure 1:** UI Screens aur Dialogs (8 Files) [COMPLETED]
> - **Procedure 2:** Editor Canvas, Block Views aur Toolbars (8 Files) [COMPLETED]
> - **Procedure 3:** ViewModel Coordinator aur Core Engine (5 Files) [COMPLETED]
> - **Procedure 4:** Data Layer, Storage, DOCX اور PDF Services (7-8 Files) [COMPLETED]
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

### 🔹 Procedure 2: Editor Canvas, Blocks & Toolbars ✅ [COMPLETED & VERIFIED]
Procedure 2 ki تمام Canvas, Blocks aur Formatting Toolbars files ko modularize kar ke har file ko **300 lines se kam** (84 - 278 lines) kar diya gaya hai:

- [x] **`WordRibbon.kt`**: 218 lines + `WordRibbonContent.kt` (196 lines)
- [x] **`DrawingBlockView.kt`**: 201 lines + `DrawingCanvasArea.kt` (167 lines) + `DrawRibbonControls.kt` (131 lines)
- [x] **`TableBlockView.kt`**: 200 lines + `TableHeaderBar.kt` (134 lines)
- [x] **`ShapeBlockView.kt`**: 215 lines (Vector shapes: Rect, Oval, Arrow, Star, Diamond, Callout)
- [x] **`CharacterFormattingBar.kt`**: 169 lines + `CharacterFormattingMenus.kt` (191 lines) + `ColorFormattingMenus.kt` (125 lines)
- [x] **`ParagraphFormattingBar.kt`**: 206 lines + `ParagraphFormattingDropdowns.kt` (184 lines)
- [x] **`ParagraphBlockView.kt`**: 278 lines + `ParagraphPrefixView.kt` (111 lines) + `ParagraphPillsView.kt` (109 lines)
- [x] **`PaginatedSheetView.kt`**: 84 lines + `SinglePageSheet.kt` (274 lines) + `PageHeaderFooter.kt` (99 lines)
- [x] **`PaginatedItemRenderer.kt`**: 158 lines + `PaginatedSpecialItemRenderer.kt` (248 lines)
- [x] **`SpecialBlocksView.kt`**: 214 lines + `SignatureBlockItem.kt` (154 lines)

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



