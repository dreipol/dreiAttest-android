package ch.dreipol.dreiattest.multiplatform

import io.ktor.http.*

public class AttestService(private val keystore: Keystore) {

    private lateinit var sessionConfiguration: SessionConfiguration
    private lateinit var middlewareAPI: MiddlewareAPI

    public fun initWith(baseAddress: Url, sessionConfiguration: SessionConfiguration) {
        // TODO append with uuid with ; ??
        this.sessionConfiguration = sessionConfiguration
        this.middlewareAPI = MiddlewareAPI(baseAddress.toString())
    }

    public suspend fun buildSignature(): String {
        // TODO thread safe
        if (keystore.hasKeyPair(sessionConfiguration.uuid).not()) {
            val snonce = middlewareAPI.getNonce(sessionConfiguration.uuid)
            val publicKey = keystore.generateNewKeyPair(sessionConfiguration.uuid)
            val nonce = CryptoUtils.hashSHA256(sessionConfiguration.uuid + publicKey + snonce)
            val attestation = getDeviceAttestation(nonce, publicKey, sessionConfiguration.apiKey)
            middlewareAPI.setKey(attestation, sessionConfiguration.uuid)
        }
        // TODO  ?? sing (app_request ?? Body? was wenn kein body?
        return keystore.sign(sessionConfiguration.uuid, "Was genau?", getRequestNonce())
    }

    private suspend fun getRequestNonce(): String {
        // TODO level abfragen und bei Level.withNonce nonce beim server abholen
        return "00000000-0000-0000-0000-000000000000"
    }

}

public data class SessionConfiguration(val uuid: String, val level: Level = Level.SIGN_ONLY, val apiKey: String? = null)

public enum class Level {
    SIGN_ONLY,
}

internal expect fun AttestService.getDeviceAttestation(nonce: String, publicKey: String, apiKey: String?): Attestation