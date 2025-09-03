package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.integration

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.test.context.ActiveProfiles
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.integration.wiremock.HmppsAuthApiExtension

@ExtendWith(HmppsAuthApiExtension::class)
@SpringBootTest(webEnvironment = RANDOM_PORT)
@ActiveProfiles("test")
class InitialiseDatabase {

  // This is needed to initialise the database for schema spy
  @Test
  fun `initialises database`() {
    println("Database has been initialised by IntegrationTestBase")
  }
}
