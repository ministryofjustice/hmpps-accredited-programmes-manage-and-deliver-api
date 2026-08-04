package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoInteractions
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.inOrder
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatusCode
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.ClientResult
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.NDeliusIntegrationApiClient
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.CodeDescription
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.FullName
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.LicenceConditions
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.NDeliusApiProbationDeliveryUnit
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.NDeliusCaseRequirementOrLicenceConditionResponse
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.RequirementOrLicenceConditionManager
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.RequirementStaff
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.Requirements
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ReferralEntitySourcedFrom
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.LicenceConditionFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.ReferralEntityFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.RequirementFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ReferralRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service.ReferralEventNumberResolverService.Companion.INVALID_EVENT_NUMBER
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.utils.TelemetryUtils
import java.util.UUID

@ExtendWith(MockitoExtension::class)
class ReferralEventNumberResolverServiceTest {

  @Mock
  private lateinit var nDeliusIntegrationApiClient: NDeliusIntegrationApiClient

  @Mock
  private lateinit var referralRepository: ReferralRepository

  @Mock
  private lateinit var telemetryUtils: TelemetryUtils

  @InjectMocks
  private lateinit var service: ReferralEventNumberResolverService

  @Test
  fun `does not attempt resolution when event number is already non-zero`() {
    val referral =
      ReferralEntityFactory().withEventNumber(4).withSourcedFrom(ReferralEntitySourcedFrom.REQUIREMENT).produce()

    val result = service.resolveIfEventNumberIsZero(referral)

    assertThat(result!!.eventNumber).isEqualTo(4)
    assertThat(result.sourcedFrom).isEqualTo(ReferralEntitySourcedFrom.REQUIREMENT)
    verifyNoInteractions(nDeliusIntegrationApiClient)
    verifyNoInteractions(referralRepository)
    verifyNoInteractions(telemetryUtils)
  }

  @Test
  fun `should not resolve event number that has a valid event number (non-zero)`() {
    // Given
    val referral =
      ReferralEntityFactory()
        .withEventId("1234567")
        .withEventNumber(3)
        .withSourcedFrom(ReferralEntitySourcedFrom.LICENCE_CONDITION)
        .produce()

    `when`(referralRepository.findByEventNumber(INVALID_EVENT_NUMBER)).thenReturn(listOf(referral))

    // When
    service.resolveAllEventNumbers()

    // Then
    verify(referralRepository, times(0)).save(any())
    verifyNoInteractions(telemetryUtils)
  }

  @Test
  fun `should update referral event id and event number when a valid licence condition is found`() {
    // Given
    val referralId = UUID.randomUUID()
    val referral =
      ReferralEntityFactory()
        .withId(referralId)
        .withEventId("1234567")
        .withEventNumber(0)
        .withSourcedFrom(ReferralEntitySourcedFrom.LICENCE_CONDITION)
        .produce()

    `when`(referralRepository.findByEventNumber(INVALID_EVENT_NUMBER)).thenReturn(listOf(referral))

    val licenceConditions = LicenceConditions(
      listOf(
        LicenceConditionFactory()
          .withId(484848484)
          .withEventNumber("5")
          .withSubCategory(CodeDescription("LC266", "Building Choices"))
          .produce(),
      ),
    )
    `when`(nDeliusIntegrationApiClient.getLicenceConditions(referral.crn))
      .thenReturn(
        ClientResult.Success(HttpStatusCode.valueOf(200), licenceConditions),
      )

    // When
    service.resolveAllEventNumbers()

    // Then
    assertThat(referral.eventNumber).isEqualTo(5)
    assertThat(referral.eventId).isEqualTo("484848484")

    val inOrder = inOrder(telemetryUtils)

    inOrder.verify(telemetryUtils).logToAppInsights(
      eventName = "LicenceConditions.get-nDelius.success",
      integrationActionType = "GET_LICENCE_CONDITIONS_N_DELIUS",
      outcome = "success",
    )
    inOrder.verify(telemetryUtils).logToAppInsights(
      "Referral.event-number-resolution.success",
      mapOf("referralId" to "$referralId", "newEventNumber" to "5", "newEventId" to "484848484"),
    )
  }

  @Test
  fun `should update referral event id and event number when a valid requirement is found`() {
    // Given
    val referralId = UUID.randomUUID()
    val referral =
      ReferralEntityFactory()
        .withId(referralId)
        .withEventId("865945")
        .withEventNumber(0)
        .withSourcedFrom(ReferralEntitySourcedFrom.REQUIREMENT)
        .produce()

    `when`(referralRepository.findByEventNumber(INVALID_EVENT_NUMBER)).thenReturn(listOf(referral))

    val requirements = Requirements(
      content = listOf(
        RequirementFactory()
          .withId(1684953)
          .withEventNumber("2")
          .withSubCategory(CodeDescription("734", "Building Choices"))
          .produce(),
      ),
    )
    `when`(nDeliusIntegrationApiClient.getRequirements(referral.crn))
      .thenReturn(
        ClientResult.Success(HttpStatusCode.valueOf(200), requirements),
      )

    // When
    service.resolveAllEventNumbers()

    // Then
    assertThat(referral.eventNumber).isEqualTo(2)
    assertThat(referral.eventId).isEqualTo("1684953")

    val inOrder = inOrder(telemetryUtils)

    inOrder.verify(telemetryUtils).logToAppInsights(
      eventName = "Requirements.get-nDelius.success",
      integrationActionType = "GET_REQUIREMENTS_N_DELIUS",
      outcome = "success",
    )
    inOrder.verify(telemetryUtils).logToAppInsights(
      "Referral.event-number-resolution.success",
      mapOf("referralId" to "$referralId", "newEventNumber" to "2", "newEventId" to "1684953"),
    )
  }

  @Test
  fun `updates referral event number when a valid number is found`() {
    val referral =
      ReferralEntityFactory().withEventNumber(0).withSourcedFrom(ReferralEntitySourcedFrom.LICENCE_CONDITION).produce()

    `when`(nDeliusIntegrationApiClient.getLicenceConditionManagerDetails(referral.crn, referral.eventId!!)).thenReturn(
      ClientResult.Success(HttpStatusCode.valueOf(200), mockRequirementLicResponse()),
    )

    val result = service.resolveIfEventNumberIsZero(referral)

    assertThat(result!!.eventNumber).isEqualTo(3)
    assertThat(referral.eventNumber).isEqualTo(3)
    assertThat(result.sourcedFrom).isEqualTo(ReferralEntitySourcedFrom.LICENCE_CONDITION)
    verify(referralRepository).save(referral)
    verify(nDeliusIntegrationApiClient, times(1)).getLicenceConditionManagerDetails(referral.crn, referral.eventId!!)
  }

  @Test
  fun `returns original eventNumber when an unexpected error occurs`() {
    val referral =
      ReferralEntityFactory().withEventNumber(0).withSourcedFrom(ReferralEntitySourcedFrom.REQUIREMENT).produce()

    `when`(nDeliusIntegrationApiClient.getRequirementManagerDetails(referral.crn, referral.eventId!!)).thenReturn(
      ClientResult.Failure.Other(
        HttpMethod.GET,
        "/case/${referral.crn}/requirement/${referral.eventId}",
        RuntimeException("Connection refused"),
        "nDelius",
      ),
    )
    val result = service.resolveIfEventNumberIsZero(referral)

    assertThat(result!!.eventNumber).isEqualTo(0)
    assertThat(result.sourcedFrom).isEqualTo(ReferralEntitySourcedFrom.REQUIREMENT)
    verify(referralRepository, times(0)).save(referral)
    verify(nDeliusIntegrationApiClient, times(1)).getRequirementManagerDetails(referral.crn, referral.eventId!!)
    verify(nDeliusIntegrationApiClient, times(0)).getLicenceConditionManagerDetails(referral.crn, referral.eventId!!)
  }

  private fun mockRequirementLicResponse(): NDeliusCaseRequirementOrLicenceConditionResponse {
    val expectedManager = RequirementOrLicenceConditionManager(
      staff = RequirementStaff(
        code = "STAFF001",
        name = FullName(forename = "Wiremocked-Sarah", surname = "Johnson"),
      ),
      team = CodeDescription(code = "TEAM001", description = "(Wiremocked) Community Offender Management Team"),
      probationDeliveryUnit = NDeliusApiProbationDeliveryUnit(
        code = "PDU001",
        description = "(Wiremocked) London PDU",
      ),
      officeLocations = listOf(
        CodeDescription(code = "OFF001", description = "(Wiremocked) Waterloo Office"),
        CodeDescription(code = "OFF002", description = "(Wiremocked) Victoria Office"),
      ),
    )

    return NDeliusCaseRequirementOrLicenceConditionResponse(manager = expectedManager, eventNumber = 3)
  }
}
