package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.QuestionAnswer
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.localization.AppStrings
import com.example.data.model.AppLanguage
import com.example.data.model.SupportChatMessage
import com.example.ui.theme.AzizAmberSecondary
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
    var customPhoneInput by remember {
        mutableStateOf(com.example.data.system.ExternalShareHelper.getSavedWhatsAppNumber(context))
    }

    val quickChips = listOf(
        "أين طلبي الآن؟ 🛵",
        "تغيير عنوان التوصيل 📍",
        "مشكلة في طريقة الدفع 💳",
        "طلب كود خصم حصري 🎁",
        "التحدث مع مدير التوصيل 📞"
    )

    val faqs = listOf(
        Pair("كم يستغرق التوصيل مع ألو عزيز عادةً؟", "يستغرق التوصيل ما بين 15 إلى 25 دقيقة كحد أقصى بفضل شبكة مناديبنا المتواجدين في كافة أرجاء المدينة."),
        Pair("هل الدفع عبر التطبيق آمن ومضمون؟", "نعم، جميع المعاملات مشفرة بأعلى بروتوكول بنكي 256-bit SSL ومعتمدة رسمياً، كما نوفر خيار الدفع نقداً عند الاستلام ومحفظة ALLO Pay."),
        Pair("كيف أتواصل مع المندوب مباشرة؟", "فور قبول الطلب، يظهر رقم هاتف المندوب وزر الاتصال والمحادثة المباشرة داخل شاشة التتبع الفوري."),
        Pair("ماذا أفعل إذا وصل الطلب غير مكتمل؟", "فريق الدعم 24/7 يعوضك فوراً إما بإرسال المندوب مجاناً لاستكمال الصنف أو بإرجاع المبلغ لمحفظتك خلال دقيقة.")
    )

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
                                        prefilledText = "السلام عليكم، أحتاج مساعدة ودعم فني من فريق ألو عزيز."
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

                Spacer(modifier = Modifier.height(12.dp))

                // Hotline Banner
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
                            text = "خط المساعدة المجاني: 0800-ALLO-AZIZ (24h/24)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "متاح الآن 📞",
                            fontSize = 11.sp,
                            color = AzizMint,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Quick Inquiries Chips
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
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
                ChatBubble(message = msg)
            }

            // Expandable FAQ section at bottom of support
            item {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "الأسئلة الشائعة والمساعدة السريعة ❓",
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

        // Bottom Input Row
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

                Spacer(modifier = Modifier.width(8.dp))

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

    if (showWhatsAppDialog) {
        AlertDialog(
            onDismissRequest = { showWhatsAppDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("💬", fontSize = 22.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("دعم ألو عزيز عبر واتساب (WhatsApp 🇲🇦)", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "تواصل فوري ومباشر مع الدعم الفني وإدارة ألو عزيز عبر تطبيق واتساب.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text("رقم هاتف الدعم الفني / الإدارة:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = customPhoneInput,
                        onValueChange = { customPhoneInput = it },
                        placeholder = { Text("مثال: 0612345678 أو 0712345678", fontSize = 12.sp) },
                        modifier = Modifier.fillMaxWidth().testTag("support_custom_whatsapp_input"),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "💡 يمكنك كتابة رقم هاتفك أو رقم الإدارة للتواصل الفوري، وسيتم حفظه مباشرة.",
                        fontSize = 11.sp,
                        color = AzizMint
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
                                prefilledText = "السلام عليكم، أحتاج مساعدة ودعم فني من فريق ألو عزيز."
                            )
                            showWhatsAppDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AzizMint)
                ) {
                    Text("فتح واتساب الآن 🚀", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showWhatsAppDialog = false }) {
                    Text("إلغاء")
                }
            }
        )
    }
}

@Composable
fun ChatBubble(message: SupportChatMessage) {
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
                Text(
                    text = message.agentName,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = AzizOrangePrimary,
                    modifier = Modifier.padding(bottom = 2.dp, start = 4.dp)
                )
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

            Text(
                text = timeStr,
                fontSize = 9.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .padding(horizontal = 4.dp)
                    .padding(top = 2.dp)
            )
        }
    }
}
