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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddShoppingCart
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DirectionsBike
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Badge
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.localization.AppStrings
import com.example.data.model.AppLanguage
import com.example.data.model.CatalogData
import com.example.data.model.Product
import com.example.data.model.Store
import com.example.data.model.StoreCategory
import com.example.ui.components.ProductCustomizationDialog
import com.example.ui.theme.AzizGreenPrimary
import com.example.ui.theme.AzizMint
import com.example.ui.theme.AzizOrangePrimary
import com.example.ui.theme.GoldStarColor

@Composable
fun SearchScreen(
    language: AppLanguage,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    selectedCategory: StoreCategory,
    onSelectCategory: (StoreCategory) -> Unit,
    onOpenStore: (Store) -> Unit,
    onAddToCart: (Product) -> Unit,
    favoriteStoreIds: Set<String>,
    onToggleFavoriteStore: (String) -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0 = All, 1 = Stores, 2 = Products
    var filterOpenNowOnly by remember { mutableStateOf(false) }
    var filterRatingHigh by remember { mutableStateOf(false) }
    var filterLowFee by remember { mutableStateOf(false) }
    var sortBy by remember { mutableIntStateOf(0) } // 0 = Recommended, 1 = Nearest, 2 = Rating, 3 = Delivery fee
    var customizingProduct by remember { mutableStateOf<Product?>(null) }

    // Popular search chips
    val popularTags = listOf("برجر", "بيتزا", "طاجين", "شاورما", "كريب", "عصير طازج", "قهوة", "حليب وبيض", "باراسيتامول")

    // Filter Stores
    val matchedStores = CatalogData.stores.filter { store ->
        val matchesCategory = selectedCategory == StoreCategory.ALL || store.category == selectedCategory
        val matchesQuery = searchQuery.isBlank() ||
                store.name.contains(searchQuery, ignoreCase = true) ||
                store.nameAr.contains(searchQuery, ignoreCase = true) ||
                store.nameFr.contains(searchQuery, ignoreCase = true) ||
                store.address.contains(searchQuery, ignoreCase = true)

        val matchesOpen = !filterOpenNowOnly || store.isOpen
        val matchesRating = !filterRatingHigh || store.rating >= 4.5
        val matchesFee = !filterLowFee || store.deliveryFee <= 10.0

        matchesCategory && matchesQuery && matchesOpen && matchesRating && matchesFee
    }.let { list ->
        when (sortBy) {
            1 -> list.sortedBy { it.distanceKm }
            2 -> list.sortedByDescending { it.rating }
            3 -> list.sortedBy { it.deliveryFee }
            else -> list
        }
    }

    // Filter Products
    val matchedProducts = CatalogData.products.filter { product ->
        val matchesCategory = selectedCategory == StoreCategory.ALL || product.category == selectedCategory
        val matchesQuery = searchQuery.isBlank() ||
                product.nameAr.contains(searchQuery, ignoreCase = true) ||
                product.nameEn.contains(searchQuery, ignoreCase = true) ||
                product.nameFr.contains(searchQuery, ignoreCase = true) ||
                product.descriptionAr.contains(searchQuery, ignoreCase = true) ||
                product.storeName.contains(searchQuery, ignoreCase = true)

        val matchesRating = !filterRatingHigh || product.rating >= 4.5

        matchesCategory && matchesQuery && matchesRating
    }.let { list ->
        when (sortBy) {
            2 -> list.sortedByDescending { it.rating }
            3 -> list.sortedBy { it.price }
            else -> list
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("search_screen"),
        contentPadding = PaddingValues(bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Search Input Header
        item {
            Card(
                shape = RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = when (language) {
                            AppLanguage.ARABIC -> "البحث والاستكشاف بالمحمدية 🇲🇦"
                            AppLanguage.FRENCH -> "Recherche & Exploration Mohammedia 🇲🇦"
                            AppLanguage.ENGLISH -> "Search & Explore Mohammedia 🇲🇦"
                        },
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = AzizOrangePrimary
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = onSearchChange,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("search_page_input"),
                        placeholder = {
                            Text(
                                text = when (language) {
                                    AppLanguage.ARABIC -> "ابحث عن مطعم، وجبة، بقالة، صيدلية..."
                                    AppLanguage.FRENCH -> "Rechercher restaurant, plat, épicerie..."
                                    AppLanguage.ENGLISH -> "Search restaurants, meals, groceries..."
                                },
                                fontSize = 13.sp
                            )
                        },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = AzizOrangePrimary)
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { onSearchChange("") }) {
                                    Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear")
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AzizOrangePrimary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Popular tags quick chips
                    Text(
                        text = when (language) {
                            AppLanguage.ARABIC -> "عمليات البحث الشائعة:"
                            AppLanguage.FRENCH -> "Recherches populaires :"
                            AppLanguage.ENGLISH -> "Popular searches:"
                        },
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(popularTags) { tag ->
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { onSearchChange(tag) }
                            ) {
                                Text(
                                    text = tag,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        }

        // Categories & Filter Chips
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                // Category Selector
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(StoreCategory.values()) { category ->
                        val isSelected = selectedCategory == category
                        FilterChip(
                            selected = isSelected,
                            onClick = { onSelectCategory(category) },
                            label = {
                                Text(
                                    text = "${category.iconName} ${when (language) {
                                        AppLanguage.ARABIC -> category.titleAr
                                        AppLanguage.FRENCH -> category.titleFr
                                        AppLanguage.ENGLISH -> category.titleEn
                                    }}",
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = AzizOrangePrimary,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Fast Filter Chips
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    item {
                        FilterChip(
                            selected = filterOpenNowOnly,
                            onClick = { filterOpenNowOnly = !filterOpenNowOnly },
                            label = { Text("🟢 مفتوح الآن", fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = AzizMint, selectedLabelColor = Color.White)
                        )
                    }
                    item {
                        FilterChip(
                            selected = filterRatingHigh,
                            onClick = { filterRatingHigh = !filterRatingHigh },
                            label = { Text("⭐ 4.5+", fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = GoldStarColor, selectedLabelColor = Color.Black)
                        )
                    }
                    item {
                        FilterChip(
                            selected = filterLowFee,
                            onClick = { filterLowFee = !filterLowFee },
                            label = { Text("🛵 توصيل اقتصادي (≤ 10 د.م)", fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = AzizOrangePrimary, selectedLabelColor = Color.White)
                        )
                    }
                }
            }
        }

        // Tabs: All / Stores / Products
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
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
                        onClick = { selectedTab = 0 },
                        text = {
                            Text(
                                text = "الكل (${matchedStores.size + matchedProducts.size})",
                                fontSize = 12.sp,
                                fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedTab == 0) AzizOrangePrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = {
                            Text(
                                text = "المتاجر والمطاعم (${matchedStores.size})",
                                fontSize = 12.sp,
                                fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedTab == 1) AzizOrangePrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    )
                    Tab(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        text = {
                            Text(
                                text = "الوجبات والمنتجات (${matchedProducts.size})",
                                fontSize = 12.sp,
                                fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedTab == 2) AzizOrangePrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    )
                }
            }
        }

        // Empty State Check
        if ((selectedTab == 0 && matchedStores.isEmpty() && matchedProducts.isEmpty()) ||
            (selectedTab == 1 && matchedStores.isEmpty()) ||
            (selectedTab == 2 && matchedProducts.isEmpty())
        ) {
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Text("🔍", fontSize = 40.sp)
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "لم نعثر على نتائج مطابقة لـ \"$searchQuery\"",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "جرب البحث بكلمات أخرى مثل (برجر، بيتزا، طاجين، شاي، صيدلية، مرجان) أو اختر قسماً آخر.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 18.sp
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Button(
                            onClick = {
                                onSearchChange("")
                                onSelectCategory(StoreCategory.ALL)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = AzizOrangePrimary),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("عرض جميع المتاجر والوجبات")
                        }
                    }
                }
            }
        }

        // STORES LIST (Tab 0 or 1)
        if (selectedTab == 0 || selectedTab == 1) {
            if (matchedStores.isNotEmpty()) {
                item {
                    Text(
                        text = "المتاجر والمطاعم (${matchedStores.size})",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                items(matchedStores) { store ->
                    Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onOpenStore(store) }
                                .testTag("search_store_item_${store.id}")
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(74.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                ) {
                                    if (store.bannerUrl.isNotBlank()) {
                                        AsyncImage(
                                            model = store.bannerUrl,
                                            contentDescription = store.name,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    } else {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .background(MaterialTheme.colorScheme.surfaceVariant),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(store.emoji, fontSize = 32.sp)
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = store.nameAr,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(2.dp))

                                    Text(
                                        text = store.address,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )

                                    Spacer(modifier = Modifier.height(6.dp))

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = GoldStarColor.copy(alpha = 0.15f)
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(imageVector = Icons.Default.Star, contentDescription = null, tint = GoldStarColor, modifier = Modifier.size(12.dp))
                                                Spacer(modifier = Modifier.width(2.dp))
                                                Text(store.rating.toString(), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }

                                        Text(
                                            text = "⏱️ ${store.deliveryTimeMins} دقيقة",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )

                                        Text(
                                            text = "🛵 ${store.deliveryFee} د.م",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = AzizOrangePrimary
                                        )
                                    }
                                }

                                IconButton(
                                    onClick = { onToggleFavoriteStore(store.id) },
                                    modifier = Modifier.size(34.dp)
                                ) {
                                    val isFav = favoriteStoreIds.contains(store.id)
                                    Icon(
                                        imageVector = if (isFav) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                        contentDescription = "Favorite",
                                        tint = if (isFav) Color(0xFFEF4444) else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // PRODUCTS LIST (Tab 0 or 2)
        if (selectedTab == 0 || selectedTab == 2) {
            if (matchedProducts.isNotEmpty()) {
                item {
                    Text(
                        text = "الوجبات والمنتجات (${matchedProducts.size})",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                items(matchedProducts) { product ->
                    Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("search_product_item_${product.id}")
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(70.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                ) {
                                    if (product.imageUrl.isNotBlank()) {
                                        AsyncImage(
                                            model = product.imageUrl,
                                            contentDescription = product.nameAr,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    } else {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .background(MaterialTheme.colorScheme.surfaceVariant),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(product.emoji, fontSize = 28.sp)
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = product.nameAr,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = product.storeName,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "${product.price} ${AppStrings.currency(language)}",
                                        fontWeight = FontWeight.Black,
                                        fontSize = 13.sp,
                                        color = AzizOrangePrimary
                                    )
                                }

                                Button(
                                    onClick = {
                                        if (product.optionGroups.isNotEmpty()) {
                                            customizingProduct = product
                                        } else {
                                            onAddToCart(product)
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = AzizOrangePrimary),
                                    shape = RoundedCornerShape(10.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AddShoppingCart,
                                        contentDescription = "Add",
                                        modifier = Modifier.size(16.dp),
                                        tint = Color.White
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("أضف", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
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
                onAddToCart(customizingProduct!!)
                customizingProduct = null
            }
        )
    }
}
