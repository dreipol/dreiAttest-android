package ch.dreipol.dreiattest.multiplatform.utils

import platform.Foundation.NSProcessInfo

internal actual fun SystemUtils.getEnvVariable(name: String): String? {
    return NSProcessInfo.processInfo.environment[name]?.toString()
}