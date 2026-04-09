package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EntityListeners
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import jakarta.validation.constraints.NotNull
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.DayOfWeek
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.UUID

@Entity
@Table(name = "programme_group_session_slot")
@EntityListeners(AuditingEntityListener::class)
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

  @NotNull
  @Column(name = "updated_at")
  @LastModifiedDate
  var updatedAt: LocalDateTime = LocalDateTime.now(),
)
