package ch.dreipol.dreiattest.androidtest

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import ch.dreipol.dreiattest.multiplatform.DeviceAttestationService
import ch.dreipol.dreiattest.multiplatform.DreiAttestService
import ch.dreipol.dreiattest.multiplatform.SessionConfiguration
import com.russhwolf.settings.MockSettings
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class DemoApiIT {

    private val baseUrl = "https://erz-rezhycle-prod.drei.io"
    private val safetyNetAPIKey = "AIzaSyAYoH0Jpx-7COllvIn1e1aFCZ0xWd5yfjE"

    @Test
    fun testDreiAttestation() {
        val attestService = DreiAttestService(settings = MockSettings())
        attestService.initWith(
            baseUrl, SessionConfiguration("test", deviceAttestationService = DeviceAttestationService(
            InstrumentationRegistry.getInstrumentation().context, safetyNetAPIKey)))
        runBlocking {
            DemoAPI(attestService, baseUrl).demoGet()
        }
    }

}