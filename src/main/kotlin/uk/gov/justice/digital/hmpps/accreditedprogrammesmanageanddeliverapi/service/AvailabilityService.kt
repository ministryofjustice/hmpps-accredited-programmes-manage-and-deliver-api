package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.model.Availability
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.AvailabilityRepository
import java.util.UUID

@Service
class AvailabilityService(
  val availabilityRepository: AvailabilityRepository,
  val defaultAvailabilityConfigService: DefaultAvailabilityConfigService,
) {

  fun getAvailableSlots(referralId: UUID): Availability {
    val availabilityEntity = availabilityRepository.findByReferralId(referralId)

    val defaultAvailability = defaultAvailabilityConfigService.getDefaultAvailability()

    return Availability(
      id = availabilityEntity?.id,
      referralId = availabilityEntity?.referralId,
      startDate = availabilityEntity?.startDate?.atStartOfDay(), // If you need LocalDateTime
      endDate = availabilityEntity?.endDate?.atStartOfDay(),
      otherDetails = availabilityEntity?.otherDetails,
      lastModifiedBy = availabilityEntity?.lastModifiedBy,
      lastModifiedAt = availabilityEntity?.lastModifiedAt,
      availabilities = defaultAvailability,
    )
  }
}
