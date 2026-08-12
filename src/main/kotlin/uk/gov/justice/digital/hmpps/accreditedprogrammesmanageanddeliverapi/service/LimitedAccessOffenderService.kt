package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.ClientResult
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.probationAccessControlApi.ProbationAccessControlApiClient
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.probationAccessControlApi.model.AllCaseAccess

@Service
class LimitedAccessOffenderService(
  private val probationAccessControlApiClient: ProbationAccessControlApiClient,
) {
  val log = LoggerFactory.getLogger(this::class.java)

  fun getLimitedAccessOffenderByCrn(crn: String): Boolean = when (val response = probationAccessControlApiClient.getCaseAccessByCrn(crn)) {
    is ClientResult.Success -> response.body.excludedFrom.isNotEmpty() || response.body.restrictedTo.isNotEmpty()
    is ClientResult.Failure -> false
  }

  fun isLimitedAccessOffender(crn: String, caseAccessByCrn: Map<String, AllCaseAccess>?): Boolean = (
    caseAccessByCrn?.get(crn)?.excludedFrom?.isNotEmpty()
      ?: false
    ) ||
    (caseAccessByCrn?.get(crn)?.restrictedTo?.isNotEmpty()) ?: false

  fun isExcludedByUsername(
    crn: String,
    username: String,
    caseAccessByCrn: Map<String, AllCaseAccess>?,
  ): Boolean = caseAccessByCrn?.get(crn)?.excludedFrom?.any { it.username == username } ?: false

  fun getCaseAccessByCrn(crn: String): AllCaseAccess = when (val response = probationAccessControlApiClient.getCaseAccessByCrn(crn)) {
    is ClientResult.Success -> response.body

    is ClientResult.Failure -> {
      val exception = response.toException()
      log.error("Failed to retrieve LAO case access for CRN $crn: ${response.getErrorMessage()}", exception)
      throw response.toException()
    }
  }
}
