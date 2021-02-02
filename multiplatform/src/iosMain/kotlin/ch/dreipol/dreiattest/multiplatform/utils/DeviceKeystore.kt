package ch.dreipol.dreiattest.multiplatform.utils

public actual class DeviceKeystore : Keystore {
    override fun generateNewKeyPair(alias: String): ByteArray {
        TODO("Not yet implemented")
    }

    override fun deleteKeyPair(alias: String) {
        TODO("Not yet implemented")
    }

    override fun hasKeyPair(alias: String): Boolean {
        TODO("Not yet implemented")
    }

    override fun sign(alias: String, content: ByteArray): String {
        TODO("Not yet implemented")
    }
}