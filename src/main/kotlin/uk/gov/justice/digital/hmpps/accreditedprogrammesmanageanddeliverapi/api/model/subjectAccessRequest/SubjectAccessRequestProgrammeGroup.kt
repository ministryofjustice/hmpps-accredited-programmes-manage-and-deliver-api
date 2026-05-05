package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.subjectAccessRequest

import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ProgrammeGroupEntity
import java.util.UUID

data class SubjectAccessRequestProgrammeGroup(
  val id: UUID?,
  val accreditedProgrammeTemplate: SubjectAccessRequestAccreditedProgrammeTemplate?,
)

fun ProgrammeGroupEntity.toApi() = SubjectAccessRequestProgrammeGroup(
  id = id,
  accreditedProgrammeTemplate = accreditedProgrammeTemplate?.toApi(),
)
