package ch.dreipol.dreiattest.multiplatform.utils

import java.security.MessageDigest
import java.util.*

internal actual fun CryptoUtils.hashSHA256(input: String): String {
    val digest = MessageDigest.getInstance("SHA-256")
    return digest.digest(input.toByteArray()).contentToString()
}

internal actual fun CryptoUtils.generateUuid(): String {
    return UUID.randomUUID().toString()
}