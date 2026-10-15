package com.example.data.repository

import com.example.core.layout.MarginPreset
import com.example.core.layout.PageBorderStyle
import com.example.core.layout.PageLayoutConfig
import com.example.core.layout.PageOrientation
import com.example.core.layout.PageSize
import com.example.data.model.DividerBlock
import com.example.data.model.HeadingStyle
import com.example.data.model.InlineSpan
import com.example.data.model.PageBreakBlock
import com.example.data.model.ParagraphBlock
import com.example.data.model.RichDocument
import com.example.data.model.TableCell
import com.example.data.model.TableBlock
import com.example.data.model.TextAlignment

/**
 * Showcase starter templates: multi-page pagination showcase.
 */
object DocumentShowcaseTemplates {

    fun createUrduDocumentTemplate(id: String, title: String, timestamp: Long): RichDocument =
        DocumentUrduTemplate.createUrduDocumentTemplate(id, title, timestamp)

    fun createPaginationShowcaseTemplate(id: String, title: String, timestamp: Long): RichDocument {
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
            PageBreakBlock(),

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
            PageBreakBlock(),

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
            PageBreakBlock(),

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
                borderStyle = PageBorderStyle.NONE,
                borderColorHex = "#CBD5E1"
            ),
            blocks = blocks
        )
    }
}
