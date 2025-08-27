package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity

import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import jakarta.validation.constraints.NotNull
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import java.time.LocalDateTime

/**
 * A Probation Delivery Unit (PDU) is something smaller than a Region, useful for the management of Probation across the UK.
 * e.g. "Bristol PDU", "East Kent"
 *
 * @property pduId This is the business key and primary identifier, initially specified in the data created by BAs.
 * @property pduName The human-readable name of the PDU (e.g., "East Kent")
 * @property probationRegionId The foreign key reference to the parent region
 * @property ndeliusPduCode The (possibly empty) code used to identify this PDU by nDelius
 * @property createdAt Timestamp when this record was created (default to now)
 * @property updatedAt Timestamp when this record was last updated (default to now)
 * @property deletedAt (Nullable) timestamp when this record was soft-deleted.  If null, not deleted.
 * @property region The parent region containing this PDU
 * @property offices List of offices within this PDU
 */
@Entity
@Table(name = "pdu")
class PduEntity(
  @Id
  @Column(name = "pdu_id")
  @NotNull
  val pduId: Int,

  @Column(name = "pdu_name", length = 255)
  @NotNull
  val pduName: String,

  @Column(name = "probation_region_id", length = 10)
  @NotNull
  val probationRegionId: String,

  @Column(name = "ndelius_pdu_code", length = 50)
  val ndeliusPduCode: String? = null,

  @Column(name = "created_at", nullable = false)
  @CreatedDate
  val createdAt: LocalDateTime = LocalDateTime.now(),

  @Column(name = "updated_at", nullable = false)
  @LastModifiedDate
  var updatedAt: LocalDateTime = LocalDateTime.now(),

  @Column(name = "deleted_at")
  var deletedAt: LocalDateTime? = null,

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "probation_region_id", insertable = false, updatable = false)
  val region: RegionEntity? = null,

  @OneToMany(
    mappedBy = "pdu",
    cascade = [CascadeType.ALL],
    fetch = FetchType.LAZY,
  )
  val offices: MutableList<OfficeEntity> = mutableListOf(),
)
