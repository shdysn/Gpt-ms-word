package com.example.ui.home

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.DocumentEntity
import com.example.data.repository.TemplateType
import com.example.ui.home.components.EmptyDocumentsView
import com.example.ui.home.components.HomeDialogsHost
import com.example.ui.home.components.HomeFilterSortBar
import com.example.ui.home.components.HomeSearchBarAndActions
import com.example.ui.home.components.HomeTemplatesCarousel
import com.example.ui.home.components.HomeTopAppBar
import com.example.ui.home.components.homeDocumentsGridList
import com.example.ui.home.components.shareDocxFile
import com.example.ui.home.components.shareFile
import com.example.ui.theme.WordPrimary

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
    var renameTargetDoc by remember { mutableStateOf<DocumentEntity?>(null) }
    var deleteTargetDoc by remember { mutableStateOf<DocumentEntity?>(null) }

    val safImportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            try {
                val takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                context.contentResolver.takePersistableUriPermission(it, takeFlags)
            } catch (_: Exception) {}
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
            HomeTopAppBar(
                isGridView = isGridView,
                onOpenDocument = {
                    safImportLauncher.launch(
                        arrayOf(
                            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                            "application/msword",
                            "text/markdown",
                            "text/plain",
                            "*/*"
                        )
                    )
                },
                onToggleViewMode = { viewModel.toggleViewMode() },
                onOpenSettings = { showSettingsDialog = true }
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
            item {
                HomeSearchBarAndActions(
                    searchQuery = searchQuery,
                    onSearchQueryChanged = viewModel::onSearchQueryChanged,
                    onNewDocument = {
                        viewModel.createDocument("Untitled Document", TemplateType.BLANK) { docId ->
                            onNavigateToEditor(docId)
                        }
                    },
                    onOpenDocx = {
                        safImportLauncher.launch(
                            arrayOf(
                                "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                                "application/msword",
                                "*/*"
                            )
                        )
                    }
                )
            }

            item {
                HomeTemplatesCarousel(
                    onOpenMoreTemplates = { showCreateDialog = true },
                    onSelectTemplate = { template ->
                        viewModel.createDocument(template.title, template) { docId ->
                            onNavigateToEditor(docId)
                        }
                    }
                )
            }

            item {
                HomeFilterSortBar(
                    activeFilter = activeFilter,
                    sortOrder = sortOrder,
                    onFilterChanged = viewModel::onFilterChanged,
                    onSortOrderChanged = viewModel::onSortOrderChanged
                )
            }

            item {
                val sectionHeaderTitle = when {
                    searchQuery.isNotBlank() -> "Search Results (${documents.size})"
                    activeFilter == HomeFilter.PINNED -> "Pinned Documents (${documents.size})"
                    activeFilter == HomeFilter.RECENT -> "Recent Documents (${documents.size})"
                    else -> "All Documents (${documents.size})"
                }
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
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
                homeDocumentsGridList(
                    documents = documents,
                    isGridView = isGridView,
                    context = context,
                    onNavigateToEditor = onNavigateToEditor,
                    onTogglePin = { id, pinned -> viewModel.togglePinned(id, pinned) },
                    onRename = { renameTargetDoc = it },
                    onDuplicate = { viewModel.duplicateDocument(it) },
                    onExportDocx = { id ->
                        viewModel.exportDocumentToDocx(context, id) { file ->
                            file?.let { shareDocxFile(context, it) }
                        }
                    },
                    onExportPdf = { id ->
                        viewModel.exportDocumentToPdf(context, id) { file ->
                            file?.let { shareFile(context, it) }
                        }
                    },
                    onPrint = { id -> viewModel.printDocument(context, id) },
                    onDelete = { deleteTargetDoc = it }
                )
            }
        }
    }

    HomeDialogsHost(
        showSettingsDialog = showSettingsDialog,
        isAutosaveEnabled = isAutosaveEnabled,
        onAutosaveChanged = viewModel::setAutosaveEnabled,
        defaultRtl = defaultRtl,
        onDefaultRtlChanged = viewModel::setDefaultRtl,
        showWordCount = showWordCount,
        onShowWordCountChanged = viewModel::setShowWordCount,
        showRuler = showRuler,
        onShowRulerChanged = viewModel::setShowRuler,
        onDismissSettings = { showSettingsDialog = false },
        showCreateDialog = showCreateDialog,
        onDismissCreate = { showCreateDialog = false },
        onCreateDocument = { title, template ->
            viewModel.createDocument(title, template) { onNavigateToEditor(it) }
        },
        renameTargetDoc = renameTargetDoc,
        onDismissRename = { renameTargetDoc = null },
        onRenameDocument = viewModel::renameDocument,
        deleteTargetDoc = deleteTargetDoc,
        onDismissDelete = { deleteTargetDoc = null },
        onDeleteDocument = viewModel::deleteDocument
    )
}
