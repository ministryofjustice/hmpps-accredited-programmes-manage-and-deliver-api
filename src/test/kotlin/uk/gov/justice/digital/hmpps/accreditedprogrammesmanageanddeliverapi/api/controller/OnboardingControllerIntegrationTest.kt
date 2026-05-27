package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.controller

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.whenever
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpMethod
import org.springframework.test.context.bean.override.mockito.MockitoBean
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.OffenceCohort
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.ReferralDetails
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common.randomCrn
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.ReferralEntityFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service.ReferralService
import java.time.LocalDate
import java.util.UUID

class OnboardingControllerIntegrationTest : IntegrationTestBase() {

  @MockitoBean
  private lateinit var referralService: ReferralService

  @BeforeEach
  fun setup() {
    testDataCleaner.cleanAllTables()
  }

  @Test
  fun `fetchPersonalDetailsForReferrals returns success not-found and failure ids`() {
    // Given
    val firstReferral = ReferralEntityFactory().produce()
    val secondReferral = ReferralEntityFactory().produce()
    val thirdReferral = ReferralEntityFactory().produce()
    val randomReferralId = UUID.randomUUID()

    testDataGenerator.createReferralWithStatusHistory(firstReferral)
    testDataGenerator.createReferralWithStatusHistory(secondReferral)
    testDataGenerator.createReferralWithStatusHistory(thirdReferral)

    val firstReferralId = firstReferral.id!!
    val secondReferralId = secondReferral.id!!

    runBlocking {
      whenever(referralService.refreshPersonalDetailsForReferral(firstReferralId)).thenThrow(RuntimeException("Simulated failure"))
      whenever(referralService.refreshPersonalDetailsForReferral(secondReferralId)).thenReturn(buildReferralDetails(secondReferralId))
      whenever(referralService.refreshPersonalDetailsForReferral(randomReferralId)).thenReturn(null)
    }

    // When
    val response = performRequestAndExpectStatusWithBody(
      HttpMethod.POST,
      "/onboarding/referrals",
      object : ParameterizedTypeReference<FetchPersonalDetailsResponse>() {},
      body = FetchPersonalDetailsRequest(
        referralIds = listOf(
          firstReferralId.toString(),
          secondReferralId.toString(),
          randomReferralId.toString(),
        ),
      ),
      expectedResponseStatus = 200,
    )

    // Then
    assertEquals(listOf(secondReferralId.toString()), response.successIds)
    assertEquals(listOf(randomReferralId.toString()), response.notFoundIds)
    assertEquals(listOf(firstReferralId.toString()), response.failureIds)
  }

  private fun buildReferralDetails(referralId: UUID): ReferralDetails = ReferralDetails(
    id = referralId,
    crn = randomCrn(),
    personName = "Test Person",
    interventionName = "Building Choices",
    createdAt = LocalDate.now(),
    dateOfBirth = LocalDate.of(1990, 1, 1),
    probationPractitionerName = "Test Practitioner",
    probationPractitionerEmail = "test.practitioner@justice.gov.uk",
    cohort = OffenceCohort.GENERAL_OFFENCE,
    hasLdc = false,
    hasLdcDisplayText = "",
    currentStatusDescription = "Awaiting assessment",
    currentlyAllocatedGroupCode = null,
    currentlyAllocatedGroupId = null,
    pdu = "Test PDU",
    reportingTeam = "Test Team",
  )
}
