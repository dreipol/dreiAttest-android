package ch.dreipol.dreiattest.multiplatform.utils

public object CryptoUtils

public expect class Hash
public expect operator fun Hash.plus(other: ByteArray): Hash

internal expect fun CryptoUtils.hashSHA256(input: ByteArray): Hash
internal expect fun CryptoUtils.rehashSHA256(input: Hash): Hash

internal expect fun CryptoUtils.generateUuid(): String

internal expect fun CryptoUtils.encodeToBase64(input: ByteArray): String

internal expect fun CryptoUtils.decodeBase64(input: String): ByteArray

internal expect fun CryptoUtils.encodeHashedToBase64(input: Hash): String