package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ReferralRepository
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
  private val userService: UserService,
  private val sentenceService: SentenceService,
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

    referrals.forEachIndexed { index, referral ->
      log.info("[{}/{}] Checking Personal Details for Referral with id {}...", index + 1, referrals.size, referral)
      try {
        val personalDetails =
          userService.getPersonalDetailsByIdentifier(referral.crn)
        log.info("[{}/{}] Checking Sentence details for Referral with id {}...", index + 1, referrals.size, referral)
        val sentenceEndDate =
          sentenceService.getSentenceEndDate(
            referral.crn,
            referral.eventNumber,
            referral.sourcedFrom,
          ) ?: return@forEachIndexed referralRepository.deleteById(referral.id!!)

        // If we found values update them
        referral.sex = personalDetails.sex.description
        referral.sentenceEndDate = sentenceEndDate
        referralRepository.save(referral)
        log.info("Referral was updated with crn: ${referral.crn}")
      } catch (exception: Exception) {
        if (exception.message?.contains("404") == true) {
          referralRepository.deleteById(referral.id!!)
          log.info("Deleted referral for crn: ${referral.crn}")
        } else {
          log.error("Non 404 exception caught: ${exception.message}")
        }
      }
    }
    log.info("...done!!")
  }
}
