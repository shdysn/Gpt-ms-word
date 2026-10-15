package com.example.data.repository

import com.example.core.layout.PageBorderStyle
import com.example.core.layout.PageLayoutConfig
import com.example.core.layout.PageSize
import com.example.data.model.ParagraphBlock
import com.example.data.model.RichDocument

/**
 * Factory for creating initialized RichDocument instances from template presets.
 */
object DocumentTemplateFactory {

    fun createTemplate(template: TemplateType, id: String, title: String, timestamp: Long): RichDocument {
        return when (template) {
            TemplateType.BLANK -> RichDocument(
                id = id,
                title = title.ifBlank { "Untitled Document" },
                createdAt = timestamp,
                updatedAt = timestamp,
                layoutConfig = PageLayoutConfig(
                    pageSize = PageSize.A4,
                    showHeader = false,
                    showFooter = false,
                    headerText = "",
                    footerText = "",
                    borderStyle = PageBorderStyle.NONE
                ),
                blocks = listOf(
                    ParagraphBlock(text = "")
                )
            )
            TemplateType.BUSINESS_LETTER -> DocumentStandardTemplates.createBusinessLetterTemplate(id, title, timestamp)
            TemplateType.PROJECT_REPORT -> DocumentStandardTemplates.createProjectReportTemplate(id, title, timestamp)
            TemplateType.MEETING_MINUTES -> DocumentStandardTemplates.createMeetingMinutesTemplate(id, title, timestamp)
            TemplateType.PAGINATION_SHOWCASE -> DocumentShowcaseTemplates.createPaginationShowcaseTemplate(id, title, timestamp)
            TemplateType.URDU_DOCUMENT -> DocumentShowcaseTemplates.createUrduDocumentTemplate(id, title, timestamp)
        }
    }
}
