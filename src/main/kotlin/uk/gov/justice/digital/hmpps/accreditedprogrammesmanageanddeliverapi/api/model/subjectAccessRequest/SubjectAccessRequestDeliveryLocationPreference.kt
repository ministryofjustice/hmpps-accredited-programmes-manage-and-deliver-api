package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.subjectAccessRequest

import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.DeliveryLocationPreferenceEntity

data class SubjectAccessRequestDeliveryLocationPreference(
  val createdBy: String?,
  var locationCannotAttendText: String?,
)

fun DeliveryLocationPreferenceEntity.toApi() = SubjectAccessRequestDeliveryLocationPreference(
  createdBy = createdBy,
  locationCannotAttendText = locationsCannotAttendText,
)
