package com.example.data.system

import android.content.Context
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import com.example.data.model.AppLanguage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

/**
 * VoiceSpeechHelper manages Android TextToSpeech (TTS) for the AI Support Voice Conversation.
 * Speaks AI responses aloud with zero network latency using Android's native speech engine.
 */
class VoiceSpeechHelper(context: Context) {

    private val appContext = context.applicationContext
    private var textToSpeech: TextToSpeech? = null
    private var isInitialized = false

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    private val _currentUtteranceId = MutableStateFlow<String?>(null)
    val currentUtteranceId: StateFlow<String?> = _currentUtteranceId.asStateFlow()

    init {
        initTts()
    }

    private fun initTts() {
        try {
            textToSpeech = TextToSpeech(appContext) { status ->
                if (status == TextToSpeech.SUCCESS) {
                    isInitialized = true
                    textToSpeech?.setSpeechRate(1.0f)
                    textToSpeech?.setPitch(1.05f)

                    // Default to French
                    val res = textToSpeech?.setLanguage(Locale.FRENCH)
                    if (res == TextToSpeech.LANG_MISSING_DATA || res == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Log.w("VoiceSpeechHelper", "French language not supported in TTS, trying default")
                        textToSpeech?.setLanguage(Locale.getDefault())
                    }

                    setupListener()
                } else {
                    Log.e("VoiceSpeechHelper", "Failed to initialize TextToSpeech: status=$status")
                }
            }
        } catch (e: Exception) {
            Log.e("VoiceSpeechHelper", "Error initializing TextToSpeech", e)
        }
    }

    private fun setupListener() {
        textToSpeech?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                _isSpeaking.value = true
                _currentUtteranceId.value = utteranceId
            }

            override fun onDone(utteranceId: String?) {
                _isSpeaking.value = false
                _currentUtteranceId.value = null
            }

            @Deprecated("Deprecated in Java")
            override fun onError(utteranceId: String?) {
                _isSpeaking.value = false
                _currentUtteranceId.value = null
            }

            override fun onError(utteranceId: String?, errorCode: Int) {
                _isSpeaking.value = false
                _currentUtteranceId.value = null
                Log.w("VoiceSpeechHelper", "TTS Error: $errorCode on utterance: $utteranceId")
            }
        })
    }

    /**
     * Speak text aloud in the requested language (French, Arabic, or English)
     */
    fun speak(
        text: String,
        language: AppLanguage,
        utteranceId: String = "ai_msg_${System.currentTimeMillis()}"
    ) {
        if (text.isBlank()) return

        if (textToSpeech == null || !isInitialized) {
            initTts()
        }

        try {
            val targetLocale = when (language) {
                AppLanguage.FRENCH -> Locale.FRENCH
                AppLanguage.ENGLISH -> Locale.US
                AppLanguage.ARABIC -> Locale("ar")
            }

            textToSpeech?.language = targetLocale

            val params = Bundle().apply {
                putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, utteranceId)
            }

            textToSpeech?.speak(
                text,
                TextToSpeech.QUEUE_FLUSH,
                params,
                utteranceId
            )
        } catch (e: Exception) {
            Log.e("VoiceSpeechHelper", "Error in speak()", e)
            _isSpeaking.value = false
        }
    }

    /**
     * Stop any ongoing speech
     */
    fun stop() {
        try {
            textToSpeech?.stop()
        } catch (e: Exception) {
            Log.e("VoiceSpeechHelper", "Error stopping TTS", e)
        } finally {
            _isSpeaking.value = false
            _currentUtteranceId.value = null
        }
    }

    /**
     * Clean up resources
     */
    fun shutdown() {
        try {
            textToSpeech?.stop()
            textToSpeech?.shutdown()
        } catch (e: Exception) {
            Log.e("VoiceSpeechHelper", "Error shutting down TTS", e)
        }
    }
}
