package ch.dreipol.dreiattest.multiplatform.utils

public object CryptoUtils

public expect class Hash

internal expect fun CryptoUtils.hashSHA256(input: ByteArray): ByteArray

internal expect fun CryptoUtils.generateUuid(): String

internal expect fun CryptoUtils.encodeToBase64(input: ByteArray): String