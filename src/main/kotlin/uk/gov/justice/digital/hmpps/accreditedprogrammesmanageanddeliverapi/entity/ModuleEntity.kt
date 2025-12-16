package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity

import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import jakarta.persistence.OrderBy
import jakarta.persistence.Table
import jakarta.validation.constraints.NotNull
import java.util.UUID

@Entity
@Table(name = "module")
class ModuleEntity(
  @Id
  @GeneratedValue
  @Column(name = "id")
  var id: UUID? = null,

  @NotNull
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "accredited_programme_template_id")
  var accreditedProgrammeTemplate: AccreditedProgrammeTemplateEntity,

  @NotNull
  @Column(name = "name")
  var name: String,

  @NotNull
  @Column(name = "module_number")
  var moduleNumber: Int,

  @OneToMany(
    fetch = FetchType.LAZY,
    cascade = [CascadeType.ALL],
    mappedBy = "module",
  )
  @OrderBy("sessionNumber ASC")
  var sessionTemplates: MutableSet<ModuleSessionTemplateEntity> = mutableSetOf(),
)
