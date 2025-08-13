package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.oasys

import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.CodeDescription
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.NDeliusRegistration
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.NDeliusRegistrations
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.utils.Utils.createCodeDescription
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.random.Random

class NDeliusRegistrationsFactory {
  private var registrations: List<NDeliusRegistration> = listOf(
    NDeliusRegistrationFactory().produce(),
    NDeliusRegistrationFactory().produce(),
  )

  fun withRegistrations(registrations: List<NDeliusRegistration>) = apply { this.registrations = registrations }

  fun produce() = NDeliusRegistrations(
    registrations = this.registrations,
  )
}

class NDeliusRegistrationFactory {
  private var type: CodeDescription = createCodeDescription()
  private var category: CodeDescription? = if (Random.nextBoolean()) createCodeDescription() else null
  private var date: String = LocalDate.now().minusDays(Random.nextLong(1, 365)).format(DateTimeFormatter.ISO_LOCAL_DATE)
  private var nextReviewDate: String? = if (Random.nextBoolean()) {
    LocalDate.now().plusDays(Random.nextLong(30, 365))
      .format(DateTimeFormatter.ISO_LOCAL_DATE)
  } else {
    null
  }

  fun withType(type: CodeDescription) = apply { this.type = type }
  fun withCategory(category: CodeDescription?) = apply { this.category = category }
  fun withDate(date: String) = apply { this.date = date }
  fun withNextReviewDate(nextReviewDate: String?) = apply { this.nextReviewDate = nextReviewDate }

  fun produce() = NDeliusRegistration(
    type = this.type,
    category = this.category,
    date = this.date,
    nextReviewDate = this.nextReviewDate,
  )
}
