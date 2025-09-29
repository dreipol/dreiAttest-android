package ch.dreipol.dreiattest.multiplatform.utils

public interface SystemInfo {
    public val appVersion: String?
    public val appBuild: String
    public val appIdentifier: String
    public val osVersion: String
}

const val NULL_FALLBACK = "unknown"