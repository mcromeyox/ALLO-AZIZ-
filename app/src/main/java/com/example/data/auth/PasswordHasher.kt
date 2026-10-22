package com.example.data.auth

import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

/**
 * Enterprise Password Hashing for WASLIXA (وصلـيكسا).
 * Uses PBKDF2WithHmacSHA256 with 10,000 iterations and cryptographic per-password salt.
 * Includes constant-time comparison to prevent timing attacks.
 * Also supports backward verification for legacy hashes so existing users are never locked out.
 */
object PasswordHasher {

    private const val ITERATIONS = 10000
    private const val KEY_LENGTH = 256
    private const val LEGACY_SALT = "AlloAziz_Morocco_Secured_Salt_2026#"

    fun hashPassword(plainText: String): String {
        val random = SecureRandom()
        val salt = ByteArray(16)
        random.nextBytes(salt)
        val saltHex = salt.joinToString("") { "%02x".format(it) }

        val spec = PBEKeySpec(plainText.toCharArray(), salt, ITERATIONS, KEY_LENGTH)
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val hash = factory.generateSecret(spec).encoded
        val hashHex = hash.joinToString("") { "%02x".format(it) }

        return "PBKDF2\$$ITERATIONS\$$saltHex\$$hashHex"
    }

    fun verifyPassword(candidatePlain: String, storedHashOrPlain: String): Boolean {
        if (storedHashOrPlain.startsWith("PBKDF2$")) {
            val parts = storedHashOrPlain.split("$")
            if (parts.size >= 4) {
                val iterations = parts[1].toIntOrNull() ?: ITERATIONS
                val saltHex = parts[2]
                val expectedHashHex = parts[3]

                return try {
                    val salt = hexToByteArray(saltHex)
                    val spec = PBEKeySpec(candidatePlain.toCharArray(), salt, iterations, KEY_LENGTH)
                    val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
                    val hash = factory.generateSecret(spec).encoded
                    val actualHashHex = hash.joinToString("") { "%02x".format(it) }

                    slowEquals(actualHashHex.toByteArray(Charsets.UTF_8), expectedHashHex.toByteArray(Charsets.UTF_8))
                } catch (e: Exception) {
                    false
                }
            }
        }

        // Backward-compatibility: 64-char SHA-256 legacy hash
        if (storedHashOrPlain.length == 64) {
            val legacyHash = hashLegacySha256(candidatePlain)
            if (slowEquals(legacyHash.toByteArray(Charsets.UTF_8), storedHashOrPlain.toByteArray(Charsets.UTF_8))) {
                return true
            }
        }

        return candidatePlain == storedHashOrPlain
    }

    private fun hashLegacySha256(plainText: String): String {
        val salted = plainText + LEGACY_SALT
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(salted.toByteArray(Charsets.UTF_8))
        return digest.joinToString("") { "%02x".format(it) }
    }

    private fun hexToByteArray(hex: String): ByteArray {
        val len = hex.length
        val data = ByteArray(len / 2)
        var i = 0
        while (i < len) {
            data[i / 2] = ((Character.digit(hex[i], 16) shl 4) + Character.digit(hex[i + 1], 16)).toByte()
            i += 2
        }
        return data
    }

    private fun slowEquals(a: ByteArray, b: ByteArray): Boolean {
        var diff = a.size xor b.size
        val minLen = minOf(a.size, b.size)
        for (i in 0 until minLen) {
            diff = diff or (a[i].toInt() xor b[i].toInt())
        }
        return diff == 0
    }
}
