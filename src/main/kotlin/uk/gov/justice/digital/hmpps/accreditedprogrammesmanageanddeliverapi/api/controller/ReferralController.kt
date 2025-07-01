package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.MediaType
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.dto.ReferralDto
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service.ReferralService
import java.util.UUID

@RestController
@Tag(
  name = "Referral",
  description = """
    A series of endpoints for managing referrals.
  """,
)
class ReferralController(
  private val referralService: ReferralService
) {

  @Operation(
    tags = ["Referral"],
    summary = "Get referral by ID",
    operationId = "getReferralByld",
    description = """""",
    responses = [], // TODO Update response DTO once we have confirmation from nDelius
    security = [SecurityRequirement(name = "bearerAuth")],
  )
  @PreAuthorize("hasAnyRole('ROLE_ACCREDITED_PROGRAMMES_MANAGE_AND_DELIVER_API__ACPMAD_UI_WR')")
  @GetMapping("/referral/{id}", produces = [MediaType.APPLICATION_JSON_VALUE])
  fun getReferralByld(@PathVariable("id") id: String) : ReferralDto {
    return referralService.getReferralById(id);
  }
}
