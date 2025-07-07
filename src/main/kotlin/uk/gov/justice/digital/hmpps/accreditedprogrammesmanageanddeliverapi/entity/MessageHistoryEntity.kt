package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import org.springframework.data.annotation.CreatedDate
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "message_history")
open class MessageHistoryEntity(

  @Id
  @GeneratedValue
  @Column(name = "id")
  var id: UUID? = null,

  @Column(name = "event_type")
  var eventType: String,

  @Column(name = "detail_url")
  var detailUrl: String,

  @Column(name = "description")
  var description: String? = null,

  @Column(name = "occurred_at")
  var occurredAt: LocalDateTime? = null,

  @Column(name = "message")
  var message: String,

  @Column(name = "created_at")
  @CreatedDate
  var createdAt: LocalDateTime? = null,

  @OneToOne(fetch = FetchType.LAZY, optional = true)
  @JoinColumn(name = "referral_id")
  var referral: ReferralEntity? = null,
)
