package ch.dreipol.dreiattest.androidtest

import ch.dreipol.dreiattest.multiplatform.AttestService
import ch.dreipol.dreiattest.multiplatform.DreiAttestPlugin
import co.touchlab.kermit.Logger
import io.ktor.client.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.plugins.logging.Logger as KtorLogger
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

class DemoAPI(private val attestService: AttestService, private val baseUrl: String) {

    private val client = HttpClient {
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
        install(DreiAttestPlugin) {
            attestService = this@DemoAPI.attestService
        }
    }

    suspend fun demoGet(): HttpResponse {
        return client.get("demo") {
            url.setBase(baseUrl)
        }
    }

}

internal class HttpLogger : KtorLogger {
    override fun log(message: String) {
        Logger.d { message }
    }
}


private fun URLBuilder.setBase(base: String): URLBuilder {
    val path = encodedPath
    return takeFrom(base).apply {
        encodedPath += path
    }
}