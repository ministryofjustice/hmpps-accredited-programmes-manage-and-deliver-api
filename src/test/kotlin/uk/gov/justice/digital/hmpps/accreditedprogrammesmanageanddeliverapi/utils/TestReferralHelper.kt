package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.utils

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo
import org.awaitility.kotlin.await
import org.awaitility.kotlin.matches
import org.awaitility.kotlin.untilCallTo
import org.awaitility.kotlin.withPollDelay
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.TestComponent
import org.springframework.context.annotation.Import
import org.springframework.data.repository.findByIdOrNull
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.OffenceCohort
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.CodeDescription
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.toFullName
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.oasysApi.model.Osp
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.oasysApi.model.RiskScoreLevel
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common.randomCrn
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common.randomFullName
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
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.PniAssessmentFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.PniResponseFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.integration.wiremock.HmppsAuthApiExtension
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.integration.wiremock.HmppsAuthApiExtension.Companion.hmppsAuth
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.integration.wiremock.stubs.ArnsApiStubs
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.integration.wiremock.stubs.NDeliusApiStubs
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.integration.wiremock.stubs.OasysApiStubs
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.listener.model.PersonReference
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ReferralRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ReferralStatusDescriptionRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service.ReferralService
import java.time.Duration.ofSeconds
import java.util.UUID

/**
 * Test helper class for creating referral entities via domain events in integration tests.
 *
 * This helper simulates the referral creation flow by:
 * - Sending domain events to the event queue
 * - Stubbing external API responses (OASys, NDelius, HMPPS Auth)
 * - Waiting for asynchronous event processing to complete
 * - Retrieving the created referral entities from the repository
 *
 * Usage:
 * ```
 * // Create a single referral (waits for processing)
 * val referral = testReferralHelper.createReferral(reportingPdu = "PDU 1")
 *
 * // Create multiple referrals efficiently (single wait)
 * val referrals = testReferralHelper.createReferrals(
 *   listOf(
 *     ReferralConfig(reportingPdu = "PDU 1"),
 *     ReferralConfig(reportingPdu = "PDU 2")
 *   )
 * )
 * ```
 */
@TestComponent
@ExtendWith(HmppsAuthApiExtension::class)
@Import(OasysApiStubs::class, NDeliusApiStubs::class, DomainEventsQueueConfig::class, ArnsApiStubs::class)
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

  /**
   * Creates a single referral by sending a domain event and waiting for processing to complete.
   *
   * This method:
   * 1. Sends a referral created event to the domain events queue
   * 2. Stubs all required external API responses
   * 3. Waits for the event to be processed (approximately 1 second)
   * 4. Retrieves and returns the created referral entity
   *
   * @param crn The Case Reference Number. If null, a random CRN is generated.
   * @param personName The full name of the person being referred. Defaults to a random full name.
   * @param referralId The referral UUID. If null, a random UUID is generated.
   * @param sourcedFrom The source of the referral. Defaults to LICENCE_CONDITION.
   * @param reportingPdu The reporting Probation Delivery Unit name. Defaults to "PDU 1".
   * @param reportingTeam The reporting team name. Defaults to "Team A".
   * @param sex The sex of the person. Defaults to "Male".
   * @param cohort The offence cohort for the referral. Defaults to GENERAL_OFFENCE.
   * @return The created [ReferralEntity]
   */
  fun createReferral(
    crn: String = randomCrn(),
    personName: String = randomFullName().toString(),
    referralId: UUID = UUID.randomUUID(),
    sourcedFrom: ReferralEntitySourcedFrom = ReferralEntitySourcedFrom.LICENCE_CONDITION,
    reportingPdu: String = "PDU 1",
    reportingTeam: String = "Team A",
    sex: String = "Male",
    cohort: OffenceCohort = OffenceCohort.GENERAL_OFFENCE,
  ): ReferralEntity {
    val actualCrn =
      sendReferralEvent(crn, personName, referralId, sourcedFrom, reportingPdu, reportingTeam, sex, cohort)
    waitForQueueToEmpty()
    return referralRepository.findByCrn(actualCrn).first()
  }

  /**
   * Creates multiple referrals efficiently by batching event sends and waiting once.
   *
   * This method is more efficient than calling [createReferral] multiple times as it:
   * 1. Sends all referral created events to the queue
   * 2. Waits once for all events to be processed (saves approximately 1 second per referral)
   * 3. Retrieves and returns all created referral entities
   *
   * Example:
   * ```
   * // Create 5 referrals with default config
   * val referrals = createReferrals(count = 5)
   *
   * // Create 3 referrals with custom config
   * val referrals = createReferrals(
   *   count = 3,
   *   defaultConfig = ReferralConfig(reportingPdu = "London PDU")
   * )
   *
   * // Create referrals with individual configs
   * val referrals = createReferrals(
   *   referralConfigs = listOf(
   *     ReferralConfig(crn = "X123456", reportingPdu = "London PDU"),
   *     ReferralConfig(crn = "Y789012", reportingPdu = "Manchester PDU")
   *   )
   * )
   * ```
   *
   * @param count Number of referrals to create with the default config. Ignored if referralConfigs is provided.
   * @param defaultConfig The default configuration to use for each referral when using count.
   * @param referralConfigs List of specific referral configurations. Takes precedence over count.
   * @return List of created [ReferralEntity] objects
   */
  fun createReferrals(
    count: Int = 1,
    defaultConfig: ReferralConfig = ReferralConfig(),
    referralConfigs: List<ReferralConfig>? = null,
  ): List<ReferralEntity> {
    val configs = referralConfigs ?: List(count) { defaultConfig }

    val crns = configs.map { config ->
      sendReferralEvent(
        crn = config.crn,
        personName = config.personName,
        referralId = config.referralId,
        sourcedFrom = config.sourcedFrom,
        reportingPdu = config.reportingPdu,
        reportingTeam = config.reportingTeam,
        sex = config.sex,
        cohort = config.cohort,
      )
    }

    waitForQueueToEmpty()

    return crns.map { crn ->
      referralRepository.findByCrn(crn).first()
    }
  }

  /**
   * Creates a referral and updates it to a specific status.
   *
   * This is a convenience method that:
   * 1. Creates a referral using [createReferral]
   * 2. Updates the referral's status using the referral service
   * 3. Retrieves and returns the updated referral entity
   *
   * @param statusEntity The status to set. If null, defaults to AWAITING_ALLOCATION status.
   * @return The created and updated [ReferralEntity]
   */
  fun createReferralWithStatus(statusEntity: ReferralStatusDescriptionEntity? = null): ReferralEntity {
    val status = statusEntity ?: referralStatusDescriptionRepository.getAwaitingAllocationStatusDescription()
    val referral = createReferral()
    referralService.updateStatus(referral, status.id, null, "AUTH_USER")

    return referralRepository.findByIdOrNull(referral.id!!)!!
  }

  /**
   * Sends a referral created domain event and stubs all required external API responses.
   *
   * This is an internal method that handles:
   * - Generating or using provided CRN and referral ID
   * - Creating the referral details with factories
   * - Stubbing OASys, NDelius, and HMPPS Auth API responses
   * - Sending the domain event to the queue
   *
   * @param crn The Case Reference Number. Defaults to a random CRN.
   * @param personName The full name of the person being referred. Defaults to a random full name.
   * @param referralId The referral UUID. Defaults to a random UUID.
   * @param sourcedFrom The source of the referral. Defaults to LICENCE_CONDITION.
   * @param reportingPdu The reporting Probation Delivery Unit name. Defaults to "PDU 1".
   * @param reportingTeam The reporting team name. Defaults to "Team A".
   * @param sex The sex of the person. Defaults to "Male".
   * @param cohort The offence cohort for the referral. Defaults to GENERAL_OFFENCE.
   * @return The CRN used for the referral
   */
  private fun sendReferralEvent(
    crn: String = randomCrn(),
    personName: String = randomFullName().toString(),
    referralId: UUID = UUID.randomUUID(),
    sourcedFrom: ReferralEntitySourcedFrom = ReferralEntitySourcedFrom.LICENCE_CONDITION,
    reportingPdu: String = "PDU 1",
    reportingTeam: String = "Team A",
    sex: String = "Male",
    cohort: OffenceCohort = OffenceCohort.GENERAL_OFFENCE,
  ): String {
    val findAndReferReferralDetails = FindAndReferReferralDetailsFactory()
      .withInterventionName("Test Intervention")
      .withInterventionType(InterventionType.ACP)
      .withReferralId(referralId)
      .withPersonReference(crn)
      .withPersonReferenceType(PersonReferenceType.CRN)
      .withSourcedFromReferenceType(sourcedFrom)
      .withSourcedFromReference("LIC-12345")
      .withEventNumber(1)
      .produce()

    // Sets the values for our PNI calculation if the cohort should be SEXUAL OFFENCE
    val pniResponse = PniResponseFactory().withAssessment(
      PniAssessmentFactory().also {
        if (cohort == OffenceCohort.SEXUAL_OFFENCE) {
          it.withOsp(
            Osp(
              RiskScoreLevel.HIGH,
              RiskScoreLevel.HIGH,
            ),
          )
        } else {
          it.withOsp(
            Osp(
              RiskScoreLevel.NOT_APPLICABLE,
              RiskScoreLevel.NOT_APPLICABLE,
            ),
          )
        }
      }.produce(),
    ).produce()
    oasysApiStubs.stubSuccessfulPniResponse(crn, pniResponse)
    nDeliusApiStubs.stubSuccessfulSentenceInformationResponse(
      crn,
      findAndReferReferralDetails.eventNumber,
      sourcedFrom = sourcedFrom,
    )
    nDeliusApiStubs.stubPersonalDetailsResponseForCrn(
      crn,
      NDeliusPersonalDetailsFactory()
        .withCrn(crn)
        .withSex(CodeDescription(randomUppercaseString(), sex))
        .withName(personName.toFullName())
        .withProbationDeliveryUnit(CodeDescription(randomUppercaseString(), reportingPdu))
        .withTeam(CodeDescription(randomUppercaseString(), reportingTeam))
        .produce(),
    )
    hmppsAuth.stubGrantToken()
    wiremock.stubFor(
      get(urlEqualTo("/referral/$referralId"))
        .willReturn(
          aResponse()
            .withStatus(200)
            .withHeader("Content-Type", "application/json")
            .withBody(objectMapper.writeValueAsString(findAndReferReferralDetails)),
        ),
    )

    val eventType = "interventions.community-referral.created"
    val domainEventsMessage = DomainEventsMessageFactory()
      .withDetailUrl("/referral/$referralId")
      .withEventType(eventType)
      .withPersonReference(PersonReference(listOf(PersonReference.Identifier("CRN", crn))))
      .produce()

    domainEventsQueueConfig.sendDomainEvent(domainEventsMessage)

    return crn
  }

  /**
   * Waits for the domain events queue to be empty (all events processed).
   *
   * Polls the queue every 1 second until no messages remain.
   * This ensures that asynchronous event processing has completed before continuing.
   */
  private fun waitForQueueToEmpty() {
    await withPollDelay ofSeconds(1) untilCallTo {
      with(domainEventsQueueConfig) {
        domainEventQueue.countAllMessagesOnQueue()
      }
    } matches { it == 0 }
  }

  /**
   * Configuration for creating a referral via domain event.
   *
   * @property crn The Case Reference Number. Defaults to a random CRN.
   * @property personName The full name of the person being referred. Defaults to a random full name.
   * @property referralId The referral UUID. Defaults to a random UUID.
   * @property sourcedFrom The source of the referral. Defaults to LICENCE_CONDITION.
   * @property reportingPdu The reporting Probation Delivery Unit name. Defaults to "PDU 1".
   * @property reportingTeam The reporting team name. Defaults to "Team A".
   * @property sex The sex of the person. Defaults to "Male".
   * @property cohort The offence cohort for the referral. Defaults to GENERAL_OFFENCE.
   */
  data class ReferralConfig(
    val crn: String = randomCrn(),
    val personName: String = randomFullName().toString(),
    val referralId: UUID = UUID.randomUUID(),
    val sourcedFrom: ReferralEntitySourcedFrom = ReferralEntitySourcedFrom.LICENCE_CONDITION,
    val reportingPdu: String = "PDU 1",
    val reportingTeam: String = "Team A",
    val sex: String = "Male",
    val cohort: OffenceCohort = OffenceCohort.GENERAL_OFFENCE,
  )
}
