package ch.dreipol.dreiattest.multiplatform.utils

internal actual fun SystemUtils.getEnvVariable(name: String): String? {
    return System.getenv(name)
}