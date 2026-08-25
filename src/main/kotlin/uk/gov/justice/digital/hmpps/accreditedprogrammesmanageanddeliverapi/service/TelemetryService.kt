package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service

import com.microsoft.applicationinsights.TelemetryClient
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.ReferralSentenceReferenceRequest
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.config.logToAppInsights
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ReferralCohortHistoryEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ReferralEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ProgrammeGroupMembershipRepository
import java.util.UUID

@Service
class TelemetryService(
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
        "cohort" to (
          referralEntity?.referralCohortHistories
            // The `@OrderBy("createdAt DESC")` on `referralCohortHistories` makes Hibernate
            // return most-recent-first, so `firstOrNull()` picks the current cohort — the
            // intent for telemetry. The explicit `sortedWith(...)` re-establishes the same
            // ordering in-Kotlin with a deterministic natural-attribute tiebreak
            // (`createdBy` then `cohort.name`) so that ties on `createdAt` do not resolve
            // via Hibernate's undefined-order fallback (LinkedHashSet insertion order,
            // which is Hibernate-version-dependent).
            ?.sortedWith(
              compareByDescending<ReferralCohortHistoryEntity> { it.createdAt }
                .thenBy { it.createdBy }
                .thenBy { it.cohort.name },
            )
            ?.firstOrNull()
            ?.cohort?.toString() ?: ""
          ),
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
