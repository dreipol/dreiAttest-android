package ch.dreipol.dreiattest.multiplatform

import android.content.Context
import android.util.Base64
import com.google.android.gms.safetynet.SafetyNet
import kotlinx.coroutines.tasks.await

public actual class DeviceAttestationService(private val context: Context, private val apiKey: String) {
    internal actual suspend fun getAttestation(nonce: String, publicKey: ByteArray): Attestation {
        val deviceAttestation = SafetyNet.getClient(context).attest(nonce.toByteArray(), apiKey).await().jwsResult
        return Attestation(Base64.encodeToString(publicKey, Base64.DEFAULT), deviceAttestation)
    }
}