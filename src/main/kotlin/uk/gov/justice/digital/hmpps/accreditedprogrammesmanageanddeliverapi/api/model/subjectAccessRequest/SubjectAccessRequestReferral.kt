package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.subjectAccessRequest

import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.AttendeeEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.MessageHistoryEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ReferralEntity
import java.time.LocalDate

data class SubjectAccessRequestReferral(
  val crn: String,
  val dateOfBirth: LocalDate?,
  val eventId: String?,
  val personName: String,
  val sentenceEndDate: LocalDate?,
  val sex: String?,
  val deliveryLocationPreference: SubjectAccessRequestDeliveryLocationPreference?,
  val programmeGroupMemberships: MutableSet<SubjectAccessRequestProgrammeGroupMembership>,
  val statusHistories: MutableList<SubjectAccessRequestReferralStatusHistory>,
  val messageHistories: MutableList<SubjectAccessRequestMessageHistory>,
  val referralLdcHistories: MutableSet<SubjectAccessRequestReferralLdcHistory>,
  val referralMotivationBackgroundAndNonAssociation: SubjectAccessRequestReferralMotivationBackgroundAndNonAssociation?,
  val referralReportingLocation: SubjectAccessRequestReferralReportingLocation?,
  val attendees: MutableList<SubjectAccessRequestAttendee>,
)

fun ReferralEntity.toApi(messageHistoryEntities: List<MessageHistoryEntity>, attendeeEntities: List<AttendeeEntity>) = SubjectAccessRequestReferral(
  crn = crn,
  dateOfBirth = dateOfBirth,
  eventId = eventId,
  personName = personName,
  sentenceEndDate = sentenceEndDate,
  sex = sex,
  deliveryLocationPreference = deliveryLocationPreferences?.toApi(),
  programmeGroupMemberships = programmeGroupMemberships.map { it.toApi() }.toMutableSet(),
  statusHistories = statusHistories.map { it.toApi() }.toMutableList(),
  messageHistories = messageHistoryEntities.map { it.toApi() }.toMutableList(),
  referralLdcHistories = referralLdcHistories.map { it.toApi() }.toMutableSet(),
  referralMotivationBackgroundAndNonAssociation = referralMotivationBackgroundAndNonAssociations?.toApi(),
  referralReportingLocation = referralReportingLocationEntity?.toApi(),
  attendees = attendeeEntities.map { it.toApi() }.toMutableList(),
)
