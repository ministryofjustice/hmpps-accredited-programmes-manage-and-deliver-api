package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory

import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.Offence
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common.randomNumber
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common.randomSentence
import java.time.LocalDate

class OffenceFactory {
  private var date: LocalDate = LocalDate.now().minusMonths(3)
  private var mainCategoryCode: String = randomNumber(2).toString()
  private var mainCategoryDescription: String = randomSentence()
  private var subCategoryCode: String = randomNumber(2).toString()
  private var subCategoryDescription: String = randomSentence()

  fun withDate(date: LocalDate) = apply { this.date = date }
  fun withMainCategoryCode(mainCategoryCode: String) = apply { this.mainCategoryCode = mainCategoryCode }
  fun withMainCategoryDescription(mainCategoryDescription: String) = apply { this.mainCategoryDescription = mainCategoryDescription }
  fun withSubCategoryCode(subCategoryCode: String) = apply { this.subCategoryCode = subCategoryCode }
  fun withSubCategoryDescription(subCategoryDescription: String) = apply { this.subCategoryDescription = subCategoryDescription }

  fun produce() = Offence(
    date = this.date,
    mainCategoryCode = this.mainCategoryCode,
    mainCategoryDescription = this.mainCategoryDescription,
    subCategoryCode = this.subCategoryCode,
    subCategoryDescription = this.subCategoryDescription,
  )
}
