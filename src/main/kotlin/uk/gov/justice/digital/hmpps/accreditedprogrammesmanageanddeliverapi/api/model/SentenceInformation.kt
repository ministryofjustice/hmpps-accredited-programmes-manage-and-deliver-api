package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.CodeDescription
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.NDeliusSentenceResponse
import java.time.LocalDate

data class SentenceInformation(

  @Schema(
    example = "ORA community order",
    required = false,
    description = "The type of sentence.",
  )
  @get:JsonProperty("sentenceType", required = false)
  val sentenceType: String? = null,

  @Schema(
    example = "Released on licence",
    required = false,
    description = "The release type.",
  )
  @get:JsonProperty("releaseType", required = false)
  val releaseType: String? = null,

  @Schema(
    example = "['Accredited programme: Building Choices']",
    required = false,
    description = "A list of the licence conditions.",
  )
  @get:JsonProperty("licenceConditions", required = false)
  val licenceConditions: List<CodeDescription>? = null,

  @Schema(
    example = "10 June 2025",
    required = false,
    description = "The end date of the licence.",
  )
  @get:JsonProperty("licenceEndDate", required = false)
  @JsonFormat(pattern = "d MMMM yyyy")
  val licenceEndDate: LocalDate? = null,

  @Schema(
    example = "10 June 2025",
    required = false,
    description = "The start date of the post supervision.",
  )
  @get:JsonProperty("postSentenceSupervisionStartDate", required = false)
  @JsonFormat(pattern = "d MMMM yyyy")
  val postSentenceSupervisionStartDate: LocalDate? = null,

  @Schema(
    example = "10 June 2025",
    required = false,
    description = "The end date of the post supervision.",
  )
  @get:JsonProperty("postSentenceSupervisionEndDate", required = false)
  @JsonFormat(pattern = "d MMMM yyyy")
  val postSentenceSupervisionEndDate: LocalDate? = null,

  @Schema(
    example = "10 June 2025",
    required = false,
    description = "The date two thirds of the way to the end of the sentence.",
  )
  @get:JsonProperty("twoThirdsPoint", required = false)
  @JsonFormat(pattern = "d MMMM yyyy")
  val twoThirdsPoint: LocalDate? = null,

  @Schema(
    example = "['Accredited programme: Building Choices']",
    required = false,
    description = "A list of the order requirements.",
  )
  @get:JsonProperty("orderRequirements", required = false)
  val orderRequirements: List<CodeDescription>? = null,

  @Schema(
    example = "10 June 2025",
    required = false,
    description = "The end date of the order.",
  )
  @get:JsonProperty("orderEndDate", required = false)
  @JsonFormat(pattern = "d MMMM yyyy")
  val orderEndDate: LocalDate? = null,
)

fun NDeliusSentenceResponse.toModel() = SentenceInformation(
  sentenceType = description,
  releaseType = releaseType,
  licenceConditions = licenceConditions,
  licenceEndDate = licenceExpiryDate,
  // TODO check this value
  postSentenceSupervisionStartDate = LocalDate.now(),
  postSentenceSupervisionEndDate = postSentenceSupervisionEndDate,
  twoThirdsPoint = twoThirdsSupervisionDate,
  orderRequirements = requirements,
  // TODO check this value
  orderEndDate = LocalDate.now(),
)
