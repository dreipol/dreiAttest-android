package ch.dreipol.dreiattest.multiplatform.util

import ch.dreipol.dreiattest.multiplatform.AttestService
import ch.dreipol.dreiattest.multiplatform.DreiAttestFeature
import ch.dreipol.dreiattest.multiplatform.api.middlewareClientCreator
import io.ktor.client.*
import io.ktor.client.engine.mock.*
import io.ktor.client.features.json.*
import io.ktor.client.features.json.serializer.*
import io.ktor.client.request.*
import io.ktor.http.*

const val TEST_BASE_URL = "https://test.dreipol.ch"
const val TEST_NONCE = "testnonce"
const val TEST_REQUEST_ENDPOINT = "https://test.dreipol.ch/test"

fun mockMiddlewareClient(): RequestHistory {
    val requestHistory = RequestHistory()
    middlewareClientCreator = { createMockClient(requestHistory) }
    return requestHistory
}

data class RequestHistory(
    val requests: MutableList<HttpRequestData> = mutableListOf()
)


fun requestClientMock(attestService: AttestService, requestHistory: RequestHistory): HttpClient {
    return HttpClient(MockEngine) {
        install(JsonFeature) {
            serializer = KotlinxSerializer(
                kotlinx.serialization.json.Json {
                    ignoreUnknownKeys = true
                }
            )
        }
        install(DreiAttestFeature) {
            this.attestService = attestService
        }
        engine {
            addHandler { request ->
                requestHistory.requests.add(request)
                when (request.url.fullUrl) {
                    TEST_REQUEST_ENDPOINT -> {
                        val responseHeaders = headersOf("Content-Type" to listOf(ContentType.Text.Plain.toString()))
                        respond(TEST_NONCE, headers = responseHeaders)
                    }
                    else -> error("Unhandled ${request.url.fullUrl}")
                }
            }
        }
    }
}

private fun createMockClient(middlewareRequestHistory: RequestHistory): HttpClient {
    return HttpClient(MockEngine) {
        install(JsonFeature) {
            serializer = KotlinxSerializer(
                kotlinx.serialization.json.Json {
                    ignoreUnknownKeys = true
                }
            )
        }
        engine {
            addHandler { request ->
                middlewareRequestHistory.requests.add(request)
                when (request.url.fullUrl) {
                    "https://test.dreipol.ch/nonce" -> {
                        val responseHeaders = headersOf("Content-Type" to listOf(ContentType.Text.Plain.toString()))
                        respond(TEST_NONCE, headers = responseHeaders)
                    }
                    "https://test.dreipol.ch/key" -> {
                        respondOk()
                    }
                    else -> error("Unhandled ${request.url.fullUrl}")
                }
            }
        }
    }
}

private val Url.hostWithPortIfRequired: String get() = if (port == protocol.defaultPort) host else hostWithPort
private val Url.fullUrl: String get() = "${protocol.name}://$hostWithPortIfRequired$fullPath"