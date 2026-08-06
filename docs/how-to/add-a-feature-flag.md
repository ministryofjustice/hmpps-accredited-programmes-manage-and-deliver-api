# How to Add a Feature Flag

This guide provides a brief outline of how to add a Spring Boot feature flag to this codebase, using the `lao-access-check-enabled` flag as a worked example.

## 1. Define the Flag in `application.yml`

Add your feature flag under the `app.features` section in `src/main/resources/application.yml`, with an
environment variable placeholder and a safe default:

```yaml
app:
  features:
    lao-access-check-enabled: ${LAO_ACCESS_CHECK_ENABLED:false}
```

The `${VAR:default}` form means the flag is off everywhere unless an environment explicitly overrides it, and it
documents the environment variable name in one place. If your flag has the same value in every environment, a plain
literal (`lao-access-check-enabled: false`) is fine — see the next section for making it environment specific.

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

## 4. Make the Flag Environment Specific

To give a flag a different value per environment, set the environment variable in that environment's helm values file
under `generic-service.env`. This is how the other environment specific settings in this repo work, such as
`REPORTING_ENABLED` and `CACHE_EVICT_BANK_HOLIDAYS_CRON`.

```yaml
# helm_deploy/values-preprod.yaml
generic-service:
  env:
    LAO_ACCESS_CHECK_ENABLED: "true"
```

Add the same line to `values-dev.yaml` or `values-prod.yaml` as the rollout progresses. Set the value explicitly in
each environment you care about, even when it matches the default — an explicit `"false"` makes it obvious that the
environment was considered, and turning the flag on later is a one word change.

Note the values must be quoted strings: helm renders them into the Kubernetes deployment as environment variables,
and Spring converts `"true"` / `"false"` back to `Boolean` when binding.

### Naming the environment variable

Spring's relaxed binding uppercases the property name and converts both dots and hyphens to underscores, so
`app.features.lao-access-check-enabled` can also be set directly as `APP_FEATURES_LAO_ACCESS_CHECK_ENABLED` without
the `${...}` placeholder from step 1. This is the same rule that lets `SPRINGDOC_SWAGGER_UI_ENABLED` in
`values-prod.yaml` override `springdoc.swagger-ui.enabled`. Prefer the shorter placeholder name — it keeps the helm
files readable and makes the flag's environment variable discoverable from `application.yml`.

### Why not `application-preprod.yml`?

Each environment does activate a Spring profile (`SPRING_PROFILES_ACTIVE` is set in every `values-<env>.yaml`), so
adding `src/main/resources/application-preprod.yml` would also work. Prefer the helm approach:

- Environment variables take precedence over *every* `application-*.yml` file. If a flag is set in both places, the
  helm value silently wins and the profile file becomes dead config.
- All the environment specific configuration stays in one place, next to the URLs and secrets for that environment.
- `application-dev.yml` is the *deployed* dev environment, not your local machine — local development uses the
  `local` profile. Putting a flag in `application-dev.yml` ships it to dev.

Either way, changing a flag requires a deployment. Neither mechanism can be toggled at runtime.

### Tests

`src/test/resources/application-test.yml` sets its own value for `app.features`, so integration tests are unaffected
by the helm values. If a flag needs to be exercised in both states, override it per test class with
`@TestPropertySource(properties = ["app.features.lao-access-check-enabled=false"])`.

## 5. (Optionally) Create a tech debt JIRA ticket for removing the feature flag

Once the feature flag is no longer needed, create a JIRA ticket to remove it. This will help maintain a clean codebase and ensure that feature flags are only used when necessary.