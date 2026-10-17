package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ElectricScooter
import androidx.compose.material.icons.filled.HeadsetMic
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.localization.AppStrings
import com.example.data.model.AppLanguage
import com.example.data.model.UserRole
import com.example.ui.theme.AzizAmberSecondary
import com.example.ui.theme.AzizMint
import com.example.ui.theme.AzizOrangePrimary
import com.example.ui.viewmodel.AppScreen

@Composable
fun AlloAzizTopBar(
    currentScreen: AppScreen,
    currentRole: UserRole,
    language: AppLanguage,
    onRoleChange: (UserRole) -> Unit,
    onLanguageChange: (AppLanguage) -> Unit,
    onBackClick: () -> Unit,
    onNotificationClick: () -> Unit,
    onCartClick: () -> Unit,
    unreadNotifCount: Int,
    cartItemCount: Int,
    deliveryAddress: String,
    onAddressClick: (() -> Unit)? = null,
    onAiVoiceClick: (() -> Unit)? = null
) {
    var showLangMenu by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding(),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .padding(horizontal = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            if (currentScreen == AppScreen.HOME) {
                // Home: Logo
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.testTag("brand_logo_tag")
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(AzizOrangePrimary, AzizAmberSecondary)
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "🛵", fontSize = 16.sp)
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "ALLO AZIZ",
                        fontWeight = FontWeight.Black,
                        fontSize = 15.sp,
                        color = AzizOrangePrimary,
                        letterSpacing = 0.5.sp
                    )
                }

                // Center: Glovo-style Delivery Address Pill
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.75f),
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 8.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .clickable { onAddressClick?.invoke() }
                        .testTag("topbar_delivery_address_pill")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = AzizOrangePrimary,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = deliveryAddress.ifBlank {
                                when (language) {
                                    AppLanguage.FRENCH -> "Mohammédia, Maroc 🇲🇦"
                                    AppLanguage.ENGLISH -> "Mohammedia, Morocco 🇲🇦"
                                    AppLanguage.ARABIC -> "المحمدية 🇲🇦"
                                }
                            },
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            } else {
                // Subscreens: Back button + Title
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("top_bar_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = AzizOrangePrimary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    val title = when (currentScreen) {
                        AppScreen.CART_CHECKOUT -> when (language) {
                            AppLanguage.FRENCH -> "Panier et Paiement"
                            AppLanguage.ENGLISH -> "Cart & Checkout"
                            AppLanguage.ARABIC -> "سلة المشتريات والدفع"
                        }
                        AppScreen.ORDER_TRACKING -> when (language) {
                            AppLanguage.FRENCH -> "Suivi de Commande en Direct"
                            AppLanguage.ENGLISH -> "Live Order Tracking"
                            AppLanguage.ARABIC -> "تتبع مسار الطلب"
                        }
                        AppScreen.ORDER_HISTORY -> when (language) {
                            AppLanguage.FRENCH -> "Historique des Commandes"
                            AppLanguage.ENGLISH -> "Order History"
                            AppLanguage.ARABIC -> "سجل الطلبات"
                        }
                        AppScreen.FAVORITES -> when (language) {
                            AppLanguage.FRENCH -> "Mes Favoris"
                            AppLanguage.ENGLISH -> "My Favorites"
                            AppLanguage.ARABIC -> "قائمتي المفضلة"
                        }
                        AppScreen.PROFILE_SETTINGS -> when (language) {
                            AppLanguage.FRENCH -> "Profil et Paramètres"
                            AppLanguage.ENGLISH -> "Profile & Settings"
                            AppLanguage.ARABIC -> "الملف الشخصي والإعدادات"
                        }
                        AppScreen.SEARCH -> when (language) {
                            AppLanguage.FRENCH -> "Recherche & Catégories"
                            AppLanguage.ENGLISH -> "Search & Categories"
                            AppLanguage.ARABIC -> "البحث والتصنيفات"
                        }
                        AppScreen.NOTIFICATIONS -> when (language) {
                            AppLanguage.FRENCH -> "Notifications & Alertes"
                            AppLanguage.ENGLISH -> "Notifications & Alerts"
                            AppLanguage.ARABIC -> "التنبيهات والإشعارات"
                        }
                        AppScreen.LIVE_SUPPORT -> when (language) {
                            AppLanguage.FRENCH -> "Support Client 24/7"
                            AppLanguage.ENGLISH -> "24/7 Customer Support"
                            AppLanguage.ARABIC -> "الدعم الفني المباشر"
                        }
                        AppScreen.RESTAURANT_DASHBOARD -> when (language) {
                            AppLanguage.FRENCH -> "Tableau de Bord Restaurant 🍳"
                            AppLanguage.ENGLISH -> "Restaurant Dashboard 🍳"
                            AppLanguage.ARABIC -> "لوحة تحكم المطعم 🍳"
                        }
                        AppScreen.DRIVER_DASHBOARD -> when (language) {
                            AppLanguage.FRENCH -> "Espace Coursier Aziz 🛵"
                            AppLanguage.ENGLISH -> "Courier Aziz Space 🛵"
                            AppLanguage.ARABIC -> "لوحة كابتن التوصيل 🛵"
                        }
                        AppScreen.ADMIN_DASHBOARD -> when (language) {
                            AppLanguage.FRENCH -> "Administration Système ⚙️"
                            AppLanguage.ENGLISH -> "System Administration ⚙️"
                            AppLanguage.ARABIC -> "لوحة إدارة النظام ⚙️"
                        }
                        AppScreen.SUPPORT_DASHBOARD -> when (language) {
                            AppLanguage.FRENCH -> "Centre Support Client 🎧"
                            AppLanguage.ENGLISH -> "Customer Support Hub 🎧"
                            AppLanguage.ARABIC -> "مركز خدمة العملاء 🎧"
                        }
                        else -> "ALLO AZIZ"
                    }
                    Text(
                        text = title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Right Actions: Live indicator, Language Chip, Notifications, Cart
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                if (currentScreen == AppScreen.ORDER_TRACKING) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = AzizOrangePrimary.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = when (language) {
                                AppLanguage.FRENCH -> "DIRECT 🔴"
                                AppLanguage.ENGLISH -> "LIVE 🔴"
                                AppLanguage.ARABIC -> "مباشر 🔴"
                            },
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = AzizOrangePrimary
                        )
                    }
                }

                if (currentRole != UserRole.CUSTOMER && currentScreen == AppScreen.HOME) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(currentRole.badgeColorHex).copy(alpha = 0.15f),
                        modifier = Modifier.testTag("role_indicator_pill")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                        ) {
                            Text(currentRole.emoji, fontSize = 11.sp)
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = when (language) {
                                    AppLanguage.ARABIC -> currentRole.titleAr
                                    AppLanguage.FRENCH -> currentRole.titleFr
                                    AppLanguage.ENGLISH -> currentRole.titleEn
                                },
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(currentRole.badgeColorHex)
                            )
                        }
                    }
                }

                // Language Switcher Chip
                Box {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = AzizOrangePrimary.copy(alpha = 0.12f),
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { showLangMenu = true }
                            .testTag("language_switch_button")
                    ) {
                        Text(
                            text = when (language) {
                                AppLanguage.ARABIC -> "ع 🇲🇦"
                                AppLanguage.FRENCH -> "FR 🇫🇷"
                                AppLanguage.ENGLISH -> "EN 🇬🇧"
                            },
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = AzizOrangePrimary
                        )
                    }

                    DropdownMenu(
                        expanded = showLangMenu,
                        onDismissRequest = { showLangMenu = false }
                    ) {
                        val orderedLanguages = listOf(AppLanguage.FRENCH, AppLanguage.ARABIC, AppLanguage.ENGLISH)
                        orderedLanguages.forEach { lang ->
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = lang.displayName,
                                            fontWeight = if (lang == language) FontWeight.Black else FontWeight.Normal,
                                            fontSize = 13.sp,
                                            color = if (lang == language) AzizOrangePrimary else MaterialTheme.colorScheme.onSurface
                                        )
                                        if (lang == AppLanguage.FRENCH) {
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Surface(
                                                shape = RoundedCornerShape(4.dp),
                                                color = AzizMint.copy(alpha = 0.2f)
                                            ) {
                                                Text(
                                                    text = "Principale ⭐",
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = AzizMint,
                                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                    }
                                },
                                onClick = {
                                    onLanguageChange(lang)
                                    showLangMenu = false
                                }
                            )
                        }
                    }
                }

                if (currentScreen != AppScreen.AUTH) {
                    // Notifications Bell with Badge
                    BadgedBox(
                        badge = {
                            if (unreadNotifCount > 0) {
                                Badge(
                                    containerColor = AzizOrangePrimary,
                                    contentColor = Color.White
                                ) {
                                    Text(unreadNotifCount.toString(), fontSize = 9.sp)
                                }
                            }
                        }
                    ) {
                        IconButton(
                            onClick = onNotificationClick,
                            modifier = Modifier
                                .size(34.dp)
                                .testTag("notifications_icon_button")
                        ) {
                            Icon(
                                imageVector = if (unreadNotifCount > 0) Icons.Default.Notifications else Icons.Outlined.Notifications,
                                contentDescription = "Notifications",
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(19.dp)
                            )
                        }
                    }

                    // AI Voice Shopping Assistant Button (Customer only)
                    if (currentRole == UserRole.CUSTOMER) {
                        Surface(
                            onClick = { onAiVoiceClick?.invoke() },
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFF6366F1).copy(alpha = 0.15f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF6366F1).copy(alpha = 0.4f)),
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .testTag("topbar_ai_voice_button")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = "AI Voice Assistant",
                                    tint = Color(0xFF6366F1),
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = "AI 🎙️",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFF4F46E5)
                                )
                            }
                        }
                    }

                    // Cart Icon (only if customer and not already on Cart screen)
                    if (currentRole == UserRole.CUSTOMER && currentScreen != AppScreen.CART_CHECKOUT) {
                        BadgedBox(
                            badge = {
                                if (cartItemCount > 0) {
                                    Badge(
                                        containerColor = AzizMint,
                                        contentColor = Color.White
                                    ) {
                                        Text(cartItemCount.toString(), fontSize = 9.sp)
                                    }
                                }
                            }
                        ) {
                            IconButton(
                                onClick = onCartClick,
                                modifier = Modifier
                                    .size(34.dp)
                                    .testTag("cart_icon_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ShoppingBag,
                                    contentDescription = "Cart",
                                    tint = if (cartItemCount > 0) AzizOrangePrimary else MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.size(19.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AlloAzizBottomNav(
    currentScreen: AppScreen,
    currentRole: UserRole,
    language: AppLanguage,
    onNavigate: (AppScreen) -> Unit,
    activeOrderCount: Int
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp,
        modifier = Modifier.fillMaxWidth().testTag("bottom_nav_bar")
    ) {
        when (currentRole) {
            UserRole.CUSTOMER -> {
                val homeSelected = currentScreen == AppScreen.HOME
                val searchSelected = currentScreen == AppScreen.SEARCH
                val ordersSelected = currentScreen == AppScreen.ORDER_HISTORY || currentScreen == AppScreen.ORDER_TRACKING
                val favoritesSelected = currentScreen == AppScreen.FAVORITES
                val profileSelected = currentScreen == AppScreen.PROFILE_SETTINGS

                NavigationBarItem(
                    selected = homeSelected,
                    onClick = { onNavigate(AppScreen.HOME) },
                    icon = {
                        Icon(
                            imageVector = if (homeSelected) Icons.Filled.Home else Icons.Outlined.Home,
                            contentDescription = AppStrings.tabHome(language)
                        )
                    },
                    label = {
                        Text(
                            text = AppStrings.tabHome(language),
                            fontSize = 10.sp,
                            fontWeight = if (homeSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = AzizOrangePrimary,
                        selectedTextColor = AzizOrangePrimary,
                        indicatorColor = AzizOrangePrimary.copy(alpha = 0.15f)
                    )
                )

                NavigationBarItem(
                    selected = searchSelected,
                    onClick = { onNavigate(AppScreen.SEARCH) },
                    icon = {
                        Icon(
                            imageVector = if (searchSelected) Icons.Filled.Search else Icons.Outlined.Search,
                            contentDescription = AppStrings.tabSearch(language)
                        )
                    },
                    label = {
                        Text(
                            text = AppStrings.tabSearch(language),
                            fontSize = 10.sp,
                            fontWeight = if (searchSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = AzizOrangePrimary,
                        selectedTextColor = AzizOrangePrimary,
                        indicatorColor = AzizOrangePrimary.copy(alpha = 0.15f)
                    )
                )

                NavigationBarItem(
                    selected = ordersSelected,
                    onClick = { onNavigate(AppScreen.ORDER_HISTORY) },
                    icon = {
                        BadgedBox(
                            badge = {
                                if (activeOrderCount > 0) {
                                    Badge(containerColor = AzizOrangePrimary, contentColor = Color.White) {
                                        Text(activeOrderCount.toString())
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = if (ordersSelected) Icons.Filled.History else Icons.Outlined.History,
                                contentDescription = AppStrings.tabOrders(language)
                            )
                        }
                    },
                    label = {
                        Text(
                            text = AppStrings.tabOrders(language),
                            fontSize = 10.sp,
                            fontWeight = if (ordersSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = AzizOrangePrimary,
                        selectedTextColor = AzizOrangePrimary,
                        indicatorColor = AzizOrangePrimary.copy(alpha = 0.15f)
                    )
                )

                NavigationBarItem(
                    selected = favoritesSelected,
                    onClick = { onNavigate(AppScreen.FAVORITES) },
                    icon = {
                        Icon(
                            imageVector = if (favoritesSelected) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = AppStrings.tabFavorites(language)
                        )
                    },
                    label = {
                        Text(
                            text = AppStrings.tabFavorites(language),
                            fontSize = 10.sp,
                            fontWeight = if (favoritesSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = AzizOrangePrimary,
                        selectedTextColor = AzizOrangePrimary,
                        indicatorColor = AzizOrangePrimary.copy(alpha = 0.15f)
                    )
                )

                NavigationBarItem(
                    selected = profileSelected,
                    onClick = { onNavigate(AppScreen.PROFILE_SETTINGS) },
                    icon = {
                        Icon(
                            imageVector = if (profileSelected) Icons.Filled.Person else Icons.Outlined.Person,
                            contentDescription = AppStrings.tabProfile(language)
                        )
                    },
                    label = {
                        Text(
                            text = AppStrings.tabProfile(language),
                            fontSize = 10.sp,
                            fontWeight = if (profileSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = AzizOrangePrimary,
                        selectedTextColor = AzizOrangePrimary,
                        indicatorColor = AzizOrangePrimary.copy(alpha = 0.15f)
                    )
                )
            }

            UserRole.RESTAURANT_OWNER -> {
                NavigationBarItem(
                    selected = currentScreen == AppScreen.RESTAURANT_DASHBOARD,
                    onClick = { onNavigate(AppScreen.RESTAURANT_DASHBOARD) },
                    icon = { Icon(Icons.Default.Restaurant, contentDescription = null) },
                    label = { Text("المطبخ والطلبات", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(selectedIconColor = AzizOrangePrimary, indicatorColor = AzizOrangePrimary.copy(alpha = 0.15f))
                )
                NavigationBarItem(
                    selected = currentScreen == AppScreen.HOME,
                    onClick = { onNavigate(AppScreen.HOME) },
                    icon = { Icon(Icons.Default.ShoppingBag, contentDescription = null) },
                    label = { Text("تصفح المتجر", fontSize = 11.sp) }
                )
                NavigationBarItem(
                    selected = currentScreen == AppScreen.LIVE_SUPPORT,
                    onClick = { onNavigate(AppScreen.LIVE_SUPPORT) },
                    icon = { Icon(Icons.Default.SupportAgent, contentDescription = null) },
                    label = { Text("دعم الشركاء", fontSize = 11.sp) }
                )
                NavigationBarItem(
                    selected = currentScreen == AppScreen.PROFILE_SETTINGS,
                    onClick = { onNavigate(AppScreen.PROFILE_SETTINGS) },
                    icon = { Icon(Icons.Default.Person, contentDescription = null) },
                    label = { Text("حساب المطعم", fontSize = 11.sp) }
                )
            }

            UserRole.DRIVER -> {
                NavigationBarItem(
                    selected = currentScreen == AppScreen.DRIVER_DASHBOARD,
                    onClick = { onNavigate(AppScreen.DRIVER_DASHBOARD) },
                    icon = { Icon(Icons.Default.ElectricScooter, contentDescription = null) },
                    label = { Text("مهام التوصيل", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(selectedIconColor = AzizMint, indicatorColor = AzizMint.copy(alpha = 0.15f))
                )
                NavigationBarItem(
                    selected = currentScreen == AppScreen.ORDER_TRACKING,
                    onClick = { onNavigate(AppScreen.ORDER_TRACKING) },
                    icon = { Icon(Icons.Default.LocationOn, contentDescription = null) },
                    label = { Text("خريطة الملاحة", fontSize = 11.sp) }
                )
                NavigationBarItem(
                    selected = currentScreen == AppScreen.LIVE_SUPPORT,
                    onClick = { onNavigate(AppScreen.LIVE_SUPPORT) },
                    icon = { Icon(Icons.Default.SupportAgent, contentDescription = null) },
                    label = { Text("دعم السائقين", fontSize = 11.sp) }
                )
                NavigationBarItem(
                    selected = currentScreen == AppScreen.PROFILE_SETTINGS,
                    onClick = { onNavigate(AppScreen.PROFILE_SETTINGS) },
                    icon = { Icon(Icons.Default.Person, contentDescription = null) },
                    label = { Text("محفظتي", fontSize = 11.sp) }
                )
            }

            UserRole.SUPPORT_AGENT -> {
                NavigationBarItem(
                    selected = currentScreen == AppScreen.SUPPORT_DASHBOARD,
                    onClick = { onNavigate(AppScreen.SUPPORT_DASHBOARD) },
                    icon = { Icon(Icons.Default.HeadsetMic, contentDescription = null) },
                    label = { Text("تفعيل السائقين", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(selectedIconColor = AzizOrangePrimary, indicatorColor = AzizOrangePrimary.copy(alpha = 0.15f))
                )
                NavigationBarItem(
                    selected = currentScreen == AppScreen.LIVE_SUPPORT,
                    onClick = { onNavigate(AppScreen.LIVE_SUPPORT) },
                    icon = { Icon(Icons.Default.SupportAgent, contentDescription = null) },
                    label = { Text("محادثات الدعم", fontSize = 11.sp) }
                )
                NavigationBarItem(
                    selected = currentScreen == AppScreen.PROFILE_SETTINGS,
                    onClick = { onNavigate(AppScreen.PROFILE_SETTINGS) },
                    icon = { Icon(Icons.Default.Person, contentDescription = null) },
                    label = { Text("الملف الشخصي", fontSize = 11.sp) }
                )
            }

            UserRole.SUPER_ADMIN -> {
                NavigationBarItem(
                    selected = currentScreen == AppScreen.ADMIN_DASHBOARD,
                    onClick = { onNavigate(AppScreen.ADMIN_DASHBOARD) },
                    icon = { Icon(Icons.Default.AdminPanelSettings, contentDescription = null) },
                    label = { Text("الإدارة العامة", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(selectedIconColor = Color(0xFF6A1B9A), indicatorColor = Color(0xFF6A1B9A).copy(alpha = 0.15f))
                )
                NavigationBarItem(
                    selected = currentScreen == AppScreen.SUPPORT_DASHBOARD,
                    onClick = { onNavigate(AppScreen.SUPPORT_DASHBOARD) },
                    icon = { Icon(Icons.Default.HeadsetMic, contentDescription = null) },
                    label = { Text("الدعم والسائقين", fontSize = 11.sp) }
                )
                NavigationBarItem(
                    selected = currentScreen == AppScreen.HOME,
                    onClick = { onNavigate(AppScreen.HOME) },
                    icon = { Icon(Icons.Default.ShoppingBag, contentDescription = null) },
                    label = { Text("تصفح المنصة", fontSize = 11.sp) }
                )
                NavigationBarItem(
                    selected = currentScreen == AppScreen.PROFILE_SETTINGS,
                    onClick = { onNavigate(AppScreen.PROFILE_SETTINGS) },
                    icon = { Icon(Icons.Default.Person, contentDescription = null) },
                    label = { Text("حساب المدير", fontSize = 11.sp) }
                )
            }
        }
    }
}
