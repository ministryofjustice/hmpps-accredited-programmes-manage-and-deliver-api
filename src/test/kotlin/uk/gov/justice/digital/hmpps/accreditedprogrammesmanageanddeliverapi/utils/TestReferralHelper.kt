package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.utils

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import org.awaitility.kotlin.await
import org.awaitility.kotlin.matches
import org.awaitility.kotlin.untilCallTo
import org.awaitility.kotlin.withPollDelay
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.TestComponent
import org.springframework.context.annotation.Import
import org.springframework.data.repository.findByIdOrNull
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.CodeDescription
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common.randomCrn
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common.randomUppercaseString
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.config.DomainEventsQueueConfig
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ReferralEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ReferralEntitySourcedFrom
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ReferralStatusDescriptionEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.type.InterventionType
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.type.PersonReferenceType
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.DomainEventsMessageFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.FindAndReferReferralDetailsFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.NDeliusPersonalDetailsFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.integration.wiremock.HmppsAuthApiExtension
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.integration.wiremock.HmppsAuthApiExtension.Companion.hmppsAuth
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.integration.wiremock.stubs.NDeliusApiStubs
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.integration.wiremock.stubs.OasysApiStubs
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.listener.model.PersonReference
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ReferralRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ReferralStatusDescriptionRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service.ReferralService
import java.time.Duration.ofSeconds
import java.util.UUID

@TestComponent
@ExtendWith(HmppsAuthApiExtension::class)
@Import(OasysApiStubs::class, NDeliusApiStubs::class, DomainEventsQueueConfig::class)
class TestReferralHelper {

  @Autowired
  private lateinit var referralService: ReferralService

  @Autowired
  private lateinit var oasysApiStubs: OasysApiStubs

  @Autowired
  private lateinit var nDeliusApiStubs: NDeliusApiStubs

  @Autowired
  private lateinit var domainEventsQueueConfig: DomainEventsQueueConfig

  @Autowired
  private lateinit var wiremock: WireMockServer

  @Autowired
  private lateinit var objectMapper: ObjectMapper

  @Autowired
  private lateinit var referralRepository: ReferralRepository

  @Autowired
  private lateinit var referralStatusDescriptionRepository: ReferralStatusDescriptionRepository

  fun createReferral(
    crn: String? = null,
    referralId: UUID? = null,
    sourcedFrom: ReferralEntitySourcedFrom? = null,
    reportingPdu: String? = "Test PDU 1",
    reportingTeam: String? = "Team A",
  ): ReferralEntity {
    val actualCrn = sendReferralEvent(crn, referralId, sourcedFrom, reportingPdu, reportingTeam)
    waitForQueueToEmpty()
    return referralRepository.findByCrn(actualCrn).first()
  }

  fun createMultipleReferrals(numReferrals: Int): List<ReferralEntity> = runBlocking {
    val crns = (1..numReferrals).map {
      async { sendReferralEvent() }
    }.awaitAll()

    waitForQueueToEmpty()

    crns.map { crn ->
      referralRepository.findByCrn(crn).first()
    }
  }

  fun createReferralWithStatus(statusEntity: ReferralStatusDescriptionEntity? = null): ReferralEntity {
    val status = statusEntity ?: referralStatusDescriptionRepository.getAwaitingAllocationStatusDescription()
    val referral = createReferral()
    referralService.updateStatus(referral, status.id, null, "AUTH_USER")

    return referralRepository.findByIdOrNull(referral.id!!)!!
  }

  private fun sendReferralEvent(
    crn: String? = null,
    referralId: UUID? = null,
    sourcedFrom: ReferralEntitySourcedFrom? = null,
    reportingPdu: String? = "Test PDU 1",
    reportingTeam: String? = "Team A",
  ): String {
    val actualCrn = crn ?: randomCrn()
    val actualReferralId = referralId ?: UUID.randomUUID()
    val actualSourcedFrom = sourcedFrom ?: ReferralEntitySourcedFrom.LICENCE_CONDITION

    val findAndReferReferralDetails = FindAndReferReferralDetailsFactory()
      .withInterventionName("Test Intervention")
      .withInterventionType(InterventionType.ACP)
      .withReferralId(actualReferralId)
      .withPersonReference(actualCrn)
      .withPersonReferenceType(PersonReferenceType.CRN)
      .withSourcedFromReferenceType(actualSourcedFrom)
      .withSourcedFromReference("LIC-12345")
      .withEventNumber(1)
      .produce()

    oasysApiStubs.stubSuccessfulPniResponse(actualCrn)
    nDeliusApiStubs.stubSuccessfulSentenceInformationResponse(
      actualCrn,
      findAndReferReferralDetails.eventNumber,
      sourcedFrom = actualSourcedFrom,
    )
    nDeliusApiStubs.stubPersonalDetailsResponse(
      NDeliusPersonalDetailsFactory().withCrn(actualCrn)
        .withProbationDeliveryUnit(CodeDescription(randomUppercaseString(), reportingPdu!!))
        .withTeam(CodeDescription(randomUppercaseString(), reportingTeam!!))
        .produce(),
    )
    hmppsAuth.stubGrantToken()
    wiremock.stubFor(
      get(urlEqualTo("/referral/$actualReferralId"))
        .willReturn(
          aResponse()
            .withStatus(200)
            .withHeader("Content-Type", "application/json")
            .withBody(objectMapper.writeValueAsString(findAndReferReferralDetails)),
        ),
    )

    val eventType = "interventions.community-referral.created"
    val domainEventsMessage = DomainEventsMessageFactory()
      .withDetailUrl("/referral/$actualReferralId")
      .withEventType(eventType)
      .withPersonReference(PersonReference(listOf(PersonReference.Identifier("CRN", actualCrn))))
      .produce()

    domainEventsQueueConfig.sendDomainEvent(domainEventsMessage)

    return actualCrn
  }

  private fun waitForQueueToEmpty() {
    await withPollDelay ofSeconds(1) untilCallTo {
      with(domainEventsQueueConfig) {
        domainEventQueue.countAllMessagesOnQueue()
      }
    } matches { it == 0 }
  }
}
