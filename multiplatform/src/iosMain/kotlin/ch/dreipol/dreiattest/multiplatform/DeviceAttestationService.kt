package ch.dreipol.dreiattest.multiplatform

public actual class DeviceAttestationService() {
    public actual suspend fun getAttestation(nonce: String, publicKey: String): Attestation {
        throw NotImplementedError()
    }
}