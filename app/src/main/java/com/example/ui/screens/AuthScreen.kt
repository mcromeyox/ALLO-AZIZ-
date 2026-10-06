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
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.TwoWheeler
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
    onRegister: (name: String, email: String, phone: String, pass: String, role: UserRole, restName: String, vType: String, plate: String) -> Unit,
    onClearMessages: () -> Unit,
    onOpenStaffLogin: () -> Unit = {},
    onRequestPasswordReset: ((identifier: String, onOtp: (String) -> Unit) -> Unit)? = null,
    onConfirmPasswordReset: ((identifier: String, newPass: String) -> Unit)? = null
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0 = Sign In, 1 = Register

    // Sign In inputs
    var loginIdentifier by remember { mutableStateOf("") }
    var loginPassword by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }

    // Register inputs
    var regName by remember { mutableStateOf("") }
    var regEmail by remember { mutableStateOf("") }
    var regPhone by remember { mutableStateOf("") }
    var regPassword by remember { mutableStateOf("") }
    var regShowPassword by remember { mutableStateOf(false) }
    var regRole by remember { mutableStateOf(UserRole.CUSTOMER) }
    var regRestaurantName by remember { mutableStateOf("") }
    var regVehicleType by remember { mutableStateOf("دراجة نارية / سكوتر") }
    var regPlateNumber by remember { mutableStateOf("") }

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
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // App Brand Header
        item {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 10.dp, bottom = 8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(22.dp))
                        .background(
                            Brush.linearGradient(
                                colors = listOf(AzizOrangePrimary, AzizAmberSecondary)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "🛵", fontSize = 42.sp)
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "ALLO AZIZ",
                    fontWeight = FontWeight.Black,
                    fontSize = 28.sp,
                    color = AzizOrangePrimary,
                    letterSpacing = 1.sp
                )

                Text(
                    text = "ألو عزيز • سرعة، أمان، وأشهى المأكولات 🇲🇦",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Auth Tabs: تسجيل الدخول / إنشاء حساب
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color.Transparent,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = AzizOrangePrimary,
                            height = 3.dp
                        )
                    }
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = {
                            selectedTab = 0
                            onClearMessages()
                        },
                        text = {
                            Text(
                                text = "تسجيل الدخول",
                                fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 14.sp,
                                color = if (selectedTab == 0) AzizOrangePrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = {
                            selectedTab = 1
                            onClearMessages()
                        },
                        text = {
                            Text(
                                text = "إنشاء حساب جديد",
                                fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 14.sp,
                                color = if (selectedTab == 1) AzizOrangePrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    )
                }
            }
        }

        // Messages & Alerts
        if (pendingApprovalMsg != null) {
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = AzizAmberSecondary.copy(alpha = 0.15f)),
                    border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(AzizAmberSecondary, AzizOrangePrimary))),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = Icons.Default.HourglassTop,
                            contentDescription = null,
                            tint = AzizOrangePrimary,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "طلب الانضمام قيد المراجعة والاعتماد ⏳",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = AzizOrangeDark
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = pendingApprovalMsg,
                                fontSize = 12.sp,
                                lineHeight = 17.sp,
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

        // TAB 0: SIGN IN (Customer Login Only)
        if (selectedTab == 0) {
            // Google Sign-In Button
            item {
                OutlinedButton(
                    onClick = { showGooglePickerDialog = true },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("google_signin_button"),
                    colors = ButtonDefaults.outlinedButtonColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Text(text = "🇬", fontSize = 20.sp)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "تسجيل الدخول بحساب Google (Gmail)",
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
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    HorizontalDivider(modifier = Modifier.weight(1f))
                    Text(
                        text = "أو بالبريد / رقم الهاتف",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 10.dp)
                    )
                    HorizontalDivider(modifier = Modifier.weight(1f))
                }
            }

            // Credentials Fields
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
                        .height(50.dp)
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
                    TextButton(onClick = { selectedTab = 1 }) {
                        Text(text = "إنشاء حساب جديد الآن", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AzizOrangePrimary)
                    }
                }
            }
        }

        // TAB 1: REGISTER
        if (selectedTab == 1) {
            // Role-Specific Notice
            item {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = if (regRole == UserRole.CUSTOMER) AzizMint.copy(alpha = 0.12f) else AzizAmberSecondary.copy(alpha = 0.12f),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = Brush.horizontalGradient(
                            if (regRole == UserRole.CUSTOMER) listOf(AzizMint, AzizOrangePrimary)
                            else listOf(AzizAmberSecondary, AzizOrangePrimary)
                        )
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (regRole == UserRole.CUSTOMER) "⚡" else "🛡️",
                            fontSize = 22.sp
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = if (regRole == UserRole.CUSTOMER) {
                                "تسجيل فوري للزبائن: حسابك يتفعل مباشرة فور التسجيل لتصفح المتاجر وإتمام الطلبات بدون أي انتظار."
                            } else {
                                "طلب انضمام شريك: يتم تدقيق واعتماد طلبات المتاجر والسائقين من قبل إدارة ALLO AZIZ، وسنخبرك فور تفعيل حسابك."
                            },
                            fontSize = 11.sp,
                            lineHeight = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            // Role Picker
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "نوع الحساب المطلوب إنشاؤه:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    val roles = listOf(
                        UserRole.CUSTOMER,
                        UserRole.RESTAURANT_OWNER,
                        UserRole.DRIVER
                    )

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(roles) { r ->
                            val isSelected = regRole == r
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) AzizOrangePrimary else MaterialTheme.colorScheme.surface,
                                shadowElevation = if (isSelected) 3.dp else 1.dp,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { regRole = r }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(text = r.emoji, fontSize = 16.sp)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = r.titleAr,
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

            // Full Name
            item {
                OutlinedTextField(
                    value = regName,
                    onValueChange = { regName = it },
                    label = { Text("الاسم الكامل") },
                    placeholder = { Text("الاسم الشخصي والعائلي") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = AzizOrangePrimary) },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("reg_name_input")
                )
            }

            // Email (Gmail)
            item {
                OutlinedTextField(
                    value = regEmail,
                    onValueChange = { regEmail = it },
                    label = { Text("البريد الإلكتروني (جيميل / Gmail)") },
                    placeholder = { Text("yourname@gmail.com") },
                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = AzizOrangePrimary) },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("reg_email_input")
                )
            }

            // Phone
            item {
                OutlinedTextField(
                    value = regPhone,
                    onValueChange = { regPhone = it },
                    label = { Text("رقم الهاتف المحمول") },
                    placeholder = { Text("+212 6... أو 06...") },
                    leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = AzizOrangePrimary) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("reg_phone_input")
                )
            }

            // Password
            item {
                OutlinedTextField(
                    value = regPassword,
                    onValueChange = { regPassword = it },
                    label = { Text("كلمة المرور") },
                    placeholder = { Text("اختر كلمة مرور قوية") },
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = AzizOrangePrimary) },
                    trailingIcon = {
                        IconButton(onClick = { regShowPassword = !regShowPassword }) {
                            Icon(
                                imageVector = if (regShowPassword) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = "Toggle password"
                            )
                        }
                    },
                    visualTransformation = if (regShowPassword) VisualTransformation.None else PasswordVisualTransformation(),
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("reg_password_input")
                )
            }

            // Extra fields for Restaurant Owner
            if (regRole == UserRole.RESTAURANT_OWNER) {
                item {
                    OutlinedTextField(
                        value = regRestaurantName,
                        onValueChange = { regRestaurantName = it },
                        label = { Text("اسم المطعم أو المتجر") },
                        placeholder = { Text("مثال: شواية الميدان، مطعم فاس") },
                        leadingIcon = { Icon(Icons.Default.Restaurant, contentDescription = null, tint = AzizOrangePrimary) },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // Extra fields for Driver Courier
            if (regRole == UserRole.DRIVER) {
                item {
                    OutlinedTextField(
                        value = regVehicleType,
                        onValueChange = { regVehicleType = it },
                        label = { Text("نوع وسيلة النقل") },
                        placeholder = { Text("سكوتر 150cc / دراجة نارية / سيارة") },
                        leadingIcon = { Icon(Icons.Default.TwoWheeler, contentDescription = null, tint = AzizOrangePrimary) },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    OutlinedTextField(
                        value = regPlateNumber,
                        onValueChange = { regPlateNumber = it },
                        label = { Text("رقم لوحة المركبة (ماتريكول)") },
                        placeholder = { Text("مثال: أ - 49210") },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // Register Submit Button
            item {
                Button(
                    onClick = {
                        onRegister(
                            regName,
                            regEmail,
                            regPhone,
                            regPassword,
                            regRole,
                            regRestaurantName,
                            regVehicleType,
                            regPlateNumber
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AzizOrangePrimary),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("register_submit_button")
                ) {
                    Text(
                        text = if (regRole == UserRole.CUSTOMER) "إنشاء الحساب والبدء الآن 🚀" else "إرسال طلب الانضمام للاعتماد 📝",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color.White
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "لديك حساب مسجل بالفعل؟", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    TextButton(onClick = { selectedTab = 0 }) {
                        Text(text = "تسجيل الدخول", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AzizOrangePrimary)
                    }
                }
            }
        }

        // Discrete Footer for app info and authorized staff portal access (3 taps)
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "ALLO AZIZ v2.0 • Mohammedia, Morocco 🇲🇦",
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

    // Google Account Picker Dialog (Realistic Customer Accounts, No Demo Admin Credentials)
    if (showGooglePickerDialog) {
        AlertDialog(
            onDismissRequest = { showGooglePickerDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("🇬", fontSize = 24.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("المتابعة باستخدام Google", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "اختر حساب Google للمتابعة إلى تطبيق ألو عزيز:",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Karim Tazi (Customer)
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                showGooglePickerDialog = false
                                onLoginGoogle("karim.tazi@gmail.com", "كريم التازي")
                            }
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("👤", fontSize = 20.sp)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("كريم التازي", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text("karim.tazi@gmail.com", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }

                    // Fatima Zahra (Customer)
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                showGooglePickerDialog = false
                                onLoginGoogle("fz.elamrani@gmail.com", "فاطمة الزهراء العمراني")
                            }
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("👤", fontSize = 20.sp)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("فاطمة الزهراء العمراني", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text("fz.elamrani@gmail.com", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }

                    // Mehdi Bennani (Customer)
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                showGooglePickerDialog = false
                                onLoginGoogle("mehdi.bennani@gmail.com", "مهدي بناني")
                            }
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("👤", fontSize = 20.sp)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("مهدي بناني", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text("mehdi.bennani@gmail.com", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showGooglePickerDialog = false }) {
                    Text("إلغاء")
                }
            }
        )
    }

    // Forgot Password Dialog
    if (showForgotPasswordDialog) {
        AlertDialog(
            onDismissRequest = { showForgotPasswordDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Security, contentDescription = null, tint = AzizOrangePrimary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "استعادة كلمة المرور", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    if (forgotMessage != null) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = AzizOrangePrimary.copy(alpha = 0.12f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = forgotMessage!!,
                                modifier = Modifier.padding(10.dp),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = AzizOrangeDark
                            )
                        }
                    }

                    if (!forgotOtpSent) {
                        Text(
                            text = "أدخل بريدك الإلكتروني أو رقم هاتفك لتلقي رمز التحقق واستعادة حسابك:",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        OutlinedTextField(
                            value = forgotIdentifier,
                            onValueChange = { forgotIdentifier = it },
                            label = { Text("البريد الإلكتروني أو رقم الهاتف") },
                            placeholder = { Text("example@gmail.com أو 0612345678") },
                            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = AzizOrangePrimary) },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        Text(
                            text = "تم إرسال رمز التحقق. أدخل الرمز وكلمة المرور الجديدة:",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        OutlinedTextField(
                            value = forgotEnteredOtp,
                            onValueChange = { forgotEnteredOtp = it },
                            label = { Text("رمز التحقق (OTP)") },
                            placeholder = { Text("أدخل الرمز مثل $forgotDevOtpCode") },
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = AzizOrangePrimary) },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = forgotNewPassword,
                            onValueChange = { forgotNewPassword = it },
                            label = { Text("كلمة المرور الجديدة") },
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = AzizOrangePrimary) },
                            visualTransformation = PasswordVisualTransformation(),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            },
            confirmButton = {
                if (!forgotOtpSent) {
                    Button(
                        onClick = {
                            if (forgotIdentifier.isNotBlank()) {
                                if (onRequestPasswordReset != null) {
                                    onRequestPasswordReset(forgotIdentifier) { code ->
                                        forgotDevOtpCode = code
                                        forgotOtpSent = true
                                        forgotMessage = "وضع التطوير: رمز التحقق المرسل هو $code"
                                    }
                                } else {
                                    forgotDevOtpCode = "842910"
                                    forgotOtpSent = true
                                    forgotMessage = "وضع التطوير: رمز التحقق المرسل هو 842910"
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AzizOrangePrimary)
                    ) {
                        Text("إرسال الرمز")
                    }
                } else {
                    Button(
                        onClick = {
                            if (forgotEnteredOtp.trim() == forgotDevOtpCode && forgotNewPassword.isNotBlank()) {
                                onConfirmPasswordReset?.invoke(forgotIdentifier, forgotNewPassword)
                                showForgotPasswordDialog = false
                            } else {
                                forgotMessage = "رمز التحقق غير صحيح، يرجى كتابة $forgotDevOtpCode"
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AzizOrangePrimary)
                    ) {
                        Text("حفظ وتغيير كلمة المرور")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showForgotPasswordDialog = false }) {
                    Text("إلغاء")
                }
            }
        )
    }
}
