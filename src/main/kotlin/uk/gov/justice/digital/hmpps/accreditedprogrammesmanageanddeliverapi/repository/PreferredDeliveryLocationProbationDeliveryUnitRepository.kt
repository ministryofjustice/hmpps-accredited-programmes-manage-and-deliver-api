package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository

import jakarta.transaction.Transactional
import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.PreferredDeliveryLocationProbationDeliveryUnitEntity
import java.util.UUID

interface PreferredDeliveryLocationProbationDeliveryUnitRepository : JpaRepository<PreferredDeliveryLocationProbationDeliveryUnitEntity, UUID> {

  @Transactional
  fun findByDeliusCode(deliusCode: String): PreferredDeliveryLocationProbationDeliveryUnitEntity?
}
