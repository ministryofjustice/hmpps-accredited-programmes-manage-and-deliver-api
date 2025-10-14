package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common.exception.ConflictException
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.ProgrammeGroupFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ProgrammeGroupRepository

class ProgrammeGroupServiceIntegrationTest : IntegrationTestBase() {

  @Autowired
  private lateinit var programmeGroupService: ProgrammeGroupService

  @Autowired
  private lateinit var programmeGroupRepository: ProgrammeGroupRepository

  @BeforeEach
  override fun beforeEach() {
    testDataCleaner.cleanAllTables()
  }

  @AfterEach
  fun afterEach() {
    testDataCleaner.cleanAllTables()
  }

  @Test
  fun `createGroup can successfully create a new group`() {
    val groupCode = "AAA111"
    programmeGroupService.createGroup(groupCode)
    val createdGroup = programmeGroupRepository.findByCode(groupCode)
    assertThat { createdGroup }.isNotNull
    assertThat(createdGroup?.code).isEqualTo(groupCode)
  }

  @Test
  fun `createGroup throws an error if a group already exists`() {
    val groupCode = "AAA111"
    val existingGroup = ProgrammeGroupFactory().withCode(groupCode).produce()
    testDataGenerator.createGroup(existingGroup)

    assertThrows<ConflictException> { programmeGroupService.createGroup(groupCode) }
  }
}
