package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository

import jakarta.transaction.Transactional
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.integration.IntegrationTestBase

open class PduRepositoryIntegrationTest : IntegrationTestBase() {
  @Autowired
  private lateinit var repository: PduRepository

  @Test
  @Transactional
  open fun `should retrieve a known PDU`() {
    println("Number of PDUs: ${repository.count()}")
    var suffolk = repository.findById(8).orElseThrow()
    assertThat(suffolk.pduName).isEqualTo("Suffolk")
    assertThat(suffolk.region?.regionName).isEqualTo("East of England")
  }
}
