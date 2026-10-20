package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ElectricScooter
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Percent
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import coil.compose.AsyncImage
import com.example.data.local.OrderEntity
import com.example.data.localization.AppStrings
import com.example.data.model.AppLanguage
import com.example.data.model.CatalogData
import com.example.data.model.Product
import com.example.data.model.Store
import com.example.data.model.StoreCategory
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Tune
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.data.local.AddressEntity
import com.example.ui.components.AddressManagerDialog
import com.example.ui.components.ProductCustomizationDialog
import com.example.ui.theme.AzizAmberSecondary
import com.example.ui.theme.AzizCharcoalDark
import com.example.ui.theme.AzizMint
import com.example.ui.theme.AzizOrangePrimary
import com.example.ui.theme.GoldStarColor

@Composable
fun HomeScreen(
    language: AppLanguage,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    selectedCategory: StoreCategory,
    onSelectCategory: (StoreCategory) -> Unit,
    activeOrder: OrderEntity?,
    onOpenStore: (Store) -> Unit,
    onAddToCart: (Product) -> Unit,
    onTrackOrder: (String) -> Unit,
    onApplyPromo: (String) -> Unit,
    favoriteStoreIds: Set<String> = emptySet(),
    onToggleFavoriteStore: (String) -> Unit = {},
    onAddToCartWithOptions: (Product, Int, List<String>, Double, String) -> Unit = { p, q, o, e, n -> onAddToCart(p) },
    currentAddress: String = "Bd Hassan II, Parc des Villes Jumelées, Mohammedia 🇲🇦",
    currentCity: String = "Mohammedia",
    onSelectAddress: (String) -> Unit = {},
    onSelectCity: (String) -> Unit = {},
    savedAddresses: List<AddressEntity> = emptyList(),
    onAddNewAddress: (String, String, String, String, String, String) -> Unit = { _, _, _, _, _, _ -> },
    onDeleteAddress: (String) -> Unit = {},
    onNavigateToSearch: () -> Unit = {},
    cartItemCount: Int = 0,
    cartTotal: Double = 0.0,
    onOpenCart: () -> Unit = {},
    onOpenLocationPicker: (() -> Unit)? = null,
    onOpenAiAssistant: (() -> Unit)? = null,
    onOpenSupportVoiceCall: (() -> Unit)? = null,
    selectedBranch: com.example.data.model.AppBranch = com.example.data.model.AppBranch.FOOD,
    onSelectBranch: (com.example.data.model.AppBranch) -> Unit = {},
    onOpenMerchantRegistration: (() -> Unit)? = null,
    onOpenPricingModal: (() -> Unit)? = null
) {
    var customizingProduct by remember { mutableStateOf<Product?>(null) }
    var showAddressDialog by remember { mutableStateOf(false) }

    val isEcommerceBranch = selectedBranch == com.example.data.model.AppBranch.ECOMMERCE

    val filteredStores = CatalogData.stores.filter { store ->
        val matchesBranch = if (isEcommerceBranch) {
            store.category == StoreCategory.ECOMMERCE
        } else {
            store.category != StoreCategory.ECOMMERCE
        }
        val matchesCategory = if (isEcommerceBranch) {
            true
        } else {
            selectedCategory == StoreCategory.ALL || store.category == selectedCategory
        }
        val matchesQuery = searchQuery.isBlank() ||
                store.name.contains(searchQuery, ignoreCase = true) ||
                store.nameAr.contains(searchQuery, ignoreCase = true) ||
                store.nameFr.contains(searchQuery, ignoreCase = true)
        matchesBranch && matchesCategory && matchesQuery
    }

    val popularProducts = CatalogData.products.filter { product ->
        val matchesBranch = if (isEcommerceBranch) {
            product.category == StoreCategory.ECOMMERCE
        } else {
            product.category != StoreCategory.ECOMMERCE
        }
        val matchesCategory = if (isEcommerceBranch) {
            true
        } else {
            selectedCategory == StoreCategory.ALL || product.category == selectedCategory
        }
        val matchesQuery = searchQuery.isBlank() ||
                product.nameAr.contains(searchQuery, ignoreCase = true) ||
                product.nameEn.contains(searchQuery, ignoreCase = true) ||
                product.nameFr.contains(searchQuery, ignoreCase = true)
        matchesBranch && matchesCategory && matchesQuery
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .testTag("home_screen_lazy_column"),
            contentPadding = PaddingValues(bottom = if (cartItemCount > 0) 90.dp else 24.dp)
        ) {
        // Location / Address Bar at Top of Home Screen
        item {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showAddressDialog = true }
                    .testTag("home_location_selector"),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 1.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(AzizOrangePrimary.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = "Location",
                                tint = AzizOrangePrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = AppStrings.deliverTo(language),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "▼",
                                    fontSize = 10.sp,
                                    color = AzizOrangePrimary
                                )
                            }
                            Text(
                                text = currentAddress,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = AzizMint.copy(alpha = 0.12f)
                    ) {
                        Text(
                            text = "$currentCity 🇲🇦",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = AzizMint
                        )
                    }
                }
            }
        }

        // Active Order Floating Tracker Banner (if an order is currently active)
        if (activeOrder != null && activeOrder.status != "DELIVERED") {
            item {
                ActiveOrderTrackerBanner(
                    order = activeOrder,
                    language = language,
                    onTrackClick = { onTrackOrder(activeOrder.id) }
                )
            }
        }

        // Department / Branch Switcher (Food & Dining vs E-Commerce & Retail Marketplace)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .testTag("home_branch_switcher"),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(6.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Branch 1: Food
                        val isFood = selectedBranch == com.example.data.model.AppBranch.FOOD
                        Surface(
                            onClick = { onSelectBranch(com.example.data.model.AppBranch.FOOD) },
                            shape = RoundedCornerShape(12.dp),
                            color = if (isFood) AzizOrangePrimary else Color.Transparent,
                            shadowElevation = if (isFood) 3.dp else 0.dp,
                            modifier = Modifier.weight(1f)
                        ) {
                            Row(
                                modifier = Modifier.padding(vertical = 10.dp, horizontal = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text("🍔", fontSize = 16.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (language == AppLanguage.ARABIC) "المطاعم والمأكولات" else if (language == AppLanguage.FRENCH) "Restaurants & Plats" else "Food & Dining",
                                    fontSize = 12.sp,
                                    fontWeight = if (isFood) FontWeight.Black else FontWeight.Bold,
                                    color = if (isFood) Color.White else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }

                        // Branch 2: E-Commerce
                        val isEcom = selectedBranch == com.example.data.model.AppBranch.ECOMMERCE
                        Surface(
                            onClick = { onSelectBranch(com.example.data.model.AppBranch.ECOMMERCE) },
                            shape = RoundedCornerShape(12.dp),
                            color = if (isEcom) Color(0xFF6366F1) else Color.Transparent,
                            shadowElevation = if (isEcom) 3.dp else 0.dp,
                            modifier = Modifier.weight(1f)
                        ) {
                            Row(
                                modifier = Modifier.padding(vertical = 10.dp, horizontal = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text("🛍️", fontSize = 16.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (language == AppLanguage.ARABIC) "سوق التجارة الإلكترونية" else if (language == AppLanguage.FRENCH) "E-Commerce & High-Tech" else "E-Commerce Retail",
                                    fontSize = 12.sp,
                                    fontWeight = if (isEcom) FontWeight.Black else FontWeight.Bold,
                                    color = if (isEcom) Color.White else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }

                    // Transparent Service Fee Strip ("كم الخدمة؟")
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (selectedBranch == com.example.data.model.AppBranch.FOOD) {
                                    if (language == AppLanguage.ARABIC) "⚡ توصيل ساخن 20-30 دقيقة • رسوم التوصيل: 8 د.م فقط"
                                    else "⚡ Livraison rapide 20-30 min • Frais : 8 DH"
                                } else {
                                    if (language == AppLanguage.ARABIC) "📦 شحن وتأمين الطرود • رسوم الشحن: 18 د.م (ضمان واسترجاع)"
                                    else "📦 Expédition colis sécurisée • Frais : 18 DH"
                                },
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Surface(
                            onClick = { onOpenPricingModal?.invoke() },
                            shape = RoundedCornerShape(8.dp),
                            color = AzizMint.copy(alpha = 0.15f),
                            modifier = Modifier.padding(start = 4.dp)
                        ) {
                            Text(
                                text = if (language == AppLanguage.ARABIC) "كم الخدمة؟ 💡" else "Tarifs 💡",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = AzizMint,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }

        // Search Bar
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("home_search_input"),
                    placeholder = {
                        Text(
                            text = AppStrings.searchPlaceholder(language),
                            fontSize = 13.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = AzizOrangePrimary
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { onSearchChange("") }) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Clear",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AzizOrangePrimary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    )
                )
            }
        }

        // ==========================================
        // UNIVERSAL AI VOICE & TEXT SHOPPING HERO BANNER (Food + Global Concierge)
        // ==========================================
        item {
            Surface(
                onClick = { onOpenAiAssistant?.invoke() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 5.dp)
                        .clip(RoundedCornerShape(22.dp))
                        .testTag("home_ai_voice_hero_banner"),
                    shape = RoundedCornerShape(22.dp),
                    border = androidx.compose.foundation.BorderStroke(1.2.dp, Color(0xFF818CF8).copy(alpha = 0.45f)),
                    shadowElevation = 5.dp
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(
                                        Color(0xFF0F172A),
                                        Color(0xFF1E1B4B),
                                        Color(0xFF2C1654)
                                    )
                                )
                            )
                            .padding(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(46.dp)
                                        .clip(CircleShape)
                                        .background(
                                            Brush.linearGradient(
                                                colors = listOf(AzizOrangePrimary, AzizAmberSecondary)
                                            )
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Mic,
                                        contentDescription = "Voice Shopping",
                                        tint = Color.White,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = when (language) {
                                                AppLanguage.FRENCH -> "Assistant Vocal DELIVIO AI"
                                                AppLanguage.ENGLISH -> "DELIVIO AI Voice Assistant"
                                                AppLanguage.ARABIC -> "مساعد ديليفيو الصوتي AI"
                                            },
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Black,
                                            color = Color.White
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = AzizMint.copy(alpha = 0.25f)
                                        ) {
                                            Text(
                                                text = when (language) {
                                                    AppLanguage.FRENCH -> "NOUVEAU ⚡"
                                                    AppLanguage.ENGLISH -> "NEW ⚡"
                                                    AppLanguage.ARABIC -> "جديد ⚡"
                                                },
                                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = AzizMint
                                            )
                                        }
                                    }
                                    Text(
                                        text = if (isEcommerceBranch) {
                                            when (language) {
                                                AppLanguage.FRENCH -> "Commandez tout article de toute plateforme (Zara, Shein, Amazon...) à la voix !"
                                                AppLanguage.ENGLISH -> "Find any item from any platform (Zara, Shein, Amazon...) by voice!"
                                                AppLanguage.ARABIC -> "اطلب أي منتج من أي منصة (زارا، شين، أمازون...) بالصوت أو الكتابة!"
                                            }
                                        } else {
                                            when (language) {
                                                AppLanguage.FRENCH -> "Commandez à la voix, l'IA prépare votre panier !"
                                                AppLanguage.ENGLISH -> "Order with your voice, AI builds your cart!"
                                                AppLanguage.ARABIC -> "تكلم واطلب مشترياتك بالصوت وسنجهز سلتك فوراً!"
                                            }
                                        },
                                        fontSize = 11.sp,
                                        color = Color.White.copy(alpha = 0.85f),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = AzizOrangePrimary,
                                modifier = Modifier.padding(start = 8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = when (language) {
                                            AppLanguage.FRENCH -> "Parler 🎙️"
                                            AppLanguage.ENGLISH -> "Speak 🎙️"
                                            AppLanguage.ARABIC -> "تكلم الآن 🎙️"
                                        },
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }
                }
            }

        if (!isEcommerceBranch) {
            // AI Voice Customer Support Hero Card (Sara IA)
            item {
                Surface(
                    onClick = { onOpenSupportVoiceCall?.invoke() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .testTag("home_ai_voice_support_hero_banner"),
                    shape = RoundedCornerShape(20.dp),
                    shadowElevation = 3.dp
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(
                                        Color(0xFF0F172A),
                                        Color(0xFF1E1B4B),
                                        Color(0xFF31104B)
                                    )
                                )
                            )
                            .padding(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(46.dp)
                                        .clip(CircleShape)
                                        .background(
                                            Brush.linearGradient(
                                                colors = listOf(AzizMint, Color(0xFF059669))
                                            )
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.RecordVoiceOver,
                                        contentDescription = "Support Vocal IA",
                                        tint = Color.White,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = when (language) {
                                                AppLanguage.FRENCH -> "Sara IA • Support Vocal 24/7"
                                                AppLanguage.ENGLISH -> "Sara AI • 24/7 Voice Support"
                                                AppLanguage.ARABIC -> "سارة AI • الدعم الصوتي الفوري"
                                            },
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Black,
                                            color = Color.White
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = AzizMint.copy(alpha = 0.25f)
                                        ) {
                                            Text(
                                                text = when (language) {
                                                    AppLanguage.FRENCH -> "EN DIRECT 🔴"
                                                    AppLanguage.ENGLISH -> "LIVE 🔴"
                                                    AppLanguage.ARABIC -> "مباشر 🔴"
                                                },
                                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = AzizMint
                                            )
                                        }
                                    }
                                    Text(
                                        text = when (language) {
                                            AppLanguage.FRENCH -> "Parlez en direct avec l'IA, elle vous répond à voix haute !"
                                            AppLanguage.ENGLISH -> "Speak live with AI, it answers you out loud!"
                                            AppLanguage.ARABIC -> "تحدث مع الدعم الفني بالصوت وتجيبك سارة فورياً!"
                                        },
                                        fontSize = 11.sp,
                                        color = Color.White.copy(alpha = 0.85f),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = AzizMint,
                                modifier = Modifier.padding(start = 8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = when (language) {
                                            AppLanguage.FRENCH -> "Appeler 📞"
                                            AppLanguage.ENGLISH -> "Call 📞"
                                            AppLanguage.ARABIC -> "اتصال 📞"
                                        },
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Hero Express Banner
            item {
                HeroDeliveryBanner(
                    language = language,
                    onApplyCode = onApplyPromo
                )
            }

            // Food Categories Horizontal Selector
            item {
                Column(modifier = Modifier.padding(top = 10.dp)) {
                    Text(
                        text = AppStrings.categories(language),
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                    )

                    val foodCategories = StoreCategory.values().filter { it != StoreCategory.ECOMMERCE }
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(foodCategories) { category ->
                            val isSelected = selectedCategory == category
                            val title = when (language) {
                                AppLanguage.ARABIC -> category.titleAr
                                AppLanguage.FRENCH -> category.titleFr
                                AppLanguage.ENGLISH -> category.titleEn
                            }

                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = if (isSelected) AzizOrangePrimary else MaterialTheme.colorScheme.surface,
                                tonalElevation = if (isSelected) 4.dp else 1.dp,
                                shadowElevation = if (isSelected) 3.dp else 0.dp,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(14.dp))
                                    .clickable { onSelectCategory(category) }
                                    .testTag("category_chip_${category.id}")
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                ) {
                                    Text(text = category.iconName, fontSize = 16.sp)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = title,
                                        fontSize = 13.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // ==========================================
            // E-COMMERCE & RETAIL MARKETPLACE BRANCH
            // ==========================================
            item {
                Surface(
                    onClick = { onOpenMerchantRegistration?.invoke() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                        .clip(RoundedCornerShape(22.dp))
                        .testTag("home_ecommerce_hero_banner"),
                    shape = RoundedCornerShape(22.dp),
                    border = androidx.compose.foundation.BorderStroke(1.2.dp, Color(0xFF6366F1).copy(alpha = 0.5f)),
                    shadowElevation = 5.dp
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(
                                        Color(0xFF1E1B4B),
                                        Color(0xFF312E81),
                                        Color(0xFF4338CA)
                                    )
                                )
                            )
                            .padding(16.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(46.dp)
                                            .clip(CircleShape)
                                            .background(Color.White.copy(alpha = 0.15f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("🛍️", fontSize = 24.sp)
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = if (language == AppLanguage.ARABIC) "سوق التجارة الإلكترونية المعتمد 🛡️" else "Marketplace E-Commerce Certifié",
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Black,
                                                color = Color.White
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = AzizMint
                                            ) {
                                                Text(
                                                    text = "KYC ✓",
                                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.White
                                                )
                                            }
                                        }
                                        Text(
                                            text = if (language == AppLanguage.ARABIC) "متاجر موثوقة ومصادق عليها بالـ CIN والسجل التجاري (Glovo & Temu)" else "Vendeurs certifiés avec licence et RC",
                                            fontSize = 10.sp,
                                            color = Color.White.copy(alpha = 0.85f)
                                        )
                                    }
                                }

                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = AzizMint
                                ) {
                                    Text(
                                        text = if (language == AppLanguage.ARABIC) "ضمان 7 أيام ✓" else "Garantie 7j",
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))
                            HorizontalDivider(color = Color.White.copy(alpha = 0.2f))
                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = if (language == AppLanguage.ARABIC) "📦 شحن الطرود: 18 د.م (مؤمن ومغلف)" else "📦 Expédition colis sécurisée : 18 DH",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = Color.White
                                    )
                                    Text(
                                        text = if (language == AppLanguage.ARABIC) "شحن خاص للطرود مع تتبع المسار وضمان فحص السلعة عند الاستلام" else "Suivi direct et inspection avant paiement",
                                        fontSize = 10.sp,
                                        color = Color.White.copy(alpha = 0.8f)
                                    )
                                }

                                Button(
                                    onClick = { onOpenMerchantRegistration?.invoke() },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                                    shape = RoundedCornerShape(10.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = if (language == AppLanguage.ARABIC) "انضم كبائع 💼" else "Devenir Vendeur",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF312E81)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // E-Commerce Categories
            item {
                Column(modifier = Modifier.padding(top = 10.dp)) {
                    Text(
                        text = if (language == AppLanguage.ARABIC) "أقسام سوق التجارة الإلكترونية 🛍️" else "Catégories E-Commerce",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                    )

                    val ecomTags = listOf(
                        "الكل في المتجر 🛍️",
                        "هواتف وإلكترونيات 📱",
                        "جيمنج وكمبيوتر 🎮",
                        "أزياء وموضة 👗",
                        "عطور فاخرة 🧴",
                        "جلد طبيعي وأصالة 🎒",
                        "شواحن وإكسسوارات 🔌"
                    )

                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(ecomTags) { tag ->
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = MaterialTheme.colorScheme.surface,
                                tonalElevation = 2.dp,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(14.dp))
                                    .clickable { onSearchChange(tag.substringBefore(" ").takeIf { it != "الكل" } ?: "") }
                            ) {
                                Text(
                                    text = tag,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        }

        // Featured Stores section
        item {
            Column(modifier = Modifier.padding(top = 18.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isEcommerceBranch) {
                            if (language == AppLanguage.ARABIC) "متاجر وبوتيكات إلكترونية معتمدة 🏬"
                            else if (language == AppLanguage.FRENCH) "Boutiques E-Commerce Vérifiées 🏬"
                            else "Verified E-Commerce Boutiques 🏬"
                        } else {
                            AppStrings.featuredStores(language)
                        },
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${filteredStores.size}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = AzizOrangePrimary
                    )
                }

                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(filteredStores) { store ->
                        StoreCard(
                            store = store,
                            language = language,
                            onClick = { onOpenStore(store) },
                            isFavorite = favoriteStoreIds.contains(store.id),
                            onToggleFavorite = { onToggleFavoriteStore(store.id) }
                        )
                    }
                }
            }
        }

        // Most Popular Dishes / Products section
        item {
            Column(modifier = Modifier.padding(top = 22.dp, start = 16.dp, end = 16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isEcommerceBranch) {
                            if (language == AppLanguage.ARABIC) "أحدث المنتجات والإلكترونيات الفاخرة 🛍️"
                            else if (language == AppLanguage.FRENCH) "Produits & Gadgets Tendances 🛍️"
                            else "Trending Products & Tech 🛍️"
                        } else {
                            AppStrings.popularMeals(language)
                        },
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (isEcommerceBranch) "📦 شحن 18 د.م" else "⭐ DELIVIO GOURMET",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isEcommerceBranch) Color(0xFF6366F1) else AzizAmberSecondary
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
            }
        }

        items(popularProducts) { product ->
            Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                ProductItemRow(
                    product = product,
                    language = language,
                    onAddToCart = {
                        if (product.optionGroups.isNotEmpty()) {
                            customizingProduct = product
                        } else {
                            onAddToCart(product)
                        }
                    }
                )
            }
        }

        // Partner Onboarding & KYC Banner (Glovo & Temu Standard)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp)
                    .clickable { onOpenMerchantRegistration?.invoke() }
                    .testTag("merchant_onboarding_cta_banner"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(CircleShape)
                                .background(AzizOrangePrimary.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(if (isEcommerceBranch) "🛍️" else "👨‍🍳", fontSize = 22.sp)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = if (isEcommerceBranch) {
                                    if (language == AppLanguage.ARABIC) "انضم كتاجر إلكتروني معتمد 💼" else "Devenir Vendeur E-Commerce"
                                } else {
                                    if (language == AppLanguage.ARABIC) "انضم كشريك مطعم معتمد 👨‍🍳" else "Devenir Restaurant Partenaire"
                                },
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (language == AppLanguage.ARABIC)
                                    "إيداع الوثائق الرسمية (CIN، السجل التجاري، التراخيص) - معايير Glovo & Temu"
                                else "Validation KYC & Documents officiels (CIN, RC, Licence)",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = AzizOrangePrimary
                    ) {
                        Text(
                            text = if (language == AppLanguage.ARABIC) "تقديم الوثائق 📑" else "Postuler",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }

        // Trust Features Footer Card
        item {
            Box(modifier = Modifier.padding(16.dp)) {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("🛡️", fontSize = 22.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "ضمان ديليفيو DELIVIO للتوصيل السريع",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "• تتبع فوري مباشر لحركة المندوب على الخريطة بالمحمدية\n• دفع آمن 100% مع تشفير كامل للبيانات (نقداً أو بالبطاقة)\n• دعم فني 24/7 لحل أي مشكلة في ثوانٍ معدودة",
                            fontSize = 12.sp,
                            lineHeight = 18.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }

        // Glovo-style Floating Cart Bar
        AnimatedVisibility(
            visible = cartItemCount > 0,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { onOpenCart() }
                    .testTag("home_floating_cart_bar"),
                color = AzizOrangePrimary,
                shadowElevation = 8.dp,
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = Color.White.copy(alpha = 0.25f),
                            modifier = Modifier.size(34.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "$cartItemCount",
                                    color = Color.White,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 14.sp
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = when (language) {
                                    AppLanguage.FRENCH -> "Voir le panier et commander 🛒"
                                    AppLanguage.ENGLISH -> "View Cart & Checkout 🛒"
                                    AppLanguage.ARABIC -> "عرض السلة وإتمام الطلب 🛒"
                                },
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = when (language) {
                                    AppLanguage.FRENCH -> "DELIVIO Express • Mohammedia"
                                    AppLanguage.ENGLISH -> "DELIVIO Express • Mohammedia"
                                    AppLanguage.ARABIC -> "ديليفيو إكسبريس • المحمدية"
                                },
                                color = Color.White.copy(alpha = 0.85f),
                                fontSize = 11.sp
                            )
                        }
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "%.2f ${AppStrings.currency(language)}".format(cartTotal),
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 15.sp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }

        // Floating Action Buttons for Voice AI (Support Call + Shopping Assistant)
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(
                    end = 16.dp,
                    bottom = if (cartItemCount > 0) 80.dp else 24.dp
                ),
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Support Vocal IA (Sara)
            SmallFloatingActionButton(
                onClick = { onOpenSupportVoiceCall?.invoke() },
                containerColor = Color(0xFF0F172A),
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.testTag("home_fab_support_voice")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.RecordVoiceOver,
                        contentDescription = "Support Vocal",
                        tint = AzizMint,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = when (language) {
                            AppLanguage.FRENCH -> "Sara IA Support 📞"
                            AppLanguage.ENGLISH -> "Sara AI Support 📞"
                            AppLanguage.ARABIC -> "سارة AI دعم 📞"
                        },
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            // AI Voice Shopping Assistant
            FloatingActionButton(
                onClick = { onOpenAiAssistant?.invoke() },
                containerColor = Color(0xFF4F46E5),
                contentColor = Color.White,
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.testTag("home_fab_ai_voice")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "AI Voice Assistant",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = when (language) {
                            AppLanguage.FRENCH -> "DELIVIO AI 🎙️"
                            AppLanguage.ENGLISH -> "DELIVIO AI 🎙️"
                            AppLanguage.ARABIC -> "ديليفيو AI 🎙️"
                        },
                        fontWeight = FontWeight.Black,
                        fontSize = 13.sp,
                        color = Color.White
                    )
                }
            }
        }
    }

    if (customizingProduct != null) {
        ProductCustomizationDialog(
            product = customizingProduct!!,
            language = language,
            onDismiss = { customizingProduct = null },
            onConfirmAddToCart = { qty, options, extra, notes ->
                onAddToCartWithOptions(customizingProduct!!, qty, options, extra, notes)
            }
        )
    }

    if (showAddressDialog) {
        AddressManagerDialog(
            language = language,
            currentAddress = currentAddress,
            currentCity = currentCity,
            savedAddresses = savedAddresses,
            onSelectAddress = onSelectAddress,
            onSelectCity = onSelectCity,
            onAddNewAddress = onAddNewAddress,
            onDeleteAddress = onDeleteAddress,
            onDismiss = { showAddressDialog = false },
            onOpenGoogleMapsPicker = onOpenLocationPicker
        )
    }
}

@Composable
fun ActiveOrderTrackerBanner(
    order: OrderEntity,
    language: AppLanguage,
    onTrackClick: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "scooter_pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable { onTrackClick() }
            .testTag("active_order_banner"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = AzizOrangePrimary
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .scale(scale)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.25f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ElectricScooter,
                            contentDescription = null,
                            tint = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "${AppStrings.orderIdPrefix(language)} #${order.id}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color.White
                        )
                        val statusText = when (order.status) {
                            "RECEIVED" -> AppStrings.statusReceived(language)
                            "PREPARING" -> AppStrings.statusPreparing(language)
                            "ON_THE_WAY" -> AppStrings.statusOnTheWay(language)
                            "ARRIVED" -> AppStrings.statusArrived(language)
                            else -> AppStrings.statusDelivered(language)
                        }
                        Text(
                            text = statusText,
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = AppStrings.liveTracking(language),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = AzizOrangePrimary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            modifier = Modifier.size(12.dp),
                            tint = AzizOrangePrimary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            LinearProgressIndicator(
                progress = { order.courierProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = AzizAmberSecondary,
                trackColor = Color.White.copy(alpha = 0.3f)
            )

            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${order.courierName}",
                    fontSize = 11.sp,
                    color = Color.White.copy(alpha = 0.9f)
                )
                Text(
                    text = "${order.estimatedMinsLeft} min ${AppStrings.estimatedArrival(language)}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
fun HeroDeliveryBanner(
    language: AppLanguage,
    onApplyCode: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .testTag("hero_banner_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
            ) {
                // Background image from generated assets
                Image(
                    painter = painterResource(id = R.drawable.hero_delivery_banner_1790889761948),
                    contentDescription = "Express Delivery Banner",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                // Gradient overlay
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color.Black.copy(alpha = 0.75f)
                                )
                            )
                        )
                )

                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(14.dp)
                ) {
                    Surface(
                        color = AzizOrangePrimary,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "⚡ DELIVIO EXPRESS",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = AppStrings.expressBannerTitle(language),
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 17.sp,
                        color = Color.White
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Percent,
                        contentDescription = null,
                        tint = AzizOrangePrimary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "كود الخصم: DELIVIO20 (خصم 20%)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = AzizOrangePrimary,
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { onApplyCode("DELIVIO20") }
                        .testTag("apply_hero_promo_button")
                ) {
                    Text(
                        text = AppStrings.apply(language),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun StoreCard(
    store: Store,
    language: AppLanguage,
    onClick: () -> Unit,
    isFavorite: Boolean = false,
    onToggleFavorite: () -> Unit = {}
) {
    ElevatedCard(
        modifier = Modifier
            .width(220.dp)
            .clickable { onClick() }
            .testTag("store_card_${store.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(115.dp)
                    .background(
                        Brush.linearGradient(
                            listOf(
                                AzizOrangePrimary.copy(alpha = 0.2f),
                                AzizAmberSecondary.copy(alpha = 0.3f)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (store.bannerUrl.isNotBlank()) {
                    AsyncImage(
                        model = store.bannerUrl,
                        contentDescription = store.nameAr,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Black.copy(alpha = 0.25f),
                                        Color.Transparent,
                                        Color.Black.copy(alpha = 0.45f)
                                    )
                                )
                            )
                    )
                } else {
                    Text(
                        text = store.emoji,
                        fontSize = 44.sp
                    )
                }

                // Favorite Heart Button
                Surface(
                    shape = CircleShape,
                    color = Color.White.copy(alpha = 0.9f),
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp)
                        .size(30.dp)
                        .clickable { onToggleFavorite() }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Favorite",
                            tint = if (isFavorite) Color(0xFFEF4444) else Color.Gray,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                if (store.badge.isNotEmpty()) {
                    Surface(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp),
                        shape = RoundedCornerShape(6.dp),
                        color = AzizOrangePrimary
                    ) {
                        Text(
                            text = store.badge,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }

            Column(modifier = Modifier.padding(12.dp)) {
                val storeName = when (language) {
                    AppLanguage.ARABIC -> store.nameAr
                    AppLanguage.FRENCH -> store.nameFr
                    AppLanguage.ENGLISH -> store.name
                }

                Text(
                    text = storeName,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Rating",
                        tint = GoldStarColor,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = "${store.rating}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = " (${store.reviewCount})",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = null,
                            tint = AzizOrangePrimary,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = AppStrings.minTime(language, store.deliveryTimeMins),
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Text(
                        text = "${store.deliveryFee} ${AppStrings.currency(language)}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = AzizMint
                    )
                }
            }
        }
    }
}

@Composable
fun ProductItemRow(
    product: Product,
    language: AppLanguage,
    onAddToCart: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("product_row_${product.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Food Photo / Emoji Box
            Box(
                modifier = Modifier
                    .size(74.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(AzizOrangePrimary.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                if (product.imageUrl.isNotBlank()) {
                    AsyncImage(
                        model = product.imageUrl,
                        contentDescription = product.nameAr,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Text(
                        text = product.emoji,
                        fontSize = 32.sp
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                val productName = when (language) {
                    AppLanguage.ARABIC -> product.nameAr
                    AppLanguage.FRENCH -> product.nameFr
                    AppLanguage.ENGLISH -> product.nameEn
                }
                val productDesc = when (language) {
                    AppLanguage.ARABIC -> product.descriptionAr
                    AppLanguage.FRENCH -> product.descriptionFr
                    AppLanguage.ENGLISH -> product.descriptionEn
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = productName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    if (product.isPopular) {
                        Text(
                            text = "🔥",
                            fontSize = 12.sp,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = productDesc,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 15.sp
                )

                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "${product.price} ${AppStrings.currency(language)}",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 14.sp,
                        color = AzizOrangePrimary
                    )

                    Surface(
                        shape = CircleShape,
                        color = AzizOrangePrimary,
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .clickable { onAddToCart() }
                            .testTag("add_product_${product.id}")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add to Cart",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
