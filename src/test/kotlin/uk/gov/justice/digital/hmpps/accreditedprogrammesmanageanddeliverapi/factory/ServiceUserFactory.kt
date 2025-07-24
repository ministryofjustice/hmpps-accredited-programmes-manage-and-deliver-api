package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory

import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.ServiceUser
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common.randomDateOfBirth
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common.randomNumber
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common.randomSentence
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common.randomUppercaseString
import java.time.LocalDate

class ServiceUserFactory {
  private var name: String = randomSentence(wordRange = 1..3)
  private var crn: String = randomUppercaseString()
  private var dateOfBirth: LocalDate = randomDateOfBirth()
  private var age: String = randomNumber().toString()
  private var gender: String = randomUppercaseString()
  private var ethnicity: String = randomUppercaseString()
  private var currentPdu: String = randomSentence()

  fun produce() = ServiceUser(
    name = this.name,
    crn = this.crn,
    dateOfBirth = this.dateOfBirth,
    age = this.age,
    gender = this.gender,
    ethnicity = this.name,
    currentPdu = this.currentPdu,
  )
}
