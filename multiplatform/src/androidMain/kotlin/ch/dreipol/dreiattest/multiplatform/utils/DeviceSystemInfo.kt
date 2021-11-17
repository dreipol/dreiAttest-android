package ch.dreipol.dreiattest.multiplatform.utils

import android.content.Context

internal class DeviceSystemInfo(context: Context) : SystemInfo {
    private val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)

    override val appVersion: String
        get() = packageInfo.versionName

    override val appBuild: String
        get() = packageInfo.versionCode.toString()

    override val appIdentifier: String
        get() = packageInfo.packageName

    override val osVersion: String
        get() = "Android ${android.os.Build.VERSION.RELEASE} (API ${android.os.Build.VERSION.SDK_INT})"
}