package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.util.UUID

@Entity
@Table(name = "referral_status_transition")
class ReferralStatusTransitionEntity(
  @Id
  @Column(name = "referral_status_transition_id")
  val id: UUID,

  @ManyToOne
  @JoinColumn(name = "transition_from_status")
  val fromStatus: ReferralStatusDescriptionEntity,

  @ManyToOne
  @JoinColumn(name = "transition_to_status")
  val toStatus: ReferralStatusDescriptionEntity,

  @Column(name = "description")
  val description: String?,

)
