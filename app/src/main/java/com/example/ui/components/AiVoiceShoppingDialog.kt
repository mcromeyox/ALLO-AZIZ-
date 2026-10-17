package com.example.ui.components

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.speech.RecognizerIntent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
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
import com.example.data.system.VoiceSpeechHelper
import com.example.ui.theme.AzizAmberSecondary
import com.example.ui.theme.AzizCharcoalDark
import com.example.ui.theme.AzizMint
import com.example.ui.theme.AzizOrangePrimary
import kotlinx.coroutines.delay
import java.util.Locale

/**
 * Ultra-Professional, Elegant Holographic Voice Shopping Assistant Dialog.
 * Enables customers to speak their orders naturally, automatically curates products
 * from local Moroccan catalogs with high precision, speaks the recommendation back out loud,
 * and allows instant single-tap confirmation ("Confirmer").
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

    // TTS Voice Speech Helper to speak AI responses out loud
    val voiceHelper = remember { VoiceSpeechHelper(context) }
    val isSpeaking by voiceHelper.isSpeaking.collectAsState()

    DisposableEffect(Unit) {
        onDispose {
            voiceHelper.stop()
            voiceHelper.shutdown()
        }
    }

    // Auto-speak recommendation out loud when generated
    LaunchedEffect(recommendation) {
        if (recommendation != null && !isMuted) {
            val audioText = if (language == AppLanguage.FRENCH) {
                recommendation.speechAudioTextFr.ifBlank { recommendation.aiExplanationFr }
            } else if (language == AppLanguage.ARABIC) {
                recommendation.speechAudioTextAr.ifBlank { recommendation.aiExplanationAr }
            } else {
                recommendation.speechAudioTextFr.ifBlank { recommendation.aiExplanationFr }
            }
            if (audioText.isNotBlank()) {
                delay(350)
                voiceHelper.speak(audioText, language)
            }
        }
    }

    // Speech-to-Text Activity Result Launcher
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
            launchSystemSpeechRecognizer(context, speechRecognizerLauncher, language)
        } else {
            val msg = when (language) {
                AppLanguage.FRENCH -> "Veuillez activer le microphone pour dicter vos courses 🎙️"
                AppLanguage.ENGLISH -> "Please enable microphone permission to speak with AI 🎙️"
                AppLanguage.ARABIC -> "يرجى تفعيل الميكروفون للتحدث الصوتي مع الذكاء الاصطناعي 🎙️"
            }
            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
        }
    }

    fun startListeningFlow() {
        voiceHelper.stop()
        HapticFeedbackHelper.vibrateClick(context)

        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED

        if (hasPermission) {
            launchSystemSpeechRecognizer(context, speechRecognizerLauncher, language)
        } else {
            recordAudioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    // Animated Holographic Pulsing Orb
    val infiniteTransition = rememberInfiniteTransition(label = "hologram_voice_anim")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isListening || isSpeaking) 1.28f else 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 850, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    val orbRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 6000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "orb_rotation"
    )

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = modifier
                .fillMaxWidth(0.96f)
                .fillMaxHeight(0.94f)
                .clip(RoundedCornerShape(32.dp))
                .shadow(elevation = 28.dp, shape = RoundedCornerShape(32.dp))
                .testTag("ai_voice_shopping_dialog"),
            color = Color(0xFF090D16)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF090D16),
                                Color(0xFF0F172A),
                                Color(0xFF1E1B4B)
                            )
                        )
                    )
            ) {
                // 1. Futuristic Header Bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    Color(0xFF0F172A),
                                    Color(0xFF1E1B4B),
                                    Color(0xFF31104B)
                                )
                            )
                        )
                        .padding(horizontal = 18.dp, vertical = 14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            // Glowing AI Icon Badge
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.linearGradient(
                                            colors = listOf(AzizMint, Color(0xFF6366F1))
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = "AI Voice Engine",
                                    tint = Color.White,
                                    modifier = Modifier.size(22.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "ALLO AZIZ AI",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color.White,
                                        letterSpacing = 0.5.sp
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = AzizMint.copy(alpha = 0.25f)
                                    ) {
                                        Text(
                                            text = "4.0 PRO ⚡",
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = AzizMint
                                        )
                                    }
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(7.dp)
                                            .clip(CircleShape)
                                            .background(AzizMint)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = when (language) {
                                            AppLanguage.FRENCH -> "Reconnaissance vocale haute fidélité (99%)"
                                            AppLanguage.ENGLISH -> "High-Fidelity Voice AI (99% Accuracy)"
                                            AppLanguage.ARABIC -> "دقة مطابقة ذكية فائقة (99%)"
                                        },
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Color.White.copy(alpha = 0.8f)
                                    )
                                }
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Mute / Unmute Voice Toggle
                            IconButton(
                                onClick = {
                                    isMuted = !isMuted
                                    if (isMuted) voiceHelper.stop()
                                },
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.12f))
                                    .testTag("ai_voice_mute_toggle")
                            ) {
                                Icon(
                                    imageVector = if (isMuted) Icons.Default.VolumeMute else Icons.Default.VolumeUp,
                                    contentDescription = "Mute",
                                    tint = if (isMuted) Color(0xFFEF4444) else AzizMint,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            // Close Dialog Button
                            IconButton(
                                onClick = onDismissRequest,
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.12f))
                                    .testTag("close_ai_dialog_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Close",
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }

                // 2. Scrollable Body Content
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    item {
                        Spacer(modifier = Modifier.height(2.dp))
                    }

                    // Central Holographic Voice Orb Hero Card
                    item {
                        Surface(
                            shape = RoundedCornerShape(26.dp),
                            color = Color(0xFF13182E).copy(alpha = 0.95f),
                            border = androidx.compose.foundation.BorderStroke(
                                1.5.dp,
                                when {
                                    isListening -> Color(0xFFEF4444)
                                    isSpeaking -> AzizMint
                                    isProcessing -> Color(0xFF818CF8)
                                    else -> AzizOrangePrimary.copy(alpha = 0.5f)
                                }
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 18.dp, horizontal = 16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                // Holographic Interactive Mic Orb
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier.size(126.dp)
                                ) {
                                    // Outer animated ripple ring
                                    Box(
                                        modifier = Modifier
                                            .size(120.dp)
                                            .scale(pulseScale)
                                            .clip(CircleShape)
                                            .background(
                                                when {
                                                    isListening -> Color(0xFFEF4444).copy(alpha = 0.25f)
                                                    isSpeaking -> AzizMint.copy(alpha = 0.25f)
                                                    isProcessing -> Color(0xFF818CF8).copy(alpha = 0.25f)
                                                    else -> AzizOrangePrimary.copy(alpha = 0.15f)
                                                }
                                            )
                                    )

                                    // Secondary translucent glow ring
                                    Box(
                                        modifier = Modifier
                                            .size(96.dp)
                                            .clip(CircleShape)
                                            .background(
                                                when {
                                                    isListening -> Color(0xFFDC2626).copy(alpha = 0.35f)
                                                    isSpeaking -> AzizMint.copy(alpha = 0.35f)
                                                    isProcessing -> Color(0xFF6366F1).copy(alpha = 0.35f)
                                                    else -> AzizAmberSecondary.copy(alpha = 0.2f)
                                                }
                                            )
                                    )

                                    // Central Interactive Button
                                    Surface(
                                        onClick = { startListeningFlow() },
                                        shape = CircleShape,
                                        color = when {
                                            isListening -> Color(0xFFDC2626)
                                            isSpeaking -> Color(0xFF059669)
                                            isProcessing -> Color(0xFF4F46E5)
                                            else -> AzizOrangePrimary
                                        },
                                        shadowElevation = 12.dp,
                                        modifier = Modifier
                                            .size(80.dp)
                                            .testTag("ai_mic_button")
                                    ) {
                                        Box(
                                            contentAlignment = Alignment.Center,
                                            modifier = Modifier.fillMaxSize()
                                        ) {
                                            if (isProcessing) {
                                                CircularProgressIndicator(
                                                    modifier = Modifier.size(36.dp),
                                                    color = Color.White,
                                                    strokeWidth = 3.dp
                                                )
                                            } else {
                                                Icon(
                                                    imageVector = if (isSpeaking) Icons.Default.RecordVoiceOver else Icons.Default.Mic,
                                                    contentDescription = "Tap to speak",
                                                    tint = Color.White,
                                                    modifier = Modifier.size(40.dp)
                                                )
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                // Dynamic Status Text with live state
                                Text(
                                    text = when {
                                        isProcessing -> when (language) {
                                            AppLanguage.FRENCH -> "L'IA analyse vos souhaits et prépare votre panier... 🧠⚡"
                                            AppLanguage.ENGLISH -> "AI is analyzing your voice request... 🧠⚡"
                                            AppLanguage.ARABIC -> "الذكاء الاصطناعي يحلل طلبك الصوتي بدقة... 🧠⚡"
                                        }
                                        isListening -> when (language) {
                                            AppLanguage.FRENCH -> "Je vous écoute... Parlez en toute liberté 🎙️"
                                            AppLanguage.ENGLISH -> "Listening now... Speak freely 🎙️"
                                            AppLanguage.ARABIC -> "أستمع إليك الآن... تكلم براحتك 🎙️"
                                        }
                                        isSpeaking -> when (language) {
                                            AppLanguage.FRENCH -> "L'IA vous présente votre sélection vocale... 🔊"
                                            AppLanguage.ENGLISH -> "AI is speaking your order summary... 🔊"
                                            AppLanguage.ARABIC -> "المساعد الذكي يقرأ لك السلة بصوته... 🔊"
                                        }
                                        else -> when (language) {
                                            AppLanguage.FRENCH -> "Touchez le micro et commandez à la voix 🎙️"
                                            AppLanguage.ENGLISH -> "Tap microphone and order by voice 🎙️"
                                            AppLanguage.ARABIC -> "اضغط على الميكروفون واطلب مشترياتك بالصوت 🎙️"
                                        }
                                    },
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Black,
                                    color = when {
                                        isListening -> Color(0xFFF87171)
                                        isSpeaking -> AzizMint
                                        isProcessing -> Color(0xFFA5B4FC)
                                        else -> AzizAmberSecondary
                                    },
                                    textAlign = TextAlign.Center
                                )

                                // Live Animated Sound Equalizer Waves
                                if (isListening) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    VoiceSoundEqualizerWave(color = Color(0xFFF87171))
                                } else if (isSpeaking) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    VoiceSoundEqualizerWave(color = AzizMint)
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = when (language) {
                                        AppLanguage.FRENCH -> "Dictez librement : « 2 Big Mac avec frites », « Tajine d'agneau », « Courses Marjane »..."
                                        AppLanguage.ENGLISH -> "Say anything: \"2 Big Macs with fries\", \"Lamb tajine\", \"Marjane groceries\"..."
                                        AppLanguage.ARABIC -> "تكلم بكل حرية: «وجبة بيج ماك مع ماك فلوري»، «طاجين لحم بالبرقوق»، «سلة مرجان»..."
                                    },
                                    fontSize = 11.sp,
                                    color = Color.White.copy(alpha = 0.65f),
                                    textAlign = TextAlign.Center,
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    }

                    // 3. User Voice Transcript (Live or recorded)
                    if (spokenQuery.isNotBlank()) {
                        item {
                            Surface(
                                shape = RoundedCornerShape(18.dp),
                                color = Color.White.copy(alpha = 0.08f),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    Color.White.copy(alpha = 0.18f)
                                ),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(text = "🗣️", fontSize = 20.sp)
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = when (language) {
                                                AppLanguage.FRENCH -> "Demande vocale enregistrée :"
                                                AppLanguage.ENGLISH -> "Detected voice query:"
                                                AppLanguage.ARABIC -> "طلبك الصوتي المكتشف:"
                                            },
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = AzizAmberSecondary
                                        )
                                        Text(
                                            text = "« $spokenQuery »",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color.White
                                        )
                                    }
                                    IconButton(
                                        onClick = { startListeningFlow() },
                                        modifier = Modifier
                                            .size(34.dp)
                                            .clip(CircleShape)
                                            .background(AzizOrangePrimary.copy(alpha = 0.2f))
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Refresh,
                                            contentDescription = "Speak again",
                                            tint = AzizOrangePrimary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // 4. Quick Examples Carousel
                    item {
                        Column {
                            Text(
                                text = when (language) {
                                    AppLanguage.FRENCH -> "Suggestions vocales en 1 clic :"
                                    AppLanguage.ENGLISH -> "Quick 1-tap voice orders:"
                                    AppLanguage.ARABIC -> "أمثلة لطلبات شائعة بنقرة واحدة:"
                                },
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White.copy(alpha = 0.7f),
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                when (language) {
                                    AppLanguage.FRENCH -> {
                                        LuxQuickChip("🍔 Big Mac & McFlurry Oreo") { onSubmitQuery("Je veux un menu Maxi Big Mac avec un McFlurry Oreo") }
                                        LuxQuickChip("🍗 Bucket KFC 12 Pièces") { onSubmitQuery("Bucket KFC 12 pièces avec frites et sauce piquante") }
                                        LuxQuickChip("🍕 Pizza Super Supreme") { onSubmitQuery("Une grande Pizza Supreme avec croûte farcie au fromage") }
                                        LuxQuickChip("🍲 Tajine Agneau Pruneaux") { onSubmitQuery("Tajine d'agneau royal aux pruneaux pour 2 personnes") }
                                        LuxQuickChip("🥦 Panier Marjane Frais") { onSubmitQuery("Courses Marjane: lait frais, 30 œufs et huile d'olive") }
                                        LuxQuickChip("🥐 Café & Croissants Paul") { onSubmitQuery("Croissants pur beurre et tartelette aux fraises de chez Paul") }
                                        LuxQuickChip("🦐 Plateau Poissons Port") { onSubmitQuery("Plateau de fruits de mer et poissons grillés du port") }
                                        LuxQuickChip("💊 Vitamine C Pharmacie") { onSubmitQuery("Vitamine C effervescente et paracétamol de la pharmacie") }
                                    }
                                    AppLanguage.ENGLISH -> {
                                        LuxQuickChip("🍔 Big Mac & McFlurry") { onSubmitQuery("Big Mac combo with Oreo McFlurry") }
                                        LuxQuickChip("🍗 KFC 12pc Crispy Bucket") { onSubmitQuery("KFC 12 pieces crispy bucket with fries") }
                                        LuxQuickChip("🍕 Cheesy Crust Pizza") { onSubmitQuery("Super Supreme pizza with stuffed cheesy crust") }
                                        LuxQuickChip("🍲 Royal Lamb Tajine") { onSubmitQuery("Authentic lamb tajine with prunes and almonds") }
                                        LuxQuickChip("🥦 Marjane Grocery Pack") { onSubmitQuery("Fresh milk, eggs and extra virgin olive oil from Marjane") }
                                        LuxQuickChip("🥐 Paul Bakery Coffee") { onSubmitQuery("Butter croissants and strawberry tart from Paul") }
                                    }
                                    AppLanguage.ARABIC -> {
                                        LuxQuickChip("🍔 وجبة بيج ماك وماك فلوري") { onSubmitQuery("أريد وجبة بيج ماك ماكسي مع ماك فلوري أوريو") }
                                        LuxQuickChip("🍗 باكت كنتاكي 12 قطعة") { onSubmitQuery("باكت كنتاكي كريسبي مقرمش مع صوص حار") }
                                        LuxQuickChip("🍕 بيتزا تشيزي كراست") { onSubmitQuery("بيتزا سوبر سوبريم كبيرة مع أطراف جبن") }
                                        LuxQuickChip("🍲 طاجين لحم بالبرقوق") { onSubmitQuery("طاجين بلدي لحم بالبرقوق واللوز مع خبز ساخن") }
                                        LuxQuickChip("🥦 سلة مشتريات مرجان") { onSubmitQuery("حليب طازج، 30 بيضة، وزيت زيتون من مرجان") }
                                        LuxQuickChip("🥐 قهوة وكرواسون بول") { onSubmitQuery("كرواسون زبدة فرنسي وتارت فراولة من بول") }
                                    }
                                }
                            }
                        }
                    }

                    // 5. THE AI GENERATED BASKET CARD
                    if (recommendation != null && recommendation.items.isNotEmpty()) {
                        item {
                            Surface(
                                shape = RoundedCornerShape(24.dp),
                                color = Color(0xFF13182E),
                                border = androidx.compose.foundation.BorderStroke(1.5.dp, AzizMint),
                                shadowElevation = 10.dp,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("ai_recommendation_card")
                            ) {
                                Column(modifier = Modifier.padding(18.dp)) {
                                    // Header: Store Name, Store Badge, and ETA
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text(text = recommendation.storeBadgeEmoji, fontSize = 22.sp)
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Column {
                                                Text(
                                                    text = recommendation.targetStoreName,
                                                    fontSize = 15.sp,
                                                    fontWeight = FontWeight.Black,
                                                    color = Color.White,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(
                                                        imageVector = Icons.Default.Timer,
                                                        contentDescription = null,
                                                        tint = AzizOrangePrimary,
                                                        modifier = Modifier.size(13.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(3.dp))
                                                    Text(
                                                        text = "Livraison ~${recommendation.estimatedTimeMins} min",
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = AzizOrangePrimary
                                                    )
                                                }
                                            }
                                        }

                                        // Precision Badge
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = AzizMint.copy(alpha = 0.2f),
                                            border = androidx.compose.foundation.BorderStroke(1.dp, AzizMint.copy(alpha = 0.4f))
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = "⭐ ${recommendation.confidenceScore}% Précis",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Black,
                                                    color = AzizMint
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))

                                    // AI Speech Bubble with Replay Button
                                    Surface(
                                        shape = RoundedCornerShape(16.dp),
                                        color = Color(0xFF1E293B).copy(alpha = 0.9f),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155)),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(text = "🤖", fontSize = 20.sp)
                                            Spacer(modifier = Modifier.width(10.dp))
                                            val explanation = when (language) {
                                                AppLanguage.FRENCH -> recommendation.aiExplanationFr.ifBlank { recommendation.aiExplanationAr }
                                                AppLanguage.ENGLISH -> recommendation.aiExplanationFr.ifBlank { recommendation.aiExplanationAr }
                                                AppLanguage.ARABIC -> recommendation.aiExplanationAr.ifBlank { recommendation.aiExplanationFr }
                                            }
                                            Text(
                                                text = explanation,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = Color(0xFFF1F5F9),
                                                lineHeight = 18.sp,
                                                modifier = Modifier.weight(1f)
                                            )
                                            IconButton(
                                                onClick = {
                                                    val audioText = if (language == AppLanguage.FRENCH) {
                                                        recommendation.speechAudioTextFr.ifBlank { recommendation.aiExplanationFr }
                                                    } else {
                                                        recommendation.speechAudioTextAr.ifBlank { recommendation.aiExplanationAr }
                                                    }
                                                    voiceHelper.speak(audioText, language)
                                                },
                                                modifier = Modifier.size(32.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.VolumeUp,
                                                    contentDescription = "Replay Speech",
                                                    tint = AzizMint,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                        }
                                    }

                                    // Detected Notes / Tags
                                    if (recommendation.detectedNotes.isNotEmpty()) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .horizontalScroll(rememberScrollState()),
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            recommendation.detectedNotes.forEach { note ->
                                                Surface(
                                                    shape = RoundedCornerShape(8.dp),
                                                    color = AzizAmberSecondary.copy(alpha = 0.15f),
                                                    border = androidx.compose.foundation.BorderStroke(1.dp, AzizAmberSecondary.copy(alpha = 0.35f))
                                                ) {
                                                    Text(
                                                        text = note,
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = AzizAmberSecondary,
                                                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(14.dp))

                                    // List of Recommended Items
                                    Column(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        recommendation.items.forEach { item ->
                                            LuxAiItemRow(
                                                item = item,
                                                language = language,
                                                onIncrease = {
                                                    HapticFeedbackHelper.vibrateClick(context)
                                                    onUpdateQuantity(item.product.id, 1)
                                                },
                                                onDecrease = {
                                                    HapticFeedbackHelper.vibrateClick(context)
                                                    onUpdateQuantity(item.product.id, -1)
                                                },
                                                onDelete = {
                                                    HapticFeedbackHelper.vibrateClick(context)
                                                    onRemoveItem(item.product.id)
                                                }
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(14.dp))

                                    val currSuffix = when (language) {
                                        AppLanguage.FRENCH -> " DH"
                                        AppLanguage.ENGLISH -> " MAD"
                                        AppLanguage.ARABIC -> " د.م."
                                    }

                                    // Financial Breakdown Card
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(16.dp))
                                            .background(Color.White.copy(alpha = 0.05f))
                                            .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
                                            .padding(14.dp),
                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = when (language) {
                                                    AppLanguage.FRENCH -> "Sous-total articles :"
                                                    AppLanguage.ENGLISH -> "Items subtotal:"
                                                    AppLanguage.ARABIC -> "مجموع المشتريات:"
                                                },
                                                fontSize = 12.sp,
                                                color = Color.White.copy(alpha = 0.7f)
                                            )
                                            Text(
                                                text = String.format(Locale.US, "%.2f%s", recommendation.estimatedTotal, currSuffix),
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = Color.White
                                            )
                                        }

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = when (language) {
                                                    AppLanguage.FRENCH -> "Livraison express Aziz :"
                                                    AppLanguage.ENGLISH -> "Express delivery fee:"
                                                    AppLanguage.ARABIC -> "رسوم التوصيل السريع:"
                                                },
                                                fontSize = 12.sp,
                                                color = Color.White.copy(alpha = 0.7f)
                                            )
                                            Text(
                                                text = String.format(Locale.US, "%.2f%s", recommendation.deliveryFee, currSuffix),
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = AzizMint
                                            )
                                        }

                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(1.dp)
                                                .background(Color.White.copy(alpha = 0.15f))
                                        )

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = when (language) {
                                                    AppLanguage.FRENCH -> "Total à payer :"
                                                    AppLanguage.ENGLISH -> "Grand Total:"
                                                    AppLanguage.ARABIC -> "الإجمالي النهائي:"
                                                },
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Black,
                                                color = Color.White
                                            )
                                            Text(
                                                text = String.format(Locale.US, "%.2f%s", recommendation.grandTotal, currSuffix),
                                                fontSize = 18.sp,
                                                fontWeight = FontWeight.Black,
                                                color = AzizOrangePrimary
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(16.dp))

                                    // THE CRITICAL "CONFIRMER" BUTTON (User Request: "والضغط على التاكيد confèrem")
                                    Button(
                                        onClick = {
                                            HapticFeedbackHelper.vibrateSuccess(context)
                                            onConfirmRecommendation(recommendation)
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(56.dp)
                                            .shadow(12.dp, RoundedCornerShape(18.dp))
                                            .testTag("confirm_ai_shopping_list_button"),
                                        shape = RoundedCornerShape(18.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = AzizMint,
                                            contentColor = Color.White
                                        )
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.CheckCircle,
                                                contentDescription = "Confirm",
                                                tint = Color.White,
                                                modifier = Modifier.size(24.dp)
                                            )
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Text(
                                                text = when (language) {
                                                    AppLanguage.FRENCH -> "Confirmer la commande (Confirmer) ✓"
                                                    AppLanguage.ENGLISH -> "Confirm Basket & Checkout ✓"
                                                    AppLanguage.ARABIC -> "تأكيد السلة ومتابعة الطلب (Confirmer) ✓"
                                                },
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.Black,
                                                color = Color.White
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }

                // 6. Bottom Bar: Fallback Text Input
                Surface(
                    color = Color(0xFF0F172A),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = textInput,
                            onValueChange = { textInput = it },
                            placeholder = {
                                Text(
                                    text = when (language) {
                                        AppLanguage.FRENCH -> "Ou écrivez vos courses (ex: 2 Big Mac, pizza...)"
                                        AppLanguage.ENGLISH -> "Or type your order here..."
                                        AppLanguage.ARABIC -> "أو اكتب طلبك هنا يدوياً..."
                                    },
                                    fontSize = 12.sp,
                                    color = Color.White.copy(alpha = 0.45f)
                                )
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("ai_text_input_field"),
                            shape = RoundedCornerShape(24.dp),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                            keyboardActions = KeyboardActions(onSend = {
                                if (textInput.isNotBlank()) {
                                    onSubmitQuery(textInput)
                                    textInput = ""
                                }
                            }),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AzizOrangePrimary,
                                unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedContainerColor = Color.White.copy(alpha = 0.05f),
                                unfocusedContainerColor = Color.White.copy(alpha = 0.05f)
                            )
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        IconButton(
                            onClick = {
                                if (textInput.isNotBlank()) {
                                    onSubmitQuery(textInput)
                                    textInput = ""
                                } else {
                                    startListeningFlow()
                                }
                            },
                            modifier = Modifier
                                .size(46.dp)
                                .clip(CircleShape)
                                .background(if (textInput.isNotBlank()) AzizMint else AzizOrangePrimary)
                                .testTag("send_ai_voice_query_button")
                        ) {
                            Icon(
                                imageVector = if (textInput.isNotBlank()) Icons.AutoMirrored.Filled.Send else Icons.Default.Mic,
                                contentDescription = "Send",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Luxury Product Item Row inside the AI recommendation list
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
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFF1E293B).copy(alpha = 0.8f),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Product Thumbnail / Image
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(RoundedCornerShape(12.dp))
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
                    Text(text = item.product.emoji, fontSize = 26.sp)
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            // Details
            Column(modifier = Modifier.weight(1f)) {
                val productName = when (language) {
                    AppLanguage.FRENCH -> item.product.nameFr
                    AppLanguage.ENGLISH -> item.product.nameEn
                    AppLanguage.ARABIC -> item.product.nameAr
                }
                Text(
                    text = productName,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (item.customNote.isNotBlank()) {
                    Text(
                        text = item.customNote,
                        fontSize = 10.sp,
                        color = AzizAmberSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                val currSuffix = when (language) {
                    AppLanguage.FRENCH -> " DH"
                    AppLanguage.ENGLISH -> " MAD"
                    AppLanguage.ARABIC -> " د.م."
                }
                Text(
                    text = String.format(Locale.US, "%.2f%s", item.subtotal, currSuffix),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = AzizMint
                )
            }

            // Stepper Quantity Controls
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color.White.copy(alpha = 0.1f))
                    .padding(horizontal = 4.dp, vertical = 2.dp)
            ) {
                IconButton(
                    onClick = {
                        if (item.quantity > 1) onDecrease() else onDelete()
                    },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = if (item.quantity > 1) Icons.Default.Remove else Icons.Default.DeleteOutline,
                        contentDescription = "Decrease",
                        tint = if (item.quantity > 1) Color.White else Color(0xFFEF4444),
                        modifier = Modifier.size(16.dp)
                    )
                }

                Text(
                    text = "${item.quantity}",
                    fontWeight = FontWeight.Black,
                    fontSize = 13.sp,
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 6.dp)
                )

                IconButton(
                    onClick = onIncrease,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Increase",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
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
        shape = RoundedCornerShape(14.dp),
        color = Color(0xFF1E293B).copy(alpha = 0.9f),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.15f))
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = Color.White
        )
    }
}

@Composable
private fun VoiceSoundEqualizerWave(color: Color) {
    val infiniteTransition = rememberInfiniteTransition(label = "voice_equalizer")
    val b1 by infiniteTransition.animateFloat(
        initialValue = 6f, targetValue = 24f,
        animationSpec = infiniteRepeatable(tween(320, easing = LinearEasing), RepeatMode.Reverse),
        label = "b1"
    )
    val b2 by infiniteTransition.animateFloat(
        initialValue = 18f, targetValue = 8f,
        animationSpec = infiniteRepeatable(tween(400, easing = LinearEasing), RepeatMode.Reverse),
        label = "b2"
    )
    val b3 by infiniteTransition.animateFloat(
        initialValue = 10f, targetValue = 30f,
        animationSpec = infiniteRepeatable(tween(280, easing = LinearEasing), RepeatMode.Reverse),
        label = "b3"
    )
    val b4 by infiniteTransition.animateFloat(
        initialValue = 26f, targetValue = 10f,
        animationSpec = infiniteRepeatable(tween(450, easing = LinearEasing), RepeatMode.Reverse),
        label = "b4"
    )
    val b5 by infiniteTransition.animateFloat(
        initialValue = 8f, targetValue = 22f,
        animationSpec = infiniteRepeatable(tween(360, easing = LinearEasing), RepeatMode.Reverse),
        label = "b5"
    )

    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        listOf(b1, b2, b3, b4, b5).forEach { h ->
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

private fun launchSystemSpeechRecognizer(
    context: Context,
    launcher: androidx.activity.result.ActivityResultLauncher<Intent>,
    language: AppLanguage
) {
    val langTag = when (language) {
        AppLanguage.FRENCH -> "fr-FR"
        AppLanguage.ENGLISH -> "en-US"
        AppLanguage.ARABIC -> "ar-MA"
    }
    val promptMsg = when (language) {
        AppLanguage.FRENCH -> "Dictez votre commande (ex: 2 Big Mac avec frites et McFlurry...)"
        AppLanguage.ENGLISH -> "Dictate your order (e.g., Big Mac, pizza, tajine, groceries...)"
        AppLanguage.ARABIC -> "تكلم واطلب مشترياتك (مثال: وجبة بيج ماك وماك فلوري، بيتزا، طاجين...)"
    }
    val fallbackMsg = when (language) {
        AppLanguage.FRENCH -> "Reconnaissance vocale non disponible sur cet appareil. Tapez ci-dessous !"
        AppLanguage.ENGLISH -> "Voice recognition not available. You can type below!"
        AppLanguage.ARABIC -> "خدمة التعرف الصوتي غير متوفرة، يمكنك كتابة طلبك في خانة النص أدناه!"
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
    } catch (e: Exception) {
        Toast.makeText(context, fallbackMsg, Toast.LENGTH_LONG).show()
    }
}
