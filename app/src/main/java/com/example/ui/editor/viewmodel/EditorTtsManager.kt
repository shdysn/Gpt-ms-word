package com.example.ui.editor.viewmodel

import android.content.Context
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import com.example.data.model.ImageBlock
import com.example.data.model.ParagraphBlock
import com.example.data.model.ShapeBlock
import com.example.data.model.SignatureBlock
import com.example.data.model.TableBlock
import com.example.ui.editor.EditorViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

/**
 * Text-to-speech read aloud engine, sentence sequencer, and speech rate controller.
 */
class EditorTtsManager(
    private val vm: EditorViewModel
) {
    private var tts: TextToSpeech? = null
    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    private val _speakingParagraphIndex = MutableStateFlow<Int?>(null)
    val speakingParagraphIndex: StateFlow<Int?> = _speakingParagraphIndex.asStateFlow()

    private val _speechRate = MutableStateFlow(1.0f)
    val speechRate: StateFlow<Float> = _speechRate.asStateFlow()

    fun startReadAloud(context: Context) {
        val doc = vm.currentDocument ?: return
        val utterances = mutableListOf<String>()
        doc.blocks.forEach { block ->
            when (block) {
                is ParagraphBlock -> {
                    if (block.text.isNotBlank()) utterances.add(block.text)
                }
                is TableBlock -> {
                    val tableText = block.cells.flatten()
                        .map { it.text.trim() }
                        .filter { it.isNotBlank() }
                        .joinToString(", ")
                    if (tableText.isNotBlank()) utterances.add("Table with ${block.rowCount} rows: $tableText")
                }
                is ShapeBlock -> {
                    val shapeText = buildString {
                        if (block.title.isNotBlank()) append("${block.title}. ")
                        if (block.content.isNotBlank()) append(block.content)
                    }.trim()
                    if (shapeText.isNotBlank()) utterances.add("Note: $shapeText")
                }
                is ImageBlock -> {
                    if (block.caption.isNotBlank()) utterances.add("Image caption: ${block.caption}")
                }
                is SignatureBlock -> {
                    if (block.signerName.isNotBlank()) utterances.add("Digitally signed by ${block.signerName}, ${block.signerTitle}")
                }
                else -> Unit
            }
        }

        if (utterances.isEmpty()) {
            vm.showStatusMessage("Document contains no text to read aloud")
            return
        }

        if (tts == null) {
            tts = TextToSpeech(context.applicationContext) { status ->
                if (status == TextToSpeech.SUCCESS) {
                    tts?.language = Locale.getDefault()
                    tts?.setSpeechRate(_speechRate.value)
                    speakUtteranceSequence(utterances)
                } else {
                    vm.showStatusMessage("Failed to initialize Text-To-Speech engine")
                }
            }
        } else {
            tts?.setSpeechRate(_speechRate.value)
            speakUtteranceSequence(utterances)
        }
    }

    private fun speakUtteranceSequence(utterances: List<String>) {
        _isSpeaking.value = true
        tts?.stop()

        var uIndex = 0
        val total = utterances.size

        tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                _speakingParagraphIndex.value = uIndex
            }

            override fun onDone(utteranceId: String?) {
                uIndex++
                if (uIndex < total) {
                    speakNextUtterance(utterances[uIndex], uIndex)
                } else {
                    _isSpeaking.value = false
                    _speakingParagraphIndex.value = null
                }
            }

            override fun onError(utteranceId: String?) {
                _isSpeaking.value = false
                _speakingParagraphIndex.value = null
            }
        })

        speakNextUtterance(utterances[0], 0)
    }

    private fun speakNextUtterance(text: String, index: Int) {
        val params = Bundle()
        params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "u_$index")
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, params, "u_$index")
    }

    fun stopReadAloud() {
        tts?.stop()
        _isSpeaking.value = false
        _speakingParagraphIndex.value = null
    }

    fun setTtsSpeechRate(rate: Float) {
        _speechRate.value = rate.coerceIn(0.5f, 2.5f)
        tts?.setSpeechRate(_speechRate.value)
    }

    fun cycleSpeechRate() {
        val nextRate = when (_speechRate.value) {
            0.75f -> 1.0f
            1.0f -> 1.25f
            1.25f -> 1.5f
            1.5f -> 2.0f
            else -> 0.75f
        }
        setTtsSpeechRate(nextRate)
    }

    fun cleanupTts() {
        tts?.stop()
        tts?.shutdown()
        tts = null
    }
}
