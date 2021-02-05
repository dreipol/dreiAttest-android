package ch.dreipol.dreiattest.multiplatform

import ch.dreipol.dreiattest.multiplatform.api.*
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
        addSignature(request)
        setUid(request)
    }

    private suspend fun addSignature(request: HttpRequestBuilder) {
        request.setSignature(
            attestService.buildSignature(request.readUrl(), request.readMethod(), request.readHeaders(), request.readBody()))

    }

    private fun setUid(request: HttpRequestBuilder) {
        request.setUid(attestService.uid)
    }

}