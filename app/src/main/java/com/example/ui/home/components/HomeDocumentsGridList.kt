package com.example.ui.home.components

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.data.model.DocumentEntity

fun LazyListScope.homeDocumentsGridList(
    documents: List<DocumentEntity>,
    isGridView: Boolean,
    context: Context,
    onNavigateToEditor: (String) -> Unit,
    onTogglePin: (String, Boolean) -> Unit,
    onRename: (DocumentEntity) -> Unit,
    onDuplicate: (String) -> Unit,
    onExportDocx: (String) -> Unit,
    onExportPdf: (String) -> Unit,
    onPrint: (String) -> Unit,
    onDelete: (DocumentEntity) -> Unit
) {
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
                            onTogglePin = { onTogglePin(doc.id, doc.isPinned) },
                            onRename = { onRename(doc) },
                            onDuplicate = { onDuplicate(doc.id) },
                            onExportDocx = { onExportDocx(doc.id) },
                            onExportPdf = { onExportPdf(doc.id) },
                            onPrint = { onPrint(doc.id) },
                            onDelete = { onDelete(doc) }
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
                onTogglePin = { onTogglePin(doc.id, doc.isPinned) },
                onRename = { onRename(doc) },
                onDuplicate = { onDuplicate(doc.id) },
                onExportDocx = { onExportDocx(doc.id) },
                onExportPdf = { onExportPdf(doc.id) },
                onPrint = { onPrint(doc.id) },
                onDelete = { onDelete(doc) }
            )
        }
    }
}
