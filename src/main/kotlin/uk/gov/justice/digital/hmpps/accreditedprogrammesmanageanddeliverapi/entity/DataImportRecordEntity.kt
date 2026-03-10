package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.validation.constraints.NotNull
import java.time.OffsetDateTime
import java.util.UUID

@Entity
@Table(name = "data_import_record")
class DataImportRecordEntity(
  @Id
  @GeneratedValue
  @Column(name = "id")
  var id: UUID? = null,

  @NotNull
  @Column(name = "entity_type", nullable = false)
  var entityType: String,

  @NotNull
  @Column(name = "source_id", nullable = false)
  var sourceId: String,

  @NotNull
  @Column(name = "target_id", nullable = false)
  var targetId: UUID,

  @NotNull
  @Column(name = "imported_at", nullable = false)
  var importedAt: OffsetDateTime,
)
