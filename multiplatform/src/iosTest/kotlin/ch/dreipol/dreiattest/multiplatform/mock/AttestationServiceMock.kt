package ch.dreipol.dreiattest.multiplatform.mock

import ch.dreipol.dreiattest.multiplatform.AttestationService
import ch.dreipol.dreiattest.multiplatform.api.Attestation
import ch.dreipol.dreiattest.multiplatform.platformDriver
import ch.dreipol.dreiattest.multiplatform.utils.CryptoUtils
import ch.dreipol.dreiattest.multiplatform.utils.Hash
import ch.dreipol.dreiattest.multiplatform.utils.encodeHashedToBase64
import ch.dreipol.dreiattest.multiplatform.utils.encodeToBase64

actual class AttestationServiceMock : AttestationService {
    override suspend fun getAttestation(nonce: Hash, publicKey: ByteArray): Attestation {
        return Attestation(keyId = CryptoUtils.encodeToBase64(publicKey), attestation = CryptoUtils.encodeHashedToBase64(nonce),
            driver = platformDriver)
    }
}