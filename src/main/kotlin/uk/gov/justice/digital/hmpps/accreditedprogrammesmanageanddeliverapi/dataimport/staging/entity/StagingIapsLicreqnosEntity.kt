package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.dataimport.staging.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import jakarta.validation.constraints.NotNull

/**
 * Reference entities which map an nDelius Licence Condition or Requirement number (licreqno)
 * to a source referral ID.
 *
 * This data has been manually exported in coordination with the IAPS team.
 * If you need support with it, please speak with Wilson, who has been coordinating with them.
 *
 * It is only necessary for Referrals before February 2026, because the production system.
 * has since been configured to pick up Referrals, and auto-populate their licence/requirement numbers,
 * directly from the incoming event.
 * Therefor this data will be loaded all at once, and won't be updated incrementally with the other data.
 */
@Entity
@Table(name = "iaps_licreqnos", schema = "im_data_import")
class StagingIapsLicreqnosEntity(
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  var id: Long? = null,

  @NotNull
  @Column(name = "source_referral_id")
  var sourceReferralId: String,

  @NotNull
  @Column(name = "licreqno")
  var licreqno: String,

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "source_referral_id", insertable = false, updatable = false)
  var referral: StagingReferralEntity? = null,
)
