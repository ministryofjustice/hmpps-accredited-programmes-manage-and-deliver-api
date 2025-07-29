package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service

import jakarta.transaction.Transactional
import org.slf4j.LoggerFactory
import org.springframework.data.repository.findByIdOrNull
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common.exception.NotFoundException
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.AvailabilitySlotEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.SlotName
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.model.Availability
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.model.create.CreateAvailability
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.model.toDayOfWeek
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.model.update.UpdateAvailability
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.AvailabilityRepository
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeParseException
import java.util.UUID

@Service
class AvailabilityService(
  val availabilityRepository: AvailabilityRepository,
  val defaultAvailabilityConfigService: DefaultAvailabilityConfigService,
) {

  private val log = LoggerFactory.getLogger(this::class.java)

  fun getAvailability(referralId: UUID): Availability {
    val availabilityEntity = availabilityRepository.findByReferralId(referralId)

    if (availabilityEntity == null) {
      log.info("No availability found for referralId $referralId using default availability config")
      return Availability(
        availabilities = defaultAvailabilityConfigService.getDefaultAvailability(),
      )
    }

    return availabilityEntity.toModel()
  }

  /**
   *  This method returns a pair, so that the controller can display if there is a duplicate availability.
   *  true - would mean that there is a duplicate and the controller would send a 409 (conflict)
   *  false - availability created
   */
  fun createAvailability(createAvailability: CreateAvailability): Pair<Availability, Boolean> {
    val existingAvailability = availabilityRepository.findByReferralId(createAvailability.referralId)
    if (existingAvailability != null) {
      log.info("Availability already exists for referralId ${createAvailability.referralId}")
      return Pair(existingAvailability.toModel(), true)
    }

    val availabilityEntity = createAvailability.toEntity(getAuthenticatedReferrerUser())
    val savedAvailabilityEntity = availabilityRepository.save(availabilityEntity)
    return Pair(savedAvailabilityEntity.toModel(), false)
  }

  private fun getAuthenticatedReferrerUser() = SecurityContextHolder.getContext().authentication?.name
    ?: throw SecurityException("Authentication information not found")

  @Transactional
  fun updateAvailability(updateAvailability: UpdateAvailability): Availability {
    val availabilityEntity = availabilityRepository.findByIdOrNull(updateAvailability.availabilityId)
      ?: throw NotFoundException("No availability with id ${updateAvailability.availabilityId}")

    availabilityEntity.referralId = updateAvailability.referralId
    availabilityEntity.startDate = updateAvailability.startDate?.toLocalDate() ?: LocalDate.now()
    availabilityEntity.endDate = updateAvailability.endDate?.toLocalDate()
    availabilityEntity.otherDetails = updateAvailability.otherDetails
    availabilityEntity.lastModifiedAt = LocalDateTime.now()
    availabilityEntity.lastModifiedBy = getAuthenticatedReferrerUser()

    availabilityEntity.slots.clear()

    updateAvailability.availabilities
      .filter { daily -> daily.slots.any { it.value } }
      .forEach { daily ->
        val dayOfWeek = daily.label.toDayOfWeek()
        daily.slots.filter { it.value }.forEach { slot ->
          val availabilitySlotEntity = AvailabilitySlotEntity(
            dayOfWeek = dayOfWeek,
            slotName = SlotName.valueOf(slot.label.uppercase()),
            availability = availabilityEntity,
          )
          availabilityEntity.slots.add(availabilitySlotEntity)
        }
      }

    val updateAvailability = availabilityRepository.save(availabilityEntity)
    return updateAvailability.toModel()
  }
}

fun String.toLocalDate(): LocalDate {
  try {
    return LocalDate.parse(this)
  } catch (e: DateTimeParseException) {
    throw IllegalArgumentException("Invalid date: $this")
  }
}
