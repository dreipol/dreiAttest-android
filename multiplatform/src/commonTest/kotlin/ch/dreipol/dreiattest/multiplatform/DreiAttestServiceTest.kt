package ch.dreipol.dreiattest.multiplatform

import ch.dreipol.dreiattest.multiplatform.api.Attestation
import ch.dreipol.dreiattest.multiplatform.util.*
import ch.dreipol.dreiattest.multiplatform.utils.CryptoUtils
import ch.dreipol.dreiattest.multiplatform.utils.SharedPreferences
import ch.dreipol.dreiattest.multiplatform.utils.encodeToBase64
import com.russhwolf.settings.MockSettings
import com.russhwolf.settings.Settings
import io.ktor.http.content.*
import io.ktor.utils.io.concurrent.*
import io.ktor.utils.io.core.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlin.test.*

class DreiAttestServiceTest {

    private val testUser = "testuser"
    private val testUser2 = "testuser2"
    private lateinit var requestHistory: RequestHistory
    private lateinit var keyStore: KeystoreMock
    private lateinit var mockSettings: Settings

    @BeforeTest
    fun prepare() {
        requestHistory = mockMiddlewareClient()
        keyStore = KeystoreMock()
        mockSettings = MockSettings.Factory().create()
    }

    @Test
    fun testUidGeneration() {
        val attestService = DreiAttestService(keyStore, mockSettings)
        attestService.initWith(TEST_BASE_URL, SessionConfiguration(testUser, deviceAttestationService = AttestationServiceMock()))
        attestService.initWith(TEST_BASE_URL, SessionConfiguration(testUser2, deviceAttestationService = AttestationServiceMock()))

        val sharedPreferences = SharedPreferences(mockSettings)
        val uid1 = sharedPreferences.getUid(testUser)
        val uid2 = sharedPreferences.getUid(testUser2)
        assertNotNull(uid1)
        assertNotNull(uid2)

        val user1 = uid1.split(";")[0]
        val user2 = uid2.split(";")[0]
        assertEquals(testUser, user1)
        assertEquals(testUser2, user2)

        attestService.initWith(TEST_BASE_URL, SessionConfiguration(testUser, deviceAttestationService = AttestationServiceMock()))
        assertEquals(uid1, sharedPreferences.getUid(testUser))
    }

    @Test
    fun testKeyGeneration() {
        val attestService = DreiAttestService(keyStore, mockSettings)
        attestService.initWith(TEST_BASE_URL, SessionConfiguration(testUser, deviceAttestationService = AttestationServiceMock()))

        val headers = listOf("test" to "test")
        val url = "$TEST_BASE_URL/test"
        val requestMethod = "POST"
        launchAndWait {
            attestService.buildSignature(url, requestMethod, headers, null)
        }

        assertEquals(2, requestHistory.requests.size)
        assertEquals(1, keyStore.keys.size)
        val publicKey = keyStore.keys.toList().first().second.toByteArray()
        val requestBody = requestHistory.requests[1].body
        assertTrue(requestBody is OutgoingContent.ByteArrayContent)
        val attestation = Json.decodeFromString<Attestation>(requestBody.bytes().decodeToString())
        assertEquals(CryptoUtils.encodeToBase64(publicKey), attestation.publicKey)

        launchAndWait {
            attestService.buildSignature(url, requestMethod, headers, null)
        }
        assertEquals(2, requestHistory.requests.size)
        assertEquals(1, keyStore.keys.size)
    }

    @Test
    fun testShouldByPass() {
        val attestService = DreiAttestService(keyStore, mockSettings)
        attestService.initWith(TEST_BASE_URL, SessionConfiguration(testUser, deviceAttestationService = AttestationServiceMock()))

        assertFalse(attestService.shouldByPass("$TEST_BASE_URL/test"))
        assertTrue(attestService.shouldByPass("https://test2.dreipol.ch/test"))
    }

    @Test
    fun testDeregister() {
        val attestService = DreiAttestService(keyStore, mockSettings)
        val sharedPreferences = SharedPreferences(mockSettings)
        val uid = "testuid"
        sharedPreferences.setUid(testUser, uid)
        attestService.initWith(TEST_BASE_URL, SessionConfiguration(testUser, deviceAttestationService = AttestationServiceMock()))
        keyStore.generateNewKeyPair(uid)

        assertTrue(keyStore.hasKeyPair(uid))
        val publicKey = keyStore.getPublicKey(uid)

        launchAndWait {
            attestService.deregister()
        }

        assertFalse(keyStore.hasKeyPair(uid))
        assertEquals(1, requestHistory.requests.size)
        val deleteKeyRequest = requestHistory.requests[0]
        val body = deleteKeyRequest.body
        assertTrue(body is OutgoingContent.ByteArrayContent)
        assertEquals(CryptoUtils.encodeToBase64(publicKey), body.bytes().decodeToString())
    }

}