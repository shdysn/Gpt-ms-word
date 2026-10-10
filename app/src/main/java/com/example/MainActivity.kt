package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.data.local.AppDatabase
import com.example.data.local.PreferencesManager
import com.example.data.repository.DocumentRepositoryImpl
import com.example.data.service.DocumentImportServiceImpl
import com.example.data.service.PdfExportServiceImpl
import com.example.data.storage.DocumentStorageManager
import com.example.ui.navigation.WordNavGraph
import com.example.ui.theme.SmartWordTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize Data & Storage Layer
        val database = AppDatabase.getDatabase(applicationContext)
        val storageManager = DocumentStorageManager(applicationContext)
        val exportService = PdfExportServiceImpl()
        val importService = DocumentImportServiceImpl(storageManager)
        val preferencesManager = PreferencesManager(applicationContext)

        val repository = DocumentRepositoryImpl(
            documentDao = database.documentDao(),
            storageManager = storageManager,
            exportService = exportService,
            importService = importService
        )

        setContent {
            SmartWordTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    WordNavGraph(
                        repository = repository,
                        preferencesManager = preferencesManager
                    )
                }
            }
        }
    }
}
