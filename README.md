# dreiAttest
<a href="https://codeclimate.com/repos/6037df8d1f799401a100ee8c/maintainability"><img src="https://api.codeclimate.com/v1/badges/1da7383a198367950d32/maintainability" /></a>

dreiAttest implements Google's [SafetyNet](https://developer.android.com/training/safetynet) and Apple's [DeviceCheck Frameworks](https://developer.apple.com/documentation/devicecheck) to allow you to verify that request made to your server come from an actual device. It can be used in Android and Kotlin Multiplatform projects. An [iOS-only version](https://github.com/dreipol/dreiAttest-ios) is also available. To use dreiAttest you need to run [dreiAttest on your server](https://github.com/dreipol/dreiAttest-django).

Typically only certain endpoints over which sensitive data can be accessed are protected by dreiAttest. For this reason you define a base URL: requests starting with this base URL are handled by dreiAttest, while requests to other endpoints are simply forwarded to your server. For example if you define the base URL `https://example.com/attested`:
- Requests to `https://example.com/login` are **not** handled by dreiAttest
- Requests to `https://example.com/attested/profile-info` are handle by dreiAttest

You should only create a an `AttestService` after the user has logged in and pass in your service's user id. dreiAttest will generate a new key every time a user logs in with a different account. Apple counts these keys for you and allows you to identify suspicious login behavior.

For more information on how dreiAttest works read the [whitepaper](https://dreiattest.readthedocs.io/en/latest/index.html) or our [blog post]().

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

### `DeviceAttestationServices`
The library uses the `DeviceAttestationService` - class to generate the actual google or apple device attestation

#### Android
For android dreiattest is using SafetyNet for your device attestation. To use this service you need to create an api key, which is described [here](https://developer.android.com/training/safetynet/attestation#obtain-api-key).

### `DreiAttestService`
To use the `DreiAttestService` create a new instance and call the `initWith` - function, as follows:
```kotlin
val attestService = DreiAttestService()
attestService.initWith(baseAddress = "https://example.com/attested", sessionConfiguration = SessionConfiguration(user = "hello@example.com", deviceAttestationService = deviceAttestationService))
```

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
