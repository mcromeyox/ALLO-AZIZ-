package com.example.data.services

import com.example.data.system.VoicePersona

/**
 * Cloud AI Voice Synthesis Service using Google's cutting-edge Gemini Audio Foundation Model
 * and External AI Voice engines.
 */
object GeminiCloudTtsService {

    suspend fun synthesizeSpeechPcm(
        text: String,
        persona: VoicePersona = VoicePersona.SARA
    ): ByteArray? {
        val result = ExternalAiVoiceService.synthesizeSpeech(text, persona)
        return when (val audio = result?.first) {
            is AudioSynthesizedResult.Pcm24kHz -> audio.bytes
            else -> null
        }
    }
}
