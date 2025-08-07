package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model

import java.time.LocalDate

data class Offence(
  val date: LocalDate,
  val mainCategoryCode: String,
  val mainCategoryDescription: String,
  val subCategoryCode: String,
  val subCategoryDescription: String,
)
