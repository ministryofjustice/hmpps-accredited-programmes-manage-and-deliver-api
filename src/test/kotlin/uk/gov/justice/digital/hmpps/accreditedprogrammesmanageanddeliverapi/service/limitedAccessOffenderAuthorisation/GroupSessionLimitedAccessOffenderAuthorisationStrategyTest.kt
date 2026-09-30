package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service.limitedAccessOffenderAuthorisation

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.web.bind.annotation.RequestMethod
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.type.SessionType
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.ModuleSessionTemplateEntityFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.ReferralEntityFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.programmeGroup.AttendeeFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.programmeGroup.SessionFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.SessionRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service.UserAccessService
import java.util.Optional
import java.util.UUID

class GroupSessionLimitedAccessOffenderAuthorisationStrategyTest {
  private val userAccessService: UserAccessService = mockk()
  private val sessionRepository: SessionRepository = mockk()
  private lateinit var strategy: GroupSessionLimitedAccessOffenderAuthorisationStrategy

  @BeforeEach
  fun setUp() {
    strategy = GroupSessionLimitedAccessOffenderAuthorisationStrategy(userAccessService, sessionRepository)
  }

  @Test
  fun `isSupportedPath returns true for GET requests to group session path`() {
    // Given
    val groupId = UUID.randomUUID()
    val sessionId = UUID.randomUUID()
    val path = "/bff/group/$groupId/session/$sessionId"

    // When
    val result = strategy.isSupportedPath(RequestMethod.GET.name, path)

    // Then
    assertThat(result).isTrue()
  }

  @Test
  fun `isSupportedPath returns true for GET requests to group session sub-paths`() {
    // Given
    val groupId = UUID.randomUUID()
    val sessionId = UUID.randomUUID()
    val path = "/bff/group/$groupId/session/$sessionId/something-else"

    // When
    val result = strategy.isSupportedPath(RequestMethod.GET.name, path)

    // Then
    assertThat(result).isTrue()
  }

  @Test
  fun `isSupportedPath returns false for non-GET requests`() {
    // Given
    val groupId = UUID.randomUUID()
    val sessionId = UUID.randomUUID()
    val path = "/bff/group/$groupId/session/$sessionId"

    // When
    val result = strategy.isSupportedPath(RequestMethod.POST.name, path)

    // Then
    assertThat(result).isFalse()
  }

  @Test
  fun `isSupportedPath returns false for non-matching paths`() {
    // Given
    val path = "/something-else"

    // When
    val result = strategy.isSupportedPath(RequestMethod.GET.name, path)

    // Then
    assertThat(result).isFalse()
  }

  @Test
  fun `isAuthorised returns true when sessionId cannot be extracted`() {
    // Given
    val path = "/invalid-path"
    val username = "test-user"

    // When
    val result = strategy.isAuthorised(path, username)

    // Then
    assertThat(result).isTrue()
  }

  @Test
  fun `isAuthorised returns true when session is not found`() {
    // Given
    val sessionId = UUID.randomUUID()
    val path = "/bff/group/${UUID.randomUUID()}/session/$sessionId"
    val username = "test-user"

    every { sessionRepository.findById(sessionId) } returns Optional.empty()

    // When
    val result = strategy.isAuthorised(path, username)

    // Then
    assertThat(result).isTrue()
    verify { sessionRepository.findById(sessionId) }
  }

  @Test
  fun `isAuthorised returns true when session is not ONE_TO_ONE`() {
    // Given
    val sessionId = UUID.randomUUID()
    val path = "/bff/group/${UUID.randomUUID()}/session/$sessionId"
    val username = "test-user"

    val session = SessionFactory()
      .withModuleSessionTemplate(
        ModuleSessionTemplateEntityFactory().withSessionType(SessionType.GROUP).produce(),
      )
      .produce()

    every { sessionRepository.findById(sessionId) } returns Optional.of(session)

    // When
    val result = strategy.isAuthorised(path, username)

    // Then
    assertThat(result).isTrue()
    verify { sessionRepository.findById(sessionId) }
  }

  @Test
  fun `isAuthorised returns true when session has no attendees`() {
    // Given
    val sessionId = UUID.randomUUID()
    val path = "/bff/group/${UUID.randomUUID()}/session/$sessionId"
    val username = "test-user"

    val session = SessionFactory()
      .withModuleSessionTemplate(
        ModuleSessionTemplateEntityFactory().withSessionType(SessionType.ONE_TO_ONE).produce(),
      )
      .withAttendees(mutableListOf())
      .produce()

    every { sessionRepository.findById(sessionId) } returns Optional.of(session)

    // When
    val result = strategy.isAuthorised(path, username)

    // Then
    assertThat(result).isTrue()
    verify { sessionRepository.findById(sessionId) }
  }

  @Test
  fun `isAuthorised returns true when userAccessService returns no access info for CRN`() {
    // Given
    val sessionId = UUID.randomUUID()
    val path = "/bff/group/${UUID.randomUUID()}/session/$sessionId"
    val username = "test-user"
    val crn = "X123456"

    val referral = ReferralEntityFactory().withCrn(crn).produce()
    val session = SessionFactory()
      .withModuleSessionTemplate(
        ModuleSessionTemplateEntityFactory().withSessionType(SessionType.ONE_TO_ONE).produce(),
      )
      .produce()

    val attendee = AttendeeFactory().withReferral(referral).withSession(session).produce()
    session.attendees.add(attendee)

    every { sessionRepository.findById(sessionId) } returns Optional.of(session)
    every { userAccessService.determineUserAccess(username, listOf(crn)) } returns emptyMap()

    // When
    val result = strategy.isAuthorised(path, username)

    // Then
    assertThat(result).isTrue()
    verify { sessionRepository.findById(sessionId) }
    verify { userAccessService.determineUserAccess(username, listOf(crn)) }
  }

  @Test
  fun `isAuthorised returns true when user is NOT excluded`() {
    // Given
    val sessionId = UUID.randomUUID()
    val path = "/bff/group/${UUID.randomUUID()}/session/$sessionId"
    val username = "test-user"
    val crn = "X123456"

    val referral = ReferralEntityFactory().withCrn(crn).produce()
    val session = SessionFactory()
      .withModuleSessionTemplate(
        ModuleSessionTemplateEntityFactory().withSessionType(SessionType.ONE_TO_ONE).produce(),
      )
      .produce()

    val attendee = AttendeeFactory().withReferral(referral).withSession(session).produce()
    session.attendees.add(attendee)

    every { sessionRepository.findById(sessionId) } returns Optional.of(session)
    every { userAccessService.determineUserAccess(username, listOf(crn)) } returns mapOf(
      crn to UserAccessService.Access(isExcluded = false, isLimitedAccessOffender = false),
    )

    // When
    val result = strategy.isAuthorised(path, username)

    // Then
    assertThat(result).isTrue()
    verify { sessionRepository.findById(sessionId) }
    verify { userAccessService.determineUserAccess(username, listOf(crn)) }
  }

  @Test
  fun `isAuthorised returns false when user IS excluded`() {
    // Given
    val sessionId = UUID.randomUUID()
    val path = "/bff/group/${UUID.randomUUID()}/session/$sessionId"
    val username = "test-user"
    val crn = "X123456"

    val referral = ReferralEntityFactory().withCrn(crn).produce()
    val session = SessionFactory()
      .withModuleSessionTemplate(
        ModuleSessionTemplateEntityFactory().withSessionType(SessionType.ONE_TO_ONE).produce(),
      )
      .produce()

    val attendee = AttendeeFactory().withReferral(referral).withSession(session).produce()
    session.attendees.add(attendee)

    every { sessionRepository.findById(sessionId) } returns Optional.of(session)
    every { userAccessService.determineUserAccess(username, listOf(crn)) } returns mapOf(
      crn to UserAccessService.Access(isExcluded = true, isLimitedAccessOffender = true),
    )

    // When
    val result = strategy.isAuthorised(path, username)

    // Then
    assertThat(result).isFalse()
    verify { sessionRepository.findById(sessionId) }
    verify { userAccessService.determineUserAccess(username, listOf(crn)) }
  }
}
