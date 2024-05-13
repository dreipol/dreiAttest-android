package ch.dreipol.dreiattest.multiplatform.utils

import kotlinx.cinterop.ExperimentalForeignApi
import platform.CoreFoundation.kCFBundleIdentifierKey
import platform.CoreFoundation.kCFBundleVersionKey
import platform.Foundation.CFBridgingRelease
import platform.Foundation.NSBundle
import platform.UIKit.UIDevice

internal object DeviceSystemInfo : SystemInfo {
    private const val VERSION_KEY = "CFBundleShortVersionString"
    @OptIn(ExperimentalForeignApi::class)
    private val BUILD_KEY = CFBridgingRelease(kCFBundleVersionKey) as String
    @OptIn(ExperimentalForeignApi::class)
    private val BUNDLE_ID_KEY = CFBridgingRelease(kCFBundleIdentifierKey) as String

    override val appVersion: String
        get() = NSBundle.mainBundle.infoDictionary?.get(VERSION_KEY)?.toString() ?: "unknown"

    override val appBuild: String
        get() = NSBundle.mainBundle.infoDictionary?.get(BUILD_KEY)?.toString() ?: "unknown"

    override val appIdentifier: String
        get() = NSBundle.mainBundle.infoDictionary?.get(BUNDLE_ID_KEY)?.toString() ?: "unknown"

    override val osVersion: String
        get() = UIDevice.currentDevice.systemName + " " + UIDevice.currentDevice.systemVersion
}