package com.example.data.services

import android.util.Base64
import android.util.Log
import com.example.BuildConfig
import com.example.data.system.VoicePersona
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

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

    private const val TAG = "ExternalAiVoiceService"

    private val client = OkHttpClient.Builder()
        .connectTimeout(6, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    var preferredEngine: AiVoiceEngine = AiVoiceEngine.GEMINI_2_5_FLASH

    suspend fun synthesizeSpeech(
        text: String,
        persona: VoicePersona = VoicePersona.SARA
    ): Pair<AudioSynthesizedResult, AiVoiceEngine>? = withContext(Dispatchers.IO) {
        val cleanText = text.trim()
        // 1. Google Gemini Audio Foundation Model (Studio Human Realism)
        val geminiKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (_: Throwable) { "" }

        if (geminiKey.isNotBlank() && !geminiKey.contains("DEFAULT_GEMINI") && !geminiKey.contains("PLACEHOLDER")) {
            try {
                val audioResult = callGeminiAudioTts(cleanText, geminiKey, persona)
                if (audioResult != null) {
                    Log.i(TAG, "Successfully synthesized studio speech with Google Gemini Audio")
                    return@withContext Pair(audioResult, AiVoiceEngine.GEMINI_2_5_FLASH)
                }
            } catch (e: Exception) {
                Log.w(TAG, "Gemini Audio TTS request failed: ${e.message}")
            }
        }

        // 2. Try ElevenLabs if configured and valid
        val elevenLabsKey = try {
            BuildConfig.ELEVENLABS_API_KEY
        } catch (_: Throwable) { "" }

        if (elevenLabsKey.isNotBlank() && !elevenLabsKey.contains("PLACEHOLDER") && !elevenLabsKey.contains("sk_a18753be")) {
            try {
                val mp3Bytes = callElevenLabs(cleanText, elevenLabsKey, persona)
                if (mp3Bytes != null && mp3Bytes.isNotEmpty()) {
                    return@withContext Pair(AudioSynthesizedResult.Mp3(mp3Bytes), AiVoiceEngine.ELEVENLABS)
                }
            } catch (e: Exception) {
                Log.w(TAG, "ElevenLabs TTS request failed: ${e.message}")
            }
        }

        // Fallback to Android Neural Device TTS
        return@withContext null
    }

    private fun callElevenLabs(
        text: String,
        apiKey: String,
        persona: VoicePersona
    ): ByteArray? {
        // Voice IDs: Sara -> "21m00Tcm4TlvDq8ikWAM" (Rachel/Warm natural voice)
        // Karim -> "pNInz6obpgDQGcFmaJgB" (Adam/Friendly clear voice)
        val voiceId = if (persona == VoicePersona.SARA) "21m00Tcm4TlvDq8ikWAM" else "pNInz6obpgDQGcFmaJgB"
        val endpoint = "https://api.elevenlabs.io/v1/text-to-speech/$voiceId"

        val bodyJson = JSONObject().apply {
            put("text", text)
            put("model_id", "eleven_multilingual_v2")
            put("voice_settings", JSONObject().apply {
                put("stability", 0.65)
                put("similarity_boost", 0.85)
                put("style", 0.35)
                put("use_speaker_boost", true)
            })
        }

        val request = Request.Builder()
            .url(endpoint)
            .addHeader("xi-api-key", apiKey)
            .addHeader("Accept", "audio/mpeg")
            .post(bodyJson.toString().toRequestBody("application/json".toMediaType()))
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                Log.w(TAG, "ElevenLabs error code: ${response.code}")
                return null
            }
            return response.body?.bytes()
        }
    }

    private fun callGeminiAudioTts(
        text: String,
        apiKey: String,
        persona: VoicePersona
    ): AudioSynthesizedResult? {
        val voiceName = if (persona == VoicePersona.SARA) "Aoede" else "Puck"
        // 1. Try gemini-3.8-flash-tts (Generates native studio audio/wav)
        val res38 = tryModelTts("gemini-3.8-flash-tts", text, apiKey, voiceName)
        if (res38 != null) return res38

        // 2. Fallback to gemini-2.5-flash-preview-tts (Generates audio/pcm)
        return tryModelTts("gemini-2.5-flash-preview-tts", text, apiKey, voiceName)
    }

    private fun tryModelTts(
        modelName: String,
        text: String,
        apiKey: String,
        voiceName: String
    ): AudioSynthesizedResult? {
        val endpoint = "https://generativelanguage.googleapis.com/v1beta/models/$modelName:generateContent?key=$apiKey"

        val jsonBody = JSONObject().apply {
            put("contents", JSONArray().apply {
                put(JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().put("text", text))
                    })
                })
            })
            put("generationConfig", JSONObject().apply {
                put("responseModalities", JSONArray().apply {
                    put("AUDIO")
                })
                put("speechConfig", JSONObject().apply {
                    put("voiceConfig", JSONObject().apply {
                        put("prebuiltVoiceConfig", JSONObject().apply {
                            put("voiceName", voiceName)
                        })
                    })
                })
            })
        }

        val request = Request.Builder()
            .url(endpoint)
            .post(jsonBody.toString().toRequestBody("application/json".toMediaType()))
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                Log.w(TAG, "$modelName returned HTTP ${response.code}")
                return null
            }
            val responseString = response.body?.string() ?: return null
            val rootJson = JSONObject(responseString)
            val candidates = rootJson.optJSONArray("candidates") ?: return null
            if (candidates.length() == 0) return null

            val firstCand = candidates.getJSONObject(0)
            val content = firstCand.optJSONObject("content") ?: return null
            val parts = content.optJSONArray("parts") ?: return null
            if (parts.length() == 0) return null

            val part = parts.getJSONObject(0)
            val inlineData = part.optJSONObject("inlineData") ?: return null
            val mimeType = inlineData.optString("mimeType", "")
            val base64Data = inlineData.optString("data", "")
            if (base64Data.isBlank()) return null

            val decodedBytes = Base64.decode(base64Data, Base64.DEFAULT)
            return if (mimeType.contains("wav") || (decodedBytes.size >= 4 && decodedBytes[0] == 'R'.code.toByte() && decodedBytes[1] == 'I'.code.toByte())) {
                AudioSynthesizedResult.Wav(decodedBytes)
            } else {
                AudioSynthesizedResult.Pcm24kHz(decodedBytes)
            }
        }
    }
}
