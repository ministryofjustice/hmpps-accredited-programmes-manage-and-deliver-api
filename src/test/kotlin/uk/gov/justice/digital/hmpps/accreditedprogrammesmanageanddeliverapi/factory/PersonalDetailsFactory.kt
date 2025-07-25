package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory

import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.CodeDescription
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.FullName
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.PersonalDetails
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.ProbationPractitioner
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common.randomFullName
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common.randomNumber
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common.randomSentence
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common.randomUppercaseString

class PersonalDetailsFactory {
  private var name: FullName = randomFullName()
  private var crn: String = randomUppercaseString()
  private var dateOfBirth: String = randomUppercaseString()
  private var age: String = randomNumber().toString()
  private var sex: CodeDescription = createCodeDescription()
  private var ethnicity: CodeDescription = createCodeDescription()
  private var probationPractitioner: ProbationPractitioner = createProbationPractitioner()
  private var probationDeliveryUnit: CodeDescription = createCodeDescription()

  fun createProbationPractitioner(): ProbationPractitioner = ProbationPractitioner(randomFullName(), randomUppercaseString(2), randomSentence())

  fun createCodeDescription(): CodeDescription = CodeDescription(randomUppercaseString(2), randomSentence())

  fun produce() = PersonalDetails(
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
