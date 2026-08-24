package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.subjectAccessRequest

import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.AttendeeEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.AvailabilityEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.MessageHistoryEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ProgrammeGroupMembershipEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ReferralCohortHistoryEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ReferralEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ReferralLdcHistoryEntity
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

data class SubjectAccessRequestReferral(
  val id: UUID?,
  val sentenceEndDate: LocalDate?,
  val sex: String?,
  val createdAt: LocalDateTime,
  val interventionName: String?,
  val interventionType: String,
  val setting: String,
  val sourcedFrom: String?,
  val deliveryLocationPreference: SubjectAccessRequestDeliveryLocationPreference?,
  val programmeGroupMemberships: MutableList<SubjectAccessRequestProgrammeGroupMembership>,
  val statusHistories: MutableList<SubjectAccessRequestReferralStatusHistory>,
  val messageHistories: MutableList<SubjectAccessRequestMessageHistory>,
  val referralLdcHistories: MutableList<SubjectAccessRequestReferralLdcHistory>,
  val referralCohortHistories: MutableList<SubjectAccessRequestReferralCohortHistory>,
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
  sentenceEndDate = sentenceEndDate,
  sex = sex,
  createdAt = createdAt,
  interventionName = interventionName,
  interventionType = interventionType.displayName,
  setting = setting.displayName,
  sourcedFrom = sourcedFrom?.displayName,
  deliveryLocationPreference = deliveryLocationPreferences?.toApi(),
  // ASC by createdAt: SAR template numbers "Group Allocation 1, 2, ..." via @index,
  // so chronological order reads naturally in the report. `.thenBy { it.id }` gives a
  // deterministic tie-break when two memberships share a createdAt (JPA Sets have no order).
  programmeGroupMemberships = programmeGroupMemberships
    .sortedWith(compareBy<ProgrammeGroupMembershipEntity> { it.createdAt }.thenBy { it.id })
    .map { it.toApi() }
    .toMutableList(),
  statusHistories = statusHistories.map { it.toApi() }.toMutableList(),
  messageHistories = messageHistoryEntities.map { it.toApi() }.toMutableList(),
  // DESC by createdAt: history sections show most-recent-first, matching cohort history.
  // `.thenBy { it.id }` is the deterministic tie-break (see tracker Correction #8).
  referralLdcHistories = referralLdcHistories
    .sortedWith(compareByDescending<ReferralLdcHistoryEntity> { it.createdAt }.thenBy { it.id })
    .map { it.toApi() }
    .toMutableList(),
  // DESC by createdAt: most-recent cohort first. `.thenBy { it.id }` is the deterministic
  // tie-break added on Branch 1 (commit bab0cb03) after a Hibernate-version-dependent flake.
  referralCohortHistories = referralCohortHistories
    .sortedWith(compareByDescending<ReferralCohortHistoryEntity> { it.createdAt }.thenBy { it.id })
    .map { it.toApi() }
    .toMutableList(),
  referralMotivationBackgroundAndNonAssociation = referralMotivationBackgroundAndNonAssociations?.toApi(),
  referralReportingLocation = referralReportingLocation?.toApi(),
  attendees = attendeeEntities.map { it.toApi() }.toMutableList(),
  availability = availabilityEntity?.toApi(),
)
