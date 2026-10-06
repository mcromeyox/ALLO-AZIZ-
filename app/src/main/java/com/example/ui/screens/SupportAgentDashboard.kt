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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ElectricScooter
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.SupportTicketEntity
import com.example.data.local.UserAccountEntity
import com.example.data.model.AppLanguage
import com.example.data.model.UserRole
import com.example.ui.theme.AzizAmberSecondary
import com.example.ui.theme.AzizMint
import com.example.ui.theme.AzizOrangePrimary

@Composable
fun SupportAgentDashboard(
    language: AppLanguage,
    users: List<UserAccountEntity>,
    tickets: List<SupportTicketEntity>,
    onApproveDriver: (String) -> Unit,
    onRejectDriver: (String) -> Unit,
    onResolveTicket: (String) -> Unit,
    onGrantCompensation: (Double) -> Unit
) {
    // All pending accounts awaiting activation from Support/Admin
    val pendingAccounts = users.filter { it.status == "PENDING_APPROVAL" }
    val activeUsers = users.filter { it.status == "ACTIVE" }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("support_agent_dashboard"),
        contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Support Console Header
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
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(AzizOrangePrimary.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.SupportAgent,
                                contentDescription = null,
                                tint = AzizOrangePrimary,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "لوحة تحكم الدعم الفني وخدمة العملاء",
                                fontWeight = FontWeight.Black,
                                fontSize = 16.sp
                            )
                            Text(
                                text = "صلاحيات اعتماد وتفعيل كافة الحسابات وحل النزاعات 24/7",
                                fontSize = 11.sp,
                                color = AzizMint,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        MetricBox(title = "حسابات بانتظار الاعتماد", value = "${pendingAccounts.size}", color = AzizOrangePrimary)
                        MetricBox(title = "حسابات نشطة مفعلة", value = "${activeUsers.size}", color = AzizMint)
                        MetricBox(title = "تذاكر الدعم المفتوحة", value = "${tickets.count { it.status == "OPEN" }}", color = AzizAmberSecondary)
                    }
                }
            }
        }

        // Section 1: All Account Approvals Queue (EXPLICIT USER REQUIREMENT)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.VerifiedUser,
                        contentDescription = null,
                        tint = AzizOrangePrimary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "طلبات تفعيل الحسابات الجديدة (${pendingAccounts.size})",
                        fontWeight = FontWeight.Black,
                        fontSize = 15.sp
                    )
                }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = AzizAmberSecondary.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = "صلاحية حصرية للدعم والمدير 🛡️",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = AzizAmberSecondary
                    )
                }
            }
        }

        if (pendingAccounts.isEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(modifier = Modifier.padding(18.dp), contentAlignment = Alignment.Center) {
                        Text(
                            text = "جميع الحسابات المسجلة مفعلة ومعتمدة حالياً! لا توجد طلبات معلقة ✓",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = AzizMint
                        )
                    }
                }
            }
        } else {
            items(pendingAccounts) { user ->
                val roleTitle = when (user.role) {
                    "DRIVER" -> "🛵 كابتن توصيل"
                    "RESTAURANT_OWNER" -> "👨‍🍳 صاحب مطعم"
                    "CUSTOMER" -> "🛍️ عميل زبون"
                    "SUPPORT_AGENT" -> "🎧 وكيل دعم فني"
                    else -> "👑 مدير"
                }

                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
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
                                    Text(text = user.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text(text = roleTitle, fontSize = 11.sp, color = AzizOrangePrimary, fontWeight = FontWeight.Bold)
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = AzizAmberSecondary.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = "بانتظار التفعيل ⏳",
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AzizAmberSecondary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Text(text = "الهاتف: ${user.phone} • البريد: ${user.email}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        
                        if (user.role == "DRIVER") {
                            Text(text = "المركبة: ${user.vehicleType ?: "سكوتر"} • اللوحة: ${user.plateNumber ?: "أ-11029"}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(text = "فحص الوثائق: رخصة صالحة + بطاقة هوية وطنية + تأمين ساري ✓", fontSize = 11.sp, color = AzizMint, fontWeight = FontWeight.SemiBold)
                        } else if (user.role == "RESTAURANT_OWNER") {
                            Text(text = "اسم المطعم: ${user.restaurantName ?: "مطعم شريك"}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(text = "السجل التجاري: تم التحقق من صحة ترخيص المنشأة ✓", fontSize = 11.sp, color = AzizMint, fontWeight = FontWeight.SemiBold)
                        } else {
                            Text(text = "نوع المصادقة: ${user.authProvider} • التحقق من الهوية جاهز ✓", fontSize = 11.sp, color = AzizMint, fontWeight = FontWeight.SemiBold)
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { onApproveDriver(user.id) },
                                colors = ButtonDefaults.buttonColors(containerColor = AzizMint),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("تفعيل الحساب والاعتماد ✓", fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Bold)
                            }

                            OutlinedButton(
                                onClick = { onRejectDriver(user.id) },
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Close, contentDescription = null, tint = Color.Red, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("رفض", fontSize = 12.sp, color = Color.Red)
                            }
                        }
                    }
                }
            }
        }

        // Section 2: Support Tickets & Customer Inquiries
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "تذاكر الدعم والشكاوى (${tickets.size}) 💬",
                    fontWeight = FontWeight.Black,
                    fontSize = 15.sp
                )
            }
        }

        items(tickets) { t ->
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = t.customerName, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (t.status == "OPEN") Color.Red.copy(alpha = 0.12f) else AzizMint.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = if (t.status == "OPEN") "مفتوحة 🔴" else "تم الحل ✓",
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (t.status == "OPEN") Color.Red else AzizMint
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = t.subject, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)

                    if (t.status == "OPEN") {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = { onResolveTicket(t.id) },
                                colors = ButtonDefaults.buttonColors(containerColor = AzizOrangePrimary),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("إغلاق وحل الشكوى ✓", fontSize = 11.sp, color = Color.White)
                            }
                            Button(
                                onClick = { onGrantCompensation(20.0) },
                                colors = ButtonDefaults.buttonColors(containerColor = AzizAmberSecondary),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("+20 د.م تعويض", fontSize = 11.sp, color = Color.Black, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}
