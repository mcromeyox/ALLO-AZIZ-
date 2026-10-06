package com.example.ui.screens

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ElectricScooter
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.OrderEntity
import com.example.data.location.DeviceLocation
import com.example.data.location.LocationServiceManager
import com.example.data.model.AppLanguage
import com.example.ui.theme.AzizAmberSecondary
import com.example.ui.theme.AzizMint
import com.example.ui.theme.AzizOrangePrimary
import com.example.ui.theme.GoldStarColor

@Composable
fun DriverPartnerDashboard(
    language: AppLanguage,
    isOnline: Boolean,
    orders: List<OrderEntity>,
    currentDeviceLocation: DeviceLocation? = null,
    onToggleOnline: () -> Unit,
    onAcceptTask: (String) -> Unit,
    onMarkPickedUp: (String) -> Unit,
    onMarkDelivered: (String) -> Unit,
    onNavigateWithGoogleMaps: ((OrderEntity) -> Unit)? = null,
    onBroadcastGpsLocation: ((orderId: String) -> Unit)? = null
) {
    val activeDelivery = orders.firstOrNull { it.status == "ON_THE_WAY" || it.status == "PREPARING" }
    val availableTasks = orders.filter { it.status == "RECEIVED" || (it.status == "PREPARING" && it.id != activeDelivery?.id) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("driver_dashboard_screen"),
        contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Driver Header & Online Toggle
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
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(AzizMint.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("🛵", fontSize = 26.sp)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "عزيز برادة (كابتن ألو عزيز)",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 16.sp
                                )
                                Text(
                                    text = "دراجة هوندا 125cc • لوحة: أ-54921",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // Online / Offline Switch
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = if (isOnline) "متصل 🟢" else "غير متصل 🔴",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isOnline) AzizMint else Color.Red
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Switch(
                                checked = isOnline,
                                onCheckedChange = { onToggleOnline() },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = AzizMint
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(14.dp))

                    // Daily Earnings Metrics
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        MetricBox(title = "أرباح اليوم", value = "480 د.م", color = AzizMint)
                        MetricBox(title = "رحلات مكتملة", value = "18 توصيلة", color = AzizOrangePrimary)
                        MetricBox(title = "تقييم العملاء", value = "4.95 ★", color = GoldStarColor)
                    }
                }
            }
        }

        // Active Delivery in Progress (If Any)
        if (activeDelivery != null) {
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = AzizOrangePrimary.copy(alpha = 0.1f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("📍", fontSize = 20.sp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "مهمة التوصيل الحالية النشطة",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 15.sp,
                                    color = AzizOrangePrimary
                                )
                            }
                            Text(
                                text = "طلب #${activeDelivery.id}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = AzizOrangePrimary
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "استلام من: ${activeDelivery.storeName}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Text(
                            text = "تسليم إلى: ${activeDelivery.deliveryAddress}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "الأصناف: ${activeDelivery.itemsSummary}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "المبلغ المطلوب تحصيله: ${activeDelivery.total} د.م (${activeDelivery.paymentMethod})",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = AzizOrangePrimary
                        )

                        val context = LocalContext.current
                        val distKm = remember(activeDelivery.courierLat, activeDelivery.courierLng, activeDelivery.customerLat, activeDelivery.customerLng) {
                            LocationServiceManager.calculateDistanceKm(
                                activeDelivery.courierLat, activeDelivery.courierLng,
                                activeDelivery.customerLat, activeDelivery.customerLng
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // GPS Location Live Feedback Bar
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Navigation,
                                    contentDescription = "GPS Nav",
                                    tint = AzizMint,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "المسافة للوجهة: %.1f كم (Play Services)".format(distKm),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            if (onBroadcastGpsLocation != null) {
                                Text(
                                    text = "بث GPS نشط 📡",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AzizMint
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Google Maps Navigation button for Driver
                        OutlinedButton(
                            onClick = {
                                if (onNavigateWithGoogleMaps != null) {
                                    onNavigateWithGoogleMaps(activeDelivery)
                                } else {
                                    LocationServiceManager.openGoogleMapsNavigation(
                                        context = context,
                                        destinationLat = activeDelivery.customerLat,
                                        destinationLng = activeDelivery.customerLng,
                                        destinationLabel = activeDelivery.deliveryAddress
                                    )
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("driver_google_maps_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Map,
                                contentDescription = "Navigation",
                                tint = AzizOrangePrimary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "فتح الملاحة في خرائط Google (Turn-by-Turn GPS) 🗺️",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = AzizOrangePrimary
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            if (activeDelivery.status != "ON_THE_WAY") {
                                Button(
                                    onClick = { onMarkPickedUp(activeDelivery.id) },
                                    colors = ButtonDefaults.buttonColors(containerColor = AzizOrangePrimary),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("تم استلام الطلب من المطعم 📦", fontSize = 11.sp, color = Color.White)
                                }
                            } else {
                                Button(
                                    onClick = { onMarkDelivered(activeDelivery.id) },
                                    colors = ButtonDefaults.buttonColors(containerColor = AzizMint),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("تم تسليم الطلب للزبون بنجاح 🏁", fontSize = 11.sp, color = Color.White)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Available Nearby Delivery Tasks
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "طلبات التوصيل القريبة المتاحة (${availableTasks.size})",
                    fontWeight = FontWeight.Black,
                    fontSize = 16.sp
                )
                Text("عمولة مجزية 💸", fontSize = 11.sp, color = AzizMint, fontWeight = FontWeight.Bold)
            }
        }

        if (availableTasks.isEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(modifier = Modifier.padding(20.dp), contentAlignment = Alignment.Center) {
                        Text(
                            text = "لا توجد طلبات توصيل قريبة معلقة الآن. خليك متصل وسنرسل لك إشعاراً فورياً!",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(availableTasks) { task ->
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = task.storeName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text(
                                text = "+${task.deliveryFee.toInt() + 15} د.م عمولتك",
                                fontWeight = FontWeight.Black,
                                fontSize = 13.sp,
                                color = AzizMint
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = "التسليم: ${task.deliveryAddress}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(text = "المسافة: 2.3 كم • الوقت المتوقع: 15 دقيقة", fontSize = 11.sp, color = AzizOrangePrimary)

                        Spacer(modifier = Modifier.height(10.dp))

                        Button(
                            onClick = { onAcceptTask(task.id) },
                            colors = ButtonDefaults.buttonColors(containerColor = AzizOrangePrimary),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("قبول مهمة التوصيل والتوجه للمطعم 🛵", fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }
        }
    }
}
