package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common.exception.NotFoundException
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ReferralEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.type.SessionRole
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.event.DomainEventPublisher
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.event.HmppsDomainEventTypes
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.event.model.DomainEventsMessage
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.event.model.PersonReference
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ModuleSessionTemplateRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ProgrammeGroupMembershipRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ReferralStatusDescriptionRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.SessionAttendanceRepository
import java.time.ZonedDateTime
import java.util.UUID

@Service
@Transactional
class AttendanceService(
  @Value($$"${services.manage-and-deliver-api.base-url}") private val madBaseUrl: String,
  private val referralService: ReferralService,
  private val referralStatusDescriptionRepository: ReferralStatusDescriptionRepository,
  private val programmeGroupMembershipRepository: ProgrammeGroupMembershipRepository,
  private val moduleSessionTemplateRepository: ModuleSessionTemplateRepository,
  private val sessionAttendanceRepository: SessionAttendanceRepository,
  private val domainEventPublisher: DomainEventPublisher,
) {

  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }

  // Referral -> attendance status
  // Referral -> activeMembership -> group -> sessions -> post-programme review session -> session for referralId

  fun checkProgrammeCompleteStatusForReferralAndPublishEvent(referralId: UUID) {
    // Programme complete status
    // Need to have completed post programme review meeting and attended and complied
    // Referral status is programme complete

    val referral = referralService.getReferralById(referralId)

    val latestStatus = referralStatusDescriptionRepository.findMostRecentStatusByReferralId(referralId)
      ?: throw NotFoundException("Cannot find latest status history entry for referral with id: $referralId")
    val activeGroupMembership = programmeGroupMembershipRepository.findCurrentGroupByReferralId(referralId)
      ?: throw NotFoundException("Cannot find active group membership for referral with id: $referralId")

    val postProgrammeReviewTemplate = moduleSessionTemplateRepository.findBySessionRole(SessionRole.POST_PROGRAMME)
      ?: throw NotFoundException("Cannot find Post programme review session template")

    // TODO might have to just find one here
    val session = sessionAttendanceRepository.findBySessionIdAndGroupMembershipReferralId(postProgrammeReviewTemplate)

    if (latestStatus.description == "Programme complete") {
      publishProgrammeCompleteEvent(referral)
    }
  }

  private fun publishProgrammeCompleteEvent(referral: ReferralEntity) {
    val hmppsDomainEvent = DomainEventsMessage(
      eventType = HmppsDomainEventTypes.ACP_COMMUNITY_REFERRAL_CREATED.value,
      version = 1,
      detailUrl = "$madBaseUrl/referral/${referral.id}/programme-complete-details",
      occurredAt = ZonedDateTime.now(),
      description = "A referral in Accredited Programme community has been marked as complete.",
      additionalInformation = mutableMapOf(),
      personReference = PersonReference.fromCrn(referral.crn),
    )
    log.info("Publishing ${HmppsDomainEventTypes.ACP_COMMUNITY_PROGRAMME_COMPLETE.value} event for referralId: ${referral.id}")
    domainEventPublisher.publish(hmppsDomainEvent)
  }
}
