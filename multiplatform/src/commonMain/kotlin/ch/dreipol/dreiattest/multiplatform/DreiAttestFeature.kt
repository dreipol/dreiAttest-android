package ch.dreipol.dreiattest.multiplatform

import ch.dreipol.dreiattest.multiplatform.api.*
import ch.dreipol.dreiattest.multiplatform.utils.Request
import io.ktor.client.*
import io.ktor.client.features.*
import io.ktor.client.request.*
import io.ktor.util.*

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
        }
    }

    public class Config {
        public lateinit var attestService: AttestService
    }

    public suspend fun addHeaders(request: HttpRequestBuilder) {
        if (attestService.shouldByPass(request.readUrl())) {
            return
        }
        val bypassSecret = attestService.getBypassSecret()
        if (bypassSecret != null) {
            request.setSharedSecret(bypassSecret)
            return
        }
        val snonce = attestService.getRequestNonce()
        addSignature(request, snonce)
        setUid(request)
        setNonce(request, snonce)
    }

    private suspend fun addSignature(request: HttpRequestBuilder, snonce: ByteArray) {
        request.setSignature(
            attestService.buildSignature(Request(request.readUrl(), request.readMethod(), request.readHeaders(), request.readBody()),
                snonce)
        )
    }

    private fun setUid(request: HttpRequestBuilder) {
        request.setUid(attestService.uid)
    }

    private fun setNonce(request: HttpRequestBuilder, snonce: ByteArray) {
        request.setNonce(snonce)
    }
}