package ch.dreipol.dreiattest.multiplatform.utils

import kotlinx.cinterop.addressOf
import kotlinx.cinterop.allocArrayOf
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.usePinned
import platform.Foundation.NSData
import platform.Foundation.create
import platform.posix.memcpy

internal object Conversion {
    @OptIn(ExperimentalUnsignedTypes::class)
    fun dataToByteArray(data: NSData): ByteArray {
        return ByteArray(data.length.toInt()).apply {
            usePinned {
                memcpy(it.addressOf(0), data.bytes, data.length)
            }
        }
    }

    fun byteArrayToData(bytes: ByteArray): NSData = memScoped {
        NSData.create(allocArrayOf(bytes), bytes.size.toULong())
    }

    fun base64ToByteArray(base64String: String): ByteArray {
        return dataToByteArray(NSData.create(base64String) ?: throw IllegalArgumentException())
    }
}