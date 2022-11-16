package ch.dreipol.dreiattest.multiplatform.api

import ch.dreipol.dreiattest.multiplatform.api.dto.Attestation
import ch.dreipol.dreiattest.multiplatform.utils.SystemInfo
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.http.content.*

internal class MiddlewareAPI(private val middlewareUrl: URLBuilder, private val systemInfo: SystemInfo) {

    private fun urlFor(path: String): Url = URLBuilder(middlewareUrl).appendPathSegments(path).build()

    suspend fun getNonce(uid: String): String {
        return NetworkHelper.middlewareClient.get(urlFor("nonce")) {
            setCommonHeaders(systemInfo)
            setUid(uid)
        }.body()
    }

    suspend fun setKey(attestation: Attestation, uid: String, nonce: String) {
        return NetworkHelper.middlewareClient.post(urlFor("key")) {
            setCommonHeaders(systemInfo)
            setUid(uid)
            setNonce(nonce)
            contentType(ContentType.Application.Json)
            setBody(attestation)
        }.body()
    }

    suspend fun deleteKey(uid: String, publicKey: String, nonce: String, setSignature: suspend (HttpRequestBuilder) -> Unit) {
        return NetworkHelper.middlewareClient.delete(urlFor("key")) {
            setBody(TextContent(publicKey, ContentType.Text.Plain))
            setCommonHeaders(systemInfo)
            setUid(uid)
            setUserHeaders()
            setSignature(this)
            setNonce(nonce)
        }.body()
    }
}
