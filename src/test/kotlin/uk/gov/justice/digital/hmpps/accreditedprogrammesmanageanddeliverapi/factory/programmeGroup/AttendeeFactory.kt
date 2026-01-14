package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.programmeGroup

import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.AttendeeEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ReferralEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.SessionEntity
import java.util.UUID

class AttendeeFactory {
  private var id: UUID? = null
  private var referral: ReferralEntity? = null
  private var session: SessionEntity? = null

  fun withId(id: UUID?) = apply { this.id = id }
  fun withReferral(referral: ReferralEntity) = apply { this.referral = referral }
  fun withSession(session: SessionEntity) = apply { this.session = session }

  fun produce() = AttendeeEntity(
    id = this.id,
    referral = this.referral!!,
    session = this.session!!,
  )
}
