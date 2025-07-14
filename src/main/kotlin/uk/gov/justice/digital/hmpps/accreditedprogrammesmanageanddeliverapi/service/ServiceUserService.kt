package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.ClientResult
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.NDeliusIntegrationApiClient
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.ServiceUser
import java.time.LocalDate

@Service
class ServiceUserService(
  private val nDeliusIntegrationApiClient: NDeliusIntegrationApiClient,
) {
  fun getServiceUserByIdentifier(identifier: String): ServiceUser {
    return when (val result = nDeliusIntegrationApiClient.getOffenderIdentifiers(identifier)) {
      is ClientResult.Success -> {
        val user = result.body
        ServiceUser(
          name = listOfNotNull(user.name.forename, user.name.middleNames, user.name.surname)
            .filter { it.isNotBlank() }
            .joinToString(" "),
          crn = user.crn,
          dob = LocalDate.parse(user.dateOfBirth),
          gender = user.sex.description,
          ethnicity = user.ethnicity.description,
          currentPdu = user.probationDeliveryUnit.code,
        )
      }

      is ClientResult.Failure -> result.throwException()
    }
  }

  fun checkIfAuthenticatedDeliusUserHasAccessToServiceUser(username: String, identifier: String): Boolean {
    return when (val result = nDeliusIntegrationApiClient.verifyLaoc(username, listOf(identifier))) {
      is ClientResult.Success -> {
        val response = result.body
        !response.userExcluded && !response.userRestricted
      }

      is ClientResult.Failure -> result.throwException()
    }
  }
}
