package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.web.client.HttpClientErrorException
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.ClientResult
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.NDeliusIntegrationApiClient
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.NDeliusPersonalDetails
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ReferralEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ReferralRepository
import java.time.LocalDate
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
  private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
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

  // Limit DB operations to 5
  private val dbSemaphore = Semaphore(5)

  /**
   * This method is intended on cleaning up referrals in dev that have no information in Ndelius or Oasys.
   * It processes referrals that have no 'sex' or 'sentence_end_date' in the referral table as these are values fetched by Ndelius and Oasys respectively.
   *
   * It is designed to run as a long-running process after the HTTP response
   * has been sent to the client.
   */

  suspend fun cleanUpReferralsWithNoDeliusOrOasysData(): Int = coroutineScope {
    val referrals = referralRepository.getOldReferralsWithNulLSentenceEndDateOrSex(LocalDateTime.now().minusDays(7))
    log.info("Found {} referrals with no 'sentence_end_date' or 'sex' ", referrals.size)

    val results = referrals.mapIndexed { index, referral ->
      async(dispatcher) {
        log.info("[{}/{}] Processing referral {}", index + 1, referrals.size, referral.id)
        try {
          val personalDetails = fetchPersonalDetails(referral.crn) ?: return@async ProcessingResult.DELETED
          val sentenceEndDate = fetchSentenceEndDate(referral) ?: return@async ProcessingResult.DELETED

          dbSemaphore.withPermit {
            referral.sex = personalDetails.sex.description
            referral.sentenceEndDate = sentenceEndDate
            referral.dateOfBirth = personalDetails.dateOfBirth.toLocalDate()
            referralRepository.save(referral)
            log.info("Successfully updated referral for crn: ${referral.crn}")
          }
          ProcessingResult.UPDATED
        } catch (e: Exception) {
          log.error("Unexpected error processing referral ${referral.id}, crn ${referral.crn}: ${e.message}", e)
          ProcessingResult.ERROR
        }
      }
    }.awaitAll()

    val summary = results.groupingBy { it }.eachCount()
    log.info(
      "Cleanup completed. Total: {}, Updated: {}, Deleted: {}, Errors: {}",
      results.size,
      summary[ProcessingResult.UPDATED] ?: 0,
      summary[ProcessingResult.DELETED] ?: 0,
      summary[ProcessingResult.ERROR] ?: 0,
    )
    summary[ProcessingResult.DELETED] ?: 0
  }

  private suspend fun fetchPersonalDetails(crn: String): NDeliusPersonalDetails? = withContext(Dispatchers.IO) {
    try {
      when (val result = nDeliusIntegrationApiClient.getPersonalDetails(crn)) {
        is ClientResult.Success -> result.body
        is ClientResult.Failure -> result.throwException()
      }
    } catch (e: Exception) {
      if (e is HttpClientErrorException.NotFound) {
        log.info("Personal details not found (404) for crn: $crn, deleting referral")
        dbSemaphore.withPermit {
          val referral = referralRepository.findByCrn(crn).first()
          referral.let { referralRepository.deleteById(it.id!!) }
        }
      } else {
        log.error("Error fetching personal details for crn $crn: ${e.message}", e)
      }
      null
    }
  }

  private suspend fun fetchSentenceEndDate(referral: ReferralEntity): LocalDate? = withContext(Dispatchers.IO) {
    try {
      val sentenceEndDate = sentenceService.getSentenceEndDate(
        referral.crn,
        referral.eventNumber,
        referral.sourcedFrom,
      )

      if (sentenceEndDate == null) {
        log.info("Sentence end date is null for crn: ${referral.crn}, deleting referral")
        dbSemaphore.withPermit {
          referralRepository.deleteById(referral.id!!)
        }
        null
      } else {
        sentenceEndDate
      }
    } catch (e: Exception) {
      if (e is HttpClientErrorException.NotFound) {
        log.info("Sentence details not found (404) for crn: ${referral.crn}, deleting referral")
        dbSemaphore.withPermit {
          referralRepository.deleteById(referral.id!!)
        }
      } else {
        log.error("Error fetching sentence details for crn ${referral.crn}: ${e.message}", e)
      }
      null
    }
  }

  private enum class ProcessingResult {
    UPDATED,
    DELETED,
    ERROR,
  }
}
