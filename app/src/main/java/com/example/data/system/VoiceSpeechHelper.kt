package com.example.data.system

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.speech.tts.Voice
import android.util.Log
import com.example.data.model.AppLanguage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.math.sin

/**
 * Voice Personas for ultra-realistic human-like voice synthesis.
 */
enum class VoicePersona(
    val id: String,
    val displayNameFr: String,
    val displayNameAr: String,
    val displayNameEn: String,
    val avatarEmoji: String,
    val gender: String,
    val pitch: Float,
    val speed: Float
) {
    SARA(
        id = "sara",
        displayNameFr = "Sara (Voix féminine naturelle)",
        displayNameAr = "سارة (صوت طبيعي دافئ)",
        displayNameEn = "Sara (Warm Natural Female Voice)",
        avatarEmoji = "👩🏻‍💼",
        gender = "female",
        pitch = 1.03f,
        speed = 0.98f
    ),
    KARIM(
        id = "karim",
        displayNameFr = "Karim (Voix masculine amicale)",
        displayNameAr = "كريم (صوت طبيعي ودود)",
        displayNameEn = "Karim (Friendly Natural Male Voice)",
        avatarEmoji = "👨🏻‍💼",
        gender = "male",
        pitch = 0.93f,
        speed = 1.01f
    )
}

/**
 * Ultra-Realistic Human Voice Engine for ALLO AZIZ AI.
 * Features:
 * - High-Fidelity Neural voice selection from Android TextToSpeech engine
 * - Dual persona switching (Sara / Karim)
 * - Human conversational prosody (micro-pauses, currency/number normalization)
 * - Soft acoustic earcons (listening & affirmation tones)
 * - Dynamic real-time speech waveform simulation
 * - Instant barge-in / zero-latency speech interruption
 */
class VoiceSpeechHelper(context: Context) {

    private val appContext = context.applicationContext
    private var textToSpeech: TextToSpeech? = null
    private var isInitialized = false

    private val scope = CoroutineScope(Dispatchers.Main + Job())
    private var waveformJob: Job? = null

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    private val _currentUtteranceId = MutableStateFlow<String?>(null)
    val currentUtteranceId: StateFlow<String?> = _currentUtteranceId.asStateFlow()

    private val _activePersona = MutableStateFlow(VoicePersona.SARA)
    val activePersona: StateFlow<VoicePersona> = _activePersona.asStateFlow()

    // 8-band animated audio frequency visualizer amplitudes
    private val _speechAmplitudes = MutableStateFlow(listOf(0.2f, 0.3f, 0.4f, 0.5f, 0.4f, 0.3f, 0.2f, 0.15f))
    val speechAmplitudes: StateFlow<List<Float>> = _speechAmplitudes.asStateFlow()

    init {
        initTts()
    }

    private fun initTts() {
        try {
            textToSpeech = TextToSpeech(appContext) { status ->
                if (status == TextToSpeech.SUCCESS) {
                    isInitialized = true
                    applyPersonaConfig(_activePersona.value)
                    setupListener()
                } else {
                    Log.e("VoiceSpeechHelper", "Failed to initialize TextToSpeech: status=$status")
                }
            }
        } catch (e: Exception) {
            Log.e("VoiceSpeechHelper", "Error initializing TextToSpeech", e)
        }
    }

    fun setPersona(persona: VoicePersona) {
        _activePersona.value = persona
        applyPersonaConfig(persona)
    }

    private fun applyPersonaConfig(persona: VoicePersona) {
        textToSpeech?.let { tts ->
            tts.setPitch(persona.pitch)
            tts.setSpeechRate(persona.speed)
        }
    }

    private fun setupListener() {
        textToSpeech?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                _isSpeaking.value = true
                _currentUtteranceId.value = utteranceId
                startWaveformSimulation()
            }

            override fun onDone(utteranceId: String?) {
                _isSpeaking.value = false
                _currentUtteranceId.value = null
                stopWaveformSimulation()
            }

            @Deprecated("Deprecated in Java")
            override fun onError(utteranceId: String?) {
                _isSpeaking.value = false
                _currentUtteranceId.value = null
                stopWaveformSimulation()
            }

            override fun onError(utteranceId: String?, errorCode: Int) {
                _isSpeaking.value = false
                _currentUtteranceId.value = null
                stopWaveformSimulation()
                Log.w("VoiceSpeechHelper", "TTS Error: $errorCode on utterance: $utteranceId")
            }
        })
    }

    private fun startWaveformSimulation() {
        waveformJob?.cancel()
        waveformJob = scope.launch(Dispatchers.Default) {
            var phase = 0.0
            while (isActive && _isSpeaking.value) {
                phase += 0.35
                val bands = List(8) { i ->
                    val wave = (sin(phase + i * 0.8) + 1.0) / 2.0
                    (0.25f + (wave * 0.70f).toFloat()).coerceIn(0.15f, 1.0f)
                }
                _speechAmplitudes.value = bands
                delay(70)
            }
            _speechAmplitudes.value = listOf(0.15f, 0.2f, 0.25f, 0.25f, 0.2f, 0.15f, 0.15f, 0.1f)
        }
    }

    private fun stopWaveformSimulation() {
        waveformJob?.cancel()
        waveformJob = null
        _speechAmplitudes.value = listOf(0.15f, 0.2f, 0.25f, 0.25f, 0.2f, 0.15f, 0.15f, 0.1f)
    }

    /**
     * Finds and assigns the highest quality neural/natural voice for the target language.
     */
    private fun selectBestVoiceForLanguage(targetLocale: Locale, persona: VoicePersona) {
        val tts = textToSpeech ?: return
        try {
            val availableVoices = tts.voices
            if (!availableVoices.isNullOrEmpty()) {
                val candidateVoices = availableVoices.filter { voice ->
                    voice.locale.language.equals(targetLocale.language, ignoreCase = true)
                }

                if (candidateVoices.isNotEmpty()) {
                    val targetGender = persona.gender.lowercase()
                    val bestVoice = candidateVoices.maxByOrNull { voice ->
                        var score = 0
                        val name = voice.name.lowercase()

                        // Favor high quality neural models
                        if (voice.quality >= Voice.QUALITY_VERY_HIGH) score += 60
                        else if (voice.quality >= Voice.QUALITY_HIGH) score += 35

                        // Favor low latency
                        if (voice.latency <= Voice.LATENCY_VERY_LOW) score += 20
                        else if (voice.latency <= Voice.LATENCY_LOW) score += 10

                        // Match persona gender
                        if (targetGender == "female") {
                            if (name.contains("female") || name.contains("femme") || name.contains("-f-") || name.contains("wom")) score += 30
                            if (name.contains("male") && !name.contains("female")) score -= 25
                        } else {
                            if (name.contains("male") && !name.contains("female")) score += 30
                            if (name.contains("female") || name.contains("femme")) score -= 25
                        }

                        // Prefer neural/network voices if present
                        if (name.contains("neural") || name.contains("network") || name.contains("wavenet")) score += 25

                        score
                    }

                    if (bestVoice != null) {
                        tts.voice = bestVoice
                    }
                }
            }
        } catch (e: Exception) {
            Log.w("VoiceSpeechHelper", "Voice selection fallback to locale: ${e.message}")
        }
        tts.language = targetLocale
    }

    /**
     * Speaks text aloud in the requested language (French, Arabic, or English)
     * using humanized conversational prosody and neural voice configuration.
     */
    fun speak(
        text: String,
        language: AppLanguage,
        utteranceId: String = "ai_msg_${System.currentTimeMillis()}",
        persona: VoicePersona? = null
    ) {
        if (text.isBlank()) return

        if (textToSpeech == null || !isInitialized) {
            initTts()
        }

        val active = persona ?: _activePersona.value
        applyPersonaConfig(active)

        val targetLocale = when (language) {
            AppLanguage.FRENCH -> Locale.FRENCH
            AppLanguage.ENGLISH -> Locale.US
            AppLanguage.ARABIC -> Locale("ar")
        }

        selectBestVoiceForLanguage(targetLocale, active)

        val naturalText = humanizeTextForSpeech(text, language)

        try {
            val params = Bundle().apply {
                putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, utteranceId)
            }

            textToSpeech?.speak(
                naturalText,
                TextToSpeech.QUEUE_FLUSH,
                params,
                utteranceId
            )
        } catch (e: Exception) {
            Log.e("VoiceSpeechHelper", "Error in speak()", e)
            _isSpeaking.value = false
            stopWaveformSimulation()
        }
    }

    /**
     * Stops any ongoing speech immediately (Barge-in capability).
     */
    fun stop() {
        try {
            textToSpeech?.stop()
        } catch (e: Exception) {
            Log.e("VoiceSpeechHelper", "Error stopping TTS", e)
        } finally {
            _isSpeaking.value = false
            _currentUtteranceId.value = null
            stopWaveformSimulation()
        }
    }

    /**
     * Plays a pleasant micro-acoustic chime when speech recognition begins.
     */
    fun playListeningChime() {
        scope.launch(Dispatchers.Default) {
            playDualSineTone(freq1 = 587, freq2 = 880, durationMs = 85)
        }
    }

    /**
     * Plays a soft affirmation acoustic chime when AI recommendation is generated.
     */
    fun playAffirmationChime() {
        scope.launch(Dispatchers.Default) {
            playDualSineTone(freq1 = 659, freq2 = 987, durationMs = 110)
        }
    }

    private fun playDualSineTone(freq1: Int, freq2: Int, durationMs: Int) {
        try {
            val sampleRate = 22050
            val numSamples = (sampleRate * (durationMs / 1000.0)).toInt()
            val audioData = ShortArray(numSamples)

            for (i in 0 until numSamples) {
                val t = i.toDouble() / sampleRate
                // Window envelope to prevent clicks
                val envelope = sin(Math.PI * i / numSamples)
                val sample1 = sin(2 * Math.PI * freq1 * t)
                val sample2 = sin(2 * Math.PI * freq2 * t)
                val combined = ((sample1 + sample2) * 0.5 * envelope * 0.35 * Short.MAX_VALUE).toInt()
                audioData[i] = combined.toShort()
            }

            val audioTrack = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(audioData.size * 2)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()

            audioTrack.write(audioData, 0, audioData.size)
            audioTrack.play()
            scope.launch {
                delay(durationMs.toLong() + 50)
                try {
                    audioTrack.stop()
                    audioTrack.release()
                } catch (_: Exception) {}
            }
        } catch (e: Exception) {
            Log.w("VoiceSpeechHelper", "Soft earcon fallback: ${e.message}")
        }
    }

    /**
     * Normalizes text for human conversational delivery.
     * Expands technical tokens, adds natural breath micro-pauses, and removes robotic markdown.
     */
    fun humanizeTextForSpeech(raw: String, language: AppLanguage): String {
        var clean = raw
            .replace("*", "")
            .replace("#", "")
            .replace("`", "")
            .replace("~", "")
            .replace(">", "")
            .replace("_", " ")

        when (language) {
            AppLanguage.FRENCH -> {
                clean = clean
                    .replace(Regex("(?i)\\bDH\\b"), "dirhams")
                    .replace(Regex("(?i)\\bDhs\\b"), "dirhams")
                    .replace("%", " pour cent")
                    .replace(Regex("(?i)15-25\\s*min"), "quinze à vingt-cinq minutes")
                    .replace(Regex("(?i)\\bmin\\b"), "minutes")
                    .replace(Regex("(?i)\\bsec\\b"), "secondes")
                    .replace(Regex("(?i)\\bkm\\b"), "kilomètres")
                    .replace(Regex("(?i)\\bkg\\b"), "kilogrammes")
                    .replace(" - ", ", ")
                    .replace(" • ", ", ")
                    .replace("  ", " ")
            }
            AppLanguage.ARABIC -> {
                clean = clean
                    .replace(Regex("(?i)\\bDH\\b"), "درهم")
                    .replace(Regex("(?i)\\bDhs\\b"), "درهم")
                    .replace("%", " بالمئة ")
                    .replace("15-25", "خمسة عشر إلى خمس وعشرين")
                    .replace(" - ", "، ")
                    .replace(" • ", "، ")
                    .replace("  ", " ")
            }
            AppLanguage.ENGLISH -> {
                clean = clean
                    .replace(Regex("(?i)\\bDH\\b"), "dirhams")
                    .replace(Regex("(?i)\\bDhs\\b"), "dirhams")
                    .replace("%", " percent")
                    .replace(Regex("(?i)15-25\\s*min"), "15 to 25 minutes")
                    .replace(Regex("(?i)\\bmin\\b"), "minutes")
                    .replace(" - ", ", ")
                    .replace(" • ", ", ")
                    .replace("  ", " ")
            }
        }

        return clean.trim()
    }

    fun shutdown() {
        try {
            waveformJob?.cancel()
            textToSpeech?.stop()
            textToSpeech?.shutdown()
        } catch (e: Exception) {
            Log.e("VoiceSpeechHelper", "Error shutting down TTS", e)
        }
    }
}
