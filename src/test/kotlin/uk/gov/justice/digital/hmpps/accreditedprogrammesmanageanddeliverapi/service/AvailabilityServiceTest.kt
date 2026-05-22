package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.factory.AvailabilityEntityFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.ReferralEntityFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.model.DailyAvailabilityModel
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.model.Slot
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.AvailabilityRepository
import java.time.DayOfWeek
import java.util.UUID

class AvailabilityServiceTest {

  private val availabilityRepository = mockk<AvailabilityRepository>()
  private val defaultAvailabilityConfigService = mockk<DefaultAvailabilityConfigService>()
  private lateinit var availabilityService: AvailabilityService
  private var referralService = mockk<ReferralService>()

  @BeforeEach
  fun setup() {
    availabilityService = AvailabilityService(availabilityRepository, defaultAvailabilityConfigService, referralService)
  }

  @Test
  fun `getAvailability should return default availability when availability does not exist for a referral`() {
    // Given
    val referralEntity = ReferralEntityFactory().withId(UUID.randomUUID()).produce()
    val availabilityEntity = AvailabilityEntityFactory().withReferral(referralEntity).produce()

    val defaultAvailability = DayOfWeek.entries.map { day ->
      DailyAvailabilityModel(
        label = day.toAvailabilityOptions(),
        slots = listOf(
          Slot("daytime", false),
          Slot("evening", false),
        ),
      )
    }

    every { availabilityRepository.findByReferralId(availabilityEntity.referral.id!!) } returns availabilityEntity
    every { defaultAvailabilityConfigService.getDefaultAvailability() } returns defaultAvailability

    // When
    val result = availabilityService.getAvailability(availabilityEntity.referral.id!!)

    // Then
    assertThat(result.referralId).isEqualTo(availabilityEntity.referral.id)
    assertThat(result.startDate.toString()).isEqualTo(availabilityEntity.startDate.toString())
    assertThat(result.endDate).isEqualTo(availabilityEntity.endDate?.toString())
    assertThat(result.otherDetails).isEqualTo(availabilityEntity.otherDetails)
    assertThat(result.lastModifiedBy).isEqualTo(availabilityEntity.lastModifiedBy)
    assertThat(result.lastModifiedAt).isEqualTo(availabilityEntity.lastModifiedAt.toString())
    assertThat(result.availabilities).isEqualTo(defaultAvailability)

    verify { availabilityRepository.findByReferralId(availabilityEntity.referral.id!!) }
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
    val result = availabilityService.getAvailability(referralId)

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
