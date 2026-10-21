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
    // Always query addresses for the authenticated account, not a hard-coded demo user.
    fun addressesForUser(userId: String): Flow<List<AddressEntity>> = database.addressDao().getAddressesForUser(userId)
    val userProfile: Flow<UserProfile?> = database.userProfileDao().getDefaultUserProfile()

    fun getUserProfile(userId: String): Flow<UserProfile?> = database.userProfileDao().getUserProfile(userId)
    suspend fun getUserProfileOnce(userId: String): UserProfile? = database.userProfileDao().getUserProfileOnce(userId)

    suspend fun saveUserProfile(profile: UserProfile) {
        database.userProfileDao().saveUserProfile(profile)
    }

    suspend fun updateDeliveryAddress(
        userId: String,
        address: String,
        city: String,
        neighborhood: String,
        buildingInfo: String,
        notes: String
    ) {
        database.userProfileDao().updateDeliveryAddress(
            userId = userId,
            address = address,
            city = city,
            neighborhood = neighborhood,
            buildingInfo = buildingInfo,
            notes = notes
        )
    }

    suspend fun updatePreferences(
        userId: String,
        language: String,
        paymentMethod: String,
        contactless: Boolean,
        cutlery: Boolean,
        pushNotifs: Boolean
    ) {
        database.userProfileDao().updatePreferences(
            userId = userId,
            language = language,
            paymentMethod = paymentMethod,
            contactless = contactless,
            cutlery = cutlery,
            pushNotifs = pushNotifs
        )
    }

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
    fun getOrdersForCustomer(customerId: String): Flow<List<OrderEntity>> = database.orderDao().getOrdersForCustomer(customerId)
    fun getActiveOrderForCustomer(customerId: String): Flow<OrderEntity?> = database.orderDao().getActiveOrderForCustomer(customerId)
    fun getOrdersForStoreOwner(ownerId: String): Flow<List<OrderEntity>> = database.orderDao().getOrdersForStoreOwner(ownerId)
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

    suspend fun assignDriverToOrder(orderId: String, driverId: String, driverName: String, driverPhone: String = "") {
        database.orderDao().assignDriverToOrder(orderId, driverId, driverName, driverPhone)
    }

    suspend fun updateCourierPhone(orderId: String, phone: String) {
        database.orderDao().updateCourierPhone(orderId, phone)
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
        if (database.storeDao().getStoreCount() > 0) {
            // High-Speed Launch: Database is already primed, skip heavy seeding for ultra-fast startup!
            return
        }

        // Seeded demo accounts receive unique random passwords that are never disclosed.
        // Real staff accounts must be provisioned by a trusted authentication backend.

        // No demo customer or staff accounts are provisioned locally.


        // 2. Batch seed Stores into Room database
        database.storeDao().insertStores(CatalogData.stores.map { st ->
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
        })

        // 3. Batch seed Products into Room database
        database.productDao().insertProducts(CatalogData.products.map { p ->
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
        })

        // Do not seed fictitious tickets, reviews, addresses, balances, or customer profiles.
    }
}
