package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDateTime
import java.util.*

@Entity
@Table(name = "referral_status_history")
class ReferralStatusHistoryEntity(
  @Id
  @GeneratedValue
  @Column(name = "id")
  var id: UUID? = null,

  @Column(name = "status")
  var status: String? = null,

  @Column(name = "created_at")
  var createdAt: LocalDateTime = LocalDateTime.now(),

  @Column(name = "created_by")
  var createdBy: String? = null,

  @Column(name = "start_date")
  var startDate: LocalDateTime? = null,

  @Column(name = "end_date")
  var endDate: LocalDateTime? = null,
)
