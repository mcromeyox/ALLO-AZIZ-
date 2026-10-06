package com.example.data.services

import android.content.Context
import android.util.Log
import com.example.BuildConfig
import com.google.firebase.FirebaseApp

/**
 * ServiceManager
 *
 * Implements graceful service abstractions and development fallbacks for ALLO AZIZ.
 * In Development / Preview mode, all external services are optional.
 * When API keys are not provided, the app runs completely on local Room database,
 * interactive simulated map views, and local development authentication.
 */
object ServiceManager {

    fun isFirebaseConfigured(context: Context): Boolean {
        return try {
            FirebaseApp.getApps(context).isNotEmpty() && BuildConfig.FIREBASE_PROJECT_ID.isNotBlank()
        } catch (e: Exception) {
            false
        }
    }

    fun isGoogleMapsConfigured(): Boolean {
        val key = try { BuildConfig.GOOGLE_MAPS_API_KEY } catch (e: Exception) { "" }
        return key.isNotBlank() && !key.contains("YourReal") && !key.contains("PLACEHOLDER")
    }

    fun isPaymentGatewayConfigured(): Boolean {
        val key = try { BuildConfig.PAYMENT_PROVIDER_KEY } catch (e: Exception) { "" }
        return key.isNotBlank() && !key.contains("merchant_key")
    }

    fun isSmsProviderConfigured(): Boolean = false
    fun isEmailProviderConfigured(): Boolean = false
    fun isStorageConfigured(): Boolean = false

    /**
     * Development OTP System
     * Generates a safe, fixed development OTP for testing phone authentication.
     */
    fun generateDevOtp(phoneNumber: String): String {
        val devOtp = "1234"
        Log.i("ALLO_AZIZ_DEV", "[DEVELOPMENT ONLY] OTP generated for $phoneNumber: $devOtp")
        return devOtp
    }

    /**
     * Development Email Logger
     */
    fun recordDevEmail(to: String, subject: String, body: String) {
        Log.i("ALLO_AZIZ_DEV", "[DEVELOPMENT EMAIL] To: $to | Subject: $subject | Content: $body")
    }
}
