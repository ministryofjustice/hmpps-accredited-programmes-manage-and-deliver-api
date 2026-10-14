package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity

import jakarta.annotation.Nullable
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

  @param:NotNull
  @Column(name = "person_forename", nullable = false)
  var personForename: String,

  @param:Nullable
  @Column(name = "person_middle_names")
  var personMiddleNames: String? = null,

  @param:NotNull
  @Column(name = "person_surname", nullable = false)
  var personSurname: String,

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
  personForename = facilitatorForename,
  personMiddleNames = facilitatorMiddleNames,
  personSurname = facilitatorSurname,
  ndeliusPersonCode = facilitatorCode,
  ndeliusTeamCode = teamCode,
  ndeliusTeamName = teamName,
)
