package ch.dreipol.dreiattest.multiplatform

import ch.dreipol.dreiattest.multiplatform.api.setSignature
import ch.dreipol.dreiattest.multiplatform.api.setUid
import ch.dreipol.dreiattest.multiplatform.utils.Keystore
import io.ktor.client.request.*
import io.ktor.http.content.*


// call in default request of callerLibrary
// TODO ev with http send
// change url
// TODO bypass
public suspend fun HttpRequestBuilder.withAttestation(keystore: Keystore, baseAddress: String, sessionConfiguration: SessionConfiguration) {
    val attestService = DreiAttestService(keystore)
    attestService.initWith(baseAddress, sessionConfiguration)
    setSignature(attestService.buildSignature(url.buildString(), readMethod(), readHeaders(), readBody()))
    setUid(attestService.uid)
    // TODO error handling
}

internal fun HttpRequestBuilder.readBody(): ByteArray? {
    return when (val body = body) {
        is OutgoingContent.ByteArrayContent -> body.bytes()
        is OutgoingContent.NoContent -> null
        else -> throw NotImplementedError()
    }
}

internal fun HttpRequestBuilder.readHeaders(): List<Pair<String, String>> {
    return headers.entries().flatMap { e -> e.value.map { e.key to it } }
}

internal fun HttpRequestBuilder.readMethod(): String {
    return method.value
}