package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service

import uk.gov.justice.hmpps.kotlin.sar.HmppsProbationSubjectAccessRequestService
import uk.gov.justice.hmpps.kotlin.sar.HmppsSubjectAccessRequestContent
import java.time.LocalDate

class SubjectAccessRequestService : HmppsProbationSubjectAccessRequestService {
  override fun getProbationContentFor(
    crn: String,
    fromDate: LocalDate?,
    toDate: LocalDate?,
  ): HmppsSubjectAccessRequestContent? {
    TODO("Not yet implemented")
  }
}
