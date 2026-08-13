package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.ClientResult
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.probationAccessControlApi.ProbationAccessControlApiClient
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.probationAccessControlApi.model.AllCaseAccess
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.probationAccessControlApi.model.AllCaseAccessUsernameRange
import java.time.OffsetDateTime

class LimitedAccessResolverServiceTest {

  private val probationAccessControlApiClient: ProbationAccessControlApiClient = mockk()
  private val userService: UserService = mockk()

  private val username = "AUTH_ADM"
  private val ordinaryCrn = "CRN-ORDINARY"
  private val laoAuthorisedCrn = "CRN-LAO-AUTHORISED"
  private val laoRestrictedCrn = "CRN-LAO-RESTRICTED"

  private fun resolver(accessCheckEnabled: Boolean, restrictionEnabled: Boolean) = LimitedAccessResolverService(
    probationAccessControlApiClient = probationAccessControlApiClient,
    userService = userService,
    laoAccessCheckEnabled = accessCheckEnabled,
    restrictionEnabled = restrictionEnabled,
  )

  private fun stubCaseAccess(
    crn: String,
    excludedFrom: List<String> = emptyList(),
    restrictedTo: List<String> = emptyList(),
  ) {
    every { probationAccessControlApiClient.getCaseAccessByCrn(crn) } returns ClientResult.Success(
      HttpStatus.OK,
      AllCaseAccess(
        crn = crn,
        excludedFrom = excludedFrom.map { usernameRange(it) },
        restrictedTo = restrictedTo.map { usernameRange(it) },
      ),
    )
  }

  private fun usernameRange(name: String) = AllCaseAccessUsernameRange(username = name, since = OffsetDateTime.now().minusDays(1), until = null)

  @Test
  fun `both feature flags off should not make any calls to getCaseAccessByCrn and no LAO's`() {
    val result = resolver(accessCheckEnabled = false, restrictionEnabled = false)
      .resolve(username, listOf(ordinaryCrn, laoRestrictedCrn))

    assertThat(result[ordinaryCrn]).isEqualTo(LimitedAccessResolverService.Access(lao = false, isExcluded = false))
    assertThat(result[laoRestrictedCrn]).isEqualTo(LimitedAccessResolverService.Access(lao = false, isExcluded = false))

    verify(exactly = 0) { probationAccessControlApiClient.getCaseAccessByCrn(any()) }
    verify(exactly = 0) { userService.getAccessibleOffenders(any(), any()) }
  }

  @Test
  fun `access check enabled feature flag shows authorised LAO's`() {
    stubCaseAccess(ordinaryCrn)
    stubCaseAccess(laoAuthorisedCrn, restrictedTo = listOf(username))
    stubCaseAccess(laoRestrictedCrn, excludedFrom = listOf(username))
    // Only the ordinary and authorised-LAO CRNs are accessible to this user.
    every { userService.getAccessibleOffenders(username, any()) } returns setOf(ordinaryCrn, laoAuthorisedCrn)

    val result = resolver(accessCheckEnabled = true, restrictionEnabled = false)
      .resolve(username, listOf(ordinaryCrn, laoAuthorisedCrn, laoRestrictedCrn))

    assertThat(result[ordinaryCrn]).isEqualTo(LimitedAccessResolverService.Access(lao = false, isExcluded = false))
    assertThat(result[laoAuthorisedCrn]).isEqualTo(LimitedAccessResolverService.Access(lao = true, isExcluded = false))
    assertThat(result[laoRestrictedCrn]).isEqualTo(LimitedAccessResolverService.Access(lao = true, isExcluded = true))

    verify(exactly = 1) { userService.getAccessibleOffenders(username, any()) }
  }

  @Test
  fun `restriction flag enabled unauthorised participants are excluded`() {
    every { userService.getAccessibleOffenders(username, any()) } returns setOf(ordinaryCrn, laoAuthorisedCrn)

    val result = resolver(accessCheckEnabled = false, restrictionEnabled = true)
      .resolve(username, listOf(ordinaryCrn, laoAuthorisedCrn, laoRestrictedCrn))

    assertThat(result[ordinaryCrn]).isEqualTo(LimitedAccessResolverService.Access(lao = false, isExcluded = false))
    assertThat(result[laoAuthorisedCrn]).isEqualTo(LimitedAccessResolverService.Access(lao = false, isExcluded = false))
    assertThat(result[laoRestrictedCrn]).isEqualTo(LimitedAccessResolverService.Access(lao = false, isExcluded = true))

    verify(exactly = 0) { probationAccessControlApiClient.getCaseAccessByCrn(any()) }
    verify(exactly = 1) { userService.getAccessibleOffenders(username, any()) }
  }

  @Test
  fun `both feature flags enabled - LAOs are access checked and unauthorised LAOs are excluded`() {
    stubCaseAccess(ordinaryCrn)
    stubCaseAccess(laoAuthorisedCrn, restrictedTo = listOf(username))
    stubCaseAccess(laoRestrictedCrn, excludedFrom = listOf(username))
    every { userService.getAccessibleOffenders(username, any()) } returns setOf(ordinaryCrn, laoAuthorisedCrn)

    val result = resolver(accessCheckEnabled = true, restrictionEnabled = true)
      .resolve(username, listOf(ordinaryCrn, laoAuthorisedCrn, laoRestrictedCrn))

    assertThat(result[ordinaryCrn]).isEqualTo(LimitedAccessResolverService.Access(lao = false, isExcluded = false))
    assertThat(result[laoAuthorisedCrn]).isEqualTo(LimitedAccessResolverService.Access(lao = true, isExcluded = false))
    assertThat(result[laoRestrictedCrn]).isEqualTo(LimitedAccessResolverService.Access(lao = true, isExcluded = true))
  }

  @Test
  fun `empty crns - returns an empty map and makes no external calls`() {
    val result = resolver(accessCheckEnabled = true, restrictionEnabled = true).resolve(username, emptyList())

    assertThat(result).isEmpty()
    verify(exactly = 0) { probationAccessControlApiClient.getCaseAccessByCrn(any()) }
    verify(exactly = 0) { userService.getAccessibleOffenders(any(), any()) }
  }
}
