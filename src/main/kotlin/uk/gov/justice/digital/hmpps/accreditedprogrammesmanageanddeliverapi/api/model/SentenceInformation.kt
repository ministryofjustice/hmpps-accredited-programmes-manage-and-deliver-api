package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.NDeliusPersonalDetails
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.getNameAsString
import java.time.LocalDate

data class SentenceInformation(
//  @Schema(
//    example = "X933590",
//    required = true,
//    description = "The crn associated with this referral.",
//  )
  @get:JsonProperty("description", required = true)
  val description: String,


  @get:JsonProperty("startDate", required = true)
  val startDate: LocalDate,

  @get:JsonProperty("licenceExpiryDate", required = true)
  @JsonFormat(pattern = "d MMMM yyyy")
  val licenceExpiryDate: LocalDate,

  @get:JsonProperty("postSentenceSupervisionEndDate", required = true)
  val postSentenceSupervisionEndDate: LocalDate? = null,

  @get:JsonProperty("twoThirdsSupervisionDate", required = true)
  val twoThirdsSupervisionDate: LocalDate,

  @get:JsonProperty("custodial", required = true)
  val custodial: Boolean,

  @get:JsonProperty("releaseType", required = true)
  val releaseType: String,

  @get:JsonProperty("licenceConditions", required = true)
  val licenceConditions: LicenseCondition? = null,

  @get:JsonProperty("requirements", required = true)
  val requirements: Requirements,

  @get:JsonProperty("postSentenceSupervisionRequirements", required = true)
  val postSentenceSupervisionRequirements: PostSentenceSupervisionRequirements,
)

fun NDeliusPersonalDetails.toModel(setting: String) = SentenceInformation(
  crn = crn,
  name = name.getNameAsString(),
  dateOfBirth = LocalDate.parse(dateOfBirth),
  ethnicity = ethnicity?.description,
  age = age,
  gender = sex.description,
  setting = setting,
  probationDeliveryUnit = probationDeliveryUnit?.description,
  dateRetrieved = LocalDate.now(),
)

data class LicenseCondition(
  @get:JsonProperty("code", required = true)
  val code: String? = null,

  @get:JsonProperty("description", required = true)
  val description: String? = null,
)

data class Requirements(
  @get:JsonProperty("code", required = true)
  val code: String? = null,

  @get:JsonProperty("description", required = true)
  val description: String? = null,
)

data class PostSentenceSupervisionRequirements(
  @get:JsonProperty("code", required = true)
  val code: String? = null,

  @get:JsonProperty("description", required = true)
  val description: String? = null,
)
