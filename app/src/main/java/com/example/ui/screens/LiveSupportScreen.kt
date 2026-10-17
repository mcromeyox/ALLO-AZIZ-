package com.example.ui.screens

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.speech.RecognizerIntent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.data.localization.AppStrings
import com.example.data.model.AppLanguage
import com.example.data.model.SupportChatMessage
import com.example.data.system.VoiceSpeechHelper
import com.example.ui.components.AiSupportVoiceCallDialog
import com.example.ui.theme.AzizAmberSecondary
import com.example.ui.theme.AzizCharcoalDark
import com.example.ui.theme.AzizMint
import com.example.ui.theme.AzizOrangePrimary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun LiveSupportScreen(
    messages: List<SupportChatMessage>,
    language: AppLanguage,
    onSendMessage: (String) -> Unit
) {
    val context = LocalContext.current
    var inputMessage by remember { mutableStateOf("") }
    var expandedFaqIndex by remember { mutableIntStateOf(-1) }
    var showWhatsAppDialog by remember { mutableStateOf(false) }
    var showVoiceCallDialog by remember { mutableStateOf(false) }
    var customPhoneInput by remember {
        mutableStateOf(com.example.data.system.ExternalShareHelper.getSavedWhatsAppNumber(context))
    }

    // Speech synthesis helper for reading chat messages aloud
    val voiceHelper = remember { VoiceSpeechHelper(context) }
    val isSpeaking by voiceHelper.isSpeaking.collectAsState()
    val currentUtteranceId by voiceHelper.currentUtteranceId.collectAsState()

    DisposableEffect(Unit) {
        onDispose {
            voiceHelper.stop()
            voiceHelper.shutdown()
        }
    }

    // Speech-to-Text launcher for chat microphone button
    val chatSpeechLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            val spoken = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()?.trim()
            if (!spoken.isNullOrBlank()) {
                inputMessage = spoken
                onSendMessage(spoken)
                inputMessage = ""
            }
        }
    }

    val audioPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            launchChatSpeechRecognizer(context, chatSpeechLauncher, language)
        } else {
            val msg = when (language) {
                AppLanguage.FRENCH -> "Veuillez activer le microphone pour dicter votre message 🎙️"
                AppLanguage.ENGLISH -> "Please enable microphone permission to speak 🎙️"
                AppLanguage.ARABIC -> "يرجى تفعيل الميكروفون للتحدث الصوتي 🎙️"
            }
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
        }
    }

    val quickChips = when (language) {
        AppLanguage.FRENCH -> listOf(
            "Où est ma commande ? 🛵",
            "Changer mon adresse 📍",
            "Moyens de paiement 💳",
            "Code promo exclusif 🎁",
            "Parler au responsable 📞"
        )
        AppLanguage.ENGLISH -> listOf(
            "Where is my order? 🛵",
            "Change delivery address 📍",
            "Payment issue 💳",
            "Request promo code 🎁",
            "Speak to manager 📞"
        )
        AppLanguage.ARABIC -> listOf(
            "أين طلبي الآن؟ 🛵",
            "تغيير عنوان التوصيل 📍",
            "طريقة الدفع 💳",
            "طلب كود خصم حصري 🎁",
            "التحدث مع المشرف 📞"
        )
    }

    val faqs = when (language) {
        AppLanguage.FRENCH -> listOf(
            Pair(
                "Combien de temps prend la livraison avec ALLO AZIZ ?",
                "La livraison prend entre 15 et 25 minutes grâce à notre réseau de coursiers déployés partout à Mohammedia et ses environs."
            ),
            Pair(
                "Le paiement via l'application est-il sécurisé ?",
                "Oui, tous les paiements par carte bancaire sont 100% sécurisés (cryptage bancaire 256-bit SSL). Nous proposons aussi le paiement en espèces à la livraison et le portefeuille ALLO Pay."
            ),
            Pair(
                "Comment contacter le coursier Aziz directement ?",
                "Dès l'acceptation de la commande, le numéro de téléphone et les boutons d'appel et chat directs avec Aziz apparaissent dans l'écran de suivi en direct."
            ),
            Pair(
                "Que faire si un article manque à ma commande ?",
                "L'équipe support 24/7 vous indemnise immédiatement : soit par réexpédition gratuite et immédiate, soit par recrédit direct sur votre portefeuille ALLO Pay en 1 minute."
            )
        )
        AppLanguage.ENGLISH -> listOf(
            Pair(
                "How long does delivery usually take?",
                "Delivery takes between 15 and 25 minutes thanks to our dedicated courier fleet across Mohammedia."
            ),
            Pair(
                "Is payment through the app secure?",
                "Yes, 100% encrypted with 256-bit SSL banking security. We also accept Cash on Delivery and ALLO Pay wallet."
            ),
            Pair(
                "How can I contact the courier directly?",
                "As soon as an order is accepted, you can call or message courier Aziz directly from the live tracking screen."
            ),
            Pair(
                "What if an item is missing from my order?",
                "Our 24/7 team covers you: we immediately re-dispatch the missing item for free, or refund your wallet in 1 minute."
            )
        )
        AppLanguage.ARABIC -> listOf(
            Pair(
                "كم يستغرق التوصيل مع ألو عزيز عادةً؟",
                "يستغرق التوصيل ما بين 15 إلى 25 دقيقة كحد أقصى بفضل شبكة مناديبنا المتواجدين في كافة أرجاء المدينة."
            ),
            Pair(
                "هل الدفع عبر التطبيق آمن ومضمون؟",
                "نعم، جميع المعاملات مشفرة بأعلى بروتوكول بنكي ومعتمدة رسمياً، كما نوفر الدفع نقداً ومحفظة ALLO Pay."
            ),
            Pair(
                "كيف أتواصل مع المندوب مباشرة؟",
                "فور قبول الطلب، يظهر رقم هاتف المندوب وزر الاتصال والمحادثة المباشرة داخل شاشة التتبع الفوري."
            ),
            Pair(
                "ماذا أفعل إذا وصل الطلب غير مكتمل؟",
                "فريق الدعم 24/7 يعوضك فوراً إما بإرسال المندوب مجاناً أو بإرجاع المبلغ لمحفظتك خلال دقيقة."
            )
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("live_support_screen")
    ) {
        // Support Header with 24/7 Status
        Card(
            shape = RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(AzizOrangePrimary.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.SupportAgent,
                                contentDescription = null,
                                tint = AzizOrangePrimary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = AppStrings.supportTitle(language),
                                fontWeight = FontWeight.Black,
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(AzizMint)
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = AppStrings.supportOnlineBadge(language),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = AzizMint
                                )
                            }
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // WhatsApp Support button
                        IconButton(
                            onClick = {
                                val saved = com.example.data.system.ExternalShareHelper.getSavedWhatsAppNumber(context)
                                if (saved.isNotBlank() && !com.example.data.system.ExternalShareHelper.isPlaceholderOrInvalid(saved)) {
                                    com.example.data.system.ExternalShareHelper.openWhatsApp(
                                        context = context,
                                        phoneE164 = saved,
                                        prefilledText = if (language == AppLanguage.FRENCH) {
                                            "Bonjour, j'ai besoin d'une assistance du support ALLO AZIZ."
                                        } else {
                                            "السلام عليكم، أحتاج مساعدة ودعم فني من فريق ألو عزيز."
                                        }
                                    )
                                } else {
                                    showWhatsAppDialog = true
                                }
                            },
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(AzizMint)
                                .testTag("support_whatsapp_btn")
                        ) {
                            Text("💬", fontSize = 16.sp)
                        }

                        // Direct call button
                        IconButton(
                            onClick = {
                                val intent = Intent(Intent.ACTION_DIAL).apply {
                                    data = Uri.parse("tel:0800255629")
                                }
                                context.startActivity(intent)
                            },
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(AzizOrangePrimary)
                                .testTag("support_call_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Call,
                                contentDescription = "Call Hotline",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Hotline Banner (French prominent)
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = when (language) {
                                AppLanguage.FRENCH -> "Hotline Gratuite : 0800-ALLO-AZIZ (24h/24)"
                                AppLanguage.ENGLISH -> "Toll-Free Hotline: 0800-ALLO-AZIZ (24/7)"
                                AppLanguage.ARABIC -> "خط المساعدة المجاني: 0800-ALLO-AZIZ (24h/24)"
                            },
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = when (language) {
                                AppLanguage.FRENCH -> "En ligne 📞"
                                AppLanguage.ENGLISH -> "Live 📞"
                                AppLanguage.ARABIC -> "متاح الآن 📞"
                            },
                            fontSize = 11.sp,
                            color = AzizMint,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // FEATURED: AI Voice Conversation Hero Card ("Appel Vocal avec l'IA Support")
        Surface(
            onClick = { showVoiceCallDialog = true },
            shape = RoundedCornerShape(20.dp),
            shadowElevation = 4.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .clip(RoundedCornerShape(20.dp))
                .testTag("support_ai_voice_call_hero_card")
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xFF1E1B4B),
                                Color(0xFF31104B),
                                AzizCharcoalDark
                            )
                        )
                    )
                    .padding(14.dp)
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
                                .size(46.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(listOf(AzizOrangePrimary, AzizAmberSecondary))
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.RecordVoiceOver,
                                contentDescription = "Voice Conversation",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = when (language) {
                                        AppLanguage.FRENCH -> "Appel Vocal avec Sara IA"
                                        AppLanguage.ENGLISH -> "Voice Call with Sara AI"
                                        AppLanguage.ARABIC -> "محادثة صوتية مع سارة AI"
                                    },
                                    color = Color.White,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 14.sp
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = AzizMint.copy(alpha = 0.25f)
                                ) {
                                    Text(
                                        text = "NOUVEAU 🎙️",
                                        color = AzizMint,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            Text(
                                text = when (language) {
                                    AppLanguage.FRENCH -> "Parlez à voix haute, l'IA vous écoute et vous répond !"
                                    AppLanguage.ENGLISH -> "Speak aloud, AI listens and speaks back to you!"
                                    AppLanguage.ARABIC -> "تكلم بصوتك وستجيبك سارة صوتياً في الحال!"
                                },
                                color = Color.White.copy(alpha = 0.85f),
                                fontSize = 11.sp,
                                maxLines = 1
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = AzizOrangePrimary,
                        modifier = Modifier.padding(start = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = when (language) {
                                    AppLanguage.FRENCH -> "Parler 🎙️"
                                    AppLanguage.ENGLISH -> "Speak 🎙️"
                                    AppLanguage.ARABIC -> "تكلم الآن 🎙️"
                                },
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // Quick Inquiries Chips
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(quickChips) { chip ->
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = AzizOrangePrimary.copy(alpha = 0.1f),
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .clickable { onSendMessage(chip) }
                ) {
                    Text(
                        text = chip,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = AzizOrangePrimary
                    )
                }
            }
        }

        // Chat Message Log
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            items(messages) { msg ->
                val utteranceId = "msg_${msg.id}"
                val isThisMessageSpeaking = isSpeaking && currentUtteranceId == utteranceId

                ChatBubble(
                    message = msg,
                    language = language,
                    isPlayingVoice = isThisMessageSpeaking,
                    onPlayVoice = { text ->
                        if (isThisMessageSpeaking) {
                            voiceHelper.stop()
                        } else {
                            voiceHelper.speak(text, language, utteranceId)
                        }
                    }
                )
            }

            // Expandable FAQ section at bottom of support
            item {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = when (language) {
                        AppLanguage.FRENCH -> "Questions Fréquentes & Aide Rapide ❓"
                        AppLanguage.ENGLISH -> "Frequently Asked Questions & Help ❓"
                        AppLanguage.ARABIC -> "الأسئلة الشائعة والمساعدة السريعة ❓"
                    },
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(vertical = 6.dp)
                )
            }

            items(faqs.indices.toList()) { index ->
                val (q, a) = faqs[index]
                val isExpanded = expandedFaqIndex == index

                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            expandedFaqIndex = if (isExpanded) -1 else index
                        }
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = q,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.sp,
                                modifier = Modifier.weight(1f)
                            )
                            Icon(
                                imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = null,
                                tint = AzizOrangePrimary
                            )
                        }

                        AnimatedVisibility(visible = isExpanded) {
                            Column(modifier = Modifier.padding(top = 8.dp)) {
                                Text(
                                    text = a,
                                    fontSize = 11.sp,
                                    lineHeight = 16.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }

        // Bottom Input Row (with Voice Microphone Button)
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 80.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = inputMessage,
                    onValueChange = { inputMessage = it },
                    placeholder = {
                        Text(AppStrings.supportTypeMessage(language), fontSize = 12.sp)
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("support_input_field"),
                    singleLine = true,
                    shape = RoundedCornerShape(20.dp)
                )

                Spacer(modifier = Modifier.width(4.dp))

                // Interactive AI Voice Call Launcher (Sara IA)
                IconButton(
                    onClick = { showVoiceCallDialog = true },
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF4F46E5).copy(alpha = 0.15f))
                        .testTag("support_start_voice_call_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.RecordVoiceOver,
                        contentDescription = "Voice Call with Sara IA",
                        tint = Color(0xFF6366F1),
                        modifier = Modifier.size(19.dp)
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                // Microphone voice dictation button
                IconButton(
                    onClick = {
                        val hasPermission = ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.RECORD_AUDIO
                        ) == PackageManager.PERMISSION_GRANTED
                        if (hasPermission) {
                            launchChatSpeechRecognizer(context, chatSpeechLauncher, language)
                        } else {
                            audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                        }
                    },
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(AzizMint.copy(alpha = 0.18f))
                        .testTag("support_mic_input_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = "Speak to Support",
                        tint = AzizMint,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                IconButton(
                    onClick = {
                        if (inputMessage.isNotBlank()) {
                            onSendMessage(inputMessage)
                            inputMessage = ""
                        }
                    },
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(AzizOrangePrimary)
                        .testTag("send_support_message_btn")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }

    // Interactive AI Voice Call Dialog
    if (showVoiceCallDialog) {
        AiSupportVoiceCallDialog(
            language = language,
            onDismissRequest = { showVoiceCallDialog = false },
            onSupportMessageLogged = { userQ, aiR ->
                // Optionally log into current session chat
                onSendMessage(userQ)
            }
        )
    }

    if (showWhatsAppDialog) {
        AlertDialog(
            onDismissRequest = { showWhatsAppDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("💬", fontSize = 22.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = when (language) {
                            AppLanguage.FRENCH -> "Support WhatsApp ALLO AZIZ 🇲🇦"
                            AppLanguage.ENGLISH -> "ALLO AZIZ WhatsApp Support 🇲🇦"
                            AppLanguage.ARABIC -> "دعم ألو عزيز عبر واتساب (WhatsApp 🇲🇦)"
                        },
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = when (language) {
                            AppLanguage.FRENCH -> "Contact direct et immédiat avec le support et la direction d'ALLO AZIZ via WhatsApp."
                            AppLanguage.ENGLISH -> "Instant contact with ALLO AZIZ customer care via WhatsApp."
                            AppLanguage.ARABIC -> "تواصل فوري ومباشر مع الدعم الفني وإدارة ألو عزيز عبر تطبيق واتساب."
                        },
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = when (language) {
                            AppLanguage.FRENCH -> "Numéro WhatsApp du support :"
                            AppLanguage.ENGLISH -> "Support WhatsApp number:"
                            AppLanguage.ARABIC -> "رقم هاتف الدعم الفني / الإدارة:"
                        },
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = customPhoneInput,
                        onValueChange = { customPhoneInput = it },
                        placeholder = { Text("Ex: 0612345678 ou 0712345678", fontSize = 12.sp) },
                        modifier = Modifier.fillMaxWidth().testTag("support_custom_whatsapp_input"),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val phoneToUse = customPhoneInput.trim()
                        if (phoneToUse.isNotBlank()) {
                            com.example.data.system.ExternalShareHelper.saveWhatsAppNumber(context, phoneToUse)
                            com.example.data.system.ExternalShareHelper.openWhatsApp(
                                context = context,
                                phoneE164 = phoneToUse,
                                prefilledText = when (language) {
                                    AppLanguage.FRENCH -> "Bonjour, j'ai besoin d'une assistance du support ALLO AZIZ."
                                    AppLanguage.ENGLISH -> "Hello, I need assistance from ALLO AZIZ support."
                                    AppLanguage.ARABIC -> "السلام عليكم، أحتاج مساعدة ودعم فني من فريق ألو عزيز."
                                }
                            )
                            showWhatsAppDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AzizMint)
                ) {
                    Text(
                        text = when (language) {
                            AppLanguage.FRENCH -> "Ouvrir WhatsApp 🚀"
                            AppLanguage.ENGLISH -> "Open WhatsApp 🚀"
                            AppLanguage.ARABIC -> "فتح واتساب الآن 🚀"
                        },
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showWhatsAppDialog = false }) {
                    Text(
                        text = when (language) {
                            AppLanguage.FRENCH -> "Annuler"
                            AppLanguage.ENGLISH -> "Cancel"
                            AppLanguage.ARABIC -> "إلغاء"
                        }
                    )
                }
            }
        )
    }
}

@Composable
fun ChatBubble(
    message: SupportChatMessage,
    language: AppLanguage = AppLanguage.FRENCH,
    isPlayingVoice: Boolean = false,
    onPlayVoice: (String) -> Unit = {}
) {
    val isUser = message.isUser
    val timeStr = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(message.timestamp))

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        if (!isUser) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(AzizOrangePrimary),
                contentAlignment = Alignment.Center
            ) {
                Text("👩‍💼", fontSize = 14.sp)
            }
            Spacer(modifier = Modifier.width(6.dp))
        }

        Column(
            horizontalAlignment = if (isUser) Alignment.End else Alignment.Start,
            modifier = Modifier.fillMaxWidth(0.85f)
        ) {
            if (!isUser) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = message.agentName,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = AzizOrangePrimary,
                        modifier = Modifier.padding(bottom = 2.dp, start = 4.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "• IA 24/7",
                        fontSize = 9.sp,
                        color = AzizMint,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Box(
                modifier = Modifier
                    .clip(
                        RoundedCornerShape(
                            topStart = 14.dp,
                            topEnd = 14.dp,
                            bottomStart = if (isUser) 14.dp else 2.dp,
                            bottomEnd = if (isUser) 2.dp else 14.dp
                        )
                    )
                    .background(
                        if (isUser) AzizOrangePrimary else MaterialTheme.colorScheme.surfaceVariant
                    )
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Text(
                    text = message.text,
                    fontSize = 13.sp,
                    color = if (isUser) Color.White else MaterialTheme.colorScheme.onSurface,
                    lineHeight = 18.sp
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
            ) {
                Text(
                    text = timeStr,
                    fontSize = 9.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Voice playback button for support answers
                if (!isUser) {
                    Surface(
                        onClick = { onPlayVoice(message.text) },
                        shape = RoundedCornerShape(10.dp),
                        color = if (isPlayingVoice) AzizMint.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier.testTag("play_support_voice_${message.id}")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Icon(
                                imageVector = if (isPlayingVoice) Icons.Default.VolumeUp else Icons.Default.VolumeUp,
                                contentDescription = "Play Audio",
                                tint = if (isPlayingVoice) AzizMint else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = if (isPlayingVoice) {
                                    when (language) {
                                        AppLanguage.FRENCH -> "En lecture... 🔊"
                                        AppLanguage.ENGLISH -> "Playing... 🔊"
                                        AppLanguage.ARABIC -> "جاري الاستماع... 🔊"
                                    }
                                } else {
                                    when (language) {
                                        AppLanguage.FRENCH -> "Écouter"
                                        AppLanguage.ENGLISH -> "Listen"
                                        AppLanguage.ARABIC -> "استماع"
                                    }
                                },
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isPlayingVoice) AzizMint else MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            if (isPlayingVoice) {
                                Spacer(modifier = Modifier.width(4.dp))
                                MiniWaveEqualizer(color = AzizMint)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MiniWaveEqualizer(color: Color) {
    val infiniteTransition = rememberInfiniteTransition(label = "mini_equalizer")
    val h1 by infiniteTransition.animateFloat(
        initialValue = 3f, targetValue = 10f,
        animationSpec = infiniteRepeatable(tween(300, easing = LinearEasing), RepeatMode.Reverse),
        label = "m1"
    )
    val h2 by infiniteTransition.animateFloat(
        initialValue = 8f, targetValue = 2f,
        animationSpec = infiniteRepeatable(tween(360, easing = LinearEasing), RepeatMode.Reverse),
        label = "m2"
    )
    val h3 by infiniteTransition.animateFloat(
        initialValue = 4f, targetValue = 11f,
        animationSpec = infiniteRepeatable(tween(280, easing = LinearEasing), RepeatMode.Reverse),
        label = "m3"
    )

    Row(
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        listOf(h1, h2, h3).forEach { h ->
            Box(
                modifier = Modifier
                    .width(2.dp)
                    .height(h.dp)
                    .clip(RoundedCornerShape(1.dp))
                    .background(color)
            )
        }
    }
}

private fun launchChatSpeechRecognizer(
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
        AppLanguage.FRENCH -> "Dictez votre message au support..."
        AppLanguage.ENGLISH -> "Dictate your message to support..."
        AppLanguage.ARABIC -> "تكلم بصوتك لكتابة استفسارك للدعم..."
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
        Toast.makeText(
            context,
            if (language == AppLanguage.FRENCH) "Reconnaissance vocale non disponible sur cet appareil." else "Reconnaissance vocale non disponible.",
            Toast.LENGTH_SHORT
        ).show()
    }
}
