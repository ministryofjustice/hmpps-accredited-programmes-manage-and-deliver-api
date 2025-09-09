package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service

import jakarta.transaction.Transactional
import org.slf4j.LoggerFactory
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.DeliveryLocationOption
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.DeliveryLocationPreferencesFormData
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.ExistingDeliveryLocationPreferences
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.PersonOnProbationSummary
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.ProbationDeliveryUnit
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.deliveryLocationPreferences.CreateDeliveryLocationPreferences
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.deliveryLocationPreferences.PreferredDeliveryLocations
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

  @Transactional
  fun createDeliveryLocationPreferences(
    referralId: UUID,
    createDeliveryLocationPreferences: CreateDeliveryLocationPreferences,
  ): DeliveryLocationPreferenceEntity {
    deliveryLocationPreferenceRepository.findByReferralId(referralId)
      ?.let {
        throw ConflictException(
          "A delivery location preferences for this referral already exists",
        )
      }
    val referral =
      referralRepository.findByIdOrNull(referralId) ?: throw NotFoundException("No referral found with id: $referralId")
    val preferredDeliveryLocations =
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

    val deliveryLocationPreferencesEntity =
      createDeliveryLocationPreferences.toEntity(referral, preferredDeliveryLocations)
    return deliveryLocationPreferenceRepository.save(deliveryLocationPreferencesEntity)
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

  private fun findOrCreateProbationDeliveryUnit(
    preferredLocations: PreferredDeliveryLocations,
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
