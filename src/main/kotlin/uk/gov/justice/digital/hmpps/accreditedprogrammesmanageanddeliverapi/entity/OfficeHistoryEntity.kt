package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity

import jakarta.persistence.*
import org.jetbrains.annotations.NotNull
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "office_history")
class OfficeHistoryEntity(
  @Id
  @GeneratedValue
  @NotNull
  @Column(name = "referral_id")
  var referral_id: UUID,

  @NotNull
  @Column(name = "office_name")
  var officeName: String,

  @NotNull
  @Column(name = "created_at")
  var createdAt: LocalDateTime,

  @NotNull
  @Column(name = "created_by_user")
  var createdByUser: String,

  @Column(name = "deleted_at")
  var deletedAt: LocalDateTime? = null,
)
