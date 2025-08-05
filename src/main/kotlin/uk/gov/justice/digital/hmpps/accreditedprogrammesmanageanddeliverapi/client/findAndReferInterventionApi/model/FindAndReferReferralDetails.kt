package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.findAndReferInterventionApi.model

import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ReferralEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ReferralStatusHistoryEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.type.InterventionType
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.type.PersonReferenceType
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.type.SettingType
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.type.SourcedFromReferenceType
import java.util.UUID

data class FindAndReferReferralDetails(
  val interventionType: InterventionType,
  val interventionName: String,
  val personReference: String,
  val personReferenceType: PersonReferenceType,
  val referralId: UUID,
  val setting: SettingType,
  val sourcedFromReference: String,
  val sourcedFromReferenceType: SourcedFromReferenceType,
  val eventNumber: String,
)

fun FindAndReferReferralDetails.toReferralEntity(statusHistories: MutableList<ReferralStatusHistoryEntity>) = ReferralEntity(
  crn = if (personReferenceType == PersonReferenceType.CRN) personReference else "UNKNOWN",
  interventionType = interventionType,
  interventionName = interventionName,
  setting = setting,
  personName = "UNKNOWN",
  statusHistories = statusHistories,
  eventNumber = sourcedFromReference,
  eventId = eventNumber,
)
