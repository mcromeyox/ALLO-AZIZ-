package com.example.data.auth

import java.security.MessageDigest

/**
 * Secure password hashing utility for ALLO AZIZ production architecture.
 * Uses SHA-256 with salt to ensure passwords are never stored in plain text.
 */
object PasswordHasher {

    private const val SALT = "AlloAziz_Morocco_Secured_Salt_2026#"

    fun hashPassword(plainText: String): String {
        val salted = plainText + SALT
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(salted.toByteArray(Charsets.UTF_8))
        return digest.joinToString("") { "%02x".format(it) }
    }

    fun verifyPassword(candidatePlain: String, storedHashOrPlain: String): Boolean {
        // If stored string is already a 64-char SHA-256 hex string, verify with hash
        if (storedHashOrPlain.length == 64) {
            return hashPassword(candidatePlain) == storedHashOrPlain
        }
        // Backward-compatibility fallback for initial unhashed migration entries
        if (candidatePlain == storedHashOrPlain) {
            return true
        }
        return hashPassword(candidatePlain) == storedHashOrPlain
    }
}
