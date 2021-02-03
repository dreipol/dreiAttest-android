package ch.dreipol.dreiattest.multiplatform.api

import io.ktor.client.features.json.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.http.content.*
import kotlinx.serialization.Serializable

internal class MiddlewareAPI(private val middlewareUrl: String) {
    suspend fun getNonce(uid: String): ByteArray {
        return NetworkHelper.middlewareClient.get("nonce") {
            host = middlewareUrl
            setUid(uid)
        }
    }

    suspend fun setKey(attestation: Attestation, uid: String) {
        return NetworkHelper.middlewareClient.post("key") {
            host = middlewareUrl
            setUid(uid)
            body = defaultSerializer().write(attestation)
        }
    }

    suspend fun deleteKey(uid: String, publicKey: String, setSignature: suspend (HttpRequestBuilder) -> Unit) {
        return NetworkHelper.middlewareClient.delete("key") {
            host = middlewareUrl
            body = publicKey
            setUid(uid)
            setSignature(this)
        }
    }
}

@Serializable
internal data class Attestation(val publicKey: String, val attestation: String)