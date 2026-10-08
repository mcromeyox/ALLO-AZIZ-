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
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
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

    // Screen Navigation
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

    private val _currentUserId = MutableStateFlow("usr_customer_1")
    val currentUserId: StateFlow<String> = _currentUserId.asStateFlow()

    // Driver Status
    private val _driverIsOnline = MutableStateFlow(true)
    val driverIsOnline: StateFlow<Boolean> = _driverIsOnline.asStateFlow()

    // Language
    private val _selectedLanguage = MutableStateFlow(AppLanguage.ARABIC)
    val selectedLanguage: StateFlow<AppLanguage> = _selectedLanguage.asStateFlow()

    // Store & Catalog selection
    private val _selectedCategory = MutableStateFlow(StoreCategory.ALL)
    val selectedCategory: StateFlow<StoreCategory> = _selectedCategory.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedStore = MutableStateFlow<Store?>(null)
    val selectedStore: StateFlow<Store?> = _selectedStore.asStateFlow()

    // Cart State
    private val _cartItems = MutableStateFlow<List<CartItem>>(emptyList())
    val cartItems: StateFlow<List<CartItem>> = _cartItems.asStateFlow()

    private val _appliedPromoCode = MutableStateFlow<String?>(null)
    val appliedPromoCode: StateFlow<String?> = _appliedPromoCode.asStateFlow()

    private val _promoDiscountPercent = MutableStateFlow(0.0)
    val promoDiscountPercent: StateFlow<Double> = _promoDiscountPercent.asStateFlow()

    private val _deliveryAddress = MutableStateFlow("شارع الحسن الثاني، قرب حديقة المدن المتوأمة، المحمدية 🇲🇦")
    val deliveryAddress: StateFlow<String> = _deliveryAddress.asStateFlow()

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

        allOrders = repository.allOrders.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

        activeOrder = repository.activeOrder.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            null
        )

        allNotifications = repository.allNotifications.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

        unreadNotificationsCount = repository.unreadCount.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            0
        )

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

        allAddresses = repository.allAddresses.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

        userProfile = repository.userProfile.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            null
        )

        // Seed initial room data
        viewModelScope.launch {
            repository.initializeDefaultDataIfEmpty()
            initSupportChat()
        }
    }

    private fun initSupportChat() {
        val greeting = when (_selectedLanguage.value) {
            AppLanguage.ARABIC -> "أهلاً بك في خدمة عملاء ألو عزيز 24/7! أنا سارة، كيف يمكنني مساعدتك اليوم؟"
            AppLanguage.FRENCH -> "Bienvenue au support ALLO AZIZ 24/7 ! Je suis Sara, comment puis-je vous aider ?"
            AppLanguage.ENGLISH -> "Welcome to ALLO AZIZ 24/7 Support! I'm Sara, how can I assist you today?"
        }
        _supportMessages.value = listOf(
            SupportChatMessage(
                id = "msg_init",
                text = greeting,
                isUser = false,
                agentName = "سارة - دعم ألو عزيز"
            )
        )
    }

    // Authentication & Account Verification (Strict Multi-Role Policy)
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
            val user = repository.getUserByIdentifier(trimmedId)
            if (user == null) {
                _authError.value = "هذا الحساب غير مسجل مسبقاً! يجب عليك إنشاء حساب جديد أولاً."
                return@launch
            }

            // Status checks
            when (user.status) {
                "PENDING_APPROVAL" -> {
                    val roleMsg = if (user.role == "RESTAURANT_OWNER") {
                        "طلب الانضمام كشريك مطعم (${user.name}) قيد المراجعة والاعتماد من طرف إدارة ALLO AZIZ. ستتمكن من تشغيل متجرك فور اعتماد الحساب."
                    } else if (user.role == "DRIVER") {
                        "طلب الانضمام كسائق مندوب (${user.name}) قيد المراجعة والاعتماد من طرف إدارة ALLO AZIZ. ستتمكن من استقبال الطلبات فور اعتماد الحساب."
                    } else {
                        "حسابك (${user.name}) قيد المراجعة والاعتماد من قبل إدارة ألو عزيز."
                    }
                    _authPendingApprovalMessage.value = roleMsg
                    return@launch
                }
                "SUSPENDED" -> {
                    _authError.value = "تم إيقاف هذا الحساب من قبل الإدارة. يرجى التواصل مع فريق الدعم الفني."
                    return@launch
                }
                "REJECTED" -> {
                    _authError.value = "عذراً، تم رفض طلب التسجيل من طرف إدارة ALLO AZIZ لعدم استيفاء الشروط. للتواصل مع الدعم: support@alloaziz.ma"
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

            // Verify password
            if (user.password.isNotBlank() && user.password != trimmedPass) {
                _authError.value = "كلمة المرور غير صحيحة! يرجى التأكد وإعادة المحاولة."
                return@launch
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
        viewModelScope.launch {
            clearAuthMessages()
            val trimmedEmail = gmail.trim()

            val existingUser = repository.getUserByEmail(trimmedEmail)
            if (existingUser != null) {
                // User exists in database: check database status & role
                when (existingUser.status) {
                    "PENDING_APPROVAL" -> {
                        val roleMsg = if (existingUser.role == "RESTAURANT_OWNER") {
                            "طلب الانضمام كشريك مطعم (${existingUser.name}) قيد المراجعة والاعتماد من طرف إدارة ALLO AZIZ."
                        } else if (existingUser.role == "DRIVER") {
                            "طلب الانضمام كسائق (${existingUser.name}) قيد المراجعة والاعتماد من طرف إدارة ALLO AZIZ."
                        } else {
                            "حسابك قيد المراجعة والاعتماد من قبل الإدارة."
                        }
                        _authPendingApprovalMessage.value = roleMsg
                        return@launch
                    }
                    "SUSPENDED" -> {
                        _authError.value = "هذا الحساب موقوف حالياً من قبل الإدارة."
                        return@launch
                    }
                    "REJECTED" -> {
                        _authError.value = "تم رفض طلب الانضمام لهذا الحساب مسبقاً من طرف إدارة ALLO AZIZ."
                        return@launch
                    }
                    "DEACTIVATED" -> {
                        _authError.value = "هذا الحساب معطل حالياً."
                        return@launch
                    }
                }

                _currentUser.value = existingUser
                _currentUserId.value = existingUser.id
                val matchedRole = UserRole.values().firstOrNull { it.name == existingUser.role } ?: UserRole.CUSTOMER
                _currentRole.value = matchedRole

                when (matchedRole) {
                    UserRole.CUSTOMER -> _currentScreen.value = AppScreen.HOME
                    UserRole.RESTAURANT_OWNER -> _currentScreen.value = AppScreen.RESTAURANT_DASHBOARD
                    UserRole.DRIVER -> _currentScreen.value = AppScreen.DRIVER_DASHBOARD
                    UserRole.SUPPORT_AGENT -> _currentScreen.value = AppScreen.SUPPORT_DASHBOARD
                    UserRole.SUPER_ADMIN -> _currentScreen.value = AppScreen.ADMIN_DASHBOARD
                }
                _userAlertMessage.value = "تم الدخول بحساب Google: ${existingUser.name}! 🌟"
            } else {
                // Brand new Google user: defaults to role = CUSTOMER, status = ACTIVE
                val newGoogleUser = UserAccountEntity(
                    id = "usr_g_${System.currentTimeMillis()}",
                    name = name.ifBlank { "عميل Google" },
                    email = trimmedEmail,
                    phone = "+212 600-000000",
                    password = "google_authenticated",
                    role = "CUSTOMER",
                    status = "ACTIVE",
                    balance = 0.0,
                    avatarEmoji = "👤",
                    authProvider = "GOOGLE"
                )
                repository.insertUser(newGoogleUser)

                _currentUser.value = newGoogleUser
                _currentUserId.value = newGoogleUser.id
                _currentRole.value = UserRole.CUSTOMER
                _currentScreen.value = AppScreen.HOME
                _userAlertMessage.value = "مرحباً بك يا ${newGoogleUser.name}! تم إنشاء وتفعيل حسابك بحساب Google بنجاح 🛍️"
            }
        }
    }

    fun registerUser(
        name: String,
        email: String,
        phone: String,
        pass: String,
        role: UserRole,
        restaurantName: String = "",
        vehicleType: String = "",
        plateNumber: String = ""
    ) {
        viewModelScope.launch {
            clearAuthMessages()
            val trimmedName = name.trim()
            val trimmedEmail = email.trim()
            val trimmedPhone = phone.trim()
            val trimmedPass = pass.trim().ifBlank { "abdomery" }

            // Security: Support and Admin accounts cannot be created via public registration
            if (role == UserRole.SUPPORT_AGENT || role == UserRole.SUPER_ADMIN) {
                _authError.value = "لا يمكن إنشاء حسابات إدارية أو دعم فني من واجهة التسجيل العامة."
                return@launch
            }

            if (trimmedName.isBlank() || (trimmedEmail.isBlank() && trimmedPhone.isBlank())) {
                _authError.value = "يرجى ملء الاسم ورقم الهاتف أو البريد الإلكتروني."
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
            // RESTAURANT_OWNER (Merchant) & DRIVER -> PENDING_APPROVAL
            val initialStatus = if (role == UserRole.CUSTOMER) "ACTIVE" else "PENDING_APPROVAL"

            val newId = "usr_${System.currentTimeMillis()}"
            val newAccount = UserAccountEntity(
                id = newId,
                name = trimmedName,
                email = trimmedEmail.ifBlank { "$trimmedPhone@alloaziz.ma" },
                phone = trimmedPhone.ifBlank { "+212 600-000000" },
                password = trimmedPass,
                role = role.name,
                status = initialStatus,
                restaurantName = if (role == UserRole.RESTAURANT_OWNER) restaurantName.ifBlank { "مطعم $trimmedName" } else null,
                vehicleType = if (role == UserRole.DRIVER) vehicleType.ifBlank { "دراجة نارية / سكوتر" } else null,
                plateNumber = if (role == UserRole.DRIVER) plateNumber.ifBlank { "أ - 12345" } else null,
                balance = 0.0,
                avatarEmoji = role.emoji,
                authProvider = if (trimmedEmail.endsWith("@gmail.com")) "GOOGLE" else "PHONE"
            )

            repository.insertUser(newAccount)

            if (role == UserRole.CUSTOMER) {
                // Immediately active! Immediately log in and enter Customer Home
                _currentUser.value = newAccount
                _currentUserId.value = newAccount.id
                _currentRole.value = UserRole.CUSTOMER
                _currentScreen.value = AppScreen.HOME
                _userAlertMessage.value = "مرحباً بك يا $trimmedName في ألو عزيز! تم إنشاء حسابك بنجاح وبإمكانك الطلب الآن 🛍️"

                repository.addNotification(
                    NotificationEntity(
                        id = "notif_welcome_${System.currentTimeMillis()}",
                        titleAr = "مرحباً بك في ألو عزيز! 🇲🇦",
                        titleFr = "Bienvenue sur ALLO AZIZ !",
                        titleEn = "Welcome to ALLO AZIZ!",
                        bodyAr = "تم تفعيل حسابك بنجاح. استمتع بأشهى المأكولات والتوصيل السريع بالمحمدية.",
                        bodyFr = "Votre compte a été activé avec succès.",
                        bodyEn = "Your account is now active.",
                        timestamp = System.currentTimeMillis(),
                        type = "WELCOME"
                    )
                )
            } else {
                // Partner (Merchant or Driver): Requires Admin Approval
                _authSuccessMessage.value = "تم إرسال طلبك بنجاح. سيتم مراجعة طلب الانضمام من طرف إدارة ALLO AZIZ، وسنخبرك عند تفعيل حسابك."

                repository.addNotification(
                    NotificationEntity(
                        id = "notif_reg_${System.currentTimeMillis()}",
                        titleAr = "طلب انضمام شريك جديد بانتظار الاعتماد 🔔",
                        titleFr = "Nouvelle demande de partenariat",
                        titleEn = "New Partner Application",
                        bodyAr = "سجل ${trimmedName} كـ (${role.titleAr}) وهو بانتظار مراجعة واعتماد الإدارة.",
                        bodyFr = "${trimmedName} a postulé en tant que (${role.titleFr}).",
                        bodyEn = "${trimmedName} registered as (${role.titleEn}) and awaits activation.",
                        timestamp = System.currentTimeMillis(),
                        type = "SECURITY"
                    )
                )
            }
        }
    }

    fun approvePartnerApplication(userId: String) {
        viewModelScope.launch {
            repository.updateUserStatus(userId, "ACTIVE")
            repository.addNotification(
                NotificationEntity(
                    id = "notif_appr_${System.currentTimeMillis()}",
                    titleAr = "تم قبول طلب انضمامك إلى ALLO AZIZ بنجاح 🎉",
                    titleFr = "Demande acceptée avec succès 🎉",
                    titleEn = "Partner Application Approved 🎉",
                    bodyAr = "تم قبول طلب انضمامك إلى ALLO AZIZ بنجاح. يمكنك الآن تسجيل الدخول وبدء العمل واستقبال الطلبات.",
                    bodyFr = "Votre demande a été approuvée par l'administration ALLO AZIZ.",
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
            repository.updateUserStatus(userId, "REJECTED")
            repository.addNotification(
                NotificationEntity(
                    id = "notif_rej_${System.currentTimeMillis()}",
                    titleAr = "تحديث بخصوص طلب الانضمام إلى ALLO AZIZ",
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
            val newAddr = AddressEntity(
                id = "addr_${System.currentTimeMillis()}",
                userId = _currentUserId.value,
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
            repository.deleteAddress(addressId)
            _userAlertMessage.value = "تم حذف العنوان"
        }
    }

    fun setDefaultAddress(addressId: String) {
        viewModelScope.launch {
            repository.setDefaultAddress(_currentUserId.value, addressId)
            _userAlertMessage.value = "تم تعيين العنوان كعنوان افتراضي"
        }
    }

    fun updateCommissionRates(merchantPercent: Double, driverPayout: Double, serviceFee: Double) {
        _merchantCommissionPercent.value = merchantPercent
        _driverPayoutPercent.value = driverPayout
        _platformServiceFee.value = serviceFee
        _userAlertMessage.value = "تم تحديث نظام العمولات والرسوم بنجاح ✓"
    }

    fun toggleDeliveryZone(zoneId: String) {
        _deliveryZones.value = _deliveryZones.value.map { zone ->
            if (zone.id == zoneId) zone.copy(isActive = !zone.isActive) else zone
        }
        _userAlertMessage.value = "تم تحديث حالة منطقة التوصيل بالمحمدية"
    }

    // Role Switching
    fun switchRole(newRole: UserRole) {
        _currentRole.value = newRole
        when (newRole) {
            UserRole.CUSTOMER -> {
                _currentUserId.value = "usr_customer_1"
                navigateTo(AppScreen.HOME)
                _userAlertMessage.value = "تم التبديل إلى: حساب الزبون 🛍️"
            }
            UserRole.RESTAURANT_OWNER -> {
                _currentUserId.value = "owner_mcdo"
                navigateTo(AppScreen.RESTAURANT_DASHBOARD)
                _userAlertMessage.value = "تم التبديل إلى: بوابة صاحب المطعم (ماكدونالدز) 👨‍🍳"
            }
            UserRole.DRIVER -> {
                _currentUserId.value = "drv_aziz"
                navigateTo(AppScreen.DRIVER_DASHBOARD)
                _userAlertMessage.value = "تم التبديل إلى: بوابة السائق (الكابتن عزيز) 🛵"
            }
            UserRole.SUPPORT_AGENT -> {
                _currentUserId.value = "usr_support_1"
                navigateTo(AppScreen.SUPPORT_DASHBOARD)
                _userAlertMessage.value = "تم التبديل إلى: لوحة الدعم الفني وتفعيل السائقين 🎧"
            }
            UserRole.SUPER_ADMIN -> {
                _currentUserId.value = "usr_admin_1"
                navigateTo(AppScreen.ADMIN_DASHBOARD)
                _userAlertMessage.value = "تم التبديل إلى: لوحة الإدارة العامة والتحكم الشامل 👑"
            }
        }
    }

    // Driver Partner functions
    fun toggleDriverOnline() {
        _driverIsOnline.value = !_driverIsOnline.value
        _userAlertMessage.value = if (_driverIsOnline.value) "أنت متصل الآن ومتاح لتلقي الطلبات 🟢" else "أنت غير متصل حالياً 🔴"
    }

    fun acceptDeliveryTask(orderId: String) {
        viewModelScope.launch {
            val driver = _currentUser.value
            val driverName = driver?.name?.ifBlank { "كابتن التوصيل" } ?: "كابتن التوصيل"
            val driverPhone = driver?.phone?.ifBlank { "" } ?: ""
            repository.assignDriverToOrder(orderId, driver?.id ?: "drv_aziz", driverName, driverPhone)
            HapticFeedbackHelper.vibrateAlert(getApplication())
            AndroidNotificationHelper.showOrderNotification(
                context = getApplication(),
                orderId = orderId,
                title = "تم إسناد الطلب #$orderId إليك 🛵",
                body = "توجه إلى المطعم لاستلام الوجبة والتحقق من تفاصيل الطلب."
            )
            _userAlertMessage.value = "تم قبول مهمة التوصيل للطلب #$orderId! توجه للمطعم 🛵"
        }
    }

    fun markOrderPickedUpFromKitchen(orderId: String) {
        viewModelScope.launch {
            repository.updateOrderStatus(orderId, "ON_THE_WAY", 0.60f, 10)
            HapticFeedbackHelper.vibrateAlert(getApplication())
            AndroidNotificationHelper.showOrderNotification(
                context = getApplication(),
                orderId = orderId,
                title = "تم استلام الطلب من المطعم 📦",
                body = "الطلب #$orderId في حوزتك الآن، يرجى التوجه لعنوان العميل."
            )
            _userAlertMessage.value = "تم تأكيد استلام الطلب من المطعم! انطلق نحو موقع العميل 📍"
        }
    }

    fun markOrderDeliveredToCustomer(orderId: String) {
        viewModelScope.launch {
            repository.updateOrderStatus(orderId, "DELIVERED", 1.0f, 0)
            HapticFeedbackHelper.vibrateSuccess(getApplication())
            AndroidNotificationHelper.showOrderNotification(
                context = getApplication(),
                orderId = orderId,
                title = "تم إكمال التوصيل بنجاح! 🏆",
                body = "أحسنت! تم تسليم الطلب #$orderId وإيداع عمولة التوصيل في رصيدك."
            )
            _userAlertMessage.value = "تم تأكيد تسليم الطلب للعميل بنجاح! تمت إضافة عمولة التوصيل لمحفظتك 🎉"
        }
    }

    // Support Agent functions
    fun approveDriverAccount(driverId: String) {
        viewModelScope.launch {
            repository.updateUserStatus(driverId, "ACTIVE")
            repository.addNotification(
                NotificationEntity(
                    id = "notif_drv_appr_${System.currentTimeMillis()}",
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
            repository.updateUserStatus(driverId, "SUSPENDED")
            _userAlertMessage.value = "تم رفض طلب التسجيل مؤقتاً لعدم استيفاء الوثائق."
        }
    }

    fun resolveTicket(ticketId: String) {
        viewModelScope.launch {
            repository.updateTicketStatus(ticketId, "RESOLVED")
            _userAlertMessage.value = "تم حل التذكرة #$ticketId بنجاح!"
        }
    }

    fun grantCustomerCompensation(amount: Double) {
        viewModelScope.launch {
            repository.topUpWallet(amount)
            repository.addNotification(
                NotificationEntity(
                    id = "notif_comp_${System.currentTimeMillis()}",
                    titleAr = "تعويض فوري من خدمة العملاء 🎁",
                    titleFr = "Dédommagement client 🎁",
                    titleEn = "Customer Compensation 🎁",
                    bodyAr = "تمت إضافة $amount د.م إلى محفظتك اعتذاراً عن أي إزعاج.",
                    bodyFr = "$amount DH ont été crédités sur votre compte.",
                    bodyEn = "$amount MAD credited to your wallet.",
                    timestamp = System.currentTimeMillis()
                )
            )
            _userAlertMessage.value = "تم إرسال تعويض مالي فوري قدره $amount د.م إلى محفظة العميل!"
        }
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
            repository.toggleProductAvailability(productId, !currentAvailable)
            _userAlertMessage.value = if (!currentAvailable) "أصبحت الوجبة متاحة للطلب الآن ✓" else "تم تحديد الوجبة كغير متوفرة حالياً ✕"
        }
    }

    fun deleteProduct(productId: String) {
        viewModelScope.launch {
            repository.deleteProduct(productId)
            _userAlertMessage.value = "تم حذف الوجبة من القائمة بنجاح."
        }
    }

    fun toggleStoreOpen(storeId: String, currentOpen: Boolean) {
        viewModelScope.launch {
            repository.toggleStoreOpen(storeId, !currentOpen)
            _userAlertMessage.value = if (!currentOpen) "المطعم مفتوح الآن وجاهز لاستقبال الطلبات 🟢" else "المطعم مغلق حالياً 🔴"
        }
    }

    fun updateOrderCookingStatus(orderId: String, restStatus: String, orderStatus: String) {
        viewModelScope.launch {
            repository.updateRestaurantStatus(orderId, restStatus, orderStatus)
            HapticFeedbackHelper.vibrateAlert(getApplication())
            val statusText = when (restStatus) {
                "COOKING" -> "المطعم بدأ في تحضير الطلب #$orderId 👨‍🍳"
                "READY_FOR_PICKUP" -> "الطلب #$orderId جاهز وفي انتظار استلام المندوب 📦"
                else -> "تحديث حالة الطلب #$orderId: $restStatus"
            }
            AndroidNotificationHelper.showOrderNotification(
                context = getApplication(),
                orderId = orderId,
                title = "تحديث من المطعم 🏪",
                body = statusText
            )
            _userAlertMessage.value = "تم تحديث حالة الطلب إلى: $restStatus"
        }
    }

    // Super Admin functions
    fun updateUserStatus(userId: String, newStatus: String) {
        viewModelScope.launch {
            repository.updateUserStatus(userId, newStatus)
            _userAlertMessage.value = "تم تحديث حالة المستخدم إلى: $newStatus"
        }
    }

    fun updateUserRole(userId: String, newRole: String) {
        viewModelScope.launch {
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
        viewModelScope.launch {
            val notif = NotificationEntity(
                id = "broadcast_${System.currentTimeMillis()}",
                titleAr = title,
                titleFr = title,
                titleEn = title,
                bodyAr = body,
                bodyFr = body,
                bodyEn = body,
                timestamp = System.currentTimeMillis(),
                type = "SYSTEM"
            )
            repository.addNotification(notif)
            HapticFeedbackHelper.vibrateAlert(getApplication())
            AndroidNotificationHelper.showPromoNotification(
                context = getApplication(),
                title = title,
                body = body
            )
            _userAlertMessage.value = "تم إرسال الإشعار الترويجي العام لجميع مستخدمي التطبيق بنجاح! 📢"
        }
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

            if (trimmedId.equals("mcromeyox@gmail.com", ignoreCase = true) && trimmedPass == "abdomery") {
                val adminUser = repository.getUserByEmail("mcromeyox@gmail.com") ?: UserAccountEntity(
                    id = "usr_super_admin",
                    name = "المدير العام (Super Admin)",
                    email = "mcromeyox@gmail.com",
                    phone = "+212 600-000001",
                    password = "abdomery",
                    role = "SUPER_ADMIN",
                    status = "ACTIVE",
                    balance = 150000.0,
                    avatarEmoji = "👑",
                    authProvider = "EMAIL"
                )
                repository.insertUser(adminUser)
                _currentUser.value = adminUser
                _currentUserId.value = adminUser.id
                _currentRole.value = UserRole.SUPER_ADMIN
                _currentScreen.value = AppScreen.ADMIN_DASHBOARD
                _userAlertMessage.value = "تم تسجيل الدخول كمدير عام بنجاح 👑"
                return@launch
            }

            val user = repository.getUserByIdentifier(trimmedId)
            if (user == null) {
                _authError.value = "حساب الموظف أو الشريك غير مسجل بالنظام."
                return@launch
            }

            if (user.password.isNotBlank() && user.password != trimmedPass) {
                _authError.value = "كلمة المرور غير صحيحة."
                return@launch
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
        viewModelScope.launch {
            val trimmed = identifier.trim()
            if (trimmed.isBlank()) {
                _userAlertMessage.value = "يرجى كتابة البريد الإلكتروني أو رقم الهاتف."
                return@launch
            }
            val otp = "842910"
            _userAlertMessage.value = "📩 [وضع التطوير] رمز التحقق لتغيير كلمة المرور: $otp"
            onOtpGenerated(otp)
        }
    }

    fun confirmPasswordReset(identifier: String, newPass: String) {
        viewModelScope.launch {
            val user = repository.getUserByIdentifier(identifier.trim())
            if (user != null) {
                repository.insertUser(user.copy(password = newPass.trim()))
                _authSuccessMessage.value = "تم تغيير كلمة المرور بنجاح! يمكنك الآن تسجيل الدخول."
            } else {
                _authSuccessMessage.value = "تم حفظ كلمة المرور الجديدة بنجاح."
            }
        }
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
        val cleanCode = code.trim().uppercase()
        if (cleanCode == "AZIZ20") {
            _appliedPromoCode.value = cleanCode
            _promoDiscountPercent.value = 0.20 // 20%
            _userAlertMessage.value = when (_selectedLanguage.value) {
                AppLanguage.ARABIC -> "تم تطبيق خصم 20% بنجاح! كود AZIZ20 مفعل 🎉"
                AppLanguage.FRENCH -> "Code AZIZ20 appliqué ! -20% sur la commande 🎉"
                AppLanguage.ENGLISH -> "Code AZIZ20 applied! 20% off your order 🎉"
            }
            return true
        } else if (cleanCode == "ALLO10") {
            _appliedPromoCode.value = cleanCode
            _promoDiscountPercent.value = 0.10 // 10%
            _userAlertMessage.value = when (_selectedLanguage.value) {
                AppLanguage.ARABIC -> "تم تطبيق خصم 10% بنجاح! كود ALLO10 مفعل 🎉"
                AppLanguage.FRENCH -> "Code ALLO10 appliqué ! -10% 🎉"
                AppLanguage.ENGLISH -> "Code ALLO10 applied! 10% off 🎉"
            }
            return true
        } else {
            _userAlertMessage.value = when (_selectedLanguage.value) {
                AppLanguage.ARABIC -> "عذراً، كود الخصم غير صالح. جرب AZIZ20"
                AppLanguage.FRENCH -> "Code promo invalide. Essayez AZIZ20"
                AppLanguage.ENGLISH -> "Invalid promo code. Try AZIZ20"
            }
            return false
        }
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
    fun placeOrder(
        onSuccess: (orderId: String) -> Unit
    ) {
        val items = _cartItems.value
        if (items.isEmpty()) return

        val storeName = items.first().product.storeName
        val storeId = items.first().product.storeId
        val subtotal = items.sumOf { it.product.price * it.quantity }
        val deliveryFee = 10.0
        val discount = subtotal * _promoDiscountPercent.value
        val total = (subtotal - discount + deliveryFee).coerceAtLeast(0.0)

        val orderNum = Random.nextInt(1000, 9999)
        val orderId = "AZ-$orderNum"

        val itemsSummary = items.joinToString(", ") { "${it.quantity}x ${it.product.nameAr}" }
        val itemsCount = items.sumOf { it.quantity }

        val paymentMethodStr = when (_selectedPaymentMethod.value) {
            PaymentMethodType.CREDIT_CARD -> "بطاقة مصرفية آمنة (Visa/Mastercard)"
            PaymentMethodType.CASH_ON_DELIVERY -> "الدفع عند الاستلام (كاش)"
            PaymentMethodType.ALLO_PAY_WALLET -> "محفظة ALLO Pay"
            PaymentMethodType.DIGITAL_WALLET -> "Google Pay / Apple Pay"
        }

        viewModelScope.launch {
            if (_selectedPaymentMethod.value == PaymentMethodType.ALLO_PAY_WALLET) {
                repository.deductWallet(total)
            }

            val newOrder = OrderEntity(
                id = orderId,
                storeId = storeId,
                storeName = storeName,
                itemsSummary = itemsSummary,
                itemsCount = itemsCount,
                subtotal = subtotal,
                deliveryFee = deliveryFee,
                discount = discount,
                total = total,
                status = "RECEIVED",
                restaurantStatus = "PENDING_ACCEPTANCE",
                driverId = null,
                paymentMethod = paymentMethodStr,
                deliveryAddress = _deliveryAddress.value.ifBlank { "المحمدية، المغرب" },
                deliveryNotes = _deliveryNotes.value,
                timestamp = System.currentTimeMillis(),
                courierName = "جاري تعيين كابتن التوصيل... 🛵",
                courierPhone = "",
                courierRating = 5.0,
                courierVehicle = "دراجة نارية",
                courierProgress = 0.10f,
                estimatedMinsLeft = 25,
                hasReviewed = false
            )

            repository.insertOrder(newOrder)
            FirestoreSyncManager.syncOrderToCloud(newOrder)

            HapticFeedbackHelper.vibrateSuccess(getApplication())
            AndroidNotificationHelper.showOrderNotification(
                context = getApplication(),
                orderId = orderId,
                title = "تم تأكيد طلبك #$orderId بنجاح! 🛵",
                body = "طلبك قيد المراجعة لدى متجر \"$storeName\". سيتم إسناد أقرب كابتن إليك."
            )

            repository.addNotification(
                NotificationEntity(
                    id = "notif_${System.currentTimeMillis()}",
                    titleAr = "تم تأكيد طلبك #$orderId بنجاح! 🛵",
                    titleFr = "Commande #$orderId confirmée ! 🛵",
                    titleEn = "Order #$orderId Confirmed! 🛵",
                    bodyAr = "طلبك قيد المراجعة لدى متجر \"$storeName\". سيتم إسناد أقرب كابتن إليك.",
                    bodyFr = "Votre commande chez \"$storeName\" est confirmée.",
                    bodyEn = "Your order at \"$storeName\" is confirmed.",
                    timestamp = System.currentTimeMillis(),
                    isRead = false,
                    type = "ORDER",
                    orderId = orderId
                )
            )

            clearCart()
            _trackingOrderId.value = orderId
            navigateTo(AppScreen.ORDER_TRACKING)
            onSuccess(orderId)
        }
    }

    fun advanceOrderTrackingStage(orderId: String) {
        viewModelScope.launch {
            val order = allOrders.value.firstOrNull { it.id == orderId } ?: activeOrder.value ?: return@launch
            when (order.status) {
                "RECEIVED", "PLACED" -> {
                    repository.updateOrderStatus(orderId, "PREPARING", 0.35f, 18)
                    repository.updateRestaurantStatus(orderId, "COOKING", "PREPARING")
                    HapticFeedbackHelper.vibrateAlert(getApplication())
                    AndroidNotificationHelper.showOrderNotification(
                        context = getApplication(),
                        orderId = orderId,
                        title = "المطعم يجهز وجبتك بعناية 👨‍🍳",
                        body = "طلبك #$orderId قيد التحضير في المطبخ الآن."
                    )
                    _userAlertMessage.value = "المطعم بدأ في تحضير الطلب الآن 👨‍🍳"
                }
                "PREPARING" -> {
                    val savedPhone = ExternalShareHelper.getSavedWhatsAppNumber(getApplication())
                    val courierPhone = if (!ExternalShareHelper.isPlaceholderOrInvalid(order.courierPhone)) {
                        order.courierPhone
                    } else if (!ExternalShareHelper.isPlaceholderOrInvalid(savedPhone)) {
                        savedPhone
                    } else {
                        ""
                    }
                    val courierName = if (order.courierName.contains("جاري") || order.courierName.isBlank()) {
                        "الكابتن عزيز برادة"
                    } else {
                        order.courierName
                    }
                    repository.assignDriverToOrder(orderId, order.driverId ?: "drv_aziz", courierName, courierPhone)
                    repository.updateOrderStatus(orderId, "ON_THE_WAY", 0.65f, 10)
                    repository.updateRestaurantStatus(orderId, "READY_FOR_PICKUP", "ON_THE_WAY")
                    HapticFeedbackHelper.vibrateAlert(getApplication())
                    AndroidNotificationHelper.showOrderNotification(
                        context = getApplication(),
                        orderId = orderId,
                        title = "الكابتن استلم الطلب وهو في الطريق إليك! 🛵💨",
                        body = "الكابتن انطلق بدراجته النارية لتسليم طلبك."
                    )
                    _userAlertMessage.value = "الكابتن استلم الطلب وهو في الطريق إليك 🛵💨"
                }
                "ON_THE_WAY" -> {
                    repository.updateOrderStatus(orderId, "ARRIVED", 0.90f, 2)
                    HapticFeedbackHelper.vibrateAlert(getApplication())
                    AndroidNotificationHelper.showOrderNotification(
                        context = getApplication(),
                        orderId = orderId,
                        title = "الكابتن وصل أمام العنوان! 📍🚪",
                        body = "الكابتن متواجد أمام المبنى لتسليمك الطلب ساخناً."
                    )
                    _userAlertMessage.value = "الكابتن وصل عند بابك لتسليم الطلب 📍🚪"
                }
                "ARRIVED" -> {
                    repository.updateOrderStatus(orderId, "DELIVERED", 1.0f, 0)
                    HapticFeedbackHelper.vibrateSuccess(getApplication())
                    AndroidNotificationHelper.showOrderNotification(
                        context = getApplication(),
                        orderId = orderId,
                        title = "تم تسليم الطلب بنجاح! بالصحة والراحة 😋🎉",
                        body = "شكراً لاختيارك ألو عزيز! شاركنا تقييمك للخدمة."
                    )
                    _userAlertMessage.value = "تم تأكيد تسليم واستلام الطلب بنجاح! 🎉"
                }
            }
        }
    }

    fun setTrackingOrder(orderId: String) {
        _trackingOrderId.value = orderId
        navigateTo(AppScreen.ORDER_TRACKING)
    }

    // Real-Time GPS Tracking with Google Play Services Location
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
                val active = activeOrder.value
                // If current user is a driver on an active order, update live courier location
                if (active != null && _currentRole.value == UserRole.DRIVER && active.status == "ON_THE_WAY") {
                    val distKm = LocationServiceManager.calculateDistanceKm(
                        loc.latitude, loc.longitude,
                        active.customerLat, active.customerLng
                    )
                    val minsLeft = LocationServiceManager.estimateMinutesLeft(distKm)
                    val progress = ((1.0 - (distKm / 3.0)).toFloat()).coerceIn(0.1f, 0.98f)
                    repository.updateCourierGps(active.id, loc.latitude, loc.longitude, progress, minsLeft)
                    FirestoreSyncManager.updateCourierGpsCloud(active.id, loc.latitude, loc.longitude, progress, minsLeft)
                }
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
        viewModelScope.launch {
            val distKm = LocationServiceManager.calculateDistanceKm(lat, lng, customerLat, customerLng)
            val minsLeft = LocationServiceManager.estimateMinutesLeft(distKm)
            val progress = ((1.0 - (distKm / 3.0)).toFloat()).coerceIn(0.1f, 0.98f)
            repository.updateCourierGps(orderId, lat, lng, progress, minsLeft)
            FirestoreSyncManager.updateCourierGpsCloud(orderId, lat, lng, progress, minsLeft)
        }
    }

    // Submit Review
    fun submitReview(
        orderId: String,
        storeName: String,
        storeRating: Int,
        courierRating: Int,
        tags: List<String>,
        comment: String
    ) {
        viewModelScope.launch {
            val dateStr = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())
            val reviewEntity = ReviewEntity(
                id = "rev_${System.currentTimeMillis()}",
                orderId = orderId,
                storeName = storeName,
                customerName = "كريم التازي",
                storeRating = storeRating,
                courierRating = courierRating,
                tagsJson = tags.joinToString(prefix = "[", postfix = "]") { "\"$it\"" },
                comment = comment.ifBlank { "تجربة رائعة وتوصيل فائق السرعة!" },
                dateString = dateStr
            )
            repository.addReview(reviewEntity)
            repository.markOrderReviewed(orderId)

            _userAlertMessage.value = when (_selectedLanguage.value) {
                AppLanguage.ARABIC -> "شكراً لتقييمك! تم إرسال تقييمك ومكافأة المندوب عزيز ⭐"
                AppLanguage.FRENCH -> "Merci pour votre évaluation ! Avis enregistré avec succès ⭐"
                AppLanguage.ENGLISH -> "Thank you! Your rating has been submitted successfully ⭐"
            }
        }
    }

    fun reorder(order: OrderEntity) {
        val storeProducts = CatalogData.products.filter { it.storeId == order.storeId }
        val targetProduct = storeProducts.firstOrNull() ?: CatalogData.products.first()
        addToCart(targetProduct, 1, "إعادة طلب سابق #${order.id}")
        navigateTo(AppScreen.CART_CHECKOUT)
    }

    // 24/7 Support Chat
    fun sendSupportMessage(userText: String) {
        if (userText.isBlank()) return

        val userMsg = SupportChatMessage(
            id = "msg_${System.currentTimeMillis()}",
            text = userText,
            isUser = true
        )
        _supportMessages.value = _supportMessages.value + userMsg

        viewModelScope.launch {
            delay(1000)
            val lang = _selectedLanguage.value
            val responseText = when {
                userText.contains("أين طلبي") || userText.contains("où est") || userText.contains("where") -> {
                    when (lang) {
                        AppLanguage.ARABIC -> "أهلاً بك! يمكنك تتبع طلبك لحظة بلحظة عبر قسم \"التتبع الفوري\" على الخريطة مباشرة لمعرفة موقع المندوب عزيز بدقة."
                        AppLanguage.FRENCH -> "Vous pouvez suivre la position exacte du coursier Aziz en temps réel dans l'onglet 'Suivi en direct'."
                        AppLanguage.ENGLISH -> "You can track courier Aziz live in real-time in the 'Live Tracking' screen on the map."
                    }
                }
                userText.contains("سائق") || userText.contains("تفعيل") || userText.contains("driver") -> {
                    when (lang) {
                        AppLanguage.ARABIC -> "فريق الدعم الفني يراجع طلبات انضمام السائقين الجديدة فوراً! تم إعطاء الصلاحية للدعم لتفعيل حسابك فور استيفاء رخصة القيادة والبطاقة."
                        AppLanguage.FRENCH -> "L'équipe support valide immédiatement les nouveaux chauffeurs éligibles !"
                        AppLanguage.ENGLISH -> "Support team instantly verifies and activates eligible driver accounts!"
                    }
                }
                userText.contains("دفع") || userText.contains("paiement") || userText.contains("pay") -> {
                    when (lang) {
                        AppLanguage.ARABIC -> "نظام الدفع في ألو عزيز آمن 100% ومشفر. نقبل البطاقات البنكية، محفظة ALLO Pay، والدفع نقداً عند الاستلام."
                        AppLanguage.FRENCH -> "Le paiement est 100% sécurisé (cartes bancaires, portefeuille ALLO Pay ou paiement en espèces)."
                        AppLanguage.ENGLISH -> "Payment is 100% encrypted & secure. We accept cards, ALLO Pay wallet, and Cash on Delivery."
                    }
                }
                userText.contains("كود") || userText.contains("خصم") || userText.contains("promo") || userText.contains("discount") -> {
                    when (lang) {
                        AppLanguage.ARABIC -> "كود الخصم الحصري لك اليوم هو: AZIZ20 يمنحك خصماً فورياً 20% على طلبك!"
                        AppLanguage.FRENCH -> "Votre code promo du jour est AZIZ20 pour -20% immédiats !"
                        AppLanguage.ENGLISH -> "Your exclusive promo code is AZIZ20 for an instant 20% off!"
                    }
                }
                else -> {
                    when (lang) {
                        AppLanguage.ARABIC -> "شكراً لتواصلك مع ألو عزيز! فريق الدعم الفني 24/7 يتابع استفسارك باهتمام وسنقوم بالرد فوراً."
                        AppLanguage.FRENCH -> "Merci de contacter ALLO AZIZ ! Notre support 24/7 est à votre disposition."
                        AppLanguage.ENGLISH -> "Thank you for contacting ALLO AZIZ! Our 24/7 team is here to assist you."
                    }
                }
            }

            val botMsg = SupportChatMessage(
                id = "bot_${System.currentTimeMillis()}",
                text = responseText,
                isUser = false,
                agentName = "سارة - دعم ألو عزيز"
            )
            _supportMessages.value = _supportMessages.value + botMsg
        }
    }

    fun topUpWallet(amount: Double) {
        viewModelScope.launch {
            repository.topUpWallet(amount)
            repository.addNotification(
                NotificationEntity(
                    id = "notif_topup_${System.currentTimeMillis()}",
                    titleAr = "شحن المحفظة بنجاح 💳",
                    titleFr = "Portefeuille rechargé 💳",
                    titleEn = "Wallet Topped Up 💳",
                    bodyAr = "تمت إضافة $amount د.م إلى محفظة ALLO Pay بنجاح.",
                    bodyFr = "$amount DH ajoutés avec succès à votre portefeuille ALLO Pay.",
                    bodyEn = "$amount MAD successfully added to your ALLO Pay balance.",
                    timestamp = System.currentTimeMillis(),
                    type = "WALLET"
                )
            )
            _userAlertMessage.value = when (_selectedLanguage.value) {
                AppLanguage.ARABIC -> "تم شحن المحفظة بمبلغ $amount د.م بنجاح! 💳"
                AppLanguage.FRENCH -> "Recharge de $amount DH réussie ! 💳"
                AppLanguage.ENGLISH -> "Successfully recharged $amount MAD! 💳"
            }
        }
    }

    fun markNotificationRead(id: String) {
        viewModelScope.launch {
            repository.markNotificationRead(id)
        }
    }

    fun markAllNotificationsRead() {
        viewModelScope.launch {
            repository.markAllNotificationsRead()
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
            else -> ExternalShareHelper.getSavedWhatsAppNumber(context)
        }
        if (ExternalShareHelper.isPlaceholderOrInvalid(targetPhone)) {
            return false
        }
        return ExternalShareHelper.openWhatsApp(
            context = context,
            phoneE164 = targetPhone,
            prefilledText = "السلام عليكم كابتن عزيز، أنا العميل بخصوص طلبي #${order.id} من متجر ${order.storeName} - العنوان: ${order.deliveryAddress}."
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
            prefilledText = "السلام عليكم، أحتاج مساعدة ودعم فني من إدارة ألو عزيز بخصوص تطبيق التوصيل."
        )
    }

    // Local UserProfile & Preferences Management
    fun saveUserProfile(profile: UserProfile) {
        viewModelScope.launch {
            repository.saveUserProfile(profile)
            _userAlertMessage.value = "تم حفظ الملف الشخصي وتفضيلات التوصيل محلياً بنجاح! 💾"
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
            val current = userProfile.value ?: UserProfile()
            val updated = current.copy(
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
