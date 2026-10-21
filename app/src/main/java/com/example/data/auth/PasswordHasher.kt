package com.example.data.auth

import android.util.Base64
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

/**
 * Password hashing helper for local development only.
 *
 * Production authentication and credential storage must be owned by a trusted backend.
 * New local hashes use PBKDF2-HMAC-SHA256 with a per-password random salt.
 * Legacy salted SHA-256 hashes are accepted only for migration and should be upgraded
 * after successful login. Plain-text password values are deliberately rejected.
 */
object PasswordHasher {
    private const val ITERATIONS = 210_000
    private const val KEY_LENGTH_BITS = 256
    private const val SALT_BYTES = 16
    private const val LEGACY_SALT = "AlloAziz_Morocco_Secured_Salt_2026#"
    private const val PREFIX = "pbkdf2_sha256"

    fun hashPassword(plainText: String): String {
        val salt = ByteArray(SALT_BYTES).also { SecureRandom().nextBytes(it) }
        val hash = derive(plainText, salt, ITERATIONS)
        return "$PREFIX\$$ITERATIONS\$${Base64.encodeToString(salt, Base64.NO_WRAP)}\$${Base64.encodeToString(hash, Base64.NO_WRAP)}"
    }

    fun verifyPassword(candidatePlain: String, storedHashOrPlain: String): Boolean {
        if (storedHashOrPlain.startsWith("$PREFIX\$")) {
            val parts = storedHashOrPlain.split('$')
            if (parts.size != 4) return false
            val iterations = parts[1].toIntOrNull() ?: return false
            if (iterations !in 100_000..1_000_000) return false
            return try {
                val salt = Base64.decode(parts[2], Base64.NO_WRAP)
                val expected = Base64.decode(parts[3], Base64.NO_WRAP)
                val actual = derive(candidatePlain, salt, iterations)
                MessageDigest.isEqual(expected, actual)
            } catch (_: IllegalArgumentException) {
                false
            }
        }

        // Transitional verification for the repository's previous 64-character SHA-256 hash.
        // Never accept a plain-text stored password.
        if (storedHashOrPlain.matches(Regex("^[0-9a-fA-F]{64}$"))) {
            val legacy = MessageDigest.getInstance("SHA-256")
                .digest((candidatePlain + LEGACY_SALT).toByteArray(Charsets.UTF_8))
                .joinToString("") { "%02x".format(it) }
            return MessageDigest.isEqual(
                legacy.lowercase().toByteArray(Charsets.US_ASCII),
                storedHashOrPlain.lowercase().toByteArray(Charsets.US_ASCII)
            )
        }
        return false
    }

    private fun derive(password: String, salt: ByteArray, iterations: Int): ByteArray {
        val spec = PBEKeySpec(password.toCharArray(), salt, iterations, KEY_LENGTH_BITS)
        return try {
            SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).encoded
        } finally {
            spec.clearPassword()
        }
    }
}
