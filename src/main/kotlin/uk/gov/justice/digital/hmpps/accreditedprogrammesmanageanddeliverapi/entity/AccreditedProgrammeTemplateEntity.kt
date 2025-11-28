package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity

import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.OneToMany
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import jakarta.validation.constraints.NotNull
import java.time.LocalDate
import java.util.UUID

@Entity
@Table(name = "accredited_programme_template")
class AccreditedProgrammeTemplateEntity(
  @Id
  @GeneratedValue
  @Column(name = "id")
  var id: UUID? = null,

  @NotNull
  @Column(name = "name")
  var name: String,

  @NotNull
  @Column(name = "valid_from")
  var validFrom: LocalDate,

  @Column(name = "valid_until")
  var validUntil: LocalDate? = null,

  @OneToMany(
    fetch = FetchType.LAZY,
    cascade = [CascadeType.ALL],
    mappedBy = "accreditedProgrammeTemplate",
  )
  var modules: MutableSet<ModuleEntity> = mutableSetOf(),

  @OneToOne(mappedBy = "accreditedProgrammeTemplate", fetch = FetchType.LAZY)
  var programmeGroup: ProgrammeGroupEntity? = null,
)
