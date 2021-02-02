package ch.dreipol.dreiattest.multiplatform

public actual class DeviceAttestationService() {
    internal actual suspend fun getAttestation(nonce: String, publicKey: ByteArray): Attestation {
        throw NotImplementedError()
    }
}