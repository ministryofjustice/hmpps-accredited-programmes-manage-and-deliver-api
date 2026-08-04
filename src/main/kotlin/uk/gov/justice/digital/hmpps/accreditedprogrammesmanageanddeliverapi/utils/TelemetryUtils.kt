package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.utils

import com.microsoft.applicationinsights.TelemetryClient
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.ReferralSentenceReferenceRequest
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.config.logToAppInsights
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ReferralEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ProgrammeGroupMembershipRepository
import java.util.UUID

@Component
class TelemetryUtils(
  private val telemetryClient: TelemetryClient,
  private val programmeGroupMembershipRepository: ProgrammeGroupMembershipRepository,
) {
  fun logToAppInsights(
    referralEntity: ReferralEntity?,
    eventName: String,
    activityType: String,
    toReferralStatusId: UUID?,
    appliedBy: String?,
  ) {
    val referralId = referralEntity?.id
    val programmeGroupMembership =
      if (referralId != null) programmeGroupMembershipRepository.findCurrentGroupByReferralId(referralId) else null

    telemetryClient.logToAppInsights(
      eventName,
      mapOf(
        "activityType" to activityType,
        "regionName" to (referralEntity?.referralReportingLocation?.regionName ?: ""),
        "deliveryUnitCode" to (referralEntity?.referralReportingLocation?.pduName ?: ""),
        "deliveryLocation" to (programmeGroupMembership?.programmeGroup?.deliveryLocationName ?: ""),
        "referralId" to (referralId?.toString() ?: ""),
        "referralStatus" to (
          referralEntity?.statusHistories?.firstOrNull()?.referralStatusDescription?.description
            ?: ""
          ),
        "cohort" to (referralEntity?.referralCohortHistories?.firstOrNull()?.cohort?.toString() ?: ""),
        "crn" to (referralEntity?.crn ?: ""),
        "fromStatus" to (
          referralEntity?.statusHistories?.firstOrNull()?.referralStatusDescription?.id?.toString()
            ?: ""
          ),
        "toStatus" to (toReferralStatusId?.toString() ?: ""),
        "appliedBy" to (appliedBy ?: ""),
      ),
    )
  }

  fun logToAppInsights(
    referralEntity: ReferralEntity,
    eventName: String,
    activityType: String,
    fromSourcedFromName: String?,
    fromEventId: String?,
    referralSentenceReferenceRequest: ReferralSentenceReferenceRequest,
    appliedBy: String,
  ) {
    telemetryClient.logToAppInsights(
      eventName,
      mapOf(
        "activityType" to activityType,
        "referralId" to referralEntity.id.toString(),
        "crn" to referralEntity.crn,
        "fromSourcedFrom" to (fromSourcedFromName ?: ""),
        "fromEventId" to (fromEventId ?: ""),
        "toSourcedFrom" to referralSentenceReferenceRequest.sourcedFrom.name,
        "toEventId" to referralSentenceReferenceRequest.eventId,
        "appliedBy" to appliedBy,
      ),
    )
  }

  fun logToAppInsights(eventName: String, integrationActionType: String, outcome: String) {
    telemetryClient.logToAppInsights(
      eventName,
      mapOf(
        "integrationActionType" to integrationActionType,
        "outcome" to outcome,
      ),
    )
  }

  fun logToAppInsights(eventName: String, properties: Map<String, String>) {
    telemetryClient.logToAppInsights(
      eventName,
      properties,
    )
  }
}
