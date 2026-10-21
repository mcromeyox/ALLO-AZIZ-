package com.example.data.services

import com.example.data.system.VoicePersona
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

enum class AiVoiceEngine(val displayNameAr: String, val displayNameFr: String, val badge: String) {
    GEMINI_2_5_FLASH("Gemini (backend not configured)", "Gemini (serveur non configuré)", "⚠️ Not configured"),
    ELEVENLABS("ElevenLabs (backend not configured)", "ElevenLabs (serveur non configuré)", "⚠️ Not configured"),
    DEVICE_NEURAL("محرك الجهاز العصبي فائق الدقة", "Moteur Neural Haute Fidélité", "⚡ Neural Studio")
}

sealed class AudioSynthesizedResult {
    data class Wav(val bytes: ByteArray) : AudioSynthesizedResult()
    data class Pcm24kHz(val bytes: ByteArray) : AudioSynthesizedResult()
    data class Mp3(val bytes: ByteArray) : AudioSynthesizedResult()
}

/**
 * External voice service adapter.
 * Remote synthesis is disabled until a trusted backend proxy is configured.
 * Callers should fall back to Android's on-device TextToSpeech implementation.
 */
object ExternalAiVoiceService {
    // External AI providers are disabled until a trusted backend proxy is configured.
    // Do not embed Gemini or ElevenLabs API keys in the APK.
    var preferredEngine: AiVoiceEngine = AiVoiceEngine.DEVICE_NEURAL

    suspend fun synthesizeSpeech(
        text: String,
        persona: VoicePersona = VoicePersona.SARA
    ): Pair<AudioSynthesizedResult, AiVoiceEngine>? = withContext(Dispatchers.Default) {
        null
    }
}
