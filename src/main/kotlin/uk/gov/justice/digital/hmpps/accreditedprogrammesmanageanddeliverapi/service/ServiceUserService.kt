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
  fun getServiceUserByIdentifier(identifier: String): ServiceUser = when (val result = nDeliusIntegrationApiClient.getOffenderIdentifiers(identifier)) {
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

  fun hasAccessToLimitedAccessOffender(username: String, identifier: String): Boolean = when (val result = nDeliusIntegrationApiClient.verifyLimitedAccessOffenderCheck(username, listOf(identifier))) {
    is ClientResult.Success -> {
      val response = result.body
      val accessCheck = response.access.firstOrNull { it.crn == identifier }
      accessCheck?.let { !it.userExcluded && !it.userRestricted } ?: false
    }

    is ClientResult.Failure -> result.throwException()
  }
}
