package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.subjectAccessRequest

import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.AccreditedProgrammeTemplateEntity

data class SubjectAccessRequestAccreditedProgrammeTemplate(
  val name: String,
)

fun AccreditedProgrammeTemplateEntity.toApi() = SubjectAccessRequestAccreditedProgrammeTemplate(
  name = name,
)
