package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.subjectAccessRequest

import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.AttendeeEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.AvailabilityEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.MessageHistoryEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ReferralEntity
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

data class SubjectAccessRequestReferral(
  val id: UUID?,
  val crn: String,
  val dateOfBirth: LocalDate?,
  val eventId: String?,
  val personName: String,
  val sentenceEndDate: LocalDate?,
  val sex: String?,
  val cohort: String,
  val createdAt: LocalDateTime,
  val interventionName: String?,
  val interventionType: String,
  val setting: String,
  val sourcedFrom: String?,
  val deliveryLocationPreference: SubjectAccessRequestDeliveryLocationPreference?,
  val programmeGroupMemberships: MutableSet<SubjectAccessRequestProgrammeGroupMembership>,
  val statusHistories: MutableList<SubjectAccessRequestReferralStatusHistory>,
  val messageHistories: MutableList<SubjectAccessRequestMessageHistory>,
  val referralLdcHistories: MutableSet<SubjectAccessRequestReferralLdcHistory>,
  val referralMotivationBackgroundAndNonAssociation: SubjectAccessRequestReferralMotivationBackgroundAndNonAssociation?,
  val referralReportingLocation: SubjectAccessRequestReferralReportingLocation?,
  val attendees: MutableList<SubjectAccessRequestAttendee>,
  val availability: SubjectAccessRequestAvailability?,
)

fun ReferralEntity.toApi(
  messageHistoryEntities: List<MessageHistoryEntity>,
  attendeeEntities: List<AttendeeEntity>,
  availabilityEntity: AvailabilityEntity?,
) = SubjectAccessRequestReferral(
  id = id,
  crn = crn,
  dateOfBirth = dateOfBirth,
  eventId = eventId,
  personName = personName,
  sentenceEndDate = sentenceEndDate,
  sex = sex,
  cohort = cohort.name,
  createdAt = createdAt,
  interventionName = interventionName,
  interventionType = interventionType.name,
  setting = setting.name,
  sourcedFrom = sourcedFrom?.name,
  deliveryLocationPreference = deliveryLocationPreferences?. toApi(),
  programmeGroupMemberships = programmeGroupMemberships.map { it.toApi() }.toMutableSet(),
  statusHistories = statusHistories.map { it.toApi() }.toMutableList(),
  messageHistories = messageHistoryEntities.map { it.toApi() }.toMutableList(),
  referralLdcHistories = referralLdcHistories.map { it.toApi() }.toMutableSet(),
  referralMotivationBackgroundAndNonAssociation = referralMotivationBackgroundAndNonAssociations?.toApi(),
  referralReportingLocation = referralReportingLocationEntity?.toApi(),
  attendees = attendeeEntities.map { it.toApi() }.toMutableList(),
  availability = availabilityEntity?.toApi(),
)
