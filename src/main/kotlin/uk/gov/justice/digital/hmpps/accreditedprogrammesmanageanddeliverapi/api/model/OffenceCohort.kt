package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Offence cohort classification based on PNI assessment")
enum class OffenceCohort {
  SEXUAL_OFFENCE,
  GENERAL_OFFENCE,
}
