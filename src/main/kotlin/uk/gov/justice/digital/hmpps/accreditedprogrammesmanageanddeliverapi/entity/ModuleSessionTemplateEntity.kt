package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import jakarta.validation.constraints.NotNull
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.type.Pathway
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.type.SessionType
import java.util.UUID

@Entity
@Table(name = "module_session_template")
class ModuleSessionTemplateEntity(
  @Id
  @GeneratedValue
  @Column(name = "id")
  var id: UUID? = null,

  @NotNull
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "module_id")
  var module: ModuleEntity,

  @NotNull
  @Column(name = "session_number")
  var sessionNumber: Int,

  @NotNull
  @Enumerated(EnumType.STRING)
  @Column(name = "session_type")
  var sessionType: SessionType,

  @NotNull
  @Enumerated(EnumType.STRING)
  @Column(name = "pathway")
  var pathway: Pathway,

  @NotNull
  @Column(name = "name")
  var name: String,

  @Column(name = "description")
  var description: String? = null,

  @NotNull
  @Column(name = "duration_minutes")
  var durationMinutes: Int,
)
