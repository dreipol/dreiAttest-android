package ch.dreipol.dreiattest.multiplatform.api

import ch.dreipol.dreiattest.multiplatform.utils.CryptoUtils
import ch.dreipol.dreiattest.multiplatform.utils.encodeToBase64
import co.touchlab.kermit.CommonLogger
import co.touchlab.kermit.Kermit
import io.ktor.client.*
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.features.json.serializer.*
import io.ktor.client.features.logging.*
import io.ktor.client.request.*
import io.ktor.http.content.*
import kotlinx.serialization.json.Json

internal var middlewareClientCreator = {
    HttpClient {
        install(JsonFeature) {
            serializer = KotlinxSerializer(
                Json {
                    ignoreUnknownKeys = true
                }
            )
        }
        install(Logging) {
            logger = HttpLogger()
            level = LogLevel.ALL
        }
    }
}

internal object NetworkHelper {

    private const val HEADER_PREFIX = "Dreiattest"

    internal const val HEADER_UID = "$HEADER_PREFIX-Uid"
    internal const val HEADER_SIGNATURE = "$HEADER_PREFIX-Signature"
    internal const val HEADER_NONCE = "$HEADER_PREFIX-Nonce"
    internal const val HEADER_SHARED_SECRET = "$HEADER_PREFIX-Shared-Secret"
    internal const val HEADER_USER_HEADERS = "$HEADER_PREFIX-User-Headers"

    internal val middlewareClient: HttpClient
        get() = middlewareClientCreator()
}

internal fun HttpRequestBuilder.setUid(uid: String) {
    headers.append(NetworkHelper.HEADER_UID, uid)
}

internal fun HttpRequestBuilder.setSignature(signature: String) {
    headers.append(NetworkHelper.HEADER_SIGNATURE, signature)
}

internal fun HttpRequestBuilder.setNonce(nonce: String) {
    headers.append(NetworkHelper.HEADER_NONCE, nonce)
}

internal fun HttpRequestBuilder.setSharedSecret(sharedSecret: String?) {
    sharedSecret?.let { headers.append(NetworkHelper.HEADER_SHARED_SECRET, sharedSecret) }
}

internal fun HttpRequestBuilder.setUserHeaders() {
    val headerKeys = readHeaders().map { it.first }.toMutableList()
    headerKeys.add(NetworkHelper.HEADER_USER_HEADERS)
    headers.append(NetworkHelper.HEADER_USER_HEADERS, headerKeys.sortedBy { it }.joinToString(","))
}

internal fun HttpRequestBuilder.readBody(): ByteArray? {
    return when (val body = body) {
        is OutgoingContent.ByteArrayContent -> body.bytes()
        is OutgoingContent.NoContent -> null
        else -> throw NotImplementedError()
    }
}

internal fun HttpRequestBuilder.readHeaders(): List<Pair<String, String>> {
    val headers = headers.entries().flatMap { e -> e.value.map { e.key to it } }.toMutableList()
    val body = body
    if (body is OutgoingContent) {
        headers.addAll(body.headers.entries().flatMap { e -> e.value.map { e.key to it } })
    }
    return headers
}

internal fun HttpRequestBuilder.readMethod(): String {
    return method.value
}

internal fun HttpRequestBuilder.readUrl(): String {
    return url.buildString()
}

internal class HttpLogger : Logger {
    override fun log(message: String) {
        kermit().d { message }
    }
}

internal fun kermit(): Kermit {
    return Kermit(CommonLogger())
}