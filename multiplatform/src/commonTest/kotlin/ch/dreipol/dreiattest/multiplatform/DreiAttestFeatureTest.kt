package ch.dreipol.dreiattest.multiplatform

import ch.dreipol.dreiattest.multiplatform.api.NetworkHelper
import ch.dreipol.dreiattest.multiplatform.mock.AttestServiceMock
import ch.dreipol.dreiattest.multiplatform.mock.AttestationProviderMock
import ch.dreipol.dreiattest.multiplatform.util.TEST_BASE_URL
import ch.dreipol.dreiattest.multiplatform.util.TEST_BYPASS_ENDPOINT
import ch.dreipol.dreiattest.multiplatform.util.TEST_REQUEST_ENDPOINT
import ch.dreipol.dreiattest.multiplatform.util.requestClientMock
import ch.dreipol.dreiattest.multiplatform.utils.CryptoUtils
import ch.dreipol.dreiattest.multiplatform.utils.encodeToBase64
import io.ktor.client.engine.mock.*
import io.ktor.client.features.json.*
import io.ktor.client.request.*
import io.ktor.client.utils.*
import io.ktor.http.*
import io.ktor.util.*
import io.ktor.utils.io.core.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.*

class DreiAttestFeatureTest {

    @KtorExperimentalAPI
    @Test
    fun testSingedRequest() {
        runBlocking {
            val attestService = AttestServiceMock()
            attestService.initWith(TEST_BASE_URL, SessionConfiguration("testUser", deviceAttestationProvider = AttestationProviderMock()))
            val headers = listOf("test" to "test", "test2" to "test2")
            val body = "testBody"
            val client = requestClientMock(attestService) {
                assertNotNull(it.headers[NetworkHelper.HEADER_SIGNATURE])
                assertEquals(attestService.uid, it.headers[NetworkHelper.HEADER_UID])
                assertEquals("GET", it.method.value)
                assertEquals(TEST_REQUEST_ENDPOINT, it.url.toString())
                val expectedHeaders = headers.toMutableList()
                addBodyHeaders(expectedHeaders)
                addDreiattestHeaders(expectedHeaders)
                assertEquals(expectedHeaders, it.headers.flattenEntries())
                assertNotNull(it.body)
                val jsonBody = it.body.toByteArray().decodeToString()
                val jsonBodyExpected = Json.encodeToString(body)
                assertEquals(jsonBodyExpected, jsonBody)
            }
            client.get<String> {
                url {
                    takeFrom(TEST_REQUEST_ENDPOINT)
                }
                addHeaders(headers)
                this.body = defaultSerializer().write(body)
            }
        }
    }

    @Test
    fun testSingedRequestWithoutBody() {
        runBlocking {
            val attestService = AttestServiceMock()
            attestService.initWith(TEST_BASE_URL, SessionConfiguration("testUser", deviceAttestationProvider = AttestationProviderMock()))
            val headers = listOf("test" to "test")
            val client = requestClientMock(attestService) {
                assertNotNull(it.headers[NetworkHelper.HEADER_SIGNATURE])
                assertEquals(attestService.uid, it.headers[NetworkHelper.HEADER_UID])
                assertEquals("GET", it.method.value)
                assertEquals(TEST_REQUEST_ENDPOINT, it.url.toString())
                val expectedHeaders = headers.toMutableList()
                addBodyHeaders(expectedHeaders)
                addDreiattestHeaders(expectedHeaders)
                assertEquals(expectedHeaders, it.headers.flattenEntries())
                assertTrue(it.body is EmptyContent)
            }
            client.get<String> {
                url {
                    takeFrom(TEST_REQUEST_ENDPOINT)
                }
                addHeaders(headers)
            }
        }
    }

    @Test
    fun testBypass() {
        runBlocking {
            val attestService = AttestServiceMock()
            attestService.initWith(TEST_BASE_URL, SessionConfiguration("testUser", deviceAttestationProvider = AttestationProviderMock()))
            val client = requestClientMock(attestService) {
                assertNull(it.headers[NetworkHelper.HEADER_SIGNATURE])
                assertNull(it.headers[NetworkHelper.HEADER_UID])
            }
            val headers = mutableListOf("test" to "test")
            client.get<String> {
                url {
                    takeFrom(TEST_BYPASS_ENDPOINT)
                }
                addHeaders(headers)
            }
        }
    }

    @KtorExperimentalAPI
    @Test
    fun testSharedSecret() {
        runBlocking {
            val attestService = AttestServiceMock()
            val sharedSecret = "sharedSecret"
            attestService.initWith(
                TEST_BASE_URL,
                SessionConfiguration("testUser", deviceAttestationProvider = AttestationProviderMock(), bypassSecret = sharedSecret)
            )
            val headers = listOf("test" to "test")
            val client = requestClientMock(attestService) {
                val expectedHeaders = headers.toMutableList()
                addBodyHeaders(expectedHeaders)
                addSharedSecretHeader(expectedHeaders, sharedSecret)
                assertEquals(expectedHeaders, it.headers.flattenEntries())
            }
            client.get<String> {
                url {
                    takeFrom(TEST_REQUEST_ENDPOINT)
                }
                addHeaders(headers)
            }
        }
    }

    private fun addBodyHeaders(headers: MutableList<Pair<String, String>>) {
        headers.add("Accept" to "application/json")
        headers.add("Accept-Charset" to "UTF-8")
    }

    private fun addDreiattestHeaders(headers: MutableList<Pair<String, String>>) {
        headers.add(NetworkHelper.HEADER_UID to "test")
        val headerKeys = headers.map { it.first }.toMutableList()
        headerKeys.add(NetworkHelper.HEADER_USER_HEADERS)
        headers.add(NetworkHelper.HEADER_USER_HEADERS to headerKeys.sortedBy { it }.joinToString(","))
        headers.add(NetworkHelper.HEADER_SIGNATURE to "signature")
        headers.add(NetworkHelper.HEADER_NONCE to CryptoUtils.encodeToBase64("00000000-0000-0000-0000-000000000000".toByteArray()))
    }

    private fun addSharedSecretHeader(headers: MutableList<Pair<String, String>>, sharedSecret: String) {
        headers.add(NetworkHelper.HEADER_SHARED_SECRET to sharedSecret)
    }

    private fun HttpRequestBuilder.addHeaders(headers: List<Pair<String, String>>) {
        headers.forEach {
            this.headers.append(it.first, it.second)
        }
    }
}