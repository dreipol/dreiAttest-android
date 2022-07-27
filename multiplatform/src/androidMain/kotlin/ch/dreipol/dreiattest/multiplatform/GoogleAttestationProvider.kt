package ch.dreipol.dreiattest.multiplatform

import android.content.Context
import ch.dreipol.dreiattest.multiplatform.api.dto.Attestation
import ch.dreipol.dreiattest.multiplatform.utils.DeviceSystemInfo
import ch.dreipol.dreiattest.multiplatform.utils.Hash
import ch.dreipol.dreiattest.multiplatform.utils.SystemInfo
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.safetynet.SafetyNet
import kotlinx.coroutines.tasks.await

internal const val platformDriver = "google"

public class GoogleAttestationProvider(private val context: Context, private val apiKey: String) : AttestationProvider {
    override val systemInfo: SystemInfo = DeviceSystemInfo(context)
    override val isSupported: Boolean =
        GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(context) == ConnectionResult.SUCCESS

    public override suspend fun getAttestation(nonce: Hash, publicKey: String): Attestation {
        val deviceAttestation = SafetyNet.getClient(context).attest(nonce, apiKey).await().jwsResult!!
        return Attestation(publicKey = publicKey, attestation = deviceAttestation, driver = platformDriver)
    }
}