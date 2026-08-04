package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common.exception.NotFoundException
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.factory.AvailabilityEntityFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.CreateAvailabilityFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.ReferralEntityFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.UpdateAvailabilityFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.model.DailyAvailabilityModel
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.model.Slot
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.AvailabilityRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.utils.TelemetryUtils
import java.time.DayOfWeek
import java.util.Optional
import java.util.UUID

class AvailabilityServiceTest {
  private val availabilityRepository = mockk<AvailabilityRepository>()
  private val defaultAvailabilityConfigService = mockk<DefaultAvailabilityConfigService>()
  private var referralService = mockk<ReferralService>()
  private var telemetryUtils = mockk<TelemetryUtils>()
  private lateinit var availabilityService: AvailabilityService

  @BeforeEach
  fun setup() {
    availabilityService = AvailabilityService(
      availabilityRepository,
      defaultAvailabilityConfigService,
      referralService,
      telemetryUtils,
    )
  }

  @Test
  fun `createAvailability should return created availability when it does not exist`() {
    // Given
    val referralEntity = ReferralEntityFactory().withId(UUID.randomUUID()).produce()
    val createAvailability = CreateAvailabilityFactory().withReferralId(referralEntity.id!!).produce()

    val securityContext = mockk<SecurityContext>()
    val authentication = mockk<Authentication>()
    every { securityContext.authentication } returns authentication
    every { authentication.name } returns "test-user"
    SecurityContextHolder.setContext(securityContext)

    val savedEntity = AvailabilityEntityFactory()
      .withReferral(referralEntity)
      .withStartDate(createAvailability.startDate!!.toLocalDate())
      .withEndDate(createAvailability.endDate?.toLocalDate())
      .withOtherDetails(createAvailability.otherDetails)
      .produce()

    every { referralService.getReferralById(referralEntity.id!!) } returns referralEntity
    every { availabilityRepository.save(any()) } returns savedEntity
    every { telemetryUtils.logToAppInsights(any(), any(), any(), any(), any()) } returns Unit

    // When
    val (result, isDuplicate) = availabilityService.createAvailability(createAvailability)

    // Then
    assertThat(isDuplicate).isFalse()
    assertThat(result.referralId).isEqualTo(referralEntity.id)
    assertThat(result.startDate).isEqualTo(createAvailability.startDate)
    assertThat(result.endDate).isEqualTo(createAvailability.endDate)
    assertThat(result.otherDetails).isEqualTo(createAvailability.otherDetails)

    verify { referralService.getReferralById(referralEntity.id!!) }
    verify { availabilityRepository.save(any()) }
    verify {
      telemetryUtils.logToAppInsights(
        referralEntity = referralEntity,
        eventName = "Availability.create-availability.success",
        activityType = "SET_AVAILABILITY",
        toReferralStatusId = null,
        appliedBy = null,
      )
    }
  }

  @Test
  fun `createAvailability should return existing availability when it already exists`() {
    // Given
    val referralEntity = ReferralEntityFactory().withId(UUID.randomUUID()).produce()
    val existingAvailability = AvailabilityEntityFactory().withReferral(referralEntity).produce()
    referralEntity.availability = existingAvailability

    val createAvailability = CreateAvailabilityFactory().withReferralId(referralEntity.id!!).produce()

    every { referralService.getReferralById(referralEntity.id!!) } returns referralEntity

    // When
    val (result, isDuplicate) = availabilityService.createAvailability(createAvailability)

    // Then
    assertThat(isDuplicate).isTrue()
    assertThat(result.referralId).isEqualTo(referralEntity.id)
    assertThat(result.otherDetails).isEqualTo(existingAvailability.otherDetails)

    verify { referralService.getReferralById(referralEntity.id!!) }
    verify(exactly = 0) { availabilityRepository.save(any()) }
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

  @Test
  fun `updateAvailability should update and return availability when it exists`() {
    // Given
    val referralEntity = ReferralEntityFactory().withId(UUID.randomUUID()).produce()
    val existingAvailability = AvailabilityEntityFactory().withReferral(referralEntity).produce()
    val updateAvailability = UpdateAvailabilityFactory()
      .withAvailabilityId(existingAvailability.id!!)
      .withReferralId(referralEntity.id!!)
      .withStartDate("2025-08-01")
      .withEndDate("2025-08-31")
      .withOtherDetails("Updated details")
      .produce()

    val securityContext = mockk<SecurityContext>()
    val authentication = mockk<Authentication>()
    every { securityContext.authentication } returns authentication
    every { authentication.name } returns "updater-user"
    SecurityContextHolder.setContext(securityContext)

    every { referralService.getReferralById(referralEntity.id!!) } returns referralEntity
    every { availabilityRepository.findById(existingAvailability.id!!) } returns Optional.of(existingAvailability)
    every { availabilityRepository.save(any()) } returns existingAvailability
    every { telemetryUtils.logToAppInsights(any(), any(), any(), any(), any()) } returns Unit

    // When
    val result = availabilityService.updateAvailability(updateAvailability)

    // Then
    assertThat(result.referralId).isEqualTo(referralEntity.id)
    assertThat(result.startDate).isEqualTo("2025-08-01")
    assertThat(result.endDate).isEqualTo("2025-08-31")
    assertThat(result.otherDetails).isEqualTo("Updated details")
    assertThat(result.lastModifiedBy).isEqualTo("updater-user")

    verify { referralService.getReferralById(referralEntity.id!!) }
    verify { availabilityRepository.findById(existingAvailability.id!!) }
    verify { availabilityRepository.save(any()) }
    verify {
      telemetryUtils.logToAppInsights(
        referralEntity = referralEntity,
        eventName = "Availability.update-availability.success",
        activityType = "UPDATE_AVAILABILITY",
        toReferralStatusId = null,
        appliedBy = null,
      )
    }
  }

  @Test
  fun `updateAvailability should throw NotFoundException when availability does not exist`() {
    // Given
    val referralEntity = ReferralEntityFactory().withId(UUID.randomUUID()).produce()
    val updateAvailability = UpdateAvailabilityFactory()
      .withReferralId(referralEntity.id!!)
      .produce()

    every { referralService.getReferralById(referralEntity.id!!) } returns referralEntity
    every { availabilityRepository.findById(updateAvailability.availabilityId) } returns Optional.empty()

    // When & Then
    val exception = assertThrows<NotFoundException> {
      availabilityService.updateAvailability(updateAvailability)
    }
    assertThat(exception.message).isEqualTo("No availability with id ${updateAvailability.availabilityId}")

    verify { referralService.getReferralById(referralEntity.id!!) }
    verify { availabilityRepository.findById(updateAvailability.availabilityId) }
    verify(exactly = 0) { availabilityRepository.save(any()) }
  }
}
