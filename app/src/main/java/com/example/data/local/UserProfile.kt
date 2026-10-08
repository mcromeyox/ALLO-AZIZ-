package com.example.data.local

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * UserProfile Entity
 *
 * Local Room Database entity representing the user's profile, including their
 * saved delivery address, location coordinates, and order preferences.
 */
@Entity(tableName = "user_profiles")
data class UserProfile(
    @PrimaryKey val userId: String = "default_user",
    val fullName: String = "",
    val email: String = "",
    val phoneNumber: String = "",

    // Delivery Address Details
    val deliveryAddressTitle: String = "المنزل",
    val streetAddress: String = "",
    val city: String = "المحمدية",
    val neighborhood: String = "",
    val buildingInfo: String = "",
    val deliveryNotes: String = "",
    val latitude: Double = 33.7063,
    val longitude: Double = -7.3829,

    // User Preferences
    val preferredLanguage: String = "AR", // AR, FR, EN
    val preferredPaymentMethod: String = "CASH_ON_DELIVERY", // CASH_ON_DELIVERY, CREDIT_CARD, ALLO_PAY_WALLET
    val contactlessDelivery: Boolean = false,
    val requestCutlery: Boolean = true,
    val enablePushNotifications: Boolean = true,
    val enableOrderSmsUpdates: Boolean = true,
    val enablePromoNotifications: Boolean = true,
    val isDarkMode: Boolean = false,
    val updatedAt: Long = System.currentTimeMillis()
)

// Alias for convenience if referenced as UserProfileEntity
typealias UserProfileEntity = UserProfile

/**
 * UserProfileDao
 *
 * Room Data Access Object for local UserProfile and preferences persistence.
 */
@Dao
interface UserProfileDao {

    @Query("SELECT * FROM user_profiles WHERE userId = :userId LIMIT 1")
    fun getUserProfile(userId: String): Flow<UserProfile?>

    @Query("SELECT * FROM user_profiles LIMIT 1")
    fun getDefaultUserProfile(): Flow<UserProfile?>

    @Query("SELECT * FROM user_profiles WHERE userId = :userId LIMIT 1")
    suspend fun getUserProfileOnce(userId: String): UserProfile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveUserProfile(profile: UserProfile)

    @Update
    suspend fun updateUserProfile(profile: UserProfile)

    @Query("""
        UPDATE user_profiles 
        SET streetAddress = :address, 
            city = :city, 
            neighborhood = :neighborhood, 
            buildingInfo = :buildingInfo,
            deliveryNotes = :notes, 
            updatedAt = :updatedAt 
        WHERE userId = :userId
    """)
    suspend fun updateDeliveryAddress(
        userId: String,
        address: String,
        city: String,
        neighborhood: String,
        buildingInfo: String,
        notes: String,
        updatedAt: Long = System.currentTimeMillis()
    )

    @Query("""
        UPDATE user_profiles 
        SET preferredLanguage = :language, 
            preferredPaymentMethod = :paymentMethod, 
            contactlessDelivery = :contactless, 
            requestCutlery = :cutlery, 
            enablePushNotifications = :pushNotifs, 
            updatedAt = :updatedAt 
        WHERE userId = :userId
    """)
    suspend fun updatePreferences(
        userId: String,
        language: String,
        paymentMethod: String,
        contactless: Boolean,
        cutlery: Boolean,
        pushNotifs: Boolean,
        updatedAt: Long = System.currentTimeMillis()
    )

    @Query("DELETE FROM user_profiles WHERE userId = :userId")
    suspend fun deleteUserProfile(userId: String)
}
