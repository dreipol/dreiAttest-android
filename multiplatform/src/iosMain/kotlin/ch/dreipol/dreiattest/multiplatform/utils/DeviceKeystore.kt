package ch.dreipol.dreiattest.multiplatform.utils

import kotlinx.coroutines.CompletableDeferred
import platform.DeviceCheck.DCAppAttestService
import platform.Foundation.NSData
import platform.Foundation.NSError
import platform.Foundation.NSUserDefaults
import platform.Foundation.base64EncodedStringWithOptions
import kotlin.native.concurrent.freeze

private class KeyGenCompletable {
    private val completable = CompletableDeferred<Pair<String?, NSError?>>()

    fun receiveKey(keyId: String?, error: NSError?) {
        completable.complete(Pair(keyId, error))
    }

    suspend fun await() = completable.await()
}

private class SignatureCompletable {
    private val completable = CompletableDeferred<Pair<NSData?, NSError?>>()

    fun receiveSignature(assertion: NSData?, error: NSError?) {
        completable.complete(Pair(assertion, error))
    }

    suspend fun await() = completable.await()
}

public actual class DeviceKeystore : Keystore {
    private val service = DCAppAttestService.sharedService

    private fun keyFor(alias: String): String = "dreiAttest.Key.keyId(uid: \"${alias}\")"

    override suspend fun generateNewKeyPair(alias: String): ByteArray {
        assert(service.isSupported())

        val completable = KeyGenCompletable()
        val receiver = completable::receiveKey
        receiver.freeze()
        service.generateKeyWithCompletionHandler(receiver)

        val result = completable.await()
        result.second?.let {
            throw Exception(it.description())
        }

        val keyId = result.first ?: throw IllegalStateException()
        NSUserDefaults.standardUserDefaults.setObject(keyId, keyFor(alias))

        return Conversion.base64ToByteArray(keyId)
    }

    override fun deleteKeyPair(alias: String) {
        NSUserDefaults.standardUserDefaults.removeObjectForKey(keyFor(alias))
    }

    override fun hasKeyPair(alias: String): Boolean {
        return NSUserDefaults.standardUserDefaults.objectForKey(keyFor(alias)) != null
    }

    override suspend fun sign(alias: String, content: Hash): String {
        assert(service.isSupported())

        val keyId = NSUserDefaults.standardUserDefaults.stringForKey(keyFor(alias))
            ?: throw IllegalArgumentException()

        val completable = SignatureCompletable()
        val receiver = completable::receiveSignature
        receiver.freeze()
        service.generateAssertion(keyId, content, receiver)

        val result = completable.await()
        result.second?.let {
            throw Exception(it.description())
        }

        val assertion = result.first ?: throw IllegalStateException()
        return assertion.base64EncodedStringWithOptions(0)
    }

    override fun getPublicKey(alias: String): ByteArray {
        val keyId = NSUserDefaults.standardUserDefaults.stringForKey(keyFor(alias))
            ?: throw IllegalArgumentException()
        return Conversion.base64ToByteArray(keyId)
    }
}