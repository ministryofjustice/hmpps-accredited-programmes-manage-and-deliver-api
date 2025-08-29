package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.oasysApi.model.risksAndNeeds

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.risksAndNeeds.EducationTrainingAndEmployment
import java.time.LocalDate

@JsonIgnoreProperties(ignoreUnknown = true)
data class OasysEducationTrainingAndEmployment(
  val learningDifficulties: String?,
  val crn: String? = null,
)

fun OasysEducationTrainingAndEmployment.toModel(assessmentCompletedDate: LocalDate?) = EducationTrainingAndEmployment(
  assessmentCompleted = assessmentCompletedDate,
  learningDifficulties = learningDifficulties,
)
