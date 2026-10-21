package com.example.data.services

import android.content.Context
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
            val app = FirebaseApp.getApps(context).firstOrNull() ?: return false
            !app.options.projectId.isNullOrBlank() && !app.options.applicationId.isNullOrBlank()
        } catch (e: Exception) {
            false
        }
    }

    fun isGoogleMapsConfigured(): Boolean {
        val key = try { BuildConfig.GOOGLE_MAPS_API_KEY } catch (e: Exception) { "" }
        return key.isNotBlank() && !key.contains("YourReal") && !key.contains("PLACEHOLDER")
    }

    // A client-side API key is not proof of a secure payment integration.
    fun isPaymentGatewayConfigured(): Boolean = false

    fun isSmsProviderConfigured(): Boolean = false
    fun isEmailProviderConfigured(): Boolean = false
    fun isStorageConfigured(): Boolean = false

    /**
     * OTP Verification Code Generator
     * Generates a 6-digit verification code for phone or email activation.
     */
    @Deprecated("Use a trusted server-issued OTP; development OTPs are disabled.")
    fun generateDevOtp(recipient: String): String = ""

    /**
     * Development Email & SMS Logger
     */
    @Deprecated("Use a trusted email/SMS provider; development logging is disabled.")
    fun recordDevEmail(to: String, subject: String, body: String) = Unit
}
