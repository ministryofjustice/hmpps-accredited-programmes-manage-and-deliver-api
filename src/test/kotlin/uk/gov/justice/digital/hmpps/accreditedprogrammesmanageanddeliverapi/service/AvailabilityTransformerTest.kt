package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.SlotName
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.model.AvailabilityOption
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.model.DailyAvailabilityModel
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.model.Slot
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.model.create.CreateAvailability
import java.time.LocalDateTime
import java.util.*

class AvailabilityTransformerTest {

  @Test
  fun `toEntity should map AvailabilityCreate to AvailabilityEntity correctly`() {
    val referralId = UUID.randomUUID()
    val now = LocalDateTime.now()
    val end = LocalDateTime.now().plusWeeks(10)

    val lastModifiedUsername = "test_user"
    val createAvailability = CreateAvailability(
      referralId = referralId,
      startDate = now,
      endDate = end,
      otherDetails = "Available remotely",
      availabilities = listOf(
        DailyAvailabilityModel(
          label = AvailabilityOption.MONDAY,
          slots = listOf(
            Slot(label = "daytime", value = true),
            Slot(label = "evening", value = false),
          ),
        ),
        DailyAvailabilityModel(
          label = AvailabilityOption.WEDNESDAY,
          slots = listOf(
            Slot(label = "daytime", value = false),
            Slot(label = "evening", value = true),
          ),
        ),
      ),
    )

    val entity = createAvailability.toEntity(lastModifiedBy = lastModifiedUsername)

    assertThat(entity.referralId).isEqualTo(referralId)
    assertThat(entity.startDate).isEqualTo(now.toLocalDate())
    assertThat(entity.endDate).isEqualTo(end.toLocalDate())
    assertThat(entity.otherDetails).isEqualTo("Available remotely")
    assertThat(entity.lastModifiedBy).isEqualTo(lastModifiedUsername)
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
