package com.example

import android.Manifest
import android.accounts.AccountManager
import android.app.Activity
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.location.LocationServiceManager
import com.example.data.model.UserRole
import com.example.ui.components.AlloAzizBottomNav
import com.example.ui.components.AlloAzizTopBar
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.CartCheckoutScreen
import com.example.ui.screens.DriverPartnerDashboard
import com.example.ui.screens.FavoritesScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LiveSupportScreen
import com.example.ui.screens.NotificationsScreen
import com.example.ui.screens.OrderHistoryScreen
import com.example.ui.screens.OrderTrackingScreen
import com.example.ui.screens.ProfileSettingsScreen
import com.example.ui.screens.RestaurantOwnerDashboard
import com.example.ui.screens.SearchScreen
import com.example.ui.screens.StaffLoginScreen
import com.example.ui.screens.StoreDetailScreen
import com.example.ui.screens.SuperAdminDashboard
import com.example.ui.screens.SupportAgentDashboard
import com.example.ui.theme.AlloAzizTheme
import com.example.ui.viewmodel.AlloAzizViewModel
import com.example.ui.viewmodel.AppScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AlloAzizApp()
        }
    }
}

@Composable
fun AlloAzizApp(viewModel: AlloAzizViewModel = viewModel()) {
    val context = LocalContext.current
    val currentScreen by viewModel.currentScreen.collectAsState()
    val currentRole by viewModel.currentRole.collectAsState()
    val driverIsOnline by viewModel.driverIsOnline.collectAsState()
    val language by viewModel.selectedLanguage.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedStore by viewModel.selectedStore.collectAsState()

    val cartItems by viewModel.cartItems.collectAsState()
    val appliedPromo by viewModel.appliedPromoCode.collectAsState()
    val promoDiscount by viewModel.promoDiscountPercent.collectAsState()
    val deliveryAddress by viewModel.deliveryAddress.collectAsState()
    val deliveryNotes by viewModel.deliveryNotes.collectAsState()
    val paymentMethod by viewModel.selectedPaymentMethod.collectAsState()

    val allOrders by viewModel.allOrders.collectAsState()
    val activeOrder by viewModel.activeOrder.collectAsState()
    val trackingOrderId by viewModel.trackingOrderId.collectAsState()
    val allNotifications by viewModel.allNotifications.collectAsState()
    val unreadNotifCount by viewModel.unreadNotificationsCount.collectAsState()
    val allReviews by viewModel.allReviews.collectAsState()
    val wallet by viewModel.wallet.collectAsState()
    val supportMessages by viewModel.supportMessages.collectAsState()
    val alertMessage by viewModel.userAlertMessage.collectAsState()

    val allUsers by viewModel.allUsers.collectAsState()
    val allStoresFromDb by viewModel.allStoresFromDb.collectAsState()
    val allProductsFromDb by viewModel.allProductsFromDb.collectAsState()
    val allTickets by viewModel.allTickets.collectAsState()
    val favoriteStoreIds by viewModel.favoriteStoreIds.collectAsState()
    val favoriteProductIds by viewModel.favoriteProductIds.collectAsState()
    val allAddresses by viewModel.allAddresses.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()
    val selectedCity by viewModel.selectedCity.collectAsState()
    val deviceLocation by viewModel.deviceLocation.collectAsState()
    val isGpsLive by viewModel.isLiveLocationTrackingEnabled.collectAsState()
    val isNetworkConnected by viewModel.isNetworkConnected.collectAsState()

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { /* Permission result handled */ }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
        val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (fineGranted || coarseGranted) {
            viewModel.fetchCurrentGpsLocation(context, updateDeliveryAddress = true)
        }
    }

    val googleAccountPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            val accountEmail = result.data?.getStringExtra(AccountManager.KEY_ACCOUNT_NAME)
            if (!accountEmail.isNullOrBlank()) {
                val displayName = accountEmail.substringBefore("@")
                    .replace(".", " ")
                    .split(" ")
                    .joinToString(" ") { part ->
                        part.replaceFirstChar { char -> if (char.isLowerCase()) char.titlecase() else char.toString() }
                    }
                viewModel.loginWithGoogle(accountEmail, displayName)
            }
        }
    }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
        val notifOrderId = (context as? ComponentActivity)?.intent?.getStringExtra("TRACK_ORDER_ID")
        if (!notifOrderId.isNullOrBlank()) {
            viewModel.setTrackingOrder(notifOrderId)
        }
    }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(alertMessage) {
        alertMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearAlertMessage()
        }
    }

    // Set RTL layout for Arabic, LTR for French & English
    val layoutDirection = if (language.isRtl) LayoutDirection.Rtl else LayoutDirection.Ltr

    CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
        AlloAzizTheme {
            // Android System Back Button Handler
            BackHandler(enabled = currentScreen != AppScreen.HOME) {
                viewModel.navigateBack()
            }

            Scaffold(
                modifier = Modifier.fillMaxSize(),
                contentWindowInsets = WindowInsets.safeDrawing,
                topBar = {
                    AlloAzizTopBar(
                        currentScreen = currentScreen,
                        currentRole = currentRole,
                        language = language,
                        onRoleChange = { viewModel.switchRole(it) },
                        onLanguageChange = { viewModel.setLanguage(it) },
                        onBackClick = { viewModel.navigateBack() },
                        onNotificationClick = { viewModel.navigateTo(AppScreen.NOTIFICATIONS) },
                        onCartClick = { viewModel.navigateTo(AppScreen.CART_CHECKOUT) },
                        unreadNotifCount = unreadNotifCount,
                        cartItemCount = cartItems.sumOf { it.quantity },
                        deliveryAddress = deliveryAddress
                    )
                },
                bottomBar = {
                    if (currentScreen != AppScreen.AUTH) {
                        val activeCount = allOrders.count { it.status != "DELIVERED" }
                        AlloAzizBottomNav(
                            currentScreen = currentScreen,
                            currentRole = currentRole,
                            language = language,
                            onNavigate = { viewModel.navigateTo(it) },
                            activeOrderCount = activeCount
                        )
                    }
                },
                snackbarHost = { SnackbarHost(snackbarHostState) }
            ) { innerPadding ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    AnimatedVisibility(visible = !isNetworkConnected) {
                        Surface(
                            color = MaterialTheme.colorScheme.errorContainer,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = "⚠️ أنت في وضع عدم الاتصال (Offline) - يعمل التطبيق بذاكرة الجهاز المحلية",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                    when (currentScreen) {
                        AppScreen.AUTH -> {
                            val authError by viewModel.authError.collectAsState()
                            val pendingMsg by viewModel.authPendingApprovalMessage.collectAsState()
                            val authSuccess by viewModel.authSuccessMessage.collectAsState()

                            AuthScreen(
                                language = language,
                                authError = authError,
                                pendingApprovalMsg = pendingMsg,
                                authSuccessMsg = authSuccess,
                                onLoginCredentials = { id, pass -> viewModel.loginWithCredentials(id, pass) },
                                onLoginGoogle = { email, name -> viewModel.loginWithGoogle(email, name) },
                                onTriggerGoogleSignIn = { onFallback ->
                                    try {
                                        val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                            AccountManager.newChooseAccountIntent(
                                                null,
                                                null,
                                                arrayOf("com.google"),
                                                null,
                                                null,
                                                null,
                                                null
                                            )
                                        } else {
                                            @Suppress("DEPRECATION")
                                            AccountManager.newChooseAccountIntent(
                                                null,
                                                null,
                                                arrayOf("com.google"),
                                                false,
                                                null,
                                                null,
                                                null,
                                                null
                                            )
                                        }
                                        googleAccountPickerLauncher.launch(intent)
                                    } catch (e: Exception) {
                                        onFallback()
                                    }
                                },
                                onRegister = { name, email, phone, pass, role, rName, vType, plate ->
                                    viewModel.registerUser(name, email, phone, pass, role, rName, vType, plate)
                                },
                                onClearMessages = { viewModel.clearAuthMessages() },
                                onOpenStaffLogin = { viewModel.navigateTo(AppScreen.STAFF_LOGIN) },
                                onRequestPasswordReset = { id, onOtp -> viewModel.requestPasswordReset(id, onOtp) },
                                onConfirmPasswordReset = { id, newPass -> viewModel.confirmPasswordReset(id, newPass) }
                            )
                        }

                        AppScreen.STAFF_LOGIN -> {
                            val authError by viewModel.authError.collectAsState()
                            StaffLoginScreen(
                                language = language,
                                authError = authError,
                                onBackToCustomerLogin = { viewModel.navigateTo(AppScreen.AUTH) },
                                onLoginStaff = { id, pass, targetRole ->
                                    viewModel.loginStaff(id, pass, targetRole)
                                }
                            )
                        }

                        AppScreen.HOME -> {
                            HomeScreen(
                                language = language,
                                searchQuery = searchQuery,
                                onSearchChange = { viewModel.setSearchQuery(it) },
                                selectedCategory = selectedCategory,
                                onSelectCategory = { viewModel.selectCategory(it) },
                                activeOrder = activeOrder,
                                onOpenStore = { viewModel.openStore(it) },
                                onAddToCart = { viewModel.addToCart(it) },
                                onTrackOrder = { viewModel.setTrackingOrder(it) },
                                onApplyPromo = { viewModel.applyPromoCode(it) },
                                favoriteStoreIds = favoriteStoreIds,
                                onToggleFavoriteStore = { viewModel.toggleFavoriteStore(it) },
                                currentAddress = deliveryAddress,
                                currentCity = selectedCity,
                                onSelectAddress = { viewModel.setDeliveryAddress(it) },
                                onSelectCity = { viewModel.setDeliveryCity(it) },
                                savedAddresses = allAddresses,
                                onAddNewAddress = { title, type, city, neighborhood, street, notes ->
                                    viewModel.addNewAddress(title, type, city, neighborhood, street, notes)
                                },
                                onDeleteAddress = { viewModel.deleteAddress(it) },
                                onNavigateToSearch = { viewModel.navigateTo(AppScreen.SEARCH) }
                            )
                        }

                        AppScreen.SEARCH -> {
                            SearchScreen(
                                language = language,
                                searchQuery = searchQuery,
                                onSearchChange = { viewModel.setSearchQuery(it) },
                                selectedCategory = selectedCategory,
                                onSelectCategory = { viewModel.selectCategory(it) },
                                onOpenStore = { viewModel.openStore(it) },
                                onAddToCart = { viewModel.addToCart(it) },
                                favoriteStoreIds = favoriteStoreIds,
                                onToggleFavoriteStore = { viewModel.toggleFavoriteStore(it) }
                            )
                        }

                        AppScreen.STORE_DETAIL -> {
                            selectedStore?.let { store ->
                                StoreDetailScreen(
                                    store = store,
                                    language = language,
                                    onBack = { viewModel.navigateBack() },
                                    onAddToCart = { viewModel.addToCart(it) }
                                )
                            } ?: run {
                                viewModel.navigateTo(AppScreen.HOME)
                            }
                        }

                        AppScreen.CART_CHECKOUT -> {
                            CartCheckoutScreen(
                                language = language,
                                cartItems = cartItems,
                                walletBalance = wallet?.balance ?: 0.0,
                                deliveryAddress = deliveryAddress,
                                deliveryNotes = deliveryNotes,
                                promoCode = appliedPromo,
                                discountPercent = promoDiscount,
                                selectedPaymentMethod = paymentMethod,
                                onBack = { viewModel.navigateBack() },
                                onUpdateQuantity = { id, delta -> viewModel.updateCartQuantity(id, delta) },
                                onAddressChange = { viewModel.setDeliveryAddress(it) },
                                onNotesChange = { viewModel.setDeliveryNotes(it) },
                                onApplyPromo = { viewModel.applyPromoCode(it) },
                                onSelectPaymentMethod = { viewModel.setPaymentMethod(it) },
                                onPlaceOrder = { viewModel.placeOrder { /* handled */ } },
                                onNavigateToHome = { viewModel.navigateTo(AppScreen.HOME) },
                                onUseCurrentGpsLocation = {
                                    if (!LocationServiceManager.hasLocationPermission(context)) {
                                        locationPermissionLauncher.launch(
                                            arrayOf(
                                                Manifest.permission.ACCESS_FINE_LOCATION,
                                                Manifest.permission.ACCESS_COARSE_LOCATION
                                            )
                                        )
                                    } else {
                                        viewModel.fetchCurrentGpsLocation(context, updateDeliveryAddress = true)
                                    }
                                }
                            )
                        }

                        AppScreen.ORDER_TRACKING -> {
                            val orderToTrack = allOrders.firstOrNull { it.id == trackingOrderId }
                                ?: activeOrder
                                ?: allOrders.firstOrNull()

                            OrderTrackingScreen(
                                order = orderToTrack,
                                language = language,
                                currentDeviceLocation = deviceLocation,
                                isGpsLive = isGpsLive,
                                onBack = { viewModel.navigateBack() },
                                onSubmitReview = { orderId, storeName, sRating, cRating, tags, comment ->
                                    viewModel.submitReview(orderId, storeName, sRating, cRating, tags, comment)
                                },
                                onContactSupport = { viewModel.navigateTo(AppScreen.LIVE_SUPPORT) },
                                onOpenGoogleMaps = { order ->
                                    viewModel.openGoogleMapsRoute(context, order)
                                },
                                onRequestGpsLocation = {
                                    if (!LocationServiceManager.hasLocationPermission(context)) {
                                        locationPermissionLauncher.launch(
                                            arrayOf(
                                                Manifest.permission.ACCESS_FINE_LOCATION,
                                                Manifest.permission.ACCESS_COARSE_LOCATION
                                            )
                                        )
                                    } else {
                                        viewModel.fetchCurrentGpsLocation(context)
                                    }
                                },
                                onToggleGpsTracking = { enabled ->
                                    if (enabled) {
                                        viewModel.startRealtimeLocationTracking(context)
                                    } else {
                                        viewModel.stopRealtimeLocationTracking()
                                    }
                                },
                                onShareReceipt = { order ->
                                    viewModel.shareOrderReceipt(context, order)
                                },
                                onOpenWhatsAppCourier = { order ->
                                    viewModel.openCourierWhatsApp(context, order)
                                },
                                onAdvanceStage = { orderId ->
                                    viewModel.advanceOrderTrackingStage(orderId)
                                }
                            )
                        }

                        AppScreen.ORDER_HISTORY -> {
                            OrderHistoryScreen(
                                orders = allOrders,
                                language = language,
                                onTrackOrder = { viewModel.setTrackingOrder(it) },
                                onReorder = { viewModel.reorder(it) }
                            )
                        }

                        AppScreen.LIVE_SUPPORT -> {
                            LiveSupportScreen(
                                messages = supportMessages,
                                language = language,
                                onSendMessage = { viewModel.sendSupportMessage(it) }
                            )
                        }

                        AppScreen.NOTIFICATIONS -> {
                            NotificationsScreen(
                                notifications = allNotifications,
                                language = language,
                                onNotificationClick = { notif ->
                                    viewModel.markNotificationRead(notif.id)
                                    if (notif.orderId != null) {
                                        viewModel.setTrackingOrder(notif.orderId)
                                    }
                                },
                                onMarkAllRead = { viewModel.markAllNotificationsRead() }
                            )
                        }

                        AppScreen.FAVORITES -> {
                            FavoritesScreen(
                                language = language,
                                favoriteStoreIds = favoriteStoreIds,
                                favoriteProductIds = favoriteProductIds,
                                onToggleFavoriteStore = { viewModel.toggleFavoriteStore(it) },
                                onToggleFavoriteProduct = { viewModel.toggleFavoriteProduct(it) },
                                onOpenStore = { viewModel.openStore(it) },
                                onAddToCart = { viewModel.addToCart(it) }
                            )
                        }

                        AppScreen.PROFILE_SETTINGS -> {
                            val currentUser by viewModel.currentUser.collectAsState()
                            ProfileSettingsScreen(
                                language = language,
                                currentRole = currentRole,
                                currentUser = currentUser,
                                wallet = wallet,
                                reviews = allReviews,
                                userProfile = userProfile,
                                onSaveUserProfile = { viewModel.saveUserProfile(it) },
                                onRoleChange = { viewModel.switchRole(it) },
                                onLanguageChange = { viewModel.setLanguage(it) },
                                onTopUpWallet = { viewModel.topUpWallet(it) },
                                onNavigateToSupport = { viewModel.navigateTo(AppScreen.LIVE_SUPPORT) },
                                onLogout = { viewModel.logout() }
                            )
                        }

                        AppScreen.RESTAURANT_DASHBOARD -> {
                            RestaurantOwnerDashboard(
                                language = language,
                                stores = allStoresFromDb,
                                products = allProductsFromDb,
                                orders = allOrders,
                                onToggleStoreOpen = { storeId, isOpen -> viewModel.toggleStoreOpen(storeId, isOpen) },
                                onAddProduct = { sId, sName, nAr, nFr, price, cat, desc, emoji, imageUrl ->
                                    viewModel.addProductToMenu(sId, sName, nAr, nFr, price, cat, desc, emoji, imageUrl)
                                },
                                onToggleProductAvailable = { pId, isAvail -> viewModel.toggleProductAvailability(pId, isAvail) },
                                onDeleteProduct = { pId -> viewModel.deleteProduct(pId) },
                                onUpdateOrderStatus = { oId, rStat, oStat -> viewModel.updateOrderCookingStatus(oId, rStat, oStat) }
                            )
                        }

                        AppScreen.DRIVER_DASHBOARD -> {
                            DriverPartnerDashboard(
                                language = language,
                                isOnline = driverIsOnline,
                                orders = allOrders,
                                currentDeviceLocation = deviceLocation,
                                onToggleOnline = { viewModel.toggleDriverOnline() },
                                onAcceptTask = { orderId -> viewModel.acceptDeliveryTask(orderId) },
                                onMarkPickedUp = { orderId -> viewModel.markOrderPickedUpFromKitchen(orderId) },
                                onMarkDelivered = { orderId -> viewModel.markOrderDeliveredToCustomer(orderId) },
                                onNavigateWithGoogleMaps = { order ->
                                    viewModel.openGoogleMapsRoute(context, order)
                                },
                                onBroadcastGpsLocation = { orderId ->
                                    viewModel.startRealtimeLocationTracking(context)
                                }
                            )
                        }

                        AppScreen.SUPPORT_DASHBOARD -> {
                            SupportAgentDashboard(
                                language = language,
                                users = allUsers,
                                tickets = allTickets,
                                onApproveDriver = { userId -> viewModel.approveUserAccount(userId) },
                                onRejectDriver = { userId -> viewModel.rejectUserAccount(userId) },
                                onResolveTicket = { ticketId -> viewModel.resolveTicket(ticketId) },
                                onGrantCompensation = { amount -> viewModel.grantCustomerCompensation(amount) }
                            )
                        }

                        AppScreen.ADMIN_DASHBOARD -> {
                            SuperAdminDashboard(
                                language = language,
                                users = allUsers,
                                stores = allStoresFromDb,
                                onUpdateUserStatus = { uId, stat -> viewModel.updateUserStatus(uId, stat) },
                                onUpdateUserRole = { uId, role -> viewModel.updateUserRole(uId, role) },
                                onAddNewStore = { name, nameAr, cat, addr, fee, emoji ->
                                    viewModel.addNewStore(name, nameAr, cat, addr, fee, emoji)
                                },
                                onBroadcastAnnouncement = { title, body -> viewModel.broadcastSystemAnnouncement(title, body) }
                            )
                        }
                    }
                }
            }
            }
        }
    }
}
