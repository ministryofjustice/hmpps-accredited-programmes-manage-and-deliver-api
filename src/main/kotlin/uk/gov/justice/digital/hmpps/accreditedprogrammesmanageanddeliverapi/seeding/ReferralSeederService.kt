package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.seeding

import net.datafaker.Faker
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.OffenceCohort
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ReferralEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ReferralEntitySourcedFrom
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ReferralReportingLocationEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ReferralStatusDescriptionEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ReferralStatusHistoryEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.type.InterventionType
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.type.SettingType
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ReferralReportingLocationRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ReferralRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ReferralStatusDescriptionRepository
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.Random

@Service
@Profile("seeding")
class ReferralSeederService(
  private val fakePersonGenerator: FakePersonGenerator,
  private val wiremockStubGenerator: WiremockStubGenerator,
  private val referralRepository: ReferralRepository,
  private val referralReportingLocationRepository: ReferralReportingLocationRepository,
  private val referralStatusDescriptionRepository: ReferralStatusDescriptionRepository,
) {

  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }

  @Transactional
  fun seedReferrals(count: Int): SeedingResult {
    val createdReferrals = mutableListOf<SeededReferralInfo>()

    val statusDescription = referralStatusDescriptionRepository
      .getAwaitingAssessmentStatusDescription()

    repeat(count) { _ ->
      val crn = fakePersonGenerator.generateCrn()
      val seed = crn.hashCode().toLong()
      val faker = Faker(Random(seed))

      val person = fakePersonGenerator.generatePerson(crn)
      val requirementId = generateRequirementId(faker)
      val sourcedFrom = generateSourcedFrom(faker)
      val sentenceEndDate = generateSentenceEndDate(faker)

      wiremockStubGenerator.generateStub(person)

      val referral = createReferralEntity(crn, requirementId, person, statusDescription, sourcedFrom, sentenceEndDate)

      createdReferrals.add(
        SeededReferralInfo(
          referralId = referral.id.toString(),
          crn = crn,
          requirementId = requirementId,
          personName = "${person.firstName} ${person.lastName}",
        ),
      )
    }

    return SeedingResult(
      count = createdReferrals.size,
      referrals = createdReferrals,
    )
  }

  private fun generateRequirementId(faker: Faker): String = "5" + faker.number().digits(7)

  private fun generateSourcedFrom(faker: Faker): ReferralEntitySourcedFrom = if (faker.bool().bool()) {
    ReferralEntitySourcedFrom.LICENCE_CONDITION
  } else {
    ReferralEntitySourcedFrom.REQUIREMENT
  }

  private fun generateSentenceEndDate(faker: Faker): LocalDate {
    val now = LocalDateTime.now()
    val sentenceDuration = faker.number().numberBetween(12, 48)
    return now.plusMonths(sentenceDuration.toLong()).toLocalDate()
  }

  private fun createReferralEntity(
    crn: String,
    requirementId: String,
    person: FakePerson,
    statusDescription: ReferralStatusDescriptionEntity,
    sourcedFrom: ReferralEntitySourcedFrom,
    sentenceEndDate: LocalDate? = null,
  ): ReferralEntity {
    val referral = ReferralEntity(
      crn = crn,
      personName = "${person.firstName} ${person.lastName}",
      interventionType = InterventionType.ACP,
      interventionName = "Building Choices",
      setting = SettingType.COMMUNITY,
      cohort = OffenceCohort.GENERAL_OFFENCE,
      createdAt = LocalDateTime.now(),
      sex = person.sexCode,
      dateOfBirth = person.dateOfBirth,
      eventId = requirementId,
      eventNumber = 1,
      sourcedFrom = sourcedFrom,
      sentenceEndDate = sentenceEndDate,
    )

    val statusHistory = ReferralStatusHistoryEntity(
      referral = referral,
      referralStatusDescription = statusDescription,
      createdBy = "SEEDING_SYSTEM",
      createdAt = LocalDateTime.now(),
      startDate = LocalDateTime.now(),
    )

    val reportingLocation = ReferralReportingLocationEntity(
      referral = referral,
      regionName = "Wiremocked Region Name",
      pduName = "Wiremocked PDU",
      reportingTeam = "Wiremocked Team",
    )

    referral.statusHistories.add(statusHistory)

    val savedReferral = referralRepository.save(referral)
    referralReportingLocationRepository.save(reportingLocation)
    return savedReferral
  }

  /**
   * DANGER: Deletes ALL referrals from the database.
   *
   * This method is intentionally destructive and will remove all referral data.
   * It is only available when the 'seeding' profile is active, which must NEVER
   * be enabled in production or pre-production environments.
   */
  @Transactional
  fun dangerouslyDeleteAllReferrals(): TeardownResult {
    val count = referralRepository.count()
    referralRepository.deleteAll()
    log.warn("DANGER: Deleted ALL $count referrals from the database.")

    wiremockStubGenerator.clearSeededStubs()

    return TeardownResult(deletedCount = count.toInt())
  }
}

data class SeedingResult(
  val count: Int,
  val referrals: List<SeededReferralInfo>,
)

data class SeededReferralInfo(
  val referralId: String,
  val crn: String,
  val personName: String,
  val requirementId: String,
)

data class TeardownResult(
  val deletedCount: Int,
)
