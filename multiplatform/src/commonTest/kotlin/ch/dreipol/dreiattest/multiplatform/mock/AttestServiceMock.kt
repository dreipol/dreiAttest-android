package ch.dreipol.dreiattest.multiplatform.mock

import ch.dreipol.dreiattest.multiplatform.AttestService
import ch.dreipol.dreiattest.multiplatform.SessionConfiguration
import ch.dreipol.dreiattest.multiplatform.utils.Request
import ch.dreipol.dreiattest.multiplatform.utils.SystemInfo
import io.ktor.utils.io.core.*

class AttestServiceMock(override val uid: String = "test") : AttestService {

    lateinit var baseAddress: String
    lateinit var sessionConfiguration: SessionConfiguration
    override val systemInfo: SystemInfo = SystemInfoMock

    override fun initWith(baseAddress: String, sessionConfiguration: SessionConfiguration) {
        this.baseAddress = baseAddress
        this.sessionConfiguration = sessionConfiguration
    }

    override suspend fun buildSignature(request: Request, snonce: String): String {
        return "signature"
    }

    override suspend fun deregister() {
        // nothing to do
    }

    override fun shouldHandle(url: String): Boolean {
        return url.contains(baseAddress)
    }

    override suspend fun getRequestNonce(): String {
        return "00000000-0000-0000-0000-000000000000"
    }

    override fun getBypassSecret(): String? {
        return sessionConfiguration.bypassSecret
    }
}