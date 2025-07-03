package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.util.UriComponentsBuilder
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.ManageAndDeliverRestClient
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.dto.ReferralDto
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

@Service
class ReferralService(
  @Value("\${manage-and-deliver-and-delius.locations.find-person}") private val findPersonLocation: String,

  private val manageAndDeliverApiClient: ManageAndDeliverRestClient
) {
  fun getReferralById(identifier: String) : ReferralDto {
    val offenderIdentifiersPath = UriComponentsBuilder.fromPath(findPersonLocation)
      .buildAndExpand(identifier)
      .toString()
    val serviceUser = manageAndDeliverApiClient.get(offenderIdentifiersPath)
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
    return ReferralDto(serviceUser = serviceUser)
  }
}