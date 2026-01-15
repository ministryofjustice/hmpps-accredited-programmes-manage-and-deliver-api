package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service

import org.slf4j.LoggerFactory
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup.RemoveFromGroupRequest
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common.exception.BusinessException
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common.exception.ConflictException
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common.exception.NotFoundException
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.AttendeeEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ProgrammeGroupEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ProgrammeGroupMembershipEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ReferralEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ReferralStatusHistoryEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ProgrammeGroupMembershipRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ProgrammeGroupRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ReferralRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ReferralStatusDescriptionRepository
import java.time.LocalDateTime
import java.util.UUID

@Service
@Transactional
class ProgrammeGroupMembershipService(
  private val programmeGroupRepositoryImpl: ProgrammeGroupRepository,
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

    val group = programmeGroupRepositoryImpl.findByIdOrNull(groupId)
      ?: throw NotFoundException("Group with id $groupId not found")

    val latestStatus = referralStatusDescriptionRepository.findMostRecentStatusByReferralId(referralId)
    if (latestStatus?.isClosed == true) {
      throw BusinessException("Cannot assign referral to group as referral with id ${referral.id} is in a closed state")
    }

    getCurrentlyAllocatedGroup(referral)
      ?.let { throw ConflictException("Referral with id ${referral.id} is already allocated to a group: ${it.programmeGroup.code}") }

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
    val currentGroupMembership = programmeGroupMembershipRepository.findCurrentGroupByReferralId(referralId)
      ?: throw NotFoundException("No group membership found for referral $referralId")

    group.sessions.forEach { session ->
      session.attendees.add(
        AttendeeEntity(
          referral = currentGroupMembership.referral,
          session = session,
        ),
      )
    }

    return referralRepository.save(referral)
  }

  fun getCurrentlyAllocatedGroup(referral: ReferralEntity): ProgrammeGroupMembershipEntity? {
    val membership = programmeGroupMembershipRepository.findCurrentGroupByReferralId(referral.id!!) ?: return null

    if (membership.deletedAt == null) return membership

    return null
  }

  fun getActiveGroupMemberships(groupId: UUID): List<ProgrammeGroupMembershipEntity> = programmeGroupMembershipRepository.findAllActiveByProgrammeGroupId(groupId)

  fun removeReferralFromGroup(
    referralId: UUID,
    groupId: UUID,
    removedFromGroupBy: String,
    removeFromGroupRequest: RemoveFromGroupRequest,
  ): ReferralEntity {
    log.info("Attempting to remove Referral with id: $referralId from group with id: $groupId...")

    val group = (
      programmeGroupRepositoryImpl.findByIdOrNull(groupId)
        ?: throw NotFoundException("Group with id $groupId not found")
      )

    val referral = referralRepository.findByIdOrNull(referralId)
      ?: throw NotFoundException("Referral with id $referralId not found")

    deleteGroupMembershipForReferralAndGroup(referral, group, removedFromGroupBy)

    val desiredStatus = referralStatusDescriptionRepository.findByIdOrNull(removeFromGroupRequest.referralStatusDescriptionId)
      ?: throw NotFoundException("No Referral Status Description found for id: ${removeFromGroupRequest.referralStatusDescriptionId}")

    val statusHistory =
      ReferralStatusHistoryEntity(
        referral = referral,
        referralStatusDescription = desiredStatus,
        additionalDetails = removeFromGroupRequest.additionalDetails,
        createdBy = removedFromGroupBy,
      )

    referral.statusHistories.add(statusHistory)
    return referralRepository.save(referral)
  }

  fun deleteGroupMembershipForReferralAndGroup(
    referral: ReferralEntity,
    group: ProgrammeGroupEntity,
    deletedByUsername: String,
  ): ProgrammeGroupEntity {
    val groupMembership = programmeGroupMembershipRepository.findNonDeletedByReferralAndGroupIds(referral.id!!, group.id!!)
      ?: throw NotFoundException(
        "No active Membership found for Referral (${referral.id}) and Group (${group.id})",
      )

    groupMembership.deletedAt = LocalDateTime.now()
    groupMembership.deletedByUsername = deletedByUsername

    // Remove the PoP from the list of attendees for any future sessions of the group they have been removed from
    val now = LocalDateTime.now()
    group.sessions.forEach { session -> session.attendees.removeIf { it.referral.id == referral.id!! && session.startsAt > now } }
    log.info("...Successfully found Referral (${referral.id}), Group (${group.id}), and Membership (${groupMembership.id}) to remove")
    return programmeGroupRepositoryImpl.save(group)
  }
}
