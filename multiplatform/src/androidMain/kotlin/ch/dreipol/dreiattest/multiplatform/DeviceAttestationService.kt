package ch.dreipol.dreiattest.multiplatform

import android.content.Context
import ch.dreipol.dreiattest.multiplatform.api.Attestation
import ch.dreipol.dreiattest.multiplatform.utils.CryptoUtils
import ch.dreipol.dreiattest.multiplatform.utils.Hash
import ch.dreipol.dreiattest.multiplatform.utils.encodeToBase64
import com.google.android.gms.safetynet.SafetyNet
import kotlinx.coroutines.tasks.await

internal const val platformDriver = "google"

public actual class DeviceAttestationService(private val context: Context, private val apiKey: String) : AttestationService {
    public override suspend fun getAttestation(nonce: Hash, publicKey: ByteArray): Attestation {
        val deviceAttestation = SafetyNet.getClient(context).attest(nonce, apiKey).await().jwsResult
        return Attestation(publicKey = CryptoUtils.encodeToBase64(publicKey), attestation = deviceAttestation, driver = platformDriver)
    }
}