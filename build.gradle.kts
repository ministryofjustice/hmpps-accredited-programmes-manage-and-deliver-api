import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  id("uk.gov.justice.hmpps.gradle-spring-boot") version "11.0.7"
  kotlin("plugin.spring") version "2.4.10"
  kotlin("plugin.jpa") version "2.4.10"
  kotlin("plugin.allopen") version "2.4.10"
  id("io.sentry.jvm.gradle") version "6.21.0"
}

configurations {
  testImplementation { exclude(group = "org.junit.vintage") }
}

dependencies {
  val shedLockVersion = "7.10.0"
  implementation("uk.gov.justice.service.hmpps:hmpps-kotlin-spring-boot-starter:3.0.1")
  implementation("uk.gov.justice.service.hmpps:hmpps-sqs-spring-boot-starter:7.4.1")
  implementation("org.springframework.boot:spring-boot-starter-webclient")
  implementation("org.springframework.boot:spring-boot-starter-webflux")
  implementation("org.springframework.boot:spring-boot-starter-data-jpa")
  implementation("org.springframework.boot:spring-boot-starter-flyway")
  implementation("org.springframework.boot:spring-boot-starter-web")
  implementation("org.springframework.boot:spring-boot-starter-security")
  implementation("org.springframework.boot:spring-boot-starter-oauth2-client")
  implementation("org.springframework.boot:spring-boot-starter-cache")
  implementation("com.github.ben-manes.caffeine:caffeine")
  implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:3.1.0")
  implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-csv")
  implementation("com.fasterxml.jackson.core:jackson-databind")
  implementation("net.javacrumbs.shedlock:shedlock-spring:$shedLockVersion")
  implementation("net.javacrumbs.shedlock:shedlock-provider-jdbc-template:$shedLockVersion")

  // Seeding
  implementation("net.datafaker:datafaker:2.7.0")

  // Coroutines
  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.11.0")
  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.11.0")

  // security
  implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
  implementation("org.springframework.security:spring-security-crypto")
  implementation("com.nimbusds:oauth2-oidc-sdk")
  implementation("io.github.resilience4j:resilience4j-spring-boot2:2.3.0")

  testImplementation("uk.gov.justice.service.hmpps:hmpps-kotlin-spring-boot-starter-test:3.0.1")
  testImplementation("org.springframework.boot:spring-boot-starter-webclient-test")
  testImplementation("org.springframework.boot:spring-boot-starter-webflux-test")
  testImplementation("org.wiremock:wiremock-standalone:3.13.2")
  testImplementation("io.swagger.parser.v3:swagger-parser:2.1.48") {
    exclude(group = "io.swagger.core.v3")
  }
  testImplementation("org.testcontainers:testcontainers:2.0.5")
  testImplementation("org.testcontainers:testcontainers-postgresql:2.0.5")
  testImplementation("org.testcontainers:testcontainers-localstack:2.0.5")
  testImplementation("org.testcontainers:testcontainers-junit-jupiter:2.0.5")
  testImplementation("com.ninja-squad:springmockk:5.0.1")
  testImplementation("io.kotest:kotest-assertions-core:6.2.4")
  testImplementation("io.mockk:mockk:1.14.11")
  testImplementation("org.awaitility:awaitility-kotlin:4.3.0")
  testImplementation("uk.gov.justice.service.hmpps:hmpps-subject-access-request-test-support:2.8.1")

  runtimeOnly("org.flywaydb:flyway-database-postgresql")
  runtimeOnly("org.postgresql:postgresql:42.7.13")
}

kotlin {
  jvmToolchain(25)
}

// This test is required for schema spy implementation and should NOT be run alongside our normal testsuite as it runs against a local application rather than the testcontainers instances.
tasks.test {
  exclude("**/InitialiseDatabase.class")
  // Netty loads native libraries; on JDK 24+ this needs explicit native-access to avoid warnings.
  jvmArgs("--enable-native-access=ALL-UNNAMED")
}

val test = testing.suites.named<JvmTestSuite>("test")

tasks.register<Test>("initialiseDatabase") {
  testClassesDirs = files(test.map { it.sources.output.classesDirs })
  classpath = files(test.map { it.sources.runtimeClasspath })
  include("**/InitialiseDatabase.class")
  onlyIf { gradle.startParameter.taskNames.contains("initialiseDatabase") }
}

tasks {
  withType<KotlinCompile> {
    compilerOptions.jvmTarget = JvmTarget.JVM_25
  }
}
allOpen {
  annotation("jakarta.persistence.Entity")
  annotation("jakarta.persistence.MappedSuperclass")
  annotation("jakarta.persistence.Embeddable")
}
