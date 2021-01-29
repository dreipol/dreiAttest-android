package ch.dreipol.dreiattest.multiplatform

public interface Keystore {

    public fun generateNewKeyPair(alias: String): String

    public fun hasKeyPair(alias: String): Boolean

    public fun sign(alias: String, content: String, nonce: String): String
}

public expect class DeviceKeystore : Keystore