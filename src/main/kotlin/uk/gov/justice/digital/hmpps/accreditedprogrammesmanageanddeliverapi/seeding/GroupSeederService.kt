package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.seeding

import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ProgrammeGroupRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service.ProgrammeGroupService

@Service
@Profile("seeding")
class GroupSeederService(
  private val fakeGroupGenerator: FakeGroupGenerator,
  private val groupService: ProgrammeGroupService,
  private val programmeGroupRepository: ProgrammeGroupRepository,
) {

  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)

    private const val SEEDING_USERNAME = "SEEDING_SYSTEM"
  }

  @Transactional
  fun seedGroups(count: Int): GroupSeedingResult {
    val createdGroups = mutableListOf<SeededGroupInfo>()

    repeat(count) { _ ->
      val request = fakeGroupGenerator.generateCreateGroupRequest()

      val group = groupService.createGroup(request, SEEDING_USERNAME)

      createdGroups.add(
        SeededGroupInfo(
          groupId = group.id.toString(),
          groupCode = group.code,
          regionName = group.regionName,
        ),
      )
    }

    return GroupSeedingResult(count = createdGroups.size, groups = createdGroups)
  }

  /**
   * DANGER: Deletes ALL programme groups from the database. Only available under the 'seeding'
   * profile, which must NEVER be enabled in production or pre-production.
   */
  @Transactional
  fun dangerouslyDeleteAllGroups(): TeardownResult {
    val count = programmeGroupRepository.count()

    programmeGroupRepository.deleteAll()
    log.warn("DANGER: Deleted ALL $count programme groups from the database.")

    return TeardownResult(deletedCount = count.toInt())
  }
}

data class GroupSeedingResult(
  val count: Int,
  val groups: List<SeededGroupInfo>,
)

data class SeededGroupInfo(
  val groupId: String,
  val groupCode: String,
  val regionName: String,
)
