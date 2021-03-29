package ch.dreipol.dreiattest.multiplatform

import ch.dreipol.dreiattest.multiplatform.api.*
import ch.dreipol.dreiattest.multiplatform.utils.*
import com.russhwolf.settings.Settings
import com.russhwolf.settings.invoke
import io.ktor.utils.io.core.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

public interface AttestService {
    public val uid: String
    public fun initWith(baseAddress: String, sessionConfiguration: SessionConfiguration)
    public suspend fun buildSignature(request: Request, snonce: ByteArray): String
    public suspend fun deregister()
    public fun shouldByPass(url: String): Boolean
    public suspend fun getRequestNonce(): ByteArray
}

public class DreiAttestService(private val keystore: Keystore, settings: Settings = Settings()) : AttestService {

    internal companion object {
        internal val usernamePattern = Regex("([a-z]|[A-Z]|[0-9]|[.]|[_]|[-]|[@]){0,255}")
    }

    public override val uid: String
        get() = uidBackingField
    private val sharedPreferences = SharedPreferences(settings)
    private lateinit var sessionConfiguration: SessionConfiguration
    private lateinit var middlewareAPI: MiddlewareAPI
    private lateinit var baseAddress: String
    private lateinit var uidBackingField: String
    private val mutex = Mutex()

    override fun initWith(baseAddress: String, sessionConfiguration: SessionConfiguration) {
        validateUsername(sessionConfiguration.user)
        this.sessionConfiguration = sessionConfiguration
        this.middlewareAPI = MiddlewareAPI(baseAddress)
        this.baseAddress = baseAddress
        uidBackingField = sharedPreferences.getUid(sessionConfiguration.user) ?: generateUid(sessionConfiguration.user)
    }

    override fun shouldByPass(url: String): Boolean {
        return url.contains(baseAddress).not()
    }

    override suspend fun buildSignature(
        request: Request,
        snonce: ByteArray,
    ): String {
        mutex.withLock {
            if (keystore.hasKeyPair(uid).not()) {
                val signatureNonce = middlewareAPI.getNonce(uid)
                val publicKey = keystore.generateNewKeyPair(uid)
                val nonce = CryptoUtils.hashSHA256(uid.toByteArray() + publicKey + signatureNonce)
                val attestation = sessionConfiguration.deviceAttestationService.getAttestation(nonce, publicKey)
                middlewareAPI.setKey(attestation, uid, signatureNonce)
            }
        }
        return signRequest(request, snonce)
    }

    override suspend fun deregister() {
        mutex.withLock {
            if (keystore.hasKeyPair(uid).not()) {
                return
            }
            val publicKey = keystore.getPublicKey(uid)
            val snonce = getRequestNonce()
            try {
                middlewareAPI.deleteKey(uid, CryptoUtils.encodeToBase64(publicKey), snonce) {
                    val signature = signRequest(Request(it.readUrl(), it.readMethod(), it.readHeaders(), it.readBody()), snonce)
                    it.setSignature(signature)
                }
            } finally {
                keystore.deleteKeyPair(uid)
            }
        }
    }

    override suspend fun getRequestNonce(): ByteArray {
        // TODO check level and request nonce from middleware if configured
        return "00000000-0000-0000-0000-000000000000".toByteArray()
    }

    private suspend fun signRequest(request: Request, snonce: ByteArray): String {
        val headerJson = Json.encodeToString(request.headers).toByteArray()
        val requestHash = CryptoUtils.hashSHA256(
            request.url.toByteArray() + request.requestMethod.toByteArray() + headerJson + (request.body ?: ByteArray(0)))
        return keystore.sign(uid, requestHash + snonce)
    }

    private fun generateUid(user: String): String {
        val uuid = CryptoUtils.generateUuid()
        val uid = "$user;$uuid"
        sharedPreferences.setUid(user, uid)
        return uid
    }

    private fun validateUsername(username: String) {
        if (usernamePattern.matches(username).not()) {
            throw InvalidUsernameError(username)
        }
    }
}

public data class SessionConfiguration(
    val user: String,
    val level: Level = Level.SIGN_ONLY,
    val deviceAttestationService: AttestationService
)

public enum class Level {
    SIGN_ONLY,
}

public interface AttestationService {
    public suspend fun getAttestation(nonce: Hash, publicKey: ByteArray): Attestation
}

public expect class DeviceAttestationService : AttestationService