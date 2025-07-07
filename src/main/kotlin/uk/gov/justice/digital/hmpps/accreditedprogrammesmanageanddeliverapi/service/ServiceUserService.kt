package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.util.UriComponentsBuilder
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.ManageAndDeliverRestClient
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.dto.ServiceUserDto
import java.time.LocalDate

data class OffenderIdentifiersResponse(
  val crn: String,
  val nomsNumber: String?,
  val name: OffenderName,
  val dateOfBirth: String,
  val ethnicity: String?,
  val gender: String?,
  val probationDeliveryUnit: ProbationDeliveryUnit,
  val setting: String,
)

data class ProbationDeliveryUnit(
  val code: String?,
  val description: String,
)

data class OffenderName(
  val forename: String,
  val surname: String,
)

data class LimitedAccessOffenderCheckResponse(
  val crn: String,
  val userExcluded: Boolean,
  val userRestricted: Boolean,
  val exclusionMessage: String? = null,
  val restrictionMessage: String? = null,
)

@Service
class ServiceUserService(
  @Value("\${manage-and-deliver-and-delius.locations.find-person}") private val findPersonLocation: String,
  @Value("\${manage-and-deliver-and-delius.locations.limited-access-offender-check}") private val laocLocation: String,

  private val manageAndDeliverApiClient: ManageAndDeliverRestClient
) {
  fun getServiceUserByIdentifier(identifier: String) : ServiceUserDto {
    val offenderIdentifiersPath = UriComponentsBuilder.fromPath(findPersonLocation)
      .buildAndExpand(identifier)
      .toString()
    return manageAndDeliverApiClient.get(offenderIdentifiersPath)
      .retrieve()
      .bodyToMono(OffenderIdentifiersResponse::class.java)
      .block().let { it ->
        it?.let { it ->
          ServiceUserDto(
            name = it.name.forename + " " + it.name.surname,
            crn = it.crn,
            dob = LocalDate.parse(it.dateOfBirth),
            gender = it.gender,
            ethnicity = it.ethnicity,
            currentPdu = it.probationDeliveryUnit.code,
            setting = it.setting,
          )
        }
      }
  }

  fun checkIfAuthenticatedDeliusUserHasAccessToServiceUser(username: String, identifier: String): Boolean {
    val laocPath = UriComponentsBuilder.fromPath(laocLocation)
      .buildAndExpand(username, identifier)
      .toString()

    val limitedAccessOffenderCheckResponse = manageAndDeliverApiClient.get(laocPath)
      .retrieve()
      .bodyToMono(LimitedAccessOffenderCheckResponse::class.java)
      .block()

    return limitedAccessOffenderCheckResponse?.let {
      !it.userExcluded && !it.userRestricted
    } ?: false
  }
}