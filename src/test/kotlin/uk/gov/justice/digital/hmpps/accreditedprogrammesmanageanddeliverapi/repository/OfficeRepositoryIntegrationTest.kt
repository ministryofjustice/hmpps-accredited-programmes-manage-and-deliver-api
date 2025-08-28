package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository

import jakarta.transaction.Transactional
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.integration.IntegrationTestBase

open class OfficeRepositoryIntegrationTest : IntegrationTestBase() {
  @Autowired
  private lateinit var repository: OfficeRepository

  @Test
  @Transactional
  open fun `should retrieve a known Office`() {
    var castleHouseOffice = repository.findById("14").orElseThrow()
    assertThat(castleHouseOffice).isNotNull
    assertThat(castleHouseOffice.officeName).isEqualTo("Castle House")
    assertThat(castleHouseOffice.region?.regionName).isEqualTo("East Midlands")
    assertThat(castleHouseOffice.pdu?.pduName).isEqualTo("Nottingham City")
  }
}
