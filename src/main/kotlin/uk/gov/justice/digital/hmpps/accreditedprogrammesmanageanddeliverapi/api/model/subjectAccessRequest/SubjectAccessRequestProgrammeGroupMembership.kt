package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.subjectAccessRequest

import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ProgrammeGroupMembershipEntity
import java.time.LocalDateTime

data class SubjectAccessRequestProgrammeGroupMembership(
  val createdByUsername: String?,
  val createdAt: LocalDateTime,
  val deletedByUsername: String?,
  val programmeGroup: SubjectAccessRequestProgrammeGroup,
  val attendances: List<SubjectAccessRequestSessionAttendance>,
)

fun ProgrammeGroupMembershipEntity.toApi() = SubjectAccessRequestProgrammeGroupMembership(
  createdByUsername = createdByUsername,
  createdAt = createdAt,
  deletedByUsername = deletedByUsername,
  programmeGroup = programmeGroup.toApi(),
  attendances = attendances.map { it.toApi() },
)
