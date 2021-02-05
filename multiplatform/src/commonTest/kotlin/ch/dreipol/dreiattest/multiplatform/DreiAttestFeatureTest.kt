package ch.dreipol.dreiattest.multiplatform

import ch.dreipol.dreiattest.multiplatform.api.NetworkHelper
import ch.dreipol.dreiattest.multiplatform.mock.AttestServiceMock
import ch.dreipol.dreiattest.multiplatform.util.RequestHistory
import ch.dreipol.dreiattest.multiplatform.util.TEST_REQUEST_ENDPOINT
import ch.dreipol.dreiattest.multiplatform.util.launchAndWait
import ch.dreipol.dreiattest.multiplatform.util.requestClientMock
import io.ktor.client.features.json.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class DreiAttestFeatureTest {

    @Test
    fun testSingedRequest() {
        val attestService = AttestServiceMock()
        val requestHistory = RequestHistory()
        val client = requestClientMock(attestService, requestHistory)
        val headers = mutableListOf("test" to "test", "test2" to "test2")
        val body = "testBody"
        launchAndWait {
            client.get<String> {
                url {
                    takeFrom(TEST_REQUEST_ENDPOINT)
                }
                addHeaders(headers)
                this.body = defaultSerializer().write(body)
            }
        }

        addBodyHeaders(headers)
        assertEquals(1, attestService.requests.size)
        val request = attestService.requests[0]
        assertEquals("GET", request.requestMethod)
        assertEquals(TEST_REQUEST_ENDPOINT, request.url)
        assertEquals(headers, request.headers)
        assertNotNull(request.body)
        val jsonBody = request.body.decodeToString()
        val jsonBodyExpected = Json.encodeToString(body)
        assertEquals(jsonBodyExpected, jsonBody)
        assertEquals(1, requestHistory.requests.size)
        val clientRequest = requestHistory.requests[0]
        assertNotNull(clientRequest.headers[NetworkHelper.HEADER_SIGNATURE])
        assertEquals(attestService.uid, clientRequest.headers[NetworkHelper.HEADER_UID])
    }

    @Test
    fun testSingedRequestWithoutBody() {
        val attestService = AttestServiceMock()
        val requestHistory = RequestHistory()
        val client = requestClientMock(attestService, requestHistory)
        val headers = mutableListOf("test" to "test")
        launchAndWait {
            client.get<String> {
                url {
                    takeFrom(TEST_REQUEST_ENDPOINT)
                }
                addHeaders(headers)
            }
        }

        addBodyHeaders(headers)
        assertEquals(1, attestService.requests.size)
        val request = attestService.requests[0]
        assertEquals("GET", request.requestMethod)
        assertEquals(TEST_REQUEST_ENDPOINT, request.url)
        assertEquals(headers, request.headers)
        assertNull(request.body)
        assertEquals(1, requestHistory.requests.size)
        val clientRequest = requestHistory.requests[0]
        assertNotNull(clientRequest.headers[NetworkHelper.HEADER_SIGNATURE])
        assertEquals(attestService.uid, clientRequest.headers[NetworkHelper.HEADER_UID])
    }

    private fun addBodyHeaders(headers: MutableList<Pair<String, String>>) {
        headers.add("Accept" to "application/json")
        headers.add("Accept-Charset" to "UTF-8")
    }

    private fun HttpRequestBuilder.addHeaders(headers: List<Pair<String, String>>) {
        headers.forEach {
            this.headers.append(it.first, it.second)
        }
    }
}