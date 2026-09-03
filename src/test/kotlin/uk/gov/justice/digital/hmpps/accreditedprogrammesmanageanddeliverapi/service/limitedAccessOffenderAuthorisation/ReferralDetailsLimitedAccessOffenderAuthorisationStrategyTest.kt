package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service.limitedAccessOffenderAuthorisation

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.ReferralEntityFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service.ReferralService
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service.UserAccessService
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service.UserAccessService.Access
import java.util.UUID

class ReferralDetailsLimitedAccessOffenderAuthorisationStrategyTest {
  private val referralService = mockk<ReferralService>()
  private val userAccessService = mockk<UserAccessService>()
  private lateinit var strategy: ReferralDetailsLimitedAccessOffenderAuthorisationStrategy

  @BeforeEach
  fun setUp() {
    strategy = ReferralDetailsLimitedAccessOffenderAuthorisationStrategy(referralService, userAccessService)
  }

  @Test
  fun `should support referral details path`() {
    // Given
    val method = "GET"
    val path = "/referral-details/edf44a90-eb51-482f-b3f2-6961e439488b"

    // When
    val result = strategy.isSupportedPath(method, path)

    // Then
    assertThat(result).isTrue
  }

  @Test
  fun `should support referral personal details path`() {
    // Given
    val method = "GET"
    val path = "/referral-details/edf44a90-eb51-482f-b3f2-6961e439488b/personal-details"

    // When
    val result = strategy.isSupportedPath(method, path)

    // Then
    assertThat(result).isTrue
  }

  @Test
  fun `should support not support path`() {
    // Given
    val method = "GET"
    val path = "/referrals/edf44a90-eb51-482f-b3f2-6961e439488b"

    // When
    val result = strategy.isSupportedPath(method, path)

    // Then
    assertThat(result).isFalse
  }

  @Test
  fun `should support referral personal details method`() {
    // Given
    val method = "POST"
    val path = "/referral-details/edf44a90-eb51-482f-b3f2-6961e439488b"

    // When
    val result = strategy.isSupportedPath(method, path)

    // Then
    assertThat(result).isFalse
  }

  @Test
  fun `should authorise user to view referral details`() {
    // Given
    val username = "jsmith"
    val referralId = UUID.fromString("edf44a90-eb51-482f-b3f2-6961e439488b")
    val path = "/referral-details/$referralId"
    val referralEntity = ReferralEntityFactory().withId(referralId).produce()
    val caseReferenceNumber = referralEntity.crn
    val access = Access(isLimitedAccessOffender = true, isExcluded = false)
    val accessMap = mapOf(caseReferenceNumber to access)

    every { referralService.getReferralById(any()) } returns referralEntity
    every { userAccessService.determineUserAccess(any(), any()) } returns accessMap

    // When
    val result = strategy.isAuthorised(path, username)

    // Then
    assertThat(result).isTrue
    verify(exactly = 1) { referralService.getReferralById(referralId) }
    verify(exactly = 1) { userAccessService.determineUserAccess(username, listOf(caseReferenceNumber)) }
  }

  @Test
  fun `should authorise user to view referral personal details`() {
    // Given
    val username = "jsmith"
    val referralId = UUID.fromString("edf44a90-eb51-482f-b3f2-6961e439488b")
    val path = "/referral-details/$referralId/personal-details"
    val referralEntity = ReferralEntityFactory().withId(referralId).produce()
    val caseReferenceNumber = referralEntity.crn
    val access = Access(isLimitedAccessOffender = true, isExcluded = false)
    val accessMap = mapOf(caseReferenceNumber to access)

    every { referralService.getReferralById(any()) } returns referralEntity
    every { userAccessService.determineUserAccess(any(), any()) } returns accessMap

    // When
    val result = strategy.isAuthorised(path, username)

    // Then
    assertThat(result).isTrue
    verify(exactly = 1) { referralService.getReferralById(referralId) }
    verify(exactly = 1) { userAccessService.determineUserAccess(username, listOf(caseReferenceNumber)) }
  }

  @Test
  fun `should not authorise user to view referral details`() {
    // Given
    val username = "jsmith"
    val referralId = UUID.fromString("edf44a90-eb51-482f-b3f2-6961e439488b")
    val path = "/referral-details/$referralId"
    val referralEntity = ReferralEntityFactory().withId(referralId).produce()
    val caseReferenceNumber = referralEntity.crn
    val access = Access(isLimitedAccessOffender = true, isExcluded = true)
    val accessMap = mapOf(caseReferenceNumber to access)

    every { referralService.getReferralById(any()) } returns referralEntity
    every { userAccessService.determineUserAccess(any(), any()) } returns accessMap

    // When
    val result = strategy.isAuthorised(path, username)

    // Then
    assertThat(result).isFalse
    verify(exactly = 1) { referralService.getReferralById(referralId) }
    verify(exactly = 1) { userAccessService.determineUserAccess(username, listOf(caseReferenceNumber)) }
  }

  @Test
  fun `should not authorise user to view referral details if invalid referral uuid`() {
    // Given
    val username = "jsmith"
    val path = "/referral-details/123"

    // When
    val result = strategy.isAuthorised(path, username)

    // Then
    assertThat(result).isFalse
    verify(exactly = 0) { referralService.getReferralById(any()) }
    verify(exactly = 0) { userAccessService.determineUserAccess(any(), any()) }
  }
}
