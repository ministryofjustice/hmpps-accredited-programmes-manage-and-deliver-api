package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.oasysApi.model.NeedLevel
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.oasysApi.model.PniResponse

@Schema(description = "Domain scores from PNI assessment")
data class DomainScores(

  @Schema(description = "Sex domain assessment scores", required = true)
  @get:JsonProperty("SexDomainScore") val sexDomainScore: SexDomainScore,
  @Schema(description = "Thinking domain assessment scores", required = true)
  @get:JsonProperty("ThinkingDomainScore") val thinkingDomainScore: ThinkingDomainScore,
  @Schema(description = "Relationship domain assessment scores", required = true)
  @get:JsonProperty("RelationshipDomainScore") val relationshipDomainScore: RelationshipDomainScore,
  @Schema(description = "Self-management domain assessment scores", required = true)
  @get:JsonProperty("SelfManagementDomainScore") val selfManagementDomainScore: SelfManagementDomainScore,
) {
  companion object {
    fun from(pniResponse: PniResponse): DomainScores = DomainScores(
      sexDomainScore = SexDomainScore(
        overallSexDomainLevel = NeedLevel.fromLevel(pniResponse.pniCalculation?.sexDomain?.level),
        individualSexScores = IndividualSexScores(
          sexualPreOccupation = pniResponse.assessment?.questions?.sexualPreOccupation?.score,
          offenceRelatedSexualInterests = pniResponse.assessment?.questions?.offenceRelatedSexualInterests?.score,
          emotionalCongruence = pniResponse.assessment?.questions?.emotionalCongruence?.score,
        ),
      ),
      thinkingDomainScore = ThinkingDomainScore(
        overallThinkingDomainLevel = NeedLevel.fromLevel(pniResponse.pniCalculation?.thinkingDomain?.level),
        individualThinkingScores = IndividualCognitiveScores(
          proCriminalAttitudes = pniResponse.assessment?.questions?.proCriminalAttitudes?.score,
          hostileOrientation = pniResponse.assessment?.questions?.hostileOrientation?.score,
        ),
      ),
      relationshipDomainScore = RelationshipDomainScore(
        overallRelationshipDomainLevel = NeedLevel.fromLevel(pniResponse.pniCalculation?.relationshipDomain?.level),
        individualRelationshipScores = IndividualRelationshipScores(
          curRelCloseFamily = pniResponse.assessment?.questions?.relCloseFamily?.score,
          prevCloseRelationships = pniResponse.assessment?.questions?.prevCloseRelationships?.score,
          easilyInfluenced = pniResponse.assessment?.questions?.easilyInfluenced?.score,
          aggressiveControllingBehaviour = pniResponse.assessment?.questions?.aggressiveControllingBehaviour?.score,
        ),
      ),
      selfManagementDomainScore = SelfManagementDomainScore(
        overallSelfManagementDomainLevel = NeedLevel.fromLevel(pniResponse.pniCalculation?.selfManagementDomain?.level),
        individualSelfManagementScores = IndividualSelfManagementScores(
          impulsivity = pniResponse.assessment?.questions?.impulsivity?.score,
          temperControl = pniResponse.assessment?.questions?.temperControl?.score,
          problemSolvingSkills = pniResponse.assessment?.questions?.problemSolvingSkills?.score,
          difficultiesCoping = pniResponse.assessment?.questions?.difficultiesCoping?.score,
        ),
      ),
    )
  }
}

data class SexDomainScore(
  @Schema(example = "2", required = true)
  @get:JsonProperty("overallSexDomainLevel") val overallSexDomainLevel: NeedLevel?,
  @get:JsonProperty("individualSexScores") val individualSexScores: IndividualSexScores,
)

data class ThinkingDomainScore(
  @get:JsonProperty("overallThinkingDomainLevel") val overallThinkingDomainLevel: NeedLevel?,
  @get:JsonProperty("individualThinkingScores") val individualThinkingScores: IndividualCognitiveScores,
)

data class RelationshipDomainScore(
  @get:JsonProperty("overallRelationshipDomainLevel") val overallRelationshipDomainLevel: NeedLevel?,
  @get:JsonProperty("individualRelationshipScores") val individualRelationshipScores: IndividualRelationshipScores,
)

data class SelfManagementDomainScore(
  @get:JsonProperty("overallSelfManagementDomainLevel") val overallSelfManagementDomainLevel: NeedLevel?,
  @get:JsonProperty("individualSelfManagementScores") val individualSelfManagementScores: IndividualSelfManagementScores,
)
