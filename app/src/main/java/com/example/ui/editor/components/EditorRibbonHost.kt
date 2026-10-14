package com.example.ui.editor.components

import android.content.Context
import android.content.Intent
import android.speech.RecognizerIntent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import com.example.data.model.ParagraphBlock
import com.example.ui.editor.EditorDialogsState
import com.example.ui.editor.EditorViewModel
import com.example.ui.editor.RibbonTab
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun EditorRibbonHost(
    viewModel: EditorViewModel,
    dState: EditorDialogsState,
    activeRibbonTab: RibbonTab,
    isRibbonVisible: Boolean,
    activeParagraph: ParagraphBlock?,
    context: Context,
    coroutineScope: CoroutineScope,
    snackbarHostState: SnackbarHostState,
    voiceTypingLauncher: ActivityResultLauncher<Intent>,
    photoPickerLauncher: ActivityResultLauncher<PickVisualMediaRequest>
) {
    WordRibbon(
        activeTab = activeRibbonTab,
        onTabSelected = viewModel::selectRibbonTab,
        isRibbonVisible = isRibbonVisible,
        onToggleRibbon = viewModel::toggleRibbonVisibility,
        activeParagraph = activeParagraph,
        viewModel = viewModel,
        context = context,
        onOpenTableDialog = { dState.showTableDialog = true },
        onOpenPhotoPicker = {
            photoPickerLauncher.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
        },
        onOpenLinkDialog = { dState.showLinkDialog = true },
        onOpenHeaderFooterDialog = { dState.showHeaderFooterDialog = true },
        onOpenPageNumberDialog = { dState.showPageNumberDialog = true },
        onOpenLayoutDialog = { dState.showLayoutDialog = true },
        onOpenSignatureDialog = { dState.showSignatureDialog = true },
        onOpenShapeDialog = { dState.showShapeDialog = true },
        onOpenWatermarkDialog = { dState.showWatermarkDialog = true },
        onOpenPasswordDialog = { dState.showPasswordDialog = true },
        onOpenVersionHistoryDialog = { dState.showVersionHistoryDialog = true },
        onOpenStatsDialog = { dState.showStatsDialog = true },
        onOpenFootnoteDialog = { dState.showFootnoteDialog = true },
        onOpenPageBordersDialog = { dState.showPageBordersDialog = true },
        onOpenPageColorDialog = { dState.showPageColorDialog = true },
        onOpenCommentsDialog = { dState.showCommentsDialog = true },
        onOpenSmartWritingDialog = { dState.showSmartWritingDialog = true },
        onClearFormatting = { viewModel.clearFormatting() },
        onChangeCase = { viewModel.changeCase(it) },
        onOpenTypographyEffectsDialog = { dState.showTypographyEffectsDialog = true },
        onOpenParagraphSpacingDialog = { dState.showParagraphSpacingDialog = true },
        onToggleChecklist = { viewModel.toggleChecklist() },
        onCreateCustomStyle = { dState.showCreateCustomStyleDialog = true },
        onApplyCustomStyle = { viewModel.applyCustomStyle(it) },
        onOpenOutline = { dState.showOutlineSheet = true },
        onStartVoiceTyping = {
            try {
                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                    putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak to dictate text into document...")
                }
                if (context.packageManager.queryIntentActivities(intent, 0).isNotEmpty()) {
                    voiceTypingLauncher.launch(intent)
                } else {
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("Voice typing requires Google Speech Services on your device")
                    }
                }
            } catch (_: Exception) {
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("Speech recognition service not available on this device")
                }
            }
        }
    )
}
