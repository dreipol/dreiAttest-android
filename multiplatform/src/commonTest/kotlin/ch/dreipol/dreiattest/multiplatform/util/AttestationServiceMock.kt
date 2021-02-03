package ch.dreipol.dreiattest.multiplatform.util

import ch.dreipol.dreiattest.multiplatform.AttestationService
import ch.dreipol.dreiattest.multiplatform.api.Attestation
import ch.dreipol.dreiattest.multiplatform.utils.CryptoUtils
import ch.dreipol.dreiattest.multiplatform.utils.encodeToBase64

class AttestationServiceMock : AttestationService {
    override suspend fun getAttestation(nonce: ByteArray, publicKey: ByteArray): Attestation {
        return Attestation(CryptoUtils.encodeToBase64(publicKey), CryptoUtils.encodeToBase64(nonce))
    }
}