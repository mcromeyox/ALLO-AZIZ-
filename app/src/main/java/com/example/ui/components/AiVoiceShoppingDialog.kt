package com.example.ui.components

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.speech.RecognizerIntent
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import com.example.data.model.AppLanguage
import com.example.data.services.AiRecommendedItem
import com.example.data.services.AiShoppingRecommendation
import com.example.data.system.HapticFeedbackHelper
import com.example.data.system.VoicePersona
import com.example.data.system.VoiceSpeechHelper
import com.example.ui.theme.AzizMint
import com.example.ui.theme.AzizOrangePrimary
import kotlinx.coroutines.delay
import java.util.Locale

/**
 * Ultra-Compact, Non-Intrusive Google Gemini AI Voice Shopping Assistant.
 * Designed as a sleek floating capsule dock that never obscures products or freezes the UI.
 * Expands only on demand or when a recommendation is ready, with easy 1-tap minimize & dismiss.
 */
@Composable
fun AiVoiceShoppingDialog(
    language: AppLanguage,
    recommendation: AiShoppingRecommendation?,
    isListening: Boolean,
    isProcessing: Boolean,
    spokenQuery: String,
    onDismissRequest: () -> Unit,
    onSubmitQuery: (String) -> Unit,
    onConfirmRecommendation: (AiShoppingRecommendation) -> Unit,
    onUpdateQuantity: (productId: String, delta: Int) -> Unit,
    onRemoveItem: (productId: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var textInput by remember { mutableStateOf("") }
    var isMuted by remember { mutableStateOf(false) }

    // Start minimized as a sleek floating pill dock if there is no finished recommendation yet
    var isMinimized by remember { mutableStateOf(recommendation == null) }
    var showTextInput by remember { mutableStateOf(false) }

    // When a recommendation arrives, expand smoothly to show the product card
    LaunchedEffect(recommendation) {
        if (recommendation != null && recommendation.items.isNotEmpty()) {
            isMinimized = false
        }
    }

    // TTS Voice Speech Helper with Google Gemini Audio Engine
    val voiceHelper = remember { VoiceSpeechHelper(context) }
    val isSpeaking by voiceHelper.isSpeaking.collectAsState()
    val activePersona by voiceHelper.activePersona.collectAsState()
    val speechAmplitudes by voiceHelper.speechAmplitudes.collectAsState()

    DisposableEffect(Unit) {
        onDispose {
            voiceHelper.stop()
            voiceHelper.shutdown()
        }
    }

    // Auto-speak recommendation out loud with natural Google Audio voice
    LaunchedEffect(recommendation) {
        if (recommendation != null && !isMuted) {
            voiceHelper.playAffirmationChime()
            delay(200)
            val audioText = if (language == AppLanguage.FRENCH) {
                recommendation.speechAudioTextFr.ifBlank { recommendation.aiExplanationFr }
            } else if (language == AppLanguage.ARABIC) {
                recommendation.speechAudioTextAr.ifBlank { recommendation.aiExplanationAr }
            } else {
                recommendation.speechAudioTextFr.ifBlank { recommendation.aiExplanationFr }
            }
            if (audioText.isNotBlank()) {
                voiceHelper.speak(audioText, language, persona = activePersona)
            }
        }
    }

    // Google Android Speech Recognition Launcher
    val speechRecognizerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK && result.data != null) {
            val spokenMatches = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            val recognizedText = spokenMatches?.firstOrNull()?.trim()
            if (!recognizedText.isNullOrBlank()) {
                onSubmitQuery(recognizedText)
            }
        }
    }

    // Audio Permission Launcher
    val recordAudioPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            voiceHelper.playListeningChime()
            launchSystemSpeechRecognizer(context, speechRecognizerLauncher, language)
        } else {
            val msg = when (language) {
                AppLanguage.FRENCH -> "Veuillez autoriser le microphone pour dicter vos commandes 🎙️"
                AppLanguage.ENGLISH -> "Please enable microphone permission to speak with AI 🎙️"
                AppLanguage.ARABIC -> "يرجى تفعيل إذن الميكروفون للطلب بالصوت عبر الذكاء الاصطناعي 🎙️"
            }
            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
        }
    }

    fun startListeningFlow() {
        voiceHelper.stop()
        HapticFeedbackHelper.vibrateClick(context)
        voiceHelper.playListeningChime()

        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED

        if (hasPermission) {
            launchSystemSpeechRecognizer(
                context = context,
                launcher = speechRecognizerLauncher,
                language = language,
                onFallback = {
                    showTextInput = true
                }
            )
        } else {
            recordAudioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    // Holographic Pulsing Orb animation
    val infiniteTransition = rememberInfiniteTransition(label = "pulse_anim")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isListening || isSpeaking) 1.20f else 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    // Back handler: minimize first if expanded, or dismiss if already minimized
    BackHandler {
        if (!isMinimized && recommendation == null) {
            isMinimized = true
        } else {
            onDismissRequest()
        }
    }

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                // Transparent when minimized so underlying products stay 100% visible and bright!
                .background(if (isMinimized) Color.Transparent else Color.Black.copy(alpha = 0.22f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    if (!isMinimized) {
                        isMinimized = true
                    } else {
                        onDismissRequest()
                    }
                },
            contentAlignment = Alignment.BottomCenter
        ) {
            Surface(
                modifier = modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = if (isMinimized) 14.dp else 10.dp,
                        vertical = if (isMinimized) 12.dp else 8.dp
                    )
                    .navigationBarsPadding()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { /* Catch clicks inside capsule */ }
                    .shadow(
                        elevation = if (isMinimized) 12.dp else 16.dp,
                        shape = RoundedCornerShape(if (isMinimized) 30.dp else 24.dp)
                    )
                    .testTag("ai_voice_shopping_sheet"),
                shape = RoundedCornerShape(if (isMinimized) 30.dp else 24.dp),
                color = Color(0xF20F172A),
                border = androidx.compose.foundation.BorderStroke(
                    width = 1.2.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF4285F4), // Google Blue
                            Color(0xFF9B51E0), // Gemini Violet
                            Color(0xFF00C49F)  // Mint Cyan
                        )
                    )
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .animateContentSize()
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    if (isMinimized) {
                        // ==========================================
                        // 1. SLEEK FLOATING GEMINI CAPSULE DOCK (62dp)
                        // Never obscures products, ultra-compact & fast
                        // ==========================================
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Left: Animated Google Gemini Sparkle Orb
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { isMinimized = false }
                            ) {
                                Surface(
                                    onClick = { startListeningFlow() },
                                    shape = CircleShape,
                                    color = if (isListening) Color(0xFFDC2626) else AzizOrangePrimary,
                                    shadowElevation = 4.dp,
                                    modifier = Modifier
                                        .size(38.dp)
                                        .scale(pulseScale)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = if (isSpeaking) Icons.Default.RecordVoiceOver else Icons.Default.Mic,
                                            contentDescription = "Speak",
                                            tint = Color.White,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(10.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "✨ Gemini 3.8",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Black,
                                            color = AzizMint
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        LiveAudioSpectrumWave(
                                            amplitudes = speechAmplitudes,
                                            color = when {
                                                isListening -> Color(0xFFF87171)
                                                isSpeaking -> AzizMint
                                                isProcessing -> Color(0xFFA5B4FC)
                                                else -> Color.White.copy(alpha = 0.35f)
                                            }
                                        )
                                    }
                                    Text(
                                        text = when {
                                            isListening -> if (language == AppLanguage.ARABIC) "أستمع إليك الآن... 🎙️" else "Je vous écoute... 🎙️"
                                            isProcessing -> if (language == AppLanguage.ARABIC) "جاري التحليل بـ Gemini... 🧠" else "Analyse en cours... 🧠"
                                            spokenQuery.isNotBlank() -> "🗣️ \"$spokenQuery\""
                                            recommendation != null -> if (language == AppLanguage.ARABIC) "✅ الطلب جاهز • انقر للعرض" else "✅ Commande prête • Voir"
                                            else -> if (language == AppLanguage.ARABIC) "المس الميكروفون للتحدث والطلب" else "Touchez le micro pour commander"
                                        },
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Color.White.copy(alpha = 0.9f),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }

                            // Right: Persona, Expand and Close Controls
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                // Voice persona button
                                IconButton(
                                    onClick = {
                                        val next = if (activePersona == VoicePersona.SARA) VoicePersona.KARIM else VoicePersona.SARA
                                        voiceHelper.setPersona(next)
                                        HapticFeedbackHelper.vibrateClick(context)
                                    },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Text(text = activePersona.avatarEmoji, fontSize = 14.sp)
                                }

                                // Expand button
                                IconButton(
                                    onClick = { isMinimized = false },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.KeyboardArrowUp,
                                        contentDescription = "Expand",
                                        tint = AzizMint,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                // Close button
                                IconButton(
                                    onClick = onDismissRequest,
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Close",
                                        tint = Color.White.copy(alpha = 0.7f),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    } else {
                        // ==========================================
                        // 2. EXPANDED COMPACT ASSISTANT (Max 300dp)
                        // Shows Recommendation or Quick Chips without hiding products
                        // ==========================================
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 310.dp)
                        ) {
                            // Top Bar inside expanded view
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        shape = CircleShape,
                                        color = Color(0xFF4F46E5).copy(alpha = 0.3f),
                                        modifier = Modifier.size(26.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = Icons.Default.AutoAwesome,
                                                contentDescription = null,
                                                tint = AzizMint,
                                                modifier = Modifier.size(15.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Google Gemini AI",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color.White
                                    )
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    // Mute toggle
                                    IconButton(
                                        onClick = {
                                            isMuted = !isMuted
                                            if (isMuted) voiceHelper.stop()
                                            HapticFeedbackHelper.vibrateClick(context)
                                        },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (isMuted) Icons.Default.VolumeMute else Icons.Default.VolumeUp,
                                            contentDescription = "Mute",
                                            tint = if (isMuted) Color(0xFFEF4444) else AzizMint,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }

                                    // Minimize button
                                    IconButton(
                                        onClick = { isMinimized = true },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.KeyboardArrowDown,
                                            contentDescription = "Minimize",
                                            tint = AzizMint,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }

                                    // Close button
                                    IconButton(
                                        onClick = onDismissRequest,
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Close",
                                            tint = Color.White.copy(alpha = 0.8f),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }

                            // Central Mic / Status Strip
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = Color(0xFF131D33),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (isListening) Color(0xFFEF4444) else Color.White.copy(alpha = 0.12f)
                                ),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        onClick = { startListeningFlow() },
                                        shape = CircleShape,
                                        color = when {
                                            isListening -> Color(0xFFDC2626)
                                            isSpeaking -> Color(0xFF059669)
                                            isProcessing -> Color(0xFF4F46E5)
                                            else -> AzizOrangePrimary
                                        },
                                        modifier = Modifier
                                            .size(40.dp)
                                            .scale(pulseScale)
                                            .testTag("ai_mic_button")
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            if (isProcessing) {
                                                CircularProgressIndicator(
                                                    modifier = Modifier.size(18.dp),
                                                    color = Color.White,
                                                    strokeWidth = 2.dp
                                                )
                                            } else {
                                                Icon(
                                                    imageVector = if (isSpeaking) Icons.Default.RecordVoiceOver else Icons.Default.Mic,
                                                    contentDescription = "Speak",
                                                    tint = Color.White,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(8.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = when {
                                                isProcessing -> if (language == AppLanguage.ARABIC) "جاري استخراج الأصناف بذكاء Gemini... 🧠" else "Extraction des articles par Gemini... 🧠"
                                                isListening -> if (language == AppLanguage.ARABIC) "تفضل، أستمع لطلبك... 🎙️" else "Je vous écoute... 🎙️"
                                                isSpeaking -> if (language == AppLanguage.ARABIC) "إجابة صوتية Google الطبيعية 🔊" else "Réponse audio naturelle Google 🔊"
                                                else -> if (language == AppLanguage.ARABIC) "المس الميكروفون أو اكتب طلبك" else "Touchez le micro ou écrivez"
                                            },
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isListening) Color(0xFFF87171) else Color.White
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        LiveAudioSpectrumWave(
                                            amplitudes = speechAmplitudes,
                                            color = if (isListening) Color(0xFFF87171) else AzizMint
                                        )
                                    }

                                    // Keyboard Toggle
                                    IconButton(
                                        onClick = { showTextInput = !showTextInput },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Text(text = "⌨️", fontSize = 14.sp)
                                    }
                                }
                            }

                            // Optional Text input bar
                            if (showTextInput) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    OutlinedTextField(
                                        value = textInput,
                                        onValueChange = { textInput = it },
                                        modifier = Modifier.weight(1f),
                                        placeholder = {
                                            Text(
                                                text = if (language == AppLanguage.ARABIC) "اكتب طلبك..." else "Écrivez votre commande...",
                                                fontSize = 11.sp
                                            )
                                        },
                                        singleLine = true,
                                        shape = RoundedCornerShape(10.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = AzizOrangePrimary,
                                            unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                                            focusedTextColor = Color.White,
                                            unfocusedTextColor = Color.White
                                        )
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Button(
                                        onClick = {
                                            if (textInput.isNotBlank()) {
                                                onSubmitQuery(textInput)
                                                textInput = ""
                                                showTextInput = false
                                            }
                                        },
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = AzizOrangePrimary),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                    ) {
                                        Icon(Icons.Default.Send, contentDescription = "Send", tint = Color.White, modifier = Modifier.size(14.dp))
                                    }
                                }
                            }

                            // User Spoken Transcript
                            if (spokenQuery.isNotBlank()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color.White.copy(alpha = 0.08f),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = "🗣️ $spokenQuery",
                                        fontSize = 11.sp,
                                        color = Color.White.copy(alpha = 0.9f),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            // Recommendation Result OR Fast Voice Suggestions
                            if (recommendation != null && recommendation.items.isNotEmpty()) {
                                // Product Recommendation Card
                                Card(
                                    shape = RoundedCornerShape(14.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, AzizMint.copy(alpha = 0.4f)),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(1f, fill = false)
                                ) {
                                    Column(modifier = Modifier.padding(8.dp)) {
                                        // Store Name and Grand Total
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "${recommendation.storeBadgeEmoji} ${recommendation.targetStoreName}",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp,
                                                color = Color.White,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                                modifier = Modifier.weight(1f)
                                            )
                                            Text(
                                                text = String.format(Locale.US, "%.1f DH", recommendation.grandTotal),
                                                fontWeight = FontWeight.Black,
                                                fontSize = 12.sp,
                                                color = AzizMint
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(4.dp))

                                        // Items List
                                        LazyColumn(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .heightIn(max = 110.dp),
                                            verticalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            items(recommendation.items) { item ->
                                                LuxAiItemRow(
                                                    item = item,
                                                    language = language,
                                                    onIncrease = { onUpdateQuantity(item.product.id, 1) },
                                                    onDecrease = { onUpdateQuantity(item.product.id, -1) },
                                                    onDelete = { onRemoveItem(item.product.id) }
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(6.dp))

                                        // Confirm & Add to Cart Button
                                        Button(
                                            onClick = { onConfirmRecommendation(recommendation) },
                                            colors = ButtonDefaults.buttonColors(containerColor = AzizOrangePrimary),
                                            shape = RoundedCornerShape(10.dp),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .testTag("confirm_ai_recommendation_btn"),
                                            contentPadding = PaddingValues(vertical = 6.dp)
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    imageVector = Icons.Default.ShoppingCart,
                                                    contentDescription = null,
                                                    tint = Color.White,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = if (language == AppLanguage.ARABIC) {
                                                        "تأكيد الطلب وإضافته للسلة 🛒"
                                                    } else {
                                                        "Confirmer et ajouter au panier 🛒"
                                                    },
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 12.sp,
                                                    color = Color.White
                                                )
                                            }
                                        }
                                    }
                                }
                            } else if (recommendation != null && recommendation.isUnavailableNotice) {
                                Card(
                                    shape = RoundedCornerShape(14.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF59E0B).copy(alpha = 0.6f)),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(1f, fill = false)
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Text(
                                            text = "ℹ️ " + if (language == AppLanguage.ARABIC) recommendation.aiExplanationAr else recommendation.aiExplanationFr,
                                            fontSize = 12.sp,
                                            color = Color.White,
                                            lineHeight = 16.sp
                                        )
                                    }
                                }
                            } else {
                                // Fast Voice Suggestions Chips
                                LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    val chips = if (language == AppLanguage.ARABIC) {
                                        listOf(
                                            "🍔 2 بيج ماك وبطاطس" to "بغيت جوج وجبات بيج ماك ماكسي وبطاطس",
                                            "🍗 باكت كنتاكي 12 قطعة" to "باكت كنتاكي كريسبي مقرمش 12 قطعة مع بطاطس",
                                            "🍕 بيتزا سوبر سوبريم" to "بيتزا سوبر سوبريم كبيرة مع جبن موزاريلا",
                                            "🍲 طاجين لحم بالبرقوق" to "طاجين بلدي لحم بالبرقوق واللوز مع خبز سخون",
                                            "🥦 مقاضي من مرجان" to "حليب طازج، 30 بيضة، وزيت زيتون من مرجان"
                                        )
                                    } else {
                                        listOf(
                                            "🍔 2 Menus Big Mac" to "Deux menus Big Mac avec frites et Coca",
                                            "🍗 Bucket KFC 12 pcs" to "Bucket KFC 12 pièces croustillant avec frites",
                                            "🍕 Pizza Super Supreme" to "Grande pizza Super Supreme avec fromage fondu",
                                            "🍲 Tajine Agneau" to "Tajine royal d'agneau aux pruneaux et amandes",
                                            "🥦 Courses Marjane" to "Lait frais, 30 œufs fermiers et huile d'olive Marjane"
                                        )
                                    }

                                    items(chips) { (label, query) ->
                                        LuxQuickChip(label) { onSubmitQuery(query) }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Compact Product Item Row inside the AI recommendation list
 */
@Composable
private fun LuxAiItemRow(
    item: AiRecommendedItem,
    language: AppLanguage,
    onIncrease: () -> Unit,
    onDecrease: () -> Unit,
    onDelete: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = Color(0xFF0F172A).copy(alpha = 0.7f),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(AzizOrangePrimary.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                if (item.product.imageUrl.isNotBlank()) {
                    AsyncImage(
                        model = item.product.imageUrl,
                        contentDescription = item.product.nameFr,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Text(text = item.product.emoji, fontSize = 16.sp)
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Column(modifier = Modifier.weight(1f)) {
                val productName = when (language) {
                    AppLanguage.FRENCH -> item.product.nameFr
                    AppLanguage.ENGLISH -> item.product.nameEn
                    AppLanguage.ARABIC -> item.product.nameAr
                }
                Text(
                    text = productName,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = String.format(Locale.US, "%.1f DH", item.subtotal),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = AzizMint
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.White.copy(alpha = 0.1f))
                    .padding(horizontal = 2.dp)
            ) {
                IconButton(
                    onClick = { if (item.quantity > 1) onDecrease() else onDelete() },
                    modifier = Modifier.size(22.dp)
                ) {
                    Icon(
                        imageVector = if (item.quantity > 1) Icons.Default.Remove else Icons.Default.DeleteOutline,
                        contentDescription = "Decrease",
                        tint = if (item.quantity > 1) Color.White else Color(0xFFEF4444),
                        modifier = Modifier.size(12.dp)
                    )
                }

                Text(
                    text = "${item.quantity}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )

                IconButton(
                    onClick = onIncrease,
                    modifier = Modifier.size(22.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Increase",
                        tint = Color.White,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun LuxQuickChip(label: String, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(10.dp),
        color = Color(0xFF1E293B),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.15f))
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
            color = Color.White
        )
    }
}

@Composable
private fun LiveAudioSpectrumWave(amplitudes: List<Float>, color: Color) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(2.5.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.height(16.dp)
    ) {
        amplitudes.forEach { amp ->
            val barHeight = (amp * 14f).coerceIn(2.5f, 14f)
            Box(
                modifier = Modifier
                    .width(2.5.dp)
                    .height(barHeight.dp)
                    .clip(RoundedCornerShape(1.dp))
                    .background(color)
            )
        }
    }
}

private fun launchSystemSpeechRecognizer(
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
        AppLanguage.FRENCH -> "Dictez votre commande (ex: 2 Big Mac avec frites)..."
        AppLanguage.ENGLISH -> "Dictate your order (e.g., Big Mac, pizza, groceries)..."
        AppLanguage.ARABIC -> "تكلم واطلب مشترياتك (مثال: وجبة بيج ماك، طاجين، بيتزا...)"
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
