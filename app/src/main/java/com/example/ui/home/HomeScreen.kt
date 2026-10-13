package com.example.ui.home

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.automirrored.filled.ViewList
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.DocumentEntity
import com.example.data.repository.TemplateType
import com.example.ui.home.components.CreateDocumentDialog
import com.example.ui.home.components.DeleteDocumentDialog
import com.example.ui.home.components.DocumentGridCard
import com.example.ui.home.components.DocumentItemRow
import com.example.ui.home.components.EmptyDocumentsView
import com.example.ui.home.components.HomeSettingsDialog
import com.example.ui.home.components.RenameDocumentDialog
import com.example.ui.home.components.TemplateCard
import com.example.ui.home.components.shareDocxFile
import com.example.ui.home.components.shareFile
import com.example.ui.theme.WordPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onNavigateToEditor: (String) -> Unit
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    val documents by viewModel.documents.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val activeFilter by viewModel.activeFilter.collectAsStateWithLifecycle()
    val sortOrder by viewModel.sortOrder.collectAsStateWithLifecycle()
    val isGridView by viewModel.isGridView.collectAsStateWithLifecycle()
    val infoMessage by viewModel.infoMessage.collectAsStateWithLifecycle()

    val isAutosaveEnabled by viewModel.isAutosaveEnabled.collectAsStateWithLifecycle()
    val defaultRtl by viewModel.defaultRtl.collectAsStateWithLifecycle()
    val showWordCount by viewModel.showWordCount.collectAsStateWithLifecycle()
    val showRuler by viewModel.showRuler.collectAsStateWithLifecycle()

    var showCreateDialog by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }
    var showSortMenu by remember { mutableStateOf(false) }
    var renameTargetDoc by remember { mutableStateOf<DocumentEntity?>(null) }
    var deleteTargetDoc by remember { mutableStateOf<DocumentEntity?>(null) }

    // SAF Import Launcher
    val safImportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            try {
                val takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                context.contentResolver.takePersistableUriPermission(it, takeFlags)
            } catch (_: Exception) {
                // Safely ignored if provider does not support persistent grant
            }
            viewModel.importDocument(context, it) { docId ->
                onNavigateToEditor(docId)
            }
        }
    }

    LaunchedEffect(infoMessage) {
        infoMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearInfoMessage()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = WordPrimary,
                            modifier = Modifier.size(38.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "W",
                                    color = Color.White,
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Smart Word",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Word Processor",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            safImportLauncher.launch(
                                arrayOf(
                                    "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                                    "application/msword",
                                    "*/*"
                                )
                            )
                        },
                        modifier = Modifier.testTag("action_open_document")
                    ) {
                        Icon(
                            imageVector = Icons.Default.FolderOpen,
                            contentDescription = "Open Document"
                        )
                    }
                    IconButton(
                        onClick = { viewModel.toggleViewMode() },
                        modifier = Modifier.testTag("toggle_view_mode")
                    ) {
                        Icon(
                            imageVector = if (isGridView) Icons.AutoMirrored.Filled.ViewList else Icons.Default.GridView,
                            contentDescription = "Toggle Grid or List View"
                        )
                    }
                    IconButton(
                        onClick = { showSettingsDialog = true },
                        modifier = Modifier.testTag("action_settings")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    viewModel.createDocument("Untitled Document", TemplateType.BLANK) { docId ->
                        onNavigateToEditor(docId)
                    }
                },
                containerColor = WordPrimary,
                contentColor = Color.White,
                modifier = Modifier.testTag("fab_new_document")
            ) {
                Icon(Icons.Default.Add, contentDescription = "New Document")
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(bottom = 88.dp)
        ) {
            // Document Search Bar
            item {
                Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = viewModel::onSearchQueryChanged,
                        placeholder = { Text("Search documents by title or keywords...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { viewModel.onSearchQueryChanged("") }) {
                                    Icon(Icons.Default.Close, contentDescription = "Clear")
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("search_text_field")
                    )
                }
            }

            // Quick Primary Action Buttons (New Document + Open Document)
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = {
                            viewModel.createDocument("Untitled Document", TemplateType.BLANK) { docId ->
                                onNavigateToEditor(docId)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = WordPrimary),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .testTag("btn_new_document")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("New Document", fontWeight = FontWeight.SemiBold)
                    }

                    OutlinedButton(
                        onClick = {
                            safImportLauncher.launch(
                                arrayOf(
                                    "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                                    "application/msword",
                                    "*/*"
                                )
                            )
                        },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .testTag("btn_open_document")
                    ) {
                        Icon(Icons.Default.FolderOpen, contentDescription = null, modifier = Modifier.size(18.dp), tint = WordPrimary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Open DOCX", color = WordPrimary, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            // Templates Carousel Section
            item {
                Column(modifier = Modifier.padding(top = 12.dp, bottom = 8.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Start from a template",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        TextButton(onClick = { showCreateDialog = true }) {
                            Text("More templates", color = WordPrimary)
                        }
                    }

                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(TemplateType.entries.toTypedArray()) { template ->
                            TemplateCard(
                                template = template,
                                onClick = {
                                    viewModel.createDocument(template.title, template) { docId ->
                                        onNavigateToEditor(docId)
                                    }
                                }
                            )
                        }
                    }
                }
            }

            // Filter Tabs (All / Recent / Pinned) & Sort Selector
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        FilterChip(
                            selected = activeFilter == HomeFilter.ALL,
                            onClick = { viewModel.onFilterChanged(HomeFilter.ALL) },
                            label = { Text("All") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                        FilterChip(
                            selected = activeFilter == HomeFilter.RECENT,
                            onClick = { viewModel.onFilterChanged(HomeFilter.RECENT) },
                            label = { Text("Recent") }
                        )
                        FilterChip(
                            selected = activeFilter == HomeFilter.PINNED,
                            onClick = { viewModel.onFilterChanged(HomeFilter.PINNED) },
                            label = { Text("Pinned") }
                        )
                        FilterChip(
                            selected = activeFilter == HomeFilter.FAVORITES,
                            onClick = { viewModel.onFilterChanged(HomeFilter.FAVORITES) },
                            label = { Text("Favorites") }
                        )
                    }

                    // Sort Order Dropdown
                    Box {
                        AssistChip(
                            onClick = { showSortMenu = true },
                            label = { Text(sortOrder.displayName, fontSize = 12.sp) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Sort,
                                    contentDescription = "Sort options",
                                    modifier = Modifier.size(15.dp)
                                )
                            },
                            modifier = Modifier.testTag("btn_sort_documents")
                        )

                        DropdownMenu(
                            expanded = showSortMenu,
                            onDismissRequest = { showSortMenu = false }
                        ) {
                            DocumentSortOrder.entries.forEach { order ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = order.displayName,
                                            fontWeight = if (sortOrder == order) FontWeight.Bold else FontWeight.Normal,
                                            color = if (sortOrder == order) WordPrimary else MaterialTheme.colorScheme.onSurface
                                        )
                                    },
                                    onClick = {
                                        viewModel.onSortOrderChanged(order)
                                        showSortMenu = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Section Header
            item {
                val sectionHeaderTitle = when {
                    searchQuery.isNotBlank() -> "Search Results (${documents.size})"
                    activeFilter == HomeFilter.PINNED -> "Pinned Documents (${documents.size})"
                    activeFilter == HomeFilter.RECENT -> "Recent Documents (${documents.size})"
                    else -> "All Documents (${documents.size})"
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = sectionHeaderTitle,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Empty State
            if (documents.isEmpty()) {
                item {
                    EmptyDocumentsView(
                        isSearching = searchQuery.isNotBlank(),
                        activeFilter = activeFilter,
                        onClearFilter = {
                            viewModel.onSearchQueryChanged("")
                            viewModel.onFilterChanged(HomeFilter.ALL)
                        },
                        onCreateClick = {
                            viewModel.createDocument("Untitled Document", TemplateType.BLANK) {
                                onNavigateToEditor(it)
                            }
                        }
                    )
                }
            } else {
                // Documents Items (List or Grid)
                if (isGridView) {
                    val chunkedDocs = documents.chunked(2)
                    items(chunkedDocs) { rowDocs ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            for (doc in rowDocs) {
                                Box(modifier = Modifier.weight(1f)) {
                                    DocumentGridCard(
                                        doc = doc,
                                        onClick = { onNavigateToEditor(doc.id) },
                                        onTogglePin = { viewModel.togglePinned(doc.id, doc.isPinned) },
                                        onRename = { renameTargetDoc = doc },
                                        onDuplicate = { viewModel.duplicateDocument(doc.id) },
                                        onExportDocx = {
                                            viewModel.exportDocumentToDocx(context, doc.id) { file ->
                                                file?.let { shareDocxFile(context, it) }
                                            }
                                        },
                                        onExportPdf = {
                                            viewModel.exportDocumentToPdf(context, doc.id) { file ->
                                                file?.let { shareFile(context, it) }
                                            }
                                        },
                                        onPrint = { viewModel.printDocument(context, doc.id) },
                                        onDelete = { deleteTargetDoc = doc }
                                    )
                                }
                            }
                            if (rowDocs.size == 1) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                } else {
                    items(documents, key = { it.id }) { doc ->
                        DocumentItemRow(
                            doc = doc,
                            onClick = { onNavigateToEditor(doc.id) },
                            onTogglePin = { viewModel.togglePinned(doc.id, doc.isPinned) },
                            onRename = { renameTargetDoc = doc },
                            onDuplicate = { viewModel.duplicateDocument(doc.id) },
                            onExportDocx = {
                                viewModel.exportDocumentToDocx(context, doc.id) { file ->
                                    file?.let { shareDocxFile(context, it) }
                                }
                            },
                            onExportPdf = {
                                viewModel.exportDocumentToPdf(context, doc.id) { file ->
                                    file?.let { shareFile(context, it) }
                                }
                            },
                            onPrint = { viewModel.printDocument(context, doc.id) },
                            onDelete = { deleteTargetDoc = doc }
                        )
                    }
                }
            }
        }
    }

    // Settings Dialog
    if (showSettingsDialog) {
        HomeSettingsDialog(
            isAutosaveEnabled = isAutosaveEnabled,
            onAutosaveChanged = viewModel::setAutosaveEnabled,
            defaultRtl = defaultRtl,
            onDefaultRtlChanged = viewModel::setDefaultRtl,
            showWordCount = showWordCount,
            onShowWordCountChanged = viewModel::setShowWordCount,
            showRuler = showRuler,
            onShowRulerChanged = viewModel::setShowRuler,
            onDismiss = { showSettingsDialog = false }
        )
    }

    // New Document Dialog
    if (showCreateDialog) {
        CreateDocumentDialog(
            onDismiss = { showCreateDialog = false },
            onConfirm = { title, template ->
                showCreateDialog = false
                viewModel.createDocument(title, template) {
                    onNavigateToEditor(it)
                }
            }
        )
    }

    // Rename Dialog
    renameTargetDoc?.let { target ->
        RenameDocumentDialog(
            doc = target,
            onDismiss = { renameTargetDoc = null },
            onConfirm = { newTitle ->
                viewModel.renameDocument(target.id, newTitle)
                renameTargetDoc = null
            }
        )
    }

    // Delete Confirmation Dialog
    deleteTargetDoc?.let { target ->
        DeleteDocumentDialog(
            doc = target,
            onDismiss = { deleteTargetDoc = null },
            onConfirm = {
                viewModel.deleteDocument(target.id)
                deleteTargetDoc = null
            }
        )
    }
}
