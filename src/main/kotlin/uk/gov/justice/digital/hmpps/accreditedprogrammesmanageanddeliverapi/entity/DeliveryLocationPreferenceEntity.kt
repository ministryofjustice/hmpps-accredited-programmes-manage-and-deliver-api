package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity

import jakarta.annotation.Nullable
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EntityListeners
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.JoinTable
import jakarta.persistence.ManyToMany
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import jakarta.validation.constraints.NotNull
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import org.springframework.security.core.context.SecurityContextHolder
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "delivery_location_preferences")
@EntityListeners(AuditingEntityListener::class)
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
  @CreatedBy
  val createdBy: String? = SecurityContextHolder.getContext().authentication?.name ?: "UNKNOWN_USER",

  @NotNull
  @CreatedDate
  val createdAt: LocalDateTime? = LocalDateTime.now(),

  @NotNull
  @Column(name = "last_updated_at")
  @LastModifiedDate
  val lastUpdatedAt: LocalDateTime? = LocalDateTime.now(),

  @Nullable
  @Column(name = "locations_cannot_attend_text")
  val locationsCannotAttendText: String? = null,
)
