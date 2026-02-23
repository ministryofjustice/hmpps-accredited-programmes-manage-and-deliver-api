import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  id("uk.gov.justice.hmpps.gradle-spring-boot") version "10.0.4"
  kotlin("plugin.spring") version "2.3.10"
  kotlin("plugin.jpa") version "2.3.10"
  kotlin("plugin.allopen") version "2.3.10"
  id("io.sentry.jvm.gradle") version "6.1.0"
}

configurations {
  testImplementation { exclude(group = "org.junit.vintage") }
}

dependencies {
  implementation("uk.gov.justice.service.hmpps:hmpps-kotlin-spring-boot-starter:2.0.1")
  implementation("uk.gov.justice.service.hmpps:hmpps-sqs-spring-boot-starter:7.0.1")
  implementation("org.springframework.boot:spring-boot-starter-webclient")
  implementation("org.springframework.boot:spring-boot-starter-webflux")
  implementation("org.springframework.boot:spring-boot-starter-data-jpa")
  implementation("org.springframework.boot:spring-boot-starter-flyway")
  implementation("org.springframework.boot:spring-boot-starter-web")
  implementation("org.springframework.boot:spring-boot-starter-security")
  implementation("org.springframework.boot:spring-boot-starter-oauth2-client")
  implementation("org.springframework.boot:spring-boot-starter-cache")
  implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:3.0.1")

  // Seeding
  implementation("net.datafaker:datafaker:2.5.4")

  // Coroutines
  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.10.2")

  // security
  implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server:4.0.3")
  implementation("org.springframework.boot:spring-boot-starter-oauth2-client:4.0.3")
  implementation("org.springframework.security:spring-security-crypto:7.0.3")
  implementation("com.nimbusds:oauth2-oidc-sdk:11.33")
  implementation("io.github.resilience4j:resilience4j-spring-boot2:2.3.0")

  testImplementation("uk.gov.justice.service.hmpps:hmpps-kotlin-spring-boot-starter-test:2.0.1")
  testImplementation("org.springframework.boot:spring-boot-starter-webclient-test")
  testImplementation("org.springframework.boot:spring-boot-starter-webflux-test")
  testImplementation("org.wiremock:wiremock-standalone:3.13.2")
  testImplementation("io.swagger.parser.v3:swagger-parser:2.1.38") {
    exclude(group = "io.swagger.core.v3")
  }
  testImplementation("org.testcontainers:testcontainers:2.0.3")
  testImplementation("org.testcontainers:localstack:1.21.4")
  testImplementation("org.testcontainers:postgresql:1.21.4")
  testImplementation("org.testcontainers:junit-jupiter:1.21.4")
  testImplementation("com.ninja-squad:springmockk:5.0.1")
  testImplementation("io.kotest:kotest-assertions-core:6.1.3")
  testImplementation("io.mockk:mockk:1.14.9")
  testImplementation("org.awaitility:awaitility-kotlin:4.3.0")

  runtimeOnly("org.flywaydb:flyway-database-postgresql")
  runtimeOnly("org.postgresql:postgresql:42.7.10")
}

kotlin {
  jvmToolchain(21)
}

// This test is required for schema spy implementation and should NOT be run alongside our normal testsuite as it runs against a local application rather than the testcontainers instances.
tasks.test {
  exclude("**/InitialiseDatabase.class")
}

val test by testing.suites.existing(JvmTestSuite::class)

tasks.register<Test>("initialiseDatabase") {
  testClassesDirs = files(test.map { it.sources.output.classesDirs })
  classpath = files(test.map { it.sources.runtimeClasspath })
  include("**/InitialiseDatabase.class")
  onlyIf { gradle.startParameter.taskNames.contains("initialiseDatabase") }
}

tasks {
  withType<KotlinCompile> {
    compilerOptions.jvmTarget = org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21
  }
}
val compileKotlin: KotlinCompile by tasks
compileKotlin.compilerOptions {
  freeCompilerArgs.set(listOf("-Xannotation-default-target=param-property"))
}

allOpen {
  annotation("jakarta.persistence.Entity")
  annotation("jakarta.persistence.MappedSuperclass")
  annotation("jakarta.persistence.Embeddable")
}
val compileTestKotlin: KotlinCompile by tasks
compileTestKotlin.compilerOptions {
  freeCompilerArgs.set(listOf("-Xannotation-default-target=param-property"))
}
