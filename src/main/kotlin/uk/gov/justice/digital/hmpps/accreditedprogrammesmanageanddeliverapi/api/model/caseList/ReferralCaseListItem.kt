package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.caseList

import org.springframework.data.domain.Page
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.OffenceCohort
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ReferralCaseListItemViewEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ReferralEntitySourcedFrom
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.utils.ReferralStatusUtils
import java.time.LocalDate
import java.util.UUID

data class ReferralCaseListItem(
  val referralId: UUID,
  val crn: String,
  val personName: String,
  val sex: String?,
  val referralStatus: String,
  val statusLabelColour: String,
  val cohort: OffenceCohort,
  val hasLdc: Boolean,
  val pdu: String,
  val reportingTeam: String,
  val sentenceEndDate: LocalDate?,
  val sentenceEndDateSource: ReferralEntitySourcedFrom?,
  val lao: Boolean? = false,
  val isExcluded: Boolean? = false,
)

fun ReferralCaseListItemViewEntity.toApi(lao: Boolean? = false, isExcluded: Boolean = false) = ReferralCaseListItem(
  referralId = referralId,
  crn = crn,
  personName = personName,
  sex = sex,
  referralStatus = ReferralStatusUtils.formatStatus(status),
  statusLabelColour = statusLabelColour,
  cohort = OffenceCohort.valueOf(cohort),
  hasLdc = hasLdc,
  pdu = pduName,
  reportingTeam = reportingTeam,
  sentenceEndDate = sentenceEndDate,
  sentenceEndDateSource = sentenceEndDateSource,
  lao = lao,
  isExcluded = isExcluded,
)

data class CaseListReferrals(
  val pagedReferrals: Page<ReferralCaseListItem>,
  val otherTabTotal: Int,
  val filters: CaseListFilterValues,
)
