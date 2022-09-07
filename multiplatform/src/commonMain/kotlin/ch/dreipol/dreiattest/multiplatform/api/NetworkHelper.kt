package ch.dreipol.dreiattest.multiplatform.api

import ch.dreipol.dreiattest.multiplatform.DreiAttest
import ch.dreipol.dreiattest.multiplatform.utils.Request
import ch.dreipol.dreiattest.multiplatform.utils.SystemInfo
import co.touchlab.kermit.CommonLogger
import co.touchlab.kermit.Kermit
import io.ktor.client.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

internal var middlewareClientCreator = {
    HttpClient {
        install(ContentNegotiation) {
            json(
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

    internal const val HEADER_LIBRARY_VERSION = "$HEADER_PREFIX-Library-Version"
    internal const val HEADER_APP_VERSION = "$HEADER_PREFIX-App-Version"
    internal const val HEADER_APP_BUILD = "$HEADER_PREFIX-App-Build"
    internal const val HEADER_APP_IDENTIFIER = "$HEADER_PREFIX-App-Identifier"
    internal const val HEADER_OS = "$HEADER_PREFIX-OS"

    internal val middlewareClient: HttpClient
        get() = middlewareClientCreator()

    internal fun isDreiattestHeader(header: String): Boolean = header.startsWith("Dreiattest-")
}

internal fun HttpStatusCode.isRedirect(): Boolean = 300 <= value && value < 400

internal fun HttpRequestBuilder.setUid(uid: String) {
    headers.append(NetworkHelper.HEADER_UID, uid)
}

internal fun HttpRequestBuilder.setSignature(signature: String) {
    headers.append(NetworkHelper.HEADER_SIGNATURE, signature)
}

internal fun HttpRequestBuilder.setNonce(nonce: String) {
    headers.append(NetworkHelper.HEADER_NONCE, nonce)
}

internal fun HttpRequestBuilder.setSharedSecret(sharedSecret: String) {
    headers.append(NetworkHelper.HEADER_SHARED_SECRET, sharedSecret)
}

internal fun HttpRequestBuilder.setUserHeaders() {
    val headerKeys = signableHeaders().map { it.first }.toMutableList()
    headerKeys.add(NetworkHelper.HEADER_USER_HEADERS)
    headers.append(NetworkHelper.HEADER_USER_HEADERS, headerKeys.sortedBy { it }.joinToString(","))
}

internal fun HttpRequestBuilder.setCommonHeaders(systemInfo: SystemInfo) {
    headers.append(NetworkHelper.HEADER_LIBRARY_VERSION, DreiAttest.version)
    headers.append(NetworkHelper.HEADER_APP_VERSION, systemInfo.appVersion)
    headers.append(NetworkHelper.HEADER_APP_BUILD, systemInfo.appBuild)
    headers.append(NetworkHelper.HEADER_APP_IDENTIFIER, systemInfo.appIdentifier)
    headers.append(NetworkHelper.HEADER_OS, systemInfo.osVersion)
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

internal fun Iterable<Pair<String, String>>.signableHeaders(): Collection<Pair<String, String>> =
    filterNot { it.first.startsWith("Accept") || it.first == "User-Agent" }

internal fun Request.signableHeaders(): Collection<Pair<String, String>> =
    headers.signableHeaders()

internal fun HttpRequestBuilder.signableHeaders(): Collection<Pair<String, String>> =
    readHeaders().signableHeaders()

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