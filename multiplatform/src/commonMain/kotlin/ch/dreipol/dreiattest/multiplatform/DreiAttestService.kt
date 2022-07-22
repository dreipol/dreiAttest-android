package ch.dreipol.dreiattest.multiplatform

import ch.dreipol.dreiattest.multiplatform.api.*
import ch.dreipol.dreiattest.multiplatform.api.dto.Attestation
import ch.dreipol.dreiattest.multiplatform.utils.*
import com.russhwolf.settings.Settings
import io.ktor.utils.io.charsets.*
import io.ktor.utils.io.core.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

public class UnsupportedException: Exception("Attestation is not supported on this device!")

public interface AttestService {
    public val uid: String
    public val systemInfo: SystemInfo
    public fun initWith(baseAddress: String, sessionConfiguration: SessionConfiguration)
    public suspend fun buildSignature(request: Request, snonce: String): String
    public suspend fun deregister()
    public fun shouldHandle(url: String): Boolean
    public suspend fun getRequestNonce(): String
    public fun getBypassSecret(): String?
}

public class DreiAttestService(private val keystore: Keystore = DeviceKeystore(), settings: Settings = Settings()) : AttestService {

    internal companion object {
        internal val usernamePattern = Regex("([a-z]|[A-Z]|[0-9]|[.]|[_]|[-]|[@]){0,255}")
        private const val BYPASS_SECRET_ENV = "DREIATTEST_BYPASS_SECRET"
    }

    public override val uid: String
        get() = uidBackingField
    override val systemInfo: SystemInfo
        get() = sessionConfiguration.deviceAttestationProvider.systemInfo
    private val sharedPreferences = SharedPreferences(settings)
    private var bypassSecret: String? = null
    private lateinit var sessionConfiguration: SessionConfiguration
    private lateinit var middlewareAPI: MiddlewareAPI
    private lateinit var baseAddress: String
    private lateinit var uidBackingField: String
    private val mutex = Mutex()

    override fun initWith(baseAddress: String, sessionConfiguration: SessionConfiguration) {
        bypassSecret = sessionConfiguration.bypassSecret ?: SystemUtils.getEnvVariable(BYPASS_SECRET_ENV)
        if (bypassSecret == null && !sessionConfiguration.deviceAttestationProvider.isSupported) {
            throw UnsupportedException()
        }

        validateUsername(sessionConfiguration.user)
        this.sessionConfiguration = sessionConfiguration
        this.middlewareAPI = MiddlewareAPI(baseAddress + "/dreiattest", sessionConfiguration.deviceAttestationProvider.systemInfo)
        this.baseAddress = baseAddress
        uidBackingField = sharedPreferences.getUid(sessionConfiguration.user) ?: generateUid(sessionConfiguration.user)
    }

    override fun shouldHandle(url: String): Boolean {
        return url.contains(baseAddress)
    }

    override fun getBypassSecret(): String? {
        return bypassSecret
    }

    override suspend fun buildSignature(
        request: Request,
        snonce: String,
    ): String {
        if (keystore.hasKeyPair(uid).not()) {
            mutex.withLock {
                if (keystore.hasKeyPair(uid)) { return@withLock }

                val signatureNonce = middlewareAPI.getNonce(uid).trim('"')
                val publicKey = CryptoUtils.encodeToBase64(keystore.generateNewKeyPair(uid))
                val nonce = CryptoUtils.hashSHA256((uid + publicKey + signatureNonce).toByteArray(Charsets.UTF_8))
                val attestation = sessionConfiguration.deviceAttestationProvider.getAttestation(nonce, publicKey)
                try {
                    middlewareAPI.setKey(attestation, uid, signatureNonce)
                } catch (t: Throwable) {
                    keystore.deleteKeyPair(uid)
                    throw t
                }
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

    override suspend fun getRequestNonce(): String {
        // TODO check level and request nonce from middleware if configured
        return "00000000-0000-0000-0000-000000000000"
    }

    private suspend fun signRequest(request: Request, snonce: String): String {
        val headerJson = JsonUtil.sortedJsonData(request.signableHeaders().toMap())

        val urlWithoutProtocol = request.url.removeProtocolFromUrl()
        val requestHash = CryptoUtils.hashSHA256(
            urlWithoutProtocol.toByteArray() + request.requestMethod.toByteArray() + headerJson + (request.body ?: ByteArray(0))
        )
        val nonce = CryptoUtils.rehashSHA256(requestHash + snonce.toByteArray(Charsets.UTF_8))
        return keystore.sign(uid, nonce)
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
    val user: String = "",
    val level: Level = Level.SIGN_ONLY,
    val deviceAttestationProvider: AttestationProvider,
    val bypassSecret: String? = null,
)

public enum class Level {
    SIGN_ONLY,
}

public interface AttestationProvider {
    public val systemInfo: SystemInfo
    public val isSupported: Boolean
    public suspend fun getAttestation(nonce: Hash, publicKey: String): Attestation
}