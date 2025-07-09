package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.AvailabilityEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.model.DailyAvailabilityModel
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.model.SlotModel
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.AvailabilityRepository
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

class AvailabilityServiceTest {

  private val availabilityRepository = mockk<AvailabilityRepository>()
  private val defaultAvailabilityConfigService = mockk<DefaultAvailabilityConfigService>()
  private lateinit var availabilityService: AvailabilityService

  @BeforeEach
  fun setup() {
    availabilityService = AvailabilityService(availabilityRepository, defaultAvailabilityConfigService)
  }

  @Test
  fun `getAvailableSlots should return AvailabilityModel with entity and default availability`() {
    // Given
    val referralId = UUID.randomUUID()
    val availabilityEntity = AvailabilityEntity(
      id = UUID.randomUUID(),
      referralId = referralId,
      startDate = LocalDate.of(2025, 7, 10),
      endDate = LocalDate.of(2025, 7, 20),
      otherDetails = "Details here",
      lastModifiedBy = "admin",
      lastModifiedAt = LocalDateTime.of(2025, 7, 9, 10, 0),
    )

    val defaultAvailability = DayOfWeek.entries.map { day ->
      DailyAvailabilityModel(
        label = day.toPluralLabel(),
        slots = listOf(
          SlotModel("daytime", false),
          SlotModel("evening", false),
        ),
      )
    }

    every { availabilityRepository.findByReferralId(referralId) } returns availabilityEntity
    every { defaultAvailabilityConfigService.getDefaultAvailability() } returns defaultAvailability

    // When
    val result = availabilityService.getAvailableSlots(referralId)

    // Then
    assertEquals(availabilityEntity.id, result.id)
    assertEquals(availabilityEntity.referralId, result.referralId)
    assertEquals(availabilityEntity.startDate.atStartOfDay(), result.startDate)
    assertEquals(availabilityEntity.endDate?.atStartOfDay(), result.endDate)
    assertEquals(availabilityEntity.otherDetails, result.otherDetails)
    assertEquals(availabilityEntity.lastModifiedBy, result.lastModifiedBy)
    assertEquals(availabilityEntity.lastModifiedAt, result.lastModifiedAt)
    assertEquals(defaultAvailability, result.availabilities)

    verify { availabilityRepository.findByReferralId(referralId) }
    verify { defaultAvailabilityConfigService.getDefaultAvailability() }
  }

  @Test
  fun `getAvailableSlots should return default availability when entity is null`() {
    // Given
    val referralId = UUID.randomUUID()

    val defaultAvailability = DayOfWeek.entries.map { day ->
      DailyAvailabilityModel(
        label = day.toPluralLabel(),
        slots = listOf(
          SlotModel("daytime", false),
          SlotModel("evening", false),
        ),
      )
    }

    every { availabilityRepository.findByReferralId(referralId) } returns null
    every { defaultAvailabilityConfigService.getDefaultAvailability() } returns defaultAvailability

    // When
    val result = availabilityService.getAvailableSlots(referralId)

    // Then
    assertEquals(null, result.id)
    assertEquals(null, result.referralId)
    assertEquals(null, result.startDate)
    assertEquals(null, result.endDate)
    assertEquals(null, result.otherDetails)
    assertEquals(null, result.lastModifiedBy)
    assertEquals(null, result.lastModifiedAt)
    assertEquals(defaultAvailability, result.availabilities)

    verify { availabilityRepository.findByReferralId(referralId) }
    verify { defaultAvailabilityConfigService.getDefaultAvailability() }
  }
}
