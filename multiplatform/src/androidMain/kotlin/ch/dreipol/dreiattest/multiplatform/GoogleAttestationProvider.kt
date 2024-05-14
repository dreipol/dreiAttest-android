package ch.dreipol.dreiattest.multiplatform

import android.content.Context
import android.util.Base64
import ch.dreipol.dreiattest.multiplatform.api.dto.Attestation
import ch.dreipol.dreiattest.multiplatform.utils.DeviceSystemInfo
import ch.dreipol.dreiattest.multiplatform.utils.Hash
import ch.dreipol.dreiattest.multiplatform.utils.SystemInfo
import com.google.android.play.core.integrity.IntegrityManagerFactory
import com.google.android.play.core.integrity.IntegrityTokenRequest
import kotlinx.coroutines.tasks.await

internal const val platformDriver = "google_play_integrity_api"

class GoogleAttestationProvider(private val context: Context, private val cloudProjectNumber: Long? = null) : AttestationProvider {
    private val integrityManager by lazy {
        IntegrityManagerFactory.create(context)
    }
    override val systemInfo: SystemInfo = DeviceSystemInfo(context)
    override val isSupported: Boolean = true


    override suspend fun getAttestation(nonce: Hash, publicKey: String): Attestation {
        val requestBuilder = IntegrityTokenRequest.builder()
            .setNonce(Base64.encodeToString(nonce, Base64.URL_SAFE or Base64.NO_WRAP))
        cloudProjectNumber?.let { requestBuilder.setCloudProjectNumber(it) }
        val deviceAttestation = integrityManager.requestIntegrityToken(requestBuilder.build()).await().token()
        return Attestation(publicKey = publicKey, attestation = deviceAttestation, driver = platformDriver)
    }
}