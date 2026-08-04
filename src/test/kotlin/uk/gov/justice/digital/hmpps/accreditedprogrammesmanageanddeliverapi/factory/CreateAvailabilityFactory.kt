package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory

import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.model.AvailabilityOption
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.model.DailyAvailabilityModel
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.model.Slot
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.model.create.CreateAvailability
import java.util.UUID

class CreateAvailabilityFactory {
  private var referralId: UUID = UUID.randomUUID()
  private var startDate: String? = "2025-07-10"
  private var endDate: String? = "2025-07-20"
  private var otherDetails: String? = "Available for remote sessions"
  private var availabilities: List<DailyAvailabilityModel> = AvailabilityOption.entries.map { option ->
    DailyAvailabilityModel(
      label = option,
      slots = listOf(
        Slot("daytime", false),
        Slot("evening", false),
      ),
    )
  }

  fun withReferralId(referralId: UUID) = apply { this.referralId = referralId }
  fun withStartDate(startDate: String?) = apply { this.startDate = startDate }
  fun withEndDate(endDate: String?) = apply { this.endDate = endDate }
  fun withOtherDetails(otherDetails: String?) = apply { this.otherDetails = otherDetails }
  fun withAvailabilities(availabilities: List<DailyAvailabilityModel>) = apply { this.availabilities = availabilities }

  fun produce() = CreateAvailability(
    referralId = this.referralId,
    startDate = this.startDate,
    endDate = this.endDate,
    otherDetails = this.otherDetails,
    availabilities = this.availabilities,
  )
}
