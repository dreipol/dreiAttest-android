package ch.dreipol.dreiattest.multiplatform.util

import ch.dreipol.dreiattest.multiplatform.AttestService
import ch.dreipol.dreiattest.multiplatform.DreiAttestPlugin
import ch.dreipol.dreiattest.multiplatform.api.middlewareClientCreator
import ch.dreipol.dreiattest.multiplatform.utils.CryptoUtils
import ch.dreipol.dreiattest.multiplatform.utils.encodeToBase64
import io.ktor.client.*
import io.ktor.client.engine.mock.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.utils.io.core.*
import kotlinx.serialization.json.Json

const val TEST_BASE_URL = "https://test.dreipol.ch"
const val TEST_NONCE = "testnonce"
const val TEST_REQUEST_ENDPOINT = "https://test.dreipol.ch/test/"
const val TEST_BYPASS_ENDPOINT = "https://bypass.dreipol.ch/"
const val TEST_NONCE_ENDPOINT = "https://test.dreipol.ch/dreiattest/nonce"
const val TEST_KEY_ENDPOINT = "https://test.dreipol.ch/dreiattest/key"

fun mockMiddlewareClient(assertions: suspend (HttpRequestData) -> Unit) {
    middlewareClientCreator = { createMockClient(assertions) }
}

fun requestClientMock(attestService: AttestService, assertions: suspend (HttpRequestData) -> Unit): HttpClient {
    return HttpClient(MockEngine) {
        install(ContentNegotiation) {
            json(
                Json {
                    ignoreUnknownKeys = true
                }
            )
        }
        install(DreiAttestPlugin) {
            this.attestService = attestService
        }
        engine {
            addHandler { request ->
                assertions(request)
                when (request.url.fullUrl) {
                    TEST_REQUEST_ENDPOINT -> {
                        val responseHeaders = headersOf("Content-Type" to listOf(ContentType.Application.Json.toString()))
                        respond(CryptoUtils.encodeToBase64(TEST_NONCE.toByteArray()), headers = responseHeaders)
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
    val mockEngine = MockEngine { request ->
        assertions(request)
        when (request.url.fullUrl) {
            TEST_NONCE_ENDPOINT -> {
                val responseHeaders = headersOf("Content-Type" to listOf(ContentType.Text.Plain.toString()))
                respond(CryptoUtils.encodeToBase64(TEST_NONCE.toByteArray()), headers = responseHeaders)
            }
            TEST_KEY_ENDPOINT -> {
                respondOk()
            }
            else -> error("Unhandled ${request.url.fullUrl}")
        }
    }

    return HttpClient(mockEngine) {
        install(ContentNegotiation) {
            json(
                Json {
                    ignoreUnknownKeys = true
                }
            )
        }
    }
}

private val Url.hostWithPortIfRequired: String get() = if (port == protocol.defaultPort) host else hostWithPort
private val Url.fullUrl: String get() = "${protocol.name}://$hostWithPortIfRequired$fullPath"