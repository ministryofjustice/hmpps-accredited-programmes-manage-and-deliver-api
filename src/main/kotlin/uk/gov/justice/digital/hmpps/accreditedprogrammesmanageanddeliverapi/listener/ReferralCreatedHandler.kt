package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.listener

import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.listener.model.DomainEventsMessage
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.listener.model.toEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.MessageHistoryRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service.ReferralService
import java.util.UUID

@Service
@Transactional
class ReferralCreatedHandler(
  private val objectMapper: ObjectMapper,
  private val messageHistoryRepository: MessageHistoryRepository,
  private val referralService: ReferralService,
) {

  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }

  fun handle(domainEventMessage: DomainEventsMessage) {
    val referralId = extractReferralId(domainEventMessage.detailUrl)
    log.info("Received referral created event for referral id: $referralId")
    messageHistoryRepository.save(domainEventMessage.toEntity(objectMapper.writeValueAsString(domainEventMessage)))

    val referralDetails = referralService.getFindAndReferReferralDetails(referralId)
    referralService.createReferral(referralDetails)
  }

  private fun extractReferralId(detailUrl: String) = UUID.fromString(detailUrl.split("/").last())
}
