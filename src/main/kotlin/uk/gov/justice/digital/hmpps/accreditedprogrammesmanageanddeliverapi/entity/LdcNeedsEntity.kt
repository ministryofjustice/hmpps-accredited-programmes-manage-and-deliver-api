package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import java.util.UUID

@Entity
@Table(name = "ldc_needs")
class LdcNeedsEntity(
  @Id
  @GeneratedValue
  var id: UUID? = null,

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "referral_id", nullable = false, unique = true)
  var referral: ReferralEntity,

  @Column(name = "has_ldc_needs", nullable = false)
  var hasLdcNeeds: Boolean,

  @Column(name = "overridden", nullable = false)
  var overridden: Boolean = false,
)
