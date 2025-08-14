package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.PniScore
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.oasysApi.model.PniResponse
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.oasysApi.model.toPniScore

@Service
class LdcNeedsService(
  private val pniService: PniService,
) {
  companion object {
    private const val LDC_NEEDS_THRESHOLD = 3
  }

  fun hasLdcNeeds(crn: String): Boolean {
    val pniScore = pniService.getRawPniResponse(crn)
    val ldcScore = pniScore.assessment?.ldc?.score

    return ldcScore != null && ldcScore >= LDC_NEEDS_THRESHOLD
  }
}
