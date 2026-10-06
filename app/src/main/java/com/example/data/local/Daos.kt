package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface UserAccountDao {
    @Query("SELECT * FROM users ORDER BY createdAt DESC")
    fun getAllUsers(): Flow<List<UserAccountEntity>>

    @Query("SELECT * FROM users WHERE role = :role ORDER BY createdAt DESC")
    fun getUsersByRole(role: String): Flow<List<UserAccountEntity>>

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    fun getUserById(id: String): Flow<UserAccountEntity?>

    @Query("SELECT * FROM users WHERE LOWER(email) = LOWER(:email) LIMIT 1")
    suspend fun getUserByEmail(email: String): UserAccountEntity?

    @Query("SELECT * FROM users WHERE phone = :phone LIMIT 1")
    suspend fun getUserByPhone(phone: String): UserAccountEntity?

    @Query("SELECT * FROM users WHERE (LOWER(email) = LOWER(:identifier) OR phone = :identifier) LIMIT 1")
    suspend fun getUserByIdentifier(identifier: String): UserAccountEntity?

    @Query("SELECT * FROM users WHERE status = 'PENDING_APPROVAL' ORDER BY createdAt DESC")
    fun getPendingUsers(): Flow<List<UserAccountEntity>>

    @Query("SELECT * FROM users WHERE status = 'PENDING_APPROVAL' AND role IN ('DRIVER', 'RESTAURANT_OWNER') ORDER BY createdAt DESC")
    fun getPendingPartnerApplications(): Flow<List<UserAccountEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserAccountEntity)

    @Query("UPDATE users SET status = :status WHERE id = :userId")
    suspend fun updateUserStatus(userId: String, status: String)

    @Query("UPDATE users SET role = :newRole WHERE id = :userId")
    suspend fun updateUserRole(userId: String, newRole: String)

    @Query("DELETE FROM users WHERE id = :userId")
    suspend fun deleteUser(userId: String)
}

@Dao
interface StoreDao {
    @Query("SELECT * FROM stores ORDER BY isFeatured DESC, rating DESC")
    fun getAllStores(): Flow<List<StoreEntity>>

    @Query("SELECT * FROM stores WHERE ownerId = :ownerId")
    fun getStoresByOwner(ownerId: String): Flow<List<StoreEntity>>

    @Query("SELECT * FROM stores WHERE id = :storeId LIMIT 1")
    fun getStoreById(storeId: String): Flow<StoreEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStore(store: StoreEntity)

    @Update
    suspend fun updateStore(store: StoreEntity)

    @Query("UPDATE stores SET isOpen = :isOpen WHERE id = :storeId")
    suspend fun toggleStoreOpen(storeId: String, isOpen: Boolean)

    @Query("DELETE FROM stores WHERE id = :storeId")
    suspend fun deleteStore(storeId: String)
}

@Dao
interface ProductDao {
    @Query("SELECT * FROM products ORDER BY isPopular DESC, rating DESC")
    fun getAllProducts(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE storeId = :storeId ORDER BY isPopular DESC")
    fun getProductsByStore(storeId: String): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE id = :productId LIMIT 1")
    suspend fun getProductById(productId: String): ProductEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: ProductEntity)

    @Update
    suspend fun updateProduct(product: ProductEntity)

    @Query("UPDATE products SET isAvailable = :isAvailable WHERE id = :productId")
    suspend fun toggleAvailability(productId: String, isAvailable: Boolean)

    @Query("DELETE FROM products WHERE id = :productId")
    suspend fun deleteProduct(productId: String)
}

@Dao
interface OrderDao {
    @Query("SELECT * FROM orders ORDER BY timestamp DESC")
    fun getAllOrders(): Flow<List<OrderEntity>>

    @Query("SELECT * FROM orders WHERE id = :orderId LIMIT 1")
    fun getOrderById(orderId: String): Flow<OrderEntity?>

    @Query("SELECT * FROM orders WHERE status != 'DELIVERED' ORDER BY timestamp DESC LIMIT 1")
    fun getActiveOrder(): Flow<OrderEntity?>

    @Query("SELECT * FROM orders WHERE storeId = :storeId ORDER BY timestamp DESC")
    fun getOrdersByStore(storeId: String): Flow<List<OrderEntity>>

    @Query("SELECT * FROM orders WHERE driverId = :driverId OR (driverId IS NULL AND status IN ('PREPARING', 'ON_THE_WAY')) ORDER BY timestamp DESC")
    fun getOrdersForDriver(driverId: String): Flow<List<OrderEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: OrderEntity)

    @Update
    suspend fun updateOrder(order: OrderEntity)

    @Query("UPDATE orders SET status = :status, courierProgress = :progress, estimatedMinsLeft = :minsLeft WHERE id = :orderId")
    suspend fun updateOrderStatus(orderId: String, status: String, progress: Float, minsLeft: Int)

    @Query("UPDATE orders SET courierLat = :lat, courierLng = :lng, courierProgress = :progress, estimatedMinsLeft = :minsLeft, status = 'ON_THE_WAY' WHERE id = :orderId")
    suspend fun updateCourierGps(orderId: String, lat: Double, lng: Double, progress: Float, minsLeft: Int)

    @Query("UPDATE orders SET restaurantStatus = :restStatus, status = :orderStatus WHERE id = :orderId")
    suspend fun updateRestaurantStatus(orderId: String, restStatus: String, orderStatus: String)

    @Query("UPDATE orders SET driverId = :driverId, courierName = :driverName, status = 'ON_THE_WAY', courierProgress = 0.5 WHERE id = :orderId")
    suspend fun assignDriverToOrder(orderId: String, driverId: String, driverName: String)

    @Query("UPDATE orders SET hasReviewed = 1 WHERE id = :orderId")
    suspend fun markOrderReviewed(orderId: String)
}

@Dao
interface ReviewDao {
    @Query("SELECT * FROM reviews ORDER BY timestamp DESC")
    fun getAllReviews(): Flow<List<ReviewEntity>>

    @Query("SELECT * FROM reviews WHERE storeName = :storeName ORDER BY timestamp DESC")
    fun getReviewsForStore(storeName: String): Flow<List<ReviewEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReview(review: ReviewEntity)
}

@Dao
interface NotificationDao {
    @Query("SELECT * FROM notifications ORDER BY timestamp DESC")
    fun getAllNotifications(): Flow<List<NotificationEntity>>

    @Query("SELECT COUNT(*) FROM notifications WHERE isRead = 0")
    fun getUnreadCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: NotificationEntity)

    @Query("UPDATE notifications SET isRead = 1 WHERE id = :id")
    suspend fun markAsRead(id: String)

    @Query("UPDATE notifications SET isRead = 1")
    suspend fun markAllAsRead()
}

@Dao
interface WalletDao {
    @Query("SELECT * FROM wallet_profile WHERE id = 1 LIMIT 1")
    fun getWallet(): Flow<WalletProfileEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveWallet(wallet: WalletProfileEntity)

    @Query("UPDATE wallet_profile SET balance = balance + :amount WHERE id = 1")
    suspend fun topUp(amount: Double)

    @Query("UPDATE wallet_profile SET balance = balance - :amount WHERE id = 1")
    suspend fun deduct(amount: Double)
}

@Dao
interface SupportTicketDao {
    @Query("SELECT * FROM support_tickets ORDER BY timestamp DESC")
    fun getAllTickets(): Flow<List<SupportTicketEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTicket(ticket: SupportTicketEntity)

    @Query("UPDATE support_tickets SET status = :status WHERE id = :ticketId")
    suspend fun updateTicketStatus(ticketId: String, status: String)
}

@Dao
interface AddressDao {
    @Query("SELECT * FROM addresses WHERE userId = :userId ORDER BY isDefault DESC")
    fun getAddressesForUser(userId: String): Flow<List<AddressEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAddress(address: AddressEntity)

    @Query("UPDATE addresses SET isDefault = 0 WHERE userId = :userId")
    suspend fun clearDefaultAddresses(userId: String)

    @Query("UPDATE addresses SET isDefault = 1 WHERE id = :addressId AND userId = :userId")
    suspend fun setDefaultAddress(userId: String, addressId: String)

    @Query("DELETE FROM addresses WHERE id = :addressId")
    suspend fun deleteAddress(addressId: String)
}
