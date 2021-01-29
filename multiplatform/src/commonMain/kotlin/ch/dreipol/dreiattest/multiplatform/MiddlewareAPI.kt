package ch.dreipol.dreiattest.multiplatform

import io.ktor.client.features.json.*
import io.ktor.client.request.*
import kotlinx.serialization.Serializable

internal class MiddlewareAPI(private val middlewareUrl: String) {
    suspend fun getNonce(uuid: String): ByteArray {
        return NetworkHelper.middlewareClient.get("nonce") {
            host = middlewareUrl
            setUuid(uuid)
        }
    }

    suspend fun setKey(attestation: Attestation, uuid: String) {
        return NetworkHelper.middlewareClient.post("key") {
            host = middlewareUrl
            setUuid(uuid)
            body = defaultSerializer().write(attestation)
        }
    }

    suspend fun deleteKey(signature: String, uuid: String, publicKey: String) {
        return NetworkHelper.middlewareClient.delete("key") {
            host = middlewareUrl
            setUuid(uuid)
            setSignature(signature)
            body = publicKey
        }
    }
}

@Serializable
internal data class Attestation(val publicKey: String, val attestation: String)