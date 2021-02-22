package ch.dreipol.dreiattest.multiplatform.mock

import ch.dreipol.dreiattest.multiplatform.AttestationService
import ch.dreipol.dreiattest.multiplatform.api.Attestation
import ch.dreipol.dreiattest.multiplatform.utils.CryptoUtils
import ch.dreipol.dreiattest.multiplatform.utils.Hash
import ch.dreipol.dreiattest.multiplatform.utils.encodeHashedToBase64
import ch.dreipol.dreiattest.multiplatform.utils.encodeToBase64

class AttestationServiceMock : AttestationService {

    override suspend fun getAttestation(nonce: Hash, publicKey: ByteArray): Attestation {
        return Attestation(CryptoUtils.encodeToBase64(publicKey), CryptoUtils.encodeHashedToBase64(nonce))
    }
}