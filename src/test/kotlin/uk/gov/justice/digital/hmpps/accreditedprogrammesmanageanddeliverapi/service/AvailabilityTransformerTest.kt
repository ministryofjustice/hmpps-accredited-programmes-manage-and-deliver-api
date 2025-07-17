package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service

import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.SlotName
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.model.DailyAvailabilityModel
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.model.Slot
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.model.create.AvailabilityCreate
import java.time.LocalDateTime
import java.util.*

class AvailabilityTransformerTest {

  @Test
  fun `toEntity should map AvailabilityCreate to AvailabilityEntity correctly`() {
    val referralId = UUID.randomUUID()
    val now = LocalDateTime.of(2025, 7, 10, 10, 0)
    val end = LocalDateTime.of(2025, 7, 20, 10, 0)

    val availabilityCreate = AvailabilityCreate(
      referralId = referralId,
      startDate = now,
      endDate = end,
      otherDetails = "Available remotely",
      availabilities = listOf(
        DailyAvailabilityModel(
          label = "Mondays",
          slots = listOf(
            Slot(label = "daytime", value = true),
            Slot(label = "evening", value = false),
          ),
        ),
        DailyAvailabilityModel(
          label = "Wednesdays",
          slots = listOf(
            Slot(label = "daytime", value = false),
            Slot(label = "evening", value = true),
          ),
        ),
      ),
    )

    mockkStatic(SecurityContextHolder::class)
    val mockAuth = mockk<Authentication>()
    val mockContext = mockk<SecurityContext>()

    every { mockAuth.name } returns "test_user"
    every { mockContext.authentication } returns mockAuth
    every { SecurityContextHolder.getContext() } returns mockContext

    val entity = availabilityCreate.toEntity()

    assertThat(entity.referralId).isEqualTo(referralId)
    assertThat(entity.startDate).isEqualTo(now.toLocalDate())
    assertThat(entity.endDate).isEqualTo(end.toLocalDate())
    assertThat(entity.otherDetails).isEqualTo("Available remotely")
    assertThat(entity.lastModifiedBy).isEqualTo("test_user")
    assertThat(entity.lastModifiedAt).isNotNull()

    // Only 2 active slots: Monday daytime, Wednesday evening
    assertThat(entity.slots).hasSize(2)

    val mondaySlot = entity.slots.find { it.dayOfWeek.name == "MONDAY" }
    assertThat(mondaySlot).isNotNull
    assertThat(mondaySlot!!.slotName).isEqualTo(SlotName.DAYTIME)

    val wednesdaySlot = entity.slots.find { it.dayOfWeek.name == "WEDNESDAY" }
    assertThat(wednesdaySlot).isNotNull
    assertThat(wednesdaySlot!!.slotName).isEqualTo(SlotName.EVENING)

    assertThat(entity.slots.all { it.availability == entity }).isTrue()
  }
}
