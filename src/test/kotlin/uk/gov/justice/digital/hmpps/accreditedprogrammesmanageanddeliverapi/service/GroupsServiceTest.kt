package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common.exception.ConflictException
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.GroupsFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.GroupsRepository

class GroupsServiceTest : IntegrationTestBase() {

  @Autowired
  private lateinit var groupsService: GroupsService

  @Autowired
  private lateinit var groupsRepository: GroupsRepository

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
    groupsService.createGroup(groupCode)
    val createdGroup = groupsRepository.findByCode(groupCode)
    assertThat { createdGroup }.isNotNull
    assertThat(createdGroup?.code).isEqualTo(groupCode)
  }

  @Test
  fun `createGroup throws an error if a group already exists`() {
    val groupCode = "AAA111"
    val existingGroup = GroupsFactory().withCode(groupCode).produce()
    testDataGenerator.createGroup(existingGroup)

    assertThrows<ConflictException> { groupsService.createGroup(groupCode) }
  }
}
