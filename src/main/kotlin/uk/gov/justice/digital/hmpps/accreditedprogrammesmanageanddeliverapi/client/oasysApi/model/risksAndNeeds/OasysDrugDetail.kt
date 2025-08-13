package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.oasysApi.model.risksAndNeeds

import com.fasterxml.jackson.annotation.JsonAlias
import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class OasysDrugDetail(
  @JsonAlias("LevelOfUseOfMainDrug")
  val levelOfUseOfMainDrug: String?,
  @JsonAlias("DrugsMajorActivity")
  val drugsMajorActivity: String?,
)
