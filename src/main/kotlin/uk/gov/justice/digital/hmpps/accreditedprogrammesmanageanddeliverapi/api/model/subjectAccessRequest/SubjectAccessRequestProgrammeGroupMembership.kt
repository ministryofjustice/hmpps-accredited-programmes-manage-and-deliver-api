package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.subjectAccessRequest

import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ProgrammeGroupMembershipEntity

data class SubjectAccessRequestProgrammeGroupMembership(
  val createdByUsername: String?,
  val deletedByUsername: String?,
)

fun ProgrammeGroupMembershipEntity.toApi() = SubjectAccessRequestProgrammeGroupMembership(
  createdByUsername = createdByUsername,
  deletedByUsername = deletedByUsername,
)
