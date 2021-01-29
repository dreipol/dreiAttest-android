package ch.dreipol.dreiattest.multiplatform

public actual class DeviceKeystore : Keystore {
    override fun generateNewKeyPair(alias: String): String {
        TODO("Not yet implemented")
    }

    override fun hasKeyPair(alias: String): Boolean {
        TODO("Not yet implemented")
    }

    override fun sign(alias: String, content: String, nonce: String): String {
        TODO("Not yet implemented")
    }
}