package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service

import jakarta.transaction.Transactional
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.deliveryLocationPreferences.CreateDeliveryLocationPreferences
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.deliveryLocationPreferences.toEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.DeliveryLocationPreferenceEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.DeliveryLocationPreferenceRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.OfficeRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ReferralRepository
import java.util.UUID

@Service
class DeliveryLocationPreferencesService(
  private val deliveryLocationPreferenceRepository: DeliveryLocationPreferenceRepository,
  private val officeRepository: OfficeRepository,
  private val referralRepository: ReferralRepository,
) {
  private val log = LoggerFactory.getLogger(this::class.java)

  @Transactional
  fun createDeliveryLocationPreferences(
    referralId: UUID,
    createDeliveryLocationPreferences: CreateDeliveryLocationPreferences,
  ): DeliveryLocationPreferenceEntity {
    val offices = officeRepository.findAllByIdIn(createDeliveryLocationPreferences.preferredDeliveryLocationCode)
    val referral = referralRepository.findById(referralId).get()
    val deliveryLocationPreferencesEntity = createDeliveryLocationPreferences.toEntity(referral, offices)
    return deliveryLocationPreferenceRepository.save(deliveryLocationPreferencesEntity)
  }
}
