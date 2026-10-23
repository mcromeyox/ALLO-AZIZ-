package com.example.ui.components

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.LocationSearching
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Remove
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.data.local.OrderEntity
import com.example.data.location.LocationServiceManager
import com.example.data.location.MohammediaLocationPreset
import com.example.ui.theme.AzizAmberSecondary
import com.example.ui.theme.AzizCharcoalDark
import com.example.ui.theme.AzizMint
import com.example.ui.theme.AzizOrangePrimary
import org.maplibre.android.MapLibre
import org.maplibre.android.annotations.MarkerOptions
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.Style
import org.maplibre.android.style.layers.RasterLayer
import org.maplibre.android.style.sources.RasterSource
import org.maplibre.android.style.sources.TileSet
import java.util.Locale

/**
 * Tile Style Types for OpenStreetMap via MapLibre Native SDK
 */
enum class OsmMapStyle(val labelAr: String, val labelFr: String, val labelEn: String) {
    STANDARD_OSM("خريطة OpenStreetMap", "Carte OSM Standard", "Standard OSM"),
    MAPLIBRE_DEMO("قمر صناعي / تضاريس", "Satellite / Relief", "Terrain / Satellite"),
    CARTO_VOYAGER("شوارع واضحة (Voyager)", "Rues Claires (Voyager)", "Clear Streets")
}

const val OSM_STYLE_JSON = """
{
  "version": 8,
  "name": "OpenStreetMap",
  "sources": {
    "osm-tiles": {
      "type": "raster",
      "tiles": [
        "https://tile.openstreetmap.org/{z}/{x}/{y}.png"
      ],
      "tileSize": 256,
      "attribution": "© OpenStreetMap contributors"
    }
  },
  "layers": [
    {
      "id": "osm-tiles-layer",
      "type": "raster",
      "source": "osm-tiles",
      "minzoom": 0,
      "maxzoom": 19
    }
  ]
}
"""

const val CARTO_STYLE_JSON = """
{
  "version": 8,
  "name": "CartoVoyager",
  "sources": {
    "carto-tiles": {
      "type": "raster",
      "tiles": [
        "https://basemaps.cartocdn.com/rastertiles/voyager/{z}/{x}/{y}.png"
      ],
      "tileSize": 256,
      "attribution": "© OpenStreetMap contributors, © CARTO"
    }
  },
  "layers": [
    {
      "id": "carto-tiles-layer",
      "type": "raster",
      "source": "carto-tiles",
      "minzoom": 0,
      "maxzoom": 19
    }
  ]
}
"""

/**
 * Builds an OpenStreetMap Tile Style for MapLibre Native
 */
fun buildOsmStyle(styleType: OsmMapStyle = OsmMapStyle.STANDARD_OSM): Style.Builder {
    return when (styleType) {
        OsmMapStyle.MAPLIBRE_DEMO -> {
            Style.Builder().fromUri("https://demotiles.maplibre.org/style.json")
        }
        OsmMapStyle.CARTO_VOYAGER -> {
            Style.Builder().fromJson(CARTO_STYLE_JSON)
        }
        OsmMapStyle.STANDARD_OSM -> {
            Style.Builder().fromJson(OSM_STYLE_JSON)
        }
    }
}

/**
 * Reusable Compose wrapper for MapLibre MapView with strict lifecycle wiring.
 */
@Composable
fun MapLibreMapViewContainer(
    modifier: Modifier = Modifier,
    initialLat: Double = LocationServiceManager.DEFAULT_LAT,
    initialLng: Double = LocationServiceManager.DEFAULT_LNG,
    initialZoom: Double = 14.5,
    mapStyle: OsmMapStyle = OsmMapStyle.STANDARD_OSM,
    onMapReady: (MapLibreMap) -> Unit = {}
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // Ensure MapLibre native library is initialized
    remember {
        try {
            MapLibre.getInstance(context.applicationContext)
        } catch (_: Exception) {}
    }

    val mapView = remember {
        MapView(context).apply {
            onCreate(null)
        }
    }

    DisposableEffect(lifecycleOwner, mapView) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> mapView.onStart()
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                Lifecycle.Event.ON_STOP -> mapView.onStop()
                Lifecycle.Event.ON_DESTROY -> mapView.onDestroy()
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            try {
                mapView.onDestroy()
            } catch (_: Exception) {}
        }
    }

    AndroidView(
        modifier = modifier,
        factory = {
            mapView.apply {
                getMapAsync { map ->
                    map.uiSettings.isAttributionEnabled = true
                    map.uiSettings.isLogoEnabled = false
                    map.uiSettings.isCompassEnabled = true
                    map.uiSettings.isZoomGesturesEnabled = true
                    map.uiSettings.isScrollGesturesEnabled = true
                    map.uiSettings.isRotateGesturesEnabled = false

                    map.cameraPosition = CameraPosition.Builder()
                        .target(LatLng(initialLat, initialLng))
                        .zoom(initialZoom)
                        .build()

                    map.setStyle(buildOsmStyle(mapStyle)) {
                        onMapReady(map)
                    }
                }
            }
        },
        update = {
            // Style or state updates handled via onMapReady callback
        }
    )
}

/**
 * Customer Delivery Location Picker using MapLibre and OpenStreetMap.
 * Completely replaces Google Maps for coordinate selection with OpenStreetMap tiles.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerMapLibreLocationDialog(
    initialLat: Double,
    initialLng: Double,
    initialAddress: String,
    onDismissRequest: () -> Unit,
    onConfirmLocation: (lat: Double, lng: Double, fullAddress: String) -> Unit,
    onRequestGpsLocation: (onSuccess: (Double, Double, String) -> Unit) -> Unit
) {
    val context = LocalContext.current
    var selectedLat by remember {
        mutableDoubleStateOf(if (initialLat != 0.0) initialLat else LocationServiceManager.DEFAULT_LAT)
    }
    var selectedLng by remember {
        mutableDoubleStateOf(if (initialLng != 0.0) initialLng else LocationServiceManager.DEFAULT_LNG)
    }
    var addressTitle by remember { mutableStateOf(initialAddress.ifBlank { "شارع الحسن الثاني، المحمدية 🇲🇦" }) }
    var buildingDetails by remember { mutableStateOf("") }
    var specialInstructions by remember { mutableStateOf("") }
    var selectedTag by remember { mutableStateOf("المنزل 🏠") }
    var mapStyle by remember { mutableStateOf(OsmMapStyle.STANDARD_OSM) }
    var isFetchingGps by remember { mutableStateOf(false) }
    var mapInstance by remember { mutableStateOf<MapLibreMap?>(null) }

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
                    .testTag("customer_maplibre_osm_dialog")
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
                                text = "تحديد موقعك (خريطة OpenStreetMap)",
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

                    // Style Switcher (Standard OSM / Carto / Demo)
                    IconButton(
                        onClick = {
                            mapStyle = when (mapStyle) {
                                OsmMapStyle.STANDARD_OSM -> OsmMapStyle.CARTO_VOYAGER
                                OsmMapStyle.CARTO_VOYAGER -> OsmMapStyle.MAPLIBRE_DEMO
                                OsmMapStyle.MAPLIBRE_DEMO -> OsmMapStyle.STANDARD_OSM
                            }
                            mapInstance?.setStyle(buildOsmStyle(mapStyle))
                        },
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("toggle_map_style_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Layers,
                            contentDescription = "Toggle Map Style",
                            tint = AzizOrangePrimary
                        )
                    }
                }

                // Interactive Map View Area (MapLibre Native + OpenStreetMap)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(290.dp)
                ) {
                    MapLibreMapViewContainer(
                        modifier = Modifier.fillMaxSize(),
                        initialLat = selectedLat,
                        initialLng = selectedLng,
                        initialZoom = 15.0,
                        mapStyle = mapStyle,
                        onMapReady = { map ->
                            mapInstance = map

                            // Listen for camera moves to update coordinates
                            map.addOnCameraMoveListener {
                                val center = map.cameraPosition.target
                                if (center != null) {
                                    selectedLat = center.latitude
                                    selectedLng = center.longitude
                                }
                            }

                            map.addOnCameraIdleListener {
                                val center = map.cameraPosition.target
                                if (center != null) {
                                    selectedLat = center.latitude
                                    selectedLng = center.longitude
                                    addressTitle = LocationServiceManager.reverseGeocodeCoordinates(
                                        context,
                                        selectedLat,
                                        selectedLng
                                    )
                                }
                            }

                            // Click to pan and place marker
                            map.addOnMapClickListener { point ->
                                selectedLat = point.latitude
                                selectedLng = point.longitude
                                map.animateCamera(CameraUpdateFactory.newLatLng(point))
                                addressTitle = LocationServiceManager.reverseGeocodeCoordinates(
                                    context,
                                    selectedLat,
                                    selectedLng
                                )
                                true
                            }
                        }
                    )

                    // Center Pin Indicator
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Place,
                            contentDescription = "Center Pin",
                            tint = AzizOrangePrimary,
                            modifier = Modifier
                                .size(42.dp)
                                .shadow(4.dp, shape = CircleShape)
                        )
                        Box(
                            modifier = Modifier
                                .size(8.dp, 4.dp)
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.4f))
                        )
                    }

                    // Top Floating Coordinates Badge
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Color.Black.copy(alpha = 0.8f),
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
                                text = "• MapLibre 🎯",
                                fontSize = 9.sp,
                                color = AzizMint
                            )
                        }
                    }

                    // Mandatory OpenStreetMap Attribution Badge
                    Surface(
                        shape = RoundedCornerShape(topStart = 8.dp),
                        color = Color.Black.copy(alpha = 0.65f),
                        modifier = Modifier.align(Alignment.BottomEnd)
                    ) {
                        Text(
                            text = "© OpenStreetMap contributors",
                            fontSize = 9.sp,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    // Map Zoom & GPS Controls
                    Column(
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color.White.copy(alpha = 0.94f),
                            shadowElevation = 3.dp,
                            modifier = Modifier.size(36.dp)
                        ) {
                            IconButton(
                                onClick = {
                                    mapInstance?.let { map ->
                                        map.animateCamera(CameraUpdateFactory.zoomIn())
                                    }
                                },
                                modifier = Modifier.fillMaxSize()
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Zoom In", tint = Color.Black, modifier = Modifier.size(18.dp))
                            }
                        }

                        Surface(
                            shape = CircleShape,
                            color = Color.White.copy(alpha = 0.94f),
                            shadowElevation = 3.dp,
                            modifier = Modifier.size(36.dp)
                        ) {
                            IconButton(
                                onClick = {
                                    mapInstance?.let { map ->
                                        map.animateCamera(CameraUpdateFactory.zoomOut())
                                    }
                                },
                                modifier = Modifier.fillMaxSize()
                            ) {
                                Icon(Icons.Default.Remove, contentDescription = "Zoom Out", tint = Color.Black, modifier = Modifier.size(18.dp))
                            }
                        }

                        // My Location GPS Button
                        Surface(
                            shape = CircleShape,
                            color = AzizOrangePrimary,
                            shadowElevation = 4.dp,
                            modifier = Modifier.size(36.dp)
                        ) {
                            IconButton(
                                onClick = {
                                    isFetchingGps = true
                                    onRequestGpsLocation { lat, lng, addr ->
                                        isFetchingGps = false
                                        selectedLat = lat
                                        selectedLng = lng
                                        if (addr.isNotBlank()) addressTitle = addr
                                        mapInstance?.animateCamera(
                                            CameraUpdateFactory.newLatLngZoom(LatLng(lat, lng), 16.0)
                                        )
                                    }
                                },
                                modifier = Modifier.fillMaxSize()
                            ) {
                                if (isFetchingGps) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(18.dp),
                                        color = Color.White,
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.MyLocation,
                                        contentDescription = "My GPS Location",
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Location Details & Presets Form
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    // Quick Neighborhood Presets (Mohammedia, Morocco)
                    Text(
                        text = "أحياء ومعالم المحمدية الشهيرة:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(LocationServiceManager.MOHAMMEDIA_PRESETS) { preset ->
                            val isSelected = (Math.abs(selectedLat - preset.lat) < 0.002 && Math.abs(selectedLng - preset.lng) < 0.002)
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) AzizOrangePrimary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant,
                                border = if (isSelected) androidx.compose.foundation.BorderStroke(1.dp, AzizOrangePrimary) else null,
                                modifier = Modifier.clickable {
                                    selectedLat = preset.lat
                                    selectedLng = preset.lng
                                    addressTitle = "${preset.nameAr} - ${preset.district}، المحمدية"
                                    mapInstance?.animateCamera(
                                        CameraUpdateFactory.newLatLngZoom(LatLng(preset.lat, preset.lng), 15.5)
                                    )
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
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) AzizOrangePrimary else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Address Title TextField
                    OutlinedTextField(
                        value = addressTitle,
                        onValueChange = { addressTitle = it },
                        label = { Text("العنوان الرئيسي / الشارع") },
                        leadingIcon = {
                            Icon(Icons.Default.Place, contentDescription = null, tint = AzizOrangePrimary)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Building / Apartment details
                    OutlinedTextField(
                        value = buildingDetails,
                        onValueChange = { buildingDetails = it },
                        label = { Text("رقم العمارة / الطابق / رقم الشقة (اختياري)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Courier delivery instructions
                    OutlinedTextField(
                        value = specialInstructions,
                        onValueChange = { specialInstructions = it },
                        label = { Text("تعليمات لمندوب التوصيل (مثل: قرب الصيدلية)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        maxLines = 2
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Address Type Tags (Home / Work / Other)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("المنزل 🏠", "العمل 💼", "أخرى 📍").forEach { tag ->
                            val isSelected = selectedTag == tag
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = if (isSelected) AzizOrangePrimary else MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { selectedTag = tag }
                            ) {
                                Text(
                                    text = tag,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Confirm Location Button
                    Button(
                        onClick = {
                            val combinedAddress = buildString {
                                append(addressTitle.trim())
                                if (buildingDetails.isNotBlank()) {
                                    append("، ${buildingDetails.trim()}")
                                }
                                if (specialInstructions.isNotBlank()) {
                                    append(" (${specialInstructions.trim()})")
                                }
                            }
                            onConfirmLocation(selectedLat, selectedLng, combinedAddress)
                            onDismissRequest()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("confirm_maplibre_location_button"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AzizOrangePrimary)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "تأكيد موقع التوصيل 🛵",
                            fontSize = 15.sp,
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
 * Realistic Live Delivery Map for Order Tracking using MapLibre & OpenStreetMap.
 * Displays courier position, store origin, and delivery destination with live telemetry.
 */
@Composable
fun RealisticMapLibreDeliveryMap(
    order: OrderEntity,
    onOpenExternalMap: () -> Unit,
    onCallCourier: () -> Unit,
    onWhatsAppCourier: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var mapStyle by remember { mutableStateOf(OsmMapStyle.STANDARD_OSM) }
    var mapInstance by remember { mutableStateOf<MapLibreMap?>(null) }

    val distKm = remember(order.courierLat, order.courierLng, order.customerLat, order.customerLng) {
        LocationServiceManager.calculateDistanceKm(
            order.courierLat,
            order.courierLng,
            order.customerLat,
            order.customerLng
        )
    }
    val formattedDist = if (distKm < 1.0) "${(distKm * 1000).toInt()} متر" else "%.1f كم".format(distKm)
    val etaMins = remember(distKm) { LocationServiceManager.estimateMinutesLeft(distKm) }

    ElevatedCard(
        modifier = modifier
            .fillMaxWidth()
            .testTag("realistic_maplibre_delivery_map_card"),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(AzizCharcoalDark)
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
                        text = "تتبع مسار التوصيل المباشر (OpenStreetMap) 🛰️",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                // Map Style Switcher
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White.copy(alpha = 0.15f),
                    modifier = Modifier.clickable {
                        mapStyle = when (mapStyle) {
                            OsmMapStyle.STANDARD_OSM -> OsmMapStyle.CARTO_VOYAGER
                            OsmMapStyle.CARTO_VOYAGER -> OsmMapStyle.MAPLIBRE_DEMO
                            OsmMapStyle.MAPLIBRE_DEMO -> OsmMapStyle.STANDARD_OSM
                        }
                        mapInstance?.setStyle(buildOsmStyle(mapStyle))
                    }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Layers,
                            contentDescription = "Switch Map Style",
                            tint = AzizOrangePrimary,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = mapStyle.labelAr,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }

            // Real-time MapLibre Map View
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(230.dp)
            ) {
                MapLibreMapViewContainer(
                    modifier = Modifier.fillMaxSize(),
                    initialLat = if (order.courierLat != 0.0) order.courierLat else order.customerLat,
                    initialLng = if (order.courierLng != 0.0) order.courierLng else order.customerLng,
                    initialZoom = 14.2,
                    mapStyle = mapStyle,
                    onMapReady = { map ->
                        mapInstance = map

                        // Place markers for Courier, Store, and Customer
                        map.clear()

                        val customerPoint = LatLng(order.customerLat, order.customerLng)
                        map.addMarker(
                            MarkerOptions()
                                .position(customerPoint)
                                .title("موقع الاستلام")
                                .snippet(order.deliveryAddress)
                        )

                        if (order.courierLat != 0.0 && order.courierLng != 0.0) {
                            val courierPoint = LatLng(order.courierLat, order.courierLng)
                            map.addMarker(
                                MarkerOptions()
                                    .position(courierPoint)
                                    .title(order.courierName)
                                    .snippet("الكابتن في الطريق 🛵")
                            )

                            // Center on courier with smooth zoom
                            map.animateCamera(CameraUpdateFactory.newLatLng(courierPoint))
                        }
                    }
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
                        .padding(bottom = 22.dp, end = 10.dp),
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

                // Mandatory OpenStreetMap Attribution
                Surface(
                    shape = RoundedCornerShape(topStart = 8.dp),
                    color = Color.Black.copy(alpha = 0.65f),
                    modifier = Modifier.align(Alignment.BottomEnd)
                ) {
                    Text(
                        text = "© OpenStreetMap contributors",
                        fontSize = 9.sp,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                // Zoom controls
                Column(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = Color.White.copy(alpha = 0.92f),
                        shadowElevation = 2.dp,
                        modifier = Modifier.size(30.dp)
                    ) {
                        IconButton(
                            onClick = { mapInstance?.animateCamera(CameraUpdateFactory.zoomIn()) },
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Zoom In", tint = Color.Black, modifier = Modifier.size(16.dp))
                        }
                    }
                    Surface(
                        shape = CircleShape,
                        color = Color.White.copy(alpha = 0.92f),
                        shadowElevation = 2.dp,
                        modifier = Modifier.size(30.dp)
                    ) {
                        IconButton(
                            onClick = { mapInstance?.animateCamera(CameraUpdateFactory.zoomOut()) },
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Icon(Icons.Default.Remove, contentDescription = "Zoom Out", tint = Color.Black, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }

            // Real-time Telemetry & Contact Courier Row
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
                        Icon(
                            imageVector = Icons.Default.TwoWheeler,
                            contentDescription = null,
                            tint = AzizOrangePrimary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "المسافة المتبقية: $formattedDist",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "الوقت التقديري للوصول: ~$etaMins دقيقة ⏱️",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Action Buttons
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    FilledTonalButton(
                        onClick = onCallCourier,
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.Call, contentDescription = "Call", modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("اتصال", fontSize = 11.sp)
                    }

                    Button(
                        onClick = onOpenExternalMap,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AzizOrangePrimary),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.Navigation, contentDescription = "Navigate", modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("ملاحة", fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

// Backward compatibility composables so any existing caller transitions seamlessly
@Composable
fun CustomerGoogleMapLocationDialog(
    initialLat: Double,
    initialLng: Double,
    initialAddress: String,
    onDismissRequest: () -> Unit,
    onConfirmLocation: (lat: Double, lng: Double, fullAddress: String) -> Unit,
    onRequestGpsLocation: (onSuccess: (Double, Double, String) -> Unit) -> Unit
) {
    CustomerMapLibreLocationDialog(
        initialLat = initialLat,
        initialLng = initialLng,
        initialAddress = initialAddress,
        onDismissRequest = onDismissRequest,
        onConfirmLocation = onConfirmLocation,
        onRequestGpsLocation = onRequestGpsLocation
    )
}

@Composable
fun RealisticGoogleDeliveryMap(
    order: OrderEntity,
    onOpenGoogleMapsApp: () -> Unit,
    onCallCourier: () -> Unit,
    onWhatsAppCourier: () -> Unit,
    modifier: Modifier = Modifier
) {
    RealisticMapLibreDeliveryMap(
        order = order,
        onOpenExternalMap = onOpenGoogleMapsApp,
        onCallCourier = onCallCourier,
        onWhatsAppCourier = onWhatsAppCourier,
        modifier = modifier
    )
}
