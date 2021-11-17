package ch.dreipol.dreiattest.multiplatform.mock

import ch.dreipol.dreiattest.multiplatform.utils.SystemInfo

object SystemInfoMock : SystemInfo {
    override val appVersion: String = "test"
    override val appBuild: String = "test"
    override val appIdentifier: String = "test"
    override val osVersion: String = "test"
}