package ch.dreipol.dreiattest.multiplatform.mock

import ch.dreipol.dreiattest.multiplatform.utils.CryptoUtils
import ch.dreipol.dreiattest.multiplatform.utils.Keystore
import ch.dreipol.dreiattest.multiplatform.utils.encodeToBase64
import io.ktor.utils.io.core.*

class KeystoreMock : Keystore {

    val keys = mutableMapOf<String, String>()

    override fun generateNewKeyPair(alias: String): ByteArray {
        val key = "key"
        keys[alias] = key
        return key.toByteArray()
    }

    override fun deleteKeyPair(alias: String) {
        keys.remove(alias)
    }

    override fun hasKeyPair(alias: String): Boolean {
        return keys.containsKey(alias)
    }

    override fun sign(alias: String, content: ByteArray): String {
        return CryptoUtils.encodeToBase64(content)
    }

    override fun getPublicKey(alias: String): ByteArray {
        return keys[alias]?.toByteArray() ?: throw IllegalArgumentException()
    }
}