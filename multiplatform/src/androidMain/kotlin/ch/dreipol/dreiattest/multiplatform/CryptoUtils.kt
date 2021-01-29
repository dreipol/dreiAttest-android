package ch.dreipol.dreiattest.multiplatform

import java.security.MessageDigest

internal actual fun CryptoUtils.hashSHA256(input: String): String {
    val digest = MessageDigest.getInstance("SHA-256")
    return digest.digest(input.toByteArray()).contentToString()
}