package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.TwoWheeler
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppLanguage
import com.example.data.model.UserRole
import com.example.ui.theme.AzizAmberSecondary
import com.example.ui.theme.AzizMint
import com.example.ui.theme.AzizOrangeDark
import com.example.ui.theme.AzizOrangePrimary

@Composable
fun AuthScreen(
    language: AppLanguage,
    authError: String?,
    pendingApprovalMsg: String?,
    authSuccessMsg: String?,
    onLoginCredentials: (identifier: String, pass: String) -> Unit,
    onLoginGoogle: (email: String, name: String) -> Unit,
    onTriggerGoogleSignIn: (onFallback: () -> Unit) -> Unit = { it() },
    onRegister: (
        name: String,
        email: String,
        phone: String,
        pass: String,
        role: UserRole,
        restaurantName: String,
        vehicleType: String,
        plateNumber: String,
        merchantType: String,
        iceNumber: String,
        businessAddress: String,
        cinNumber: String,
        driverLicense: String,
        ribNumber: String
    ) -> Unit,
    onClearMessages: () -> Unit,
    onOpenStaffLogin: () -> Unit = {},
    onRequestPasswordReset: ((identifier: String, onOtp: (String) -> Unit) -> Unit)? = null,
    onConfirmPasswordReset: ((identifier: String, newPass: String) -> Unit)? = null,
    onLoginWithPhone: ((phoneNumber: String, customerName: String) -> Unit)? = null,
    onSkipAsGuest: (() -> Unit)? = null
) {
    // 0 = تسجيل الدخول (Sign In), 1 = إنشاء حساب جديد (Register)
    var selectedMainTab by remember { mutableIntStateOf(0) }

    // Register Category Tab: 0 = زبون (Customer), 1 = شريك متجر/مطعم (Merchant Partner), 2 = سائق (Driver Courier)
    var regCategoryTab by remember { mutableIntStateOf(0) }

    // Role filter for Sign In
    var selectedSignInRole by remember { mutableStateOf<UserRole?>(null) }

    // Sign In inputs
    var loginIdentifier by remember { mutableStateOf("") }
    var loginPassword by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var fastPhoneInput by remember { mutableStateOf("") }

    // Customer Registration inputs
    var regCustName by remember { mutableStateOf("") }
    var regCustPhone by remember { mutableStateOf("") }
    var regCustEmail by remember { mutableStateOf("") }
    var regCustPassword by remember { mutableStateOf("") }
    var regCustShowPassword by remember { mutableStateOf(false) }

    // Business Partner (Restaurant & Store) Registration inputs
    var regMerchantType by remember { mutableStateOf("FOOD") } // FOOD, ECOMMERCE, GROCERY, PHARMACY
    var regBusinessStoreName by remember { mutableStateOf("") }
    var regBusinessOwnerName by remember { mutableStateOf("") }
    var regBusinessIce by remember { mutableStateOf("") }
    var regBusinessPhone by remember { mutableStateOf("") }
    var regBusinessEmail by remember { mutableStateOf("") }
    var regBusinessAddress by remember { mutableStateOf("") }
    var regBusinessPassword by remember { mutableStateOf("") }
    var regBusinessShowPassword by remember { mutableStateOf(false) }
    var regBusinessCin by remember { mutableStateOf("") }
    var regBusinessRc by remember { mutableStateOf("") }
    var regBusinessRib by remember { mutableStateOf("") }
    var regBusinessHygieneCheck by remember { mutableStateOf(true) }

    // Driver Courier Registration inputs
    var regDriverName by remember { mutableStateOf("") }
    var regDriverPhone by remember { mutableStateOf("") }
    var regDriverEmail by remember { mutableStateOf("") }
    var regDriverPassword by remember { mutableStateOf("") }
    var regDriverShowPassword by remember { mutableStateOf(false) }
    var regDriverVehicleType by remember { mutableStateOf("دراجة نارية / سكوتر") }
    var regDriverPlate by remember { mutableStateOf("") }
    var regDriverPermis by remember { mutableStateOf("") }
    var regDriverCin by remember { mutableStateOf("") }
    var regDriverZone by remember { mutableStateOf("المحمدية وسواحلها") }

    // Google Account Picker Dialog
    var showGooglePickerDialog by remember { mutableStateOf(false) }

    // Forgot Password Dialog
    var showForgotPasswordDialog by remember { mutableStateOf(false) }
    var forgotIdentifier by remember { mutableStateOf("") }
    var forgotOtpSent by remember { mutableStateOf(false) }
    var forgotDevOtpCode by remember { mutableStateOf("") }
    var forgotEnteredOtp by remember { mutableStateOf("") }
    var forgotNewPassword by remember { mutableStateOf("") }
    var forgotMessage by remember { mutableStateOf<String?>(null) }
    var staffTapCount by remember { mutableIntStateOf(0) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("auth_screen_container"),
        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // App Brand Header
        item {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            Brush.linearGradient(
                                colors = listOf(AzizOrangePrimary, AzizAmberSecondary)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "🛵", fontSize = 38.sp)
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "DELIVIO",
                    fontWeight = FontWeight.Black,
                    fontSize = 28.sp,
                    color = AzizOrangePrimary,
                    letterSpacing = 1.2.sp
                )

                Text(
                    text = "المنصة الذكية الموحدة للزبائن والشركاء والسائقين 🇲🇦",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Quick Guest Browsing Button (For Instant Shopping without account)
        if (onSkipAsGuest != null) {
            item {
                Surface(
                    onClick = { onSkipAsGuest() },
                    shape = RoundedCornerShape(14.dp),
                    color = AzizMint.copy(alpha = 0.12f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, AzizMint.copy(alpha = 0.35f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("skip_as_guest_top_btn")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = when (language) {
                                AppLanguage.FRENCH -> "Continuer sans compte (Voir les produits) 🛍️ →"
                                AppLanguage.ENGLISH -> "Continue as Guest (Browse Products) 🛍️ →"
                                AppLanguage.ARABIC -> "تصفح كزائر والاطلاع على المتاجر والمنتجات مباشرة 🛍️ ←"
                            },
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = AzizMint
                        )
                    }
                }
            }
        }

        // Main Mode TabRow (Sign In vs Register)
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                TabRow(
                    selectedTabIndex = selectedMainTab,
                    containerColor = Color.Transparent,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedMainTab]),
                            color = AzizOrangePrimary,
                            height = 3.dp
                        )
                    }
                ) {
                    Tab(
                        selected = selectedMainTab == 0,
                        onClick = {
                            selectedMainTab = 0
                            onClearMessages()
                        },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("🔑", fontSize = 14.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "تسجيل الدخول",
                                    fontWeight = if (selectedMainTab == 0) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 13.sp,
                                    color = if (selectedMainTab == 0) AzizOrangePrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    )
                    Tab(
                        selected = selectedMainTab == 1,
                        onClick = {
                            selectedMainTab = 1
                            onClearMessages()
                        },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("📝", fontSize = 14.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "إنشاء حساب جديد",
                                    fontWeight = if (selectedMainTab == 1) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 13.sp,
                                    color = if (selectedMainTab == 1) AzizOrangePrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    )
                }
            }
        }

        // Messages & System Notifications
        if (pendingApprovalMsg != null) {
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = AzizAmberSecondary.copy(alpha = 0.15f)),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = Brush.horizontalGradient(listOf(AzizAmberSecondary, AzizOrangePrimary))
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = Icons.Default.HourglassTop,
                            contentDescription = null,
                            tint = AzizOrangePrimary,
                            modifier = Modifier.size(26.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "طلب الانضمام قيد المراجعة والاعتماد ⏳",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = AzizOrangeDark
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = pendingApprovalMsg,
                                fontSize = 11.sp,
                                lineHeight = 16.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }

        if (authError != null) {
            item {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color.Red.copy(alpha = 0.1f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.Info, contentDescription = null, tint = Color.Red, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = authError, fontSize = 12.sp, color = Color.Red, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }

        if (authSuccessMsg != null) {
            item {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = AzizMint.copy(alpha = 0.12f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = AzizMint, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = authSuccessMsg, fontSize = 12.sp, color = AzizMint, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // ==========================================
        // TAB 0: SIGN IN (تسجيل الدخول لجميع الأدوار)
        // ==========================================
        if (selectedMainTab == 0) {
            // Role Quick Filter Row (الزبائن، أصحاب المتاجر والمطاعم، السائقين)
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "تسجيل الدخول المباشر حسب دورك:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    val roleFilters = listOf(
                        null to "الكل / زبائن 🛍️",
                        UserRole.CUSTOMER to "زبون 🛍️",
                        UserRole.RESTAURANT_OWNER to "مطعم أو متجر 🏪",
                        UserRole.DRIVER to "سائق توصيل 🛵"
                    )

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(roleFilters) { (role, label) ->
                            val isSelected = selectedSignInRole == role
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) AzizOrangePrimary else MaterialTheme.colorScheme.surface,
                                shadowElevation = if (isSelected) 2.dp else 1.dp,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable { selectedSignInRole = role }
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }
            }

            // 1-Click Fast Phone Login Card (برقم الهاتف فقط - دون كلمة سر أو جيميل)
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = AzizMint.copy(alpha = 0.08f)),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = Brush.horizontalGradient(listOf(AzizMint, AzizOrangePrimary))
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("📱", fontSize = 20.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "دخول فوري برقم الهاتف فقط (دون جيميل أو كلمة سر)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = AzizOrangeDark
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "أدخل رقم هاتفك فقط، وسيتعرف عليك التطبيق فوراً أو ينشئ حسابك تلقائياً كزبون بدون أي تعقيد.",
                            fontSize = 11.sp,
                            lineHeight = 15.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedTextField(
                            value = fastPhoneInput,
                            onValueChange = { fastPhoneInput = it },
                            label = { Text("رقم الهاتف المحمول (مثال: 0612345678)") },
                            placeholder = { Text("06... أو 07...") },
                            leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = AzizMint) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("fast_phone_login_input")
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = {
                                if (onLoginWithPhone != null) {
                                    onLoginWithPhone(fastPhoneInput, "")
                                } else {
                                    onLoginCredentials(fastPhoneInput, "phone_verified")
                                }
                            },
                            enabled = fastPhoneInput.trim().length >= 8,
                            colors = ButtonDefaults.buttonColors(containerColor = AzizMint),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .testTag("fast_phone_login_btn")
                        ) {
                            Text(
                                text = "دخول فوري برقم الهاتف فقط 🚀",
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    HorizontalDivider(modifier = Modifier.weight(1f))
                    Text(
                        text = "أو عبر خيارات الدخول الأخرى",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 10.dp)
                    )
                    HorizontalDivider(modifier = Modifier.weight(1f))
                }
            }

            // Google Sign-In Button (حساب جوجل / جيميل)
            item {
                OutlinedButton(
                    onClick = {
                        onTriggerGoogleSignIn {
                            showGooglePickerDialog = true
                        }
                    },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("google_signin_button"),
                    colors = ButtonDefaults.outlinedButtonColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Text(text = "🇬", fontSize = 18.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "تسجيل الدخول بحساب Google (Gmail)",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // Standard Credentials Fields (بريد أو هاتف + كلمة المرور)
            item {
                OutlinedTextField(
                    value = loginIdentifier,
                    onValueChange = { loginIdentifier = it },
                    label = { Text("البريد الإلكتروني (جيميل) أو رقم الهاتف") },
                    placeholder = { Text("your.email@gmail.com أو 0612345678") },
                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = AzizOrangePrimary) },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("login_identifier_input")
                )
            }

            item {
                OutlinedTextField(
                    value = loginPassword,
                    onValueChange = { loginPassword = it },
                    label = { Text("كلمة المرور") },
                    placeholder = { Text("أدخل كلمة المرور") },
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = AzizOrangePrimary) },
                    trailingIcon = {
                        IconButton(onClick = { showPassword = !showPassword }) {
                            Icon(
                                imageVector = if (showPassword) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = "Toggle password visibility"
                            )
                        }
                    },
                    visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("login_password_input")
                )
            }

            // Forgot Password Link
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = {
                            forgotIdentifier = loginIdentifier
                            forgotOtpSent = false
                            forgotDevOtpCode = ""
                            forgotEnteredOtp = ""
                            forgotNewPassword = ""
                            forgotMessage = null
                            showForgotPasswordDialog = true
                        },
                        modifier = Modifier.testTag("forgot_password_button")
                    ) {
                        Text(
                            text = "نسيت كلمة المرور؟",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = AzizOrangePrimary
                        )
                    }
                }
            }

            item {
                Button(
                    onClick = { onLoginCredentials(loginIdentifier, loginPassword) },
                    colors = ButtonDefaults.buttonColors(containerColor = AzizOrangePrimary),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("login_submit_button")
                ) {
                    Text(text = "تسجيل الدخول", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color.White)
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "ليس لديك حساب بعد؟", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    TextButton(onClick = { selectedMainTab = 1 }) {
                        Text(text = "إنشاء حساب جديد الآن", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AzizOrangePrimary)
                    }
                }
            }
        }

        // ==========================================
        // TAB 1: REGISTER (إنشاء حساب مقسم إلى 3 خانات واضحة)
        // ==========================================
        if (selectedMainTab == 1) {
            // Category Switcher: 3 Dedicated Tabs
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "اختر فئة الحساب المطلوبة للتسجيل:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // 1. زبون
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (regCategoryTab == 0) AzizOrangePrimary else MaterialTheme.colorScheme.surface,
                            shadowElevation = if (regCategoryTab == 0) 3.dp else 1.dp,
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { regCategoryTab = 0 }
                        ) {
                            Column(
                                modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("🛍️", fontSize = 18.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "زبون",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (regCategoryTab == 0) Color.White else MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "تفعيل فوري",
                                    fontSize = 9.sp,
                                    color = if (regCategoryTab == 0) Color.White.copy(alpha = 0.85f) else AzizMint
                                )
                            }
                        }

                        // 2. شريك تجاري
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (regCategoryTab == 1) AzizOrangePrimary else MaterialTheme.colorScheme.surface,
                            shadowElevation = if (regCategoryTab == 1) 3.dp else 1.dp,
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { regCategoryTab = 1 }
                        ) {
                            Column(
                                modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("🏪", fontSize = 18.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "شريك متجر/مطعم",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center,
                                    color = if (regCategoryTab == 1) Color.White else MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "اعتماد إداري",
                                    fontSize = 9.sp,
                                    color = if (regCategoryTab == 1) Color.White.copy(alpha = 0.85f) else AzizOrangeDark
                                )
                            }
                        }

                        // 3. سائق
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (regCategoryTab == 2) AzizOrangePrimary else MaterialTheme.colorScheme.surface,
                            shadowElevation = if (regCategoryTab == 2) 3.dp else 1.dp,
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { regCategoryTab = 2 }
                        ) {
                            Column(
                                modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("🛵", fontSize = 18.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "سائق توصيل",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (regCategoryTab == 2) Color.White else MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "اعتماد إداري",
                                    fontSize = 9.sp,
                                    color = if (regCategoryTab == 2) Color.White.copy(alpha = 0.85f) else AzizOrangeDark
                                )
                            }
                        }
                    }
                }
            }

            // ==========================================
            // SUB-SECTION 1: تسجيل الزبون (Customer - تفعيل فوري)
            // ==========================================
            if (regCategoryTab == 0) {
                item {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = AzizMint.copy(alpha = 0.12f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, AzizMint.copy(alpha = 0.35f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("⚡", fontSize = 22.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "تفعيل فوري 100% للزبائن بدون أي انتظار",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = AzizMint
                                )
                                Text(
                                    text = "حسابك يتفعل مباشرة فور التسجيل لتصفح المتاجر والطلب وتتبع الكابتن فوراً.",
                                    fontSize = 11.sp,
                                    lineHeight = 15.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }

                // Quick Google Registration
                item {
                    OutlinedButton(
                        onClick = {
                            onTriggerGoogleSignIn {
                                showGooglePickerDialog = true
                            }
                        },
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        colors = ButtonDefaults.outlinedButtonColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Text(text = "🇬", fontSize = 18.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "تسجيل فوري بحساب Google (Gmail)",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        HorizontalDivider(modifier = Modifier.weight(1f))
                        Text(
                            text = "أو أدخل بياناتك للتسجيل المباشر",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                        HorizontalDivider(modifier = Modifier.weight(1f))
                    }
                }

                item {
                    OutlinedTextField(
                        value = regCustName,
                        onValueChange = { regCustName = it },
                        label = { Text("الاسم الكامل") },
                        placeholder = { Text("الاسم الشخصي والعائلي") },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = AzizOrangePrimary) },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth().testTag("reg_cust_name_input")
                    )
                }

                item {
                    OutlinedTextField(
                        value = regCustPhone,
                        onValueChange = { regCustPhone = it },
                        label = { Text("رقم الهاتف المحمول (مطلوب)") },
                        placeholder = { Text("06... أو 07...") },
                        leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = AzizOrangePrimary) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth().testTag("reg_cust_phone_input")
                    )
                }

                item {
                    OutlinedTextField(
                        value = regCustEmail,
                        onValueChange = { regCustEmail = it },
                        label = { Text("البريد الإلكتروني (اختياري إذا أدخلت الهاتف)") },
                        placeholder = { Text("اختياري: yourname@gmail.com") },
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = AzizOrangePrimary) },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth().testTag("reg_cust_email_input")
                    )
                }

                item {
                    OutlinedTextField(
                        value = regCustPassword,
                        onValueChange = { regCustPassword = it },
                        label = { Text("كلمة المرور") },
                        placeholder = { Text("اختر كلمة مرور") },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = AzizOrangePrimary) },
                        trailingIcon = {
                            IconButton(onClick = { regCustShowPassword = !regCustShowPassword }) {
                                Icon(
                                    imageVector = if (regCustShowPassword) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = "Toggle password"
                                )
                            }
                        },
                        visualTransformation = if (regCustShowPassword) VisualTransformation.None else PasswordVisualTransformation(),
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth().testTag("reg_cust_password_input")
                    )
                }

                item {
                    Button(
                        onClick = {
                            onRegister(
                                regCustName,
                                regCustEmail,
                                regCustPhone,
                                regCustPassword,
                                UserRole.CUSTOMER,
                                "",
                                "",
                                "",
                                "FOOD",
                                "",
                                "",
                                "",
                                "",
                                ""
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AzizOrangePrimary),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("register_customer_button")
                    ) {
                        Text(
                            text = "إنشاء حساب زبون والبدء بالطلب الآن 🚀",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color.White
                        )
                    }
                }
            }

            // ==========================================
            // SUB-SECTION 2: تسجيل شريك تجاري (مطعم أو متجر - يتطلب موافقة المدير)
            // ==========================================
            if (regCategoryTab == 1) {
                item {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = AzizAmberSecondary.copy(alpha = 0.15f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, AzizOrangePrimary.copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("🛡️", fontSize = 22.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "يتطلب موافقة المدير العام أو الدعم الفني",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = AzizOrangeDark
                                )
                                Text(
                                    text = "تخضع حسابات المتاجر والمطاعم لتدقيق الوثائق الرسمية، ولا يتم التفعيل إلا بعد اعتماد الإدارة لضمان الجودة والأمان.",
                                    fontSize = 11.sp,
                                    lineHeight = 15.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }

                // Merchant Type Picker
                item {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text("نوع النشاط التجاري:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(6.dp))

                        val mTypes = listOf(
                            "FOOD" to "🍽️ مطعم أو مقهى",
                            "ECOMMERCE" to "🛍️ متجر أزياء وتكنولوجيا",
                            "GROCERY" to "🧺 بقالة وسوبرماركت",
                            "PHARMACY" to "💊 صيدلية ومنتجات صحية"
                        )

                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(mTypes) { (type, label) ->
                                val isSel = regMerchantType == type
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (isSel) AzizOrangePrimary else MaterialTheme.colorScheme.surface,
                                    shadowElevation = if (isSel) 2.dp else 1.dp,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .clickable { regMerchantType = type }
                                ) {
                                    Text(
                                        text = label,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSel) Color.White else MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                item {
                    OutlinedTextField(
                        value = regBusinessStoreName,
                        onValueChange = { regBusinessStoreName = it },
                        label = { Text("اسم المتجر أو المطعم التجاري") },
                        placeholder = { Text("مثال: مطعم شواية فاس / بوتيك كازابلانكا") },
                        leadingIcon = { Icon(Icons.Default.Store, contentDescription = null, tint = AzizOrangePrimary) },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    OutlinedTextField(
                        value = regBusinessOwnerName,
                        onValueChange = { regBusinessOwnerName = it },
                        label = { Text("اسم المالك أو المسير القانوني") },
                        placeholder = { Text("الاسم الكامل للمسير") },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = AzizOrangePrimary) },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    OutlinedTextField(
                        value = regBusinessPhone,
                        onValueChange = { regBusinessPhone = it },
                        label = { Text("رقم هاتف المنشأة للتواصل والطلبات") },
                        placeholder = { Text("06... أو 05...") },
                        leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = AzizOrangePrimary) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    OutlinedTextField(
                        value = regBusinessEmail,
                        onValueChange = { regBusinessEmail = it },
                        label = { Text("البريد الإلكتروني للنشاط (جيميل / بريد مهني)") },
                        placeholder = { Text("partner@store.com أو جيميل") },
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = AzizOrangePrimary) },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    OutlinedTextField(
                        value = regBusinessAddress,
                        onValueChange = { regBusinessAddress = it },
                        label = { Text("عنوان المقر والمدينة") },
                        placeholder = { Text("مثال: شارع الحسن الثاني، المحمدية") },
                        leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null, tint = AzizOrangePrimary) },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    OutlinedTextField(
                        value = regBusinessPassword,
                        onValueChange = { regBusinessPassword = it },
                        label = { Text("كلمة المرور لتسجيل الدخول للوحة التحكم") },
                        placeholder = { Text("اختر كلمة مرور آمنة") },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = AzizOrangePrimary) },
                        trailingIcon = {
                            IconButton(onClick = { regBusinessShowPassword = !regBusinessShowPassword }) {
                                Icon(
                                    imageVector = if (regBusinessShowPassword) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = "Toggle password"
                                )
                            }
                        },
                        visualTransformation = if (regBusinessShowPassword) VisualTransformation.None else PasswordVisualTransformation(),
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Official KYC & Documentation Section
                item {
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Shield, contentDescription = null, tint = AzizOrangePrimary, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("ملف التوثيق الرسمي للشركاء (KYC)", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }

                            OutlinedTextField(
                                value = regBusinessIce,
                                onValueChange = { regBusinessIce = it },
                                label = { Text("رقم التعريف الموحد (ICE) أو السجل التجاري (RC)") },
                                placeholder = { Text("مثال: 002847193000045") },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            )

                            OutlinedTextField(
                                value = regBusinessCin,
                                onValueChange = { regBusinessCin = it },
                                label = { Text("رقم بطاقة التعريف الوطنية للمسير (CIN)") },
                                placeholder = { Text("مثال: BE123456") },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            )

                            OutlinedTextField(
                                value = regBusinessRib,
                                onValueChange = { regBusinessRib = it },
                                label = { Text("رقم الحساب البنكي (RIB - 24 رقم لتحويل المبيعات)") },
                                placeholder = { Text("24 رقم بنكي مغربي") },
                                leadingIcon = { Icon(Icons.Default.AccountBalance, contentDescription = null, tint = AzizMint) },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            )

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth().clickable { regBusinessHygieneCheck = !regBusinessHygieneCheck }
                            ) {
                                Checkbox(
                                    checked = regBusinessHygieneCheck,
                                    onCheckedChange = { regBusinessHygieneCheck = it },
                                    colors = CheckboxDefaults.colors(checkedColor = AzizMint)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "أقر باستيفاء شروط السلامة والنظافة الصحية والتراخيص التجارية المعمول بها بالمملكة المغربية ✓",
                                    fontSize = 10.sp,
                                    lineHeight = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }

                item {
                    Button(
                        onClick = {
                            onRegister(
                                regBusinessOwnerName.ifBlank { regBusinessStoreName },
                                regBusinessEmail,
                                regBusinessPhone,
                                regBusinessPassword,
                                UserRole.RESTAURANT_OWNER,
                                regBusinessStoreName,
                                "",
                                "",
                                regMerchantType,
                                regBusinessIce,
                                regBusinessAddress,
                                regBusinessCin,
                                regBusinessRc,
                                regBusinessRib
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AzizOrangePrimary),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("register_partner_button")
                    ) {
                        Text(
                            text = "إرسال ملف الشريك للاعتماد والمراجعة الإدارية 📄",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = Color.White
                        )
                    }
                }
            }

            // ==========================================
            // SUB-SECTION 3: تسجيل سائق توصيل (Driver - يتطلب موافقة المدير)
            // ==========================================
            if (regCategoryTab == 2) {
                item {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = AzizAmberSecondary.copy(alpha = 0.15f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, AzizOrangePrimary.copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("🛡️", fontSize = 22.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "يتطلب تدقيق واعتماد الإدارة أو الدعم الفني",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = AzizOrangeDark
                                )
                                Text(
                                    text = "يخضع انضمام السائقين لمطابقة رخصة السياقة وأوراق المركبة، ولا يتفعل الحساب للعمل إلا بعد موافقة الإدارة.",
                                    fontSize = 11.sp,
                                    lineHeight = 15.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }

                // Vehicle Type Picker
                item {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text("نوع وسيلة النقل للتوصيل:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(6.dp))

                        val vTypes = listOf(
                            "دراجة نارية / سكوتر 150cc",
                            "دراجة هوائية / كهربائية",
                            "سيارة خفيفة للتوصيل"
                        )

                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(vTypes) { v ->
                                val isV = regDriverVehicleType == v
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (isV) AzizOrangePrimary else MaterialTheme.colorScheme.surface,
                                    shadowElevation = if (isV) 2.dp else 1.dp,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .clickable { regDriverVehicleType = v }
                                ) {
                                    Text(
                                        text = v,
                                        fontSize = 11.sp,
                                        fontWeight = if (isV) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isV) Color.White else MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                item {
                    OutlinedTextField(
                        value = regDriverName,
                        onValueChange = { regDriverName = it },
                        label = { Text("الاسم الكامل للكابتن / السائق") },
                        placeholder = { Text("الاسم الشخصي والعائلي") },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = AzizOrangePrimary) },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    OutlinedTextField(
                        value = regDriverPhone,
                        onValueChange = { regDriverPhone = it },
                        label = { Text("رقم الهاتف المحمول (الواتساب للتواصل)") },
                        placeholder = { Text("06... أو 07...") },
                        leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = AzizOrangePrimary) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    OutlinedTextField(
                        value = regDriverEmail,
                        onValueChange = { regDriverEmail = it },
                        label = { Text("البريد الإلكتروني (جيميل)") },
                        placeholder = { Text("driver@gmail.com") },
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = AzizOrangePrimary) },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    OutlinedTextField(
                        value = regDriverPlate,
                        onValueChange = { regDriverPlate = it },
                        label = { Text("رقم لوحة المركبة (الماتريكول)") },
                        placeholder = { Text("مثال: أ - 49210") },
                        leadingIcon = { Icon(Icons.Default.TwoWheeler, contentDescription = null, tint = AzizOrangePrimary) },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    OutlinedTextField(
                        value = regDriverPermis,
                        onValueChange = { regDriverPermis = it },
                        label = { Text("رقم رخصة السياقة (Permis de conduire)") },
                        placeholder = { Text("مثال: 08/123456") },
                        leadingIcon = { Icon(Icons.Default.Badge, contentDescription = null, tint = AzizOrangePrimary) },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    OutlinedTextField(
                        value = regDriverCin,
                        onValueChange = { regDriverCin = it },
                        label = { Text("رقم البطاقة الوطنية (CIN)") },
                        placeholder = { Text("مثال: T123456") },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    OutlinedTextField(
                        value = regDriverPassword,
                        onValueChange = { regDriverPassword = it },
                        label = { Text("كلمة المرور لحساب السائق") },
                        placeholder = { Text("اختر كلمة مرور") },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = AzizOrangePrimary) },
                        trailingIcon = {
                            IconButton(onClick = { regDriverShowPassword = !regDriverShowPassword }) {
                                Icon(
                                    imageVector = if (regDriverShowPassword) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = "Toggle password"
                                )
                            }
                        },
                        visualTransformation = if (regDriverShowPassword) VisualTransformation.None else PasswordVisualTransformation(),
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    Button(
                        onClick = {
                            onRegister(
                                regDriverName,
                                regDriverEmail,
                                regDriverPhone,
                                regDriverPassword,
                                UserRole.DRIVER,
                                "",
                                regDriverVehicleType,
                                regDriverPlate,
                                "DRIVER",
                                "",
                                regDriverZone,
                                regDriverCin,
                                regDriverPermis,
                                ""
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AzizOrangePrimary),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("register_driver_button")
                    ) {
                        Text(
                            text = "إرسال ملف السائق للاعتماد والتشغيل 📝",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = Color.White
                        )
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "لديك حساب مسجل بالفعل؟", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    TextButton(onClick = { selectedMainTab = 0 }) {
                        Text(text = "تسجيل الدخول", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AzizOrangePrimary)
                    }
                }
            }
        }

        // Footer & Secret Portal for Management / Staff (3 taps)
        item {
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "DELIVIO Super-App v3.2 • Super Admin & Staff Portal 🇲🇦",
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .clickable {
                        staffTapCount++
                        if (staffTapCount >= 3) {
                            staffTapCount = 0
                            onOpenStaffLogin()
                        }
                    }
                    .testTag("app_version_footer")
            )
        }
    }

    // Google Sign-In Dialog
    if (showGooglePickerDialog) {
        var googleEmailInput by remember { mutableStateOf("") }
        var googleNameInput by remember { mutableStateOf("") }
        var googleError by remember { mutableStateOf<String?>(null) }

        AlertDialog(
            onDismissRequest = { showGooglePickerDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                            .border(1.dp, Color(0xFFE0E0E0), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("G", fontWeight = FontWeight.Black, fontSize = 20.sp, color = Color(0xFF4285F4))
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("المتابعة باستخدام Google", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "تسجيل دخول فوري وآمن عبر حسابك في Google. أدخل عنوان Gmail واسمك الشخصي للمتابعة والتفعيل الفوري:",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    OutlinedTextField(
                        value = googleEmailInput,
                        onValueChange = {
                            googleEmailInput = it
                            googleError = null
                        },
                        label = { Text("بريدك الإلكتروني (Gmail)", fontSize = 12.sp) },
                        placeholder = { Text("مثال: username@gmail.com", fontSize = 12.sp) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        leadingIcon = {
                            Icon(Icons.Default.Email, contentDescription = null, tint = AzizOrangePrimary)
                        }
                    )

                    OutlinedTextField(
                        value = googleNameInput,
                        onValueChange = {
                            googleNameInput = it
                            googleError = null
                        },
                        label = { Text("الاسم الكامل", fontSize = 12.sp) },
                        placeholder = { Text("اسمك كما ترغب أن يظهر في الحساب", fontSize = 12.sp) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        leadingIcon = {
                            Icon(Icons.Default.Person, contentDescription = null, tint = AzizOrangePrimary)
                        }
                    )

                    if (googleError != null) {
                        Text(
                            text = googleError ?: "",
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val trimmed = googleEmailInput.trim()
                        if (trimmed.isBlank() || !trimmed.contains("@")) {
                            googleError = "يرجى إدخال عنوان بريد إلكتروني صحيح."
                        } else {
                            showGooglePickerDialog = false
                            onLoginGoogle(
                                trimmed,
                                googleNameInput.trim().ifBlank { trimmed.substringBefore("@") }
                            )
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AzizOrangePrimary),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("متابعة الدخول والتفعيل", fontWeight = FontWeight.Bold, color = Color.White)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showGooglePickerDialog = false },
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("إلغاء")
                }
            }
        )
    }

    // Password Reset Dialog
    if (showForgotPasswordDialog) {
        AlertDialog(
            onDismissRequest = { showForgotPasswordDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = AzizOrangePrimary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("استعادة كلمة المرور", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "أدخل بريدك الإلكتروني أو رقم هاتفك لإرسال رمز التأكيد (OTP):",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    OutlinedTextField(
                        value = forgotIdentifier,
                        onValueChange = { forgotIdentifier = it },
                        label = { Text("البريد أو رقم الهاتف", fontSize = 12.sp) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    if (!forgotOtpSent) {
                        Button(
                            onClick = {
                                if (forgotIdentifier.trim().isNotBlank() && onRequestPasswordReset != null) {
                                    onRequestPasswordReset(forgotIdentifier.trim()) { code ->
                                        forgotDevOtpCode = code
                                        forgotOtpSent = true
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = AzizOrangePrimary),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("إرسال رمز التأكيد OTP", color = Color.White)
                        }
                    }

                    if (forgotOtpSent) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = AzizMint.copy(alpha = 0.12f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = "تم إرسال رمز التحقق بنجاح ✓",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AzizMint
                                )
                                Text(
                                    text = "رمز التأكيد التجريبي للاختبار السريع: $forgotDevOtpCode",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = AzizOrangeDark
                                )
                            }
                        }

                        OutlinedTextField(
                            value = forgotEnteredOtp,
                            onValueChange = { forgotEnteredOtp = it },
                            label = { Text("رمز التحقق (OTP)", fontSize = 12.sp) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )

                        OutlinedTextField(
                            value = forgotNewPassword,
                            onValueChange = { forgotNewPassword = it },
                            label = { Text("كلمة المرور الجديدة", fontSize = 12.sp) },
                            singleLine = true,
                            visualTransformation = PasswordVisualTransformation(),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }

                    if (forgotMessage != null) {
                        Text(
                            text = forgotMessage ?: "",
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 11.sp
                        )
                    }
                }
            },
            confirmButton = {
                if (forgotOtpSent) {
                    Button(
                        onClick = {
                            if (forgotEnteredOtp.trim() == forgotDevOtpCode && forgotNewPassword.trim().isNotBlank()) {
                                onConfirmPasswordReset?.invoke(forgotIdentifier.trim(), forgotNewPassword.trim())
                                showForgotPasswordDialog = false
                            } else {
                                forgotMessage = "رمز التحقق غير صحيح أو كلمة المرور فارغة."
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AzizOrangePrimary),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("تعيين كلمة المرور الجديدة", color = Color.White)
                    }
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showForgotPasswordDialog = false },
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("إغلاق")
                }
            }
        )
    }
}
