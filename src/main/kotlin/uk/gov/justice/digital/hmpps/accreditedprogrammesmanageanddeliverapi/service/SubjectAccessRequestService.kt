package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.subjectAccessRequest.SubjectAccessRequestContent
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.subjectAccessRequest.SubjectAccessRequestReferral
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.subjectAccessRequest.toApi
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.AttendeeRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.AvailabilityRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.MessageHistoryRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ReferralRepository
import uk.gov.justice.hmpps.kotlin.sar.HmppsProbationSubjectAccessRequestService
import uk.gov.justice.hmpps.kotlin.sar.HmppsSubjectAccessRequestContent
import java.time.LocalDate

@Service
@Transactional
class SubjectAccessRequestService(
  private val referralRepository: ReferralRepository,
  private val messageHistoryRepository: MessageHistoryRepository,
  private val attendeeRepository: AttendeeRepository,
  private val availabilityRepository: AvailabilityRepository,
) : HmppsProbationSubjectAccessRequestService {
  private val log = LoggerFactory.getLogger(this::class.java)

  override fun getProbationContentFor(
    crn: String,
    fromDate: LocalDate?,
    toDate: LocalDate?,
  ): HmppsSubjectAccessRequestContent? {
    log.info("Retrieving subject access request content for crn: $crn")
    val referrals = getSubjectAccessRequestReferrals(crn, fromDate, toDate)
    log.info("Retrieved ${referrals.size} referrals")
    // Per the HMPPS SAR component API spec, respond with HTTP 204 (no content)
    // when we recognise the identifier type but hold no data for the subject.
    // Returning `null` here signals the starter's SAR controller to emit 204,
    // which lets the SAR collator render its single top-level "no data held"
    // section instead of our template rendering every empty sub-section.
    if (referrals.isEmpty()) {
      return null
    }
    val content = SubjectAccessRequestContent(referrals)

    return HmppsSubjectAccessRequestContent(content)
  }

  private fun getSubjectAccessRequestReferrals(
    crn: String,
    fromDate: LocalDate?,
    toDate: LocalDate?,
  ): List<SubjectAccessRequestReferral> {
    val referrals = referralRepository.findByCrn(crn)

    return referrals.filter { referral ->
      val afterFromDate = fromDate?.let { referral.createdAt.isAfter(it.atStartOfDay()) } ?: true
      val beforeToDate = toDate?.let { referral.createdAt.isBefore(it.plusDays(1).atStartOfDay()) } ?: true
      afterFromDate && beforeToDate
    }.map { referralEntity ->
      val messageHistoryEntities = messageHistoryRepository.findByReferral(referralEntity)
      val attendeeEntities = attendeeRepository.findByReferral(referralEntity)
      val availabilityEntity = referralEntity.id?.let { availabilityRepository.findByReferralId(it) }
      referralEntity.toApi(messageHistoryEntities, attendeeEntities, availabilityEntity)
    }.toList()
  }
}
