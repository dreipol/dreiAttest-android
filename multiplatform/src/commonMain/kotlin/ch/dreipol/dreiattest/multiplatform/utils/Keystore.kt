package ch.dreipol.dreiattest.multiplatform.utils

public interface Keystore {

    public suspend fun generateNewKeyPair(alias: String): ByteArray

    public fun deleteKeyPair(alias: String)

    public fun hasKeyPair(alias: String): Boolean

    public suspend fun sign(alias: String, content: Hash): String

    public fun getPublicKey(alias: String): ByteArray
}

public expect class DeviceKeystore() : Keystore