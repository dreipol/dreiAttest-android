package ch.dreipol.dreiattest.multiplatform.api

import ch.dreipol.dreiattest.multiplatform.api.dto.Attestation
import ch.dreipol.dreiattest.multiplatform.utils.SystemInfo
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.http.content.*

internal class MiddlewareAPI(private val middlewareUrl: String, private val systemInfo: SystemInfo) {
    suspend fun getNonce(uid: String): String {
        return NetworkHelper.middlewareClient.get("nonce") {
            url.setBase(middlewareUrl)
            setCommonHeaders(systemInfo)
            setUid(uid)
        }.body()
    }

    suspend fun setKey(attestation: Attestation, uid: String, nonce: String) {
        return NetworkHelper.middlewareClient.post("key") {
            contentType(ContentType.Application.Json)
            url.setBase(middlewareUrl)
            setCommonHeaders(systemInfo)
            setUid(uid)
            setNonce(nonce)
            setBody(attestation)
        }.body()
    }

    suspend fun deleteKey(uid: String, publicKey: String, nonce: String, setSignature: suspend (HttpRequestBuilder) -> Unit) {
        return NetworkHelper.middlewareClient.delete("key") {
            url.setBase(middlewareUrl)
            setBody(TextContent(publicKey, ContentType.Text.Plain))
            setCommonHeaders(systemInfo)
            setUid(uid)
            setUserHeaders()
            setSignature(this)
            setNonce(nonce)
        }.body()
    }
}

private fun URLBuilder.setBase(base: String): URLBuilder {
    val path = encodedPath
    return takeFrom(base).apply {
        encodedPath += path
    }
}