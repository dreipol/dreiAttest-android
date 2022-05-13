package ch.dreipol.dreiattest.multiplatform

import ch.dreipol.dreiattest.multiplatform.api.*
import ch.dreipol.dreiattest.multiplatform.utils.Request
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.features.*
import io.ktor.client.request.*
import io.ktor.util.*

public class InvalidHeaderException: Exception("Requests should not already contain \"Dreiattest-\" headers!")

/**
 * install this feature in your client to sign your requests
 */
public class DreiAttestFeature(private val attestService: AttestService) {

        public companion object : HttpClientFeature<Config, DreiAttestFeature> {
        override val key: AttributeKey<DreiAttestFeature> = AttributeKey("DreiAttestFeature")

        override fun prepare(block: Config.() -> Unit): DreiAttestFeature {
            val config = Config().apply(block)
            return DreiAttestFeature(config.attestService)
        }

        override fun install(feature: DreiAttestFeature, scope: HttpClient) {
            scope.sendPipeline.intercept(HttpSendPipeline.State) {
                feature.addHeaders(context)
            }
            scope.sendPipeline.intercept(HttpSendPipeline.Receive) {
                if ((subject as? HttpClientCall)?.response?.status?.isRedirect() == true) {
                    context.headers.names().filter(NetworkHelper::isDreiattestHeader).forEach(context.headers::remove)
                }
            }
        }
    }

    public class Config {
        public lateinit var attestService: AttestService
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