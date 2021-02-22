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
const val TEST_BYPASS_ENDPOINT = "https://bypass.dreipol.ch/"
const val TEST_NONCE_ENDPOINT = "https://test.dreipol.ch/nonce"
const val TEST_KEY_ENDPOINT = "https://test.dreipol.ch/key"

fun mockMiddlewareClient(assertions: suspend (HttpRequestData) -> Unit) {
    middlewareClientCreator = { createMockClient(assertions) }
}

fun requestClientMock(attestService: AttestService, assertions: suspend (HttpRequestData) -> Unit): HttpClient {
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
                assertions(request)
                when (request.url.fullUrl) {
                    TEST_REQUEST_ENDPOINT -> {
                        val responseHeaders = headersOf("Content-Type" to listOf(ContentType.Text.Plain.toString()))
                        respond(TEST_NONCE, headers = responseHeaders)
                    }
                    TEST_BYPASS_ENDPOINT -> {
                        respondOk()
                    }
                    else -> error("Unhandled ${request.url.fullUrl}")
                }
            }
        }
    }
}

private fun createMockClient(assertions: suspend (HttpRequestData) -> Unit): HttpClient {
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
                assertions(request)
                when (request.url.fullUrl) {
                    TEST_NONCE_ENDPOINT -> {
                        val responseHeaders = headersOf("Content-Type" to listOf(ContentType.Text.Plain.toString()))
                        respond(TEST_NONCE, headers = responseHeaders)
                    }
                    TEST_KEY_ENDPOINT -> {
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