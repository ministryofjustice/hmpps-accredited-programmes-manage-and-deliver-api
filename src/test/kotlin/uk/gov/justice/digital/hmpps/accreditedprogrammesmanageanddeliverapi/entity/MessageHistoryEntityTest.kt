package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.MessageHistoryEntityFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.ReferralEntityFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.MessageHistoryRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ReferralRepository

class MessageHistoryEntityTest : IntegrationTestBase() {

  @Autowired
  private lateinit var messageHistoryRepository: MessageHistoryRepository

  @Autowired
  private lateinit var referralRepository: ReferralRepository

  @Test
  @Transactional
  fun `should save and retrieve message history with optional referral`() {
    // Given
    val referral = ReferralEntityFactory().withCrn("CRN123").produce()
    referralRepository.save(referral)

    val messageHistory = MessageHistoryEntityFactory().withReferral(referral).produce()
    messageHistoryRepository.save(messageHistory)

    // When
    val retrievedMessageHistory = messageHistoryRepository.findById(messageHistory.id!!).get()

    // Then
    assertThat(retrievedMessageHistory).isNotNull
    assertThat(retrievedMessageHistory.eventType).isEqualTo(messageHistory.eventType)
    assertThat(retrievedMessageHistory.referral).isNotNull
    assertThat(retrievedMessageHistory.referral!!.id).isEqualTo(referral.id)
    assertThat(retrievedMessageHistory.referral!!.crn).isEqualTo("CRN123")
  }

  @Test
  @Transactional
  fun `should save and retrieve message history without referral`() {
    // Given
    val messageHistory = MessageHistoryEntityFactory().withReferral(null).produce()

    messageHistoryRepository.save(messageHistory)

    // When
    val retrievedMessageHistory = messageHistoryRepository.findById(messageHistory.id!!).get()

    // Then
    assertThat(retrievedMessageHistory).isNotNull
    assertThat(retrievedMessageHistory.eventType).isEqualTo(messageHistory.eventType)
    assertThat(retrievedMessageHistory.description).isEqualTo(messageHistory.description)
    assertThat(retrievedMessageHistory.message).isEqualTo(messageHistory.message)
    assertThat(retrievedMessageHistory.referral).isNull()
  }
}
