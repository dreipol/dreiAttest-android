package ch.dreipol.dreiattest.multiplatform

import ch.dreipol.dreiattest.multiplatform.api.setSignature
import ch.dreipol.dreiattest.multiplatform.api.setUid
import ch.dreipol.dreiattest.multiplatform.utils.Keystore
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.util.*


// call in default request of callerLibrary
public suspend fun HttpRequestBuilder.withAttestation(keystore: Keystore, baseAddress: Url, sessionConfiguration: SessionConfiguration) {
    val attestService = AttestService(keystore)
    attestService.initWith(baseAddress, sessionConfiguration)
    setSignature(attestService.buildSignature(url.buildString(), readMethod(), readHeaders(), readBody()))
    setUid(attestService.uid)
    // TODO error handling
}

internal fun HttpRequestBuilder.readBody(): ByteArray? {
    return when (val body = body as OutgoingContent) {
        is OutgoingContent.ByteArrayContent -> body.bytes()
        is OutgoingContent.NoContent -> null
        else -> throw NotImplementedError()
    }
}

internal fun HttpRequestBuilder.readHeaders(): List<Pair<String, String>> {
    return build().headers.flattenEntries().sortedBy { it.first }
}

internal fun HttpRequestBuilder.readMethod(): String {
    return method.value
}