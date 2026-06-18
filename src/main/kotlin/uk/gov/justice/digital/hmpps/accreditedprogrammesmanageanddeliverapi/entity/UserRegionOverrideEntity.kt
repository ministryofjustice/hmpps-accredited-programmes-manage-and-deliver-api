package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.validation.constraints.NotNull
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "user_region_override")
class UserRegionOverrideEntity(
  @Id
  @GeneratedValue
  @Column(name = "id")
  var id: UUID? = null,

  @NotNull
  @Column(name = "username")
  var username: String,

  @NotNull
  @Column(name = "region_name")
  var regionName: String,

  @Column(name = "created_at")
  var createdAt: LocalDateTime? = null,

  @Column(name = "created_by")
  var createdBy: String? = null,

  @Column(name = "deleted_at")
  var deletedAt: LocalDateTime? = null,

  @Column(name = "deleted_by")
  var deletedBy: String? = null,
)
