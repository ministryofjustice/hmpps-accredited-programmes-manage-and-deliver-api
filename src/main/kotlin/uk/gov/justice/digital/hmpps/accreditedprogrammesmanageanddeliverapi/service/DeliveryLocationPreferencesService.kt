package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.DeliveryLocationOption
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.DeliveryLocationPreferencesFormData
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.ExistingDeliveryLocationPreferences
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.PersonOnProbationSummary
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.ProbationDeliveryUnit
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.NDeliusApiProbationDeliveryUnitWithOfficeLocations
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common.exception.NotFoundException
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.DeliveryLocationPreferenceRepository
import java.util.UUID

@Service
class DeliveryLocationPreferencesService(
  private val deliveryLocationPreferenceRepository: DeliveryLocationPreferenceRepository,
  private val referralService: ReferralService,
  private val serviceUserService: ServiceUserService,
) {

  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }

  fun getDeliveryLocationPreferencesFormDataForReferral(referralId: UUID): DeliveryLocationPreferencesFormData {
    log.info("Getting delivery location preferences form data for referral: $referralId")

    val referral = referralService.getReferralById(referralId)
      ?: throw NotFoundException("Referral not found id $referralId")

    val personalDetails = serviceUserService.getPersonalDetailsByIdentifier(referral.crn)

    val existingPreferences = deliveryLocationPreferenceRepository.findByReferralId(referralId).firstOrNull()

    val managerDetails = referralService.attemptToFindManagerForReferral(referralId)
      ?: throw NotFoundException("Could not retrieve manager details for referral $referralId")

    val additionalPdusData = referralService.attemptToFindNonPrimaryPdusForReferal(referralId)
      ?: throw NotFoundException("Could not find additional PDUS for referral $referralId")

    return DeliveryLocationPreferencesFormData(
      personOnProbation = PersonOnProbationSummary(
        name = "${personalDetails.name.forename} ${personalDetails.name.surname}",
        crn = personalDetails.crn,
        tier = personalDetails.probationDeliveryUnit?.description, // Using PDU description as tier for now
        dateOfBirth = java.time.LocalDate.parse(personalDetails.dateOfBirth),
      ),
      existingDeliveryLocationPreferences = existingPreferences?.let { preferences ->
        ExistingDeliveryLocationPreferences(
          canAttendLocationsValues = preferences.preferredDeliveryLocations.map { location ->
            DeliveryLocationOption(
              value = location.deliusCode,
              label = location.deliusDescription,
            )
          },
          cannotAttendLocations = preferences.locationsCannotAttendText,
        )
      },
      primaryPdu = ProbationDeliveryUnit(
        name = managerDetails.probationDeliveryUnit.description,
        deliveryLocations = managerDetails.officeLocations.map { office ->
          DeliveryLocationOption(
            value = office.code,
            label = office.description,
          )
        },
      ),
      otherPdusInSameRegion = additionalPdusData
        .filter { pdu: NDeliusApiProbationDeliveryUnitWithOfficeLocations -> pdu.code != managerDetails.probationDeliveryUnit.code }
        .map { pdu: NDeliusApiProbationDeliveryUnitWithOfficeLocations -> pdu.toProbationDeliveryUnit() },
    )
  }

  private fun NDeliusApiProbationDeliveryUnitWithOfficeLocations.toProbationDeliveryUnit(): ProbationDeliveryUnit = ProbationDeliveryUnit(
    name = this.description,
    deliveryLocations = this.officeLocations.map { office ->
      DeliveryLocationOption(
        value = office.code,
        label = office.description,
      )
    },
  )
}
