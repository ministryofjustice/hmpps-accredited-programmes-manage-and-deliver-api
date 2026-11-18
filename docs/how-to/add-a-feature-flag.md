# How to Add a Feature Flag

This guide provides a brief outline of how to add a Spring Boot feature flag to this codebase. The names below are illustrative; replace them with the property and environment variable names for your flag.

## 1. Define the Flag in `application.yml`

Add your feature flag under the `app.features` section in `src/main/resources/application.yml`, with an
environment variable placeholder and a safe default:

```yaml
app:
  features:
    example-feature-enabled: ${EXAMPLE_FEATURE_ENABLED:false}
```

The `${VAR:default}` form means the flag is off everywhere unless an environment explicitly overrides it, and it
documents the environment variable name in one place. If your flag has the same value in every environment, a plain
literal (`example-feature-enabled: false`) is fine — see the next section for making it environment specific.

## 2. Inject the Flag into Your Service

Use the `@Value` annotation to inject the property value into your Spring-managed bean. Ensure you import the correct class.

### Import
```kotlin
import org.springframework.beans.factory.annotation.Value
```

### Usage
In a Spring-managed service, inject the property:

```kotlin
@Value("${app.features.example-feature-enabled}")
private val exampleFeatureEnabled: Boolean
```

## 3. Use the Flag in Your Logic

You can then use this boolean to conditionally execute code.

```kotlin
if (exampleFeatureEnabled) {
  performOptionalOperation()
}
```

Use the flag to control the operation that should be temporary or environment-specific.

## 4. Make the Flag Environment Specific

To give a flag a different value per environment, set the environment variable in that environment's helm values file
under `generic-service.env`. This is how the other environment specific settings in this repo work, such as
`REPORTING_ENABLED` and `CACHE_EVICT_BANK_HOLIDAYS_CRON`.

```yaml
# helm_deploy/values-preprod.yaml
generic-service:
  env:
    EXAMPLE_FEATURE_ENABLED: "true"
```

Add the same line to `values-dev.yaml` or `values-prod.yaml` as the rollout progresses. Set the value explicitly in
each environment you care about, even when it matches the default — an explicit `"false"` makes it obvious that the
environment was considered, and turning the flag on later is a one word change.

Note the values must be quoted strings: helm renders them into the Kubernetes deployment as environment variables,
and Spring converts `"true"` / `"false"` back to `Boolean` when binding.

### Naming the environment variable

Spring's relaxed binding uppercases the property name and converts both dots and hyphens to underscores, so
`app.features.example-feature-enabled` can also be set directly as `APP_FEATURES_EXAMPLE_FEATURE_ENABLED` without
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

Set the property in test configuration when a test needs a default value. If a flag needs to be exercised in both
states, override it per test class with
`@TestPropertySource(properties = ["app.features.example-feature-enabled=false"])`.

## 5. (Optionally) Create a tech debt JIRA ticket for removing the feature flag

Once the feature flag is no longer needed, create a JIRA ticket to remove it. This will help maintain a clean codebase and ensure that feature flags are only used when necessary.