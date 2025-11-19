package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import jakarta.validation.constraints.NotNull
import java.time.DayOfWeek
import java.time.LocalTime
import java.util.UUID

@Entity
@Table(name = "programme_group_session_slot")
class ProgrammeGroupSessionSlotEntity(
  @Id
  @GeneratedValue
  @Column(name = "id")
  var id: UUID? = null,

  @NotNull
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "programme_group_id")
  var programmeGroup: ProgrammeGroupEntity,

  @NotNull
  @Column(name = "day_of_week")
  var dayOfWeek: DayOfWeek,

  @NotNull
  @Column(name = "start_time")
  var startTime: LocalTime,
)
