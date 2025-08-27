package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import jakarta.validation.constraints.NotNull
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import java.time.LocalDateTime

/**
 * Entity representing a probation office, out of which an Accredited Programme might be delivered.
 * e.g. "Derby: Derwent Centre", "Bristol Central Office", is an Office.
 *
 * @property probationOfficeId The unique identifier for the office. This is the business key and primary identifier.
 * @property name The full name of the office (e.g., "Derby: Derwent Centre")
 * @property officeName The short name of the office (e.g., "Derwent Centre")
 * @property officeAddress The full (postal) address of the office
 * @property pduId The foreign key reference to the parent PDU
 * @property probationRegionId The foreign key reference to the region (redundant but included for data integrity)
 * @property deliusCrsLocationId Optional identifier used in Delius CRS system
 * @property isDeliveryOffice Whether this office delivers programmes/services
 * @property isReportingOffice Whether this office is used for reporting/supervision
 * @property createdAt Timestamp when this record was created
 * @property updatedAt Timestamp when this record was last updated
 * @property deletedAt Optional timestamp when this record was soft-deleted (null if active)
 * @property pdu The parent PDU containing this office
 * @property region The parent region containing this office
 */
@Entity
@Table(name = "office")
class OfficeEntity(
  @Id
  @Column(name = "probation_office_id", length = 20)
  @NotNull
  val probationOfficeId: String,

  @Column(name = "name", length = 500)
  @NotNull
  val name: String,

  @Column(name = "office_name", length = 255)
  @NotNull
  val officeName: String,

  @Column(name = "office_address", length = 1000)
  val officeAddress: String? = null,

  @Column(name = "pdu_id")
  @NotNull
  val pduId: Int,

  @Column(name = "probation_region_id", length = 10)
  @NotNull
  val probationRegionId: String,

  @Column(name = "delius_crs_location_id", length = 50)
  val deliusCrsLocationId: String? = null,

  @Column(name = "is_delivery_office")
  @NotNull
  val isDeliveryOffice: Boolean = false,

  @Column(name = "is_reporting_office")
  @NotNull
  val isReportingOffice: Boolean = false,

  @Column(name = "created_at", nullable = false)
  @CreatedDate
  val createdAt: LocalDateTime = LocalDateTime.now(),

  @Column(name = "updated_at", nullable = false)
  @LastModifiedDate
  var updatedAt: LocalDateTime = LocalDateTime.now(),

  @Column(name = "deleted_at")
  var deletedAt: LocalDateTime? = null,

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "pdu_id", insertable = false, updatable = false)
  val pdu: PduEntity? = null,

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "probation_region_id", insertable = false, updatable = false)
  val region: RegionEntity? = null,
)
