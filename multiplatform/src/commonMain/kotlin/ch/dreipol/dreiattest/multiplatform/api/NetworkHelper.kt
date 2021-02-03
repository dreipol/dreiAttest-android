package ch.dreipol.dreiattest.multiplatform.api

import co.touchlab.kermit.CommonLogger
import co.touchlab.kermit.Kermit
import io.ktor.client.*
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.features.json.serializer.*
import io.ktor.client.features.logging.*
import io.ktor.client.request.*
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

internal class HttpLogger : Logger {
    override fun log(message: String) {
        kermit().d { message }
    }
}

internal fun kermit(): Kermit {
    return Kermit(CommonLogger())
}