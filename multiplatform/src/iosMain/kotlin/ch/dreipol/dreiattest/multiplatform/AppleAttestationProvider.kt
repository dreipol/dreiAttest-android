package ch.dreipol.dreiattest.multiplatform

import ch.dreipol.dreiattest.multiplatform.api.Attestation
import ch.dreipol.dreiattest.multiplatform.utils.DeviceSystemInfo
import ch.dreipol.dreiattest.multiplatform.utils.Hash
import ch.dreipol.dreiattest.multiplatform.utils.SystemInfo
import kotlinx.coroutines.CompletableDeferred
import platform.DeviceCheck.DCAppAttestService
import platform.Foundation.NSData
import platform.Foundation.NSError
import platform.Foundation.base64EncodedStringWithOptions
import kotlin.native.concurrent.freeze

private class AttestationCompletable {
    private val completable = CompletableDeferred<Pair<NSData?, NSError?>>()

    fun receiveAttestation(attestation: NSData?, error: NSError?) {
        completable.complete(Pair(attestation, error))
    }

    suspend fun await() = completable.await()
}

internal const val platformDriver = "apple"

public class AppleAttestationProvider(): AttestationProvider {
    private val service = DCAppAttestService.sharedService
    override val systemInfo: SystemInfo = DeviceSystemInfo

    public override suspend fun getAttestation(nonce: Hash, publicKey: String): Attestation {
        val completable = AttestationCompletable()
        val receiver = completable::receiveAttestation
        receiver.freeze()
        service.attestKey(publicKey, nonce, receiver)

        val result = completable.await()
        result.second?.let {
            throw Exception(it.description())
        }

        val attestation = result.first ?: throw IllegalStateException()
        return Attestation(keyId = publicKey, attestation = attestation.base64EncodedStringWithOptions(0), driver = platformDriver)
    }
}