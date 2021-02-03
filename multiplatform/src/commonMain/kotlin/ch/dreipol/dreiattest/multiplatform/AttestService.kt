package ch.dreipol.dreiattest.multiplatform

import ch.dreipol.dreiattest.multiplatform.api.Attestation
import ch.dreipol.dreiattest.multiplatform.api.MiddlewareAPI
import ch.dreipol.dreiattest.multiplatform.api.setSignature
import ch.dreipol.dreiattest.multiplatform.utils.*
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.util.*
import io.ktor.utils.io.core.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

public class AttestService(private val keystore: Keystore) {

    private companion object {
        private val mutex = Mutex()
    }

    private lateinit var sessionConfiguration: SessionConfiguration
    private lateinit var middlewareAPI: MiddlewareAPI
    internal lateinit var uid: String
        private set

    public fun initWith(baseAddress: Url, sessionConfiguration: SessionConfiguration) {
        this.sessionConfiguration = sessionConfiguration
        this.middlewareAPI = MiddlewareAPI(baseAddress.toString())
        uid = SharedPreferences.getUid(sessionConfiguration.user) ?: generateUid(sessionConfiguration.user)
    }

    public suspend fun buildSignature(url: String, requestMethod: String, headers: List<Pair<String, String>>, body: ByteArray?): String {
        mutex.withLock {
            if (keystore.hasKeyPair(uid).not()) {
                val snonce = middlewareAPI.getNonce(uid)
                val publicKey = keystore.generateNewKeyPair(uid)
                val nonce = CryptoUtils.hashSHA256(uid.toByteArray() + publicKey + snonce)
                val attestation = sessionConfiguration.deviceAttestationService.getAttestation(nonce, publicKey)
                middlewareAPI.setKey(attestation, uid)
            }
        }
        return signRequest(url, requestMethod, headers, body)
    }

    public suspend fun deregister() {
        mutex.withLock {
            if (keystore.hasKeyPair(uid).not()) {
                return
            }
            val publicKey = keystore.getPublicKey(uid)
            try {
                middlewareAPI.deleteKey(uid, CryptoUtils.encodeToBase64(publicKey)) {
                    val signature = signRequest(it.url.buildString(), it.readMethod(), it.readHeaders(), it.readBody())
                    it.setSignature(signature)
                }
            } finally {
                keystore.deleteKeyPair(uid)
            }
        }
    }

    private suspend fun signRequest(url: String, requestMethod: String, headers: List<Pair<String, String>>, body: ByteArray?): String {
        val requestNonce = getRequestNonce()
        val headerJson = Json.encodeToString(headers).toByteArray()
        val requestHash = CryptoUtils.hashSHA256(url.toByteArray() + requestMethod.toByteArray() + headerJson + (body ?: ByteArray(0)))
        return keystore.sign(uid, requestHash + requestNonce)
    }

    private fun generateUid(user: String): String {
        val uuid = CryptoUtils.generateUuid()
        val uid = "$user;$uuid"
        SharedPreferences.setUid(user, uid)
        return uid
    }

    private suspend fun getRequestNonce(): ByteArray {
        // TODO check level and request nonce from middleware if configured
        return "00000000-0000-0000-0000-000000000000".toByteArray()
    }

}

public data class SessionConfiguration(val user: String, val level: Level = Level.SIGN_ONLY,
    val deviceAttestationService: DeviceAttestationService)

public enum class Level {
    SIGN_ONLY,
}

public expect class DeviceAttestationService {
    internal suspend fun getAttestation(nonce: ByteArray, publicKey: ByteArray): Attestation
}