package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.type.SessionAttendanceNDeliusCode
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.SessionAttendanceEntityFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.SessionAttendanceNDeliusOutcomeEntityFactory
import java.time.LocalDateTime

/**
 * Pure unit tests for the [latestByCreatedAt] extension helper.
 *
 * The helper's contract is safety-critical for APG-2580 follow-up 4: it replaces UUID-lottery
 * `.thenBy { it.id }` tiebreaks with deterministic natural-attribute tiebreaks. These tests
 * pin the tiebreak contract so a future edit that reverts to `it.id` — or changes the
 * tiebreak key order — will fail here rather than silently drifting on tied timestamps.
 */
class SessionAttendanceEntityTest {

  private val baseTime: LocalDateTime = LocalDateTime.of(2026, 8, 25, 10, 0, 0)

  @Test
  fun `latestByCreatedAt returns null for an empty iterable`() {
    assertThat(emptyList<SessionAttendanceEntity>().latestByCreatedAt()).isNull()
  }

  @Test
  fun `latestByCreatedAt picks the row with the highest createdAt`() {
    val earlier = SessionAttendanceEntityFactory().withCreatedAt(baseTime).produce()
    val later = SessionAttendanceEntityFactory().withCreatedAt(baseTime.plusMinutes(1)).produce()

    val winner = listOf(earlier, later).latestByCreatedAt()

    assertThat(winner).isSameAs(later)
  }

  @Test
  fun `latestByCreatedAt breaks createdAt ties on alphabetically-highest createdBy (never on entity id)`() {
    val aliceRow = SessionAttendanceEntityFactory()
      .withCreatedAt(baseTime)
      .withCreatedBy("ALICE")
      .produce()
    val zoeRow = SessionAttendanceEntityFactory()
      .withCreatedAt(baseTime)
      .withCreatedBy("ZOE")
      .produce()

    val winner = listOf(aliceRow, zoeRow).latestByCreatedAt()

    assertThat(winner).isSameAs(zoeRow)
    assertThat(winner?.createdBy).isEqualTo("ZOE")
  }

  @Test
  fun `latestByCreatedAt breaks createdAt-plus-createdBy ties on alphabetically-highest outcomeType code name`() {
    val attcOutcome = SessionAttendanceNDeliusOutcomeEntityFactory()
      .withCode(SessionAttendanceNDeliusCode.ATTC)
      .produce()
    val uaabOutcome = SessionAttendanceNDeliusOutcomeEntityFactory()
      .withCode(SessionAttendanceNDeliusCode.UAAB)
      .produce()

    val attcRow = SessionAttendanceEntityFactory()
      .withCreatedAt(baseTime)
      .withCreatedBy("SYSTEM")
      .withOutcomeType(attcOutcome)
      .produce()
    val uaabRow = SessionAttendanceEntityFactory()
      .withCreatedAt(baseTime)
      .withCreatedBy("SYSTEM")
      .withOutcomeType(uaabOutcome)
      .produce()

    val winner = listOf(attcRow, uaabRow).latestByCreatedAt()

    assertThat(winner).isSameAs(uaabRow)
    assertThat(winner?.outcomeType?.code).isEqualTo(SessionAttendanceNDeliusCode.UAAB)
  }

  @Test
  fun `latestByCreatedAt is deterministic across input ordering`() {
    val a = SessionAttendanceEntityFactory().withCreatedAt(baseTime).withCreatedBy("ALICE").produce()
    val b = SessionAttendanceEntityFactory().withCreatedAt(baseTime).withCreatedBy("BOB").produce()
    val c = SessionAttendanceEntityFactory().withCreatedAt(baseTime).withCreatedBy("CAROL").produce()

    // Same three rows, four different input orderings — helper must always pick the same winner.
    val winners = listOf(
      listOf(a, b, c).latestByCreatedAt(),
      listOf(c, b, a).latestByCreatedAt(),
      listOf(b, a, c).latestByCreatedAt(),
      listOf(c, a, b).latestByCreatedAt(),
    )

    assertThat(winners).allMatch { it === c }
  }
}
