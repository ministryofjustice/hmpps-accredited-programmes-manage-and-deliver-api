package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.oasysApi.model.risksAndNeeds

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class OasysLearning(
  val workRelatedSkills: String?,
  val problemsReadWriteNum: String?,
  val learningDifficulties: String?,
  val problemAreas: List<String>?,
  val qualifications: String?,
  val basicSkillsScore: String?,
  val eTEIssuesDetails: String?,
  val crn: String?,
)
