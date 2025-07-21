package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.AvailabilityEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.model.DailyAvailabilityModel
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.model.Slot
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.AvailabilityRepository
import java.time.DayOfWeek
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
      startDate = LocalDateTime.now().toLocalDate(),
      endDate = LocalDateTime.now().plusDays(20).toLocalDate(),
      otherDetails = "Details here",
      lastModifiedBy = "admin",
      lastModifiedAt = LocalDateTime.of(2025, 7, 9, 10, 0),
    )

    val defaultAvailability = DayOfWeek.entries.map { day ->
      DailyAvailabilityModel(
        label = day.toAvailabilityOptions(),
        slots = listOf(
          Slot("daytime", false),
          Slot("evening", false),
        ),
      )
    }

    every { availabilityRepository.findByReferralId(referralId) } returns availabilityEntity
    every { defaultAvailabilityConfigService.getDefaultAvailability() } returns defaultAvailability

    // When
    val result = availabilityService.getAvailableSlots(referralId)

    // Then
    assertThat(result.referralId).isEqualTo(availabilityEntity.referralId)
    assertThat(result.startDate).isEqualTo(availabilityEntity.startDate.atStartOfDay())
    assertThat(result.endDate).isEqualTo(availabilityEntity.endDate?.atStartOfDay())
    assertThat(result.otherDetails).isEqualTo(availabilityEntity.otherDetails)
    assertThat(result.lastModifiedBy).isEqualTo(availabilityEntity.lastModifiedBy)
    assertThat(result.lastModifiedAt).isEqualTo(availabilityEntity.lastModifiedAt)
    assertThat(result.availabilities).isEqualTo(defaultAvailability)

    verify { availabilityRepository.findByReferralId(referralId) }
    verify { defaultAvailabilityConfigService.getDefaultAvailability() }
  }

  @Test
  fun `getAvailableSlots should return default availability when entity is null`() {
    // Given
    val referralId = UUID.randomUUID()

    val defaultAvailability = DayOfWeek.entries.map { day ->
      DailyAvailabilityModel(
        label = day.toAvailabilityOptions(),
        slots = listOf(
          Slot("daytime", false),
          Slot("evening", false),
        ),
      )
    }

    every { availabilityRepository.findByReferralId(referralId) } returns null
    every { defaultAvailabilityConfigService.getDefaultAvailability() } returns defaultAvailability

    // When
    val result = availabilityService.getAvailableSlots(referralId)

    // Then
    assertThat(result.id).isNull()
    assertThat(result.referralId).isNull()
    assertThat(result.startDate).isNull()
    assertThat(result.endDate).isNull()
    assertThat(result.otherDetails).isNull()
    assertThat(result.lastModifiedBy).isNull()
    assertThat(result.lastModifiedAt).isNull()
    assertThat(result.availabilities).isEqualTo(defaultAvailability)

    verify { availabilityRepository.findByReferralId(referralId) }
    verify { defaultAvailabilityConfigService.getDefaultAvailability() }
  }
}
