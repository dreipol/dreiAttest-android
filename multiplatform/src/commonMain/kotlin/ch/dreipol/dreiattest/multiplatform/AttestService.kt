package ch.dreipol.dreiattest.multiplatform

import io.ktor.http.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

public class AttestService(private val keystore: Keystore) {

    private companion object {
        private val mutex = Mutex()
    }

    private lateinit var sessionConfiguration: SessionConfiguration
    private lateinit var middlewareAPI: MiddlewareAPI

    public fun initWith(baseAddress: Url, sessionConfiguration: SessionConfiguration) {
        // TODO append with uuid with ; ??
        this.sessionConfiguration = sessionConfiguration
        this.middlewareAPI = MiddlewareAPI(baseAddress.toString())
    }

    public suspend fun buildSignature(): String {
        mutex.withLock {
            if (keystore.hasKeyPair(sessionConfiguration.uuid).not()) {
                val snonce = middlewareAPI.getNonce(sessionConfiguration.uuid)
                val publicKey = keystore.generateNewKeyPair(sessionConfiguration.uuid)
                val nonce = CryptoUtils.hashSHA256(sessionConfiguration.uuid + publicKey + snonce)
                val attestation = sessionConfiguration.deviceAttestationService.getAttestation(nonce, publicKey)
                middlewareAPI.setKey(attestation, sessionConfiguration.uuid)
            }
        }
        // TODO  ?? sing (app_request ?? Body? was wenn kein body?
        return keystore.sign(sessionConfiguration.uuid, "Was genau?", getRequestNonce())
    }

    private suspend fun getRequestNonce(): String {
        // TODO level abfragen und bei Level.withNonce nonce beim server abholen
        return "00000000-0000-0000-0000-000000000000"
    }

}

public data class SessionConfiguration(val uuid: String, val level: Level = Level.SIGN_ONLY,
    val deviceAttestationService: DeviceAttestationService)

public enum class Level {
    SIGN_ONLY,
}

public expect class DeviceAttestationService {
    internal suspend fun getAttestation(nonce: String, publicKey: String): Attestation
}