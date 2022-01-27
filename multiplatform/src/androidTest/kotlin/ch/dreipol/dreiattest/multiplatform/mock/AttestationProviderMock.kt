package ch.dreipol.dreiattest.multiplatform.mock

import ch.dreipol.dreiattest.multiplatform.AttestationProvider
import ch.dreipol.dreiattest.multiplatform.api.Attestation
import ch.dreipol.dreiattest.multiplatform.platformDriver
import ch.dreipol.dreiattest.multiplatform.utils.CryptoUtils
import ch.dreipol.dreiattest.multiplatform.utils.Hash
import ch.dreipol.dreiattest.multiplatform.utils.SystemInfo
import ch.dreipol.dreiattest.multiplatform.utils.encodeHashedToBase64
import ch.dreipol.dreiattest.multiplatform.utils.encodeToBase64

actual class AttestationProviderMock : AttestationProvider {

    override val systemInfo: SystemInfo = SystemInfoMock
    override val isSupported: Boolean
        get() = true

    override suspend fun getAttestation(nonce: Hash, publicKey: String): Attestation {
        return Attestation(
            publicKey = CryptoUtils.encodeToBase64(publicKey.toByteArray()), attestation = CryptoUtils.encodeHashedToBase64(nonce),
            driver = platformDriver
        )
    }
}