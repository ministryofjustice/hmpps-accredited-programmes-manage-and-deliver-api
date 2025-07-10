package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model

import java.time.LocalDate

data class ServiceUser(
    var name: String? = null,
    var crn: String,
    var dob: LocalDate,
    var gender: String? = null,
    var ethnicity: String? = null,
    var currentPdu: String? = null,
    var setting: String? = null,
)