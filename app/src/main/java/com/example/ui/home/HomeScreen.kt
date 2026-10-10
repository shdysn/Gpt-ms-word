package com.example.ui.home

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.PictureAsPdf
import androidx.compose.material.icons.outlined.Print
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.DocumentEntity
import com.example.data.repository.TemplateType
import com.example.ui.theme.WordPrimary
import com.example.ui.theme.WordPrimaryDark
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onNavigateToEditor: (String) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val documents by viewModel.documents.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val activeFilter by viewModel.activeFilter.collectAsStateWithLifecycle()
    val isGridView by viewModel.isGridView.collectAsStateWithLifecycle()
    val infoMessage by viewModel.infoMessage.collectAsStateWithLifecycle()

    var showCreateDialog by remember { mutableStateOf(false) }
    var selectedTemplateForCreation by remember { mutableStateOf(TemplateType.BLANK) }
    var renameTargetDoc by remember { mutableStateOf<DocumentEntity?>(null) }
    var deleteTargetDoc by remember { mutableStateOf<DocumentEntity?>(null) }

    // SAF Import Launcher
    val safImportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
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
                            modifier = Modifier.size(36.dp)
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
                        onClick = { safImportLauncher.launch(arrayOf("*/*")) },
                        modifier = Modifier.testTag("import_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.FileDownload,
                            contentDescription = "Import Document"
                        )
                    }
                    IconButton(
                        onClick = { viewModel.toggleViewMode() },
                        modifier = Modifier.testTag("toggle_view_mode")
                    ) {
                        Icon(
                            imageVector = if (isGridView) Icons.Default.ViewList else Icons.Default.GridView,
                            contentDescription = "Toggle Grid or List View"
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
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            // Search Bar
            item {
                Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = viewModel::onSearchQueryChanged,
                        placeholder = { Text("Search documents, keywords...") },
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

            // Templates Carousel
            item {
                Column(modifier = Modifier.padding(top = 8.dp, bottom = 12.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Start a new document",
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

            // Filters Row
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = activeFilter == HomeFilter.ALL,
                        onClick = { viewModel.onFilterChanged(HomeFilter.ALL) },
                        label = { Text("All Documents") },
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
                }
            }

            // Document List Header
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (searchQuery.isNotBlank()) "Search Results (${documents.size})" else "Recent Documents",
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
                        onCreateClick = {
                            viewModel.createDocument("Untitled Document", TemplateType.BLANK) {
                                onNavigateToEditor(it)
                            }
                        }
                    )
                }
            } else {
                // Documents Items
                items(documents, key = { it.id }) { doc ->
                    DocumentItemRow(
                        doc = doc,
                        onClick = { onNavigateToEditor(doc.id) },
                        onTogglePin = { viewModel.togglePinned(doc.id, doc.isPinned) },
                        onRename = { renameTargetDoc = doc },
                        onDuplicate = { viewModel.duplicateDocument(doc.id) },
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

    // New Document Dialog
    if (showCreateDialog) {
        var newTitle by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            title = { Text("Create New Document") },
            text = {
                Column {
                    OutlinedTextField(
                        value = newTitle,
                        onValueChange = { newTitle = it },
                        label = { Text("Document Title") },
                        placeholder = { Text(selectedTemplateForCreation.title) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Select Template:", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(8.dp))
                    TemplateType.entries.forEach { t ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedTemplateForCreation = t }
                                .padding(vertical = 4.dp)
                        ) {
                            Text(
                                text = t.title,
                                color = if (selectedTemplateForCreation == t) WordPrimary else MaterialTheme.colorScheme.onSurface,
                                fontWeight = if (selectedTemplateForCreation == t) FontWeight.Bold else FontWeight.Normal,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val finalTitle = newTitle.ifBlank { selectedTemplateForCreation.title }
                        showCreateDialog = false
                        viewModel.createDocument(finalTitle, selectedTemplateForCreation) {
                            onNavigateToEditor(it)
                        }
                    },
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = WordPrimary)
                ) {
                    Text("Create")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) { Text("Cancel") }
            }
        )
    }

    // Rename Dialog
    renameTargetDoc?.let { target ->
        var renameText by remember { mutableStateOf(target.title) }
        AlertDialog(
            onDismissRequest = { renameTargetDoc = null },
            title = { Text("Rename Document") },
            text = {
                OutlinedTextField(
                    value = renameText,
                    onValueChange = { renameText = it },
                    label = { Text("New Title") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (renameText.isNotBlank()) {
                            viewModel.renameDocument(target.id, renameText.trim())
                        }
                        renameTargetDoc = null
                    },
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = WordPrimary)
                ) {
                    Text("Rename")
                }
            },
            dismissButton = {
                TextButton(onClick = { renameTargetDoc = null }) { Text("Cancel") }
            }
        )
    }

    // Delete Confirmation Dialog
    deleteTargetDoc?.let { target ->
        AlertDialog(
            onDismissRequest = { deleteTargetDoc = null },
            title = { Text("Delete Document") },
            text = { Text("Are you sure you want to delete '${target.title}'? This action cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteDocument(target.id)
                        deleteTargetDoc = null
                    },
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { deleteTargetDoc = null }) { Text("Cancel") }
            }
        )
    }
}

@Composable
fun TemplateCard(
    template: TemplateType,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(130.dp)
            .height(170.dp)
            .clickable(onClick = onClick)
            .testTag("template_${template.name.lowercase()}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Simulated Paper Thumbnail
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(95.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xFFF8F9FA))
                    .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(6.dp))
                    .padding(8.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    // Header line
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.6f)
                            .height(6.dp)
                            .background(WordPrimary, RoundedCornerShape(3.dp))
                    )
                    // Content lines
                    Box(modifier = Modifier.fillMaxWidth().height(4.dp).background(Color(0xFFCBD5E1), RoundedCornerShape(2.dp)))
                    Box(modifier = Modifier.fillMaxWidth(0.85f).height(4.dp).background(Color(0xFFE2E8F0), RoundedCornerShape(2.dp)))
                    Box(modifier = Modifier.fillMaxWidth(0.7f).height(4.dp).background(Color(0xFFE2E8F0), RoundedCornerShape(2.dp)))
                }
            }

            Column {
                Text(
                    text = template.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun DocumentItemRow(
    doc: DocumentEntity,
    onClick: () -> Unit,
    onTogglePin: () -> Unit,
    onRename: () -> Unit,
    onDuplicate: () -> Unit,
    onExportPdf: () -> Unit,
    onPrint: () -> Unit,
    onDelete: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }
    val dateFormat = remember { SimpleDateFormat("MMM d, yyyy • h:mm a", Locale.getDefault()) }
    val formattedDate = remember(doc.updatedAt) { dateFormat.format(Date(doc.updatedAt)) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clickable(onClick = onClick)
            .testTag("doc_item_${doc.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Paper Icon Preview
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(46.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Description,
                        contentDescription = null,
                        tint = WordPrimary,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Details
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = doc.title,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    if (doc.isPinned) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.Default.PushPin,
                            contentDescription = "Pinned",
                            tint = WordPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = doc.snippet,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = formattedDate,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                    Text("•", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                    Text(
                        text = "${doc.wordCount} words",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                    Text("•", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                    Text(
                        text = "${doc.pageSize} (${doc.pageCount} p)",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }

            // Options Overflow Menu
            Box {
                IconButton(onClick = { menuExpanded = true }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "More Options")
                }

                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text(if (doc.isPinned) "Unpin" else "Pin to top") },
                        leadingIcon = {
                            Icon(
                                if (doc.isPinned) Icons.Default.PushPin else Icons.Outlined.PushPin,
                                contentDescription = null
                            )
                        },
                        onClick = {
                            menuExpanded = false
                            onTogglePin()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Rename") },
                        leadingIcon = { Icon(Icons.Outlined.Edit, contentDescription = null) },
                        onClick = {
                            menuExpanded = false
                            onRename()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Duplicate") },
                        leadingIcon = { Icon(Icons.Outlined.ContentCopy, contentDescription = null) },
                        onClick = {
                            menuExpanded = false
                            onDuplicate()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Export as PDF") },
                        leadingIcon = { Icon(Icons.Outlined.PictureAsPdf, contentDescription = null) },
                        onClick = {
                            menuExpanded = false
                            onExportPdf()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Print") },
                        leadingIcon = { Icon(Icons.Outlined.Print, contentDescription = null) },
                        onClick = {
                            menuExpanded = false
                            onPrint()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Delete", color = MaterialTheme.colorScheme.error) },
                        leadingIcon = {
                            Icon(
                                Icons.Outlined.Delete,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error
                            )
                        },
                        onClick = {
                            menuExpanded = false
                            onDelete()
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun EmptyDocumentsView(
    isSearching: Boolean,
    onCreateClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.size(72.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.Description,
                    contentDescription = null,
                    tint = WordPrimary,
                    modifier = Modifier.size(36.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = if (isSearching) "No matching documents" else "No documents yet",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = if (isSearching) "Try a different search term" else "Create your first document or start from a template above.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )

        if (!isSearching) {
            Spacer(modifier = Modifier.height(18.dp))
            Button(
                onClick = onCreateClick,
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = WordPrimary)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("New Document")
            }
        }
    }
}

fun shareFile(context: android.content.Context, file: File) {
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
