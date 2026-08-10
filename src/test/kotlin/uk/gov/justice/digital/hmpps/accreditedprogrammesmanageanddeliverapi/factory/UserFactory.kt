package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory

import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common.randomSentence
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.model.User

class UserFactory {
  private var username: String = randomSentence(wordRange = 1..3)
  private var active: Boolean = true
  private var name: String = randomSentence(wordRange = 1..3)

  fun withUsername(username: String) = apply { this.username = username }
  fun withActive(active: Boolean) = apply { this.active = active }
  fun withName(name: String) = apply { this.name = name }

  fun produce() = User(
    username = this.username,
    active = this.active,
    name = this.name,
  )
}
