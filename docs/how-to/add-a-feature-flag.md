# How to Add a Feature Flag

This guide provides a brief outline of how to add a Spring Boot feature flag to this codebase, using the `lao-access-check-enabled` flag as a worked example.

## 1. Define the Flag in `application.yml`

Add your feature flag under the `app.features` section in `src/main/resources/application.yml`.

```yaml
app:
  features:
    lao-access-check-enabled: false
```

## 2. Inject the Flag into Your Service

Use the `@Value` annotation to inject the property value into your Spring-managed bean. Ensure you import the correct class.

### Import
```kotlin
import org.springframework.beans.factory.annotation.Value
```

### Usage
In your class (e.g., `ReferralService`), inject the property:

```kotlin
@Value("${app.features.lao-access-check-enabled}")
private val laoAccessCheckEnabled: Boolean
```

## 3. Use the Flag in Your Logic

You can then use this boolean to conditionally execute code.

```kotlin
var isLAO = false
if (laoAccessCheckEnabled) {
  isLAO = getLaoByCrn(referral.crn)
}
```

In this example, the `lao-access-check-enabled` flag controls whether the application performs a Limited Access Offender (LAO) check via the nDelius API.

## 4. (Optionally) Create a tech debt JIRA ticket for removing the feature flag

Once the feature flag is no longer needed, create a JIRA ticket to remove it. This will help maintain a clean codebase and ensure that feature flags are only used when necessary.