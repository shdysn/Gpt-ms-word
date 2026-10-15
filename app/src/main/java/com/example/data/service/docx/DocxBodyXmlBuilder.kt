package com.example.data.service.docx

import com.example.core.typography.BidiUtils
import com.example.data.model.DividerBlock
import com.example.data.model.HeadingStyle
import com.example.data.model.ImageBlock
import com.example.data.model.ListType
import com.example.data.model.PageBreakBlock
import com.example.data.model.ParagraphBlock
import com.example.data.model.RichDocument
import com.example.data.model.TableBlock
import com.example.data.model.TextAlignment

/**
 * Builds the main `word/document.xml` body content for DOCX export.
 */
object DocxBodyXmlBuilder {

    data class EmbeddedImage(
        val relId: String,
        val entryPath: String,
        val contentType: String,
        val data: ByteArray,
        val widthEmu: Long,
        val heightEmu: Long
    )

    fun buildDocumentXml(
        document: RichDocument,
        images: List<EmbeddedImage>,
        hyperlinks: Map<String, String> = emptyMap()
    ): String {
        val headingPageMap = try {
            com.example.core.layout.PaginationEngine.computeHeadingPageMap(document)
        } catch (_: Exception) {
            emptyMap<String, Int>()
        }

        val sb = StringBuilder()
        sb.append("""<?xml version="1.0" encoding="UTF-8" standalone="yes"?>""")
        sb.append("""<w:document xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main" """)
        sb.append("""xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships" """)
        sb.append("""xmlns:wp="http://schemas.openxmlformats.org/drawingml/2006/wordprocessingDrawing" """)
        sb.append("""xmlns:a="http://schemas.openxmlformats.org/drawingml/2006/main" """)
        sb.append("""xmlns:pic="http://schemas.openxmlformats.org/drawingml/2006/picture">""")
        sb.append("""<w:body>""")

        var imgIdx = 0
        for (block in document.blocks) {
            when (block) {
                is ParagraphBlock -> sb.append(buildParagraphXml(block, hyperlinks))
                is TableBlock -> sb.append(DocxTableXmlBuilder.buildTableXml(block))
                is ImageBlock -> {
                    val embeddedImg = images.getOrNull(imgIdx)
                    if (embeddedImg != null) {
                        sb.append(buildImageXml(block, embeddedImg))
                        imgIdx++
                    }
                }
                is DividerBlock -> sb.append("""<w:p><w:pPr><w:pBdr><w:bottom w:val="single" w:sz="6" w:space="1" w:color="CBD5E1"/></w:pBdr><w:spacing w:after="240"/></w:pPr></w:p>""")
                is PageBreakBlock -> sb.append("""<w:p><w:r><w:br w:type="page"/></w:r></w:p>""")
                is com.example.data.model.ColumnBreakBlock -> sb.append("""<w:p><w:r><w:br w:type="column"/></w:r></w:p>""")
                is com.example.data.model.SectionBreakBlock -> {
                    val orient = if (block.orientation.equals("LANDSCAPE", ignoreCase = true)) "landscape" else "portrait"
                    val (w, h) = if (orient == "landscape") 16838 to 11906 else 11906 to 16838
                    val typeVal = if (block.isNextPage) "nextPage" else "continuous"
                    sb.append("""<w:p><w:pPr><w:sectPr><w:type w:val="$typeVal"/><w:pgSz w:w="$w" w:h="$h" w:orient="$orient"/></w:sectPr></w:pPr></w:p>""")
                }
                is com.example.data.model.TableOfContentsBlock -> {
                    sb.append("""<w:p><w:pPr><w:pStyle w:val="Heading1"/><w:spacing w:before="240" w:after="120"/></w:pPr><w:r><w:rPr><w:b/><w:sz w:val="32"/><w:color w:val="0F172A"/></w:rPr><w:t>${DocxXmlBuilders.escapeXml(block.title)}</w:t></w:r></w:p>""")
                    for (b in document.blocks) {
                        if (b is ParagraphBlock && b.headingStyle != HeadingStyle.NORMAL && b.text.isNotBlank()) {
                            val indent = if (b.headingStyle == HeadingStyle.HEADING_2) 360 else if (b.headingStyle == HeadingStyle.HEADING_3) 720 else 0
                            val pageNum = headingPageMap[b.id] ?: 1
                            sb.append("""<w:p><w:pPr><w:tabs><w:tab w:val="right" w:leader="dot" w:pos="9360"/></w:tabs><w:ind w:left="$indent"/><w:spacing w:after="80"/></w:pPr><w:r><w:rPr><w:sz w:val="22"/><w:color w:val="1E293B"/></w:rPr><w:t>${DocxXmlBuilders.escapeXml(b.text)}</w:t></w:r><w:r><w:tab/></w:r><w:r><w:rPr><w:b/><w:sz w:val="20"/><w:color w:val="475569"/></w:rPr><w:t>$pageNum</w:t></w:r></w:p>""")
                        }
                    }
                }
                is com.example.data.model.SignatureBlock -> {
                    val cleanColor = block.strokeColorHex.removePrefix("#")
                    sb.append("""<w:p><w:pPr><w:spacing w:before="360" w:after="60"/></w:pPr><w:r><w:rPr><w:b/><w:sz w:val="18"/><w:color w:val="1E40AF"/></w:rPr><w:t>✓ VERIFIED DIGITAL SIGNATURE</w:t></w:r></w:p>""")
                    sb.append("""<w:p><w:pPr><w:spacing w:before="60" w:after="60"/></w:pPr><w:r><w:rPr><w:i/><w:sz w:val="32"/><w:color w:val="$cleanColor"/></w:rPr><w:t>${DocxXmlBuilders.escapeXml(block.signerName.ifBlank { "Authorized Signature" })}</w:t></w:r></w:p>""")
                    sb.append("""<w:p><w:pPr><w:pBdr><w:bottom w:val="single" w:sz="12" w:space="1" w:color="$cleanColor"/></w:pBdr><w:spacing w:after="100"/></w:pPr></w:p>""")
                    sb.append("""<w:p><w:pPr><w:spacing w:after="60"/></w:pPr><w:r><w:rPr><w:b/><w:sz w:val="18"/><w:color w:val="334155"/></w:rPr><w:t>${DocxXmlBuilders.escapeXml(block.signerTitle)}</w:t></w:r>${if (block.signedDate.isNotBlank()) """<w:r><w:rPr><w:sz w:val="16"/><w:color w:val="64748B"/></w:rPr><w:t>  •  Date: ${DocxXmlBuilders.escapeXml(block.signedDate)}</w:t></w:r>""" else ""}</w:p>""")
                }
                is com.example.data.model.ShapeBlock -> {
                    val cleanBdr = block.borderColorHex.removePrefix("#")
                    val cleanBg = block.backgroundColorHex.removePrefix("#")
                    sb.append("""<w:p><w:pPr><w:pBdr><w:left w:val="single" w:sz="24" w:space="4" w:color="$cleanBdr"/></w:pBdr><w:shd w:val="clear" w:color="auto" w:fill="$cleanBg"/><w:spacing w:before="120" w:after="120"/></w:pPr><w:r><w:rPr><w:b/><w:color w:val="$cleanBdr"/></w:rPr><w:t>${DocxXmlBuilders.escapeXml(block.title)}</w:t></w:r>${if (block.content.isNotBlank()) "<w:r><w:br/></w:r><w:r><w:t xml:space=\"preserve\">${DocxXmlBuilders.escapeXml(block.content)}</w:t></w:r>" else ""}</w:p>""")
                }
                is com.example.data.model.DrawingBlock -> {
                    sb.append("""<w:p><w:pPr><w:pBdr><w:bottom w:val="dashed" w:sz="6" w:space="2" w:color="94A3B8"/></w:pBdr><w:spacing w:before="120" w:after="60"/></w:pPr><w:r><w:rPr><w:i/><w:sz w:val="18"/><w:color w:val="64748B"/></w:rPr><w:t>✎ [Handwritten Inking Canvas (${block.strokes.size} strokes)]</w:t></w:r></w:p>""")
                }
                is com.example.data.model.TextBoxBlock -> {
                    val cleanBdr = block.borderColorHex.removePrefix("#")
                    val cleanBg = block.backgroundColorHex.removePrefix("#")
                    val esc = DocxXmlBuilders.escapeXml(block.text)
                    sb.append("""<w:p><w:pPr><w:pBdr><w:left w:val="single" w:sz="18" w:space="4" w:color="$cleanBdr"/><w:right w:val="single" w:sz="18" w:space="4" w:color="$cleanBdr"/><w:top w:val="single" w:sz="18" w:space="4" w:color="$cleanBdr"/><w:bottom w:val="single" w:sz="18" w:space="4" w:color="$cleanBdr"/></w:pBdr><w:shd w:val="clear" w:color="auto" w:fill="$cleanBg"/><w:spacing w:before="140" w:after="140"/></w:pPr><w:r><w:t xml:space="preserve">$esc</w:t></w:r></w:p>""")
                }
                is com.example.data.model.FootnoteBlock -> {
                    sb.append("""<w:p><w:pPr><w:pBdr><w:top w:val="single" w:sz="6" w:space="1" w:color="CBD5E1"/></w:pBdr><w:spacing w:before="120" w:after="60"/></w:pPr><w:r><w:rPr><w:vertAlign w:val="superscript"/><w:b/></w:rPr><w:t>[${block.noteNumber}] </w:t></w:r><w:r><w:rPr><w:sz w:val="18"/><w:color w:val="475569"/></w:rPr><w:t>${DocxXmlBuilders.escapeXml(block.noteContent)}</w:t></w:r></w:p>""")
                }
            }
        }

        sb.append(DocxSectionXmlBuilder.buildSectionPropertiesXml(document))
        sb.append("""</w:body></w:document>""")
        return sb.toString()
    }

    private fun buildParagraphXml(block: ParagraphBlock, hyperlinks: Map<String, String>): String {
        val sb = StringBuilder()
        sb.append("<w:p>")
        val isRtl = block.isRtl || BidiUtils.detectParagraphIsRtl(block.text)
        sb.append("<w:pPr>")

        val styleVal = when (block.headingStyle) {
            HeadingStyle.TITLE -> "Title"
            HeadingStyle.SUBTITLE -> "Subtitle"
            HeadingStyle.HEADING_1 -> "Heading1"
            HeadingStyle.HEADING_2 -> "Heading2"
            HeadingStyle.HEADING_3 -> "Heading3"
            HeadingStyle.QUOTE -> "Quote"
            HeadingStyle.NORMAL -> "Normal"
        }
        sb.append("""<w:pStyle w:val="$styleVal"/>""")

        val jcVal = when (block.alignment) {
            TextAlignment.START -> if (isRtl) "right" else "left"
            TextAlignment.CENTER -> "center"
            TextAlignment.END -> if (isRtl) "left" else "right"
            TextAlignment.JUSTIFY -> "both"
        }
        sb.append("""<w:jc w:val="$jcVal"/>""")
        if (isRtl) sb.append("<w:bidi/>")

        when (block.listType) {
            ListType.BULLET -> sb.append("""<w:numPr><w:ilvl w:val="0"/><w:numId w:val="1"/></w:numPr>""")
            ListType.NUMBERED -> sb.append("""<w:numPr><w:ilvl w:val="0"/><w:numId w:val="2"/></w:numPr>""")
            ListType.NONE -> Unit
        }

        val leftTwips = if (block.leftIndentPt > 0f) (block.leftIndentPt * 20f).toInt() else if (block.indentationLevel > 0) block.indentationLevel * 720 else 0
        if (leftTwips > 0) sb.append("""<w:ind w:left="$leftTwips"/>""")

        val beforeTwips = if (block.spaceBeforePt > 0f) (block.spaceBeforePt * 20f).toInt() else 0
        val afterTwips = if (block.spaceAfterPt > 0f) {
            (block.spaceAfterPt * 20f).toInt()
        } else {
            when (block.headingStyle) {
                HeadingStyle.TITLE -> 240
                HeadingStyle.HEADING_1 -> 160
                HeadingStyle.HEADING_2 -> 120
                else -> 100
            }
        }
        val lineTwips = (240 * block.lineSpacingMultiplier).toInt()
        sb.append("""<w:spacing w:before="$beforeTwips" w:after="$afterTwips" w:line="$lineTwips" w:lineRule="auto"/>""")
        sb.append("</w:pPr>")

        val runs = DocxRunSegmenter.segmentParagraphRuns(block.text, block.spans)
        for (run in runs) {
            val runSb = StringBuilder()
            runSb.append("<w:r><w:rPr>")
            if (run.isBold) runSb.append("<w:b/>")
            if (run.isItalic) runSb.append("<w:i/>")
            if (run.isUnderline) runSb.append("""<w:u w:val="single"/>""")
            if (run.isStrikethrough) runSb.append("<w:strike/>")
            if (run.isSuperscript) runSb.append("""<w:vertAlign w:val="superscript"/>""")
            if (run.isSubscript) runSb.append("""<w:vertAlign w:val="subscript"/>""")
            if (run.fontSizeSp != null) {
                val halfPts = (run.fontSizeSp * 2).toInt()
                runSb.append("""<w:sz w:val="$halfPts"/><w:szCs w:val="$halfPts"/>""")
            }
            if (run.textColorHex != null) runSb.append("""<w:color w:val="${run.textColorHex.removePrefix("#")}"/>""")
            if (run.highlightColorHex != null) runSb.append("""<w:highlight w:val="${DocxRunSegmenter.mapColorToWordHighlight(run.highlightColorHex)}"/>""")
            if (run.fontFamilyName != null) {
                val font = DocxXmlBuilders.escapeXml(run.fontFamilyName)
                runSb.append("""<w:rFonts w:ascii="$font" w:hAnsi="$font" w:cs="$font"/>""")
            } else if (isRtl) {
                runSb.append("""<w:rFonts w:ascii="Calibri" w:hAnsi="Calibri" w:cs="Noto Nastaliq Urdu"/>""")
            }
            if (isRtl) runSb.append("<w:rtl/>")
            runSb.append("</w:rPr>")
            runSb.append("""<w:t xml:space="preserve">${DocxXmlBuilders.escapeXml(run.text)}</w:t></w:r>""")

            val relId = run.linkUrl?.let { hyperlinks[it] }
            if (relId != null) {
                sb.append("""<w:hyperlink r:id="$relId">$runSb</w:hyperlink>""")
            } else {
                sb.append(runSb)
            }
        }

        sb.append("</w:p>")
        return sb.toString()
    }

    private fun buildImageXml(block: ImageBlock, img: EmbeddedImage): String {
        val cx = img.widthEmu
        val cy = img.heightEmu
        val imgAlign = when (block.alignment) {
            TextAlignment.START -> "left"
            TextAlignment.END -> "right"
            else -> "center"
        }
        val imageP = """<w:p><w:pPr><w:jc w:val="$imgAlign"/></w:pPr><w:r><w:drawing><wp:inline distT="0" distB="0" distL="0" distR="0"><wp:extent cx="$cx" cy="$cy"/><wp:docPr id="1" name="${DocxXmlBuilders.escapeXml(block.caption.ifBlank { "Picture" })}"/><a:graphic><a:graphicData uri="http://schemas.openxmlformats.org/drawingml/2006/picture"><pic:pic><pic:nvPicPr><pic:cNvPr id="0" name="Picture"/><pic:cNvPicPr/></pic:nvPicPr><pic:blipFill><a:blip r:embed="${img.relId}"/><a:stretch><a:fillRect/></a:stretch></pic:blipFill><pic:spPr><a:xfrm><a:off x="0" y="0"/><a:ext cx="$cx" cy="$cy"/></a:xfrm><a:prstGeom prst="rect"><a:avLst/></a:prstGeom></pic:spPr></pic:pic></a:graphicData></a:graphic></wp:inline></w:drawing></w:r></w:p>"""
        val captionP = if (block.caption.isNotBlank()) {
            """<w:p><w:pPr><w:jc w:val="$imgAlign"/></w:pPr><w:r><w:rPr><w:i/><w:sz w:val="18"/><w:color w:val="666666"/></w:rPr><w:t xml:space="preserve">${DocxXmlBuilders.escapeXml(block.caption)}</w:t></w:r></w:p>"""
        } else ""
        return imageP + captionP
    }
}
