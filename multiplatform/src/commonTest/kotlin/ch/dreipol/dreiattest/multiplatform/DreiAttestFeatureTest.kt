package ch.dreipol.dreiattest.multiplatform

import ch.dreipol.dreiattest.multiplatform.api.NetworkHelper
import ch.dreipol.dreiattest.multiplatform.api.signableHeaders
import ch.dreipol.dreiattest.multiplatform.mock.AttestServiceMock
import ch.dreipol.dreiattest.multiplatform.mock.AttestationProviderMock
import ch.dreipol.dreiattest.multiplatform.mock.SystemInfoMock
import ch.dreipol.dreiattest.multiplatform.util.TEST_BASE_URL
import ch.dreipol.dreiattest.multiplatform.util.TEST_BYPASS_ENDPOINT
import ch.dreipol.dreiattest.multiplatform.util.TEST_REQUEST_ENDPOINT
import ch.dreipol.dreiattest.multiplatform.util.requestClientMock
import io.ktor.client.call.*
import io.ktor.client.engine.mock.*
import io.ktor.client.request.*
import io.ktor.client.utils.*
import io.ktor.http.*
import io.ktor.util.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.*

const val LIBRARY_VERSION = "kotlin-1.0.3"

class DreiAttestFeatureTest {

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
                assertEquals(body, jsonBody)
            }
            val response: String = client.get {
                contentType(ContentType.Application.Json)
                url {
                    takeFrom(TEST_REQUEST_ENDPOINT)
                }
                addHeaders(headers)
                setBody(body)
            }.body()
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
            val response: String = client.get {
                url {
                    takeFrom(TEST_REQUEST_ENDPOINT)
                }
                addHeaders(headers)
            }.body()
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
            val response: String = client.get {
                url {
                    takeFrom(TEST_BYPASS_ENDPOINT)
                }
                addHeaders(headers)
            }.body()
        }
    }

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
                addSystemInfoHeaders(expectedHeaders)
                addSharedSecretHeader(expectedHeaders, sharedSecret)
                assertEquals(expectedHeaders, it.headers.flattenEntries())
            }
            val response: String = client.get {
                url {
                    takeFrom(TEST_REQUEST_ENDPOINT)
                }
                addHeaders(headers)
            }.body()
        }
    }

    private fun addBodyHeaders(headers: MutableList<Pair<String, String>>) {
        headers.add("Accept" to "application/json")
        headers.add("Accept-Charset" to "UTF-8")
    }

    private fun addSystemInfoHeaders(headers: MutableList<Pair<String, String>>) {
        headers.add(NetworkHelper.HEADER_LIBRARY_VERSION to LIBRARY_VERSION)
        headers.add(NetworkHelper.HEADER_APP_VERSION to SystemInfoMock.appVersion)
        headers.add(NetworkHelper.HEADER_APP_BUILD to SystemInfoMock.appBuild)
        headers.add(NetworkHelper.HEADER_APP_IDENTIFIER to SystemInfoMock.appIdentifier)
        headers.add(NetworkHelper.HEADER_OS to SystemInfoMock.osVersion)
    }

    private fun addDreiattestHeaders(headers: MutableList<Pair<String, String>>) {
        addSystemInfoHeaders(headers)
        headers.add(NetworkHelper.HEADER_UID to "test")
        val headerKeys = headers.signableHeaders().map { it.first }.toMutableList()
        headerKeys.add(NetworkHelper.HEADER_USER_HEADERS)
        headers.add(NetworkHelper.HEADER_USER_HEADERS to headerKeys.sortedBy { it }.joinToString(","))
        headers.add(NetworkHelper.HEADER_SIGNATURE to "signature")
        headers.add(NetworkHelper.HEADER_NONCE to "00000000-0000-0000-0000-000000000000")
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