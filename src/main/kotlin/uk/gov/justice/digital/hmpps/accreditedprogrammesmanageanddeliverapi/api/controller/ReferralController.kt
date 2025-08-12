package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.ErrorResponse
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.OffenceHistory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.PersonalDetails
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.ReferralDetails
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.SentenceInformation
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.toModel
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common.exception.NotFoundException
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service.OffenceService
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service.ReferralService
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service.SentenceService
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service.ServiceUserService
import java.util.UUID

@RestController
@PreAuthorize("hasAnyRole('ROLE_ACCREDITED_PROGRAMMES_MANAGE_AND_DELIVER_API__ACPMAD_UI_WR')")
class ReferralController(
  private val referralService: ReferralService,
  private val serviceUserService: ServiceUserService,
  private val offenceService: OffenceService,
  private val sentenceService: SentenceService,
) {

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
  fun getReferralDetailsById(
    @Parameter(description = "The id (UUID) of a referral", required = true)
    @PathVariable("id") id: UUID,
  ): ResponseEntity<ReferralDetails> = referralService.getReferralDetails(id)?.let {
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
    val referral = referralService.getReferralById(id) ?: throw NotFoundException("Referral with id $id not found")
    return serviceUserService.getPersonalDetailsByIdentifier(referral.crn).let {
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
    val referral = referralService.getReferralById(id) ?: throw NotFoundException("Referral with id $id not found")
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
    val referral = referralService.getReferralById(id) ?: throw NotFoundException("Referral with id $id not found")
    return sentenceService.getSentenceInformationByIdentifier(referral.crn, referral.eventNumber).let {
      ResponseEntity.ok(it.toModel())
    } ?: throw NotFoundException("Sentence information not found for crn ${referral.crn} not found")
  }
}
