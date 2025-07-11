package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema

data class ErrorResponse(

  @Schema(example = "404", required = true, description = "The HTTP status code returned by the server")
  @get:JsonProperty("status", required = true) val status: Int,

  @Schema(example = "404", description = "An application-specific error code")
  @get:JsonProperty("errorCode") val errorCode: Int? = null,

  @Schema(example = "Referral Not found", description = "A human readable message for the error")
  @get:JsonProperty("userMessage") val userMessage: String? = null,

  @Schema(example = "null", description = "A developer friendly message for the error")
  @get:JsonProperty("developerMessage") val developerMessage: String? = null,

  @Schema(example = "null", description = "Additional information about the error")
  @get:JsonProperty("moreInfo") val moreInfo: String? = null,
)
