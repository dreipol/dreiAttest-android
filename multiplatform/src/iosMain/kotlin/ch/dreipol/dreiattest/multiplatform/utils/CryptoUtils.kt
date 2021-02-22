package ch.dreipol.dreiattest.multiplatform.utils

import kotlinx.cinterop.CPointer
import kotlinx.cinterop.CValuesRef
import kotlinx.cinterop.UByteVar
import kotlinx.cinterop.memScoped
import platform.CoreCrypto.CC_SHA256
import platform.CoreCrypto.CC_SHA256_DIGEST_LENGTH
import platform.Foundation.*

public actual typealias Hash = NSData

@OptIn(ExperimentalUnsignedTypes::class)
internal actual fun CryptoUtils.hashSHA256(input: ByteArray): Hash {
    val data = Conversion.byteArrayToData(input)
    val hash = NSMutableData.create(CC_SHA256_DIGEST_LENGTH.toULong())
        ?: throw Exception("Could not create data.")
    hash.mutableBytes?.let {
        @Suppress("UNCHECKED_CAST")
        CC_SHA256(data.bytes, data.length.toUInt(), it as CPointer<UByteVar>)
    }
    return hash
}

internal actual fun CryptoUtils.generateUuid(): String = NSUUID().UUIDString

internal actual fun CryptoUtils.encodeToBase64(input: ByteArray): String =
    Conversion.byteArrayToData(input).base64EncodedStringWithOptions(0)