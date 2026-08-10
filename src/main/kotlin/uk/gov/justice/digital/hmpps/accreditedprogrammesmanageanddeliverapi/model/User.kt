package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.model

data class User(
  val username: String,
  val active: Boolean,
  val name: String,
)
