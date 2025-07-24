package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory

import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.ReferralDetails
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common.randomDateOfBirth
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common.randomNumber
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common.randomSentence
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common.randomUppercaseString
import java.time.LocalDate
import java.util.UUID

class ReferralDetailsFactory {
  private var id: UUID = UUID.randomUUID()
  private var age: String = randomNumber(2).toString()
  private var crn: String = randomUppercaseString(6)
  private var dateOfBirth: LocalDate = randomDateOfBirth()
  private var ethnicity: String = randomUppercaseString(6)
  private var gender: String = randomUppercaseString(4)
  private var personName: String = randomSentence(wordRange = 1..3)
  private var probationDeliveryUnit: String = randomSentence(wordRange = 1..3)
  private var setting: String = "COMMUNITY"

  fun withId(id: UUID) = apply { this.id = id }
  fun withAge(age: String) = apply { this.age = age }
  fun withCrn(crn: String) = apply { this.crn = crn }
  fun withDateOfBirth(dateOfBirth: LocalDate) = apply { this.dateOfBirth = dateOfBirth }
  fun withEthnicity(ethnicity: String) = apply { this.ethnicity = ethnicity }
  fun withGender(gender: String) = apply { this.gender = gender }
  fun withPersonName(personName: String) = apply { this.personName = personName }
  fun withProbationDeliveryUnit(probationDeliveryUnit: String) = apply { this.probationDeliveryUnit = probationDeliveryUnit }

  fun produce() = ReferralDetails(
    id = id,
    age = age,
    crn = crn,
    dateOfBirth = dateOfBirth,
    ethnicity = ethnicity,
    gender = gender,
    personName = personName,
    probationDeliveryUnit = probationDeliveryUnit,
    setting = setting,
  )
}
