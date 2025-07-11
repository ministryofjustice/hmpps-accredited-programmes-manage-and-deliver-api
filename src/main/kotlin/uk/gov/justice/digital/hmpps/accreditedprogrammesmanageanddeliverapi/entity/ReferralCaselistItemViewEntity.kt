package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.validation.constraints.NotNull
import org.hibernate.annotations.Immutable

@Entity
@Immutable
@Table(name = "referral_caselist_item_view")
class ReferralCaselistItemViewEntity(
  @NotNull
  @Id
  @Column(name = "crn", length = Integer.MAX_VALUE)
  var crn: String,

  @NotNull
  @Column(name = "person_name", length = Integer.MAX_VALUE)
  var personName: String,

  @NotNull
  @Column(name = "status", length = Integer.MAX_VALUE)
  var status: String,
)
