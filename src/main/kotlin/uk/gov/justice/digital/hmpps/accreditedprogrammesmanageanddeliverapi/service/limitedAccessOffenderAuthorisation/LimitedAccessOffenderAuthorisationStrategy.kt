package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service.limitedAccessOffenderAuthorisation

interface LimitedAccessOffenderAuthorisationStrategy {
  fun isSupportedPath(httpRequestMethod: String, httpRequestPath: String): Boolean
  fun isAuthorised(httpRequestPath: String, username: String): Boolean
}
