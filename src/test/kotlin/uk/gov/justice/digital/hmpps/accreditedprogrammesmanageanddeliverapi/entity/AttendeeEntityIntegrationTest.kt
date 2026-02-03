package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.type.Pathway
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.type.SessionType
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.programmeGroup.AccreditedProgrammeTemplateEntityFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.programmeGroup.ProgrammeGroupFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.AttendeeRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ProgrammeGroupRepository
import java.time.LocalDateTime

class AttendeeEntityIntegrationTest : IntegrationTestBase() {

  @Autowired
  private lateinit var attendeeRepository: AttendeeRepository

  @Autowired
  private lateinit var programmeGroupRepository: ProgrammeGroupRepository

  @Test
  @Transactional
  fun `should save and retrieve attendee`() {
    // Given
    val referral = testReferralHelper.createReferral(personName = "Attendee Name")

    val template = AccreditedProgrammeTemplateEntityFactory().produce()
    val module = ModuleEntity(
      accreditedProgrammeTemplate = template,
      name = "Module 1",
      moduleNumber = 1,
    )
    template.modules.add(module)

    val sessionTemplate = ModuleSessionTemplateEntity(
      module = module,
      sessionNumber = 1,
      sessionType = SessionType.GROUP,
      pathway = Pathway.MODERATE_INTENSITY,
      name = "Session 1",
      durationMinutes = 120,
    )
    module.sessionTemplates.add(sessionTemplate)

    val group = ProgrammeGroupFactory()
      .withAccreditedProgrammeTemplate(template)
      .produce()

    val session = SessionEntity(
      programmeGroup = group,
      moduleSessionTemplate = sessionTemplate,
      startsAt = LocalDateTime.now(),
      endsAt = LocalDateTime.now().plusHours(2),
      // Set individual sessions to placeholder types
      isPlaceholder = false,
    )
    group.sessions.add(session)
    programmeGroupRepository.save(group)

    val attendee = AttendeeEntity(
      referral = referral,
      session = session,
    )
    session.attendees.add(attendee)

    // When
    val savedAttendee = attendeeRepository.save(attendee)
    val retrievedAttendee = attendeeRepository.findById(savedAttendee.id!!).get()

    // Then
    assertThat(retrievedAttendee).isNotNull
    assertThat(retrievedAttendee.personName).isEqualTo("Attendee Name")
    assertThat(retrievedAttendee.referral.id).isEqualTo(referral.id)
    assertThat(retrievedAttendee.session.id).isEqualTo(session.id)

    // Verify session association
    val retrievedSession = group.sessions.first()
    assertThat(retrievedSession.attendees).hasSize(1)
    assertThat(retrievedSession.attendees[0].id).isEqualTo(savedAttendee.id)
  }
}
