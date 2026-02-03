package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema

data class DeleteSessionCaptionResponse(
  @get:JsonProperty("caption", required = true)
  @Schema(description = "Caption indicating what session is about to be deleted")
  val caption: String,
)
