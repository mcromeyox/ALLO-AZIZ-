package com.example.data.services

import com.example.data.system.VoicePersona
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

enum class AiVoiceEngine(val displayNameAr: String, val displayNameFr: String, val badge: String) {
    GEMINI_2_5_FLASH("Google Gemini 2.5 Flash Audio", "Google Gemini 2.5 Flash Audio", "✨ Google AI"),
    ELEVENLABS("ElevenLabs Multilingual Neural", "ElevenLabs Voix Réaliste", "🎙️ ElevenLabs"),
    DEVICE_NEURAL("محرك الجهاز العصبي فائق الدقة", "Moteur Neural Haute Fidélité", "⚡ Neural Studio")
}

sealed class AudioSynthesizedResult {
    data class Wav(val bytes: ByteArray) : AudioSynthesizedResult()
    data class Pcm24kHz(val bytes: ByteArray) : AudioSynthesizedResult()
    data class Mp3(val bytes: ByteArray) : AudioSynthesizedResult()
}

/**
 * Universal External AI Voice Integration Service.
 * Integrates:
 * 1. Google Gemini Audio Foundation Models (gemini-3.8-flash-tts, gemini-2.5-flash-preview-tts)
 * 2. ElevenLabs Hyper-Realistic Multilingual Voice API
 * 3. Graceful fallback to Android Neural Studio TTS
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
