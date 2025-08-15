package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.risksAndNeeds

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.oasysApi.model.risksAndNeeds.OasysLearning

/**
 * API-facing representation of Learning Needs derived from OASYS section 4 (Education, Training & Employment).
 */
data class LearningNeeds(
  @Schema(example = "Limited recent work history")
  @get:JsonProperty("workRelatedSkills") val workRelatedSkills: String? = null,

  @Schema(example = "Difficulty with numeracy")
  @get:JsonProperty("problemsReadWriteNum") val problemsReadWriteNum: String? = null,

  @Schema(example = "ADHD")
  @get:JsonProperty("learningDifficulties") val learningDifficulties: String? = null,

  @Schema(example = "[\"Difficulty with concentration\"]")
  @get:JsonProperty("problemAreas") val problemAreas: List<String>? = null,

  @Schema(example = "NVQ Level 2")
  @get:JsonProperty("qualifications") val qualifications: String? = null,

  @Schema(example = "3")
  @get:JsonProperty("basicSkillsScore") val basicSkillsScore: String? = null,

  @Schema(example = "Issues with ETE engagement")
  @get:JsonProperty("eTEIssuesDetails") val eTEIssuesDetails: String? = null,
)

fun buildLearningNeeds(oasysLearning: OasysLearning?): LearningNeeds = LearningNeeds(
  workRelatedSkills = oasysLearning?.workRelatedSkills,
  problemsReadWriteNum = oasysLearning?.problemsReadWriteNum,
  learningDifficulties = oasysLearning?.learningDifficulties,
  problemAreas = oasysLearning?.problemAreas,
  qualifications = oasysLearning?.qualifications,
  basicSkillsScore = oasysLearning?.basicSkillsScore,
  eTEIssuesDetails = oasysLearning?.eTEIssuesDetails,
)
