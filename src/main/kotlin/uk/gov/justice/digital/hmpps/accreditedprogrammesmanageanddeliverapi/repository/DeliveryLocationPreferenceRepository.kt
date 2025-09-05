package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository

import org.springframework.data.repository.Repository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.DeliveryLocationPreferenceEntity
import java.util.UUID

interface DeliveryLocationPreferenceRepository : Repository<DeliveryLocationPreferenceEntity, UUID> {
  fun findByReferralId(referralId: UUID): MutableList<DeliveryLocationPreferenceEntity>
}
