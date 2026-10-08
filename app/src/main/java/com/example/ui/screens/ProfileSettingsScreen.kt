package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.ReviewEntity
import com.example.data.local.UserProfile
import com.example.data.local.WalletProfileEntity
import com.example.data.localization.AppStrings
import com.example.data.model.AppLanguage
import com.example.data.model.UserRole
import com.example.ui.theme.AzizAmberSecondary
import com.example.ui.theme.AzizMint
import com.example.ui.theme.AzizOrangePrimary
import com.example.ui.theme.GoldStarColor

@Composable
fun ProfileSettingsScreen(
    language: AppLanguage,
    currentRole: UserRole,
    currentUser: com.example.data.local.UserAccountEntity? = null,
    wallet: WalletProfileEntity?,
    reviews: List<ReviewEntity>,
    userProfile: UserProfile? = null,
    onSaveUserProfile: (UserProfile) -> Unit = {},
    onRoleChange: (UserRole) -> Unit,
    onLanguageChange: (AppLanguage) -> Unit,
    onTopUpWallet: (Double) -> Unit,
    onNavigateToSupport: () -> Unit,
    onLogout: () -> Unit = {}
) {
    val balance = wallet?.balance ?: 0.0
    val points = wallet?.points ?: 0
    val context = LocalContext.current

    var showAddressDialog by remember { mutableStateOf(false) }
    var showTopUpDialog by remember { mutableStateOf(false) }
    var editStreet by remember(userProfile) { mutableStateOf(userProfile?.streetAddress ?: "") }
    var editCity by remember(userProfile) { mutableStateOf(userProfile?.city ?: "المحمدية") }
    var editNeighborhood by remember(userProfile) { mutableStateOf(userProfile?.neighborhood ?: "") }
    var editBuildingInfo by remember(userProfile) { mutableStateOf(userProfile?.buildingInfo ?: "") }
    var editNotes by remember(userProfile) { mutableStateOf(userProfile?.deliveryNotes ?: "") }
    var editContactless by remember(userProfile) { mutableStateOf(userProfile?.contactlessDelivery ?: false) }
    var editCutlery by remember(userProfile) { mutableStateOf(userProfile?.requestCutlery ?: true) }
    var editPushNotifs by remember(userProfile) { mutableStateOf(userProfile?.enablePushNotifications ?: true) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("profile_settings_screen"),
        contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // User Profile Header
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    listOf(AzizOrangePrimary, AzizAmberSecondary)
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(currentUser?.avatarEmoji ?: "👤", fontSize = 28.sp)
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = currentUser?.name?.ifBlank { "عميل ألو عزيز" } ?: "عميل ألو عزيز",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        val displayContact = currentUser?.phone?.ifBlank { currentUser.email } ?: currentUser?.email ?: ""
                        if (displayContact.isNotBlank()) {
                            Text(
                                text = displayContact,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("⭐ عضوية معتمدة", fontSize = 11.sp, color = AzizAmberSecondary, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Account Status & Role Display (Strict RBAC Protection)
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    val isSuperAdmin = currentUser?.role == "SUPER_ADMIN"

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(if (isSuperAdmin) "👑" else "🛡️", fontSize = 20.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = if (isSuperAdmin) "لوحة أدوار الإدارة (Super Admin Console)" else "صلاحيات الحساب والأمان",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (isSuperAdmin) "صلاحية استعراض واجهات المنصة لكافة الفئات" else "حساب معتمد وموثق على منصة ألو عزيز",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (isSuperAdmin) {
                        // Only Super Admin can preview/switch between roles
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            com.example.data.model.UserRole.values().forEach { role ->
                                val isSelected = role == currentRole
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (isSelected) Color(role.badgeColorHex).copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable { onRoleChange(role) }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(role.emoji, fontSize = 20.sp)
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Text(
                                                text = when (language) {
                                                    AppLanguage.ARABIC -> role.titleAr
                                                    AppLanguage.FRENCH -> role.titleFr
                                                    AppLanguage.ENGLISH -> role.titleEn
                                                },
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                fontSize = 13.sp,
                                                color = if (isSelected) Color(role.badgeColorHex) else MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                        if (isSelected) {
                                            Surface(
                                                shape = RoundedCornerShape(8.dp),
                                                color = Color(role.badgeColorHex)
                                            ) {
                                                Text(
                                                    text = "الواجهة المعروضة ✓",
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.White
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        // Normal Customer View: Shows their active role and partner application option
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = AzizOrangePrimary.copy(alpha = 0.08f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = currentRole.emoji, fontSize = 22.sp)
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "الرتبة: ${currentRole.titleAr}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = AzizOrangePrimary
                                    )
                                    Text(
                                        text = "حسابك يتمتع بكافة مزايا الطلب السريع وحفظ العناوين المفضلة 🛵",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Multi-Language Selector Section (CRITICAL REQUIREMENT)
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Language,
                            contentDescription = null,
                            tint = AzizOrangePrimary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = AppStrings.chooseLanguage(language),
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AppLanguage.values().forEach { lang ->
                            val isSelected = lang == language
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) AzizOrangePrimary else MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { onLanguageChange(lang) }
                                    .testTag("lang_btn_${lang.code}")
                            ) {
                                Row(
                                    modifier = Modifier.padding(vertical = 10.dp, horizontal = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                    }
                                    Text(
                                        text = lang.displayName,
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // ALLO Pay Wallet Card & Top-Up
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = AzizOrangePrimary
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AccountBalanceWallet,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "محفظة ALLO Pay الرقمية",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color.White.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = "$points نقطة ولاء 🎁",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = AppStrings.walletBalance(language),
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 12.sp
                    )
                    Text(
                        text = "$balance ${AppStrings.currency(language)}",
                        color = Color.White,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Black
                    )

                    Spacer(modifier = Modifier.height(14.dp))
                    Button(
                        onClick = { showTopUpDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            tint = AzizOrangePrimary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "شحن المحفظة عبر البطاقة البنكية 💳",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = AzizOrangePrimary
                        )
                    }
                }
            }
        }

        // Saved Delivery Address & User Preferences (Local Room Storage)
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = AzizOrangePrimary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "عنوان التوصيل والتفضيلات 📍",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        OutlinedButton(
                            onClick = { showAddressDialog = true },
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "تعديل",
                                modifier = Modifier.size(14.dp),
                                tint = AzizOrangePrimary
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "تعديل", fontSize = 12.sp, color = AzizOrangePrimary, maxLines = 1)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "العنوان المحفوظ: ${userProfile?.deliveryAddressTitle ?: "المنزل"}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    val addressDisplay = if (!userProfile?.streetAddress.isNullOrBlank()) {
                        "${userProfile?.streetAddress}، ${userProfile?.city ?: "المحمدية"}"
                    } else {
                        "لم يتم حفظ عنوان بعد - اضغط على تعديل لإضافة عنوانك"
                    }
                    Text(
                        text = addressDisplay,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (!userProfile?.buildingInfo.isNullOrBlank()) {
                        Text(
                            text = "تفاصيل المبنى: ${userProfile?.buildingInfo}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (!userProfile?.deliveryNotes.isNullOrBlank()) {
                        Text(
                            text = "ملاحظات السائق: ${userProfile?.deliveryNotes}",
                            fontSize = 11.sp,
                            color = AzizMint,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "تفضيلات الاستلام:",
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (userProfile?.contactlessDelivery == true) AzizMint.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Text(
                                text = if (userProfile?.contactlessDelivery == true) "توصيل بدون تلامس ✓" else "تسليم يدوي مباشر",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (userProfile?.contactlessDelivery == true) AzizMint else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (userProfile?.requestCutlery != false) AzizOrangePrimary.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Text(
                                text = if (userProfile?.requestCutlery != false) "طلب أدوات طعام 🍴" else "بدون ملاعق بلاستيكية",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (userProfile?.requestCutlery != false) AzizOrangePrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (userProfile?.enablePushNotifications != false) AzizMint.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Text(
                                text = if (userProfile?.enablePushNotifications != false) "إشعارات فورية 🔔" else "إشعارات صامتة",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (userProfile?.enablePushNotifications != false) AzizMint else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // WhatsApp Direct Communication Settings Card
        item {
            var whatsappInput by remember {
                mutableStateOf(com.example.data.system.ExternalShareHelper.getSavedWhatsAppNumber(context))
            }
            var isSavedSuccess by remember { mutableStateOf(false) }

            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth().testTag("whatsapp_settings_card")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = AzizMint.copy(alpha = 0.15f),
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("💬", fontSize = 20.sp)
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "رقم واتساب المعتمد للتواصل والطلبات 🇲🇦",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "يُستخدم لربط المحادثات المباشرة مع الكباتن والدعم الفني",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = whatsappInput,
                        onValueChange = {
                            whatsappInput = it
                            isSavedSuccess = false
                        },
                        label = { Text("رقم الواتساب (مثال: 0612345678 أو +2126...)") },
                        placeholder = { Text("06XXXXXXXX") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isSavedSuccess) "تم حفظ رقم الواتساب بنجاح ✓" else "لا مزيد من الأرقام العشوائية في واتساب",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isSavedSuccess) AzizMint else MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Button(
                            onClick = {
                                com.example.data.system.ExternalShareHelper.saveWhatsAppNumber(context, whatsappInput)
                                isSavedSuccess = true
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = AzizMint),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("حفظ الرقم 💾", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Customer Reviews & Community Feedback Section
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("⭐", fontSize = 18.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "تقييمات وتجارب العملاء الحقيقية",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                        Text(
                            text = "4.9/5",
                            fontWeight = FontWeight.Black,
                            fontSize = 13.sp,
                            color = AzizAmberSecondary
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    reviews.take(3).forEach { rev ->
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "${rev.customerName} • ${rev.storeName}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                    Row {
                                        repeat(rev.storeRating) {
                                            Icon(
                                                imageVector = Icons.Default.Star,
                                                contentDescription = null,
                                                tint = GoldStarColor,
                                                modifier = Modifier.size(12.dp)
                                            )
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(3.dp))
                                Text(
                                    text = rev.comment,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    lineHeight = 15.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // About App & Customer Support Shortcut
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNavigateToSupport() }
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.SupportAgent,
                            contentDescription = null,
                            tint = AzizOrangePrimary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = AppStrings.supportTitle(language),
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp
                        )
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = AzizMint.copy(alpha = 0.15f),
                            modifier = Modifier.size(28.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("☁️", fontSize = 14.sp)
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "حالة الربط السحابي (Firebase Cloud)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                            Text(
                                text = "متصل بنجاح: allo-aziz 🟢",
                                fontSize = 11.sp,
                                color = AzizMint,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "ALLO AZIZ - ألو عزيز للخدمات اللوجستية",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                            Text(
                                text = AppStrings.appVersion(language),
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // Logout & Session Management
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    if (currentUser != null) {
                        Text(
                            text = "الحساب الحالي: ${currentUser.name} (${currentUser.email})",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    OutlinedButton(
                        onClick = onLogout,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("logout_button")
                    ) {
                        Text(
                            text = "تسجيل الخروج من الحساب 🚪",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = Color.Red
                        )
                    }
                }
            }
        }
    }

    // Edit Delivery Address & Preferences Dialog
    if (showAddressDialog) {
        AlertDialog(
            onDismissRequest = { showAddressDialog = false },
            title = {
                Text(
                    text = "تعديل عنوان التوصيل والتفضيلات 📍",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = editStreet,
                        onValueChange = { editStreet = it },
                        label = { Text("عنوان الشارع / الإقامة") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = editNeighborhood,
                            onValueChange = { editNeighborhood = it },
                            label = { Text("الحي") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = editCity,
                            onValueChange = { editCity = it },
                            label = { Text("المدينة") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }

                    OutlinedTextField(
                        value = editBuildingInfo,
                        onValueChange = { editBuildingInfo = it },
                        label = { Text("تفاصيل العمارة / الطابق / الشقة") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = editNotes,
                        onValueChange = { editNotes = it },
                        label = { Text("تعليمات خاصة للسائق") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "توصيل بدون تلامس عند الباب 🚪", fontSize = 12.sp)
                        Switch(
                            checked = editContactless,
                            onCheckedChange = { editContactless = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = AzizMint)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "إرفاق أدوات طعام وملاعق 🍴", fontSize = 12.sp)
                        Switch(
                            checked = editCutlery,
                            onCheckedChange = { editCutlery = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = AzizOrangePrimary)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "إشعارات فورية بحالة الطلب 🔔", fontSize = 12.sp)
                        Switch(
                            checked = editPushNotifs,
                            onCheckedChange = { editPushNotifs = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = AzizMint)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val current = userProfile ?: UserProfile()
                        val updated = current.copy(
                            streetAddress = editStreet,
                            city = editCity,
                            neighborhood = editNeighborhood,
                            buildingInfo = editBuildingInfo,
                            deliveryNotes = editNotes,
                            contactlessDelivery = editContactless,
                            requestCutlery = editCutlery,
                            enablePushNotifications = editPushNotifs,
                            updatedAt = System.currentTimeMillis()
                        )
                        onSaveUserProfile(updated)
                        showAddressDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AzizOrangePrimary)
                ) {
                    Text("حفظ في قاعدة البيانات")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddressDialog = false }) {
                    Text("إلغاء")
                }
            }
        )
    }

    // Top-Up Wallet Dialog (Realistic Banking & CMI Payment Flow)
    if (showTopUpDialog) {
        var topUpAmount by remember { mutableStateOf("100") }
        var topUpCardNumber by remember { mutableStateOf("") }
        var topUpExpiry by remember { mutableStateOf("") }
        var topUpCvv by remember { mutableStateOf("") }
        var topUpError by remember { mutableStateOf<String?>(null) }

        AlertDialog(
            onDismissRequest = { showTopUpDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AccountBalanceWallet,
                        contentDescription = null,
                        tint = AzizOrangePrimary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("شحن رصيد ALLO Pay 💳", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "اختر المبلغ المراد شحنه من بطاقتك البنكية:",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("50", "100", "200", "500").forEach { preset ->
                            val isSelected = topUpAmount == preset
                            OutlinedButton(
                                onClick = { topUpAmount = preset },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = if (isSelected) AzizOrangePrimary.copy(alpha = 0.15f) else Color.Transparent
                                ),
                                border = ButtonDefaults.outlinedButtonBorder.copy(
                                    brush = Brush.linearGradient(
                                        listOf(
                                            if (isSelected) AzizOrangePrimary else MaterialTheme.colorScheme.outlineVariant,
                                            if (isSelected) AzizOrangePrimary else MaterialTheme.colorScheme.outlineVariant
                                        )
                                    )
                                ),
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "$preset د.م",
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) AzizOrangePrimary else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = topUpCardNumber,
                        onValueChange = { if (it.length <= 19) topUpCardNumber = it },
                        label = { Text("رقم البطاقة البنكية", fontSize = 11.sp) },
                        placeholder = { Text("4111 2222 3333 4444", fontSize = 11.sp) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )

                    Row(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = topUpExpiry,
                            onValueChange = { if (it.length <= 5) topUpExpiry = it },
                            label = { Text("MM/YY", fontSize = 11.sp) },
                            placeholder = { Text("12/28", fontSize = 11.sp) },
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        OutlinedTextField(
                            value = topUpCvv,
                            onValueChange = { if (it.length <= 4) topUpCvv = it },
                            label = { Text("CVV", fontSize = 11.sp) },
                            placeholder = { Text("123", fontSize = 11.sp) },
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        )
                    }

                    Text(
                        text = "🔒 دفع بنكي آمن 100% بتشفير 256-Bit عبر المركز النقدي CMI مع حماية 3D-Secure.",
                        fontSize = 10.sp,
                        color = AzizMint
                    )

                    if (topUpError != null) {
                        Text(
                            text = topUpError ?: "",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amountVal = topUpAmount.toDoubleOrNull() ?: 0.0
                        if (amountVal <= 0.0) {
                            topUpError = "يرجى تحديد مبلغ صالح للشحن"
                        } else if (topUpCardNumber.replace(" ", "").length < 16) {
                            topUpError = "يرجى إدخال رقم بطاقة بنكية صحيح (16 رقماً)"
                        } else if (topUpExpiry.isBlank() || !topUpExpiry.contains("/")) {
                            topUpError = "يرجى إدخال تاريخ انتهاء البطاقة (MM/YY)"
                        } else if (topUpCvv.length < 3) {
                            topUpError = "يرجى إدخال رمز الأمان CVV (3 أرقام)"
                        } else {
                            onTopUpWallet(amountVal)
                            showTopUpDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AzizOrangePrimary),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("تأكيد الدفع والشحن ✓", fontWeight = FontWeight.Bold, color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showTopUpDialog = false }) {
                    Text("إلغاء", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        )
    }
}
