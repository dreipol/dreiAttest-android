package ch.dreipol.dreiattest.multiplatform.utils

import android.util.Base64
import java.security.MessageDigest
import java.util.*

internal actual fun CryptoUtils.hashSHA256(input: ByteArray): ByteArray {
    val digest = MessageDigest.getInstance("SHA-256")
    return digest.digest(input)
}

internal actual fun CryptoUtils.generateUuid(): String {
    return UUID.randomUUID().toString()
}

internal actual fun CryptoUtils.encodeToBase64(input: ByteArray): String {
    return Base64.encodeToString(input, Base64.DEFAULT)
}