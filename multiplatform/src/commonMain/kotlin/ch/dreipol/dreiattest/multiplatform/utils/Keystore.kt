package ch.dreipol.dreiattest.multiplatform.utils

public interface Keystore {

    public fun generateNewKeyPair(alias: String): ByteArray

    public fun deleteKeyPair(alias: String)

    public fun hasKeyPair(alias: String): Boolean

    public fun sign(alias: String, content: ByteArray): String
}

public expect class DeviceKeystore : Keystore