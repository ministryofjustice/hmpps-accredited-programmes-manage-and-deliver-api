package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.util.UUID

@Entity
@Table(name = "referral_status_description")
class ReferralStatusDescriptionEntity(
  @Id
  @Column(name = "id")
  val id: UUID,

  @Column(name = "description_text")
  val description: String,

  @Column(name = "is_closed")
  val isClosed: Boolean,

  @Column(name = "label_colour")
  val labelColour: String? = null,
)
