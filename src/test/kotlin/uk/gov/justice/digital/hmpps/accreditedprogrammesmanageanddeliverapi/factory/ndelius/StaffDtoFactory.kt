package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.ndelius

import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.StaffDto
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common.randomWord

class StaffDtoFactory {
  private var forename: String = randomWord(4..8).toString()
  private var middleNames: String? = randomWord(4..8).toString()
  private var surname: String = randomWord(4..8).toString()

  fun withForename(forename: String) = apply { this.forename = forename }
  fun withMiddleNames(middleNames: String?) = apply { this.middleNames = middleNames }
  fun withSurname(surname: String) = apply { this.surname = surname }

  fun produce() = StaffDto(
    forename = this.forename,
    middleNames = this.middleNames,
    surname = this.surname,
  )
}
