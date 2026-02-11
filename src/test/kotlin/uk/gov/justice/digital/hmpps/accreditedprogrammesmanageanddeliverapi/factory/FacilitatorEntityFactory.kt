package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory

import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.FacilitatorEntity
import java.util.UUID

class FacilitatorEntityFactory {
  private var id: UUID? = null
  private var personName: String = "John Smith"
  private var ndeliusPersonCode: String = "123456"
  private var ndeliusTeamCode: String = "12345"
  private var ndeliusTeamName: String = "Team 1"

  fun withId(id: UUID?) = apply { this.id }
  fun withPersonName(personName: String) = apply { this.personName = personName }
  fun withNdeliusPersonCode(ndeliusPersonCode: String) = apply { this.ndeliusPersonCode = ndeliusPersonCode }
  fun withNdeliusTeamCode(ndeliusTeamCode: String) = apply { this.ndeliusTeamCode = ndeliusTeamCode }
  fun withNdeliusTeamName(ndeliusTeamName: String) = apply { this.ndeliusTeamName = ndeliusTeamName }
  fun produce() = FacilitatorEntity(
    id = this.id,
    personName = this.personName,
    ndeliusPersonCode = this.ndeliusPersonCode,
    ndeliusTeamCode = this.ndeliusTeamCode,
    ndeliusTeamName = this.ndeliusTeamName,
  )
}
