package ch.dreipol.dreiattest.multiplatform

public sealed class AttestError(message: String? = null) : Exception(message)

public class InvalidUsernameError(username: String) :
    AttestError("Username $username not matching ${DreiAttestService.usernamePattern.pattern}")