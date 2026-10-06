package com.example.data.local

import com.example.data.auth.PasswordHasher
import com.example.data.model.CatalogData
import kotlinx.coroutines.flow.Flow
import java.util.UUID

class AlloAzizRepository(private val database: AppDatabase) {

    val allUsers: Flow<List<UserAccountEntity>> = database.userDao().getAllUsers()
    val pendingUsers: Flow<List<UserAccountEntity>> = database.userDao().getPendingUsers()
    val pendingPartnerApplications: Flow<List<UserAccountEntity>> = database.userDao().getPendingPartnerApplications()
    val allStores: Flow<List<StoreEntity>> = database.storeDao().getAllStores()
    val allProducts: Flow<List<ProductEntity>> = database.productDao().getAllProducts()
    val allOrders: Flow<List<OrderEntity>> = database.orderDao().getAllOrders()
    val activeOrder: Flow<OrderEntity?> = database.orderDao().getActiveOrder()
    val allNotifications: Flow<List<NotificationEntity>> = database.notificationDao().getAllNotifications()
    val unreadCount: Flow<Int> = database.notificationDao().getUnreadCount()
    val allReviews: Flow<List<ReviewEntity>> = database.reviewDao().getAllReviews()
    val wallet: Flow<WalletProfileEntity?> = database.walletDao().getWallet()
    val allTickets: Flow<List<SupportTicketEntity>> = database.supportTicketDao().getAllTickets()
    val allAddresses: Flow<List<AddressEntity>> = database.addressDao().getAddressesForUser("usr_customer_1")

    suspend fun insertAddress(address: AddressEntity) {
        database.addressDao().insertAddress(address)
    }

    suspend fun setDefaultAddress(userId: String, addressId: String) {
        database.addressDao().clearDefaultAddresses(userId)
        database.addressDao().setDefaultAddress(userId, addressId)
    }

    suspend fun deleteAddress(addressId: String) {
        database.addressDao().deleteAddress(addressId)
    }

    suspend fun getUserByEmail(email: String): UserAccountEntity? = database.userDao().getUserByEmail(email)
    suspend fun getUserByPhone(phone: String): UserAccountEntity? = database.userDao().getUserByPhone(phone)
    suspend fun getUserByIdentifier(identifier: String): UserAccountEntity? = database.userDao().getUserByIdentifier(identifier)

    fun getOrdersByStore(storeId: String): Flow<List<OrderEntity>> = database.orderDao().getOrdersByStore(storeId)
    fun getOrdersForDriver(driverId: String): Flow<List<OrderEntity>> = database.orderDao().getOrdersForDriver(driverId)
    fun getStoresByOwner(ownerId: String): Flow<List<StoreEntity>> = database.storeDao().getStoresByOwner(ownerId)
    fun getProductsByStore(storeId: String): Flow<List<ProductEntity>> = database.productDao().getProductsByStore(storeId)

    // User Management
    suspend fun insertUser(user: UserAccountEntity) {
        database.userDao().insertUser(user)
    }

    suspend fun updateUserStatus(userId: String, status: String) {
        database.userDao().updateUserStatus(userId, status)
    }

    suspend fun updateUserRole(userId: String, role: String) {
        database.userDao().updateUserRole(userId, role)
    }

    suspend fun deleteUser(userId: String) {
        database.userDao().deleteUser(userId)
    }

    // Store & Product Management
    suspend fun insertStore(store: StoreEntity) {
        database.storeDao().insertStore(store)
    }

    suspend fun updateStore(store: StoreEntity) {
        database.storeDao().updateStore(store)
    }

    suspend fun toggleStoreOpen(storeId: String, isOpen: Boolean) {
        database.storeDao().toggleStoreOpen(storeId, isOpen)
    }

    suspend fun deleteStore(storeId: String) {
        database.storeDao().deleteStore(storeId)
    }

    suspend fun insertProduct(product: ProductEntity) {
        database.productDao().insertProduct(product)
    }

    suspend fun updateProduct(product: ProductEntity) {
        database.productDao().updateProduct(product)
    }

    suspend fun toggleProductAvailability(productId: String, isAvailable: Boolean) {
        database.productDao().toggleAvailability(productId, isAvailable)
    }

    suspend fun deleteProduct(productId: String) {
        database.productDao().deleteProduct(productId)
    }

    // Orders
    suspend fun insertOrder(order: OrderEntity) {
        database.orderDao().insertOrder(order)
    }

    suspend fun updateOrderStatus(orderId: String, status: String, progress: Float, minsLeft: Int) {
        database.orderDao().updateOrderStatus(orderId, status, progress, minsLeft)
    }

    suspend fun updateCourierGps(orderId: String, lat: Double, lng: Double, progress: Float, minsLeft: Int) {
        database.orderDao().updateCourierGps(orderId, lat, lng, progress, minsLeft)
    }

    suspend fun updateRestaurantStatus(orderId: String, restStatus: String, orderStatus: String) {
        database.orderDao().updateRestaurantStatus(orderId, restStatus, orderStatus)
    }

    suspend fun assignDriverToOrder(orderId: String, driverId: String, driverName: String) {
        database.orderDao().assignDriverToOrder(orderId, driverId, driverName)
    }

    suspend fun markOrderReviewed(orderId: String) {
        database.orderDao().markOrderReviewed(orderId)
    }

    // Reviews & Notifications & Tickets
    suspend fun addReview(review: ReviewEntity) {
        database.reviewDao().insertReview(review)
    }

    suspend fun addNotification(notification: NotificationEntity) {
        database.notificationDao().insertNotification(notification)
    }

    suspend fun markNotificationRead(id: String) {
        database.notificationDao().markAsRead(id)
    }

    suspend fun markAllNotificationsRead() {
        database.notificationDao().markAllAsRead()
    }

    suspend fun topUpWallet(amount: Double) {
        database.walletDao().topUp(amount)
    }

    suspend fun deductWallet(amount: Double) {
        database.walletDao().deduct(amount)
    }

    suspend fun insertTicket(ticket: SupportTicketEntity) {
        database.supportTicketDao().insertTicket(ticket)
    }

    suspend fun updateTicketStatus(ticketId: String, status: String) {
        database.supportTicketDao().updateTicketStatus(ticketId, status)
    }

    suspend fun initializeDefaultDataIfEmpty() {
        // 1. Seed Multi-role accounts with SHA-256 hashed passwords
        val defaultHashedPassword = PasswordHasher.hashPassword("abdomery")

        val defaultUsers = listOf(
            UserAccountEntity(
                id = "usr_customer_1",
                name = "كريم التازي",
                email = "karim.tazi@gmail.com",
                phone = "+212 611-223344",
                password = defaultHashedPassword,
                role = "CUSTOMER",
                status = "ACTIVE",
                balance = 120.0,
                avatarEmoji = "🛍️"
            ),
            UserAccountEntity(
                id = "owner_mcdo",
                name = "الشيف عمر - مدير ماكدونالدز",
                email = "partner.mcdo@alloaziz.ma",
                phone = "+212 522-889900",
                password = defaultHashedPassword,
                role = "RESTAURANT_OWNER",
                status = "ACTIVE",
                restaurantId = "store_mcdonalds",
                balance = 3450.0,
                totalEarnings = 18200.0,
                avatarEmoji = "👨‍🍳"
            ),
            UserAccountEntity(
                id = "owner_tajine",
                name = "الحاج عبد القادر - طاجين فاس",
                email = "tajine.fes@alloaziz.ma",
                phone = "+212 535-445566",
                password = defaultHashedPassword,
                role = "RESTAURANT_OWNER",
                status = "ACTIVE",
                restaurantId = "store_tajine",
                balance = 2800.0,
                totalEarnings = 14500.0,
                avatarEmoji = "🍲"
            ),
            UserAccountEntity(
                id = "drv_aziz",
                name = "عزيز برادة (الكابتن عزيز)",
                email = "aziz.courier@alloaziz.ma",
                phone = "+212 661-234567",
                password = defaultHashedPassword,
                role = "DRIVER",
                status = "ACTIVE",
                vehicleType = "دراجة نارية هوندا 125cc",
                plateNumber = "أ - 54921",
                balance = 480.0,
                totalEarnings = 3200.0,
                rating = 4.95,
                avatarEmoji = "🛵"
            ),
            UserAccountEntity(
                id = "drv_pending_1",
                name = "طارق المهدوي (سائق جديد)",
                email = "tarik.driver@gmail.com",
                phone = "+212 677-998877",
                password = defaultHashedPassword,
                role = "DRIVER",
                status = "PENDING_APPROVAL",
                vehicleType = "سكوتر ياماها 150cc",
                plateNumber = "ب - 11029",
                balance = 0.0,
                avatarEmoji = "🛵"
            ),
            UserAccountEntity(
                id = "owner_pending_1",
                name = "رشيد بنجلون (شريك مطعم جديد)",
                email = "rachid.benn@gmail.com",
                phone = "+212 662-887766",
                password = defaultHashedPassword,
                role = "RESTAURANT_OWNER",
                status = "PENDING_APPROVAL",
                restaurantName = "مخبزة وفطائر السلام - العالية",
                balance = 0.0,
                avatarEmoji = "👨‍🍳"
            ),
            UserAccountEntity(
                id = "usr_support_1",
                name = "سارة المنصوري (مشرفة الدعم)",
                email = "sara.support@alloaziz.ma",
                phone = "+212 520-112233",
                password = defaultHashedPassword,
                role = "SUPPORT_AGENT",
                status = "ACTIVE",
                avatarEmoji = "🎧"
            ),
            UserAccountEntity(
                id = "usr_super_admin",
                name = "المدير العام (Super Admin)",
                email = "mcromeyox@gmail.com",
                phone = "+212 600-000001",
                password = defaultHashedPassword,
                role = "SUPER_ADMIN",
                status = "ACTIVE",
                balance = 150000.0,
                avatarEmoji = "👑",
                authProvider = "EMAIL"
            )
        )

        for (u in defaultUsers) {
            database.userDao().insertUser(u)
        }

        // Always guarantee super admin with requested credentials is active and hashed
        database.userDao().insertUser(
            UserAccountEntity(
                id = "usr_super_admin",
                name = "المدير العام (Super Admin)",
                email = "mcromeyox@gmail.com",
                phone = "+212 600-000001",
                password = defaultHashedPassword,
                role = "SUPER_ADMIN",
                status = "ACTIVE",
                balance = 150000.0,
                avatarEmoji = "👑",
                authProvider = "EMAIL"
            )
        )

        // 2. Seed Stores into Room database
        for (st in CatalogData.stores) {
            database.storeDao().insertStore(
                StoreEntity(
                    id = st.id,
                    name = st.name,
                    nameAr = st.nameAr,
                    nameFr = st.nameFr,
                    category = st.category.id,
                    rating = st.rating,
                    reviewCount = st.reviewCount,
                    deliveryTimeMins = st.deliveryTimeMins,
                    deliveryFee = st.deliveryFee,
                    minOrder = st.minOrder,
                    address = st.address,
                    emoji = st.emoji,
                    isFeatured = st.isFeatured,
                    badge = st.badge,
                    isOpen = st.isOpen,
                    ownerId = st.ownerId
                )
            )
        }

        // 3. Seed Products into Room database
        for (p in CatalogData.products) {
            database.productDao().insertProduct(
                ProductEntity(
                    id = p.id,
                    storeId = p.storeId,
                    storeName = p.storeName,
                    nameEn = p.nameEn,
                    nameAr = p.nameAr,
                    nameFr = p.nameFr,
                    descriptionEn = p.descriptionEn,
                    descriptionAr = p.descriptionAr,
                    descriptionFr = p.descriptionFr,
                    price = p.price,
                    category = p.category.id,
                    emoji = p.emoji,
                    rating = p.rating,
                    isPopular = p.isPopular,
                    calories = p.calories,
                    isAvailable = p.isAvailable
                )
            )
        }

        // 4. Seed Support Tickets
        val sampleTickets = listOf(
            SupportTicketEntity(
                id = "TCK-801",
                customerName = "طارق المهدوي (سائق)",
                subject = "طلب تفعيل حساب سائق جديد ورفع الوثائق",
                status = "OPEN",
                assignedAgent = "سارة المنصوري",
                lastMessage = "أرفقت رخصة القيادة والبطاقة الوطنية، أرجو تفعيل الحساب للبدء في العمل."
            ),
            SupportTicketEntity(
                id = "TCK-802",
                customerName = "مريم الوهابي (عميل)",
                subject = "استفسار بخصوص خصم كود AZIZ20",
                status = "RESOLVED",
                assignedAgent = "سارة المنصوري",
                lastMessage = "تم تفعيل الخصم 20% بنجاح وشكراً لتعاونكم السريع."
            )
        )
        for (t in sampleTickets) {
            database.supportTicketDao().insertTicket(t)
        }

        // 5. Seed Wallet & Notifications & Reviews
        database.walletDao().saveWallet(
            WalletProfileEntity(
                id = 1,
                balance = 120.0,
                points = 350,
                defaultAddress = "حي الرياض، شارع النخيل - الدار البيضاء"
            )
        )

        database.notificationDao().insertNotification(
            NotificationEntity(
                id = "notif_welcome",
                titleAr = "مرحباً بك في ألو عزيز! 🎉",
                titleFr = "Bienvenue sur ALLO AZIZ ! 🎉",
                titleEn = "Welcome to ALLO AZIZ! 🎉",
                bodyAr = "أسرع منصة توصيل تربط العملاء، المطاعم، والسائقين بنظام احترافي متكامل!",
                bodyFr = "La plateforme de livraison N°1 connectant clients, restaurants et livreurs !",
                bodyEn = "The #1 delivery ecosystem connecting customers, restaurants and couriers!",
                timestamp = System.currentTimeMillis() - 3600000,
                isRead = false,
                type = "PROMO"
            )
        )

        database.notificationDao().insertNotification(
            NotificationEntity(
                id = "notif_wallet_gift",
                titleAr = "هدية رصيد مجاني 🎁",
                titleFr = "Cadeau de bienvenue 🎁",
                titleEn = "Welcome Gift Balance 🎁",
                bodyAr = "تمت إضافة 120 د.م في محفظة ALLO Pay مع كود AZIZ20 لخصم 20%!",
                bodyFr = "120 DH ont été ajoutés à votre portefeuille ALLO Pay !",
                bodyEn = "120 MAD added to your ALLO Pay wallet to enjoy fast delivery!",
                timestamp = System.currentTimeMillis() - 1800000,
                isRead = false,
                type = "WALLET"
            )
        )

        database.reviewDao().insertReview(
            ReviewEntity(
                id = "rev_1",
                orderId = "AZ-1011",
                storeName = "ماكدونالدز (McDonald's)",
                customerName = "ياسين المنصوري",
                storeRating = 5,
                courierRating = 5,
                tagsJson = "[\"توصيل سريع\", \"أكل ساخن ولذيذ\", \"تغليف محكم\"]",
                comment = "وصل البيج ماك والبطاطس ساخنة ومقرمشة في 17 دقيقة فقط مع الكابتن عزيز! تطبيق مذهل يفوق جميع المنافسين.",
                dateString = "اليوم، 13:45"
            )
        )

        database.reviewDao().insertReview(
            ReviewEntity(
                id = "rev_2",
                orderId = "AZ-1022",
                storeName = "طاجين فاس الملكي",
                customerName = "فاطمة الزهراء",
                storeRating = 5,
                courierRating = 5,
                tagsJson = "[\"معاملة ممتازة\", \"تتبع مباشر دقيق\"]",
                comment = "التتبع المباشر للخريطة ممتع جداً ودقيق بالثانية. المندوب مهذب جداً والدفع بالبطاقة كان سريعاً وآمناً.",
                dateString = "أمس، 20:10"
            )
        )

        // 7. Seed Default Mohammedia Addresses
        database.addressDao().insertAddress(
            AddressEntity(
                id = "addr_home_1",
                userId = "usr_customer_1",
                title = "المنزل (المحمدية)",
                type = "HOME",
                city = "المحمدية",
                neighborhood = "حي الوفاء",
                streetDetails = "شارع المقاومة، إقامة الياسمين، عمارة ب شقة 4",
                deliveryNotes = "رن الجرس مرتين، الطابق الأول",
                isDefault = true,
                latitude = 33.6845,
                longitude = -7.3820
            )
        )
        database.addressDao().insertAddress(
            AddressEntity(
                id = "addr_work_1",
                userId = "usr_customer_1",
                title = "مكتب العمل (وسط المدينة)",
                type = "WORK",
                city = "المحمدية",
                neighborhood = "وسط المدينة",
                streetDetails = "شارع الحسن الثاني، قرب حديقة المدن المتوأمة، عمارة التوفيق",
                deliveryNotes = "اتصل عند الوصول للباب الرئيسي",
                isDefault = false,
                latitude = 33.7012,
                longitude = -7.3910
            )
        )
        database.addressDao().insertAddress(
            AddressEntity(
                id = "addr_beach_1",
                userId = "usr_customer_1",
                title = "شقة الكورنيش (ميرامار)",
                type = "OTHER",
                city = "المحمدية",
                neighborhood = "حي ميرامار",
                streetDetails = "شارع الكورنيش، إقامة شاطئ المحمدية بلوك ج",
                deliveryNotes = "التسليم عند حارس العمارة",
                isDefault = false,
                latitude = 33.7120,
                longitude = -7.3750
            )
        )
    }
}
