package ch.dreipol.dreiattest.multiplatform.utils

import platform.Foundation.NSJSONSerialization
import platform.Foundation.NSJSONWritingPrettyPrinted
import platform.Foundation.NSJSONWritingSortedKeys
import platform.Foundation.NSString

actual object JsonUtil {
    @ExperimentalUnsignedTypes
    actual fun sortedJsonData(value: Map<String, String>): ByteArray {
        val data = NSJSONSerialization.dataWithJSONObject(value, NSJSONWritingSortedKeys, null)
        return data?.let { Conversion.dataToByteArray(it) } ?: ByteArray(0)
    }
}