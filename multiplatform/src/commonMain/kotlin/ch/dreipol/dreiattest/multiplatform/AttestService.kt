package ch.dreipol.dreiattest.multiplatform

import ch.dreipol.dreiattest.multiplatform.api.Attestation
import ch.dreipol.dreiattest.multiplatform.api.MiddlewareAPI
import ch.dreipol.dreiattest.multiplatform.utils.*
import io.ktor.http.*
import io.ktor.utils.io.core.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

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

    public suspend fun buildSignature(): String {
        mutex.withLock {
            if (keystore.hasKeyPair(uid).not()) {
                val snonce = middlewareAPI.getNonce(uid)
                val publicKey = keystore.generateNewKeyPair(uid)
                val nonce = CryptoUtils.hashSHA256(uid + publicKey + snonce)
                val attestation = sessionConfiguration.deviceAttestationService.getAttestation(nonce, publicKey)
                middlewareAPI.setKey(attestation, uid)
            }
        }
        return signRequest()
    }

    public suspend fun deregister() {
        mutex.withLock {
            if (keystore.hasKeyPair(uid).not()) {
                return
            }
            val publicKey = keystore.getPublicKey(uid)
            val signature = signRequest()
            keystore.deleteKeyPair(uid)
            middlewareAPI.deleteKey(signature, uid, CryptoUtils.encodeToBase64(publicKey))
        }
    }

    private suspend fun signRequest(): String {
        val requestNonce = getRequestNonce()
        // TODO  ?? sing (app_request ?? Body? was wenn kein body?
        return keystore.sign(uid, "TODO".toByteArray())
    }

    private fun generateUid(user: String): String {
        val uuid = CryptoUtils.generateUuid()
        val uid = "$user;$uuid"
        SharedPreferences.setUid(user, uid)
        return uid
    }

    private suspend fun getRequestNonce(): String {
        // TODO check level and request nonce from middleware if configured
        return "00000000-0000-0000-0000-000000000000"
    }

}

public data class SessionConfiguration(val user: String, val level: Level = Level.SIGN_ONLY,
    val deviceAttestationService: DeviceAttestationService)

public enum class Level {
    SIGN_ONLY,
}

public expect class DeviceAttestationService {
    internal suspend fun getAttestation(nonce: String, publicKey: ByteArray): Attestation
}