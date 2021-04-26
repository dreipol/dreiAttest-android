package ch.dreipol.dreiattest.androidtest

import ch.dreipol.dreiattest.multiplatform.AttestService
import ch.dreipol.dreiattest.multiplatform.DreiAttestFeature
import co.touchlab.kermit.CommonLogger
import co.touchlab.kermit.Kermit
import io.ktor.client.*
import io.ktor.client.features.json.*
import io.ktor.client.features.json.serializer.*
import io.ktor.client.features.logging.*
import io.ktor.client.request.*
import io.ktor.http.*

class DemoAPI(private val attestService: AttestService, private val baseUrl: String) {

    private val client = HttpClient {
        install(JsonFeature) {
            serializer = KotlinxSerializer(
                kotlinx.serialization.json.Json {
                    ignoreUnknownKeys = true
                }
            )
        }
        install(Logging) {
            logger = HttpLogger()
            level = LogLevel.ALL
        }
        install(DreiAttestFeature) {
            attestService = this@DemoAPI.attestService
        }
    }

    suspend fun demoGet() {
        return client.get("demo") {
            url.setBase(baseUrl)
        }
    }

}

internal class HttpLogger : Logger {
    override fun log(message: String) {
        kermit().d { message }
    }
}

internal fun kermit(): Kermit {
    return Kermit(CommonLogger())
}



private fun URLBuilder.setBase(base: String): URLBuilder {
    val path = encodedPath
    return takeFrom(base).apply {
        encodedPath += path
    }
}