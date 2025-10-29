package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.GroupWaitlistItemViewEntity
import java.time.LocalDate
import java.time.Period

data class GroupWaitlistItem(
  @Schema(
    example = "X933590",
    required = true,
    description = "The crn associated with this referral.",
  )
  @get:JsonProperty("crn", required = true)
  val crn: String,
  @Schema(
    example = "John Doe",
    required = true,
    description = "The name of the person associated with this referral.",
  )
  @get:JsonProperty("personName", required = true)
  val personName: String,
  @Schema(
    example = "15 March 1985",
    required = true,
    description = "The end date of the person's sentence.",
  )
  @get:JsonProperty("sentenceEndDate", required = true)
  @JsonFormat(pattern = "d MMMM yyyy")
  val sentenceEndDate: LocalDate,
  @Schema(
    example = "SEXUAL_OFFENCE",
    required = true,
    description = "The offence cohort this referral is classified as.",
  )
  @get:JsonProperty("cohort", required = true)
  val cohort: OffenceCohort,
  @Schema(
    example = "True",
    required = true,
    description = "Does the person this referral is associated with have LDC needs.",
  )
  @get:JsonProperty("hasLdc", required = true)
  val hasLdc: Boolean,
  @Schema(
    example = "43",
    required = true,
    description = "The age of the person.",
  )
  @get:JsonProperty("age", required = true)
  val age: Int,
  @Schema(
    example = "Male",
    required = true,
    description = "The sex of the person.",
  )
  @get:JsonProperty("sex", required = true)
  val sex: String,
  @Schema(
    example = "Durham",
    required = true,
    description = "The PDU this referral is associated with.",
  )
  @get:JsonProperty("pdu", required = true)
  val pdu: String,
  @Schema(
    example = "Durham Team 1",
    required = true,
    description = "The reporting team this referral is associated with.",
  )
  @get:JsonProperty("reportingTeam", required = true)
  val reportingTeam: String,
  @Schema(
    example = "Awaiting assessment",
    required = true,
    description = "The display name of the Referral's current Status",
  )
  @get:JsonProperty("status", required = true)
  val status: String,
)

fun GroupWaitlistItemViewEntity.toApi() = GroupWaitlistItem(
  crn = crn,
  personName = personName,
  sentenceEndDate = sentenceEndDate,
  cohort = OffenceCohort.fromDisplayName(cohort),
  hasLdc = hasLdc,
  age = Period.between(dateOfBirth, LocalDate.now()).years,
  sex = sex,
  pdu = pduName,
  reportingTeam = reportingTeam,
  status = status,
)

fun GroupWaitlistItem.toAllocatedItem() = GroupAllocatedItem(
  crn = crn,
  personName = personName,
  sentenceEndDate = sentenceEndDate,
  status = status,
)
