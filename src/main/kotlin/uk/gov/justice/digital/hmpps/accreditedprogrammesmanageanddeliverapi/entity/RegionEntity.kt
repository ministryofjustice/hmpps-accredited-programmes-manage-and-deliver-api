package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity

import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import jakarta.validation.constraints.NotNull
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import java.time.LocalDateTime

/**
 * Entity representing a probation region, a large area of the UK.
 * e.g. "North East", "South West"
 *
 * @property probationRegionId The unique identifier for the region (e.g., "A", "K"). This is the business key and primary identifier.
 * @property regionName The human-readable name of the region (e.g., "North East")
 * @property createdAt Timestamp when this record was created (defaults to now)
 * @property updatedAt Timestamp when this record was last updated (defaults to now)
 * @property deletedAt nullable timestamp when this record was soft-deleted.  If null, not deleted
 * @property pdus List of PDUs within this region
 */
@Entity
@Table(name = "region")
class RegionEntity(
  @Id
  @Column(name = "id", length = 10)
  @NotNull
  val probationRegionId: String,

  @Column(name = "region_name", length = 255)
  @NotNull
  val regionName: String,

  @Column(name = "created_at", nullable = false)
  @CreatedDate
  val createdAt: LocalDateTime = LocalDateTime.now(),

  @Column(name = "updated_at", nullable = false)
  @LastModifiedDate
  var updatedAt: LocalDateTime = LocalDateTime.now(),

  @Column(name = "deleted_at")
  var deletedAt: LocalDateTime? = null,

  @OneToMany(
    mappedBy = "region",
    cascade = [CascadeType.ALL],
    fetch = FetchType.LAZY,
  )
  val pdus: MutableList<PduEntity> = mutableListOf(),
)
