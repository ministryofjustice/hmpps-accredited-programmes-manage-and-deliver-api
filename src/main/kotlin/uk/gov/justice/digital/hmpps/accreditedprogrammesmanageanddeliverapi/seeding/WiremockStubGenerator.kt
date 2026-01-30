package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.seeding

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import java.io.File

@Component
@Profile("seeding")
class WiremockStubGenerator(
  private val objectMapper: ObjectMapper,
) {

  private val wiremockMappingsDir = File("./wiremock_mappings/seeded")

  init {
    wiremockMappingsDir.mkdirs()
  }

  fun generateStub(person: FakePerson) {
    val stub = objectMapper.createObjectNode().apply {
      // Request matching
      putObject("request").apply {
        put("method", "GET")
        put("urlPathPattern", "/case/${person.crn}/personal-details")
      }

      // Response
      putObject("response").apply {
        put("status", 200)
        putObject("headers").apply {
          put("Content-Type", "application/json")
        }
        set<ObjectNode>("jsonBody", buildResponseBody(person))
      }

      // Priority - seeded stubs should take precedence over generic mocks
      put("priority", 1)
    }

    val stubFile = File(wiremockMappingsDir, "seeded-person-${person.crn}.json")
    objectMapper.writerWithDefaultPrettyPrinter().writeValue(stubFile, stub)
  }

  private fun buildResponseBody(person: FakePerson): ObjectNode = objectMapper.createObjectNode().apply {
    put("crn", person.crn)
    putObject("name").apply {
      put("forename", person.firstName)
      put("surname", person.lastName)
    }
    put("dateOfBirth", person.dateOfBirth.toString())
    put("age", person.age)
    putObject("ethnicity").apply {
      put("code", person.ethnicityCode)
      put("description", person.ethnicityDescription)
    }
    putObject("sex").apply {
      put("code", person.sexCode)
      put("description", person.sexDescription)
    }
    putObject("probationPractitioner").apply {
      putObject("name").apply {
        put("forename", person.probationPractitionerForename)
        put("middleNames", person.probationPractitionerMiddleNames)
        put("surname", person.probationPractitionerSurname)
      }
      put("code", person.probationPractitionerCode)
      put("email", person.probationPractitionerEmail)
    }
    putObject("probationDeliveryUnit").apply {
      put("code", person.probationDeliveryUnitCode)
      put("description", person.probationDeliveryUnitDescription)
    }
    putObject("team").apply {
      put("code", person.teamCode)
      put("description", person.teamDescription)
    }
    putObject("region").apply {
      put("code", person.regionCode)
      put("description", person.regionDescription)
    }
  }

  fun clearSeededStubs() {
    wiremockMappingsDir.listFiles()?.forEach { it.delete() }
  }
}
