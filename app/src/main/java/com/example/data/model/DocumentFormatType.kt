package com.example.data.model

/**
 * Supported file format representations.
 */
enum class DocumentFormatType(val extension: String, val mimeType: String, val displayName: String) {
    SWP_NATIVE("swp", "application/json", "Smart Word Document (.swp)"),
    DOCX("docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document", "Microsoft Word Document (.docx)"),
    PDF("pdf", "application/pdf", "Portable Document Format (.pdf)"),
    PLAIN_TEXT("txt", "text/plain", "Plain Text Document (.txt)"),
    MARKDOWN("md", "text/markdown", "Markdown Document (.md)")
}
