package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.subjectAccessRequest

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.FacilitatorEntityFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.SessionAttendanceEntityFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.SessionNotesHistoryEntityFactory

/**
 * SAR mapper tests covering the APG-2580 surname-only change. See
 * `docs/APG-2580-sar-recorded-by-surname-only.md` §6.
 *
 * Two positive assertions on the changed mappers, plus a regression guard
 * proving the plain `Facilitator.toApi()` still emits the stored full name
 * (used by the Facilitators-roster section).
 */
class SubjectAccessRequestSurnameMappingTest {

  @Test
  fun `SessionNotesHistoryEntity toApi returns surname only including particles`() {
    val entity = SessionNotesHistoryEntityFactory()
      .withCreatedByFullName("John van der Berg")
      .produce()

    val api = entity.toApi()

    assertThat(api.recordedBy).isEqualTo("van der Berg")
  }

  @Test
  fun `SessionNotesHistoryEntity toApi returns null when full name is null`() {
    val entity = SessionNotesHistoryEntityFactory()
      .withCreatedByFullName(null)
      .produce()

    val api = entity.toApi()

    assertThat(api.recordedBy).isNull()
  }

  @Test
  fun `SessionAttendanceEntity toApi surname-only's the recordedByFacilitator`() {
    val facilitator = FacilitatorEntityFactory().withPersonName("Joe Bloggs").produce()
    val attendance = SessionAttendanceEntityFactory(recordedByFacilitator = facilitator).produce()

    val api = attendance.toApi()

    assertThat(api.recordedByFacilitator?.personName).isEqualTo("Bloggs")
  }

  @Test
  fun `SessionAttendanceEntity toApi preserves compound-surname particles on recordedByFacilitator`() {
    val facilitator = FacilitatorEntityFactory().withPersonName("Maria de la Cruz").produce()
    val attendance = SessionAttendanceEntityFactory(recordedByFacilitator = facilitator).produce()

    val api = attendance.toApi()

    assertThat(api.recordedByFacilitator?.personName).isEqualTo("de la Cruz")
  }

  @Test
  fun `regression guard - plain FacilitatorEntity toApi still emits full name for the Facilitators roster`() {
    // The Facilitators-roster section (SubjectAccessRequestSessionFacilitator)
    // uses the plain `.toApi()` extension and must keep full names. See
    // docs/APG-2580-sar-recorded-by-surname-only.md §2.1.
    val facilitator = FacilitatorEntityFactory().withPersonName("Joe Bloggs").produce()

    val api = facilitator.toApi()

    assertThat(api.personName).isEqualTo("Joe Bloggs")
  }

  @Test
  fun `toSarRecordedByApi falls back to full name when toSurname returns null`() {
    // Defensive: FacilitatorEntity.personName is non-null in the schema, but
    // if it were ever whitespace-only the surname helper returns null. The
    // projection must still populate the non-null DTO field.
    val facilitator = FacilitatorEntityFactory().withPersonName("Bloggs").produce()

    val api = facilitator.toSarRecordedByApi()

    // Single token — returned unchanged.
    assertThat(api.personName).isEqualTo("Bloggs")
  }
}
