package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.StoreEntity
import com.example.data.local.UserAccountEntity
import com.example.data.model.AppLanguage
import com.example.ui.theme.AzizAmberSecondary
import com.example.ui.theme.AzizMint
import com.example.ui.theme.AzizOrangePrimary

@Composable
fun SuperAdminDashboard(
    language: AppLanguage,
    users: List<UserAccountEntity>,
    stores: List<StoreEntity>,
    onUpdateUserStatus: (userId: String, status: String) -> Unit,
    onUpdateUserRole: (userId: String, role: String) -> Unit,
    onAddNewStore: (name: String, nameAr: String, category: String, address: String, fee: Double, emoji: String) -> Unit,
    onBroadcastAnnouncement: (title: String, body: String) -> Unit,
    deliveryZones: List<com.example.ui.viewmodel.DeliveryZone> = emptyList(),
    merchantCommissionPercent: Double = 12.0,
    driverPayoutPercent: Double = 80.0,
    platformServiceFee: Double = 2.5,
    onUpdateCommissionRates: (Double, Double, Double) -> Unit = { _, _, _ -> },
    onToggleDeliveryZone: (String) -> Unit = {}
) {
    var selectedRoleFilter by remember { mutableIntStateOf(0) } // 0 = All, 1 = Drivers, 2 = Restaurant, 3 = Customers
    var showAddStoreModal by remember { mutableStateOf(false) }
    var showBroadcastModal by remember { mutableStateOf(false) }

    var newStoreName by remember { mutableStateOf("") }
    var newStoreCategory by remember { mutableStateOf("burger") }
    var newStoreAddress by remember { mutableStateOf("") }
    var newStoreFee by remember { mutableStateOf("8.0") }
    var newStoreEmoji by remember { mutableStateOf("🏬") }

    var broadcastTitle by remember { mutableStateOf("") }
    var broadcastBody by remember { mutableStateOf("") }

    var editMerchantComm by remember(merchantCommissionPercent) { mutableStateOf("$merchantCommissionPercent") }
    var editDriverPayout by remember(driverPayoutPercent) { mutableStateOf("$driverPayoutPercent") }
    var editServiceFee by remember(platformServiceFee) { mutableStateOf("$platformServiceFee") }

    val pendingUsers = users.filter { it.status == "PENDING_APPROVAL" }

    val filteredUsers = when (selectedRoleFilter) {
        1 -> users.filter { it.role == "DRIVER" }
        2 -> users.filter { it.role == "RESTAURANT_OWNER" }
        3 -> users.filter { it.role == "CUSTOMER" }
        4 -> pendingUsers
        else -> users
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("super_admin_dashboard"),
        contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Admin Master Header
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF6A1B9A).copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("👑", fontSize = 24.sp)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "لوحة المدير العام والمشرف التنفيذي",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 16.sp
                                )
                                Text(
                                    text = "التحكم الكامل بكافة المستخدمين والمطاعم والعمليات",
                                    fontSize = 11.sp,
                                    color = Color(0xFF6A1B9A),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // Broadcast Announcement Button
                        IconButton(
                            onClick = { showBroadcastModal = true },
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(AzizOrangePrimary)
                        ) {
                            Icon(imageVector = Icons.Default.Campaign, contentDescription = "Broadcast", tint = Color.White)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(14.dp))

                    // Master Platform KPIs
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        MetricBox(title = "إجمالي إيرادات المنصة", value = "145,800 د.م", color = AzizMint)
                        MetricBox(title = "المطاعم الشريكة", value = "${stores.size}", color = AzizOrangePrimary)
                        MetricBox(title = "السائقين النشطين", value = "${users.count { it.role == "DRIVER" }}", color = AzizAmberSecondary)
                        MetricBox(title = "المستخدمين", value = "${users.size}", color = Color(0xFF6A1B9A))
                    }
                }
            }
        }

        // Section 1: User Accounts Management
        item {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "إدارة المستخدمين والصلاحيات (${filteredUsers.size}) 👥",
                        fontWeight = FontWeight.Black,
                        fontSize = 15.sp
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    FilterChip(
                        selected = selectedRoleFilter == 0,
                        onClick = { selectedRoleFilter = 0 },
                        label = { Text("الكل", fontSize = 11.sp) }
                    )
                    FilterChip(
                        selected = selectedRoleFilter == 1,
                        onClick = { selectedRoleFilter = 1 },
                        label = { Text("السائقين", fontSize = 11.sp) }
                    )
                    FilterChip(
                        selected = selectedRoleFilter == 2,
                        onClick = { selectedRoleFilter = 2 },
                        label = { Text("أصحاب المطاعم", fontSize = 11.sp) }
                    )
                    FilterChip(
                        selected = selectedRoleFilter == 3,
                        onClick = { selectedRoleFilter = 3 },
                        label = { Text("الزبائن", fontSize = 11.sp) }
                    )
                    FilterChip(
                        selected = selectedRoleFilter == 4,
                        onClick = { selectedRoleFilter = 4 },
                        label = { Text("قيد الاعتماد (${pendingUsers.size}) ⏳", fontSize = 11.sp) }
                    )
                }
            }
        }

        items(filteredUsers) { user ->
            val isActive = user.status == "ACTIVE"
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(user.avatarEmoji, fontSize = 20.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(text = user.name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text(text = "${user.role} • ${user.phone}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = when (user.status) {
                                "ACTIVE" -> AzizMint.copy(alpha = 0.15f)
                                "PENDING_APPROVAL" -> AzizAmberSecondary.copy(alpha = 0.15f)
                                else -> Color.Red.copy(alpha = 0.15f)
                            }
                        ) {
                            Text(
                                text = user.status,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = when (user.status) {
                                    "ACTIVE" -> AzizMint
                                    "PENDING_APPROVAL" -> AzizAmberSecondary
                                    else -> Color.Red
                                }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (user.status != "ACTIVE") {
                            Button(
                                onClick = { onUpdateUserStatus(user.id, "ACTIVE") },
                                colors = ButtonDefaults.buttonColors(containerColor = AzizMint),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("تفعيل الحساب ✓", fontSize = 11.sp, color = Color.White)
                            }
                        } else {
                            OutlinedButton(
                                onClick = { onUpdateUserStatus(user.id, "SUSPENDED") },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("تجميد الحساب ✕", fontSize = 11.sp, color = Color.Red)
                            }
                        }

                        OutlinedButton(
                            onClick = {
                                val nextRole = when (user.role) {
                                    "CUSTOMER" -> "DRIVER"
                                    "DRIVER" -> "RESTAURANT_OWNER"
                                    "RESTAURANT_OWNER" -> "SUPPORT_AGENT"
                                    else -> "CUSTOMER"
                                }
                                onUpdateUserRole(user.id, nextRole)
                            },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("ترقية الدور 🔄", fontSize = 11.sp)
                        }
                    }
                }
            }
        }

        // Section 2: Restaurant Partners Management
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "شبكة المطاعم والمتاجر الشريكة (${stores.size}) 🏬",
                    fontWeight = FontWeight.Black,
                    fontSize = 15.sp
                )

                Button(
                    onClick = { showAddStoreModal = true },
                    colors = ButtonDefaults.buttonColors(containerColor = AzizOrangePrimary),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("إضافة متجر جديد", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }

        items(stores) { store ->
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(store.emoji, fontSize = 26.sp)
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = store.nameAr, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text(text = "${store.address} • رسوم: ${store.deliveryFee} د.م", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (store.isOpen) AzizMint.copy(alpha = 0.15f) else Color.Red.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = if (store.isOpen) "يعمل 🟢" else "مغلق 🔴",
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (store.isOpen) AzizMint else Color.Red
                        )
                    }
                }
            }
        }

        // Section 3: Delivery Zones in Mohammedia (Requirement 25)
        item {
            Column {
                Text(
                    text = "مناطق التوصيل بالمحمدية (Delivery Zones) 📍",
                    fontWeight = FontWeight.Black,
                    fontSize = 15.sp
                )
                Text(
                    text = "تحديد أسعار التوصيل والحد الأدنى للطلب حسب كل منطقة في المحمدية",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        items(deliveryZones) { zone ->
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = zone.nameAr, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text(
                            text = "رسوم التوصيل: ${zone.deliveryFee} د.م • الحد الأدنى: ${zone.minOrder} د.م • الوقت: ~${zone.estimatedMins} دقيقة",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Button(
                        onClick = { onToggleDeliveryZone(zone.id) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (zone.isActive) AzizMint else Color.Gray
                        ),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = if (zone.isActive) "مفعلة ✓" else "معطلة ✕",
                            fontSize = 10.sp,
                            color = Color.White
                        )
                    }
                }
            }
        }

        // Section 4: Platform Commission & Driver Payout Engine (Requirement 26)
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "نظام العمولات والرسوم المالية (Commission Engine) 💰",
                        fontWeight = FontWeight.Black,
                        fontSize = 15.sp
                    )
                    Text(
                        text = "تحديد نسبة عمولة ألو عزيز على المطاعم ونسبة ربح السائقين ورسوم الخدمة",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = editMerchantComm,
                            onValueChange = { editMerchantComm = it },
                            label = { Text("عمولة المتجر %") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = editDriverPayout,
                            onValueChange = { editDriverPayout = it },
                            label = { Text("نسبة السائق %") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = editServiceFee,
                            onValueChange = { editServiceFee = it },
                            label = { Text("رسوم الخدمة") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            val m = editMerchantComm.toDoubleOrNull() ?: 12.0
                            val d = editDriverPayout.toDoubleOrNull() ?: 80.0
                            val s = editServiceFee.toDoubleOrNull() ?: 2.5
                            onUpdateCommissionRates(m, d, s)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AzizOrangePrimary),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("حفظ وتطبيق إعدادات العمولات والرسوم ✓", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        }
    }

    // Modal: Add New Store Partner
    if (showAddStoreModal) {
        AlertDialog(
            onDismissRequest = { showAddStoreModal = false },
            title = { Text("توثيق وإضافة متجر شريك جديد 🏬", fontWeight = FontWeight.Bold, fontSize = 15.sp) },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = newStoreName,
                        onValueChange = { newStoreName = it },
                        label = { Text("اسم المتجر أو المطعم") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newStoreAddress,
                        onValueChange = { newStoreAddress = it },
                        label = { Text("العنوان والمدينة") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newStoreFee,
                        onValueChange = { newStoreFee = it },
                        label = { Text("رسوم التوصيل (د.م)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newStoreEmoji,
                        onValueChange = { newStoreEmoji = it },
                        label = { Text("أيقونة المتجر (Emoji)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val fee = newStoreFee.toDoubleOrNull() ?: 8.0
                        if (newStoreName.isNotBlank()) {
                            onAddNewStore(newStoreName, newStoreName, newStoreCategory, newStoreAddress, fee, newStoreEmoji)
                            showAddStoreModal = false
                            newStoreName = ""
                            newStoreAddress = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AzizOrangePrimary)
                ) {
                    Text("اعتماد وإضافة المتجر", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddStoreModal = false }) {
                    Text("إلغاء")
                }
            }
        )
    }

    // Modal: Broadcast Push Notification to All Users
    if (showBroadcastModal) {
        AlertDialog(
            onDismissRequest = { showBroadcastModal = false },
            title = { Text("إرسال إشعار عام لكافة المستخدمين 📢", fontWeight = FontWeight.Bold, fontSize = 15.sp) },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = broadcastTitle,
                        onValueChange = { broadcastTitle = it },
                        label = { Text("عنوان الإشعار") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = broadcastBody,
                        onValueChange = { broadcastBody = it },
                        label = { Text("نص الرسالة أو العرض الترويجي") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (broadcastTitle.isNotBlank() && broadcastBody.isNotBlank()) {
                            onBroadcastAnnouncement(broadcastTitle, broadcastBody)
                            showBroadcastModal = false
                            broadcastTitle = ""
                            broadcastBody = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AzizOrangePrimary)
                ) {
                    Text("إرسال فوري الآن", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showBroadcastModal = false }) {
                    Text("إلغاء")
                }
            }
        )
    }
}
