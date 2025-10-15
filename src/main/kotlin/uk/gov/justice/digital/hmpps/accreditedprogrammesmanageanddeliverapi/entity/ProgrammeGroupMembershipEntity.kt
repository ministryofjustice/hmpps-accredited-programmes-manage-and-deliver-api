package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import jakarta.validation.constraints.NotNull
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import org.springframework.security.core.context.SecurityContextHolder
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "programme_group_membership")
class ProgrammeGroupMembershipEntity(
  @Id
  @GeneratedValue
  @Column(name = "id")
  var id: UUID? = null,

  @NotNull
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "referral_id")
  var referral: ReferralEntity,

  @NotNull
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "programme_group_id")
  var programmeGroup: ProgrammeGroupEntity,

  @NotNull
  @Column(name = "created_at")
  @CreatedDate
  var createdAt: LocalDateTime = LocalDateTime.now(),

  @NotNull
  @Column(name = "created_by_username")
  @CreatedBy
  var createdByUsername: String? = SecurityContextHolder.getContext().authentication?.name ?: "UNKNOWN_USER",

  @Column(name = "deleted_at")
  var deletedAt: LocalDateTime? = null,

  @Column(name = "deleted_by_username")
  var deletedByUsername: String? = null,
)
