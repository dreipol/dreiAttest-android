package ch.dreipol.dreiattest.multiplatform

internal actual fun AttestService.getDeviceAttestation(nonce: String, publicKey: String, apiKey: String?): Attestation {
    // TODO make safteyNet call (ignore public key, already in nonce)
    throw NotImplementedError()
}