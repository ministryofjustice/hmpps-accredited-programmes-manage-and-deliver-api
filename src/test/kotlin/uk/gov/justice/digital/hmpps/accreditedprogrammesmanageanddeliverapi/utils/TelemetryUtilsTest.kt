package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.utils

import com.microsoft.applicationinsights.TelemetryClient
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.config.logToAppInsights
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.ReferralCohortHistoryFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.ReferralEntityFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.ReferralReportingLocationFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.ReferralSentenceReferenceRequestFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.ReferralStatusDescriptionEntityFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.ReferralStatusHistoryEntityFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.programmeGroup.ProgrammeGroupFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.programmeGroup.ProgrammeGroupMembershipFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ProgrammeGroupMembershipRepository
import java.util.UUID

class TelemetryUtilsTest {
  private val telemetryClient = mockk<TelemetryClient>()
  private val programmeGroupMembershipRepository = mockk<ProgrammeGroupMembershipRepository>()

  private lateinit var util: TelemetryUtils

  @BeforeEach
  fun setup() {
    util = TelemetryUtils(
      telemetryClient,
      programmeGroupMembershipRepository,
    )
  }

  @Test
  fun `should log to AppInsights using referral, eventName, activityType, toReferralStatusId and appliedBy`() {
    // Given
    val referralId = UUID.randomUUID()
    val referralEntity = ReferralEntityFactory()
      .withId(referralId)
      .withCohortHistories(mutableSetOf(ReferralCohortHistoryFactory().produce()))
      .withReferralReportingLocationEntity(ReferralReportingLocationFactory().produce())
      .produce()
    val referralStatusDescriptionEntity = ReferralStatusDescriptionEntityFactory().produce()
    val statusHistory = ReferralStatusHistoryEntityFactory().produce(referralEntity, referralStatusDescriptionEntity)
    referralEntity.statusHistories.add(statusHistory)
    val programmeGroupMembershipEntity = ProgrammeGroupMembershipFactory()
      .withProgrammeGroup(ProgrammeGroupFactory().produce())
      .withReferral(referralEntity)
      .produce()
    val eventName = "testEventName"
    val activityType = "testActivityType"
    val toReferralStatusId = UUID.randomUUID()
    val appliedBy = "testAppliedBy"

    every { telemetryClient.logToAppInsights(any(), any()) } returns Unit
    every { programmeGroupMembershipRepository.findCurrentGroupByReferralId(any()) } returns programmeGroupMembershipEntity

    // When
    util.logToAppInsights(
      referralEntity = referralEntity,
      eventName = eventName,
      activityType = activityType,
      toReferralStatusId = toReferralStatusId,
      appliedBy = appliedBy,
    )

    // Then
    verify {
      telemetryClient.logToAppInsights(
        eventName,
        mapOf(
          "activityType" to activityType,
          "regionName" to referralEntity.referralReportingLocation!!.regionName,
          "deliveryUnitCode" to referralEntity.referralReportingLocation!!.pduName,
          "deliveryLocation" to programmeGroupMembershipEntity.programmeGroup.deliveryLocationName,
          "referralId" to referralEntity.id.toString(),
          "referralStatus" to referralEntity.statusHistories.first().referralStatusDescription.description,
          "cohort" to referralEntity.referralCohortHistories.first().cohort.toString(),
          "crn" to referralEntity.crn,
          "fromStatus" to referralEntity.statusHistories.first().referralStatusDescription.id.toString(),
          "toStatus" to toReferralStatusId.toString(),
          "appliedBy" to appliedBy,
        ),
      )
    }
    verify { programmeGroupMembershipRepository.findCurrentGroupByReferralId(referralEntity.id!!) }
  }

  @Test
  fun `should log to AppInsights using null referral, eventName, activityType, toReferralStatusId and appliedBy`() {
    // Given
    val referralEntity = null
    val eventName = "testEventName"
    val activityType = "testActivityType"
    val toReferralStatusId = UUID.randomUUID()
    val appliedBy = "testAppliedBy"

    every { telemetryClient.logToAppInsights(any(), any()) } returns Unit

    // When
    util.logToAppInsights(
      referralEntity = referralEntity,
      eventName = eventName,
      activityType = activityType,
      toReferralStatusId = toReferralStatusId,
      appliedBy = appliedBy,
    )

    // Then
    verify {
      telemetryClient.logToAppInsights(
        eventName,
        mapOf(
          "activityType" to activityType,
          "regionName" to "",
          "deliveryUnitCode" to "",
          "deliveryLocation" to "",
          "referralId" to "",
          "referralStatus" to "",
          "cohort" to "",
          "crn" to "",
          "fromStatus" to "",
          "toStatus" to toReferralStatusId.toString(),
          "appliedBy" to appliedBy,
        ),
      )
    }
  }

  @Test
  fun `should log to AppInsights using referral, eventName, activityType, fromSourcedFromName, fromEventId, referralSentenceReferenceRequest and appliedBy`() {
    // Given
    val referralId = UUID.randomUUID()
    val referralEntity = ReferralEntityFactory().withId(referralId).produce()
    val eventName = "testEventName"
    val activityType = "testActivityType"
    val fromSourcedFromName = "testFromSourcedFromName"
    val fromEventId = "testFromEventId"
    val referralSentenceReferenceRequest = ReferralSentenceReferenceRequestFactory().produce()
    val appliedBy = "testAppliedBy"

    every { telemetryClient.logToAppInsights(any(), any()) } returns Unit

    // When
    util.logToAppInsights(
      referralEntity = referralEntity,
      eventName = eventName,
      activityType = activityType,
      fromSourcedFromName = fromSourcedFromName,
      fromEventId = fromEventId,
      referralSentenceReferenceRequest = referralSentenceReferenceRequest,
      appliedBy = appliedBy,
    )

    // Then
    verify {
      telemetryClient.logToAppInsights(
        eventName,
        mapOf(
          "activityType" to activityType,
          "referralId" to referralEntity.id.toString(),
          "crn" to referralEntity.crn,
          "fromSourcedFrom" to fromSourcedFromName,
          "fromEventId" to fromEventId,
          "toSourcedFrom" to referralSentenceReferenceRequest.sourcedFrom.name,
          "toEventId" to referralSentenceReferenceRequest.eventId,
          "appliedBy" to appliedBy,
        ),
      )
    }
  }

  @Test
  fun `should log to AppInsights using eventName, integrationActionType and outcome`() {
    // Given
    val eventName = "testEventName"
    val integrationActionType = "testIntegrationActionType"
    val outcome = "testOutcome"

    every { telemetryClient.logToAppInsights(any(), any()) } returns Unit

    // When
    util.logToAppInsights(
      eventName = eventName,
      integrationActionType = integrationActionType,
      outcome = outcome,
    )

    // Then
    verify {
      telemetryClient.logToAppInsights(
        eventName,
        mapOf(
          "integrationActionType" to integrationActionType,
          "outcome" to outcome,
        ),
      )
    }
  }

  @Test
  fun `should log to AppInsights using eventName and properties`() {
    // Given
    val eventName = "testEventName"
    val properties = mapOf("testKey" to "testValue")

    every { telemetryClient.logToAppInsights(any(), any()) } returns Unit

    // When
    util.logToAppInsights(
      eventName = eventName,
      properties = properties,
    )

    // Then
    verify {
      telemetryClient.logToAppInsights(
        eventName,
        mapOf(
          "testKey" to "testValue",
        ),
      )
    }
  }
}
