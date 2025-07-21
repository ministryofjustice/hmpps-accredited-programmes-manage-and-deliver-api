package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.SlotName
import java.time.DayOfWeek

class DefaultAvailabilityConfigServiceTest {

  private val service = DefaultAvailabilityConfigService()

  @Test
  fun `getDefaultAvailability should return 7 days with all slots set to false`() {
    // Act
    val result = service.getDefaultAvailability()

    assertThat(result).hasSize(7)

    val expectedDayLabels = DayOfWeek.entries.map { it.toAvailabilityOptions() }
    val actualDayLabels = result.map { it.label }
    assertThat(actualDayLabels).containsExactlyElementsOf(expectedDayLabels)

    result.forEach { dailyAvailability ->
      val slotLabels = dailyAvailability.slots.map { it.label }
      val expectedSlotLabels = SlotName.entries.map { it.displayName }

      assertThat(slotLabels).containsExactlyElementsOf(expectedSlotLabels)

      assertThat(dailyAvailability.slots.all { it.value == false }).isTrue()
    }
  }
}
