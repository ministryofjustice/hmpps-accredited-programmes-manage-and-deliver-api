package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.controller

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.ErrorResponse
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.OffenceCohort
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.ReferralMotivationBackgroundAndNonAssociations
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.ReferralEntityFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.ReferralMotivationBackgroundAndNonAssociationsFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.ReferralStatusHistoryEntityFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.produce
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ReferralStatusDescriptionRepository
import java.time.LocalDateTime
import java.util.UUID

class GroupAllocationNotesControllerIntegrationTest : IntegrationTestBase() {

  @Autowired
  private lateinit var referralStatusDescriptionRepository: ReferralStatusDescriptionRepository

  @BeforeEach
  fun setup() {
    testDataCleaner.cleanAllTables()
    stubAuthTokenEndpoint()
  }

  @Nested
  @DisplayName("Get referral motivation, background and non-associations by referral ID")
  inner class GetReferralMotivationBackgroundAndNonAssociationsByReferralId {
    @Test
    fun `should return referral motivation background and non-associations object `() {
      // Given
      val createdAt = LocalDateTime.now()
      val referralEntity = ReferralEntityFactory()
        .withCreatedAt(createdAt)
        .withCohort(OffenceCohort.SEXUAL_OFFENCE)
        .produce()

      val statusHistory = ReferralStatusHistoryEntityFactory()
        .withCreatedAt(LocalDateTime.of(2025, 9, 24, 15, 0))
        .produce(
          referralEntity,
          referralStatusDescriptionRepository.getAwaitingAssessmentStatusDescription(),
        )
      val motivationBackgroundAndNonAssociations = ReferralMotivationBackgroundAndNonAssociationsFactory().produce(referral = referralEntity)
      referralEntity.referralMotivationBackgroundAndNonAssociations = motivationBackgroundAndNonAssociations
      testDataGenerator.createReferralWithFields(referralEntity, listOf(statusHistory, motivationBackgroundAndNonAssociations))

      // When
      val response = performRequestAndExpectOk(
        httpMethod = HttpMethod.GET,
        uri = "/referral/${referralEntity.id}/motivation-background-non-associations",
        returnType = object : ParameterizedTypeReference<ReferralMotivationBackgroundAndNonAssociations>() {},
      )

      // Then
      assertThat(response.id).isEqualTo(referralEntity.referralMotivationBackgroundAndNonAssociations!!.id)
      assertThat(response.referralId).isEqualTo(referralEntity.id)
      assertThat(response.maintainsInnocence).isEqualTo(referralEntity.referralMotivationBackgroundAndNonAssociations!!.maintainsInnocence)
      assertThat(response.motivations).isEqualTo(referralEntity.referralMotivationBackgroundAndNonAssociations!!.motivations)
      assertThat(response.nonAssociations).isEqualTo(referralEntity.referralMotivationBackgroundAndNonAssociations!!.nonAssociations)
      assertThat(response.otherConsiderations).isEqualTo(referralEntity.referralMotivationBackgroundAndNonAssociations!!.otherConsiderations)
      assertThat(response.createdBy).isEqualTo(referralEntity.referralMotivationBackgroundAndNonAssociations!!.createdBy)
      assertThat(response.createdAt).isNotNull
      assertThat(response.lastUpdatedBy).isNull()
      assertThat(response.lastUpdatedAt).isNull()
    }

    @Test
    fun `should return 404 when no motivation background or non-association is not found`() {
      performRequestAndExpectStatus(
        httpMethod = HttpMethod.GET,
        uri = "/referral/${UUID.randomUUID()}/motivation-background-non-associations",
        object : ParameterizedTypeReference<ErrorResponse>() {},
        HttpStatus.NOT_FOUND.value(),
      )
    }
  }
}
