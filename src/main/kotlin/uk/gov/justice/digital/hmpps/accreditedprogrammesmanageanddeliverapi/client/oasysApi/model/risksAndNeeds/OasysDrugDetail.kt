package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.oasysApi.model.risksAndNeeds

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.risksAndNeeds.DrugDetails
import java.time.LocalDate

@JsonIgnoreProperties(ignoreUnknown = true)
data class OasysDrugDetail(

  val levelOfUseOfMainDrug: String? = null,
  val drugsMajorActivity: String? = null,
  val crn: String? = null,
)

fun OasysDrugDetail.toModel(assessmentCompletedDate: LocalDate?) = DrugDetails(
  assessmentCompleted = assessmentCompletedDate,
  levelOfUseOfMainDrug = levelOfUseOfMainDrug,
  drugsMajorActivity = drugsMajorActivity,
)
