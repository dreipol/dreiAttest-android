package ch.dreipol.dreiattest.multiplatform

import ch.dreipol.dreiattest.multiplatform.api.NetworkHelper
import ch.dreipol.dreiattest.multiplatform.api.isRedirect
import ch.dreipol.dreiattest.multiplatform.api.readBody
import ch.dreipol.dreiattest.multiplatform.api.readHeaders
import ch.dreipol.dreiattest.multiplatform.api.readMethod
import ch.dreipol.dreiattest.multiplatform.api.readUrl
import ch.dreipol.dreiattest.multiplatform.api.setCommonHeaders
import ch.dreipol.dreiattest.multiplatform.api.setNonce
import ch.dreipol.dreiattest.multiplatform.api.setSharedSecret
import ch.dreipol.dreiattest.multiplatform.api.setSignature
import ch.dreipol.dreiattest.multiplatform.api.setUid
import ch.dreipol.dreiattest.multiplatform.api.setUserHeaders
import ch.dreipol.dreiattest.multiplatform.utils.Request
import io.ktor.client.HttpClient
import io.ktor.client.call.HttpClientCall
import io.ktor.client.plugins.HttpClientPlugin
import io.ktor.client.plugins.HttpRequestRetry
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.HttpSendPipeline
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.request
import io.ktor.http.HttpStatusCode
import io.ktor.util.AttributeKey
import kotlinx.coroutines.runBlocking

public class InvalidHeaderException : Exception("Requests should not already contain \"Dreiattest-\" headers!")

/**
 * install this plugin in your client to sign your requests
 */
public class DreiAttestPlugin(private val attestService: AttestService) {

    public companion object : HttpClientPlugin<Config, DreiAttestPlugin> {
        override val key: AttributeKey<DreiAttestPlugin> = AttributeKey("DreiAttestFeature")

        override fun prepare(block: Config.() -> Unit): DreiAttestPlugin {
            val config = Config().apply(block)
            return DreiAttestPlugin(config.attestService)
        }

        override fun install(plugin: DreiAttestPlugin, scope: HttpClient) {
            scope.config {
                install(HttpRequestRetry) {
                    retryIf(maxRetries = 1) { _, response ->
                        runBlocking {
                            plugin.reregister(response)
                        }
                    }
                }
            }

            scope.sendPipeline.intercept(HttpSendPipeline.State) {
                plugin.addHeaders(context)
            }
            scope.sendPipeline.intercept(HttpSendPipeline.Receive) {
                val response = (subject as? HttpClientCall)?.response
                if (response?.status?.isRedirect() == true) {
                    context.headers.names().filter(NetworkHelper::isDreiattestHeader).forEach(context.headers::remove)
                }
            }
        }
    }

    public class Config {
        public lateinit var attestService: AttestService
    }

    public suspend fun reregister(response: HttpResponse): Boolean {
        if (!attestService.shouldHandle(response.request.url.toString()) || response.status != HttpStatusCode.Unauthorized) {
            return false
        }

        attestService.forgetKey()
        return true
    }

    public suspend fun addHeaders(request: HttpRequestBuilder) {
        if (!attestService.shouldHandle(request.readUrl())) {
            return
        }
        if (request.readHeaders().any { NetworkHelper.isDreiattestHeader(it.first) }) {
            throw InvalidHeaderException()
        }

        val bypassSecret = attestService.getBypassSecret()
        if (bypassSecret != null) {
            setCommonHeaders(request)
            request.setSharedSecret(bypassSecret)
            return
        }
        val snonce = attestService.getRequestNonce()
        setCommonHeaders(request)
        setUid(request)
        setUserHeaders(request)
        addSignature(request, snonce)
        setNonce(request, snonce)
    }

    private suspend fun addSignature(request: HttpRequestBuilder, snonce: String) {
        request.setSignature(
            attestService.buildSignature(
                Request(request.readUrl(), request.readMethod(), request.readHeaders(), request.readBody()),
                snonce
            )
        )
    }

    private fun setUid(request: HttpRequestBuilder) {
        request.setUid(attestService.uid)
    }

    private fun setNonce(request: HttpRequestBuilder, snonce: String) {
        request.setNonce(snonce)
    }

    private fun setUserHeaders(request: HttpRequestBuilder) {
        request.setUserHeaders()
    }

    private fun setCommonHeaders(request: HttpRequestBuilder) {
        request.setCommonHeaders(attestService.systemInfo)
    }
}