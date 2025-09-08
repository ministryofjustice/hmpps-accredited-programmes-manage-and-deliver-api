package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity

import jakarta.annotation.Nonnull
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import jakarta.validation.constraints.NotNull
import java.util.UUID

/**
 * A PreferredDeliveryLocation represents a geographical place where a Person on Probation can attend
 * an Accredited Programme.  Initially, these are likely to be "Offices" returned from nDelius
 * (via the Person on Probation -> Licence OR Requirement -> Manager link, which will be sourced via the
 * nDelius API Integration Client.
 * In the future they may include non-office locations (e.g. a Church Hall).
 * A Delivery Location Preferences belongs to a Referral, but a Referral can have 0 DeliveryLocationPreferences
 * A Delivery Location Preferences has many Preferred Delivery Locations
 */
@Entity
@Table(name = "preferred_delivery_location")
class PreferredDeliveryLocation(
  @Id
  @Column(name = "id")
  val id: UUID,

  @Column(name = "delius_code")
  val deliusCode: String,

  @Column(name = "delius_description")
  val deliusDescription: String,

  @Nonnull
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "delivery_location_preferences_id")
  var deliveryLocationPreferences: DeliveryLocationPreferenceEntity? = null,

  @NotNull
  @ManyToOne(fetch = FetchType.LAZY, cascade = [CascadeType.ALL])
  @JoinColumn(
    name = "preferred_delivery_location_probation_delivery_unit_id",
    referencedColumnName = "id",
  ) var preferredDeliveryLocationProbationDeliveryUnit: PreferredDeliveryLocationProbationDeliveryUnit,
)
