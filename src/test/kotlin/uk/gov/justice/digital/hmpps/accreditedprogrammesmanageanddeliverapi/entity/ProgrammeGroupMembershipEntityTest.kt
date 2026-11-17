package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.toCurrentGroupDetails
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.type.SessionAttendanceNDeliusCode
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.ModuleSessionTemplateEntityFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.SessionAttendanceEntityFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.SessionAttendanceNDeliusOutcomeEntityFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.SessionNotesHistoryEntityFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.programmeGroup.ProgrammeGroupFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.programmeGroup.ProgrammeGroupMembershipFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.programmeGroup.SessionFactory
import java.time.LocalDateTime
import java.util.UUID

class ProgrammeGroupMembershipEntityTest {

  @Nested
  @DisplayName("toCurrentGroupDetails")
  inner class ToCurrentGroupDetails {

    @Test
    fun `should return CurrentGroupDetails with group information and empty attendance histories when no attendances exist`() {
      // Given
      val groupId = UUID.randomUUID()
      val groupCode = "GRP-001"
      val programmeGroup = ProgrammeGroupFactory()
        .withId(groupId)
        .withCode(groupCode)
        .produce()

      val membership = ProgrammeGroupMembershipFactory()
        .withProgrammeGroup(programmeGroup)
        .withAttendances(mutableSetOf())
        .produce()

      // When
      val result = membership.toCurrentGroupDetails(personName = "John Smith")

      // Then
      assertThat(result.currentlyAllocatedGroupId).isEqualTo(groupId)
      assertThat(result.currentlyAllocatedGroupCode).isEqualTo(groupCode)
      assertThat(result.attendanceHistories).isEmpty()
    }

    @Test
    fun `should map attendance histories from membership attendances when sessionAttendances is not provided`() {
      // Given
      val groupId = UUID.randomUUID()
      val groupCode = "GRP-002"
      val sessionId = UUID.randomUUID()
      val startsAt = LocalDateTime.of(2026, 5, 20, 10, 0)
      val endsAt = LocalDateTime.of(2026, 5, 20, 11, 0)

      val programmeGroup = ProgrammeGroupFactory()
        .withId(groupId)
        .withCode(groupCode)
        .produce()

      val moduleSessionTemplate = ModuleSessionTemplateEntityFactory()
        .withName("Session 1: Getting Started")
        .produce()

      val session = SessionFactory(programmeGroup = programmeGroup, moduleSessionTemplate = moduleSessionTemplate)
        .withId(sessionId)
        .withStartsAt(startsAt)
        .withEndsAt(endsAt)
        .withIsCatchup(false)
        .produce()

      val outcome = SessionAttendanceNDeliusOutcomeEntityFactory()
        .withCode(SessionAttendanceNDeliusCode.ATTC)
        .withDescription("Attended - Complied")
        .produce()

      val membership = ProgrammeGroupMembershipFactory()
        .withProgrammeGroup(programmeGroup)
        .produce()

      val attendance = SessionAttendanceEntityFactory(session = session, groupMembership = membership)
        .withOutcomeType(outcome)
        .produce()

      membership.attendances = mutableSetOf(attendance)

      // When
      val result = membership.toCurrentGroupDetails(personName = "Alex River")

      // Then
      assertThat(result.currentlyAllocatedGroupId).isEqualTo(groupId)
      assertThat(result.currentlyAllocatedGroupCode).isEqualTo(groupCode)
      assertThat(result.attendanceHistories).hasSize(1)

      val attendanceHistory = result.attendanceHistories.first()
      assertThat(attendanceHistory.sessionId).isEqualTo(sessionId)
      assertThat(attendanceHistory.sessionName).isEqualTo("Session 1: Getting Started")
      assertThat(attendanceHistory.groupCode).isEqualTo(groupCode)
      assertThat(attendanceHistory.popName).isEqualTo("Alex River")
      assertThat(attendanceHistory.groupId).isEqualTo(groupId)
      assertThat(attendanceHistory.date).isEqualTo("20 May 2026")
      assertThat(attendanceHistory.unformattedDate).isEqualTo(startsAt)
      assertThat(attendanceHistory.time).isEqualTo("10am to 11am")
      assertThat(attendanceHistory.timeWithCapitalisedMidday).isEqualTo("10am to 11am")
      assertThat(attendanceHistory.attendanceStatus).isEqualTo("Attended - Complied")
      assertThat(attendanceHistory.hasNotes).isFalse()
      assertThat(attendanceHistory.isCatchup).isFalse()
    }

    @Test
    fun `should map attendance histories from custom sessionAttendances parameter when provided`() {
      // Given
      val groupId = UUID.randomUUID()
      val groupCode = "GRP-003"
      val membershipSessionId = UUID.randomUUID()
      val customSessionId = UUID.randomUUID()

      val programmeGroup = ProgrammeGroupFactory()
        .withId(groupId)
        .withCode(groupCode)
        .produce()

      val membership = ProgrammeGroupMembershipFactory()
        .withProgrammeGroup(programmeGroup)
        .produce()

      val membershipSession = SessionFactory(programmeGroup = programmeGroup)
        .withId(membershipSessionId)
        .produce()
      val membershipAttendance =
        SessionAttendanceEntityFactory(session = membershipSession, groupMembership = membership)
          .produce()
      membership.attendances = mutableSetOf(membershipAttendance)

      val customSession = SessionFactory(programmeGroup = programmeGroup)
        .withId(customSessionId)
        .withStartsAt(LocalDateTime.of(2026, 6, 1, 14, 0))
        .withEndsAt(LocalDateTime.of(2026, 6, 1, 16, 0))
        .produce()
      val customAttendance = SessionAttendanceEntityFactory(session = customSession, groupMembership = membership)
        .produce()

      // When
      val result = membership.toCurrentGroupDetails(
        personName = "Jane Doe",
        sessionAttendances = listOf(customAttendance),
      )

      // Then
      assertThat(result.currentlyAllocatedGroupId).isEqualTo(groupId)
      assertThat(result.currentlyAllocatedGroupCode).isEqualTo(groupCode)
      assertThat(result.attendanceHistories).hasSize(1)
      assertThat(result.attendanceHistories.first().sessionId).isEqualTo(customSessionId)
    }

    @Test
    fun `should correctly map hasNotes when attendance has notes in notesHistory`() {
      // Given
      val groupId = UUID.randomUUID()
      val programmeGroup = ProgrammeGroupFactory().withId(groupId).produce()
      val membership = ProgrammeGroupMembershipFactory().withProgrammeGroup(programmeGroup).produce()
      val session = SessionFactory(programmeGroup = programmeGroup).withId(UUID.randomUUID()).produce()

      val attendance = SessionAttendanceEntityFactory(session = session, groupMembership = membership).produce()
      val note = SessionNotesHistoryEntityFactory(attendance = attendance).produce()
      attendance.notesHistory.add(note)

      // When
      val result = membership.toCurrentGroupDetails(
        personName = "John Smith",
        sessionAttendances = listOf(attendance),
      )

      // Then
      assertThat(result.attendanceHistories.first().hasNotes).isTrue()
    }

    @Test
    fun `should correctly map isCatchup when session is a catch-up session`() {
      // Given
      val groupId = UUID.randomUUID()
      val programmeGroup = ProgrammeGroupFactory().withId(groupId).produce()
      val membership = ProgrammeGroupMembershipFactory().withProgrammeGroup(programmeGroup).produce()
      val session = SessionFactory(programmeGroup = programmeGroup)
        .withId(UUID.randomUUID())
        .withIsCatchup(true)
        .produce()

      val attendance = SessionAttendanceEntityFactory(session = session, groupMembership = membership).produce()

      // When
      val result = membership.toCurrentGroupDetails(
        personName = "John Smith",
        sessionAttendances = listOf(attendance),
      )

      // Then
      assertThat(result.attendanceHistories.first().isCatchup).isTrue()
    }

    @Test
    fun `should correctly format midday times for both standard and capitalised time strings`() {
      // Given
      val groupId = UUID.randomUUID()
      val programmeGroup = ProgrammeGroupFactory().withId(groupId).produce()
      val membership = ProgrammeGroupMembershipFactory().withProgrammeGroup(programmeGroup).produce()
      val session = SessionFactory(programmeGroup = programmeGroup)
        .withId(UUID.randomUUID())
        .withStartsAt(LocalDateTime.of(2026, 7, 10, 12, 0))
        .withEndsAt(LocalDateTime.of(2026, 7, 10, 13, 0))
        .produce()

      val attendance = SessionAttendanceEntityFactory(session = session, groupMembership = membership).produce()

      // When
      val result = membership.toCurrentGroupDetails(
        personName = "John Smith",
        sessionAttendances = listOf(attendance),
      )

      // Then
      val history = result.attendanceHistories.first()
      assertThat(history.time).isEqualTo("midday to 1pm")
      assertThat(history.timeWithCapitalisedMidday).isEqualTo("Midday to 1pm")
    }

    @Test
    fun `should map attendanceStatus as 'Did not attend' for UAAB outcome`() {
      // Given
      val groupId = UUID.randomUUID()
      val programmeGroup = ProgrammeGroupFactory().withId(groupId).produce()
      val membership = ProgrammeGroupMembershipFactory().withProgrammeGroup(programmeGroup).produce()
      val session = SessionFactory(programmeGroup = programmeGroup).withId(UUID.randomUUID()).produce()

      val uaabOutcome = SessionAttendanceNDeliusOutcomeEntityFactory()
        .withCode(SessionAttendanceNDeliusCode.UAAB)
        .withDescription("Unacceptable Absence")
        .produce()

      val attendance = SessionAttendanceEntityFactory(session = session, groupMembership = membership)
        .withOutcomeType(uaabOutcome)
        .produce()

      // When
      val result = membership.toCurrentGroupDetails(
        personName = "John Smith",
        sessionAttendances = listOf(attendance),
      )

      // Then
      assertThat(result.attendanceHistories.first().attendanceStatus).isEqualTo("Did not attend")
    }
  }
}
