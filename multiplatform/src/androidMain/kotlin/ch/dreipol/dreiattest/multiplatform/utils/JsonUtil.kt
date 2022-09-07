package ch.dreipol.dreiattest.multiplatform.utils

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

actual object JsonUtil {
    @ExperimentalSerializationApi
    actual fun sortedJsonData(value: Map<String, String>): ByteArray {
        return Json.encodeToString(value.toSortedMap() as Map<String, String>).toByteArray()
    }
}