package ch.dreipol.dreiattest.multiplatform.api.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class Attestation(
    @SerialName("public_key")
    val publicKey: String? = null,
    @SerialName("key_id")
    val keyId: String? = null,
    val attestation: String,
    val driver: String,
)