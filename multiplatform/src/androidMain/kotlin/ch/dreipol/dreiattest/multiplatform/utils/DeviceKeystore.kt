package ch.dreipol.dreiattest.multiplatform.utils

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.Signature

public actual class DeviceKeystore : Keystore {

    private val keyStore: KeyStore
        get() = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }

    override suspend fun generateNewKeyPair(alias: String): ByteArray {
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

    override suspend fun sign(alias: String, content: Hash): String {
        val entry = keyStore.getEntry(alias, null)
        if (entry !is KeyStore.PrivateKeyEntry) {
            throw IllegalArgumentException()
        }
        return CryptoUtils.encodeToBase64(
            Signature.getInstance("SHA256withECDSA").run {
                initSign(entry.privateKey)
                update(content)
                sign()
            }
        )
    }

    override fun getPublicKey(alias: String): ByteArray {
        return keyStore.getCertificate(alias).publicKey.encoded
    }
}