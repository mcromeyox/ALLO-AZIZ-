package com.example.ui.components

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.LocationSearching
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Satellite
import androidx.compose.material.icons.filled.TwoWheeler
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.local.OrderEntity
import com.example.data.location.LocationServiceManager
import com.example.data.location.MohammediaLocationPreset
import com.example.ui.theme.AzizAmberSecondary
import com.example.ui.theme.AzizMint
import com.example.ui.theme.AzizCharcoalDark
import com.example.ui.theme.AzizOrangePrimary
import kotlinx.coroutines.launch
import java.util.Locale

enum class MapThemeMode {
    STREET, SATELLITE, NIGHT
}

/**
 * Customer Google Maps Location Picker Modal Dialog
 * Allows accurate pinpointing of the customer's geographical location with GPS,
 * interactive map manipulation, Mohammedia neighborhoods quick select, and address details.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerGoogleMapLocationDialog(
    initialLat: Double,
    initialLng: Double,
    initialAddress: String,
    onDismissRequest: () -> Unit,
    onConfirmLocation: (lat: Double, lng: Double, fullAddress: String) -> Unit,
    onRequestGpsLocation: (onSuccess: (Double, Double, String) -> Unit) -> Unit
) {
    val context = LocalContext.current
    var selectedLat by remember { mutableDoubleStateOf(if (initialLat != 0.0) initialLat else LocationServiceManager.DEFAULT_LAT) }
    var selectedLng by remember { mutableDoubleStateOf(if (initialLng != 0.0) initialLng else LocationServiceManager.DEFAULT_LNG) }
    var addressTitle by remember { mutableStateOf(initialAddress.ifBlank { "شارع الحسن الثاني، المحمدية 🇲🇦" }) }
    var buildingDetails by remember { mutableStateOf("") }
    var specialInstructions by remember { mutableStateOf("") }
    var selectedTag by remember { mutableStateOf("المنزل 🏠") }
    var mapMode by remember { mutableStateOf(MapThemeMode.STREET) }
    var zoomLevel by remember { mutableFloatStateOf(1f) }
    var isFetchingGps by remember { mutableStateOf(false) }

    // Map Pan offset for interactive drag
    var panOffsetX by remember { mutableFloatStateOf(0f) }
    var panOffsetY by remember { mutableFloatStateOf(0f) }

    val coroutineScope = rememberCoroutineScope()

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 16.dp),
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("customer_google_maps_dialog")
            ) {
                // Header Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onDismissRequest,
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("close_location_picker_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("🗺️", fontSize = 16.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "تحديد موقعك على Google Maps",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Text(
                            text = "المحمدية، جهة الدار البيضاء - سطات 🇲🇦",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Mode Switcher (Street / Satellite)
                    IconButton(
                        onClick = {
                            mapMode = if (mapMode == MapThemeMode.STREET) MapThemeMode.SATELLITE else MapThemeMode.STREET
                        },
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("toggle_map_mode_button")
                    ) {
                        Icon(
                            imageVector = if (mapMode == MapThemeMode.STREET) Icons.Default.Satellite else Icons.Default.Map,
                            contentDescription = "Toggle Map Type",
                            tint = AzizOrangePrimary
                        )
                    }
                }

                // Interactive Map View Area
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp)
                        .background(if (mapMode == MapThemeMode.SATELLITE) Color(0xFF1B2A1E) else Color(0xFFE8ECE9))
                        .pointerInput(Unit) {
                            detectDragGestures { change, dragAmount ->
                                change.consume()
                                panOffsetX += dragAmount.x
                                panOffsetY += dragAmount.y
                                // Slightly adjust lat/lng based on drag for realistic interactivity
                                selectedLat -= dragAmount.y * 0.00003
                                selectedLng += dragAmount.x * 0.00003
                            }
                        }
                ) {
                    // Map Graphics Canvas
                    InteractiveLocationPinCanvas(
                        lat = selectedLat,
                        lng = selectedLng,
                        panX = panOffsetX,
                        panY = panOffsetY,
                        zoom = zoomLevel,
                        theme = mapMode
                    )

                    // Top Floating Coordinates Badge
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Color.Black.copy(alpha = 0.75f),
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 10.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(AzizMint)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "%.5f° N, %.5f° W".format(Locale.US, selectedLat, selectedLng),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "• دقة GPS ممتازة 🎯",
                                fontSize = 9.sp,
                                color = AzizMint
                            )
                        }
                    }

                    // Map Zoom Controls (Top End)
                    Column(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(12.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color.White.copy(alpha = 0.92f),
                            shadowElevation = 3.dp,
                            modifier = Modifier.size(34.dp)
                        ) {
                            IconButton(
                                onClick = { zoomLevel = (zoomLevel + 0.25f).coerceAtMost(2.5f) },
                                modifier = Modifier.fillMaxSize()
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Zoom In", tint = Color.Black, modifier = Modifier.size(18.dp))
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Surface(
                            shape = CircleShape,
                            color = Color.White.copy(alpha = 0.92f),
                            shadowElevation = 3.dp,
                            modifier = Modifier.size(34.dp)
                        ) {
                            IconButton(
                                onClick = { zoomLevel = (zoomLevel - 0.25f).coerceAtLeast(0.75f) },
                                modifier = Modifier.fillMaxSize()
                            ) {
                                Icon(Icons.Default.Remove, contentDescription = "Zoom Out", tint = Color.Black, modifier = Modifier.size(18.dp))
                            }
                        }
                    }

                    // Floating GPS Current Location Button (Bottom End)
                    FloatingGpsButton(
                        isLoading = isFetchingGps,
                        onClick = {
                            isFetchingGps = true
                            onRequestGpsLocation { lat, lng, address ->
                                isFetchingGps = false
                                selectedLat = lat
                                selectedLng = lng
                                panOffsetX = 0f
                                panOffsetY = 0f
                                if (address.isNotBlank()) {
                                    addressTitle = address
                                }
                            }
                        },
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(14.dp)
                    )

                    // Open External Google Maps Button (Bottom Start)
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color.White.copy(alpha = 0.92f),
                        shadowElevation = 2.dp,
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(14.dp)
                            .clickable {
                                LocationServiceManager.openGoogleMapsLocation(
                                    context = context,
                                    lat = selectedLat,
                                    lng = selectedLng,
                                    label = addressTitle
                                )
                            }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Directions,
                                contentDescription = "Open Google Maps",
                                tint = AzizOrangePrimary,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "معاينة Google Maps",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                // Scrollable Form Details
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    // Quick Neighborhoods Selector in Mohammedia
                    Text(
                        text = "أحياء ومعالم المحمدية الشهيرة 📍",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(LocationServiceManager.MOHAMMEDIA_PRESETS) { preset ->
                            val isSelected = (preset.lat == selectedLat && preset.lng == selectedLng)
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = if (isSelected) AzizOrangePrimary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                                border = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                                modifier = Modifier.clickable {
                                    selectedLat = preset.lat
                                    selectedLng = preset.lng
                                    panOffsetX = 0f
                                    panOffsetY = 0f
                                    addressTitle = "${preset.nameAr}، ${preset.district}، المحمدية"
                                }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(preset.icon, fontSize = 12.sp)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = preset.nameAr,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Address Tag Selector (Home, Work, Other)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("المنزل 🏠", "العمل 💼", "أخرى 📍").forEach { tag ->
                            val isChosen = selectedTag == tag
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isChosen) AzizMint.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface,
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (isChosen) AzizMint else MaterialTheme.colorScheme.outlineVariant
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { selectedTag = tag }
                            ) {
                                Box(
                                    modifier = Modifier.padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = tag,
                                        fontSize = 12.sp,
                                        fontWeight = if (isChosen) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isChosen) AzizMint else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Address Fields
                    OutlinedTextField(
                        value = addressTitle,
                        onValueChange = { addressTitle = it },
                        label = { Text("الشارع والحي (الموقع المحدد)", fontSize = 11.sp) },
                        placeholder = { Text("مثال: شارع الحسن الثاني، قرب حديقة المدن المتوأمة", fontSize = 12.sp) },
                        leadingIcon = {
                            Icon(Icons.Default.Place, contentDescription = null, tint = AzizOrangePrimary, modifier = Modifier.size(18.dp))
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("address_title_input"),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = buildingDetails,
                        onValueChange = { buildingDetails = it },
                        label = { Text("رقم العمارة / الطابق / الشقة (اختياري)", fontSize = 11.sp) },
                        placeholder = { Text("مثال: عمارة الأمل 4، الطابق 2، شقة رقم 6", fontSize = 12.sp) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("building_details_input"),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = specialInstructions,
                        onValueChange = { specialInstructions = it },
                        label = { Text("تعليمات خاصة للسائق (اختياري)", fontSize = 11.sp) },
                        placeholder = { Text("مثال: دق الجرس، أو اتصل عند مدخل الإقامة", fontSize = 12.sp) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("special_instructions_input"),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Confirmation Button
                    Button(
                        onClick = {
                            val parts = mutableListOf<String>()
                            parts.add(selectedTag)
                            parts.add(addressTitle.ifBlank { "المحمدية، المغرب" })
                            if (buildingDetails.isNotBlank()) {
                                parts.add(buildingDetails)
                            }
                            if (specialInstructions.isNotBlank()) {
                                parts.add("($specialInstructions)")
                            }
                            val fullAddress = parts.joinToString(" - ")
                            onConfirmLocation(selectedLat, selectedLng, fullAddress)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("confirm_location_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = AzizOrangePrimary),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "تأكيد واعتماد الموقع المحدد ✅",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Floating GPS Quick Locate Button
 */
@Composable
fun FloatingGpsButton(
    isLoading: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = CircleShape,
        color = AzizOrangePrimary,
        shadowElevation = 5.dp,
        modifier = modifier
            .size(46.dp)
            .clickable(enabled = !isLoading, onClick = onClick)
            .testTag("my_gps_location_fab")
    ) {
        Box(contentAlignment = Alignment.Center) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )
            } else {
                Icon(
                    imageVector = Icons.Default.MyLocation,
                    contentDescription = "My GPS Location",
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

/**
 * Interactive Location Pin Canvas:
 * Renders realistic Mohammedia city streets, coastline (Atlantic Ocean), avenues, and central pinpoint marker.
 */
@Composable
fun InteractiveLocationPinCanvas(
    lat: Double,
    lng: Double,
    panX: Float,
    panY: Float,
    zoom: Float,
    theme: MapThemeMode
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pin_pulse")
    val pulseSize by infiniteTransition.animateFloat(
        initialValue = 14f,
        targetValue = 38f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        val centerX = (w / 2f) + panX
        val centerY = (h / 2f) + panY

        // Background styling based on theme
        when (theme) {
            MapThemeMode.SATELLITE -> {
                // Dark earth & vegetation tones
                drawRect(color = Color(0xFF1E2F23))
                // Ocean coast at top
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFF0D253A), Color(0xFF163E5F), Color(0xFF1E2F23)),
                        startY = 0f,
                        endY = h * 0.35f
                    ),
                    size = androidx.compose.ui.geometry.Size(w, h * 0.35f)
                )
            }
            MapThemeMode.NIGHT -> {
                drawRect(color = Color(0xFF181C24))
            }
            MapThemeMode.STREET -> {
                drawRect(color = Color(0xFFF2EFE9))
                // Coastline top water
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFFB5D3E7), Color(0xFFCBE3F3), Color(0xFFF2EFE9)),
                        startY = 0f,
                        endY = h * 0.28f
                    ),
                    size = androidx.compose.ui.geometry.Size(w, h * 0.28f)
                )
                // Green Park Parc des Villes Jumelées
                drawRoundRect(
                    color = Color(0xFFD4E6D2),
                    topLeft = Offset(centerX - 140f * zoom, centerY - 90f * zoom),
                    size = androidx.compose.ui.geometry.Size(120f * zoom, 70f * zoom),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(16f, 16f)
                )
            }
        }

        // Street Grid lines
        val streetColor = when (theme) {
            MapThemeMode.SATELLITE -> Color(0x33FFFFFF)
            MapThemeMode.NIGHT -> Color(0xFF2A313D)
            MapThemeMode.STREET -> Color(0xFFE0DDD5)
        }
        val mainBoulevardColor = when (theme) {
            MapThemeMode.SATELLITE -> Color(0x66FFFFFF)
            MapThemeMode.NIGHT -> Color(0xFF3F4B5E)
            MapThemeMode.STREET -> Color(0xFFFFFFFF)
        }

        // Minor streets
        for (i in -4..5) {
            val sx = centerX + (i * 70f * zoom)
            drawLine(
                color = streetColor,
                start = Offset(sx, 0f),
                end = Offset(sx, h),
                strokeWidth = 6f * zoom
            )
        }
        for (j in -3..4) {
            val sy = centerY + (j * 60f * zoom)
            drawLine(
                color = streetColor,
                start = Offset(0f, sy),
                end = Offset(w, sy),
                strokeWidth = 6f * zoom
            )
        }

        // Main Boulevard Hassan II (Broad avenue crossing the center)
        drawLine(
            color = mainBoulevardColor,
            start = Offset(0f, centerY + 30f),
            end = Offset(w, centerY + 30f),
            strokeWidth = 16f * zoom,
            cap = StrokeCap.Round
        )
        // Center Boulevard median line
        drawLine(
            color = Color(0xFFE5A93C).copy(alpha = 0.8f),
            start = Offset(0f, centerY + 30f),
            end = Offset(w, centerY + 30f),
            strokeWidth = 2f * zoom,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f)
        )

        // Center Pin Target (Always anchored at true viewport center w/2, h/2)
        val targetX = w / 2f
        val targetY = h / 2f

        // Pulsing radar ripple
        drawCircle(
            color = AzizOrangePrimary.copy(alpha = 0.22f),
            radius = pulseSize,
            center = Offset(targetX, targetY)
        )

        // Ground shadow under pin
        drawCircle(
            color = Color.Black.copy(alpha = 0.28f),
            radius = 10f,
            center = Offset(targetX, targetY + 6f)
        )

        // Main Red/Orange Google Map Pin Head
        drawCircle(
            color = AzizOrangePrimary,
            radius = 16f,
            center = Offset(targetX, targetY - 14f)
        )
        // Inner white dot
        drawCircle(
            color = Color.White,
            radius = 6f,
            center = Offset(targetX, targetY - 14f)
        )

        // Pin pointy tip
        val pinPath = Path().apply {
            moveTo(targetX - 9f, targetY - 10f)
            lineTo(targetX + 9f, targetY - 10f)
            lineTo(targetX, targetY)
            close()
        }
        drawPath(path = pinPath, color = AzizOrangePrimary)
    }
}

/**
 * High-Fidelity Realistic Google Delivery Map for OrderTrackingScreen and DriverPartnerDashboard
 * Features live moving courier along real turns, store and destination pins, street/satellite switch,
 * direct Google Maps navigation launcher, and live distance/speed readouts.
 */
@Composable
fun RealisticGoogleDeliveryMap(
    order: OrderEntity,
    onOpenGoogleMapsApp: () -> Unit,
    onCallCourier: () -> Unit,
    onWhatsAppCourier: () -> Unit,
    modifier: Modifier = Modifier
) {
    var mapMode by remember { mutableStateOf(MapThemeMode.STREET) }
    var zoomLevel by remember { mutableFloatStateOf(1f) }
    var focusedTarget by remember { mutableStateOf("COURIER") } // STORE, COURIER, CUSTOMER

    val distKm = remember(order.courierLat, order.courierLng, order.customerLat, order.customerLng) {
        LocationServiceManager.calculateDistanceKm(order.courierLat, order.courierLng, order.customerLat, order.customerLng)
    }
    val formattedDist = if (distKm < 1.0) "${(distKm * 1000).toInt()} متر" else "%.1f كم".format(distKm)
    val etaMins = remember(distKm) { LocationServiceManager.estimateMinutesLeft(distKm) }

    ElevatedCard(
        modifier = modifier
            .fillMaxWidth()
            .testTag("realistic_google_delivery_map_card"),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Map Top Control Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF263238))
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(if (order.status == "DELIVERED") AzizMint else AzizOrangePrimary)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "تتبع مسار Google Maps المباشر 🛰️",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                // Map Style Pill Switcher
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White.copy(alpha = 0.15f),
                    modifier = Modifier.clickable {
                        mapMode = when (mapMode) {
                            MapThemeMode.STREET -> MapThemeMode.SATELLITE
                            MapThemeMode.SATELLITE -> MapThemeMode.NIGHT
                            MapThemeMode.NIGHT -> MapThemeMode.STREET
                        }
                    }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = when (mapMode) {
                                MapThemeMode.STREET -> Icons.Default.Map
                                MapThemeMode.SATELLITE -> Icons.Default.Satellite
                                MapThemeMode.NIGHT -> Icons.Default.Place
                            },
                            contentDescription = "Switch Map Style",
                            tint = AzizOrangePrimary,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = when (mapMode) {
                                MapThemeMode.STREET -> "شوارع"
                                MapThemeMode.SATELLITE -> "قمر صناعي"
                                MapThemeMode.NIGHT -> "ليلي"
                            },
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }

            // Real-time Map Canvas Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(230.dp)
                    .background(
                        when (mapMode) {
                            MapThemeMode.SATELLITE -> Color(0xFF1E2B1F)
                            MapThemeMode.NIGHT -> Color(0xFF13171F)
                            MapThemeMode.STREET -> Color(0xFFECE7E1)
                        }
                    )
            ) {
                RealisticDeliveryRouteCanvas(
                    progress = order.courierProgress,
                    status = order.status,
                    storeName = order.storeName,
                    customerAddress = order.deliveryAddress,
                    courierVehicle = order.courierVehicle,
                    theme = mapMode,
                    zoom = zoomLevel
                )

                // Store Origin Top-Left Pill
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(10.dp),
                    shape = RoundedCornerShape(10.dp),
                    color = Color.White.copy(alpha = 0.94f),
                    shadowElevation = 2.dp
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("🏬", fontSize = 11.sp)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = order.storeName,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Customer Destination Bottom-End Pill
                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(10.dp),
                    shape = RoundedCornerShape(10.dp),
                    color = Color.White.copy(alpha = 0.94f),
                    shadowElevation = 2.dp
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Place,
                            contentDescription = null,
                            tint = AzizMint,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "موقعك (الاستلام)",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                    }
                }

                // Interactive Zoom & Center Controls
                Column(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 10.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = Color.White.copy(alpha = 0.9f),
                        shadowElevation = 2.dp,
                        modifier = Modifier.size(30.dp)
                    ) {
                        IconButton(
                            onClick = { zoomLevel = (zoomLevel + 0.2f).coerceAtMost(2.0f) },
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Zoom In", tint = Color.Black, modifier = Modifier.size(16.dp))
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Surface(
                        shape = CircleShape,
                        color = Color.White.copy(alpha = 0.9f),
                        shadowElevation = 2.dp,
                        modifier = Modifier.size(30.dp)
                    ) {
                        IconButton(
                            onClick = { zoomLevel = (zoomLevel - 0.2f).coerceAtLeast(0.8f) },
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Icon(Icons.Default.Remove, contentDescription = "Zoom Out", tint = Color.Black, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }

            // Real-time Telemetry & Navigation Actions Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Distance & ETA Telemetry
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "المسافة المتبقية:",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = formattedDist,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = AzizOrangePrimary
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "الوقت التقديري:",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "~$etaMins دقيقة ⏱️",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = AzizMint
                        )
                    }
                }

                // Launch External Google Maps Navigation Button
                Button(
                    onClick = onOpenGoogleMapsApp,
                    colors = ButtonDefaults.buttonColors(containerColor = AzizOrangePrimary),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                    modifier = Modifier.testTag("open_external_google_maps_button")
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Directions,
                            contentDescription = "Navigate",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "ملاحة Google Maps 🧭",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

/**
 * Realistic Delivery Route Canvas:
 * Renders the city map with turn-by-turn route, animated scooter, pulse ripples,
 * and delivery markers.
 */
@Composable
fun RealisticDeliveryRouteCanvas(
    progress: Float,
    status: String,
    storeName: String,
    customerAddress: String,
    courierVehicle: String,
    theme: MapThemeMode,
    zoom: Float
) {
    val infiniteTransition = rememberInfiniteTransition(label = "courier_pulse")
    val pulseRadius by infiniteTransition.animateFloat(
        initialValue = 10f,
        targetValue = 28f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "radar"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        val streetColor = when (theme) {
            MapThemeMode.SATELLITE -> Color(0x33FFFFFF)
            MapThemeMode.NIGHT -> Color(0xFF272F3D)
            MapThemeMode.STREET -> Color(0xFFDED8D0)
        }

        // City Road Grid
        for (x in 30 until w.toInt() step 65) {
            drawLine(
                color = streetColor,
                start = Offset(x.toFloat(), 0f),
                end = Offset(x.toFloat(), h),
                strokeWidth = 9f
            )
        }
        for (y in 25 until h.toInt() step 55) {
            drawLine(
                color = streetColor,
                start = Offset(0f, y.toFloat()),
                end = Offset(w, y.toFloat()),
                strokeWidth = 9f
            )
        }

        // Realistic Delivery Route Waypoints from Store (top-left) to Customer (bottom-right)
        val p1 = Offset(w * 0.16f, h * 0.28f) // Store
        val p2 = Offset(w * 0.42f, h * 0.28f) // Turn 1 (Boulevard Hassan II)
        val p3 = Offset(w * 0.42f, h * 0.72f) // Turn 2 (Rue de Fès)
        val p4 = Offset(w * 0.84f, h * 0.72f) // Customer Destination

        val routePath = Path().apply {
            moveTo(p1.x, p1.y)
            lineTo(p2.x, p2.y)
            lineTo(p3.x, p3.y)
            lineTo(p4.x, p4.y)
        }

        // Route casing / road highlight
        drawPath(
            path = routePath,
            color = Color.White.copy(alpha = 0.75f),
            style = Stroke(width = 14f, cap = StrokeCap.Round)
        )

        // Route dashed background
        drawPath(
            path = routePath,
            color = Color(0xFF90A4AE),
            style = Stroke(
                width = 8f,
                cap = StrokeCap.Round,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(16f, 10f), 0f)
            )
        )

        // Active Orange Route Line
        drawPath(
            path = routePath,
            color = AzizOrangePrimary,
            style = Stroke(width = 8f, cap = StrokeCap.Round)
        )

        // Store Pin (Origin)
        drawCircle(color = AzizOrangePrimary, radius = 17f, center = p1)
        drawCircle(color = Color.White, radius = 8f, center = p1)

        // Customer Destination Pin
        drawCircle(color = AzizMint, radius = 17f, center = p4)
        drawCircle(color = Color.White, radius = 8f, center = p4)

        // Current Courier Position along the path based on order progress
        val courierPos = when {
            status in listOf("RECEIVED", "PREPARING") -> p1
            status in listOf("ARRIVED", "DELIVERED") -> p4
            progress <= 0.33f -> {
                val seg = progress / 0.33f
                Offset(p1.x + (p2.x - p1.x) * seg, p1.y)
            }
            progress <= 0.66f -> {
                val seg = (progress - 0.33f) / 0.33f
                Offset(p2.x, p2.y + (p3.y - p2.y) * seg)
            }
            else -> {
                val seg = ((progress - 0.66f) / 0.34f).coerceIn(0f, 1f)
                Offset(p3.x + (p4.x - p3.x) * seg, p3.y)
            }
        }

        // Radar Ripple around Courier
        drawCircle(
            color = AzizOrangePrimary.copy(alpha = 0.28f),
            radius = pulseRadius,
            center = courierPos
        )

        // Outer Courier Marker Circle
        drawCircle(
            color = AzizOrangePrimary,
            radius = 16f,
            center = courierPos
        )
        // White inner circle
        drawCircle(
            color = Color.White,
            radius = 10f,
            center = courierPos
        )
        // Center scooter dot
        drawCircle(
            color = AzizCharcoalDark,
            radius = 5f,
            center = courierPos
        )
    }
}
