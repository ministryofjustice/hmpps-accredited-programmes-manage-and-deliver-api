package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.DomainScores
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.IndividualCognitiveScores
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.IndividualRelationshipScores
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.IndividualRiskScores
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.IndividualSelfManagementScores
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.IndividualSexScores
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.OffenceCohort
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.PniScore
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.RelationshipDomainScore
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.RiskScore
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.SelfManagementDomainScore
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.SexDomainScore
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.ThinkingDomainScore
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.oasysApi.model.NeedLevel
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.oasysApi.model.OverallIntensity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.factory.ReferralCohortHistoryEntityFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.ReferralEntityFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ProgrammeGroupMembershipRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ReferralCohortHistoryRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service.TelemetryService
import java.util.UUID

class CohortServiceTest {

  private val pniService: PniService = mockk()
  private val referralCohortHistoryRepository: ReferralCohortHistoryRepository = mockk()
  private val telemetryService: TelemetryService = mockk()
  private val programmeGroupMembershipRepository: ProgrammeGroupMembershipRepository = mockk()

  private lateinit var cohortService: CohortService

  @BeforeEach
  fun setup() {
    cohortService = CohortService(
      referralCohortHistoryRepository,
      telemetryService,
    )
  }

  @Test
  fun `updateCohortForReferral should save new history when none exists`() {
    // Given
    val referralEntity = ReferralEntityFactory().withId(UUID.randomUUID()).produce()
    val newCohort = OffenceCohort.SEXUAL_OFFENCE
    val createdBy = "test-user"

    val securityContext = mockk<SecurityContext>()
    val authentication = mockk<Authentication>()
    every { securityContext.authentication } returns authentication
    every { authentication.name } returns createdBy
    SecurityContextHolder.setContext(securityContext)

    every { referralCohortHistoryRepository.findTopByReferralIdOrderByCreatedAtDesc(referralEntity.id!!) } returns null
    every { referralCohortHistoryRepository.save(any()) } returns mockk()
    every { telemetryService.logToAppInsights(any(), any(), any(), any(), any()) } returns Unit

    // When
    val result = cohortService.updateCohortForReferral(referralEntity, newCohort)

    // Then
    assertThat(result).isEqualTo(referralEntity)
    verify {
      referralCohortHistoryRepository.save(
        withArg {
          assertThat(it.referral).isEqualTo(referralEntity)
          assertThat(it.cohort).isEqualTo(newCohort)
          assertThat(it.createdBy).isEqualTo(createdBy)
        },
      )
    }
    verify {
      telemetryService.logToAppInsights(
        referralEntity = referralEntity,
        eventName = "Referral.update-cohort.success",
        activityType = "OVERRIDE_COHORT",
        toReferralStatusId = null,
        appliedBy = null,
      )
    }
  }

  @Test
  fun `updateCohortForReferral should save new history when cohort is different from latest`() {
    // Given
    val referralEntity = ReferralEntityFactory().withId(UUID.randomUUID()).produce()
    val existingHistory = ReferralCohortHistoryEntityFactory()
      .withReferral(referralEntity)
      .withCohort(OffenceCohort.GENERAL_OFFENCE)
      .produce()
    val newCohort = OffenceCohort.SEXUAL_OFFENCE
    val createdBy = "test-user"

    val securityContext = mockk<SecurityContext>()
    val authentication = mockk<Authentication>()
    every { securityContext.authentication } returns authentication
    every { authentication.name } returns createdBy
    SecurityContextHolder.setContext(securityContext)

    every { referralCohortHistoryRepository.findTopByReferralIdOrderByCreatedAtDesc(referralEntity.id!!) } returns existingHistory
    every { referralCohortHistoryRepository.save(any()) } returns mockk()
    every { telemetryService.logToAppInsights(any(), any(), any(), any(), any()) } returns Unit

    // When
    cohortService.updateCohortForReferral(referralEntity, newCohort)

    // Then
    verify {
      referralCohortHistoryRepository.save(
        withArg {
          assertThat(it.cohort).isEqualTo(newCohort)
          assertThat(it.createdBy).isEqualTo(createdBy)
        },
      )
    }
  }

  @Test
  fun `updateCohortForReferral should NOT save new history when cohort is the same as latest`() {
    // Given
    val referralEntity = ReferralEntityFactory().withId(UUID.randomUUID()).produce()
    val newCohort = OffenceCohort.GENERAL_OFFENCE
    val existingHistory = ReferralCohortHistoryEntityFactory()
      .withReferral(referralEntity)
      .withCohort(newCohort)
      .produce()

    val securityContext = mockk<SecurityContext>()
    val authentication = mockk<Authentication>()
    every { securityContext.authentication } returns authentication
    every { authentication.name } returns "test-user"
    SecurityContextHolder.setContext(securityContext)

    every { referralCohortHistoryRepository.findTopByReferralIdOrderByCreatedAtDesc(referralEntity.id!!) } returns existingHistory
    every { telemetryService.logToAppInsights(any(), any(), any(), any(), any()) } returns Unit

    // When
    cohortService.updateCohortForReferral(referralEntity, newCohort)

    // Then
    verify(exactly = 0) { referralCohortHistoryRepository.save(any()) }
  }

  @Test
  fun `should return SEXUAL_OFFENCE when OSP DC score is significant`() {
    val pniScore = createBasePniScore("High", "Not Applicable", 0, 0, 0)

    val result = cohortService.determineOffenceCohort(pniScore)
    assertThat(result).isEqualTo(OffenceCohort.SEXUAL_OFFENCE)
  }

  @Test
  fun `should return SEXUAL_OFFENCE when OSP IIC score is significant`() {
    val pniScore = createBasePniScore("Not Applicable", "Medium", 0, 0, 0)

    val result = cohortService.determineOffenceCohort(pniScore)
    assertThat(result).isEqualTo(OffenceCohort.SEXUAL_OFFENCE)
  }

  @Test
  fun `should return SEXUAL_OFFENCE when both OSP scores are significant`() {
    val pniScore = createBasePniScore("Medium", "High", 0, 0, 0)

    val result = cohortService.determineOffenceCohort(pniScore)
    assertThat(result).isEqualTo(OffenceCohort.SEXUAL_OFFENCE)
  }

  @Test
  fun `should return SEXUAL_OFFENCE when sexual preoccupation is above zero`() {
    val pniScore = createBasePniScore("Not Applicable", "Not Applicable", 1, 0, 0)

    val result = cohortService.determineOffenceCohort(pniScore)
    assertThat(result).isEqualTo(OffenceCohort.SEXUAL_OFFENCE)
  }

  @Test
  fun `should return SEXUAL_OFFENCE when offence related sexual interests is above zero`() {
    val pniScore = createBasePniScore("Not Applicable", "Not Applicable", 0, 2, 0)

    val result = cohortService.determineOffenceCohort(pniScore)
    assertThat(result).isEqualTo(OffenceCohort.SEXUAL_OFFENCE)
  }

  @Test
  fun `should return SEXUAL_OFFENCE when emotional congruence is above zero`() {
    val pniScore = createBasePniScore("Not Applicable", "Not Applicable", 0, 0, 1)

    val result = cohortService.determineOffenceCohort(pniScore)
    assertThat(result).isEqualTo(OffenceCohort.SEXUAL_OFFENCE)
  }

  @Test
  fun `should return SEXUAL_OFFENCE when multiple sex domain scores are above zero`() {
    val pniScore = createBasePniScore("Not Applicable", "Not Applicable", 1, 2, 1)

    val result = cohortService.determineOffenceCohort(pniScore)
    assertThat(result).isEqualTo(OffenceCohort.SEXUAL_OFFENCE)
  }

  @Test
  fun `should return SEXUAL_OFFENCE when both OSP and sex domain criteria are met`() {
    val pniScore = createBasePniScore("Medium", "High", 1, 2, 1)

    val result = cohortService.determineOffenceCohort(pniScore)
    assertThat(result).isEqualTo(OffenceCohort.SEXUAL_OFFENCE)
  }

  @Test
  fun `should return GENERAL_OFFENCE when all scores are not significant`() {
    val pniScore = createBasePniScore("Not Applicable", "Not Applicable", 0, 0, 0)

    val result = cohortService.determineOffenceCohort(pniScore)
    assertThat(result).isEqualTo(OffenceCohort.GENERAL_OFFENCE)
  }

  @Test
  fun `should return GENERAL_OFFENCE when OSP scores are null`() {
    val pniScore = createBasePniScore(null, null, 0, 0, 0)

    val result = cohortService.determineOffenceCohort(pniScore)
    assertThat(result).isEqualTo(OffenceCohort.GENERAL_OFFENCE)
  }

  @Test
  fun `should return GENERAL_OFFENCE when sex domain scores are null`() {
    val pniScore = createBasePniScore("Not Applicable", "Not Applicable", null, null, null)

    val result = cohortService.determineOffenceCohort(pniScore)
    assertThat(result).isEqualTo(OffenceCohort.GENERAL_OFFENCE)
  }

  private fun createBasePniScore(
    ospDc: String?,
    ospIic: String?,
    sexualPreOccupation: Int?,
    offenceRelatedSexualInterests: Int?,
    emotionalCongruence: Int?,
  ): PniScore = PniScore(
    overallIntensity = OverallIntensity.MODERATE,
    domainScores = DomainScores(
      sexDomainScore = SexDomainScore(
        overallSexDomainLevel = NeedLevel.MEDIUM_NEED,
        individualSexScores = IndividualSexScores(
          sexualPreOccupation = sexualPreOccupation,
          offenceRelatedSexualInterests = offenceRelatedSexualInterests,
          emotionalCongruence = emotionalCongruence,
        ),
      ),
      thinkingDomainScore = ThinkingDomainScore(
        overallThinkingDomainLevel = NeedLevel.MEDIUM_NEED,
        individualThinkingScores = IndividualCognitiveScores(
          proCriminalAttitudes = 0,
          hostileOrientation = 0,
        ),
      ),
      relationshipDomainScore = RelationshipDomainScore(
        overallRelationshipDomainLevel = NeedLevel.MEDIUM_NEED,
        individualRelationshipScores = IndividualRelationshipScores(
          curRelCloseFamily = 0,
          prevCloseRelationships = 0,
          easilyInfluenced = 0,
          aggressiveControllingBehaviour = 0,
        ),
      ),
      selfManagementDomainScore = SelfManagementDomainScore(
        overallSelfManagementDomainLevel = NeedLevel.MEDIUM_NEED,
        individualSelfManagementScores = IndividualSelfManagementScores(
          impulsivity = 0,
          temperControl = 0,
          problemSolvingSkills = 0,
          difficultiesCoping = 0,
        ),
      ),
    ),
    riskScore = RiskScore(
      classification = "LOW_RISK",
      individualRiskScores = IndividualRiskScores(
        ospDc = ospDc,
        ospIic = ospIic,
        ogrs3Risk = null,
        ovpRisk = null,
        rsr = null,
        sara = null,
      ),
    ),
    validationErrors = emptyList(),
  )
}
