package ch.dreipol.dreiattest.multiplatform.utils

import android.util.Base64
import java.security.MessageDigest
import java.util.*

public actual typealias Hash = ByteArray

public actual operator fun Hash.plus(other: ByteArray): Hash = plus(elements = other)

internal actual fun CryptoUtils.hashSHA256(input: ByteArray): Hash {
    val digest = MessageDigest.getInstance("SHA-256")
    return digest.digest(input)
}

internal actual fun CryptoUtils.generateUuid(): String {
    return UUID.randomUUID().toString()
}

internal actual fun CryptoUtils.encodeToBase64(input: ByteArray): String {
    return Base64.encodeToString(input, Base64.DEFAULT).trim()
}

internal actual fun CryptoUtils.decodeBase64(input: String): ByteArray {
    return Base64.decode(input, Base64.DEFAULT)
}

internal actual fun CryptoUtils.encodeHashedToBase64(input: Hash): String {
    return encodeToBase64(input)
}