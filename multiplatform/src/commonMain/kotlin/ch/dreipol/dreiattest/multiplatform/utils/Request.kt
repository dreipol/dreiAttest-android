package ch.dreipol.dreiattest.multiplatform.utils

public data class Request(val url: String, val requestMethod: String, val headers: List<Pair<String, String>>, val body: ByteArray?)