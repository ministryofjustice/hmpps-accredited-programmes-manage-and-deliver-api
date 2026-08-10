package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory

import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.getNameAsString
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common.randomAlphanumericString
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common.randomFullName
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common.randomWord
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.FacilitatorEntity
import java.util.UUID

class FacilitatorEntityFactory {
  private var id: UUID? = null
  private var personName: String = randomFullName().getNameAsString()
  private var ndeliusPersonCode: String = randomAlphanumericString()
  private var ndeliusTeamCode: String = randomAlphanumericString()
  private var ndeliusTeamName: String = randomWord(4..6).toString()

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
