package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.risksAndNeeds

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.oasysApi.model.risksAndNeeds.OasysAccommodation
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.oasysApi.model.risksAndNeeds.OasysLearning
import java.time.LocalDate

/**
 * API-facing representation of Learning Needs derived from OASYS section 4 (Education, Training & Employment).
 */
data class LearningNeeds(

  @Schema(example = "1 August 2023")
  @JsonFormat(pattern = "d MMMM yyyy")
  @get:JsonProperty("assessmentCompleted") val assessmentCompleted: LocalDate? = null,

  @Schema(example = "true", description = "Whether the person has a fixed abode or is living at a temporary address")
  @get:JsonProperty("noFixedAbodeOrTransient") val noFixedAbodeOrTransient: Boolean? = false,

  @Schema(example = "1-Some problems")
  @get:JsonProperty("workRelatedSkills") val workRelatedSkills: String? = null,

  @Schema(example = "0-No problems")
  @get:JsonProperty("problemsReadWriteNum") val problemsReadWriteNum: String? = null,

  @Schema(example = "2-Significant problems")
  @get:JsonProperty("learningDifficulties") val learningDifficulties: String? = null,

  @Schema(example = "[\"Difficulty with concentration\"]")
  @get:JsonProperty("problemAreas") val problemAreas: List<String>? = null,

  @Schema(example = "0 - Any qualifications")
  @get:JsonProperty("qualifications") val qualifications: String? = null,

  @Schema(example = "3")
  @get:JsonProperty("basicSkillsScore") val basicSkillsScore: String? = null,

  @Schema(example = "Ms Puckett spoke of wanting to secure suitable employment although she knows that she will first need to fully address her drug issues.")
  @get:JsonProperty("basicSkillsScoreDescription") val basicSkillsScoreDescription: String? = null,
)

fun buildLearningNeeds(assessmentCompleted: LocalDate?, oasysLearning: OasysLearning?, oasysAccommodation: OasysAccommodation): LearningNeeds = LearningNeeds(
  noFixedAbodeOrTransient = oasysAccommodation.noFixedAbodeOrTransient == "Yes",
  assessmentCompleted = assessmentCompleted,
  workRelatedSkills = oasysLearning?.workRelatedSkills,
  problemsReadWriteNum = oasysLearning?.problemsReadWriteNum,
  learningDifficulties = oasysLearning?.learningDifficulties,
  problemAreas = oasysLearning?.problemAreas,
  qualifications = oasysLearning?.qualifications,
  basicSkillsScore = oasysLearning?.basicSkillsScore,
  basicSkillsScoreDescription = oasysLearning?.eTEIssuesDetails,
)
