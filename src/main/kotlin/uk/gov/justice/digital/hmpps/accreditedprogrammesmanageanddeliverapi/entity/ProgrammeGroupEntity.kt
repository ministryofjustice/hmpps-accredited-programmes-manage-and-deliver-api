package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity

import com.fasterxml.jackson.annotation.JsonFormat
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import jakarta.validation.constraints.NotNull
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import org.springframework.security.core.context.SecurityContextHolder
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.OffenceCohort
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.type.ProgrammeGroupSexEnum
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "programme_group")
class ProgrammeGroupEntity(
  @Id
  @GeneratedValue
  @Column(name = "id")
  var id: UUID? = null,

  @NotNull
  @Column(name = "code")
  var code: String,

  @NotNull
  @Column(name = "cohort")
  @Enumerated(EnumType.STRING)
  var cohort: OffenceCohort,

  @NotNull
  @Column("sex")
  @Enumerated(EnumType.STRING)
  var sex: ProgrammeGroupSexEnum,

  @NotNull
  @Column(name = "is_ldc")
  var isLdc: Boolean,

  @NotNull
  @Column(name = "created_at")
  @CreatedDate
  @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
  var createdAt: LocalDateTime = LocalDateTime.now(),

  @NotNull
  @Column(name = "created_by_username")
  @CreatedBy
  var createdByUsername: String? = SecurityContextHolder.getContext().authentication?.name ?: "UNKNOWN_USER",

  @Column(name = "updated_at")
  @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
  var updatedAt: LocalDateTime? = null,

  @Column(name = "updated_by_username")
  var updatedByUsername: String? = null,

  @Column(name = "deleted_at")
  @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
  var deletedAt: LocalDateTime? = null,

  @Column(name = "deleted_by_username")
  var deletedByUsername: String? = null,

  @NotNull
  @Column(name = "region_name")
  var regionName: String,

  @Column(name = "delivery_location_name")
  var deliveryLocationName: String? = null,

  @Column(name = "delivery_location_code")
  var deliveryLocationCode: String? = null,

  @Column(name = "probation_delivery_unit_name")
  var probationDeliveryUnitName: String? = null,

  @Column(name = "probation_delivery_unit_code")
  var probationDeliveryUnitCode: String? = null,

  @Column(name = "earliest_possible_start_date")
  var earliestPossibleStartDate: LocalDate? = null,

  @Column(name = "started_at_date")
  var startedAtDate: LocalDate? = null,

  @OneToMany(
    fetch = FetchType.LAZY,
    cascade = [CascadeType.ALL],
    mappedBy = "programmeGroup",
  )
  var programmeGroupSessionSlots: MutableSet<ProgrammeGroupSessionSlotEntity> = mutableSetOf(),
)
