package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service

import jakarta.persistence.criteria.Predicate
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.domain.Specification
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.GroupWaitlistItem
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.OffenceCohort
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.ProgrammeGroupDetails
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.toApi
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common.exception.ConflictException
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common.exception.NotFoundException
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.GroupWaitlistItemViewEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.GroupWaitlistItemViewRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ProgrammeGroupEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ProgrammeGroupRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ReferralReportingLocationRepository
import java.util.UUID

@Service
@Transactional
class ProgrammeGroupService(
  private val programmeGroupRepository: ProgrammeGroupRepository,
  private val groupWaitlistItemViewRepository: GroupWaitlistItemViewRepository,
  private val referralReportingLocationRepository: ReferralReportingLocationRepository,
) {
  fun createGroup(groupCode: String): ProgrammeGroupEntity? {
    programmeGroupRepository.findByCode(groupCode)?.let { throw ConflictException("Programme group with code $groupCode already exists") }

    return programmeGroupRepository.save(
      ProgrammeGroupEntity(
        code = groupCode,
      ),
    )
  }

  fun getGroupById(groupId: UUID): ProgrammeGroupEntity = programmeGroupRepository.findByIdOrNull(groupId)
    ?: throw NotFoundException("Programme group with id $groupId not found")

  fun getGroupWaitlistData(
    groupId: UUID,
    sex: String?,
    cohort: OffenceCohort?,
    nameOrCRN: String?,
    pdu: String?,
    pageable: Pageable,
  ): Page<GroupWaitlistItem> {
    // Verify the group exists first
    getGroupById(groupId)

    val specification = Specification<GroupWaitlistItemViewEntity> { root, _, criteriaBuilder ->
      val predicates = mutableListOf<Predicate>()

      predicates.add(criteriaBuilder.equal(root.get<UUID>("activeProgrammeGroupId"), groupId))

      sex?.let {
        predicates.add(criteriaBuilder.equal(root.get<String>("sex"), it))
      }

      cohort?.let {
        predicates.add(criteriaBuilder.equal(root.get<String>("cohort"), it.name))
      }

      nameOrCRN?.let { search ->
        val searchTerm = "%${search.lowercase()}%"
        predicates.add(
          criteriaBuilder.or(
            criteriaBuilder.like(criteriaBuilder.lower(root.get("personName")), searchTerm),
            criteriaBuilder.like(criteriaBuilder.lower(root.get("crn")), searchTerm),
          ),
        )
      }

      pdu?.let {
        predicates.add(criteriaBuilder.equal(root.get<String>("pduName"), it))
      }

      criteriaBuilder.and(*predicates.toTypedArray())
    }

    return groupWaitlistItemViewRepository.findAll(specification, pageable)
      .map { it.toApi() }
  }

  fun getGroupFilters(): ProgrammeGroupDetails.Filters {
    val referralReportingLocations = referralReportingLocationRepository.getPdusAndReportingTeams()
    val distinctPdus = referralReportingLocations.map { it.pduName }.distinct()
    val distinctReportingTeams = referralReportingLocations.map { it.reportingTeam }.distinct()

    return ProgrammeGroupDetails.Filters(
      pduNames = distinctPdus,
      reportingTeams = distinctReportingTeams,
    )
  }
}
