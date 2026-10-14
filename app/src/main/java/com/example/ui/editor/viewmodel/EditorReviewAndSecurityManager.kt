package com.example.ui.editor.viewmodel

import android.content.Context
import com.example.core.layout.MarginPreset
import com.example.core.layout.PageBorderStyle
import com.example.core.layout.PageLayoutConfig
import com.example.core.layout.PageOrientation
import com.example.core.layout.PageSize
import com.example.data.model.ParagraphBlock
import com.example.data.model.TextAlignment
import com.example.editor.engine.SpanOperations
import com.example.editor.engine.UpdateLayoutCommand
import com.example.ui.editor.EditorViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.security.MessageDigest

/**
 * Manages document security, password PIN protection, layout configuration, watermark, and find/replace,
 * delegating TTS to [EditorTtsManager] and reviews to [EditorReviewManager].
 */
class EditorReviewAndSecurityManager(
    private val vm: EditorViewModel
) {
    internal val ttsManager = EditorTtsManager(vm)
    internal val reviewManager = EditorReviewManager(vm)

    val isSpeaking: StateFlow<Boolean> get() = ttsManager.isSpeaking
    val speakingParagraphIndex: StateFlow<Int?> get() = ttsManager.speakingParagraphIndex
    val speechRate: StateFlow<Float> get() = ttsManager.speechRate

    private val _isDocumentLocked = MutableStateFlow(false)
    val isDocumentLocked: StateFlow<Boolean> = _isDocumentLocked.asStateFlow()

    private val _isFindReplaceVisible = MutableStateFlow(false)
    val isFindReplaceVisible: StateFlow<Boolean> = _isFindReplaceVisible.asStateFlow()

    private val _findQuery = MutableStateFlow("")
    val findQuery: StateFlow<String> = _findQuery.asStateFlow()

    private val _replaceQuery = MutableStateFlow("")
    val replaceQuery: StateFlow<String> = _replaceQuery.asStateFlow()

    private val _matchCount = MutableStateFlow(0)
    val matchCount: StateFlow<Int> = _matchCount.asStateFlow()

    fun setDocumentLocked(locked: Boolean) {
        _isDocumentLocked.value = locked
    }

    // TTS Delegations
    fun startReadAloud(context: Context) = ttsManager.startReadAloud(context)
    fun stopReadAloud() = ttsManager.stopReadAloud()
    fun setTtsSpeechRate(rate: Float) = ttsManager.setTtsSpeechRate(rate)
    fun cycleSpeechRate() = ttsManager.cycleSpeechRate()
    fun cleanupTts() = ttsManager.cleanupTts()

    // Review Delegations
    fun addComment(blockId: String, author: String, text: String) = reviewManager.addComment(blockId, author, text)
    fun resolveComment(commentId: String) = reviewManager.resolveComment(commentId)
    fun deleteComment(commentId: String) = reviewManager.deleteComment(commentId)
    fun toggleTrackChanges(enabled: Boolean? = null) = reviewManager.toggleTrackChanges(enabled)
    fun acceptChange(changeId: String) = reviewManager.acceptChange(changeId)
    fun rejectChange(changeId: String) = reviewManager.rejectChange(changeId)
    fun acceptAllChanges() = reviewManager.acceptAllChanges()
    fun rejectAllChanges() = reviewManager.rejectAllChanges()

    // Security PIN Lock
    private fun hashPassword(password: String): String {
        val md = MessageDigest.getInstance("SHA-256")
        val bytes = md.digest(password.toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }

    fun setDocumentPassword(password: String) {
        val doc = vm.currentDocument ?: return
        val hashed = hashPassword(password)
        vm.updateDocumentInternal(doc.copy(passwordHash = hashed))
        _isDocumentLocked.value = false
        vm.historyManager.scheduleDocumentAutosave()
        vm.showStatusMessage("Document locked with password PIN")
    }

    fun removeDocumentPassword() {
        val doc = vm.currentDocument ?: return
        vm.updateDocumentInternal(doc.copy(passwordHash = null))
        _isDocumentLocked.value = false
        vm.historyManager.scheduleDocumentAutosave()
        vm.showStatusMessage("Password protection removed")
    }

    fun verifyPassword(input: String): Boolean {
        val doc = vm.currentDocument ?: return true
        val target = doc.passwordHash ?: return true
        return hashPassword(input) == target
    }

    fun unlockDocument(password: String): Boolean {
        val verified = verifyPassword(password)
        if (verified) {
            _isDocumentLocked.value = false
            vm.showStatusMessage("Document unlocked")
        } else {
            vm.showStatusMessage("Incorrect PIN or Password")
        }
        return verified
    }

    // Page Layout, Watermark & Page Border
    fun setWatermark(text: String, colorHex: String = "#2594A3B8", angleDeg: Float = -45f) {
        val currentLayout = vm.currentDocument?.layoutConfig ?: PageLayoutConfig()
        val newLayout = currentLayout.copy(watermarkText = text, watermarkColorHex = colorHex, watermarkAngleDeg = angleDeg)
        vm.executeCommand(UpdateLayoutCommand(currentLayout, newLayout))
        vm.showStatusMessage(if (text.isNotBlank()) "Watermark set to '$text'" else "Watermark removed")
    }

    fun removeWatermark() = setWatermark("")

    fun setColumnCount(columns: Int) {
        val currentLayout = vm.currentDocument?.layoutConfig ?: PageLayoutConfig()
        val cols = columns.coerceIn(1, 2)
        vm.executeCommand(UpdateLayoutCommand(currentLayout, currentLayout.copy(columnCount = cols)))
        vm.showStatusMessage(if (cols == 2) "Two-Column layout applied" else "Single-Column layout applied")
    }

    fun setPageBorder(style: PageBorderStyle, colorHex: String) {
        val currentLayout = vm.currentDocument?.layoutConfig ?: PageLayoutConfig()
        vm.executeCommand(UpdateLayoutCommand(currentLayout, currentLayout.copy(borderStyle = style, borderColorHex = colorHex)))
        vm.showStatusMessage("Page border applied")
    }

    fun setPageBackgroundColor(bgHex: String) {
        val currentLayout = vm.currentDocument?.layoutConfig ?: PageLayoutConfig()
        vm.executeCommand(UpdateLayoutCommand(currentLayout, currentLayout.copy(pageBackgroundHex = bgHex)))
        vm.showStatusMessage("Page color updated")
    }

    fun setHeaderFooter(
        headerText: String? = null,
        footerText: String? = null,
        showHeader: Boolean? = null,
        showFooter: Boolean? = null
    ) {
        val currentLayout = vm.currentDocument?.layoutConfig ?: PageLayoutConfig()
        val newLayout = currentLayout.copy(
            headerText = headerText ?: currentLayout.headerText,
            footerText = footerText ?: currentLayout.footerText,
            showHeader = showHeader ?: currentLayout.showHeader,
            showFooter = showFooter ?: currentLayout.showFooter
        )
        vm.executeCommand(UpdateLayoutCommand(currentLayout, newLayout))
    }

    fun setPageNumberPreset(pattern: String, alignment: TextAlignment) {
        val currentLayout = vm.currentDocument?.layoutConfig ?: PageLayoutConfig()
        val newLayout = currentLayout.copy(
            footerText = pattern,
            footerAlignment = alignment,
            showFooter = true
        )
        vm.executeCommand(UpdateLayoutCommand(currentLayout, newLayout))
    }

    fun updateHeaderFooterPolicy(
        differentFirstPage: Boolean? = null,
        differentOddEven: Boolean? = null,
        headerEvenText: String? = null,
        footerEvenText: String? = null
    ) {
        val currentLayout = vm.currentDocument?.layoutConfig ?: PageLayoutConfig()
        val updated = currentLayout.copy(
            differentFirstPage = differentFirstPage ?: currentLayout.differentFirstPage,
            differentOddEvenPages = differentOddEven ?: currentLayout.differentOddEvenPages,
            headerEvenText = headerEvenText ?: currentLayout.headerEvenText,
            footerEvenText = footerEvenText ?: currentLayout.footerEvenText
        )
        vm.executeCommand(UpdateLayoutCommand(currentLayout, updated))
        vm.showStatusMessage("Header & Footer layout updated")
    }

    fun updatePageLayoutConfig(newLayout: PageLayoutConfig) {
        val currentLayout = vm.currentDocument?.layoutConfig ?: PageLayoutConfig()
        vm.executeCommand(UpdateLayoutCommand(currentLayout, newLayout))
    }

    fun updatePageLayout(
        size: PageSize? = null,
        orientation: PageOrientation? = null,
        margins: MarginPreset? = null,
        marginPreset: MarginPreset? = null,
        topMm: Float? = null,
        bottomMm: Float? = null,
        leftMm: Float? = null,
        rightMm: Float? = null
    ) {
        val currentLayout = vm.currentDocument?.layoutConfig ?: PageLayoutConfig()
        val effectivePreset = marginPreset ?: margins
        val top = topMm ?: if (effectivePreset != null && effectivePreset != MarginPreset.CUSTOM) effectivePreset.topMm else currentLayout.customTopMarginMm
        val bottom = bottomMm ?: if (effectivePreset != null && effectivePreset != MarginPreset.CUSTOM) effectivePreset.bottomMm else currentLayout.customBottomMarginMm
        val left = leftMm ?: if (effectivePreset != null && effectivePreset != MarginPreset.CUSTOM) effectivePreset.leftMm else currentLayout.customLeftMarginMm
        val right = rightMm ?: if (effectivePreset != null && effectivePreset != MarginPreset.CUSTOM) effectivePreset.rightMm else currentLayout.customRightMarginMm

        val newLayout = currentLayout.copy(
            pageSize = size ?: currentLayout.pageSize,
            orientation = orientation ?: currentLayout.orientation,
            marginPreset = effectivePreset ?: currentLayout.marginPreset,
            customTopMarginMm = top,
            customBottomMarginMm = bottom,
            customLeftMarginMm = left,
            customRightMarginMm = right
        )
        updatePageLayoutConfig(newLayout)
    }

    // Find & Replace
    fun toggleFindReplace() {
        _isFindReplaceVisible.value = !_isFindReplaceVisible.value
        if (!_isFindReplaceVisible.value) {
            _findQuery.value = ""
            _replaceQuery.value = ""
            _matchCount.value = 0
        }
    }

    fun updateFindQuery(query: String) {
        _findQuery.value = query
        recomputeMatches(query)
    }

    fun updateReplaceQuery(query: String) {
        _replaceQuery.value = query
    }

    private fun recomputeMatches(query: String) {
        if (query.isEmpty()) {
            _matchCount.value = 0
            return
        }
        val doc = vm.currentDocument ?: return
        var count = 0
        doc.blocks.filterIsInstance<ParagraphBlock>().forEach { block ->
            var idx = 0
            while (idx <= block.text.length - query.length) {
                val found = block.text.indexOf(query, idx, ignoreCase = true)
                if (found == -1) break
                count++
                idx = found + query.length
            }
        }
        _matchCount.value = count
    }

    fun replaceAll() {
        val find = _findQuery.value
        val replace = _replaceQuery.value
        if (find.isEmpty()) return

        val currentDoc = vm.currentDocument ?: return
        var totalReplaced = 0

        val updatedBlocks = currentDoc.blocks.map { block ->
            if (block is ParagraphBlock && block.text.contains(find, ignoreCase = true)) {
                val (newText, newSpans) = SpanOperations.replaceTextAndShiftSpans(
                    text = block.text,
                    spans = block.spans,
                    find = find,
                    replace = replace,
                    ignoreCase = true
                )
                totalReplaced++
                block.copy(text = newText, spans = newSpans)
            } else {
                block
            }
        }

        if (totalReplaced > 0) {
            val updatedDoc = currentDoc.copy(blocks = updatedBlocks, updatedAt = System.currentTimeMillis())
            vm.executeCommand(
                object : com.example.editor.engine.EditorCommand {
                    override val description: String = "Replace '$find' with '$replace'"
                    override fun apply(document: com.example.data.model.RichDocument) = updatedDoc
                    override fun revert(document: com.example.data.model.RichDocument) = currentDoc
                }
            )
            recomputeMatches(find)
            vm.showStatusMessage("Replaced matches across $totalReplaced paragraphs")
        }
    }
}
