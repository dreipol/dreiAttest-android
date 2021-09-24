package ch.dreipol.dreiattest.multiplatform.api

import io.ktor.client.features.json.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.http.content.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

internal class MiddlewareAPI(private val middlewareUrl: String) {
    suspend fun getNonce(uid: String): String {
        return NetworkHelper.middlewareClient.get("nonce") {
            url.setBase(middlewareUrl)
            setUid(uid)
        }
    }

    suspend fun setKey(attestation: Attestation, uid: String, nonce: String) {
        val result = defaultSerializer().write(attestation)
        return NetworkHelper.middlewareClient.post("key") {
            url.setBase(middlewareUrl)
            setUid(uid)
            setNonce(nonce)
            body = defaultSerializer().write(attestation)
        }
    }

    suspend fun deleteKey(uid: String, publicKey: String, nonce: String, setSignature: suspend (HttpRequestBuilder) -> Unit) {
        return NetworkHelper.middlewareClient.delete("key") {
            url.setBase(middlewareUrl)
            body = TextContent(publicKey, ContentType.Text.Plain)
            setUid(uid)
            setUserHeaders()
            setSignature(this)
            setNonce(nonce)
        }
    }
}

@Serializable
public data class Attestation(
    @SerialName("public_key")
    val publicKey: String? = null,
    @SerialName("key_id")
    val keyId: String? = null,
    val attestation: String,
    val driver: String,
)

private fun URLBuilder.setBase(base: String): URLBuilder {
    val path = encodedPath
    return takeFrom(base).apply {
        encodedPath += path
    }
}