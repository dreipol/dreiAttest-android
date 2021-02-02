package ch.dreipol.dreiattest.multiplatform.utils

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.Signature

public actual class DeviceKeystore : Keystore {

    private val keyStore: KeyStore
        get() = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }

    override fun generateNewKeyPair(alias: String): ByteArray {
        val kpg: KeyPairGenerator = KeyPairGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_EC,
            "AndroidKeyStore"
        )
        val parameterSpec: KeyGenParameterSpec = KeyGenParameterSpec.Builder(
            alias,
            KeyProperties.PURPOSE_SIGN or KeyProperties.PURPOSE_VERIFY
        ).run {
            setDigests(KeyProperties.DIGEST_SHA256)
            build()
        }
        kpg.initialize(parameterSpec)

        val kp = kpg.generateKeyPair()
        return kp.public.encoded
    }

    override fun deleteKeyPair(alias: String) {
        keyStore.deleteEntry(alias)
    }

    override fun hasKeyPair(alias: String): Boolean {
        return keyStore.containsAlias(alias)
    }

    override fun sign(alias: String, content: ByteArray): String {
        val entry = keyStore.getEntry(alias, null)
        if (entry !is KeyStore.PrivateKeyEntry) {
            throw IllegalArgumentException()
        }
        return Base64.encodeToString(Signature.getInstance("SHA256").run {
            initSign(entry.privateKey)
            update(content)
            sign()
        }, Base64.DEFAULT)
    }
}