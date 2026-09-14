package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service.limitedAccessOffenderAuthorisation

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.data.repository.findByIdOrNull
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.ReferralEntityFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ReferralRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service.UserAccessService
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service.UserAccessService.Access
import java.util.UUID

class SessionNoteLimitedAccessOffenderAuthorisationStrategyTest {
  private val referralRepository = mockk<ReferralRepository>()
  private val userAccessService = mockk<UserAccessService>()
  private lateinit var strategy: SessionNoteLimitedAccessOffenderAuthorisationStrategy

  @BeforeEach
  fun setUp() {
    strategy = SessionNoteLimitedAccessOffenderAuthorisationStrategy(referralRepository, userAccessService)
  }

  @Test
  fun `should support session note path`() {
    // Given
    val method = "GET"
    val path =
      "/bff/session/edf44a90-eb51-482f-b3f2-6961e439488b/referral/edf44a90-eb51-482f-b3f2-6961e439488b/session-notes"

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
  fun `should not support session note method call`() {
    // Given
    val method = "POST"
    val path =
      "/bff/session/edf44a90-eb51-482f-b3f2-6961e439488b/referral/edf44a90-eb51-482f-b3f2-6961e439488b/session-notes"

    // When
    val result = strategy.isSupportedPath(method, path)

    // Then
    assertThat(result).isFalse
  }

  @Test
  fun `should authorise user to view session note`() {
    // Given
    val username = "jsmith"
    val referralId = UUID.fromString("edf44a90-eb51-482f-b3f2-6961e439488b")
    val path = "/bff/session/edf44a90-eb51-482f-b3f2-6961e439488b/referral/$referralId/session-notes"
    val referralEntity = ReferralEntityFactory().withId(referralId).produce()
    val caseReferenceNumber = referralEntity.crn
    val access = Access(isLimitedAccessOffender = true, isExcluded = false)
    val accessMap = mapOf(caseReferenceNumber to access)

    every { referralRepository.findByIdOrNull(referralId) } returns referralEntity
    every { userAccessService.determineUserAccess(any(), any()) } returns accessMap

    // When
    val result = strategy.isAuthorised(path, username)

    // Then
    assertThat(result).isTrue
    verify(exactly = 1) { referralRepository.findByIdOrNull(referralId) }
    verify(exactly = 1) { userAccessService.determineUserAccess(username, listOf(caseReferenceNumber)) }
  }

  @Test
  fun `should not authorise user to view session note`() {
    // Given
    val username = "jsmith"
    val referralId = UUID.fromString("edf44a90-eb51-482f-b3f2-6961e439488b")
    val path = "/bff/session/edf44a90-eb51-482f-b3f2-6961e439488b/referral/$referralId/session-notes"
    val referralEntity = ReferralEntityFactory().withId(referralId).produce()
    val caseReferenceNumber = referralEntity.crn
    val access = Access(isLimitedAccessOffender = true, isExcluded = true)
    val accessMap = mapOf(caseReferenceNumber to access)

    every { referralRepository.findByIdOrNull(referralId) } returns referralEntity
    every { userAccessService.determineUserAccess(any(), any()) } returns accessMap

    // When
    val result = strategy.isAuthorised(path, username)

    // Then
    assertThat(result).isFalse
    verify(exactly = 1) { referralRepository.findByIdOrNull(referralId) }
    verify(exactly = 1) { userAccessService.determineUserAccess(username, listOf(caseReferenceNumber)) }
  }

  @Test
  fun `should authorise user to view session note if invalid referral uuid`() {
    // Given
    val username = "jsmith"
    val path = "/bff/session/edf44a90-eb51-482f-b3f2-6961e439488b/referral/123/session-notes"

    // When
    val result = strategy.isAuthorised(path, username)

    // Then
    assertThat(result).isTrue
    verify(exactly = 0) { referralRepository.findByIdOrNull(any()) }
    verify(exactly = 0) { userAccessService.determineUserAccess(any(), any()) }
  }

  @Test
  fun `should authorise user to view session note if referral not found`() {
    // Given
    val username = "jsmith"
    val referralId = UUID.fromString("edf44a90-eb51-482f-b3f2-6961e439488b")
    val path = "/bff/session/edf44a90-eb51-482f-b3f2-6961e439488b/referral/$referralId/session-notes"

    every { referralRepository.findByIdOrNull(referralId) } returns null

    // When
    val result = strategy.isAuthorised(path, username)

    // Then
    assertThat(result).isTrue
    verify(exactly = 1) { referralRepository.findByIdOrNull(referralId) }
    verify(exactly = 0) { userAccessService.determineUserAccess(any(), any()) }
  }
}
