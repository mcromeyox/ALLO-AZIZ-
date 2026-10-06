package com.example.data.services

import android.util.Log
import com.example.data.local.OrderEntity
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

/**
 * FirestoreSyncManager
 *
 * Synchronizes orders between local Room DB and Cloud Firestore (Project: allo-aziz).
 * Enables multi-device real-time sync across Customer, Driver, and Restaurant phones.
 */
object FirestoreSyncManager {

    private const val TAG = "FirestoreSyncManager"
    private const val COLLECTION_ORDERS = "orders"

    private val firestore: FirebaseFirestore? by lazy {
        try {
            FirebaseFirestore.getInstance()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize FirebaseFirestore: ${e.message}")
            null
        }
    }

    /**
     * Uploads or updates an order in Cloud Firestore.
     */
    suspend fun syncOrderToCloud(order: OrderEntity): Boolean {
        val db = firestore ?: return false
        return try {
            val orderMap = hashMapOf(
                "id" to order.id,
                "storeId" to order.storeId,
                "storeName" to order.storeName,
                "itemsSummary" to order.itemsSummary,
                "itemsCount" to order.itemsCount,
                "subtotal" to order.subtotal,
                "deliveryFee" to order.deliveryFee,
                "discount" to order.discount,
                "total" to order.total,
                "status" to order.status,
                "restaurantStatus" to order.restaurantStatus,
                "driverId" to order.driverId,
                "paymentMethod" to order.paymentMethod,
                "deliveryAddress" to order.deliveryAddress,
                "deliveryNotes" to order.deliveryNotes,
                "timestamp" to order.timestamp,
                "courierName" to order.courierName,
                "courierPhone" to order.courierPhone,
                "courierRating" to order.courierRating,
                "courierVehicle" to order.courierVehicle,
                "courierProgress" to order.courierProgress,
                "estimatedMinsLeft" to order.estimatedMinsLeft,
                "hasReviewed" to order.hasReviewed,
                "storeLat" to order.storeLat,
                "storeLng" to order.storeLng,
                "customerLat" to order.customerLat,
                "customerLng" to order.customerLng,
                "courierLat" to order.courierLat,
                "courierLng" to order.courierLng,
                "updatedAt" to System.currentTimeMillis()
            )

            db.collection(COLLECTION_ORDERS)
                .document(order.id)
                .set(orderMap, SetOptions.merge())
                .await()

            Log.i(TAG, "Order #${order.id} synced to Cloud Firestore successfully!")
            true
        } catch (e: Exception) {
            Log.w(TAG, "Cloud sync failed (will rely on local DB): ${e.message}")
            false
        }
    }

    /**
     * Updates courier real-time GPS location in Cloud Firestore.
     */
    suspend fun updateCourierGpsCloud(orderId: String, lat: Double, lng: Double, progress: Float, minsLeft: Int) {
        val db = firestore ?: return
        try {
            db.collection(COLLECTION_ORDERS)
                .document(orderId)
                .update(
                    mapOf(
                        "courierLat" to lat,
                        "courierLng" to lng,
                        "courierProgress" to progress,
                        "estimatedMinsLeft" to minsLeft,
                        "updatedAt" to System.currentTimeMillis()
                    )
                ).await()
        } catch (e: Exception) {
            Log.w(TAG, "Failed to update GPS in cloud: ${e.message}")
        }
    }
}
