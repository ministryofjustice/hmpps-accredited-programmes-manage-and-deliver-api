package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory

import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.dataimport.staging.entity.StagingIapsLicreqnosEntity

class StagingIapsLicreqnoEntityFactory {
  private var id: Long? = null
  private var sourceReferralId: String = "LS-REF-001"
  private var licreqno: String = "LIC-001"

  fun withId(id: Long?) = apply { this.id = id }
  fun withSourceReferralId(sourceReferralId: String) = apply { this.sourceReferralId = sourceReferralId }
  fun withLicreqno(licreqno: String) = apply { this.licreqno = licreqno }

  fun produce() = StagingIapsLicreqnosEntity(
    id = this.id,
    sourceReferralId = this.sourceReferralId,
    licreqno = this.licreqno,
  )
}
