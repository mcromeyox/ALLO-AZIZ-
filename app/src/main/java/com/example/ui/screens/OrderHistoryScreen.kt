package com.example.ui.screens

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ElectricScooter
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
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
import com.example.data.local.OrderEntity
import com.example.data.localization.AppStrings
import com.example.data.model.AppLanguage
import com.example.ui.theme.AzizAmberSecondary
import com.example.ui.theme.AzizMint
import com.example.ui.theme.AzizOrangePrimary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun OrderHistoryScreen(
    orders: List<OrderEntity>,
    language: AppLanguage,
    onTrackOrder: (String) -> Unit,
    onReorder: (OrderEntity) -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0 = All/Active, 1 = Past
    var selectedOrderForReceipt by remember { mutableStateOf<OrderEntity?>(null) }

    val activeOrders = orders.filter { it.status != "DELIVERED" }
    val pastOrders = orders.filter { it.status == "DELIVERED" }
    val displayedOrders = when (selectedTab) {
        0 -> orders
        1 -> activeOrders
        else -> pastOrders
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("order_history_screen")
    ) {
        // Screen Header
        Text(
            text = AppStrings.orderHistoryTitle(language),
            fontWeight = FontWeight.Black,
            fontSize = 20.sp,
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp),
            color = MaterialTheme.colorScheme.onSurface
        )

        // Filter Tabs
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                label = { Text("جميع الطلبات (${orders.size})") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = AzizOrangePrimary,
                    selectedLabelColor = Color.White
                )
            )
            FilterChip(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                label = { Text("${AppStrings.activeOrders(language)} (${activeOrders.size})") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = AzizOrangePrimary,
                    selectedLabelColor = Color.White
                )
            )
            FilterChip(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                label = { Text("${AppStrings.pastOrders(language)} (${pastOrders.size})") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = AzizOrangePrimary,
                    selectedLabelColor = Color.White
                )
            )
        }

        if (displayedOrders.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(70.dp)
                            .clip(CircleShape)
                            .background(AzizOrangePrimary.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = null,
                            tint = AzizOrangePrimary,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "لا توجد طلبات في هذا القسم حالياً",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 100.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(displayedOrders) { order ->
                    OrderHistoryCard(
                        order = order,
                        language = language,
                        onTrackClick = { onTrackOrder(order.id) },
                        onReorderClick = { onReorder(order) },
                        onViewReceiptClick = { selectedOrderForReceipt = order }
                    )
                }
            }
        }
    }

    // Receipt Invoice Modal Dialog
    if (selectedOrderForReceipt != null) {
        val o = selectedOrderForReceipt!!
        val dateFormatted = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(o.timestamp))

        AlertDialog(
            onDismissRequest = { selectedOrderForReceipt = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Receipt,
                        contentDescription = null,
                        tint = AzizOrangePrimary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("فاتورة رسمية - ألو عزيز", fontWeight = FontWeight.Black, fontSize = 16.sp)
                }
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(text = "رقم الطلب: #${o.id}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Text(text = "التاريخ: $dateFormatted", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(text = "المتجر: ${o.storeName}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(text = "العنوان: ${o.deliveryAddress}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                    Text(text = "الأصناف المطلوبة:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Text(text = o.itemsSummary, fontSize = 12.sp)

                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("المجموع الفرعي:", fontSize = 12.sp)
                        Text("${o.subtotal} ${AppStrings.currency(language)}", fontSize = 12.sp)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("رسوم التوصيل:", fontSize = 12.sp)
                        Text("${o.deliveryFee} ${AppStrings.currency(language)}", fontSize = 12.sp)
                    }
                    if (o.discount > 0) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("الخصم:", fontSize = 12.sp, color = AzizMint)
                            Text("-${o.discount} ${AppStrings.currency(language)}", fontSize = 12.sp, color = AzizMint, fontWeight = FontWeight.Bold)
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("الإجمالي المدفوع:", fontWeight = FontWeight.Black, fontSize = 14.sp)
                        Text("${o.total} ${AppStrings.currency(language)}", fontWeight = FontWeight.Black, fontSize = 16.sp, color = AzizOrangePrimary)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "طريقة الدفع: ${o.paymentMethod}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(text = "المندوب: ${o.courierName}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            },
            confirmButton = {
                Button(
                    onClick = { selectedOrderForReceipt = null },
                    colors = ButtonDefaults.buttonColors(containerColor = AzizOrangePrimary),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("إغلاق", color = Color.White)
                }
            }
        )
    }
}

@Composable
fun OrderHistoryCard(
    order: OrderEntity,
    language: AppLanguage,
    onTrackClick: () -> Unit,
    onReorderClick: () -> Unit,
    onViewReceiptClick: () -> Unit
) {
    val isDelivered = order.status == "DELIVERED"
    val dateStr = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(Date(order.timestamp))

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
                    Text("🛍️", fontSize = 20.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = order.storeName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = dateStr,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (isDelivered) AzizMint.copy(alpha = 0.15f) else AzizOrangePrimary.copy(alpha = 0.15f)
                ) {
                    val statusLabel = when (order.status) {
                        "RECEIVED" -> AppStrings.statusReceived(language)
                        "PREPARING" -> AppStrings.statusPreparing(language)
                        "ON_THE_WAY" -> AppStrings.statusOnTheWay(language)
                        "ARRIVED" -> AppStrings.statusArrived(language)
                        else -> "تم التوصيل بنجاح ✓"
                    }
                    Text(
                        text = statusLabel,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDelivered) AzizMint else AzizOrangePrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = order.itemsSummary,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2
            )

            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${order.total} ${AppStrings.currency(language)}",
                    fontWeight = FontWeight.Black,
                    fontSize = 15.sp,
                    color = AzizOrangePrimary
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = onViewReceiptClick,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Text(AppStrings.viewReceipt(language), fontSize = 11.sp)
                    }

                    if (!isDelivered) {
                        Button(
                            onClick = onTrackClick,
                            colors = ButtonDefaults.buttonColors(containerColor = AzizOrangePrimary),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.height(34.dp)
                        ) {
                            Text(AppStrings.liveTracking(language), fontSize = 11.sp, color = Color.White)
                        }
                    } else {
                        Button(
                            onClick = onReorderClick,
                            colors = ButtonDefaults.buttonColors(containerColor = AzizAmberSecondary),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.height(34.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Replay,
                                    contentDescription = null,
                                    modifier = Modifier.size(13.dp),
                                    tint = Color.Black
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(AppStrings.reorder(language), fontSize = 11.sp, color = Color.Black, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}
