package ch.dreipol.dreiattest.multiplatform.utils

import kotlinx.coroutines.sync.Mutex

internal data object InvalidKeyException: Exception("The provided dreiAttest key was invalid.")

public interface Keystore {

    public suspend fun generateNewKeyPair(alias: String): ByteArray

    public fun deleteKeyPair(alias: String)

    public fun hasKeyPair(alias: String): Boolean

    public suspend fun sign(alias: String, content: Hash, mutex: Mutex): String

    public fun getPublicKey(alias: String): ByteArray
}

public expect fun createDeviceKeystore() : Keystore