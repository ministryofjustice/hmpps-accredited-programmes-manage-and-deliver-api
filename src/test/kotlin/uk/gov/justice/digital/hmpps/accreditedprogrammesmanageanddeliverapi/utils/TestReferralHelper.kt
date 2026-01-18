package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.utils

import com.github.tomakehurst.wiremock.WireMockServer
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.TestComponent
import org.springframework.context.annotation.Import
import org.springframework.data.repository.findByIdOrNull
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.OffenceCohort
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.CodeDescription
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.getNameAsString
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.toFullName
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.oasysApi.model.Osp
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.oasysApi.model.RiskScoreLevel
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common.randomCrn
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common.randomFullName
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common.randomUppercaseString
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ReferralEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ReferralEntitySourcedFrom
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ReferralStatusDescriptionEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.type.InterventionType
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.type.PersonReferenceType
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.FindAndReferReferralDetailsFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.NDeliusPersonalDetailsFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.PniAssessmentFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.PniResponseFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.integration.wiremock.HmppsAuthApiExtension
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.integration.wiremock.HmppsAuthApiExtension.Companion.hmppsAuth
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.integration.wiremock.stubs.ArnsApiStubs
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.integration.wiremock.stubs.GovUkApiStubs
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.integration.wiremock.stubs.NDeliusApiStubs
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.integration.wiremock.stubs.OasysApiStubs
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ReferralRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ReferralStatusDescriptionRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service.ReferralService
import java.util.UUID

/**
 * Test helper class for creating referral entities in integration tests.
 *
 * This helper creates referrals by directly calling the referral service:
 * - Creating FindAndReferReferralDetails with the specified configuration
 * - Stubbing external API responses (OASys, NDelius, HMPPS Auth)
 * - Calling referralService.createReferral() to persist the entity
 *
 * Usage:
 * ```
 * // Create a single referral
 * val referral = testReferralHelper.createReferral(reportingPdu = "PDU 1")
 *
 * // Create multiple referrals
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
@Import(OasysApiStubs::class, NDeliusApiStubs::class, ArnsApiStubs::class, GovUkApiStubs::class)
class TestReferralHelper {

  @Autowired
  private lateinit var referralService: ReferralService

  @Autowired
  private lateinit var oasysApiStubs: OasysApiStubs

  @Autowired
  private lateinit var nDeliusApiStubs: NDeliusApiStubs

  @Autowired
  private lateinit var wiremock: WireMockServer

  @Autowired
  private lateinit var referralRepository: ReferralRepository

  @Autowired
  private lateinit var referralStatusDescriptionRepository: ReferralStatusDescriptionRepository

  /**
   * Creates a single referral by calling the referral service directly.
   *
   * This method:
   * 1. Creates a FindAndReferReferralDetails object with the specified configuration
   * 2. Stubs all required external API responses
   * 3. Calls referralService.createReferral() to persist the entity
   * 4. Returns the created referral entity
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
    personName: String = randomFullName().getNameAsString(),
    referralId: UUID = UUID.randomUUID(),
    sourcedFrom: ReferralEntitySourcedFrom = ReferralEntitySourcedFrom.LICENCE_CONDITION,
    reportingPdu: String = "PDU 1",
    reportingTeam: String = "Team A",
    sex: String = "Male",
    cohort: OffenceCohort = OffenceCohort.GENERAL_OFFENCE,
  ): ReferralEntity {
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

    // Stub PNI response based on cohort
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

    // Stub sentence information
    nDeliusApiStubs.stubSuccessfulSentenceInformationResponse(
      crn = crn,
      eventNumber = findAndReferReferralDetails.eventNumber,
      sourcedFrom = sourcedFrom,
    )

    // Stub personal details
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

    // Stub auth token
    hmppsAuth.stubGrantToken()

    return referralService.createReferral(findAndReferReferralDetails)
  }

  /**
   * Creates multiple referrals efficiently.
   *
   * Example:
   * ```
   * // Create 5 referrals with default config (each gets unique CRN, referralId, etc.)
   * val referrals = createReferrals(count = 5)
   *
   * // Create 3 referrals with custom PDU (each gets unique CRN, referralId, etc.)
   * val referrals = createReferrals(
   *   count = 3,
   *   configTemplate = ReferralConfigTemplate(reportingPdu = "London PDU")
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
   * @param count Number of referrals to create. Ignored if referralConfigs is provided.
   * @param configTemplate Template configuration for common values. Fresh IDs/CRNs generated per referral.
   * @param referralConfigs List of specific referral configurations. Takes precedence over count.
   * @return List of created [ReferralEntity] objects
   */
  fun createReferrals(
    count: Int = 1,
    configTemplate: ReferralConfigTemplate = ReferralConfigTemplate(),
    referralConfigs: List<ReferralConfig>? = null,
  ): List<ReferralEntity> {
    val configs = referralConfigs ?: List(count) {
      ReferralConfig(
        crn = randomCrn(),
        personName = randomFullName().toString(),
        referralId = UUID.randomUUID(),
        sourcedFrom = configTemplate.sourcedFrom,
        reportingPdu = configTemplate.reportingPdu,
        reportingTeam = configTemplate.reportingTeam,
        sex = configTemplate.sex,
        cohort = configTemplate.cohort,
      )
    }

    return configs.map { config ->
      createReferral(
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
   * Configuration for creating a referral.
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

  /**
   * Template configuration for creating multiple referrals with shared values.
   * Each referral will get unique CRN, personName, and referralId values.
   *
   * @property sourcedFrom The source of the referral. Defaults to LICENCE_CONDITION.
   * @property reportingPdu The reporting Probation Delivery Unit name. Defaults to "PDU 1".
   * @property reportingTeam The reporting team name. Defaults to "Team A".
   * @property sex The sex of the person. Defaults to "Male".
   * @property cohort The offence cohort for the referral. Defaults to GENERAL_OFFENCE.
   */
  data class ReferralConfigTemplate(
    val sourcedFrom: ReferralEntitySourcedFrom = ReferralEntitySourcedFrom.LICENCE_CONDITION,
    val reportingPdu: String = "PDU 1",
    val reportingTeam: String = "Team A",
    val sex: String = "Male",
    val cohort: OffenceCohort = OffenceCohort.GENERAL_OFFENCE,
  )
}
