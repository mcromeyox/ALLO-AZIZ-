package com.example.data.services

import android.util.Log
import com.example.data.local.OrderEntity
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * FirestoreSyncManager
 *
 * Synchronizes orders between local Room DB and Cloud Firestore (Project: allo-aziz).
 * Provides real-time snapshot listeners to track delivery progress across Customer, Driver,
 * and Restaurant roles with status updates ('Order Placed', 'Picked Up', 'Out for Delivery', etc.).
 */
object FirestoreSyncManager {

    private const val TAG = "FirestoreSyncManager"
    const val COLLECTION_ORDERS = "orders"

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

    /**
     * Updates order status in Cloud Firestore.
     */
    suspend fun updateOrderStatusCloud(orderId: String, status: String, progress: Float, minsLeft: Int) {
        val db = firestore ?: return
        try {
            db.collection(COLLECTION_ORDERS)
                .document(orderId)
                .update(
                    mapOf(
                        "status" to status,
                        "courierProgress" to progress,
                        "estimatedMinsLeft" to minsLeft,
                        "updatedAt" to System.currentTimeMillis()
                    )
                ).await()
        } catch (e: Exception) {
            Log.w(TAG, "Failed to update status in cloud: ${e.message}")
        }
    }

    /**
     * Setup a real-time snapshot listener in Firestore to track delivery progress
     * for a specific order. Emits OrderEntity whenever changes occur in Firestore.
     */
    fun listenToOrderRealtime(orderId: String): Flow<OrderEntity?> = callbackFlow {
        val db = firestore
        if (db == null) {
            trySend(null)
            close()
            return@callbackFlow
        }

        val listener: ListenerRegistration = db.collection(COLLECTION_ORDERS)
            .document(orderId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w(TAG, "Firestore listen error for order #$orderId: ${error.message}")
                    return@addSnapshotListener
                }

                if (snapshot != null && snapshot.exists()) {
                    val order = parseOrderFromSnapshot(snapshot)
                    trySend(order)
                }
            }

        awaitClose {
            Log.d(TAG, "Detaching Firestore snapshot listener for order #$orderId")
            listener.remove()
        }
    }

    /**
     * Setup a real-time snapshot listener on the 'orders' collection to track all
     * orders and live delivery progress across the platform.
     */
    fun listenToAllOrdersRealtime(): Flow<List<OrderEntity>> = callbackFlow {
        val db = firestore
        if (db == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val listener: ListenerRegistration = db.collection(COLLECTION_ORDERS)
            .addSnapshotListener { querySnapshot, error ->
                if (error != null) {
                    Log.w(TAG, "Firestore listen all orders error: ${error.message}")
                    return@addSnapshotListener
                }

                if (querySnapshot != null) {
                    val list = querySnapshot.documents.mapNotNull { parseOrderFromSnapshot(it) }
                    trySend(list)
                }
            }

        awaitClose {
            Log.d(TAG, "Detaching all orders Firestore listener")
            listener.remove()
        }
    }

    /**
     * Converts a Firestore DocumentSnapshot into an OrderEntity.
     */
    fun parseOrderFromSnapshot(doc: DocumentSnapshot): OrderEntity? {
        return try {
            val id = doc.getString("id") ?: doc.id
            val storeId = doc.getString("storeId") ?: "store_unknown"
            val storeName = doc.getString("storeName") ?: "مطعم ألو عزيز"
            val itemsSummary = doc.getString("itemsSummary") ?: "وجبة شهية"
            val itemsCount = doc.getLong("itemsCount")?.toInt() ?: 1
            val subtotal = doc.getDouble("subtotal") ?: 0.0
            val deliveryFee = doc.getDouble("deliveryFee") ?: 8.0
            val discount = doc.getDouble("discount") ?: 0.0
            val total = doc.getDouble("total") ?: (subtotal + deliveryFee - discount)
            val status = doc.getString("status") ?: "RECEIVED"
            val restaurantStatus = doc.getString("restaurantStatus") ?: "ACCEPTED"
            val driverId = doc.getString("driverId")
            val paymentMethod = doc.getString("paymentMethod") ?: "الدفع نقداً عند الاستلام"
            val deliveryAddress = doc.getString("deliveryAddress") ?: "المحمدية، المغرب"
            val deliveryNotes = doc.getString("deliveryNotes") ?: ""
            val timestamp = doc.getLong("timestamp") ?: System.currentTimeMillis()
            val courierName = doc.getString("courierName") ?: "كابتن ألو عزيز"
            val courierPhone = doc.getString("courierPhone") ?: ""
            val courierRating = doc.getDouble("courierRating") ?: 5.0
            val courierVehicle = doc.getString("courierVehicle") ?: "دراجة نارية"
            val courierProgress = (doc.getDouble("courierProgress")?.toFloat()) ?: 0.10f
            val estimatedMinsLeft = doc.getLong("estimatedMinsLeft")?.toInt() ?: 20
            val hasReviewed = doc.getBoolean("hasReviewed") ?: false
            val storeLat = doc.getDouble("storeLat") ?: 33.6930
            val storeLng = doc.getDouble("storeLng") ?: -7.3820
            val customerLat = doc.getDouble("customerLat") ?: 33.6835
            val customerLng = doc.getDouble("customerLng") ?: -7.3849
            val courierLat = doc.getDouble("courierLat") ?: 33.6900
            val courierLng = doc.getDouble("courierLng") ?: -7.3835

            OrderEntity(
                id = id,
                storeId = storeId,
                storeName = storeName,
                itemsSummary = itemsSummary,
                itemsCount = itemsCount,
                subtotal = subtotal,
                deliveryFee = deliveryFee,
                discount = discount,
                total = total,
                status = status,
                restaurantStatus = restaurantStatus,
                driverId = driverId,
                paymentMethod = paymentMethod,
                deliveryAddress = deliveryAddress,
                deliveryNotes = deliveryNotes,
                timestamp = timestamp,
                courierName = courierName,
                courierPhone = courierPhone,
                courierRating = courierRating,
                courierVehicle = courierVehicle,
                courierProgress = courierProgress,
                estimatedMinsLeft = estimatedMinsLeft,
                hasReviewed = hasReviewed,
                storeLat = storeLat,
                storeLng = storeLng,
                customerLat = customerLat,
                customerLng = customerLng,
                courierLat = courierLat,
                courierLng = courierLng
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse document ${doc.id}: ${e.message}")
            null
        }
    }
}
