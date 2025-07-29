package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service

import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatusCode
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.ClientResult
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.NDeliusIntegrationApiClient
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.FullName
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.LimitedAccessOffenderCheck
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.LimitedAccessOffenderCheckResponse
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.NDeliusPersonalDetailsFactory
import uk.gov.justice.hmpps.kotlin.auth.HmppsAuthenticationHolder

@ExtendWith(MockKExtension::class)
class ServiceUserServiceTest {

  private val nDeliusIntegrationApiClient: NDeliusIntegrationApiClient = mockk()
  private val hmppsAuthenticationHolder: HmppsAuthenticationHolder = mockk()
  private val service = ServiceUserService(nDeliusIntegrationApiClient, hmppsAuthenticationHolder)

  @BeforeEach
  fun setUp() {
    every { hmppsAuthenticationHolder.username } returns null
  }

  @Test
  fun `getServiceUserByIdentifier should return service user when client call is successful`() {
    val identifier = "X123456"
    val personalDetails = NDeliusPersonalDetailsFactory().produce()
    val accessResponse = LimitedAccessOffenderCheckResponse(
      access = listOf(
        LimitedAccessOffenderCheck(
          crn = identifier,
          userExcluded = false,
          userRestricted = false,
        ),
      ),
    )
    every {
      nDeliusIntegrationApiClient.verifyLimitedAccessOffenderCheck(any(), listOf(identifier))
    } returns ClientResult.Success(body = accessResponse, status = HttpStatusCode.valueOf(200))
    every { nDeliusIntegrationApiClient.getPersonalDetails(identifier) } returns
      ClientResult.Success(body = personalDetails, status = HttpStatusCode.valueOf(200))
    val result = service.getPersonalDetailsByIdentifier(identifier)

    assertEquals(personalDetails.name, result.name)
    assertEquals(personalDetails.crn, result.crn)
    assertEquals(personalDetails.dateOfBirth, result.dateOfBirth)
    assertEquals(personalDetails.sex, result.sex)
    assertEquals(personalDetails.ethnicity, result.ethnicity)
    assertEquals(personalDetails.age, result.age)
    assertEquals(personalDetails.probationPractitioner, result.probationPractitioner)
    assertEquals(personalDetails.probationDeliveryUnit, result.probationDeliveryUnit)

    verify { nDeliusIntegrationApiClient.getPersonalDetails(identifier) }
  }

  @Test
  fun `getServiceUserByIdentifier should return service user when client call is successful and missing middle name`() {
    val fullName = FullName(
      forename = "Jim",
      middleNames = null,
      surname = "Halbert",
    )
    val identifier = "X123456"
    val personalDetails = NDeliusPersonalDetailsFactory().withName(fullName).produce()
    val accessResponse = LimitedAccessOffenderCheckResponse(
      access = listOf(
        LimitedAccessOffenderCheck(
          crn = identifier,
          userExcluded = false,
          userRestricted = false,
        ),
      ),
    )
    every {
      nDeliusIntegrationApiClient.verifyLimitedAccessOffenderCheck(any(), listOf(identifier))
    } returns ClientResult.Success(body = accessResponse, status = HttpStatusCode.valueOf(200))
    every { nDeliusIntegrationApiClient.getPersonalDetails(identifier) } returns
      ClientResult.Success(body = personalDetails, status = HttpStatusCode.valueOf(200))
    val result = service.getPersonalDetailsByIdentifier(identifier)

    assertEquals(personalDetails.name, result.name)
    assertEquals(personalDetails.crn, result.crn)
    assertEquals(personalDetails.dateOfBirth, result.dateOfBirth)
    assertEquals(personalDetails.sex, result.sex)
    assertEquals(personalDetails.ethnicity, result.ethnicity)
    assertEquals(personalDetails.age, result.age)
    assertEquals(personalDetails.probationPractitioner, result.probationPractitioner)
    assertEquals(personalDetails.probationDeliveryUnit, result.probationDeliveryUnit)

    verify { nDeliusIntegrationApiClient.getPersonalDetails(identifier) }
  }

  @Test
  fun `checkIfAuthenticatedDeliusUserHasAccessToServiceUser should return true when user has access`() {
    val username = "jsmith"
    val identifier = "X123456"
    val accessResponse = LimitedAccessOffenderCheckResponse(
      access = listOf(
        LimitedAccessOffenderCheck(
          crn = identifier,
          userExcluded = false,
          userRestricted = false,
        ),
      ),
    )

    every {
      nDeliusIntegrationApiClient.verifyLimitedAccessOffenderCheck(username, listOf(identifier))
    } returns ClientResult.Success(body = accessResponse, status = HttpStatusCode.valueOf(200))

    val result = service.hasAccessToLimitedAccessOffender(username, identifier)

    assertTrue(result)
    verify { nDeliusIntegrationApiClient.verifyLimitedAccessOffenderCheck(username, listOf(identifier)) }
  }

  @Test
  fun `checkIfAuthenticatedDeliusUserHasAccessToServiceUser should return false when user is restricted or excluded`() {
    val username = "jsmith"
    val identifier = "X123456"
    val accessResponse = LimitedAccessOffenderCheckResponse(
      access = listOf(
        LimitedAccessOffenderCheck(
          crn = identifier,
          userExcluded = false,
          userRestricted = true,
        ),
      ),
    )

    every {
      nDeliusIntegrationApiClient.verifyLimitedAccessOffenderCheck(username, listOf(identifier))
    } returns ClientResult.Success(body = accessResponse, status = HttpStatusCode.valueOf(200))

    val result = service.hasAccessToLimitedAccessOffender(username, identifier)

    assertFalse(result)
    verify { nDeliusIntegrationApiClient.verifyLimitedAccessOffenderCheck(username, listOf(identifier)) }
  }

  @Test
  fun `checkIfAuthenticatedDeliusUserHasAccessToServiceUser should return false when no matching CRN found`() {
    val username = "jsmith"
    val identifier = "X123456"
    val accessResponse = LimitedAccessOffenderCheckResponse(
      access = listOf(
        LimitedAccessOffenderCheck(
          crn = "Y654321", // Different CRN
          userExcluded = false,
          userRestricted = false,
        ),
      ),
    )

    every {
      nDeliusIntegrationApiClient.verifyLimitedAccessOffenderCheck(username, listOf(identifier))
    } returns ClientResult.Success(body = accessResponse, status = HttpStatusCode.valueOf(200))

    val result = service.hasAccessToLimitedAccessOffender(username, identifier)

    assertFalse(result)
    verify { nDeliusIntegrationApiClient.verifyLimitedAccessOffenderCheck(username, listOf(identifier)) }
  }

  @Test
  fun `checkIfAuthenticatedDeliusUserHasAccessToServiceUser should return false when access list is empty`() {
    val username = "jsmith"
    val identifier = "X123456"
    val accessResponse = LimitedAccessOffenderCheckResponse(access = emptyList())

    every {
      nDeliusIntegrationApiClient.verifyLimitedAccessOffenderCheck(username, listOf(identifier))
    } returns ClientResult.Success(body = accessResponse, status = HttpStatusCode.valueOf(200))

    val result = service.hasAccessToLimitedAccessOffender(username, identifier)

    assertFalse(result)
    verify { nDeliusIntegrationApiClient.verifyLimitedAccessOffenderCheck(username, listOf(identifier)) }
  }

  @Test
  fun `checkIfAuthenticatedDeliusUserHasAccessToServiceUser should throw exception when client call fails`() {
    val username = "jsmith"
    val identifier = "X123456"

    every {
      nDeliusIntegrationApiClient.verifyLimitedAccessOffenderCheck(username, listOf(identifier))
    } returns ClientResult.Failure.StatusCode(
      method = HttpMethod.POST,
      path = "/user/$username/access",
      status = HttpStatusCode.valueOf(500),
      body = """{"error":"Access check failed"}""",
    )

    val exception = assertThrows<RuntimeException> {
      service.hasAccessToLimitedAccessOffender(username, identifier)
    }

    assertTrue(exception.message!!.contains("Unable to complete POST request"))
    verify { nDeliusIntegrationApiClient.verifyLimitedAccessOffenderCheck(username, listOf(identifier)) }
  }
}
