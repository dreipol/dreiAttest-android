package ch.dreipol.dreiattest.multiplatform

import ch.dreipol.dreiattest.multiplatform.api.Attestation

public actual class DeviceAttestationService() {
    internal actual suspend fun getAttestation(nonce: ByteArray, publicKey: ByteArray): Attestation {
        throw NotImplementedError()
    }
}