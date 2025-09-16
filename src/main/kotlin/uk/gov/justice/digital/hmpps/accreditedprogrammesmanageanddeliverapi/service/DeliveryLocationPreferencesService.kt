package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service

import jakarta.transaction.Transactional
import org.slf4j.LoggerFactory
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.DeliveryLocationOption
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.DeliveryLocationPreferences
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.DeliveryLocationPreferencesFormData
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.ExistingDeliveryLocationPreferences
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.PersonOnProbationSummary
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.ProbationDeliveryUnit
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.deliveryLocationPreferences.CreateDeliveryLocationPreferences
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.deliveryLocationPreferences.PreferredDeliveryLocation
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.toModel
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.NDeliusApiProbationDeliveryUnitWithOfficeLocations
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common.exception.ConflictException
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common.exception.NotFoundException
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.DeliveryLocationPreferenceEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.PreferredDeliveryLocationEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.PreferredDeliveryLocationProbationDeliveryUnitEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.toEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.DeliveryLocationPreferenceRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.PreferredDeliveryLocationProbationDeliveryUnitRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.PreferredDeliveryLocationRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ReferralRepository
import java.util.UUID

@Service
@Transactional
class DeliveryLocationPreferencesService(
  private val deliveryLocationPreferenceRepository: DeliveryLocationPreferenceRepository,
  private val referralService: ReferralService,
  private val serviceUserService: ServiceUserService,
  private val preferredDeliveryLocationProbationDeliveryUnitRepository: PreferredDeliveryLocationProbationDeliveryUnitRepository,
  private val preferredDeliveryLocationRepository: PreferredDeliveryLocationRepository,
  private val referralRepository: ReferralRepository,
) {

  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }

  fun createDeliveryLocationPreferences(
    referralId: UUID,
    createDeliveryLocationPreferences: CreateDeliveryLocationPreferences,
  ): DeliveryLocationPreferenceEntity {
    deliveryLocationPreferenceRepository.findByReferralId(referralId)
      ?.let {
        throw ConflictException(
          "A DeliveryLocationPreferences for this referral with id: $referralId already exists",
        )
      }
    val referral =
      referralRepository.findByIdOrNull(referralId) ?: throw NotFoundException("No referral found with id: $referralId")
    val preferredDeliveryLocations =
      createOrUpdateDeliveryLocations(createDeliveryLocationPreferences, referralId)

    val deliveryLocationPreferencesEntity =
      createDeliveryLocationPreferences.toEntity(referral, preferredDeliveryLocations)
    return deliveryLocationPreferenceRepository.save(deliveryLocationPreferencesEntity)
  }

  fun updateDeliveryLocationPreferences(
    referralId: UUID,
    updateDeliveryLocationPreferences: CreateDeliveryLocationPreferences,
  ) {
    val existingDeliveryLocationPreferenceEntity = deliveryLocationPreferenceRepository.findByReferralId(referralId)
      ?: throw NotFoundException("No DeliveryLocationPreferences found for referral with id: $referralId")

    // Update the existing entity for an updated locationsCannotAttendText value if it has been provided
    existingDeliveryLocationPreferenceEntity.locationsCannotAttendText =
      updateDeliveryLocationPreferences.cannotAttendText?.takeIf { it.isNotBlank() }

    // Clear existing preferred locations and add new ones
    existingDeliveryLocationPreferenceEntity.preferredDeliveryLocations.clear()

    val updatedPreferredDeliveryLocations =
      createOrUpdateDeliveryLocations(updateDeliveryLocationPreferences, referralId)

    existingDeliveryLocationPreferenceEntity.preferredDeliveryLocations.addAll(updatedPreferredDeliveryLocations)

    // Save the existing entity, so UPDATE rather than INSERT)
    deliveryLocationPreferenceRepository.save(existingDeliveryLocationPreferenceEntity)
  }

  private fun createOrUpdateDeliveryLocations(
    createDeliveryLocationPreferences: CreateDeliveryLocationPreferences,
    referralId: UUID,
  ): MutableSet<PreferredDeliveryLocationEntity> {
    val updatedPreferredDeliveryLocations =
      createDeliveryLocationPreferences.preferredDeliveryLocations.flatMap { preferredLocations ->

        val probationDeliveryUnit = findOrCreateProbationDeliveryUnit(preferredLocations)

        val preferredDeliveryLocationEntities: List<PreferredDeliveryLocationEntity> =
          preferredLocations.deliveryLocations.map { deliveryLocation ->
            PreferredDeliveryLocationEntity(
              deliusCode = deliveryLocation.code,
              deliusDescription = deliveryLocation.description,
              preferredDeliveryLocationProbationDeliveryUnit = probationDeliveryUnit,
            )
          }
        log.info("Saving ${preferredDeliveryLocationEntities.size} preferred delivery locations for referral with id: $referralId")
        preferredDeliveryLocationRepository.saveAll(preferredDeliveryLocationEntities)
      }.toMutableSet()
    return updatedPreferredDeliveryLocations
  }

  fun getDeliveryLocationPreferencesFormDataForReferral(referralId: UUID): DeliveryLocationPreferencesFormData {
    log.info("Getting delivery location preferences form data for referral: $referralId")

    val referral = referralService.getReferralById(referralId)
      ?: throw NotFoundException("Referral not found id $referralId")

    val personalDetails = serviceUserService.getPersonalDetailsByIdentifier(referral.crn)

    val existingPreferences = deliveryLocationPreferenceRepository.findByReferralId(referralId)

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
        code = managerDetails.probationDeliveryUnit.code,
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

  fun getPreferredDeliveryLocationsForReferral(referralId: UUID): DeliveryLocationPreferences {
    val referral = referralService.getReferralById(referralId)
      ?: throw NotFoundException("Referral with referralId $referralId not found")
    // Create empty delivery location preferences if there is no existing preferences
    return referral.deliveryLocationPreferences?.toModel() ?: DeliveryLocationPreferences()
  }

  private fun findOrCreateProbationDeliveryUnit(
    preferredLocations: PreferredDeliveryLocation,
  ): PreferredDeliveryLocationProbationDeliveryUnitEntity = preferredDeliveryLocationProbationDeliveryUnitRepository
    .findByDeliusCode(preferredLocations.pduCode)
    ?: run {
      log.info("Inserting new probation delivery unit with code: ${preferredLocations.pduCode} as it does not exist")
      val pduEntity = PreferredDeliveryLocationProbationDeliveryUnitEntity(
        deliusCode = preferredLocations.pduCode,
        deliusDescription = preferredLocations.pduDescription,
      )
      preferredDeliveryLocationProbationDeliveryUnitRepository.save(pduEntity)
    }

  private fun NDeliusApiProbationDeliveryUnitWithOfficeLocations.toProbationDeliveryUnit(): ProbationDeliveryUnit = ProbationDeliveryUnit(
    name = this.description,
    code = this.code,
    deliveryLocations = this.officeLocations.map { office ->
      DeliveryLocationOption(
        value = office.code,
        label = office.description,
      )
    },
  )
}
