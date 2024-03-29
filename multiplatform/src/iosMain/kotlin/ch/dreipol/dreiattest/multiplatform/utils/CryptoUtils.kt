package ch.dreipol.dreiattest.multiplatform.utils

import kotlinx.cinterop.CPointer
import kotlinx.cinterop.UByteVar
import platform.CoreCrypto.CC_SHA256
import platform.CoreCrypto.CC_SHA256_DIGEST_LENGTH
import platform.Foundation.*

public actual typealias Hash = NSData

@ExperimentalUnsignedTypes
public actual operator fun Hash.plus(other: ByteArray): Hash {
    val mutableData = NSMutableData.create(this)
    mutableData.appendData(Conversion.byteArrayToData(other))
    return mutableData
}

@ExperimentalUnsignedTypes
internal actual fun CryptoUtils.hashSHA256(input: ByteArray): Hash {
    val data = Conversion.byteArrayToData(input)
    return rehashSHA256(data)
}

@ExperimentalUnsignedTypes
internal actual fun CryptoUtils.rehashSHA256(input: Hash): Hash {
    val hash = NSMutableData.create(length = CC_SHA256_DIGEST_LENGTH.toULong())
        ?: throw Exception("Could not create data.")
    hash.mutableBytes?.let {
        @Suppress("UNCHECKED_CAST")
        CC_SHA256(input.bytes, input.length.toUInt(), it as CPointer<UByteVar>)
    }
    return hash
}

internal actual fun CryptoUtils.generateUuid(): String = NSUUID.UUID().UUIDString.toLowerCase()

@ExperimentalUnsignedTypes
internal actual fun CryptoUtils.encodeToBase64(input: ByteArray): String =
    Conversion.byteArrayToData(input).base64EncodedStringWithOptions(0)

@ExperimentalUnsignedTypes
internal actual fun CryptoUtils.decodeBase64(input: String): ByteArray = Conversion.base64ToByteArray(input)

@ExperimentalUnsignedTypes
internal actual fun CryptoUtils.encodeHashedToBase64(input: Hash): String =
    input.base64EncodedStringWithOptions(0)