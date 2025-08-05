package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.OffenceCohort
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.PniScore
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.DomainScores
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.IndividualRiskScores
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.IndividualSexScores
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.RiskScore
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.SexDomainScore
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.ThinkingDomainScore
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.RelationshipDomainScore
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.SelfManagementDomainScore
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.IndividualCognitiveScores
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.IndividualRelationshipScores
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.IndividualSelfManagementScores
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.oasysApi.model.OverallIntensity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.oasysApi.model.NeedLevel

@ExtendWith(MockitoExtension::class)
class CohortServiceTest {

  @Mock
  private lateinit var pniService: PniService

  @InjectMocks
  private lateinit var cohortService: CohortService

  @Test
  fun `determineOffenceCohort should return SEXUAL_OFFENCE when OSP DC score is above zero`() {
    // Given
    val crn = "TEST123"
    val pniScore = createBasePniScore(
      ospDc = "0.5",
      ospIic = "0.0",
      sexualPreOccupation = 0,
      offenceRelatedSexualInterests = 0,
      emotionalCongruence = 0
    )
    `when`(pniService.getPniScore(crn)).thenReturn(pniScore)

    // When
    val result = cohortService.determineOffenceCohort(crn)

    // Then
    assertThat(result).isEqualTo(OffenceCohort.SEXUAL_OFFENCE)
  }

  @Test
  fun `determineOffenceCohort should return SEXUAL_OFFENCE when OSP IIC score is above zero`() {
    // Given
    val crn = "TEST123"
    val pniScore = createBasePniScore(
      ospDc = "0.0",
      ospIic = "0.3",
      sexualPreOccupation = 0,
      offenceRelatedSexualInterests = 0,
      emotionalCongruence = 0
    )
    `when`(pniService.getPniScore(crn)).thenReturn(pniScore)

    // When
    val result = cohortService.determineOffenceCohort(crn)

    // Then
    assertThat(result).isEqualTo(OffenceCohort.SEXUAL_OFFENCE)
  }

  @Test
  fun `determineOffenceCohort should return SEXUAL_OFFENCE when both OSP scores are above zero`() {
    // Given
    val crn = "TEST123"
    val pniScore = createBasePniScore(
      ospDc = "0.4",
      ospIic = "0.6",
      sexualPreOccupation = 0,
      offenceRelatedSexualInterests = 0,
      emotionalCongruence = 0
    )
    `when`(pniService.getPniScore(crn)).thenReturn(pniScore)

    // When
    val result = cohortService.determineOffenceCohort(crn)

    // Then
    assertThat(result).isEqualTo(OffenceCohort.SEXUAL_OFFENCE)
  }

  @Test
  fun `determineOffenceCohort should return SEXUAL_OFFENCE when sexual preoccupation is above zero`() {
    // Given
    val crn = "TEST123"
    val pniScore = createBasePniScore(
      ospDc = "0.0",
      ospIic = "0.0",
      sexualPreOccupation = 1,
      offenceRelatedSexualInterests = 0,
      emotionalCongruence = 0
    )
    `when`(pniService.getPniScore(crn)).thenReturn(pniScore)

    // When
    val result = cohortService.determineOffenceCohort(crn)

    // Then
    assertThat(result).isEqualTo(OffenceCohort.SEXUAL_OFFENCE)
  }

  @Test
  fun `determineOffenceCohort should return SEXUAL_OFFENCE when offence related sexual interests is above zero`() {
    // Given
    val crn = "TEST123"
    val pniScore = createBasePniScore(
      ospDc = "0.0",
      ospIic = "0.0",
      sexualPreOccupation = 0,
      offenceRelatedSexualInterests = 2,
      emotionalCongruence = 0
    )
    `when`(pniService.getPniScore(crn)).thenReturn(pniScore)

    // When
    val result = cohortService.determineOffenceCohort(crn)

    // Then
    assertThat(result).isEqualTo(OffenceCohort.SEXUAL_OFFENCE)
  }

  @Test
  fun `determineOffenceCohort should return SEXUAL_OFFENCE when emotional congruence is above zero`() {
    // Given
    val crn = "TEST123"
    val pniScore = createBasePniScore(
      ospDc = "0.0",
      ospIic = "0.0",
      sexualPreOccupation = 0,
      offenceRelatedSexualInterests = 0,
      emotionalCongruence = 1
    )
    `when`(pniService.getPniScore(crn)).thenReturn(pniScore)

    // When
    val result = cohortService.determineOffenceCohort(crn)

    // Then
    assertThat(result).isEqualTo(OffenceCohort.SEXUAL_OFFENCE)
  }

  @Test
  fun `determineOffenceCohort should return SEXUAL_OFFENCE when multiple sex domain scores are above zero`() {
    // Given
    val crn = "TEST123"
    val pniScore = createBasePniScore(
      ospDc = "0.0",
      ospIic = "0.0",
      sexualPreOccupation = 1,
      offenceRelatedSexualInterests = 2,
      emotionalCongruence = 1
    )
    `when`(pniService.getPniScore(crn)).thenReturn(pniScore)

    // When
    val result = cohortService.determineOffenceCohort(crn)

    // Then
    assertThat(result).isEqualTo(OffenceCohort.SEXUAL_OFFENCE)
  }

  @Test
  fun `determineOffenceCohort should return SEXUAL_OFFENCE when both OSP and PNI criteria are met`() {
    // Given
    val crn = "TEST123"
    val pniScore = createBasePniScore(
      ospDc = "0.5",
      ospIic = "0.3",
      sexualPreOccupation = 1,
      offenceRelatedSexualInterests = 2,
      emotionalCongruence = 1
    )
    `when`(pniService.getPniScore(crn)).thenReturn(pniScore)

    // When
    val result = cohortService.determineOffenceCohort(crn)

    // Then
    assertThat(result).isEqualTo(OffenceCohort.SEXUAL_OFFENCE)
  }

  @Test
  fun `determineOffenceCohort should return GENERAL_OFFENCE when all scores are zero`() {
    // Given
    val crn = "TEST123"
    val pniScore = createBasePniScore(
      ospDc = "0.0",
      ospIic = "0.0",
      sexualPreOccupation = 0,
      offenceRelatedSexualInterests = 0,
      emotionalCongruence = 0
    )
    `when`(pniService.getPniScore(crn)).thenReturn(pniScore)

    // When
    val result = cohortService.determineOffenceCohort(crn)

    // Then
    assertThat(result).isEqualTo(OffenceCohort.GENERAL_OFFENCE)
  }

  @Test
  fun `determineOffenceCohort should return GENERAL_OFFENCE when OSP scores are null`() {
    // Given
    val crn = "TEST123"
    val pniScore = createBasePniScore(
      ospDc = null,
      ospIic = null,
      sexualPreOccupation = 0,
      offenceRelatedSexualInterests = 0,
      emotionalCongruence = 0
    )
    `when`(pniService.getPniScore(crn)).thenReturn(pniScore)

    // When
    val result = cohortService.determineOffenceCohort(crn)

    // Then
    assertThat(result).isEqualTo(OffenceCohort.GENERAL_OFFENCE)
  }

  @Test
  fun `determineOffenceCohort should return GENERAL_OFFENCE when OSP scores are invalid strings`() {
    // Given
    val crn = "TEST123"
    val pniScore = createBasePniScore(
      ospDc = "invalid",
      ospIic = "not_a_number",
      sexualPreOccupation = 0,
      offenceRelatedSexualInterests = 0,
      emotionalCongruence = 0
    )
    `when`(pniService.getPniScore(crn)).thenReturn(pniScore)

    // When
    val result = cohortService.determineOffenceCohort(crn)

    // Then
    assertThat(result).isEqualTo(OffenceCohort.GENERAL_OFFENCE)
  }

  @Test
  fun `determineOffenceCohort should return GENERAL_OFFENCE when sex domain scores are null`() {
    // Given
    val crn = "TEST123"
    val pniScore = createBasePniScore(
      ospDc = "0.0",
      ospIic = "0.0",
      sexualPreOccupation = null,
      offenceRelatedSexualInterests = null,
      emotionalCongruence = null
    )
    `when`(pniService.getPniScore(crn)).thenReturn(pniScore)

    // When
    val result = cohortService.determineOffenceCohort(crn)

    // Then
    assertThat(result).isEqualTo(OffenceCohort.GENERAL_OFFENCE)
  }

  @Test
  fun `determineOffenceCohort should return GENERAL_OFFENCE when OSP score is negative`() {
    // Given
    val crn = "TEST123"
    val pniScore = createBasePniScore(
      ospDc = "-0.1",
      ospIic = "0.0",
      sexualPreOccupation = 0,
      offenceRelatedSexualInterests = 0,
      emotionalCongruence = 0
    )
    `when`(pniService.getPniScore(crn)).thenReturn(pniScore)

    // When
    val result = cohortService.determineOffenceCohort(crn)

    // Then
    assertThat(result).isEqualTo(OffenceCohort.GENERAL_OFFENCE)
  }

  private fun createBasePniScore(
    ospDc: String?,
    ospIic: String?,
    sexualPreOccupation: Int?,
    offenceRelatedSexualInterests: Int?,
    emotionalCongruence: Int?
  ): PniScore {
    return PniScore(
      overallIntensity = OverallIntensity.MODERATE,
      domainScores = DomainScores(
        sexDomainScore = SexDomainScore(
          overallSexDomainLevel = NeedLevel.MEDIUM_NEED,
          individualSexScores = IndividualSexScores(
            sexualPreOccupation = sexualPreOccupation,
            offenceRelatedSexualInterests = offenceRelatedSexualInterests,
            emotionalCongruence = emotionalCongruence
          )
        ),
        thinkingDomainScore = ThinkingDomainScore(
          overallThinkingDomainLevel = NeedLevel.MEDIUM_NEED,
          individualThinkingScores = IndividualCognitiveScores(
            proCriminalAttitudes = 0,
            hostileOrientation = 0
          )
        ),
        relationshipDomainScore = RelationshipDomainScore(
          overallRelationshipDomainLevel = NeedLevel.MEDIUM_NEED,
          individualRelationshipScores = IndividualRelationshipScores(
            curRelCloseFamily = 0,
            prevCloseRelationships = 0,
            easilyInfluenced = 0,
            aggressiveControllingBehaviour = 0
          )
        ),
        selfManagementDomainScore = SelfManagementDomainScore(
          overallSelfManagementDomainLevel = NeedLevel.MEDIUM_NEED,
          individualSelfManagementScores = IndividualSelfManagementScores(
            impulsivity = 0,
            temperControl = 0,
            problemSolvingSkills = 0,
            difficultiesCoping = 0
          )
        )
      ),
      riskScore = RiskScore(
        classification = "LOW_RISK",
        individualRiskScores = IndividualRiskScores(
          ospDc = ospDc,
          ospIic = ospIic,
          ogrs3Risk = null,
          ovpRisk = null,
          rsr = null,
          sara = null
        )
      ),
      validationErrors = emptyList()
    )
  }
}