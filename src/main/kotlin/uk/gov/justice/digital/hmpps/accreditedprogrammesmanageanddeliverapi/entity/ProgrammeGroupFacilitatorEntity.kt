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
import org.springframework.data.annotation.CreatedDate
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup.CreateGroupFacilitator
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.type.FacilitatorType
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "programme_group_facilitator")
class ProgrammeGroupFacilitatorEntity(
  @Id
  @GeneratedValue
  @Column(name = "id")
  var id: UUID? = null,

  @NotNull
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "facilitator_id")
  var facilitator: FacilitatorEntity,

  @NotNull
  @CreatedDate
  @Column(name = "added_at")
  var addedAt: LocalDateTime = LocalDateTime.now(),

  @NotNull
  @Column("facilitator_type")
  @Enumerated(EnumType.STRING)
  var facilitatorType: FacilitatorType,
)

fun CreateGroupFacilitator.toProgrammeGroupFacilitatorEntity(): ProgrammeGroupFacilitatorEntity = ProgrammeGroupFacilitatorEntity(
  facilitator = this.toFacilitatorEntity(),
  facilitatorType = facilitatorType,
)

// TODO look at this setup it seems strange to me
fun CreateGroupFacilitator.toFacilitatorEntity(): FacilitatorEntity = FacilitatorEntity(
// TODO SPLIT NAMES SENSIBLY
  personForename = personName,
  personMiddleName = personName,
  personSurname = personName,
  ndeliusPersonCode = personCode,
  ndeliusTeamCode = ndeliusTeamCode,
  ndeliusTeamName = ndeliusTeamName,
)
