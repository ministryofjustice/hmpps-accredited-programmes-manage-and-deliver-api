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

class UserAccessServiceTest {

  private val probationAccessControlApiClient: ProbationAccessControlApiClient = mockk()
  private val userService: UserService = mockk()

  private val username = "AUTH_ADM"
  private val ordinaryCrn = "CRN-ORDINARY"
  private val laoAuthorisedCrn = "CRN-LAO-AUTHORISED"
  private val laoRestrictedCrn = "CRN-LAO-RESTRICTED"

  private fun resolver() = UserAccessService(
    probationAccessControlApiClient = probationAccessControlApiClient,
    userService = userService,
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
  fun `returns access details for authorised and excluded LAOs`() {
    stubCaseAccess(ordinaryCrn)
    stubCaseAccess(laoAuthorisedCrn, restrictedTo = listOf(username))
    stubCaseAccess(laoRestrictedCrn, excludedFrom = listOf(username))
    // Only the ordinary and authorised-LAO CRNs are accessible to this user.
    every { userService.getAccessibleOffenders(username, any()) } returns setOf(ordinaryCrn, laoAuthorisedCrn)

    val result = resolver()
      .determineUserAccess(username, listOf(ordinaryCrn, laoAuthorisedCrn, laoRestrictedCrn))

    assertThat(result[ordinaryCrn]).isEqualTo(UserAccessService.Access(isLimitedAccessOffender = false, isExcluded = false))
    assertThat(result[laoAuthorisedCrn]).isEqualTo(UserAccessService.Access(isLimitedAccessOffender = true, isExcluded = false))
    assertThat(result[laoRestrictedCrn]).isEqualTo(UserAccessService.Access(isLimitedAccessOffender = true, isExcluded = true))

    verify(exactly = 1) { userService.getAccessibleOffenders(username, any()) }
  }

  @Test
  fun `empty crns - returns an empty map and makes no external calls`() {
    val result = resolver().determineUserAccess(username, emptyList())

    assertThat(result).isEmpty()
    verify(exactly = 0) { probationAccessControlApiClient.getCaseAccessByCrn(any()) }
    verify(exactly = 0) { userService.getAccessibleOffenders(any(), any()) }
  }

  @Test
  fun `user is excluded when their username is present in excludedFrom`() {
    stubCaseAccess(ordinaryCrn, excludedFrom = listOf(username))

    assertThat(resolver().isUserExcluded(username, ordinaryCrn)).isTrue()
  }

  @Test
  fun `user is not excluded when excludedFrom contains another username`() {
    stubCaseAccess(ordinaryCrn, excludedFrom = listOf("OTHER_USER"))

    assertThat(resolver().isUserExcluded(username, ordinaryCrn)).isFalse()
  }

  @Test
  fun `user is not excluded when their username is only present in restrictedTo`() {
    stubCaseAccess(ordinaryCrn, restrictedTo = listOf(username))

    assertThat(resolver().isUserExcluded(username, ordinaryCrn)).isFalse()
  }
}
