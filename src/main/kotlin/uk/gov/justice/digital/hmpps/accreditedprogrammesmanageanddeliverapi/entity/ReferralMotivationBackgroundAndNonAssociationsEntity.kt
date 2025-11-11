package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import jakarta.validation.constraints.NotNull
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.security.core.context.SecurityContextHolder
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "referral_motivation_background_and_non_associations")
class ReferralMotivationBackgroundAndNonAssociationsEntity(
  @Id
  @GeneratedValue
  @Column(name = "id")
  var id: UUID? = null,

  @NotNull
  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "referral_id")
  open var referral: ReferralEntity,

  @Column(name = "maintains_innocence")
  var maintainsInnocence: Boolean? = null,

  @Column(name = "motivations")
  var motivations: String? = null,

  @Column(name = "non_associations")
  var nonAssociations: String? = null,

  @Column(name = "other_considerations")
  var otherConsiderations: String? = null,

  @NotNull
  @Column(name = "created_by")
  @CreatedBy
  var createdBy: String = SecurityContextHolder.getContext().authentication?.name ?: "UNKNOWN_USER",

  @NotNull
  @Column(name = "created_at")
  @CreatedDate
  var createdAt: LocalDateTime = LocalDateTime.now(),

  @Column(name = "last_updated_by")
  @LastModifiedBy
  var lastUpdatedBy: String? = null,

  @Column(name = "last_updated_at")
  var lastUpdatedAt: LocalDateTime? = null,
)
