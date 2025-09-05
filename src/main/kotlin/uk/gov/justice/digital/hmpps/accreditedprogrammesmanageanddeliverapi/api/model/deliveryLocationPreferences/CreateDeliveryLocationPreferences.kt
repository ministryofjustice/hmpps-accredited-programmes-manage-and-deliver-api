package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.deliveryLocationPreferences

import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.DeliveryLocationPreferenceEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.OfficeEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ReferralEntity
import java.util.UUID

data class CreateDeliveryLocationPreferences(
  val preferredDeliveryLocationCode: List<String>,
  val forbiddenLocationInformationText: String,
)

fun CreateDeliveryLocationPreferences.toEntity(referral: ReferralEntity, offices: MutableSet<OfficeEntity>) = DeliveryLocationPreferenceEntity(
  id = UUID.randomUUID(),
  referral = referral,
  locationsCannotAttendText = forbiddenLocationInformationText,
  offices = offices,
)
