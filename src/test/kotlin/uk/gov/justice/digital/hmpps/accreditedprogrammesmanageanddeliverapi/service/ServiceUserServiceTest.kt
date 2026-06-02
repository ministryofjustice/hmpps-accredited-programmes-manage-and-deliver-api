package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service

import com.microsoft.applicationinsights.TelemetryClient
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
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.LimitedAccessOffenderCheck
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.LimitedAccessOffenderCheckResponse
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common.randomFullName
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.config.logToAppInsights
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.NDeliusPersonalDetailsFactory
import uk.gov.justice.hmpps.kotlin.auth.HmppsAuthenticationHolder

@ExtendWith(MockKExtension::class)
class ServiceUserServiceTest {

  private val nDeliusIntegrationApiClient: NDeliusIntegrationApiClient = mockk()
  private val hmppsAuthenticationHolder: HmppsAuthenticationHolder = mockk()
  private val telemetryClient: TelemetryClient = mockk()
  private val service = UserService(nDeliusIntegrationApiClient, hmppsAuthenticationHolder, telemetryClient)

  @BeforeEach
  fun setUp() {
    every { hmppsAuthenticationHolder.username } returns null
  }

  @Test
  fun `getServiceUserByIdentifier should return service user when client call is successful`() {
    // Given
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
    every { telemetryClient.logToAppInsights(any(), any()) } returns Unit

    // When
    val result = service.getPersonalDetailsByIdentifier(identifier)

    // Then
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
    // Given
    val fullName = randomFullName(middleName = null)
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
    every { telemetryClient.logToAppInsights(any(), any()) } returns Unit

    // When
    val result = service.getPersonalDetailsByIdentifier(identifier)

    // Then
    assertEquals(personalDetails.name, result.name)
    assertEquals(personalDetails.crn, result.crn)
    assertEquals(personalDetails.dateOfBirth, result.dateOfBirth)
    assertEquals(personalDetails.sex, result.sex)
    assertEquals(personalDetails.ethnicity, result.ethnicity)
    assertEquals(personalDetails.age, result.age)
    assertEquals(personalDetails.probationPractitioner, result.probationPractitioner)
    assertEquals(personalDetails.probationDeliveryUnit, result.probationDeliveryUnit)

    verify { nDeliusIntegrationApiClient.getPersonalDetails(identifier) }
    verify { telemetryClient.logToAppInsights(any(), any()) }
  }

  @Test
  fun `getAccessibleOffenders should return only CRNs the user has access to`() {
    // Given
    val username = "jsmith"
    val identifiers = listOf("X123456", "Y654321", "Z111111")
    val accessResponse = LimitedAccessOffenderCheckResponse(
      access = listOf(
        LimitedAccessOffenderCheck(crn = "X123456", userExcluded = false, userRestricted = false), // allowed
        LimitedAccessOffenderCheck(crn = "Y654321", userExcluded = true, userRestricted = false), // excluded
        LimitedAccessOffenderCheck(crn = "Z111111", userExcluded = false, userRestricted = true), // restricted
      ),
    )

    every {
      nDeliusIntegrationApiClient.verifyLimitedAccessOffenderCheck(username, identifiers)
    } returns ClientResult.Success(body = accessResponse, status = HttpStatusCode.valueOf(200))
    every { telemetryClient.logToAppInsights(any(), any()) } returns Unit

    // When
    val result = service.getAccessibleOffenders(username, identifiers)

    // Then
    assertEquals(setOf("X123456"), result)
    verify { nDeliusIntegrationApiClient.verifyLimitedAccessOffenderCheck(username, identifiers) }
    verify { telemetryClient.logToAppInsights(any(), any()) }
  }

  @Test
  fun `checkIfAuthenticatedDeliusUserHasAccessToServiceUser should return true when user has access`() {
    // Given
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
    every { telemetryClient.logToAppInsights(any(), any()) } returns Unit

    // When
    val result = service.hasAccessToLimitedAccessOffender(username, identifier)

    // Then
    assertTrue(result)
    verify { nDeliusIntegrationApiClient.verifyLimitedAccessOffenderCheck(username, listOf(identifier)) }
    verify { telemetryClient.logToAppInsights(any(), any()) }
  }

  @Test
  fun `checkIfAuthenticatedDeliusUserHasAccessToServiceUser should return false when user is restricted or excluded`() {
    // Given
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
    every { telemetryClient.logToAppInsights(any(), any()) } returns Unit

    // When
    val result = service.hasAccessToLimitedAccessOffender(username, identifier)

    // Then
    assertFalse(result)
    verify { nDeliusIntegrationApiClient.verifyLimitedAccessOffenderCheck(username, listOf(identifier)) }
    verify { telemetryClient.logToAppInsights(any(), any()) }
  }

  @Test
  fun `checkIfAuthenticatedDeliusUserHasAccessToServiceUser should return false when no matching CRN found`() {
    // Given
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
    every { telemetryClient.logToAppInsights(any(), any()) } returns Unit

    // When
    val result = service.hasAccessToLimitedAccessOffender(username, identifier)

    // Then
    assertFalse(result)
    verify { nDeliusIntegrationApiClient.verifyLimitedAccessOffenderCheck(username, listOf(identifier)) }
    verify { telemetryClient.logToAppInsights(any(), any()) }
  }

  @Test
  fun `checkIfAuthenticatedDeliusUserHasAccessToServiceUser should return false when access list is empty`() {
    val username = "jsmith"
    val identifier = "X123456"
    val accessResponse = LimitedAccessOffenderCheckResponse(access = emptyList())

    every {
      nDeliusIntegrationApiClient.verifyLimitedAccessOffenderCheck(username, listOf(identifier))
    } returns ClientResult.Success(body = accessResponse, status = HttpStatusCode.valueOf(200))
    every { telemetryClient.logToAppInsights(any(), any()) } returns Unit

    val result = service.hasAccessToLimitedAccessOffender(username, identifier)

    assertFalse(result)
    verify { nDeliusIntegrationApiClient.verifyLimitedAccessOffenderCheck(username, listOf(identifier)) }
    verify { telemetryClient.logToAppInsights(any(), any()) }
  }

  @Test
  fun `checkIfAuthenticatedDeliusUserHasAccessToServiceUser should throw exception when client call fails`() {
    // Given
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
    every { telemetryClient.logToAppInsights(any(), any()) } returns Unit

    // When
    val exception = assertThrows<RuntimeException> {
      service.hasAccessToLimitedAccessOffender(username, identifier)
    }

    // Then
    assertTrue(exception.message!!.contains("Unable to complete POST request"))
    verify { nDeliusIntegrationApiClient.verifyLimitedAccessOffenderCheck(username, listOf(identifier)) }
    verify { telemetryClient.logToAppInsights(any(), any()) }
  }
}
