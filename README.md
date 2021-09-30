<a href="https://codeclimate.com/repos/6037df8d1f799401a100ee8c/maintainability"><img src="https://api.codeclimate.com/v1/badges/1da7383a198367950d32/maintainability" /></a>

# Dreiattest

## Installation
First, add the github packages maven repo:
```groovy
repositories {
    ...
    maven {
        url = uri("https://maven.pkg.github.com/dreipol/dreiAttest-android")
        credentials {
            username = <Github-Username>
            password = <Github-Token>
        }
    }
}
```

### Multiplatform
Add the dependendency to your `build.gradle`:
```groovy
implementation("ch.dreipol.dreiattest.multiplatform:multiplatform:<version>")
```

### Android only
Add the android only dependency to your `build.gradle`:
```groovy
implementation("ch.dreipol.dreiattest.multiplatform:multiplatform-android:<version>")
```

## Usage

### `AttestationProvider`
The library uses the `AttestationProvider` - interface to wrap the actual google or apple device attestation services.

#### Android
For android dreiattest is using SafetyNet for your device attestation. To use this service you need to create an api key, which is described [here](https://developer.android.com/training/safetynet/attestation#obtain-api-key).

### `DreiAttestService`
To use the `DreiAttestService` create a new instance and call the `initWith` - function, as follows:
```kotlin
val attestationProvider = ... // GoogleAttestationProvider on Android / AppleAttestationProvider on iOS
val attestService = DreiAttestService()
try {
    attestService.initWith(baseAddress = "https://example.com/attested", sessionConfiguration = SessionConfiguration(user = "hello@example.com", attestationProvider = attestationProvider))
} catch (e: UnsupportedException) {
    // handle running on unsupported devices such as iOS Simulators
}
```

You would typically want to create the `GoogleAttestionProvider` in your application's `onCreate` and the `AppleAttestationProvider` in your `application(_:didFinishLaunchingWithOptions:)` method and pass it to your multiplatform code from there.

### Ktor - feature
There is a ktor-client feature availlable, you can use it as follows:
```kotlin
HttpClient {
        ...

        install(DreiAttestFeature) {
            this.attestService = attestService
        }

        ...
    }
```
The feature is now signing every request, for which the url matches the configured `baseAddress` in the `DreiAttestService`.

### Development
During development it may be useful to setup a shared secret on the server to bypass dreiAttest. You can pass this shared secret to the library using the DREIATTEST_BYPASS_SECRET environment variable or by passing it to the AttestServie in its initializer.
