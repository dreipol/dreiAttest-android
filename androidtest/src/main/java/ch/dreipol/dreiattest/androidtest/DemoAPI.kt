package ch.dreipol.dreiattest.androidtest

import ch.dreipol.dreiattest.multiplatform.AttestService
import ch.dreipol.dreiattest.multiplatform.DreiAttestPlugin
import co.touchlab.kermit.CommonLogger
import co.touchlab.kermit.Kermit
import io.ktor.client.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*

class DemoAPI(private val attestService: AttestService, private val baseUrl: String) {

    private val client = HttpClient {
        install(ContentNegotiation) {
            json(
                kotlinx.serialization.json.Json {
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