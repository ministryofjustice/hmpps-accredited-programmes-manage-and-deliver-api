package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.oasys

import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.oasysApi.model.risksAndNeeds.OasysLearning
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common.randomCrn

class OasysLearningFactory {
  private var workRelatedSkills: String? = "Limited recent work history"
  private var problemsReadWriteNum: String? = "Difficulty with numeracy"
  private var learningDifficulties: String? = "ADHD"
  private var problemAreas: List<String>? = listOf("Difficulty with concentration")
  private var qualifications: String? = "NVQ Level 2"
  private var basicSkillsScore: String? = "3"
  private var eTEIssuesDetails: String? = "ete issues"
  private var crn: String? = randomCrn()

  fun withWorkRelatedSkills(workRelatedSkills: String?) = apply { this.workRelatedSkills = workRelatedSkills }
  fun withProblemsReadWriteNum(problemsReadWriteNum: String?) = apply { this.problemsReadWriteNum = problemsReadWriteNum }
  fun withLearningDifficulties(learningDifficulties: String?) = apply { this.learningDifficulties = learningDifficulties }
  fun withProblemAreas(problemAreas: List<String>?) = apply { this.problemAreas = problemAreas }
  fun withQualifications(qualifications: String?) = apply { this.qualifications = qualifications }
  fun withBasicSkillsScore(basicSkillsScore: String?) = apply { this.basicSkillsScore = basicSkillsScore }
  fun withEteIssuesDetails(eTEIssuesDetails: String?) = apply { this.eTEIssuesDetails = eTEIssuesDetails }
  fun withCrn(crn: String?) = apply { this.crn = crn }

  fun produce() = OasysLearning(
    workRelatedSkills = this.workRelatedSkills,
    problemsReadWriteNum = this.problemsReadWriteNum,
    learningDifficulties = this.learningDifficulties,
    problemAreas = this.problemAreas,
    qualifications = this.qualifications,
    basicSkillsScore = this.basicSkillsScore,
    crn = this.crn,
    eTEIssuesDetails = this.eTEIssuesDetails,
  ).apply { this.eTEIssuesDetails = eTEIssuesDetails }
}
