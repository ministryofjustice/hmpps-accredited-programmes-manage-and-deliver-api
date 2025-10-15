package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service

import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common.exception.BusinessException
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common.exception.ConflictException
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common.exception.NotFoundException
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ProgrammeGroupMembershipEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ReferralEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.currentlyAllocatedGroup
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.mostRecentStatus
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ProgrammeGroupRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ReferralRepository
import java.util.UUID

@Service
@Transactional
class ProgrammeGroupMembershipService(
  private val programmeGroupRepository: ProgrammeGroupRepository,
  private val referralRepository: ReferralRepository,
) {
  fun allocateReferralToGroup(referralId: UUID, groupId: UUID): ReferralEntity? {
    val referral = referralRepository.findByIdOrNull(referralId) ?: throw NotFoundException("Referral with id $referralId not found")
    val group = programmeGroupRepository.findByIdOrNull(groupId) ?: throw NotFoundException("Group with id $groupId not found")

    if (referral.mostRecentStatus().isClosed) {
      throw BusinessException("Cannot assign referral to group as referral with id ${referral.id} is in a closed state")
    }

    if (referral.currentlyAllocatedGroup() != null) {
      throw ConflictException("Referral with id ${referral.id} is already allocated to group ${group.code}")
    }

    referral.programmeGroupMemberships.add(ProgrammeGroupMembershipEntity(referral = referral, programmeGroup = group))
    return referralRepository.save(referral)
  }
}
