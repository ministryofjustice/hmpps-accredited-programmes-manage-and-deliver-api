package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory

import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.PersonalDetails
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.type.SettingType
import java.time.LocalDate
import java.util.UUID

class PersonalDetailsFactory {
  private var crn: String = "X" + UUID.randomUUID().toString().take(6).uppercase()

  @Suppress("ktlint:standard:backing-property-naming")
  private var personForename: String = "Original"
  private var personSurname: String = "Name"
  private var personMiddleNames: String? = null
  private var dateOfBirth: LocalDate = LocalDate.of(1980, 1, 1)
  private var ethnicity: String? = "White"
  private var age: String = "44"
  private var gender: String = "Male"
  private var setting: SettingType = SettingType.COMMUNITY
  private var probationDeliveryUnit: String? = "North London PDU"
  private var dateRetrieved: LocalDate = LocalDate.now()

  fun withCrn(crn: String) = apply { this.crn = crn }
  fun withPersonForename(personForename: String) = apply { this.personForename = personForename }
  fun withPersonSurname(personSurname: String) = apply { this.personSurname = personSurname }
  fun withPersonMiddleNames(personMiddleNames: String?) = apply { this.personMiddleNames = personMiddleNames }
  fun withDateOfBirth(dateOfBirth: LocalDate) = apply { this.dateOfBirth = dateOfBirth }
  fun withEthnicity(ethnicity: String?) = apply { this.ethnicity = ethnicity }
  fun withAge(age: String) = apply { this.age = age }
  fun withGender(gender: String) = apply { this.gender = gender }
  fun withSetting(setting: SettingType) = apply { this.setting = setting }
  fun withProbationDeliveryUnit(probationDeliveryUnit: String?) = apply { this.probationDeliveryUnit = probationDeliveryUnit }

  fun withDateRetrieved(dateRetrieved: LocalDate) = apply { this.dateRetrieved = dateRetrieved }

  fun produce() = PersonalDetails(
    crn = crn,
    personForename = personForename,
    personSurname = personSurname,
    personMiddleNames = personMiddleNames,
    dateOfBirth = dateOfBirth,
    ethnicity = ethnicity,
    age = age,
    gender = gender,
    setting = setting,
    probationDeliveryUnit = probationDeliveryUnit,
    dateRetrieved = dateRetrieved,
  )
}
