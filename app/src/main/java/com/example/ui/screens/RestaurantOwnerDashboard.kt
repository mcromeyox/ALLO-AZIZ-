package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.local.OrderEntity
import com.example.data.local.ProductEntity
import com.example.data.local.StoreEntity
import com.example.data.model.AppLanguage
import com.example.ui.theme.AzizAmberSecondary
import com.example.ui.theme.AzizMint
import com.example.ui.theme.AzizOrangePrimary

// Preset real appetizing food photography catalog for quick selection
data class FoodPhotoPreset(
    val titleAr: String,
    val imageUrl: String,
    val emoji: String
)

val PRESET_FOOD_PHOTOS = listOf(
    FoodPhotoPreset(
        titleAr = "برجر مشوي فاخر",
        imageUrl = "https://images.unsplash.com/photo-1568901346375-23c9450c58cd?w=600&auto=format&fit=crop&q=80",
        emoji = "🍔"
    ),
    FoodPhotoPreset(
        titleAr = "بيتزا إيطالية بالجبن",
        imageUrl = "https://images.unsplash.com/photo-1513104890138-7c749659a591?w=600&auto=format&fit=crop&q=80",
        emoji = "🍕"
    ),
    FoodPhotoPreset(
        titleAr = "دجاج مقرمش ذهبي",
        imageUrl = "https://images.unsplash.com/photo-1626082927389-6cd097cdc6ec?w=600&auto=format&fit=crop&q=80",
        emoji = "🍗"
    ),
    FoodPhotoPreset(
        titleAr = "طاجين مغربي أصيل",
        imageUrl = "https://images.unsplash.com/photo-1541518763669-27fef04b14ea?w=600&auto=format&fit=crop&q=80",
        emoji = "🍲"
    ),
    FoodPhotoPreset(
        titleAr = "تاكوس فرنسي غراتيني",
        imageUrl = "https://images.unsplash.com/photo-1565299585323-38d6b0865b47?w=600&auto=format&fit=crop&q=80",
        emoji = "🌮"
    ),
    FoodPhotoPreset(
        titleAr = "شاورما عربي دجاج",
        imageUrl = "https://images.unsplash.com/photo-1529042410759-befb1204b468?w=600&auto=format&fit=crop&q=80",
        emoji = "🌯"
    ),
    FoodPhotoPreset(
        titleAr = "كرواسون فرنسي وقهوة",
        imageUrl = "https://images.unsplash.com/photo-1509440159596-0249088772ff?w=600&auto=format&fit=crop&q=80",
        emoji = "🥐"
    ),
    FoodPhotoPreset(
        titleAr = "سوشي ياباني طازج",
        imageUrl = "https://images.unsplash.com/photo-1579871494447-9811cf80d66c?w=600&auto=format&fit=crop&q=80",
        emoji = "🍣"
    ),
    FoodPhotoPreset(
        titleAr = "سلطة سيزر صحية",
        imageUrl = "https://images.unsplash.com/photo-1512621776951-a57141f2eefd?w=600&auto=format&fit=crop&q=80",
        emoji = "🥗"
    ),
    FoodPhotoPreset(
        titleAr = "ميلك شيك وحلويات",
        imageUrl = "https://images.unsplash.com/photo-1572490122747-3968b75cc699?w=600&auto=format&fit=crop&q=80",
        emoji = "🍨"
    )
)

@Composable
fun RestaurantOwnerDashboard(
    language: AppLanguage,
    stores: List<StoreEntity>,
    products: List<ProductEntity>,
    orders: List<OrderEntity>,
    onToggleStoreOpen: (String, Boolean) -> Unit,
    onAddProduct: (storeId: String, storeName: String, nameAr: String, nameFr: String, price: Double, category: String, desc: String, emoji: String, imageUrl: String) -> Unit,
    onToggleProductAvailable: (String, Boolean) -> Unit,
    onDeleteProduct: (String) -> Unit,
    onUpdateOrderStatus: (orderId: String, restStatus: String, orderStatus: String) -> Unit
) {
    // Selected restaurant for management (defaults to first or McDonald's)
    var selectedStoreId by remember(stores) {
        mutableStateOf(stores.firstOrNull { it.id == "store_mcdonalds" }?.id ?: stores.firstOrNull()?.id ?: "")
    }
    val activeStore = stores.firstOrNull { it.id == selectedStoreId } ?: stores.firstOrNull()
    val storeProducts = products.filter { it.storeId == activeStore?.id }
    val storeOrders = orders.filter { it.storeId == activeStore?.id || it.storeName == activeStore?.nameAr }

    var showAddMealDialog by remember { mutableStateOf(false) }
    var newMealNameAr by remember { mutableStateOf("") }
    var newMealPrice by remember { mutableStateOf("") }
    var newMealDesc by remember { mutableStateOf("") }
    var newMealEmoji by remember { mutableStateOf("🍔") }
    var selectedImageUrl by remember { mutableStateOf("") }
    var customUrlInput by remember { mutableStateOf("") }
    var showUrlField by remember { mutableStateOf(false) }

    // Modern Zero-Permission Photo Picker
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri: Uri? ->
            if (uri != null) {
                selectedImageUrl = uri.toString()
            }
        }
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("restaurant_owner_dashboard"),
        contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Multi-Restaurant Switcher Header if more than 1 store
        if (stores.size > 1) {
            item {
                Column {
                    Text(
                        text = "اختر المطعم لإدارة القائمة والطلبات:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(stores) { store ->
                            val isSelected = store.id == activeStore?.id
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) AzizOrangePrimary else MaterialTheme.colorScheme.surface,
                                shadowElevation = if (isSelected) 3.dp else 1.dp,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { selectedStoreId = store.id }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(text = store.emoji, fontSize = 16.sp)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = store.nameAr,
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

        // Restaurant Status & KPI Header
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
                            Text(activeStore?.emoji ?: "🏬", fontSize = 32.sp)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = activeStore?.nameAr ?: "بوابة إدارة المطعم",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 17.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "لوحة تحكم صاحب المطعم والشريك • ألو عزيز 👑",
                                    fontSize = 11.sp,
                                    color = AzizOrangePrimary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // Open / Closed Toggle
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = if (activeStore?.isOpen == true) "مفتوح 🟢" else "مغلق 🔴",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (activeStore?.isOpen == true) AzizMint else Color.Red
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Switch(
                                checked = activeStore?.isOpen ?: true,
                                onCheckedChange = { isOpen ->
                                    activeStore?.let { onToggleStoreOpen(it.id, it.isOpen) }
                                },
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

                    // Daily Metrics KPI
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        MetricBox(title = "مبيعات اليوم", value = "4,280 د.م", color = AzizMint)
                        MetricBox(title = "الطلبات الحالية", value = "${storeOrders.size + 3}", color = AzizOrangePrimary)
                        MetricBox(title = "متوسط التحضير", value = "11 دقيقة", color = AzizAmberSecondary)
                        MetricBox(title = "تقييم المطعم", value = "★ ${activeStore?.rating ?: 4.9}", color = AzizAmberSecondary)
                    }
                }
            }
        }

        // Live Incoming Orders for Kitchen
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "طلبات المطبخ الحالية 👨‍🍳",
                    fontWeight = FontWeight.Black,
                    fontSize = 17.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = AzizOrangePrimary.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = "مباشر 🔴",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = AzizOrangePrimary
                    )
                }
            }
        }

        if (storeOrders.isEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(modifier = Modifier.padding(20.dp), contentAlignment = Alignment.Center) {
                        Text(
                            text = "لا توجد طلبات معلقة لهذا المطعم الآن. ستظهر هنا فور إرسالها من الزبائن!",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(storeOrders) { order ->
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
                            Text(text = "طلب #${order.id}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text(
                                text = "${order.total} د.م",
                                fontWeight = FontWeight.Black,
                                fontSize = 14.sp,
                                color = AzizOrangePrimary
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = order.itemsSummary, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(text = "ملاحظات الزبون: ${order.deliveryNotes.ifBlank { "بدون ملاحظات خاصة" }}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (order.status == "RECEIVED") {
                                Button(
                                    onClick = { onUpdateOrderStatus(order.id, "COOKING", "PREPARING") },
                                    colors = ButtonDefaults.buttonColors(containerColor = AzizOrangePrimary),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("بدء التحضير 🍳", fontSize = 11.sp, color = Color.White)
                                }
                            } else if (order.status == "PREPARING") {
                                Button(
                                    onClick = { onUpdateOrderStatus(order.id, "READY_FOR_PICKUP", "ON_THE_WAY") },
                                    colors = ButtonDefaults.buttonColors(containerColor = AzizMint),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("جاهز للاستلام من المندوب 🛵", fontSize = 11.sp, color = Color.White)
                                }
                            } else {
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = AzizMint.copy(alpha = 0.15f),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = "المندوب استلم الطلب وهو في الطريق ✓",
                                        modifier = Modifier.padding(vertical = 8.dp),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = AzizMint
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Menu Management Section
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "قائمة وجبات المطعم (${storeProducts.size}) 🍔",
                    fontWeight = FontWeight.Black,
                    fontSize = 17.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Button(
                    onClick = {
                        selectedImageUrl = ""
                        customUrlInput = ""
                        newMealNameAr = ""
                        newMealPrice = ""
                        newMealDesc = ""
                        showAddMealDialog = true
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AzizOrangePrimary),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("add_meal_button")
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("إضافة وجبة بصورة", fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Product Items List with Real Food Photos
        items(storeProducts) { prod ->
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Meal Image or Emoji
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(AzizOrangePrimary.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (prod.imageUrl.isNotBlank()) {
                            AsyncImage(
                                model = prod.imageUrl,
                                contentDescription = prod.nameAr,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Text(prod.emoji, fontSize = 28.sp)
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = prod.nameAr, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text(
                            text = "${prod.price} د.م • ${prod.calories}",
                            fontSize = 11.sp,
                            color = AzizOrangePrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                        if (prod.imageUrl.isNotBlank()) {
                            Text(
                                text = "📷 صورة حقيقية مفعلة",
                                fontSize = 10.sp,
                                color = AzizMint,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Toggle Available
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Switch(
                            checked = prod.isAvailable,
                            onCheckedChange = { onToggleProductAvailable(prod.id, prod.isAvailable) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = AzizMint
                            )
                        )

                        IconButton(onClick = { onDeleteProduct(prod.id) }) {
                            Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = Color.Gray, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        }
    }

    // Add New Meal Modal Dialog with Real Photo Picker & Presets
    if (showAddMealDialog) {
        AlertDialog(
            onDismissRequest = { showAddMealDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("📷 إضافة وجبة جديدة بصورة حقيقية", fontWeight = FontWeight.Black, fontSize = 16.sp)
                }
            },
            text = {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(420.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item {
                        OutlinedTextField(
                            value = newMealNameAr,
                            onValueChange = { newMealNameAr = it },
                            label = { Text("اسم الوجبة (مثال: برجر دبل كرسبي)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )
                    }

                    item {
                        OutlinedTextField(
                            value = newMealPrice,
                            onValueChange = { newMealPrice = it },
                            label = { Text("السعر بالدرهم (د.م)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )
                    }

                    item {
                        OutlinedTextField(
                            value = newMealDesc,
                            onValueChange = { newMealDesc = it },
                            label = { Text("المكونات والوصف الشهي") },
                            modifier = Modifier.fillMaxWidth(),
                            maxLines = 2,
                            shape = RoundedCornerShape(12.dp)
                        )
                    }

                    item {
                        OutlinedTextField(
                            value = newMealEmoji,
                            onValueChange = { newMealEmoji = it },
                            label = { Text("رمز تعبيري بديل (Emoji)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )
                    }

                    // Photo Selection Section
                    item {
                        Text(
                            text = "صورة المنتج الحقيقية (Real Product Photo):",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // Live Image Preview if selected
                    if (selectedImageUrl.isNotBlank()) {
                        item {
                            Card(
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(130.dp),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Box(modifier = Modifier.fillMaxSize()) {
                                    AsyncImage(
                                        model = selectedImageUrl,
                                        contentDescription = "Selected food",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )

                                    // Remove Photo Button
                                    IconButton(
                                        onClick = { selectedImageUrl = "" },
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .padding(6.dp)
                                            .size(28.dp)
                                            .clip(CircleShape)
                                            .background(Color.Black.copy(alpha = 0.6f))
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Remove photo",
                                            tint = Color.White,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }

                                    Surface(
                                        modifier = Modifier
                                            .align(Alignment.BottomStart)
                                            .padding(8.dp),
                                        shape = RoundedCornerShape(6.dp),
                                        color = AzizMint
                                    ) {
                                        Text(
                                            text = "✓ تم اختيار الصورة",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Button: Pick from Phone Gallery
                    item {
                        Button(
                            onClick = {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = AzizOrangePrimary),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(imageVector = Icons.Default.AddPhotoAlternate, contentDescription = null, tint = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("رفع صورة من ألبوم الصور (المعرض)", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }

                    // Option: Presets from Popular Dishes
                    item {
                        Text(
                            text = "أو اختر صورة جاهزة عالية الجودة من المكتبة:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    item {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(vertical = 4.dp)
                        ) {
                            items(PRESET_FOOD_PHOTOS) { preset ->
                                val isSelected = selectedImageUrl == preset.imageUrl
                                Card(
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier
                                        .size(76.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .border(
                                            width = if (isSelected) 2.dp else 0.dp,
                                            color = if (isSelected) AzizOrangePrimary else Color.Transparent,
                                            shape = RoundedCornerShape(10.dp)
                                        )
                                        .clickable {
                                            selectedImageUrl = preset.imageUrl
                                            newMealEmoji = preset.emoji
                                        }
                                ) {
                                    Box(modifier = Modifier.fillMaxSize()) {
                                        AsyncImage(
                                            model = preset.imageUrl,
                                            contentDescription = preset.titleAr,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                        Surface(
                                            color = Color.Black.copy(alpha = 0.5f),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .align(Alignment.BottomCenter)
                                        ) {
                                            Text(
                                                text = preset.emoji,
                                                fontSize = 11.sp,
                                                modifier = Modifier.padding(2.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Option: Direct Image URL
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "أو كتابة رابط صورة مباشر (URL)",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            TextButton(onClick = { showUrlField = !showUrlField }) {
                                Text(if (showUrlField) "إخفاء" else "إدخال رابط", fontSize = 11.sp, color = AzizOrangePrimary)
                            }
                        }
                    }

                    if (showUrlField) {
                        item {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                OutlinedTextField(
                                    value = customUrlInput,
                                    onValueChange = { customUrlInput = it },
                                    label = { Text("https://example.com/meal.jpg") },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Button(
                                    onClick = {
                                        if (customUrlInput.isNotBlank()) {
                                            selectedImageUrl = customUrlInput.trim()
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = AzizMint),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text("تطبيق", color = Color.White, fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val priceNum = newMealPrice.toDoubleOrNull() ?: 35.0
                        if (newMealNameAr.isNotBlank() && activeStore != null) {
                            onAddProduct(
                                activeStore.id,
                                activeStore.nameAr,
                                newMealNameAr,
                                newMealNameAr,
                                priceNum,
                                activeStore.category,
                                newMealDesc,
                                newMealEmoji,
                                selectedImageUrl
                            )
                            showAddMealDialog = false
                            newMealNameAr = ""
                            newMealPrice = ""
                            newMealDesc = ""
                            selectedImageUrl = ""
                            customUrlInput = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AzizOrangePrimary)
                ) {
                    Text("حفظ ونشر الوجبة بالصورة ✓", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddMealDialog = false }) {
                    Text("إلغاء")
                }
            }
        )
    }
}

@Composable
fun MetricBox(title: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, fontWeight = FontWeight.Black, fontSize = 15.sp, color = color)
        Text(text = title, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
