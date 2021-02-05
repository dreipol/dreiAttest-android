package ch.dreipol.dreiattest.multiplatform.mock

import ch.dreipol.dreiattest.multiplatform.AttestService
import ch.dreipol.dreiattest.multiplatform.SessionConfiguration

class AttestServiceMock(override val uid: String = "test") : AttestService {

    lateinit var baseAddress: String
    lateinit var sessionConfiguration: SessionConfiguration
    val requests = mutableListOf<Request>()
    var registered = false

    override fun initWith(baseAddress: String, sessionConfiguration: SessionConfiguration) {
        this.baseAddress = baseAddress
        this.sessionConfiguration = sessionConfiguration
        registered = true
    }

    override suspend fun buildSignature(url: String, requestMethod: String, headers: List<Pair<String, String>>, body: ByteArray?): String {
        requests.add(Request(url, requestMethod, headers, body))
        return "signature"
    }

    override suspend fun deregister() {
        registered = false
    }

    override fun shouldByPass(url: String): Boolean {
        return url.contains(baseAddress).not()
    }
}

data class Request(val url: String, val requestMethod: String, val headers: List<Pair<String, String>>, val body: ByteArray?)