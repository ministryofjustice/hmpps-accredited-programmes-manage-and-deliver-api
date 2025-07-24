package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model

import java.time.LocalDate

data class ServiceUser(
  var name: String,
  var crn: String,
  var dateOfBirth: LocalDate,
  var age: String,
  var gender: String,
  var ethnicity: String,
  var currentPdu: String,
)
