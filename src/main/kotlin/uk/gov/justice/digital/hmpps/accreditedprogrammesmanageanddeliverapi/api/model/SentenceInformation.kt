package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonProperty
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.CodeDescription
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.NDeliusSentenceResponse
import java.time.LocalDate

data class SentenceInformation(

  @get:JsonProperty("sentenceType", required = false)
  val sentenceType: String? = null,

  @get:JsonProperty("releaseType", required = false)
  val releaseType: String? = null,

  @get:JsonProperty("licenceConditions", required = false)
  val licenceConditions: List<CodeDescription>? = null,

  @get:JsonProperty("licenceEndDate", required = false)
  @JsonFormat(pattern = "d MMMM yyyy")
  val licenceEndDate: LocalDate? = null,

  @get:JsonProperty("postSentenceSupervisionStartDate", required = false)
  @JsonFormat(pattern = "d MMMM yyyy")
  val postSentenceSupervisionStartDate: LocalDate? = null,

  @get:JsonProperty("postSentenceSupervisionEndDate", required = false)
  @JsonFormat(pattern = "d MMMM yyyy")
  val postSentenceSupervisionEndDate: LocalDate? = null,

  @get:JsonProperty("twoThirdsPoint", required = false)
  @JsonFormat(pattern = "d MMMM yyyy")
  val twoThirdsPoint: LocalDate? = null,

  @get:JsonProperty("orderRequirements", required = false)
  val orderRequirements: List<CodeDescription>? = null,

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
