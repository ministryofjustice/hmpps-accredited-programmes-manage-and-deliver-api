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
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.model.create.CreateOrUpdateReferralMotivationBackgroundAndNonAssociations
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

  @Nested
  @DisplayName("Create or update referral motivation, background and non-associations")
  inner class CreateOrUpdateMotivationBackgroundAndNonAssociationsForReferral {
    @Test
    fun `should update motivation background and non-associations for a referral`() {
      // Given
      val referralEntity = ReferralEntityFactory().produce()

      val statusHistory = ReferralStatusHistoryEntityFactory().produce(
        referralEntity,
        referralStatusDescriptionRepository.getAwaitingAssessmentStatusDescription(),
      )
      val motivationBackgroundAndNonAssociations = ReferralMotivationBackgroundAndNonAssociationsFactory().produce(referral = referralEntity)
      referralEntity.referralMotivationBackgroundAndNonAssociations = motivationBackgroundAndNonAssociations
      testDataGenerator.createReferralWithFields(referralEntity, listOf(statusHistory, motivationBackgroundAndNonAssociations))

      val update = CreateOrUpdateReferralMotivationBackgroundAndNonAssociations(
        maintainsInnocence = false,
        motivations = "updated motivations.",
        otherConsiderations = "updated considerations.",
        nonAssociations = "updated non-associations.",
      )

      // When
      performRequestAndExpectStatus(
        httpMethod = HttpMethod.PUT,
        uri = "/referral/${referralEntity.id}/motivation-background-non-associations",
        body = update,
        expectedResponseStatus = HttpStatus.OK.value(),
      )

      val referral = testDataGenerator.getReferralById(referralEntity.id!!)
      assertThat(referral.referralMotivationBackgroundAndNonAssociations).isNotNull
      assertThat(referral.referralMotivationBackgroundAndNonAssociations!!.maintainsInnocence).isFalse
      assertThat(referral.referralMotivationBackgroundAndNonAssociations!!.motivations).isEqualTo(update.motivations)
      assertThat(referral.referralMotivationBackgroundAndNonAssociations!!.otherConsiderations).isEqualTo(update.otherConsiderations)
      assertThat(referral.referralMotivationBackgroundAndNonAssociations!!.nonAssociations).isEqualTo(update.nonAssociations)
      assertThat(referral.referralMotivationBackgroundAndNonAssociations!!.createdBy).isEqualTo(motivationBackgroundAndNonAssociations.createdBy)
      assertThat(referral.referralMotivationBackgroundAndNonAssociations!!.createdAt).isNotNull
      assertThat(referral.referralMotivationBackgroundAndNonAssociations!!.lastUpdatedBy).isEqualTo("AUTH_ADM")
      assertThat(referral.referralMotivationBackgroundAndNonAssociations!!.lastUpdatedAt).isNotNull
    }

    @Test
    fun `should create motivation background and non-associations for a referral`() {
      // Given
      val referralEntity = ReferralEntityFactory().produce()

      val statusHistory = ReferralStatusHistoryEntityFactory().produce(
        referralEntity,
        referralStatusDescriptionRepository.getAwaitingAssessmentStatusDescription(),
      )
      testDataGenerator.createReferralWithStatusHistory(referralEntity, statusHistory)

      // When
      performRequestAndExpectStatus(
        httpMethod = HttpMethod.PUT,
        uri = "/referral/${referralEntity.id}/motivation-background-non-associations",
        body = CreateOrUpdateReferralMotivationBackgroundAndNonAssociations(
          maintainsInnocence = true,
          motivations = "Motivated to change and improve life circumstances.",
          otherConsiderations = "Other information relevant to the referral.",
          nonAssociations = "Should not be in a group with a person who has a history of reoffending on a previous accredited programme.",
        ),
        expectedResponseStatus = HttpStatus.OK.value(),
      )

      val referral = testDataGenerator.getReferralById(referralEntity.id!!)
      assertThat(referral.referralMotivationBackgroundAndNonAssociations).isNotNull
      assertThat(referral.referralMotivationBackgroundAndNonAssociations!!.maintainsInnocence).isTrue
      assertThat(referral.referralMotivationBackgroundAndNonAssociations!!.motivations).isEqualTo("Motivated to change and improve life circumstances.")
      assertThat(referral.referralMotivationBackgroundAndNonAssociations!!.otherConsiderations).isEqualTo("Other information relevant to the referral.")
      assertThat(referral.referralMotivationBackgroundAndNonAssociations!!.nonAssociations).isEqualTo("Should not be in a group with a person who has a history of reoffending on a previous accredited programme.")
      assertThat(referral.referralMotivationBackgroundAndNonAssociations!!.createdBy).isEqualTo("AUTH_ADM")
      assertThat(referral.referralMotivationBackgroundAndNonAssociations!!.createdAt).isNotNull
    }

    @Test
    fun `should throw 404 if referral does not exist when trying to create motivation background and non-associations`() {
      // When
      performRequestAndExpectStatus(
        httpMethod = HttpMethod.PUT,
        uri = "/referral/${UUID.randomUUID()}/motivation-background-non-associations",
        body = CreateOrUpdateReferralMotivationBackgroundAndNonAssociations(
          maintainsInnocence = true,
          motivations = "Motivated to change and improve life circumstances.",
          otherConsiderations = "Other information relevant to the referral.",
          nonAssociations = "Should not be in a group with a person who has a history of reoffending on a previous accredited programme.",
        ),
        expectedResponseStatus = HttpStatus.NOT_FOUND.value(),
      )
    }
  }
}
