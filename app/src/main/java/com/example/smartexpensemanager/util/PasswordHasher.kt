package com.example.smartexpensemanager.util

import java.security.MessageDigest
import java.security.SecureRandom

/**
 * Salted SHA-256 hashing. Adequate for a coursework-scope local auth store;
 * not a substitute for a real credential system in production.
 */
object PasswordHasher {

    fun generateSalt(): String {
        val bytes = ByteArray(16)
        SecureRandom().nextBytes(bytes)
        return bytes.joinToString("") { "%02x".format(it) }
    }

    fun hash(password: String, salt: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        digest.update(salt.toByteArray(Charsets.UTF_8))
        val hashed = digest.digest(password.toByteArray(Charsets.UTF_8))
        return hashed.joinToString("") { "%02x".format(it) }
    }

    fun matches(password: String, salt: String, expectedHash: String): Boolean {
        return hash(password, salt) == expectedHash
    }
}
