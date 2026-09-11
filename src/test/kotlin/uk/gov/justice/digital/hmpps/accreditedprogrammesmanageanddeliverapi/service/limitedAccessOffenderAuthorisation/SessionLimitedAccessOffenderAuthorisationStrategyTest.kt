package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service.limitedAccessOffenderAuthorisation

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.type.SessionType
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.ModuleSessionTemplateEntityFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.ReferralEntityFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.programmeGroup.AttendeeFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.programmeGroup.SessionFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.SessionRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service.UserAccessService
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service.UserAccessService.Access
import java.util.Optional
import java.util.UUID

class SessionLimitedAccessOffenderAuthorisationStrategyTest {
  private val sessionRepository = mockk<SessionRepository>()
  private val userAccessService = mockk<UserAccessService>()
  private lateinit var strategy: SessionLimitedAccessOffenderAuthorisationStrategy

  @BeforeEach
  fun setUp() {
    strategy = SessionLimitedAccessOffenderAuthorisationStrategy(userAccessService, sessionRepository)
  }

  @Test
  fun `should support session path`() {
    // Given
    val method = "GET"
    val path = "/bff/session/edf44a90-eb51-482f-b3f2-6961e439488b"

    // When
    val result = strategy.isSupportedPath(method, path)

    // Then
    assertThat(result).isTrue
  }

  @Test
  fun `should support edit session path`() {
    // Given
    val method = "GET"
    val path = "/bff/session/edf44a90-eb51-482f-b3f2-6961e439488b/edit-session-date-and-time"

    // When
    val result = strategy.isSupportedPath(method, path)

    // Then
    assertThat(result).isTrue
  }

  @Test
  fun `should not support path`() {
    // Given
    val method = "GET"
    val path = "/session/edf44a90-eb51-482f-b3f2-6961e439488b"

    // When
    val result = strategy.isSupportedPath(method, path)

    // Then
    assertThat(result).isFalse
  }

  @Test
  fun `should not support session method call`() {
    // Given
    val method = "POST"
    val path = "/bff/session/edf44a90-eb51-482f-b3f2-6961e439488b"

    // When
    val result = strategy.isSupportedPath(method, path)

    // Then
    assertThat(result).isFalse
  }

  @Test
  fun `should authorise user to view session`() {
    // Given
    val username = "jsmith"
    val sessionId = UUID.fromString("edf44a90-eb51-482f-b3f2-6961e439488b")
    val path = "/bff/session/$sessionId"
    val referralEntity = ReferralEntityFactory().produce()
    val moduleSessionTemplate = ModuleSessionTemplateEntityFactory().withSessionType(SessionType.ONE_TO_ONE).produce()
    val sessionEntity = SessionFactory().withId(sessionId).withModuleSessionTemplate(moduleSessionTemplate).produce()
    val attendeeEntity = AttendeeFactory().withReferral(referralEntity).withSession(sessionEntity).produce()
    sessionEntity.attendees = mutableListOf(attendeeEntity)
    val caseReferenceNumber = referralEntity.crn
    val access = Access(isLimitedAccessOffender = true, isExcluded = false)
    val accessMap = mapOf(caseReferenceNumber to access)

    every { sessionRepository.findById(any()) } returns Optional.of(sessionEntity)
    every { userAccessService.determineUserAccess(any(), any()) } returns accessMap

    // When
    val result = strategy.isAuthorised(path, username)

    // Then
    assertThat(result).isTrue
    verify(exactly = 1) { sessionRepository.findById(sessionId) }
    verify(exactly = 1) { userAccessService.determineUserAccess(username, listOf(caseReferenceNumber)) }
  }

  @Test
  fun `should authorise user to view group session`() {
    // Given
    val username = "jsmith"
    val sessionId = UUID.fromString("edf44a90-eb51-482f-b3f2-6961e439488b")
    val path = "/bff/session/$sessionId"
    val referralEntity = ReferralEntityFactory().produce()
    val moduleSessionTemplate = ModuleSessionTemplateEntityFactory().withSessionType(SessionType.GROUP).produce()
    val sessionEntity = SessionFactory().withId(sessionId).withModuleSessionTemplate(moduleSessionTemplate).produce()
    val attendeeEntity = AttendeeFactory().withReferral(referralEntity).withSession(sessionEntity).produce()
    sessionEntity.attendees = mutableListOf(attendeeEntity)

    every { sessionRepository.findById(any()) } returns Optional.of(sessionEntity)

    // When
    val result = strategy.isAuthorised(path, username)

    // Then
    assertThat(result).isTrue
    verify(exactly = 1) { sessionRepository.findById(sessionId) }
    verify(exactly = 0) { userAccessService.determineUserAccess(any(), any()) }
  }

  @Test
  fun `should authorise user to view edit session`() {
    // Given
    val username = "jsmith"
    val sessionId = UUID.fromString("edf44a90-eb51-482f-b3f2-6961e439488b")
    val path = "/bff/session/$sessionId/edit-session-date-and-time"
    val referralEntity = ReferralEntityFactory().produce()
    val moduleSessionTemplate = ModuleSessionTemplateEntityFactory().withSessionType(SessionType.ONE_TO_ONE).produce()
    val sessionEntity = SessionFactory().withId(sessionId).withModuleSessionTemplate(moduleSessionTemplate).produce()
    val attendeeEntity = AttendeeFactory().withReferral(referralEntity).withSession(sessionEntity).produce()
    sessionEntity.attendees = mutableListOf(attendeeEntity)
    val caseReferenceNumber = referralEntity.crn
    val access = Access(isLimitedAccessOffender = true, isExcluded = false)
    val accessMap = mapOf(caseReferenceNumber to access)

    every { sessionRepository.findById(any()) } returns Optional.of(sessionEntity)
    every { userAccessService.determineUserAccess(any(), any()) } returns accessMap

    // When
    val result = strategy.isAuthorised(path, username)

    // Then
    assertThat(result).isTrue
    verify(exactly = 1) { sessionRepository.findById(sessionId) }
    verify(exactly = 1) { userAccessService.determineUserAccess(username, listOf(caseReferenceNumber)) }
  }

  @Test
  fun `should not authorise user to view session`() {
    // Given
    val username = "jsmith"
    val sessionId = UUID.fromString("edf44a90-eb51-482f-b3f2-6961e439488b")
    val path = "/bff/session/$sessionId"
    val referralEntity = ReferralEntityFactory().produce()
    val moduleSessionTemplate = ModuleSessionTemplateEntityFactory().withSessionType(SessionType.ONE_TO_ONE).produce()
    val sessionEntity = SessionFactory().withId(sessionId).withModuleSessionTemplate(moduleSessionTemplate).produce()
    val attendeeEntity = AttendeeFactory().withReferral(referralEntity).withSession(sessionEntity).produce()
    sessionEntity.attendees = mutableListOf(attendeeEntity)

    val caseReferenceNumber = referralEntity.crn
    val access = Access(isLimitedAccessOffender = true, isExcluded = true)
    val accessMap = mapOf(caseReferenceNumber to access)

    every { sessionRepository.findById(any()) } returns Optional.of(sessionEntity)
    every { userAccessService.determineUserAccess(any(), any()) } returns accessMap

    // When
    val result = strategy.isAuthorised(path, username)

    // Then
    assertThat(result).isFalse
    verify(exactly = 1) { sessionRepository.findById(sessionId) }
    verify(exactly = 1) { userAccessService.determineUserAccess(username, listOf(caseReferenceNumber)) }
  }

  @Test
  fun `should authorise user to view session if invalid session uuid`() {
    // Given
    val username = "jsmith"
    val path = "/bff/session/123"

    // When
    val result = strategy.isAuthorised(path, username)

    // Then
    assertThat(result).isTrue
    verify(exactly = 0) { sessionRepository.findById(any()) }
    verify(exactly = 0) { userAccessService.determineUserAccess(any(), any()) }
  }
}
