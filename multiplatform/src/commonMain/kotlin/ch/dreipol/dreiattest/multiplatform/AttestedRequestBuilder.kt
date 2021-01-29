package ch.dreipol.dreiattest.multiplatform

import io.ktor.client.request.*
import io.ktor.http.*

// call in default request of callerLibrary
public suspend fun HttpRequestBuilder.withAttestation(keystore: Keystore, baseAddress: Url, sessionConfiguration: SessionConfiguration) {
    val attestService = AttestService(keystore)
    attestService.initWith(baseAddress, sessionConfiguration)
    setSignature(attestService.buildSignature())
    // TODO error handling
}