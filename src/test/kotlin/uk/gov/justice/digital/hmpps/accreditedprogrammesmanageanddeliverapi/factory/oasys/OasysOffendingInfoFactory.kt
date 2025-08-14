package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.oasys

import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.oasysApi.model.risksAndNeeds.OasysOffendingInfo
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common.randomCrn
import java.time.LocalDateTime
import kotlin.random.Random

class OasysOffendingInfoFactory {
  private var ospCRisk: String? = listOf("LOW", "MEDIUM", "HIGH", "VERY_HIGH").random()
  private var ospIRisk: String? = listOf("LOW", "MEDIUM", "HIGH", "VERY_HIGH").random()
  private var ospIICRisk: String? = listOf("LOW", "MEDIUM", "HIGH", "VERY_HIGH").random()
  private var ospDCRisk: String? = listOf("LOW", "MEDIUM", "HIGH", "VERY_HIGH").random()
  private var crn: String? = randomCrn()
  private var latestCompleteDate: LocalDateTime = LocalDateTime.now().minusDays(Random.nextLong(1, 365))

  fun withOspCRisk(ospCRisk: String?) = apply { this.ospCRisk = ospCRisk }
  fun withOspIRisk(ospIRisk: String?) = apply { this.ospIRisk = ospIRisk }
  fun withOspIICRisk(ospIICRisk: String?) = apply { this.ospIICRisk = ospIICRisk }
  fun withOspDCRisk(ospDCRisk: String?) = apply { this.ospDCRisk = ospDCRisk }
  fun withCrn(crn: String?) = apply { this.crn = crn }
  fun withLatestCompleteDate(latestCompleteDate: LocalDateTime) = apply { this.latestCompleteDate = latestCompleteDate }

  fun produce() = OasysOffendingInfo(
    ospCRisk = this.ospCRisk,
    ospIRisk = this.ospIRisk,
    ospIICRisk = this.ospIICRisk,
    ospDCRisk = this.ospDCRisk,
    crn = this.crn,
    latestCompleteDate = this.latestCompleteDate,
  )
}
