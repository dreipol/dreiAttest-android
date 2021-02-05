package ch.dreipol.dreiattest.multiplatform.api

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

    internal const val HEADER_UID = "dreiAttest-uid"
    internal const val HEADER_SIGNATURE = "dreiAttest-signature"

    internal val middlewareClient: HttpClient
        get() = middlewareClientCreator()

}

internal fun HttpRequestBuilder.setUid(uid: String) {
    headers.append(NetworkHelper.HEADER_UID, uid)
}

internal fun HttpRequestBuilder.setSignature(signature: String) {
    headers.append(NetworkHelper.HEADER_SIGNATURE, signature)
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