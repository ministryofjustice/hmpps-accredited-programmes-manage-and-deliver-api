package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.ClientResult
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.NDeliusIntegrationApiClient
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ReferralRepository
import java.time.LocalDateTime
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
  private val pniService: PniService,
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

  /**
   * This method is intended on cleaning up referrals in dev that have no information in Ndelius or Oasys.
   * It processes referrals that have no 'sex' or 'sentence_end_date' in the referral table as these are values fetched by Ndelius and Oasys respectively.
   *
   * It is designed to run as a long-running process after the HTTP response
   * has been sent to the client.
   */

  fun cleanUpReferralsWithNoDeliusOrOasysData() {
    val cutoff = LocalDateTime.now().minusDays(7)
    val referrals = referralRepository.getOldReferralsWithNulLSentenceEndDateOrSex(cutoff)

    log.info("Found {} referrals with missing NDelius or OASys data", referrals.size)

    val results = referrals.mapIndexed { index, referral ->
      log.info("[{}/{}] Processing referral {}", index + 1, referrals.size, referral.id)

      val personalDetails = when (nDeliusIntegrationApiClient.getPersonalDetails(referral.crn)) {
        is ClientResult.Success -> true
        is ClientResult.Failure -> false
      }
      if (!personalDetails) {
        log.info("Missing NDelius personal details for crn ${referral.crn}. Deleting referral ${referral.id}...")
        referralRepository.deleteById(referral.id!!)
        return@mapIndexed ProcessingResult.DELETED
      }

      val sentenceEndDate = try {
        sentenceService.getSentenceEndDate(
          referral.crn,
          referral.eventNumber,
          referral.sourcedFrom,
        )
      } catch (e: Exception) {
        log.info(
          "Error or 404 while fetching sentence end date for crn ${referral.crn}. Deleting referral ${referral.id}... : ${e.message}",
        )
        null
      }

      if (sentenceEndDate == null) {
        log.info("Missing sentence end date for crn ${referral.crn}. Deleting referral ${referral.id}...")
        referralRepository.deleteById(referral.id!!)
        return@mapIndexed ProcessingResult.DELETED
      }

      val pni = try {
        pniService.getPniCalculation(referral.crn)
      } catch (_: Exception) {
        log.info("Failure to retrieve PNI score for crn : ${referral.crn}")
        null
      }

      if (pni == null) {
        log.info("Missing pni for crn ${referral.crn}. Deleting referral ${referral.id}...")
        referralRepository.deleteById(referral.id!!)
        return@mapIndexed ProcessingResult.DELETED
      }

      ProcessingResult.SKIPPED
    }

    val summary = results.groupingBy { it }.eachCount()
    log.info(
      "Cleanup completed. Total: {}, Deleted: {}, Skipped: {}",
      results.size,
      summary[ProcessingResult.DELETED] ?: 0,
      summary[ProcessingResult.SKIPPED] ?: 0,
    )
  }

  private enum class ProcessingResult {
    SKIPPED,
    DELETED,
  }
}
