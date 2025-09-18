package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
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

@ExtendWith(MockitoExtension::class)
class CohortServiceTest {

  @Mock
  private lateinit var pniService: PniService

  @InjectMocks
  private lateinit var cohortService: CohortService

  @Test
  fun `should return SEXUAL_OFFENCE when OSP DC score is significant`() {
    val crn = "TEST123"
    val pniScore = createBasePniScore("HIGH", "NOT_APPLICABLE", 0, 0, 0)

    val result = cohortService.determineOffenceCohort(pniScore)
    assertThat(result).isEqualTo(OffenceCohort.SEXUAL_OFFENCE)
  }

  @Test
  fun `should return SEXUAL_OFFENCE when OSP IIC score is significant`() {
    val crn = "TEST123"
    val pniScore = createBasePniScore("NOT_APPLICABLE", "MEDIUM", 0, 0, 0)

    val result = cohortService.determineOffenceCohort(pniScore)
    assertThat(result).isEqualTo(OffenceCohort.SEXUAL_OFFENCE)
  }

  @Test
  fun `should return SEXUAL_OFFENCE when both OSP scores are significant`() {
    val crn = "TEST123"
    val pniScore = createBasePniScore("MEDIUM", "HIGH", 0, 0, 0)

    val result = cohortService.determineOffenceCohort(pniScore)
    assertThat(result).isEqualTo(OffenceCohort.SEXUAL_OFFENCE)
  }

  @Test
  fun `should return SEXUAL_OFFENCE when sexual preoccupation is above zero`() {
    val crn = "TEST123"
    val pniScore = createBasePniScore("NOT_APPLICABLE", "NOT_APPLICABLE", 1, 0, 0)

    val result = cohortService.determineOffenceCohort(pniScore)
    assertThat(result).isEqualTo(OffenceCohort.SEXUAL_OFFENCE)
  }

  @Test
  fun `should return SEXUAL_OFFENCE when offence related sexual interests is above zero`() {
    val crn = "TEST123"
    val pniScore = createBasePniScore("NOT_APPLICABLE", "NOT_APPLICABLE", 0, 2, 0)

    val result = cohortService.determineOffenceCohort(pniScore)
    assertThat(result).isEqualTo(OffenceCohort.SEXUAL_OFFENCE)
  }

  @Test
  fun `should return SEXUAL_OFFENCE when emotional congruence is above zero`() {
    val crn = "TEST123"
    val pniScore = createBasePniScore("NOT_APPLICABLE", "NOT_APPLICABLE", 0, 0, 1)

    val result = cohortService.determineOffenceCohort(pniScore)
    assertThat(result).isEqualTo(OffenceCohort.SEXUAL_OFFENCE)
  }

  @Test
  fun `should return SEXUAL_OFFENCE when multiple sex domain scores are above zero`() {
    val crn = "TEST123"
    val pniScore = createBasePniScore("NOT_APPLICABLE", "NOT_APPLICABLE", 1, 2, 1)

    val result = cohortService.determineOffenceCohort(pniScore)
    assertThat(result).isEqualTo(OffenceCohort.SEXUAL_OFFENCE)
  }

  @Test
  fun `should return SEXUAL_OFFENCE when both OSP and sex domain criteria are met`() {
    val crn = "TEST123"
    val pniScore = createBasePniScore("MEDIUM", "HIGH", 1, 2, 1)

    val result = cohortService.determineOffenceCohort(pniScore)
    assertThat(result).isEqualTo(OffenceCohort.SEXUAL_OFFENCE)
  }

  @Test
  fun `should return GENERAL_OFFENCE when all scores are not significant`() {
    val crn = "TEST123"
    val pniScore = createBasePniScore("NOT_APPLICABLE", "NOT_APPLICABLE", 0, 0, 0)

    val result = cohortService.determineOffenceCohort(pniScore)
    assertThat(result).isEqualTo(OffenceCohort.GENERAL_OFFENCE)
  }

  @Test
  fun `should return GENERAL_OFFENCE when OSP scores are null`() {
    val crn = "TEST123"
    val pniScore = createBasePniScore(null, null, 0, 0, 0)

    val result = cohortService.determineOffenceCohort(pniScore)
    assertThat(result).isEqualTo(OffenceCohort.GENERAL_OFFENCE)
  }

  @Test
  fun `should return GENERAL_OFFENCE when sex domain scores are null`() {
    val crn = "TEST123"
    val pniScore = createBasePniScore("NOT_APPLICABLE", "NOT_APPLICABLE", null, null, null)

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
