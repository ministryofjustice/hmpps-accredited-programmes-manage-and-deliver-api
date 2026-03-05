package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.controller

import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.ReferralStatusInfo
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service.ReferralStatusService
import java.util.UUID

/**
 * This controller is secured by a different role other endpoints as it will be called from external services
 * and we want to restrict any access they may have to only READ data.
 */
@RestController
@PreAuthorize("hasAnyRole('ACCREDITED_PROGRAMMES__MANAGE_AND_DELIVER__READ_ONLY')")
class StatusChangeDetailsController(
  private val referralStatusService: ReferralStatusService,
) {

  private val log = LoggerFactory.getLogger(this::class.java)

  @GetMapping("/referral/{referralId}/status-change-details")
  fun getStatusChangeDetails(@PathVariable referralId: UUID): ResponseEntity<ReferralStatusInfo> {
    log.info("Request to retrieve details of status change for referral with id: $referralId")
    val statusInfo = referralStatusService.getStatusChangeDetailsForReferral(referralId)

    return ResponseEntity.ok(statusInfo)
  }
}
