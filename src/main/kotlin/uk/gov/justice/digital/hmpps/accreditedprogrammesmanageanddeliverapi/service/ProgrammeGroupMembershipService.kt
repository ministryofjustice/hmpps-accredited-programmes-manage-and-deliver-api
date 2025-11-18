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
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ReferralStatusHistoryEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ProgrammeGroupMembershipRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ProgrammeGroupRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ReferralRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ReferralStatusDescriptionRepository
import java.time.LocalDateTime
import java.util.*

@Service
@Transactional
class ProgrammeGroupMembershipService(
  private val programmeGroupRepository: ProgrammeGroupRepository,
  private val referralRepository: ReferralRepository,
  private val referralStatusDescriptionRepository: ReferralStatusDescriptionRepository,
  private val programmeGroupMembershipRepository: ProgrammeGroupMembershipRepository,
) {
  private val log = LoggerFactory.getLogger(this::class.java)

  fun allocateReferralToGroup(
    referralId: UUID,
    groupId: UUID,
    allocatedToGroupBy: String,
    additionalDetails: String,
  ): ReferralEntity {
    val referral = referralRepository.findByIdOrNull(referralId)
      ?: throw NotFoundException("Referral with id $referralId not found")

    val group = programmeGroupRepository.findByIdOrNull(groupId)
      ?: throw NotFoundException("Group with id $groupId not found")

    val latestStatus = referralStatusDescriptionRepository.findMostRecentStatusByReferralId(referralId)
    if (latestStatus?.isClosed == true) {
      throw BusinessException("Cannot assign referral to group as referral with id ${referral.id} is in a closed state")
    }

    getCurrentlyAllocatedGroup(referral)
      ?.let { throw ConflictException("Referral with id ${referral.id} is already allocated to group ${group.code}") }

    log.info("Adding referral with id: $referralId to group with id: $groupId and groupCode: ${group.code}")

    referral.programmeGroupMemberships.add(ProgrammeGroupMembershipEntity(referral = referral, programmeGroup = group))

    val statusHistory =
      ReferralStatusHistoryEntity(
        referral = referral,
        referralStatusDescription = referralStatusDescriptionRepository.getScheduledStatusDescription(),
        additionalDetails = additionalDetails,
        createdBy = allocatedToGroupBy,
      )

    referral.statusHistories.add(statusHistory)

    return referralRepository.save(referral)
  }

  fun getCurrentlyAllocatedGroup(referral: ReferralEntity): ProgrammeGroupMembershipEntity? {
    val membership = programmeGroupMembershipRepository.findCurrentGroupByReferralId(referral.id!!) ?: return null

    if (membership.deletedAt == null) return membership

    return null
  }

  fun removeReferralFromGroup(
    referralId: UUID,
    groupId: UUID,
    removedFromGroupBy: String,
    additionalDetails: String?,
    newStatusDescriptionId: UUID,
  ): ReferralEntity {
    log.info("Attempting to remove Referral with id: $referralId from group with id: $groupId...")

    programmeGroupRepository.findByIdOrNull(groupId) ?: throw NotFoundException("Group with id $groupId not found")

    val referral = referralRepository.findByIdOrNull(referralId) ?: throw NotFoundException("Referral with id $referralId not found")

    deleteGroupMembershipForReferralAndGroup(referralId, groupId, removedFromGroupBy)

    val statusDescription = referralStatusDescriptionRepository.findByIdOrNull(newStatusDescriptionId)
      ?: throw NotFoundException("No Referral Status Description found for id: $newStatusDescriptionId")

    val statusHistory =
      ReferralStatusHistoryEntity(
        referral = referral,
        referralStatusDescription = statusDescription,
        additionalDetails = additionalDetails,
        createdBy = removedFromGroupBy,
      )

    referral.statusHistories.add(statusHistory)

    return referralRepository.save(referral)
  }

  fun deleteGroupMembershipForReferralAndGroup(
    referralId: UUID,
    groupId: UUID,
    deletedByUsername: String,
  ): ProgrammeGroupMembershipEntity {
    val groupMembership = programmeGroupMembershipRepository.findByReferralAndProgrammeGroup(referralId, groupId) ?: throw NotFoundException(
      "No active Membership found for Referral ($referralId) and Group ($groupId)",
    )

    groupMembership.deletedAt = LocalDateTime.now()
    groupMembership.deletedByUsername = deletedByUsername
    log.info("...Successfully found Referral ($referralId), Group ($groupId), and Membership (${groupMembership.id}) to remove")
    return programmeGroupMembershipRepository.save(groupMembership)
  }
}
