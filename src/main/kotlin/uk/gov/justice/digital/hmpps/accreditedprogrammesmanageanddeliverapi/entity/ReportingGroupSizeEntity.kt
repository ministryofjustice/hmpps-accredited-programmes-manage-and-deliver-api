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
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.OffenceCohort
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.type.ProgrammeGroupSexEnum
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Immutable
@Table(name = "reporting_group_size")
class ReportingGroupSizeEntity(
  @NotNull
  @Id
  @Column(name = "id")
  var id: UUID,

  @NotNull
  @Column(name = "code")
  var code: String,

  @NotNull
  @Column(name = "created_at")
  var createdAt: LocalDateTime,

  @NotNull
  @Column("sex")
  @Enumerated(EnumType.STRING)
  var sex: ProgrammeGroupSexEnum,

  @NotNull
  @Column(name = "cohort")
  @Enumerated(EnumType.STRING)
  var cohort: OffenceCohort,

  @NotNull
  @Column(name = "is_ldc")
  var isLdc: Boolean,

  @NotNull
  @Column(name = "earliest_possible_start_date")
  var earliestPossibleStartDate: LocalDate,

  @NotNull
  @Column(name = "region_name")
  var regionName: String,

  @NotNull
  @Column(name = "pdu_code")
  var pduCode: String,

  @NotNull
  @Column(name = "pdu_name")
  var pduName: String,

  @NotNull
  @Column(name = "location_code")
  var locationCode: String,

  @NotNull
  @Column(name = "location_name")
  var locationName: String,

  @NotNull
  @Column(name = "group_size")
  var groupSize: Int,

  @Nullable
  @Column(name = "facilitator_staff_code")
  var facilitatorStaffCode: String? = null,
)
