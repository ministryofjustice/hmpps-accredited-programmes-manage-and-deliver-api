package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.oasys

import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.oasysApi.model.risksAndNeeds.OasysRoshSummary
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.oasysApi.model.risksAndNeeds.ScoreLevel
import kotlin.random.Random

class OasysRoshSummaryFactory {
  private var riskPrisonersCustody: ScoreLevel? = if (Random.nextBoolean()) ScoreLevel.entries.random() else null
  private var riskStaffCustody: ScoreLevel? = if (Random.nextBoolean()) ScoreLevel.entries.random() else null
  private var riskStaffCommunity: ScoreLevel? = if (Random.nextBoolean()) ScoreLevel.entries.random() else null
  private var riskKnownAdultCustody: ScoreLevel? = if (Random.nextBoolean()) ScoreLevel.entries.random() else null
  private var riskKnownAdultCommunity: ScoreLevel? = if (Random.nextBoolean()) ScoreLevel.entries.random() else null
  private var riskPublicCustody: ScoreLevel? = if (Random.nextBoolean()) ScoreLevel.entries.random() else null
  private var riskPublicCommunity: ScoreLevel? = if (Random.nextBoolean()) ScoreLevel.entries.random() else null
  private var riskChildrenCustody: ScoreLevel? = if (Random.nextBoolean()) ScoreLevel.entries.random() else null
  private var riskChildrenCommunity: ScoreLevel? = if (Random.nextBoolean()) ScoreLevel.entries.random() else null

  fun withRiskPrisonersCustody(riskPrisonersCustody: ScoreLevel?) = apply { this.riskPrisonersCustody = riskPrisonersCustody }

  fun withRiskStaffCustody(riskStaffCustody: ScoreLevel?) = apply { this.riskStaffCustody = riskStaffCustody }
  fun withRiskStaffCommunity(riskStaffCommunity: ScoreLevel?) = apply { this.riskStaffCommunity = riskStaffCommunity }
  fun withRiskKnownAdultCustody(riskKnownAdultCustody: ScoreLevel?) = apply { this.riskKnownAdultCustody = riskKnownAdultCustody }

  fun withRiskKnownAdultCommunity(riskKnownAdultCommunity: ScoreLevel?) = apply { this.riskKnownAdultCommunity = riskKnownAdultCommunity }

  fun withRiskPublicCustody(riskPublicCustody: ScoreLevel?) = apply { this.riskPublicCustody = riskPublicCustody }
  fun withRiskPublicCommunity(riskPublicCommunity: ScoreLevel?) = apply { this.riskPublicCommunity = riskPublicCommunity }

  fun withRiskChildrenCustody(riskChildrenCustody: ScoreLevel?) = apply { this.riskChildrenCustody = riskChildrenCustody }

  fun withRiskChildrenCommunity(riskChildrenCommunity: ScoreLevel?) = apply { this.riskChildrenCommunity = riskChildrenCommunity }

  fun produce() = OasysRoshSummary(
    riskPrisonersCustody = this.riskPrisonersCustody,
    riskStaffCustody = this.riskStaffCustody,
    riskStaffCommunity = this.riskStaffCommunity,
    riskKnownAdultCustody = this.riskKnownAdultCustody,
    riskKnownAdultCommunity = this.riskKnownAdultCommunity,
    riskPublicCustody = this.riskPublicCustody,
    riskPublicCommunity = this.riskPublicCommunity,
    riskChildrenCustody = this.riskChildrenCustody,
    riskChildrenCommunity = this.riskChildrenCommunity,
  )
}
