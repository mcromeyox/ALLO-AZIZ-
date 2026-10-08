package com.example.ui.screens

import android.Manifest
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ElectricScooter
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.OrderEntity
import com.example.data.location.DeviceLocation
import com.example.data.location.LocationServiceManager
import com.example.data.localization.AppStrings
import com.example.data.model.AppLanguage
import com.example.ui.theme.AzizAmberSecondary
import com.example.ui.theme.AzizMint
import com.example.ui.theme.AzizOrangePrimary
import com.example.ui.theme.GoldStarColor
import com.example.ui.theme.OrderDeliveredColor
import com.example.ui.theme.OrderOnTheWayColor
import com.example.ui.theme.OrderPreparingColor
import com.example.ui.theme.OrderReceivedColor

@Composable
fun OrderTrackingScreen(
    order: OrderEntity?,
    language: AppLanguage,
    currentDeviceLocation: DeviceLocation? = null,
    isGpsLive: Boolean = false,
    onBack: () -> Unit,
    onSubmitReview: (orderId: String, storeName: String, storeRating: Int, courierRating: Int, tags: List<String>, comment: String) -> Unit,
    onContactSupport: () -> Unit,
    onOpenGoogleMaps: ((OrderEntity) -> Unit)? = null,
    onRequestGpsLocation: (() -> Unit)? = null,
    onToggleGpsTracking: ((Boolean) -> Unit)? = null,
    onShareReceipt: ((OrderEntity) -> Unit)? = null,
    onOpenWhatsAppCourier: ((OrderEntity) -> Unit)? = null,
    onAdvanceStage: ((String) -> Unit)? = null
) {
    BackHandler { onBack() }

    val context = LocalContext.current
    var showReviewDialog by remember { mutableStateOf(false) }
    var showCourierChatDialog by remember { mutableStateOf(false) }
    var showWhatsAppModal by remember { mutableStateOf(false) }
    var customWhatsAppPhoneInput by remember(order) {
        mutableStateOf(
            if (order != null && order.courierPhone.isNotBlank() && !com.example.data.system.ExternalShareHelper.isPlaceholderOrInvalid(order.courierPhone)) {
                order.courierPhone
            } else {
                com.example.data.system.ExternalShareHelper.getSavedWhatsAppNumber(context)
            }
        )
    }
    var courierChatInput by remember { mutableStateOf("") }
    val courierChatLog = remember {
        mutableStateListOf(
            "عزيز المندوب: أهلاً بك! استلمت طلبك الساخن وأنا في الطريق إليك بأقصى سرعة."
        )
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false
        if (fineGranted || coarseGranted) {
            onRequestGpsLocation?.invoke()
            onToggleGpsTracking?.invoke(true)
        }
    }

    if (order == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .testTag("no_active_order_container"),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("🛵", fontSize = 48.sp)
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "لا يوجد طلب قيد التتبع حالياً",
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "قم بطلب وجبتك المفضلة وسنبدأ بالتتبع الفوري فوراً!",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onBack,
                    colors = ButtonDefaults.buttonColors(containerColor = AzizOrangePrimary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("العودة للرئيسية", color = Color.White)
                }
            }
        }
        return
    }

    val currentStageIndex = when (order.status) {
        "RECEIVED" -> 0
        "PREPARING" -> 1
        "ON_THE_WAY" -> 2
        "ARRIVED" -> 3
        "DELIVERED" -> 4
        else -> 0
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("order_tracking_screen"),
        contentPadding = PaddingValues(start = 16.dp, top = 12.dp, end = 16.dp, bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Top Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.testTag("tracking_back_btn")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = AzizOrangePrimary
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = AppStrings.liveTracking(language),
                        fontWeight = FontWeight.Black,
                        fontSize = 17.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${AppStrings.orderIdPrefix(language)} #${order.id}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = AzizOrangePrimary
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (order.status == "DELIVERED") AzizMint.copy(alpha = 0.15f) else AzizOrangePrimary.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = if (order.status == "DELIVERED") "مكتمل ✓" else "مباشر 🔴",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (order.status == "DELIVERED") AzizMint else AzizOrangePrimary
                    )
                }
            }
        }

        // Live Simulated Map Canvas
        item {
            LiveTrackingMapCanvas(
                progress = order.courierProgress,
                status = order.status,
                storeName = order.storeName,
                address = order.deliveryAddress
            )
        }

        // Google Play Services Real-Time GPS Tracking & Maps Navigation Card
        item {
            val distKm = remember(order.courierLat, order.courierLng, order.customerLat, order.customerLng) {
                LocationServiceManager.calculateDistanceKm(order.courierLat, order.courierLng, order.customerLat, order.customerLng)
            }
            val formattedDist = if (distKm < 1.0) {
                "${(distKm * 1000).toInt()} متر"
            } else {
                "%.1f كم".format(distKm)
            }

            ElevatedCard(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("play_services_gps_card")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                color = AzizMint.copy(alpha = 0.15f),
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Navigation,
                                        contentDescription = "GPS",
                                        tint = AzizMint,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "تتبع GPS المباشر (Play Services)",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = if (isGpsLive) "الموقع متزامن عبر الأقمار الصناعية 🛰️" else "دقة التتبع الجغرافي نشطة 📍",
                                    fontSize = 11.sp,
                                    color = AzizMint,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = AzizOrangePrimary.copy(alpha = 0.12f)
                        ) {
                            Text(
                                text = "المسافة: $formattedDist",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = AzizOrangePrimary
                            )
                        }
                    }

                    if (currentDeviceLocation != null) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.MyLocation,
                                contentDescription = "Device Location",
                                tint = AzizOrangePrimary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "إحداثياتك: %.4f, %.4f (دقة %dم)".format(
                                    currentDeviceLocation.latitude,
                                    currentDeviceLocation.longitude,
                                    currentDeviceLocation.accuracy.toInt()
                                ),
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Open in Google Maps button
                        Button(
                            onClick = {
                                if (onOpenGoogleMaps != null) {
                                    onOpenGoogleMaps(order)
                                } else {
                                    LocationServiceManager.openGoogleMapsNavigation(
                                        context = context,
                                        destinationLat = order.customerLat,
                                        destinationLng = order.customerLng,
                                        destinationLabel = order.deliveryAddress
                                    )
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("open_google_maps_btn"),
                            colors = ButtonDefaults.buttonColors(containerColor = AzizOrangePrimary),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Map,
                                contentDescription = "Google Maps",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("خرائط Google", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        // Refresh / Request GPS location button
                        OutlinedButton(
                            onClick = {
                                locationPermissionLauncher.launch(
                                    arrayOf(
                                        Manifest.permission.ACCESS_FINE_LOCATION,
                                        Manifest.permission.ACCESS_COARSE_LOCATION
                                    )
                                )
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("sync_gps_btn"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.MyLocation,
                                contentDescription = "My GPS",
                                modifier = Modifier.size(16.dp),
                                tint = AzizMint
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("تحديث GPS", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AzizMint)
                        }
                    }
                }
            }
        }

        // Live ETA & Progress Card
        item {
            ElevatedCard(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth().testTag("live_eta_progress_card")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = when (order.status) {
                                    "RECEIVED" -> "حالة الطلب: قيد مراجعة المتجر ⏳"
                                    "PREPARING" -> "حالة الطلب: قيد التحضير في المطبخ 🍳"
                                    "ON_THE_WAY" -> AppStrings.estimatedArrival(language)
                                    "ARRIVED" -> "المندوب وصل أمام العنوان 📍"
                                    else -> AppStrings.statusDelivered(language)
                                },
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            val arrivalText = when (order.status) {
                                "RECEIVED" -> "بانتظار قبول المتجر (25-30 دقيقة)"
                                "PREPARING" -> "قيد الطهي (15-20 دقيقة)"
                                "ON_THE_WAY" -> "${order.estimatedMinsLeft} ${AppStrings.minTime(language, order.estimatedMinsLeft)}"
                                "ARRIVED" -> "متواجد عند الباب الآن"
                                else -> AppStrings.statusDelivered(language)
                            }
                            Text(
                                text = arrivalText,
                                fontWeight = FontWeight.Black,
                                fontSize = 18.sp,
                                color = if (order.status == "DELIVERED") AzizMint else AzizOrangePrimary
                            )
                        }

                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(AzizOrangePrimary.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Timer,
                                contentDescription = null,
                                tint = AzizOrangePrimary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Stages Stepper
                    TrackingStagesStepper(
                        currentStage = currentStageIndex,
                        language = language
                    )

                    // Stage Progression Sync Controller for Testing & Real Sync
                    if (onAdvanceStage != null && order.status != "DELIVERED") {
                        Spacer(modifier = Modifier.height(14.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "مزامنة دورة التوصيل وتحديث الحالة:",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "انقر لتحديث مرحلة التوصيل خطوة بخطوة",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                )
                            }
                            Button(
                                onClick = { onAdvanceStage(order.id) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = when (order.status) {
                                        "RECEIVED" -> AzizOrangePrimary
                                        "PREPARING" -> AzizAmberSecondary
                                        "ON_THE_WAY" -> AzizMint
                                        else -> AzizMint
                                    }
                                ),
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                modifier = Modifier.testTag("sync_advance_stage_btn")
                            ) {
                                val nextLabel = when (order.status) {
                                    "RECEIVED" -> "بدء التحضير 🍳"
                                    "PREPARING" -> "انطلاق الكابتن 🛵"
                                    "ON_THE_WAY" -> "وصول الكابتن 📍"
                                    "ARRIVED" -> "تأكيد الاستلام ✓"
                                    else -> "تحديث الحالة 🔄"
                                }
                                Text(nextLabel, fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // Courier Profile Card (Shows pending state when not yet on the way)
        item {
            val isCourierActive = order.status in listOf("ON_THE_WAY", "ARRIVED", "DELIVERED") && !order.courierName.contains("جاري")

            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.testTag("courier_profile_card")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    if (!isCourierActive) {
                        // Pending assignment state
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = AzizOrangePrimary.copy(alpha = 0.15f),
                                modifier = Modifier.size(50.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text("🛵", fontSize = 24.sp)
                                }
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "كابتن التوصيل",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "جاري تعيين أقرب كابتن توصيل متوفر ⏳",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = AzizOrangePrimary
                                )
                                Text(
                                    text = "ستظهر بيانات الكابتن ورقم هاتفه وإمكانية الاتصال به فور انطلاقه بالطلب من المتجر.",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedButton(
                            onClick = { showWhatsAppModal = true },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("whatsapp_pending_support_btn")
                        ) {
                            Text("💬", fontSize = 15.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "مراسلة إدارة المتجر والكباتن عبر واتساب (WhatsApp)",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = AzizMint
                            )
                        }
                    } else {
                        // Assigned driver state
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.linearGradient(
                                            listOf(AzizOrangePrimary, AzizAmberSecondary)
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("🛵", fontSize = 26.sp)
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = order.courierName.ifBlank { "الكابتن عزيز برادة" },
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = null,
                                        tint = GoldStarColor,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = "${order.courierRating} (كابتن معتمد 🇲🇦)",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Text(
                                    text = order.courierVehicle.ifBlank { "دراجة نارية" },
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider()
                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    val phone = order.courierPhone.ifBlank {
                                        com.example.data.system.ExternalShareHelper.getSavedWhatsAppNumber(context)
                                    }
                                    if (phone.isNotBlank() && !com.example.data.system.ExternalShareHelper.isPlaceholderOrInvalid(phone)) {
                                        val intent = Intent(Intent.ACTION_DIAL).apply {
                                            data = Uri.parse("tel:$phone")
                                        }
                                        context.startActivity(intent)
                                    } else {
                                        showWhatsAppModal = true
                                    }
                                },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("call_courier_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Call,
                                    contentDescription = null,
                                    tint = AzizOrangePrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = AppStrings.callCourier(language),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AzizOrangePrimary
                                )
                            }

                            Button(
                                onClick = { showCourierChatDialog = true },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = AzizOrangePrimary),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("message_courier_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Chat,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = AppStrings.messageCourier(language),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedButton(
                            onClick = {
                                if (order.courierPhone.isNotBlank() && !com.example.data.system.ExternalShareHelper.isPlaceholderOrInvalid(order.courierPhone)) {
                                    com.example.data.system.ExternalShareHelper.openWhatsApp(
                                        context = context,
                                        phoneE164 = order.courierPhone,
                                        prefilledText = "السلام عليكم كابتن عزيز، أنا العميل بخصوص طلبي #${order.id} من متجر ${order.storeName}."
                                    )
                                } else {
                                    showWhatsAppModal = true
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("whatsapp_courier_button")
                        ) {
                            Text("💬", fontSize = 15.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "مراسلة عبر واتساب (WhatsApp)",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = AzizMint
                            )
                        }
                    }
                }
            }
        }

        // Rating & Review Card Button (ONLY visible when order is actually DELIVERED)
        if (order.status == "DELIVERED") {
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (order.hasReviewed) AzizMint.copy(alpha = 0.1f) else AzizAmberSecondary.copy(alpha = 0.12f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showReviewDialog = true }
                        .testTag("rate_order_action_card")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = if (order.hasReviewed) "✅" else "⭐",
                                fontSize = 24.sp
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = if (order.hasReviewed) "تم تقييم هذا الطلب بنجاح" else AppStrings.rateExperience(language),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = if (order.hasReviewed) "شكراً لدعمك المتجر والمندوب عزيز!" else "شاركنا رأيك في سرعة التوصيل وجودة الطعام",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (order.hasReviewed) AzizMint else AzizOrangePrimary
                        ) {
                            Text(
                                text = if (order.hasReviewed) "مراجعة" else "تقييم",
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }

        // Order Invoice Breakdown Card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Receipt,
                            contentDescription = null,
                            tint = AzizOrangePrimary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "تفاصيل الطلب والفاتورة",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "المتجر: ${order.storeName}",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp
                    )
                    Text(
                        text = "الأصناف: ${order.itemsSummary}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "طريقة الدفع: ${order.paymentMethod}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "العنوان: ${order.deliveryAddress}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "المبلغ الإجمالي المدفوع", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text(
                            text = "${order.total} ${AppStrings.currency(language)}",
                            fontWeight = FontWeight.Black,
                            fontSize = 15.sp,
                            color = AzizOrangePrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedButton(
                        onClick = {
                            onShareReceipt?.invoke(order) ?: run {
                                com.example.data.system.ExternalShareHelper.shareOrderReceipt(context, order)
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("share_receipt_button")
                    ) {
                        Text("🧾", fontSize = 15.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "مشاركة / طباعة إيصال الطلب",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = AzizOrangePrimary
                        )
                    }
                }
            }
        }
    }

    // Courier Chat Dialog
    if (showCourierChatDialog) {
        AlertDialog(
            onDismissRequest = { showCourierChatDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("🛵", fontSize = 20.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "محادثة سريعة مع المندوب عزيز", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    courierChatLog.forEach { msg ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .padding(8.dp)
                        ) {
                            Text(text = msg, fontSize = 12.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = courierChatInput,
                        onValueChange = { courierChatInput = it },
                        placeholder = { Text("اكتب رسالتك للمندوب...", fontSize = 12.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (courierChatInput.isNotBlank()) {
                            courierChatLog.add("أنت: $courierChatInput")
                            val reply = "عزيز المندوب: تم استلام ملاحظتك، أنا على بعد دقائق قليلة!"
                            courierChatLog.add(reply)
                            courierChatInput = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AzizOrangePrimary)
                ) {
                    Text("إرسال", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCourierChatDialog = false }) {
                    Text("إغلاق")
                }
            }
        )
    }

    // Customer Review & Rating Dialog
    if (showReviewDialog) {
        ReviewSubmissionDialog(
            order = order,
            language = language,
            onDismiss = { showReviewDialog = false },
            onSubmit = { sRating, cRating, tags, comment ->
                onSubmitReview(order.id, order.storeName, sRating, cRating, tags, comment)
                showReviewDialog = false
            }
        )
    }

    // Direct WhatsApp Modal Dialog (Ensures genuine phone number, never random dummy)
    if (showWhatsAppModal) {
        AlertDialog(
            onDismissRequest = { showWhatsAppModal = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("💬", fontSize = 22.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "مراسلة عبر واتساب (WhatsApp 🇲🇦)",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "تواصل مباشر مع كابتن التوصيل أو إدارة ألو عزيز بخصوص طلبك #${order.id}.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "رقم واتساب الفعلي للكابتن أو الإدارة:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = customWhatsAppPhoneInput,
                        onValueChange = { customWhatsAppPhoneInput = it },
                        placeholder = { Text("مثال: 0612345678 أو 0712345678", fontSize = 12.sp) },
                        modifier = Modifier.fillMaxWidth().testTag("custom_whatsapp_input"),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "💡 سيتم حفظ هذا الرقم على جهازك وسيتم فتح محادثة واتساب فوراً بالرسالة الرسمية للطلب.",
                        fontSize = 11.sp,
                        color = AzizMint
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val phoneToUse = customWhatsAppPhoneInput.trim()
                        if (phoneToUse.isNotBlank()) {
                            com.example.data.system.ExternalShareHelper.saveWhatsAppNumber(context, phoneToUse)
                            val prefilled = "السلام عليكم، أنا العميل بخصوص طلبي #${order.id} من متجر ${order.storeName} - العنوان: ${order.deliveryAddress}."
                            com.example.data.system.ExternalShareHelper.openWhatsApp(
                                context = context,
                                phoneE164 = phoneToUse,
                                prefilledText = prefilled
                            )
                            showWhatsAppModal = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AzizMint),
                    modifier = Modifier.testTag("confirm_open_whatsapp_btn")
                ) {
                    Text("فتح واتساب الآن 🚀", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showWhatsAppModal = false }) {
                    Text("إلغاء")
                }
            }
        )
    }
}

@Composable
fun LiveTrackingMapCanvas(
    progress: Float,
    status: String,
    storeName: String,
    address: String
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse_radar")
    val pulseRadius by infiniteTransition.animateFloat(
        initialValue = 10f,
        targetValue = 28f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "radar"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(210.dp)
            .testTag("tracking_map_canvas"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF1EDE8)),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height

                // Grid city background streets
                val streetColor = Color(0xFFE4DDD6)
                for (x in 40 until w.toInt() step 70) {
                    drawLine(
                        color = streetColor,
                        start = Offset(x.toFloat(), 0f),
                        end = Offset(x.toFloat(), h),
                        strokeWidth = 12f
                    )
                }
                for (y in 30 until h.toInt() step 60) {
                    drawLine(
                        color = streetColor,
                        start = Offset(0f, y.toFloat()),
                        end = Offset(w, y.toFloat()),
                        strokeWidth = 12f
                    )
                }

                // Delivery Route from Store (top-left) to Customer (bottom-right)
                val startPoint = Offset(w * 0.18f, h * 0.28f)
                val midPoint1 = Offset(w * 0.45f, h * 0.28f)
                val midPoint2 = Offset(w * 0.45f, h * 0.72f)
                val endPoint = Offset(w * 0.82f, h * 0.72f)

                val routePath = Path().apply {
                    moveTo(startPoint.x, startPoint.y)
                    lineTo(midPoint1.x, midPoint1.y)
                    lineTo(midPoint2.x, midPoint2.y)
                    lineTo(endPoint.x, endPoint.y)
                }

                // Gray path background
                drawPath(
                    path = routePath,
                    color = Color(0xFFCFD8DC),
                    style = Stroke(
                        width = 10f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(15f, 10f), 0f)
                    )
                )

                // Active Orange path
                drawPath(
                    path = routePath,
                    color = AzizOrangePrimary,
                    style = Stroke(width = 8f)
                )

                // Store Pin (Origin)
                drawCircle(
                    color = AzizOrangePrimary,
                    radius = 16f,
                    center = startPoint
                )
                drawCircle(
                    color = Color.White,
                    radius = 8f,
                    center = startPoint
                )

                // Customer Pin (Destination)
                drawCircle(
                    color = AzizMint,
                    radius = 16f,
                    center = endPoint
                )
                drawCircle(
                    color = Color.White,
                    radius = 8f,
                    center = endPoint
                )

                // Calculate Courier Position based on actual order status and progress
                val courierPos = when {
                    status in listOf("RECEIVED", "PREPARING") -> startPoint
                    status in listOf("ARRIVED", "DELIVERED") -> endPoint
                    progress <= 0.33f -> {
                        val segProgress = progress / 0.33f
                        Offset(
                            startPoint.x + (midPoint1.x - startPoint.x) * segProgress,
                            startPoint.y
                        )
                    }
                    progress <= 0.66f -> {
                        val segProgress = (progress - 0.33f) / 0.33f
                        Offset(
                            midPoint1.x,
                            midPoint1.y + (midPoint2.y - midPoint1.y) * segProgress
                        )
                    }
                    else -> {
                        val segProgress = ((progress - 0.66f) / 0.34f).coerceIn(0f, 1f)
                        Offset(
                            midPoint2.x + (endPoint.x - midPoint2.x) * segProgress,
                            midPoint2.y
                        )
                    }
                }

                // Pulsing radar ripple
                drawCircle(
                    color = AzizOrangePrimary.copy(alpha = 0.25f),
                    radius = pulseRadius,
                    center = courierPos
                )

                // Courier marker dot
                drawCircle(
                    color = AzizOrangePrimary,
                    radius = 14f,
                    center = courierPos
                )
                drawCircle(
                    color = Color.White,
                    radius = 6f,
                    center = courierPos
                )
            }

            // Origin Label
            Surface(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(10.dp),
                shape = RoundedCornerShape(8.dp),
                color = Color.White.copy(alpha = 0.95f),
                shadowElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("🏬", fontSize = 12.sp)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = storeName,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }
            }

            // Professional Development Map Header Badge (Mohammedia, Morocco)
            Surface(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(10.dp),
                shape = RoundedCornerShape(8.dp),
                color = Color.Black.copy(alpha = 0.78f),
                shadowElevation = 3.dp
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = "ALLO AZIZ",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        color = AzizOrangePrimary
                    )
                    Text(
                        text = "Mohammedia, Morocco 🇲🇦",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                }
            }

            // Destination Label
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(10.dp),
                shape = RoundedCornerShape(8.dp),
                color = Color.White.copy(alpha = 0.95f),
                shadowElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = AzizMint,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "موقعك (الاستلام)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }
            }

            // Dynamic Courier / Delivery Stage Floating Tag
            val tagAlignment = when (status) {
                "RECEIVED", "PREPARING" -> Alignment.CenterStart
                "ARRIVED", "DELIVERED" -> Alignment.CenterEnd
                else -> Alignment.Center
            }
            val stageTagLabel = when (status) {
                "RECEIVED" -> "المتجر يراجع الطلب ⏳"
                "PREPARING" -> "قيد التحضير بالمطبخ 🍳"
                "ON_THE_WAY" -> "الكابتن في الطريق 🛵"
                "ARRIVED" -> "الكابتن وصل عند العنوان 🚪"
                "DELIVERED" -> "تم التسليم بنجاح ✓"
                else -> "كابتن ألو عزيز 🛵"
            }

            Box(
                modifier = Modifier
                    .align(tagAlignment)
                    .padding(horizontal = 24.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        when (status) {
                            "DELIVERED", "ARRIVED" -> AzizMint
                            "PREPARING" -> AzizAmberSecondary
                            else -> AzizOrangePrimary
                        }
                    )
                    .padding(horizontal = 10.dp, vertical = 5.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(if (status in listOf("DELIVERED", "ARRIVED")) "✓" else "🛵", fontSize = 13.sp)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = stageTagLabel,
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun TrackingStagesStepper(
    currentStage: Int,
    language: AppLanguage
) {
    val stages = listOf(
        Pair("1", AppStrings.statusReceived(language)),
        Pair("2", AppStrings.statusPreparing(language)),
        Pair("3", AppStrings.statusOnTheWay(language)),
        Pair("4", AppStrings.statusArrived(language)),
        Pair("5", AppStrings.statusDelivered(language))
    )

    Column(modifier = Modifier.fillMaxWidth()) {
        stages.forEachIndexed { index, (num, label) ->
            val isDone = index < currentStage
            val isCurrent = index == currentStage
            val isPending = index > currentStage

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Circle Step
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(
                            when {
                                isDone -> AzizMint
                                isCurrent -> AzizOrangePrimary
                                else -> MaterialTheme.colorScheme.surfaceVariant
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (isDone) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    } else {
                        Text(
                            text = num,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isCurrent) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = label,
                    fontSize = 13.sp,
                    fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                    color = when {
                        isCurrent -> AzizOrangePrimary
                        isDone -> MaterialTheme.colorScheme.onSurface
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }
        }
    }
}

@Composable
fun ReviewSubmissionDialog(
    order: OrderEntity,
    language: AppLanguage,
    onDismiss: () -> Unit,
    onSubmit: (storeRating: Int, courierRating: Int, tags: List<String>, comment: String) -> Unit
) {
    var storeRating by remember { mutableIntStateOf(5) }
    var courierRating by remember { mutableIntStateOf(5) }
    var comment by remember { mutableStateOf("") }
    val selectedTags = remember { mutableStateListOf<String>() }

    val availableTags = listOf(
        "⚡ سريع جداً",
        "🔥 أكل ساخن ولذيذ",
        "📦 تغليف ممتاز",
        "🤝 مندوب خلوق ومحترم",
        "💯 دقة في الموعد"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = AppStrings.rateExperience(language),
                fontWeight = FontWeight.Black,
                fontSize = 17.sp,
                color = AzizOrangePrimary
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Store Rating
                Text(
                    text = "${AppStrings.storeRating(language)} (${order.storeName}):",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    (1..5).forEach { star ->
                        IconButton(onClick = { storeRating = star }) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "$star stars",
                                tint = if (star <= storeRating) GoldStarColor else Color.LightGray,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Courier Rating
                Text(
                    text = "${AppStrings.courierRating(language)} (عزيز):",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    (1..5).forEach { star ->
                        IconButton(onClick = { courierRating = star }) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "$star stars",
                                tint = if (star <= courierRating) GoldStarColor else Color.LightGray,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Quick tags
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(availableTags) { tag ->
                        val isSelected = selectedTags.contains(tag)
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) AzizOrangePrimary else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    if (isSelected) selectedTags.remove(tag) else selectedTags.add(tag)
                                }
                        ) {
                            Text(
                                text = tag,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = comment,
                    onValueChange = { comment = it },
                    placeholder = { Text(AppStrings.writeReviewPlaceholder(language), fontSize = 12.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3,
                    shape = RoundedCornerShape(10.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSubmit(storeRating, courierRating, selectedTags.toList(), comment) },
                colors = ButtonDefaults.buttonColors(containerColor = AzizOrangePrimary),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(AppStrings.submitReview(language), color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء")
            }
        }
    )
}
