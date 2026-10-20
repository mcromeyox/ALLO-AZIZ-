package com.example.data.services

import android.content.Context
import android.util.Log
import com.example.BuildConfig
import com.google.firebase.FirebaseApp

/**
 * ServiceManager
 *
 * Implements graceful service abstractions and development fallbacks for DELIVIO.
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
     * OTP Verification Code Generator
     * Generates a 6-digit verification code for phone or email activation.
     */
    fun generateDevOtp(recipient: String): String {
        val otp = ((Math.random() * 900000).toInt() + 100000).toString()
        Log.i("DELIVIO_DEV", "[DELIVIO OTP] Code for $recipient: $otp")
        return otp
    }

    /**
     * Development Email & SMS Logger
     */
    fun recordDevEmail(to: String, subject: String, body: String) {
        Log.i("DELIVIO_DEV", "[DELIVIO EMAIL/SMS] To: $to | Subject: $subject | Content: $body")
    }
}
