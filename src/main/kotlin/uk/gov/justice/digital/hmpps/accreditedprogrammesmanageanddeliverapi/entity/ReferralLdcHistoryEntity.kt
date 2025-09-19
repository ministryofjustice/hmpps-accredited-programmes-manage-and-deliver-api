package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EntityListeners
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import jakarta.validation.constraints.NotNull
import org.hibernate.annotations.ColumnDefault
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import org.springframework.security.core.context.SecurityContextHolder
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.ldc.UpdateLdc
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "referral_ldc_history")
@EntityListeners(AuditingEntityListener::class)
open class ReferralLdcHistoryEntity(
  @Id
  @GeneratedValue
  @Column(name = "id")
  open var id: UUID? = null,

  @NotNull
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "referral_id")
  open var referral: ReferralEntity,

  @NotNull
  @ColumnDefault("false")
  @Column(name = "has_ldc")
  open var hasLdc: Boolean,

  @NotNull
  @Column(name = "created_by")
  @CreatedBy
  open var createdBy: String? = SecurityContextHolder.getContext().authentication?.name ?: "UNKNOWN_USER",

  @NotNull
  @CreatedDate
  open var createdAt: LocalDateTime? = LocalDateTime.now(),
)

fun UpdateLdc.toEntity(referralEntity: ReferralEntity): ReferralLdcHistoryEntity = ReferralLdcHistoryEntity(
  referral = referralEntity,
  hasLdc = hasLdc,
)
