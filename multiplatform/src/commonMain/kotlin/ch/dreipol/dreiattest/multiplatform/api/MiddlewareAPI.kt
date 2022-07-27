package ch.dreipol.dreiattest.multiplatform.api

import ch.dreipol.dreiattest.multiplatform.api.dto.Attestation
import ch.dreipol.dreiattest.multiplatform.utils.SystemInfo
import io.ktor.client.features.json.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.http.content.*

internal class MiddlewareAPI(private val middlewareUrl: String, private val systemInfo: SystemInfo) {
    suspend fun getNonce(uid: String): String {
        return NetworkHelper.middlewareClient.get("nonce") {
            url.setBase(middlewareUrl)
            setCommonHeaders(systemInfo)
            setUid(uid)
        }
    }

    suspend fun setKey(attestation: Attestation, uid: String, nonce: String) {
        val result = defaultSerializer().write(attestation)
        return NetworkHelper.middlewareClient.post("key") {
            url.setBase(middlewareUrl)
            setCommonHeaders(systemInfo)
            setUid(uid)
            setNonce(nonce)
            body = defaultSerializer().write(attestation)
        }
    }

    suspend fun deleteKey(uid: String, publicKey: String, nonce: String, setSignature: suspend (HttpRequestBuilder) -> Unit) {
        return NetworkHelper.middlewareClient.delete("key") {
            url.setBase(middlewareUrl)
            body = TextContent(publicKey, ContentType.Text.Plain)
            setCommonHeaders(systemInfo)
            setUid(uid)
            setUserHeaders()
            setSignature(this)
            setNonce(nonce)
        }
    }
}

private fun URLBuilder.setBase(base: String): URLBuilder {
    val path = encodedPath
    return takeFrom(base).apply {
        encodedPath += path
    }
}