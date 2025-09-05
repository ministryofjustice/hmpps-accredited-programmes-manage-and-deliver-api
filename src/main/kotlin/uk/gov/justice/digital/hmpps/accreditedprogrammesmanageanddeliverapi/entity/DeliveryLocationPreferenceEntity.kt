package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity

import jakarta.annotation.Nullable
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.JoinTable
import jakarta.persistence.ManyToMany
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import jakarta.validation.constraints.NotNull
import org.springframework.data.annotation.CreatedDate
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "delivery_location_preferences")
class DeliveryLocationPreferenceEntity(
  @NotNull
  @Id
  @Column(name = "id")
  val id: UUID,

  @NotNull
  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "referral_id")
  val referral: ReferralEntity,

  @NotNull
  @Column(name = "created_by")
  val createdBy: String,

  @NotNull
  @CreatedDate
  var createdAt: LocalDateTime? = LocalDateTime.now(),

  @Nullable
  @Column(name = "locations_cannot_attend_text")
  val locationsCannotAttendText: String? = null,

  @ManyToMany
  @JoinTable(
    name = "delivery_location_office_mapping",
    joinColumns = [JoinColumn(name = "delivery_location_id")],
    inverseJoinColumns = [JoinColumn(name = "office_id")],
  )
  val offices: MutableSet<OfficeEntity> = mutableSetOf(),
)
