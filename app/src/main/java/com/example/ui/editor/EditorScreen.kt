package com.example.ui.editor

import android.content.Context
import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FindReplace
import androidx.compose.material.icons.filled.HorizontalRule
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.Title
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.material.icons.outlined.Sync
import androidx.compose.material.icons.outlined.SyncProblem
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.core.layout.MarginPreset
import com.example.core.layout.PageOrientation
import com.example.core.layout.PageSize
import com.example.data.model.BlockElement
import com.example.data.model.DividerBlock
import com.example.data.model.HeadingStyle
import com.example.data.model.ImageBlock
import com.example.data.model.ListType
import com.example.data.model.PageBreakBlock
import com.example.data.model.ParagraphBlock
import com.example.data.model.RichDocument
import com.example.data.model.TableBlock
import com.example.data.model.TextAlignment
import com.example.editor.engine.AutosaveStatus
import com.example.ui.editor.components.FindReplaceBar
import com.example.ui.editor.components.FormattingToolbar
import com.example.ui.editor.components.InsertTableDialog
import com.example.ui.editor.components.PageLayoutDialog
import com.example.ui.theme.WordDeskCanvasLight
import com.example.ui.theme.WordPrimary
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorScreen(
    viewModel: EditorViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    val document by viewModel.document.collectAsStateWithLifecycle()
    val activeRibbonTab by viewModel.activeRibbonTab.collectAsStateWithLifecycle()
    val isRibbonVisible by viewModel.isRibbonVisible.collectAsStateWithLifecycle()
    val viewMode by viewModel.viewMode.collectAsStateWithLifecycle()
    val zoomPercent by viewModel.zoomPercent.collectAsStateWithLifecycle()
    val canUndo by viewModel.canUndo.collectAsStateWithLifecycle()
    val canRedo by viewModel.canRedo.collectAsStateWithLifecycle()
    val activeBlockIndex by viewModel.activeBlockIndex.collectAsStateWithLifecycle()
    val autosaveStatus by viewModel.autosaveStatus.collectAsStateWithLifecycle()
    val statusMessage by viewModel.statusMessage.collectAsStateWithLifecycle()

    val isFindReplaceVisible by viewModel.isFindReplaceVisible.collectAsStateWithLifecycle()
    val findQuery by viewModel.findQuery.collectAsStateWithLifecycle()
    val replaceQuery by viewModel.replaceQuery.collectAsStateWithLifecycle()
    val matchCount by viewModel.matchCount.collectAsStateWithLifecycle()

    var showTitleEditDialog by remember { mutableStateOf(false) }
    var showTableDialog by remember { mutableStateOf(false) }
    var showLayoutDialog by remember { mutableStateOf(false) }
    var showStatsDialog by remember { mutableStateOf(false) }
    var showOptionsMenu by remember { mutableStateOf(false) }

    // Android Zero-Permission Photo Picker for Image Insertion
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let { viewModel.insertImage(it.toString(), "Inserted Image") }
    }

    // SAF PDF Save Launcher
    val safPdfSaveLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/pdf")
    ) { uri ->
        uri?.let { viewModel.exportPdfToSafUri(context, it) }
    }

    // Intercept hardware/system back button safely
    BackHandler {
        viewModel.saveNow()
        onNavigateBack()
    }

    LaunchedEffect(statusMessage) {
        statusMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearStatusMessage()
        }
    }

    val activeParagraph = document?.blocks?.getOrNull(activeBlockIndex) as? ParagraphBlock

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            Column {
                TopAppBar(
                    navigationIcon = {
                        IconButton(
                            onClick = {
                                viewModel.saveNow()
                                onNavigateBack()
                            },
                            modifier = Modifier.testTag("editor_back_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back to Home"
                            )
                        }
                    },
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clickable { showTitleEditDialog = true }
                                .padding(vertical = 4.dp)
                                .testTag("title_edit_container")
                        ) {
                            Text(
                                text = document?.title ?: "Document",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f, fill = false)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit Title",
                                tint = MaterialTheme.colorScheme.outline,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    },
                    actions = {
                        // Autosave Status Chip
                        AutosaveChip(status = autosaveStatus)

                        // Undo Button
                        IconButton(
                            onClick = { viewModel.undo() },
                            enabled = canUndo,
                            modifier = Modifier.testTag("action_undo")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Undo,
                                contentDescription = "Undo",
                                tint = if (canUndo) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                            )
                        }

                        // Redo Button
                        IconButton(
                            onClick = { viewModel.redo() },
                            enabled = canRedo,
                            modifier = Modifier.testTag("action_redo")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Redo,
                                contentDescription = "Redo",
                                tint = if (canRedo) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                            )
                        }

                        // Search & Replace Toggle
                        IconButton(
                            onClick = { viewModel.toggleFindReplace() },
                            modifier = Modifier.testTag("action_find_replace")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search and Replace",
                                tint = if (isFindReplaceVisible) WordPrimary else MaterialTheme.colorScheme.onSurface
                            )
                        }

                        // Share / Export PDF Quick Action
                        IconButton(
                            onClick = {
                                viewModel.exportToPdf(context) { file ->
                                    file?.let { sharePdfFile(context, it) }
                                }
                            },
                            modifier = Modifier.testTag("action_share_pdf")
                        ) {
                            Icon(Icons.Default.Share, contentDescription = "Share PDF")
                        }

                        // Overflow Options Menu
                        Box {
                            IconButton(onClick = { showOptionsMenu = true }) {
                                Icon(Icons.Default.MoreVert, contentDescription = "More Actions")
                            }

                            DropdownMenu(
                                expanded = showOptionsMenu,
                                onDismissRequest = { showOptionsMenu = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Save Document") },
                                    leadingIcon = { Icon(Icons.Default.Save, contentDescription = null) },
                                    onClick = {
                                        showOptionsMenu = false
                                        viewModel.saveNow()
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Save as PDF (SAF)...") },
                                    leadingIcon = { Icon(Icons.Default.PictureAsPdf, contentDescription = null) },
                                    onClick = {
                                        showOptionsMenu = false
                                        val safeTitle = (document?.title ?: "document").replace("[^a-zA-Z0-9.-]".toRegex(), "_")
                                        safPdfSaveLauncher.launch("$safeTitle.pdf")
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Export as Plain Text (.txt)") },
                                    leadingIcon = { Icon(Icons.Default.TextFields, contentDescription = null) },
                                    onClick = {
                                        showOptionsMenu = false
                                        viewModel.exportToPlainText(context) { file ->
                                            file?.let { shareTextFile(context, it) }
                                        }
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Print") },
                                    leadingIcon = { Icon(Icons.Default.Print, contentDescription = null) },
                                    onClick = {
                                        showOptionsMenu = false
                                        viewModel.printDocument(context)
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Page Setup...") },
                                    leadingIcon = { Icon(Icons.Default.Title, contentDescription = null) },
                                    onClick = {
                                        showOptionsMenu = false
                                        showLayoutDialog = true
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Document Statistics") },
                                    leadingIcon = { Icon(Icons.Default.Title, contentDescription = null) },
                                    onClick = {
                                        showOptionsMenu = false
                                        showStatsDialog = true
                                    }
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )

                // Docked In-Document Find and Replace Bar
                FindReplaceBar(
                    isVisible = isFindReplaceVisible,
                    findQuery = findQuery,
                    replaceQuery = replaceQuery,
                    matchCount = matchCount,
                    onFindQueryChanged = viewModel::updateFindQuery,
                    onReplaceQueryChanged = viewModel::updateReplaceQuery,
                    onReplaceAll = viewModel::replaceAll,
                    onClose = { viewModel.toggleFindReplace() }
                )
            }
        },
        bottomBar = {
            // Word Status Bar
            EditorStatusBar(
                document = document,
                autosaveStatus = autosaveStatus,
                zoomPercent = zoomPercent,
                activeBlock = activeParagraph
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(WordDeskCanvasLight)
                .imePadding() // Keyboard-aware layout: ensures soft keyboard does not obscure active content
        ) {
            // Microsoft Word Ribbon Component
            WordRibbon(
                activeTab = activeRibbonTab,
                onTabSelected = viewModel::selectRibbonTab,
                isRibbonVisible = isRibbonVisible,
                onToggleRibbon = viewModel::toggleRibbonVisibility,
                activeParagraph = activeParagraph,
                viewModel = viewModel,
                context = context,
                onOpenTableDialog = { showTableDialog = true },
                onOpenPhotoPicker = {
                    photoPickerLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                },
                onOpenLayoutDialog = { showLayoutDialog = true },
                onOpenStatsDialog = { showStatsDialog = true }
            )

            // Document Canvas Surface with Responsive Phone / Tablet Layout
            BoxWithConstraints(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.TopCenter
            ) {
                val isTabletOrExpanded = maxWidth > 600.dp

                if (document != null) {
                    DocumentCanvas(
                        document = document!!,
                        viewMode = viewMode,
                        zoomPercent = zoomPercent,
                        isTabletOrExpanded = isTabletOrExpanded,
                        activeBlockIndex = activeBlockIndex,
                        onBlockSelected = viewModel::setActiveBlock,
                        onUpdateBlockText = viewModel::updateParagraphText,
                        onInsertParagraph = viewModel::insertParagraph,
                        onDeleteCurrentBlock = viewModel::deleteCurrentBlock
                    )
                }
            }
        }
    }

    // Title Edit Dialog
    if (showTitleEditDialog) {
        var editedTitle by remember { mutableStateOf(document?.title ?: "") }
        AlertDialog(
            onDismissRequest = { showTitleEditDialog = false },
            title = { Text("Edit Document Title") },
            text = {
                OutlinedTextField(
                    value = editedTitle,
                    onValueChange = { editedTitle = it },
                    label = { Text("Title") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("edit_title_input")
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (editedTitle.isNotBlank()) {
                            viewModel.updateTitle(editedTitle.trim())
                        }
                        showTitleEditDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = WordPrimary)
                ) {
                    Text("Done")
                }
            },
            dismissButton = {
                TextButton(onClick = { showTitleEditDialog = false }) { Text("Cancel") }
            }
        )
    }

    // Insert Table Dialog
    if (showTableDialog) {
        InsertTableDialog(
            onDismiss = { showTableDialog = false },
            onConfirm = { r, c ->
                viewModel.insertTable(r, c)
                showTableDialog = false
            }
        )
    }

    // Page Layout Dialog
    if (showLayoutDialog && document != null) {
        PageLayoutDialog(
            currentLayout = document!!.layoutConfig,
            onDismiss = { showLayoutDialog = false },
            onSave = { ps, po, mp, header, footer ->
                viewModel.updatePageLayout(ps, po, mp)
                showLayoutDialog = false
            }
        )
    }

    // Document Statistics Dialog
    if (showStatsDialog) {
        val doc = document
        AlertDialog(
            onDismissRequest = { showStatsDialog = false },
            title = { Text("Document Statistics") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    StatRow("Words", "${doc?.wordCount ?: 0}")
                    StatRow("Characters", "${doc?.characterCount ?: 0}")
                    StatRow("Paragraphs", "${doc?.blocks?.count { it is ParagraphBlock } ?: 0}")
                    StatRow("Estimated Pages", "${doc?.estimatedPageCount ?: 1}")
                    StatRow("Paper Size", doc?.layoutConfig?.pageSize?.displayName ?: "A4")
                    StatRow("Orientation", doc?.layoutConfig?.orientation?.name ?: "PORTRAIT")
                    StatRow("Active Language", if (activeParagraph?.isRtl == true) "Urdu (RTL Nastaliq)" else "English (LTR)")
                }
            },
            confirmButton = {
                Button(
                    onClick = { showStatsDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = WordPrimary)
                ) {
                    Text("Close")
                }
            }
        )
    }
}

@Composable
fun StatRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun AutosaveChip(status: AutosaveStatus) {
    val (text, color, icon) = when (status) {
        AutosaveStatus.IDLE -> Triple("Saved", Color(0xFF107C41), Icons.Default.Check)
        AutosaveStatus.DIRTY -> Triple("Editing", Color(0xFFD83B01), Icons.Outlined.Sync)
        AutosaveStatus.SAVING -> Triple("Saving...", Color(0xFF185ABD), Icons.Outlined.Sync)
        AutosaveStatus.SAVED -> Triple("Saved", Color(0xFF107C41), Icons.Default.Check)
        AutosaveStatus.ERROR -> Triple("Sync Error", Color.Red, Icons.Outlined.SyncProblem)
    }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = color.copy(alpha = 0.12f),
        modifier = Modifier.padding(end = 4.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(13.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = text,
                color = color,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun WordRibbon(
    activeTab: RibbonTab,
    onTabSelected: (RibbonTab) -> Unit,
    isRibbonVisible: Boolean,
    onToggleRibbon: () -> Unit,
    activeParagraph: ParagraphBlock?,
    viewModel: EditorViewModel,
    context: Context,
    onOpenTableDialog: () -> Unit,
    onOpenPhotoPicker: () -> Unit,
    onOpenLayoutDialog: () -> Unit,
    onOpenStatsDialog: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            // Tab Header Strip
            val tabs = RibbonTab.entries
            val selectedIndex = tabs.indexOf(activeTab)

            TabRow(
                selectedTabIndex = selectedIndex,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = WordPrimary,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedIndex]),
                        color = WordPrimary,
                        height = 3.dp
                    )
                }
            ) {
                tabs.forEach { tab ->
                    Tab(
                        selected = activeTab == tab,
                        onClick = { onTabSelected(tab) },
                        text = {
                            Text(
                                text = tab.name.lowercase().replaceFirstChar { it.uppercase() },
                                fontWeight = if (activeTab == tab) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 13.sp
                            )
                        }
                    )
                }
            }

            // Toolbar Contents based on Selected Tab
            AnimatedVisibility(visible = isRibbonVisible) {
                when (activeTab) {
                    RibbonTab.HOME -> {
                        // Reusable full-featured Formatting Toolbar
                        FormattingToolbar(
                            activeParagraph = activeParagraph,
                            onSetHeadingStyle = viewModel::setHeadingStyle,
                            onToggleBold = viewModel::toggleBold,
                            onToggleItalic = viewModel::toggleItalic,
                            onToggleUnderline = viewModel::toggleUnderline,
                            onToggleStrikethrough = viewModel::toggleStrikethrough,
                            onSetFontFamily = viewModel::setFontFamily,
                            onSetFontSize = viewModel::setFontSize,
                            onSetTextColor = viewModel::setTextColor,
                            onSetHighlightColor = viewModel::setHighlightColor,
                            onSetAlignment = viewModel::setAlignment,
                            onToggleListType = viewModel::toggleListType,
                            onToggleRtl = viewModel::toggleRtl
                        )
                    }

                    RibbonTab.INSERT -> {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState())
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            InsertRibbonControls(
                                viewModel = viewModel,
                                onOpenTableDialog = onOpenTableDialog,
                                onOpenPhotoPicker = onOpenPhotoPicker
                            )
                        }
                    }

                    RibbonTab.LAYOUT -> {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState())
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            LayoutRibbonControls(
                                viewModel = viewModel,
                                onOpenLayoutDialog = onOpenLayoutDialog
                            )
                        }
                    }

                    RibbonTab.VIEW -> {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState())
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            ViewRibbonControls(viewModel)
                        }
                    }

                    RibbonTab.FILE -> {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState())
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FileRibbonControls(
                                viewModel = viewModel,
                                context = context,
                                onOpenStatsDialog = onOpenStatsDialog
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun InsertRibbonControls(
    viewModel: EditorViewModel,
    onOpenTableDialog: () -> Unit,
    onOpenPhotoPicker: () -> Unit
) {
    RibbonActionButton(
        icon = Icons.Default.TableChart,
        label = "Table",
        onClick = onOpenTableDialog
    )

    RibbonActionButton(
        icon = Icons.Default.Image,
        label = "Picture",
        onClick = onOpenPhotoPicker
    )

    RibbonActionButton(
        icon = Icons.Default.HorizontalRule,
        label = "Horizontal Line",
        onClick = { viewModel.insertDivider() }
    )

    RibbonActionButton(
        icon = Icons.Default.Title,
        label = "Page Break",
        onClick = { viewModel.insertPageBreak() }
    )

    val dateFormat = remember { SimpleDateFormat("MMMM d, yyyy", Locale.getDefault()) }
    RibbonActionButton(
        icon = Icons.Default.CalendarMonth,
        label = "Date & Time",
        onClick = {
            val dateStr = dateFormat.format(Date())
            viewModel.insertDateTimeStamp(dateStr)
        }
    )
}

@Composable
fun LayoutRibbonControls(
    viewModel: EditorViewModel,
    onOpenLayoutDialog: () -> Unit
) {
    Text("Paper:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    FilterChip(
        selected = true,
        onClick = { viewModel.updatePageLayout(pageSize = PageSize.A4) },
        label = { Text("A4", fontSize = 11.sp) }
    )
    FilterChip(
        selected = false,
        onClick = { viewModel.updatePageLayout(pageSize = PageSize.LETTER) },
        label = { Text("Letter", fontSize = 11.sp) }
    )

    Box(modifier = Modifier.height(20.dp).width(1.dp).background(Color(0xFFE2E8F0)))

    Text("Margins:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    FilterChip(
        selected = true,
        onClick = { viewModel.updatePageLayout(marginPreset = MarginPreset.NORMAL) },
        label = { Text("Normal", fontSize = 11.sp) }
    )
    FilterChip(
        selected = false,
        onClick = { viewModel.updatePageLayout(marginPreset = MarginPreset.NARROW) },
        label = { Text("Narrow", fontSize = 11.sp) }
    )

    Box(modifier = Modifier.height(20.dp).width(1.dp).background(Color(0xFFE2E8F0)))

    Text("Orientation:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    FilterChip(
        selected = true,
        onClick = { viewModel.updatePageLayout(orientation = PageOrientation.PORTRAIT) },
        label = { Text("Portrait", fontSize = 11.sp) }
    )
    FilterChip(
        selected = false,
        onClick = { viewModel.updatePageLayout(orientation = PageOrientation.LANDSCAPE) },
        label = { Text("Landscape", fontSize = 11.sp) }
    )

    Box(modifier = Modifier.height(20.dp).width(1.dp).background(Color(0xFFE2E8F0)))

    RibbonActionButton(
        icon = Icons.Default.Edit,
        label = "Page Setup...",
        onClick = onOpenLayoutDialog
    )
}

@Composable
fun ViewRibbonControls(viewModel: EditorViewModel) {
    FilterChip(
        selected = true,
        onClick = { viewModel.setViewMode(EditorViewMode.PAGINATED_A4) },
        label = { Text("A4 Page View", fontSize = 11.sp) }
    )
    FilterChip(
        selected = false,
        onClick = { viewModel.setViewMode(EditorViewMode.CONTINUOUS_MOBILE) },
        label = { Text("Mobile Continuous", fontSize = 11.sp) }
    )

    Box(modifier = Modifier.height(20.dp).width(1.dp).background(Color(0xFFE2E8F0)))

    IconButton(onClick = { viewModel.setZoomPercent(75) }, modifier = Modifier.size(32.dp)) {
        Icon(Icons.Default.ZoomOut, contentDescription = "Zoom 75%")
    }
    Text("100%", fontSize = 12.sp, fontWeight = FontWeight.Bold)
    IconButton(onClick = { viewModel.setZoomPercent(125) }, modifier = Modifier.size(32.dp)) {
        Icon(Icons.Default.ZoomIn, contentDescription = "Zoom 125%")
    }
}

@Composable
fun FileRibbonControls(
    viewModel: EditorViewModel,
    context: Context,
    onOpenStatsDialog: () -> Unit
) {
    RibbonActionButton(
        icon = Icons.Default.Save,
        label = "Save Now",
        onClick = { viewModel.saveNow() }
    )

    RibbonActionButton(
        icon = Icons.Default.PictureAsPdf,
        label = "Export PDF",
        onClick = {
            viewModel.exportToPdf(context) { file ->
                file?.let { sharePdfFile(context, it) }
            }
        }
    )

    RibbonActionButton(
        icon = Icons.Default.TextFields,
        label = "Plain Text",
        onClick = {
            viewModel.exportToPlainText(context) { file ->
                file?.let { shareTextFile(context, it) }
            }
        }
    )

    RibbonActionButton(
        icon = Icons.Default.Print,
        label = "Print",
        onClick = { viewModel.printDocument(context) }
    )

    RibbonActionButton(
        icon = Icons.Default.Title,
        label = "Statistics",
        onClick = onOpenStatsDialog
    )
}

@Composable
fun RibbonActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp)
        ) {
            Icon(imageVector = icon, contentDescription = label, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(label, fontSize = 11.sp, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
fun DocumentCanvas(
    document: RichDocument,
    viewMode: EditorViewMode,
    zoomPercent: Int,
    isTabletOrExpanded: Boolean,
    activeBlockIndex: Int,
    onBlockSelected: (Int) -> Unit,
    onUpdateBlockText: (Int, String) -> Unit,
    onInsertParagraph: () -> Unit,
    onDeleteCurrentBlock: () -> Unit
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(if (isTabletOrExpanded) 28.dp else 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (viewMode == EditorViewMode.PAGINATED_A4) {
            // A4 Physical Sheet
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = if (isTabletOrExpanded) 760.dp else 640.dp)
                    .shadow(6.dp, RoundedCornerShape(4.dp)),
                shape = RoundedCornerShape(4.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = if (isTabletOrExpanded) 40.dp else 24.dp, vertical = 32.dp)
                ) {
                    // Page Header Area
                    if (document.layoutConfig.headerText.isNotBlank()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = document.layoutConfig.headerText,
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.Gray
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        HorizontalDivider(color = Color(0xFFE5E7EB), thickness = 0.5.dp)
                        Spacer(modifier = Modifier.height(18.dp))
                    }

                    // Blocks List
                    var listNumberCounter = 1
                    document.blocks.forEachIndexed { index, block ->
                        val itemNumber = if (block is ParagraphBlock && block.listType == ListType.NUMBERED) {
                            listNumberCounter++
                        } else {
                            listNumberCounter = 1
                            null
                        }

                        BlockItemView(
                            block = block,
                            itemNumber = itemNumber,
                            isActive = index == activeBlockIndex,
                            onFocus = { onBlockSelected(index) },
                            onTextChanged = { newText -> onUpdateBlockText(index, newText) },
                            onEnterPressed = onInsertParagraph
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    // Page Footer Area
                    Spacer(modifier = Modifier.height(24.dp))
                    HorizontalDivider(color = Color(0xFFE5E7EB), thickness = 0.5.dp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = document.layoutConfig.footerText
                                .replace("{page}", "1")
                                .replace("{pages}", "${document.estimatedPageCount}"),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Gray
                        )
                    }
                }
            }
        } else {
            // Continuous Mobile View
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(1.dp, RoundedCornerShape(12.dp)),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    var listNumberCounter = 1
                    document.blocks.forEachIndexed { index, block ->
                        val itemNumber = if (block is ParagraphBlock && block.listType == ListType.NUMBERED) {
                            listNumberCounter++
                        } else {
                            listNumberCounter = 1
                            null
                        }

                        BlockItemView(
                            block = block,
                            itemNumber = itemNumber,
                            isActive = index == activeBlockIndex,
                            onFocus = { onBlockSelected(index) },
                            onTextChanged = { newText -> onUpdateBlockText(index, newText) },
                            onEnterPressed = onInsertParagraph
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun BlockItemView(
    block: BlockElement,
    itemNumber: Int?,
    isActive: Boolean,
    onFocus: () -> Unit,
    onTextChanged: (String) -> Unit,
    onEnterPressed: () -> Unit
) {
    when (block) {
        is ParagraphBlock -> {
            val align = when (block.alignment) {
                TextAlignment.CENTER -> TextAlign.Center
                TextAlignment.END -> TextAlign.End
                TextAlignment.JUSTIFY -> TextAlign.Justify
                TextAlignment.START -> if (block.isRtl) TextAlign.End else TextAlign.Start
            }

            val textDirection = if (block.isRtl) TextDirection.Rtl else TextDirection.Ltr

            val customSizeSp = block.spans.lastOrNull { it.fontSizeSp != null }?.fontSizeSp
            val fontSize = customSizeSp?.sp ?: when (block.headingStyle) {
                HeadingStyle.TITLE -> 26.sp
                HeadingStyle.SUBTITLE -> 18.sp
                HeadingStyle.HEADING_1 -> 22.sp
                HeadingStyle.HEADING_2 -> 18.sp
                HeadingStyle.HEADING_3 -> 16.sp
                HeadingStyle.QUOTE -> 15.sp
                HeadingStyle.NORMAL -> 15.sp
            }

            val fontWeight = when {
                block.headingStyle.isBold || block.spans.any { it.isBold } -> FontWeight.Bold
                else -> FontWeight.Normal
            }

            val fontStyle = if (block.spans.any { it.isItalic } || block.headingStyle == HeadingStyle.QUOTE) {
                FontStyle.Italic
            } else FontStyle.Normal

            val fontFamilyName = block.spans.lastOrNull { it.fontFamilyName != null }?.fontFamilyName
            val fontFamily = when (fontFamilyName) {
                "Arial" -> FontFamily.SansSerif
                "Times New Roman", "Georgia" -> FontFamily.Serif
                "Courier New" -> FontFamily.Monospace
                "Nastaliq" -> FontFamily.Cursive
                else -> FontFamily.Default
            }

            val textColorHex = block.spans.lastOrNull { it.textColorHex != null }?.textColorHex
            val textColor = when {
                textColorHex != null -> try {
                    Color(android.graphics.Color.parseColor(textColorHex))
                } catch (e: Exception) { Color(0xFF1E293B) }
                block.headingStyle == HeadingStyle.TITLE || block.headingStyle == HeadingStyle.HEADING_1 -> WordPrimary
                else -> Color(0xFF1E293B)
            }

            val highlightHex = block.spans.lastOrNull { it.highlightColorHex != null }?.highlightColorHex
            val highlightColor = if (highlightHex != null) {
                try {
                    Color(android.graphics.Color.parseColor(highlightHex))
                } catch (e: Exception) { Color.Transparent }
            } else Color.Transparent

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onFocus)
                    .background(
                        if (isActive) WordPrimary.copy(alpha = 0.04f) else Color.Transparent,
                        RoundedCornerShape(4.dp)
                    )
                    .padding(horizontal = 4.dp, vertical = 2.dp),
                verticalAlignment = Alignment.Top
            ) {
                // List prefix
                when (block.listType) {
                    ListType.BULLET -> {
                        Text(
                            text = "• ",
                            fontWeight = FontWeight.Bold,
                            fontSize = fontSize,
                            color = WordPrimary,
                            modifier = Modifier.padding(end = 4.dp)
                        )
                    }
                    ListType.NUMBERED -> {
                        Text(
                            text = "${itemNumber ?: 1}. ",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = fontSize,
                            color = WordPrimary,
                            modifier = Modifier.padding(end = 4.dp)
                        )
                    }
                    ListType.NONE -> Unit
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(highlightColor, RoundedCornerShape(2.dp))
                ) {
                    BasicTextField(
                        value = block.text,
                        onValueChange = onTextChanged,
                        textStyle = TextStyle(
                            fontSize = fontSize,
                            fontWeight = fontWeight,
                            fontStyle = fontStyle,
                            fontFamily = fontFamily,
                            color = textColor,
                            textAlign = align,
                            textDirection = textDirection,
                            lineHeight = fontSize * block.lineSpacingMultiplier
                        ),
                        cursorBrush = SolidColor(WordPrimary),
                        modifier = Modifier.fillMaxWidth().testTag("paragraph_input_${block.id}")
                    )
                }
            }
        }

        is TableBlock -> {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(4.dp))
                    .padding(2.dp)
            ) {
                for (r in 0 until block.rowCount) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        for (c in 0 until block.columnCount) {
                            val cell = block.cells.getOrNull(r)?.getOrNull(c)
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .border(0.5.dp, Color(0xFFE2E8F0))
                                    .background(if (r == 0) Color(0xFFF1F5F9) else Color.White)
                                    .padding(8.dp)
                            ) {
                                Text(
                                    text = cell?.text?.ifBlank { "Cell ${r + 1},${c + 1}" } ?: "",
                                    fontWeight = if (r == 0) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 12.sp,
                                    color = Color.DarkGray
                                )
                            }
                        }
                    }
                }
            }
        }

        is ImageBlock -> {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFFF1F5F9),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .clickable(onClick = onFocus)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.Image, contentDescription = null, tint = WordPrimary, modifier = Modifier.size(36.dp))
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = block.caption.ifBlank { "Picture Asset" },
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
            }
        }

        is DividerBlock -> {
            HorizontalDivider(
                color = Color(0xFFCBD5E1),
                thickness = 1.dp,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        is PageBreakBlock -> {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HorizontalDivider(modifier = Modifier.weight(1f), color = WordPrimary.copy(alpha = 0.4f))
                Text(
                    text = " PAGE BREAK ",
                    fontSize = 10.sp,
                    color = WordPrimary,
                    fontWeight = FontWeight.Bold
                )
                HorizontalDivider(modifier = Modifier.weight(1f), color = WordPrimary.copy(alpha = 0.4f))
            }
        }
    }
}

@Composable
fun EditorStatusBar(
    document: RichDocument?,
    autosaveStatus: AutosaveStatus,
    zoomPercent: Int,
    activeBlock: ParagraphBlock?
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 4.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Page 1 of ${document?.estimatedPageCount ?: 1}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text("•", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                Text(
                    text = "${document?.wordCount ?: 0} words",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text("•", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                Text(
                    text = "${document?.characterCount ?: 0} chars",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text("•", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                Text(
                    text = if (activeBlock?.isRtl == true) "Urdu (RTL)" else "English (LTR)",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = WordPrimary
                )
            }

            Text(
                text = "$zoomPercent%",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

fun sharePdfFile(context: Context, file: File) {
    try {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val chooser = Intent.createChooser(sendIntent, "Share Document PDF")
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    } catch (e: Exception) {
        // Fallback
    }
}

fun shareTextFile(context: Context, file: File) {
    try {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val chooser = Intent.createChooser(sendIntent, "Share Plain Text Document")
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    } catch (e: Exception) {
        // Fallback
    }
}
