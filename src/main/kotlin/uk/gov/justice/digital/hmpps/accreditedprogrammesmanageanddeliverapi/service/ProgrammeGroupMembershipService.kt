package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service

import org.slf4j.LoggerFactory
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common.exception.BusinessException
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common.exception.ConflictException
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common.exception.NotFoundException
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ProgrammeGroupMembershipEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ReferralEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.currentlyAllocatedGroup
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ProgrammeGroupRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ReferralRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ReferralStatusDescriptionRepository
import java.util.UUID

@Service
@Transactional
class ProgrammeGroupMembershipService(
  private val programmeGroupRepository: ProgrammeGroupRepository,
  private val referralRepository: ReferralRepository,
  private val referralStatusDescriptionRepository: ReferralStatusDescriptionRepository,
) {
  private val log = LoggerFactory.getLogger(this::class.java)

  fun allocateReferralToGroup(referralId: UUID, groupId: UUID): ReferralEntity? {
    val referral =
      referralRepository.findByIdOrNull(referralId) ?: throw NotFoundException("Referral with id $referralId not found")
    val group =
      programmeGroupRepository.findByIdOrNull(groupId) ?: throw NotFoundException("Group with id $groupId not found")

    val latestStatus = referralStatusDescriptionRepository.findMostRecentStatusByReferralId(referralId)
    if (latestStatus?.isClosed == true) {
      throw BusinessException("Cannot assign referral to group as referral with id ${referral.id} is in a closed state")
    }

    if (referral.currentlyAllocatedGroup() != null) {
      throw ConflictException("Referral with id ${referral.id} is already allocated to group ${group.code}")
    }

    log.info("Adding referral with id: $referralId to group with id: $groupId and groupCode: ${group.code}")
    referral.programmeGroupMemberships.add(ProgrammeGroupMembershipEntity(referral = referral, programmeGroup = group))
    return referralRepository.save(referral)
  }
}
