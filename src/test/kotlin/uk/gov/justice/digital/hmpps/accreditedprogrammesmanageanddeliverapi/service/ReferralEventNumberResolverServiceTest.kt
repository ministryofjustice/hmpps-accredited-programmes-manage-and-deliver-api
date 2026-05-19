package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service

import com.microsoft.applicationinsights.TelemetryClient
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoInteractions
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatusCode
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.ClientResult
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.NDeliusIntegrationApiClient
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.CodeDescription
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.FullName
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.NDeliusApiProbationDeliveryUnit
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.NDeliusCaseRequirementOrLicenceConditionResponse
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.RequirementOrLicenceConditionManager
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.RequirementStaff
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ReferralEntitySourcedFrom
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.ReferralEntityFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ReferralRepository

@ExtendWith(MockitoExtension::class)
class ReferralEventNumberResolverServiceTest {

  @Mock
  private lateinit var nDeliusIntegrationApiClient: NDeliusIntegrationApiClient

  @Mock
  private lateinit var referralRepository: ReferralRepository

  @Mock
  private lateinit var telemetryClient: TelemetryClient

  @Test
  fun `does not attempt resolution when event number is already non-zero`() {
    val referral = ReferralEntityFactory().withEventNumber(4).produce()
    val service = ReferralEventNumberResolverService(nDeliusIntegrationApiClient, referralRepository, telemetryClient)

    val result = service.resolveIfEventNumberIsZero(referral)

    assertThat(result).isEqualTo(4)
    verifyNoInteractions(nDeliusIntegrationApiClient)
    verifyNoInteractions(referralRepository)
    verifyNoInteractions(telemetryClient)
  }

  @Test
  fun `updates referral event number when a valid number is found`() {
    val referral =
      ReferralEntityFactory().withEventNumber(0).withSourcedFrom(ReferralEntitySourcedFrom.REQUIREMENT).produce()
    val service = ReferralEventNumberResolverService(nDeliusIntegrationApiClient, referralRepository, telemetryClient)

    `when`(nDeliusIntegrationApiClient.getRequirementManagerDetails(referral.crn, referral.eventId!!)).thenReturn(
      ClientResult.Success(HttpStatusCode.valueOf(200), mockRequirementLicResponse()),
    )

    val result = service.resolveIfEventNumberIsZero(referral)

    assertThat(result).isEqualTo(3)
    assertThat(referral.eventNumber).isEqualTo(3)
    verify(referralRepository).save(referral)
    verify(nDeliusIntegrationApiClient).getRequirementManagerDetails(referral.crn, referral.eventId!!)
  }

  @Test
  fun `returns original eventNumber when an unexpected error occurs`() {
    val referral =
      ReferralEntityFactory().withEventNumber(0).withSourcedFrom(ReferralEntitySourcedFrom.REQUIREMENT).produce()
    val service = ReferralEventNumberResolverService(nDeliusIntegrationApiClient, referralRepository, telemetryClient)

    `when`(nDeliusIntegrationApiClient.getRequirementManagerDetails(referral.crn, referral.eventId!!)).thenReturn(
      ClientResult.Failure.Other(
        HttpMethod.GET,
        "/case/${referral.crn}/requirement/${referral.eventId}",
        RuntimeException("Connection refused"),
        "nDelius",
      ),
    )
    val result = service.resolveIfEventNumberIsZero(referral)

    assertThat(result).isEqualTo(0)
    assertThat(referral.eventNumber).isEqualTo(0)
    verify(referralRepository, times(0)).save(referral)
    verify(nDeliusIntegrationApiClient).getRequirementManagerDetails(referral.crn, referral.eventId!!)
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
