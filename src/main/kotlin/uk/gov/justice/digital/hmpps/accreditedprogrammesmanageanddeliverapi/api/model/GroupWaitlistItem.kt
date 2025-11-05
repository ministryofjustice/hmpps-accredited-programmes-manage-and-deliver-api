package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.GroupWaitlistItemViewEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ReferralEntitySourcedFrom
import java.time.LocalDate
import java.time.Period
import java.util.UUID

data class GroupWaitlistItem(
  @Schema(
    example = "1ff57cea-352c-4a99-8f66-3e626aac3265",
    required = true,
    description = "The UUID of the referral.",
  )
  @get:JsonProperty("referralId", required = true)
  val referralId: UUID,
  @Schema(
    example = "Order end date",
    required = true,
    description = "A human-readable string describing the entity (Licence Condition or Requirement) that caused the Referral to be created in our system",
  )
  @get:JsonProperty("sourcedFrom", required = true)
  val sourcedFrom: String? = null,

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
    example = "1 January 2030",
    required = true,
    description = "The end date of the person's sentence.",
  )
  @get:JsonProperty("sentenceEndDate", required = false)
  @JsonFormat(pattern = "d MMMM yyyy")
  val sentenceEndDate: LocalDate?,

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
  referralId = referralId,
  sourcedFrom = sourcedFrom?.let { if (sourcedFrom?.equals(ReferralEntitySourcedFrom.REQUIREMENT) == true) "Order end date" else "Licence end date" },
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
  referralId = referralId,
  crn = crn,
  personName = personName,
  sentenceEndDate = sentenceEndDate,
  status = status,
  sourcedFrom = sourcedFrom,
)
