package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.controller

import io.swagger.v3.oas.annotations.Parameter
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.deliveryLocationPreferences.CreateDeliveryLocationPreferences
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service.DeliveryLocationPreferencesService
import java.util.UUID

@RestController
@PreAuthorize("hasAnyRole('ROLE_ACCREDITED_PROGRAMMES_MANAGE_AND_DELIVER_API__ACPMAD_UI_WR')")
class DeliveryLocationPreferencesController(
  private val deliveryLocationPreferencesService: DeliveryLocationPreferencesService,
) {
  private val log = LoggerFactory.getLogger(this::class.java)

  @PostMapping("referral/{referralId}/delivery-location-preferences", produces = [MediaType.APPLICATION_JSON_VALUE])
  fun createDeliveryLocationPreferencesForReferral(
    @Parameter(description = "The id (UUID) of a referral", required = true)
    @PathVariable("referralId") referralId: UUID,
    @Parameter(
      description = "The delivery location preferences for a referral",
      required = true,
    ) @RequestBody createDeliveryLocationPreferences: CreateDeliveryLocationPreferences,
  ): ResponseEntity<UUID> {
    log.info("Received request to set Delivery Location preferences for ReferralId: $referralId")
    val deliveryLocationPreferences =
      deliveryLocationPreferencesService.createDeliveryLocationPreferences(
        referralId,
        createDeliveryLocationPreferences,
      )

    return ResponseEntity.status(HttpStatus.CREATED).body(deliveryLocationPreferences.id)
  }
}
