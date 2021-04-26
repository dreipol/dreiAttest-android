@file:JvmName("Base64")

package android.util

import android.os.Build
import androidx.annotation.RequiresApi

public object Base64 {
    @JvmStatic
    @RequiresApi(Build.VERSION_CODES.O)
    public fun encodeToString(input: ByteArray?, flags: Int): String {
        return java.util.Base64.getEncoder().encodeToString(input)
    }

    @JvmStatic
    @RequiresApi(Build.VERSION_CODES.O)
    public fun decode(input: String?, flags: Int): ByteArray {
        return java.util.Base64.getDecoder().decode(input)
    }
}