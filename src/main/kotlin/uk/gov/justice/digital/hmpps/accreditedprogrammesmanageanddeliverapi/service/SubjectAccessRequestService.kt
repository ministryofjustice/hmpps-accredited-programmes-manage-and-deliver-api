package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.subjectAccessRequest.SubjectAccessRequestContent
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.subjectAccessRequest.SubjectAccessRequestGroupWaitlistItemView
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.subjectAccessRequest.SubjectAccessRequestReferral
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.subjectAccessRequest.SubjectAccessRequestReferralCaseListItemView
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.subjectAccessRequest.toApi
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.AttendeeRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.AvailabilityRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.GroupWaitlistItemViewRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.MessageHistoryRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ReferralCaseListItemRepository
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
  private val groupWaitlistItemViewRepository: GroupWaitlistItemViewRepository,
  private val referralCaseListItemRepository: ReferralCaseListItemRepository,
) : HmppsProbationSubjectAccessRequestService {

  override fun getProbationContentFor(
    crn: String,
    fromDate: LocalDate?,
    toDate: LocalDate?,
  ): HmppsSubjectAccessRequestContent {
    val referrals = getSubjectAccessRequestReferrals(crn, fromDate, toDate)
    val groupWaitlistItemViews = getSubjectAccessRequestGroupWaitlistItemViews(crn)
    val referralCaseListItemViews = getSubjectAccessRequestReferralCaseListItemViews(crn)
    val content = SubjectAccessRequestContent(
      referrals,
      groupWaitlistItemViews,
      referralCaseListItemViews,
    )

    return HmppsSubjectAccessRequestContent(content)
  }

  private fun getSubjectAccessRequestReferralCaseListItemViews(crn: String): List<SubjectAccessRequestReferralCaseListItemView> {
    val referralCaseListItemViews = referralCaseListItemRepository.findByCrn(crn)

    return referralCaseListItemViews.map { it.toApi() }.toList()
  }

  private fun getSubjectAccessRequestGroupWaitlistItemViews(crn: String): List<SubjectAccessRequestGroupWaitlistItemView> {
    val groupWaitlistItemViews = groupWaitlistItemViewRepository.findByCrn(crn)

    return groupWaitlistItemViews.map { it.toApi() }.toList()
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
