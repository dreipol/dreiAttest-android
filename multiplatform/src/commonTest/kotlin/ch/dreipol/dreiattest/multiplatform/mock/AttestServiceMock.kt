package ch.dreipol.dreiattest.multiplatform.mock

import ch.dreipol.dreiattest.multiplatform.AttestService
import ch.dreipol.dreiattest.multiplatform.SessionConfiguration
import kotlin.native.concurrent.SharedImmutable
import kotlin.native.concurrent.ThreadLocal

class AttestServiceMock(override val uid: String = "test") : AttestService {

    lateinit var baseAddress: String
    lateinit var sessionConfiguration: SessionConfiguration

    override fun initWith(baseAddress: String, sessionConfiguration: SessionConfiguration) {
        this.baseAddress = baseAddress
        this.sessionConfiguration = sessionConfiguration
    }

    override suspend fun buildSignature(url: String, requestMethod: String, headers: List<Pair<String, String>>, body: ByteArray?): String {
        return "signature"
    }

    override suspend fun deregister() {
        // nothing to do
    }

    override fun shouldByPass(url: String): Boolean {
        return url.contains(baseAddress).not()
    }
}