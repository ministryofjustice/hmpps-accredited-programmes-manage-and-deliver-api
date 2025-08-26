package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.oasys

import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.oasysApi.model.risksAndNeeds.OasysDrugDetail
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common.randomCrn

class OasysDrugDetailFactory {
  private var levelOfUseOfMainDrug: String? = "1 - Some problems"
  private var drugsMajorActivity: String? = "2 - At least once a week"
  private var crn: String? = randomCrn()

  fun withLevelOfUseOfMainDrug(levelOfUseOfMainDrug: String?) = apply {
    this.levelOfUseOfMainDrug = levelOfUseOfMainDrug
  }

  fun withDrugsMajorActivity(drugsMajorActivity: String?) = apply { this.drugsMajorActivity = drugsMajorActivity }

  fun withCrn(crn: String?) = apply { this.crn = crn }

  fun produce() = OasysDrugDetail(
    levelOfUseOfMainDrug = this.levelOfUseOfMainDrug,
    drugsMajorActivity = this.drugsMajorActivity,
    crn = this.crn,
  )
}
