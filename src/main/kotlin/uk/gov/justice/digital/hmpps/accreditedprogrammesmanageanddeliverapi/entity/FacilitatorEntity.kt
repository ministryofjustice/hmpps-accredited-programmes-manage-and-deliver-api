package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.validation.constraints.NotNull
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup.CreateGroupTeamMember
import java.util.UUID

@Entity
@Table(name = "facilitator")
class FacilitatorEntity(
  @Id
  @GeneratedValue
  @Column(name = "id")
  var id: UUID? = null,

  @NotNull
  @Column(name = "person_name")
  var personName: String,

  @NotNull
  @Column(name = "ndelius_person_code")
  var ndeliusPersonCode: String,

  @NotNull
  @Column(name = "ndelius_team_code")
  var ndeliusTeamCode: String,

  @NotNull
  @Column(name = "ndelius_team_name")
  var ndeliusTeamName: String,
)

fun CreateGroupTeamMember.toFacilitatorEntity(): FacilitatorEntity = FacilitatorEntity(
  personName = facilitator,
  ndeliusPersonCode = facilitatorCode,
  ndeliusTeamCode = ndeliusTeamCode,
  ndeliusTeamName = teamName,
)
