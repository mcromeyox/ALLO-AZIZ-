package com.example.ui.components

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.speech.RecognizerIntent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import com.example.data.model.AppLanguage
import com.example.data.services.AiSupportVoiceService
import com.example.data.system.HapticFeedbackHelper
import com.example.data.system.VoicePersona
import com.example.data.system.VoiceSpeechHelper
import com.example.ui.theme.AzizAmberSecondary
import com.example.ui.theme.AzizMint
import com.example.ui.theme.AzizOrangePrimary
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Interactive AI Voice Call Dialog for Customer Support.
 * Allows customers to have a real-time conversational voice discussion with the AI,
 * where they talk and the AI answers back out loud via native speech synthesis.
 */
@Composable
fun AiSupportVoiceCallDialog(
    language: AppLanguage,
    onDismissRequest: () -> Unit,
    onSupportMessageLogged: (userQuery: String, aiReply: String) -> Unit = { _, _ -> }
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Initialize TTS voice helper with realistic human prosody
    val voiceHelper = remember { VoiceSpeechHelper(context) }
    val isSpeaking by voiceHelper.isSpeaking.collectAsState()
    val activePersona by voiceHelper.activePersona.collectAsState()
    val speechAmplitudes by voiceHelper.speechAmplitudes.collectAsState()

    var isListening by remember { mutableStateOf(false) }
    var isProcessing by remember { mutableStateOf(false) }
    var isMuted by remember { mutableStateOf(false) }

    var lastUserQuestion by remember {
        mutableStateOf("")
    }

    var lastAiReply by remember {
        mutableStateOf(
            when (language) {
                AppLanguage.FRENCH -> "Bonjour ! Je suis Sara, votre assistante vocale ALLO AZIZ. Je vous écoute, comment puis-je vous aider aujourd'hui ?"
                AppLanguage.ENGLISH -> "Hello! I am Sara, your ALLO AZIZ AI assistant. How can I assist you today?"
                AppLanguage.ARABIC -> "أهلاً بك! أنا سارة من الدعم الفني لألو عزيز. تفضل بالسؤال وسأجيبك فوراً!"
            }
        )
    }

    // Call timer (seconds)
    var callSeconds by remember { mutableIntStateOf(0) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            callSeconds++
        }
    }

    // Auto-speak greeting on dialog open
    LaunchedEffect(Unit) {
        delay(600)
        if (!isMuted && lastAiReply.isNotBlank()) {
            voiceHelper.speak(lastAiReply, language, persona = activePersona)
        }
    }

    // Clean up TTS when dialog dismissed
    DisposableEffect(Unit) {
        onDispose {
            voiceHelper.stop()
            voiceHelper.shutdown()
        }
    }

    // Speech Recognizer launcher
    val speechLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        isListening = false
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            val spokenMatches = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            val query = spokenMatches?.firstOrNull()?.trim()
            if (!query.isNullOrBlank()) {
                lastUserQuestion = query
                isProcessing = true
                voiceHelper.playAffirmationChime()

                scope.launch {
                    val reply = AiSupportVoiceService.generateSupportResponse(query, language)
                    isProcessing = false
                    lastAiReply = reply
                    onSupportMessageLogged(query, reply)

                    if (!isMuted) {
                        voiceHelper.speak(reply, language, persona = activePersona)
                    }
                }
            }
        }
    }

    // Permission launcher for RECORD_AUDIO
    val audioPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            voiceHelper.playListeningChime()
            launchSystemSpeechRecognizerForSupport(context, speechLauncher, language)
            isListening = true
        } else {
            val msg = when (language) {
                AppLanguage.FRENCH -> "Veuillez activer le microphone pour parler avec Sara IA 🎙️"
                AppLanguage.ENGLISH -> "Please grant microphone permission to speak with AI 🎙️"
                AppLanguage.ARABIC -> "يرجى تفعيل إذن الميكروفون للتحدث الصوتي مع سارة 🎙️"
            }
            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
        }
    }

    fun startListening() {
        voiceHelper.stop()
        HapticFeedbackHelper.vibrateClick(context)
        voiceHelper.playListeningChime()

        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED

        if (hasPermission) {
            isListening = true
            launchSystemSpeechRecognizerForSupport(context, speechLauncher, language, onFallback = {
                isListening = false
                val defaultPrompt = when (language) {
                    AppLanguage.FRENCH -> "Où se trouve ma commande et mon coursier Aziz ?"
                    AppLanguage.ENGLISH -> "Where is my order and courier Aziz?"
                    AppLanguage.ARABIC -> "أين طلبي وموقع المندوب عزيز حالياً؟"
                }
                lastUserQuestion = defaultPrompt
                isProcessing = true
                voiceHelper.playAffirmationChime()
                scope.launch {
                    val reply = AiSupportVoiceService.generateSupportResponse(defaultPrompt, language)
                    isProcessing = false
                    lastAiReply = reply
                    onSupportMessageLogged(defaultPrompt, reply)
                    if (!isMuted) {
                        voiceHelper.speak(reply, language, persona = activePersona)
                    }
                }
            })
        } else {
            audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    // Pulsing animation for listening/speaking
    val infiniteTransition = rememberInfiniteTransition(label = "support_call_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isSpeaking || isListening) 1.25f else 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "call_pulse"
    )

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnClickOutside = true,
            dismissOnBackPress = true
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.35f))
                .clickable { onDismissRequest() },
            contentAlignment = Alignment.BottomCenter
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.62f)
                    .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                    .shadow(24.dp, RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                    .clickable(enabled = false) { /* Catch clicks inside */ }
                    .testTag("ai_support_voice_call_dialog"),
                color = Color(0xFF070B14)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color(0xFF070B14),
                                    Color(0xFF0F172A),
                                    Color(0xFF1E1B4B)
                                )
                            )
                        )
                ) {
                    // Drag handle pill
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp, bottom = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .width(38.dp)
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(Color.White.copy(alpha = 0.35f))
                        )
                    }

                    // Top Header: Agent Status + Call Duration + Close
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 18.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(AzizMint)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = when (language) {
                                    AppLanguage.FRENCH -> "${activePersona.displayNameFr.substringBefore(" ")} IA • Support Vocal 24/7"
                                    AppLanguage.ENGLISH -> "${activePersona.displayNameEn.substringBefore(" ")} AI • 24/7 Voice Support"
                                    AppLanguage.ARABIC -> "${activePersona.displayNameAr.substringBefore(" ")} AI • الدعم الصوتي المباشر"
                                },
                                color = Color.White,
                                fontWeight = FontWeight.Black,
                                fontSize = 15.sp
                            )
                            val minutes = callSeconds / 60
                            val seconds = callSeconds % 60
                            Text(
                                text = String.format("%02d:%02d • En direct (Voix Humaine 🎙️)", minutes, seconds),
                                color = AzizMint,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Persona Toggle (Sara / Karim)
                        Surface(
                            onClick = {
                                val next = if (activePersona == VoicePersona.SARA) VoicePersona.KARIM else VoicePersona.SARA
                                voiceHelper.setPersona(next)
                                HapticFeedbackHelper.vibrateClick(context)
                                val msg = "Voix : ${next.displayNameFr.substringBefore(" ")}"
                                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                voiceHelper.speak(msg, language, persona = next)
                            },
                            shape = RoundedCornerShape(12.dp),
                            color = Color.White.copy(alpha = 0.15f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, AzizMint.copy(alpha = 0.4f)),
                            modifier = Modifier.testTag("support_toggle_persona_btn")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = activePersona.avatarEmoji, fontSize = 14.sp)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (activePersona == VoicePersona.SARA) "Sara" else "Karim",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        IconButton(
                            onClick = onDismissRequest,
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.15f))
                                .testTag("close_support_voice_call_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                // Scrollable Conversation Area
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(10.dp))

                    // Central Visualizer & Avatar Circle
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.size(150.dp)
                    ) {
                        // Outer animated ripple rings
                        Box(
                            modifier = Modifier
                                .size(140.dp)
                                .scale(pulseScale)
                                .clip(CircleShape)
                                .background(
                                    when {
                                        isListening -> Color(0xFFEF4444).copy(alpha = 0.25f)
                                        isSpeaking -> AzizMint.copy(alpha = 0.25f)
                                        isProcessing -> Color(0xFF6366F1).copy(alpha = 0.25f)
                                        else -> AzizMint.copy(alpha = 0.12f)
                                    }
                                )
                        )

                        // Avatar Center
                        Surface(
                            onClick = { startListening() },
                            shape = CircleShape,
                            color = when {
                                isListening -> Color(0xFFDC2626)
                                isSpeaking -> Color(0xFF059669)
                                isProcessing -> Color(0xFF4F46E5)
                                else -> AzizOrangePrimary
                            },
                            shadowElevation = 14.dp,
                            modifier = Modifier
                                .size(96.dp)
                                .testTag("support_voice_main_mic_button")
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                if (isProcessing) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(40.dp),
                                        color = Color.White,
                                        strokeWidth = 3.dp
                                    )
                                } else {
                                    Icon(
                                        imageVector = if (isSpeaking) Icons.Default.RecordVoiceOver else Icons.Default.Mic,
                                        contentDescription = "Microphone",
                                        tint = Color.White,
                                        modifier = Modifier.size(44.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Dynamic Call Status Indicator
                    val agentName = if (activePersona == VoicePersona.SARA) "Sara" else "Karim"
                    Text(
                        text = when {
                            isListening -> when (language) {
                                AppLanguage.FRENCH -> "$agentName vous écoute... Parlez naturellement 🎙️"
                                AppLanguage.ENGLISH -> "$agentName is listening... Speak now 🎙️"
                                AppLanguage.ARABIC -> "$agentName يستمع لصوتك الآن... تفضل 🎙️"
                            }
                            isProcessing -> when (language) {
                                AppLanguage.FRENCH -> "$agentName prépare votre réponse... 🧠"
                                AppLanguage.ENGLISH -> "$agentName is thinking... 🧠"
                                AppLanguage.ARABIC -> "$agentName يجهز الإجابة المناسبة... 🧠"
                            }
                            isSpeaking -> when (language) {
                                AppLanguage.FRENCH -> "$agentName vous répond à voix haute... 🔊"
                                AppLanguage.ENGLISH -> "$agentName is speaking... 🔊"
                                AppLanguage.ARABIC -> "$agentName يجيبك صوتياً الآن... 🔊"
                            }
                            else -> when (language) {
                                AppLanguage.FRENCH -> "Touchez le micro pour parler à $agentName 🎙️"
                                AppLanguage.ENGLISH -> "Tap microphone to speak to $agentName 🎙️"
                                AppLanguage.ARABIC -> "اضغط على الميكروفون للتحدث مع $agentName 🎙️"
                            }
                        },
                        color = when {
                            isListening -> Color(0xFFF87171)
                            isSpeaking -> AzizMint
                            isProcessing -> Color(0xFFA5B4FC)
                            else -> AzizAmberSecondary
                        },
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black,
                        textAlign = TextAlign.Center
                    )

                    // Audio visualizer wave bars driven by real speech amplitudes
                    Spacer(modifier = Modifier.height(10.dp))
                    LiveSupportEqualizerWave(
                        amplitudes = speechAmplitudes,
                        color = when {
                            isListening -> Color(0xFFF87171)
                            isSpeaking -> AzizMint
                            isProcessing -> Color(0xFFA5B4FC)
                            else -> Color.White.copy(alpha = 0.25f)
                        }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // User Question Card (if asked)
                    AnimatedVisibility(visible = lastUserQuestion.isNotBlank()) {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = Color.White.copy(alpha = 0.08f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.15f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.Top) {
                                Text(text = "👤", fontSize = 16.sp)
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = when (language) {
                                            AppLanguage.FRENCH -> "Vous avez dit :"
                                            AppLanguage.ENGLISH -> "You asked:"
                                            AppLanguage.ARABIC -> "سؤالك الصوتي:"
                                        },
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = AzizAmberSecondary
                                    )
                                    Text(
                                        text = "« $lastUserQuestion »",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }

                    // AI Speech Response Speech Bubble
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Color(0xFF1E293B).copy(alpha = 0.95f),
                        border = androidx.compose.foundation.BorderStroke(
                            1.5.dp,
                            if (isSpeaking) AzizMint else Color(0xFF475569)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(text = activePersona.avatarEmoji, fontSize = 20.sp)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "$agentName IA (Voix Naturelle)",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = AzizMint
                                    )
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    // Re-listen / Replay audio button
                                    IconButton(
                                        onClick = {
                                            if (lastAiReply.isNotBlank()) {
                                                voiceHelper.speak(lastAiReply, language, persona = activePersona)
                                            }
                                        },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.VolumeUp,
                                            contentDescription = "Replay audio",
                                            tint = AzizMint,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = lastAiReply,
                                fontSize = 13.sp,
                                lineHeight = 20.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFFF1F5F9)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Quick Suggestion Chips (French by default)
                    Text(
                        text = when (language) {
                            AppLanguage.FRENCH -> "Questions fréquentes à poser :"
                            AppLanguage.ENGLISH -> "Quick questions you can ask:"
                            AppLanguage.ARABIC -> "أسئلة شائعة يمكنك طرحها:"
                        },
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White.copy(alpha = 0.6f),
                        modifier = Modifier.align(Alignment.Start)
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val suggestions = when (language) {
                            AppLanguage.FRENCH -> listOf(
                                "Où est ma commande ? 🛵" to "Où se trouve ma commande et mon coursier Aziz ?",
                                "Code promo du jour 🎁" to "Avez-vous un code promo pour ma commande ?",
                                "Modes de paiement 💳" to "Quels sont les modes de paiement disponibles ?",
                                "Temps de livraison ⚡" to "Combien de temps prend la livraison à Mohammedia ?",
                                "Article manquant ⚠️" to "Que faire si un article manque à ma commande ?"
                            )
                            AppLanguage.ENGLISH -> listOf(
                                "Where is my order? 🛵" to "Where is courier Aziz and my order?",
                                "Promo code 🎁" to "What is today's discount promo code?",
                                "Payment options 💳" to "What payment methods are supported?",
                                "Delivery time ⚡" to "How long does delivery take in Mohammedia?",
                                "Missing item ⚠️" to "What if an item is missing from my order?"
                            )
                            AppLanguage.ARABIC -> listOf(
                                "أين طلبي الآن؟ 🛵" to "أين طلبي وموقع المندوب عزيز حالياً؟",
                                "كود خصم حصري 🎁" to "ما هو كود الخصم المتاح اليوم؟",
                                "طرق الدفع 💳" to "ما هي طرق الدفع المتوفرة لديكم؟",
                                "وقت التوصيل ⚡" to "كم يستغرق التوصيل بالمحمدية عادةً؟",
                                "عنصر ناقص ⚠️" to "ماذا أفعل لو وصلني طلب ناقص؟"
                            )
                        }

                        suggestions.forEach { (label, prompt) ->
                            Surface(
                                onClick = {
                                    lastUserQuestion = prompt
                                    isProcessing = true
                                    voiceHelper.stop()
                                    voiceHelper.playAffirmationChime()

                                    scope.launch {
                                        val reply = AiSupportVoiceService.generateSupportResponse(prompt, language)
                                        isProcessing = false
                                        lastAiReply = reply
                                        onSupportMessageLogged(prompt, reply)

                                        if (!isMuted) {
                                            voiceHelper.speak(reply, language, persona = activePersona)
                                        }
                                    }
                                },
                                shape = RoundedCornerShape(12.dp),
                                color = Color.White.copy(alpha = 0.12f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.2f))
                            ) {
                                Text(
                                    text = label,
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Bottom Call Controls Bar
                Surface(
                    color = Color(0xFF0F172A),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 14.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Mute / Unmute Button
                        IconButton(
                            onClick = {
                                isMuted = !isMuted
                                if (isMuted) {
                                    voiceHelper.stop()
                                } else {
                                    voiceHelper.speak(lastAiReply, language, persona = activePersona)
                                }
                            },
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .background(if (isMuted) Color(0xFFEF4444).copy(alpha = 0.2f) else Color.White.copy(alpha = 0.1f))
                                .testTag("support_call_mute_toggle_btn")
                        ) {
                            Icon(
                                imageVector = if (isMuted) Icons.Default.VolumeMute else Icons.Default.VolumeUp,
                                contentDescription = "Mute",
                                tint = if (isMuted) Color(0xFFEF4444) else Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        // Big Push-To-Talk Button
                        Surface(
                            onClick = { startListening() },
                            shape = CircleShape,
                            color = AzizOrangePrimary,
                            shadowElevation = 8.dp,
                            modifier = Modifier
                                .size(68.dp)
                                .testTag("support_call_speak_again_btn")
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Mic,
                                    contentDescription = "Speak to AI",
                                    tint = Color.White,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }

                        // End Call Button
                        IconButton(
                            onClick = onDismissRequest,
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFDC2626))
                                .testTag("support_call_hangup_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.CallEnd,
                                contentDescription = "End call",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
}

@Composable
private fun LiveSupportEqualizerWave(amplitudes: List<Float>, color: Color) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.height(26.dp)
    ) {
        amplitudes.forEach { amp ->
            val h = (amp * 24f).coerceIn(4f, 24f)
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(h.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(color)
            )
        }
    }
}

private fun launchSystemSpeechRecognizerForSupport(
    context: Context,
    launcher: androidx.activity.result.ActivityResultLauncher<Intent>,
    language: AppLanguage,
    onFallback: () -> Unit = {}
) {
    val langTag = when (language) {
        AppLanguage.FRENCH -> "fr-FR"
        AppLanguage.ENGLISH -> "en-US"
        AppLanguage.ARABIC -> "ar-MA"
    }
    val promptMsg = when (language) {
        AppLanguage.FRENCH -> "Parlez à l'IA (ex: Où est ma commande, code promo, paiement...)"
        AppLanguage.ENGLISH -> "Speak to AI (e.g., Where is my order, promo code, payment...)"
        AppLanguage.ARABIC -> "تكلم مع المساعد الذكي (مثال: أين طلبي، كود الخصم، طرق الدفع...)"
    }
    val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
        putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        putExtra(RecognizerIntent.EXTRA_LANGUAGE, langTag)
        putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, langTag)
        putExtra(RecognizerIntent.EXTRA_ONLY_RETURN_LANGUAGE_PREFERENCE, langTag)
        putExtra(RecognizerIntent.EXTRA_PROMPT, promptMsg)
    }

    try {
        launcher.launch(intent)
    } catch (_: Throwable) {
        onFallback()
    }
}
