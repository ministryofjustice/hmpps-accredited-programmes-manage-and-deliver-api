package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import java.time.LocalDate

@JsonIgnoreProperties(ignoreUnknown = true)
data class NDeliusSentenceResponse(
//  @Schema(
//    example = "X933590",
//    required = true,
//    description = "The crn associated with this referral.",
//  )
  @get:JsonProperty("description", required = false)
  val description: String? = null,

  @get:JsonProperty("startDate", required = true)
  val startDate: LocalDate,

  @get:JsonProperty("licenceExpiryDate", required = false)
  @JsonFormat(pattern = "d MMMM yyyy")
  val licenceExpiryDate: LocalDate? = null,

  @get:JsonProperty("postSentenceSupervisionEndDate", required = false)
  val postSentenceSupervisionEndDate: LocalDate? = null,

  @get:JsonProperty("twoThirdsSupervisionDate", required = false)
  val twoThirdsSupervisionDate: LocalDate? = null,

  @get:JsonProperty("custodial", required = true)
  val custodial: Boolean,

  @get:JsonProperty("releaseType", required = false)
  val releaseType: String? = null,

  @get:JsonProperty("licenceConditions", required = true)
  val licenceConditions: List<CodeDescription>,

  @get:JsonProperty("requirements", required = true)
  val requirements: List<CodeDescription>,

  @get:JsonProperty("postSentenceSupervisionRequirements", required = true)
  val postSentenceSupervisionRequirements: List<CodeDescription>,
)
