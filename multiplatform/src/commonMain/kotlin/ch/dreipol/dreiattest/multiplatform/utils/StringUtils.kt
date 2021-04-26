package ch.dreipol.dreiattest.multiplatform.utils


internal fun String.removeProtocolFromUrl(): String {
    return replace(Regex(".*://"), "")
}