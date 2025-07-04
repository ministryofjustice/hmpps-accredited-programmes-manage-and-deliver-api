import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  id("uk.gov.justice.hmpps.gradle-spring-boot") version "8.3.0"
  kotlin("plugin.spring") version "2.2.0"
  kotlin("plugin.jpa") version "2.2.0"
  kotlin("plugin.allopen") version "2.2.0"
}

configurations {
  testImplementation { exclude(group = "org.junit.vintage") }
}

dependencies {
  implementation("uk.gov.justice.service.hmpps:hmpps-kotlin-spring-boot-starter:1.4.7")
  implementation("org.springframework.boot:spring-boot-starter-webflux")
  implementation("org.springframework.boot:spring-boot-starter-data-jpa")
  implementation("org.springframework.boot:spring-boot-starter-web")
  implementation("org.springframework.boot:spring-boot-starter-security")
  implementation("org.springframework.boot:spring-boot-starter-oauth2-client")
  implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.9")

  // AWS
  implementation("uk.gov.justice.service.hmpps:hmpps-sqs-spring-boot-starter:5.4.6")

  testImplementation("uk.gov.justice.service.hmpps:hmpps-kotlin-spring-boot-starter-test:1.4.7")
  testImplementation("org.wiremock:wiremock-standalone:3.13.1")
  testImplementation("io.swagger.parser.v3:swagger-parser:2.1.30") {
    exclude(group = "io.swagger.core.v3")
  }
  testImplementation("org.testcontainers:testcontainers:1.21.3")
  testImplementation("org.testcontainers:localstack:1.21.3")
  testImplementation("org.testcontainers:postgresql:1.21.3")
  testImplementation("org.testcontainers:junit-jupiter:1.21.3")
  testImplementation("org.awaitility:awaitility-kotlin")

  runtimeOnly("org.flywaydb:flyway-database-postgresql")
  runtimeOnly("org.postgresql:postgresql:42.7.7")
}

kotlin {
  jvmToolchain(21)
}

tasks {
  withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
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
