package ch.dreipol.dreiattest.multiplatform

import ch.dreipol.dreiattest.multiplatform.api.Attestation
import ch.dreipol.dreiattest.multiplatform.utils.Conversion
import ch.dreipol.dreiattest.multiplatform.utils.Hash
import platform.DeviceCheck.DCAppAttestService
import platform.Foundation.NSData
import platform.Foundation.NSError
import platform.Foundation.base64EncodedStringWithOptions
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

internal const val platformDriver = "apple"

public actual class DeviceAttestationService() : AttestationService {
    private val service = DCAppAttestService.sharedService

    public override suspend fun getAttestation(nonce: Hash, publicKey: ByteArray): Attestation {
        val keyId = Conversion.byteArrayToData(publicKey).base64EncodedStringWithOptions(0)

        val result: Pair<NSData?, NSError?> = suspendCoroutine { continuation ->
            service.attestKey(keyId, nonce) { data, error ->
                continuation.resume(Pair(data, error))
            }
        }

        result.second?.let {
            throw Exception(it.description())
        }

        val attestation = result.first ?: throw IllegalStateException()
        return Attestation(keyId = keyId, attestation = attestation.base64EncodedStringWithOptions(0), driver = platformDriver)
    }
}