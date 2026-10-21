package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.location.DeviceLocation
import com.example.data.location.LocationServiceManager
import com.example.data.system.AndroidNotificationHelper
import com.example.data.system.ExternalShareHelper
import com.example.data.system.HapticFeedbackHelper
import com.example.data.system.NetworkConnectivityObserver
import com.example.data.services.FirestoreSyncManager
import com.example.data.local.AddressEntity
import com.example.data.local.AlloAzizRepository
import com.example.data.local.AppDatabase
import com.example.data.local.NotificationEntity
import com.example.data.local.OrderEntity
import com.example.data.local.ProductEntity
import com.example.data.local.ReviewEntity
import com.example.data.local.StoreEntity
import com.example.data.local.SupportTicketEntity
import com.example.data.local.UserAccountEntity
import com.example.data.local.UserProfile
import com.example.data.local.WalletProfileEntity
import com.example.data.model.AppLanguage
import com.example.data.model.CartItem
import com.example.data.model.CatalogData
import com.example.data.model.PaymentMethodType
import com.example.data.model.Product
import com.example.data.model.Store
import com.example.data.model.StoreCategory
import com.example.data.model.SupportChatMessage
import com.example.data.model.UserRole
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.random.Random

enum class AppScreen {
    AUTH,
    HOME,
    SEARCH,
    STORE_DETAIL,
    CART_CHECKOUT,
    ORDER_TRACKING,
    ORDER_HISTORY,
    FAVORITES,
    LIVE_SUPPORT,
    NOTIFICATIONS,
    PROFILE_SETTINGS,
    // Portals for other roles:
    STAFF_LOGIN,
    RESTAURANT_DASHBOARD,
    DRIVER_DASHBOARD,
    SUPPORT_DASHBOARD,
    ADMIN_DASHBOARD
}

data class DeliveryZone(
    val id: String,
    val nameAr: String,
    val nameFr: String,
    val deliveryFee: Double,
    val minOrder: Double,
    val estimatedMins: Int,
    val isActive: Boolean = true
)

class AlloAzizViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: AlloAzizRepository

    // Screen Navigation (Opens directly into Login/Register screen)
    private val _currentScreen = MutableStateFlow(AppScreen.AUTH)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    private val _screenHistory = mutableListOf<AppScreen>()

    // Authentication Session State
    private val _currentUser = MutableStateFlow<UserAccountEntity?>(null)
    val currentUser: StateFlow<UserAccountEntity?> = _currentUser.asStateFlow()

    private val _authError = MutableStateFlow<String?>(null)
    val authError: StateFlow<String?> = _authError.asStateFlow()

    private val _authPendingApprovalMessage = MutableStateFlow<String?>(null)
    val authPendingApprovalMessage: StateFlow<String?> = _authPendingApprovalMessage.asStateFlow()

    private val _authSuccessMessage = MutableStateFlow<String?>(null)
    val authSuccessMessage: StateFlow<String?> = _authSuccessMessage.asStateFlow()

    // Active Role & User Session
    private val _currentRole = MutableStateFlow(UserRole.CUSTOMER)
    val currentRole: StateFlow<UserRole> = _currentRole.asStateFlow()

    private val _currentUserId = MutableStateFlow("")
    val currentUserId: StateFlow<String> = _currentUserId.asStateFlow()

    // Driver Status
    private val _driverIsOnline = MutableStateFlow(false)
    val driverIsOnline: StateFlow<Boolean> = _driverIsOnline.asStateFlow()

    // Language (Default: French 🇫🇷 as primary language, customer can switch to English or Arabic)
    private val prefs = getApplication<Application>().getSharedPreferences("allo_aziz_prefs", Context.MODE_PRIVATE)

    private val _selectedLanguage = MutableStateFlow(
        try {
            val savedCode = prefs.getString("user_lang", AppLanguage.FRENCH.code)
            AppLanguage.values().firstOrNull { it.code == savedCode } ?: AppLanguage.FRENCH
        } catch (e: Exception) {
            AppLanguage.FRENCH
        }
    )
    val selectedLanguage: StateFlow<AppLanguage> = _selectedLanguage.asStateFlow()

    // Store & Catalog selection
    private val _selectedBranch = MutableStateFlow(com.example.data.model.AppBranch.FOOD)
    val selectedBranch: StateFlow<com.example.data.model.AppBranch> = _selectedBranch.asStateFlow()

    fun selectBranch(branch: com.example.data.model.AppBranch) {
        _selectedBranch.value = branch
        if (branch == com.example.data.model.AppBranch.ECOMMERCE) {
            _selectedCategory.value = StoreCategory.ECOMMERCE
        } else if (_selectedCategory.value == StoreCategory.ECOMMERCE) {
            _selectedCategory.value = StoreCategory.ALL
        }
    }

    private val _selectedCategory = MutableStateFlow(StoreCategory.ALL)
    val selectedCategory: StateFlow<StoreCategory> = _selectedCategory.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedStore = MutableStateFlow<Store?>(null)
    val selectedStore: StateFlow<Store?> = _selectedStore.asStateFlow()

    // Merchant KYC & Documents (Glovo & Temu Standard)
    private val _merchantApplications = MutableStateFlow<List<com.example.data.model.MerchantApplication>>(emptyList())

    val merchantApplications: StateFlow<List<com.example.data.model.MerchantApplication>> = _merchantApplications.asStateFlow()

    private val _showMerchantRegistration = MutableStateFlow(false)
    val showMerchantRegistration: StateFlow<Boolean> = _showMerchantRegistration.asStateFlow()

    fun openMerchantRegistration() {
        _showMerchantRegistration.value = true
    }

    fun closeMerchantRegistration() {
        _showMerchantRegistration.value = false
    }

    fun submitMerchantApplication(
        storeName: String,
        merchantType: String,
        iceNumber: String,
        phone: String,
        address: String,
        cinUri: String?,
        licenseUri: String?,
        hygieneUri: String?,
        ribUri: String?,
        storefrontUri: String?
    ) {
        // KYC files and business documents must not be stored or approved only in local UI state.
        _userAlertMessage.value = "تسجيل المتاجر متوقف مؤقتاً حتى ربط خادم آمن لحفظ الوثائق والتحقق منها. لم يتم إرسال أي طلب."
    }

    fun approveMerchantApplication(appId: String) {
        if (!requireRole(UserRole.SUPER_ADMIN)) return
        _merchantApplications.value = _merchantApplications.value.map {
            if (it.id == appId) it.copy(status = "APPROVED", notes = "تمت المصادقة واعتماد المتجر رسمياً ✓")
            else it
        }
        _userAlertMessage.value = "تمت المصادقة على المتجر ومنحه شارة التوثيق الذهبية 🛡️✨"
    }

    fun rejectMerchantApplication(appId: String, reason: String = "الوثائق غير مكتملة") {
        if (!requireRole(UserRole.SUPER_ADMIN)) return
        _merchantApplications.value = _merchantApplications.value.map {
            if (it.id == appId) it.copy(status = "REJECTED", notes = "مرفوض: $reason")
            else it
        }
        _userAlertMessage.value = "تم رفض الطلب: $reason"
    }

    // Pricing & Service Transparency ("كم الخدمة؟")
    private val _showPricingModal = MutableStateFlow(false)
    val showPricingModal: StateFlow<Boolean> = _showPricingModal.asStateFlow()

    fun openPricingModal() {
        _showPricingModal.value = true
    }

    fun closePricingModal() {
        _showPricingModal.value = false
    }

    // High-Tech Modern Delivery: Scheduled Delivery & Driver Tip
    private val _scheduledDeliveryTime = MutableStateFlow<String?>("الآن (توصيل فوري ⚡)")
    val scheduledDeliveryTime: StateFlow<String?> = _scheduledDeliveryTime.asStateFlow()

    fun setScheduledDeliveryTime(time: String?) {
        _scheduledDeliveryTime.value = time
    }

    private val _courierTip = MutableStateFlow(0.0)
    val courierTip: StateFlow<Double> = _courierTip.asStateFlow()

    fun setCourierTip(tip: Double) {
        _courierTip.value = tip
    }

    private val _groupOrderCode = MutableStateFlow<String?>(null)
    val groupOrderCode: StateFlow<String?> = _groupOrderCode.asStateFlow()

    fun startGroupOrder(): String {
        val code = "ALLO-GRP-${Random.nextInt(100, 999)}"
        _groupOrderCode.value = code
        _userAlertMessage.value = "تم تفعيل الطلب الجماعي: $code - شارك الرابط مع أصدقائك 👥"
        return code
    }

    // Cart State
    private val _cartItems = MutableStateFlow<List<CartItem>>(emptyList())
    val cartItems: StateFlow<List<CartItem>> = _cartItems.asStateFlow()

    private val _appliedPromoCode = MutableStateFlow<String?>(null)
    val appliedPromoCode: StateFlow<String?> = _appliedPromoCode.asStateFlow()

    private val _promoDiscountPercent = MutableStateFlow(0.0)
    val promoDiscountPercent: StateFlow<Double> = _promoDiscountPercent.asStateFlow()

    private val _deliveryAddress = MutableStateFlow("Bd Hassan II, Parc des Villes Jumelées, Mohammedia 🇲🇦")
    val deliveryAddress: StateFlow<String> = _deliveryAddress.asStateFlow()

    private val _customerLat = MutableStateFlow(LocationServiceManager.DEFAULT_LAT)
    val customerLat: StateFlow<Double> = _customerLat.asStateFlow()

    private val _customerLng = MutableStateFlow(LocationServiceManager.DEFAULT_LNG)
    val customerLng: StateFlow<Double> = _customerLng.asStateFlow()

    private val _showLocationPickerModal = MutableStateFlow(false)
    val showLocationPickerModal: StateFlow<Boolean> = _showLocationPickerModal.asStateFlow()

    fun openLocationPicker() {
        _showLocationPickerModal.value = true
    }

    fun closeLocationPicker() {
        _showLocationPickerModal.value = false
    }

    fun setAccurateCustomerLocation(lat: Double, lng: Double, address: String) {
        _customerLat.value = lat
        _customerLng.value = lng
        if (address.isNotBlank()) {
            _deliveryAddress.value = address
        }
        _showLocationPickerModal.value = false
        _userAlertMessage.value = "تم اعتماد موقع التوصيل الجغرافي وتثبيته بدقة: $address 🎯"
    }

    // AI Voice Shopping Assistant
    private val _showAiAssistant = MutableStateFlow(false)
    val showAiAssistant: StateFlow<Boolean> = _showAiAssistant.asStateFlow()

    private val _aiRecommendation = MutableStateFlow<com.example.data.services.AiShoppingRecommendation?>(null)
    val aiRecommendation: StateFlow<com.example.data.services.AiShoppingRecommendation?> = _aiRecommendation.asStateFlow()

    private val _isAiListening = MutableStateFlow(false)
    val isAiListening: StateFlow<Boolean> = _isAiListening.asStateFlow()

    private val _isAiProcessing = MutableStateFlow(false)
    val isAiProcessing: StateFlow<Boolean> = _isAiProcessing.asStateFlow()

    private val _aiSpokenQuery = MutableStateFlow("")
    val aiSpokenQuery: StateFlow<String> = _aiSpokenQuery.asStateFlow()

    fun openAiAssistant(initialQuery: String? = null) {
        _showAiAssistant.value = true
        if (!initialQuery.isNullOrBlank()) {
            submitAiVoiceQuery(initialQuery)
        }
    }

    fun closeAiAssistant() {
        _showAiAssistant.value = false
        _isAiListening.value = false
        _isAiProcessing.value = false
    }

    // Interactive AI Voice Call Dialog for Technical Support (Sara IA)
    private val _showAiVoiceSupportDialog = MutableStateFlow(false)
    val showAiVoiceSupportDialog: StateFlow<Boolean> = _showAiVoiceSupportDialog.asStateFlow()

    fun openAiVoiceSupportDialog() {
        _showAiVoiceSupportDialog.value = true
    }

    fun closeAiVoiceSupportDialog() {
        _showAiVoiceSupportDialog.value = false
    }

    fun setAiListening(listening: Boolean) {
        _isAiListening.value = listening
    }

    fun submitAiVoiceQuery(query: String) {
        val clean = query.trim()
        if (clean.isBlank()) return
        _aiSpokenQuery.value = clean
        _isAiListening.value = false
        _isAiProcessing.value = true

        viewModelScope.launch {
            try {
                val recommendation = com.example.data.services.AiShoppingService.analyzeShoppingIntent(clean)
                _aiRecommendation.value = recommendation
            } catch (t: Throwable) {
                _aiRecommendation.value = com.example.data.services.AiShoppingService.matchLocalCatalogWithNlp(clean, CatalogData.products)
            } finally {
                _isAiProcessing.value = false
            }
        }
    }

    fun updateAiItemQuantity(productId: String, delta: Int) {
        val currentRec = _aiRecommendation.value ?: return
        val updatedItems = currentRec.items.mapNotNull { item ->
            if (item.product.id == productId) {
                val newQty = item.quantity + delta
                if (newQty <= 0) null else item.copy(quantity = newQty, subtotal = item.product.price * newQty)
            } else {
                item
            }
        }
        val newSubtotal = updatedItems.sumOf { it.subtotal }
        val newDelivery = if (updatedItems.isEmpty()) 0.0 else currentRec.deliveryFee
        _aiRecommendation.value = currentRec.copy(
            items = updatedItems,
            estimatedTotal = newSubtotal,
            grandTotal = newSubtotal + newDelivery
        )
    }

    fun removeAiItem(productId: String) {
        updateAiItemQuantity(productId, -999)
    }

    fun confirmAiShoppingRecommendation(recommendation: com.example.data.services.AiShoppingRecommendation) {
        if (recommendation.items.isEmpty()) return
        val current = _cartItems.value.toMutableList()
        recommendation.items.forEach { aiItem ->
            val existingIndex = current.indexOfFirst { it.product.id == aiItem.product.id }
            if (existingIndex >= 0) {
                val existing = current[existingIndex]
                current[existingIndex] = existing.copy(
                    quantity = existing.quantity + aiItem.quantity,
                    notes = if (aiItem.customNote.isNotBlank()) aiItem.customNote else existing.notes
                )
            } else {
                current.add(
                    CartItem(
                        product = aiItem.product,
                        quantity = aiItem.quantity,
                        notes = aiItem.customNote
                    )
                )
            }
        }
        _cartItems.value = current
        _showAiAssistant.value = false
        _userAlertMessage.value = when (_selectedLanguage.value) {
            AppLanguage.ARABIC -> "تم تأكيد مشترياتك بالذكاء الاصطناعي وإضافتها للسلة بنجاح! 🛒✨"
            AppLanguage.FRENCH -> "Votre panier intelligent est confirmé et prêt pour la commande ! 🛒"
            AppLanguage.ENGLISH -> "Smart basket confirmed and ready for checkout! 🛒"
        }
        navigateTo(AppScreen.CART_CHECKOUT)
    }

    private val _selectedCity = MutableStateFlow("المحمدية (Mohammedia)")
    val selectedCity: StateFlow<String> = _selectedCity.asStateFlow()

    private val _favoriteStoreIds = MutableStateFlow<Set<String>>(setOf("store_mcdonalds", "store_paul", "store_tajine"))
    val favoriteStoreIds: StateFlow<Set<String>> = _favoriteStoreIds.asStateFlow()

    private val _favoriteProductIds = MutableStateFlow<Set<String>>(setOf("mcd_bigmac", "paul_croissant", "taj_lamb"))
    val favoriteProductIds: StateFlow<Set<String>> = _favoriteProductIds.asStateFlow()

    // Platform Commission Engine (Configurable by Admin)
    private val _merchantCommissionPercent = MutableStateFlow(12.0)
    val merchantCommissionPercent: StateFlow<Double> = _merchantCommissionPercent.asStateFlow()

    private val _driverPayoutPercent = MutableStateFlow(80.0)
    val driverPayoutPercent: StateFlow<Double> = _driverPayoutPercent.asStateFlow()

    private val _platformServiceFee = MutableStateFlow(2.5)
    val platformServiceFee: StateFlow<Double> = _platformServiceFee.asStateFlow()

    // Delivery Zones in Mohammedia & surrounding areas
    private val _deliveryZones = MutableStateFlow(
        listOf(
            DeliveryZone("zone_centre", "المحمدية المركز والحدائق", "Centre-Ville & Parc", 7.0, 25.0, 15, true),
            DeliveryZone("zone_miramar", "كورنيش ميرامار والشاطئ", "Corniche Miramar & Plage", 8.0, 30.0, 18, true),
            DeliveryZone("zone_alia", "حي العالية والراشيدية", "El Alia & Rachidia", 8.0, 25.0, 20, true),
            DeliveryZone("zone_kasbah", "القصبة والميناء التاريخي", "Kasbah & Port", 9.0, 30.0, 20, true),
            DeliveryZone("zone_wafa", "حي الوفاء والنصر", "El Wafa & Ennasr", 8.0, 25.0, 22, true),
            DeliveryZone("zone_monica", "حي مونيكا والياسمين", "Monica & Jasmin", 10.0, 35.0, 25, true)
        )
    )
    val deliveryZones: StateFlow<List<DeliveryZone>> = _deliveryZones.asStateFlow()

    private val _deliveryNotes = MutableStateFlow("")
    val deliveryNotes: StateFlow<String> = _deliveryNotes.asStateFlow()

    private val _selectedPaymentMethod = MutableStateFlow(PaymentMethodType.CASH_ON_DELIVERY)
    val selectedPaymentMethod: StateFlow<PaymentMethodType> = _selectedPaymentMethod.asStateFlow()

    // Active order being tracked
    private val _trackingOrderId = MutableStateFlow<String?>(null)
    val trackingOrderId: StateFlow<String?> = _trackingOrderId.asStateFlow()

    // Support Chat
    private val _supportMessages = MutableStateFlow<List<SupportChatMessage>>(emptyList())
    val supportMessages: StateFlow<List<SupportChatMessage>> = _supportMessages.asStateFlow()

    // Snackbars / Toast Alerts
    private val _userAlertMessage = MutableStateFlow<String?>(null)
    val userAlertMessage: StateFlow<String?> = _userAlertMessage.asStateFlow()

    // Room Database Flows
    val allUsers: StateFlow<List<UserAccountEntity>>
    val allStoresFromDb: StateFlow<List<StoreEntity>>
    val allProductsFromDb: StateFlow<List<ProductEntity>>
    val allOrders: StateFlow<List<OrderEntity>>
    val activeOrder: StateFlow<OrderEntity?>
    val allNotifications: StateFlow<List<NotificationEntity>>
    val unreadNotificationsCount: StateFlow<Int>
    val allReviews: StateFlow<List<ReviewEntity>>
    val wallet: StateFlow<WalletProfileEntity?>
    val allTickets: StateFlow<List<SupportTicketEntity>>
    val pendingUsers: StateFlow<List<UserAccountEntity>>
    val pendingPartnerApplications: StateFlow<List<UserAccountEntity>>
    val allAddresses: StateFlow<List<AddressEntity>>
    val userProfile: StateFlow<UserProfile?>

    // Google Play Services Real-Time Location Tracking
    private val _deviceLocation = MutableStateFlow<DeviceLocation?>(null)
    val deviceLocation: StateFlow<DeviceLocation?> = _deviceLocation.asStateFlow()

    private val _isLiveLocationTrackingEnabled = MutableStateFlow(false)
    val isLiveLocationTrackingEnabled: StateFlow<Boolean> = _isLiveLocationTrackingEnabled.asStateFlow()

    // Real-Time Network Connectivity Observer
    private val connectivityObserver = NetworkConnectivityObserver(application)
    val isNetworkConnected: StateFlow<Boolean> = connectivityObserver.observe().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        connectivityObserver.isCurrentlyConnected()
    )

    private var trackingJob: Job? = null
    private var locationTrackingJob: Job? = null

    init {
        AndroidNotificationHelper.initChannels(application)
        val database = AppDatabase.getInstance(application)
        repository = AlloAzizRepository(database)

        allUsers = repository.allUsers.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

        pendingUsers = repository.pendingUsers.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

        pendingPartnerApplications = repository.pendingPartnerApplications.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

        allStoresFromDb = repository.allStores.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

        allProductsFromDb = repository.allProducts.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

        allOrders = kotlinx.coroutines.flow.combine(_currentUserId, _currentRole) { userId, role -> userId to role }
            .flatMapLatest { (userId, role) ->
                if (userId.isBlank()) kotlinx.coroutines.flow.flowOf(emptyList())
                else when (role) {
                    UserRole.SUPER_ADMIN -> repository.allOrders
                    UserRole.DRIVER -> repository.getOrdersForDriver(userId)
                    UserRole.RESTAURANT_OWNER -> repository.getOrdersForStoreOwner(userId)
                    else -> repository.getOrdersForCustomer(userId)
                }
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        activeOrder = _currentUserId
            .flatMapLatest { userId ->
                if (userId.isBlank()) kotlinx.coroutines.flow.flowOf(null)
                else repository.getActiveOrderForCustomer(userId)
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

        allNotifications = _currentUserId
            .flatMapLatest { userId ->
                if (userId.isBlank()) kotlinx.coroutines.flow.flowOf(emptyList())
                else repository.getNotificationsForUser(userId)
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        unreadNotificationsCount = _currentUserId
            .flatMapLatest { userId ->
                if (userId.isBlank()) kotlinx.coroutines.flow.flowOf(0)
                else repository.getUnreadCountForUser(userId)
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

        allReviews = repository.allReviews.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

        wallet = repository.wallet.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            WalletProfileEntity()
        )

        allTickets = repository.allTickets.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

        allAddresses = _currentUserId
            .flatMapLatest { userId ->
                if (userId.isBlank()) kotlinx.coroutines.flow.flowOf(emptyList())
                else repository.addressesForUser(userId)
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        userProfile = _currentUserId
            .flatMapLatest { userId ->
                if (userId.isBlank()) kotlinx.coroutines.flow.flowOf(null)
                else repository.getUserProfile(userId)
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

        // Seed initial room data asynchronously off main thread for zero-lag launch
        viewModelScope.launch(Dispatchers.IO) {
            repository.initializeDefaultDataIfEmpty()
            initSupportChat()
        }
    }

    private fun initSupportChat() {
        val greeting = when (_selectedLanguage.value) {
            AppLanguage.ARABIC -> "أهلاً بك في مساعد ديليفيو DELIVIO. كيف يمكنني مساعدتك اليوم؟"
            AppLanguage.FRENCH -> "Bienvenue dans l’assistance DELIVIO. Je suis Sara, comment puis-je vous aider ?"
            AppLanguage.ENGLISH -> "Welcome to DELIVIO assistance. I'm Sara, how can I help you today?"
        }
        _supportMessages.value = listOf(
            SupportChatMessage(
                id = "msg_init",
                text = greeting,
                isUser = false,
                agentName = "سارة - دعم ديليفيو DELIVIO"
            )
        )
    }

    // Authentication & Account Verification (Strict Multi-Role Policy)
    private fun requireRole(role: UserRole): Boolean {
        val user = _currentUser.value
        val allowed = user != null && user.role == role.name && _currentRole.value == role
        if (!allowed) _userAlertMessage.value = "هذه العملية تتطلب تسجيل الدخول بحساب مخوّل."
        return allowed
    }

    fun clearAuthMessages() {
        _authError.value = null
        _authPendingApprovalMessage.value = null
        _authSuccessMessage.value = null
    }

    fun loginWithCredentials(identifier: String, pass: String) {
        viewModelScope.launch {
            clearAuthMessages()
            val trimmedId = identifier.trim()
            val trimmedPass = pass.trim()

            if (trimmedId.isBlank() || trimmedPass.isBlank()) {
                _authError.value = "يرجى كتابة البريد الإلكتروني أو رقم الهاتف وكلمة المرور."
                return@launch
            }

            // Query user from Room database
            var user = repository.getUserByIdentifier(trimmedId)

            if (user == null) {
                _authError.value = "هذا الحساب غير مسجل مسبقاً! يجب عليك إنشاء حساب جديد أولاً."
                return@launch
            }

            // Never accept empty/plaintext passwords or hard-coded administrator credentials.
            if (!com.example.data.auth.PasswordHasher.verifyPassword(trimmedPass, user.password)) {
                _authError.value = "كلمة المرور غير صحيحة! يرجى التأكد وإعادة المحاولة."
                return@launch
            }

            if (!com.example.data.auth.PasswordHasher.isCurrentHash(user.password)) {
                repository.insertUser(user.copy(password = com.example.data.auth.PasswordHasher.hashPassword(trimmedPass)))
            }

            // Status checks
            when (user.status) {
                "PENDING_APPROVAL" -> {
                    val roleMsg = if (user.role == "RESTAURANT_OWNER") {
                        "طلب الانضمام كشريك مطعم (${user.name}) قيد المراجعة والاعتماد من طرف إدارة DELIVIO (ديليفيو). ستتمكن من تشغيل متجرك فور اعتماد الحساب."
                    } else if (user.role == "DRIVER") {
                        "طلب الانضمام كسائق مندوب (${user.name}) قيد المراجعة والاعتماد من طرف إدارة DELIVIO (ديليفيو). ستتمكن من استقبال الطلبات فور اعتماد الحساب."
                    } else if (user.role == "SUPPORT_AGENT") {
                        "طلب الانضمام لفريق الدعم الفني (${user.name}) قيد المراجعة والاعتماد من طرف مدير المنصة لتعيين الصلاحيات."
                    } else {
                        "حسابك (${user.name}) قيد المراجعة والاعتماد من قبل إدارة DELIVIO (ديليفيو)."
                    }
                    _authPendingApprovalMessage.value = roleMsg
                    return@launch
                }
                "SUSPENDED" -> {
                    _authError.value = "تم إيقاف هذا الحساب من قبل الإدارة. يرجى التواصل مع فريق الدعم الفني."
                    return@launch
                }
                "REJECTED" -> {
                    _authError.value = "عذراً، تم رفض طلب التسجيل من طرف إدارة DELIVIO لعدم استيفاء الشروط. للتواصل مع الدعم: support@delivio.ma"
                    return@launch
                }
                "DEACTIVATED" -> {
                    _authError.value = "هذا الحساب معطل حالياً."
                    return@launch
                }
                "ACTIVE" -> {
                    // Status is active, proceed to password verification
                }
                else -> {
                    // Allowed
                }
            }

            // Successful login
            _currentUser.value = user
            _currentUserId.value = user.id
            val matchedRole = UserRole.values().firstOrNull { it.name == user.role } ?: UserRole.CUSTOMER
            _currentRole.value = matchedRole

            when (matchedRole) {
                UserRole.CUSTOMER -> _currentScreen.value = AppScreen.HOME
                UserRole.RESTAURANT_OWNER -> _currentScreen.value = AppScreen.RESTAURANT_DASHBOARD
                UserRole.DRIVER -> _currentScreen.value = AppScreen.DRIVER_DASHBOARD
                UserRole.SUPPORT_AGENT -> _currentScreen.value = AppScreen.SUPPORT_DASHBOARD
                UserRole.SUPER_ADMIN -> _currentScreen.value = AppScreen.ADMIN_DASHBOARD
            }
            _userAlertMessage.value = "مرحباً بك مجدداً يا ${user.name}! 🌟"
        }
    }

    fun loginWithGoogle(gmail: String, name: String) {
        // Do not trust an email/name supplied by the UI as proof of Google identity.
        // Integrate Firebase Authentication and verify the ID token before enabling this path.
        _authError.value = "تسجيل الدخول عبر Google غير مفعّل حتى ربط Firebase Authentication والتحقق من الهوية."
    }

    fun loginWithPhoneOnly(phoneNumber: String, customerName: String = "") {
        // A phone number is not proof of ownership. Require a real, server-verified SMS OTP.
        _authError.value = "تسجيل الدخول بالهاتف غير مفعّل حتى إضافة رمز SMS موثّق من الخادم."
    }

    fun registerUser(
        name: String,
        email: String,
        phone: String,
        pass: String,
        role: UserRole,
        restaurantName: String = "",
        vehicleType: String = "",
        plateNumber: String = "",
        merchantType: String = "FOOD",
        iceNumber: String = "",
        businessAddress: String = "",
        cinNumber: String = "",
        driverLicense: String = "",
        ribNumber: String = ""
    ) {
        viewModelScope.launch {
            clearAuthMessages()
            val trimmedName = name.trim()
            val trimmedEmail = email.trim()
            val trimmedPhone = phone.trim()
            val trimmedPass = pass.trim()
            if (trimmedPass.length < 8) {
                _authError.value = "يجب أن تتكون كلمة المرور من 8 أحرف على الأقل."
                return@launch
            }

            // Security: Only Super Admin accounts cannot be created via public registration
            if (role == UserRole.SUPER_ADMIN) {
                _authError.value = "لا يمكن إنشاء حساب مدير عام من واجهة التسجيل العامة."
                return@launch
            }

            if (role != UserRole.CUSTOMER) {
                _authError.value = "تسجيل الشركاء والسائقين متوقف حتى تجهيز خادم آمن لحفظ الوثائق ومراجعة الطلبات."
                return@launch
            }

            if (trimmedName.isBlank() || trimmedEmail.isBlank()) {
                _authError.value = "يرجى إدخال الاسم والبريد الإلكتروني. التسجيل برقم الهاتف يحتاج تحقق SMS غير مفعّل حالياً."
                return@launch
            }

            if (trimmedEmail.isNotBlank()) {
                val existing = repository.getUserByEmail(trimmedEmail)
                if (existing != null) {
                    _authError.value = "هذا البريد الإلكتروني مسجل مسبقاً! يرجى تسجيل الدخول بدلاً من ذلك."
                    return@launch
                }
            }

            if (trimmedPhone.isNotBlank()) {
                val existingPhone = repository.getUserByPhone(trimmedPhone)
                if (existingPhone != null) {
                    _authError.value = "رقم الهاتف هذا مسجل مسبقاً! يرجى تسجيل الدخول بدلاً من ذلك."
                    return@launch
                }
            }

            // Role-specific status logic:
            // CUSTOMER -> ACTIVE immediately (no approval required!)
            // RESTAURANT_OWNER, DRIVER, SUPPORT_AGENT -> PENDING_APPROVAL
            val initialStatus = if (role == UserRole.CUSTOMER) "ACTIVE" else "PENDING_APPROVAL"

            val newId = "usr_${System.currentTimeMillis()}"
            val newAccount = UserAccountEntity(
                id = newId,
                name = trimmedName,
                email = trimmedEmail.ifBlank { "$trimmedPhone@delivio.ma" },
                phone = trimmedPhone,
                password = com.example.data.auth.PasswordHasher.hashPassword(trimmedPass),
                role = role.name,
                status = initialStatus,
                restaurantName = if (role == UserRole.RESTAURANT_OWNER) restaurantName.ifBlank { "متجر $trimmedName" } else null,
                vehicleType = if (role == UserRole.DRIVER && vehicleType.isNotBlank()) vehicleType else null,
                plateNumber = if (role == UserRole.DRIVER && plateNumber.isNotBlank()) plateNumber else null,
                balance = 0.0,
                avatarEmoji = role.emoji,
                authProvider = if (trimmedEmail.isNotBlank()) "EMAIL" else "PHONE",
                cinNumber = cinNumber.ifBlank { null },
                driverLicense = driverLicense.ifBlank { null },
                iceNumber = iceNumber.ifBlank { null },
                businessAddress = businessAddress.ifBlank { null },
                ribNumber = ribNumber.ifBlank { null }
            )

            repository.insertUser(newAccount)

            if (role == UserRole.CUSTOMER) {
                // Immediately active! Immediately log in and enter Customer Home
                _currentUser.value = newAccount
                _currentUserId.value = newAccount.id
                _currentRole.value = UserRole.CUSTOMER
                _currentScreen.value = AppScreen.HOME
                _userAlertMessage.value = "مرحباً بك يا $trimmedName في ديليفيو! تم إنشاء حساب محلي على هذا الجهاز. تأكيد الطلبات غير متاح حتى ربط الخادم."

                repository.addNotification(
                    NotificationEntity(
                        id = "notif_welcome_${System.currentTimeMillis()}",
                        userId = newAccount.id,
                        titleAr = "مرحباً بك في DELIVIO! 🇲🇦",
                        titleFr = "Bienvenue sur DELIVIO !",
                        titleEn = "Welcome to DELIVIO!",
                        bodyAr = "تم إنشاء حساب محلي على هذا الجهاز. تأكيد الطلبات يحتاج ربط الخادم.",
                        bodyFr = "Votre compte a été activé avec succès.",
                        bodyEn = "Your account is now active.",
                        timestamp = System.currentTimeMillis(),
                        type = "WELCOME"
                    )
                )
            } else if (role == UserRole.RESTAURANT_OWNER) {
                // Partner Merchant (Restaurant or E-commerce store): Requires Admin / Support Approval
                val mApp = com.example.data.model.MerchantApplication(
                    id = "app_reg_${System.currentTimeMillis()}",
                    storeName = restaurantName.ifBlank { "متجر $trimmedName" },
                    merchantType = merchantType,
                    ownerName = trimmedName,
                    iceNumber = iceNumber.ifBlank { "002847193000045" },
                    phone = trimmedPhone,
                    address = businessAddress.ifBlank { "المحمدية، المغرب" },
                    cinUri = if (cinNumber.isNotBlank()) "CIN: $cinNumber" else "بطاقة وطنية موثقة",
                    licenseUri = "سجل تجاري / رخصة معتمدة",
                    hygieneUri = "شهادة النظافة والسلامة الصحية",
                    ribUri = if (ribNumber.isNotBlank()) "RIB: $ribNumber" else "حساب بنكي معتمد",
                    status = "PENDING_VERIFICATION",
                    notes = "تسجيل شريك جديد عبر استمارة الانضمام الموحدة"
                )
                _merchantApplications.value = listOf(mApp) + _merchantApplications.value

                _authPendingApprovalMessage.value = "تم استلام ملف الشريك التجاري (${restaurantName.ifBlank { trimmedName }}) بنجاح! 📄 حسابك قيد التدقيق الإداري من طرف إدارة DELIVIO أو الدعم الفني، وسيتم تفعيله فور اعتماد الوثائق."

                repository.addNotification(
                    NotificationEntity(
                        id = "notif_reg_${System.currentTimeMillis()}",
                        userId = newAccount.id,
                        titleAr = "طلب انضمام شريك متجر/مطعم جديد بانتظار الاعتماد 🔔",
                        titleFr = "Nouvelle demande de partenaire marchand",
                        titleEn = "New Merchant Partner Application",
                        bodyAr = "سجل المتجر (${restaurantName.ifBlank { trimmedName }}) وهو بانتظار مراجعة واعتماد الإدارة.",
                        bodyFr = "${trimmedName} a postulé pour un commerce (${restaurantName}).",
                        bodyEn = "${trimmedName} registered store (${restaurantName}) and awaits activation.",

                    timestamp = System.currentTimeMillis(),
                        type = "SECURITY"
                    )
                )
            } else if (role == UserRole.SUPPORT_AGENT) {
                // Technical Support Agent: Requires Admin Approval
                _authPendingApprovalMessage.value = "تم استلام ملف الانضمام لفريق الدعم الفني (${trimmedName}) بنجاح! 🎧 حسابك قيد المراجعة والاعتماد من طرف مدير المنصة لتعيين الصلاحيات."

                repository.addNotification(
                    NotificationEntity(
                        id = "notif_reg_supp_${System.currentTimeMillis()}",
                        userId = newAccount.id,
                        titleAr = "طلب انضمام عضو دعم فني جديد بانتظار الاعتماد 🔔",
                        titleFr = "Nouvelle demande agent de support",
                        titleEn = "New Technical Support Application",
                        bodyAr = "سجل العضو ${trimmedName} (${trimmedPhone}) في قسم الدعم وبانتظار اعتماد الإدارة.",
                        bodyFr = "Candidature support de ${trimmedName}.",
                        bodyEn = "Support application for ${trimmedName} awaiting admin approval.",

                    timestamp = System.currentTimeMillis(),
                        type = "SECURITY"
                    )
                )
            } else {
                // Driver Partner: Requires Admin / Support Approval
                _authPendingApprovalMessage.value = "تم استلام ملف السائق (${trimmedName}) بنجاح! 🛵 حسابك قيد التدقيق الإداري من قبل إدارة DELIVIO، وسيتم تفعيله فور اعتماد البيانات."

                repository.addNotification(
                    NotificationEntity(
                        id = "notif_reg_drv_${System.currentTimeMillis()}",
                        userId = newAccount.id,
                        titleAr = "طلب انضمام سائق توصيل جديد بانتظار الاعتماد 🔔",
                        titleFr = "Nouvelle candidature livreur",
                        titleEn = "New Driver Application",
                        bodyAr = "سجل الكابتن ${trimmedName} (${vehicleType.ifBlank { "دراجة نارية" }}) وهو بانتظار مراجعة واعتماد الإدارة.",
                        bodyFr = "Candidature chauffeur de ${trimmedName}.",
                        bodyEn = "Driver registration for ${trimmedName} awaiting admin approval.",

                    timestamp = System.currentTimeMillis(),
                        type = "SECURITY"
                    )
                )
            }
        }
    }

    fun approvePartnerApplication(userId: String) {
        viewModelScope.launch {
            if (!requireRole(UserRole.SUPER_ADMIN)) return@launch
            repository.updateUserStatus(userId, "ACTIVE")
            repository.addNotification(
                NotificationEntity(
                    id = "notif_appr_${System.currentTimeMillis()}",
                    userId = userId,
                    titleAr = "تم قبول طلب انضمامك إلى DELIVIO بنجاح 🎉",
                    titleFr = "Demande acceptée avec succès 🎉",
                    titleEn = "Partner Application Approved 🎉",
                    bodyAr = "تم قبول طلب انضمامك إلى DELIVIO بنجاح. يمكنك الآن تسجيل الدخول وبدء العمل واستقبال الطلبات.",
                    bodyFr = "Votre demande a été approuvée par l'administration DELIVIO.",
                    bodyEn = "Your application was approved. You can now login and start working.",

                    timestamp = System.currentTimeMillis(),
                    type = "APPROVAL"
                )
            )
            _userAlertMessage.value = "تم قبول واعتماد الشريك بنجاح! أصبح الحساب نشطاً الآن 🟢"
        }
    }

    fun rejectPartnerApplication(userId: String) {
        viewModelScope.launch {
            if (!requireRole(UserRole.SUPER_ADMIN)) return@launch
            repository.updateUserStatus(userId, "REJECTED")
            repository.addNotification(
                NotificationEntity(
                    id = "notif_rej_${System.currentTimeMillis()}",
                    userId = userId,
                    titleAr = "تحديث بخصوص طلب الانضمام إلى DELIVIO",
                    titleFr = "Mise à jour concernant votre demande",
                    titleEn = "Application Status Update",
                    bodyAr = "نأسف لإبلاغك بأنه تم رفض طلب الانضمام لعدم استيفاء الشروط والمعايير المطلوبة. يمكنك التواصل مع الدعم الفني لمزيد من المعلومات.",
                    bodyFr = "Votre demande a été refusée pour non-conformité aux critères requis.",
                    bodyEn = "Your application was rejected as it does not meet requirements.",

                    timestamp = System.currentTimeMillis(),
                    type = "REJECTION"
                )
            )
            _userAlertMessage.value = "تم رفض طلب الانضمام وتحديث الحالة إلى REJECTED ✕"
        }
    }

    fun approveUserAccount(userId: String) {
        approvePartnerApplication(userId)
    }

    fun rejectUserAccount(userId: String) {
        rejectPartnerApplication(userId)
    }

    fun logout() {
        _currentUser.value = null
        _currentUserId.value = ""
        _currentRole.value = UserRole.CUSTOMER
        _driverIsOnline.value = false
        _currentScreen.value = AppScreen.AUTH
        _userAlertMessage.value = "تم تسجيل الخروج بنجاح."
    }

    // Favorites & City Selection
    fun toggleFavoriteStore(storeId: String) {
        val current = _favoriteStoreIds.value.toMutableSet()
        if (current.contains(storeId)) {
            current.remove(storeId)
            _userAlertMessage.value = "تمت الإزالة من المتاجر المفضلة"
        } else {
            current.add(storeId)
            _userAlertMessage.value = "تمت الإضافة إلى المتاجر المفضلة ❤️"
        }
        _favoriteStoreIds.value = current
    }

    fun toggleFavoriteProduct(productId: String) {
        val current = _favoriteProductIds.value.toMutableSet()
        if (current.contains(productId)) {
            current.remove(productId)
            _userAlertMessage.value = "تمت الإزالة من الوجبات المفضلة"
        } else {
            current.add(productId)
            _userAlertMessage.value = "تم حفظ الوجبة في المفضلة ❤️"
        }
        _favoriteProductIds.value = current
    }

    fun setDeliveryCity(city: String) {
        _selectedCity.value = city
        _deliveryAddress.value = "وسط مدينة $city، المغرب 🇲🇦"
        _userAlertMessage.value = "تم تغيير مدينة التوصيل إلى: $city"
    }

    fun addNewAddress(title: String, type: String, city: String, neighborhood: String, street: String, notes: String) {
        viewModelScope.launch {
            val userId = _currentUserId.value
            if (userId.isBlank()) {
                _userAlertMessage.value = "يرجى تسجيل الدخول قبل حفظ عنوان التوصيل."
                return@launch
            }
            val newAddr = AddressEntity(
                id = "addr_${System.currentTimeMillis()}",
                userId = userId,
                title = title,
                type = type,
                city = city,
                neighborhood = neighborhood,
                streetDetails = street,
                deliveryNotes = notes,
                isDefault = true
            )
            repository.insertAddress(newAddr)
            _deliveryAddress.value = "$title - $street، $neighborhood، $city"
            _userAlertMessage.value = "تم حفظ العنوان الجديد بنجاح 📍"
        }
    }

    fun deleteAddress(addressId: String) {
        viewModelScope.launch {
            val userId = _currentUserId.value
            if (userId.isBlank()) {
                _userAlertMessage.value = "يرجى تسجيل الدخول قبل حذف عنوان التوصيل."
                return@launch
            }
            repository.deleteAddress(addressId, userId)
            _userAlertMessage.value = "تم حذف العنوان"
        }
    }

    fun setDefaultAddress(addressId: String) {
        viewModelScope.launch {
            val userId = _currentUserId.value
            if (userId.isBlank()) {
                _userAlertMessage.value = "يرجى تسجيل الدخول قبل تغيير العنوان الافتراضي."
                return@launch
            }
            repository.setDefaultAddress(userId, addressId)
            _userAlertMessage.value = "تم تعيين العنوان كعنوان افتراضي"
        }
    }

    fun updateCommissionRates(merchantPercent: Double, driverPayout: Double, serviceFee: Double) {
        if (!requireRole(UserRole.SUPER_ADMIN)) return
        _merchantCommissionPercent.value = merchantPercent
        _driverPayoutPercent.value = driverPayout
        _platformServiceFee.value = serviceFee
        _userAlertMessage.value = "تم تحديث نظام العمولات والرسوم بنجاح ✓"
    }

    fun toggleDeliveryZone(zoneId: String) {
        if (!requireRole(UserRole.SUPER_ADMIN)) return
        _deliveryZones.value = _deliveryZones.value.map { zone ->
            if (zone.id == zoneId) zone.copy(isActive = !zone.isActive) else zone
        }
        _userAlertMessage.value = "تم تحديث حالة منطقة التوصيل بالمحمدية"
    }

    // Role Switching
    fun switchRole(newRole: UserRole) {
        val user = _currentUser.value
        if (user == null || user.role != newRole.name) {
            _userAlertMessage.value = "يرجى تسجيل الدخول بالحساب والصلاحية الصحيحة للوصول إلى هذه الواجهة."
            return
        }
        _currentRole.value = newRole
        when (newRole) {
            UserRole.CUSTOMER -> navigateTo(AppScreen.HOME)
            UserRole.RESTAURANT_OWNER -> navigateTo(AppScreen.RESTAURANT_DASHBOARD)
            UserRole.DRIVER -> navigateTo(AppScreen.DRIVER_DASHBOARD)
            UserRole.SUPPORT_AGENT -> navigateTo(AppScreen.SUPPORT_DASHBOARD)
            UserRole.SUPER_ADMIN -> navigateTo(AppScreen.ADMIN_DASHBOARD)
        }
    }

    // Driver Partner functions
    fun toggleDriverOnline() {
        if (!requireRole(UserRole.DRIVER)) return
        _userAlertMessage.value = "تغيير حالة السائق غير متاح حتى ربط النظام بالخادم."
    }

    fun acceptDeliveryTask(orderId: String) {
        if (!requireRole(UserRole.DRIVER)) return
        _userAlertMessage.value = "هذه العملية متوقفة حتى ربط خادم موثوق لتحديث حالة الطلبات وتتبع المندوبين."
    }

    fun markOrderPickedUpFromKitchen(orderId: String) {
        if (!requireRole(UserRole.DRIVER)) return
        _userAlertMessage.value = "هذه العملية متوقفة حتى ربط خادم موثوق لتحديث حالة الطلبات وتتبع المندوبين."
    }

    fun markOrderDeliveredToCustomer(orderId: String) {
        if (!requireRole(UserRole.DRIVER)) return
        _userAlertMessage.value = "هذه العملية متوقفة حتى ربط خادم موثوق لتحديث حالة الطلبات وتتبع المندوبين."
    }

    fun approveDriverAccount(driverId: String) {
        viewModelScope.launch {
            if (!requireRole(UserRole.SUPER_ADMIN)) return@launch
            repository.updateUserStatus(driverId, "ACTIVE")
            repository.addNotification(
                NotificationEntity(
                    id = "notif_drv_appr_${System.currentTimeMillis()}",
                    userId = driverId,
                    titleAr = "تم تفعيل حساب السائق بنجاح! 🎉",
                    titleFr = "Compte chauffeur activé ! 🎉",
                    titleEn = "Driver Account Activated! 🎉",
                    bodyAr = "تمت مراجعة الوثائق وتفعيل حساب السائق للبدء في استقبال طلبات التوصيل.",
                    bodyFr = "Vos documents ont été validés avec succès.",
                    bodyEn = "Your documents have been approved successfully.",

                    timestamp = System.currentTimeMillis()
                )
            )
            _userAlertMessage.value = "تمت الموافقة وتفعيل حساب السائق بنجاح! أصبح جاهزاً للعمل 🛵"
        }
    }

    fun rejectDriverAccount(driverId: String) {
        viewModelScope.launch {
            if (!requireRole(UserRole.SUPER_ADMIN)) return@launch
            repository.updateUserStatus(driverId, "SUSPENDED")
            _userAlertMessage.value = "تم رفض طلب التسجيل مؤقتاً لعدم استيفاء الوثائق."
        }
    }

    fun resolveTicket(ticketId: String) {
        viewModelScope.launch {
            if (!requireRole(UserRole.SUPPORT_AGENT)) return@launch
            repository.updateTicketStatus(ticketId, "RESOLVED")
            _userAlertMessage.value = "تم حل التذكرة #$ticketId بنجاح!"
        }
    }

    fun grantCustomerCompensation(amount: Double) {
        _userAlertMessage.value = "لا يمكن إصدار تعويض مالي محلياً. يلزم تنفيذ معتمد على الخادم مع سجل تدقيق."
    }

    // Restaurant Owner functions
    fun addProductToMenu(
        storeId: String,
        storeName: String,
        nameAr: String,
        nameFr: String,
        price: Double,
        category: String,
        desc: String,
        emoji: String,
        imageUrl: String = ""
    ) {
        viewModelScope.launch {
            if (!requireRole(UserRole.RESTAURANT_OWNER)) return@launch
            val newProduct = ProductEntity(
                id = "prod_${System.currentTimeMillis()}",
                storeId = storeId,
                storeName = storeName,
                nameEn = nameFr,
                nameAr = nameAr,
                nameFr = nameFr,
                descriptionEn = desc,
                descriptionAr = desc,
                descriptionFr = desc,
                price = price,
                category = category,
                emoji = emoji.ifBlank { "🍽️" },
                rating = 5.0,
                isPopular = true,
                calories = "450 kcal",
                isAvailable = true,
                imageUrl = imageUrl
            )
            repository.insertProduct(newProduct)
            _userAlertMessage.value = "تمت إضافة الوجبة \"$nameAr\" مع الصورة إلى قائمة المطعم بنجاح! 🍽️"
        }
    }

    fun toggleProductAvailability(productId: String, currentAvailable: Boolean) {
        viewModelScope.launch {
            if (!requireRole(UserRole.RESTAURANT_OWNER)) return@launch
            repository.toggleProductAvailability(productId, !currentAvailable)
            _userAlertMessage.value = if (!currentAvailable) "أصبحت الوجبة متاحة للطلب الآن ✓" else "تم تحديد الوجبة كغير متوفرة حالياً ✕"
        }
    }

    fun deleteProduct(productId: String) {
        viewModelScope.launch {
            if (!requireRole(UserRole.RESTAURANT_OWNER)) return@launch
            repository.deleteProduct(productId)
            _userAlertMessage.value = "تم حذف الوجبة من القائمة بنجاح."
        }
    }

    fun toggleStoreOpen(storeId: String, currentOpen: Boolean) {
        viewModelScope.launch {
            if (!requireRole(UserRole.RESTAURANT_OWNER)) return@launch
            repository.toggleStoreOpen(storeId, !currentOpen)
            _userAlertMessage.value = if (!currentOpen) "المطعم مفتوح الآن وجاهز لاستقبال الطلبات 🟢" else "المطعم مغلق حالياً 🔴"
        }
    }

    fun updateOrderCookingStatus(orderId: String, restStatus: String, orderStatus: String) {
        if (!requireRole(UserRole.RESTAURANT_OWNER)) return
        _userAlertMessage.value = "هذه العملية متوقفة حتى ربط خادم موثوق لتحديث حالة الطلبات وتتبع المندوبين."
    }

    fun updateUserStatus(userId: String, newStatus: String) {
        viewModelScope.launch {
            if (!requireRole(UserRole.SUPER_ADMIN)) return@launch
            repository.updateUserStatus(userId, newStatus)
            _userAlertMessage.value = "تم تحديث حالة المستخدم إلى: $newStatus"
        }
    }

    fun updateUserRole(userId: String, newRole: String) {
        viewModelScope.launch {
            if (!requireRole(UserRole.SUPER_ADMIN)) return@launch
            repository.updateUserRole(userId, newRole)
            _userAlertMessage.value = "تم تغيير رتبة المستخدم إلى: $newRole"
        }
    }

    fun addNewStore(
        name: String,
        nameAr: String,
        category: String,
        address: String,
        deliveryFee: Double,
        emoji: String
    ) {
        viewModelScope.launch {
            if (!requireRole(UserRole.SUPER_ADMIN)) return@launch
            val newStore = StoreEntity(
                id = "store_${System.currentTimeMillis()}",
                name = name,
                nameAr = nameAr,
                nameFr = name,
                category = category,
                rating = 5.0,
                reviewCount = 10,
                deliveryTimeMins = 20,
                deliveryFee = deliveryFee,
                minOrder = 25.0,
                address = address,
                emoji = emoji.ifBlank { "🏪" },
                isFeatured = true,
                badge = "شريك جديد موثوق 🌟",
                isOpen = true,
                ownerId = "owner_${System.currentTimeMillis()}"
            )
            repository.insertStore(newStore)
            _userAlertMessage.value = "تم اعتماد وإضافة المتجر الجديد \"$nameAr\" إلى التطبيق بنجاح! 🏪"
        }
    }

    fun broadcastSystemAnnouncement(title: String, body: String) {
        _userAlertMessage.value = "إرسال الإعلانات متوقف حتى ربط خادم يوزع الإشعارات على الحسابات المستهدفة."
    }


    // Navigation with RBAC Authorization
    fun navigateTo(screen: AppScreen) {
        val user = _currentUser.value

        when (screen) {
            AppScreen.ADMIN_DASHBOARD -> {
                if (user == null || (user.role != "SUPER_ADMIN" && user.role != "ADMIN")) {
                    _userAlertMessage.value = "⚠️ غير مصرح: منطقة الإدارة العامة تتطلب حساب مسؤول معتمد."
                    return
                }
            }
            AppScreen.SUPPORT_DASHBOARD -> {
                if (user == null || (user.role != "SUPPORT_AGENT" && user.role != "SUPER_ADMIN" && user.role != "ADMIN")) {
                    _userAlertMessage.value = "⚠️ غير مصرح: منطقة الدعم الفني تتطلب صلاحيات فريق الدعم."
                    return
                }
            }
            AppScreen.DRIVER_DASHBOARD -> {
                if (user == null || (user.role != "DRIVER" && user.role != "SUPER_ADMIN")) {
                    _userAlertMessage.value = "⚠️ غير مصرح: تتطلب حساب كابتن توصيل معتمد."
                    return
                }
            }
            AppScreen.RESTAURANT_DASHBOARD -> {
                if (user == null || (user.role != "RESTAURANT_OWNER" && user.role != "SUPER_ADMIN")) {
                    _userAlertMessage.value = "⚠️ غير مصرح: تتطلب حساب شريك مطعم معتمد."
                    return
                }
            }
            else -> {}
        }

        if (_currentScreen.value != screen) {
            _screenHistory.add(_currentScreen.value)
            _currentScreen.value = screen
        }
    }

    fun loginStaff(identifier: String, pass: String, targetRole: UserRole) {
        viewModelScope.launch {
            clearAuthMessages()
            val trimmedId = identifier.trim()
            val trimmedPass = pass.trim()

            if (trimmedId.isBlank() || trimmedPass.isBlank()) {
                _authError.value = "يرجى إدخال البريد المهني وكلمة المرور."
                return@launch
            }

            // No hard-coded administrator bypass: all staff accounts must pass normal verification.
            val user = repository.getUserByIdentifier(trimmedId)
            if (user == null) {
                _authError.value = "حساب الموظف أو الشريك غير مسجل بالنظام."
                return@launch
            }

            if (!com.example.data.auth.PasswordHasher.verifyPassword(trimmedPass, user.password)) {
                _authError.value = "كلمة المرور غير صحيحة."
                return@launch
            }

            // Upgrade legacy SHA-256 hashes after successful verification.
            if (!com.example.data.auth.PasswordHasher.isCurrentHash(user.password)) {
                val upgradedPasswordHash = com.example.data.auth.PasswordHasher.hashPassword(trimmedPass)
                repository.insertUser(user.copy(password = upgradedPasswordHash))
            }

            val actualRole = UserRole.values().firstOrNull { it.name == user.role } ?: UserRole.CUSTOMER
            if (actualRole != targetRole && actualRole != UserRole.SUPER_ADMIN) {
                _authError.value = "هذا الحساب لا يملك صلاحية ${targetRole.titleAr}."
                return@launch
            }

            if (user.status == "PENDING_APPROVAL") {
                _authPendingApprovalMessage.value = "الحساب قيد المراجعة والاعتماد من قبل الإدارة."
                return@launch
            }

            if (user.status == "REJECTED") {
                _authError.value = "تم رفض طلب الانضمام لهذا الحساب مسبقاً من طرف الإدارة."
                return@launch
            }

            if (user.status == "SUSPENDED") {
                _authError.value = "تم إيقاف هذا الحساب من قبل الإدارة."
                return@launch
            }

            _currentUser.value = user
            _currentUserId.value = user.id
            _currentRole.value = actualRole

            when (actualRole) {
                UserRole.RESTAURANT_OWNER -> _currentScreen.value = AppScreen.RESTAURANT_DASHBOARD
                UserRole.DRIVER -> _currentScreen.value = AppScreen.DRIVER_DASHBOARD
                UserRole.SUPPORT_AGENT -> _currentScreen.value = AppScreen.SUPPORT_DASHBOARD
                UserRole.SUPER_ADMIN -> _currentScreen.value = AppScreen.ADMIN_DASHBOARD
                UserRole.CUSTOMER -> _currentScreen.value = AppScreen.HOME
            }
            _userAlertMessage.value = "مرحباً بك مجدداً يا ${user.name}!"
        }
    }

    fun requestPasswordReset(identifier: String, onOtpGenerated: (String) -> Unit) {
        // Do not issue a predictable OTP in the client. Wire this to a trusted email/SMS provider.
        _authError.value = "استعادة كلمة المرور غير مفعّلة حالياً. يرجى التواصل مع الدعم بعد التحقق من هويتك."
    }

    fun confirmPasswordReset(identifier: String, newPass: String) {
        // Never change credentials without a verified, expiring, single-use reset token.
        _authError.value = "لا يمكن تغيير كلمة المرور دون رمز تحقق صالح من الخادم."
    }

    fun navigateBack(): Boolean {
        return if (_screenHistory.isNotEmpty()) {
            val previous = _screenHistory.removeAt(_screenHistory.lastIndex)
            _currentScreen.value = previous
            true
        } else if (_currentScreen.value != AppScreen.HOME) {
            _currentScreen.value = AppScreen.HOME
            true
        } else {
            false
        }
    }

    // Language
    fun setLanguage(lang: AppLanguage) {
        _selectedLanguage.value = lang
        try {
            prefs.edit().putString("user_lang", lang.code).apply()
        } catch (e: Exception) {
            // ignore
        }
        _userAlertMessage.value = when (lang) {
            AppLanguage.FRENCH -> "Langue définie sur Français 🇫🇷"
            AppLanguage.ENGLISH -> "Language set to English 🇬🇧"
            AppLanguage.ARABIC -> "تم ضبط اللغة على العربية 🇲🇦"
        }
    }

    // Filter stores
    fun selectCategory(cat: StoreCategory) {
        _selectedCategory.value = cat
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun openStore(store: Store) {
        _selectedStore.value = store
        navigateTo(AppScreen.STORE_DETAIL)
    }

    // Cart Operations
    fun addToCart(product: Product, quantity: Int = 1, notes: String = "") {
        if (!product.isAvailable || !product.isApproved || !product.price.isFinite() || product.price <= 0.0 || product.stockCount <= 0 || quantity <= 0) {
            _userAlertMessage.value = "هذا المنتج غير متاح أو أن بيانات سعره أو مخزونه غير صالحة."
            return
        }
        val current = _cartItems.value.toMutableList()
        val existingIndex = current.indexOfFirst { it.product.id == product.id }
        if (existingIndex >= 0) {
            val existing = current[existingIndex]
            current[existingIndex] = existing.copy(quantity = existing.quantity + quantity)
        } else {
            current.add(CartItem(product = product, quantity = quantity, notes = notes))
        }
        _cartItems.value = current
        _userAlertMessage.value = when (_selectedLanguage.value) {
            AppLanguage.ARABIC -> "تمت إضافة \"${product.nameAr}\" إلى السلة بنجاح!"
            AppLanguage.FRENCH -> "\"${product.nameFr}\" ajouté au panier !"
            AppLanguage.ENGLISH -> "\"${product.nameEn}\" added to basket!"
        }
    }

    fun updateCartQuantity(productId: String, delta: Int) {
        val current = _cartItems.value.toMutableList()
        val index = current.indexOfFirst { it.product.id == productId }
        if (index >= 0) {
            val item = current[index]
            val newQty = item.quantity + delta
            if (newQty <= 0) {
                current.removeAt(index)
            } else {
                current[index] = item.copy(quantity = newQty)
            }
            _cartItems.value = current
        }
    }

    fun clearCart() {
        _cartItems.value = emptyList()
        _appliedPromoCode.value = null
        _promoDiscountPercent.value = 0.0
    }

    fun applyPromoCode(code: String): Boolean {
        _promoDiscountPercent.value = 0.0
        _appliedPromoCode.value = ""
        _userAlertMessage.value = when (_selectedLanguage.value) {
            AppLanguage.ARABIC -> "العروض غير مفعّلة حالياً. لن يتم احتساب خصم قبل التحقق منه."
            AppLanguage.FRENCH -> "Les promotions ne sont pas configurées. Aucun rabais n'a été appliqué."
            AppLanguage.ENGLISH -> "Promotions are not configured yet. No discount has been applied."
        }
        return false
    }

    fun setDeliveryAddress(address: String) {
        _deliveryAddress.value = address
    }

    fun setDeliveryNotes(notes: String) {
        _deliveryNotes.value = notes
    }

    fun setPaymentMethod(method: PaymentMethodType) {
        _selectedPaymentMethod.value = method
    }

    fun clearAlertMessage() {
        _userAlertMessage.value = null
    }

    // Checkout & Place Order
    fun placeOrder(onSuccess: (orderId: String) -> Unit) {
        // Order creation requires a trusted backend that validates identity, prices, stock,
        // payment and fulfillment. Never mark a local-only order as confirmed.
        _userAlertMessage.value = when (_selectedLanguage.value) {
            AppLanguage.ARABIC -> "تأكيد الطلبات متوقف مؤقتاً حتى ربط خادم موثوق للتحقق من الحساب والأسعار والدفع والتوصيل. لم يتم إنشاء طلب."
            AppLanguage.FRENCH -> "La validation des commandes est temporairement désactivée jusqu'à l'intégration d'un serveur fiable pour vérifier l'identité, les prix, le paiement et la livraison. Aucune commande n'a été créée."
            AppLanguage.ENGLISH -> "Checkout is temporarily disabled until a trusted backend can verify identity, catalog prices, payment and fulfillment. No order was created."
        }
    }

    fun advanceOrderTrackingStage(orderId: String) {
        _userAlertMessage.value = "هذه العملية متوقفة حتى ربط خادم موثوق لتحديث حالة الطلبات وتتبع المندوبين."
    }

    fun setTrackingOrder(orderId: String) {
        _trackingOrderId.value = orderId
        navigateTo(AppScreen.ORDER_TRACKING)
    }

    private var simulationJob: kotlinx.coroutines.Job? = null
    private val _isCourierSimulating = MutableStateFlow(false)
    val isCourierSimulating: StateFlow<Boolean> = _isCourierSimulating.asStateFlow()

    fun startLiveDemoOrderTracking() {
        _userAlertMessage.value = "تم تعطيل الطلب التجريبي حتى لا يظهر موقع أو مندوب وهمي كأنه حقيقي."
    }

    fun toggleLiveCourierSimulation(orderId: String) {
        simulationJob?.cancel()
        _isCourierSimulating.value = false
        _userAlertMessage.value = "محاكاة حركة المندوب متوقفة. سيظهر التتبع فقط بعد ربط بيانات GPS موثوقة."
    }

    fun fetchCurrentGpsLocation(context: Context, updateDeliveryAddress: Boolean = false) {
        viewModelScope.launch {
            if (!LocationServiceManager.hasLocationPermission(context)) {
                _userAlertMessage.value = "يرجى منح إذن الموقع لتحديد عنوانك بدقة عبر GPS 📍"
                return@launch
            }
            _userAlertMessage.value = "جاري تحديد موقعك الفعلي عبر الأقمار الصناعية (GPS)... 🛰️"
            val location = LocationServiceManager.getCurrentLocation(context)
            if (location != null) {
                _deviceLocation.value = location
                _customerLat.value = location.latitude
                _customerLng.value = location.longitude
                if (updateDeliveryAddress && location.addressLabel.isNotBlank()) {
                    _deliveryAddress.value = location.addressLabel
                    _userAlertMessage.value = "تم تحديد موقعك بدقة: ${location.addressLabel} 🎯"
                } else {
                    _userAlertMessage.value = "تم التقاط إحداثيات موقعك الفعلي بدقة (دقة: ${location.accuracy.toInt()}م) 🎯"
                }
            } else {
                _userAlertMessage.value = "تعذر تحديد الإحداثيات حالياً، تأكد من تفعيل خدمة GPS على الجهاز."
            }
        }
    }

    fun startRealtimeLocationTracking(context: Context) {
        if (!LocationServiceManager.hasLocationPermission(context)) return

        locationTrackingJob?.cancel()
        _isLiveLocationTrackingEnabled.value = true
        _userAlertMessage.value = "تم تفعيل التتبع الفوري المباشر عبر Google Play Services 🛰️"

        locationTrackingJob = viewModelScope.launch {
            LocationServiceManager.getLocationUpdates(context, intervalMillis = 3000L).collect { loc ->
                _deviceLocation.value = loc
                // Device location remains local until authenticated backend tracking is configured.
            }
        }
    }

    fun stopRealtimeLocationTracking() {
        locationTrackingJob?.cancel()
        locationTrackingJob = null
        _isLiveLocationTrackingEnabled.value = false
        _userAlertMessage.value = "تم إيقاف تتبع الموقع المباشر."
    }

    fun openGoogleMapsRoute(context: Context, order: OrderEntity) {
        LocationServiceManager.openGoogleMapsNavigation(
            context = context,
            destinationLat = order.customerLat,
            destinationLng = order.customerLng,
            destinationLabel = order.deliveryAddress
        )
    }

    fun updateCourierGpsFromDriver(orderId: String, lat: Double, lng: Double, customerLat: Double, customerLng: Double) {
        if (!requireRole(UserRole.DRIVER)) return
        _userAlertMessage.value = "هذه العملية متوقفة حتى ربط خادم موثوق لتحديث حالة الطلبات وتتبع المندوبين."
    }

    fun submitReview(
        orderId: String,
        storeName: String,
        storeRating: Int,
        courierRating: Int,
        tags: List<String>,
        comment: String
    ) {
        _userAlertMessage.value = "إضافة التقييم متوقفة حتى التحقق من طلب مكتمل عبر الخادم."
    }

    fun reorder(order: OrderEntity) {
        _userAlertMessage.value = "إعادة الطلب غير متاحة حتى حفظ تفاصيل المنتجات الفعلية والتحقق من الطلب السابق عبر الخادم."
    }

    fun sendSupportMessage(userText: String) {
        if (userText.isBlank()) return

        val userMsg = SupportChatMessage(
            id = "msg_${System.currentTimeMillis()}",
            text = userText,
            isUser = true
        )
        _supportMessages.value = _supportMessages.value + userMsg

        viewModelScope.launch {
            delay(500)
            val lang = _selectedLanguage.value
            val responseText = com.example.data.services.AiSupportVoiceService.generateSupportResponse(
                userPrompt = userText,
                language = lang
            )

            val botMsg = SupportChatMessage(
                id = "bot_${System.currentTimeMillis()}",
                text = responseText,
                isUser = false,
                agentName = when (lang) {
                    AppLanguage.FRENCH -> "Sara (IA Support DELIVIO)"
                    AppLanguage.ENGLISH -> "Sara (DELIVIO AI Support)"
                    AppLanguage.ARABIC -> "سارة - دعم ديليفيو DELIVIO"
                }
            )
            _supportMessages.value = _supportMessages.value + botMsg
        }
    }

    fun topUpWallet(amount: Double) {
        _userAlertMessage.value = when (_selectedLanguage.value) {
            AppLanguage.ARABIC -> "شحن المحفظة غير متاح حتى ربط مزود دفع حقيقي والتحقق من العملية."
            AppLanguage.FRENCH -> "Le rechargement est indisponible tant qu'un prestataire de paiement sécurisé n'est pas connecté."
            AppLanguage.ENGLISH -> "Wallet top-ups are unavailable until a real payment provider and server verification are configured."
        }
    }

    fun markNotificationRead(id: String) {
        viewModelScope.launch {
            repository.markNotificationRead(id, _currentUserId.value)
        }
    }

    fun markAllNotificationsRead() {
        viewModelScope.launch {
            repository.markAllNotificationsRead(_currentUserId.value)
        }
    }

    // Real-World Android Sharing & External Integrations
    fun shareOrderReceipt(context: Context, order: OrderEntity) {
        ExternalShareHelper.shareOrderReceipt(context, order)
    }

    fun openCourierWhatsApp(context: Context, order: OrderEntity, customPhone: String? = null): Boolean {
        val targetPhone = when {
            !customPhone.isNullOrBlank() && !ExternalShareHelper.isPlaceholderOrInvalid(customPhone) -> customPhone
            !order.courierPhone.isBlank() && !ExternalShareHelper.isPlaceholderOrInvalid(order.courierPhone) -> order.courierPhone
            else -> ""
        }
        if (ExternalShareHelper.isPlaceholderOrInvalid(targetPhone)) {
            return false
        }
        return ExternalShareHelper.openWhatsApp(
            context = context,
            phoneE164 = targetPhone,
            prefilledText = "السلام عليكم كابتن ديليفيو، أنا العميل بخصوص طلبي #${order.id} من متجر ${order.storeName} - العنوان: ${order.deliveryAddress}."
        )
    }

    fun openSupportWhatsApp(context: Context, customPhone: String? = null): Boolean {
        val targetPhone = when {
            !customPhone.isNullOrBlank() && !ExternalShareHelper.isPlaceholderOrInvalid(customPhone) -> customPhone
            else -> ExternalShareHelper.getSavedWhatsAppNumber(context)
        }
        if (ExternalShareHelper.isPlaceholderOrInvalid(targetPhone)) {
            return false
        }
        return ExternalShareHelper.openWhatsApp(
            context = context,
            phoneE164 = targetPhone,
            prefilledText = "السلام عليكم، أحتاج مساعدة ودعم فني من إدارة ديليفيو DELIVIO بخصوص تطبيق التوصيل."
        )
    }

    fun updateCourierPhone(orderId: String, phone: String) {
        _userAlertMessage.value = "لا يمكن تعديل رقم مندوب في طلب محلي. يجب أن تأتي بيانات التواصل من الطلب المؤكد على الخادم."
    }

    fun saveUserProfile(profile: UserProfile) {
        viewModelScope.launch {
            val userId = _currentUserId.value
            if (userId.isBlank()) {
                _userAlertMessage.value = "يرجى تسجيل الدخول قبل حفظ الملف الشخصي."
                return@launch
            }
            repository.saveUserProfile(profile.copy(userId = userId))
            _userAlertMessage.value = "تم حفظ الملف الشخصي على هذا الجهاز."
        }
    }

    fun updateDeliveryPreferences(
        deliveryAddress: String,
        city: String,
        neighborhood: String,
        buildingInfo: String,
        notes: String,
        contactless: Boolean = false,
        requestCutlery: Boolean = true,
        paymentMethod: String = "CASH_ON_DELIVERY"
    ) {
        viewModelScope.launch {
            val userId = _currentUserId.value
            if (userId.isBlank()) {
                _userAlertMessage.value = "يرجى تسجيل الدخول قبل حفظ تفضيلات التوصيل."
                return@launch
            }
            val current = userProfile.value ?: UserProfile(userId = userId)
            val updated = current.copy(userId = userId,
                streetAddress = deliveryAddress,
                city = city,
                neighborhood = neighborhood,
                buildingInfo = buildingInfo,
                deliveryNotes = notes,
                contactlessDelivery = contactless,
                requestCutlery = requestCutlery,
                preferredPaymentMethod = paymentMethod,
                updatedAt = System.currentTimeMillis()
            )
            repository.saveUserProfile(updated)
            _deliveryAddress.value = deliveryAddress
            _deliveryNotes.value = notes
            _userAlertMessage.value = "تم تحديث عنوان التوصيل والتفضيلات محلياً بنجاح! 📍"
        }
    }
}
