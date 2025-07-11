package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity

import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.time.DayOfWeek
import java.util.UUID

@Entity
@Table(name = "slot")
class SlotEntity(
  @Id
  @GeneratedValue
  var id: UUID? = null,

  @Enumerated(EnumType.STRING)
  @Column(name = "day_of_week", nullable = false)
  val dayOfWeek: DayOfWeek,

  @Enumerated(EnumType.STRING)
  @Column(name = "slot_name", nullable = false)
  val slotName: SlotName,

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "availability_id", nullable = false)
  @JsonIgnore
  var availability: AvailabilityEntity,
)

enum class SlotName(val order: Int, val displayName: String) {
  DAYTIME(10, "daytime"),
  EVENING(20, "evening"),
}
