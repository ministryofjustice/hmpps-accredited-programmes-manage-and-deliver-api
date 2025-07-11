package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service

import org.slf4j.LoggerFactory
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.Referral
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.toApi
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.ClientResult
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.findAndReferInterventionApi.FindAndReferInterventionApiClient
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.findAndReferInterventionApi.model.ReferralDetails
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.findAndReferInterventionApi.model.toReferralEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common.exception.NotFoundException
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ReferralStatusHistoryEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ReferralRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ReferralStatusHistoryRepository
import java.time.LocalDateTime
import java.util.*

@Service
@Transactional
class ReferralService(
  val findAndReferInterventionApiClient: FindAndReferInterventionApiClient,
  val referralRepository: ReferralRepository,
  val referralStatusHistoryRepository: ReferralStatusHistoryRepository,
) {
  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }

  fun getReferralDetails(referralId: UUID): ReferralDetails {
    val referralDetails =
      when (val result = findAndReferInterventionApiClient.getReferral(referralId = referralId)) {
        is ClientResult.Failure -> {
          log.warn("Failure to retrieve referral details for uuid : $referralId")
          throw NotFoundException("No referral details found for id: $referralId")
        }
        is ClientResult.Success -> result.body
      }
    return referralDetails
  }

  fun createReferral(referralDetails: ReferralDetails) {
    val referralEntity = referralRepository.save(referralDetails.toReferralEntity())
    val statusHistoryEntity = referralStatusHistoryRepository.save(
      ReferralStatusHistoryEntity(
        status = "Created",
        startDate = LocalDateTime.now(),
        endDate = null,
      ),
    )
    referralEntity.statusHistories.add(statusHistoryEntity)
  }

  fun getReferralById(id: UUID): Referral? = referralRepository.findByIdOrNull(id)?.toApi()
}
