package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity

import jakarta.annotation.Nullable
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.validation.constraints.NotNull
import org.hibernate.annotations.Immutable
import java.time.LocalDate
import java.util.UUID

@Entity
@Immutable
@Table(name = "referral_caselist_item_view")
class ReferralCaseListItemViewEntity(
  @NotNull
  @Id
  @Column(name = "id")
  var referralId: UUID,

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

  @NotNull
  @Column(name = "has_ldc")
  var hasLdc: Boolean,

  @NotNull
  @Column(name = "pdu_name")
  var pduName: String,

  @NotNull
  @Column(name = "reporting_team")
  var reportingTeam: String,

  @NotNull
  @Column(name = "region_name")
  var regionName: String,

  @Nullable
  @Column(name = "sentence_end_date")
  var sentenceEndDate: LocalDate? = null,

  @Nullable
  @Enumerated(EnumType.STRING)
  @Column(name = "sentence_end_date_source")
  var sentenceEndDateSource: ReferralEntitySourcedFrom,
)
