package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service

import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.http.HttpStatusCode
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.ClientResult
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.NDeliusIntegrationApiClient
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.CodeDescription
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.LimitedAccessOffenderCheck
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.LimitedAccessOffenderCheckResponse
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.OffenderFullName
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.OffenderIdentifiers
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.ProbationDeliveryUnit
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.ProbationPractitioner
import java.time.LocalDate

@ExtendWith(MockKExtension::class)
class ServiceUserServiceTest {

  private val nDeliusIntegrationApiClient: NDeliusIntegrationApiClient = mockk()
  private val service = ServiceUserService(nDeliusIntegrationApiClient)

  @Test
  fun `getServiceUserByIdentifier should return service user when client call is successful`() {
    val identifier = "X123456"
    val offenderIdentifiers = OffenderIdentifiers(
      crn = "X123456",
      name = OffenderFullName(forename = "John", middleNames = "H", surname = "Doe"),
      dateOfBirth = "1990-01-01",
      age = "33",
      sex = CodeDescription(code = "M", description = "Male"),
      ethnicity = CodeDescription(code = "W1", description = "White"),
      probationPractitioner = ProbationPractitioner(
        name = OffenderFullName("Prob", "", "Officer"),
        code = "PRAC01",
        email = "prob.officer@example.com",
      ),
      probationDeliveryUnit = ProbationDeliveryUnit(code = "PDU1", description = "Central PDU"),
    )
    every { nDeliusIntegrationApiClient.getOffenderIdentifiers(identifier) } returns
      ClientResult.Success(body = offenderIdentifiers, status = HttpStatusCode.valueOf(200))
    val result = service.getServiceUserByIdentifier(identifier)

    assertEquals("John H Doe", result.name)
    assertEquals("X123456", result.crn)
    assertEquals(LocalDate.of(1990, 1, 1), result.dob)
    assertEquals("Male", result.gender)
    assertEquals("White", result.ethnicity)
    assertEquals("PDU1", result.currentPdu)

    verify { nDeliusIntegrationApiClient.getOffenderIdentifiers(identifier) }
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
      method = org.springframework.http.HttpMethod.POST,
      path = "/users/$username/access",
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
