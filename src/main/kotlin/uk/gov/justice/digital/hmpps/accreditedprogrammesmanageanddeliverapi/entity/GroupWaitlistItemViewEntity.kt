package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
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
  @Column(name = "id") var id: UUID,

  @NotNull
  @Column(name = "crn")
  var crn: String,

  @NotNull
  @Column(name = "person_name")
  var personName: String,

  @NotNull
  @Column(name = "sentence_end_date")
  var sentenceEndDate: LocalDate,

  @NotNull
  @Column(name = "cohort")
  var cohort: String,

  @NotNull
  @Column(name = "has_ldc")
  var hasLdc: Boolean,

  @NotNull
  @Column(name = "date_of_birth")
  var dateOfBirth: LocalDate,

  @NotNull
  @Column(name = "sex")
  var sex: String,

  @NotNull
  @Column(name = "status")
  var status: String,

  @NotNull
  @Column(name = "pdu_name")
  var pduName: String,

  @NotNull
  @Column(name = "reporting_team")
  var reportingTeam: String,
)
