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
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ShoppingCart
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
import com.example.data.model.AppLanguage
import com.example.data.services.AiShoppingRecommendation
import com.example.ui.theme.AzizAmberSecondary
import com.example.ui.theme.AzizCharcoalDark
import com.example.ui.theme.AzizMint
import com.example.ui.theme.AzizOrangePrimary
import java.util.Locale

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

    // Speech-to-Text Activity Result Launcher
    val speechRecognizerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
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
                AppLanguage.FRENCH -> "Veuillez activer l'accès au microphone pour parler avec l'IA 🎙️"
                AppLanguage.ENGLISH -> "Please enable microphone permission to speak with AI 🎙️"
                AppLanguage.ARABIC -> "يرجى تفعيل إذن الميكروفون للتحدث الصوتي مع الذكاء الاصطناعي 🎙️"
            }
            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
        }
    }

    fun startListeningFlow() {
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

    // Mic Pulsing Animation
    val infiniteTransition = rememberInfiniteTransition(label = "ai_voice_animation")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isListening) 1.25f else 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    val gradientRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "gradient_rotation"
    )

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = modifier
                .fillMaxWidth(0.96f)
                .fillMaxHeight(0.92f)
                .clip(RoundedCornerShape(28.dp))
                .shadow(elevation = 24.dp, shape = RoundedCornerShape(28.dp))
                .testTag("ai_voice_shopping_dialog"),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surface)
            ) {
                // Header: AI Gradient Bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    AzizCharcoalDark,
                                    Color(0xFF2C1654),
                                    Color(0xFF1A365D)
                                )
                            )
                        )
                        .padding(horizontal = 20.dp, vertical = 16.dp)
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
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.linearGradient(
                                            colors = listOf(AzizOrangePrimary, AzizAmberSecondary)
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = "AI Assistant",
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "ALLO AZIZ AI",
                                        fontSize = 17.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color.White
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = AzizOrangePrimary.copy(alpha = 0.25f)
                                    ) {
                                        Text(
                                            text = when (language) {
                                                AppLanguage.FRENCH -> "Assistant Vocal 🧠"
                                                AppLanguage.ENGLISH -> "Smart Voice AI 🧠"
                                                AppLanguage.ARABIC -> "مساعد صوتي ذكي 🧠"
                                            },
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = AzizAmberSecondary
                                        )
                                    }
                                }
                                Text(
                                    text = when (language) {
                                        AppLanguage.FRENCH -> "Parlez et l'IA sélectionnera votre liste de courses instantanément !"
                                        AppLanguage.ENGLISH -> "Speak and AI will pick your shopping basket instantly!"
                                        AppLanguage.ARABIC -> "تكلم بصوتك وسيقوم الذكاء الاصطناعي باختيار قائمتك فوراً!"
                                    },
                                    fontSize = 11.sp,
                                    color = Color.White.copy(alpha = 0.85f),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        IconButton(
                            onClick = onDismissRequest,
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.15f))
                                .testTag("close_ai_dialog_button")
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

                // Main Content Body
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        Spacer(modifier = Modifier.height(4.dp))
                    }

                    // Voice Microphone Hero Card
                    item {
                        Surface(
                            shape = RoundedCornerShape(22.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            border = borderStrokeForAi(isListening, isProcessing),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 20.dp, horizontal = 16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                // Microphone with Sound Wave Pulsing Ring
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier.size(110.dp)
                                ) {
                                    // Outer animated ripple ring
                                    Box(
                                        modifier = Modifier
                                            .size(105.dp)
                                            .scale(pulseScale)
                                            .clip(CircleShape)
                                            .background(
                                                when {
                                                    isListening -> AzizOrangePrimary.copy(alpha = 0.25f)
                                                    isProcessing -> Color(0xFF6366F1).copy(alpha = 0.25f)
                                                    else -> AzizAmberSecondary.copy(alpha = 0.15f)
                                                }
                                            )
                                    )

                                    // Main Interactive Mic Button
                                    Surface(
                                        onClick = { startListeningFlow() },
                                        shape = CircleShape,
                                        color = when {
                                            isListening -> Color(0xFFDC2626)
                                            isProcessing -> Color(0xFF4F46E5)
                                            else -> AzizOrangePrimary
                                        },
                                        shadowElevation = 8.dp,
                                        modifier = Modifier
                                            .size(76.dp)
                                            .testTag("ai_mic_button")
                                    ) {
                                        Box(
                                            contentAlignment = Alignment.Center,
                                            modifier = Modifier.fillMaxSize()
                                        ) {
                                            if (isProcessing) {
                                                CircularProgressIndicator(
                                                    modifier = Modifier.size(32.dp),
                                                    color = Color.White,
                                                    strokeWidth = 3.dp
                                                )
                                            } else {
                                                Icon(
                                                    imageVector = Icons.Default.Mic,
                                                    contentDescription = "Tap to speak",
                                                    tint = Color.White,
                                                    modifier = Modifier.size(38.dp)
                                                )
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                // Status Label & Equalizer
                                Text(
                                    text = when {
                                        isProcessing -> when (language) {
                                            AppLanguage.FRENCH -> "L'IA analyse votre commande et prépare vos articles... ⚡"
                                            AppLanguage.ENGLISH -> "AI is analyzing your order and preparing items... ⚡"
                                            AppLanguage.ARABIC -> "الذكاء الاصطناعي يحلل طلبك ويختار المشتريات... ⚡"
                                        }
                                        isListening -> when (language) {
                                            AppLanguage.FRENCH -> "Écoute en cours... Parlez librement 🎙️"
                                            AppLanguage.ENGLISH -> "Listening now... Speak freely 🎙️"
                                            AppLanguage.ARABIC -> "جاري الاستماع لصوتك الآن... تكلم براحتك 🎙️"
                                        }
                                        else -> when (language) {
                                            AppLanguage.FRENCH -> "Appuyez sur le micro et parlez en français ou darija 🎙️"
                                            AppLanguage.ENGLISH -> "Tap microphone and speak your order 🎙️"
                                            AppLanguage.ARABIC -> "اضغط على الميكروفون وتكلم بالدارجة أو العربية 🎙️"
                                        }
                                    },
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isListening) Color(0xFFDC2626) else AzizOrangePrimary,
                                    textAlign = TextAlign.Center
                                )

                                if (isListening) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    VoiceWaveformBars()
                                }

                                Text(
                                    text = when (language) {
                                        AppLanguage.FRENCH -> "Commandez n'importe quoi (ex: Menu Big Mac, Tajine, Pizza Supreme, courses Marjane...)"
                                        AppLanguage.ENGLISH -> "Order anything (e.g. Big Mac meal, Tajine, Pizza, Marjane groceries...)"
                                        AppLanguage.ARABIC -> "اطلب أي شيء (مثال: وجبة بيج ماك، طاجين بلدي، بيتزا سوبريم، حليب وبيض من مرجان...)"
                                    },
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                        }
                    }

                    // Live Speech Transcript (if available)
                    if (spokenQuery.isNotBlank()) {
                        item {
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = AzizOrangePrimary.copy(alpha = 0.08f),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    AzizOrangePrimary.copy(alpha = 0.3f)
                                ),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(text = "🗣️", fontSize = 18.sp)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = when (language) {
                                                AppLanguage.FRENCH -> "Votre demande vocale :"
                                                AppLanguage.ENGLISH -> "Your voice request:"
                                                AppLanguage.ARABIC -> "طلبك الصوتي المسموع:"
                                            },
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = AzizOrangePrimary
                                        )
                                        Text(
                                            text = "\"$spokenQuery\"",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                    IconButton(
                                        onClick = { startListeningFlow() },
                                        modifier = Modifier.size(32.dp)
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

                    // Quick Voice Suggestions Carousel
                    item {
                        Column {
                            Text(
                                text = when (language) {
                                    AppLanguage.FRENCH -> "Ou choisissez un exemple en un clic :"
                                    AppLanguage.ENGLISH -> "Or select a quick order example with one tap:"
                                    AppLanguage.ARABIC -> "أو اختر أمثلة طلبات صوتية شائعة بنقرة واحدة:"
                                },
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
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
                                        VoiceQuickChip("🍔 Menu Big Mac Maxi") { onSubmitQuery("Je veux un menu Maxi Best Of Big Mac") }
                                        VoiceQuickChip("🍕 Pizza Supreme Croûte") { onSubmitQuery("Je veux une pizza Supreme avec croûte au fromage") }
                                        VoiceQuickChip("🍗 Bucket KFC 12 Pièces") { onSubmitQuery("Bucket KFC 12 pièces avec frites") }
                                        VoiceQuickChip("🍲 Tajine de Bœuf Pruneaux") { onSubmitQuery("Un tajine de bœuf aux pruneaux avec du pain") }
                                        VoiceQuickChip("🥦 Panier Épicerie Marjane") { onSubmitQuery("Lait, œufs frais et huile d'olive de chez Marjane") }
                                        VoiceQuickChip("🥐 Café & Croissant Paul") { onSubmitQuery("Croissants chauds et café chez Paul") }
                                        VoiceQuickChip("💊 Vitamine C Pharmacie") { onSubmitQuery("Vitamine C et paracétamol de la pharmacie") }
                                        VoiceQuickChip("🛵 Coursier Express Jibli") { onSubmitQuery("Besoin d'un coursier express pour un colis") }
                                    }
                                    AppLanguage.ENGLISH -> {
                                        VoiceQuickChip("🍔 Big Mac Combo Meal") { onSubmitQuery("I want a Big Mac combo meal from McDonald's") }
                                        VoiceQuickChip("🍕 Pizza Hut Stuffed Crust") { onSubmitQuery("Supreme pizza with cheese stuffed crust") }
                                        VoiceQuickChip("🍗 KFC 12pc Crispy Bucket") { onSubmitQuery("KFC 12 pieces crispy chicken bucket") }
                                        VoiceQuickChip("🍲 Moroccan Beef Tajine") { onSubmitQuery("Authentic beef tajine with prunes and bread") }
                                        VoiceQuickChip("🥦 Marjane Grocery Pack") { onSubmitQuery("Fresh milk, eggs, olive oil from Marjane") }
                                        VoiceQuickChip("🥐 Paul Coffee & Croissant") { onSubmitQuery("Hot croissants and coffee from Paul Bakery") }
                                        VoiceQuickChip("💊 Vitamin C & Pharmacy") { onSubmitQuery("Vitamin C and headache medicine from pharmacy") }
                                        VoiceQuickChip("🛵 Allo Jibli Courier") { onSubmitQuery("I need an on-demand courier for a parcel") }
                                    }
                                    AppLanguage.ARABIC -> {
                                        VoiceQuickChip("🍔 وجبة بيج ماك كومبو") { onSubmitQuery("أريد وجبة بيج ماك كومبو من ماكدونالدز") }
                                        VoiceQuickChip("🍕 بيتزا هت تشيزي كراست") { onSubmitQuery("أريد بيتزا سوبريم مع أطراف جبن") }
                                        VoiceQuickChip("🍗 باكت كنتاكي 12 قطعة") { onSubmitQuery("باكت كنتاكي كريسبي مع بطاطس") }
                                        VoiceQuickChip("🍲 طاجين لحم بالبرقوق") { onSubmitQuery("طاجين بلدي لحم بالبرقوق مع خبز") }
                                        VoiceQuickChip("🥦 سلة مشتريات مرجان") { onSubmitQuery("حليب وبيض وزيت زيتون من مرجان") }
                                        VoiceQuickChip("🥐 قهوة وكرواسون بول") { onSubmitQuery("كرواسون فرنسي وقهوة من مقهى بول") }
                                        VoiceQuickChip("💊 فيتامين C وصيدلية") { onSubmitQuery("فيتامين سي ومسكن صداع من الصيدلية") }
                                        VoiceQuickChip("🛵 كابتن ألو جيب لي") { onSubmitQuery("أريد كابتن لتوصيل طرد فوري") }
                                    }
                                }
                            }
                        }
                    }

                    // AI Generated Recommendation & List
                    if (recommendation != null && recommendation.items.isNotEmpty()) {
                        item {
                            Surface(
                                shape = RoundedCornerShape(22.dp),
                                color = MaterialTheme.colorScheme.surface,
                                border = androidx.compose.foundation.BorderStroke(
                                    1.5.dp,
                                    AzizMint.copy(alpha = 0.6f)
                                ),
                                shadowElevation = 4.dp,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    // AI Header & Store
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(text = "✨", fontSize = 16.sp)
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = when (language) {
                                                    AppLanguage.FRENCH -> "Panier sélectionné par l'IA"
                                                    AppLanguage.ENGLISH -> "AI-Selected Shopping Basket"
                                                    AppLanguage.ARABIC -> "قائمة مشترياتك المختارة بالذكاء الاصطناعي"
                                                },
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Black,
                                                color = AzizCharcoalDark
                                            )
                                        }

                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = AzizMint.copy(alpha = 0.15f)
                                        ) {
                                            Text(
                                                text = when (language) {
                                                    AppLanguage.FRENCH -> "${recommendation.items.size} articles 🛒"
                                                    AppLanguage.ENGLISH -> "${recommendation.items.size} items 🛒"
                                                    AppLanguage.ARABIC -> "${recommendation.items.size} أصناف 🛒"
                                                },
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = AzizMint
                                            )
                                        }
                                    }

                                    // Store & ETA Pill
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Surface(
                                            shape = RoundedCornerShape(10.dp),
                                            color = MaterialTheme.colorScheme.surfaceVariant
                                        ) {
                                            Text(
                                                text = when (language) {
                                                    AppLanguage.FRENCH -> "🏪 Magasin : ${recommendation.targetStoreName}"
                                                    AppLanguage.ENGLISH -> "🏪 Store: ${recommendation.targetStoreName}"
                                                    AppLanguage.ARABIC -> "🏪 المتجر: ${recommendation.targetStoreName}"
                                                },
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Surface(
                                            shape = RoundedCornerShape(10.dp),
                                            color = AzizOrangePrimary.copy(alpha = 0.12f)
                                        ) {
                                            Text(
                                                text = when (language) {
                                                    AppLanguage.FRENCH -> "⚡ Livraison : ~${recommendation.estimatedTimeMins} min"
                                                    AppLanguage.ENGLISH -> "⚡ Delivery: ~${recommendation.estimatedTimeMins} mins"
                                                    AppLanguage.ARABIC -> "⚡ توصيل: ~${recommendation.estimatedTimeMins} دقيقة"
                                                },
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = AzizOrangePrimary
                                            )
                                        }
                                    }

                                    // AI Explanation Speech Bubble
                                    Surface(
                                        shape = RoundedCornerShape(14.dp),
                                        color = Color(0xFFF0FDF4),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFBBF7D0)),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(bottom = 12.dp)
                                    ) {
                                        Row(modifier = Modifier.padding(12.dp)) {
                                            Text(text = "🤖", fontSize = 18.sp)
                                            Spacer(modifier = Modifier.width(8.dp))
                                            val explanation = when (language) {
                                                AppLanguage.FRENCH -> recommendation.aiExplanationFr.ifBlank { recommendation.aiExplanationAr }
                                                AppLanguage.ENGLISH -> recommendation.aiExplanationFr.ifBlank { recommendation.aiExplanationAr }
                                                AppLanguage.ARABIC -> recommendation.aiExplanationAr.ifBlank { recommendation.aiExplanationFr }
                                            }
                                            Text(
                                                text = explanation,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = Color(0xFF166534),
                                                lineHeight = 18.sp
                                            )
                                        }
                                    }

                                    // Item List
                                    Column(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        recommendation.items.forEach { item ->
                                            AiItemRow(
                                                item = item,
                                                language = language,
                                                onIncrease = { onUpdateQuantity(item.product.id, 1) },
                                                onDecrease = { onUpdateQuantity(item.product.id, -1) },
                                                onDelete = { onRemoveItem(item.product.id) }
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(14.dp))

                                    val currSuffix = when (language) {
                                        AppLanguage.FRENCH -> " DH"
                                        AppLanguage.ENGLISH -> " MAD"
                                        AppLanguage.ARABIC -> " د.م."
                                    }

                                    // Pricing Breakdown
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(
                                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                                                RoundedCornerShape(14.dp)
                                            )
                                            .padding(12.dp),
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
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
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Text(
                                                text = String.format(Locale.US, "%.2f%s", recommendation.estimatedTotal, currSuffix),
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = when (language) {
                                                    AppLanguage.FRENCH -> "Livraison express (Mohammédia) :"
                                                    AppLanguage.ENGLISH -> "Express delivery (Mohammedia):"
                                                    AppLanguage.ARABIC -> "رسوم التوصيل السريع (المحمدية):"
                                                },
                                                fontSize = 12.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Text(
                                                text = String.format(Locale.US, "%.2f%s", recommendation.deliveryFee, currSuffix),
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = AzizMint
                                            )
                                        }

                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(1.dp)
                                                .background(MaterialTheme.colorScheme.outlineVariant)
                                                .padding(vertical = 4.dp)
                                        )

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = when (language) {
                                                    AppLanguage.FRENCH -> "Montant Total :"
                                                    AppLanguage.ENGLISH -> "Total Amount:"
                                                    AppLanguage.ARABIC -> "المبلغ الإجمالي النهائي:"
                                                },
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Black,
                                                color = AzizCharcoalDark
                                            )
                                            Text(
                                                text = String.format(Locale.US, "%.2f%s", recommendation.grandTotal, currSuffix),
                                                fontSize = 16.sp,
                                                fontWeight = FontWeight.Black,
                                                color = AzizOrangePrimary
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(16.dp))

                                    // THE CRITICAL CONFIRMATION BUTTON:
                                    // "ثم لا يقال العميل والضغط على التاكيد confèrem"
                                    Button(
                                        onClick = { onConfirmRecommendation(recommendation) },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(54.dp)
                                            .shadow(8.dp, RoundedCornerShape(16.dp))
                                            .testTag("confirm_ai_shopping_list_button"),
                                        shape = RoundedCornerShape(16.dp),
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
                                                modifier = Modifier.size(22.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
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

                // Bottom Bar: Fallback Text Input Field
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 6.dp,
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
                                        AppLanguage.FRENCH -> "Ou écrivez vos courses ici (ex: Big Mac, pizza...)"
                                        AppLanguage.ENGLISH -> "Or type your order here (e.g. Big Mac, pizza...)"
                                        AppLanguage.ARABIC -> "أو اكتب طلبك هنا يدوياً..."
                                    },
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
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
                                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
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
                                .background(AzizOrangePrimary)
                                .testTag("ai_send_query_button")
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

@Composable
private fun AiItemRow(
    item: com.example.data.services.AiRecommendedItem,
    language: AppLanguage,
    onIncrease: () -> Unit,
    onDecrease: () -> Unit,
    onDelete: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = item.product.emoji, fontSize = 24.sp)
            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                val productName = when (language) {
                    AppLanguage.FRENCH -> item.product.nameFr
                    AppLanguage.ENGLISH -> item.product.nameEn
                    AppLanguage.ARABIC -> item.product.nameAr
                }
                Text(
                    text = productName,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (item.customNote.isNotBlank()) {
                    Text(
                        text = item.customNote,
                        fontSize = 10.sp,
                        color = AzizOrangePrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                val curr = when (language) {
                    AppLanguage.FRENCH -> "DH"
                    AppLanguage.ENGLISH -> "MAD"
                    AppLanguage.ARABIC -> "د.م."
                }
                Text(
                    text = String.format(Locale.US, "%.2f %s", item.product.price, curr),
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Quantity Editor
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Surface(
                    onClick = onDecrease,
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier.size(26.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(text = "-", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }

                Text(
                    text = item.quantity.toString(),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )

                Surface(
                    onClick = onIncrease,
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier.size(26.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(text = "+", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }

                Spacer(modifier = Modifier.width(4.dp))

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(26.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Remove",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun VoiceWaveformBars() {
    val infiniteTransition = rememberInfiniteTransition(label = "waveform_anim")
    val h1 by infiniteTransition.animateFloat(
        initialValue = 6f, targetValue = 28f,
        animationSpec = infiniteRepeatable(tween(250, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "h1"
    )
    val h2 by infiniteTransition.animateFloat(
        initialValue = 18f, targetValue = 36f,
        animationSpec = infiniteRepeatable(tween(350, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "h2"
    )
    val h3 by infiniteTransition.animateFloat(
        initialValue = 10f, targetValue = 42f,
        animationSpec = infiniteRepeatable(tween(400, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "h3"
    )
    val h4 by infiniteTransition.animateFloat(
        initialValue = 22f, targetValue = 30f,
        animationSpec = infiniteRepeatable(tween(300, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "h4"
    )
    val h5 by infiniteTransition.animateFloat(
        initialValue = 8f, targetValue = 24f,
        animationSpec = infiniteRepeatable(tween(280, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "h5"
    )

    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.height(44.dp)
    ) {
        listOf(h1, h2, h3, h4, h5).forEach { h ->
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(h.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(AzizOrangePrimary)
            )
        }
    }
}

@Composable
private fun VoiceQuickChip(label: String, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

private fun borderStrokeForAi(isListening: Boolean, isProcessing: Boolean): androidx.compose.foundation.BorderStroke {
    return when {
        isListening -> androidx.compose.foundation.BorderStroke(2.dp, Color(0xFFDC2626))
        isProcessing -> androidx.compose.foundation.BorderStroke(2.dp, Color(0xFF6366F1))
        else -> androidx.compose.foundation.BorderStroke(1.dp, AzizOrangePrimary.copy(alpha = 0.35f))
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
        AppLanguage.FRENCH -> "Parlez et dictez vos courses (ex: Big Mac, pizza, tajine, lait, œufs...)"
        AppLanguage.ENGLISH -> "Speak and dictate your shopping list (e.g., Big Mac, pizza, tajine, milk, eggs...)"
        AppLanguage.ARABIC -> "تكلم بالصوت واطلب مشترياتك (مثال: وجبة بيج ماك، بيتزا، طاجين، حليب وبيض...)"
    }
    val fallbackMsg = when (language) {
        AppLanguage.FRENCH -> "Reconnaissance vocale non disponible sur cet appareil. Tapez votre demande ci-dessous !"
        AppLanguage.ENGLISH -> "Voice recognition not available on this device. You can type below!"
        AppLanguage.ARABIC -> "خدمة التعرف الصوتي غير متوفرة في المحاكي، يمكنك كتابة طلبك في خانة النص أدناه!"
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
