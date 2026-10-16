package com.example.ui.editor.components

import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.platform.TextToolbar
import androidx.compose.ui.platform.TextToolbarStatus

/**
 * Custom TextToolbar replacing Android's stock system selection bar
 * with the Microsoft Word mobile floating contextual menu.
 */
class WordTextToolbar(
    private val onShow: (
        rect: Rect,
        onCopy: (() -> Unit)?,
        onPaste: (() -> Unit)?,
        onCut: (() -> Unit)?,
        onSelectAll: (() -> Unit)?
    ) -> Unit,
    private val onHide: () -> Unit
) : TextToolbar {

    private var currentStatus: TextToolbarStatus = TextToolbarStatus.Hidden

    override val status: TextToolbarStatus
        get() = currentStatus

    override fun hide() {
        currentStatus = TextToolbarStatus.Hidden
        onHide()
    }

    override fun showMenu(
        rect: Rect,
        onCopyRequested: (() -> Unit)?,
        onPasteRequested: (() -> Unit)?,
        onCutRequested: (() -> Unit)?,
        onSelectAllRequested: (() -> Unit)?
    ) {
        currentStatus = TextToolbarStatus.Shown
        onShow(rect, onCopyRequested, onPasteRequested, onCutRequested, onSelectAllRequested)
    }
}

/**
 * State holding active context menu coordinates and action callbacks.
 */
data class ContextMenuDisplayState(
    val isVisible: Boolean = false,
    val anchorX: Float = 0f,
    val anchorY: Float = 0f,
    val onCopy: (() -> Unit)? = null,
    val onPaste: (() -> Unit)? = null,
    val onCut: (() -> Unit)? = null,
    val onSelectAll: (() -> Unit)? = null,
    val isFromTextSelection: Boolean = false
)
