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

### `DeviceAttestationServices`
The library uses the `DeviceAttestationService` - class to generate the actual google or apple device attestation. You instantiate it as followed:
#### Android
For android dreiattest is using SafetyNet for your device attestation. To use this service you need to create an api key, which is described [here](https://developer.android.com/training/safetynet/attestation#obtain-api-key).
```kotlin
val attestationService = DeviceAttestationService(context = context, apiKey = <your SafetyNet API key>)
```
#### iOS
```swift
let attestationService = DeviceAttestationService()
```

### `DreiAttestService`
To use the `DreiAttestService` create a new instance and call the `initWith` - function, like followed:
```kotlin
val attestService = DreiAttestService()
attestService.initWith(baseAddress = "https://example.com/attested", sessionConfiguration = SessionConfiguration(user = "hello@example.com", deviceAttestationService = deviceAttestationService))
```

### Ktor - feature
There is a ktor-client feature availlable, you can use it as followed:
```kotlin
HttpClient {
        ...

        install(DreiAttestFeature) {
            this.attestService = attestService
        }

        ...
    }
```
The feature is now signing every request, if the url is matching the configured `baseAddress` in the `DreiAttestService`.