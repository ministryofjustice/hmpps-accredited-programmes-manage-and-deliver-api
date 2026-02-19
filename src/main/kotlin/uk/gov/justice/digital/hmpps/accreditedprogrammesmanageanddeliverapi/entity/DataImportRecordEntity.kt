package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.validation.constraints.NotNull
import java.time.Instant
import java.util.UUID

/**
 * This entity records a (successful) import of an entity (e.g. a Referral) from the
 * Interventions Manager source data, and the M&D target data.
 * It has multiple uses:
 * 1. Allows for referential integrity to be maintained from IM -> M&D
 * 2. Used to prevent duplicate imports
 * 3. Auditing of the import process
 */
@Entity
@Table(name = "data_import_record")
class DataImportRecordEntity(
  @Id
  @GeneratedValue
  @Column(name = "id")
  var id: UUID? = null,

  @NotNull
  @Column(name = "entity_type")
  var entityType: String,

  /**
   * The ID from Interventions Manager
   */
  @NotNull
  @Column(name = "source_id")
  var sourceId: String,

  /**
   * The ID of the corresponding M&D entity that was created/updated
   */
  @NotNull
  @Column(name = "target_id")
  var targetId: UUID,

  @NotNull
  @Column(name = "imported_at")
  var importedAt: Instant = Instant.now(),
)
