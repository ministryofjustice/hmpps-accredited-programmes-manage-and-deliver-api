package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.subjectAccessRequest

import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ProgrammeGroupEntity

data class SubjectAccessRequestProgrammeGroup(
  val treatmentManager: SubjectAccessRequestFacilitator?,
  val updatedByUsername: String?,
)

fun ProgrammeGroupEntity.toApi() = SubjectAccessRequestProgrammeGroup(
  treatmentManager = treatmentManager?.toApi(),
  updatedByUsername = updatedByUsername,
)
