package com.example.data.repository

import com.example.core.layout.MarginPreset
import com.example.core.layout.PageLayoutConfig
import com.example.core.layout.PageSize
import com.example.data.model.DividerBlock
import com.example.data.model.HeadingStyle
import com.example.data.model.InlineSpan
import com.example.data.model.ParagraphBlock
import com.example.data.model.RichDocument
import com.example.data.model.TableCell
import com.example.data.model.TableBlock
import com.example.data.model.TextAlignment

/**
 * Standard starter templates: Business Letter, Project Report, and Meeting Minutes.
 */
object DocumentStandardTemplates {

    fun createBusinessLetterTemplate(id: String, title: String, timestamp: Long): RichDocument {
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

    fun createProjectReportTemplate(id: String, title: String, timestamp: Long): RichDocument {
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

    fun createMeetingMinutesTemplate(id: String, title: String, timestamp: Long): RichDocument {
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
}
