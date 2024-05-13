package ch.dreipol.dreiattest.multiplatform.utils

import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSJSONSerialization
import platform.Foundation.NSJSONWritingSortedKeys

actual object JsonUtil {
    @OptIn(ExperimentalForeignApi::class)
    @ExperimentalUnsignedTypes
    actual fun sortedJsonData(value: Map<String, String>): ByteArray {
        val data = NSJSONSerialization.dataWithJSONObject(value, NSJSONWritingSortedKeys, null)
        return data?.let { Conversion.dataToByteArray(it) } ?: ByteArray(0)
    }
}