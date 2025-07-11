package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory

import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common.randomSentence
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common.randomUppercaseString
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ReferralCaselistItemViewEntity

class ReferralCaselistItemViewFactory

fun ReferralCaselistItemViewFactory.produce(
  crn: String = randomUppercaseString(6),
  personName: String = randomSentence(wordRange = 1..3),
  status: String = randomUppercaseString(6),
) = ReferralCaselistItemViewEntity(
  crn = crn,
  personName = personName,
  status = status,
)
