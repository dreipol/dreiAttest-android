package ch.dreipol.dreiattest.androidtest

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import ch.dreipol.dreiattest.multiplatform.DeviceAttestationService
import ch.dreipol.dreiattest.multiplatform.DreiAttestService
import ch.dreipol.dreiattest.multiplatform.SessionConfiguration
import com.russhwolf.settings.MockSettings
import io.ktor.client.call.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<Button>(R.id.test_button).setOnClickListener {
            GlobalScope.launch {
                val baseUrl = "https://erz-rezhycle-prod.drei.io"
                val safetyNetAPIKey = "AIzaSyAIvaC0aAvyuqWgE0_EMqfEYRe_jMzThS0"

                val attestService = DreiAttestService(settings = MockSettings())
                attestService.initWith(
                    baseUrl, SessionConfiguration("test", deviceAttestationService = DeviceAttestationService(this@MainActivity, safetyNetAPIKey)
                    )
                )

                try {
                    val response = DemoAPI(attestService, baseUrl).demoGet()
                    println("got status code: ${response.status}")
                } catch (e: Throwable) {
                    e.printStackTrace()
                }
            }
        }
    }
}