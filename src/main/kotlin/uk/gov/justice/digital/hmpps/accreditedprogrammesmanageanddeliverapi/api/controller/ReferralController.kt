package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import org.slf4j.LoggerFactory
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.DeliveryLocationPreferences
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.ErrorResponse
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.OffenceHistory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.PersonalDetails
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.Referral
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.ReferralDetails
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.ReferralStatusHistory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.ReferralStatusTransitions
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.SentenceInformation
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.toModel
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.RequirementOrLicenceConditionManager
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common.exception.BusinessException
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common.exception.NotFoundException
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.model.create.CreateReferralStatusHistory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.model.update.UpdateCohort
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service.DeliveryLocationPreferencesService
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service.OffenceService
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service.ReferralService
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service.ReferralStatusService
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service.SentenceService
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service.UserService
import java.util.UUID

@RestController
@PreAuthorize("hasAnyRole('ROLE_ACCREDITED_PROGRAMMES_MANAGE_AND_DELIVER_API__ACPMAD_UI_WR')")
class ReferralController(
  private val referralService: ReferralService,
  private val userService: UserService,
  private val offenceService: OffenceService,
  private val sentenceService: SentenceService,
  private val deliveryLocationPreferencesService: DeliveryLocationPreferencesService,
  private val referralStatusService: ReferralStatusService,
) {

  private val log = LoggerFactory.getLogger(this::class.java)

  @Operation(
    tags = ["Referrals"],
    summary = "Retrieve a referral",
    operationId = "getReferralDetailsById",
    description = """""",
    responses = [
      ApiResponse(
        responseCode = "200",
        description = "Information about the referral",
        content = [Content(schema = Schema(implementation = ReferralDetails::class))],
      ),
      ApiResponse(
        responseCode = "401",
        description = "The request was unauthorised",
        content = [Content(schema = Schema(implementation = ErrorResponse::class))],
      ),
      ApiResponse(
        responseCode = "403",
        description = "Forbidden.  The client is not authorised to access this referral.",
        content = [Content(schema = Schema(implementation = ErrorResponse::class))],
      ),
      ApiResponse(
        responseCode = "404",
        description = "The referral does not exist",
        content = [Content(schema = Schema(implementation = ErrorResponse::class))],
      ),
    ],
    security = [SecurityRequirement(name = "bearerAuth")],
  )
  @GetMapping("/referral-details/{id}", produces = [MediaType.APPLICATION_JSON_VALUE])
  suspend fun getReferralDetailsById(
    @Parameter(description = "The id (UUID) of a referral", required = true)
    @PathVariable("id") id: UUID,
  ): ResponseEntity<ReferralDetails> = referralService.refreshPersonalDetailsForReferral(id)?.let {
    ResponseEntity.ok(it)
  } ?: throw NotFoundException("Referral with id $id not found")

  @Operation(
    tags = ["Referrals"],
    summary = "Retrieve personal details for a referral",
    operationId = "getPersonalDetailsByIdentifier",
    description = """""",
    responses = [
      ApiResponse(
        responseCode = "200",
        description = "Information about the referral",
        content = [Content(schema = Schema(implementation = PersonalDetails::class))],
      ),
      ApiResponse(
        responseCode = "401",
        description = "The request was unauthorised",
        content = [Content(schema = Schema(implementation = ErrorResponse::class))],
      ),
      ApiResponse(
        responseCode = "403",
        description = "Forbidden.  The client is not authorised to access this referral.",
        content = [Content(schema = Schema(implementation = ErrorResponse::class))],
      ),
      ApiResponse(
        responseCode = "404",
        description = "The referral does not exist",
        content = [Content(schema = Schema(implementation = ErrorResponse::class))],
      ),
    ],
    security = [SecurityRequirement(name = "bearerAuth")],
  )
  @GetMapping("/referral-details/{id}/personal-details", produces = [MediaType.APPLICATION_JSON_VALUE])
  fun getPersonalDetailsByReferralId(
    @Parameter(description = "The id (UUID) of a referral", required = true)
    @PathVariable("id") id: UUID,
  ): ResponseEntity<PersonalDetails> {
    val referral = referralService.getReferralById(id)
    return userService.getPersonalDetailsByIdentifier(referral.crn).let {
      ResponseEntity.ok(it.toModel(referral.setting))
    } ?: throw NotFoundException("Personal details not found for crn ${referral.crn} not found")
  }

  @Operation(
    tags = ["Referrals"],
    summary = "Retrieve offence history for a referral",
    operationId = "getOffenceHistoryByReferralId",
    responses = [
      ApiResponse(
        responseCode = "200",
        description = "Information about the offence history",
        content = [Content(schema = Schema(implementation = OffenceHistory::class))],
      ),
      ApiResponse(
        responseCode = "400",
        description = "The offence history could not be retrieved due to missing data on the referral",
        content = [Content(schema = Schema(implementation = ErrorResponse::class))],
      ),
      ApiResponse(
        responseCode = "401",
        description = "The request was unauthorised",
        content = [Content(schema = Schema(implementation = ErrorResponse::class))],
      ),
      ApiResponse(
        responseCode = "403",
        description = "Forbidden.  The client is not authorised to access this referral.",
        content = [Content(schema = Schema(implementation = ErrorResponse::class))],
      ),
      ApiResponse(
        responseCode = "404",
        description = "The referral does not exist",
        content = [Content(schema = Schema(implementation = ErrorResponse::class))],
      ),
      ApiResponse(
        responseCode = "404",
        description = "The offence history for the referral does not exist",
        content = [Content(schema = Schema(implementation = ErrorResponse::class))],
      ),
    ],
    security = [SecurityRequirement(name = "bearerAuth")],
  )
  @GetMapping("/referral-details/{id}/offence-history", produces = [MediaType.APPLICATION_JSON_VALUE])
  fun getOffenceHistoryByReferralId(
    @Parameter(description = "The id (UUID) of a referral", required = true)
    @PathVariable("id") id: UUID,
  ): ResponseEntity<OffenceHistory> {
    val referral = referralService.getReferralById(id)
    return offenceService.getOffenceHistory(referral).let { ResponseEntity.ok(it) }
      ?: throw NotFoundException("Offence history not found for crn ${referral.crn} and referral with id $id")
  }

  @Operation(
    tags = ["Referrals"],
    summary = "Retrieve sentence information for a referral",
    operationId = "getSentenceInformationByReferralId",
    description = """""",
    responses = [
      ApiResponse(
        responseCode = "200",
        description = "Information about the sentence",
        content = [Content(schema = Schema(implementation = SentenceInformation::class))],
      ),
      ApiResponse(
        responseCode = "401",
        description = "The request was unauthorised",
        content = [Content(schema = Schema(implementation = ErrorResponse::class))],
      ),
      ApiResponse(
        responseCode = "403",
        description = "Forbidden.  The client is not authorised to access this referral.",
        content = [Content(schema = Schema(implementation = ErrorResponse::class))],
      ),
      ApiResponse(
        responseCode = "404",
        description = "The referral does not exist",
        content = [Content(schema = Schema(implementation = ErrorResponse::class))],
      ),
    ],
    security = [SecurityRequirement(name = "bearerAuth")],
  )
  @GetMapping("/referral-details/{id}/sentence-information", produces = [MediaType.APPLICATION_JSON_VALUE])
  fun getSentenceInformationByReferralId(
    @Parameter(description = "The id (UUID) of a referral", required = true)
    @PathVariable("id") id: UUID,
  ): ResponseEntity<SentenceInformation> {
    val referral = referralService.getReferralById(id)
    if (referral.eventNumber == null) {
      log.error("Referral with id $id has null eventNumber")
      throw BusinessException("Referral with id $id has null eventNumber")
    }
    return sentenceService.getSentenceInformationByIdentifier(referral.crn, referral.eventNumber).let {
      ResponseEntity.ok(it.toModel())
    } ?: throw NotFoundException("Sentence information not found for crn ${referral.crn} not found")
  }

  @Operation(
    tags = ["Referrals"],
    summary = "Update cohort information for a referral",
    operationId = "updateCohortForReferral",
    description = """""",
    responses = [
      ApiResponse(
        responseCode = "204",
        description = "No content - cohort updated successfully",
      ),
      ApiResponse(
        responseCode = "401",
        description = "The request was unauthorised",
        content = [Content(schema = Schema(implementation = ErrorResponse::class))],
      ),
      ApiResponse(
        responseCode = "403",
        description = "Forbidden.  The client is not authorised to access this referral.",
        content = [Content(schema = Schema(implementation = ErrorResponse::class))],
      ),
      ApiResponse(
        responseCode = "404",
        description = "The referral does not exist",
        content = [Content(schema = Schema(implementation = ErrorResponse::class))],
      ),
    ],
    security = [SecurityRequirement(name = "bearerAuth")],
  )
  @PutMapping("/referral/{id}/update-cohort", produces = [MediaType.APPLICATION_JSON_VALUE])
  fun updateCohortForReferral(
    @Parameter(
      description = "The id (UUID) of a referral allowed values SEXUAL_OFFENCE or GENERAL_OFFENCE",
      required = true,
    )
    @PathVariable("id") id: UUID,
    @Parameter(
      description = "Cohort to update the referral with",
      required = true,
    ) @RequestBody updateCohort: UpdateCohort,
  ): ResponseEntity<Referral?> {
    val referral = referralService.getReferralById(id)
    val updateCohort = referralService.updateCohort(referral, updateCohort.cohort)
    return ResponseEntity.ok(updateCohort)
  }

  @Operation(
    tags = ["Referrals"],
    summary = "Update the Status of a Referral",
    operationId = "updateStatusForReferral",
    description = """Updates the Status of a Referral, by creating a new entry in the log of Referral Statuses""",
    responses = [
      ApiResponse(
        responseCode = "201",
        description = "Referral Status updated successfully",
        content = [Content(schema = Schema(implementation = ReferralStatusHistory::class))],
      ),
      ApiResponse(
        responseCode = "401",
        description = "The request was unauthorised",
        content = [Content(schema = Schema(implementation = ErrorResponse::class))],
      ),
      ApiResponse(
        responseCode = "403",
        description = "Forbidden.  The client is not authorised to access this referral.",
        content = [Content(schema = Schema(implementation = ErrorResponse::class))],
      ),
      ApiResponse(
        responseCode = "404",
        description = "The referral does not exist",
        content = [Content(schema = Schema(implementation = ErrorResponse::class))],
      ),
    ],
    security = [SecurityRequirement(name = "bearerAuth")],
  )
  @PostMapping("/referral/{id}/status-history", produces = [MediaType.APPLICATION_JSON_VALUE])
  fun updateStatusForReferral(
    @Parameter(
      description = "The id (UUID) of a Referral",
      required = true,
    )
    @PathVariable("id") id: UUID,
    @Parameter(
      description = "Details of the new Referral Status to assign",
      required = true,
    ) @RequestBody updateReferralStatus: CreateReferralStatusHistory,
  ): ResponseEntity<ReferralStatusHistory> {
    val referral = referralService.getReferralById(id)

    val result = referralService.updateStatus(
      referral,
      updateReferralStatus.referralStatusDescriptionId,
      additionalDetails = updateReferralStatus.additionalDetails,
      createdBy = SecurityContextHolder.getContext().authentication?.name ?: "UNKNOWN_USER",
    )

    return ResponseEntity.ok(result)
  }

  @Operation(
    tags = ["Referrals"],
    summary = "Get the Status History for a Referral",
    operationId = "getStatusHistoryForReferral",
    description = """Fetches an event log history of the Referral Status for a given Referral""",
    responses = [
      ApiResponse(
        responseCode = "200",
        description = "The Referral Status History of the Referral",
        content = [Content(array = ArraySchema(schema = Schema(implementation = ReferralStatusHistory::class)))],
      ),
      ApiResponse(
        responseCode = "401",
        description = "The request was unauthorised",
        content = [Content(schema = Schema(implementation = ErrorResponse::class))],
      ),
      ApiResponse(
        responseCode = "403",
        description = "Forbidden.  The client is not authorised to access this referral.",
        content = [Content(schema = Schema(implementation = ErrorResponse::class))],
      ),
      ApiResponse(
        responseCode = "404",
        description = "The Referral does not exist",
        content = [Content(schema = Schema(implementation = ErrorResponse::class))],
      ),
    ],
    security = [SecurityRequirement(name = "bearerAuth")],
  )
  @GetMapping("/referral/{id}/status-history", produces = [MediaType.APPLICATION_JSON_VALUE])
  fun getStatusHistoryForReferral(
    @Parameter(
      description = "The id (UUID) of a Referral",
      required = true,
    )
    @PathVariable("id") id: UUID,
  ): ResponseEntity<List<ReferralStatusHistory>> {
    val result = referralService.getStatusHistory(id)

    return ResponseEntity.ok(result)
  }

  @Operation(
    tags = ["Referrals"],
    summary = "Retrieve the manager associated with the Licence Condition or Requirement associated with a referral",
    operationId = "getManagerByReferralId",
    description = """
      Retrieves the manager (Probation Practitioner) associated with the Case, which is upstream of the 
      Referral itself.  We use this to retrieve a list of Delivery Locations (Offices) within the same
      PDU as a Referral itself.
      """,
    responses = [
      ApiResponse(
        responseCode = "200",
        description = "Information about the Manager associated with a Referral",
        content = [Content(schema = Schema(implementation = RequirementOrLicenceConditionManager::class))],
      ),
      ApiResponse(
        responseCode = "401",
        description = "The request was unauthorised",
        content = [Content(schema = Schema(implementation = ErrorResponse::class))],
      ),
      ApiResponse(
        responseCode = "403",
        description = "Forbidden.  The client is not authorised to access this referral.",
        content = [Content(schema = Schema(implementation = ErrorResponse::class))],
      ),
      ApiResponse(
        responseCode = "404",
        description = "The referral does not exist",
        content = [Content(schema = Schema(implementation = ErrorResponse::class))],
      ),
    ],
    security = [SecurityRequirement(name = "bearerAuth")],
  )
  @GetMapping("/referral-details/{id}/manager")
  fun getDeliveryLocationsByReferralId(
    @Parameter(description = "The id (UUID) of a referral", required = true)
    @PathVariable("id") id: UUID,
  ): ResponseEntity<RequirementOrLicenceConditionManager> = referralService.attemptToFindManagerForReferral(id)?.let {
    ResponseEntity.ok(it)
  } ?: throw NotFoundException("Could not retrieve Delivery Locations for Referral with ID: $id")

  @Operation(
    tags = ["Referrals"],
    summary = "Retrieve preferred delivery locations for a referral",
    operationId = "getPreferredDeliveryLocationsByReferralId",
    responses = [
      ApiResponse(
        responseCode = "200",
        description = "Preferred delivery locations and restrictions for the referral",
        content = [Content(schema = Schema(implementation = DeliveryLocationPreferences::class))],
      ),
      ApiResponse(
        responseCode = "401",
        description = "The request was unauthorised",
        content = [Content(schema = Schema(implementation = ErrorResponse::class))],
      ),
      ApiResponse(
        responseCode = "403",
        description = "Forbidden.  The client is not authorised to access this referral.",
        content = [Content(schema = Schema(implementation = ErrorResponse::class))],
      ),
      ApiResponse(
        responseCode = "404",
        description = "The referral does not exist",
        content = [Content(schema = Schema(implementation = ErrorResponse::class))],
      ),
    ],
    security = [SecurityRequirement(name = "bearerAuth")],
  )
  @GetMapping("/referral-details/{id}/delivery-location-preferences", produces = [MediaType.APPLICATION_JSON_VALUE])
  fun getPreferredDeliveryLocationsByReferralId(
    @Parameter(description = "The id (UUID) of a referral", required = true)
    @PathVariable("id") id: UUID,
  ): ResponseEntity<DeliveryLocationPreferences> {
    val deliveryLocationPreferences = deliveryLocationPreferencesService.getPreferredDeliveryLocationsForReferral(id)
    return ResponseEntity.ok().body(deliveryLocationPreferences)
  }

  @Operation(
    tags = ["Referrals"],
    summary = "Retrieve status transition data for a referral.",
    operationId = "getStatusTransitionsForReferral",
    description = "Returns all possible data for the update referral status form based on the referral id",
    responses = [
      ApiResponse(
        responseCode = "200",
        description = "Data for update referral status form",
        content = [Content(array = ArraySchema(schema = Schema(implementation = ReferralStatusTransitions::class)))],
      ),
      ApiResponse(
        responseCode = "401",
        description = "The request was unauthorised",
        content = [Content(schema = Schema(implementation = ErrorResponse::class))],
      ),
      ApiResponse(
        responseCode = "403",
        description = "Forbidden. The client is not authorised to access this resource.",
        content = [Content(schema = Schema(implementation = ErrorResponse::class))],
      ),
    ],
    security = [SecurityRequirement(name = "bearerAuth")],
  )
  @GetMapping("/bff/status-transitions/referral/{referralId}", produces = [MediaType.APPLICATION_JSON_VALUE])
  fun getStatusTransitionsForReferral(
    @Parameter(description = "The id (UUID) of a referral status description", required = true)
    @PathVariable referralId: UUID,
  ): ResponseEntity<ReferralStatusTransitions> = referralStatusService.getStatusTransitionsForReferral(referralId)
    ?.let {
      ResponseEntity.ok(it)
    } ?: throw NotFoundException("Referral status history for referral with id $referralId not found")
}
