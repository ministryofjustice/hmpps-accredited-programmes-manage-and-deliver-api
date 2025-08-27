package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository

import jakarta.transaction.Transactional
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.integration.IntegrationTestBase

open class RegionRepositoryIntegrationTest : IntegrationTestBase() {
  @Autowired
  private lateinit var repository: RegionRepository

  @Test
  @Transactional
  open fun `should retrieve a known Region`() {
    var southCentral = repository.findById("H").orElseThrow()
    assertThat(southCentral).isNotNull
    assertThat(southCentral.regionName).isEqualTo("South Central")
    assertThat(southCentral.pdus.size).isEqualTo(7)
  }
}
