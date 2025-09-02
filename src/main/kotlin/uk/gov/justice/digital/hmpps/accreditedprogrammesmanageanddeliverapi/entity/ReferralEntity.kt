package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity

import jakarta.annotation.Nullable
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.JoinTable
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import jakarta.validation.constraints.NotNull
import org.springframework.data.annotation.CreatedDate
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.OffenceCohort
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.type.InterventionType
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.type.SettingType
import java.time.LocalDateTime
import java.util.*

@Entity
@Table(name = "referral")
class ReferralEntity(
  @Id
  @GeneratedValue
  @Column(name = "id")
  var id: UUID? = null,

  @Column(name = "person_name")
  var personName: String,

  @NotNull
  @Column(name = "crn")
  var crn: String,

  @NotNull
  @Column(name = "intervention_type")
  @Enumerated(EnumType.STRING)
  var interventionType: InterventionType,

  @Column(name = "intervention_name")
  var interventionName: String? = null,

  @NotNull
  @Column(name = "setting")
  @Enumerated(EnumType.STRING)
  var setting: SettingType,

  @NotNull
  @Column(name = "cohort")
  @Enumerated(EnumType.STRING)
  var cohort: OffenceCohort,

  @Column(name = "created_at")
  @CreatedDate
  var createdAt: LocalDateTime = LocalDateTime.now(),

  @OneToMany(
    fetch = FetchType.LAZY,
    cascade = [CascadeType.ALL],
    orphanRemoval = true,
  )
  @JoinTable(
    name = "referral_status_history_mapping",
    joinColumns = [JoinColumn(name = "referral_id")],
    inverseJoinColumns = [JoinColumn(name = "referral_status_history_id")],
  )
  var statusHistories: MutableList<ReferralStatusHistoryEntity> = mutableListOf(),

  @Nullable
  @Column("sourced_from")
  var sourcedFrom: ReferralEntitySourcedFrom? = null,

  //  This is an alias to the sourced_from_id, i.e. the Requirement or Licence ID
  @Nullable
  @Column("event_id")
  val eventId: String? = null,

  //  The Delius event number representing a specific court case event
  @Nullable
  @Column("event_number")
  val eventNumber: Int? = null,
)

enum class ReferralEntitySourcedFrom {
  REQUIREMENT,
  LICENSE_CONDITION,
}
