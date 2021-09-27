package ch.dreipol.dreiattest.multiplatform.utils

expect object JsonUtil {
    fun sortedJsonData(value: Map<String, String>): ByteArray
}