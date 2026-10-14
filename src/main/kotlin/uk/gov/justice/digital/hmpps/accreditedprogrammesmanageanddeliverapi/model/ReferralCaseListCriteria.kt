package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.model

import org.springframework.data.domain.Pageable
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.controller.OpenOrClosed
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.OffenceCohort
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup.ProgrammeGroupCohort

data class ReferralCaseListCriteria(
  val pageable: Pageable,
  var openOrClosed: OpenOrClosed,
  val username: String,
  val caseReferenceNumberOrPersonName: String?,
  var offenceCohortAndLdcPairs: List<Pair<OffenceCohort, Boolean>> = emptyList(),
  var statuses: List<String>?,
  val sexes: List<String>?,
  val probationDeliveryUnits: List<String>?,
  val reportingTeams: List<String>?,
  val cohorts: List<ProgrammeGroupCohort>?,
)
