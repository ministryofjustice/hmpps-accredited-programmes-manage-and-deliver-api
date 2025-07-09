package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.model.AvailabilityModel
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.AvailabilityRepository
import java.util.UUID

@Service
class AvailabilityService(
  val availabilityRepository: AvailabilityRepository,
  val defaultAvailabilityConfigService: DefaultAvailabilityConfigService,
) {

  fun getAvailableSlots(referralId: UUID): AvailabilityModel {
    val availabilityEntity = availabilityRepository.findByReferralId(referralId)

    val defaultAvailability = defaultAvailabilityConfigService.getDefaultAvailability()

    return AvailabilityModel(
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
