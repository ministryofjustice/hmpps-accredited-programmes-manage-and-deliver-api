package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.findAndReferInterventionApi.model

import io.swagger.v3.oas.annotations.media.Schema
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.OffenceCohort
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.NDeliusPersonalDetails
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.getNameAsString
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ReferralEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ReferralEntitySourcedFrom
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ReferralStatusHistoryEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.type.InterventionType
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.type.PersonReferenceType
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.type.SettingType
import java.util.UUID

@Schema(description = "Full details of the Referral")
data class FindAndReferReferralDetails(
  @field:Schema(description = "The type of intervention", example = "ACP")
  val interventionType: InterventionType,

  @field:Schema(description = "The name of the intervention")
  val interventionName: String,

  @field:Schema(description = "The person reference (CRN or NOMS number, see personReferencType)")
  val personReference: String,

  @field:Schema(description = "The type of person reference, detailed in personReference")
  val personReferenceType: PersonReferenceType,

  @field:Schema(description = "The unique identifier for this referral")
  val referralId: UUID,

  @field:Schema(description = "The setting where the intervention will take place", example = "COMMUNITY")
  val setting: SettingType,

  @field:Schema(
    description = "What was the up-stream event that caused this Referral to be automatically created",
    example = "requirement",
  )
  val sourcedFromReferenceType: ReferralEntitySourcedFrom,

  @field:Schema(description = "A unique identifier pointing to the licence or requirement", example = "abc-123")
  val sourcedFromReference: String,

  @field:Schema(description = "The event number from the source system, starts at 1.")
  val eventNumber: Int,
)

fun FindAndReferReferralDetails.toReferralEntity(
  statusHistories: MutableList<ReferralStatusHistoryEntity>,
  cohort: OffenceCohort,
  personalDetails: NDeliusPersonalDetails?,
) = ReferralEntity(
  crn = if (personReferenceType == PersonReferenceType.CRN) personReference else "UNKNOWN",
  interventionType = interventionType,
  interventionName = interventionName,
  setting = setting,
  personName = personalDetails?.name?.getNameAsString() ?: "UNKNOWN",
  statusHistories = statusHistories,
  cohort = cohort,
  sourcedFrom = sourcedFromReferenceType,
  eventId = sourcedFromReference,
  eventNumber = eventNumber,

)
