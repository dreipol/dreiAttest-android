package ch.dreipol.dreiattest.multiplatform.mock

import ch.dreipol.dreiattest.multiplatform.AttestationProvider
import ch.dreipol.dreiattest.multiplatform.api.dto.Attestation
import ch.dreipol.dreiattest.multiplatform.platformDriver
import ch.dreipol.dreiattest.multiplatform.utils.*
import io.ktor.utils.io.core.*

actual class AttestationProviderMock : AttestationProvider {
    override val systemInfo = SystemInfoMock
    override val isSupported: Boolean
        get() = true

    override suspend fun getAttestation(nonce: Hash, publicKey: String): Attestation {
        return Attestation(
            publicKey = CryptoUtils.encodeToBase64(publicKey.toByteArray()),
            attestation = CryptoUtils.encodeHashedToBase64(nonce),
            driver = platformDriver
        )
    }
}