package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.subjectAccessRequest

import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.SessionFacilitatorEntity

data class SubjectAccessRequestSessionFacilitator(
  val facilitator: SubjectAccessRequestFacilitator,
)

fun SessionFacilitatorEntity.toApi() = SubjectAccessRequestSessionFacilitator(
  facilitator = id.facilitator.toApi(),
)
