package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Offence classification based on assessment")
enum class OffenceCohort(val displayName: String) {
  SEXUAL_OFFENCE("Sexual offence"),
  GENERAL_OFFENCE("General offence"),
  ;

  companion object {
    fun fromDisplayName(input: String): OffenceCohort = entries.find {
      it.displayName.equals(input, ignoreCase = true) ||
        it.name.equals(normalize(input), ignoreCase = true)
    } ?: throw IllegalArgumentException("Invalid cohort: $input")

    private fun normalize(input: String?): String = input?.replace("\\s+".toRegex(), "_")?.uppercase() ?: ""
  }
}
