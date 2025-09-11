package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.util.UUID

@Entity
@Table(name = "preferred_delivery_location_probation_delivery_unit")
class PreferredDeliveryLocationProbationDeliveryUnitEntity(
  @Id
  @GeneratedValue
  val id: UUID? = null,
  @Column(name = "delius_code")
  val deliusCode: String,
  @Column(name = "delius_description")
  val deliusDescription: String,
)
