package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.AvailabilityEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.model.Availability
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.model.create.AvailabilityCreate
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.AvailabilityRepository
import java.util.UUID

@Service
class AvailabilityService(
  val availabilityRepository: AvailabilityRepository,
  val defaultAvailabilityConfigService: DefaultAvailabilityConfigService,
) {

  private val log = LoggerFactory.getLogger(this::class.java)

  fun getAvailableSlots(referralId: UUID): Availability {
    val availabilityEntity = availabilityRepository.findByReferralId(referralId)

    if (availabilityEntity == null) {
      log.info("No availability found with referralId $referralId using default availability config")
    }

    return Availability(
      id = availabilityEntity?.id,
      referralId = availabilityEntity?.referralId,
      startDate = availabilityEntity?.startDate?.atStartOfDay(),
      endDate = availabilityEntity?.endDate?.atStartOfDay(),
      otherDetails = availabilityEntity?.otherDetails,
      lastModifiedBy = availabilityEntity?.lastModifiedBy,
      lastModifiedAt = availabilityEntity?.lastModifiedAt,
      availabilities = defaultAvailabilityConfigService.getDefaultAvailability(),
    )
  }

  fun createAvailability(availability: AvailabilityCreate): AvailabilityEntity {
    val availabilityEntity = availability.toEntity()
    return availabilityRepository.save(availabilityEntity)
  }
}
