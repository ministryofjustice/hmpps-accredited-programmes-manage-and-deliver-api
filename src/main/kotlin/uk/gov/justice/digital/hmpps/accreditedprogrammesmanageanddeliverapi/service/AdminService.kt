package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.client.HttpClientErrorException
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.ClientResult
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.NDeliusIntegrationApiClient
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.NDeliusPersonalDetails
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ReferralEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ReferralRepository
import java.time.LocalDate
import java.util.UUID

/**
 * Service for dev-facing operations.
 *
 * This service handles long-running administrative tasks such as refreshing
 * personal details for referrals in bulk.
 */
@Service
class AdminService(
  private val referralRepository: ReferralRepository,
  private val referralService: ReferralService,
  private val sentenceService: SentenceService,
  private val nDeliusIntegrationApiClient: NDeliusIntegrationApiClient,
) {
  private val log = LoggerFactory.getLogger(this::class.java)

  /**
   * Refreshes personal details for the specified referrals.
   *
   * This method is designed to run as a long-running process after the HTTP response
   * has been sent to the client.
   *
   * @param referralIds List of referral UUIDs to refresh personal details for
   */
  suspend fun refreshPersonalDetailsForReferrals(referralIds: List<UUID>) {
    log.info("Starting refresh of personal details for {} referrals", referralIds.size)
    referralIds.forEachIndexed { index, id ->
      log.info("[{}/{}] Refreshing Personal Details for Referral with id {}...", index + 1, referralIds.size, id)
      referralService.refreshPersonalDetailsForReferral(id)
      log.info("...done!")
    }
  }

  /**
   * Refreshes personal details for all referrals in the system.
   *
   * This method is designed to run as a long-running process after the HTTP response
   * has been sent to the client.
   */
  suspend fun refreshPersonalDetailsForAllReferrals() {
    log.info("Starting refresh of personal details for all referrals")
    val ids = referralRepository.getAllIds()
    refreshPersonalDetailsForReferrals(ids)
  }

  @Transactional
  fun cleanUpReferralsWithNoDeliusOrOasysData() {
    val referrals = referralRepository.getAllReferralsWithNulLSentenceEndDateOrSex()
    log.info("Starting cleanup of {} referrals", referrals.size)

    val results = referrals.mapIndexed { index, referral ->
      log.info("[{}/{}] Processing referral {}", index + 1, referrals.size, referral.id)
      processReferral(referral)
    }

    val summary = results.groupingBy { it }.eachCount()
    log.info(
      "Cleanup completed. Total: {}, Updated: {}, Deleted: {}, Errors: {}",
      results.size,
      summary[ProcessingResult.UPDATED] ?: 0,
      summary[ProcessingResult.DELETED] ?: 0,
      summary[ProcessingResult.ERROR] ?: 0,
    )
  }

  private fun processReferral(referral: ReferralEntity): ProcessingResult {
    return try {
      val personalDetails = fetchPersonalDetails(referral.crn) ?: return ProcessingResult.DELETED
      val sentenceEndDate = fetchSentenceEndDate(referral) ?: return ProcessingResult.DELETED

      updateReferral(referral, personalDetails, sentenceEndDate)
    } catch (e: Exception) {
      log.error("Unexpected error processing referral ${referral.id}, crn ${referral.crn}: ${e.message}", e)
      ProcessingResult.ERROR
    }
  }

  private fun fetchPersonalDetails(crn: String): NDeliusPersonalDetails? = try {
    when (val result = nDeliusIntegrationApiClient.getPersonalDetails(crn)) {
      is ClientResult.Success -> result.body
      is ClientResult.Failure -> result.throwException()
    }
  } catch (e: Exception) {
    handleFetchError(crn, "personal details", e)
  }

  private fun fetchSentenceEndDate(referral: ReferralEntity): LocalDate? = try {
    val sentenceEndDate = sentenceService.getSentenceEndDate(
      referral.crn,
      referral.eventNumber,
      referral.sourcedFrom,
    )

    if (sentenceEndDate == null) {
      log.info("Sentence end date is null for crn: ${referral.crn}, deleting referral")
      referralRepository.deleteById(referral.id!!)
      null
    } else {
      sentenceEndDate
    }
  } catch (e: Exception) {
    handleFetchError(referral.crn, "sentence details", e)
  }

  private fun handleFetchError(crn: String, dataType: String, exception: Exception): Nothing? = if (is404Error(exception)) {
    log.info("$dataType not found (404) for crn: $crn, deleting referral")
    referralRepository.findByCrn(crn).first().let { referralRepository.deleteById(it.id!!) }
    null
  } else {
    log.error("Error fetching $dataType for crn $crn: ${exception.message}", exception)
    null
  }

  private fun updateReferral(
    referral: ReferralEntity,
    personalDetails: NDeliusPersonalDetails,
    sentenceEndDate: LocalDate,
  ): ProcessingResult {
    referral.sex = personalDetails.sex.description
    referral.sentenceEndDate = sentenceEndDate
    referralRepository.save(referral)
    log.info("Successfully updated referral for crn: ${referral.crn}")
    return ProcessingResult.UPDATED
  }

  private fun is404Error(exception: Exception): Boolean = exception.message?.contains("404") == true ||
    exception is HttpClientErrorException.NotFound ||
    (exception.cause?.message?.contains("404") == true)

  private enum class ProcessingResult {
    UPDATED,
    DELETED,
    ERROR,
  }
}
