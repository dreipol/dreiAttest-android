package ch.dreipol.dreiattest.multiplatform

import android.content.Context
import ch.dreipol.dreiattest.multiplatform.api.Attestation
import ch.dreipol.dreiattest.multiplatform.utils.CryptoUtils
import ch.dreipol.dreiattest.multiplatform.utils.encodeToBase64
import com.google.android.gms.safetynet.SafetyNet
import kotlinx.coroutines.tasks.await

public actual class DeviceAttestationService(private val context: Context, private val apiKey: String) {
    internal actual suspend fun getAttestation(nonce: ByteArray, publicKey: ByteArray): Attestation {
        val deviceAttestation = SafetyNet.getClient(context).attest(nonce, apiKey).await().jwsResult
        return Attestation(CryptoUtils.encodeToBase64(publicKey), deviceAttestation)
    }
}