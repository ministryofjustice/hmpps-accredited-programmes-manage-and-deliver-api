package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "referral_status_transition")
class ReferralStatusTransitionEntity(
  @Id
  @Column(name = "id")
  val id: UUID,

  @ManyToOne
  @JoinColumn(name = "from_status")
  val fromStatus: ReferralStatusDescriptionEntity,

  @ManyToOne
  @JoinColumn(name = "to_status")
  val toStatus: ReferralStatusDescriptionEntity,

  @Column(name = "description")
  val description: String?,

  @Column(name = "is_continuing")
  val isContinuing: Boolean = false,

  @Column(name = "created_at")
  val createdAt: LocalDateTime? = null,

  @Column(name = "updated_at")
  val updatedAt: LocalDateTime? = null,

  @Column(name = "deleted_at")
  val deletedAt: LocalDateTime? = null,

  @Column(name = "is_visible")
  val isVisible: Boolean = true,

)
