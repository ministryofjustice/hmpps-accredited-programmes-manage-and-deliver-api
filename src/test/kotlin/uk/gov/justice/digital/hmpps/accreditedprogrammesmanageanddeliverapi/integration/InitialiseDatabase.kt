package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.integration

import org.junit.jupiter.api.Test

class InitialiseDatabase : IntegrationTestBase() {

  // This is needed to initialise the database for schema spy
  @Test
  fun `initialises database`() {
    println("Database has been initialised by IntegrationTestBase")
  }
}
