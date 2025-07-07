package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import jakarta.validation.constraints.NotNull
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "referral_caselist_item")
class ReferralCaselistItemEntity(

  @NotNull
  @Id
  @Column("id")
  val id: UUID,

  @NotNull
  @OneToOne
  @JoinColumn(name = "referral_id", referencedColumnName = "id")
  var referral: ReferralEntity,

  @NotNull
  @Column(name = "last_updated_at")
  var lastUpdatedAt: LocalDateTime,

  @NotNull
  @Column(name = "crn", length = Integer.MAX_VALUE)
  var crn: String,

  @NotNull
  @Column(name = "person_name", length = Integer.MAX_VALUE)
  var personName: String,

  @NotNull
  @Column(name = "probation_office", length = Integer.MAX_VALUE)
  var probationOffice: String,

  @NotNull
  @Column(name = "sentence_end_date", length = Integer.MAX_VALUE)
  var sentenceEndDate: LocalDateTime,

  @Column(name = "pss_end_date", length = Integer.MAX_VALUE)
  var pssEndDate: LocalDateTime? = null,

  @NotNull
  @Column(name = "cohort", length = Integer.MAX_VALUE)
  var cohort: String,

  @NotNull
  @Column(name = "referral_status", length = Integer.MAX_VALUE)
  var referralStatus: String,
)
