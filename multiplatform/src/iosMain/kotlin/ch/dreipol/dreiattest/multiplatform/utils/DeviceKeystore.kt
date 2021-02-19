package ch.dreipol.dreiattest.multiplatform.utils

import platform.DeviceCheck.DCAppAttestService
import platform.Foundation.NSData
import platform.Foundation.NSError
import platform.Foundation.NSUserDefaults
import platform.Foundation.base64EncodedStringWithOptions
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

public actual class DeviceKeystore : Keystore {
    private val service = DCAppAttestService.sharedService

    init {
        assert(service.isSupported())
    }

    private fun keyFor(alias: String): String = "dreiAttest.Key.keyId(uid: \"${alias}\")"

    override suspend fun generateNewKeyPair(alias: String): ByteArray {
        val result: Pair<String?, NSError?> = suspendCoroutine { continuation ->
            service.generateKeyWithCompletionHandler { keyId, error ->
                continuation.resume(Pair(keyId, error))
            }
        }

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
        val keyId = NSUserDefaults.standardUserDefaults.stringForKey(keyFor(alias))
            ?: throw IllegalArgumentException()

        val result: Pair<NSData?, NSError?> = suspendCoroutine { continuation ->
            service.generateAssertion(keyId, content) { assertion, error ->
                continuation.resume(Pair(assertion, error))
            }
        }

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