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
@Table(name = "group_waitlist_item_view")
class GroupWaitlistItemViewEntity(
  @NotNull
  @Id
  @Column(name = "referral_id")
  var referralId: UUID,

  @NotNull
  @Column(name = "crn")
  var crn: String,

  @NotNull
  @Column(name = "person_name")
  var personName: String,

  @Nullable
  @Column(name = "sentence_end_date")
  var sentenceEndDate: LocalDate? = null,

  @Nullable
  @Enumerated(EnumType.STRING)
  @Column(name = "sourced_from")
  var sourcedFrom: ReferralEntitySourcedFrom? = null,

  @NotNull
  @Column(name = "cohort")
  var cohort: String,

  @NotNull
  @Column(name = "has_ldc")
  var hasLdc: Boolean,

  @Nullable
  @Column(name = "date_of_birth")
  var dateOfBirth: LocalDate? = null,

  @Nullable
  @Column(name = "sex")
  var sex: String? = null,

  @NotNull
  @Column(name = "status")
  var status: String,

  @NotNull
  @Column(name = "status_colour")
  var statusColour: String,

  @NotNull
  @Column(name = "pdu_name")
  var pduName: String,

  @NotNull
  @Column(name = "reporting_team")
  var reportingTeam: String,

  @Nullable
  @Column(name = "active_programme_group_id")
  var activeProgrammeGroupId: UUID? = null,
)
