package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service

import org.springframework.security.access.AccessDeniedException
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.ClientResult
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.NDeliusIntegrationApiClient
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.ServiceUser
import uk.gov.justice.hmpps.kotlin.auth.HmppsAuthenticationHolder
import java.time.LocalDate

@Service
class ServiceUserService(
  private val nDeliusIntegrationApiClient: NDeliusIntegrationApiClient,
  private val authenticationHolder: HmppsAuthenticationHolder,
) {
  fun getServiceUserByIdentifier(identifier: String): ServiceUser {
    val userName = authenticationHolder.username ?: "UNKNOWN_USER"
    if (!hasAccessToLimitedAccessOffender(userName, identifier)) {
      throw AccessDeniedException(
        "You are not authorized to view this person's details. Either contact your administrator or enter a different CRN or Prison Number",
      )
    }

    return when (val result = nDeliusIntegrationApiClient.getOffenderIdentifiers(identifier)) {
      is ClientResult.Success -> {
        val user = result.body
        ServiceUser(
          name = listOfNotNull(user.name.forename, user.name.middleNames, user.name.surname)
            .filter { it.isNotBlank() }
            .joinToString(" "),
          crn = user.crn,
          dateOfBirth = LocalDate.parse(user.dateOfBirth),
          age = user.age,
          gender = user.sex.description,
          ethnicity = user.ethnicity.description,
          currentPdu = user.probationDeliveryUnit.code,
        )
      }

      is ClientResult.Failure -> result.throwException()
    }
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
