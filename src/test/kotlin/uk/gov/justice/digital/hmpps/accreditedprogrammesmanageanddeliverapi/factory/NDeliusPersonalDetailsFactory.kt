package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory

import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.CodeDescription
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.FullName
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.NDeliusPersonalDetails
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.ProbationPractitioner
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common.randomDateOfBirth
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common.randomFullName
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common.randomNumber
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common.randomSentence
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common.randomUppercaseString
import java.time.LocalDate

class NDeliusPersonalDetailsFactory {
  private var name: FullName = randomFullName()
  private var crn: String = randomUppercaseString()
  private var dateOfBirth: String = randomDateOfBirth().toString()
  private var age: String = randomNumber().toString()
  private var sex: CodeDescription = createCodeDescription()
  private var ethnicity: CodeDescription? = createCodeDescription()
  private var probationPractitioner: ProbationPractitioner = createProbationPractitioner()
  private var probationDeliveryUnit: CodeDescription = createCodeDescription()

  fun createProbationPractitioner(): ProbationPractitioner = ProbationPractitioner(randomFullName(), randomUppercaseString(2), randomSentence())

  fun withEthnicity(ethnicity: CodeDescription?) = apply { this.ethnicity = ethnicity }
  fun createCodeDescription(): CodeDescription = CodeDescription(randomUppercaseString(2), randomSentence())
  fun withDateOfBirth(dateOfBirth: LocalDate?) = apply { this.dateOfBirth = dateOfBirth.toString() }
  fun withName(fullName: FullName) = apply { this.name = fullName }

  fun produce() = NDeliusPersonalDetails(
    name = this.name,
    crn = this.crn,
    dateOfBirth = this.dateOfBirth,
    age = this.age,
    sex = this.sex,
    ethnicity = this.ethnicity,
    probationPractitioner = this.probationPractitioner,
    probationDeliveryUnit = this.probationDeliveryUnit,
  )
}
