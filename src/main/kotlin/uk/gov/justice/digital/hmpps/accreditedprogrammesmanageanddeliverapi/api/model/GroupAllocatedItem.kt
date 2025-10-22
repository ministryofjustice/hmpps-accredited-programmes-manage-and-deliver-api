package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model

import java.time.LocalDate

data class GroupAllocatedItem(
  val crn: String,
  val personName: String,
  val sentenceEndDate: LocalDate,
  val status: String,
)
