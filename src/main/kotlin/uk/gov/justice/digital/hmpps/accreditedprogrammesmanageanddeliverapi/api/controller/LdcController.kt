package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.controller

import io.swagger.v3.oas.annotations.Parameter
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.ldc.UpdateLdc
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service.LdcService
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service.ReferralService
import java.util.UUID

@RestController
@PreAuthorize("hasAnyRole('ROLE_ACCREDITED_PROGRAMMES_MANAGE_AND_DELIVER_API__ACPMAD_UI_WR')")
class LdcController(
  private val ldcService: LdcService,
  private val referralService: ReferralService,
) {

  @PostMapping(
    "/referral/{referralId}/update-ldc",
    produces = [MediaType.APPLICATION_JSON_VALUE],
    consumes = [MediaType.APPLICATION_JSON_VALUE],
  )
  fun updateLdcStatusForReferral(
    @Parameter(
      description = "The referralId (UUID) of a referral",
      required = true,
    )
    @PathVariable("referralId") referralId: UUID,
    @Parameter(
      description = "Does the person associated with the referral have LDC needs.",
      required = true,
    )
    @Valid
    @RequestBody updateLdc: UpdateLdc,
  ): ResponseEntity<Void> {
    val referral = referralService.getReferralById(referralId)
    ldcService.updateLdcStatusForReferral(referral, updateLdc)
    return ResponseEntity.status(HttpStatus.OK).build()
  }
}
