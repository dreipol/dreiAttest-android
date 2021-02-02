package ch.dreipol.dreiattest.multiplatform.utils

public object CryptoUtils

internal expect fun CryptoUtils.hashSHA256(input: String): String

internal expect fun CryptoUtils.generateUuid(): String

internal expect fun CryptoUtils.encodeToBase64(input: ByteArray): String