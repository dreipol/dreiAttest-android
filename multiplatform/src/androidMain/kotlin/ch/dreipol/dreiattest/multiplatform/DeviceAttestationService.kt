package ch.dreipol.dreiattest.multiplatform

import android.content.Context
import com.google.android.gms.safetynet.SafetyNet
import kotlinx.coroutines.tasks.await

public actual class DeviceAttestationService(private val context: Context, private val apiKey: String) {
    internal actual suspend fun getAttestation(nonce: String, publicKey: String): Attestation {
        val deviceAttestation = SafetyNet.getClient(context).attest(nonce.toByteArray(), apiKey).await().jwsResult
        return Attestation(publicKey, deviceAttestation)
    }
}