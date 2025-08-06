package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.validation.constraints.NotNull
import org.hibernate.annotations.Immutable
import java.util.UUID

@Entity
@Immutable
@Table(name = "referral_caselist_item_view")
class ReferralCaseListItemViewEntity(
  @NotNull
  @Id
  @Column(name = "id")
  val referralId: UUID,

  @NotNull
  @Column(name = "crn")
  var crn: String,

  @NotNull
  @Column(name = "person_name")
  var personName: String,

  @NotNull
  @Column(name = "status")
  var status: String,

  @NotNull
  @Column(name = "cohort")
  var cohort: String,
)
