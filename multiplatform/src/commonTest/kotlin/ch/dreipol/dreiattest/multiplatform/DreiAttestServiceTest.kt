package ch.dreipol.dreiattest.multiplatform

import ch.dreipol.dreiattest.multiplatform.api.dto.Attestation
import ch.dreipol.dreiattest.multiplatform.mock.AttestationProviderMock
import ch.dreipol.dreiattest.multiplatform.mock.KeystoreMock
import ch.dreipol.dreiattest.multiplatform.util.TEST_BASE_URL
import ch.dreipol.dreiattest.multiplatform.util.TEST_KEY_ENDPOINT
import ch.dreipol.dreiattest.multiplatform.util.mockMiddlewareClient
import ch.dreipol.dreiattest.multiplatform.utils.CryptoUtils
import ch.dreipol.dreiattest.multiplatform.utils.Request
import ch.dreipol.dreiattest.multiplatform.utils.SharedPreferences
import ch.dreipol.dreiattest.multiplatform.utils.encodeToBase64
import com.russhwolf.settings.MockSettings
import com.russhwolf.settings.Settings
import io.ktor.http.content.*
import io.ktor.utils.io.core.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlin.test.*

class DreiAttestServiceTest {

    private val testUser = "testuser"
    private val testUser2 = "testuser2"
    private lateinit var mockSettings: Settings

    @BeforeTest
    fun prepare() {
        KeystoreMock.keys.clear()
        mockSettings = MockSettings.Factory().create()
    }

    @Test
    fun testUidGeneration() {
        val attestService = DreiAttestService(KeystoreMock, mockSettings)
        attestService.initWith(TEST_BASE_URL, SessionConfiguration(testUser, deviceAttestationProvider = AttestationProviderMock()))
        attestService.initWith(TEST_BASE_URL, SessionConfiguration(testUser2, deviceAttestationProvider = AttestationProviderMock()))

        val sharedPreferences = SharedPreferences(mockSettings)
        val uid1 = sharedPreferences.getUid(testUser)
        val uid2 = sharedPreferences.getUid(testUser2)
        assertNotNull(uid1)
        assertNotNull(uid2)

        val user1 = uid1.split(";")[0]
        val user2 = uid2.split(";")[0]
        assertEquals(testUser, user1)
        assertEquals(testUser2, user2)

        attestService.initWith(TEST_BASE_URL, SessionConfiguration(testUser, deviceAttestationProvider = AttestationProviderMock()))
        assertEquals(uid1, sharedPreferences.getUid(testUser))
    }

    @Test
    fun testKeyGeneration() {
        runBlocking {
            val attestService = DreiAttestService(KeystoreMock, mockSettings)
            attestService.initWith(TEST_BASE_URL, SessionConfiguration(testUser, deviceAttestationProvider = AttestationProviderMock()))

            val headers = listOf("test" to "test")
            val url = "$TEST_BASE_URL/test"
            val requestMethod = "POST"
            mockMiddlewareClient {
                when (url) {
                    TEST_KEY_ENDPOINT -> {
                        assertEquals(1, KeystoreMock.keys.size)
                        val publicKey = KeystoreMock.keys.toList().first().second.toByteArray()
                        val requestBody = it.body
                        assertTrue(requestBody is OutgoingContent.ByteArrayContent)
                        val attestation = Json.decodeFromString<Attestation>(requestBody.bytes().decodeToString())
                        assertEquals(CryptoUtils.encodeToBase64(publicKey), attestation.publicKey)
                    }
                }
            }
            attestService.buildSignature(Request(url, requestMethod, headers, null), attestService.getRequestNonce())
            // assertEquals(2, counterContext.count)

            mockMiddlewareClient {
            }
            attestService.buildSignature(Request(url, requestMethod, headers, null), attestService.getRequestNonce())
            // assertEquals(2, counterContext.count)
            assertEquals(1, KeystoreMock.keys.size)
        }
    }

    @Test
    fun testShouldByPass() {
        val attestService = DreiAttestService(KeystoreMock, mockSettings)
        attestService.initWith(TEST_BASE_URL, SessionConfiguration(testUser, deviceAttestationProvider = AttestationProviderMock()))

        assertTrue(attestService.shouldHandle("$TEST_BASE_URL/test"))
        assertFalse(attestService.shouldHandle("https://test2.dreipol.ch/test"))
    }

    @Test
    fun testDeregister() {
        runBlocking {
            val attestService = DreiAttestService(KeystoreMock, mockSettings)
            val sharedPreferences = SharedPreferences(mockSettings)
            val uid = "testuid"
            sharedPreferences.setUid(testUser, uid)
            attestService.initWith(TEST_BASE_URL, SessionConfiguration(testUser, deviceAttestationProvider = AttestationProviderMock()))
            KeystoreMock.generateNewKeyPair(uid)

            assertTrue(KeystoreMock.hasKeyPair(uid))
            val publicKey = KeystoreMock.getPublicKey(uid)

            mockMiddlewareClient {
                val body = it.body
                assertTrue(body is OutgoingContent.ByteArrayContent)
                assertEquals(CryptoUtils.encodeToBase64(publicKey), body.bytes().decodeToString())
            }
            attestService.deregister()
            assertFalse(KeystoreMock.hasKeyPair(uid))
            // assertEquals(1, it.requests.size)
        }
    }

    @Test
    fun testUsernameValidation() {
        val attestService = DreiAttestService(KeystoreMock, mockSettings)

        var username = getRandomUsername(20)
        initWithUsername(attestService, username)

        username = getRandomUsername(255)
        initWithUsername(attestService, username)

        username = getRandomUsername(256)
        var error = assertFails {
            initWithUsername(attestService, username)
        }
        assertTrue(error is InvalidUsernameError)

        username = ""
        initWithUsername(attestService, username)

        username = getRandomUsername(100, ';')
        error = assertFails {
            initWithUsername(attestService, username)
        }
        assertTrue(error is InvalidUsernameError)
    }

    private fun initWithUsername(attestService: DreiAttestService, userName: String) {
        attestService.initWith(TEST_BASE_URL, SessionConfiguration(userName, deviceAttestationProvider = AttestationProviderMock()))
    }

    private fun getRandomUsername(length: Int, invalidCharacter: Char? = null): String {
        val allowedChars = ('A'..'Z') + ('a'..'z') + ('0'..'9') + '-' + '_' + '.' + '@'
        val randomLength = length - if (invalidCharacter == null) 0 else 1
        val random = (1..randomLength)
            .map { allowedChars.random() }
            .joinToString("")
        return if (invalidCharacter == null) random else random + invalidCharacter
    }
}