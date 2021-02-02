package ch.dreipol.dreiattest.multiplatform.utils

import com.russhwolf.settings.Settings
import com.russhwolf.settings.invoke

internal object SharedPreferences {
    private val settings = Settings()

    fun getUid(user: String): String? {
        return settings.getStringOrNull(user)
    }

    fun setUid(user: String, uuId: String) {
        settings.putString(user, uuId)
    }
}