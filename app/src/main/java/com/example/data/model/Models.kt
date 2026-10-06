package com.example.data.model

enum class UserRole(
    val titleAr: String,
    val titleFr: String,
    val titleEn: String,
    val emoji: String,
    val badgeColorHex: Long
) {
    CUSTOMER("زبون / عميل", "Client", "Customer", "🛍️", 0xFFFF5722),
    RESTAURANT_OWNER("صاحب مطعم / شريك", "Restaurateur", "Restaurant Partner", "👨‍🍳", 0xFFE65100),
    DRIVER("سائق / مندوب توصيل", "Livreur", "Courier Driver", "🛵", 0xFF00897B),
    SUPPORT_AGENT("فريق الدعم الفني", "Support Client", "Support Agent", "🎧", 0xFF1565C0),
    SUPER_ADMIN("المدير العام / المشرف", "Administrateur", "Super Admin", "👑", 0xFF6A1B9A)
}

enum class AccountStatus {
    ACTIVE,
    PENDING_APPROVAL,
    SUSPENDED,
    REJECTED,
    DEACTIVATED
}

data class UserAccount(
    val id: String,
    val name: String,
    val email: String,
    val phone: String,
    val role: UserRole,
    val status: AccountStatus = AccountStatus.ACTIVE,
    val restaurantId: String? = null,
    val vehicleType: String? = null,
    val plateNumber: String? = null,
    val balance: Double = 0.0,
    val totalEarnings: Double = 0.0,
    val rating: Double = 4.9,
    val avatarEmoji: String = "👤"
)

enum class StoreCategory(
    val id: String,
    val titleAr: String,
    val titleFr: String,
    val titleEn: String,
    val iconName: String
) {
    ALL("all", "الكل", "Tous", "All", "🍽️"),
    BURGER("burger", "برجر وساندويتش", "Burgers", "Burgers", "🍔"),
    PIZZA("pizza", "بيتزا وباستا", "Pizzas & Pâtes", "Pizza & Pasta", "🍕"),
    FAST_FOOD("fast_food", "وجبات سريعة عالمية", "Fast Food", "Fast Food", "🍟"),
    MOROCCAN("moroccan", "أطباق مغربية أصيلة", "Plats Marocains", "Moroccan Food", "🍲"),
    CAFE("cafe", "مخابز وقهوة وحلويات", "Boulangerie & Café", "Bakery & Sweets", "☕"),
    GROCERY("grocery", "سوبرماركت وبقالة", "Supermarché", "Groceries", "🛒"),
    EXPRESS("express", "ألو جيب لي ⚡", "Allo Express ⚡", "Express Courier ⚡", "📦")
}

data class MealOptionItem(
    val id: String,
    val nameAr: String,
    val nameFr: String,
    val nameEn: String,
    val extraPrice: Double = 0.0
)

data class MealOptionGroup(
    val id: String,
    val titleAr: String,
    val titleFr: String,
    val titleEn: String,
    val isRequired: Boolean = false,
    val isMultiSelect: Boolean = false,
    val options: List<MealOptionItem>
)

data class Store(
    val id: String,
    val name: String,
    val nameAr: String,
    val nameFr: String,
    val category: StoreCategory,
    val rating: Double,
    val reviewCount: Int,
    val deliveryTimeMins: Int,
    val deliveryFee: Double,
    val minOrder: Double,
    val address: String,
    val emoji: String,
    val isFeatured: Boolean = false,
    val badge: String = "",
    val isOpen: Boolean = true,
    val ownerId: String = "owner_default",
    val bannerUrl: String = "",
    val distanceKm: Double = 1.8,
    val isPrimeEligible: Boolean = true
)

data class Product(
    val id: String,
    val storeId: String,
    val storeName: String,
    val nameEn: String,
    val nameAr: String,
    val nameFr: String,
    val descriptionEn: String,
    val descriptionAr: String,
    val descriptionFr: String,
    val price: Double,
    val category: StoreCategory,
    val emoji: String,
    val rating: Double = 4.8,
    val isPopular: Boolean = false,
    val calories: String = "450 kcal",
    val isAvailable: Boolean = true,
    val imageUrl: String = "",
    val optionGroups: List<MealOptionGroup> = emptyList()
) {
    val name: String get() = nameAr
    val description: String get() = descriptionAr
}

data class CartItem(
    val product: Product,
    val quantity: Int,
    val selectedOptions: List<String> = emptyList(),
    val extraOptionsPrice: Double = 0.0,
    val notes: String = ""
)

enum class OrderStatus(val stepIndex: Int) {
    RECEIVED(0),
    PREPARING(1),
    ON_THE_WAY(2),
    ARRIVED(3),
    DELIVERED(4)
}

enum class PaymentMethodType {
    CREDIT_CARD,
    CASH_ON_DELIVERY,
    ALLO_PAY_WALLET,
    DIGITAL_WALLET
}

data class CustomerReview(
    val id: String,
    val orderId: String,
    val storeName: String,
    val customerName: String,
    val storeRating: Int,
    val courierRating: Int,
    val tags: List<String>,
    val comment: String,
    val dateString: String
)

data class AppNotification(
    val id: String,
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

data class SupportChatMessage(
    val id: String,
    val text: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis(),
    val agentName: String = "سارة - دعم ألو عزيز"
)

data class SupportTicket(
    val id: String,
    val customerName: String,
    val subject: String,
    val status: String = "OPEN",
    val assignedAgent: String = "سارة المنصوري",
    val lastMessage: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
