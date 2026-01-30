package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.seeding

import net.datafaker.Faker
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import java.time.LocalDate
import java.util.Random

@Component
@Profile("seeding")
class FakePersonGenerator {

  fun generatePerson(crn: String): FakePerson {
    // Use CRN hashcode as seed for deterministic generation
    val seed = crn.hashCode().toLong()
    val faker = Faker(Random(seed))

    val firstName = faker.name().firstName()
    val lastName = faker.name().lastName()

    val sexCode = generateSexCode(faker)
    val sexDescription = generateSexDescription(sexCode)

    return FakePerson(
      crn = crn,
      firstName = firstName,
      lastName = lastName,
      dateOfBirth = generateDateOfBirth(faker),
      age = calculateAge(generateDateOfBirth(faker)),
      // These two fields won't match, but that's acceptable for fake data
      ethnicityCode = generateEthnicityCode(faker),
      ethnicityDescription = generateEthnicityDescription(faker),
      sexCode = sexCode,
      sexDescription = sexDescription,
      probationPractitionerForename = faker.name().firstName(),
      probationPractitionerMiddleNames = faker.name().firstName(),
      probationPractitionerSurname = faker.name().lastName(),
      probationPractitionerCode = "PRAC${faker.number().digits(2)}",
      probationPractitionerEmail = faker.internet().emailAddress(),
      probationDeliveryUnitCode = generatePduCode(faker),
      probationDeliveryUnitDescription = generatePduDescription(faker),
      // These are coordinated with the Wiremocks in the UI codebase, to make sure
      // that Referrals appear in the UI - Changing these may cause unexpected
      // behaviour in which Referrals are visible to the REFER_MONITOR_PP user in UI.
      teamCode = "TEAM01",
      teamDescription = "Wiremocked Team",
      regionCode = "wiremocked_region_code",
      regionDescription = "Wiremocked Region Name",
    )
  }

  private fun generateDateOfBirth(faker: Faker): LocalDate {
    val age = faker.number().numberBetween(18, 65)
    return LocalDate.now().minusYears(age.toLong())
      .minusDays(faker.number().numberBetween(0, 365).toLong())
  }

  private fun calculateAge(dateOfBirth: LocalDate): Int = LocalDate.now().year - dateOfBirth.year

  private fun generateEthnicityCode(faker: Faker): String {
    val codes = listOf("W1", "B1", "Z1", "W9", "M1", "B2", "A2", "A1", "B9")
    return codes[faker.number().numberBetween(0, codes.size)]
  }

  private fun generateEthnicityDescription(faker: Faker): String {
    // This is an alphabetically sorted list of possible ten possible "RaceEthnicity" values from
    // a data export
    val descriptions = listOf(
      "Asian or Asian British: Indian",
      "Asian or Asian British: Pakistani",
      "Black or Black British: African",
      "Black or Black British: Caribbean",
      "Black or Black British: Other",
      "Missing",
      "Mixed: White & Black Caribbean",
      "White: British",
      "White: British/English, Welsh, Scottish, Northern Irish",
      "White: Other",
    )
    return descriptions[faker.number().numberBetween(0, descriptions.size)]
  }

  private fun generateSexCode(faker: Faker): String = if (faker.bool().bool()) "M" else "F"

  private fun generateSexDescription(sexCode: String): String = when (sexCode) {
    "M" -> "Male"
    "F" -> "Female"
    else -> "Unknown"
  }

  private fun generatePduCode(faker: Faker): String {
    val codes = listOf(
      "PDU01",
      "PDU02",
      "PDU03",
      "PDU04",
      "PDU05",
    )
    return codes[faker.number().numberBetween(0, codes.size)]
  }

  private fun generatePduDescription(faker: Faker): String {
    val cities = listOf(
      "Seeded PDU - North",
      "Seeded PDU - South",
      "Seeded PDU - East",
      "Seeded PDU - West",
    )
    return cities[faker.number().numberBetween(0, cities.size)]
  }

  fun generateCrn(): String {
    // 'S' for "Seeded" - helps make it clear these are fake CRNs
    return "S" + Faker().number().digits(9)
  }
}

data class FakePerson(
  val crn: String,
  val firstName: String,
  val lastName: String,
  val dateOfBirth: LocalDate,
  val age: Int,
  val ethnicityCode: String,
  val ethnicityDescription: String,
  val sexCode: String,
  val sexDescription: String,
  val probationPractitionerForename: String,
  val probationPractitionerMiddleNames: String,
  val probationPractitionerSurname: String,
  val probationPractitionerCode: String,
  val probationPractitionerEmail: String,
  val probationDeliveryUnitCode: String,
  val probationDeliveryUnitDescription: String,
  val teamCode: String,
  val teamDescription: String,
  val regionCode: String,
  val regionDescription: String,
)
