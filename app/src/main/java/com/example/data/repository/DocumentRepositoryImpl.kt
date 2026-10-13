package com.example.data.repository

import android.content.Context
import android.net.Uri
import com.example.core.error.DocumentError
import com.example.core.error.DocumentResult
import com.example.core.layout.MarginPreset
import com.example.core.layout.PageLayoutConfig
import com.example.core.layout.PageOrientation
import com.example.core.layout.PageSize
import com.example.data.local.DocumentDao
import com.example.data.model.DividerBlock
import com.example.data.model.DocumentEntity
import com.example.data.model.HeadingStyle
import com.example.data.model.InlineSpan
import com.example.data.model.ListType
import com.example.data.model.ParagraphBlock
import com.example.data.model.RichDocument
import com.example.data.model.TableCell
import com.example.data.model.TableBlock
import com.example.data.model.TextAlignment
import com.example.data.service.DocumentExportService
import com.example.data.service.DocumentImportService
import com.example.data.storage.DocumentStorageManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID

class DocumentRepositoryImpl(
    private val documentDao: DocumentDao,
    private val storageManager: DocumentStorageManager,
    private val exportService: DocumentExportService,
    private val importService: DocumentImportService
) : DocumentRepository {

    override fun getAllDocuments(): Flow<List<DocumentEntity>> {
        return documentDao.getAllDocuments()
    }

    override fun searchDocuments(query: String): Flow<List<DocumentEntity>> {
        return documentDao.searchDocuments(query)
    }

    override suspend fun getDocument(id: String): DocumentResult<RichDocument> {
        return storageManager.loadFromInternalStorage(id)
    }

    override suspend fun saveDocument(document: RichDocument): DocumentResult<Unit> =
        withContext(Dispatchers.IO) {
            val now = System.currentTimeMillis()
            val updatedDoc = document.copy(updatedAt = now)

            val saveResult = storageManager.saveToInternalStorage(updatedDoc)
            if (saveResult is DocumentResult.Failure) {
                return@withContext DocumentResult.Failure(saveResult.error)
            }

            val entity = DocumentEntity(
                id = updatedDoc.id,
                title = updatedDoc.title.ifBlank { "Untitled Document" },
                snippet = updatedDoc.plainTextSnippet,
                wordCount = updatedDoc.wordCount,
                characterCount = updatedDoc.characterCount,
                pageCount = updatedDoc.estimatedPageCount,
                createdAt = updatedDoc.createdAt,
                updatedAt = now,
                filePath = "documents/${updatedDoc.id}.swp",
                pageSize = updatedDoc.layoutConfig.pageSize.name,
                orientation = updatedDoc.layoutConfig.orientation.name,
                marginPreset = updatedDoc.layoutConfig.marginPreset.name,
                hasRecoveryDraft = false,
                isPasswordProtected = updatedDoc.isPasswordProtected
            )

            documentDao.insertOrUpdateDocument(entity)
            storageManager.clearRecoveryDraft(updatedDoc.id)
            DocumentResult.Success(Unit)
        }

    override suspend fun saveDocumentAs(
        originalDoc: RichDocument,
        newTitle: String
    ): DocumentResult<RichDocument> = withContext(Dispatchers.IO) {
        val newId = UUID.randomUUID().toString()
        val now = System.currentTimeMillis()
        val title = newTitle.ifBlank { "${originalDoc.title} (Copy)" }

        val newDoc = originalDoc.copy(
            id = newId,
            title = title,
            createdAt = now,
            updatedAt = now
        )

        val saveRes = saveDocument(newDoc)
        if (saveRes is DocumentResult.Failure) {
            return@withContext DocumentResult.Failure(saveRes.error)
        }
        DocumentResult.Success(newDoc)
    }

    override fun hasRecoveryDraft(id: String): Boolean {
        return storageManager.hasRecoveryDraft(id)
    }

    override suspend fun loadRecoveryDraft(id: String): DocumentResult<RichDocument> =
        withContext(Dispatchers.IO) {
            storageManager.loadRecoveryDraft(id)
        }

    override suspend fun saveRecoveryDraft(document: RichDocument): DocumentResult<Unit> =
        withContext(Dispatchers.IO) {
            val res = storageManager.saveRecoveryDraft(document)
            if (res is DocumentResult.Success) {
                documentDao.setHasRecoveryDraft(document.id, true)
                DocumentResult.Success(Unit)
            } else if (res is DocumentResult.Failure) {
                DocumentResult.Failure(res.error)
            } else {
                DocumentResult.Success(Unit)
            }
        }

    override suspend fun clearRecoveryDraft(id: String): DocumentResult<Unit> =
        withContext(Dispatchers.IO) {
            storageManager.clearRecoveryDraft(id)
            documentDao.setHasRecoveryDraft(id, false)
            DocumentResult.Success(Unit)
        }

    override suspend fun createDocument(
        title: String,
        template: TemplateType
    ): DocumentResult<RichDocument> = withContext(Dispatchers.IO) {
        val newId = UUID.randomUUID().toString()
        val now = System.currentTimeMillis()

        val doc = when (template) {
            TemplateType.BLANK -> RichDocument(
                id = newId,
                title = title.ifBlank { "Untitled Document" },
                createdAt = now,
                updatedAt = now,
                layoutConfig = PageLayoutConfig(
                    pageSize = PageSize.A4,
                    showHeader = false,
                    showFooter = false,
                    headerText = "",
                    footerText = "",
                    borderStyle = com.example.core.layout.PageBorderStyle.NONE
                ),
                blocks = listOf(
                    ParagraphBlock(text = "")
                )
            )

            TemplateType.BUSINESS_LETTER -> createBusinessLetterTemplate(newId, title, now)
            TemplateType.PROJECT_REPORT -> createProjectReportTemplate(newId, title, now)
            TemplateType.MEETING_MINUTES -> createMeetingMinutesTemplate(newId, title, now)
            TemplateType.PAGINATION_SHOWCASE -> createPaginationShowcaseTemplate(newId, title, now)
            TemplateType.URDU_DOCUMENT -> createUrduDocumentTemplate(newId, title, now)
        }

        val saveRes = saveDocument(doc)
        if (saveRes is DocumentResult.Failure) {
            return@withContext DocumentResult.Failure(saveRes.error)
        }
        DocumentResult.Success(doc)
    }

    override suspend fun duplicateDocument(id: String): DocumentResult<RichDocument> =
        withContext(Dispatchers.IO) {
            when (val originalRes = getDocument(id)) {
                is DocumentResult.Failure -> originalRes
                is DocumentResult.Success -> {
                    val original = originalRes.data
                    val newId = UUID.randomUUID().toString()
                    val now = System.currentTimeMillis()
                    val duplicated = original.copy(
                        id = newId,
                        title = "${original.title} (Copy)",
                        createdAt = now,
                        updatedAt = now
                    )
                    saveDocument(duplicated)
                    DocumentResult.Success(duplicated)
                }
            }
        }

    override suspend fun renameDocument(id: String, newTitle: String): DocumentResult<Unit> =
        withContext(Dispatchers.IO) {
            val now = System.currentTimeMillis()
            documentDao.renameDocument(id, newTitle, now)
            val docRes = storageManager.loadFromInternalStorage(id)
            if (docRes is DocumentResult.Success) {
                val updated = docRes.data.copy(title = newTitle, updatedAt = now)
                storageManager.saveToInternalStorage(updated)
            }
            DocumentResult.Success(Unit)
        }

    override suspend fun deleteDocument(id: String): DocumentResult<Unit> =
        withContext(Dispatchers.IO) {
            documentDao.deleteDocumentById(id)
            storageManager.deleteFromInternalStorage(id)
            DocumentResult.Success(Unit)
        }

    override suspend fun togglePinned(id: String, isPinned: Boolean): DocumentResult<Unit> =
        withContext(Dispatchers.IO) {
            documentDao.setPinned(id, isPinned)
            DocumentResult.Success(Unit)
        }

    override suspend fun toggleFavorite(id: String, isFavorite: Boolean): DocumentResult<Unit> =
        withContext(Dispatchers.IO) {
            documentDao.setFavorite(id, isFavorite)
            DocumentResult.Success(Unit)
        }

    override suspend fun exportToPdf(
        context: Context,
        document: RichDocument,
        outputFile: File
    ): DocumentResult<File> {
        return exportService.exportToPdf(context, document, outputFile)
    }

    override suspend fun exportPdfToUri(
        context: Context,
        document: RichDocument,
        targetUri: Uri
    ): DocumentResult<Unit> {
        return exportService.exportPdfToUri(context, document, targetUri)
    }

    override suspend fun exportToDocx(
        context: Context,
        document: RichDocument,
        outputFile: File
    ): DocumentResult<File> {
        return exportService.exportToDocx(context, document, outputFile)
    }

    override suspend fun exportDocxToUri(
        context: Context,
        document: RichDocument,
        targetUri: Uri
    ): DocumentResult<Unit> {
        return exportService.exportDocxToUri(context, document, targetUri)
    }

    override suspend fun exportToPlainText(
        context: Context,
        document: RichDocument,
        outputFile: File
    ): DocumentResult<File> {
        return exportService.exportToPlainText(context, document, outputFile)
    }

    override fun printDocument(context: Context, document: RichDocument) {
        exportService.printDocument(context, document)
    }

    override fun createPrintDocumentAdapter(context: Context, document: RichDocument): android.print.PrintDocumentAdapter {
        return exportService.createPrintDocumentAdapter(context, document)
    }

    override suspend fun importFromUri(context: Context, uri: Uri): DocumentResult<RichDocument> =
        withContext(Dispatchers.IO) {
            val importedResult = importService.importDocumentFromUri(context, uri)
            if (importedResult is DocumentResult.Success) {
                val doc = importedResult.data.copy(
                    id = UUID.randomUUID().toString(),
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis()
                )
                saveDocument(doc)
                DocumentResult.Success(doc)
            } else {
                importedResult
            }
        }

    override suspend fun importDocxFromUri(
        context: Context,
        uri: Uri
    ): DocumentResult<com.example.data.service.docx.DocxImportResult> = withContext(Dispatchers.IO) {
        val importedResult = importService.importDocxFromUri(context, uri)
        if (importedResult is DocumentResult.Success) {
            val doc = importedResult.data.document.copy(
                id = UUID.randomUUID().toString(),
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            saveDocument(doc)
            DocumentResult.Success(importedResult.data.copy(document = doc))
        } else {
            importedResult
        }
    }

    override suspend fun initializeDefaultDocumentsIfEmpty() = withContext(Dispatchers.IO) {
        val count = documentDao.getDocumentCount()
        if (count == 0) {
            // Seed a pristine blank document ready for typing
            createDocument("Untitled Document", TemplateType.BLANK)
        }
    }

    // ----------------------------------------------------
    // Starter Template Builders
    // ----------------------------------------------------

    private fun createBusinessLetterTemplate(id: String, title: String, timestamp: Long): RichDocument {
        val blocks = listOf(
            ParagraphBlock(
                text = "Apex Corporation • 100 Innovation Way • Suite 500 • New York, NY",
                alignment = TextAlignment.CENTER,
                headingStyle = HeadingStyle.SUBTITLE,
                spans = listOf(InlineSpan(start = 0, end = 16, isBold = true, textColorHex = "#185ABD"))
            ),
            DividerBlock(),
            ParagraphBlock(text = "October 10, 2026"),
            ParagraphBlock(text = "Mr. Jonathan Vance\nChief Operations Officer\nGlobal Horizons Ltd.\nLondon, United Kingdom"),
            ParagraphBlock(
                text = "Subject: Strategic Partnership and Technology Integration Proposal",
                headingStyle = HeadingStyle.HEADING_2,
                spans = listOf(InlineSpan(start = 0, end = 67, isBold = true))
            ),
            ParagraphBlock(text = "Dear Mr. Vance,"),
            ParagraphBlock(
                text = "Thank you for taking the time to discuss our potential collaboration during last week's technology summit. Following our constructive dialogue, I am pleased to formally submit our proposal for deploying our enterprise cloud architecture across your regional hubs."
            ),
            ParagraphBlock(
                text = "Key highlights of this strategic engagement include:\n• 40% reduction in document indexing overhead\n• Zero-latency synchronization across multi-platform terminals\n• Compliance with ISO/IEC 27001 data governance standards"
            ),
            ParagraphBlock(
                text = "We welcome the opportunity to schedule a technical walkthrough with your engineering leads at your earliest convenience."
            ),
            ParagraphBlock(text = "Sincerely,\n\nAlexander Wright\nVice President of Product Architecture\nApex Corporation")
        )

        return RichDocument(
            id = id,
            title = title.ifBlank { "Official Business Letter" },
            author = "Alexander Wright",
            createdAt = timestamp,
            updatedAt = timestamp,
            layoutConfig = PageLayoutConfig(
                pageSize = PageSize.A4,
                marginPreset = MarginPreset.NORMAL,
                headerText = "Apex Corporation - Official Correspondence"
            ),
            blocks = blocks
        )
    }

    private fun createProjectReportTemplate(id: String, title: String, timestamp: Long): RichDocument {
        val blocks = listOf(
            ParagraphBlock(
                text = "SMART WORD PROCESSOR",
                alignment = TextAlignment.CENTER,
                headingStyle = HeadingStyle.TITLE,
                spans = listOf(InlineSpan(start = 0, end = 20, isBold = true, textColorHex = "#185ABD"))
            ),
            ParagraphBlock(
                text = "Next-Generation Android Document Architecture Specification",
                alignment = TextAlignment.CENTER,
                headingStyle = HeadingStyle.SUBTITLE
            ),
            DividerBlock(),
            ParagraphBlock(
                text = "1. Executive Summary",
                headingStyle = HeadingStyle.HEADING_1
            ),
            ParagraphBlock(
                text = "Modern mobile workflows demand desktop-class word processing capabilities on touch devices. Smart Word Processor delivers a native Android application built on Jetpack Compose and Material 3, inspired by the familiar productivity workflows of Microsoft Word."
            ),
            ParagraphBlock(
                text = "2. System Architecture",
                headingStyle = HeadingStyle.HEADING_1
            ),
            ParagraphBlock(
                text = "The application implements a decoupled Model-View-ViewModel (MVVM) structure, guaranteeing isolated state handling, local Room database persistence, Storage Access Framework (SAF) file management, and hardware print integration."
            ),
            TableBlock(
                rowCount = 3,
                columnCount = 3,
                cells = listOf(
                    listOf(
                        TableCell("Module", isBold = true, backgroundColorHex = "#DCE7F8"),
                        TableCell("Technology", isBold = true, backgroundColorHex = "#DCE7F8"),
                        TableCell("Status", isBold = true, backgroundColorHex = "#DCE7F8")
                    ),
                    listOf(
                        TableCell("Core Editor"),
                        TableCell("Jetpack Compose"),
                        TableCell("Active")
                    ),
                    listOf(
                        TableCell("Export Engine"),
                        TableCell("PdfDocument & Print"),
                        TableCell("Verified")
                    )
                )
            ),
            ParagraphBlock(
                text = "3. Internationalization & Bidirectional Text",
                headingStyle = HeadingStyle.HEADING_1
            ),
            ParagraphBlock(
                text = "Full bidirectional (BiDi) layout engine support ensures native readability and typesetting for Right-to-Left scripts, including Urdu (اردو), Arabic, and Hebrew alongside Latin LTR typography."
            )
        )

        return RichDocument(
            id = id,
            title = title.ifBlank { "Executive Project Proposal" },
            author = "Lead Architect",
            createdAt = timestamp,
            updatedAt = timestamp,
            layoutConfig = PageLayoutConfig(
                pageSize = PageSize.A4,
                marginPreset = MarginPreset.NORMAL,
                headerText = "Smart Word • Technical Specification",
                footerText = "Confidential • Page {page} of {pages}"
            ),
            blocks = blocks
        )
    }

    private fun createMeetingMinutesTemplate(id: String, title: String, timestamp: Long): RichDocument {
        val blocks = listOf(
            ParagraphBlock(
                text = "Executive Strategy Board - Meeting Minutes",
                headingStyle = HeadingStyle.TITLE,
                spans = listOf(InlineSpan(start = 0, end = 40, isBold = true, textColorHex = "#185ABD"))
            ),
            ParagraphBlock(text = "Date: October 10, 2026 | Time: 10:00 AM EST | Location: Boardroom Alpha / Hybrid"),
            DividerBlock(),
            ParagraphBlock(text = "Attendees:", headingStyle = HeadingStyle.HEADING_2),
            ParagraphBlock(text = "• Sarah Jenkins (CEO)\n• Marcus Chen (CTO)\n• Elena Rostova (VP Design)\n• David Miller (Lead Counsel)"),
            ParagraphBlock(text = "Agenda Items & Decisions:", headingStyle = HeadingStyle.HEADING_2),
            ParagraphBlock(text = "1. Q4 Product Roadmap: Approved unanimously with Phase 2 OpenXML integration prioritized."),
            ParagraphBlock(text = "2. Security Audit Findings: Zero high-severity vulnerabilities identified in offline document storage layer."),
            ParagraphBlock(text = "3. Localization Rollout: RTL Nastaliq validation scheduled for next sprint cycle."),
            ParagraphBlock(text = "Action Items:", headingStyle = HeadingStyle.HEADING_2),
            ParagraphBlock(text = "• M. Chen: Finalize PDF export printer adapter benchmarks by Friday.\n• E. Rostova: Deliver ribbon iconography tokens for tablet and foldable form-factors.")
        )

        return RichDocument(
            id = id,
            title = title.ifBlank { "Executive Meeting Minutes" },
            author = "Recording Secretary",
            createdAt = timestamp,
            updatedAt = timestamp,
            layoutConfig = PageLayoutConfig(
                pageSize = PageSize.A4,
                marginPreset = MarginPreset.NORMAL
            ),
            blocks = blocks
        )
    }

    private fun createUrduDocumentTemplate(id: String, title: String, timestamp: Long): RichDocument {
        val blocks = listOf(
            ParagraphBlock(
                text = "اردو ادب اور جدید ورڈ پروسیسنگ (Smart Word Processor)",
                alignment = TextAlignment.CENTER,
                headingStyle = HeadingStyle.TITLE,
                isRtl = true,
                spans = listOf(
                    InlineSpan(start = 0, end = 31, isBold = true, textColorHex = "#185ABD"),
                    InlineSpan(start = 32, end = 53, isBold = true, isUnderline = true, textColorHex = "#0F4C81")
                )
            ),
            ParagraphBlock(
                text = "تحریر: ڈاکٹر محمد علیم | شعبہ اردو زبان و کمپیوٹر سائنس",
                alignment = TextAlignment.CENTER,
                headingStyle = HeadingStyle.SUBTITLE,
                isRtl = true
            ),
            DividerBlock(),
            ParagraphBlock(
                text = "1. مکمل اردو پیراگراف (Complete Urdu Paragraph)",
                headingStyle = HeadingStyle.HEADING_1,
                isRtl = true,
                alignment = TextAlignment.START
            ),
            ParagraphBlock(
                text = "اردو زبان برصغیر پاک و ہند کی ایک عظیم اور شیریں زبان ہے۔ اس میں شعری لطافت اور نثری وسعت بے مثال ہے۔ سمارٹ ورڈ پروسیسر میں نستعلیق خطاطی اور مکمل اعراب (زبر، زیر، پیش) کی حفاظت کی گئی ہے۔",
                alignment = TextAlignment.JUSTIFY,
                isRtl = true,
                lineSpacingMultiplier = 1.4f
            ),
            ParagraphBlock(
                text = "2. Complete English Paragraph",
                headingStyle = HeadingStyle.HEADING_1,
                isRtl = false,
                alignment = TextAlignment.START
            ),
            ParagraphBlock(
                text = "Smart Word Processor is engineered to deliver enterprise-grade typography and seamless multilingual document interchange. It guarantees full compliance with the Unicode Bidirectional Algorithm without destructive string reversals.",
                alignment = TextAlignment.JUSTIFY,
                isRtl = false,
                lineSpacingMultiplier = 1.25f
            ),
            ParagraphBlock(
                text = "3. اردو اور انگریزی کا باہمی ملاپ (Mixed Urdu and English)",
                headingStyle = HeadingStyle.HEADING_1,
                isRtl = true,
                alignment = TextAlignment.START
            ),
            ParagraphBlock(
                text = "جدید دور میں Artificial Intelligence اور Machine Learning نے زبانوں کی تحقیق کو انتہائی آسان بنا دیا ہے۔ ہم Smart Word Processor کا استعمال کر کے دونوں زبانوں میں ہم آہنگی پیدا کر سکتے ہیں۔",
                alignment = TextAlignment.JUSTIFY,
                isRtl = true,
                lineSpacingMultiplier = 1.4f
            ),
            ParagraphBlock(
                text = "4. اردو تحریر میں اعداد (Numbers within Urdu Text)",
                headingStyle = HeadingStyle.HEADING_1,
                isRtl = true,
                alignment = TextAlignment.START
            ),
            ParagraphBlock(
                text = "پاکستان 14 اگست 1947 کو وجود میں آیا۔ آج 2026 میں ہم ڈیجیٹل دور میں ترقی کر رہے ہیں جبکہ روایتی اعداد جیسے ۱، ۲، ۳ اور ۴ بھی کامیابی سے لکھے جا سکتے ہیں۔",
                alignment = TextAlignment.JUSTIFY,
                isRtl = true,
                lineSpacingMultiplier = 1.4f
            ),
            ParagraphBlock(
                text = "5. ای میل پتے اور ویب لنکس (Email Addresses and URLs)",
                headingStyle = HeadingStyle.HEADING_1,
                isRtl = true,
                alignment = TextAlignment.START
            ),
            ParagraphBlock(
                text = "کسی بھی مسئلے یا رائے کے لیے براہ کرم info@smartword.org پر ای میل بھیجیں یا ہماری ویب سائٹ https://smartword.org ملاحظہ فرمائیں۔",
                alignment = TextAlignment.JUSTIFY,
                isRtl = true,
                lineSpacingMultiplier = 1.4f,
                spans = listOf(
                    InlineSpan(start = 38, end = 57, isUnderline = true, textColorHex = "#185ABD", linkUrl = "mailto:info@smartword.org"),
                    InlineSpan(start = 82, end = 103, isUnderline = true, textColorHex = "#185ABD", linkUrl = "https://smartword.org")
                )
            ),
            ParagraphBlock(
                text = "6. اردو بلٹ لسٹ اور فہرست (Urdu Bullet and Numbered Lists)",
                headingStyle = HeadingStyle.HEADING_1,
                isRtl = true,
                alignment = TextAlignment.START
            ),
            ParagraphBlock(
                text = "دائیں سے بائیں خطاطی اور کرسر کی درست سمت بندی",
                alignment = TextAlignment.START,
                isRtl = true,
                listType = ListType.BULLET,
                lineSpacingMultiplier = 1.35f
            ),
            ParagraphBlock(
                text = "بنڈل شدہ جمیل نوری و نوٹو نستعلیق فونٹ کے ذریعے مکمل ڈسپلے",
                alignment = TextAlignment.START,
                isRtl = true,
                listType = ListType.BULLET,
                lineSpacingMultiplier = 1.35f
            ),
            ParagraphBlock(
                text = "پی ڈی ایف پرنٹنگ اور اے فور صفحات پر خط کی خوبصورتی",
                alignment = TextAlignment.START,
                isRtl = true,
                listType = ListType.BULLET,
                lineSpacingMultiplier = 1.35f
            ),
            ParagraphBlock(
                text = "7. جلی اور خط کشیدہ اردو تحریر (Bold and Underlined Urdu)",
                headingStyle = HeadingStyle.HEADING_1,
                isRtl = true,
                alignment = TextAlignment.START
            ),
            ParagraphBlock(
                text = "یہ عبارت خاص طور پر جلی حروف اور خط کشیدہ انداز میں پیش کی گئی ہے تاکہ واضح رہے۔",
                alignment = TextAlignment.START,
                isRtl = true,
                lineSpacingMultiplier = 1.4f,
                spans = listOf(
                    InlineSpan(start = 22, end = 31, isBold = true, textColorHex = "#B91C1C"),
                    InlineSpan(start = 36, end = 49, isUnderline = true, isBold = true, textColorHex = "#1D4ED8")
                )
            ),
            DividerBlock(),
            ParagraphBlock(
                text = "تمام حقوق برائے سمارٹ ورڈ پروسیسر محفوظ ہیں • نسخہ 1.0",
                alignment = TextAlignment.CENTER,
                headingStyle = HeadingStyle.QUOTE,
                isRtl = true
            )
        )

        return RichDocument(
            id = id,
            title = title.ifBlank { "اردو اور انگریزی دستاویز (Multilingual Document)" },
            author = "ڈاکٹر محمد علیم",
            createdAt = timestamp,
            updatedAt = timestamp,
            layoutConfig = PageLayoutConfig(
                pageSize = PageSize.A4,
                marginPreset = MarginPreset.NORMAL,
                headerText = "سمارٹ ورڈ پروسیسر • Smart Word Processor",
                footerText = "صفحہ {page} از {pages}"
            ),
            blocks = blocks
        )
    }

    private fun createPaginationShowcaseTemplate(id: String, title: String, timestamp: Long): RichDocument {
        val blocks = listOf(
            ParagraphBlock(
                text = "SMART WORD PROCESSOR",
                alignment = TextAlignment.CENTER,
                headingStyle = HeadingStyle.TITLE,
                spans = listOf(InlineSpan(start = 0, end = 20, isBold = true, textColorHex = "#185ABD"))
            ),
            ParagraphBlock(
                text = "Comprehensive Document Layout & Typographic Pagination Specification",
                alignment = TextAlignment.CENTER,
                headingStyle = HeadingStyle.SUBTITLE
            ),
            DividerBlock(),
            ParagraphBlock(
                text = "Author: Architecture Core Team • Date: October 2026 • Status: Production Ready",
                alignment = TextAlignment.CENTER,
                headingStyle = HeadingStyle.QUOTE
            ),
            ParagraphBlock(
                text = "Abstract: This specification defines the physical page layout architecture implemented in Smart Word Processor. It details the page partitioning mechanism across standard ISO and ANSI paper formats (A4, A5, Letter, Legal), margin models, paragraph line splitting, header/footer lifecycle, and print output pipelines."
            ),
            com.example.data.model.PageBreakBlock(), // Page 1 -> Page 2

            ParagraphBlock(
                text = "1. Supported Paper Dimensions & Metrics",
                headingStyle = HeadingStyle.HEADING_1
            ),
            ParagraphBlock(
                text = "Smart Word Processor provides precise physical paper size calculations based on 72 DPI PostScript points, guaranteeing zero discrepancy between on-screen interactive rendering and printed PDF outputs."
            ),
            TableBlock(
                rowCount = 5,
                columnCount = 4,
                cells = listOf(
                    listOf(
                        TableCell("Paper Format", isBold = true, backgroundColorHex = "#E2E8F0"),
                        TableCell("Dimensions (mm)", isBold = true, backgroundColorHex = "#E2E8F0"),
                        TableCell("Width × Height (pt)", isBold = true, backgroundColorHex = "#E2E8F0"),
                        TableCell("Standard Use Case", isBold = true, backgroundColorHex = "#E2E8F0")
                    ),
                    listOf(
                        TableCell("ISO A4"),
                        TableCell("210 × 297 mm"),
                        TableCell("595 × 842 pt"),
                        TableCell("International standard correspondence & reports")
                    ),
                    listOf(
                        TableCell("US Letter"),
                        TableCell("215.9 × 279.4 mm"),
                        TableCell("612 × 792 pt"),
                        TableCell("North American business & academic documents")
                    ),
                    listOf(
                        TableCell("US Legal"),
                        TableCell("215.9 × 355.6 mm"),
                        TableCell("612 × 1008 pt"),
                        TableCell("Contracts, legal briefs & governmental deeds")
                    ),
                    listOf(
                        TableCell("ISO A5"),
                        TableCell("148 × 210 mm"),
                        TableCell("420 × 595 pt"),
                        TableCell("Handbooks, booklets & compact memo pads")
                    )
                )
            ),
            ParagraphBlock(
                text = "2. Margin Systems and Printable Boundaries",
                headingStyle = HeadingStyle.HEADING_2
            ),
            ParagraphBlock(
                text = "Margins define the printable boundary within each sheet. Presets include Normal (25.4 mm / 1.0 in on all edges), Narrow (12.7 mm / 0.5 in), Moderate (25.4 mm vertical, 19.1 mm horizontal), and Wide (25.4 mm vertical, 50.8 mm horizontal), alongside custom millimetric margins."
            ),
            com.example.data.model.PageBreakBlock(), // Page 2 -> Page 3

            ParagraphBlock(
                text = "3. Dynamic Pagination & Text Wrap Engine",
                headingStyle = HeadingStyle.HEADING_1
            ),
            ParagraphBlock(
                text = "Unlike naive mockups that draw decorative rectangles behind continuous text fields, the PaginationEngine evaluates font metrics (baseline, ascent, descent, and line spacing multipliers) and measures available vertical space per page. When a paragraph exceeds available page height, it calculates line-boundary splits without orphan words or awkwardly clipped glyphs."
            ),
            ParagraphBlock(
                text = "4. Header, Footer, and Dynamic Numbering",
                headingStyle = HeadingStyle.HEADING_2
            ),
            ParagraphBlock(
                text = "Headers and footers integrate dynamic token substitution. Tokens such as {page} and {pages} evaluate automatically into Arabic numerals (1, 2, 3), lowercase Roman (i, ii, iii), uppercase Roman (I, II, III), or alphabet characters (A, B, C). Furthermore, the Different First Page option enables professional title and cover page layouts by suppressing headers and footers on page one."
            ),
            com.example.data.model.PageBreakBlock(), // Page 3 -> Page 4

            ParagraphBlock(
                text = "5. Verification and Export Fidelity",
                headingStyle = HeadingStyle.HEADING_1
            ),
            ParagraphBlock(
                text = "All layout attributes—including background fills, borders, margins, headers, and footers—persist in the document package and translate identically to Android PdfDocument output and hardware printing."
            ),
            DividerBlock(),
            ParagraphBlock(
                text = "Document prepared and verified by Smart Word Processor Engine v1.0 • Ready for multi-page export.",
                alignment = TextAlignment.CENTER,
                headingStyle = HeadingStyle.QUOTE
            )
        )

        return RichDocument(
            id = id,
            title = title.ifBlank { "Multi-Page Architecture Specification" },
            author = "Architecture Core Team",
            createdAt = timestamp,
            updatedAt = timestamp,
            layoutConfig = PageLayoutConfig(
                pageSize = PageSize.A4,
                orientation = PageOrientation.PORTRAIT,
                marginPreset = MarginPreset.NORMAL,
                showHeader = false,
                showFooter = false,
                headerText = "",
                footerText = "",
                differentFirstPage = false,
                pageBackgroundHex = "#FFFFFF",
                borderStyle = com.example.core.layout.PageBorderStyle.NONE,
                borderColorHex = "#CBD5E1"
            ),
            blocks = blocks
        )
    }

    override fun getSnapshots(documentId: String): Flow<List<com.example.data.model.DocumentRevisionSnapshot>> {
        return documentDao.getSnapshotsForDocument(documentId).map { entities ->
            entities.map { entity ->
                val doc = try {
                    storageManager.deserializeFromJson(entity.documentJson)
                } catch (_: Exception) {
                    null
                }
                com.example.data.model.DocumentRevisionSnapshot(
                    id = entity.id,
                    timestamp = entity.timestamp,
                    label = entity.label,
                    wordCount = entity.wordCount,
                    characterCount = entity.characterCount,
                    snippet = entity.snippet,
                    snapshotDoc = doc
                )
            }
        }
    }

    override suspend fun saveSnapshot(
        documentId: String,
        snapshot: com.example.data.model.DocumentRevisionSnapshot
    ): DocumentResult<Unit> = withContext(Dispatchers.IO) {
        val snapDoc = snapshot.snapshotDoc ?: run {
            when (val res = getDocument(documentId)) {
                is DocumentResult.Success -> res.data
                is DocumentResult.Failure -> return@withContext DocumentResult.Failure(res.error)
            }
        }
        val jsonStr = storageManager.serializeToJson(snapDoc)
        val entity = com.example.data.model.DocumentSnapshotEntity(
            id = snapshot.id,
            documentId = documentId,
            timestamp = snapshot.timestamp,
            label = snapshot.label,
            wordCount = snapshot.wordCount,
            characterCount = snapshot.characterCount,
            snippet = snapshot.snippet,
            documentJson = jsonStr
        )
        documentDao.insertSnapshot(entity)
        DocumentResult.Success(Unit)
    }

    override suspend fun getSnapshot(snapshotId: String): DocumentResult<com.example.data.model.DocumentRevisionSnapshot> = withContext(Dispatchers.IO) {
        val entity = documentDao.getSnapshotById(snapshotId)
            ?: return@withContext DocumentResult.Failure(DocumentError.DocumentNotFound(snapshotId))
        val doc = try {
            storageManager.deserializeFromJson(entity.documentJson)
        } catch (e: Exception) {
            return@withContext DocumentResult.Failure(DocumentError.CorruptDocumentError("Failed to decode snapshot document: ${e.message}"))
        }
        DocumentResult.Success(
            com.example.data.model.DocumentRevisionSnapshot(
                id = entity.id,
                timestamp = entity.timestamp,
                label = entity.label,
                wordCount = entity.wordCount,
                characterCount = entity.characterCount,
                snippet = entity.snippet,
                snapshotDoc = doc
            )
        )
    }

    override suspend fun deleteSnapshot(snapshotId: String): DocumentResult<Unit> = withContext(Dispatchers.IO) {
        documentDao.deleteSnapshotById(snapshotId)
        DocumentResult.Success(Unit)
    }
}
