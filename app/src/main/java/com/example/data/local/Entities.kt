package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserAccountEntity(
    @PrimaryKey val id: String,
    val name: String,
    val email: String,
    val phone: String,
    val password: String = "abdomery",
    val role: String, // CUSTOMER, RESTAURANT_OWNER, DRIVER, SUPPORT_AGENT, SUPER_ADMIN
    val status: String = "PENDING_APPROVAL", // ACTIVE, PENDING_APPROVAL, SUSPENDED
    val restaurantId: String? = null,
    val restaurantName: String? = null,
    val vehicleType: String? = null,
    val plateNumber: String? = null,
    val balance: Double = 0.0,
    val totalEarnings: Double = 0.0,
    val rating: Double = 4.9,
    val avatarEmoji: String = "👤",
    val authProvider: String = "EMAIL", // GOOGLE, PHONE, EMAIL
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "stores")
data class StoreEntity(
    @PrimaryKey val id: String,
    val name: String,
    val nameAr: String,
    val nameFr: String,
    val category: String,
    val rating: Double = 4.8,
    val reviewCount: Int = 120,
    val deliveryTimeMins: Int = 20,
    val deliveryFee: Double = 8.0,
    val minOrder: Double = 30.0,
    val address: String,
    val emoji: String,
    val isFeatured: Boolean = false,
    val badge: String = "",
    val isOpen: Boolean = true,
    val ownerId: String = "owner_default",
    val bannerUrl: String = "",
    val distanceKm: Double = 1.8
)

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey val id: String,
    val storeId: String,
    val storeName: String,
    val nameEn: String,
    val nameAr: String,
    val nameFr: String,
    val descriptionEn: String,
    val descriptionAr: String,
    val descriptionFr: String,
    val price: Double,
    val category: String,
    val emoji: String,
    val rating: Double = 4.8,
    val isPopular: Boolean = false,
    val calories: String = "450 kcal",
    val isAvailable: Boolean = true,
    val imageUrl: String = ""
)

@Entity(tableName = "orders")
data class OrderEntity(
    @PrimaryKey val id: String,
    val storeId: String,
    val storeName: String,
    val itemsSummary: String,
    val itemsCount: Int,
    val subtotal: Double,
    val deliveryFee: Double,
    val discount: Double,
    val total: Double,
    val status: String, // RECEIVED, PREPARING, ON_THE_WAY, ARRIVED, DELIVERED
    val restaurantStatus: String = "ACCEPTED", // PENDING, COOKING, READY_FOR_PICKUP
    val driverId: String? = null,
    val paymentMethod: String,
    val deliveryAddress: String,
    val deliveryNotes: String,
    val timestamp: Long,
    val courierName: String,
    val courierPhone: String,
    val courierRating: Double,
    val courierVehicle: String,
    val courierProgress: Float, // 0.0 to 1.0 along route
    val estimatedMinsLeft: Int,
    val hasReviewed: Boolean = false,
    val storeLat: Double = 33.6930,
    val storeLng: Double = -7.3820,
    val customerLat: Double = 33.6835,
    val customerLng: Double = -7.3849,
    val courierLat: Double = 33.6900,
    val courierLng: Double = -7.3835
)

@Entity(tableName = "reviews")
data class ReviewEntity(
    @PrimaryKey val id: String,
    val orderId: String,
    val storeName: String,
    val customerName: String,
    val storeRating: Int,
    val courierRating: Int,
    val tagsJson: String,
    val comment: String,
    val dateString: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey val id: String,
    val titleAr: String,
    val titleFr: String,
    val titleEn: String,
    val bodyAr: String,
    val bodyFr: String,
    val bodyEn: String,
    val timestamp: Long,
    val isRead: Boolean = false,
    val type: String = "ORDER",
    val orderId: String? = null
)

@Entity(tableName = "wallet_profile")
data class WalletProfileEntity(
    @PrimaryKey val id: Int = 1,
    val balance: Double = 0.0,
    val points: Int = 0,
    val defaultAddress: String = ""
)

@Entity(tableName = "support_tickets")
data class SupportTicketEntity(
    @PrimaryKey val id: String,
    val customerName: String,
    val subject: String,
    val status: String = "OPEN", // OPEN, IN_PROGRESS, RESOLVED
    val assignedAgent: String = "سارة المنصوري",
    val lastMessage: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "addresses")
data class AddressEntity(
    @PrimaryKey val id: String,
    val userId: String = "usr_customer_1",
    val title: String, // "المنزل", "العمل", "مكتب المحمدية"
    val type: String = "HOME", // HOME, WORK, OTHER
    val city: String = "المحمدية",
    val neighborhood: String,
    val streetDetails: String,
    val buildingInfo: String = "",
    val deliveryNotes: String = "",
    val isDefault: Boolean = false,
    val latitude: Double = 33.6835,
    val longitude: Double = -7.3849
)
